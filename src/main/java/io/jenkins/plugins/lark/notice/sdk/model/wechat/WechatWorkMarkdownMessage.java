package io.jenkins.plugins.lark.notice.sdk.model.wechat;

import io.jenkins.plugins.lark.notice.sdk.model.lark.support.at.At;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * WeCom markdown message.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WechatWorkMarkdownMessage extends BaseWechatWorkMessage {

    private MarkdownContent markdown;

    public WechatWorkMarkdownMessage(MarkdownContent markdown) {
        this.markdown = markdown;
        setMsgType("markdown");
    }

    public static WechatWorkMarkdownMessage build(At at, String content) {
        return new WechatWorkMarkdownMessage(new MarkdownContent(appendMarkdownAtInfo(content, at)));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarkdownContent implements Serializable {

        private String content;
    }
}
