package io.jenkins.plugins.lark.notice.logging;

import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;

import java.util.function.Supplier;

/**
 * Provides runtime log settings used by {@link NoticeLog}.
 */
final class NoticeLogSettings {

    private static Supplier<Boolean> verboseResolver = NoticeLogSettings::resolveVerboseFromGlobalConfig;

    private NoticeLogSettings() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    static boolean isVerboseEnabled() {
        return Boolean.TRUE.equals(verboseResolver.get());
    }

    static void useVerboseResolver(Supplier<Boolean> resolver) {
        verboseResolver = resolver == null ? NoticeLogSettings::resolveVerboseFromGlobalConfig : resolver;
    }

    static void reset() {
        verboseResolver = NoticeLogSettings::resolveVerboseFromGlobalConfig;
    }

    private static boolean resolveVerboseFromGlobalConfig() {
        LarkGlobalConfig globalConfig = LarkGlobalConfig.getInstance();
        return globalConfig != null && globalConfig.isVerbose();
    }
}
