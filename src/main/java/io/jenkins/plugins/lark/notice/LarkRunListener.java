package io.jenkins.plugins.lark.notice;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.plugins.lark.notice.config.LarkNotifier;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.config.property.LarkBranchJobProperty;
import io.jenkins.plugins.lark.notice.config.property.LarkJobProperty;
import io.jenkins.plugins.lark.notice.config.property.LarkNotifierProvider;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
import io.jenkins.plugins.lark.notice.service.NotificationOrchestrator;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;

import java.util.List;
import java.util.Optional;

/**
 * A Jenkins RunListener that sends notifications to Lark at various stages of a job's lifecycle,
 * such as when the job starts, completes, or fails. Notifications can be customized to include
 * build metadata, executor information, and links to the build.
 *
 * @author xm.z
 */
@Extension
public class LarkRunListener extends RunListener<Run<?, ?>> {

    private static final String SOURCE = "run-listener";

    /**
     * The messaging service used to dispatch messages to Lark.
     */
    private final MessageDispatcher messageDispatcher = MessageDispatcher.getInstance();

    /**
     * Triggered when a job starts.
     * Sends a notification to Lark indicating the job has started.
     *
     * @param run      the current job run
     * @param listener the task listener for logging
     */
    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        sendNotification(run, listener, NoticeOccasionEnum.START);
    }

    /**
     * Triggered when a job completes.
     * Determines the appropriate notification type based on the build result,
     * and sends the message to Lark.
     *
     * @param run      the current job run
     * @param listener the task listener for logging
     */
    @Override
    public void onCompleted(Run<?, ?> run, @NonNull TaskListener listener) {
        Result result = run.getResult();
        sendNotification(run, listener, NoticeOccasionEnum.getNoticeOccasion(result));
    }

    /**
     * Sends a Lark notification based on the specified occasion.
     *
     * @param run      the current job run
     * @param listener the task listener for logging
     * @param occasion the notification occasion (start, success, failure, etc.)
     */
    private void sendNotification(Run<?, ?> run, TaskListener listener, NoticeOccasionEnum occasion) {
        List<LarkNotifierConfig> configs = getAvailableLarkNotifierConfigs(run.getParent());
        NotificationOrchestrator.notify(SOURCE, run, listener, occasion, configs, messageDispatcher);
    }

    /**
     * Retrieves available Lark notifier configurations for the given job.
     *
     * @param job the Jenkins job
     * @return a list of Lark notifier configurations, or an empty list if none found
     */
    private List<LarkNotifierConfig> getAvailableLarkNotifierConfigs(Job<?, ?> job) {
        // If the job has LarkNotifier configured as a Post-build Action, skip RunListener
        // to avoid duplicate notifications.
        if (job instanceof AbstractProject) {
            AbstractProject<?, ?> project = (AbstractProject<?, ?>) job;
            if (project.getPublishersList().get(LarkNotifier.class) != null) {
                return List.of();
            }
        }

        return Optional.ofNullable(job.getProperty(LarkJobProperty.class))
                .map(LarkNotifierProvider::getAvailableLarkNotifierConfigs)
                .or(() -> Optional.ofNullable(job.getProperty(BranchJobProperty.class))
                        .map(BranchJobProperty::getBranch)
                        .map(branch -> branch.getProperty(LarkBranchJobProperty.class))
                        .map(LarkNotifierProvider::getAvailableLarkNotifierConfigs))
                .orElse(List.of());
    }
}
