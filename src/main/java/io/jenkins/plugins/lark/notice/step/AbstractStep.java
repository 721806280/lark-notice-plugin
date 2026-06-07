package io.jenkins.plugins.lark.notice.step;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.model.ButtonModel;
import io.jenkins.plugins.lark.notice.model.ImgModel;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Button;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.TextElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.img.ImgElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.title.TitleElement;
import jenkins.model.Jenkins;
import lombok.Getter;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AbstractStep is an abstract class that extends the Step class.
 * It provides common functionality and properties for different types of steps.
 *
 * @author xm.z
 */
@Getter
public abstract class AbstractStep extends Step {

    protected final MessageDispatcher service = MessageDispatcher.getInstance();

    protected final String rootPath = Jenkins.get().getRootUrl();

    protected String robot;

    protected MsgTypeEnum type;

    public AbstractStep(String robot, MsgTypeEnum type) {
        this.robot = robot;
        this.type = type;
    }

    protected abstract SendResult send(Run<?, ?> run, EnvVars envVars, TaskListener listener);

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new GenericStepExecution<>(this, context);
    }

    protected String expandNullable(EnvVars envVars, String value) {
        return value == null ? null : envVars.expand(value);
    }

    protected ImgElement buildImg(EnvVars envVars, ImgModel imgModel) {
        if (imgModel == null) {
            return null;
        }
        ImgElement imgElement = new ImgElement();
        imgElement.setImgKey(expandNullable(envVars, imgModel.getImgKey()));
        imgElement.setAlt(TextElement.of(expandNullable(envVars, imgModel.getAltContent())));
        imgElement.setTitle(TitleElement.buildPlainText(expandNullable(envVars, imgModel.getTitle())));
        imgElement.setCornerRadius(imgModel.getCornerRadius());
        imgElement.setScaleType(imgModel.getScaleType());
        imgElement.setSize(imgModel.getSize());
        imgElement.setTransparent(imgModel.getTransparent());
        imgElement.setPreview(imgModel.getPreview());
        return imgElement;
    }

    protected List<Button> expandButtons(EnvVars envVars, List<ButtonModel> buttons) {
        if (CollectionUtils.isEmpty(buttons)) {
            return null;
        }
        return buttons.stream().map(item ->
                new Button(envVars.expand(item.getTitle()), envVars.expand(item.getUrl()), item.getType())
        ).collect(Collectors.toList());
    }

    /**
     * Base StepDescriptor that provides the shared required-context set.
     */
    public abstract static class AbstractStepDescriptor extends StepDescriptor implements Serializable {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class, EnvVars.class);
        }
    }
}