package io.jenkins.plugins.feishu.notification.enums;

import io.jenkins.plugins.feishu.notification.model.MessageModel;
import io.jenkins.plugins.feishu.notification.sdk.FeiShuTalkSender;
import io.jenkins.plugins.feishu.notification.sdk.model.SendResult;

/**
 * 消息类型
 *
 * @author xm.z
 */
public enum MsgTypeEnum {

    /**
     * text 类型
     */
    TEXT {
        @Override
        public SendResult send(FeiShuTalkSender sender, MessageModel msg) {
            return sender.sendText(msg);
        }
    },

    /**
     * image 类型
     */
    IMAGE {
        @Override
        public SendResult send(FeiShuTalkSender sender, MessageModel msg) {
            return sender.sendImage(msg);
        }
    },

    /**
     * shareChat 类型
     */
    SHARE_CHAT {
        @Override
        public SendResult send(FeiShuTalkSender sender, MessageModel msg) {
            return sender.sendShareChat(msg);
        }
    },

    /**
     * post 类型
     */
    POST {
        @Override
        public SendResult send(FeiShuTalkSender sender, MessageModel msg) {
            return sender.sendPost(msg);
        }
    },

    /**
     * Interactive 类型
     */
    INTERACTIVE {
        @Override
        public SendResult send(FeiShuTalkSender sender, MessageModel msg) {
            return sender.sendInteractive(msg);
        }
    };

    public abstract SendResult send(FeiShuTalkSender sender, MessageModel msg);
}
