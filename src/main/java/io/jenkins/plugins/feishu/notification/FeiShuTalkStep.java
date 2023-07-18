package io.jenkins.plugins.feishu.notification;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.feishu.notification.config.FeiShuTalkGlobalConfig;
import io.jenkins.plugins.feishu.notification.config.FeiShuTalkRobotConfig;
import io.jenkins.plugins.feishu.notification.enums.MsgTypeEnum;
import io.jenkins.plugins.feishu.notification.model.ButtonModel;
import io.jenkins.plugins.feishu.notification.model.ImgModel;
import io.jenkins.plugins.feishu.notification.model.MessageModel;
import io.jenkins.plugins.feishu.notification.sdk.impl.FeiShuTalkServiceImpl;
import io.jenkins.plugins.feishu.notification.sdk.model.SendResult;
import io.jenkins.plugins.feishu.notification.sdk.model.entity.support.Alt;
import io.jenkins.plugins.feishu.notification.sdk.model.entity.support.Button;
import io.jenkins.plugins.feishu.notification.sdk.model.entity.support.ImgElement;
import io.jenkins.plugins.feishu.notification.tools.JsonUtils;
import io.jenkins.plugins.feishu.notification.tools.Logger;
import io.jenkins.plugins.feishu.notification.tools.Utils;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.Setter;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FeiShuTalkStep 是 Jenkins 插件中用于发送飞书消息的 Step 实现类。
 * 支持发送文本消息、图片消息、富文本消息、分享聊天消息等类型，同时支持添加按钮交互操作。
 * 插件中运用了 FeiShuTalk GlobalConfig 进行机器人配置，支持多个机器人同时使用。
 *
 * @author xm.z
 */
@Getter
@Setter
@SuppressWarnings("unused")
public class FeiShuTalkStep extends Step {

    /**
     * 用于发送飞书消息的机器人名称。
     */
    private String robot;

    /**
     * 消息类型，目前支持 TEXT、IMAGE、POST、INTERACTIVE 和 SHARE_CHAT。
     */
    private MsgTypeEnum type;

    /**
     * 消息标题，在不同类型的消息中具有不同的作用。
     */
    private String title;

    /**
     * 文本类型消息中的内容，是一个字符串列表。
     */
    private List<String> text;

    /**
     * 分享聊天类型消息中需要分享的聊天 ID。
     */
    private String shareChatId;

    /**
     * 图片类型消息中需要展示的图片 key。
     */
    private String imageKey;

    /**
     * 富文本类型消息中展示内容的数据结构。
     */
    private List<List<Map<String, String>>> post;

    /**
     * 消息正文顶部的图片 -- 仅适用于卡片消息。
     */
    private ImgModel topImg;

    /**
     * 消息正文底部的图片 -- 仅适用于卡片消息。
     */
    private ImgModel bottomImg;

    /**
     * 消息中需要包含的按钮列表。
     */
    private List<ButtonModel> buttons;

    /**
     * Jenkins 主页的路径，用于构造文本消息中的链接和按钮链接。
     */
    private String rootPath = Jenkins.get().getRootUrl();

    /**
     * 飞书机器人实现类的实例。
     */
    private FeiShuTalkServiceImpl service = new FeiShuTalkServiceImpl();

    /**
     * 插件的构造函数，指定机器人名称。
     *
     * @param robot 用于发送飞书消息的机器人名称。
     */
    @DataBoundConstructor
    public FeiShuTalkStep(String robot) {
        this.robot = robot;
    }

    /**
     * 设置消息类型，如果为空，则使用默认值 TEXT。
     *
     * @param type 消息类型。
     */
    @DataBoundSetter
    public void setType(MsgTypeEnum type) {
        this.type = type == null ? MsgTypeEnum.TEXT : type;
    }

    /**
     * 设置消息标题。
     *
     * @param title 消息标题。
     */
    @DataBoundSetter
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 设置文本消息中的内容数据列表。
     *
     * @param text 文本消息中的内容数据列表。
     */
    @DataBoundSetter
    public void setText(List<String> text) {
        this.text = text;
    }

    /**
     * 设置分享聊天消息中需要分享的聊天 ID。
     *
     * @param shareChatId 分享聊天消息中需要分享的聊天 ID。
     */
    @DataBoundSetter
    public void setShareChatId(String shareChatId) {
        this.shareChatId = shareChatId;
    }

    /**
     * 设置图片类型消息中需要展示的图片 key。
     *
     * @param imageKey 图片类型消息中需要展示的图片 key。
     */
    @DataBoundSetter
    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    /**
     * 设置富文本类型消息中展示内容的数据结构。
     *
     * @param post 富文本类型消息中展示内容的数据结构。
     */
    @DataBoundSetter
    public void setPost(List<List<Map<String, String>>> post) {
        this.post = post;
    }

    /**
     * 设置消息中正文顶部的图片。
     *
     * @param topImg 消息中正文顶部的图片。
     */
    @DataBoundSetter
    public void setTopImg(ImgModel topImg) {
        this.topImg = topImg;
    }

