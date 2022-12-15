package io.jenkins.plugins;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import lombok.Getter;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * 飞书用户信息配置
 *
 * @author xm.z
 */
public class FeiShuTalkUserProperty extends UserProperty {

    @Getter
    private final String mobile;

    public FeiShuTalkUserProperty(String mobile) {
        this.mobile = mobile;
    }

    @Extension(ordinal = 1)
    public static final class FeiShuTalkUserPropertyDescriptor extends UserPropertyDescriptor {

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.UserProperty_mobile();
        }

        @Override
        public UserProperty newInstance(User user) {
            return new FeiShuTalkUserProperty(null);
        }

        @Override
        public UserProperty newInstance(@Nullable StaplerRequest req, @NonNull JSONObject formData) {
            return new FeiShuTalkUserProperty(formData.optString("mobile"));
        }
    }
}
