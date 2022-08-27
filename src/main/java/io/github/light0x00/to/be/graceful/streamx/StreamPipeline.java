package io.github.light0x00.to.be.graceful.streamx;

import io.github.light0x00.to.be.graceful.StreamX;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author light
 * @since 2022/8/27
 */
@AllArgsConstructor
public abstract class StreamPipeline<Input, Output> implements StreamX<Output> {

    private StreamPipeline<?, Input> prevStage;

    private Collection<?> collection;

    public abstract void invoke(Input in, Chain<Output> chain);

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
            public void invoke(Output driving, Chain<NextOutput> chain) {
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
            public void invoke(Output out, Chain<Output> chain) {
                chain.next(out);
            }
        };
    }

    @Override
    public <NextOut> StreamX<NextOut> map(Function<Output, NextOut> mapper) {
        return new StreamPipeline<Output, NextOut>(this, collection) {
            @Override
            public void invoke(Output in, Chain<NextOut> chain) {
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
            public void invoke(Output out, Chain<Output> chain) {
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
