package io.jenkins.plugins.lark.notice.sdk.model.lark.support;

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

    private String url;

    private String type;

    private String text;

    public Button(String content, String url, String type) {
        this.url = url;
        this.text = content;
        this.type = StringUtils.defaultIfBlank(type, "primary_filled");
    }

}
