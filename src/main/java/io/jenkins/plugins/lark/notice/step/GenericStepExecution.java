package io.jenkins.plugins.lark.notice.step;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.tools.LogEvent;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.tools.Logger;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

/**
 * Generic pipeline step executor that delegates real sending logic to {@link AbstractStep}.
 *
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
            if (step == null) {
                context.onFailure(new IllegalStateException(Logger.format(Messages.pipeline_step_definition_missing())));
                return false;
            }
            if (run == null || envVars == null) {
                context.onFailure(new IllegalStateException(Logger.format(Messages.pipeline_context_missing())));
                return false;
            }

            Logger.event(listener, LogEvent.PIPELINE_STEP_START,
                    "step", step.getClass().getSimpleName(),
                    "run", run.getExternalizableId(),
                    "robot", step.getRobot(),
                    "msgType", step.getType());
            SendResult sendResult = this.step.send(run, envVars, listener);
            if (sendResult == null) {
                context.onFailure(new IllegalStateException(Logger.format(Messages.dispatcher_error_send_result_null())));
                return false;
            }
            if (sendResult.isOk()) {
                Logger.event(listener, LogEvent.PIPELINE_STEP_END,
                        "step", step.getClass().getSimpleName(),
                        "run", run.getExternalizableId(),
                        "ok", true,
                        "code", sendResult.getCode(),
                        "msg", Logger.clip(sendResult.getMsg(), 200));
                context.onSuccess(sendResult.getMsg());
            } else {
                Logger.event(listener, LogEvent.PIPELINE_STEP_END,
                        "step", step.getClass().getSimpleName(),
                        "run", run.getExternalizableId(),
                        "ok", false,
                        "code", sendResult.getCode(),
                        "msg", Logger.clip(sendResult.getMsg(), 200));
                context.onFailure(new IllegalStateException(Logger.format(sendResult.getMsg())));
            }
            return true;
        } catch (Exception e) {
            Logger.event(listener, LogEvent.PIPELINE_STEP_EXCEPTION,
                    "step", step.getClass().getSimpleName(),
                    "errorType", e.getClass().getSimpleName(),
                    "error", e.getMessage());
            context.onFailure(e);
            return false;
        }
    }
}
