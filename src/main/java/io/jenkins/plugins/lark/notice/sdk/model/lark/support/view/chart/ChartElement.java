package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.chart;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * 图表组件（chart）
 * 适用于飞书客户端 7.1 及以上版本，支持嵌入基于 VChart 的图表
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartElement {
    /**
     * 组件标签，固定为 "chart"
     */
    private final String tag = "chart";

    /**
     * 图表宽高比。格式为 "x:y"，如 "16:9"
     */
    @JsonProperty("aspect_ratio")
    private String aspectRatio;

    /**
     * 图表主题。默认值 brand
     */
    @JsonProperty("color_theme")
    private String colorTheme;

    /**
     * 基于 VChart 的图表定义，格式为 JSON 对象
     * 详细用法请参考 VChart 官方文档
     */
    @JsonProperty("chart_spec")
    private Map<String, Object> chartSpec;

    /**
     * 是否支持独立窗口查看图表，默认值 true
     */
    private Boolean preview;

    /**
     * 图表组件的高度，默认值 auto（飞书客户端 7.10+ 支持）
     * 可选值：auto / [具体像素值]
     */
    private String height;
}
