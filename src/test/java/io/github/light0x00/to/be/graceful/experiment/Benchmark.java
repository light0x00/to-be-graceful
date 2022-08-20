package io.github.light0x00.to.be.graceful.experiment;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author 陈光应
 * @date 2022/8/3
 */
@Slf4j
public class Benchmark {
    final String title;
    final LocalDateTime beginTime;
    final ThreadLocal<LocalDateTime> lastTimeMarkL = ThreadLocal.withInitial(LocalDateTime::now);

    public Benchmark(String title) {
        this.title = title;
        this.beginTime = LocalDateTime.now();
        lastTimeMarkL.set(this.beginTime);
    }

    public void mark() {
        mark(null);
    }

    public void flip() {
        lastTimeMarkL.set(LocalDateTime.now());
    }

    public void mark(String prefix) {
        LocalDateTime now = LocalDateTime.now();
        log0("{}阶段耗时: {},总耗时:{}",
                Optional.ofNullable(prefix).map(p -> p + ",").orElse(""),
                ChronoUnit.MILLIS.between(lastTimeMarkL.get(), now) + "ms",
                ChronoUnit.MILLIS.between(beginTime, now) + "ms"
        );
        lastTimeMarkL.set(now);
    }

    private void log0(String pattern, Object... args) {
        String prefix = Slf4jTemplateFormatter.format("[{}] [{}]", Thread.currentThread().getName(), title);
        log.info(prefix + " " + pattern, args);
    }

    public Runnable wrap(String prefix, Runnable runnable) {
        return () -> {
            flip();
            runnable.run();
            mark(prefix);
        };
    }

    public <T> Supplier<T> wrap(String prefix, Supplier<T> supplier) {
        return () -> {
            flip();
            T r = supplier.get();
            mark(prefix);
            return r;
        };
    }
}
