package io.jenkins.plugins.lark.notice.sdk.impl;

import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.JsonNode;
import io.jenkins.plugins.lark.notice.enums.BuildStatusEnum;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.enums.RobotType;
import io.jenkins.plugins.lark.notice.model.BuildJobModel;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.model.RobotConfigModel;
import io.jenkins.plugins.lark.notice.sdk.MessageDispatcher;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Button;
import io.jenkins.plugins.lark.notice.tools.JsonUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for WeCom message payload generation.
 */
public class WechatWorkMessageSenderTest {

    private HttpServer server;

    private AtomicReference<String> requestBody;

    @Before
    public void setUp() throws IOException {
        requestBody = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/cgi-bin/webhook/send", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{\"errcode\":0,\"errmsg\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void sendTextShouldUseWechatWorkPayloadShape() {
        RobotConfigModel robotConfig = new RobotConfigModel();
        robotConfig.setRobotType(RobotType.WECHAT_WORK);
        robotConfig.setWebhook("http://localhost:" + server.getAddress().getPort() + "/cgi-bin/webhook/send?key=token");
        WechatWorkMessageSender sender = new WechatWorkMessageSender(robotConfig);

        MessageModel message = MessageModel.builder()
                .text("hello")
                .atAll(false)
                .atUserIds(Set.of("13800138000", "zhangsan"))
                .build();

        SendResult result = sender.sendText(message);

        assertTrue(result.isOk());
        assertEquals(requestBody.get(), result.getRequestBody());
        assertTrue(requestBody.get().contains("\"msgtype\":\"text\""));
        assertTrue(requestBody.get().contains("\"content\":\"hello\""));
        assertTrue(requestBody.get().contains("\"mentioned_list\":[\"zhangsan\"]"));
        assertTrue(requestBody.get().contains("\"mentioned_mobile_list\":[\"13800138000\"]"));
    }

    @Test
    public void cardMessageShouldUseNewsNoticeTemplateCardPayload() {
        RobotConfigModel robotConfig = new RobotConfigModel();
        robotConfig.setRobotType(RobotType.WECHAT_WORK);
        robotConfig.setWebhook("http://localhost:" + server.getAddress().getPort() + "/cgi-bin/webhook/send?key=token");
        WechatWorkMessageSender sender = new WechatWorkMessageSender(robotConfig);

        MessageModel message = MessageModel.builder()
                .type(MsgTypeEnum.CARD)
                .title("Build Notice")
                .text("build ok")
                .projectName("Demo Project")
                .projectUrl("https://jenkins.example/job/demo/")
                .jobName("#1")
                .jobUrl("https://jenkins.example/job/demo/1/")
                .statusType(BuildStatusEnum.SUCCESS)
                .duration("1 sec")
                .executorName("xm.z")
                .buttons(List.of(
                        new Button("Changes", "https://jenkins.example/job/demo/1/changes", "primary_filled"),
                        new Button("Console", "https://jenkins.example/job/demo/1/console", "default")
                ))
                .atAll(false)
                .atUserIds(Set.of("zhangsan"))
                .build();

        SendResult result = MessageDispatcher.getInstance().send(null, null, message, sender);

        assertTrue(result.isOk());
        JsonNode root = JsonUtils.readTree(requestBody.get());
        JsonNode card = root.path("template_card");
        assertEquals("template_card", root.path("msgtype").asText());
        assertEquals("news_notice", card.path("card_type").asText());
        assertFalse(card.has("quote_area"));
        assertEquals("https://get.jenkins.io/art/jenkins-logo/favicon.ico", card.path("source").path("icon_url").asText());
        assertEquals("Lark Notice · Jenkins", card.path("source").path("desc").asText());
        assertEquals(3, card.path("source").path("desc_color").asInt());
        assertEquals("Build Notice", card.path("main_title").path("title").asText());
        assertTrue(card.path("main_title").path("desc").isNull());
        assertEquals("https://www.jenkins.io/images/post-images/2025/07/24/redesigning-jenkins-part-two.png",
                card.path("card_image").path("url").asText());
        assertEquals(2.25d, card.path("card_image").path("aspect_ratio").asDouble(), 0.001d);
        assertHorizontalContent(card.path("horizontal_content_list").get(0),
                1, "Task Name", "Demo Project", "https://jenkins.example/job/demo/");
        assertHorizontalContent(card.path("horizontal_content_list").get(1),
                1, "Job Number", "#1", "https://jenkins.example/job/demo/1/");
        assertHorizontalContent(card.path("horizontal_content_list").get(2),
                0, "Build Status", "Success", "");
        assertHorizontalContent(card.path("horizontal_content_list").get(3),
                0, "Build Duration", "1 sec", "");
        assertHorizontalContent(card.path("horizontal_content_list").get(4),
                0, "Executor", "xm.z", "");
        assertEquals("https://jenkins.example/job/demo/1/changes", card.path("card_action").path("url").asText());
        assertEquals("Changes", card.path("jump_list").get(0).path("title").asText());
        assertEquals("Console", card.path("jump_list").get(1).path("title").asText());
    }

    @Test
    public void buildNoticeMarkdownShouldUseWechatWorkCompatibleFormat() {
        BuildJobModel model = BuildJobModel.builder()
                .title("Build Notice")
                .projectName("Demo Project")
                .projectUrl("https://jenkins.example/job/demo/")
                .jobName("#1")
                .jobUrl("https://jenkins.example/job/demo/1/")
                .statusType(BuildStatusEnum.SUCCESS)
                .duration("1 sec")
                .executorName("xm.z")
                .build();

        String markdown = model.toMarkdown(RobotType.WECHAT_WORK, Locale.US);

        assertTrue(markdown.contains(">**Task Name**: [Demo Project](https://jenkins.example/job/demo/)"));
        assertTrue(markdown.contains(">**Job Number**: [#1](https://jenkins.example/job/demo/1/)"));
        assertTrue(markdown.contains(">**Build Status**: <font color=\"info\">Success</font>"));
        assertTrue(markdown.contains(">**Build Duration**: 1 sec"));
        assertTrue(markdown.contains(">**Executor**: xm.z"));
    }

    private static void assertHorizontalContent(JsonNode content, int type, String key, String value, String url) {
        assertEquals(type, content.path("type").asInt());
        assertEquals(key, content.path("keyname").asText());
        assertEquals(value, content.path("value").asText());
        if (type == 1) {
            assertEquals(url, content.path("url").asText());
        } else {
            assertTrue(content.path("url").isNull());
        }
    }
}
