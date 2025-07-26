package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.img;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 多图混排组件（img_combination）
 * 用于在卡片中展示多张图片的组合排版
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImgCombinationElement {

    /**
     * 组件标签，固定为 "img_combination"
     */
    private String tag;

    /**
     * 多图混排的方式。例如：double（双图）、triple（三图）等
     */
    @JsonProperty("combination_mode")
    private String combinationMode;

    /**
     * 多图混排图片的圆角半径，单位是像素（px）
     */
    @JsonProperty("corner_radius")
    private String cornerRadius;

    /**
     * 图片资源数组，顺序与图片排列顺序一致
     */
    @JsonProperty("img_list")
    private List<ImgCombinationItem> imgList;

}