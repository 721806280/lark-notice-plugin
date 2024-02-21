package io.jenkins.plugins.lark.notice.enums;

import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.sdk.MessageSender;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;

/**
 * Defines the types of messages that can be sent using a MessageSender. Each enum constant represents a different
 * message type (e.g., text, image) and implements an abstract method {@code send} to send the message
 * through the specified MessageSender with a given MessageModel.
 *
 * @author xm.z
 */
public enum MsgTypeEnum {

    /**
     * Represents a text message type. Implements the {@code send} method to send text messages.
     */
    TEXT {
        @Override
        public SendResult send(MessageSender sender, MessageModel msg) {
            return sender.sendText(msg);
        }
    },

    /**
     * Represents an image message type. Implements the {@code send} method to send image messages.
     */
    IMAGE {
        @Override
        public SendResult send(MessageSender sender, MessageModel msg) {
            return sender.sendImage(msg);
        }
    },

    //====================================================Lark==========================================================

    /**
     * Represents a shared chat message type. Implements the {@code send} method to send shared chat messages.
     */
    SHARE_CHAT {
        @Override
        public SendResult send(MessageSender sender, MessageModel msg) {
            return sender.sendShareChat(msg);
        }
    },

    /**
     * Represents a post message type. Implements the {@code send} method to send post messages.
     */
    POST {
        @Override
        public SendResult send(MessageSender sender, MessageModel msg) {
            return sender.sendPost(msg);
        }
    },

    LINK {
        @Override
        public SendResult send(MessageSender sender, MessageModel msg) {
            return sender.sendLink(msg);
        }
    },

    MARKDOWN {
        @Override
        public SendResult send(MessageSender sender, MessageModel msg) {
            return sender.sendMarkdown(msg);
        }
    },

    /**
     * Represents an interactive message type. Implements the {@code send} method to send interactive messages.
     */
    CARD {
        @Override
        public SendResult send(MessageSender sender, MessageModel msg) {
            return sender.sendCard(msg);
        }
    };

    /**
     * Abstract method to be implemented by each enum constant. It defines how a message of a specific type
     * should be sent using a MessageSender with the provided MessageModel.
     *
     * @param sender The MessageSender instance used to send the message.
     * @param msg    The message model containing the details of the message to be sent.
     * @return A SendResult indicating the result of the send operation.
     */
    public abstract SendResult send(MessageSender sender, MessageModel msg);
}
