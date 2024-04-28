package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.io.Serializable;

/**
 * Card
 *
 * @author xm.z
 */
@Data
public class Card implements Serializable {

    private Config config;

    private Header header;

    private JsonNode elements;

}