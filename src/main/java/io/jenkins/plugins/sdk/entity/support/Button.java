package io.jenkins.plugins.sdk.entity.support;

import lombok.Data;

/**
 * Button
 *
 * @author xm.z
 */
@Data
public class Button {

    private String type = "primary";

    private String tag = "button";

    private String title;

    private String url;

    private LarkMdText text;

    public Button(String title, String actionUrl) {
        this.title = title;
        this.url = actionUrl;
        this.text = new LarkMdText("plain_text", title);
    }

    public static Button of(String title, String actionUrl) {
        return new Button(title, actionUrl);
    }

}
