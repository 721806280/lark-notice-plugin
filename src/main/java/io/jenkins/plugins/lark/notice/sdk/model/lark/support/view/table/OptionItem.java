package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.table;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 表格选项项（options）
 * 用于表示选项类型的单元格内容
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OptionItem {
    /**
     * 选项文本
     */
    private String text;

    /**
     * 选项颜色
     */
    private String color;
}