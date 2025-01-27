package io.jenkins.plugins.lark.notice.sdk.model.lark;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class LarkImageMessage extends BaseLarkMessage {

    private ImageContent content;

    public LarkImageMessage(ImageContent content) {
        this.content = content;
        setMsgType("image");
    }

    public static LarkImageMessage build(String text) {
        ImageContent content = new ImageContent(text);
        return new LarkImageMessage(content);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageContent implements Serializable {

        @JsonProperty("image_key")
        @SuppressWarnings("lgtm[jenkins/plaintext-storage]")
        private String imageKey;

    }

}
