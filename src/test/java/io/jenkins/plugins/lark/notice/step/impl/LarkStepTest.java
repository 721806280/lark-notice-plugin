package io.jenkins.plugins.lark.notice.step.impl;

import hudson.EnvVars;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.model.ImgModel;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.img.ImgElement;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LarkStepTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void buildImgShouldExpandEnvironmentVariablesForAllDisplayFields() throws Exception {
        LarkStep step = new LarkStep("robot-a", MsgTypeEnum.CARD);
        ImgModel imgModel = new ImgModel(
                "${IMG_KEY}",
                "${IMG_TITLE}",
                "12px",
                "crop_center",
                "large",
                Boolean.TRUE,
                Boolean.TRUE,
                "${IMG_ALT}"
        );
        EnvVars envVars = new EnvVars();
        envVars.put("IMG_KEY", "img_v3_key");
        envVars.put("IMG_TITLE", "Build Report");
        envVars.put("IMG_ALT", "Preview image");

        Method buildImg = LarkStep.class.getDeclaredMethod("buildImg", EnvVars.class, ImgModel.class);
        buildImg.setAccessible(true);
        ImgElement imgElement = (ImgElement) buildImg.invoke(step, envVars, imgModel);

        assertNotNull(imgElement);
        assertEquals("img_v3_key", imgElement.getImgKey());
        assertEquals("Build Report", imgElement.getTitle().getContent());
        assertEquals("Preview image", imgElement.getAlt().getContent());
    }

    @Test
    public void descriptorShouldExposeStableMetadata() {
        LarkStep.LarkStepDescriptor descriptor = jenkins.jenkins.getDescriptorByType(LarkStep.LarkStepDescriptor.class);

        assertNotNull(descriptor);
        assertEquals("lark", descriptor.getFunctionName());
        assertEquals("lark notice", descriptor.getDisplayName());
    }
}
