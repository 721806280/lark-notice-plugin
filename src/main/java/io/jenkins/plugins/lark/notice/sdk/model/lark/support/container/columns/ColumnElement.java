package io.jenkins.plugins.lark.notice.sdk.model.lark.support.container.columns;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.link.Link;
import lombok.Data;

import java.util.List;

/**
 * 列容器组件（column）
 * 支持布局、样式、交互及嵌套子组件
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ColumnElement {

    /**
     * 组件标签，固定为 "column"
     */
    private final String tag = "column";

    /**
     * 操作组件的唯一标识
     */
    @JsonProperty("element_id")
    private String elementId;

    /**
     * 列的背景色样式，默认 default
     */
    @JsonProperty("background_style")
    private String backgroundStyle;

    /**
     * 列的宽度，默认 auto
     */
    private String width;

    /**
     * 当 width 为 weighted 时生效，表示当前列的宽度占比
     */
    private Integer weight;

    /**
     * 列内组件之间的水平间距，默认 medium（8px）
     */
    @JsonProperty("horizontal_spacing")
    private String horizontalSpacing;

    /**
     * 列内组件水平对齐方式，默认 left
     */
    @JsonProperty("horizontal_align")
    private String horizontalAlign;

    /**
     * 列内组件垂直对齐方式，默认 top
     */
    @JsonProperty("vertical_align")
    private String verticalAlign;

    /**
     * 列内组件纵向间距，默认 default（8px）
     */
    @JsonProperty("vertical_spacing")
    private String verticalSpacing;

    /**
     * 列的排列方向，默认 vertical（垂直）
     */
    private String direction;

    /**
     * 列的内边距，默认 0px
     */
    private String padding;

    /**
     * 列的外边距，默认 0px
     */
    private String margin;

    /**
     * 点击列时的跳转行为配置
     */
    private Link action;

    /**
     * 列容器内嵌的组件（如 text、button、img 等）
     */
    private List<?> elements;

}
