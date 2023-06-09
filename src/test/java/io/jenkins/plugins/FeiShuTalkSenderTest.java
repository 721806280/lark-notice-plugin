package io.jenkins.plugins;

import hudson.util.Secret;
import io.jenkins.plugins.feishu.notification.config.FeiShuTalkRobotConfig;
import io.jenkins.plugins.feishu.notification.config.FeiShuTalkSecurityPolicyConfig;
import io.jenkins.plugins.feishu.notification.enums.BuildStatusEnum;
import io.jenkins.plugins.feishu.notification.model.BuildJobModel;
import io.jenkins.plugins.feishu.notification.model.MessageModel;
import io.jenkins.plugins.feishu.notification.model.RobotConfigModel;
import io.jenkins.plugins.feishu.notification.sdk.FeiShuTalkSender;
import io.jenkins.plugins.feishu.notification.sdk.impl.DefaultFeiShuTalkSender;
import io.jenkins.plugins.feishu.notification.sdk.model.SendResult;

import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FeiShuTalkSenderTest
 *
 * @author xm.z
 */
public class FeiShuTalkSenderTest {

    /**
     * 飞书机器人的 Webhook 地址。
     * Webhook 是一种通过 HTTP 协议向机器人发送消息的方式。飞书机器人的 Webhook 用于接收来自用户的消息，
     * 并将其转发给相应的机器人处理程序。
     * <p>
     * 示例 Webhook：<a href="https://open.feishu.cn/open-apis/bot/v2/hook/XXX">...</a>
     * 注意：'XXX' 应该替换为你自己的飞书机器人的 Webhook Token。
     */
    private static final String WEBHOOK = "https://open.feishu.cn/open-apis/bot/v2/hook/XXX";

    // @Test
    void testSendFeiShuTalkMessage() {
        // 设置机器人配置
        List<FeiShuTalkSecurityPolicyConfig> securityPolicyConfigs = new ArrayList<>();
        securityPolicyConfigs.add(new FeiShuTalkSecurityPolicyConfig("KEY", "jenkins", ""));
        FeiShuTalkRobotConfig robot = new FeiShuTalkRobotConfig();
        robot.setWebhook(Secret.fromString(WEBHOOK));
        robot.setSecurityPolicyConfigs(securityPolicyConfigs);

        // 构建消息体
        BuildJobModel buildJobModel = BuildJobModel.builder()
                .projectName("欢迎使用飞书机器人插件~")
                .projectUrl("/")
                .jobName("系统配置")
                .jobUrl("/configure")
                .statusType(BuildStatusEnum.SUCCESS)
                .duration("-")
                .executorName("test")
                .executorMobile("test")
                .build();
        MessageModel msg = MessageModel.builder()
                .title("飞书机器人测试成功")
                .text(buildJobModel.toMarkdown())
                .atAll(false)
                .build();

        // 发送消息
        FeiShuTalkSender sender = new DefaultFeiShuTalkSender(RobotConfigModel.of(robot, ProxySelector.of(null)));
        SendResult sendResult = sender.sendText(msg);
        assertTrue(sendResult.isOk());
    }

}
