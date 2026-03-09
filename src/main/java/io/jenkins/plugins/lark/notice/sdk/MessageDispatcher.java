package io.jenkins.plugins.lark.notice.sdk;

import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.model.RobotConfigModel;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.tools.LogEvent;
import io.jenkins.plugins.lark.notice.tools.LogField;
import io.jenkins.plugins.lark.notice.tools.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Responsible for dispatching messages to specified robots on the Lark platform.
 * It maintains a cache of MessageSender instances, each associated with a unique robot ID,
 * to efficiently handle message sending operations.
 *
 * @author xm.z
 */
public class MessageDispatcher {

    /**
     * The single instance of the MessageDispatcher, ensuring that only one instance of this class exists.
     */
    private static final MessageDispatcher INSTANCE = new MessageDispatcher();

    /**
     * A thread-safe map used as a cache for storing MessageSender instances. Each entry in the map is identified by a unique robot ID.
     */
    private transient Map<String, MessageSender> senders = new ConcurrentHashMap<>();

    private MessageDispatcher() {
        // Prevents instantiation from outside the class.
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
     * Clears all {@link MessageSender} instances from the senders cache.
     * This method may include additional checks to ensure that clearing is appropriate,
     * such as verifying global configurations or current system state.
     */
    public void clearSenders() {
        // Proceed to clear the senders map.
        getSenders().clear();
    }

    /**
     * Retrieves or creates a MessageSender instance for the given robot ID.
     * If a MessageSender for the specified robot ID does not exist in the cache,
     * it attempts to create a new one using the robot's configuration. If the robot's
     * configuration is not found, it returns null.
     *
     * @param robotId The ID of the Lark robot.
     * @return The MessageSender instance for the specified robot, or null if the robot's
     * configuration cannot be found.
     */
    private MessageSender getSender(String robotId) {
        return getSenders().computeIfAbsent(robotId, id -> {
            LarkGlobalConfig globalConfig = LarkGlobalConfig.getInstance();
            ArrayList<LarkRobotConfig> robotConfigs = globalConfig.getRobotConfigs();
            Optional<LarkRobotConfig> robotConfigOptional =
                    robotConfigs.stream().filter(item -> id.equals(item.getId())).findAny();
            if (robotConfigOptional.isPresent()) {
                LarkRobotConfig larkRobotConfig = robotConfigOptional.get();
                RobotType robotType = larkRobotConfig.obtainRobotType();
                RobotConfigModel robotConfigModel = RobotConfigModel.of(larkRobotConfig,
                        globalConfig.obtainProxySelector());
                return robotType.obtainInstance(robotConfigModel);
            }
            return null;
        });
    }

    private Map<String, MessageSender> getSenders() {
        if (senders == null) {
            senders = new ConcurrentHashMap<>(8);
        }
        return senders;
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

        MessageSender sender = getSender(robotId);
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

        Logger.log(listener, Messages.dispatcher_log_current_robot(),
                LarkGlobalConfig.getRobot(robotId).map(LarkRobotConfig::getName));

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
