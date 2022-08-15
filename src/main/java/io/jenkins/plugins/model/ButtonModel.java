package io.jenkins.plugins.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author xm.z
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ButtonModel extends AbstractDescribableImpl<ButtonModel> {

    private String type = "primary";

    private String tag = "button";

    private String title;

    private String url;

    private ButtonText text;

    @DataBoundConstructor
    public ButtonModel(String title, String actionUrl) {
        this.title = title;
        this.url = actionUrl;
        this.text = new ButtonText("plain_text", title);
    }

    public static ButtonModel of(String title, String actionUrl) {
        return new ButtonModel(title, actionUrl);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ButtonModel> {

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ButtonText {
        private String tag;

        private String content;
    }

}
