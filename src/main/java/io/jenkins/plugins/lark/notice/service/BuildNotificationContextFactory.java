package io.jenkins.plugins.lark.notice.service;

import hudson.EnvVars;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.EnvVarsResolver;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import io.jenkins.plugins.lark.notice.model.BuildJobModel;
import io.jenkins.plugins.lark.notice.model.RunUser;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;

/**
 * Factory for creating {@link BuildNotificationContext}.
 *
 * @author xm.z
 */
public final class BuildNotificationContextFactory {

    private BuildNotificationContextFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates one notification context for a build run.
     *
     * @param run      build run
     * @param listener task listener
     * @param occasion current notice occasion
     * @return immutable context object used by notification orchestrator
     */
    public static BuildNotificationContext create(Run<?, ?> run, TaskListener listener, NoticeOccasionEnum occasion) {
        Job<?, ?> job = run.getParent();
        RunUser executor = RunUser.getExecutor(run, listener);

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
        return new BuildNotificationContext(executor, model, envVars);
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
