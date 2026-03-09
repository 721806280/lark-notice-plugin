package io.jenkins.plugins.lark.notice.service;

import hudson.EnvVars;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.model.BuildJobModel;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.model.RunUser;
import org.junit.Test;

import java.util.Set;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.DEFAULT_TITLE;
import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link NotificationDispatchExecutor} internal helpers.
 *
 * @author xm.z
 */
public class NotificationDispatchExecutorTest {

    @Test
    public void resolveAtUserIdsShouldMergeConfiguredAndExecutorIdentities() {
        LarkNotifierConfig config = createNotifierConfig(false, "title", "content", "message", "${IDS}");
        RunUser executor = RunUser.builder().name("user").mobile("13900000000").openId("executor-open").build();
        EnvVars envVars = new EnvVars();
        envVars.put("IDS", "config-open,13800000000");

        Set<String> atUserIds = NotificationDispatchExecutor.resolveAtUserIds(config, executor, envVars);

        assertEquals(4, atUserIds.size());
        assertTrue(atUserIds.contains("config-open"));
        assertTrue(atUserIds.contains("13800000000"));
        assertTrue(atUserIds.contains("executor-open"));
        assertTrue(atUserIds.contains("13900000000"));
    }

    @Test
    public void applyModelTemplateValuesShouldExpandVariablesAndFallbackDefaultTitle() {
        LarkNotifierConfig config = createNotifierConfig(false, "", "hello\\n${TARGET}", "message", "");
        BuildJobModel model = createModel();
        EnvVars envVars = new EnvVars();
        envVars.put("TARGET", "world");

        NotificationDispatchExecutor.applyModelTemplateValues(config, model, envVars);

        assertEquals(DEFAULT_TITLE, model.getTitle());
        assertEquals("hello" + LF + "world", model.getContent());
    }

    @Test
    public void resolveMessageTextAndBuildMessageModelShouldRespectRawMode() {
        BuildJobModel model = createModel();
        EnvVars envVars = new EnvVars();
        envVars.put("BUILD_REF", "42");

        LarkNotifierConfig markdownConfig = createNotifierConfig(false, "title", "body", "raw-${BUILD_REF}", "");
        NotificationDispatchExecutor.applyModelTemplateValues(markdownConfig, model, envVars);
        String markdownText = NotificationDispatchExecutor.resolveMessageText(
                markdownConfig, model, envVars, RobotType.LARK);
        assertTrue(markdownText.contains("Demo Project"));
        assertTrue(markdownText.contains("body"));

        LarkNotifierConfig rawConfig = createNotifierConfig(true, "title", "body", "raw-${BUILD_REF}", "");
        String rawText = NotificationDispatchExecutor.resolveMessageText(rawConfig, model, envVars, RobotType.LARK);
        assertEquals("raw-42", rawText);

        MessageModel messageModel = NotificationDispatchExecutor.buildMessageModel(
                model, rawConfig, Set.of("u1"), rawText);
        assertEquals(MsgTypeEnum.CARD, messageModel.getType());
        assertEquals(Set.of("u1"), messageModel.getAtUserIds());
        assertEquals("raw-42", messageModel.getText());
    }

    private static BuildJobModel createModel() {
        return BuildJobModel.builder()
                .projectName("Demo Project")
                .projectUrl("https://example.com/project")
                .jobName("#42")
                .jobUrl("https://example.com/job/42")
                .statusType(BuildStatusEnum.SUCCESS)
                .duration("1 sec")
                .executorName("xm.z")
                .build();
    }

    private static LarkNotifierConfig createNotifierConfig(boolean raw, String title, String content,
                                                           String message, String atUserId) {
        return new LarkNotifierConfig(
                raw,
                false,
                true,
                "robot-test",
                "Robot Test",
                false,
                atUserId,
                title,
                content,
                message,
                Set.of("START", "SUCCESS")
        );
    }
}
