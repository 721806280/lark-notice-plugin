package io.jenkins.plugins.lark.notice.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return notifierConfigs == null ? null : new ArrayList<>(notifierConfigs);
    }

    /**
     * Creates notifier configs from the currently configured global robot list.
     *
     * @return notifier configs initialized from global robot definitions
     */
    public static List<LarkNotifierConfig> fromGlobalRobots() {
        return LarkGlobalConfig.getInstance().getRobotConfigs()
                .stream()
                .map(LarkNotifierConfig::new)
                .collect(Collectors.toList());
    }

    /**
     * Merges globally configured robots with local notifier overrides keyed by robot ID.
     * The first local config for a given robot ID wins to preserve current form submission semantics.
     *
     * @param localNotifierConfigs local notifier configs, may be null
     * @return merged notifier configs derived from the global robot list
     */
    public static List<LarkNotifierConfig> mergeWithGlobalRobots(List<LarkNotifierConfig> localNotifierConfigs) {
        Map<String, LarkNotifierConfig> localConfigByRobotId = indexByRobotId(localNotifierConfigs);
        return LarkGlobalConfig.getInstance().getRobotConfigs()
                .stream()
                .map(robotConfig -> {
                    LarkNotifierConfig mergedConfig = new LarkNotifierConfig(robotConfig);
                    LarkNotifierConfig localConfig = localConfigByRobotId.get(robotConfig.getId());
                    if (localConfig != null) {
                        mergedConfig.copy(localConfig);
                    }
                    return mergedConfig;
                })
                .collect(Collectors.toList());
    }

    /**
     * Filters merged notifier configs down to checked entries.
     *
     * @param notifierConfigs notifier configs to filter
     * @return checked notifier configs
     */
    public static List<LarkNotifierConfig> filterEnabled(List<LarkNotifierConfig> notifierConfigs) {
        return notifierConfigs.stream()
                .filter(LarkNotifierConfig::isChecked)
                .collect(Collectors.toList());
    }

    /**
     * Filters merged notifier configs down to checked and non-disabled entries.
     *
     * @param notifierConfigs notifier configs to filter
     * @return available notifier configs
     */
    public static List<LarkNotifierConfig> filterAvailable(List<LarkNotifierConfig> notifierConfigs) {
        return notifierConfigs.stream()
                .filter(config -> config.isChecked() && !config.isDisabled())
                .collect(Collectors.toList());
    }

    private static Map<String, LarkNotifierConfig> indexByRobotId(List<LarkNotifierConfig> notifierConfigs) {
        Map<String, LarkNotifierConfig> configByRobotId = new LinkedHashMap<>();
        if (notifierConfigs == null) {
            return configByRobotId;
        }

        for (LarkNotifierConfig config : notifierConfigs) {
            configByRobotId.putIfAbsent(config.getRobotId(), config);
        }
        return configByRobotId;
    }
}
