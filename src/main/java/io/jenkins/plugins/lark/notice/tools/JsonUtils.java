package io.jenkins.plugins.lark.notice.tools;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Minimal JSON helper facade used by message rendering and transport code.
 *
 * @author xm.z
 */
public final class JsonUtils {

    private JsonUtils() {
    }

    /**
     * Serializes the given object to a JSON string.
     *
     * @param object source object
     * @return serialized JSON, or {@code null} when the input is {@code null}
     */
    @SneakyThrows(JsonProcessingException.class)
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        return mapper().writeValueAsString(object);
    }

    /**
     * Serializes the given object to indented JSON for human-readable exports.
     *
     * @param object source object
     * @return serialized JSON, or {@code null} when the input is {@code null}
     */
    @SneakyThrows(JsonProcessingException.class)
    public static String toPrettyJson(Object object) {
        if (object == null) {
            return null;
        }
        return mapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    /**
     * Parses a JSON string into a tree model.
     *
     * @param jsonString raw JSON string
     * @return parsed JSON tree
     */
    @SneakyThrows(JsonProcessingException.class)
    public static JsonNode readTree(String jsonString) {
        return mapper().readTree(Objects.requireNonNull(jsonString, "jsonString is null"));
    }

    /**
     * Deserializes a JSON string into the requested type.
     *
     * @param jsonString raw JSON string
     * @param valueType target type
     * @param <T> target type parameter
     * @return deserialized object, or {@code null} when the input is blank
     */
    @SneakyThrows(IOException.class)
    public static <T> T readValue(String jsonString, Class<T> valueType) {
        if (!StringUtils.hasText(jsonString)) {
            return null;
        }
        return mapper().readValue(jsonString, valueType);
    }

    /**
     * Deserializes a JSON array into a typed list.
     *
     * @param content raw JSON array string
     * @param elementClass list element type
     * @param <T> element type parameter
     * @return deserialized list, or an empty list when the input is blank
     */
    @SneakyThrows(IOException.class)
    public static <T> List<T> readList(String content, Class<T> elementClass) {
        if (!StringUtils.hasText(content)) {
            return List.of();
        }
        return mapper().readerForListOf(elementClass).readValue(content);
    }

    /**
     * Converts a value into a mutable JSON object tree.
     *
     * @param fromValue source object
     * @return converted object node
     */
    public static ObjectNode valueToTree(Object fromValue) {
        return mapper().valueToTree(fromValue);
    }

    /**
     * Validates whether the provided string is syntactically valid JSON.
     *
     * @param jsonString raw JSON string
     * @return {@code true} when the input can be parsed as JSON
     */
    public static boolean isValidJson(String jsonString) {
        if (!StringUtils.hasText(jsonString)) {
            return false;
        }
        ObjectMapper validator = mapper().copy();
        validator.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        validator.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
        try {
            validator.readTree(jsonString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static ObjectMapper mapper() {
        return JacksonHolder.INSTANCE;
    }

    /**
     * Lazy holder for the shared {@link ObjectMapper} instance.
     */
    private static class JacksonHolder {

        /**
         * Shared mapper configured with permissive parsing and plugin defaults.
         */
        private static final ObjectMapper INSTANCE = new ObjectMapper(JsonFactory.builder()
                .configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)
                .configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS, true)
                .build());

        static {
            INSTANCE.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            INSTANCE.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            INSTANCE.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            INSTANCE.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
            INSTANCE.registerModule(new SimpleModule().addSerializer(Long.class, ToStringSerializer.instance));
            INSTANCE.findAndRegisterModules();
        }
    }
}
