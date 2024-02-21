package io.jenkins.plugins.lark.notice.sdk.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.model.RobotConfigModel;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.sdk.model.lark.*;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Card;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

/**
 * Lark implementation for sending Lark messages.
 *
 * @author xm.z
 */
@Slf4j
@Getter
@AllArgsConstructor
public class LarkMessageSender extends AbstractMessageSender {

    /**
     * The robot configuration information.
     */
    private final RobotConfigModel robotConfig;

    /**
     * Constructs the request parameters for the Lark API.
     *
     * @param message The message content.
     * @return The request parameters for the Lark API.
     */
    protected String signToJson(Object message) {
        if (StringUtils.isNotBlank(robotConfig.getSign())) {
            ObjectNode objectNode = JsonUtils.valueToTree(message);
            long timestamp = System.currentTimeMillis() / 1000L;
            objectNode.put("timestamp", String.valueOf(timestamp));
            objectNode.put("sign", robotConfig.createSign(timestamp));
            return JsonUtils.toJson(objectNode);
        }
        return JsonUtils.toJson(message);
    }

    /**
     * Sends a text message.
     *
     * @param msg The message content.
     * @return The send result.
     */
    @Override
    public SendResult sendText(MessageModel msg) {
        String text = addKeyWord(msg.getText(), robotConfig.getKeys());
        LarkTextMessage message = LarkTextMessage.build(msg.getAt(), text);
        return sendMessage(signToJson(message));
    }

    /**
     * Sends an image message.
     *
     * @param msg The message content.
     * @return The send result.
     */
    @Override
    public SendResult sendImage(MessageModel msg) {
        String text = msg.getText();
        LarkImageMessage message = LarkImageMessage.build(text);
        return sendMessage(signToJson(message));
    }

    /**
     * Sends a shared chat message.
     *
     * @param msg The message content.
     * @return The send result.
     */
    @Override
    public SendResult sendShareChat(MessageModel msg) {
        String text = msg.getText();
        LarkShareChatMessage message = LarkShareChatMessage.build(text);
        return sendMessage(signToJson(message));
    }

    /**
     * Sends a markdown message.
     *
     * @param msg The message content encapsulated in a MessageModel object.
     * @return SendResult object containing the result of the send operation.
     */
    @Override
    public SendResult sendMarkdown(MessageModel msg) {
        String title = addKeyWord(msg.getTitle(), robotConfig.getKeys());
        LarkCardMessage message = LarkCardMessage.build(msg.getAt(), title, msg.getText(), msg.getButtons());
        return sendMessage(signToJson(message));
    }

    /**
     * Sends a rich text message.
     *
     * @param msg The message content.
     * @return The send result.
     */
    @Override
    public SendResult sendPost(MessageModel msg) {
        String title = addKeyWord(msg.getTitle(), robotConfig.getKeys());
        LarkPostMessage message = LarkPostMessage.build(title, msg.getText());
        return sendMessage(signToJson(message));
    }

    /**
     * Sends a card message.
     *
     * @param msg Message details.
     * @return Failure result.
     */
    @Override
    public SendResult sendCard(MessageModel msg) {
        String text = msg.getText();
        if (JsonUtils.isValidJson(text)) {
            Card card = Optional.ofNullable(JsonUtils.readValue(text, Card.class)).orElseGet(Card::new);
            Optional.ofNullable(card.getHeader())
                    .filter(header -> Objects.nonNull(header.getTemplate()) && StringUtils.isBlank(header.getTemplate()))
                    .ifPresent(header -> header.setTemplate(msg.obtainHeaderTemplate()));
            if (Objects.nonNull(card.getElements())) {
                return sendMessage(signToJson(new LarkCardMessage(card)));
            }
        }
        String title = addKeyWord(msg.getTitle(), robotConfig.getKeys());
        LarkCardMessage message = LarkCardMessage.build(msg.getAt(), msg.obtainHeaderTemplate(), title, text,
                msg.getTopImg(), msg.getBottomImg(), msg.getButtons());
        return sendMessage(signToJson(message));
    }

}
