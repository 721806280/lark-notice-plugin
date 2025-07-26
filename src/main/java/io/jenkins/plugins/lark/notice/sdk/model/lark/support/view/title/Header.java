package io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.title;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.icon.Icon;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 卡片头部信息，包含标题、副标题等
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Header implements Serializable {
    /**
     * 卡片主标题。必填。要为标题配置多语言，参考配置卡片多语言文档
     */
    private TitleElement title;

    /**
     * 卡片副标题。可选
     */
    private TitleElement subtitle;

    /**
     * 标题后缀标签，最多设置 3 个标签，超出不展示。可选
     */
    @JsonProperty("text_tag_list")
    private List<TextTag> textTagList;

    /**
     * 多语言标题后缀标签。每个语言环境最多设置 3 个 tag，超出不展示。可选。
     * 同时配置原字段和国际化字段，优先生效多语言配置
     */
    @JsonProperty("i18n_text_tag_list")
    private Map<String, List<TextTag>> i18nTextTagList;

    /**
     * 标题主题样式颜色。支持 "blue"|"wathet"|"turquoise"|"green"|"yellow"|"orange"|"red"|"carmine"|"violet"|"purple"|"indigo"|"grey"|"default"。默认值 default
     */
    private String template;

    /**
     * 前缀图标
     */
    private Icon icon;

    /**
     * 标题组件的内边距。JSON 2.0 新增属性。默认值 "12px"，支持范围 [0,99]px
     */
    private String padding;
}