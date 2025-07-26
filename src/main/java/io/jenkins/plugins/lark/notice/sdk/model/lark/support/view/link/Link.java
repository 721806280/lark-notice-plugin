package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.link;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 卡片整体的跳转链接
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Link {
    /**
     * 默认链接地址。未配置指定端地址时，该配置生效
     */
    private String url;

    /**
     * Android 客户端跳转链接
     */
    @JsonProperty("android_url")
    private String androidUrl;

    /**
     * iOS 客户端跳转链接
     */
    @JsonProperty("ios_url")
    private String iosUrl;

    /**
     * PC 客户端跳转链接
     */
    @JsonProperty("pc_url")
    private String pcUrl;
}