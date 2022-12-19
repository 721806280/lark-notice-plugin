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

    /**
     * 按钮名称
     */
    private String title;

    /**
     * 点击按钮请求的地址
     */
    private String url;

    /**
     * 按钮类型 primary | danger | default
     */
    private String type;

    @DataBoundConstructor
    public ButtonModel(String title, String url, String type) {
        this.title = title;
        this.url = url;
        this.type = type;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ButtonModel> {

    }

}
