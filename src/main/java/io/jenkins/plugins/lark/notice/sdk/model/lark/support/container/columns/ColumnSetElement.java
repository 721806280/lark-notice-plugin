package io.jenkins.plugins.lark.notice.sdk.model.lark.support.container.columns;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.link.Link;
import lombok.Data;

import java.util.List;

/**
 * 分栏容器组件（column_set）
 * 支持多列布局、样式、交互
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ColumnSetElement {
    /**
     * 组件标签，固定为 "column_set"
     */
    private final String tag = "column_set";

    /**
     * 操作组件的唯一标识
     */
    @JsonProperty("element_id")
    private String elementId;

    /**
     * 分栏的外边距，默认 0px
     */
    private String margin;

    /**
     * 分栏内组件之间的水平间距，默认 medium（8px）
     */
    @JsonProperty("horizontal_spacing")
    private String horizontalSpacing;

    /**
     * 分栏内组件水平对齐方式，默认 left
     */
    @JsonProperty("horizontal_align")
    private String horizontalAlign;

    /**
     * 移动端和 PC 窄屏下的列自适应方式，默认 none
     */
    @JsonProperty("flex_mode")
    private String flexMode;

    /**
     * 分栏的背景色样式，默认 default
     */
    @JsonProperty("background_style")
    private String backgroundStyle;

    /**
     * 点击分栏时的跳转行为配置
     */
    private Link action;

    /**
     * 分栏中的列容器列表
     */
    private List<ColumnElement> columns;
}