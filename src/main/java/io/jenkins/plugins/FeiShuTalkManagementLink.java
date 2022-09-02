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
 * FeiShuTalkManagementLink
 *
 * @author xm.z
 */
@Extension(ordinal = Double.MAX_VALUE)
public class FeiShuTalkManagementLink extends ManagementLink {

    @Override
    public String getIconFileName() {
        return "/plugin/feishu-notifications/images/talk.png";
    }

    @Override
    public String getDisplayName() {
        return Messages.displayName();
    }

    @Override
    public String getUrlName() {
        return "fsTalk";
    }

    @Override
    public String getDescription() {
        return Messages.ManagementLink_description();
    }

    @POST
    public void doConfigure(StaplerRequest req, StaplerResponse res)
            throws ServletException, FormException, IOException {
        getFeiShuTalkGlobalConfigDescriptor().configure(req, req.getSubmittedForm());
        FormApply.success(req.getContextPath() + "/manage").generateResponse(req, res, null);
    }

    /**
     * 全局配置页面
     *
     * @return FeiShuTalkGlobalConfig
     */
    public Descriptor<FeiShuTalkGlobalConfig> getFeiShuTalkGlobalConfigDescriptor() {
        return Jenkins.get().getDescriptorByType(FeiShuTalkGlobalConfig.class);
    }

}