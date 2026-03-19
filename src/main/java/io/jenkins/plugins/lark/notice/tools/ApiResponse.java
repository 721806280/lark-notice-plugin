package io.jenkins.plugins.lark.notice.tools;

import net.sf.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple response wrapper for JSON endpoints.
 */
public final class ApiResponse {

    private final Map<String, Object> payload = new LinkedHashMap<>();

    private ApiResponse(boolean ok) {
        payload.put("ok", ok);
    }

    public static ApiResponse ok() {
        return new ApiResponse(true);
    }

    public static ApiResponse ok(String message) {
        return new ApiResponse(true).message(message);
    }

    public static ApiResponse ok(String message, Object data) {
        return new ApiResponse(true).message(message).data(data);
    }

    public static ApiResponse fail(String message) {
        return new ApiResponse(false).message(message);
    }

    public ApiResponse message(String message) {
        if (message != null) {
            payload.put("message", message);
        }
        return this;
    }

    public ApiResponse data(Object data) {
        if (data != null) {
            payload.put("data", data);
        }
        return this;
    }

    public ApiResponse put(String key, Object value) {
        if (key != null && value != null) {
            payload.put(key, value);
        }
        return this;
    }

    public JSONObject toJson() {
        return JSONObject.fromObject(payload);
    }
}
