package io.jenkins.plugins.lark.notice.service;

import hudson.model.AbstractProject;
import hudson.model.Job;
import io.jenkins.plugins.lark.notice.config.LarkNotifier;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.config.property.LarkBranchJobProperty;
import io.jenkins.plugins.lark.notice.config.property.LarkJobProperty;
import io.jenkins.plugins.lark.notice.config.property.LarkNotifierProvider;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;

import java.util.List;
import java.util.Optional;

/**
 * Resolves effective notifier configs for a given Jenkins job context.
 *
 * @author xm.z
 */
public final class NotifierConfigResolver {

    private NotifierConfigResolver() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
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
                .flatMap(NotifierConfigResolver::resolveFromProvider);
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
                .flatMap(NotifierConfigResolver::resolveFromProvider);
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
