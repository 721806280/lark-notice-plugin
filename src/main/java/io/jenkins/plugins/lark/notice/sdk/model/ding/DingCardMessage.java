package io.jenkins.plugins.lark.notice.sdk.model.ding;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.At;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author xm.z
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DingCardMessage extends BaseDingMessage {

    private At at;

    private ActionCardContent actionCard;

    public DingCardMessage(At at, ActionCardContent actionCard) {
        this.at = at;
        this.actionCard = actionCard;
        setMsgType("actionCard");
    }

    public static DingCardMessage build(At at, String title, String text, String btnOrientation, List<Button> buttons) {
        ActionCardContent content = new ActionCardContent(title, addAtInfo(text, at, true),
                null, null, StringUtils.defaultIfBlank(btnOrientation, "1"), buttons);
        return new DingCardMessage(at, content);
    }

    public static DingCardMessage build(At at, String title, String text, String singleTitle, String singleUrl) {
        ActionCardContent content = new ActionCardContent(title,
                addAtInfo(text, at, true), singleTitle, singleUrl, null, null);
        return new DingCardMessage(at, content);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionCardContent {

        private String title;

        private String text;

        /**
         * 单个按钮的标题, 设置此项和singleURL后，buttons 无效
         */
        private String singleTitle;

        /**
         * 点击消息跳转的URL
         */
        private String singleUrl;

        /**
         * 0：按钮竖直排列 1：按钮横向排列
         */
        private String btnOrientation;

        @JsonProperty("btns")
        private List<Button> buttons;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Button {

        private String title;

        @JsonProperty("actionURL")
        private String url;

    }

}
