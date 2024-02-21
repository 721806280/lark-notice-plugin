package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * Card
 *
 * @author xm.z
 */
@Data
public class Card {

    private Config config;

    private Header header;

    private JsonNode elements;

}