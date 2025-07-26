package io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.ConfirmDialog;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.TextElement;
import lombok.Data;

import java.util.Map;

/**
 * 输入框组件（input）
 * 支持多种输入类型、占位符、默认值、确认弹窗等
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InputElement {

    /**
     * 输入框的标签，固定为 "input"
     */
    private final String tag = "input";

    /**
     * 输入框的唯一标识，用于识别用户提交的文本
     */
    private String name;

    /**
     * 输入框内容是否必填（仅在表单容器中生效）
     */
    private Boolean required;

    /**
     * 输入框占位文本
     */
    private TextElement placeholder;

    /**
     * 输入框默认值
     */
    @JsonProperty("default_value")
    private String defaultValue;

    /**
     * 是否禁用该输入框
     */
    private Boolean disabled;

    /**
     * 输入框宽度
     */
    private String width;

    /**
     * 输入框最大文本长度，默认 1000
     */
    @JsonProperty("max_length")
    private Integer maxLength;

    /**
     * 输入类型，如 text / multiline_text / password
     */
    @JsonProperty("input_type")
    private String inputType;

    /**
     * 多行文本展示行数
     */
    private Integer rows;

    /**
     * 多行文本是否自适应高度（仅 PC 端生效）
     */
    @JsonProperty("auto_resize")
    private Boolean autoResize;

    /**
     * 最大展示行数（仅当 auto_resize 为 true 时有效）
     */
    @JsonProperty("max_rows")
    private Integer maxRows;

    /**
     * 是否展示前缀图标（仅密码类型有效）
     */
    @JsonProperty("show_icon")
    private Boolean showIcon;

    /**
     * 输入框的文本标签（提示用户填写内容）
     */
    private TextElement label;

    /**
     * 文本标签的位置，默认 top
     */
    @JsonProperty("label_position")
    private String labelPosition;

    /**
     * 回传数据，支持 string 或 object
     */
    private Map<String, Object> value;

    /**
     * 二次确认弹窗配置
     */
    private ConfirmDialog confirm;

    /**
     * 降级文案配置
     */
    private FallbackElement fallback;
}
