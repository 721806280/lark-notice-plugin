package io.jenkins.plugins.lark.notice.step.impl;

import com.google.common.collect.ImmutableSet;
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
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Alt;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Button;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.ImgElement;
import io.jenkins.plugins.lark.notice.step.AbstractStep;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import io.jenkins.plugins.lark.notice.tools.Utils;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.DEFAULT_TITLE;

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

    @DataBoundConstructor
    public LarkStep(String robot, MsgTypeEnum type) {
        super(robot, type);
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
    public void setTopImg(ImgModel topImg) {
        this.topImg = topImg;
    }

    @DataBoundSetter
    public void setBottomImg(ImgModel bottomImg) {
        this.bottomImg = bottomImg;
    }

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

        MessageModel message = MessageModel.builder().type(type)
                .statusType(noticeOccasion.buildStatus())
                .title(envVars.expand(StringUtils.defaultIfBlank(title, DEFAULT_TITLE)))
                .text(envVars.expand(buildText())).buttons(buildButtons(run, envVars))
                .topImg(buildImg(envVars, topImg)).bottomImg(buildImg(envVars, bottomImg))
                .build();

        return service.send(listener, envVars.expand(robot), message);
    }

    /**
     * Creates an image node for the message body.
     *
     * @param envVars  The environment variables during the Jenkins job execution.
     * @param imgModel The image model for the message body.
     * @return An image node for the message body.
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
     * Constructs a list of buttons to be included in the message.
     *
     * @param run     The context information of the Jenkins job at runtime.
     * @param envVars The environment variables during the Jenkins job execution.
     * @return A list of buttons to be included in the message.
     */
    private List<Button> buildButtons(Run<?, ?> run, EnvVars envVars) {
        if (MsgTypeEnum.CARD.equals(type) && CollectionUtils.isEmpty(buttons)) {
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

    /**
     * Descriptor implementation for the LarkStep.
     * This class provides Jenkins with information about the LarkStep, such as the required context,
     * the function name to be used in Jenkinsfiles, and the display name shown in the Jenkins UI.
     */
    @Extension
    public static class LarkStepDescriptor extends StepDescriptor {

        /**
         * Returns the set of context classes that this step requires.
         * Indicates which types of context information are needed for the execution of this step.
         *
         * @return A set of required context classes.
         */
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class, EnvVars.class);
        }

        /**
         * Returns the function name for this step to be used in Jenkinsfiles.
         * This name is used by users when writing their Jenkinsfiles.
         *
         * @return The function name for this step.
         */
        @Override
        public String getFunctionName() {
            return "lark";
        }

        /**
         * Returns the display name for this step in the Jenkins UI.
         * This name is shown to users when configuring jobs in Jenkins.
         *
         * @return The display name for this step.
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return "lark notice";
        }
    }

}