package io.jenkins.plugins.lark.notice.sdk.model.lark;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 富文本消息 类型
 *
 * @author xm.z
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LarkPostMessage extends BaseLarkMessage {

    private PostContent content;

    public LarkPostMessage(PostContent content) {
        this.content = content;
        setMsgType("post");
    }

    public static LarkPostMessage build(String title, String text) {
        Content content = new Content();
        content.setTitle(title);
        content.setContent(JsonUtils.readTree(text));
        RichText richText = new RichText(content);
        PostContent postContent = new PostContent(richText);
        return new LarkPostMessage(postContent);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostContent implements Serializable {

        /**
         * 富文本消息
         */
        private RichText post;

    }

    /**
     * Content
     *
     * @author xm.z
     */
    @Data
    public static class Content implements Serializable {

        private String title;

        private JsonNode content;

    }

    /**
     * RichText
     *
     * @author xm.z
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RichText implements Serializable {

        @JsonProperty(value = "zh_cn")
        private Content zhCn;

    }

}