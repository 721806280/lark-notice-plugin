package io.jenkins.plugins.lark.notice.sdk.model.lark.support.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 卡片配置项
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Config {
    /**
     * 卡片是否处于流式更新模式，默认值为 false
     */
    @JsonProperty("streaming_mode")
    private Boolean streamingMode;

    /**
     * 流式更新配置，详情参考相关文档
     */
    @JsonProperty("streaming_config")
    private Map<String, Object> streamingConfig;

    /**
     * 卡片摘要信息。可通过该参数自定义客户端聊天栏消息预览中的展示文案
     */
    private Summary summary;

    /**
     * JSON 2.0 新增属性。用于指定生效的语言。如果配置 locales，则只有 locales 中的语言会生效
     */
    private List<String> locales;

    /**
     * 是否支持转发卡片。默认值为 true
     */
    @JsonProperty("enable_forward")
    private boolean enableForward;

    /**
     * 是否为共享卡片。默认值为 true，JSON 2.0 暂时仅支持设为 true，即更新卡片的内容对所有收到这张卡片的人员可见
     */
    @JsonProperty("update_multi")
    private Boolean updateMulti;

    /**
     * 卡片宽度模式。支持 "compact"（紧凑宽度 400px）模式 或 "fill"（撑满聊天窗口宽度）模式。默认不填时的宽度为 600px
     */
    @JsonProperty("width_mode")
    private String widthMode;

    /**
     * 是否使用自定义翻译数据。默认值 false。为 true 时，在用户点击消息翻译后，使用 i18n 对应的目标语种作为翻译结果。
     * 若 i18n 取不到，则使用当前内容请求翻译，不使用自定义翻译数据
     */
    @JsonProperty("use_custom_translation")
    private Boolean useCustomTranslation;

    /**
     * 转发的卡片是否仍然支持回传交互。默认值 false
     */
    @JsonProperty("enable_forward_interaction")
    private Boolean enableForwardInteraction;

    /**
     * 添加自定义字号和颜色。可应用在组件 JSON 数据中，设置字号和颜色属性
     */
    private Style style;

}