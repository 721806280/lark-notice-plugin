package io.jenkins.plugins.lark.notice.sdk;

import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.tools.LogEvent;
import io.jenkins.plugins.lark.notice.tools.LogField;
import io.jenkins.plugins.lark.notice.tools.Logger;

/**
 * Responsible for dispatching messages to specified robots on the Lark platform.
 *
 * @author xm.z
 */
public class MessageDispatcher {

    /**
     * The single instance of the MessageDispatcher, ensuring that only one instance of this class exists.
     */
    private static final MessageDispatcher INSTANCE = new MessageDispatcher();

    private final MessageSenderRegistry senderRegistry;

    private MessageDispatcher() {
        this(MessageSenderRegistry.getInstance());
    }

    MessageDispatcher(MessageSenderRegistry senderRegistry) {
        this.senderRegistry = senderRegistry;
    }

    /**
     * Provides access to the single instance of MessageDispatcher.
     *
     * @return the single instance of MessageDispatcher.
     */
    public static MessageDispatcher getInstance() {
        return INSTANCE;
    }

    /**
     * Sends a message to a specified Lark robot using its ID.
     * This method determines the appropriate MessageSender for the robot and
     * delegates the message sending operation to it. If the robot ID is not recognized,
     * or if the message type is not specified, it returns a failure result.
     *
     * @param listener The task listener
     * @param robotId  The ID of the Lark robot to which the message should be sent.
     * @param msg      The message to be sent, encapsulated in a MessageModel object.
     * @return A SendResult object representing the outcome of the send operation.
     * This includes status codes and messages indicating success or failure.
     */
    public SendResult send(TaskListener listener, String robotId, MessageModel msg) {
        Logger.event(listener, LogEvent.DISPATCHER_SEND_START,
                LogField.ROBOT_ID, robotId,
                LogField.MSG_TYPE, msg == null || msg.getType() == null ? "<null>" : msg.getType().name());

        MessageSender sender = senderRegistry.resolve(robotId);
        if (sender == null) {
            return fail(listener, robotId, null, String.format(Messages.dispatcher_error_robot_not_exist(), robotId));
        }

        if (msg == null) {
            return fail(listener, robotId, null, Messages.dispatcher_error_message_missing());
        }

        MsgTypeEnum type = msg.getType();
        if (type == null) {
            return fail(listener, robotId, null, Messages.dispatcher_error_message_type_missing());
        }

        Logger.log(listener, Messages.dispatcher_log_current_robot(), senderRegistry.findRobotName(robotId));

        SendResult sendResult = type.send(sender, msg);
        if (sendResult == null) {
            return fail(listener, robotId, type, Messages.dispatcher_error_send_result_missing());
        }

        Logger.log(listener, Messages.dispatcher_log_send_details(), sendResult.getRequestBody());
        Logger.event(listener, LogEvent.DISPATCHER_SEND_END,
                LogField.ROBOT_ID, robotId,
                LogField.MSG_TYPE, type.name(),
                LogField.OK, sendResult.isOk(),
                LogField.CODE, sendResult.getCode(),
                LogField.MSG, Logger.clip(sendResult.getMsg(), 200),
                LogField.REQUEST_SIZE, sendResult.getRequestBody() == null ? 0 : sendResult.getRequestBody().length());

        if (!sendResult.isOk()) {
            Logger.error(listener, sendResult.getMsg());
        }

        return sendResult;
    }

    /**
     * Builds a failed result and emits a structured end-event for easier troubleshooting.
     */
    private SendResult fail(TaskListener listener, String robotId, MsgTypeEnum msgType, String message) {
        SendResult failed = SendResult.fail(message);
        Logger.event(listener, LogEvent.DISPATCHER_SEND_END,
                LogField.ROBOT_ID, robotId,
                LogField.MSG_TYPE, msgType == null ? "<null>" : msgType.name(),
                LogField.OK, false,
                LogField.CODE, failed.getCode(),
                LogField.MSG, Logger.clip(failed.getMsg(), 200));
        return failed;
    }
}
