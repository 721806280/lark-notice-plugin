package io.jenkins.plugins.feishu.notification.tools;

import io.jenkins.plugins.feishu.notification.sdk.model.entity.support.Button;

import java.util.ArrayList;
import java.util.List;

import static io.jenkins.plugins.feishu.notification.sdk.constant.Constants.LF;

/**
 * 通用方法合集
 *
 * @author xm.z
 */
public class Utils {

    /**
     * 创建默认的按钮列表
     *
     * @param jobUrl 任务地址
     * @return 按钮列表
     */
    public static List<Button> createDefaultButtons(String jobUrl) {
        String changeLog = jobUrl + "/changes";
        String console = jobUrl + "/console";

        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.of("更改记录", changeLog));
        buttons.add(Button.of("控制台", console));

        return buttons;
    }

    /**
     * markdown 数组转字符串
     *
     * @param list 数组
     * @return 字符串
     */
    public static String join(Iterable<? extends CharSequence> list) {
        if (list == null) {
            return "";
        }
        return String.join(LF, list);
    }

}
