package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.hr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 水平分割线组件（hr）
 * 用于在卡片中展示一条分割线
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HrElement {

    /**
     * 组件标签，固定为 "hr"
     */
    @JsonProperty("tag")
    private final String tag = "hr";

}