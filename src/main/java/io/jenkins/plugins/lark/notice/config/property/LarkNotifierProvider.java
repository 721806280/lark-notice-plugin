package io.jenkins.plugins.lark.notice.config.property;

import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interface providing access to Lark notifier configurations at the job or branch level.
 * Implementing classes are expected to provide their own implementation for retrieving
 * local Lark notifier configurations.
 *
 * @author xm.z
 */
public interface LarkNotifierProvider {

    /**
     * Gets the list of Lark notifier configurations defined at the job or branch level.
     * This method must be implemented by subclasses to return their specific configuration.
     *
     * @return a list of Lark notifier configurations, may be null or empty
     */
    List<LarkNotifierConfig> getLarkNotifierConfigs();

    /**
     * Gets the merged list of notifier configurations combining global settings and local overrides.
     * If a robot ID exists in both global and local configurations, the local one overrides the global one.
     *
     * @return merged notifier configurations
     */
    default List<LarkNotifierConfig> getMergedNotifierConfigs() {
        List<LarkNotifierConfig> localNotifierConfigs = getLarkNotifierConfigs();
        Map<String, LarkNotifierConfig> localConfigByRobotId = new LinkedHashMap<>();
        if (localNotifierConfigs != null) {
            for (LarkNotifierConfig config : localNotifierConfigs) {
                localConfigByRobotId.putIfAbsent(config.getRobotId(), config);
            }
        }

        return LarkGlobalConfig.getInstance().getRobotConfigs()
                .stream()
                .map(robotConfig -> {
                    LarkNotifierConfig newNotifierConfig = new LarkNotifierConfig(robotConfig);
                    LarkNotifierConfig localConfig = localConfigByRobotId.get(robotConfig.getId());
                    if (localConfig != null) {
                        newNotifierConfig.copy(localConfig);
                    }
                    return newNotifierConfig;
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets notifier configurations marked as enabled.
     *
     * @return enabled notifier configurations
     */
    default List<LarkNotifierConfig> getEnabledNotifierConfigs() {
        return getMergedNotifierConfigs().stream()
                .filter(LarkNotifierConfig::isChecked)
                .collect(Collectors.toList());
    }

    /**
     * Gets available notifier configurations.
     * A configuration is considered available if it is both enabled and not disabled.
     *
     * @return available notifier configurations
     */
    default List<LarkNotifierConfig> getAvailableNotifierConfigs() {
        return getMergedNotifierConfigs().stream()
                .filter(config -> config.isChecked() && !config.isDisabled())
                .collect(Collectors.toList());
    }

}
