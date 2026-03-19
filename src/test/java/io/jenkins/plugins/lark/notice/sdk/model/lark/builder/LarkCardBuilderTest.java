package io.jenkins.plugins.lark.notice.sdk.model.lark.builder;

import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Button;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.Card;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.container.columns.ColumnElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.container.columns.ColumnSetElement;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.button.ButtonBehavior;
import io.jenkins.plugins.lark.notice.sdk.model.lark.support.form.button.ButtonElement;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for Lark card button rendering.
 */
public class LarkCardBuilderTest {

    @Test
    public void shouldPopulateClickableUrlsForButtonColumns() {
        String targetUrl = "https://example.com/job/demo/42/console";
        Card card = new LarkCardBuilder()
                .withButtons(List.of(new Button("Console", targetUrl, "default")))
                .build();

        ColumnSetElement columnSet = (ColumnSetElement) card.getBody().getElements().get(0);
        ColumnElement column = columnSet.getColumns().get(0);
        ButtonElement button = (ButtonElement) column.getElements().get(0);
        ButtonBehavior behavior = button.getBehaviors().get(0);

        assertNotNull(column.getAction());
        assertEquals(targetUrl, behavior.getDefaultUrl());
    }
}
