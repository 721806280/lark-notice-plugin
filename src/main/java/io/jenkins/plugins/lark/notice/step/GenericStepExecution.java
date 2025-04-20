package io.jenkins.plugins.lark.notice.step;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.tools.Logger;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

/**
 * @author xm.z
 */
public class GenericStepExecution<T extends AbstractStep> extends StepExecution {

    private final transient T step;

    public GenericStepExecution(T step, StepContext context) {
        super(context);
        this.step = step;
    }

    @Override
    public boolean start() throws Exception {
        StepContext context = this.getContext();
        Run<?, ?> run = context.get(Run.class);
        EnvVars envVars = context.get(EnvVars.class);
        TaskListener listener = context.get(TaskListener.class);
        try {
            assert run != null;
            assert envVars != null;
            SendResult sendResult = this.step.send(run, envVars, listener);
            if (sendResult.isOk()) {
                context.onSuccess(sendResult.getMsg());
            } else {
                context.onFailure(new Throwable(Logger.format(sendResult.getMsg())));
            }
            return true;
        } catch (Exception e) {
            context.onFailure(e);
            return false;
        }
    }
}