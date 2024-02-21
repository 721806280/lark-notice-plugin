package io.jenkins.plugins;

import hudson.util.Secret;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.config.LarkSecurityPolicyConfig;
import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.model.BuildJobModel;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.model.RobotConfigModel;
import io.jenkins.plugins.lark.notice.sdk.MessageSender;
import io.jenkins.plugins.lark.notice.sdk.impl.LarkMessageSender;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;

import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * MessageSenderTest
 *
 * @author xm.z
 */
public class MessageSenderTest {

    /**
     * The Webhook URL for the bot.
     * <p>
     * Example Webhook: <a href="https://open.feishu.cn/open-apis/bot/v2/hook/XXX">...</a>
     * Note: 'XXX' should be replaced with your own Lark (Feishu) bot's Webhook Token.
     */
    private static final String WEBHOOK = "https://open.feishu.cn/open-apis/bot/v2/hook/XXX";

    // @Test
    void testSendLarkMessage() {
        // 设置机器人配置
        List<LarkSecurityPolicyConfig> securityPolicyConfigs = new ArrayList<>();
        securityPolicyConfigs.add(new LarkSecurityPolicyConfig("KEY", "jenkins", ""));
        LarkRobotConfig robot = new LarkRobotConfig();
        robot.setWebhook(Secret.fromString(WEBHOOK));
        robot.setSecurityPolicyConfigs(securityPolicyConfigs);

        // 构建消息体
        BuildJobModel buildJobModel = BuildJobModel.builder()
                .projectName("Lark Notice Plugin")
                .projectUrl("/")
                .jobName("System Configuration")
                .jobUrl("/configure")
                .statusType(BuildStatusEnum.SUCCESS)
                .duration("-")
                .executorName("test")
                .executorMobile("15866668888")
                .executorOpenId("test")
                .build();
        MessageModel msg = MessageModel.builder()
                .title("Test Successful")
                .text(buildJobModel.toMarkdown(robot.obtainRobotType()))
                .atAll(false)
                .build();

        // 发送消息
        MessageSender sender = new LarkMessageSender(RobotConfigModel.of(robot, ProxySelector.of(null)));
        SendResult sendResult = sender.sendText(msg);
        assertTrue(sendResult.isOk());
    }

}
