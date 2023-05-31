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
import io.jenkins.plugins.enums.BuildStatusEnum;
import io.jenkins.plugins.enums.MsgTypeEnum;
import io.jenkins.plugins.enums.NoticeOccasionEnum;
import io.jenkins.plugins.model.BuildJobModel;
import io.jenkins.plugins.model.MessageModel;
import io.jenkins.plugins.model.RunUser;
import io.jenkins.plugins.sdk.impl.FeiShuTalkServiceImpl;
import io.jenkins.plugins.sdk.model.SendResult;
import io.jenkins.plugins.sdk.model.entity.support.Button;
import io.jenkins.plugins.tools.JsonUtils;
import io.jenkins.plugins.tools.Logger;
import io.jenkins.plugins.tools.Utils;
import jenkins.model.Jenkins;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

        // 环境变量
        EnvVars envVars = getEnvVars(run, listener);

        // 执行人信息
        RunUser user = Utils.getExecutor(run, listener);
        String executorName = envVars.get("EXECUTOR_NAME", user.getName());
        String executorMobile = envVars.get("EXECUTOR_MOBILE", user.getMobile());

        // 项目信息
        String projectName = job.getFullDisplayName();
        String projectUrl = job.getAbsoluteUrl();

        // 构建信息
        String jobName = run.getDisplayName(), jobUrl = rootPath + run.getUrl(), duration = run.getDurationString();
        // 构建状态
        BuildStatusEnum statusType = noticeOccasion.buildStatus();
        // 设置环境变量
        envVars.put("EXECUTOR_NAME", StringUtils.defaultIfBlank(executorName, ""));
        envVars.put("EXECUTOR_MOBILE", StringUtils.defaultIfBlank(executorMobile, ""));
        envVars.put("PROJECT_NAME", projectName);
        envVars.put("PROJECT_URL", projectUrl);
        envVars.put("JOB_NAME", jobName);
        envVars.put("JOB_URL", jobUrl);
        envVars.put("JOB_DURATION", duration);
        envVars.put("JOB_STATUS", statusType.getLabel());

        // 默认按钮
        List<Button> buttons = Utils.createDefaultButtons(jobUrl);
        List<String> errors = new ArrayList<>();
        List<FeiShuTalkNotifierConfig> notifierConfigs = property.getAvailableNotifierConfigs();

        for (FeiShuTalkNotifierConfig item : notifierConfigs) {
            boolean skipped = skip(listener, noticeOccasion, item);
            if (skipped) {
                continue;
            }

            String robotId = item.getRobotId(), content = item.getContent();
            boolean atAll = item.isAtAll();
            Set<String> atOpenIds = item.resolveAtOpenIds(envVars);

            String text;
            if (item.isRaw()) {
                text = envVars.expand(item.getMessage());
            } else {
                BuildJobModel buildJobModel = BuildJobModel.builder().projectName(projectName)
                        .projectUrl(projectUrl).jobName(jobName).jobUrl(jobUrl)
                        .statusType(statusType).duration(duration).executorName(executorName)
                        .executorMobile(executorMobile).content(envVars.expand(content).replaceAll("\\\\n", "\n"))
                        .build();
                text = buildJobModel.toMarkdown();
            }

            MessageModel messageModel = MessageModel.builder().type(MsgTypeEnum.INTERACTIVE)
                    .atAll(atAll).atOpenIds(atOpenIds).text(text).buttons(buttons)
                    .title(String.format("%s %s %s", "\uD83D\uDCE2", projectName, statusType.getLabel()))
                    .build();

            Logger.log(listener, "当前机器人信息: %s", item.getRobotName());
            Logger.log(listener, "发送的消息详情: %s", JsonUtils.toJsonStr(messageModel));

            SendResult sendResult = service.send(robotId, messageModel);

            if (!sendResult.isOk()) {
                errors.add(sendResult.getMsg());
            }
        }

        if (!errors.isEmpty()) {
            errors.forEach(error -> Logger.error(listener, error));
        }
    }

    /**
     * 获取任务的环境变量
     *
     * @param run      当前运行的任务实例
     * @param listener 任务输出流监听器
     * @return 环境变量键值对
     */
    private EnvVars getEnvVars(Run<?, ?> run, TaskListener listener) {
        EnvVars envVars;
        try {
            envVars = run.getEnvironment(listener);
        } catch (Exception e) {
            envVars = new EnvVars();
            log.error(e);
            Logger.log(listener, "获取 Job 任务的环境变量时发生异常");
            Logger.log(listener, ExceptionUtils.getStackTrace(e));
            Thread.currentThread().interrupt();
        }

        try {
            EnvVars pipelineEnvVars = PipelineEnvContext.get();
            envVars.overrideAll(pipelineEnvVars);
        } catch (Exception e) {
            log.error(e);
            Logger.log(listener, "获取 Pipeline 环境变量时发生异常");
            Logger.log(listener, ExceptionUtils.getStackTrace(e));
        }

        return envVars;
    }

    /**
     * 判断当前机器人是否要跳过当前消息通知类型的发送
     *
     * @param listener       任务输出流监听器
     * @param noticeOccasion 消息通知类型，枚举值为开始、完成或失败
     * @param notifierConfig 当前机器人的配置信息
     * @return 是否要跳过当前发送
     */
    private boolean skip(TaskListener listener, NoticeOccasionEnum noticeOccasion,
                         FeiShuTalkNotifierConfig notifierConfig) {
        String stage = noticeOccasion.name();
        Set<String> noticeOccasions = notifierConfig.getNoticeOccasions();
        if (noticeOccasions.contains(stage)) {
            return false;
        }
        Logger.log(listener, "机器人 %s 已跳过 %s 环节", notifierConfig.getRobotName(), stage);
        return true;
    }
}