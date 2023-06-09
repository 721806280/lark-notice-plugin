package io.jenkins.plugins.feishu.notification.context;

import hudson.EnvVars;

/**
 * PipelineEnvContext 是一个线程安全的环境变量上下文类，用于在 Jenkins 流水线中管理环境变量。
 *
 * <p>该类包含了以下三个静态方法：</p>
 * <ul>
 *     <li>{@link #merge(EnvVars)} - 将传入的 {@link EnvVars} 对象合并到当前线程的环境变量中</li>
 *     <li>{@link #get()} - 获取当前线程的环境变量</li>
 *     <li>{@link #reset()} - 删除当前线程的环境变量</li>
 * </ul>
 *
 * <p>{@link #merge(EnvVars)} 方法将传入的 {@link EnvVars} 对象合并到当前线程的环境变量中，
 * 如果当前线程还没有关联的环境变量，则会初始化一个新的 {@link EnvVars} 对象并存储。</p>
 *
 * <p>{@link #get()} 方法获取当前线程关联的 {@link EnvVars} 对象，
 * 如果当前线程没有关联的 {@link EnvVars} 对象，则创建一个新的空对象并返回。</p>
 *
 * <p>{@link #reset()} 方法删除当前线程关联的 {@link EnvVars} 对象。</p>
 *
 * <p>由于使用了 {@link ThreadLocal} 来存储当前线程的环境变量对象，所以 {@code PipelineEnvContext}
 * 是线程安全的。</p>
 *
 * @author xm.z
 */
public class PipelineEnvContext {

    private static final ThreadLocal<EnvVars> STORE = new ThreadLocal<>();

    /**
     * 将传入的 {@link EnvVars} 对象合并到当前线程的环境变量中。
     *
     * <p>如果传入的 {@link EnvVars} 为 null，本方法不会进行任何操作。</p>
     *
     * @param value 要合并的 {@link EnvVars} 对象
     */
    public static void merge(EnvVars value) {
        if (value == null) {
            return;
        }
        EnvVars current = STORE.get();
        if (current == null) {
            STORE.set(value);
        } else {
            current.overrideAll(value);
        }
    }

    /**
     * 获取当前线程的环境变量。
     *
     * <p>如果当前线程没有关联的 {@link EnvVars} 对象，则创建一个新的空对象并返回。</p>
     *
     * @return 当前线程的环境变量对象
     */
    public static EnvVars get() {
        EnvVars current = STORE.get();
        return current == null ? new EnvVars() : current;
    }

    /**
     * 删除当前线程的环境变量。
     */
    public static void reset() {
        STORE.remove();
    }
}