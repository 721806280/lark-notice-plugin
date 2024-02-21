package io.jenkins.plugins.lark.notice.sdk.impl;

import io.jenkins.cli.shaded.org.apache.commons.lang.ArrayUtils;
import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import io.jenkins.cli.shaded.org.apache.commons.lang.exception.ExceptionUtils;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.model.RobotConfigModel;
import io.jenkins.plugins.lark.notice.sdk.MessageSender;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Abstract class for sending Lark messages.
 * Provides common methods for sending messages via Lark API.
 * Extending classes need to implement the getRobotConfig() method to retrieve the robot configuration.
 *
 * @author xm.z
 */
@Slf4j
public abstract class AbstractMessageSender implements MessageSender {

    /**
     * Retrieves the robot configuration information.
     *
     * @return The robot configuration information.
     */
    protected abstract RobotConfigModel getRobotConfig();

    /**
     * Sends a message by calling the Lark API.
     *
     * @param body The request body.
     * @return The send result.
     */
    protected SendResult sendMessage(String body, String... headers) {
        SendResult sendResult;
        try {
            RobotConfigModel robotConfig = getRobotConfig();
            String webhook = robotConfig.getWebhook();

            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(webhook))
                    .header(CONTENT_TYPE, APPLICATION_JSON_VALUE).timeout(Duration.ofMinutes(3))
                    .POST(HttpRequest.BodyPublishers.ofString(StringUtils.defaultString(body)));

            if (ArrayUtils.isNotEmpty(headers)) {
                if (RobotType.DING_TAlK.equals(robotConfig.getRobotType())) {
                    String uri = webhook + String.format("&%s=%s&%s=%s", headers[0], headers[1], headers[2], headers[3]);
                    builder.uri(URI.create(uri));
                }
                builder.headers(headers);
            }

            HttpResponse<String> response = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL).proxy(robotConfig.getProxySelector()).build()
                    .send(builder.build(), HttpResponse.BodyHandlers.ofString());

            sendResult = JsonUtils.readValue(response.body(), SendResult.class);
        } catch (Exception e) {
            log.error("Failed to send Lark message", e);
            sendResult = SendResult.fail(ExceptionUtils.getStackTrace(e));
        }
        Optional.ofNullable(sendResult).ifPresent(result -> result.setRequestBody(body));
        return sendResult;
    }

}
