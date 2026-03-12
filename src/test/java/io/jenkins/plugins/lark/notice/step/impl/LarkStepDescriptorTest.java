package io.jenkins.plugins.lark.notice.step.impl;

import hudson.model.Descriptor.FormException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests helper-field normalization used by the Lark pipeline step snippet generator.
 *
 * @author xm.z
 */
public class LarkStepDescriptorTest {

    @Test
    public void normalizeFormDataShouldMapHelperFieldsToBindableParameters() throws Exception {
        JSONObject formData = new JSONObject();
        formData.element("robot", "robot-a");
        formData.element("type", "CARD");
        formData.element("title", "Build Notice");
        formData.element("textValue", "line one\n\n line two ");
        formData.element("postJson", "[[{\"tag\":\"text\",\"text\":\"hello\"}]]");
        formData.element("topImgJson", "{\"imgKey\":\"img-top\",\"title\":\"Top image\",\"preview\":true}");
        formData.element("bottomImgJson", "{\"imgKey\":\"img-bottom\",\"title\":\"Bottom image\"}");
        formData.element("buttonsJson", "[{\"title\":\"Open\",\"url\":\"https://example.com\",\"type\":\"primary_filled\"}]");

        JSONObject normalized = LarkStep.LarkStepDescriptor.normalizeFormData(formData);

        assertFalse(normalized.has("textValue"));
        assertFalse(normalized.has("postJson"));
        assertFalse(normalized.has("topImgJson"));
        assertFalse(normalized.has("bottomImgJson"));
        assertFalse(normalized.has("buttonsJson"));

        JSONArray text = normalized.getJSONArray("text");
        assertEquals(2, text.size());
        assertEquals("line one", text.getString(0));
        assertEquals("line two", text.getString(1));

        JSONArray post = normalized.getJSONArray("post");
        assertEquals("text", post.getJSONArray(0).getJSONObject(0).getString("tag"));
        assertEquals("hello", post.getJSONArray(0).getJSONObject(0).getString("text"));

        JSONObject topImg = normalized.getJSONObject("topImg");
        assertEquals("img-top", topImg.getString("imgKey"));
        assertTrue(topImg.getBoolean("preview"));

        JSONObject bottomImg = normalized.getJSONObject("bottomImg");
        assertEquals("img-bottom", bottomImg.getString("imgKey"));

        JSONArray buttons = normalized.getJSONArray("buttons");
        assertEquals(1, buttons.size());
        assertEquals("Open", buttons.getJSONObject(0).getString("title"));
        assertEquals("primary_filled", buttons.getJSONObject(0).getString("type"));
    }

    @Test
    public void normalizeFormDataShouldRejectInvalidJsonHelpers() {
        JSONObject formData = new JSONObject();
        formData.element("postJson", "{");

        try {
            LarkStep.LarkStepDescriptor.normalizeFormData(formData);
        } catch (FormException ex) {
            assertEquals("postJson", ex.getFormField());
            return;
        }

        throw new AssertionError("Expected invalid JSON to raise FormException");
    }
}
