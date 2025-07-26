package io.jenkins.plugins.lark.notice.sdk.model.ding;

import io.jenkins.plugins.lark.notice.sdk.model.lark.support.at.At;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author xm.z
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DingMdMessage extends BaseDingMessage {

    private At at;

    private MarkdownContent markdown;

    public DingMdMessage(At at, MarkdownContent markdown) {
        this.at = at;
        this.markdown = markdown;
        setMsgType("markdown");
    }

    public static DingMdMessage build(At at, String title, String text) {
        MarkdownContent content = new MarkdownContent(title, addAtInfo(text, at, true));
        return new DingMdMessage(at, content);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarkdownContent implements Serializable {

        private String title;

        private String text;

    }

}
