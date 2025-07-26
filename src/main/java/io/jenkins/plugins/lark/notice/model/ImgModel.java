package io.jenkins.plugins.lark.notice.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Model for storing image-related information. This class encapsulates properties related to an image,
 * such as a unique identifier (imgKey), display mode, whether it should be displayed in a compact width,
 * custom maximum display width, and alternative text content for hover actions. It extends {@link AbstractDescribableImpl}
 * to facilitate integration with Jenkins' configuration system, allowing images to be dynamically configured within Jenkins plugins or jobs.
 *
 * @author xm.z
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ImgModel extends AbstractDescribableImpl<ImgModel> {

    /**
     * The unique key associated with the image. This key is typically used to fetch or reference the image.
     */
    @SuppressWarnings("lgtm[jenkins/plaintext-storage]")
    private String imgKey;

    /**
     * Alternative content text that appears when hovering over the image.
     */
    private String altContent;

    /**
     * Constructor for creating a new instance of an ImgModel with specified image key, display mode, compact width flag,
     * custom maximum width, and alternative hover content.
     *
     * @param imgKey       The unique key for the image.
     * @param altContent   The alternative content text for hover actions.
     */
    @DataBoundConstructor
    public ImgModel(String imgKey, String altContent) {
        this.imgKey = imgKey;
        this.altContent = altContent;
    }

    /**
     * Descriptor class for {@link ImgModel}. This class is used to describe the properties of the ImgModel
     * that are configurable in Jenkins UI. By extending {@link Descriptor}, it integrates with Jenkins' system
     * for managing plugin configurations, allowing users to specify values for imgKey, mode, compactWidth, customWidth,
     * and altContent through the Jenkins interface.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ImgModel> {

    }

}