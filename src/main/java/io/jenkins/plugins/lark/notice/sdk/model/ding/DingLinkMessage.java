package io.jenkins.plugins.lark.notice.sdk.model.ding;

import io.jenkins.plugins.lark.notice.sdk.model.lark.support.At;
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
public class DingLinkMessage extends BaseDingMessage {

    private At at;

    private LinkContent link;

    public DingLinkMessage(At at, LinkContent link) {
        this.at = at;
        this.link = link;
        setMsgType("link");
    }

    public static DingLinkMessage build(At at, String title, String text, String picUrl, String messageUrl) {
        LinkContent content = new LinkContent(title, text, picUrl, messageUrl);
        return new DingLinkMessage(at, content);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkContent implements Serializable {

        /**
         * 消息标题
         */
        private String title;

        /**
         * 消息内容
         */
        private String text;

        /**
         * 图片URL
         */
        private String picUrl;

        /**
         * 点击消息跳转的URL
         */
        private String messageUrl;

    }

}
