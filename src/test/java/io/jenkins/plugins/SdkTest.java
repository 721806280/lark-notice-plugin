package io.jenkins.plugins;

import hudson.util.Secret;
import io.jenkins.plugins.enums.BuildStatusEnum;
import io.jenkins.plugins.model.BuildJobModel;
import io.jenkins.plugins.model.MessageModel;
import io.jenkins.plugins.sdk.FeiShuTalkSender;

import java.util.ArrayList;

/**
 * @author xm.z
 */
public class SdkTest {

    public static void main(String... args) {
        ArrayList<FeiShuTalkSecurityPolicyConfig> securityPolicyConfigs =
                new ArrayList<>();
        securityPolicyConfigs.add(new FeiShuTalkSecurityPolicyConfig("KEY", "jenkins", ""));
        FeiShuTalkRobotConfig robot = new FeiShuTalkRobotConfig();
        robot.setWebhook(
                Secret.fromString(
                        "https://open.feishu.cn/open-apis/bot/v2/hook/xxx"));
        robot.setSecurityPolicyConfigs(securityPolicyConfigs);

        FeiShuTalkSender sender = new FeiShuTalkSender(robot, null);
        String text =
                BuildJobModel.builder()
                        .projectName("欢迎使用飞书机器人插件~")
                        .projectUrl("/")
                        .jobName("系统配置")
                        .jobUrl("/configure")
                        .statusType(BuildStatusEnum.SUCCESS)
                        .duration("-")
                        .executorName("test")
                        .executorMobile("test")
                        .build()
                        .toMarkdown();
        MessageModel msg =
                MessageModel.builder().title("飞书机器人测试成功").text(text).messageUrl("/").atAll(false).build();
        sender.sendText(msg);
    }
}
