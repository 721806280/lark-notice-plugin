package io.jenkins.plugins.lark.notice.sdk;

import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.model.RobotConfigModel;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves and caches {@link MessageSender} instances by robot id.
 */
public class MessageSenderRegistry {

    private static final MessageSenderRegistry INSTANCE = new MessageSenderRegistry();

    private transient Map<String, MessageSender> senders = new ConcurrentHashMap<>();

    private MessageSenderRegistry() {
        // singleton
    }

    public static MessageSenderRegistry getInstance() {
        return INSTANCE;
    }

    public MessageSender resolve(String robotId) {
        return cache().computeIfAbsent(robotId, this::createSender);
    }

    public Optional<String> findRobotName(String robotId) {
        return LarkGlobalConfig.getRobot(robotId).map(LarkRobotConfig::getName);
    }

    public void clear() {
        cache().clear();
    }

    int cacheSize() {
        return cache().size();
    }

    private MessageSender createSender(String robotId) {
        return LarkGlobalConfig.getRobot(robotId)
                .map(robotConfig -> {
                    RobotType robotType = robotConfig.obtainRobotType();
                    RobotConfigModel robotConfigModel = RobotConfigModel.of(
                            robotConfig,
                            LarkGlobalConfig.getInstance().obtainProxySelector()
                    );
                    return robotType.obtainInstance(robotConfigModel);
                })
                .orElse(null);
    }

    private Map<String, MessageSender> cache() {
        if (senders == null) {
            senders = new ConcurrentHashMap<>(8);
        }
        return senders;
    }
}
