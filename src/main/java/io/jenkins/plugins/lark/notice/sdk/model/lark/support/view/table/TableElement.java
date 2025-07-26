package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.table;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 表格组件（table）
 * 用于在卡片中展示结构化表格数据
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableElement {
    /**
     * 组件标签，固定为 "table"
     */
    private final String tag = "table";

    /**
     * 每页最大展示的数据行数。支持 [1,10] 整数，默认值 5
     */
    @JsonProperty("page_size")
    private Integer pageSize;

    /**
     * 行高设置，默认值 low
     */
    private String rowHeight;

    /**
     * 是否冻结首列，默认 false
     */
    @JsonProperty("freeze_first_column")
    private Boolean freezeFirstColumn;

    /**
     * 表头样式配置
     */
    @JsonProperty("header_style")
    private HeaderStyle headerStyle;

    /**
     * 表格列定义列表
     */
    private List<TableColumn> columns;

    /**
     * 表格行数据列表
     */
    private List<Map<String, Object>> rows;
}