package io.jenkins.plugins.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * JsonUtils
 *
 * @author xm.z
 */
public class JsonUtils {

    @Getter
    @Setter
    static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static void config(Consumer<ObjectMapper> consumer) {
        consumer.accept(mapper);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static String toJsonStr(Object obj) {
        if (null == obj) {
            return null;
        }
        return mapper.writeValueAsString(obj);
    }

    @SneakyThrows(JsonProcessingException.class)
    public static String toJsonPrettyStr(Object obj) {
        if (null == obj) {
            return null;
        }
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    @SneakyThrows({JsonMappingException.class, JsonProcessingException.class})
    public static <T> T toBean(String jsonString, Class<T> beanClass) {
        return null == jsonString ? null : mapper.readValue(jsonString, beanClass);
    }

    @SneakyThrows({JsonMappingException.class, JsonProcessingException.class})
    public static <T> T toBean(String jsonString, Type beanType) {
        return null == jsonString ? null : mapper.readValue(jsonString, mapper.constructType(beanType));
    }

    @SneakyThrows({JsonMappingException.class, JsonProcessingException.class})
    public static <T> T toBean(String jsonString, TypeReference<T> typeReference) {
        return null == jsonString ? null : mapper.readValue(jsonString, new TypeReference<T>() {
            @Override
            public Type getType() {
                return typeReference.getType();
            }
        });
    }

    @SneakyThrows({JsonMappingException.class, JsonProcessingException.class})
    public static JsonNode readTree(String jsonString) {
        return mapper.readTree(Objects.requireNonNull(jsonString, "jsonString is null"));
    }

}
