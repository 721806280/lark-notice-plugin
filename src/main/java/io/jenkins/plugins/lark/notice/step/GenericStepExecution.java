package io.jenkins.plugins.lark.notice.step;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.tools.LogEvent;
import io.jenkins.plugins.lark.notice.tools.LogField;
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
                    LogField.STEP, step.getClass().getSimpleName(),
                    LogField.RUN, run.getExternalizableId(),
                    LogField.ROBOT, step.getRobot(),
                    LogField.MSG_TYPE, step.getType());
            SendResult sendResult = this.step.send(run, envVars, listener);
            if (sendResult == null) {
                context.onFailure(new IllegalStateException(Logger.format(Messages.dispatcher_error_send_result_null())));
                return false;
            }
            if (sendResult.isOk()) {
                Logger.event(listener, LogEvent.PIPELINE_STEP_END,
                        LogField.STEP, step.getClass().getSimpleName(),
                        LogField.RUN, run.getExternalizableId(),
                        LogField.OK, true,
                        LogField.CODE, sendResult.getCode(),
                        LogField.MSG, Logger.clip(sendResult.getMsg(), 200));
                context.onSuccess(sendResult.getMsg());
            } else {
                Logger.event(listener, LogEvent.PIPELINE_STEP_END,
                        LogField.STEP, step.getClass().getSimpleName(),
                        LogField.RUN, run.getExternalizableId(),
                        LogField.OK, false,
                        LogField.CODE, sendResult.getCode(),
                        LogField.MSG, Logger.clip(sendResult.getMsg(), 200));
                context.onFailure(new IllegalStateException(Logger.format(sendResult.getMsg())));
            }
            return true;
        } catch (Exception e) {
            Logger.event(listener, LogEvent.PIPELINE_STEP_EXCEPTION,
                    LogField.STEP, step.getClass().getSimpleName(),
                    LogField.ERROR_TYPE, e.getClass().getSimpleName(),
                    LogField.ERROR, e.getMessage());
            context.onFailure(e);
            return false;
        }
    }
}
