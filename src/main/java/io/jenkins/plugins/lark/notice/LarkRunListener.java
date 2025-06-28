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
import io.jenkins.plugins.lark.notice.tools.Logger;
import jenkins.model.Jenkins;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.DEFAULT_TITLE;
import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;

/**
 * A listener for Jenkins job run events that sends notifications to Lark at various stages of the job lifecycle.
 * This includes when a job starts, completes, or fails. The notifications are customizable and can include details
 * such as the job name, status, and links to the job console or change log.
 *
 * @author xm.z
 */
@Log4j
@Extension
public class LarkRunListener extends RunListener<Run<?, ?>> {

    /**
     * Instance of the Lark messaging service.
     */
    private final MessageDispatcher service = MessageDispatcher.getInstance();

    /**
     * Root path of the Jenkins instance.
     * This path is used when constructing URLs for sending messages.
     */
    private final String rootPath = Jenkins.get().getRootUrl();

    /**
     * Sends a start notification when a job begins execution.
     *
     * @param run      The current job run instance.
     * @param listener The listener for job output streams.
     */
    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        this.send(run, listener, NoticeOccasionEnum.START);
    }

    /**
     * Sends a completion or failure notification when a job finishes execution.
     * The type of notification depends on the result of the job run.
     *
     * @param run      The current job run instance.
     * @param listener The listener for job output streams.
     */
    @Override
    public void onCompleted(Run<?, ?> run, @NonNull TaskListener listener) {
        Result result = run.getResult();
        NoticeOccasionEnum noticeOccasion = NoticeOccasionEnum.getNoticeOccasion(result);
        try {
            this.send(run, listener, noticeOccasion);
        } catch (Exception e) {
            Logger.log(listener, "发送消息时报错: %s", e);
        } finally {
            PipelineEnvContext.reset();
        }
    }

    /**
     * Sends a Lark message based on the specified occasion (start, completion, failure).
     *
     * @param run            The current job run instance.
     * @param listener       The listener for job output streams.
     * @param noticeOccasion The occasion for the notification (start, complete, fail).
     */
    private void send(Run<?, ?> run, TaskListener listener, NoticeOccasionEnum noticeOccasion) {
        Job<?, ?> job = run.getParent();

        List<LarkNotifierConfig> availableLarkNotifierConfigs = getAvailableLarkNotifierConfigs(job);
        if (availableLarkNotifierConfigs == null || availableLarkNotifierConfigs.isEmpty()) {
            Logger.log(listener, "No Lark notifier configured for this job. Skipping notification.");
            return;
        }

        // 执行人信息
        RunUser user = RunUser.getExecutor(run, listener);

        // 构建任务信息
        BuildJobModel buildJobModel = BuildJobModel.builder()
                // 项目信息
                .projectName(job.getFullDisplayName()).projectUrl(job.getAbsoluteUrl())
                // 构建信息
                .jobName(run.getDisplayName()).jobUrl(rootPath + run.getUrl()).duration(run.getDurationString())
                // 执行人信息
                .executorName(user.getName()).executorMobile(user.getMobile()).executorOpenId(user.getOpenId())
                // 构建状态
                .statusType(noticeOccasion.buildStatus()).build();

        // 获取并更新环境变量
        EnvVars envVars = fetchUpdateEnvVariables(run, listener, buildJobModel);

        // 遍历所有可用的通知器配置
        availableLarkNotifierConfigs.stream()
                // 根据配置决定是否跳过当前项
                .filter(config -> config.getNoticeOccasions().contains(noticeOccasion.name()))
                .forEach(config -> {
                    RobotType robotType = LarkGlobalConfig.getRobot(config.getRobotId())
                            .map(LarkRobotConfig::obtainRobotType).orElseThrow();

                    Set<String> atUserIds = config.resolveAtUserIds(envVars);
                    if (StringUtils.isNotBlank(user.getOpenId())) {
                        atUserIds.add(user.getOpenId());
                    }

                    if (StringUtils.isNotBlank(user.getMobile())) {
                        atUserIds.add(user.getMobile());
                    }

                    buildJobModel.setTitle(envVars.expand(StringUtils.defaultIfBlank(config.getTitle(), DEFAULT_TITLE)));
                    buildJobModel.setContent(envVars.expand(config.getContent()).replaceAll("\\\\n", LF));
                    String text = config.isRaw() ? envVars.expand(config.getMessage()) : buildJobModel.toMarkdown(robotType);

                    MessageModel messageModel = buildJobModel.messageModelBuilder()
                            .atAll(config.isAtAll()).atUserIds(atUserIds).text(text).build();

                    service.send(listener, config.getRobotId(), messageModel);
                });
    }

    private List<LarkNotifierConfig> getAvailableLarkNotifierConfigs(Job<?, ?> job) {
        LarkNotifierProvider larkNotifierProvider;

        if (job instanceof WorkflowJob) {
            larkNotifierProvider = Optional.ofNullable(job.getProperty(BranchJobProperty.class))
                    .map(BranchJobProperty::getBranch)
                    .map(branch -> branch.getProperty(LarkBranchJobProperty.class))
                    .orElse(null);
        } else {
            larkNotifierProvider = job.getProperty(LarkJobProperty.class);
        }

        return larkNotifierProvider == null ? List.of() : larkNotifierProvider.getAvailableLarkNotifierConfigs();
    }

    /**
     * Retrieves the environment variables for a given job run.
     *
     * @param run      The current job run instance.
     * @param listener The listener for job output streams.
     * @return A map of environment variable names to their values.
     */
    private EnvVars getEnvVars(Run<?, ?> run, TaskListener listener) {
        EnvVars envVars = new EnvVars();
        try {
            envVars = run.getEnvironment(listener);
            envVars.overrideAll(PipelineEnvContext.get());
        } catch (IOException | InterruptedException e) {
            Logger.log(listener, "获取 Job 任务的环境变量时发生异常");
            Logger.log(listener, ExceptionUtils.getStackTrace(e));
        }
        return envVars;
    }

    /**
     * Updates environment variables with information from the given Run object and BuildJobModel.
     * This includes executor details, project information, and job status.
     *
     * @param run           The current build run object.
     * @param listener      The task listener for logging and error reporting.
     * @param buildJobModel The model containing build and job information to set in the environment variables.
     * @return The updated environment variables object.
     */
    private EnvVars fetchUpdateEnvVariables(Run<?, ?> run, TaskListener listener, BuildJobModel buildJobModel) {
        EnvVars envVars = getEnvVars(run, listener);
        envVars.put("EXECUTOR_NAME", StringUtils.defaultIfBlank(buildJobModel.getExecutorName(), ""));
        envVars.put("EXECUTOR_MOBILE", StringUtils.defaultIfBlank(buildJobModel.getExecutorMobile(), ""));
        envVars.put("EXECUTOR_OPENID", StringUtils.defaultIfBlank(buildJobModel.getExecutorOpenId(), ""));
        envVars.put("PROJECT_NAME", buildJobModel.getProjectName());
        envVars.put("PROJECT_URL", buildJobModel.getProjectUrl());
        envVars.put("JOB_NAME", buildJobModel.getJobName());
        envVars.put("JOB_URL", buildJobModel.getJobUrl());
        envVars.put("JOB_DURATION", buildJobModel.getDuration());
        envVars.put("JOB_STATUS", buildJobModel.getStatusType().getLabel());
        return envVars;
    }

}