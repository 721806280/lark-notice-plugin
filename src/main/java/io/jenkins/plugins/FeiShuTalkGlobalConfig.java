package io.jenkins.plugins;

import hudson.Extension;
import io.jenkins.plugins.FeiShuTalkRobotConfig.FeiShuTalkRobotConfigDescriptor;
import io.jenkins.plugins.enums.NoticeOccasionEnum;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.ToString;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局配置
 *
 * @author xm.z
 */
@Getter
@ToString
@Extension(ordinal = 100)
@SuppressWarnings("unused")
public class FeiShuTalkGlobalConfig extends GlobalConfiguration {
    private static volatile FeiShuTalkGlobalConfig instance;

    /**
     * 网络代理
     */
    private FeiShuTalkProxyConfig proxyConfig;

    /**
     * 是否打印详细日志
     */
    private boolean verbose;

    /**
     * 通知时机
     */
    private Set<String> noticeOccasions =
            Arrays.stream(NoticeOccasionEnum.values()).map(Enum::name).collect(Collectors.toSet());

    /**
     * 机器人配置列表
     */
    private ArrayList<FeiShuTalkRobotConfig> robotConfigs = new ArrayList<>();

    @DataBoundConstructor
    public FeiShuTalkGlobalConfig(
            FeiShuTalkProxyConfig proxyConfig,
            boolean verbose,
            Set<String> noticeOccasions,
            ArrayList<FeiShuTalkRobotConfig> robotConfigs) {
        this.proxyConfig = proxyConfig;
        this.verbose = verbose;
        this.noticeOccasions = noticeOccasions;
        this.robotConfigs = robotConfigs;
    }

    public FeiShuTalkGlobalConfig() {
        this.load();
    }

    /**
     * 获取全局配置信息
     *
     * @return 全局配置信息
     */
    public static FeiShuTalkGlobalConfig getInstance() {
        if (instance == null) {
            synchronized (FeiShuTalkGlobalConfig.class) {
                if (instance == null) {
                    instance = GlobalConfiguration.all().getInstance(FeiShuTalkGlobalConfig.class);
                }
            }
        }
        return instance;
    }

    /**
     * 通知时机列表
     *
     * @return 通知时机
     */
    public NoticeOccasionEnum[] getNoticeOccasionTypes() {
        return NoticeOccasionEnum.values();
    }

    /**
     * 获取网络代理
     *
     * @return proxy
     */
    public Proxy getProxy() {
        if (proxyConfig == null) {
            return null;
        }
        return proxyConfig.getProxy();
    }

    @DataBoundSetter
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @DataBoundSetter
    public void setNoticeOccasions(Set<String> noticeOccasions) {
        this.noticeOccasions = noticeOccasions;
    }

    @DataBoundSetter
    public void setProxyConfig(FeiShuTalkProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    @DataBoundSetter
    public void setRobotConfigs(ArrayList<FeiShuTalkRobotConfig> robotConfigs) {
        this.robotConfigs = robotConfigs;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        Object robotConfigObj = json.get("robotConfigs");
        if (robotConfigObj == null) {
            json.put("robotConfigs", new JSONArray());
        } else {
            JSONArray robotConfigs = JSONArray.fromObject(robotConfigObj);
            robotConfigs.removeIf(
                    item -> {
                        JSONObject jsonObject = JSONObject.fromObject(item);
                        String webhook = jsonObject.getString("webhook");
                        return StringUtils.isEmpty(webhook);
                    });
        }
        //    System.out.println(json.toString());
        req.bindJSON(this, json);
        this.save();
        return super.configure(req, json);
    }

    /**
     * `网络代理` 配置页面
     *
     * @return 网络代理配置页面
     */
    public FeiShuTalkProxyConfig getFeiShuTalkProxyConfig() {
        return Jenkins.get().getDescriptorByType(FeiShuTalkProxyConfig.class);
    }

    /**
     * `机器人` 配置页面
     *
     * @return 机器人配置页面
     */
    public FeiShuTalkRobotConfigDescriptor getFeiShuTalkRobotConfigDescriptor() {
        return Jenkins.get().getDescriptorByType(FeiShuTalkRobotConfigDescriptor.class);
    }
}
