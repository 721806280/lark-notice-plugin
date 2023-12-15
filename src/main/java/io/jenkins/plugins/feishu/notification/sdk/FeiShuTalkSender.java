package io.jenkins.plugins.feishu.notification.sdk;

import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import io.jenkins.plugins.feishu.notification.model.MessageModel;
import io.jenkins.plugins.feishu.notification.sdk.model.SendResult;
import io.jenkins.plugins.feishu.notification.sdk.model.entity.support.At;

import java.util.List;
import java.util.stream.Collectors;

import static io.jenkins.plugins.feishu.notification.tools.Utils.LF;

/**
 * 飞书消息发送接口
 *
 * @author xm.z
 */
public interface FeiShuTalkSender {

    /**
     * 发送纯文本消息
     *
     * @param msg 消息内容
     * @return 发送结果
     */
    SendResult sendText(MessageModel msg);

    /**
     * 发送图片消息
     *
     * @param msg 消息内容
     * @return 发送结果
     */
    SendResult sendImage(MessageModel msg);

    /**
     * 发送群名片消息
     *
     * @param msg 消息内容
     * @return 发送结果
     */
    SendResult sendShareChat(MessageModel msg);

    /**
     * 发送富文本消息
     *
     * @param msg 消息内容
     * @return 发送结果
     */
    SendResult sendPost(MessageModel msg);

    /**
     * 发送交互式卡片消息
     *
     * @param msg 消息内容
     * @return 发送结果
     */
    SendResult sendInteractive(MessageModel msg);

    /**
     * 在消息内容中添加关键字
     *
     * @param str  原始内容
     * @param keys 关键字
     * @return 包含关键字的内容
     */
    default String addKeyWord(String str, String keys) {
        if (StringUtils.isEmpty(keys)) {
            return str;
        }
        return str + " " + keys;
    }

    /**
     * 在消息内容中添加at信息
     *
     * @param content 原始内容
     * @param at      At配置
     * @return 包含at信息的内容
     */
    default String addAtInfo(String content, At at) {
        String atTemplate = "<at id=%s></at>";
        if (at.getIsAtAll()) {
            return content + String.format(atTemplate, "all");
        }

        List<String> atOpenIds = at.getAtOpenIds();
        if (atOpenIds == null || atOpenIds.isEmpty()) {
            return content;
        }

        List<String> atContents = atOpenIds.stream().map(v -> String.format(atTemplate, v)).collect(Collectors.toList());
        String atContent = StringUtils.join(atContents, "");
        return (StringUtils.endsWith(content, LF) ? content : content + LF) + atContent;
    }
}