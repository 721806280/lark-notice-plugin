package io.jenkins.plugins.service.impl;

import io.jenkins.plugins.FeiShuTalkGlobalConfig;
import io.jenkins.plugins.FeiShuTalkRobotConfig;
import io.jenkins.plugins.enums.MsgTypeEnum;
import io.jenkins.plugins.model.MessageModel;
import io.jenkins.plugins.sdk.FeiShuTalkSender;
import io.jenkins.plugins.service.FeiShuTalkService;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * service
 *
 * @author xm.z
 */
public class FeiShuTalkServiceImpl implements FeiShuTalkService {

    private final Map<String, FeiShuTalkSender> senders = new ConcurrentHashMap<>();

    private FeiShuTalkSender getSender(String robotId) {
        FeiShuTalkSender sender = senders.get(robotId);
        if (sender == null) {
            FeiShuTalkGlobalConfig globalConfig = FeiShuTalkGlobalConfig.getInstance();
            Proxy proxy = globalConfig.getProxy();
            ArrayList<FeiShuTalkRobotConfig> robotConfigs = globalConfig.getRobotConfigs();
            Optional<FeiShuTalkRobotConfig> robotConfigOptional =
                    robotConfigs.stream().filter(item -> robotId.equals(item.getId())).findAny();
            if (robotConfigOptional.isPresent()) {
                FeiShuTalkRobotConfig robotConfig = robotConfigOptional.get();
                sender = new FeiShuTalkSender(robotConfig, proxy);
                senders.put(robotId, sender);
            }
        }
        return sender;
    }

    @Override
    public String send(String robotId, MessageModel msg) {
        MsgTypeEnum type = msg.getType();
        FeiShuTalkSender sender = getSender(robotId);
        if (sender == null) {
            return String.format("ID 为 %s 的机器人不存在。", robotId);
        }
        if (type == null) {
            return "消息类型【type】不能为空";
        }
        switch (type) {
            case TEXT:
                return sender.sendText(msg);
            case IMAGE:
                return sender.sendImage(msg);
            case SHARE_CHAT:
                return sender.sendShareChat(msg);
            case POST:
                return sender.sendPost(msg);
            case INTERACTIVE:
                return sender.sendInteractive(msg);
            default:
                return String.format("错误的消息类型：%s", type.name());
        }
    }
}
