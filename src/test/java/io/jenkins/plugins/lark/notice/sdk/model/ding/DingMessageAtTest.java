package io.jenkins.plugins.lark.notice.sdk.model.ding;

import io.jenkins.plugins.lark.notice.sdk.model.lark.support.at.At;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests for DingTalk mention rendering.
 *
 * @author xm.z
 */
public class DingMessageAtTest {

    @Test
    public void textMessageShouldRenderAtAll() {
        At at = new At();
        at.setIsAtAll(true);

        DingTextMessage message = DingTextMessage.build(at, "build finished");

        assertTrue(message.getText().getContent().contains("@all"));
    }

    @Test
    public void markdownMessageShouldRenderAtAll() {
        At at = new At();
        at.setIsAtAll(true);

        DingMdMessage message = DingMdMessage.build(at, "title", "build finished");

        assertTrue(message.getMarkdown().getText().contains("@all"));
    }
}
