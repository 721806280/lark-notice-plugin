package io.jenkins.plugins.lark.notice.sdk;

import io.jenkins.plugins.lark.notice.config.LarkRetryConfig;
import io.jenkins.plugins.lark.notice.enums.MsgTypeEnum;
import io.jenkins.plugins.lark.notice.model.MessageModel;
import io.jenkins.plugins.lark.notice.sdk.model.SendResult;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Behavioral tests for retry handling in {@link MessageDispatcher}.
 */
public class MessageDispatcherRetryTest {

    @Test
    public void shouldRetryUntilSuccess() {
        RetryPolicy policy = RetryPolicy.from(new LarkRetryConfig(true, 3, 0, 0, 1.0, 0.0));
        TestDispatcher dispatcher = new TestDispatcher(policy);

        AtomicInteger attempts = new AtomicInteger();
        MessageSender sender = new MessageSender() {
            @Override
            public SendResult sendText(MessageModel msg) {
                int count = attempts.incrementAndGet();
                return count < 3 ? SendResult.fail("fail") : new SendResult(0, "ok", null);
            }

            @Override
            public SendResult sendMarkdown(MessageModel msg) {
                return sendText(msg);
            }
        };

        MessageModel message = MessageModel.builder()
                .type(MsgTypeEnum.TEXT)
                .text("hello")
                .build();

        SendResult result = dispatcher.send(null, null, message, sender);
        assertTrue(result.isOk());
        assertEquals(3, attempts.get());
    }

    @Test
    public void shouldStopAfterMaxAttempts() {
        RetryPolicy policy = RetryPolicy.from(new LarkRetryConfig(true, 2, 0, 0, 1.0, 0.0));
        TestDispatcher dispatcher = new TestDispatcher(policy);

        AtomicInteger attempts = new AtomicInteger();
        MessageSender sender = new MessageSender() {
            @Override
            public SendResult sendText(MessageModel msg) {
                attempts.incrementAndGet();
                return SendResult.fail("fail");
            }

            @Override
            public SendResult sendMarkdown(MessageModel msg) {
                return sendText(msg);
            }
        };

        MessageModel message = MessageModel.builder()
                .type(MsgTypeEnum.TEXT)
                .text("hello")
                .build();

        SendResult result = dispatcher.send(null, null, message, sender);
        assertFalse(result.isOk());
        assertEquals(2, attempts.get());
    }

    private static final class TestDispatcher extends MessageDispatcher {
        private final RetryPolicy retryPolicy;

        private TestDispatcher(RetryPolicy retryPolicy) {
            super(MessageSenderRegistry.getInstance());
            this.retryPolicy = retryPolicy;
        }

        @Override
        RetryPolicy resolveRetryPolicy(String robotId) {
            return retryPolicy;
        }
    }
}
