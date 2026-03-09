package io.jenkins.plugins.lark.notice.sdk.constant;

import io.jenkins.plugins.lark.notice.Messages;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants class containing various string constants used in the application.
 *
 * @author xm.z
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    /**
     * Represents a Unicode string for a notification symbol.
     */
    public static final String NOTICE_ICON = "\uD83D\uDCE2";

    /**
     * String constant: Line Feed {@code "\n"}
     */
    public static final String LF = "\n";

    /**
     * String constant: Comma {@code ","}
     */
    public static final String COMMA = ",";

    /**
     * Returns the localized default title for build notifications.
     */
    public static String defaultTitle() {
        return Messages.notification_default_title();
    }

}
