package io.jenkins.plugins.lark.notice.sdk.impl;

import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.model.RobotConfigModel;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.sdk.model.wechat.WechatWorkMarkdownMessage;
import io.jenkins.plugins.lark.notice.sdk.model.wechat.WechatWorkTemplateCardMessage;
import io.jenkins.plugins.lark.notice.sdk.model.wechat.WechatWorkTextMessage;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import static io.jenkins.plugins.lark.notice.sdk.constant.Constants.LF;

/**
 * WeCom implementation for sending group robot messages.
 */
@Getter
@AllArgsConstructor
public class WechatWorkMessageSender extends AbstractMessageSender {

    /**
     * The robot configuration information.
     */
    private final RobotConfigModel robotConfig;

    /**
     * Sends a text message.
     *
     * @param msg Message details.
     * @return Result of the send operation.
     */
    @Override
    public SendResult sendText(MessageModel msg) {
        String text = addKeyWord(msg.getText(), robotConfig.getKeys());
        WechatWorkTextMessage message = WechatWorkTextMessage.build(msg.getAt(), text);
        return sendMessage(JsonUtils.toJson(message));
    }

    /**
     * Sends a markdown message.
     *
     * @param msg Message details.
     * @return Result of the send operation.
     */
    @Override
    public SendResult sendMarkdown(MessageModel msg) {
        String text = addKeyWord(msg.getText(), robotConfig.getKeys());
        WechatWorkMarkdownMessage message = WechatWorkMarkdownMessage.build(msg.getAt(), withTitle(msg.getTitle(), text));
        return sendMessage(JsonUtils.toJson(message));
    }

    /**
     * Sends a WeCom news-notice template card for the plugin's default card model.
     *
     * @param msg Message details.
     * @return Result of the send operation.
     */
    @Override
    public SendResult sendCard(MessageModel msg) {
        String text = addKeyWord(msg.getText(), robotConfig.getKeys());
        WechatWorkTemplateCardMessage message = WechatWorkTemplateCardMessage.build(msg, text);
        return sendMessage(JsonUtils.toJson(message));
    }

    /**
     * WeCom group robots do not support the plugin's link message model. Send a markdown fallback.
     *
     * @param msg Message details.
     * @return Result of the send operation.
     */
    @Override
    public SendResult sendLink(MessageModel msg) {
        return sendMarkdown(msg);
    }

    /**
     * WeCom group robots do not support the plugin's rich post model. Send a markdown fallback.
     *
     * @param msg Message details.
     * @return Result of the send operation.
     */
    @Override
    public SendResult sendPost(MessageModel msg) {
        return sendMarkdown(msg);
    }

    private static String withTitle(String title, String text) {
        if (StringUtils.isBlank(title)) {
            return text;
        }
        return "## " + title + LF + LF + StringUtils.defaultString(text);
    }
}
