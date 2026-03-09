package io.jenkins.plugins.lark.notice.logging;

/**
 * One structured field emitted in a trace log line.
 *
 * @author xm.z
 */
public record NoticeLogField(NoticeLogKey key, Object value) {

    /**
     * Creates one structured log field instance.
     *
     * @param key   field key
     * @param value field value
     * @return structured field instance
     */
    public static NoticeLogField of(NoticeLogKey key, Object value) {
        return new NoticeLogField(key, value);
    }

    /**
     * Renders this field into {@code key=value} form.
     *
     * @return rendered field
     */
    String render() {
        return key.externalName() + "=" + renderValue();
    }

    /**
     * Renders the field value using a stable null placeholder.
     *
     * @return rendered value
     */
    private String renderValue() {
        return value == null ? "<null>" : value.toString();
    }
}
