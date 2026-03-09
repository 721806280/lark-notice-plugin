package io.jenkins.plugins.lark.notice.logging;

import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;

import java.util.function.Supplier;

/**
 * Provides runtime log settings used by {@link NoticeLog}.
 *
 * @author xm.z
 */
final class NoticeLogSettings {

    private static Supplier<Boolean> verboseResolver = NoticeLogSettings::resolveVerboseFromGlobalConfig;

    private NoticeLogSettings() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Indicates whether verbose logging is currently enabled.
     *
     * @return {@code true} when verbose logging should be emitted
     */
    static boolean isVerboseEnabled() {
        return Boolean.TRUE.equals(verboseResolver.get());
    }

    /**
     * Overrides the verbose resolver for tests or alternate runtime wiring.
     *
     * @param resolver custom verbose resolver; {@code null} resets to the default resolver
     */
    static void useVerboseResolver(Supplier<Boolean> resolver) {
        verboseResolver = resolver == null ? NoticeLogSettings::resolveVerboseFromGlobalConfig : resolver;
    }

    /**
     * Restores the default verbose resolver backed by global configuration.
     */
    static void reset() {
        verboseResolver = NoticeLogSettings::resolveVerboseFromGlobalConfig;
    }

    /**
     * Reads the verbose flag from global plugin configuration.
     *
     * @return {@code true} when global verbose logging is enabled
     */
    private static boolean resolveVerboseFromGlobalConfig() {
        LarkGlobalConfig globalConfig = LarkGlobalConfig.getInstance();
        return globalConfig != null && globalConfig.isVerbose();
    }
}
