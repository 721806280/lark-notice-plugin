package io.jenkins.plugins.lark.notice.logging;

/**
 * One structured field emitted in a trace log line.
 */
public record NoticeLogField(NoticeLogKey key, Object value) {

    public static NoticeLogField of(NoticeLogKey key, Object value) {
        return new NoticeLogField(key, value);
    }

    String render() {
        return key.externalName() + "=" + renderValue();
    }

    private String renderValue() {
        return value == null ? "<null>" : value.toString();
    }
}
