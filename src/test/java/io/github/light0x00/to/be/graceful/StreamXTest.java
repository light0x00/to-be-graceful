package io.github.light0x00.to.be.graceful;

import io.github.light0x00.to.be.graceful.streamx.JoinType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.hasItems;
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
                .join(JoinType.INNER_JOIN, groups, Message::getGroupId, Group::getGroupId,
                        (msg, group) -> {
                            MessageVO out = new MessageVO();
                            out.setContent(msg.getContent());
                            out.setGroupName(group.getGroupName());
                            out.setUserId(msg.getUserId());
                            return out;
                        })
                .join(JoinType.INNER_JOIN, users, MessageVO::getUserId, User::getUserId,
                        (msg, usr) -> {
                            msg.setUserName(usr.getUserName());
                            return msg;
                        })
                .collect(ArrayList::new);
        result.forEach(System.out::println);
    }

    @Test
    public void testInnerJoin() {
        List<Integer> drivingList = Arrays.asList(1, 3, 5, 7);
        List<Integer> joiningList = Arrays.asList(1, 2, 5);

        ArrayList<Integer> result = StreamX.of(drivingList)
                .join(JoinType.INNER_JOIN, joiningList, Function.identity(), Function.identity(),
                        Integer::sum)
                .collect(ArrayList::new);
        assertThat(result, hasItems(2, 10));
    }

    @Test
    public void testLeftJoin() {
        List<Integer> drivingList = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> joiningList = Arrays.asList(1, 3, 4, 5, 6);

        List<Integer> result = StreamX.of(drivingList)
                .join(JoinType.LEFT_JOIN, joiningList, Function.identity(), Function.identity(),
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
                .join(JoinType.LEFT_JOIN, joiningList, Function.identity(), Function.identity(),
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
        //Message 和 Group 之间的连接字段
        Function<Message, Integer> drivingGroupKey = Message::getGroupId;
        Function<Group, Integer> joiningGroupKey = Group::getGroupId;

        //Message 合并 Group 的函数
        BiFunction<Message, Group, MessageVO> mergedMsgAndGroup = (msg, group) -> {
            MessageVO out = new MessageVO();
            out.setContent(msg.getContent());
            out.setGroupName(group.getGroupName());
            out.setUserId(msg.getUserId());
            return out;
        };

        //Message 和 User 之间的连接字段
        Function<MessageVO, Integer> drivingUserKey = MessageVO::getUserId;
        Function<User, Integer> joiningUserKey = User::getUserId;

        //Message 合并 User 的函数
        BiFunction<MessageVO, User, MessageVO> mergedMsgAndUser = (msg, usr) -> {
            msg.setUserName(usr.getUserName());
            return msg;
        };

        List<MessageVO> result = new LinkedList<>();
        //遍历消息列表
        for (Message message : messages) {
            //合并群组信息
            Group group = groups.stream().filter(g -> Objects.equals(
                            joiningGroupKey.apply(g),
                            drivingGroupKey.apply(message)
                    ))
                    .findAny().get();
            MessageVO msgVO =mergedMsgAndGroup.apply(message, group);
            //合并用户信息
            User user = users.stream().filter(u ->
                            Objects.equals(joiningUserKey.apply(u), drivingUserKey.apply(msgVO)))
                    .findAny().orElse(null);

            //将合并后的结果放入结果集
            MessageVO merged2 = mergedMsgAndUser.apply(msgVO, user);
            result.add(merged2);
        }
    }

}
