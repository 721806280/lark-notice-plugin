package io.jenkins.plugins.lark.notice.config.property;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import io.jenkins.plugins.lark.notice.Messages;
import lombok.Getter;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest2;

/**
 * Represents a user property for storing Lark-specific information about a Jenkins user.
 * This includes the user's mobile number and OpenID which can be used for notifications or identification in Lark.
 *
 * @author xm.z
 */
@Getter
public class LarkUserProperty extends UserProperty {

    /**
     * The mobile number associated with the user's Lark account.
     */
    private final String mobile;

    /**
     * The OpenID associated with the user's Lark account.
     */
    private final String openId;

    /**
     * Constructs a new LarkUserProperty with the specified mobile number and OpenID.
     *
     * @param mobile The mobile number associated with the user's Lark account.
     * @param openId The OpenID associated with the user's Lark account.
     */
    public LarkUserProperty(String mobile, String openId) {
        this.mobile = mobile;
        this.openId = openId;
    }

    /**
     * Descriptor for {@link LarkUserProperty}. This descriptor is used by Jenkins to manage the user property,
     * including presenting configuration options in the UI and handling data binding for the property.
     */
    @Extension(ordinal = 1)
    public static final class LarkUserPropertyDescriptor extends UserPropertyDescriptor {

        /**
         * Returns the display name for this user property, which is shown in the Jenkins UI.
         *
         * @return A string containing the display name of this user property.
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.user_property_title();
        }

        /**
         * Creates a new instance of {@link LarkUserProperty} with default values.
         * This is used when a new user is created.
         *
         * @param user The user for whom the property is being created.
         * @return A new instance of LarkUserProperty with null for both mobile and openId.
         */
        @Override
        public UserProperty newInstance(User user) {
            return new LarkUserProperty(null, null);
        }

        /**
         * Creates a new instance of {@link LarkUserProperty} using data submitted from the configuration form.
         *
         * @param req      The request from which to read the submission.
         * @param formData The JSON object containing the form data.
         * @return A new instance of LarkUserProperty initialized with form data.
         */
        @Override
        public UserProperty newInstance(@Nullable StaplerRequest2 req, @NonNull JSONObject formData) {
            return new LarkUserProperty(formData.optString("mobile"), formData.optString("openId"));
        }
    }
}

