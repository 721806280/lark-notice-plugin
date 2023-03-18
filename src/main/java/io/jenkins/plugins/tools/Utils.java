package io.jenkins.plugins.tools;

import hudson.model.*;
import io.jenkins.plugins.FeiShuTalkUserProperty;
import io.jenkins.plugins.model.RunUser;
import io.jenkins.plugins.sdk.entity.support.Button;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 通用方法合集
 *
 * @author xm.z
 */
public class Utils {

    /**
     * 字符串分隔符
     */
    public static final String DELIMITER = "\n";

    /**
     * 创建默认的按钮列表
     *
     * @param jobUrl 任务地址
     * @return 按钮列表
     */
    public static List<Button> createDefaultButtons(String jobUrl) {
        String changeLog = jobUrl + "/changes";
        String console = jobUrl + "/console";

        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.of("更改记录", changeLog));
        buttons.add(Button.of("控制台", console));

        return buttons;
    }

    /**
     * markdown 染色
     *
     * @param content 内容
     * @param color   颜色
     * @return 带颜色的内容
     */
    public static String dye(String content, String color) {
        return "<font color=" + color + ">" + content + "</font>";
    }

    /**
     * markdown 数组转字符串
     *
     * @param list 数组
     * @return 字符串
     */
    public static String join(Iterable<? extends CharSequence> list) {
        if (list == null) {
            return "";
        }
        return String.join(DELIMITER, list);
    }

    //====================================================================================

    /**
     * @see <a
     * href="https://github.com/jenkinsci/build-user-vars-plugin/blob/master/src/main/java/org/jenkinsci/plugins/builduser/BuildUser.java">...</a>
     */
    public static RunUser getExecutor(Run<?, ?> run, TaskListener listener) {
        RunUser executor = getExecutorFromUser(run, listener);
        if (executor == null) {
            executor = getExecutorFromRemote(run);
        }
        if (executor == null) {
            executor = getExecutorFromUpstream(run, listener);
        }
        if (executor == null) {
            executor = getExecutorFromBuild(run);
        }
        return executor;
    }

    public static RunUser getExecutorFromUser(Run<?, ?> run, TaskListener listener) {
        Cause.UserIdCause userIdCause = run.getCause(Cause.UserIdCause.class);
        if (Objects.isNull(userIdCause) || Objects.isNull(userIdCause.getUserId())) {
            return null;
        }

        User user = User.getById(userIdCause.getUserId(), false);
        if (Objects.isNull(user)) {
            return null;
        }

        String name = user.getDisplayName(), mobile = user.getProperty(FeiShuTalkUserProperty.class).getMobile();
        if (StringUtils.isEmpty(mobile)) {
            Logger.log(listener, "用户【%s】暂未设置手机号码，请前往 %s 添加。", name, user.getAbsoluteUrl() + "/configure");
        }
        return new RunUser(name, mobile);
    }

    public static RunUser getExecutorFromRemote(Run<?, ?> run) {
        Cause.RemoteCause remoteCause = run.getCause(Cause.RemoteCause.class);
        return Objects.isNull(remoteCause) ? null :
                new RunUser(String.format("%s %s", remoteCause.getAddr(), remoteCause.getNote()), null);
    }

    public static RunUser getExecutorFromUpstream(Run<?, ?> run, TaskListener listener) {
        Cause.UpstreamCause upstreamCause = run.getCause(Cause.UpstreamCause.class);
        if (Objects.isNull(upstreamCause)) {
            return null;
        }

        Job<?, ?> job = Jenkins.get().getItemByFullName(upstreamCause.getUpstreamProject(), Job.class);
        if (job != null) {
            Run<?, ?> upstream = job.getBuildByNumber(upstreamCause.getUpstreamBuild());
            if (upstream != null) {
                return getExecutor(upstream, listener);
            }
        }
        return new RunUser(upstreamCause.getUpstreamProject(), null);
    }

    public static RunUser getExecutorFromBuild(Run<?, ?> run) {
        String name = run.getCauses().stream().map(Cause::getShortDescription).collect(Collectors.joining());
        return new RunUser(name, null);
    }
}
