package io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.button;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.ConfirmDialog;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.TextElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.icon.Icon;
import lombok.Data;

import java.util.List;

/**
 * 按钮组件（button）
 * 支持多种类型、尺寸、交互行为、确认弹窗等
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ButtonElement {
    /**
     * 组件标签，固定为 "button"
     */
    private final String tag = "button";

    /**
     * 按钮类型，默认 default
     */
    private String type;

    /**
     * 按钮尺寸，默认 medium
     */
    private String size;

    /**
     * 按钮宽度，默认 default
     */
    private String width;

    /**
     * 按钮上的文本
     */
    private TextElement text;

    /**
     * 前缀图标
     */
    private Icon icon;

    /**
     * 用户悬浮在按钮上时的提示文案
     */
    @JsonProperty("hover_tips")
    private TextElement hoverTips;

    /**
     * 是否禁用该按钮，默认 false
     */
    private Boolean disabled;

    /**
     * 按钮禁用时的提示文案
     */
    @JsonProperty("disabled_tips")
    private TextElement disabledTips;

    /**
     * 二次确认弹窗配置
     */
    private ConfirmDialog confirm;

    /**
     * 按钮交互行为列表（open_url / callback / form_action）
     */
    private List<ButtonBehavior> behaviors;

}
