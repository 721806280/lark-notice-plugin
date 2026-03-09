package io.jenkins.plugins.lark.notice.model;

import hudson.model.*;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.property.LarkUserProperty;
import io.jenkins.plugins.lark.notice.logging.NoticeLog;
import io.jenkins.plugins.lark.notice.logging.NoticeLogKey;
import io.jenkins.plugins.lark.notice.logging.NoticeTrace;
import jenkins.model.Jenkins;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the user entity class, containing user name, phone number, and openId information.
 *
 * @author xm.z
 */
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RunUser {

    /**
     * The name of the user running the current task.
     */
    private final String name;

    /**
     * The mobile number configured by the user running the current task.
     */
    private final String mobile;

    /**
     * The openId configured by the user running the current task.
     */
    private final String openId;

    /**
     * Retrieves the executor information based on the running task.
     *
     * @param run      The running task
     * @param listener The task listener
     * @return The executor information
     */
    public static RunUser getExecutor(Run<?, ?> run, TaskListener listener) {
        return Stream.<Supplier<RunUser>>of(
                        () -> getExecutorFromUser(run, listener),
                        () -> getExecutorFromRemote(run),
                        () -> getExecutorFromUpstream(run, listener),
                        () -> getExecutorFromBuild(run))
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(new RunUser("", "", ""));
    }

    /**
     * Retrieves the executor information from the UserIdCause based on the running task.
     *
     * @param run      The running task
     * @param listener The task listener
     * @return The executor information
     */
    private static RunUser getExecutorFromUser(Run<?, ?> run, TaskListener listener) {
        Cause.UserIdCause userIdCause = run.getCause(Cause.UserIdCause.class);
        if (userIdCause == null || StringUtils.isBlank(userIdCause.getUserId())) {
            return null;
        }

        User user = User.getById(userIdCause.getUserId(), false);
        if (user == null) {
            return null;
        }

        String name = user.getDisplayName();

        Optional<LarkUserProperty> userPropertyOpt = Optional.ofNullable(user.getProperty(LarkUserProperty.class));

        String mobile = userPropertyOpt.map(LarkUserProperty::getMobile).orElse("");
        if (StringUtils.isBlank(mobile)) {
            NoticeLog.verbose(listener, Messages.run_user_missing_mobile(), name, user.getAbsoluteUrl() + "/configure");
            NoticeLog.trace(listener, NoticeTrace.RUN_USER_MOBILE_MISSING,
                    NoticeLog.field(NoticeLogKey.USER, name),
                    NoticeLog.field(NoticeLogKey.URL, user.getAbsoluteUrl() + "/configure"));
        }

        String openId = userPropertyOpt.map(LarkUserProperty::getOpenId).orElse("");
        if (StringUtils.isBlank(openId)) {
            NoticeLog.verbose(listener, Messages.run_user_missing_open_id(), name, user.getAbsoluteUrl() + "/configure");
            NoticeLog.trace(listener, NoticeTrace.RUN_USER_OPEN_ID_MISSING,
                    NoticeLog.field(NoticeLogKey.USER, name),
                    NoticeLog.field(NoticeLogKey.URL, user.getAbsoluteUrl() + "/configure"));
        }

        return new RunUser(name, mobile, openId);
    }


    /**
     * Retrieves the executor information from the RemoteCause based on the running task.
     *
     * @param run The running task
     * @return The executor information
     */
    private static RunUser getExecutorFromRemote(Run<?, ?> run) {
        Cause.RemoteCause remoteCause = run.getCause(Cause.RemoteCause.class);
        if (remoteCause == null) {
            return null;
        }
        String note = StringUtils.defaultString(remoteCause.getNote()).trim();
        String name = StringUtils.isBlank(note) ? remoteCause.getAddr() : remoteCause.getAddr() + " " + note;
        return new RunUser(name, "", "");
    }

    /**
     * Retrieves the executor information from the UpstreamCause based on the running task.
     *
     * @param run      The running task
     * @param listener The task listener
     * @return The executor information
     */
    private static RunUser getExecutorFromUpstream(Run<?, ?> run, TaskListener listener) {
        Cause.UpstreamCause upstreamCause = run.getCause(Cause.UpstreamCause.class);
        if (upstreamCause == null) {
            return null;
        }

        Job<?, ?> job = Jenkins.get().getItemByFullName(upstreamCause.getUpstreamProject(), Job.class);
        if (job == null) {
            return null;
        }

        Optional<Run<?, ?>> upstreamOpt = Optional.ofNullable(job.getBuildByNumber(upstreamCause.getUpstreamBuild()));
        return upstreamOpt.map(upstream -> getExecutor(upstream, listener)).orElse(null);
    }

    /**
     * Retrieves the executor information from the Build cause based on the running task.
     *
     * @param run The running task
     * @return The executor information
     */
    private static RunUser getExecutorFromBuild(Run<?, ?> run) {
        String name = run.getCauses().stream().map(Cause::getShortDescription).collect(Collectors.joining(" | "));
        return new RunUser(name, "", "");
    }
}
