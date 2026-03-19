package io.jenkins.plugins.lark.notice.service;

import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;

import java.util.List;

/**
 * Prepares dispatch data used by the orchestrator.
 */
public final class NotificationDispatchPlanner {

    private NotificationDispatchPlanner() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Builds a dispatch plan including the notification context and matched configs.
     *
     * @param run      build run
     * @param listener Jenkins task listener
     * @param occasion current notice occasion
     * @param configs  available notifier configs
     * @return prepared dispatch plan
     */
    public static NotificationDispatchPlan plan(Run<?, ?> run, TaskListener listener, NoticeOccasionEnum occasion,
                                                List<LarkNotifierConfig> configs) {
        BuildNotificationContext context = BuildNotificationContextFactory.create(run, listener, occasion);
        List<LarkNotifierConfig> matchedConfigs = NotifierOccasionFilter.filterByOccasion(configs, occasion);
        return new NotificationDispatchPlan(context, matchedConfigs);
    }
}
