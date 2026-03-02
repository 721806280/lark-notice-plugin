package io.jenkins.plugins.lark.notice;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifier;
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
import io.jenkins.plugins.lark.notice.tools.LogEvent;
import io.jenkins.plugins.lark.notice.tools.Logger;
import jenkins.model.Jenkins;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
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
        try {
            Job<?, ?> job = run.getParent();

            List<LarkNotifierConfig> configs = getAvailableLarkNotifierConfigs(job);
            Logger.event(listener, LogEvent.NOTIFY_PREPARE,
                    "source", SOURCE,
                    "job", job.getFullName(),
                    "run", run.getExternalizableId(),
                    "build", run.getNumber(),
                    "occasion", occasion.name(),
                    "configCount", configs.size());
            if (configs.isEmpty()) {
                Logger.log(listener, Messages.notifier_log_no_config());
                return;
            }

            RunUser executor = RunUser.getExecutor(run, listener);
            Logger.event(listener, LogEvent.NOTIFY_EXECUTOR,
                    "source", SOURCE,
                    "executor", executor.getName(),
                    "hasMobile", StringUtils.isNotBlank(executor.getMobile()),
                    "hasOpenId", StringUtils.isNotBlank(executor.getOpenId()));

            BuildJobModel model = BuildJobModel.builder()
                    .projectName(job.getFullDisplayName())
                    .projectUrl(job.getAbsoluteUrl())
                    .jobName(run.getDisplayName())
                    .jobUrl(buildRunUrl(run))
                    .duration(run.getDurationString())
                    .executorName(executor.getName())
                    .executorMobile(executor.getMobile())
                    .executorOpenId(executor.getOpenId())
                    .statusType(occasion.buildStatus())
                    .build();

            EnvVars envVars = EnvVarsResolver.resolveBuildEnvVars(run, listener, model);

            List<LarkNotifierConfig> matchedConfigs = configs.stream()
                    .filter(config -> config.getNoticeOccasions().contains(occasion.name()))
                    .toList();

            Logger.event(listener, LogEvent.NOTIFY_MATCH,
                    "source", SOURCE,
                    "occasion", occasion.name(),
                    "matchedConfigCount", matchedConfigs.size());

            matchedConfigs.forEach(config -> sendMessageToLark(run, listener, envVars, model, config, executor, occasion));
        } catch (Exception e) {
            Logger.event(listener, LogEvent.NOTIFY_EXCEPTION,
                    "source", SOURCE,
                    "run", run.getExternalizableId(),
                    "occasion", occasion.name(),
                    "errorType", e.getClass().getSimpleName(),
                    "error", e.getMessage());
            Logger.log(listener, Messages.notifier_log_send_failed(), e.getMessage());
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
     * @param occasion the notification occasion
     */
    private void sendMessageToLark(Run<?, ?> run, TaskListener listener, EnvVars envVars,
                                   BuildJobModel model, LarkNotifierConfig config, RunUser executor,
                                   NoticeOccasionEnum occasion) {
        RobotType robotType = LarkGlobalConfig.getRobot(config.getRobotId())
                .map(LarkRobotConfig::obtainRobotType)
                .orElseThrow(() -> new IllegalStateException(
                        String.format(Messages.notifier_error_robot_not_found(), config.getRobotId())));

        Set<String> atUserIds = config.resolveAtUserIds(envVars);

        if (StringUtils.isNotBlank(executor.getOpenId())) {
            atUserIds.add(executor.getOpenId());
        }

        if (StringUtils.isNotBlank(executor.getMobile())) {
            atUserIds.add(executor.getMobile());
        }

        Logger.event(listener, LogEvent.NOTIFY_DISPATCH,
                "source", SOURCE,
                "job", run.getParent().getFullName(),
                "build", run.getNumber(),
                "occasion", occasion.name(),
                "robotId", config.getRobotId(),
                "robotType", robotType,
                "raw", config.isRaw(),
                "atAll", config.isAtAll(),
                "atUserCount", atUserIds.size());

        model.setTitle(envVars.expand(StringUtils.defaultIfBlank(config.getTitle(), DEFAULT_TITLE)));
        model.setContent(envVars.expand(config.getContent()).replaceAll("\\\\n", LF));
        String messageText = config.isRaw() ? envVars.expand(config.getMessage()) : model.toMarkdown(robotType);

        MessageModel messageModel = model.messageModelBuilder()
                .atAll(config.isAtAll())
                .atUserIds(atUserIds)
                .text(messageText)
                .build();

        SendResult result = messageDispatcher.send(listener, config.getRobotId(), messageModel);
        Logger.event(listener, LogEvent.NOTIFY_RESULT,
                "source", SOURCE,
                "job", run.getParent().getFullName(),
                "build", run.getNumber(),
                "occasion", occasion.name(),
                "robotId", config.getRobotId(),
                "ok", result != null && result.isOk(),
                "code", result == null ? "<null>" : result.getCode(),
                "msg", result == null ? "<null>" : Logger.clip(result.getMsg(), 200));

        if (result == null || !result.isOk()) {
            run.setResult(Result.FAILURE);
            Logger.event(listener, LogEvent.NOTIFY_MARK_BUILD_FAILURE,
                    "source", SOURCE,
                    "job", run.getParent().getFullName(),
                    "build", run.getNumber(),
                    "robotId", config.getRobotId());
        }
    }

    /**
     * Builds an absolute run URL. Jenkins root URL may be empty in some local setups.
     */
    private String buildRunUrl(Run<?, ?> run) {
        String rootUrl = Jenkins.get().getRootUrl();
        if (StringUtils.isNotBlank(rootUrl)) {
            return rootUrl + run.getUrl();
        }
        return StringUtils.defaultString(run.getParent().getAbsoluteUrl()) + run.getNumber() + "/";
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
