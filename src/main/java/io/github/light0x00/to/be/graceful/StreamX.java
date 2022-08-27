package io.github.light0x00.to.be.graceful;

import io.github.light0x00.to.be.graceful.streamx.*;

import java.util.*;
import java.util.function.*;

/**
 * @author light
 * @since 2022/8/14
 */
public interface StreamX<T> {

    static <In> StreamX<In> of(Collection<In> coll) {
        return new StreamPipeline<In, In>(null, coll) {
            @Override
            public void invoke(In in, Chain<In> chain) {
                chain.next(in);
            }
        };
    }

    /**
     * 适用场景: A join B 返回 C
     *
     * @param joinType            左连接 or 内连接
     * @param joiningCollection   要连接的集合
     * @param drivingKeyExtractor 如何获取`驱动集合的连接键值`
     * @param joiningKeyExtractor 如何获取`被连接集合的连接键值`
     * @param joinerSupplier      连接算法,见 {@link Joiners}
     * @param eachToJoin          驱动集合的每一条记录发生连接时执行,输入: 驱动集合元素和连接集合元素,输出: 任意类型
     * @param <Joining>           连接集合的元素类型
     * @param <NextOutput>        连接后返回的类型
     * @param <JoinKey>           连接键类型
     */
    <Joining, NextOutput, JoinKey> StreamX<NextOutput> join(JoinType joinType,
                                                            Collection<Joining> joiningCollection,
                                                            Function<T, JoinKey> drivingKeyExtractor,
                                                            Function<Joining, JoinKey> joiningKeyExtractor,
                                                            JoinerSupplier joinerSupplier,
                                                            BiFunction<T, Joining, NextOutput> eachToJoin);

    /**
     * @see #join(JoinType, Collection, Function, Function, JoinerSupplier, BiFunction)
     */
    default <Joining, NextOutput, JoinKey> StreamX<NextOutput> join(JoinType joinType,
                                                                    Collection<Joining> joiningCollection,
                                                                    Function<T, JoinKey> drivingKeyExtractor,
                                                                    Function<Joining, JoinKey> joiningKeyExtractor,
                                                                    BiFunction<T, Joining, NextOutput> eachToJoin) {
        return join(joinType, joiningCollection, drivingKeyExtractor, joiningKeyExtractor, Joiners.byScan(), eachToJoin);
    }

    /**
     * 适用场景： A join B 返回 A
     */
    default <Joining, JoinKey> StreamX<T> joinAsItself(
            JoinType joinType,
            Collection<Joining> joiningCollection,
            Function<T, JoinKey> drivingKeyExtractor,
            Function<Joining, JoinKey> joiningKeyExtractor,
            JoinerSupplier joinerSupplier,
            BiConsumer<T, Joining> eachToJoin
    ) {
        return join(joinType, joiningCollection, drivingKeyExtractor, joiningKeyExtractor, joinerSupplier, (d, j) -> {
            eachToJoin.accept(d, j);
            return d;
        });
    }

    /**
     * @see #joinAsItself(JoinType, Collection, Function, Function, JoinerSupplier, BiConsumer)
     */
    default <Joining, JoinKey> StreamX<T> joinAsItself(
            JoinType joinType,
            Collection<Joining> joiningCollection,
            Function<T, JoinKey> drivingKeyExtractor,
            Function<Joining, JoinKey> joiningKeyExtractor,
            BiConsumer<T, Joining> eachToJoin
    ) {
        return joinAsItself(joinType, joiningCollection, drivingKeyExtractor, joiningKeyExtractor, Joiners.byScan(), eachToJoin);
    }

    StreamX<T> consume(Consumer<T> consumer);

    <M> StreamX<M> map(Function<T, M> mapper);

    Optional<T> reduce(BiFunction<T, T, T> reducer);

    StreamX<T> filter(Predicate<T> predicate);

    Optional<T> findFirst();

    void forEach(Consumer<T> consumer);

    default <C extends Collection<T>> C collect(Supplier<C> cSupplier) {
        C outs = cSupplier.get();
        forEach(outs::add);
        return outs;
    }

    default void withoutCollect() {
        forEach((o) -> {
        });
    }

}
