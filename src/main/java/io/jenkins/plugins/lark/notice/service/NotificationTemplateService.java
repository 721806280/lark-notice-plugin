package io.jenkins.plugins.lark.notice.service;

import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.config.MessageLocaleResolver;
import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.i18n.NoticeI18n;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;
import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.defaultTitle;

/**
 * Builds editable default templates for notifier configuration pages.
 *
 * @author xm.z
 */
public final class NotificationTemplateService {

    private NotificationTemplateService() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Builds the editable default template for one notifier configuration.
     *
     * @param config notifier configuration from the UI
     * @return editable default template content
     */
    public static String buildEditableDefaultTemplate(LarkNotifierConfig config) {
        Locale locale = MessageLocaleResolver.resolve(config);
        RobotType robotType = resolveRobotType(config);
        return buildEditableDefaultTemplate(config, locale, robotType);
    }

    static String buildEditableDefaultTemplate(LarkNotifierConfig config, Locale locale, RobotType robotType) {
        String configuredTitle = StringUtils.trimToNull(config.getTitle());
        String titleTemplate = configuredTitle == null || NoticeI18n.isBuiltInDefaultTitle(configuredTitle)
                ? defaultTitle(locale)
                : configuredTitle;
        String contentTemplate = StringUtils.defaultString(config.getContent()).replaceAll("\\\\n", LF);
        String tagName = robotType.getStatusTagName();
        String separator = RobotType.DING_TAlK.equals(robotType) ? "  " + LF : LF;
        List<String> lines = new ArrayList<>();

        if (RobotType.DING_TAlK.equals(robotType)) {
            lines.add(String.format("## <%s color='%s'>%s</%s>",
                    tagName, BuildStatusEnum.SUCCESS.getColor(), titleTemplate, tagName));
            lines.add("---");
        }

        Collections.addAll(lines,
                String.format("\uD83D\uDCCB **%s**: [${PROJECT_NAME}](${PROJECT_URL})", NoticeI18n.buildMessageProjectName(locale)),
                String.format("\uD83D\uDD22 **%s**: [${JOB_NAME}](${JOB_URL})", NoticeI18n.buildMessageJobName(locale)),
                String.format("\uD83C\uDF1F **%s**:  <%s color='%s'>${JOB_STATUS}</%s>",
                        NoticeI18n.buildMessageStatus(locale), tagName, BuildStatusEnum.SUCCESS.getColor(), tagName),
                String.format("\uD83D\uDD50 **%s**:  ${JOB_DURATION}", NoticeI18n.buildMessageDuration(locale)),
                String.format("\uD83D\uDC64 **%s**:  ${EXECUTOR_NAME}", NoticeI18n.buildMessageExecutor(locale))
        );
        if (StringUtils.isNotBlank(contentTemplate)) {
            lines.add(contentTemplate);
        }
        return String.join(separator, lines);
    }

    private static RobotType resolveRobotType(LarkNotifierConfig config) {
        return LarkGlobalConfig.getRobot(config.getRobotId())
                .map(LarkRobotConfig::obtainRobotType)
                .orElse(RobotType.LARK);
    }
}
