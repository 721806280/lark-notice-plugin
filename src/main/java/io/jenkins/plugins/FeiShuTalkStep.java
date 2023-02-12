package io.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.enums.MsgTypeEnum;
import io.jenkins.plugins.model.ButtonModel;
import io.jenkins.plugins.model.MessageModel;
import io.jenkins.plugins.sdk.entity.support.Button;
import io.jenkins.plugins.service.impl.FeiShuTalkServiceImpl;
import io.jenkins.plugins.tools.JsonUtils;
import io.jenkins.plugins.tools.Logger;
import io.jenkins.plugins.tools.Utils;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 支持 pipeline 中使用
 *
 * <p>* 不要使用 @Data 注解，spotbugs 会报错 *
 *
 * <p>* Redundant nullcheck of this$title, which is known to be non-null in *
 * io.jenkins.plugins.model.MessageModel.equals(Object)
 *
 * @author xm.z
 */
@Getter
@Setter
@SuppressWarnings("unused")
public class FeiShuTalkStep extends Step {

    /**
     * 机器人 id
     */
    private String robot;

    /**
     * 消息类型
     */
    private MsgTypeEnum type;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 文本字符串
     */
    private List<String> text;

    /**
     * 群名片ID
     */
    private String shareChatId;

    /**
     * 图片KEY
     */
    private String imageKey;

    /**
     * 富文本消息体
     */
    private List<List<Map<String, String>>> post;

    /**
     * 按钮列表
     */
    private List<ButtonModel> buttons;

    private String rootPath = Jenkins.get().getRootUrl();

    private FeiShuTalkServiceImpl service = new FeiShuTalkServiceImpl();

    @DataBoundConstructor
    public FeiShuTalkStep(String robot) {
        this.robot = robot;
    }

    @DataBoundSetter
    public void setType(MsgTypeEnum type) {
        this.type = type == null ? MsgTypeEnum.TEXT : type;
    }

    @DataBoundSetter
    public void setTitle(String title) {
        this.title = title;
    }

    @DataBoundSetter
    public void setText(List<String> text) {
        this.text = text;
    }

    @DataBoundSetter
    public void setShareChatId(String shareChatId) {
        this.shareChatId = shareChatId;
    }

    @DataBoundSetter
    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    @DataBoundSetter
    public void setPost(List<List<Map<String, String>>> post) {
        this.post = post;
    }

    @DataBoundSetter
    public void setButtons(List<ButtonModel> buttons) {
        this.buttons = buttons;
    }

    public String send(Run<?, ?> run, EnvVars envVars, TaskListener listener) {
        MessageModel message = MessageModel.builder().type(type).title(envVars.expand(title))
                .text(envVars.expand(buildText())).buttons(buildButtons(run, envVars)).build();

        Logger.log(listener, "当前机器人信息: %s", FeiShuTalkGlobalConfig.getRobot(robot).map(FeiShuTalkRobotConfig::getName));
        Logger.log(listener, "发送的消息详情: %s", JsonUtils.toJsonStr(message));

        return service.send(envVars.expand(robot), message);
    }

    private List<Button> buildButtons(Run<?, ?> run, EnvVars envVars) {
        if (MsgTypeEnum.INTERACTIVE.equals(type) && CollectionUtils.isEmpty(buttons)) {
            String jobUrl = rootPath + run.getUrl();
            return Utils.createDefaultButtons(jobUrl);
        } else if (!CollectionUtils.isEmpty(buttons)) {
            return buttons.stream().map(item ->
                    new Button(envVars.expand(item.getTitle()), envVars.expand(item.getUrl()), item.getType())
            ).collect(Collectors.toList());
        }
        return null;
    }

    private String buildText() {
        switch (type) {
            case IMAGE:
                return imageKey;
            case SHARE_CHAT:
                return shareChatId;
            case POST:
                return JsonUtils.toJsonStr(post);
            default:
                return Utils.join(text);
        }
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new FeiShuTalkStepExecution(this, context);
    }

    private static class FeiShuTalkStepExecution extends StepExecution {
        private static final long serialVersionUID = 1L;
        private final transient FeiShuTalkStep step;

        private FeiShuTalkStepExecution(FeiShuTalkStep step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        public boolean start() throws Exception {
            StepContext context = this.getContext();
            Run<?, ?> run = context.get(Run.class);
            EnvVars envVars = context.get(EnvVars.class);
            TaskListener listener = context.get(TaskListener.class);
            try {
                assert envVars != null;
                String result = this.step.send(run, envVars, listener);
                if (StringUtils.isEmpty(result)) {
                    context.onSuccess(result);
                } else {
                    context.onFailure(new Throwable(Logger.format(result)));
                }
                return true;
            } catch (Exception e) {
                context.onFailure(e);
                return false;
            }
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return new HashSet<>() {{
                add(Run.class);
                add(TaskListener.class);
            }};
        }

        @Override
        public String getFunctionName() {
            return "feiShuTalk";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Send FeiShuTalk Message";
        }
    }

}
