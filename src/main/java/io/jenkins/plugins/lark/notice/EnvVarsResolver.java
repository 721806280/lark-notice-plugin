package io.jenkins.plugins.lark.notice;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import io.jenkins.cli.shaded.org.apache.commons.lang.exception.ExceptionUtils;
import io.jenkins.plugins.lark.notice.context.PipelineEnvContext;
import io.jenkins.plugins.lark.notice.model.BuildJobModel;
import io.jenkins.plugins.lark.notice.tools.Logger;

import java.io.IOException;

/**
 * Utility class for resolving and enriching environment variables with build-related information.
 * <p>
 * This class is a static utility and cannot be instantiated.
 * </p>
 *
 * @author xm.z
 */
public final class EnvVarsResolver {

    private EnvVarsResolver() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Resolves the environment variables for the given build and injects build-related information.
     *
     * @param run      the Jenkins build run instance
     * @param listener the task listener for logging
     * @param model    the build job model containing metadata
     * @return enriched environment variables with build information
     */
    public static EnvVars resolveBuildEnvVars(Run<?, ?> run, TaskListener listener, BuildJobModel model) {
        EnvVars envVars = getBuildEnvironment(run, listener);
        injectBuildInfoToEnvVars(envVars, model);
        return envVars;
    }

    /**
     * Retrieves the environment variables from the build context,
     * including variables from the PipelineEnvContext.
     *
     * @param run      the Jenkins build run instance
     * @param listener the task listener for logging
     * @return environment variables of the build
     */
    private static EnvVars getBuildEnvironment(Run<?, ?> run, TaskListener listener) {
        EnvVars envVars = new EnvVars();
        try {
            envVars = run.getEnvironment(listener);
            envVars.overrideAll(PipelineEnvContext.get());
        } catch (IOException | InterruptedException e) {
            Logger.log(listener, "Failed to retrieve environment variables for the job: %s", e.getMessage());
            Logger.log(listener, ExceptionUtils.getStackTrace(e));
        }
        return envVars;
    }

    /**
     * Injects build-related metadata into the environment variables.
     * These variables can be used in message templates or scripts.
     *
     * @param envVars the environment variables to enrich
     * @param model   the build job model containing metadata
     */
    private static void injectBuildInfoToEnvVars(EnvVars envVars, BuildJobModel model) {
        envVars.put("EXECUTOR_NAME", StringUtils.defaultIfBlank(model.getExecutorName(), ""));
        envVars.put("EXECUTOR_MOBILE", StringUtils.defaultIfBlank(model.getExecutorMobile(), ""));
        envVars.put("EXECUTOR_OPENID", StringUtils.defaultIfBlank(model.getExecutorOpenId(), ""));
        envVars.put("PROJECT_NAME", model.getProjectName());
        envVars.put("PROJECT_URL", model.getProjectUrl());
        envVars.put("JOB_NAME", model.getJobName());
        envVars.put("JOB_URL", model.getJobUrl());
        envVars.put("JOB_DURATION", model.getDuration());
        envVars.put("JOB_STATUS", model.getStatusType().getLabel());
    }
}
