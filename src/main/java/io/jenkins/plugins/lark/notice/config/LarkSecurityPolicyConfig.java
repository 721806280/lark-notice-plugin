package io.jenkins.plugins.lark.notice.config;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.Secret;
import io.jenkins.plugins.lark.notice.enums.SecurityPolicyEnum;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Represents a configuration for Lark security policies. This class is used to define and manage
 * different types of security policies that can be applied within a Lark application or service.
 * It encapsulates the policy type, its value, and a description to provide context.
 *
 * @author xm.z
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class LarkSecurityPolicyConfig implements Describable<LarkSecurityPolicyConfig> {

    /**
     * The type of the security policy
     */
    private String type;

    /**
     * The value of the security policy, stored securely
     */
    private Secret value;

    /**
     * A description of the security policy
     */
    private String desc;

    /**
     * Data-bound constructor for setting up a new LarkSecurityPolicyConfig instance.
     *
     * @param type  The type of the security policy.
     * @param value The value of the security policy.
     * @param desc  A description of the security policy.
     */
    @DataBoundConstructor
    public LarkSecurityPolicyConfig(String type, String value, String desc) {
        this.type = type;
        this.desc = desc;
        this.value = Secret.fromString(value);
    }

    /**
     * Factory method to create a new LarkSecurityPolicyConfig instance from an enum representing
     * a security policy.
     *
     * @param securityPolicyEnum The enum representing the security policy.
     * @return A new instance of LarkSecurityPolicyConfig with the type and description set from the enum.
     */
    public static LarkSecurityPolicyConfig of(SecurityPolicyEnum securityPolicyEnum) {
        return new LarkSecurityPolicyConfig(
                securityPolicyEnum.name(), "", securityPolicyEnum.getDesc());
    }

    /**
     * Retrieves the plain text value of the security policy.
     *
     * @return The decrypted value of the security policy if set; otherwise, null.
     */
    public String getValue() {
        if (value == null) {
            return null;
        }
        return value.getPlainText();
    }

    /**
     * Sets the value of the security policy. The value is securely stored.
     *
     * @param value The value to be set for the security policy.
     */
    public void setValue(String value) {
        this.value = Secret.fromString(value);
    }

    /**
     * Provides the descriptor for this class which is used by Jenkins for UI binding and instantiation.
     *
     * @return The descriptor instance for this class.
     */
    @Override
    public Descriptor<LarkSecurityPolicyConfig> getDescriptor() {
        return Jenkins.get().getDescriptorByType(LarkSecurityPolicyConfigDescriptor.class);
    }

    /**
     * Descriptor for {@link LarkSecurityPolicyConfig}. This class is necessary for Jenkins to
     * properly handle our {@link LarkSecurityPolicyConfig} instances within its UI and data management system.
     */
    @Extension
    public static class LarkSecurityPolicyConfigDescriptor extends Descriptor<LarkSecurityPolicyConfig> {
    }
}
