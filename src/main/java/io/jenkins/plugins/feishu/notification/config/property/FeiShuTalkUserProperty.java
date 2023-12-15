package io.jenkins.plugins.feishu.notification.config.property;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import io.jenkins.plugins.feishu.notification.Messages;
import lombok.Getter;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * 用户属性类，用于在 Jenkins 中为用户添加 FeiShuTalk 相关的属性
 *
 * @author xm.z
 */
@Getter
public class FeiShuTalkUserProperty extends UserProperty {

    /**
     * 用户的手机号码
     */
    private final String mobile;

    /**
     * 用户的 OpenID
     */
    private final String openId;

    /**
     * 构造函数
     *
     * @param mobile 用户的手机号码
     * @param openId 用户的 OpenID
     */
    public FeiShuTalkUserProperty(String mobile, String openId) {
        this.mobile = mobile;
        this.openId = openId;
    }

    /**
     * 静态内部类，用于描述该 UserProperty 的属性和行为。
     */
    @Extension(ordinal = 1)
    public static final class FeiShuTalkUserPropertyDescriptor extends UserPropertyDescriptor {

        /**
         * 获取该 UserProperty 在 Jenkins UI 中显示的名称。
         *
         * @return 该 UserProperty 在 Jenkins UI 中显示的名称。
         */
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.user_property_title();
        }

        /**
         * 创建一个新的 UserProperty 实例。
         *
         * @param user 包含该 UserProperty 的用户。
         * @return 新的 UserProperty 实例。
         */
        @Override
        public UserProperty newInstance(User user) {
            return new FeiShuTalkUserProperty(null, null);
        }

        /**
         * 使用指定的请求数据创建一个新的 UserProperty 实例。
         *
         * @param req      Stapler 请求对象，用于获取表单数据。
         * @param formData 表单数据。
         * @return 新的 UserProperty 实例。
         */
        @Override
        public UserProperty newInstance(@Nullable StaplerRequest req, @NonNull JSONObject formData) {
            return new FeiShuTalkUserProperty(formData.optString("mobile"), formData.optString("openId"));
        }
    }
}
