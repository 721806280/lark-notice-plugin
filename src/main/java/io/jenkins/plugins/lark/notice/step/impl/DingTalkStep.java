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
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Button;
import io.jenkins.plugins.lark.notice.step.AbstractStep;
import io.jenkins.plugins.lark.notice.tools.Utils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.DEFAULT_TITLE;

/**
 * This class represents a step for sending notifications to DingTalk in Jenkins.
 *
 * @author xm.z
 */
@Getter
@SuppressWarnings("unused")
public class DingTalkStep extends AbstractStep {

    /**
     * The title of the message, with different meanings in different message types.
     */
    private String title;

    /**
     * The content of the text message, represented as a list of strings.
     */
    private List<String> text;

    /**
     * URL to redirect to when clicking on a single message.
     */
    private String messageUrl;

    /**
     * URL of the image to be displayed after a single message.
     */
    private String picUrl;

    /**
     * Title of a single button. If set along with singleURL, buttons will be invalid.
     */
    private String singleTitle;

    /**
     * URL to redirect when clicking on the message.
     */
    private String singleUrl;

    /**
     * List of users to tag using '@'.
     */
    private Set<String> ats;

    /**
     * Whether to tag everyone.
     */
    private boolean atAll;

    /**
     * Whether to display buttons vertically. Default is horizontally.
     */
    private boolean verticalButton;

    /**
     * The list of buttons to be included in the message.
     */
    private List<ButtonModel> buttons;

    @DataBoundConstructor
    public DingTalkStep(String robot, MsgTypeEnum type) {
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
    public void setAts(List<String> ats) {
        this.ats = ats == null ? new HashSet<>() : new HashSet<>(ats);
    }

    @DataBoundSetter
    public void setAtAll(boolean atAll) {
        this.atAll = atAll;
    }

    @DataBoundSetter
    public void setMessageUrl(String messageUrl) {
        this.messageUrl = messageUrl;
    }

    @DataBoundSetter
    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    @DataBoundSetter
    public void setSingleTitle(String singleTitle) {
        this.singleTitle = singleTitle;
    }

    @DataBoundSetter
    public void setSingleUrl(String singleUrl) {
        this.singleUrl = singleUrl;
    }

    @DataBoundSetter
    public void setVerticalButton(boolean verticalButton) {
        this.verticalButton = verticalButton;
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
    protected SendResult send(Run<?, ?> run, EnvVars envVars, TaskListener listener) {
        NoticeOccasionEnum noticeOccasion = NoticeOccasionEnum.getNoticeOccasion(run.getResult());

        MessageModel message = MessageModel.builder().type(type)
                .statusType(noticeOccasion.buildStatus())
                .title(envVars.expand(StringUtils.defaultIfBlank(title, DEFAULT_TITLE)))
                .text(envVars.expand(Utils.join(text))).buttons(buildButtons(run, envVars))
                .messageUrl(envVars.expand(messageUrl))
                .picUrl(envVars.expand(picUrl))
                .singleTitle(envVars.expand(singleTitle))
                .singleUrl(envVars.expand(singleUrl))
                .btnOrientation(isVerticalButton() ? "0" : "1")
                .atAll(atAll).atUserIds(ats)
                .build();

        return service.send(listener, envVars.expand(robot), message);
    }

    /**
     * Constructs a list of buttons to be included in the message.
     *
     * @param run     The context information of the Jenkins job at runtime.
     * @param envVars The environment variables during the Jenkins job execution.
     * @return A list of buttons to be included in the message.
     */
    private List<Button> buildButtons(Run<?, ?> run, EnvVars envVars) {
        if (MsgTypeEnum.CARD.equals(type) && CollectionUtils.isEmpty(buttons) && StringUtils.isNotBlank(singleTitle)) {
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
     * Descriptor implementation for the DingTalkStep.
     * This class provides Jenkins with information about the DingTalkStep, such as the required context,
     * the function name to be used in Jenkinsfiles, and the display name shown in the Jenkins UI.
     */
    @Extension(optional = true)
    public static class DingTalkStepDescriptor extends StepDescriptor implements Serializable {

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
            return "dingTalk";
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
            return "DingTalk Notice";
        }
    }

}