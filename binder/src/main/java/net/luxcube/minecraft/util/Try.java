package net.luxcube.minecraft.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A Try is a wrapper around a function that may throw an exception.
 *
 * <p>It is similar to the Try monad in Haskell. It is used to avoid the boilerplate code of
 * handling exceptions. It is also used to avoid the boilerplate code of handling exceptions in a
 * functional way.
 *
 * @param <A>
 * @author Gabrielle GuimarÃ£es de Oliveira
 * @since 21/05/22
 */
public interface Try<A> {
    /**
     * Returns a new [Try<B>] with the result of applying the given function to the successful
     * result of unwrapped [A].
     *
     * @param f the function to apply to the successful result of unwrapped [A]
     * @return a new [Try<B>] with the result of applying the given function to the successful
     *     result
     * @param <B> the type of the result of the function
     */
    @NotNull
    <B> Try<B> flatMap(@NotNull final Function<A, @NotNull Try<B>> f);

    /**
     * Returns a new [Try<A>] with the result of applying the given function to the successful
     * result, if throws an error, returns a new [Try<A>] with the error.
     *
     * @param kind the kind of error to catch
     * @param f the function to apply to consume the error
     * @return a new [Try<A>] with the result of applying the given function to the successful
     *     result
     * @param <E> the type of the error
     */
    @NotNull
    <E extends Exception> Try<A> catching(
            @NotNull final Class<E> kind, @NotNull final Consumer<E> f);

    /**
     * Unwraps this [Try].
     *
     * @return the successful result of unwrapped [A]
     */
    @NotNull
    A unwrap();

    /**
     * Returns a new [Try<B>] with the result of applying the given function to the successful
     * result.
     *
     * @param f the function to apply to the successful result of unwrapped [A]
     * @return a new [Try<B>] with the result of applying the given function to the successful
     *     result
     * @param <B> the type of the result of the function
     */
    @NotNull
    default <B> Try<B> map(@NotNull final Function<A, B> f) {
        return flatMap(a -> Try.of(f.apply(a)));
    }

    /**
     * Returns a new [Try<A>] running a consumer through the successful result.
     *
     * @param f the consumer to apply to the successful result of unwrapped [A]
     * @return a new [Try<A>] running a consumer through the successful result
     */
    @NotNull
    default Try<A> onEach(@NotNull final Consumer<A> f) {
        return map(
                a -> {
                    f.accept(a);
                    return a;
                });
    }

    /**
     * Returns a new [Try<A>] catching exceptions from [supplier].
     *
     * @param supplier the supplier to get the value of [Try<A>]
     * @return a new [Try<A>] catching exceptions from [supplier]
     * @param <A> the type of the value of [Try<A>]
     */
    @Contract("_ -> new")
    @NotNull
    static <A> Try<A> catching(@NotNull final Supplier<A> supplier) {
        try {
            return new Success<>(supplier.get());
        } catch (Exception e) {
            return new Fail<>(e);
        }
    }

    /**
     * Returns a new [Try<A>] catching exceptions from [runnable].
     *
     * @param runnable the supplier to catch exceptions
     * @return a new [Try<A>] catching exceptions from [runnable]
     */
    @Contract("_ -> new")
    @NotNull
    static Try<Void> catchingVoid(@NotNull final Runnable runnable) {
        return Try.catching(
                () -> {
                    runnable.run();

                    return null;
                });
    }

    /**
     * Returns a new successful [Try<A>].
     *
     * @param a the value of the successful [Try<A>]
     * @return a new successful [Try<A>]
     * @param <A> the type of the value of the successful [Try<A>]
     */
    @Contract("_ -> new")
    @NotNull
    static <A> Try<A> of(final A a) {
        return new Success<>(a);
    }

    /**
     * Returns a new failed [Try<A>].
     *
     * @param e the exception from failed [Try<A>]
     * @return a new failed [Try<A>]
     * @param <A> the type of the value of [Try<A>]
     * @param <E> the type of the exception from failed [Try<A>]
     */
    @Contract("_ -> new")
    @NotNull
    static <A, E extends Exception> Try<A> fail(final E e) {
        return new Fail<>(e);
    }
}

record Success<A>(A a) implements Try<A> {
    @Contract(value = "_, _ -> this", pure = true)
    @Override
    @NotNull
    public <E extends Exception> Try<A> catching(
            @NotNull final Class<E> kind, @NotNull final Consumer<E> f) {
        return this;
    }

    @Override
    @NotNull
    public <B> Try<B> flatMap(@NotNull final Function<A, @NotNull Try<B>> f) {
        return f.apply(a);
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public A unwrap() {
        return a;
    }
}

record Fail<A>(@NotNull Exception exception) implements Try<A> {
    @Override
    @NotNull
    public <E extends Exception> Try<A> catching(
            @NotNull final Class<E> kind, @NotNull final Consumer<E> f) {
        if (!kind.isInstance(exception)) return this;

        try {
            f.accept(kind.cast(exception));
        } catch (Exception e) {
            return new Fail<>(e);
        }

        return this;
    }

    @Contract(value = "_ -> this", pure = true)
    @SuppressWarnings("unchecked")
    @Override
    @NotNull
    public <B> Try<B> flatMap(@NotNull final Function<A, @NotNull Try<B>> f) {
        return (Fail<B>) this;
    }

    @Contract(value = " -> fail", pure = true)
    @Override
    @NotNull
    public A unwrap() {
        throw exception instanceof RuntimeException exception
                ? exception
                : new RuntimeException(exception);
    }
}
