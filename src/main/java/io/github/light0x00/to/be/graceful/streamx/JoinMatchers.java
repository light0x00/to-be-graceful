package io.github.light0x00.to.be.graceful.streamx;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author light
 * @since 2022/8/19
 */
public class JoinMatchers {

    /**
     * 以全表扫描的方式寻找连接表的记录
     * <p>
     * 查找成本:
     * 时间: O(N*N)
     * 空间: O(1)
     */
    public static JoinMatcherWrapper byScan() {
        return new JoinMatcherWrapper() {
            @Override
            public <Driving, Joining, Key> Function<Driving, List<Joining>> create(
                    Collection<Joining> joiningCollection,
                    Function<Driving, Key> drivingKeyExtractor,
                    Function<Joining, Key> joiningKeyExtractor
            ) {
                return driving ->
                        joiningCollection.stream()
                                .filter(joining -> Objects.equals(joiningKeyExtractor.apply(joining), drivingKeyExtractor.apply(driving)))
                                .collect(Collectors.toList());
            }
        };
    }

    public static JoinMatcherWrapper byScanFirst() {
        return new JoinMatcherWrapper() {
            @Override
            public <Driving, Joining, Key> Function<Driving, List<Joining>> create(
                    Collection<Joining> joiningCollection,
                    Function<Driving, Key> drivingKeyExtractor,
                    Function<Joining, Key> joiningKeyExtractor
            ) {
                return driving ->
                        joiningCollection.stream()
                                .filter(joining -> Objects.equals(joiningKeyExtractor.apply(joining), drivingKeyExtractor.apply(driving)))
                                .findFirst().map(Collections::singletonList).orElse(Collections.emptyList());
            }
        };
    }

    /**
     * 采用建立 Map 索引的方式寻找连接表的记录
     * <p>
     * 查找成本:
     * 时间: O(1)
     * <p>
     * 建立 Map 索引的成本：
     * 空间: O(N)
     * 时间: O(N)
     */
    public static JoinMatcherWrapper byMap() {
        return new JoinMatcherWrapper() {
            @Override
            public <Driving, Joining, JoinKey> Function<Driving, List<Joining>> create(
                    Collection<Joining> joiningCollection,
                    Function<Driving, JoinKey> drivingKeyExtractor,
                    Function<Joining, JoinKey> joiningKeyExtractor
            ) {
                final Map<JoinKey, List<Joining>> joiningMap = joiningCollection.stream().collect(Collectors.groupingBy(joiningKeyExtractor));
                return new Function<Driving, List<Joining>>() {

                    /**
                     * 接收驱动表的一条记录，返回连接表的关联记录
                     * @param driving 驱动表记录
                     * @return 关联表记录
                     */
                    @Override
                    public List<Joining> apply(Driving driving) {
                        return Optional.ofNullable(joiningMap.get(drivingKeyExtractor.apply(driving))).orElse(Collections.emptyList());
                    }
                };
            }
        };
    }
}
