package io.jenkins.plugins.lark.notice.i18n;

import io.jenkins.plugins.lark.notice.Messages;
import org.jvnet.localizer.ResourceBundleHolder;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.lang3.StringUtils;

/**
 * Locale-aware accessors for message strings used by runtime notification rendering.
 *
 * @author xm.z
 */
public final class NoticeI18n {

    private static final ResourceBundleHolder HOLDER = ResourceBundleHolder.get(Messages.class);

    private NoticeI18n() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String defaultTitle(Locale locale) {
        return get(locale, "notification.default.title");
    }

    public static String buildMessageProjectName(Locale locale) {
        return get(locale, "build.message.project.name");
    }

    public static String buildMessageJobName(Locale locale) {
        return get(locale, "build.message.job.name");
    }

    public static String buildMessageStatus(Locale locale) {
        return get(locale, "build.message.status");
    }

    public static String buildMessageDuration(Locale locale) {
        return get(locale, "build.message.duration");
    }

    public static String buildMessageExecutor(Locale locale) {
        return get(locale, "build.message.executor");
    }

    public static String robotTestProjectName(Locale locale) {
        return get(locale, "robot.test.project.name");
    }

    public static String robotTestJobName(Locale locale) {
        return get(locale, "robot.test.job.name");
    }

    public static String robotTestSuccessTitle(Locale locale) {
        return get(locale, "robot.test.success.title");
    }

    public static boolean isBuiltInDefaultTitle(String value) {
        String normalized = StringUtils.trimToNull(value);
        if (normalized == null) {
            return false;
        }
        return normalized.equals(defaultTitle(Locale.SIMPLIFIED_CHINESE))
                || normalized.equals(defaultTitle(Locale.US));
    }

    public static String buildMessageButtonChangeLog(Locale locale) {
        return get(locale, "build.message.button.change.log");
    }

    public static String buildMessageButtonConsole(Locale locale) {
        return get(locale, "build.message.button.console");
    }

    public static String buildStatusLabel(Locale locale, String keySuffix) {
        return get(locale, "build.status." + keySuffix);
    }

    private static String get(Locale locale, String key, Object... args) {
        Locale resolved = locale == null ? Locale.getDefault() : locale;
        ResourceBundle bundle = HOLDER.get(resolved);
        String value = bundle.getString(key);
        return args.length == 0 ? value : MessageFormat.format(value, args);
    }
}
