package io.jenkins.plugins.lark.notice.config;

import io.jenkins.plugins.lark.notice.enums.MessageLocaleStrategy;

import java.util.Locale;

/**
 * Resolves the effective locale used by default notification content.
 *
 * @author xm.z
 */
public final class MessageLocaleResolver {

    private MessageLocaleResolver() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Resolves one effective locale for the current notifier.
     *
     * @param notifierConfig notifier configuration, may be {@code null}
     * @return effective locale for default notification rendering
     */
    public static Locale resolve(LarkNotifierConfig notifierConfig) {
        if (notifierConfig == null) {
            return resolve((MessageLocaleStrategy) null);
        }
        return resolveForRobotId(notifierConfig.getRobotId());
    }

    /**
     * Resolves one effective locale for the provided robot strategy.
     *
     * @param robotStrategy robot locale strategy, may be {@code null}
     * @return effective locale for default notification rendering
     */
    public static Locale resolve(MessageLocaleStrategy robotStrategy) {
        return (robotStrategy == null ? MessageLocaleStrategy.SYSTEM_DEFAULT : robotStrategy).toLocale();
    }

    /**
     * Resolves one effective locale for the provided robot configuration.
     *
     * @param robotConfig robot configuration, may be {@code null}
     * @return effective locale for default notification rendering
     */
    public static Locale resolve(LarkRobotConfig robotConfig) {
        return resolve(robotConfig == null ? null : robotConfig.getMessageLocaleStrategy());
    }

    /**
     * Resolves one effective locale for the robot identified by the given id.
     *
     * @param robotId robot identifier, may be {@code null}
     * @return effective locale for default notification rendering
     */
    public static Locale resolveForRobotId(String robotId) {
        MessageLocaleStrategy robotStrategy = LarkGlobalConfig.getRobot(robotId)
                .map(LarkRobotConfig::getMessageLocaleStrategy)
                .orElse(MessageLocaleStrategy.SYSTEM_DEFAULT);
        return resolve(robotStrategy);
    }
}
