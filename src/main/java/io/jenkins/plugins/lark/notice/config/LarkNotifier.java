package io.jenkins.plugins.lark.notice.config;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import io.jenkins.plugins.lark.notice.EnvVarsResolver;
import io.jenkins.plugins.lark.notice.Messages;
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
import jenkins.tasks.SimpleBuildStep;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.DEFAULT_TITLE;
import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;

/**
 * A Jenkins Notifier (Post-build Action) that sends notifications to Lark
 * at various stages of a job's lifecycle.
 * <p>
 * This class appears in the "Post-build Actions" dropdown menu of Freestyle jobs,
 * providing a familiar configuration experience for Jenkins users.
 *
 * @author xm.z
 */
@Log4j
public class LarkNotifier extends Notifier implements SimpleBuildStep, LarkNotifierProvider {

    private static final String SOURCE = "post-build";

    /**
     * Runtime-only dispatcher. Must not be serialized with job config.
     */
    private transient MessageDispatcher messageDispatcher = MessageDispatcher.getInstance();

    @Getter
    private List<LarkNotifierConfig> larkNotifierConfigs;

    @DataBoundConstructor
    public LarkNotifier(List<LarkNotifierConfig> notifierConfigs) {
        this.larkNotifierConfigs = toMutableList(notifierConfigs);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    /**
     * Handles the START notification before the build begins.
     */
    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        sendNotification(build, listener, NoticeOccasionEnum.START);
        return true;
    }

    /**
     * 声明此构建步骤不需要工作空间上下文。
     * 这将引导 Jenkins 调用 perform(Run, EnvVars, TaskListener) 方法，
     * 从而避免 AbstractMethodError。
     */
    @Override
    public boolean requiresWorkspace() {
        return false;
    }

    /**
     * Handles post-build notifications based on build result.
     */
    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull EnvVars env, @NonNull TaskListener listener) throws InterruptedException, IOException {
        NoticeOccasionEnum occasion = NoticeOccasionEnum.getNoticeOccasion(run.getResult());
        sendNotification(run, listener, occasion);
    }

    /**
     * Sends a Lark notification based on the specified occasion.
     */
    private void sendNotification(Run<?, ?> run, TaskListener listener, NoticeOccasionEnum occasion) {
        try {
            List<LarkNotifierConfig> configs = getAvailableLarkNotifierConfigs();
            Job<?, ?> job = run.getParent();
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

        SendResult result = getMessageDispatcher().send(listener, config.getRobotId(), messageModel);
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
     * Returns a stable absolute run URL even when Jenkins root URL is empty.
     */
    private String buildRunUrl(Run<?, ?> run) {
        String rootUrl = Jenkins.get().getRootUrl();
        if (StringUtils.isNotBlank(rootUrl)) {
            return rootUrl + run.getUrl();
        }
        return StringUtils.defaultString(run.getParent().getAbsoluteUrl()) + run.getNumber() + "/";
    }

    /**
     * Returns a lazily initialized dispatcher for runtime message sending.
     */
    private MessageDispatcher getMessageDispatcher() {
        if (messageDispatcher == null) {
            messageDispatcher = MessageDispatcher.getInstance();
        }
        return messageDispatcher;
    }

    private static List<LarkNotifierConfig> toMutableList(List<LarkNotifierConfig> notifierConfigs) {
        return notifierConfigs == null ? null : new ArrayList<>(notifierConfigs);
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.plugin_display_name();
        }

        public List<LarkNotifierConfig> getDefaultNotifierConfigs() {
            return LarkGlobalConfig.getInstance().getRobotConfigs()
                    .stream()
                    .map(LarkNotifierConfig::new)
                    .collect(Collectors.toList());
        }
    }
}
