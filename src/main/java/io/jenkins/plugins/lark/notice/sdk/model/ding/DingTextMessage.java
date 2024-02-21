package io.jenkins.plugins.lark.notice.sdk.model.ding;

import io.jenkins.plugins.lark.notice.sdk.model.lark.support.At;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author xm.z
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DingTextMessage extends BaseDingMessage {

    private At at;

    private TextContent text;

    public DingTextMessage(At at, TextContent text) {
        this.at = at;
        this.text = text;
        setMsgType("text");
    }

    public static DingTextMessage build(At at, String text) {
        TextContent content = new TextContent(addAtInfo(text, at, false));
        return new DingTextMessage(at, content);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextContent {

        private String content;

    }

}
