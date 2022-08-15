package io.jenkins.plugins.sdk;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author xm.z
 */
@Data
public class HttpResponse {

    private String body;

    private Map<String, List<String>> headers;

}
