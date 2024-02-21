package io.jenkins.plugins.lark.notice.config;

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
 * Represents the configuration for Lark notification proxy, used to configure the proxy server information for Lark notifications.
 *
 * <p>The LarkProxyConfig class extends Descriptor, indicating that this configuration item can be configured in the Jenkins system configuration center.</p>
 *
 * <p>LarkProxyConfig includes the following properties:</p>
 * <ul>
 *     <li>type: The proxy type, supporting SOCKS and HTTP.</li>
 *     <li>host: The hostname or IP address of the proxy server.</li>
 *     <li>port: The port number of the proxy server.</li>
 * </ul>
 *
 * <p>LarkProxyConfig contains an obtainProxySelector method for obtaining an instance of the proxy selector for the proxy server.</p>
 *
 * @author xm.z
 */
@Getter
@Setter
@ToString
@Extension
public class LarkProxyConfig extends Descriptor<LarkProxyConfig> implements Describable<LarkProxyConfig> {

    /**
     * Proxy type, supporting SOCKS and HTTP.
     */
    private Type type;

    /**
     * Hostname or IP address of the proxy server.
     */
    private String host;

    /**
     * Port number of the proxy server.
     */
    private Integer port;

    /**
     * Creates a new instance of LarkProxyConfig.
     */
    public LarkProxyConfig() {
        super(LarkProxyConfig.class);
    }

    /**
     * Constructs a new instance of LarkProxyConfig with input parameters.
     *
     * @param type Proxy type.
     * @param host Hostname or IP address of the proxy server.
     * @param port Port number of the proxy server.
     */
    @DataBoundConstructor
    public LarkProxyConfig(Type type, String host, int port) {
        this();
        this.type = type;
        this.host = host;
        this.port = port;
    }

    /**
     * Sets the hostname or IP address of the proxy server, removing any leading or trailing whitespace.
     *
     * @param host Hostname or IP address of the proxy server.
     */
    public void setHost(String host) {
        if (host != null) {
            this.host = host.trim();
        }
    }

    /**
     * Returns the descriptor for LarkProxyConfig.
     *
     * @return The descriptor for LarkProxyConfig.
     */
    @Override
    public Descriptor<LarkProxyConfig> getDescriptor() {
        return this;
    }

    /**
     * Obtains an instance of the proxy selector for the proxy server, returning NO_PROXY if no proxy is needed.
     *
     * @return An instance of the proxy selector for the proxy server.
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