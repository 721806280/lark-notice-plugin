package io.jenkins.plugins.lark.notice.sdk.model.lark;

import io.jenkins.plugins.lark.notice.sdk.model.lark.support.*;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

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

    public static LarkCardMessage build(At at, String title, String text, List<Button> buttons) {
        Card card = new Card();
        card.setHeader(new Header(null, new TagContent("plain_text", title)));

        List<Object> elements = new ArrayList<>();
        elements.add(new TagContent("lark_md", addAtInfo(text, at)));
        if (CollectionUtils.isNotEmpty(buttons)) {
            Map<String, Object> actions = new HashMap<>(8);
            actions.put("actions", buttons);
            actions.put("tag", "action");
            elements.add(actions);
        }
        card.setElements(JsonUtils.valueToTree(elements));
        return new LarkCardMessage(card);
    }

    public static LarkCardMessage build(At at, String headerTemplate, String title, String text,
                                        ImgElement topImg, ImgElement bottomImg, List<Button> buttons) {
        Card card = new Card();
        card.setConfig(new Config(true, true));
        card.setHeader(new Header(headerTemplate, new TagContent("plain_text", title)));

        Hr hr = new Hr();
        TagContent mdContent = new TagContent("markdown", addAtInfo(text, at));

        List<Object> elements = new ArrayList<>();
        if (Objects.nonNull(topImg)) {
            elements.add(hr);
            elements.add(topImg);
        }
        elements.add(hr);
        elements.add(mdContent);
        if (Objects.nonNull(bottomImg)) {
            elements.add(hr);
            elements.add(bottomImg);
        }
        elements.add(hr);

        if (!CollectionUtils.isEmpty(buttons)) {
            Map<String, Object> actions = new HashMap<>(8);
            actions.put("actions", buttons);
            actions.put("tag", "action");
            elements.add(actions);
        }

        card.setElements(JsonUtils.valueToTree(elements));

        return new LarkCardMessage(card);
    }

}
