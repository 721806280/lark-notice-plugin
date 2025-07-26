package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 表格列定义
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableColumn {
    /**
     * 自定义列的标记（必填）
     */
    private String name;

    /**
     * 列名称（为空时不展示）
     */
    @JsonProperty("display_name")
    private String displayName;

    /**
     * 列宽
     */
    private String width;

    /**
     * 列的数据类型
     */
    @JsonProperty("data_type")
    private String dataType;

    /**
     * 数值格式（仅用于 number 类型）
     */
    private ColumnFormat format;

    /**
     * 日期格式（仅用于 date 类型）
     */
    @JsonProperty("date_format")
    private String dateFormat;

    /**
     * 垂直对齐方式
     */
    @JsonProperty("vertical_align")
    private String verticalAlign;

    /**
     * 水平对齐方式
     */
    @JsonProperty("horizontal_align")
    private String horizontalAlign;
}