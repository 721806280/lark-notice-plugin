package io.jenkins.plugins.lark.notice.service;

import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import org.junit.Test;

import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link NotificationTemplateService}.
 */
public class NotificationTemplateServiceTest {

    @Test
    public void defaultTemplateShouldContainEditableVariables() {
        LarkNotifierConfig config = createConfig(false, "${JOB_NAME} build", "extra: ${JOB_STATUS}", "");

        String template = NotificationTemplateService.buildEditableDefaultTemplate(
                config, Locale.SIMPLIFIED_CHINESE, RobotType.LARK);

        assertTrue(template.contains("${PROJECT_NAME}"));
        assertTrue(template.contains("${JOB_STATUS}"));
        assertTrue(template.contains("extra: ${JOB_STATUS}"));
        assertTrue(template.contains("**任务名称**"));
    }

    @Test
    public void defaultTemplateShouldRemainLoadableForCustomModeEditing() {
        LarkNotifierConfig config = createConfig(true, "", "", "build: ${JOB_NAME}");

        String template = NotificationTemplateService.buildEditableDefaultTemplate(
                config, Locale.US, RobotType.LARK);

        assertTrue(template.contains("${PROJECT_NAME}"));
        assertTrue(template.contains("${JOB_STATUS}"));
        assertTrue(template.contains("${EXECUTOR_NAME}"));
    }

    private static LarkNotifierConfig createConfig(boolean raw, String title, String content, String message) {
        return new LarkNotifierConfig(
                raw,
                false,
                true,
                "robot-preview",
                "Robot Preview",
                false,
                "",
                title,
                content,
                message,
                Set.of("SUCCESS")
        );
    }
}
