package io.github.light0x00.to.be.graceful.experiment;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author light
 * @date 2022/8/12
 */
public class CollectionBuilder<C extends Collection<T>, T> {

    private C coll;

    public CollectionBuilder(C coll) {
        this.coll = coll;
    }

    public static <C extends Collection<T>, T> CollectionBuilder<C, T> from(C coll) {
        return new CollectionBuilder<>(coll);
    }

    public CollectionBuilder<C, T> add(T ele) {
        coll.add(ele);
        return this;
    }

    public CollectionBuilder<C, T> addAll(T... e) {
        addAll(Arrays.asList(e));
        return this;
    }

    public CollectionBuilder<C, T> addAll(Collection<T> c) {
        this.coll.addAll(c);
        return this;
    }

    public C build() {
        return coll;
    }

}
