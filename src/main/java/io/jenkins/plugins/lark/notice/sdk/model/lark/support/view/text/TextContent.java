package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.text;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 普通文本内容配置（用于 div 组件的 text 字段）
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextContent {
    /**
     * 文本类型标签：plain_text 或 lark_md
     */
    private String tag;

    /**
     * 文本内容。当 tag 为 lark_md 时，支持部分 Markdown 语法
     */
    private String content;

    /**
     * 文本大小。默认值 normal
     */
    @JsonProperty("text_size")
    private String textSize;

    /**
     * 文本颜色。仅在 tag 为 plain_text 时生效。默认值 default
     */
    @JsonProperty("text_color")
    private String textColor;

    /**
     * 文本对齐方式。默认值 left
     */
    @JsonProperty("text_align")
    private String textAlign;

    /**
     * 内容最大显示行数，超出显示 ...
     */
    private Integer lines;
}