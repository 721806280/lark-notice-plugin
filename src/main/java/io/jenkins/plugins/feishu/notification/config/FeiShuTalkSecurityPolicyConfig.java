package io.jenkins.plugins.feishu.notification.config;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.Secret;
import io.jenkins.plugins.feishu.notification.enums.SecurityPolicyEnum;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * FeiShuTalkSecurityPolicyConfig 是一个 Jenkins 配置项，用于存储飞书机器人的安全策略配置信息。
 *
 * <p>该类包含了以下三个属性：</p>
 * <ul>
 *     <li>type - 安全类型</li>
 *     <li>value - 策略值</li>
 *     <li>desc - 描述</li>
 * </ul>
 *
 * <p>该类提供了以下几个方法：</p>
 * <ul>
 *     <li>构造函数 {@link #FeiShuTalkSecurityPolicyConfig(String, String, String)}</li>
 *     <li>静态工厂方法 {@link #of(SecurityPolicyEnum)}</li>
 *     <li>获取策略值的方法 {@link #getValue()}</li>
 *     <li>设置策略值的方法 {@link #setValue(String)}</li>
 *     <li>获得描述器的方法 {@link #getDescriptor()}</li>
 * </ul>
 *
 * <p>构造函数使用 {@link DataBoundConstructor} 注解以自动绑定表单参数，同时将传入的字符串转换为 Secret 类型保存，
 * 以保证信息的安全性。静态工厂方法 {@link #of(SecurityPolicyEnum)} 用于创建对应安全类型的默认配置，
 * value 属性为空。</p>
 *
 * <p>获取策略值的方法 {@link #getValue()} 返回加密后的策略值明文，或者空值（如果未设置策略值）。
 * 设置策略值的方法 {@link #setValue(String)} 接受一个字符串类型的参数，
 * 并将该参数转换为 Secret 类型保存，以保证信息的安全性。</p>
 *
 * <p>该类实现了 Jenkins 的扩展点 {@link Descriptor}，
 * 并提供了描述器 {@link FeiShuTalkSecurityPolicyConfigDescriptor}，用于管理该类的配置信息。</p>
 *
 * @author xm.z
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class FeiShuTalkSecurityPolicyConfig implements Describable<FeiShuTalkSecurityPolicyConfig> {

    /**
     * 安全类型
     */
    private String type;

    /**
     * 策略值
     */
    private Secret value;

    /**
     * 描述
     */
    private String desc;

    /**
     * 构造函数。使用 DataBoundConstructor 注解自动绑定表单参数。
     *
     * @param type  安全类型
     * @param value 策略值
     * @param desc  描述
     */
    @DataBoundConstructor
    public FeiShuTalkSecurityPolicyConfig(String type, String value, String desc) {
        this.type = type;
        this.desc = desc;
        this.value = Secret.fromString(value);
    }

    /**
     * 静态工厂方法，用于创建对应安全类型的默认配置。
     *
     * @param securityPolicyEnum 安全类型
     * @return FeiShuTalkSecurityPolicyConfig 对象
     */
    public static FeiShuTalkSecurityPolicyConfig of(SecurityPolicyEnum securityPolicyEnum) {
        return new FeiShuTalkSecurityPolicyConfig(
                securityPolicyEnum.name(), "", securityPolicyEnum.getDesc());
    }

    /**
     * 获取策略值的方法。返回加密后的策略值明文，或者空值（如果未设置策略值）。
     *
     * @return 策略值明文或空
     */
    public String getValue() {
        if (value == null) {
            return null;
        }
        return value.getPlainText();
    }

    /**
     * 设置策略值的方法。接受一个字符串类型的参数，
     * 并将该参数转换为 Secret 类型保存，以保证信息的安全性。
     *
     * @param value 要设置的策略值
     */
    public void setValue(String value) {
        this.value = Secret.fromString(value);
    }

    /**
     * 获得描述器的方法，用于管理该类的配置信息。
     *
     * @return 描述器
     */
    @Override
    public Descriptor<FeiShuTalkSecurityPolicyConfig> getDescriptor() {
        return Jenkins.get().getDescriptorByType(FeiShuTalkSecurityPolicyConfigDescriptor.class);
    }

    /**
     * 该类描述器，用于管理该类的配置信息。
     * 该类实现了 Jenkins 的扩展点 descriptor，以允许该类被用作插件扩展点。
     */
    @Extension
    public static class FeiShuTalkSecurityPolicyConfigDescriptor
            extends Descriptor<FeiShuTalkSecurityPolicyConfig> {
    }
}