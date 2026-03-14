package io.jenkins.plugins.lark.notice.tools;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

    @Test
    public void isMobileShouldValidateCommonPatterns() {
        assertFalse(Utils.isMobile(null));
        assertFalse(Utils.isMobile(""));
        assertFalse(Utils.isMobile("123"));
        assertFalse(Utils.isMobile("10000000000"));

        assertTrue(Utils.isMobile("13912345678"));
        assertTrue(Utils.isMobile("8613912345678"));
        assertTrue(Utils.isMobile("+8613912345678"));
    }

    @Test
    public void joinShouldHandleNullAndJoinLines() {
        assertEquals("", Utils.join(null));
        assertEquals("a\nb\nc", Utils.join(List.of("a", "b", "c")));
    }
}
