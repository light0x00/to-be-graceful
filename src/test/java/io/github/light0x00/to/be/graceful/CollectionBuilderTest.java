package io.github.light0x00.to.be.graceful;

import io.github.light0x00.to.be.graceful.experiment.CollectionBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author light
 * @date 2022/8/12
 */
public class CollectionBuilderTest {

    @Test
    public void test() {
        List<Integer> list = CollectionBuilder.fromCollection(new ArrayList<Integer>())
                .addVarargs(1, 2, 3)
                .add(4)
                .build();

        assertThat(list, is(Arrays.asList(1, 2, 3, 4)));
    }

}
