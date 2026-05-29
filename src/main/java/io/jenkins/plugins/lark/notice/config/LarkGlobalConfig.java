package io.jenkins.plugins.lark.notice.config;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import io.jenkins.plugins.lark.notice.config.LarkRobotConfig.LarkRobotConfigDescriptor;
import io.jenkins.plugins.lark.notice.config.security.LarkPermissions;
import io.jenkins.plugins.lark.notice.enums.NoticeOccasionEnum;
import io.jenkins.plugins.lark.notice.sdk.MessageSenderRegistry;
import jenkins.model.Jenkins;
import lombok.Getter;
import lombok.ToString;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest2;

import java.net.ProxySelector;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Global configuration for the Lark notification plugin.
 * <p>
 * This class extends {@link Descriptor} and implements {@link Describable} to integrate
 * into the Jenkins "Configure System" page as a persistent global setting. It manages
 * network proxy settings, logging verbosity, notification triggers, and the list of configured Lark robots.
 * </p>
 *
 * @author xm.z
 */
@Getter
@ToString
@Extension
@SuppressWarnings("unused")
public class LarkGlobalConfig extends Descriptor<LarkGlobalConfig> implements Describable<LarkGlobalConfig> {

    /**
     * The network proxy configuration for outbound connections.
     */
    private LarkProxyConfig proxyConfig;

    /**
     * Whether verbose logging is enabled for debugging and tracing purposes.
     */
    private boolean verbose;

    /**
     * Set of occasion names (corresponding to {@link NoticeOccasionEnum}) that trigger notifications.
     */
    private Set<String> noticeOccasions = defaultNoticeOccasions();

    /**
     * The backing list of individual Lark robot configurations.
     */
    private ArrayList<LarkRobotConfig> robotConfigs = new ArrayList<>();

    /**
     * In-memory cache index mapping Robot ID to its configuration for fast lookups.
     * Marked as volatile to ensure visibility across threads under Double-Checked Locking (DCL).
     */
    private transient volatile Map<String, LarkRobotConfig> robotConfigIndex;

    /**
     * Data-bound constructor used by Jenkins to instantiate the global configuration
     * when the system configuration form is submitted.
     *
     * @param proxyConfig     The proxy settings, or {@code null} if no proxy is needed.
     * @param verbose         {@code true} to enable verbose logging.
     * @param noticeOccasions The set of build scenarios that should trigger notifications.
     * @param robotConfigs    The list of individual Lark robot configurations.
     */
    @DataBoundConstructor
    public LarkGlobalConfig(LarkProxyConfig proxyConfig, boolean verbose,
                            Set<String> noticeOccasions, ArrayList<LarkRobotConfig> robotConfigs) {
        this.proxyConfig = proxyConfig;
        this.verbose = verbose;
        setNoticeOccasions(noticeOccasions);
        setRobotConfigs(robotConfigs);
    }

    /**
     * Default constructor.
     * Invokes {@link #load()} to automatically deserialize previously saved configurations from disk (XML).
     */
    public LarkGlobalConfig() {
        super(LarkGlobalConfig.class);
        load();
    }

    /**
     * Retrieves the active singleton instance of this global configuration from the Jenkins context.
     *
     * @return The active {@link LarkGlobalConfig} instance.
     */
    public static LarkGlobalConfig getInstance() {
        return Jenkins.get().getDescriptorByType(LarkGlobalConfig.class);
    }

