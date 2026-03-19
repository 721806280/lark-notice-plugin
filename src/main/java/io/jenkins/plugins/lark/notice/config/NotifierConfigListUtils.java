package io.jenkins.plugins.lark.notice.config;

import io.jenkins.plugins.lark.notice.service.NotifierConfigService;

import java.util.List;

/**
 * Utility methods for copying, merging, and filtering notifier configuration lists.
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
        return NotifierConfigService.copyOrNull(notifierConfigs);
    }

    /**
     * Creates notifier configs from the currently configured global robot list.
     *
     * @return notifier configs initialized from global robot definitions
     */
    public static List<LarkNotifierConfig> fromGlobalRobots() {
        return NotifierConfigService.fromGlobalRobots();
    }

    /**
     * Merges globally configured robots with local notifier overrides keyed by robot ID.
     * The first local config for a given robot ID wins to preserve current form submission semantics.
     *
     * @param localNotifierConfigs local notifier configs, may be null
     * @return merged notifier configs derived from the global robot list
     */
    public static List<LarkNotifierConfig> mergeWithGlobalRobots(List<LarkNotifierConfig> localNotifierConfigs) {
        return NotifierConfigService.mergeWithGlobalRobots(localNotifierConfigs);
    }

    /**
     * Filters merged notifier configs down to checked entries.
     *
     * @param notifierConfigs notifier configs to filter
     * @return checked notifier configs
     */
    public static List<LarkNotifierConfig> filterEnabled(List<LarkNotifierConfig> notifierConfigs) {
        return NotifierConfigService.filterEnabled(notifierConfigs);
    }

    /**
     * Filters merged notifier configs down to checked and non-disabled entries.
     *
     * @param notifierConfigs notifier configs to filter
     * @return available notifier configs
     */
    public static List<LarkNotifierConfig> filterAvailable(List<LarkNotifierConfig> notifierConfigs) {
        return NotifierConfigService.filterAvailable(notifierConfigs);
    }
}
