package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.table;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 表格列格式化配置（仅用于 number 类型）
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ColumnFormat {
    /**
     * 数字前展示的货币单位
     */
    private String symbol;

    /**
     * 数字的小数点位数
     */
    private Integer precision;

    /**
     * 是否生效按千分位逗号分割的数字样式
     */
    private Boolean separator;
}