package io.jenkins.plugins.lark.notice.service;

import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkNotifierConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.model.BuildJobModel;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.model.RunUser;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.tools.LogEvent;
import io.jenkins.plugins.lark.notice.tools.LogField;
import io.jenkins.plugins.lark.notice.tools.Logger;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.DEFAULT_TITLE;
import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;

/**
 * Executes one concrete message dispatch for a matched notifier config.
 *
 * @author xm.z
 */
public final class NotificationDispatchExecutor {

    private NotificationDispatchExecutor() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Dispatches one final message according to notifier configuration.
     *
     * @param source            logical trigger source
     * @param run               build run
     * @param listener          Jenkins task listener
     * @param occasion          current notice occasion
     * @param config            notifier configuration
     * @param context           prepared build notification context
     * @param messageDispatcher dispatcher used to send built message
     */
    public static void dispatch(String source, Run<?, ?> run, TaskListener listener, NoticeOccasionEnum occasion,
                                LarkNotifierConfig config, BuildNotificationContext context,
                                MessageDispatcher messageDispatcher) {
        RobotType robotType = LarkGlobalConfig.getRobot(config.getRobotId())
                .map(LarkRobotConfig::obtainRobotType)
                .orElseThrow(() -> new IllegalStateException(
                        String.format(Messages.notifier_error_robot_not_found(), config.getRobotId())));

        RunUser executor = context.executor();
        Set<String> atUserIds = config.resolveAtUserIds(context.envVars());
        if (StringUtils.isNotBlank(executor.getOpenId())) {
            atUserIds.add(executor.getOpenId());
        }
        if (StringUtils.isNotBlank(executor.getMobile())) {
            atUserIds.add(executor.getMobile());
        }

        Logger.event(listener, LogEvent.NOTIFY_DISPATCH,
                LogField.SOURCE, source,
                LogField.JOB, run.getParent().getFullName(),
                LogField.BUILD, run.getNumber(),
                LogField.OCCASION, occasion.name(),
                LogField.ROBOT_ID, config.getRobotId(),
                LogField.ROBOT_TYPE, robotType,
                LogField.RAW, config.isRaw(),
                LogField.AT_ALL, config.isAtAll(),
                LogField.AT_USER_COUNT, atUserIds.size());

        BuildJobModel model = context.model();
        model.setTitle(context.envVars().expand(StringUtils.defaultIfBlank(config.getTitle(), DEFAULT_TITLE)));
        model.setContent(context.envVars().expand(config.getContent()).replaceAll("\\\\n", LF));
        String messageText = config.isRaw()
                ? context.envVars().expand(config.getMessage())
                : model.toMarkdown(robotType);

        MessageModel messageModel = model.messageModelBuilder()
                .atAll(config.isAtAll())
                .atUserIds(atUserIds)
                .text(messageText)
                .build();

        SendResult result = messageDispatcher.send(listener, config.getRobotId(), messageModel);
        handleSendResult(source, run, listener, occasion, config.getRobotId(), result);
    }

    /**
     * Handles send result logging and build-result fallback.
     *
     * @param source     logical trigger source
     * @param run        build run
     * @param listener   Jenkins task listener
     * @param occasion   current notice occasion
     * @param robotId    robot identifier
     * @param sendResult send result returned by dispatcher
     */
    static void handleSendResult(String source, Run<?, ?> run, TaskListener listener,
                                 NoticeOccasionEnum occasion, String robotId, SendResult sendResult) {
        Logger.event(listener, LogEvent.NOTIFY_RESULT,
                LogField.SOURCE, source,
                LogField.JOB, run.getParent().getFullName(),
                LogField.BUILD, run.getNumber(),
                LogField.OCCASION, occasion.name(),
                LogField.ROBOT_ID, robotId,
                LogField.OK, sendResult != null && sendResult.isOk(),
                LogField.CODE, sendResult == null ? "<null>" : sendResult.getCode(),
                LogField.MSG, sendResult == null ? "<null>" : Logger.clip(sendResult.getMsg(), 200));

        if (sendResult == null || !sendResult.isOk()) {
            run.setResult(Result.FAILURE);
            Logger.event(listener, LogEvent.NOTIFY_MARK_BUILD_FAILURE,
                    LogField.SOURCE, source,
                    LogField.JOB, run.getParent().getFullName(),
                    LogField.BUILD, run.getNumber(),
                    LogField.ROBOT_ID, robotId);
        }
    }
}
