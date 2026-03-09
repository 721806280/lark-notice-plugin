package io.jenkins.plugins.lark.notice.step;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.logging.NoticeLog;
import io.jenkins.plugins.lark.notice.logging.NoticeLogKey;
import io.jenkins.plugins.lark.notice.logging.NoticeTrace;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
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
        String stepName = step == null ? "<undefined>" : step.getClass().getSimpleName();
        try {
            if (step == null) {
                context.onFailure(new IllegalStateException(NoticeLog.failureMessage(Messages.pipeline_step_definition_missing())));
                return false;
            }
            if (run == null || envVars == null) {
                context.onFailure(new IllegalStateException(NoticeLog.failureMessage(Messages.pipeline_context_missing())));
                return false;
            }

            NoticeLog.trace(listener, NoticeTrace.PIPELINE_STEP_START,
                    NoticeLog.field(NoticeLogKey.STEP, stepName),
                    NoticeLog.field(NoticeLogKey.RUN, run.getExternalizableId()),
                    NoticeLog.field(NoticeLogKey.ROBOT, step.getRobot()),
                    NoticeLog.field(NoticeLogKey.MESSAGE_TYPE, step.getType()));
            SendResult sendResult = this.step.send(run, envVars, listener);
            if (sendResult == null) {
                context.onFailure(new IllegalStateException(NoticeLog.failureMessage(Messages.dispatcher_error_send_result_missing())));
                return false;
            }
            if (sendResult.isOk()) {
                NoticeLog.trace(listener, NoticeTrace.PIPELINE_STEP_FINISH,
                        NoticeLog.field(NoticeLogKey.STEP, stepName),
                        NoticeLog.field(NoticeLogKey.RUN, run.getExternalizableId()),
                        NoticeLog.field(NoticeLogKey.SUCCESS, true),
                        NoticeLog.field(NoticeLogKey.RESULT_CODE, sendResult.getCode()),
                        NoticeLog.field(NoticeLogKey.MESSAGE, NoticeLog.abbreviate(sendResult.getMsg(), 200)));
                context.onSuccess(sendResult.getMsg());
            } else {
                NoticeLog.trace(listener, NoticeTrace.PIPELINE_STEP_FINISH,
                        NoticeLog.field(NoticeLogKey.STEP, stepName),
                        NoticeLog.field(NoticeLogKey.RUN, run.getExternalizableId()),
                        NoticeLog.field(NoticeLogKey.SUCCESS, false),
                        NoticeLog.field(NoticeLogKey.RESULT_CODE, sendResult.getCode()),
                        NoticeLog.field(NoticeLogKey.MESSAGE, NoticeLog.abbreviate(sendResult.getMsg(), 200)));
                context.onFailure(new IllegalStateException(NoticeLog.failureMessage(sendResult.getMsg())));
            }
            return true;
        } catch (Exception e) {
            NoticeLog.trace(listener, NoticeTrace.PIPELINE_STEP_FAILURE,
                    NoticeLog.field(NoticeLogKey.STEP, stepName),
                    NoticeLog.field(NoticeLogKey.ERROR_TYPE, e.getClass().getSimpleName()),
                    NoticeLog.field(NoticeLogKey.ERROR, e.getMessage()));
            context.onFailure(e);
            return false;
        }
    }
}
