package io.jenkins.plugins.lark.notice.tools;

import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;

/**
 * Utility class providing helper methods for common operations.
 *
 * @author xm.z
 */
public class Utils {

    /**
     * 移动电话
     * eg: 中国大陆： +86  180 4953 1399，2位区域码标示+11位数字
     * 中国大陆 +86 Mainland China
     */
    public final static Pattern MOBILE = Pattern.compile("(?:0|86|\\+86)?1[3-9]\\d{9}");

    /**
     * 验证是否为手机号码（中国）
     *
     * @param value 值
     * @return 是否为手机号码（中国）
     */
    public static boolean isMobile(CharSequence value) {
        if (value == null) {
            // 提供null的字符串为不匹配
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
        String changeLog = jobUrl + "changes";
        String console = jobUrl + "console";

        List<Button> buttons = new ArrayList<>();
        buttons.add(new Button("更改记录", changeLog, "primary_filled"));
        buttons.add(new Button("控制台", console, "default"));

        return buttons;
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
