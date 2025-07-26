package io.jenkins.plugins.lark.notice;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.config.property.LarkBranchJobProperty;
import io.jenkins.plugins.lark.notice.config.property.LarkJobProperty;
import io.jenkins.plugins.lark.notice.config.property.LarkNotifierProvider;
import io.jenkins.plugins.lark.notice.context.PipelineEnvContext;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.model.BuildJobModel;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.model.RunUser;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.tools.Logger;
import jenkins.model.Jenkins;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.DEFAULT_TITLE;
import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;

/**
 * A Jenkins RunListener that sends notifications to Lark at various stages of a job's lifecycle,
 * such as when the job starts, completes, or fails. Notifications can be customized to include
 * build metadata, executor information, and links to the build.
 *
 * @author xm.z
 */
@Log4j
@Extension
public class LarkRunListener extends RunListener<Run<?, ?>> {

    /**
     * The messaging service used to dispatch messages to Lark.
     */
    private final MessageDispatcher messageDispatcher = MessageDispatcher.getInstance();

    /**
     * The root URL of the Jenkins instance, used for constructing links.
     */
    private final String jenkinsRootUrl = Jenkins.get().getRootUrl();

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
        try {
            Job<?, ?> job = run.getParent();

            List<LarkNotifierConfig> configs = getAvailableLarkNotifierConfigs(job);
            if (configs == null || configs.isEmpty()) {
                Logger.log(listener, "No Lark notifier configured for this job. Skipping notification.");
                return;
            }

            RunUser executor = RunUser.getExecutor(run, listener);

            BuildJobModel model = BuildJobModel.builder()
                    .projectName(job.getFullDisplayName())
                    .projectUrl(job.getAbsoluteUrl())
                    .jobName(run.getDisplayName())
                    .jobUrl(jenkinsRootUrl + run.getUrl())
                    .duration(run.getDurationString())
                    .executorName(executor.getName())
                    .executorMobile(executor.getMobile())
                    .executorOpenId(executor.getOpenId())
                    .statusType(occasion.buildStatus())
                    .build();

            EnvVars envVars = EnvVarsResolver.resolveBuildEnvVars(run, listener, model);

            configs.stream()
                    .filter(config -> config.getNoticeOccasions().contains(occasion.name()))
                    .forEach(config -> sendMessageToLark(run, listener, envVars, model, config, executor));
        } catch (Exception e) {
            Logger.log(listener, "Failed to send Lark notification: %s", e.getMessage());
        } finally {
            PipelineEnvContext.reset();
        }
    }

    /**
     * Sends a message to Lark using the given configuration.
     *
     * @param run      the current job run
     * @param listener the task listener for logging
     * @param envVars  the environment variables of the build
     * @param model    the build job model containing metadata
     * @param config   the Lark notifier configuration
     * @param executor the executor of the build
     */
    private void sendMessageToLark(Run<?, ?> run, TaskListener listener, EnvVars envVars,
                                   BuildJobModel model, LarkNotifierConfig config, RunUser executor) {
        RobotType robotType = LarkGlobalConfig.getRobot(config.getRobotId())
                .map(LarkRobotConfig::obtainRobotType)
                .orElseThrow(() -> new IllegalStateException("Robot not found for ID: " + config.getRobotId()));

        Set<String> atUserIds = config.resolveAtUserIds(envVars);

        if (StringUtils.isNotBlank(executor.getOpenId())) {
            atUserIds.add(executor.getOpenId());
        }

        if (StringUtils.isNotBlank(executor.getMobile())) {
            atUserIds.add(executor.getMobile());
        }

        model.setTitle(envVars.expand(StringUtils.defaultIfBlank(config.getTitle(), DEFAULT_TITLE)));
        model.setContent(envVars.expand(config.getContent()).replaceAll("\\\\n", LF));
        String messageText = config.isRaw() ? envVars.expand(config.getMessage()) : model.toMarkdown(robotType);

        MessageModel messageModel = model.messageModelBuilder()
                .atAll(config.isAtAll())
                .atUserIds(atUserIds)
                .text(messageText)
                .build();

        SendResult result = messageDispatcher.send(listener, config.getRobotId(), messageModel);
        if (!result.isOk()) {
            run.setResult(Result.FAILURE);
        }
    }

    /**
     * Retrieves available Lark notifier configurations for the given job.
     *
     * @param job the Jenkins job
     * @return a list of Lark notifier configurations, or an empty list if none found
     */
    private List<LarkNotifierConfig> getAvailableLarkNotifierConfigs(Job<?, ?> job) {
        return Optional.ofNullable(job.getProperty(LarkJobProperty.class))
                .map(LarkNotifierProvider::getAvailableLarkNotifierConfigs)
                .or(() -> Optional.ofNullable(job.getProperty(BranchJobProperty.class))
                        .map(BranchJobProperty::getBranch)
                        .map(branch -> branch.getProperty(LarkBranchJobProperty.class))
                        .map(LarkNotifierProvider::getAvailableLarkNotifierConfigs))
                .orElse(List.of());
    }
}