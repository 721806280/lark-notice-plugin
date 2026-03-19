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

import java.util.Locale;
import java.util.Set;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;
import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.defaultTitle;
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

        NotificationDispatchExecutor.applyModelTemplateValues(config, model, envVars, Locale.SIMPLIFIED_CHINESE);

        assertEquals(defaultTitle(Locale.SIMPLIFIED_CHINESE), model.getTitle());
        assertEquals("hello" + LF + "world", model.getContent());
    }

    @Test
    public void resolveMessageTextAndBuildMessageModelShouldRespectRawMode() {
        BuildJobModel model = createModel();
        EnvVars envVars = new EnvVars();
        envVars.put("BUILD_REF", "42");

        LarkNotifierConfig markdownConfig = createNotifierConfig(false, "title", "body", "raw-${BUILD_REF}", "");
        NotificationDispatchExecutor.applyModelTemplateValues(markdownConfig, model, envVars, Locale.US);
        String markdownText = NotificationDispatchExecutor.resolveMessageText(
                markdownConfig, model, envVars, RobotType.LARK, Locale.US);
        assertTrue(markdownText.contains("Demo Project"));
        assertTrue(markdownText.contains("body"));

        LarkNotifierConfig rawConfig = createNotifierConfig(true, "title", "body", "raw-${BUILD_REF}", "");
        String rawText = NotificationDispatchExecutor.resolveMessageText(rawConfig, model, envVars, RobotType.LARK, Locale.US);
        assertEquals("raw-42", rawText);

        MessageModel messageModel = NotificationDispatchExecutor.buildMessageModel(
                model, rawConfig, Set.of("u1"), rawText, Locale.US);
        assertEquals(MsgTypeEnum.CARD, messageModel.getType());
        assertEquals(Set.of("u1"), messageModel.getAtUserIds());
        assertEquals("raw-42", messageModel.getText());
    }

    @Test
    public void defaultModeShouldRenderConfiguredLocaleLabels() {
        BuildJobModel model = createModel();
        EnvVars envVars = new EnvVars();
        LarkNotifierConfig config = createNotifierConfig(false, "", "", "", "");

        NotificationDispatchExecutor.applyModelTemplateValues(config, model, envVars, Locale.SIMPLIFIED_CHINESE);
        String markdownText = NotificationDispatchExecutor.resolveMessageText(
                config, model, envVars, RobotType.LARK, Locale.SIMPLIFIED_CHINESE);
        MessageModel messageModel = NotificationDispatchExecutor.buildMessageModel(
                model, config, Set.of(), markdownText, Locale.SIMPLIFIED_CHINESE);

        assertTrue(markdownText.contains("**任务名称**"));
        assertTrue(markdownText.contains("**构建状态**"));
        assertEquals("更改记录", messageModel.getButtons().get(0).getText());
        assertEquals("控制台", messageModel.getButtons().get(1).getText());
    }

    @Test
    public void builtInDefaultTitleShouldFollowLocaleEvenWhenPreviouslySaved() {
        BuildJobModel model = createModel();
        EnvVars envVars = new EnvVars();
        LarkNotifierConfig config = createNotifierConfig(false, "📢 Jenkins 构建通知", "", "", "");

        NotificationDispatchExecutor.applyModelTemplateValues(config, model, envVars, Locale.US);

        assertEquals("📢 Jenkins Build Notice", model.getTitle());
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
