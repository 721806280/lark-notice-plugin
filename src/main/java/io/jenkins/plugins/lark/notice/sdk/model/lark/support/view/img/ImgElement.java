package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.img;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.TextElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.title.TitleElement;
import lombok.Data;

/**
 * 图片组件（img）
 * 用于在卡片中展示图片
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImgElement {
    /**
     * 组件标签，固定为 "img"
     */
    private String tag = "img";

    /**
     * 图片的 Key。可通过上传图片接口或在搭建工具中上传图片后获得。
     */
    @JsonProperty("img_key")
    private String imgKey;

    /**
     * 光标悬浮在图片上时展示的说明文本
     */
    private TextElement alt;

    /**
     * 图片标题
     */
    private TitleElement title;

    /**
     * 图片的圆角半径，单位是像素（px）
     */
    @JsonProperty("corner_radius")
    private String cornerRadius;

    /**
     * 图片的裁剪模式，当 size 字段的比例和图片的比例不一致时会触发裁剪
     */
    @JsonProperty("scale_type")
    private String scaleType;

    /**
     * 图片尺寸。仅在 scale_type 字段为 crop_center 或 crop_top 时生效
     */
    private String size;

    /**
     * 是否为透明底色。默认为 false，即图片为白色底色
     */
    private Boolean transparent;

    /**
     * 点击后是否放大图片。默认值为 true
     */
    private Boolean preview;

}