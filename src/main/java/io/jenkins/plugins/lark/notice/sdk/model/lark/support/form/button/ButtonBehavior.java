package io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.button;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * 按钮交互行为配置
 * 支持打开链接、回传数据、表单事件等
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ButtonBehavior {
    /**
     * 行为类型，如 open_url / callback / form_action
     */
    private String type;

    /**
     * 兜底跳转地址
     */
    @JsonProperty("default_url")
    private String defaultUrl;

    /**
     * 安卓端跳转地址
     */
    @JsonProperty("android_url")
    private String androidUrl;

    /**
     * iOS 端跳转地址
     */
    @JsonProperty("ios_url")
    private String iosUrl;

    /**
     * 桌面端跳转地址
     */
    @JsonProperty("pc_url")
    private String pcUrl;

    /**
     * 回传数据，支持 string 或 object
     */
    private Map<String, Object> value;

    /**
     * 表单事件类型，默认为 submit
     */
    private String behavior;
}
