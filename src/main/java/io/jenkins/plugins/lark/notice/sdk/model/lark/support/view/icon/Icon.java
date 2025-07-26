package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.icon;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 图标配置
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Icon {
    /**
     * 图标类型
     */
    private String tag;

    /**
     * 图标的 token。仅在 tag 为 standard_icon 时生效
     */
    private String token;

    /**
     * 图标颜色。仅在 tag 为 standard_icon 时生效
     */
    private String color;

    /**
     * 图片的 key。仅在 tag 为 custom_icon 时生效
     */
    @JsonProperty("img_key")
    private String imgKey;
}