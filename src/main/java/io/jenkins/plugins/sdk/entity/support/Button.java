package io.jenkins.plugins.sdk.entity.support;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

/**
 * Button
 *
 * @author xm.z
 */
@Data
public class Button {

    private String tag = "button";

    private String title;

    private String url;

    private String type;

    private LarkMdText text;

    public Button(String title, String actionUrl, String type) {
        this.title = title;
        this.url = actionUrl;
        this.text = new LarkMdText("plain_text", title);
        this.type = StringUtils.defaultIfBlank(type, "primary");
    }

    public static Button of(String title, String actionUrl) {
        return new Button(title, actionUrl, null);
    }

}
