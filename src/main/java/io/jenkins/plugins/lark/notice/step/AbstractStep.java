package io.jenkins.plugins.lark.notice.step;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import jenkins.model.Jenkins;
import lombok.Getter;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

/**
 * AbstractStep is an abstract class that extends the Step class.
 * It provides common functionality and properties for different types of steps.
 *
 * @author xm.z
 */
@Getter
public abstract class AbstractStep extends Step {

    /**
     * An instance of the service class.
     */
    protected final MessageDispatcher service = new MessageDispatcher();

    /**
     * The absolute URL of Jenkins, such as {@code http://localhost/jenkins/}.
     */
    protected final String rootPath = Jenkins.get().getRootUrl();

    /**
     * The name of the robot.
     */
    protected String robot;

    /**
     * The type of the message.
     */
    protected MsgTypeEnum type;

    /**
     * Constructs a new AbstractStep with the specified robot name and message type.
     *
     * @param robot The name of the robot.
     * @param type  The type of the message.
     */
    public AbstractStep(String robot, MsgTypeEnum type) {
        this.robot = robot;
        this.type = type;
    }

    /**
     * Sends the message to the specified run, environment variables, and task listener.
     *
     * @param run      The run to send the message to.
     * @param envVars  The environment variables.
     * @param listener The task listener.
     * @return The SendResult indicating the success or failure of the message sending.
     */
    protected abstract SendResult send(Run<?, ?> run, EnvVars envVars, TaskListener listener);

    /**
     * Overrides the start method of the Step interface to create and return a new StepExecution object instance,
     * which is used to execute the actual logic of sending messages for the current step.
     *
     * @param context The context information for the step.
     * @return A StepExecution object instance for executing the sending of messages.
     * @throws Exception If an error occurs during execution.
     */
    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new GenericStepExecution<>(this, context);
    }
}