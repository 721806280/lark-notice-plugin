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
 * JsonUtils 提供了 JSON 与 Java 对象之间相互转换的工具方法。
 *
 * @author xm.z
 */
public class JsonUtils {

    /**
     * ObjectMapper 用于进行 JSON 转换。
     */
    @Getter
    @Setter
    static ObjectMapper mapper = new ObjectMapper();

    static {
        // 忽略未知属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * 配置 ObjectMapper。
     *
     * @param consumer 用于配置 ObjectMapper 的 Consumer。
     */
    public static void config(Consumer<ObjectMapper> consumer) {
        consumer.accept(mapper);
    }

    /**
     * 将 Java 对象转换成 JSON 字符串。
     *
     * @param obj 要转换的 Java 对象。
     * @return 转换后的 JSON 字符串，如果对象为 null，则返回 null。
     */
    @SneakyThrows(JsonProcessingException.class)
    public static String toJsonStr(Object obj) {
        if (null == obj) {
            return null;
        }
        return mapper.writeValueAsString(obj);
    }

    /**
     * 将 Java 对象转换成格式化后的 JSON 字符串。
     *
     * @param obj 要转换的 Java 对象。
     * @return 格式化后的 JSON 字符串，如果对象为 null，则返回 null。
     */
    @SneakyThrows(JsonProcessingException.class)
    public static String toJsonPrettyStr(Object obj) {
        if (null == obj) {
            return null;
        }
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    /**
     * 将 JSON 字符串转换成 Java 对象。
     *
     * @param jsonString 要转换的 JSON 字符串。
     * @param beanClass  要转换成的 Java 对象类型。
     * @param <T>        Java 对象的类型参数。
     * @return 转换后的 Java 对象，如果 jsonString 为 null，则返回 null。
     */
    @SneakyThrows({JsonMappingException.class, JsonProcessingException.class})
    public static <T> T toBean(String jsonString, Class<T> beanClass) {
        return null == jsonString ? null : mapper.readValue(jsonString, beanClass);
    }

    /**
     * 将 JSON 字符串转换成 Java 对象。
     *
     * @param jsonString 要转换的 JSON 字符串。
     * @param beanType   要转换成的 Java 对象类型。
     * @param <T>        Java 对象的类型参数。
     * @return 转换后的 Java 对象，如果 jsonString 为 null，则返回 null。
     */
    @SneakyThrows({JsonMappingException.class, JsonProcessingException.class})
    public static <T> T toBean(String jsonString, Type beanType) {
        return null == jsonString ? null : mapper.readValue(jsonString, mapper.constructType(beanType));
    }

    /**
     * 将 JSON 字符串转换成 Java 对象。
     *
     * @param jsonString    要转换的 JSON 字符串。
     * @param typeReference 要转换成的 Java 对象类型引用。
     * @param <T>           Java 对象的类型参数。
     * @return 转换后的 Java 对象，如果 jsonString 为 null，则返回 null。
     */
    @SneakyThrows({JsonMappingException.class, JsonProcessingException.class})
    public static <T> T toBean(String jsonString, TypeReference<T> typeReference) {
        return null == jsonString ? null : mapper.readValue(jsonString, new TypeReference<T>() {
            @Override
            public Type getType() {
                return typeReference.getType();
            }
        });
    }

    /**
     * 读取 JSON 字符串并转换成 JsonNode。
     *
     * @param jsonString 要转换的 JSON 字符串。
     * @return 转换后的 JsonNode，如果 jsonString 为 null，则返回 null。
     */
    @SneakyThrows({JsonProcessingException.class})
    public static JsonNode readTree(String jsonString) {
        return mapper.readTree(Objects.requireNonNull(jsonString, "jsonString is null"));
    }

}