package io.jenkins.plugins.lark.notice.sdk.constant;

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
     * Default title for the message, incorporating a notice icon and a prefix indicating it's a Jenkins build notification.
     */
    public static final String DEFAULT_TITLE = "\uD83D\uDCE2 Jenkins 构建通知";

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

}