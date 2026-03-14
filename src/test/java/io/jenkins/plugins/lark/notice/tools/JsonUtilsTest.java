package io.jenkins.plugins.lark.notice.tools;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class JsonUtilsTest {

    @Test
    public void toJsonShouldReturnNullForNullInput() {
        assertNull(JsonUtils.toJson(null));
        assertNull(JsonUtils.toPrettyJson(null));
    }

    @Test
    public void readValueAndListShouldHandleBlankInput() {
        assertNull(JsonUtils.readValue("  ", Sample.class));
        assertTrue(JsonUtils.readList("   ", Sample.class).isEmpty());
    }

    @Test
    public void isValidJsonShouldRejectTrailingTokens() {
        assertTrue(JsonUtils.isValidJson("{\"a\":1}"));
        assertFalse(JsonUtils.isValidJson("{\"a\":1}{}"));
        assertFalse(JsonUtils.isValidJson(""));
    }

    @Test
    public void readTreeShouldParseJson() {
        JsonNode node = JsonUtils.readTree("{\"a\":1}");
        assertEquals(1, node.get("a").asInt());
    }

    @Test
    public void readListShouldDeserializeArray() {
        String payload = "[{\"name\":\"alpha\"},{\"name\":\"beta\"}]";
        List<Sample> list = JsonUtils.readList(payload, Sample.class);
        assertEquals(2, list.size());
        assertEquals("alpha", list.get(0).name);
        assertEquals("beta", list.get(1).name);
    }

    private static final class Sample {
        public String name;
    }
}
