package io.jenkins.plugins.lark.notice.tools;

import jakarta.servlet.ServletException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

import java.io.IOException;

/**
 * Shared factories for small Stapler HTTP responses used by plugin descriptors and management endpoints.
 */
public final class HttpResponses {

    private HttpResponses() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates one JSON response with UTF-8 content type.
     *
     * @param payload JSON payload to write
     * @return stapler response writing the provided JSON object
     */
    public static HttpResponse json(JSONObject payload) {
        return new HttpResponse() {
            @Override
            public void generateResponse(StaplerRequest2 req, StaplerResponse2 rsp, Object node) throws IOException {
                rsp.setContentType("application/json; charset=UTF-8");
                rsp.getWriter().write(payload.toString());
            }
        };
    }

    /**
     * Creates one downloadable response body with JSON content type.
     *
     * @param body response body to write
     * @param fileName download file name
     * @return stapler response serving the provided body as an attachment
     */
    public static HttpResponse downloadJson(String body, String fileName) {
        return new HttpResponse() {
            @Override
            public void generateResponse(StaplerRequest2 req, StaplerResponse2 rsp, Object node)
                    throws IOException, ServletException {
                rsp.setContentType("application/json; charset=UTF-8");
                rsp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                rsp.getWriter().write(body);
            }
        };
    }
}
