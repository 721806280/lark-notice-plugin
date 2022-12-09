package io.jenkins.plugins.sdk.entity.support;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * Content
 *
 * @author xm.z
 */
@Data
public class Content {

    private String title;

    private JsonNode content;

}