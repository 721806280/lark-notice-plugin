package io.jenkins.plugins.lark.notice.sdk.model.lark;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 群名片消息 类型
 *
 * @author xm.z
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LarkShareChatMessage extends BaseLarkMessage {

    private ShareChatContent content;

    public LarkShareChatMessage(ShareChatContent content) {
        this.content = content;
        setMsgType("share_chat");
    }

    public static LarkShareChatMessage build(String text) {
        ShareChatContent content = new ShareChatContent(text);
        return new LarkShareChatMessage(content);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareChatContent {

        @JsonProperty(value = "share_chat_id")
        private String shareChatId;

    }

}