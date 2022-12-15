package io.jenkins.plugins;

import hudson.util.Secret;
import io.jenkins.plugins.enums.BuildStatusEnum;
import io.jenkins.plugins.model.BuildJobModel;
import io.jenkins.plugins.model.MessageModel;
import io.jenkins.plugins.sdk.FeiShuTalkSender;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xm.z
 */
public class SdkTest {

    public static void main(String... args) {
        List<FeiShuTalkSecurityPolicyConfig> securityPolicyConfigs = new ArrayList<>();
        securityPolicyConfigs.add(new FeiShuTalkSecurityPolicyConfig("KEY", "jenkins", ""));
        FeiShuTalkRobotConfig robot = new FeiShuTalkRobotConfig();
        robot.setWebhook(Secret.fromString("https://open.feishu.cn/open-apis/bot/v2/hook/xxx"));
        robot.setSecurityPolicyConfigs(securityPolicyConfigs);

        BuildJobModel buildJobModel = BuildJobModel.builder().projectName("欢迎使用飞书机器人插件~").projectUrl("/")
                .jobName("系统配置").jobUrl("/configure").statusType(BuildStatusEnum.SUCCESS)
                .duration("-").executorName("test").executorMobile("test").build();

        MessageModel msg = MessageModel.builder().title("飞书机器人测试成功").text(buildJobModel.toMarkdown())
                .atAll(false).build();

        FeiShuTalkSender sender = new FeiShuTalkSender(robot, null);
        sender.sendText(msg);
    }
}
