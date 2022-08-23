package io.github.light0x00.to.be.graceful;

import io.github.light0x00.to.be.graceful.streamx.JoinType;
import io.github.light0x00.to.be.graceful.streamx.Joiner;
import io.github.light0x00.to.be.graceful.streamx.JoinerSupplier;
import io.github.light0x00.to.be.graceful.streamx.Joiners;
import lombok.AllArgsConstructor;

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
            void invoke(In in, Chain<In> chain) {
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

    interface Chain<T> {
        void next(T in);
    }

    @AllArgsConstructor
    abstract class StreamPipeline<Input, Output> implements StreamX<Output> {

        private StreamPipeline<?, Input> prevStage;

        private Collection<?> collection;

        abstract void invoke(Input in, Chain<Output> chain);

        @Override
        public <Joining, NextOutput, JoinKey> StreamX<NextOutput> join(JoinType joinType,
                                                                       Collection<Joining> joiningCollection,
                                                                       Function<Output, JoinKey> drivingKeyExtractor,
                                                                       Function<Joining, JoinKey> joiningKeyExtractor,
                                                                       JoinerSupplier joinerSupplier,
                                                                       BiFunction<Output, Joining, NextOutput> eachToJoin) {
            return new StreamPipeline<Output, NextOutput>(this, collection) {
                final Joiner<Output, Joining> joiner;

                {
                    joiner = joinerSupplier.create(joiningCollection, drivingKeyExtractor, joiningKeyExtractor);
                }

                @Override
                void invoke(Output driving, Chain<NextOutput> chain) {
                    Optional<Joining> joining = joiner.doJoin(driving);
                    if (joining.isPresent()) {
                        chain.next(eachToJoin.apply(driving, joining.get()));
                    } else if (joinType == JoinType.LEFT_JOIN) {
                        chain.next(eachToJoin.apply(driving, null));
                    }
                }
            };
        }

        @Override
        public StreamX<Output> consume(Consumer<Output> consumer) {
            return new StreamPipeline<Output, Output>(this, collection) {
                @Override
                void invoke(Output out, Chain<Output> chain) {
                    chain.next(out);
                }
            };
        }

        @Override
        public <NextOut> StreamX<NextOut> map(Function<Output, NextOut> mapper) {
            return new StreamPipeline<Output, NextOut>(this, collection) {
                @Override
                void invoke(Output in, Chain<NextOut> chain) {
                    chain.next(mapper.apply(in));
                }
            };
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Optional<Output> reduce(BiFunction<Output, Output, Output> accumulator) {
            List<Output> c = new ArrayList<>(2);
            Chain chain = buildChain((Chain<Output>) c::add);

            for (Object o : collection) {
                chain.next(o);
                if (c.size() > 1) {
                    Output acc = accumulator.apply(c.get(0), c.get(1));
                    c.clear();
                    c.add(acc);
                }
            }
            if (c.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.ofNullable(c.get(0));
            }
        }

        @Override
        public StreamX<Output> filter(Predicate<Output> predicate) {
            return new StreamPipeline<Output, Output>(this, collection) {
                @Override
                void invoke(Output out, Chain<Output> chain) {
                    if (predicate.test(out)) {
                        chain.next(out);
                    }
                }
            };
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Optional<Output> findFirst() {
            List<Output> c = new ArrayList<>(1);
            Chain chain = buildChain((Chain<Output>) c::add);

            for (Object o : collection) {
                chain.next(o);
                if (c.size() > 0) {
                    return Optional.ofNullable(c.get(0));
                }
            }
            return Optional.empty();
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public void forEach(Consumer<Output> collector) {
            Chain chain = buildChain((Chain<Output>) collector::accept);
            for (Object o : collection) {
                chain.next(o);
            }
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private <C extends Collection<Output>> Chain buildChain(Chain chain) {
            for (StreamPipeline lastOp = this; lastOp != null; lastOp = lastOp.prevStage) {
                StreamPipeline curOp = lastOp;
                Chain curChain = chain;
                chain = in -> curOp.invoke(in, curChain);
            }
            return chain;
        }
    }
}
