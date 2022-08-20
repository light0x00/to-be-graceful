package io.github.light0x00.to.be.graceful.archives;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author light
 * @since 2022/8/14
 */
public class PointerTest {

    @Test
    public void test() {
        List<Runnable> list = new ArrayList<>();
        for (int[] i = {0}; i[0] < 10; i[0]++) {
            int[] j = i;
            list.add(() -> {
                System.out.println(i[0]);
                System.out.println(j.hashCode() + ":" + j[0]);
            });
        }
        list.forEach(Runnable::run);
    }

    @Test
    public void test2() {
        List<Runnable> list = new ArrayList<>();
        for (Integer i = 0; i < 10; i++) {
            Integer j = i;
            list.add(() -> {
                System.out.println(j);
            });
        }
        list.forEach(Runnable::run);
    }

    @Test
    public void test3(){
        Object o = new Object();
        Object o2 = o;
        System.out.println(o.hashCode()+","+o2.hashCode());
    }

}
