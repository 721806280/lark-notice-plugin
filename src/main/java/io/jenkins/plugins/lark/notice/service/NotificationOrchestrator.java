package io.jenkins.plugins.lark.notice.service;

import hudson.EnvVars;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.EnvVarsResolver;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
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
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.DEFAULT_TITLE;
import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;

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

            RunUser executor = RunUser.getExecutor(run, listener);
            Logger.event(listener, LogEvent.NOTIFY_EXECUTOR,
                    "source", source,
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
                    "source", source,
                    "occasion", occasion.name(),
                    "matchedConfigCount", matchedConfigs.size());

            matchedConfigs.forEach(config -> sendMessageToLark(source, run, listener, envVars, model,
                    config, executor, occasion, messageDispatcher));
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

    /**
     * Builds and sends one final message for a matched configuration.
     *
     * @param source            logical trigger source
     * @param run               build run
     * @param listener          Jenkins task listener
     * @param envVars           resolved environment variables
     * @param model             build model used for rendering message
     * @param config            notifier configuration
     * @param executor          detected build executor
     * @param occasion          current notice occasion
     * @param messageDispatcher dispatcher used to send built messages
     */
    private static void sendMessageToLark(String source, Run<?, ?> run, TaskListener listener, EnvVars envVars,
                                          BuildJobModel model, LarkNotifierConfig config, RunUser executor,
                                          NoticeOccasionEnum occasion, MessageDispatcher messageDispatcher) {
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
                "source", source,
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
                "source", source,
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
                    "source", source,
                    "job", run.getParent().getFullName(),
                    "build", run.getNumber(),
                    "robotId", config.getRobotId());
        }
    }

    /**
     * Builds an absolute run URL. Jenkins root URL may be empty in some local setups.
     *
     * @param run build run
     * @return absolute run URL
     */
    private static String buildRunUrl(Run<?, ?> run) {
        String rootUrl = Jenkins.get().getRootUrl();
        if (StringUtils.isNotBlank(rootUrl)) {
            return rootUrl + run.getUrl();
        }
        return StringUtils.defaultString(run.getParent().getAbsoluteUrl()) + run.getNumber() + "/";
    }
}
