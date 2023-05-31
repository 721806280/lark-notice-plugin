package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.ManagementLink;
import hudson.util.FormApply;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Jenkins 系统管理中心下的飞书通知管理页面链接。
 *
 * <p>FeiShuTalkManagementLink 类继承 ManagementLink，表示该链接可以展示在 Jenkins 系统管理中心下。
 * 该页面用于对飞书机器人和全局通知配置进行添加、编辑和删除等操作。</p>
 *
 * <p>FeiShuTalkManagementLink 包含一个 doConfigure 方法，用于保存用户提交的表单数据，并根据用户权限决定
 * 是否允许执行此操作。</p>
 *
 * @author xm.z
 */
@Extension(ordinal = Double.MAX_VALUE)
public class FeiShuTalkManagementLink extends ManagementLink {

    /**
     * 获取飞书通知管理页面链接的图标文件名。
     *
     * @return 飞书通知管理页面链接的图标文件名。
     */
    @Override
    public String getIconFileName() {
        return "/plugin/feishu-notification/images/logo.png";
    }

    /**
     * 获取飞书通知管理页面链接在系统管理中心的显示名称。
     *
     * @return 飞书通知管理页面链接在系统管理中心的显示名称。
     */
    @Override
    public String getDisplayName() {
        return Messages.displayName();
    }

    /**
     * 获取飞书通知管理页面链接的 URL 名称。
     *
     * @return 飞书通知管理页面链接的 URL 名称。
     */
    @Override
    public String getUrlName() {
        return "fsTalk";
    }

    /**
     * 获取飞书通知管理页面链接在系统管理中心的描述信息。
     *
     * @return 飞书通知管理页面链接在系统管理中心的描述信息。
     */
    @Override
    public String getDescription() {
        return Messages.ManagementLink_description();
    }

    /**
     * 处理用户提交的表单数据，并保存到全局配置中。
     *
     * @param req HTTP 请求对象。
     * @param res HTTP 响应对象。
     * @throws ServletException Servlet 异常。
     * @throws FormException    表单异常。
     * @throws IOException      IO 异常。
     */
    @POST
    public void doConfigure(StaplerRequest req, StaplerResponse res)
            throws ServletException, FormException, IOException {
        if (Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
            getFeiShuTalkGlobalConfigDescriptor().configure(req, req.getSubmittedForm());
            FormApply.success(req.getContextPath() + "/manage").generateResponse(req, res, null);
        }
    }

    /**
     * 获取飞书通知全局配置的描述器。
     *
     * @return 飞书通知全局配置的描述器。
     */
    public Descriptor<FeiShuTalkGlobalConfig> getFeiShuTalkGlobalConfigDescriptor() {
        return Jenkins.get().getDescriptorByType(FeiShuTalkGlobalConfig.class);
    }

}