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
        MessageLocaleStrategy robotStrategy = notifierConfig == null
                ? MessageLocaleStrategy.SYSTEM_DEFAULT
                : LarkGlobalConfig.getRobot(notifierConfig.getRobotId())
                .map(LarkRobotConfig::getMessageLocaleStrategy)
                .orElse(MessageLocaleStrategy.SYSTEM_DEFAULT);
        return resolve(robotStrategy);
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
}
