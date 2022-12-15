package io.jenkins.plugins.sdk;

import io.jenkins.plugins.FeiShuTalkRobotConfig;
import io.jenkins.plugins.enums.MsgTypeEnum;
import io.jenkins.plugins.model.MessageModel;
import io.jenkins.plugins.model.RobotConfigModel;
import io.jenkins.plugins.sdk.entity.*;
import io.jenkins.plugins.sdk.entity.support.*;
import io.jenkins.plugins.tools.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.Proxy;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息发送器
 *
 * @author xm.z
 */
@Slf4j
public class FeiShuTalkSender {
    private final RobotConfigModel robotConfigModel;

    private final Proxy proxy;

    public FeiShuTalkSender(FeiShuTalkRobotConfig robotConfig, Proxy proxy) {
        this.robotConfigModel = RobotConfigModel.of(robotConfig);
        this.proxy = proxy;
    }

    /**
     * 发送 text 类型的消息
     *
     * @param msg 消息
     * @return 异常信息
     */
    public String sendText(MessageModel msg) {
        Text text = new Text();
        text.setText(addKeyWord(msg.getText()));
        return call(buildParams(MsgTypeEnum.TEXT, text));
    }

    /**
     * 发送 image 类型的消息
     *
     * @param msg 消息
     * @return 异常信息
     */
    public String sendImage(MessageModel msg) {
        Image image = new Image();
        image.setImageKey(msg.getText());
        return call(buildParams(MsgTypeEnum.IMAGE, image));
    }

    /**
     * 发送 share_chat 类型的消息
     *
     * @param msg 消息
     * @return 异常信息
     */
    public String sendShareChat(MessageModel msg) {
        ShareChat shareChat = new ShareChat();
        shareChat.setShareChatId(msg.getText());
        return call(buildParams(MsgTypeEnum.SHARE_CHAT, shareChat));
    }

    /**
     * 发送 post 类型的消息
     *
     * @param msg 消息
     * @return 异常信息
     */
    public String sendPost(MessageModel msg) {
        Content content = new Content();
        content.setTitle(addKeyWord(msg.getTitle()));
        content.setContent(JsonUtils.readTree(msg.getText()));

        Post post = new Post();
        post.setPost(new RichText(content));

        return call(buildParams(MsgTypeEnum.POST, post));
    }

    /**
     * 发送 Interactive 类型的消息
     *
     * @param msg 消息
     * @return 异常信息
     */
    public String sendInteractive(MessageModel msg) {
        Card card = new Card();
        card.setConfig(new Config(true, true));
        card.setHeader(new Header("BLUE", new LarkMdText("plain_text", addKeyWord(msg.getTitle()))));

        Hr hr = new Hr();
        LarkMdElement element = new LarkMdElement("div", new LarkMdText("lark_md", addAtInfo(msg.getText(), msg.getAt())));

        List<Object> elements = new ArrayList<>();
        elements.add(hr);
        elements.add(element);
        elements.add(hr);

        if (!CollectionUtils.isEmpty(msg.getButtons())) {
            Map<String, Object> actions = new HashMap<>(8);
            actions.put("actions", msg.getButtons());
            actions.put("tag", "action");
            elements.add(actions);
        }

        card.setElements(JsonUtils.readTree(JsonUtils.toJsonStr(elements)));
        return call(buildParams(MsgTypeEnum.INTERACTIVE, new ActionCard(card)));
    }


    private Map<String, Object> buildParams(MsgTypeEnum msgType, Object obj) {
        Map<String, Object> params = new HashMap<>(8);
        params.put("msg_type", msgType.name().toLowerCase());
        if (MsgTypeEnum.INTERACTIVE.equals(msgType)) {
            params.put("card", ((ActionCard) obj).getCard());
        } else {
            params.put("content", obj);
        }
        return params;
    }

    /**
     * 统一处理请求
     *
     * @param params 请求参数
     * @return 异常信息
     */
    private String call(Map<String, Object> params) {
        try {
            String body = HttpRequest.builder()
                    .server(robotConfigModel.getWebhook())
                    .data(robotConfigModel.buildSign(params))
                    .method(Constants.METHOD_POST).proxy(proxy)
                    .build().request().getBody();

            FeiShuRobotResponse data = JsonUtils.toBean(body, FeiShuRobotResponse.class);
            if (Objects.isNull(data) || Objects.nonNull(data.getCode())) {
                log.error("飞书消息发送失败：{}", body);
                return body;
            }
        } catch (IOException e) {
            log.error("飞书消息发送失败：", e);
            return ExceptionUtils.getStackTrace(e);
        }
        return null;
    }

    /**
     * 添加关键字
     *
     * @param str 原始内容
     * @return 带关键字的信息
     */
    private String addKeyWord(String str) {
        String keys = robotConfigModel.getKeys();
        if (StringUtils.isEmpty(keys)) {
            return str;
        }
        return str + " " + keys;
    }

    /**
     * 添加 at 信息
     *
     * @param content 原始内容
     * @param at      at 配置
     * @return 包含 at 信息的内容
     */
    private String addAtInfo(String content, At at) {
        String atTemplate = "<at id=%s></at>";
        if (at.getIsAtAll()) {
            return content + String.format(atTemplate, "all");
        }

        List<String> atOpenIds = at.getAtOpenIds();
        if (atOpenIds == null || atOpenIds.isEmpty()) {
            return content;
        }

        List<String> atContents = atOpenIds.stream().map(v -> String.format(atTemplate, v)).collect(Collectors.toList());
        String atContent = StringUtils.join(atContents, "");
        return content + "\n\n" + atContent + "\n";
    }
}
