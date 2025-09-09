package io.jenkins.plugins.lark.notice.sdk.model.lark;

import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.sdk.model.lark.builder.LarkCardBuilder;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Card;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 卡片消息 类型
 *
 * @author xm.z
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LarkCardMessage extends BaseLarkMessage {

    private Card card;

    public LarkCardMessage(Card card) {
        this.card = card;
        setMsgType("interactive");
    }

    public static LarkCardMessage build(MessageModel msg) {
        String markdownContent = addAtInfo(msg.getText(), msg.getAt());

        Card card = new LarkCardBuilder()
                .withHeader(msg.obtainHeaderTemplate(), msg.getTitle())
                .withImage(msg.getTopImg())
                .withMarkdown(markdownContent)
                .withImage(msg.getBottomImg())
                .withPersonList(msg.getAt())
                .withButtons(msg.getButtons())
                .build();

        return new LarkCardMessage(card);
    }

}
