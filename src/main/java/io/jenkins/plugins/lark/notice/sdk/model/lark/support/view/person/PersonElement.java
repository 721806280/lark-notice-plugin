package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.person;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 人员组件（person）
 * 用于在卡片中展示一个用户的信息，包括头像、用户名等
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonElement {
    /**
     * 组件标签，固定为 "person"
     */
    private final String tag = "person";

    /**
     * 人员头像尺寸。默认值为 medium。
     * 可选值：extra_small / small / medium / large / extra_large
     */
    private String size;

    /**
     * 人员的用户 ID
     */
    @JsonProperty("user_id")
    private String userId;

    /**
     * 是否展示人员的头像。默认为 true
     */
    @JsonProperty("show_avatar")
    private Boolean showAvatar;

    /**
     * 是否展示人员的用户名。默认为 false
     */
    @JsonProperty("show_name")
    private Boolean showName;

    /**
     * 人员组件的展示样式。默认为 normal（正常样式）
     * 可选值：normal / capsule（胶囊样式）
     */
    private String style;
}
