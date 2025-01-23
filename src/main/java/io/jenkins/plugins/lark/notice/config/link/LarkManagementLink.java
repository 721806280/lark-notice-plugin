package io.jenkins.plugins.lark.notice.config.link;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.ManagementLink;
import hudson.util.FormApply;
import io.jenkins.plugins.lark.notice.Messages;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import jakarta.servlet.ServletException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;

/**
 * LarkManagementLink provides a management link in Jenkins' system configuration page
 * for configuring the Lark notification plugin. This class extends ManagementLink,
 * allowing it to define additional management options specific to the Lark plugin.
 *
 * @author xm.z
 */
@Extension(ordinal = Double.MAX_VALUE)
public class LarkManagementLink extends ManagementLink {

    /**
     * Returns the path to the icon file displayed next to the link on the management page.
     *
     * @return A string representing the relative path to the icon file.
     */
    @Override
    public String getIconFileName() {
        return "/plugin/lark-notice/images/logo.png";
    }

    /**
     * Returns the display name of the management link as defined in resource bundle.
     *
     * @return A string representing the display name of the link.
     */
    @Override
    public String getDisplayName() {
        return Messages.plugin_display_name();
    }

    /**
     * Returns the URL name of the management link. This URL is appended to "/manage"
     * to access this management link.
     *
     * @return A string representing the URL name.
     */
    @Override
    public String getUrlName() {
        return "lark";
    }

    /**
     * Provides a detailed description for the management link, typically used as tooltip text.
     *
     * @return A string representing the description of the management link.
     */
    @Override
    public String getDescription() {
        return Messages.plugin_management_description();
    }

    /**
     * Processes the configuration submission for the Lark plugin. If the user has administrative
     * permissions, this method updates the plugin's global configuration based on the submitted form data.
     *
     * @param req The request object containing the form submission.
     * @param res The response object used to redirect the user after successful submission.
     * @throws ServletException If an error occurs during the servlet operation.
     * @throws FormException    If there is an error processing the submitted form.
     * @throws IOException      If an I/O error occurs.
     */
    @POST
    public void doConfigure(StaplerRequest2 req, StaplerResponse2 res)
            throws FormException, IOException, jakarta.servlet.ServletException {
        if (Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
            getLarkGlobalConfigDescriptor().configure(req, req.getSubmittedForm());
            FormApply.success(req.getContextPath() + "/manage").generateResponse(req, res, null);
        }
    }

    /**
     * Retrieves the descriptor instance for the LarkGlobalConfig class, which contains
     * the configuration logic and data for the Lark plugin.
     *
     * @return The descriptor instance for LarkGlobalConfig.
     */
    public Descriptor<LarkGlobalConfig> getLarkGlobalConfigDescriptor() {
        return Jenkins.get().getDescriptorByType(LarkGlobalConfig.class);
    }

}
