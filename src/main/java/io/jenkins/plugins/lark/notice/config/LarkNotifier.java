package io.jenkins.plugins.lark.notice.config;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.property.LarkNotifierProvider;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
import io.jenkins.plugins.lark.notice.service.NotificationOrchestrator;
import jenkins.tasks.SimpleBuildStep;
import lombok.Getter;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Jenkins Notifier (Post-build Action) that sends notifications to Lark
 * at various stages of a job's lifecycle.
 * <p>
 * This class appears in the "Post-build Actions" dropdown menu of Freestyle jobs,
 * providing a familiar configuration experience for Jenkins users.
 *
 * @author xm.z
 */
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
     *
     * @param run      current build run
     * @param listener task listener
     * @param occasion notice occasion
     */
    private void sendNotification(Run<?, ?> run, TaskListener listener, NoticeOccasionEnum occasion) {
        NotificationOrchestrator.notify(SOURCE, run, listener, occasion, getAvailableLarkNotifierConfigs(), getMessageDispatcher());
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
