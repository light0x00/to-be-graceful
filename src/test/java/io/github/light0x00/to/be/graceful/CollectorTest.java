package io.github.light0x00.to.be.graceful;

import io.github.light0x00.to.be.graceful.model.MessageVO;
import io.github.light0x00.to.be.graceful.streamx.Collectors;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author light
 * @since 2022/9/18
 */
public class CollectorTest {

    List<MessageVO> list = new ArrayList<>();

    {
        list.add(MessageVO.builder().userName("何塞阿尔卡迪奥").content("地球是圆的就像个橙子").build());
        list.add(MessageVO.builder().userName("何塞阿尔卡迪奥").content("我们注定在这里活活烂掉，享受不到科学带来的好处").build());
        list.add(MessageVO.builder().userName("梅尔吉亚得斯").content("魔鬼已被证明具有硫化物的属性，而这只不过是一点氯化汞").build());
    }

    @Test
    public void testToList() {
        StreamX.of(Arrays.asList(1, 2, 3))
                .collect(Collectors.toList());
    }

    @Test
    public void testToMap() {
        Map<String, MessageVO> result = StreamX.of(list)
                .collect(Collectors.toMap(MessageVO::getUserName, Function.identity(), (d1, d2) -> d1));
    }

    @Test
    public void testGroupBy(){
        Map<String, List<MessageVO>> result = StreamX.of(list).collect(Collectors.groupBy(MessageVO::getUserName));
        System.out.println(result);
    }

}