    /**
     * 设置消息中正文底部的图片。
     *
     * @param bottomImg 消息中正文底部的图片。
     */
    @DataBoundSetter
    public void setBottomImg(ImgModel bottomImg) {
        this.bottomImg = bottomImg;
    }

    /**
     * 设置消息中需要包含的按钮列表。
     *
     * @param buttons 消息中需要包含的按钮列表。
     */
    @DataBoundSetter
    public void setButtons(List<ButtonModel> buttons) {
        this.buttons = buttons;
    }

    /**
     * 发送飞书消息的方法。
     *
     * @param run      Jenkins 任务运行时的上下文信息。
     * @param envVars  Jenkins 任务运行时的环境变量。
     * @param listener Jenkins 任务运行时的监听器。
     * @return 消息发送结果。
     */
    public SendResult send(Run<?, ?> run, EnvVars envVars, TaskListener listener) {
        MessageModel message = MessageModel.builder().type(type).title(envVars.expand(title))
                .text(envVars.expand(buildText())).buttons(buildButtons(run, envVars))
                .topImg(buildImg(envVars, topImg)).bottomImg(buildImg(envVars, bottomImg))
                .build();

        Logger.log(listener, "当前机器人信息: %s",
                FeiShuTalkGlobalConfig.getRobot(robot).map(FeiShuTalkRobotConfig::getName));
        Logger.log(listener, "发送的消息详情: %s", JsonUtils.toJsonStr(message));

        return service.send(envVars.expand(robot), message);
    }

    /**
     * 消息正文的图片节点
     *
     * @param envVars  Jenkins 任务运行时的环境变量。
     * @param imgModel 消息正文的图片模型
     * @return 消息正文的图片节点
     */
    private ImgElement buildImg(EnvVars envVars, ImgModel imgModel) {
        if (Objects.isNull(imgModel)) {
            return null;
        }
        ImgElement imgElement = new ImgElement();
        imgElement.setImgKey(imgModel.getImgKey());
        imgElement.setMode(imgModel.getMode());
        imgElement.setCompactWidth(imgModel.isCompactWidth());
        imgElement.setCustomWidth(imgModel.getCustomWidth());
        imgElement.setAlt(Alt.build(envVars.expand(imgModel.getAltContent())));
        return imgElement;
    }

    /**
     * 构造消息中要包含的按钮列表。
     *
     * @param run     Jenkins 任务运行时的上下文信息。
     * @param envVars Jenkins 任务运行时的环境变量。
     * @return 消息中要包含的按钮列表。
     */
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

    /**
     * 根据消息类型构造要发送的消息内容。
     *
     * @return 要发送的消息内容。
     */
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

    /**
     * 重写 Step 接口的 start 方法，创建并返回一个 FeiShuTalkStepExecution 对象实例，用于执行当前的 step。
     *
     * @param context Step 的上下文信息
     * @return 一个 FeiShuTalkStepExecution 对象实例，用于执行发送飞书消息的实际逻辑
     * @throws Exception 执行异常
     */
    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new FeiShuTalkStepExecution(this, context);
    }

    /**
     * StepExecution 的实现类。
     */
    private static class FeiShuTalkStepExecution extends StepExecution {
        private static final long serialVersionUID = 1L;
        private final transient FeiShuTalkStep step;

        private FeiShuTalkStepExecution(FeiShuTalkStep step, StepContext context) {
            super(context);
            this.step = step;
        }

        /**
         * 开始执行 Step。
         *
         * @return 执行结果。
         * @throws Exception 执行异常。
         */
        @Override
        public boolean start() throws Exception {
            StepContext context = this.getContext();
            Run<?, ?> run = context.get(Run.class);
            EnvVars envVars = context.get(EnvVars.class);
            TaskListener listener = context.get(TaskListener.class);
            try {
                assert envVars != null;
                SendResult sendResult = this.step.send(run, envVars, listener);
                if (sendResult.isOk()) {
                    context.onSuccess(sendResult.getMsg());
                } else {
                    context.onFailure(new Throwable(Logger.format(sendResult.getMsg())));
                }
                return true;
            } catch (Exception e) {
                context.onFailure(e);
                return false;
            }
        }
    }

    /**
     * StepDescriptor 的实现类。
     */
    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        /**
         * 获取执行该 Step 所需的上下文信息。
         *
         * @return 执行该 Step 所需的上下文信息。
         */
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return new HashSet<>() {{
                add(Run.class);
                add(TaskListener.class);
            }};
        }

        /**
         * 获取该 Step 在 Jenkinsfile 中使用的函数名。
         *
         * @return 该 Step 在 Jenkinsfile 中使用的函数名。
         */
        @Override
        public String getFunctionName() {
            return "feiShuTalk";
        }

        /**
         * 获取该 Step 在 Jenkins UI 中显示的名称。
         *
         * @return 该 Step 在 Jenkins UI 中显示的名称。
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return "Send FeiShuTalk Message";
        }
    }

}