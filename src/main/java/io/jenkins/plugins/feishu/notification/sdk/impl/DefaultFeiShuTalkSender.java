package io.jenkins.plugins.feishu.notification.sdk.impl;

import io.jenkins.plugins.feishu.notification.enums.MsgTypeEnum;
import io.jenkins.plugins.feishu.notification.model.MessageModel;
import io.jenkins.plugins.feishu.notification.model.RobotConfigModel;
import io.jenkins.plugins.feishu.notification.sdk.model.SendResult;
import io.jenkins.plugins.feishu.notification.sdk.model.entity.*;
import io.jenkins.plugins.feishu.notification.sdk.model.entity.support.*;
import io.jenkins.plugins.feishu.notification.tools.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * 飞书默认消息发送实现
 *
 * @author xm.z
 */
@Getter
@AllArgsConstructor
public class DefaultFeiShuTalkSender extends AbstractFeiShuTalkSender {

    /**
     * 机器人配置信息
     */
    private final RobotConfigModel robotConfig;

    /**
     * 发送纯文本消息
     *
     * @param msg 消息内容
     * @return 发送结果
     */
    @Override
    public SendResult sendText(MessageModel msg) {
        Text text = new Text();
        text.setText(addKeyWord(msg.getText(), robotConfig.getKeys()));
        return sendMessage(buildParams(MsgTypeEnum.TEXT, text));
    }

    /**
     * 发送图片消息
     *
     * @param msg 消息内容
     * @return 发送结果
     */
    @Override
    public SendResult sendImage(MessageModel msg) {
        Image image = new Image();
        image.setImageKey(msg.getText());
        return sendMessage(buildParams(MsgTypeEnum.IMAGE, image));
    }

    /**
     * 发送群名片消息
     *
     * @param msg 消息内容
     * @return 发送结果
     */
    @Override
    public SendResult sendShareChat(MessageModel msg) {
        ShareChat shareChat = new ShareChat();
        shareChat.setShareChatId(msg.getText());
        return sendMessage(buildParams(MsgTypeEnum.SHARE_CHAT, shareChat));
    }

    /**
     * 发送富文本消息
     *
     * @param msg 消息内容
     * @return 发送结果
     */
    @Override
    public SendResult sendPost(MessageModel msg) {
        Content content = new Content();
        content.setTitle(addKeyWord(msg.getTitle(), robotConfig.getKeys()));
        content.setContent(JsonUtils.readTree(msg.getText()));

        Post post = new Post();
        post.setPost(new RichText(content));

        return sendMessage(buildParams(MsgTypeEnum.POST, post));
    }

    /**
     * 发送交互式卡片消息
     *
     * @param msg 消息内容
     * @return 发送结果
     */
    @Override
    public SendResult sendInteractive(MessageModel msg) {
        Card card = new Card();
        card.setConfig(new Config(true, true));
        card.setHeader(new Header("BLUE", new TagContent("plain_text", addKeyWord(msg.getTitle(), robotConfig.getKeys()))));

        Hr hr = new Hr();
        TagContent mdContent = new TagContent("markdown", addAtInfo(msg.getText(), msg.getAt()));

        List<Object> elements = new ArrayList<>();
        if (Objects.nonNull(msg.getTopImg())) {
            elements.add(hr);
            elements.add(msg.getTopImg());
        }
        elements.add(hr);
        elements.add(mdContent);
        if (Objects.nonNull(msg.getBottomImg())) {
            elements.add(hr);
            elements.add(msg.getBottomImg());
        }
        elements.add(hr);

        if (!CollectionUtils.isEmpty(msg.getButtons())) {
            Map<String, Object> actions = new HashMap<>(8);
            actions.put("actions", msg.getButtons());
            actions.put("tag", "action");
            elements.add(actions);
        }

        card.setElements(JsonUtils.valueToTree(elements));
        return sendMessage(buildParams(MsgTypeEnum.INTERACTIVE, new ActionCard(card)));
    }

}
