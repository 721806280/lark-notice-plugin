package io.jenkins.plugins.lark.notice.service;

import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.context.PipelineEnvContext;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
import io.jenkins.plugins.lark.notice.tools.LogEvent;
import io.jenkins.plugins.lark.notice.tools.Logger;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Shared notification orchestration used by all build triggers.
 *
 * @author xm.z
 */
public final class NotificationOrchestrator {

    private NotificationOrchestrator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Sends notifications for one build occasion with a prepared config list.
     *
     * @param source            logical trigger source, e.g. {@code run-listener} or {@code post-build}
     * @param run               build run
     * @param listener          Jenkins task listener
     * @param occasion          current notice occasion
     * @param configs           available notifier configurations
     * @param messageDispatcher dispatcher used to send built messages
     */
    public static void notify(String source, Run<?, ?> run, TaskListener listener, NoticeOccasionEnum occasion,
                              List<LarkNotifierConfig> configs, MessageDispatcher messageDispatcher) {
        try {
            Job<?, ?> job = run.getParent();
            Logger.event(listener, LogEvent.NOTIFY_PREPARE,
                    "source", source,
                    "job", job.getFullName(),
                    "run", run.getExternalizableId(),
                    "build", run.getNumber(),
                    "occasion", occasion.name(),
                    "configCount", configs.size());
            if (configs.isEmpty()) {
                Logger.log(listener, Messages.notifier_log_no_config());
                return;
            }

            BuildNotificationContext context = BuildNotificationContextFactory.create(run, listener, occasion);
            String executorName = context.executor().getName();
            Logger.event(listener, LogEvent.NOTIFY_EXECUTOR,
                    "source", source,
                    "executor", executorName,
                    "hasMobile", StringUtils.isNotBlank(context.executor().getMobile()),
                    "hasOpenId", StringUtils.isNotBlank(context.executor().getOpenId()));

            List<LarkNotifierConfig> matchedConfigs = configs.stream()
                    .filter(config -> config.getNoticeOccasions().contains(occasion.name()))
                    .toList();

            Logger.event(listener, LogEvent.NOTIFY_MATCH,
                    "source", source,
                    "occasion", occasion.name(),
                    "matchedConfigCount", matchedConfigs.size());

            matchedConfigs.forEach(config -> NotificationDispatchExecutor.dispatch(
                    source, run, listener, occasion, config, context, messageDispatcher));
        } catch (Exception e) {
            Logger.event(listener, LogEvent.NOTIFY_EXCEPTION,
                    "source", source,
                    "run", run.getExternalizableId(),
                    "occasion", occasion.name(),
                    "errorType", e.getClass().getSimpleName(),
                    "error", e.getMessage());
            Logger.log(listener, Messages.notifier_log_send_failed(), e.getMessage());
        } finally {
            PipelineEnvContext.reset();
        }
    }
}
