package io.jenkins.plugins.feishu.notification.sdk.impl;

import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import io.jenkins.cli.shaded.org.apache.commons.lang.exception.ExceptionUtils;
import io.jenkins.plugins.feishu.notification.enums.MsgTypeEnum;
import io.jenkins.plugins.feishu.notification.model.RobotConfigModel;
import io.jenkins.plugins.feishu.notification.sdk.FeiShuTalkSender;
import io.jenkins.plugins.feishu.notification.sdk.model.SendResult;
import io.jenkins.plugins.feishu.notification.sdk.model.entity.ActionCard;
import io.jenkins.plugins.feishu.notification.tools.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * 飞书消息发送抽象类
 *
 * @author xm.z
 */
@Slf4j
public abstract class AbstractFeiShuTalkSender implements FeiShuTalkSender {

    /**
     * 获取机器人配置信息
     *
     * @return 机器人配置信息
     */
    protected abstract RobotConfigModel getRobotConfig();

    /**
     * 调用飞书API发送消息
     *
     * @param params 请求参数
     * @return 发送结果
     */
    protected SendResult sendMessage(Map<String, Object> params) {
        SendResult sendResult;
        try {
            RobotConfigModel robotConfig = getRobotConfig();
            String body = StringUtils.defaultString(JsonUtils.toJson(robotConfig.buildSign(params)));
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(robotConfig.getWebhook()))
                    .header(CONTENT_TYPE, APPLICATION_JSON_VALUE).timeout(Duration.ofMinutes(3))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL).proxy(robotConfig.getProxySelector()).build()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            sendResult = JsonUtils.readValue(response.body(), SendResult.class);
        } catch (Exception e) {
            log.error("飞书消息发送失败", e);
            sendResult = SendResult.fail(ExceptionUtils.getStackTrace(e));
        }
        return sendResult;
    }

    /**
     * 构造飞书API请求参数
     *
     * @param msgType 消息类型
     * @param obj     消息内容
     * @return 飞书API请求参数
     */
    protected Map<String, Object> buildParams(MsgTypeEnum msgType, Object obj) {
        Map<String, Object> params = new HashMap<>(8);
        params.put("msg_type", msgType.name().toLowerCase());
        if (MsgTypeEnum.INTERACTIVE.equals(msgType)) {
            params.put("card", ((ActionCard) obj).getCard());
        } else {
            params.put("content", obj);
        }
        return params;
    }

}