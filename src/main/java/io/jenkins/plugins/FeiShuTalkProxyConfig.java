package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.net.*;
import java.net.Proxy.Type;
import java.util.Collections;
import java.util.List;

/**
 * 表示飞书通知代理配置，用于配置飞书通知的代理服务器信息。
 *
 * <p>FeiShuTalkProxyConfig 类继承 Descriptor，表示该配置项可以在 Jenkins 系统配置中心进行配置。</p>
 *
 * <p>FeiShuTalkProxyConfig 包含以下属性：</p>
 * <ul>
 *     <li>type：代理类型，支持 SOCKS 和 HTTP。</li>
 *     <li>host：代理服务器主机名或 IP 地址。</li>
 *     <li>port：代理服务器端口号。</li>
 * </ul>
 *
 * <p>FeiShuTalkProxyConfig 包含一个 obtainProxySelector 方法，用于获取代理服务器的选择器实例。</p>
 *
 * @author xm.z
 */
@Getter
@Setter
@ToString
@Extension
public class FeiShuTalkProxyConfig extends Descriptor<FeiShuTalkProxyConfig> implements Describable<FeiShuTalkProxyConfig> {

    /**
     * 获取代理类型，支持 SOCKS 和 HTTP。
     */
    private Type type;

    /**
     * 获取代理服务器主机名或 IP 地址。
     */
    private String host;

    /**
     * 获取代理服务器端口号。
     */
    private Integer port;

    /**
     * 创建 FeiShuTalkProxyConfig 的新实例。
     */
    public FeiShuTalkProxyConfig() {
        super(FeiShuTalkProxyConfig.class);
    }

    /**
     * 根据输入参数构造 FeiShuTalkProxyConfig 的新实例。
     *
     * @param type 代理类型。
     * @param host 代理服务器主机名或 IP 地址。
     * @param port 代理服务器端口号。
     */
    @DataBoundConstructor
    public FeiShuTalkProxyConfig(Type type, String host, int port) {
        this();
        this.type = type;
        this.host = host;
        this.port = port;
    }

    /**
     * 设置代理服务器主机名或 IP 地址，并去除首尾空白字符。
     *
     * @param host 代理服务器主机名或 IP 地址。
     */
    public void setHost(String host) {
        if (host != null) {
            this.host = host.trim();
        }
    }

    /**
     * 获取 FeiShuTalkProxyConfig 的描述器。
     *
     * @return FeiShuTalkProxyConfig 的描述器。
     */
    @Override
    public Descriptor<FeiShuTalkProxyConfig> getDescriptor() {
        return this;
    }

    /**
     * 获取代理服务器的选择器实例，如果不需要代理则返回 NO_PROXY。
     *
     * @return 代理服务器选择器实例。
     */
    public ProxySelector obtainProxySelector() {
        return new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                if (type == Type.DIRECT || StringUtils.isEmpty(host) || port == null) {
                    return Collections.singletonList(Proxy.NO_PROXY);
                }
                InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
                return Collections.singletonList(new Proxy(type, inetSocketAddress));
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                /* ignore */
            }
        };
    }
}
