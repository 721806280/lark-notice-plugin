package io.jenkins.plugins.lark.notice.sdk;

import hudson.model.TaskListener;
import io.jenkins.plugins.lark.notice.config.LarkGlobalConfig;
import io.jenkins.plugins.lark.notice.config.LarkProxyConfig;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.lang.reflect.Field;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests cache invalidation behavior in {@link MessageDispatcher}.
 */
public class MessageDispatcherCacheInvalidationTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void setUp() {
        MessageDispatcher.getInstance().clearSenders();
        LarkGlobalConfig.getInstance().setRobotConfigs(new ArrayList<>(List.of(createRobot("robot-a"))));
        LarkGlobalConfig.getInstance().setProxyConfig(null);
    }

    @Test
    public void proxyConfigChangeShouldClearCachedSenders() throws Exception {
        MessageDispatcher dispatcher = MessageDispatcher.getInstance();

        dispatcher.send(TaskListener.NULL, "robot-a", null);
        assertEquals(1, senderCache(dispatcher).size());

        LarkGlobalConfig.getInstance().setProxyConfig(new LarkProxyConfig(Proxy.Type.HTTP, "127.0.0.1", 8080));
        assertEquals(0, senderCache(dispatcher).size());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, MessageSender> senderCache(MessageDispatcher dispatcher) throws Exception {
        Field field = MessageDispatcher.class.getDeclaredField("senders");
        field.setAccessible(true);
        return (Map<String, MessageSender>) field.get(dispatcher);
    }

    private static LarkRobotConfig createRobot(String id) {
        return new LarkRobotConfig(
                id,
                "Robot-" + id,
                "https://open.feishu.cn/open-apis/bot/v2/hook/" + id,
                List.of()
        );
    }
}
