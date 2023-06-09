package io.jenkins.plugins.feishu.notification.sdk.model.entity.support;

import lombok.Data;

/**
 * Action
 *
 * @author xm.z
 */
@Data
public class Action {
    private String type = "default";

    /**
     * 标签类型
     */
    private String tag = "button";

    /**
     * 信息文本
     */
    private LarkMdText text;

    /**
     * 跳转地址
     */
    private String url;
}
