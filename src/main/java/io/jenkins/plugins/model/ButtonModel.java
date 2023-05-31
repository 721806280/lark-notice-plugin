package io.jenkins.plugins.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * 用于存储按钮相关的模型。
 *
 * @author xm.z
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ButtonModel extends AbstractDescribableImpl<ButtonModel> {

    /**
     * 按钮名称。
     */
    private String title;

    /**
     * 点击按钮请求的地址。
     */
    private String url;

    /**
     * 按钮类型 primary | danger | default。
     */
    private String type;

    /**
     * 构造函数。
     *
     * @param title 按钮名称。
     * @param url   点击按钮请求的地址。
     * @param type  按钮类型。
     */
    @DataBoundConstructor
    public ButtonModel(String title, String url, String type) {
        this.title = title;
        this.url = url;
        this.type = type;
    }

    /**
     * 描述符类，用于表示该模型的属性在 Jenkins 中的可配置选项。
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ButtonModel> {

    }

}