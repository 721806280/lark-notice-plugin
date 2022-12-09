package io.jenkins.plugins.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author xm.z
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ButtonModel extends AbstractDescribableImpl<ButtonModel> {

    private String title;

    private String url;

    @DataBoundConstructor
    public ButtonModel(String title, String actionUrl) {
        this.title = title;
        this.url = actionUrl;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ButtonModel> {

    }

}
