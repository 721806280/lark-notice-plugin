package io.jenkins.plugins.lark.notice.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for copying notifier configuration lists.
 *
 * @author xm.z
 */
public final class NotifierConfigListUtils {

    private NotifierConfigListUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Returns a defensive copy of notifier configs while preserving null semantics.
     *
     * @param notifierConfigs source notifier configs
     * @return copied list, or null when source is null
     */
    public static List<LarkNotifierConfig> copyOrNull(List<LarkNotifierConfig> notifierConfigs) {
        return notifierConfigs == null ? null : new ArrayList<>(notifierConfigs);
    }
}
