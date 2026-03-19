package io.jenkins.plugins.lark.notice.service;

import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;

import java.util.List;

/**
 * Immutable dispatch plan containing the prepared context and matched configs.
 */
public final class NotificationDispatchPlan {

    private final BuildNotificationContext context;
    private final List<LarkNotifierConfig> matchedConfigs;

    NotificationDispatchPlan(BuildNotificationContext context, List<LarkNotifierConfig> matchedConfigs) {
        this.context = context;
        this.matchedConfigs = matchedConfigs;
    }

    public BuildNotificationContext getContext() {
        return context;
    }

    public List<LarkNotifierConfig> getMatchedConfigs() {
        return matchedConfigs;
    }
}
