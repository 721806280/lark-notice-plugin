package io.jenkins.plugins.sdk.entity;

import io.jenkins.plugins.sdk.entity.support.RichText;
import lombok.Data;

/**
 * 富文本消息 类型
 *
 * @author xm.z
 */
@Data
public class Post {

    /**
     * 富文本消息
     */
    private RichText post;

}