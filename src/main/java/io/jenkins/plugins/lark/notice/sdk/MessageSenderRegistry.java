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
 *
 * @author xm.z
 */
public class MessageSenderRegistry {

    private static final MessageSenderRegistry INSTANCE = new MessageSenderRegistry();

    private transient Map<String, MessageSender> senders = new ConcurrentHashMap<>();

    private MessageSenderRegistry() {
        // singleton
    }

    /**
     * Returns the shared registry instance.
     *
     * @return singleton registry
     */
    public static MessageSenderRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Resolves one sender for the given robot id, creating and caching it on first access.
     *
     * @param robotId target robot id
     * @return cached or newly created sender; {@code null} when the robot does not exist
     */
    public MessageSender resolve(String robotId) {
        return cache().computeIfAbsent(robotId, this::createSender);
    }

    /**
     * Looks up the configured robot display name for logging and diagnostics.
     *
     * @param robotId robot id
     * @return optional robot display name
     */
    public Optional<String> findRobotName(String robotId) {
        return LarkGlobalConfig.getRobot(robotId).map(LarkRobotConfig::getName);
    }

    /**
     * Clears all cached senders so future resolution reflects current configuration.
     */
    public void clear() {
        cache().clear();
    }

    /**
     * Returns the current cache size. Intended for package-level tests.
     *
     * @return number of cached senders
     */
    int cacheSize() {
        return cache().size();
    }

    /**
     * Creates one sender from current global robot and proxy configuration.
     *
     * @param robotId robot id
     * @return sender instance, or {@code null} when no matching robot exists
     */
    private MessageSender createSender(String robotId) {
        return LarkGlobalConfig.getRobot(robotId)
                .map(robotConfig -> {
                    RobotType robotType = robotConfig.obtainRobotType();
                    if (robotType == null) {
                        return null;
                    }
                    RobotConfigModel robotConfigModel = RobotConfigModel.of(
                            robotConfig,
                            LarkGlobalConfig.getInstance().obtainProxySelector()
                    );
                    return robotType.obtainInstance(robotConfigModel);
                })
                .orElse(null);
    }

    /**
     * Returns the internal sender cache, recreating it after deserialization if needed.
     *
     * @return mutable sender cache
     */
    private Map<String, MessageSender> cache() {
        if (senders == null) {
            senders = new ConcurrentHashMap<>(8);
        }
        return senders;
    }
}
