package io.jenkins.plugins.lark.notice.tools.function;

import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * A functional interface similar to {@link java.util.function.Consumer} that allows for checked exceptions.
 * This interface is designed for use in lambda expressions or method references where checked exceptions need to be handled.
 * It extends {@link Serializable} to allow instances to be serialized if necessary.
 * <p>
 * The primary use case for this interface is when you want to perform an operation that might throw a checked exception,
 * but you are working in a context (such as streams) where checked exceptions are not allowed. By using this interface,
 * you can wrap such operations, and then handle exceptions explicitly outside of the lambda expression or method reference.
 *
 * @param <T> the type of the input to the operation
 * @author xm.z
 */
@FunctionalInterface
public interface CheckedConsumer<T> extends Serializable {

    /**
     * Performs this operation on the given argument, allowing checked exceptions to be thrown.
     * <p>
     * Unlike {@link java.util.function.Consumer}, this method is declared to throw {@link Throwable},
     * which means it can throw both checked and unchecked exceptions. This provides greater flexibility
     * for methods that might need to throw exceptions during their execution and cannot be used directly
     * in standard functional interfaces due to the Java language's checked exception rules.
     * <p>
     * Implementors are expected to specify more concrete exception types in their implementations if possible,
     * rather than the broad {@link Throwable} type, to provide clearer documentation and enable better exception handling.
     *
     * @param t the input argument. It can be {@code null} if the implementation supports it.
     * @throws Throwable if an error occurs during the operation. The type of exceptions that can be thrown
     *                   depends on the implementation.
     */
    void accept(@Nullable T t) throws Throwable;

}