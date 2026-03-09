package io.jenkins.plugins.lark.notice.service;

import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.context.PipelineEnvContext;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import io.jenkins.plugins.lark.notice.logging.NoticeLog;
import io.jenkins.plugins.lark.notice.logging.NoticeLogKey;
import io.jenkins.plugins.lark.notice.logging.NoticeTrace;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
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
            NoticeLog.trace(listener, NoticeTrace.NOTIFICATION_PREPARE,
                    NoticeLog.field(NoticeLogKey.SOURCE, source),
                    NoticeLog.field(NoticeLogKey.JOB, job.getFullName()),
                    NoticeLog.field(NoticeLogKey.RUN, run.getExternalizableId()),
                    NoticeLog.field(NoticeLogKey.BUILD, run.getNumber()),
                    NoticeLog.field(NoticeLogKey.OCCASION, occasion.name()),
                    NoticeLog.field(NoticeLogKey.CONFIG_TOTAL, configs.size()));
            if (configs.isEmpty()) {
                NoticeLog.verbose(listener, Messages.notifier_log_no_config());
                return;
            }

            BuildNotificationContext context = BuildNotificationContextFactory.create(run, listener, occasion);
            String executorName = context.executor().getName();
            NoticeLog.trace(listener, NoticeTrace.NOTIFICATION_EXECUTOR,
                    NoticeLog.field(NoticeLogKey.SOURCE, source),
                    NoticeLog.field(NoticeLogKey.EXECUTOR, executorName),
                    NoticeLog.field(NoticeLogKey.HAS_MOBILE, StringUtils.isNotBlank(context.executor().getMobile())),
                    NoticeLog.field(NoticeLogKey.HAS_OPEN_ID, StringUtils.isNotBlank(context.executor().getOpenId())));

            List<LarkNotifierConfig> matchedConfigs = NotifierOccasionFilter.filterByOccasion(configs, occasion);

            NoticeLog.trace(listener, NoticeTrace.NOTIFICATION_MATCH,
                    NoticeLog.field(NoticeLogKey.SOURCE, source),
                    NoticeLog.field(NoticeLogKey.OCCASION, occasion.name()),
                    NoticeLog.field(NoticeLogKey.MATCHED_CONFIG_TOTAL, matchedConfigs.size()));

            matchedConfigs.forEach(config -> NotificationDispatchExecutor.dispatch(
                    source, run, listener, occasion, config, context, messageDispatcher));
        } catch (Exception e) {
            NoticeLog.trace(listener, NoticeTrace.NOTIFICATION_EXCEPTION,
                    NoticeLog.field(NoticeLogKey.SOURCE, source),
                    NoticeLog.field(NoticeLogKey.RUN, run.getExternalizableId()),
                    NoticeLog.field(NoticeLogKey.OCCASION, occasion.name()),
                    NoticeLog.field(NoticeLogKey.ERROR_TYPE, e.getClass().getSimpleName()),
                    NoticeLog.field(NoticeLogKey.ERROR, e.getMessage()));
            NoticeLog.verbose(listener, Messages.notifier_log_send_failure(), e.getMessage());
        } finally {
            PipelineEnvContext.reset();
        }
    }
}
