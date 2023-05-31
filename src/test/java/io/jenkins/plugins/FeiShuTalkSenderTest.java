package io.jenkins.plugins;

import hudson.util.Secret;
import io.jenkins.plugins.enums.BuildStatusEnum;
import io.jenkins.plugins.model.BuildJobModel;
import io.jenkins.plugins.model.MessageModel;
import io.jenkins.plugins.model.RobotConfigModel;
import io.jenkins.plugins.sdk.FeiShuTalkSender;
import io.jenkins.plugins.sdk.impl.DefaultFeiShuTalkSender;
import io.jenkins.plugins.sdk.model.SendResult;
import org.junit.jupiter.api.Test;

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

    @Test
    void testSendFeiShuTalkMessage() {
        // 设置机器人配置
        List<FeiShuTalkSecurityPolicyConfig> securityPolicyConfigs = new ArrayList<>();
        securityPolicyConfigs.add(new FeiShuTalkSecurityPolicyConfig("KEY", "jenkins", ""));
        FeiShuTalkRobotConfig robot = new FeiShuTalkRobotConfig();
        robot.setWebhook(Secret.fromString("https://open.feishu.cn/open-apis/bot/v2/hook/XXX"));
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
