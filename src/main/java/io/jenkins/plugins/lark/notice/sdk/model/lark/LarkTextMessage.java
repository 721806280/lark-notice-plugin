package io.jenkins.plugins.lark.notice.sdk.model.lark;

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
public class LarkTextMessage extends BaseLarkMessage {

    private TextContent content;

    public LarkTextMessage(TextContent content) {
        this.content = content;
        setMsgType("text");
    }

    public static LarkTextMessage build(At at, String text) {
        TextContent content = new TextContent(addAtInfo(text, at));
        return new LarkTextMessage(content);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextContent {

        private String text;

    }

}
