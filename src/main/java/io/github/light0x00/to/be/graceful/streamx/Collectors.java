package io.github.light0x00.to.be.graceful.streamx;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author light
 * @since 2022/9/18
 */
public class Collectors {

    @AllArgsConstructor
    @Getter
    public static class Collector<T, R> {
        public Consumer<T> accumulator;
        public Supplier<R> result;
    }

    public static <T> Collector<T, List<T>> toList() {
        List<T> list = new LinkedList<>();
        return new Collector<T, List<T>>(
                new Consumer<T>() {
                    @Override
                    public void accept(T o) {
                        list.add(o);
                    }
                },
                new Supplier<List<T>>() {
                    @Override
                    public List<T> get() {
                        return list;
                    }
                }
        );
    }

    public static <T, K, V> Collector<T, Map<K, V>> toMap(Function<T, K> keyMapper, Function<T, V> valMapper, BiFunction<V, V, V> mergeFunction) {
        Map<K, V> map = new HashMap<>();
        return new Collector<T, Map<K, V>>(
                new Consumer<T>() {
                    @Override
                    public void accept(T t) {
                        K key = keyMapper.apply(t);
                        V value = valMapper.apply(t);
                        V existingValue = map.putIfAbsent(key, value);
                        if (existingValue != null) {
                            map.put(key, mergeFunction.apply(value, existingValue));
                        }
                    }
                },
                new Supplier<Map<K, V>>() {
                    @Override
                    public Map<K, V> get() {
                        return map;
                    }
                }
        );
    }

    public static <T, K> Collector<T, Map<K, List<T>>> groupBy(Function<T, K> keyMapper) {
        Map<K, List<T>> map = new HashMap<>();
        return new Collector<T, Map<K, List<T>>>(
                new Consumer<T>() {
                    @Override
                    public void accept(T t) {
                        K key = keyMapper.apply(t);
                        List<T> list = map.computeIfAbsent(key, k -> new LinkedList<>());
                        list.add(t);
                    }
                },
                new Supplier<Map<K, List<T>>>() {
                    @Override
                    public Map<K, List<T>> get() {
                        return map;
                    }
                }
        );
    }

}
