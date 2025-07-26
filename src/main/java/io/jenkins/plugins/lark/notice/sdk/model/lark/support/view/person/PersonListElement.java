package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.person;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.icon.Icon;
import lombok.Data;

import java.util.List;

/**
 * 人员列表组件（person_list）
 * 用于在卡片中展示一组用户的信息（头像 + 用户名）
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonListElement {
    /**
     * 组件标签，固定为 "person_list"
     */
    private final String tag = "person_list";

    /**
     * 当人员列表中有无效用户 ID 时，是否忽略无效 ID。
     * 默认为 false，表示若存在无效用户 ID，将报错并返回无效的用户 ID 列表。
     */
    @JsonProperty("drop_invalid_user_id")
    private Boolean dropInvalidUserId;

    /**
     * 最大显示行数，默认关闭不限制最大显示行数。
     */
    private Integer lines;

    /**
     * 是否展示人员对应的用户名。
     */
    @JsonProperty("show_name")
    private Boolean showName;

    /**
     * 是否展示人员对应的头像。
     */
    @JsonProperty("show_avatar")
    private Boolean showAvatar;

    /**
     * 人员头像的尺寸。
     * 可选值：extra_small / small / medium / large / extra_large
     */
    private String size;

    /**
     * 人员列表。人员的 ID 支持 open_id、user_id、union_id
     */
    @JsonProperty("persons")
    private List<PersonSimpleElement> persons;

    /**
     * 前缀图标。
     */
    private Icon icon;

    /**
     * 图标库中的前缀图标，和 icon 同时设置时以 icon 为准
     */
    @JsonProperty("ud_icon")
    private UdIcon udIcon;
}