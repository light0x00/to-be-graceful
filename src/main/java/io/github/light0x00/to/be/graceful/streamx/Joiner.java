package io.github.light0x00.to.be.graceful.streamx;

import java.util.List;

/**
 * 对于驱动表的每一条记录，在连接表中的寻找关联记录的策略
 *
 * @author light
 * @since 2022/8/19
 */
public interface Joiner<Driving, Merging> {
    List<Merging> doJoin(Driving driving);
}
