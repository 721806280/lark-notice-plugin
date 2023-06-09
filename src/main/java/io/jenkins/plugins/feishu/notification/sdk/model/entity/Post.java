package io.jenkins.plugins.feishu.notification.sdk.model.entity;

import io.jenkins.plugins.feishu.notification.sdk.model.entity.support.RichText;
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