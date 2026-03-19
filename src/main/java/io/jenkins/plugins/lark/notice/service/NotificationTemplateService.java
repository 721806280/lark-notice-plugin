package io.jenkins.plugins.lark.notice.service;

import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.config.MessageLocaleResolver;
import io.jenkins.plugins.lark.notice.context.NoticeEnvVars;
import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.i18n.NoticeI18n;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;

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
        String titleTemplate = NotificationTemplateTextResolver.resolveTitleTemplate(config.getTitle(), locale);
        String contentTemplate = NotificationTemplateTextResolver.normalizeContent(config.getContent());
        String tagName = robotType.getStatusTagName();
        String separator = RobotType.DING_TAlK.equals(robotType) ? "  " + LF : LF;
        List<String> lines = new ArrayList<>();

        if (RobotType.DING_TAlK.equals(robotType)) {
            lines.add(String.format("## <%s color='%s'>%s</%s>",
                    tagName, BuildStatusEnum.SUCCESS.getColor(), titleTemplate, tagName));
            lines.add("---");
        }

        String projectName = NoticeEnvVars.placeholder(NoticeEnvVars.PROJECT_NAME);
        String projectUrl = NoticeEnvVars.placeholder(NoticeEnvVars.PROJECT_URL);
        String jobName = NoticeEnvVars.placeholder(NoticeEnvVars.JOB_NAME);
        String jobUrl = NoticeEnvVars.placeholder(NoticeEnvVars.JOB_URL);
        String jobStatus = NoticeEnvVars.placeholder(NoticeEnvVars.JOB_STATUS);
        String jobDuration = NoticeEnvVars.placeholder(NoticeEnvVars.JOB_DURATION);
        String executorName = NoticeEnvVars.placeholder(NoticeEnvVars.EXECUTOR_NAME);

        Collections.addAll(lines,
                String.format("\uD83D\uDCCB **%s**: [%s](%s)", NoticeI18n.buildMessageProjectName(locale), projectName, projectUrl),
                String.format("\uD83D\uDD22 **%s**: [%s](%s)", NoticeI18n.buildMessageJobName(locale), jobName, jobUrl),
                String.format("\uD83C\uDF1F **%s**:  <%s color='%s'>%s</%s>",
                        NoticeI18n.buildMessageStatus(locale), tagName, BuildStatusEnum.SUCCESS.getColor(), jobStatus, tagName),
                String.format("\uD83D\uDD50 **%s**:  %s", NoticeI18n.buildMessageDuration(locale), jobDuration),
                String.format("\uD83D\uDC64 **%s**:  %s", NoticeI18n.buildMessageExecutor(locale), executorName)
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
