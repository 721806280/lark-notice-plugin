package io.jenkins.plugins.lark.notice.service;

import hudson.model.Job;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;

import java.util.List;

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
     * If a Freestyle job already configured {@link io.jenkins.plugins.lark.notice.config.LarkNotifier} as Post-build Action,
     * listener-based sending is skipped to avoid duplicated notifications.
     *
     * @param job Jenkins job
     * @return available notifier configs, never {@code null}
     */
    public static List<LarkNotifierConfig> resolveForRunListener(Job<?, ?> job) {
        return NotifierConfigService.resolveForRunListener(job);
    }
}
