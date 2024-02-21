package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

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
    private TagContent text;

    /**
     * 跳转地址
     */
    private String url;
}
