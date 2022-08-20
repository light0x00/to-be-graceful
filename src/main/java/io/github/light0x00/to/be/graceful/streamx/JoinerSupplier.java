package io.github.light0x00.to.be.graceful.streamx;

import java.util.Collection;
import java.util.function.Function;

/**
 * 用于延迟 {@linkplain Joiner } 的创建时机
 * @author light
 * @since 2022/8/19
 */
public interface JoinerSupplier {
    <Driving, Joining, Key> Joiner<Driving, Joining> create(Collection<Joining> joiningCollection,
                                                                 Function<Driving, Key> drivingKeyExtractor,
                                                                 Function<Joining, Key> joiningKeyExtractor);
}
