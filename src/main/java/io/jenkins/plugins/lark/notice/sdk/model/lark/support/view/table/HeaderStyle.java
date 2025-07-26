package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 表格表头样式配置
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HeaderStyle {
    /**
     * 文本对齐方式
     */
    @JsonProperty("text_align")
    private String textAlign;

    /**
     * 字号
     */
    @JsonProperty("text_size")
    private String textSize;

    /**
     * 背景色
     */
    @JsonProperty("background_style")
    private String backgroundStyle;

    /**
     * 文本颜色
     */
    @JsonProperty("text_color")
    private String textColor;

    /**
     * 是否加粗
     */
    private Boolean bold;

    /**
     * 文本行数
     */
    private Integer lines;
}