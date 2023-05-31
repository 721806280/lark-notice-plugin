package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import io.jenkins.plugins.FeiShuTalkRobotConfig.FeiShuTalkRobotConfigDescriptor;
import io.jenkins.plugins.enums.NoticeOccasionEnum;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.ToString;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.net.ProxySelector;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 飞书机器人插件全局配置的类。
 *
 * <p>使用 FeiShuTalkGlobalConfig 可以获取和更新所有机器人的全局配置，包括代理配置、调试模式、通知事件等。</p>
 *
 * <p>FeiShuTalkGlobalConfig 类通过实现 Describable 接口来提供描述器，用于在 Jenkins 系统管理中心页面
 * 中展示全局配置界面。</p>
 *
 * <p>FeiShuTalkGlobalConfig 中包含多个机器人配置（FeiShuTalkRobotConfig），并且每个机器人都可以单独进行
 * 配置和管理。</p>
 *
 * @author xm.z
 */
@Getter
@ToString
@Extension
@Symbol("fsTalk")
@SuppressWarnings("unused")
public class FeiShuTalkGlobalConfig extends Descriptor<FeiShuTalkGlobalConfig> implements Describable<FeiShuTalkGlobalConfig> {

    /**
     * 代理配置。
     */
    private FeiShuTalkProxyConfig proxyConfig;

    /**
     * 是否开启调试模式。
     */
    private boolean verbose;

    /**
     * 所有通知事件的集合。
     */
    private Set<String> noticeOccasions = Arrays.stream(NoticeOccasionEnum.values()).map(Enum::name).collect(Collectors.toSet());

    /**
     * 多个机器人的配置信息列表。
     */
    private ArrayList<FeiShuTalkRobotConfig> robotConfigs = new ArrayList<>();

    /**
     * FeiShuTalkGlobalConfig 的构造函数，用于创建实例并初始化各属性的值。
     *
     * @param proxyConfig     代理配置。
     * @param verbose         是否开启调试模式。
     * @param noticeOccasions 所有通知事件的集合。
     * @param robotConfigs    多个机器人的配置信息列表。
     */
    @DataBoundConstructor
    public FeiShuTalkGlobalConfig(FeiShuTalkProxyConfig proxyConfig, boolean verbose,
                                  Set<String> noticeOccasions, ArrayList<FeiShuTalkRobotConfig> robotConfigs) {
        this.proxyConfig = proxyConfig;
        this.verbose = verbose;
        this.noticeOccasions = noticeOccasions;
        this.robotConfigs = robotConfigs;
    }

    /**
     * FeiShuTalkGlobalConfig 的默认构造函数。
     * 在 Jenkins 启动时会调用该方法进行实例化并加载配置文件。
     */
    public FeiShuTalkGlobalConfig() {
        super(FeiShuTalkGlobalConfig.class);
        this.load();
    }

    /**
     * 获取 FeiShuTalkGlobalConfig 实例。
     *
     * @return FeiShuTalkGlobalConfig 实例。
     */
    public static FeiShuTalkGlobalConfig getInstance() {
        return Jenkins.get().getDescriptorByType(FeiShuTalkGlobalConfig.class);
    }

    /**
     * 根据机器人 ID 获取机器人配置。
     *
     * @param robotId 机器人 ID。
     * @return 机器人配置。
     */
    public static Optional<FeiShuTalkRobotConfig> getRobot(String robotId) {
        return getInstance().robotConfigs.stream().filter(item -> Objects.equals(item.getId(), robotId)).findAny();
    }

    /**
     * 从代理配置中获取代理选择器。
     *
     * @return 代理选择器。
     */
    public ProxySelector obtainProxySelector() {
        return proxyConfig == null ? null : proxyConfig.obtainProxySelector();
    }

    /**
     * 设置是否开启调试模式。
     *
     * @param verbose 是否开启调试模式。
     */
    @DataBoundSetter
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * 设置所有通知事件的集合。
     *
     * @param noticeOccasions 所有通知事件的集合。
     */
    @DataBoundSetter
    public void setNoticeOccasions(Set<String> noticeOccasions) {
        this.noticeOccasions = noticeOccasions;
    }

    /**
     * 设置代理配置。
     *
     * @param proxyConfig 代理配置。
     */
    @DataBoundSetter
    public void setProxyConfig(FeiShuTalkProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    /**
     * 设置多个机器人的配置信息列表。
     *
     * @param robotConfigs 多个机器人的配置信息列表。
     */
    @DataBoundSetter
    public void setRobotConfigs(ArrayList<FeiShuTalkRobotConfig> robotConfigs) {
        this.robotConfigs = robotConfigs;
    }

    /**
     * 将表单提交的 JSON 对象转换成 FeiShuTalkGlobalConfig 类型的实例，并保存到配置文件中。
     *
     * @param req  HTTP 请求对象。
     * @param json 表单提交的 JSON 对象。
     * @return true 如果配置保存成功，false 否则。
     * @throws FormException 如果表单提交的数据不能正确绑定到对象上，则抛出该异常。
     */
    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        Object robotConfigObj = json.get("robotConfigs");
        if (robotConfigObj == null) {
            json.put("robotConfigs", new JSONArray());
        } else {
            JSONArray robotConfigs = JSONArray.fromObject(robotConfigObj);
            robotConfigs.removeIf(item -> {
                JSONObject jsonObject = JSONObject.fromObject(item);
                String webhook = jsonObject.getString("webhook");
                return StringUtils.isEmpty(webhook);
            });
        }
        req.bindJSON(this, json);
        this.save();
        return super.configure(req, json);
    }

    /**
     * 获取所有通知事件的枚举值。
     *
     * @return 所有通知事件的枚举值。
     */
    public NoticeOccasionEnum[] getAllNoticeOccasions() {
        return NoticeOccasionEnum.values();
    }

    /**
     * 获取描述器。
     *
     * @return 描述器对象。
     */
    @Override
    public Descriptor<FeiShuTalkGlobalConfig> getDescriptor() {
        return this;
    }

    /**
     * 获取代理配置。
     *
     * @return 代理配置。
     */
    public FeiShuTalkProxyConfig getFeiShuTalkProxyConfig() {
        return Jenkins.get().getDescriptorByType(FeiShuTalkProxyConfig.class);
    }

    /**
     * 获取机器人配置的描述器。
     *
     * @return 机器人配置的描述器。
     */
    public FeiShuTalkRobotConfigDescriptor getFeiShuTalkRobotConfigDescriptor() {
        return Jenkins.get().getDescriptorByType(FeiShuTalkRobotConfigDescriptor.class);
    }
}