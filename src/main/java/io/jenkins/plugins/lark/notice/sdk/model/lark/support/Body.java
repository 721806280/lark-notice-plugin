package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 卡片正文内容
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Body {
    /**
     * 正文或容器内组件的排列方向。可选值："vertical"（垂直排列）、"horizontal"（水平排列）。默认为 "vertical"
     */
    private String direction;

    /**
     * 正文或容器内组件的内边距，支持范围 [0,99]px
     */
    private String padding;

    /**
     * 正文或容器内组件的水平间距，可选值："small"(4px)、"medium"(8px)、"large"(12px)、"extra_large"(16px)或[0,99]px
     */
    @JsonProperty("horizontal_spacing")
    private String horizontalSpacing;

    /**
     * 正文或容器内组件的水平对齐方式，可选值："left"、"center"、"right"。默认值为 "left"
     */
    @JsonProperty("horizontal_align")
    private String horizontalAlign;

    /**
     * 正文或容器内组件的垂直间距，可选值："small"(4px)、"medium"(8px)、"large"(12px)、"extra_large"(16px)或[0,99]px
     */
    @JsonProperty("vertical_spacing")
    private String verticalSpacing;

    /**
     * 正文或容器内组件的垂直对齐方式，可选值："top"、"center"、"bottom"，默认值为 "top"
     */
    @JsonProperty("vertical_align")
    private String verticalAlign;

    /**
     * 在此传入各个组件的 JSON 数据，组件将按数组顺序纵向流式排列
     */
    private List<?> elements;
}