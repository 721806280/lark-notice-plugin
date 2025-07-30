package io.jenkins.plugins.lark.notice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Model for storing image-related information. This class encapsulates properties related to an image,
 * such as a unique identifier (imgKey), title, corner radius, scaling type, size, transparency, preview behavior,
 * and alternative text content for hover actions. It extends {@link AbstractDescribableImpl}
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
     * Title of the image, which can provide context or description for the image.
     */
    private String title;

    /**
     * Corner radius of the image, specified in pixels (px) or percentage (%).
     * This property allows rounding the corners of the image to fit design requirements.
     */
    @JsonProperty("corner_radius")
    private String cornerRadius;

    /**
     * Scaling type of the image, determining how the image should be scaled or cropped when its aspect ratio does not match the desired size.
     * <p>
     * Supported values:
     * 1. crop_center: Crops the image from the center.
     * 2. crop_top: Crops the image from the top.
     * 3. fit_horizontal: Fits the image horizontally without cropping.
     */
    @JsonProperty("scale_type")
    private String scaleType;

    /**
     * Size of the image. This property is effective only when 'scale_type' is set to 'crop_center' or 'crop_top'.
     * <p>
     * Supported values:
     * 1. stretch: For images with an aspect ratio less than 16:9.
     * 2. large: 160x160 pixels, suitable for mixed image layouts.
     * 3. medium: 80x80 pixels, suitable for cover images in text-image combinations.
     * 4. small: 40x40 pixels, suitable for user avatars.
     * 5. tiny: 16x16 pixels, suitable for icons or notes.
     * 6. [width]px [height]px: Custom dimensions in pixels.
     */
    private String size;

    /**
     * Indicates if the image has a transparent background. Defaults to false, indicating a white background.
     */
    private Boolean transparent;

    /**
     * Indicates whether clicking on the image will enlarge it. Defaults to true.
     */
    private Boolean preview;

    /**
     * Alternative content text that appears when hovering over the image. Useful for accessibility or providing additional context.
     */
    private String altContent;

    /**
     * Constructor for creating a new instance of ImgModel.
     *
     * @param imgKey       Unique key for referencing the image.
     * @param title        Title of the image.
     * @param cornerRadius Radius for rounding the image's corners.
     * @param scaleType    Scaling method applied to the image.
     * @param size         Desired size of the image.
     * @param transparent  Whether the image should have a transparent background.
     * @param preview      Whether the image should be enlarged upon clicking.
     * @param altContent   Text to display when hovering over the image.
     */
    @DataBoundConstructor
    public ImgModel(String imgKey, String title, String cornerRadius, String scaleType, String size,
                    Boolean transparent, Boolean preview, String altContent) {
        this.imgKey = imgKey;
        this.title = title;
        this.cornerRadius = cornerRadius;
        this.scaleType = scaleType;
        this.size = size;
        this.transparent = transparent;
        this.preview = preview;
        this.altContent = altContent;
    }

    /**
     * Descriptor class for {@link ImgModel}. This class describes the configurable properties of ImgModel
     * within the Jenkins UI, facilitating the management of plugin configurations through the Jenkins interface.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ImgModel> {
        // Implementation details for configuring ImgModel via Jenkins UI
    }
}