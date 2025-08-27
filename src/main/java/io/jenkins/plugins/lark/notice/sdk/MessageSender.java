package io.jenkins.plugins.lark.notice.sdk;

import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import org.apache.commons.lang3.StringUtils;

/**
 * MessageSender is an interface designed to abstract the process of sending various types of messages.
 * It allows for sending text, markdown, and other forms of messages through different platforms.
 * The interface provides default implementations for several message types that might not be supported across all platforms,
 * indicating failure for such cases. Additionally, it includes a utility method for appending keywords to messages,
 * enhancing their functionality or discoverability.
 *
 * @author xm.z
 */
public interface MessageSender {

    /**
     * Sends a text message.
     *
     * @param msg Message details.
     * @return Result of the send operation.
     */
    SendResult sendText(MessageModel msg);

    /**
     * Default method to send an image message, not supported by default.
     *
     * @param msg Message details.
     * @return Failure result.
     */
    default SendResult sendImage(MessageModel msg) {
        return SendResult.fail("This type of message is not supported.");
    }

    /**
     * Default method to send a share chat message, not supported by default.
     *
     * @param msg Message details.
     * @return Failure result.
     */
    default SendResult sendShareChat(MessageModel msg) {
        return SendResult.fail("This type of message is not supported.");
    }

    /**
     * Sends a markdown message.
     *
     * @param msg Message details.
     * @return Result of the send operation.
     */
    SendResult sendMarkdown(MessageModel msg);

    /**
     * Default method to send a link message, not supported by default.
     *
     * @param msg Message details.
     * @return Failure result.
     */
    default SendResult sendLink(MessageModel msg) {
        return SendResult.fail("This type of message is not supported.");
    }

    /**
     * Default method to send a post message, not supported by default.
     *
     * @param msg Message details.
     * @return Failure result.
     */
    default SendResult sendPost(MessageModel msg) {
        return SendResult.fail("This type of message is not supported.");
    }

    /**
     * Default method to send a card message, not supported by default.
     *
     * @param msg Message details.
     * @return Failure result.
     */
    default SendResult sendCard(MessageModel msg) {
        return SendResult.fail("This type of message is not supported.");
    }

    /**
     * Appends keywords to a message.
     *
     * @param str  Original message content.
     * @param keys Keywords to append.
     * @return Content with appended keywords.
     */
    default String addKeyWord(String str, String keys) {
        if (StringUtils.isEmpty(keys)) {
            return str;
        }
        return str + " " + keys;
    }

}