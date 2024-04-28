package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.io.Serializable;

/**
 * Content
 *
 * @author xm.z
 */
@Data
public class Content implements Serializable {

    private String title;

    private JsonNode content;

}