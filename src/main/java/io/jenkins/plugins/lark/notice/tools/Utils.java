package io.jenkins.plugins.lark.notice.tools;

import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.i18n.NoticeI18n;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;

/**
 * Utility class providing helper methods for common operations.
 *
 * @author xm.z
 */
public class Utils {

    /**
     * Mainland China mobile number pattern.
     * Supported forms include plain 11-digit numbers and values prefixed with {@code 86} or {@code +86}.
     */
    public final static Pattern MOBILE = Pattern.compile("(?:0|86|\\+86)?1[3-9]\\d{9}");

    /**
     * Validates whether the given value is a Mainland China mobile number.
     *
     * @param value candidate value
     * @return {@code true} when the value matches the mobile number pattern
     */
    public static boolean isMobile(CharSequence value) {
        if (value == null) {
            // A null value is never considered a valid mobile number.
            return false;
        }
        return MOBILE.matcher(value).matches();
    }

    /**
     * Creates a default list of buttons for a given job URL.
     * This method constructs buttons for common actions such as viewing change logs and accessing the console,
     * making it easier to generate consistent user interfaces across different parts of the application.
     *
     * @param jobUrl The base URL for the job, used to construct specific action URLs for the buttons.
     * @return A list of {@link Button} objects, each representing an action that can be taken from the UI.
     */
    public static List<Button> createDefaultButtons(String jobUrl) {
        return createDefaultButtons(jobUrl, Locale.getDefault());
    }

    /**
     * Creates a default list of buttons for a given job URL using the provided locale.
     *
     * @param jobUrl The base URL for the job, used to construct specific action URLs for the buttons.
     * @param locale locale used to render button labels
     * @return A list of {@link Button} objects, each representing an action that can be taken from the UI.
     */
    public static List<Button> createDefaultButtons(String jobUrl, Locale locale) {
        String normalizedJobUrl = ensureTrailingSlash(jobUrl);
        String changeLog = normalizedJobUrl + "changes";
        String console = normalizedJobUrl + "console";

        List<Button> buttons = new ArrayList<>();
        buttons.add(new Button(NoticeI18n.buildMessageButtonChangeLog(locale), changeLog, "primary_filled"));
        buttons.add(new Button(NoticeI18n.buildMessageButtonConsole(locale), console, "default"));

        return buttons;
    }

    /**
     * Ensures URLs used as directory-like prefixes end with one trailing slash.
     *
     * @param value candidate base URL
     * @return normalized URL prefix
     */
    static String ensureTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.endsWith("/") ? value : value + "/";
    }

    /**
     * Converts an Iterable of CharSequence items into a single String.
     * This method is particularly useful for joining multiple markdown elements or similar text-based items,
     * using a newline character as the separator. It simplifies the process of aggregating text for display
     * or further processing.
     *
     * @param list An Iterable containing CharSequence items to be joined.
     * @return A String resulting from concatenating all items in the list, separated by newline characters.
     * Returns an empty string if the input list is null.
     */
    public static String join(Iterable<? extends CharSequence> list) {
        if (list == null) {
            return "";
        }
        return String.join(LF, list);
    }

}
