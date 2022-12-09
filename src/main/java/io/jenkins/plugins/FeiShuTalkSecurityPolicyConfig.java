package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.Secret;
import io.jenkins.plugins.enums.SecurityPolicyEnum;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * 安全策略配置页面
 *
 * @author xm.z
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class FeiShuTalkSecurityPolicyConfig implements Describable<FeiShuTalkSecurityPolicyConfig> {

    private String type;

    private String desc;

    private Secret value;

    @DataBoundConstructor
    public FeiShuTalkSecurityPolicyConfig(String type, String value, String desc) {
        this.type = type;
        this.desc = desc;
        this.value = Secret.fromString(value);
    }

    public static FeiShuTalkSecurityPolicyConfig of(SecurityPolicyEnum securityPolicyEnum) {
        return new FeiShuTalkSecurityPolicyConfig(
                securityPolicyEnum.name(), "", securityPolicyEnum.getDesc());
    }

    public String getValue() {
        if (value == null) {
            return null;
        }
        return value.getPlainText();
    }

    public void setValue(String value) {
        this.value = Secret.fromString(value);
    }

    @Override
    public Descriptor<FeiShuTalkSecurityPolicyConfig> getDescriptor() {
        return Jenkins.get().getDescriptorByType(FeiShuTalkSecurityPolicyConfigDescriptor.class);
    }

    @Extension
    public static class FeiShuTalkSecurityPolicyConfigDescriptor
            extends Descriptor<FeiShuTalkSecurityPolicyConfig> {
    }
}
