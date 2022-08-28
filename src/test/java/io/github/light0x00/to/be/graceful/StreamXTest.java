package io.github.light0x00.to.be.graceful;

import io.github.light0x00.to.be.graceful.experiment.CollectionBuilder;
import io.github.light0x00.to.be.graceful.streamx.JoinType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author light
 * @since 2022/8/14
 */
public class StreamXTest {

    @AllArgsConstructor
    @Data
    static class Message {
        private Integer groupId;
        private Integer userId;
        private String content;
    }

    @AllArgsConstructor
    @Data
    static class Group {
        private Integer groupId;
        private String groupName;
    }

    @AllArgsConstructor
    @Data
    static class User {
        private Integer userId;
        private String userName;
    }

    @Data
    static class MessageVO {
        private Integer userId;
        private String userName;
        private String groupName;
        private String content;
    }

    List<Message> messages = Arrays.asList(
            new Message(1, 1, "Alice: msg1 in group1"),
            new Message(1, 2, "Bob: msg2 in group1"),

            new Message(2, 2, "Bob: msg3 in group2"),
            new Message(2, 3, "Cindy: msg4 in group2"),

            new Message(3, 1, "Alice: msg5 in group3"),
            new Message(3, 2, "Bob: msg6 in group3"),
            new Message(3, 3, "Cindy: msg7 in group3 ")
    );

    List<Group> groups = Arrays.asList(
            new Group(1, "group1"),
            new Group(2, "group2"),
            new Group(3, "group3")
    );

    List<User> users = Arrays.asList(
            new User(1, "Alice"),
            new User(2, "Bob"),
            new User(3, "Cindy")
    );

    @Test
    public void testJoin0() {
List<MessageVO> result = StreamX.of(messages)
        .flapJoin(JoinType.INNER_JOIN, groups, Message::getGroupId, Group::getGroupId,
                (msg, group) -> {
                    MessageVO out = new MessageVO();
                    out.setContent(msg.getContent());
                    out.setGroupName(group.getGroupName());
                    out.setUserId(msg.getUserId());
                    return out;
                })
        .flapJoinAsItself(JoinType.INNER_JOIN, users, MessageVO::getUserId, User::getUserId,
                (msg, usr) -> {
                    msg.setUserName(usr.getUserName());
                })
        .collect(ArrayList::new);
        result.forEach(System.out::println);
    }


    @Test
    public void testJoin() {
        List<Integer> drivingCollection = Arrays.asList(1, 2, 3, 4);
        List<Integer> joiningCollection = Arrays.asList(1, 1, 2, 1, 2, 3);
        List<List<Integer>> collect = StreamX.of(drivingCollection)
                .join(JoinType.LEFT_JOIN, joiningCollection, Function.identity(), Function.identity(),
                        (d, joiningList) -> {
                            return CollectionBuilder.fromCollection(new ArrayList<Integer>())
                                    .add(d)
                                    .addAll(Optional.ofNullable(joiningList).orElse(Collections.emptyList()))
                                    .build();
                        })
                .collect(ArrayList::new);

        assertThat(collect, is(Arrays.asList(
                Arrays.asList(1, 1, 1, 1),
                Arrays.asList(2, 2, 2),
                Arrays.asList(3, 3),
                Arrays.asList(4))));
    }

    static class A {
        int id;
        int fk_id;
    }

    static class B {
        int id;
    }

    @Test
    public void testJoin2() {
        List<Integer> drivingCollection = Arrays.asList(1, 2, 3, 4);
        List<Integer> joiningCollection = Arrays.asList(1, 1, 2, 1, 2, 3);
        List<List<Integer>> result = StreamX.of(drivingCollection)
                .join(JoinType.INNER_JOIN, joiningCollection, Function.identity(), Function.identity(),
                        (driving, joiningList) -> {
                            System.out.println("驱动表记录:" + driving + ",连接到的记录:" + joiningList);
                            ArrayList<Integer> merge = new ArrayList<>();
                            merge.add(driving);
                            merge.addAll(joiningList);
                            return merge;
                        })
                .collect(ArrayList::new);
        System.out.println(result);
    }

    @Test
    public void testFlapJoin() {
        List<Integer> drivingList = Arrays.asList(1, 2, 3, 4);
        List<Integer> joiningList = Arrays.asList(1, 1, 2, 1, 2, 3);
        List<List<Integer>> collect = StreamX.of(drivingList)
                .flapJoin(JoinType.LEFT_JOIN, joiningList, Function.identity(), Function.identity(), Arrays::asList)
                .collect(ArrayList::new);
        System.out.println(collect);
        assertThat(collect, is(Arrays.asList(
                Arrays.asList(1, 1),
                Arrays.asList(1, 1),
                Arrays.asList(1, 1),
                Arrays.asList(2, 2),
                Arrays.asList(2, 2),
                Arrays.asList(3, 3),
                Arrays.asList(4, null)
        )));
    }

