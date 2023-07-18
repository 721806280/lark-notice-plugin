package io.jenkins.plugins.feishu.notification.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * 用于存储图片相关的模型。
 *
 * @author xm.z
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ImgModel extends AbstractDescribableImpl<ImgModel> {

    /**
     * 图片的key
     */
    private String imgKey;

    /**
     * 图片显示模式
     * <p>
     * crop_center: 居中裁剪模式，对长图会限高，并居中裁剪后展示
     * fit_horizontal: 平铺模式，宽度撑满卡片完整展示上传的图片
     * custom_width: 自定义宽度
     * compact_width: 紧凑宽度
     * </p>
     */
    private String mode;

    /**
     * 是否展示为紧凑型的图片。
     * <p>
     * 默认值为false，如果配置为true，则展示最大宽度为278px的紧凑型图片。
     * </p>
     */
    private boolean compactWidth;

    /**
     * 自定义图片的最大展示宽度。
     * <p>
     * 默认展示宽度撑满卡片的通栏图片，可在278px~580px范围内指定最大展示宽度。
     * </p>
     */
    private Integer customWidth;

    /**
     * hover图片时弹出的说明文案
     */
    private String altContent;

    /**
     * 构造函数。
     *
     * @param imgKey       图片的key
     * @param mode         图片显示模式
     * @param compactWidth 是否展示为紧凑型的图片
     * @param customWidth  customWidth
     * @param altContent   hover图片时弹出的说明文案
     */
    @DataBoundConstructor
    public ImgModel(String imgKey, String mode, boolean compactWidth, Integer customWidth, String altContent) {
        this.imgKey = imgKey;
        this.mode = mode;
        this.compactWidth = compactWidth;
        this.customWidth = customWidth;
        this.altContent = altContent;
    }

    /**
     * 描述符类，用于表示该模型的属性在 Jenkins 中的可配置选项。
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ImgModel> {

    }

}