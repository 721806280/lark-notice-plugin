package io.jenkins.plugins;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

/**
 * @author xm.z
 */
@Getter
@Setter
@ToString
@Extension
public class FeiShuTalkProxyConfig extends Descriptor<FeiShuTalkProxyConfig>
        implements Describable<FeiShuTalkProxyConfig> {

    private Type type;

    private String host;

    private Integer port;

    public FeiShuTalkProxyConfig() {
        super(FeiShuTalkProxyConfig.class);
    }

    @DataBoundConstructor
    public FeiShuTalkProxyConfig(Type type, String host, int port) {
        this();
        this.type = type;
        this.host = host;
        this.port = port;
    }

    public void setHost(String host) {
        if (host != null) {
            this.host = host.trim();
        }
    }

    @Override
    public Descriptor<FeiShuTalkProxyConfig> getDescriptor() {
        return this;
    }

    public Proxy getProxy() {
        // {@link Proxy#Proxy(Type, SocketAddress)} 防止抛异常
        if (type == Type.DIRECT || StringUtils.isEmpty(host) || port == null) {
            return Proxy.NO_PROXY;
        }
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        return new Proxy(type, inetSocketAddress);
    }
}
