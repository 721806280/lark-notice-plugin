package io.jenkins.plugins.lark.notice;

import hudson.EnvVars;
import hudson.Extension;
import io.jenkins.plugins.lark.notice.context.PipelineEnvContext;
import lombok.extern.slf4j.Slf4j;
import org.jenkinsci.plugins.workflow.flow.StepListener;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.springframework.lang.NonNull;

/**
 * A listener for Jenkins Pipeline steps that merges Pipeline environment variables with the current environment.
 * This class implements StepListener to handle notifications before a new Pipeline step starts.
 * <p>
 * It retrieves the environment variables from the Pipeline context and merges them with the current environment using PipelineEnvContext.
 * Any exceptions that occur during this process are logged using SLF4J.
 *
 * @author xm.z
 */
@Slf4j
@Extension
public class LarkStepListener implements StepListener {

    /**
     * This method is called before a new Pipeline step starts, and it merges Pipeline environment variables with the current environment.
     *
     * @param step    The new Step instance.
     * @param context Step context information.
     */
    @Override
    public void notifyOfNewStep(@NonNull Step step, @NonNull StepContext context) {
        try {
            EnvVars envVars = context.get(EnvVars.class);
            PipelineEnvContext.merge(envVars);
        } catch (Exception e) {
            log.error("[lark] An exception occurred while retrieving environment variables from the pipeline", e);
        }
    }

}