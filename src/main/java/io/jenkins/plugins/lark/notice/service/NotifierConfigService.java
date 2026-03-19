package io.jenkins.plugins.lark.notice.service;

import hudson.model.AbstractProject;
import hudson.model.Job;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifier;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.config.property.LarkBranchJobProperty;
import io.jenkins.plugins.lark.notice.config.property.LarkJobProperty;
import io.jenkins.plugins.lark.notice.config.property.LarkNotifierProvider;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Centralized helper for resolving notifier configs and list operations.
 *
 * @author xm.z
 */
public final class NotifierConfigService {

    private NotifierConfigService() {
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

    /**
     * Resolves configs used by {@code LarkRunListener}.
     * <p>
     * If a Freestyle job already configured {@link LarkNotifier} as Post-build Action,
     * listener-based sending is skipped to avoid duplicated notifications.
     *
     * @param job Jenkins job
     * @return available notifier configs, never {@code null}
     */
    public static List<LarkNotifierConfig> resolveForRunListener(Job<?, ?> job) {
        if (hasFreestylePostBuildNotifier(job)) {
            return List.of();
        }
        return resolvePreferredPropertyConfigs(job).orElse(List.of());
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

    /**
     * Checks whether the given job already configured {@link LarkNotifier} as a Freestyle Post-build action.
     *
     * @param job Jenkins job
     * @return {@code true} if Post-build notifier exists and listener notifications should be skipped
     */
    private static boolean hasFreestylePostBuildNotifier(Job<?, ?> job) {
        if (job instanceof AbstractProject<?, ?> project) {
            return project.getPublishersList().get(LarkNotifier.class) != null;
        }
        return false;
    }

    /**
     * Resolves available configs with precedence: job property first, then branch property.
     *
     * @param job Jenkins job
     * @return optional resolved configs from preferred property source
     */
    private static Optional<List<LarkNotifierConfig>> resolvePreferredPropertyConfigs(Job<?, ?> job) {
        return resolveFromJobProperty(job)
                .or(() -> resolveFromBranchProperty(job));
    }

    /**
     * Resolves available notifier configs from job property.
     *
     * @param job Jenkins job
     * @return optional config list from {@link LarkJobProperty}
     */
    private static Optional<List<LarkNotifierConfig>> resolveFromJobProperty(Job<?, ?> job) {
        return Optional.ofNullable(job.getProperty(LarkJobProperty.class))
                .flatMap(NotifierConfigService::resolveFromProvider);
    }

    /**
     * Resolves available notifier configs from multibranch branch property.
     *
     * @param job Jenkins job
     * @return optional config list from {@link LarkBranchJobProperty}
     */
    private static Optional<List<LarkNotifierConfig>> resolveFromBranchProperty(Job<?, ?> job) {
        return Optional.ofNullable(job.getProperty(BranchJobProperty.class))
                .map(BranchJobProperty::getBranch)
                .map(branch -> branch.getProperty(LarkBranchJobProperty.class))
                .flatMap(NotifierConfigService::resolveFromProvider);
    }

    /**
     * Resolves available configs from one notifier provider.
     *
     * @param provider notifier provider
     * @return optional available configs
     */
    private static Optional<List<LarkNotifierConfig>> resolveFromProvider(LarkNotifierProvider provider) {
        return Optional.ofNullable(provider)
                .map(LarkNotifierProvider::getAvailableNotifierConfigs);
    }
}
