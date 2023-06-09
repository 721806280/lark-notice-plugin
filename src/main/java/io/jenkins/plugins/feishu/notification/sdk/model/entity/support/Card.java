package io.jenkins.plugins.feishu.notification.sdk.model.entity.support;

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