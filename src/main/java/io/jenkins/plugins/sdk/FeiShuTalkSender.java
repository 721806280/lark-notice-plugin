package io.jenkins.plugins.sdk;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.jenkins.plugins.FeiShuTalkRobotConfig;
import io.jenkins.plugins.model.MessageModel;
import io.jenkins.plugins.model.RobotConfigModel;
import io.jenkins.plugins.sdk.FeiShuTalkRobotRequest.*;
import io.jenkins.plugins.tools.AntdColor;
import io.jenkins.plugins.tools.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        return call(text);
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
        return call(image);
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
        return call(shareChat);
    }

    /**
     * 发送 post 类型的消息
     *
     * @param msg 消息
     * @return 异常信息
     */
    public String sendPost(MessageModel msg) {
        Post post = new Post();
        post.setPost(new Post.RichText(new Post.RichText.Content(addKeyWord(msg.getTitle()),
                JSONArray.of(JSON.parseArray(msg.getText())))));
        return call(post);
    }

    /**
     * 发送 Interactive 类型的消息
     *
     * @param msg 消息
     * @return 异常信息
     */
    public String sendInteractive(MessageModel msg) {
        At at = msg.getAt();
        ActionCard actioncard = new ActionCard();
        actioncard.setAt(at);

        ActionCard.Card card = new ActionCard.Card();
        card.setConfig(new ActionCard.Card.Config(true, true));
        card.setHeader(new ActionCard.Card.Header("BLUE", new ActionCard.Card.Text("plain_text", addKeyWord(msg.getTitle()))));

        ActionCard.Card.Hr hr = new ActionCard.Card.Hr();
        ActionCard.Card.Element element = new ActionCard.Card.Element("div", new ActionCard.Card.Text("lark_md", addAtInfo(msg.getText(), at)));

        if (CollectionUtils.isEmpty(msg.getButtons())) {
            card.setElements(JSONArray.of(hr, element, hr));
        } else {
            JSONObject actions = JSONObject.of("actions", JSONArray.of(msg.getButtons().toArray()), "tag", "action");
            card.setElements(JSONArray.of(hr, element, hr, actions));
        }

        actioncard.setCard(card);
        return call(actioncard);
    }

    /**
     * 统一处理请求
     *
     * @param request 请求
     * @return 异常信息
     */
    private String call(FeiShuTalkRobotRequest request) {
        try {
            Map<String, Object> content = robotConfigModel.buildSign(request.getParams());

            HttpResponse response = HttpRequest.builder()
                    .server(robotConfigModel.getWebhook()).data(content)
                    .method(Constants.METHOD_POST).proxy(proxy)
                    .build().request();
            String body = response.getBody();
            FeiShuRobotResponse data = Utils.fromJson(body, FeiShuRobotResponse.class);
            if (Objects.nonNull(data.getCode())) {
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
        return content + "\n\n" + Utils.dye(atContent, AntdColor.BLUE.toString()) + "\n";
    }
}
