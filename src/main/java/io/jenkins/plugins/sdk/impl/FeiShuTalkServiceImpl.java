package io.jenkins.plugins.sdk.impl;

import io.jenkins.plugins.FeiShuTalkGlobalConfig;
import io.jenkins.plugins.FeiShuTalkRobotConfig;
import io.jenkins.plugins.enums.MsgTypeEnum;
import io.jenkins.plugins.model.MessageModel;
import io.jenkins.plugins.model.RobotConfigModel;
import io.jenkins.plugins.sdk.FeiShuTalkSender;
import io.jenkins.plugins.sdk.FeiShuTalkService;
import io.jenkins.plugins.sdk.model.SendResult;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 飞书消息发送服务实现类
 *
 * @author xm.z
 */
public class FeiShuTalkServiceImpl implements FeiShuTalkService {

    /**
     * 机器人发送者缓存
     */
    private final Map<String, FeiShuTalkSender> senders = new ConcurrentHashMap<>();

    /**
     * 获取指定机器人的发送者实例，如果不存在，则创建并缓存一个新实例
     *
     * @param robotId 机器人 ID
     * @return 发送者实例
     */
    private FeiShuTalkSender getSender(String robotId) {
        return senders.computeIfAbsent(robotId, id -> {
            FeiShuTalkGlobalConfig globalConfig = FeiShuTalkGlobalConfig.getInstance();
            ArrayList<FeiShuTalkRobotConfig> robotConfigs = globalConfig.getRobotConfigs();
            Optional<FeiShuTalkRobotConfig> robotConfigOptional =
                    robotConfigs.stream().filter(item -> id.equals(item.getId())).findAny();
            if (robotConfigOptional.isPresent()) {
                RobotConfigModel robotConfig = RobotConfigModel.of(robotConfigOptional.get(), globalConfig.obtainProxySelector());
                return new DefaultFeiShuTalkSender(robotConfig);
            }
            return null;
        });
    }

    /**
     * 发送消息给指定机器人
     *
     * @param robotId 机器人 ID
     * @param msg     消息体
     * @return 发送结果
     */
    @Override
    public SendResult send(String robotId, MessageModel msg) {
        FeiShuTalkSender sender = getSender(robotId);
        if (sender == null) {
            return SendResult.fail(String.format("ID 为 %s 的机器人不存在。", robotId));
        }

        MsgTypeEnum type = msg.getType();
        if (type == null) {
            return SendResult.fail("消息类型【type】不能为空");
        }

        return type.send(sender, msg);
    }
}