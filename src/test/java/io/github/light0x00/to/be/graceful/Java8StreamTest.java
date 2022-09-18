package io.github.light0x00.to.be.graceful;

import org.junit.Test;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author light
 * @since 2022/9/18
 */
public class Java8StreamTest {

    @Test
    public void test() {
        Stream.of(1, 2, 3).collect(Collectors.toMap(Function.identity(), Function.identity(), (d1, d2) -> d2));
    }

}
