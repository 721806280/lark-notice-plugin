package io.jenkins.plugins.lark.notice.service;

import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.config.MessageLocaleResolver;
import io.jenkins.plugins.lark.notice.context.NoticeEnvVars;
import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.i18n.NoticeI18n;

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

        BuildMessageLineFormatter.BuildMessageLineValues values =
                new BuildMessageLineFormatter.BuildMessageLineValues(
                        NoticeEnvVars.placeholder(NoticeEnvVars.PROJECT_NAME),
                        NoticeEnvVars.placeholder(NoticeEnvVars.PROJECT_URL),
                        NoticeEnvVars.placeholder(NoticeEnvVars.JOB_NAME),
                        NoticeEnvVars.placeholder(NoticeEnvVars.JOB_URL),
                        NoticeEnvVars.placeholder(NoticeEnvVars.JOB_STATUS),
                        BuildStatusEnum.SUCCESS.getColor(),
                        NoticeEnvVars.placeholder(NoticeEnvVars.JOB_DURATION),
                        NoticeEnvVars.placeholder(NoticeEnvVars.EXECUTOR_NAME),
                        contentTemplate
                );

        lines.addAll(BuildMessageLineFormatter.buildBodyLines(locale, robotType, values, false));
        return String.join(separator, lines);
    }

    private static RobotType resolveRobotType(LarkNotifierConfig config) {
        return LarkGlobalConfig.getRobot(config.getRobotId())
                .map(LarkRobotConfig::obtainRobotType)
                .orElse(RobotType.LARK);
    }
}
