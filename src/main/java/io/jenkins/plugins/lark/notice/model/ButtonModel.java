package io.jenkins.plugins.lark.notice.model;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Model for storing button-related information. This class represents a button element that can be used in various UI contexts,
 * containing properties such as the button's title, the URL to be requested upon clicking, and the button's visual style type.
 * It extends {@link Describable} to allow for easy integration with Jenkins' configuration system.
 *
 * @author xm.z
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ButtonModel implements Describable<ButtonModel> {

    /**
     * The title of the button. This is typically displayed as the button's text.
     */
    private String title;

    /**
     * The URL to be requested when the button is clicked. This could link to an internal or external resource.
     */
    private String url;

    /**
     * The type of the button, which determines its visual style. Valid types include "primary", "danger", and "default".
     */
    private String type;

    /**
     * Constructor for creating a new instance of a ButtonModel with specified title, URL, and type.
     *
     * @param title The title (text) of the button.
     * @param url   The URL to be requested upon clicking the button.
     * @param type  The visual style type of the button ("primary", "danger", "default").
     */
    @DataBoundConstructor
    public ButtonModel(String title, String url, String type) {
        this.title = title;
        this.url = url;
        this.type = type;
    }

    /**
     * Descriptor class for {@link ButtonModel}. This class is used to describe the properties of the ButtonModel
     * that are configurable in Jenkins UI. By extending {@link Descriptor}, it integrates with Jenkins' system
     * for managing plugin configurations, allowing users to specify values for title, url, and type through the Jenkins interface.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ButtonModel> {

    }

}