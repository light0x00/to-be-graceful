package io.github.light0x00.to.be.graceful.streamx;

/**
 * @author light
 * @since 2022/8/27
 */
public interface Chain<T> {
    void next(T in);
}
