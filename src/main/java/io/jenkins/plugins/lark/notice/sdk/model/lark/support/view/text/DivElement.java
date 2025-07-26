package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.text;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.icon.Icon;
import lombok.Data;

/**
 * 普通文本组件（div）
 * 用于展示纯文本或简单格式文本
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DivElement {

    /**
     * 组件标签，固定为 "div"
     */
    private String tag;

    /**
     * 配置普通文本信息
     */
    private TextContent text;

    /**
     * 前缀图标
     */
    private Icon icon;

}