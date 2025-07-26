package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.config.Config;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.link.Link;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.view.title.Header;
import lombok.Data;

/**
 * 卡片根对象
 *
 * @author xm.z
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Card {
    /**
     * 卡片 JSON 结构的版本。默认为 1.0。要使用 JSON 2.0 结构，必须显示声明 2.0。
     */
    private String schema = "2.0";

    /**
     * 卡片配置项
     */
    private Config config;

    /**
     * 指定卡片整体的跳转链接
     */
    @JsonProperty("card_link")
    private Link cardLink;

    /**
     * 卡片头部信息，包含标题、副标题等
     */
    private Header header;

    /**
     * 卡片正文内容，包含多个组件元素
     */
    private Body body;

}