package io.jenkins.plugins.model;

import hudson.model.*;
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import io.jenkins.plugins.config.property.FeiShuTalkUserProperty;
import io.jenkins.plugins.tools.Logger;
import jenkins.model.Jenkins;
import lombok.*;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 运行用户实体类，包含用户名称和手机号信息
 *
 * @author xm.z
 */
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RunUser {

    /**
     * 当前执行任务的用户名称
     */
    private final String name;

    /**
     * 当前执行任务的用户配置的手机号
     */
    private final String mobile;

    /**
     * 根据运行任务获取执行人信息
     *
     * @param run      运行任务
     * @param listener 任务监听器
     * @return 执行人信息
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
                .orElse(new RunUser("", ""));
    }

    /**
     * 根据运行任务从UserIdCause中获取执行人信息
     *
     * @param run      运行任务
     * @param listener 任务监听器
     * @return 执行人信息
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
        Optional<String> mobileOpt = Optional.ofNullable(user.getProperty(FeiShuTalkUserProperty.class)).map(FeiShuTalkUserProperty::getMobile);
        mobileOpt.ifPresent(mobile -> Logger.log(listener, "用户【%s】暂未设置手机号码，请前往 %s 添加。", name, user.getAbsoluteUrl() + "/configure"));

        return new RunUser(name, mobileOpt.orElse(null));
    }

    /**
     * 根据运行任务从RemoteCause中获取执行人信息
     *
     * @param run 运行任务
     * @return 执行人信息
     */
    private static RunUser getExecutorFromRemote(Run<?, ?> run) {
        Cause.RemoteCause remoteCause = run.getCause(Cause.RemoteCause.class);
        return remoteCause == null ? null :
                new RunUser(String.format("%s %s", remoteCause.getAddr(), remoteCause.getNote()), "");
    }

    /**
     * 根据运行任务从UpstreamCause中获取执行人信息
     *
     * @param run      运行任务
     * @param listener 任务监听器
     * @return 执行人信息
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
     * 根据运行任务从Build中获取执行人信息
     *
     * @param run 运行任务
     * @return 执行人信息
     */
    private static RunUser getExecutorFromBuild(Run<?, ?> run) {
        String name = run.getCauses().stream().map(Cause::getShortDescription).collect(Collectors.joining());
        return new RunUser(name, "");
    }
}
