package io.jenkins.plugins.lark.notice.logging;

import hudson.model.TaskListener;
import org.junit.After;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link NoticeLog}.
 *
 * @author xm.z
 */
public class NoticeLogTest {

    @After
    public void tearDown() {
        NoticeLogSettings.reset();
    }

    @Test
    public void verboseShouldRespectRuntimeVerboseSetting() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        TaskListener listener = taskListener(output);

        NoticeLogSettings.useVerboseResolver(() -> false);
        NoticeLog.verbose(listener, "hidden");
        assertEquals("", output.toString(StandardCharsets.UTF_8));

        NoticeLogSettings.useVerboseResolver(() -> true);
        NoticeLog.verbose(listener, "visible %s", "entry");
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("[Lark] visible entry"));
    }

    @Test
    public void traceShouldRenderStructuredFields() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        TaskListener listener = taskListener(output);

        NoticeLogSettings.useVerboseResolver(() -> true);
        NoticeLog.trace(listener, NoticeTrace.NOTIFICATION_PREPARE,
                NoticeLog.field(NoticeLogKey.SOURCE, "run-listener"),
                NoticeLog.field(NoticeLogKey.CONFIG_TOTAL, 2));

        String line = output.toString(StandardCharsets.UTF_8);
        assertTrue(line.contains("[Lark] [notification.prepare]"));
        assertTrue(line.contains("source=run-listener"));
        assertTrue(line.contains("configTotal=2"));
    }

    @Test
    public void failureMessageAndAbbreviateShouldProduceStableOutput() {
        assertEquals("[Lark] hello world", NoticeLog.failureMessage("hello %s", "world"));
        assertEquals("ab...", NoticeLog.abbreviate("abcdef", 5));
    }

    private static TaskListener taskListener(ByteArrayOutputStream output) {
        return () -> new PrintStream(output, true, StandardCharsets.UTF_8);
    }
}
