package io.github.light0x00.to.be.graceful;

import io.github.light0x00.to.be.graceful.experiment.CollectionBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author light
 * @date 2022/8/12
 */
public class CollectionBuilderTest {

    @Test
    public void test() {
        List<Integer> list = CollectionBuilder.from(new ArrayList<Integer>())
                .addAll(1, 2, 3)
                .add(4)
                .build();

        assertThat(list, hasItems(1, 2, 3, 4));
    }

}
