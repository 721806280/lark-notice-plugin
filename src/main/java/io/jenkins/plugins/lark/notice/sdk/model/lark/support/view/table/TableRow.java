package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.table;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

/**
 * 表格行数据
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableRow {
    /**
     * 使用 name: value 的方式定义每一列的数据
     */
    @JsonAnySetter
    private Map<String, Object> rowData;
}