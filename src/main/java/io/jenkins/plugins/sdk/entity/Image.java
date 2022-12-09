package io.jenkins.plugins.sdk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 图片消息 类型
 *
 * @author xm.z
 */
@Data
public class Image {

    /**
     * 图片的key
     */
    @JsonProperty(value = "image_key")
    private String imageKey;

}