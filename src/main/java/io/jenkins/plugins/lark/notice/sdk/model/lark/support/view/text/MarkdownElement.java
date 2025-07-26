package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.text;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.icon.Icon;
import lombok.Data;

/**
 * 富文本组件（markdown）
 * 支持飞书自定义 Markdown 语法
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarkdownElement {

    /**
     * 组件标签，固定为 "markdown"
     */
    private String tag = "markdown";

    /**
     * 文本大小。默认值 normal
     */
    @JsonProperty("text_size")
    private String textSize;

    /**
     * 文本对齐方式。默认值 left
     */
    @JsonProperty("text_align")
    private String textAlign;

    /**
     * 前缀图标
     */
    private Icon icon;

    /**
     * 采用飞书 Markdown 语法编写的内容
     */
    private String content;
}