    /**
     * Retrieves a specific Lark robot configuration by its unique ID.
     * Utilizes the thread-safe internal index cache for rapid retrieval.
     *
     * @param robotId The unique identifier of the target robot.
     * @return An {@link Optional} containing the configuration if found; otherwise {@link Optional#empty()}.
     */
    public static Optional<LarkRobotConfig> getRobot(String robotId) {
        if (robotId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(getInstance().robotIndex().get(robotId));
    }

    /**
     * Provides the default baseline selection of notice occasions (all active by default).
     */
    private static Set<String> defaultNoticeOccasions() {
        return Arrays.stream(NoticeOccasionEnum.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    /**
     * Indexes the raw list of robot configurations into a predictable, insertion-ordered {@link LinkedHashMap}.
     * Filters out null items or items with null keys to guarantee structural reliability.
     */
    private static Map<String, LarkRobotConfig> buildRobotIndex(ArrayList<LarkRobotConfig> robotConfigs) {
        Map<String, LarkRobotConfig> index = new LinkedHashMap<>();
        for (LarkRobotConfig robotConfig : robotConfigs) {
            if (robotConfig != null && robotConfig.getId() != null) {
                index.put(robotConfig.getId(), robotConfig);
            }
        }
        return index;
    }

    /**
     * Obtains a standard {@link ProxySelector} based on the current proxy configurations.
     *
     * @return A valid {@link ProxySelector}, or {@code null} if proxying is disabled.
     */
    public ProxySelector obtainProxySelector() {
        return proxyConfig == null ? null : proxyConfig.obtainProxySelector();
    }

    /**
     * Sets whether verbose logging is enabled.
     *
     * @param verbose {@code true} to enable verbose outputs.
     */
    @DataBoundSetter
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Updates the set of build occasions that trigger a notification.
     *
     * @param noticeOccasions A set of occasion names. If {@code null}, it resets to the default full-selection.
     */
    @DataBoundSetter
    public void setNoticeOccasions(Set<String> noticeOccasions) {
        this.noticeOccasions = noticeOccasions == null ? defaultNoticeOccasions() : new HashSet<>(noticeOccasions);
    }

    /**
     * Updates the global proxy configuration.
     * <p>Clears cached senders in {@link MessageSenderRegistry} to force reconnection through the new proxy path.</p>
     *
     * @param proxyConfig The new proxy configuration, or {@code null} to disable proxy usage.
     */
    @DataBoundSetter
    public void setProxyConfig(LarkProxyConfig proxyConfig) {
        MessageSenderRegistry.getInstance().clear();
        this.proxyConfig = proxyConfig;
    }

    /**
     * Returns a read-only snapshot of the configured Lark robots.
     * <p>Uses {@link List#copyOf(java.util.Collection)} to ensure the returned list is immutable,
     * preventing accidental external mutations to the underlying array.</p>
     *
     * @return An immutable {@link List} of {@link LarkRobotConfig}.
     */
    public List<LarkRobotConfig> getRobotConfigs() {
        return List.copyOf(robotConfigs);
    }

    /**
     * Updates the global list of robot configurations.
     * <p>Clears the cached senders in {@link MessageSenderRegistry} and invalidates the internal lookup index cache.</p>
     *
     * @param robotConfigs The updated list of robot configurations. If {@code null}, it defaults to an empty list.
     */
    @DataBoundSetter
    public void setRobotConfigs(ArrayList<LarkRobotConfig> robotConfigs) {
        MessageSenderRegistry.getInstance().clear();
        this.robotConfigs = robotConfigs == null ? new ArrayList<>() : new ArrayList<>(robotConfigs);
        invalidateRobotIndex();
    }

    /**
     * Callback method executed when the global configuration form is saved in the Jenkins UI.
     * Handles permission checks, form payload sanitization (filtering invalid robots), data binding, and persistence.
     *
     * @param req  The Stapler incoming request context.
     * @param json The structured JSON payload representing the submitted form.
     * @return Always returns {@code true} indicating a successful form processing.
     * @throws FormException If data binding, processing, or validation rules fail.
     */
    @Override
    public boolean configure(StaplerRequest2 req, JSONObject json) throws FormException {
        // Access Control: Ensure the current operator possesses global configuration privileges
        Jenkins.get().checkPermission(LarkPermissions.CONFIGURE);

        // Data Sanitization: Filter out malformed robot data payloads (e.g., missing webhook URLs)
        json.put("robotConfigs", GlobalConfigFormDataSanitizer.normalizeRobotConfigsPayload(json.get("robotConfigs")));

        // Map the sanitized JSON back into this object instance via DataBoundSetters
        req.bindJSON(this, json);

        // Persist current field state into the backing XML storage file
        save();
        return super.configure(req, json);
    }

    /**
     * Returns all available notification occasions for UI selection rendering in Jelly files.
     *
     * @return An array containing all values of {@link NoticeOccasionEnum}.
     */
    public NoticeOccasionEnum[] getAllNoticeOccasions() {
        return NoticeOccasionEnum.values();
    }

    /**
     * Returns the proxy configuration Descriptor. Used for UI binding in Jelly via {@code f:property}.
     *
     * @return The descriptor instance of {@link LarkProxyConfig}.
     */
    public LarkProxyConfig getLarkProxyConfigDescriptor() {
        return Jenkins.get().getDescriptorByType(LarkProxyConfig.class);
    }

    /**
     * Returns the robot configuration Descriptor. Used for UI dynamic list rendering via {@code f:repeatable}.
     *
     * @return The descriptor instance of {@link LarkRobotConfigDescriptor}.
     */
    public LarkRobotConfigDescriptor getLarkRobotConfigDescriptor() {
        return Jenkins.get().getDescriptorByType(LarkRobotConfigDescriptor.class);
    }

    /**
     * Exposes a shared configuration views host class.
     * Allows separate UI components to safely implement stable lookups via {@code st:include}.
     *
     * @return The class token representing the shared view boundary.
     */
    public Class<?> getSharedViewsClass() {
        return SharedConfigViews.class;
    }

    @Override
    public Descriptor<LarkGlobalConfig> getDescriptor() {
        return this;
    }

    /**
     * Obtains or lazily initializes the internal lookup map using Double-Checked Locking (DCL).
     * Ensures high performance and safe thread visibility during concurrent build executions.
     */
    private Map<String, LarkRobotConfig> robotIndex() {
        Map<String, LarkRobotConfig> index = robotConfigIndex;
        if (index != null) {
            return index;
        }
        synchronized (this) {
            if (robotConfigIndex == null) {
                robotConfigIndex = buildRobotIndex(robotConfigs);
            }
            return robotConfigIndex;
        }
    }

    /**
     * Invalidates the volatile cache index by resetting it to null.
     */
    private void invalidateRobotIndex() {
        robotConfigIndex = null;
    }

}
