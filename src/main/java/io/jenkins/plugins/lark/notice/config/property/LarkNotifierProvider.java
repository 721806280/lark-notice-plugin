package io.jenkins.plugins.lark.notice.config.property;

import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
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
     * Gets the merged list of Lark notifier configurations combining global settings and job/branch-level overrides.
     * If a robot ID exists in both global and local configurations, the local one will override the global one.
     *
     * @return a list of merged Lark notifier configurations
     */
    default List<LarkNotifierConfig> getAllLarkNotifierConfigs() {
        return LarkGlobalConfig.getInstance().getRobotConfigs()
                .stream()
                .map(robotConfig -> {
                    LarkNotifierConfig newNotifierConfig = new LarkNotifierConfig(robotConfig);
                    if (CollectionUtils.isNotEmpty(getLarkNotifierConfigs())) {
                        getLarkNotifierConfigs().stream()
                                .filter(config -> robotConfig.getId().equals(config.getRobotId()))
                                .findFirst()
                                .ifPresent(newNotifierConfig::copy);
                    }
                    return newNotifierConfig;
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets the list of Lark notifier configurations that are marked as enabled.
     *
     * @return a list of enabled Lark notifier configurations
     */
    default List<LarkNotifierConfig> getEnabledLarkNotifierConfigs() {
        return getAllLarkNotifierConfigs().stream()
                .filter(LarkNotifierConfig::isChecked)
                .collect(Collectors.toList());
    }

    /**
     * Gets the list of available Lark notifier configurations.
     * A configuration is considered available if it is both enabled and not disabled.
     *
     * @return a list of available Lark notifier configurations
     */
    default List<LarkNotifierConfig> getAvailableLarkNotifierConfigs() {
        return getAllLarkNotifierConfigs().stream()
                .filter(config -> config.isChecked() && !config.isDisabled())
                .collect(Collectors.toList());
    }

}