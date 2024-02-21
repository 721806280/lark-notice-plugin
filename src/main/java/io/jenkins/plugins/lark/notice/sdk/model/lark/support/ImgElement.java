package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ImgElement
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImgElement {

    /**
     * 标签类型
     */
    private String tag = "img";

    /**
     * 图片的key
     */
    @JsonProperty(value = "img_key")
    private String imgKey;

    /**
     * 图片显示模式
     * <p>
     * crop_center 居中裁剪模式，对长图会限高，并居中裁剪后展示
     * fit_horizontal：平铺模式，宽度撑满卡片完整展示上传的图片
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
    @JsonProperty(value = "compact_width")
    private Boolean compactWidth;

    /**
     * 自定义图片的最大展示宽度。
     * <p>
     * 默认展示宽度撑满卡片的通栏图片，可在278px~580px范围内指定最大展示宽度。
     * </p>
     */
    @JsonProperty(value = "custom_width")
    private Integer customWidth;

    /**
     * hover图片时弹出的说明文案
     */
    private Alt alt;

}
