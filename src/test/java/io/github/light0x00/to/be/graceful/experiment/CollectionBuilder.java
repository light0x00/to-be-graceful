package io.github.light0x00.to.be.graceful.experiment;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * @author 陈光应
 * @date 2022/8/12
 */
public class CollectionBuilder<C extends Collection<T>, T> {

    private C coll;

    public CollectionBuilder(C coll) {
        this.coll = coll;
    }

    public static  <C extends Collection<T>, T> CollectionBuilder<C, T>  from(Supplier<C> collSupplier, T... t) {
        return new CollectionBuilder<>(collSupplier.get()).addVarargs(t);
    }

    public static <C extends Collection<T>, T> CollectionBuilder<C, T> fromCollection(C coll) {
        return new CollectionBuilder<>(coll);
    }

    public static <C extends Collection<T>, T> CollectionBuilder<C, T> from(Supplier<C> collSupplier) {
        return new CollectionBuilder<>(collSupplier.get());
    }

    public CollectionBuilder<C, T> add(T ele) {
        coll.add(ele);
        return this;
    }

    public CollectionBuilder<C, T> addVarargs(T... e) {
        Collection<T> ts = Arrays.asList(e);
        this.coll.addAll(ts);
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
