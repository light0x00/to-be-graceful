package io.github.light0x00.to.be.graceful.streamx;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 用于延迟 {@linkplain Joiner } 的创建时机
 *
 * @author light
 * @since 2022/8/19
 */
public interface JoinMatcherWrapper {
    <Driving, Joining, Key> Function<Driving, List<Joining>> create(
            Collection<Joining> joiningCollection,
            Function<Driving, Key> drivingKeyExtractor,
            Function<Joining, Key> joiningKeyExtractor
    );
}
