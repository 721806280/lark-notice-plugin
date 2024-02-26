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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
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
     * Define a mock TrustManager to ignore certificate validation
     */
    private static final TrustManager MOCK_TRUST_MANAGER = new X509ExtendedTrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
        }
    };

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

            // Create HttpRequest.Builder
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(webhook))
                    .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .timeout(Duration.ofMinutes(3))
                    .POST(HttpRequest.BodyPublishers.ofString(StringUtils.defaultString(body)));

            if (ArrayUtils.isNotEmpty(headers)) {
                if (RobotType.DING_TAlK.equals(robotConfig.getRobotType())) {
                    String uri = webhook + String.format("&%s=%s&%s=%s", headers[0], headers[1], headers[2], headers[3]);
                    builder.uri(URI.create(uri));
                }
                builder.headers(headers);
            }

            // Create HttpClient and send the request
            HttpResponse<String> response = createHttpClient(robotConfig)
                    .send(builder.build(), HttpResponse.BodyHandlers.ofString());
            sendResult = JsonUtils.readValue(response.body(), SendResult.class);
        } catch (Exception e) {
            log.error("Failed to send Lark message", e);
            sendResult = SendResult.fail(ExceptionUtils.getStackTrace(e));
        }
        Optional.ofNullable(sendResult).ifPresent(result -> result.setRequestBody(body));
        return sendResult;
    }

    /**
     * Create HttpClient.
     *
     * @param robotConfig Robot configuration information.
     * @return HttpClient instance.
     * @throws Exception Exception during HttpClient creation.
     */
    private HttpClient createHttpClient(RobotConfigModel robotConfig) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{MOCK_TRUST_MANAGER}, new SecureRandom());
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .proxy(robotConfig.getProxySelector())
                .sslContext(sslContext)
                .build();
    }

}
