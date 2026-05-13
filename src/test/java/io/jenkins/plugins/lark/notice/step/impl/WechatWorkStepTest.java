package io.jenkins.plugins.lark.notice.step.impl;

import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.enums.MessageLocaleStrategy;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.enums.RobotProtocolType;
import io.jenkins.plugins.lark.notice.enums.WebhookEndpointMode;
import io.jenkins.plugins.lark.notice.i18n.NoticeI18n;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Button;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WechatWorkStepTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void buildMessageShouldPopulateStructuredBuildFieldsForCardMessages() throws Exception {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);

            LarkRobotConfig robot = new LarkRobotConfig("robot-wecom", "WeCom Robot",
                    "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=token", List.of());
            robot.setProtocolType(RobotProtocolType.WECHAT_WORK);
            robot.setEndpointMode(WebhookEndpointMode.FULL_WEBHOOK);
            robot.setMessageLocaleStrategy(MessageLocaleStrategy.EN_US);
            LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(robot)));

            FreeStyleProject project = jenkins.createFreeStyleProject("wecom-pipeline-step");
            FreeStyleBuild build = project.scheduleBuild2(0).get();

            WechatWorkStep step = new WechatWorkStep("robot-wecom", MsgTypeEnum.CARD);
            step.setText(List.of("line one", "line two"));

            Method buildMessage = WechatWorkStep.class.getDeclaredMethod(
                    "buildMessage", hudson.model.Run.class, EnvVars.class, TaskListener.class, String.class);
            buildMessage.setAccessible(true);
            MessageModel message = (MessageModel) buildMessage.invoke(
                    step, build, new EnvVars(), TaskListener.NULL, "robot-wecom");

            assertNotNull(message);
            assertEquals("wecom-pipeline-step", message.getProjectName());
            assertEquals("#1", message.getJobName());
            assertEquals("line one\nline two", message.getText());
            assertEquals("line one\nline two", message.getAdditionalContent());
            assertEquals(Locale.US, message.getLocale());
            assertTrue(message.getJobUrl().endsWith("/job/wecom-pipeline-step/1/"));
            assertNotNull(message.getButtons());
            assertEquals(2, message.getButtons().size());
            assertDefaultButton(message.getButtons().get(0), NoticeI18n.buildMessageButtonChangeLog(Locale.US), "/changes");
            assertDefaultButton(message.getButtons().get(1), NoticeI18n.buildMessageButtonConsole(Locale.US), "/console");
            assertFalse(message.isAtAll());
        } finally {
            Locale.setDefault(previous);
        }
    }

    private static void assertDefaultButton(Button button, String expectedText, String expectedUrlSuffix) {
        assertEquals(expectedText, button.getText());
        assertTrue(button.getUrl().endsWith(expectedUrlSuffix));
    }
}
