package io.jenkins.plugins.sdk;

import com.alibaba.fastjson2.JSONArray;
import com.google.gson.annotations.SerializedName;
import io.jenkins.plugins.enums.MsgTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xm.z
 */
@Data
@NoArgsConstructor
public abstract class FeiShuTalkRobotRequest {

    /**
     * 被@人的手机号
     */
    private At at;

    /**
     * 消息类型
     *
     * @return type
     */
    public abstract String getMsgType();

    public Map<String, Object> getParams() {
        String msgType = this.getMsgType();
        Map<String, Object> params = new HashMap<>(16);
        params.put("msg_type", msgType);
        if (MsgTypeEnum.INTERACTIVE.name().toLowerCase().equals(msgType)) {
            ActionCard actionCard = (ActionCard) this;
            params.put("card", actionCard.getCard());
        } else {
            params.put("content", this);
        }
        return params;
    }

    /**
     * 被@人的手机号
     *
     * @author top auto create
     * @since 1.0, null
     */
    @Data
    @NoArgsConstructor
    public static class At {
        /**
         * 被 @ 人的 open_id
         */
        private List<String> atOpenIds;
        /**
         * 是否 @ 所有人
         */
        private Boolean isAtAll;
    }

    /**
     * text类型
     *
     * @author top auto create
     * @since 1.0, null
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Text extends FeiShuTalkRobotRequest {
        /**
         * text类型
         */
        private String text;

        @Override
        public String getMsgType() {
            return MsgTypeEnum.TEXT.name().toLowerCase();
        }
    }

    /**
     * 群名片消息 类型
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class ShareChat extends FeiShuTalkRobotRequest {
        /**
         * 群名片ID
         */
        @SerializedName(value = "share_chat_id")
        private String shareChatId;

        @Override
        public String getMsgType() {
            return MsgTypeEnum.SHARE_CHAT.name().toLowerCase();
        }
    }

    /**
     * 图片消息 类型
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class Image extends FeiShuTalkRobotRequest {
        /**
         * 图片的key
         */
        @SerializedName(value = "image_key")
        private String imageKey;

        @Override
        public String getMsgType() {
            return MsgTypeEnum.IMAGE.name().toLowerCase();
        }
    }

    /**
     * 富文本消息 类型
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class Post extends FeiShuTalkRobotRequest {
        /**
         * 富文本消息
         */
        private RichText post;

        @Override
        public String getMsgType() {
            return MsgTypeEnum.POST.name().toLowerCase();
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class RichText {

            @SerializedName(value = "zh_cn")
            private Content zhCn;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Content {
                private String title;
                private JSONArray content;
            }
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Element {
            private String tag;
            private String text;
            private String href;
        }

    }

    /**
     * 卡片消息 类型
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class ActionCard extends FeiShuTalkRobotRequest {
        /**
         * 卡片消息 类型
         */
        private Card card;

        @Override
        public String getMsgType() {
            return MsgTypeEnum.INTERACTIVE.name().toLowerCase();
        }

        @Data
        @NoArgsConstructor
        public static class Card {
            private Config config;
            private Header header;
            private JSONArray elements;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Header {
                private String template;
                private Text title;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Config {
                @SerializedName(value = "wide_screen_mode")
                private boolean wideScreenMode;
                @SerializedName(value = "enable_forward")
                private boolean enableForward;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Element {
                /**
                 * 标签类型
                 */
                private String tag = "div";
                /**
                 * 信息文本
                 */
                private Text text;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Action {
                private String type = "default";
                /**
                 * 标签类型
                 */
                private String tag = "button";
                /**
                 * 信息文本
                 */
                private Text text;
                /**
                 * 跳转地址
                 */
                private String url;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Text {
                /**
                 * 标签类型
                 */
                private String tag = "lark_md";
                /**
                 * 信息文本
                 */
                private String content;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Hr {
                /**
                 * 标签类型
                 */
                private String tag = "hr";
            }
        }
    }
}
