package io.jenkins.plugins.lark.notice.sdk.model.lark.support.form;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 文本组件（plain_text）
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextElement {
    /**
     * 文本类型标签，固定为 "plain_text"
     */
    private final String tag = "plain_text";

    /**
     * 文本内容
     */
    private String content;

    public static TextElement of(String text) {
        if (text == null) {
            return null;
        }

        TextElement textElement = new TextElement();
        textElement.setContent(text);
        return textElement;
    }
}