    @Test
    public void testJoinFirst() {
        List<Integer> drivingList = Arrays.asList(1, 2, 3, 4);
        List<Integer> joiningList = Arrays.asList(1, 1, 2, 1, 2, 3);
        List<List<Integer>> collect = StreamX.of(drivingList)
                .joinFirst(JoinType.LEFT_JOIN, joiningList, Function.identity(), Function.identity(), Arrays::asList)
                .collect(ArrayList::new);
        assertThat(collect, is(Arrays.asList(
                Arrays.asList(1, 1),
                Arrays.asList(2, 2),
                Arrays.asList(3, 3),
                Arrays.asList(4, null)
        )));
    }

    @Test
    public void testInnerJoin() {
        List<Integer> drivingList = Arrays.asList(1, 3, 5, 7);
        List<Integer> joiningList = Arrays.asList(1, 2, 5);

        ArrayList<Integer> result = StreamX.of(drivingList)
                .flapJoin(JoinType.INNER_JOIN, joiningList, Function.identity(), Function.identity(),
                        Integer::sum)
                .collect(ArrayList::new);
        assertThat(result, hasItems(2, 10));
    }

    @Test
    public void testLeftJoin() {
        List<Integer> drivingList = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> joiningList = Arrays.asList(1, 3, 4, 5, 6);

        List<Integer> result = StreamX.of(drivingList)
                .flapJoin(JoinType.LEFT_JOIN, joiningList, Function.identity(), Function.identity(),
                        (a, b) ->
                                Integer.sum(Optional.ofNullable(a).orElse(0), Optional.ofNullable(b).orElse(0))
                )
                .collect(ArrayList::new);
        assertThat(result, hasItems(2, 2, 6, 8, 10));
    }

    @Test
    public void testMapReduce() {
        Optional<Integer> result = StreamX.of(Arrays.asList(1, -2, 3, -4, 5, -6))
                .map(Math::abs)
                .filter(i -> i > 2 && i < 5)
                .reduce(Integer::sum);

        assertTrue(result.isPresent());
        assertTrue(result.get() == 7);
    }

    @Test
    public void testMapReduce2() {
        Optional<Integer> result = StreamX.of(Arrays.asList(1, -2, 3))
                .map(Math::abs)
                .reduce(Integer::sum);

        assertTrue(result.isPresent());
        assertTrue(result.get() == 6);
    }

    @Test
    public void testMapReduce3() {
        Optional<Integer> result = StreamX.of(Arrays.asList(1, 2, 3))
                .map(Math::abs)
                .filter(a -> a > 3)
                .reduce(Integer::sum);

        assertTrue(!result.isPresent());
    }

    @Test
    public void testFindFirst() {
        Optional<Integer> first = StreamX.of(Arrays.asList(1, 3, 5))
                .filter(a -> a > 2)
                .findFirst();

        assertTrue(first.isPresent());
        assertTrue(first.get() == 3);
    }

    @Test
    public void testForEach() {
        List<Integer> drivingList = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> joiningList = Arrays.asList(1, 3, 4, 5, 6);

        ArrayList<Integer> result = new ArrayList<>();

        StreamX.of(drivingList)
                .flapJoin(JoinType.LEFT_JOIN, joiningList, Function.identity(), Function.identity(),
                        (a, b) ->
                                Integer.sum(Optional.ofNullable(a).orElse(0), Optional.ofNullable(b).orElse(0))
                )
                .forEach(result::add);
        assertThat(result, hasItems(2, 2, 6, 8, 10));
    }

    @Test
    public void testWithoutCollect() {
        List<Integer> drivingList = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> joiningList = Arrays.asList(1, 3, 4, 5, 6);
        List<Integer> joiningList2 = Arrays.asList(1, 4, 6);

        ArrayList<Integer> result1 = new ArrayList<>();
        ArrayList<Integer> result2 = new ArrayList<>();

        StreamX.of(drivingList)
                .joinAsItself(JoinType.INNER_JOIN, joiningList, Function.identity(), Function.identity(),
                        (a, b) -> result1.add(a)
                )
                .joinAsItself(JoinType.INNER_JOIN, joiningList2, Function.identity(), Function.identity(),
                        (a, b) -> result2.add(a)
                )
                .withoutCollect();
        assertThat(result1, hasItems(1, 3, 4, 5));
        assertThat(result2, hasItems(1, 4));
    }

    public void exampleThatNotGraceful() {
        List<MessageVO> result = new LinkedList<>();
        //遍历消息列表
        for (Message msg : messages) {

            MessageVO msgVO = new MessageVO();
            msgVO.setContent(msg.getContent());
            msgVO.setUserId(msg.getUserId());

            //合并群组信息
            Group group = groups.stream()
                    .filter(g -> Objects.equals(g.getGroupId(), msg.getGroupId()))
                    .findAny().get();
            msgVO.setGroupName(group.getGroupName());

            //合并用户信息
            User user = users.stream()
                    .filter(usr -> Objects.equals(usr.getUserId(), msg.getUserId()))
                    .findAny().orElse(null);
            msgVO.setUserName(user.getUserName());

            //将合并后的结果放入结果集
            result.add(msgVO);
        }
        result.forEach(System.out::println);
    }

}
