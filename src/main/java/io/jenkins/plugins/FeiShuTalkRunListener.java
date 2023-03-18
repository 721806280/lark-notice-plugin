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
import io.jenkins.plugins.sdk.entity.support.Button;
import io.jenkins.plugins.service.impl.FeiShuTalkServiceImpl;
import io.jenkins.plugins.tools.JsonUtils;
import io.jenkins.plugins.tools.Logger;
import io.jenkins.plugins.tools.Utils;
import jenkins.model.Jenkins;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 所有项目触发
 *
 * @author xm.z
 */
@Log4j
@Extension
public class FeiShuTalkRunListener extends RunListener<Run<?, ?>> {

    private final FeiShuTalkServiceImpl service = new FeiShuTalkServiceImpl();

    private final String rootPath = Jenkins.get().getRootUrl();

    @Override
    public void onStarted(Run<?, ?> run, TaskListener listener) {
        this.send(run, listener, NoticeOccasionEnum.START);
    }

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
        BuildStatusEnum statusType = noticeOccasion.buildStatus();
        String jobName = run.getDisplayName();
        String jobUrl = rootPath + run.getUrl();
        String duration = run.getDurationString();
        List<Button> buttons = Utils.createDefaultButtons(jobUrl);
        List<String> result = new ArrayList<>();
        List<FeiShuTalkNotifierConfig> notifierConfigs = property.getAvailableNotifierConfigs();

        for (FeiShuTalkNotifierConfig item : notifierConfigs) {
            boolean skipped = skip(listener, noticeOccasion, item);
            if (skipped) {
                continue;
            }

            String robotId = item.getRobotId();
            String content = item.getContent();
            boolean atAll = item.isAtAll();
            Set<String> atOpenIds = item.resolveAtOpenIds(envVars);

            BuildJobModel buildJobModel = BuildJobModel.builder().projectName(projectName)
                    .projectUrl(projectUrl).jobName(jobName).jobUrl(jobUrl)
                    .statusType(statusType).duration(duration).executorName(executorName)
                    .executorMobile(executorMobile).content(envVars.expand(content).replaceAll("\\\\n", "\n"))
                    .build();

            String statusLabel = statusType == null ? "unknown" : statusType.getLabel();

            MessageModel message = MessageModel.builder().type(MsgTypeEnum.INTERACTIVE)
                    .atAll(atAll).atOpenIds(atOpenIds).title(String.format("%s %s %s", "\uD83D\uDCE2", projectName, statusLabel))
                    .text(buildJobModel.toMarkdown()).buttons(buttons).build();

            Logger.log(listener, "当前机器人信息: %s", item.getRobotName());
            Logger.log(listener, "发送的消息详情: %s", JsonUtils.toJsonStr(message));

            String msg = service.send(robotId, message);

            if (msg != null) {
                result.add(msg);
            }
        }

        if (!result.isEmpty()) {
            result.forEach(msg -> Logger.error(listener, msg));
        }
    }

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
