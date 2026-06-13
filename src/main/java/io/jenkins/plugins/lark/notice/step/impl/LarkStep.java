package io.jenkins.plugins.lark.notice.step.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import io.jenkins.plugins.lark.notice.model.ButtonModel;
import io.jenkins.plugins.lark.notice.model.ImgModel;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Button;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.img.ImgElement;
import io.jenkins.plugins.lark.notice.step.AbstractStep;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import io.jenkins.plugins.lark.notice.tools.Utils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.defaultTitle;

/**
 * This class represents a step for sending messages to Lark using a specific robot.
 *
 * @author xm.z
 */
@Getter
@SuppressWarnings("unused")
public class LarkStep extends AbstractStep {

    /**
     * The title of the message, with different meanings in different message types.
     */
    private String title;

    /**
     * The content of the text message, represented as a list of strings.
     */
    private List<String> text;

    /**
     * The chat ID to be shared in a SHARE_CHAT message.
     */
    private String shareChatId;

    /**
     * The image key to be displayed in an IMAGE message.
     */
    @SuppressWarnings("lgtm[jenkins/plaintext-storage]")
    private String imageKey;

    /**
     * The data structure to display rich content in a POST message.
     */
    private List<List<Map<String, String>>> post;

    /**
     * The image at the top of the message body - only applicable to card messages.
     */
    private ImgModel topImg;

    /**
     * The image at the bottom of the message body - only applicable to card messages.
     */
    private ImgModel bottomImg;

    /**
     * The list of buttons to be included in the message.
     */
    private List<ButtonModel> buttons;

    /**
     * Creates a Lark pipeline step with the target robot and message type.
     *
     * @param robot robot identifier or expression
     * @param type  message type
     */
    @DataBoundConstructor
    public LarkStep(String robot, MsgTypeEnum type) {
        super(robot, type);
    }

    /**
     * Sets the message title.
     *
     * @param title message title
     */
    @DataBoundSetter
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the message text lines.
     *
     * @param text message text lines
     */
    @DataBoundSetter
    public void setText(List<String> text) {
        this.text = text;
    }

    /**
     * Sets the shared chat identifier for SHARE_CHAT messages.
     *
     * @param shareChatId share chat identifier
     */
    @DataBoundSetter
    public void setShareChatId(String shareChatId) {
        this.shareChatId = shareChatId;
    }

    /**
     * Sets the image key for IMAGE messages.
     *
     * @param imageKey image key
     */
    @DataBoundSetter
    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    /**
     * Sets the POST message body structure.
     *
     * @param post post body structure
     */
    @DataBoundSetter
    public void setPost(List<List<Map<String, String>>> post) {
        this.post = post;
    }

    /**
     * Sets the top image for card messages.
     *
     * @param topImg top image model
     */
    @DataBoundSetter
    public void setTopImg(ImgModel topImg) {
        this.topImg = topImg;
    }

    /**
     * Sets the bottom image for card messages.
     *
     * @param bottomImg bottom image model
     */
    @DataBoundSetter
    public void setBottomImg(ImgModel bottomImg) {
        this.bottomImg = bottomImg;
    }

    /**
     * Sets the action buttons for card messages.
     *
     * @param buttons list of button models
     */
    @DataBoundSetter
    public void setButtons(List<ButtonModel> buttons) {
        this.buttons = buttons;
    }

    /**
     * Sends the message to the specified run, environment variables, and task listener.
     *
     * @param run      The run to send the message to.
     * @param envVars  The environment variables.
     * @param listener The task listener.
     * @return The SendResult indicating the success or failure of the message sending.
     */
    @Override
    public SendResult send(Run<?, ?> run, EnvVars envVars, TaskListener listener) {
        NoticeOccasionEnum noticeOccasion = NoticeOccasionEnum.getNoticeOccasion(run.getResult());

        List<Button> resolvedButtons = expandButtons(envVars, buttons);
        if (resolvedButtons == null && MsgTypeEnum.CARD.equals(type)) {
            String jobUrl = rootPath + run.getUrl();
            resolvedButtons = Utils.createDefaultButtons(jobUrl);
        }

        MessageModel message = MessageModel.builder().type(type)
                .statusType(noticeOccasion.buildStatus())
                .title(envVars.expand(StringUtils.defaultIfBlank(title, defaultTitle())))
                .text(envVars.expand(buildText())).buttons(resolvedButtons)
                .topImg(buildImg(envVars, topImg)).bottomImg(buildImg(envVars, bottomImg))
                .build();

        return service.send(listener, envVars.expand(robot), message);
    }

    /**
     * Constructs the message content to be sent based on the message type.
     *
     * @return The message content to be sent.
     */
    private String buildText() {
        return switch (type) {
            case IMAGE -> imageKey;
            case SHARE_CHAT -> shareChatId;
            case POST -> JsonUtils.toJson(post);
            default -> Utils.join(text);
        };
    }

    @Override
    protected ImgElement buildImg(EnvVars envVars, ImgModel imgModel) {
        return super.buildImg(envVars, imgModel);
    }

    @Extension
    public static class LarkStepDescriptor extends AbstractStepDescriptor {

        @Override
        public String getFunctionName() {
            return "lark";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "lark notice";
        }
    }

}
