package io.jenkins.plugins.lark.notice.sdk.impl;

import io.jenkins.cli.shaded.org.apache.commons.lang.StringUtils;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.model.RobotConfigModel;
import io.jenkins.plugins.lark.notice.sdk.HttpClientFactory;
import io.jenkins.plugins.lark.notice.sdk.MessageSender;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Abstract class for sending Lark messages.
 * Provides common methods for sending messages via Lark API.
 * Extending classes need to implement the getRobotConfig() method to retrieve the robot configuration.
 *
 * @author xm.z
 */
@Slf4j
public abstract class AbstractMessageSender implements MessageSender {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(3);

    /**
     * Retrieves the robot configuration information.
     *
     * @return The robot configuration information.
     */
    protected abstract RobotConfigModel getRobotConfig();

    /**
     * Sends a message to the Lark API using the provided JSON body and optional headers.
     *
     * @param jsonBody The request body in JSON format.
     * @param headers  Additional headers to be included in the HTTP request, if any.
     * @return A SendResult object containing either the response from the Lark API or error details.
     */
    protected SendResult sendMessage(String jsonBody, String... headers) {
        RobotConfigModel robotConfig = getRobotConfig();
        String webhookUrl = robotConfig.getWebhook();

        try {
            HttpRequest.Builder requestBuilder = createHttpRequest(webhookUrl, jsonBody);
            configureCustomHeaders(requestBuilder, robotConfig, headers);

            HttpClient httpClient = HttpClientFactory.buildHttpClient(robotConfig.getProxySelector(), robotConfig.getNoSsl());
            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

            SendResult sendResult = JsonUtils.readValue(response.body(), SendResult.class);
            Optional.ofNullable(sendResult).ifPresent(result -> result.setRequestBody(jsonBody));
            return sendResult;
        } catch (ConnectException e) {
            log.error("Connection refused or unable to establish: {}, Webhook URL: {}", e.getMessage(), webhookUrl, e);
            return SendResult.fail("Connection refused or unable to establish: " + e.getMessage());
        } catch (IOException e) {
            log.error("IO error occurred while sending Lark message: {}", e.getMessage(), e);
            return SendResult.fail("IO error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send Lark message", e);
            return SendResult.fail(e.getMessage());
        }
    }

    /**
     * Creates an HTTP request with the specified webhook URL and JSON body.
     *
     * @param webhookUrl The URL of the Lark webhook.
     * @param jsonBody   The JSON string representing the message body.
     * @return An HttpRequest.Builder configured with the necessary settings.
     */
    private HttpRequest.Builder createHttpRequest(String webhookUrl, String jsonBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl)).timeout(DEFAULT_TIMEOUT)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(StringUtils.defaultString(jsonBody)));
    }

    /**
     * Configures custom headers on the HTTP request builder based on the robot type and provided headers.
     *
     * @param requestBuilder The HttpRequest.Builder to which headers will be added.
     * @param robotConfig    The configuration model for the robot.
     * @param headers        Array of strings representing additional headers.
     */
    private void configureCustomHeaders(HttpRequest.Builder requestBuilder, RobotConfigModel robotConfig, String[] headers) {
        if (headers == null || headers.length == 0) {
            return;
        }

        if (RobotType.DING_TAlK.equals(robotConfig.getRobotType()) && headers.length >= 4) {
            // Assuming headers are in key=value pairs and appending them to the webhook URL for DingTalk.
            String updatedWebhook = robotConfig.getWebhook() + String.format("&%s=%s&%s=%s", headers[0], headers[1], headers[2], headers[3]);
            requestBuilder.uri(URI.create(updatedWebhook));
        } else {
            requestBuilder.headers(headers);
        }
    }
}