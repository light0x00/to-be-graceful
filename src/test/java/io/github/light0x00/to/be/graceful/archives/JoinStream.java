package io.github.light0x00.to.be.graceful.archives;

import io.github.light0x00.to.be.graceful.streamx.JoinType;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * @author light
 * @since 2022/8/14
 */
public interface JoinStream<T> {

    static <T> JoinStream<T> of(Collection<T> coll) {
        return new StreamOp<T, T>(null, coll) {

            @Override
            Consumer<T> invoke(Consumer<T> nextChain) {
                return nextChain;
            }
        };
    }

    <Joining, NextOutput, JoinKey> JoinStream<NextOutput> join(JoinType joinType,
                                                               Collection<Joining> joiningCollection,
                                                               Function<T, JoinKey> drivingKeyExtractor,
                                                               Function<Joining, JoinKey> joiningKeyExtractor,
                                                               BiFunction<T, Joining, NextOutput> eachToJoin);

    JoinStream<T> forEach(Consumer<T> consumer);

    JoinStream<T> filter(Predicate<T> predicate);

    <Output> JoinStream<Output> map(Function<T, Output> mapper);

    <C extends Collection<T>> C collect(Supplier<C> collector);

    @AllArgsConstructor
    abstract class StreamOp<Driving, Output> implements JoinStream<Output> {

        StreamOp<?, Driving> prevStage;

        Collection<?> collection;

        abstract Consumer<Driving> invoke(Consumer<Output> nextChain);

        public JoinStream<Output> forEach(Consumer<Output> consumer) {
            return new StreamOp<Output, Output>(this, collection) {
                @Override
                Consumer<Output> invoke(Consumer<Output> nextChain) {
                    return output -> {
                        consumer.accept(output);
                        nextChain.accept(output);
                    };
                }
            };
        }

        public <NextOutput> JoinStream<NextOutput> map(Function<Output, NextOutput> mapper) {
            return new StreamOp<Output, NextOutput>(this, collection) {

                @Override
                Consumer<Output> invoke(Consumer<NextOutput> nextChain) {
                    return (in) -> nextChain.accept(mapper.apply(in));
                }
            };
        }

        public JoinStream<Output> filter(Predicate<Output> predicate) {
            return new StreamOp<Output, Output>(this, collection) {
                @Override
                Consumer<Output> invoke(Consumer<Output> nextChain) {
                    return output -> {
                        if (predicate.test(output)) {
                            nextChain.accept(output);
                        }
                    };
                }
            };
        }

        public <Joining, NextOutput, JoinKey> JoinStream<NextOutput> join(JoinType joinType,
                                                                          Collection<Joining> joiningCollection,
                                                                          Function<Output, JoinKey> drivingKeyExtractor,
                                                                          Function<Joining, JoinKey> joiningKeyExtractor,
                                                                          BiFunction<Output, Joining, NextOutput> eachToJoin
        ) {
            return new StreamOp<Output, NextOutput>(this, collection) {
                Map<JoinKey, Joining> joiningMap;

                {
                    joiningMap = joiningCollection.stream().collect(
                            Collectors.toMap(joiningKeyExtractor, Function.identity(), (dup1, dup2) -> dup2, () -> new HashMap<>(joiningCollection.size())));
                }

                @Override
                Consumer<Output> invoke(Consumer<NextOutput> chain) {
                    return (driving) -> {
                        Joining joining = joiningMap.get(drivingKeyExtractor.apply(driving));
                        if (joining == null && joinType == JoinType.INNER_JOIN) {
                            return;
                        }
                        chain.accept(eachToJoin.apply(driving, joining));
                    };
                }
            };
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <C extends Collection<Output>> C collect(Supplier<C> collector) {
            C outputs = collector.get();

            Consumer chain = invoke(outputs::add);
            for (StreamOp last = this.prevStage; last != null; last = last.prevStage) {
                chain = last.invoke(chain);
            }

            for (Object driving : collection) {
                chain.accept(driving);
            }
            return outputs;
        }
    }

}