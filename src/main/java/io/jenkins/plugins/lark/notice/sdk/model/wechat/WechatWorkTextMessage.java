package io.jenkins.plugins.lark.notice.sdk.model.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.at.At;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.List;

/**
 * WeCom text message.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WechatWorkTextMessage extends BaseWechatWorkMessage {

    private TextContent text;

    public WechatWorkTextMessage(TextContent text) {
        this.text = text;
        setMsgType("text");
    }

    public static WechatWorkTextMessage build(At at, String content) {
        TextContent textContent = new TextContent(content, null, null);
        if (at != null) {
            textContent.setMentionedList(at.getIsAtAll() ? List.of("@all") : at.getAtUserIds());
            textContent.setMentionedMobileList(at.getAtMobiles());
        }
        if (CollectionUtils.isEmpty(textContent.getMentionedList())) {
            textContent.setMentionedList(null);
        }
        if (CollectionUtils.isEmpty(textContent.getMentionedMobileList())) {
            textContent.setMentionedMobileList(null);
        }
        return new WechatWorkTextMessage(textContent);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextContent implements Serializable {

        private String content;

        @JsonProperty("mentioned_list")
        private List<String> mentionedList;

        @JsonProperty("mentioned_mobile_list")
        private List<String> mentionedMobileList;
    }
}
