package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.title;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 标题后缀标签
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextTag {
    /**
     * 组件类型，固定为 text_tag
     */
    private String tag;

    /**
     * 操作元素的唯一标识。用于在调用组件相关接口中指定元素。需开发者自定义
     */
    @JsonProperty("element_id")
    private String elementId;

    /**
     * 标签内容
     */
    private TitleElement text;

    /**
     * 标签颜色
     */
    private String color;
}