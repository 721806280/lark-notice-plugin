package io.jenkins.plugins.lark.notice.sdk;

import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkRetryConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.logging.NoticeLog;
import io.jenkins.plugins.lark.notice.logging.NoticeLogKey;
import io.jenkins.plugins.lark.notice.logging.NoticeTrace;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;

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
        NoticeLog.trace(listener, NoticeTrace.DISPATCHER_SEND_START,
                NoticeLog.field(NoticeLogKey.ROBOT_ID, robotId),
                NoticeLog.field(NoticeLogKey.MESSAGE_TYPE, msg == null || msg.getType() == null ? "<null>" : msg.getType().name()));

        MessageSender sender = senderRegistry.resolve(robotId);
        if (sender == null) {
            return fail(listener, robotId, null, String.format(Messages.dispatcher_error_robot_not_exist(), robotId));
        }

        return send(listener, robotId, msg, sender);
    }

    /**
     * Sends a message using a provided sender, bypassing registry resolution.
     *
     * @param listener task listener
     * @param robotId  robot identifier for logging
     * @param msg      message payload
     * @param sender   prepared message sender
     * @return send result
     */
    public SendResult send(TaskListener listener, String robotId, MessageModel msg, MessageSender sender) {
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

        if (robotId != null) {
            NoticeLog.verbose(listener, Messages.dispatcher_log_current_robot(), senderRegistry.findRobotName(robotId));
        }

        RetryPolicy retryPolicy = resolveRetryPolicy(robotId);
        int maxAttempts = retryPolicy.getMaxAttempts();
        int attempt = 1;
        SendResult sendResult = null;
        while (true) {
            sendResult = type.send(sender, msg);
            if (sendResult != null && sendResult.isOk()) {
                break;
            }
            if (!retryPolicy.isEnabled() || attempt >= maxAttempts) {
                break;
            }
            long delayMs = retryPolicy.nextDelayMs(attempt);
            NoticeLog.trace(listener, NoticeTrace.DISPATCHER_SEND_RETRY,
                    NoticeLog.field(NoticeLogKey.ROBOT_ID, robotId),
                    NoticeLog.field(NoticeLogKey.MESSAGE_TYPE, type.name()),
                    NoticeLog.field(NoticeLogKey.ATTEMPT, attempt),
                    NoticeLog.field(NoticeLogKey.MAX_ATTEMPTS, maxAttempts),
                    NoticeLog.field(NoticeLogKey.DELAY_MS, delayMs));
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return fail(listener, robotId, type, "Retry interrupted");
            }
            attempt++;
        }

        if (sendResult == null) {
            return fail(listener, robotId, type, Messages.dispatcher_error_send_result_missing());
        }

        NoticeLog.verbose(listener, Messages.dispatcher_log_send_details(), sendResult.getRequestBody());
        NoticeLog.trace(listener, NoticeTrace.DISPATCHER_SEND_FINISH,
                NoticeLog.field(NoticeLogKey.ROBOT_ID, robotId),
                NoticeLog.field(NoticeLogKey.MESSAGE_TYPE, type.name()),
                NoticeLog.field(NoticeLogKey.SUCCESS, sendResult.isOk()),
                NoticeLog.field(NoticeLogKey.RESULT_CODE, sendResult.getCode()),
                NoticeLog.field(NoticeLogKey.MESSAGE, NoticeLog.abbreviate(sendResult.getMsg(), 200)),
                NoticeLog.field(NoticeLogKey.REQUEST_SIZE, sendResult.getRequestBody() == null ? 0 : sendResult.getRequestBody().length()),
                NoticeLog.field(NoticeLogKey.ATTEMPT, attempt),
                NoticeLog.field(NoticeLogKey.MAX_ATTEMPTS, maxAttempts));

        if (!sendResult.isOk()) {
            NoticeLog.error(listener, sendResult.getMsg());
        }

        return sendResult;
    }

    /**
     * Resolves the retry policy for the given robot id, falling back to defaults when absent.
     *
     * @param robotId robot identifier, may be null for ad-hoc sends
     * @return resolved retry policy
     */
    RetryPolicy resolveRetryPolicy(String robotId) {
        if (robotId == null) {
            return RetryPolicy.from(LarkRetryConfig.defaultConfig());
        }
        return LarkGlobalConfig.getRobot(robotId)
                .map(LarkRobotConfig::getRetryConfig)
                .map(RetryPolicy::from)
                .orElseGet(() -> RetryPolicy.from(LarkRetryConfig.defaultConfig()));
    }

    /**
     * Builds a failed result and emits a structured end-event for easier troubleshooting.
     */
    private SendResult fail(TaskListener listener, String robotId, MsgTypeEnum msgType, String message) {
        SendResult failed = SendResult.fail(message);
        NoticeLog.trace(listener, NoticeTrace.DISPATCHER_SEND_FINISH,
                NoticeLog.field(NoticeLogKey.ROBOT_ID, robotId),
                NoticeLog.field(NoticeLogKey.MESSAGE_TYPE, msgType == null ? "<null>" : msgType.name()),
                NoticeLog.field(NoticeLogKey.SUCCESS, false),
                NoticeLog.field(NoticeLogKey.RESULT_CODE, failed.getCode()),
                NoticeLog.field(NoticeLogKey.MESSAGE, NoticeLog.abbreviate(failed.getMsg(), 200)));
        return failed;
    }
}
