package io.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.plugins.context.PipelineEnvContext;
import io.jenkins.plugins.enums.MsgTypeEnum;
import io.jenkins.plugins.enums.NoticeOccasionEnum;
import io.jenkins.plugins.model.BuildJobModel;
import io.jenkins.plugins.model.MessageModel;
import io.jenkins.plugins.model.RunUser;
import io.jenkins.plugins.sdk.impl.FeiShuTalkServiceImpl;
import io.jenkins.plugins.sdk.model.SendResult;
import io.jenkins.plugins.tools.JsonUtils;
import io.jenkins.plugins.tools.Logger;
import jenkins.model.Jenkins;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;

import static io.jenkins.plugins.sdk.constant.Constants.NOTICE_ICON;

/**
 * FeiShuTalkRunListener 是一个 Jenkins 监听器，它实现了 {@link hudson.model.listeners.RunListener} 接口，
 * 用于在 Jenkins 任务的构建过程中发送飞书消息。该监听器会监控任务的运行状态，并根据配置信息发送不同类型的飞书消息。
 *
 * <p>该类使用 Log4j 记录日志，并且被标记为 Jenkins 的扩展点 (@Extension)，因此可以自动注册到 Jenkins 中。</p>
 *
 * <p>当任务开始运行时，该类会产生一个开始通知，并调用 {@link FeiShuTalkServiceImpl#send(String, MessageModel)} 方法
 * 发送飞书消息。</p>
 *
 * <p>当任务运行完成后，该类会生成一个完成或失败的通知，并调用 {@link FeiShuTalkServiceImpl#send(String, MessageModel)}
 * 方法发送飞书消息。在发送飞书消息时，该类会考虑任务的各种属性，如环境变量、执行人信息、项目信息等，并通过
 * {@link MessageModel} 构造要发送的消息内容。</p>
 *
 * <p>该类会检查任务的配置信息，例如机器人 ID、通知类型、消息模板等，并根据这些信息将飞书消息发送至指定的飞书群组或用户。</p>
 *
 * <p>在发送消息之前，该类会检查当前任务是否配置了机器人，如果未配置，则会跳过消息发送。</p>
 *
 * @author xm.z
 */
@Log4j
@Extension
public class FeiShuTalkRunListener extends RunListener<Run<?, ?>> {

    /**
     * 飞书消息服务实例，用于发送飞书消息
     */
    private final FeiShuTalkServiceImpl service = new FeiShuTalkServiceImpl();

    /**
     * Jenkins 实例的根路径，在发送消息时需要使用该路径拼接任务的 URL
     */
    private final String rootPath = Jenkins.get().getRootUrl();

    /**
     * 当任务开始运行时，发送开始通知的消息
     *
     * @param run      当前运行的任务实例
     * @param listener 任务输出流监听器
     */
    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        this.send(run, listener, NoticeOccasionEnum.START);
    }

    /**
     * 当任务运行完成时，发送完成或失败通知的消息
     *
     * @param run      当前运行的任务实例
     * @param listener 任务输出流监听器
     */
    @Override
    public void onCompleted(Run<?, ?> run, @NonNull TaskListener listener) {
        Result result = run.getResult();
        NoticeOccasionEnum noticeOccasion = NoticeOccasionEnum.getNoticeOccasion(result);
        try {
            this.send(run, listener, noticeOccasion);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(listener, "发送消息时报错: %s", e);
        } finally {
            PipelineEnvContext.reset();
        }
    }

    /**
     * 发送飞书消息
     *
     * @param run            当前运行的任务实例
     * @param listener       任务输出流监听器
     * @param noticeOccasion 消息通知类型，枚举值为开始、完成或失败
     */
    private void send(Run<?, ?> run, TaskListener listener, NoticeOccasionEnum noticeOccasion) {
        Job<?, ?> job = run.getParent();

        FeiShuTalkJobProperty property = job.getProperty(FeiShuTalkJobProperty.class);
        if (property == null) {
            Logger.log(listener, "当前任务未配置机器人，已跳过");
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
                .executorName(user.getName()).executorMobile(user.getMobile())
                // 构建状态
                .statusType(noticeOccasion.buildStatus()).build();

        // 获取并更新环境变量
        EnvVars envVars = fetchUpdateEnvVariables(run, listener, buildJobModel);

        // 遍历所有可用的通知器配置
        property.getAvailableNotifierConfigs().stream()
                // 根据配置决定是否跳过当前项
                .filter(config -> config.getNoticeOccasions().contains(noticeOccasion.name()))
                .forEach(config -> {
                    buildJobModel.setContent(envVars.expand(config.getContent()).replaceAll("\\\\n", "\n"));
                    String text = config.isRaw() ? envVars.expand(config.getMessage()) : buildJobModel.toMarkdown();

                    MessageModel messageModel = MessageModel.builder().type(MsgTypeEnum.INTERACTIVE)
                            .atAll(config.isAtAll()).atOpenIds(config.resolveAtOpenIds(envVars))
                            .text(text).buttons(buildJobModel.createDefaultButtons())
                            .title(String.format("%s %s %s", NOTICE_ICON, buildJobModel.getProjectName(), buildJobModel.getStatusType().getLabel()))
                            .build();

                    Logger.log(listener, "当前机器人信息: %s", config.getRobotName());
                    Logger.log(listener, "发送的消息详情: %s", JsonUtils.toJsonStr(messageModel));

                    SendResult sendResult = service.send(config.getRobotId(), messageModel);
                    if (!sendResult.isOk()) {
                        Logger.error(listener, sendResult.getMsg());
                    }
                });
    }

    /**
     * 获取任务的环境变量
     *
     * @param run      当前运行的任务实例
     * @param listener 任务输出流监听器
     * @return 环境变量键值对
     */
    private EnvVars getEnvVars(Run<?, ?> run, TaskListener listener) {
        EnvVars envVars = new EnvVars();
        try {
            envVars = run.getEnvironment(listener);
            envVars.overrideAll(PipelineEnvContext.get());
        } catch (IOException | InterruptedException e) {
            log.error(e);
            Logger.log(listener, "获取 Job 任务的环境变量时发生异常");
            Logger.log(listener, ExceptionUtils.getStackTrace(e));
        }
        return envVars;
    }

    /**
     * 从给定的 Run 对象和 BuildJobModel 对象中获取信息并更新环境变量。
     *
     * @param run           表示当前构建的 Run 对象。
     * @param listener      用于记录日志和报告错误的 TaskListener 对象。
     * @param buildJobModel 包含要在环境变量中设置的构建与任务信息的 BuildJobModel 对象。
     * @return 更新后的 EnvVars 对象。
     */
    private EnvVars fetchUpdateEnvVariables(Run<?, ?> run, TaskListener listener, BuildJobModel buildJobModel) {
        EnvVars envVars = getEnvVars(run, listener);
        envVars.put("EXECUTOR_NAME", StringUtils.defaultIfBlank(buildJobModel.getExecutorName(), ""));
        envVars.put("EXECUTOR_MOBILE", StringUtils.defaultIfBlank(buildJobModel.getExecutorMobile(), ""));
        envVars.put("PROJECT_NAME", buildJobModel.getProjectName());
        envVars.put("PROJECT_URL", buildJobModel.getProjectUrl());
        envVars.put("JOB_NAME", buildJobModel.getJobName());
        envVars.put("JOB_URL", buildJobModel.getJobUrl());
        envVars.put("JOB_DURATION", buildJobModel.getDuration());
        envVars.put("JOB_STATUS", buildJobModel.getStatusType().getLabel());
        return envVars;
    }

}