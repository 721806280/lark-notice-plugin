package io.jenkins.plugins.lark.notice.service;

import hudson.EnvVars;
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

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;
import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.defaultTitle;

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
        RobotType robotType = resolveRobotType(config);
        Set<String> atUserIds = resolveAtUserIds(config, context.executor(), context.envVars());

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
        applyModelTemplateValues(config, model, context.envVars());
        String messageText = resolveMessageText(config, model, context.envVars(), robotType);
        MessageModel messageModel = buildMessageModel(model, config, atUserIds, messageText);

        SendResult result = messageDispatcher.send(listener, config.getRobotId(), messageModel);
        handleSendResult(source, run, listener, occasion, config.getRobotId(), result);
    }

    /**
     * Resolves robot type from global robot config by robot id.
     *
     * @param config notifier config
     * @return resolved robot type
     */
    static RobotType resolveRobotType(LarkNotifierConfig config) {
        return LarkGlobalConfig.getRobot(config.getRobotId())
                .map(LarkRobotConfig::obtainRobotType)
                .orElseThrow(() -> new IllegalStateException(
                        String.format(Messages.notifier_error_robot_missing(), config.getRobotId())));
    }

    /**
     * Resolves final @-mentioned user identities from config plus executor information.
     *
     * @param config   notifier config
     * @param executor build executor
     * @param envVars  environment variables
     * @return resolved user id set
     */
    static Set<String> resolveAtUserIds(LarkNotifierConfig config, RunUser executor, EnvVars envVars) {
        Set<String> atUserIds = config.resolveAtUserIds(envVars);
        if (StringUtils.isNotBlank(executor.getOpenId())) {
            atUserIds.add(executor.getOpenId());
        }
        if (StringUtils.isNotBlank(executor.getMobile())) {
            atUserIds.add(executor.getMobile());
        }
        return atUserIds;
    }

    /**
     * Applies title and content templates onto build model fields.
     *
     * @param config notifier config
     * @param model  build model
     * @param envVars environment variables
     */
    static void applyModelTemplateValues(LarkNotifierConfig config, BuildJobModel model, EnvVars envVars) {
        model.setTitle(envVars.expand(StringUtils.defaultIfBlank(config.getTitle(), defaultTitle())));
        model.setContent(envVars.expand(config.getContent()).replaceAll("\\\\n", LF));
    }

    /**
     * Resolves message text according to notifier mode.
     *
     * @param config    notifier config
     * @param model     build model
     * @param envVars   environment variables
     * @param robotType robot type
     * @return final message text
     */
    static String resolveMessageText(LarkNotifierConfig config, BuildJobModel model, EnvVars envVars,
                                     RobotType robotType) {
        return config.isRaw() ? envVars.expand(config.getMessage()) : model.toMarkdown(robotType);
    }

    /**
     * Builds final message model used by dispatcher.
     *
     * @param model       build model
     * @param config      notifier config
     * @param atUserIds   final at-user ids
     * @param messageText final text payload
     * @return built message model
     */
    static MessageModel buildMessageModel(BuildJobModel model, LarkNotifierConfig config,
                                          Set<String> atUserIds, String messageText) {
        return model.messageModelBuilder()
                .atAll(config.isAtAll())
                .atUserIds(atUserIds)
                .text(messageText)
                .build();
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
