package io.github.light0x00.to.be.graceful.streamx;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author light
 * @since 2022/8/19
 */
public class Joiners {

    /**
     * 以全表扫描的方式寻找连接表的记录
     * 时间: O(N)
     * 空间: O(1)
     */
    public static JoinerSupplier byScan() {
        return new JoinerSupplier() {
            @Override
            public <Driving, Joining, Key> Joiner<Driving, Joining> create(Collection<Joining> joiningCollection,
                                                                           Function<Driving, Key> drivingKeyExtractor,
                                                                           Function<Joining, Key> joiningKeyExtractor) {
                return new Joiner<Driving, Joining>() {
                    @Override
                    public Optional<Joining> doJoin(Driving driving) {
                        return joiningCollection.stream().filter(joining -> Objects.equals(joiningKeyExtractor.apply(joining), drivingKeyExtractor.apply(driving))).findAny();
                    }
                };
            }
        };
    }

    /**
     * 采用建立 Map 索引的方式寻找连接表的记录
     * 时间: O(1)
     * 空间: O(N)
     */
    public static JoinerSupplier byMap() {
        return new JoinerSupplier() {
            @Override
            public <Driving, Joining, JoinKey> Joiner<Driving, Joining> create(Collection<Joining> joiningCollection, Function<Driving, JoinKey> drivingKeyExtractor, Function<Joining, JoinKey> joiningKeyExtractor) {
                return new Joiner<Driving, Joining>() {
                    final Map<JoinKey, Joining> joiningMap;

                    {
                        joiningMap = joiningCollection.stream().collect(
                                Collectors.toMap(joiningKeyExtractor, Function.identity(), (dup1, dup2) -> dup2, () -> new HashMap<>(joiningCollection.size())));
                    }

                    /**
                     * 接收驱动表的一条记录，返回连接表的关联记录
                     * @param driving 驱动表记录
                     * @return 关联表记录
                     */
                    @Override
                    public Optional<Joining> doJoin(Driving driving) {
                        return Optional.ofNullable(joiningMap.get(drivingKeyExtractor.apply(driving)));
                    }
                };
            }
        };
    }
}
