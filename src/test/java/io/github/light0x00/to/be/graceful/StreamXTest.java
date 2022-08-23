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
        Collection<MessageVO> result = StreamX.of(messages)
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
        Function<Message, Integer> drivingGroupKey = Message::getGroupId;
        Function<Group, Integer> joiningGroupKey = Group::getGroupId;

        BiFunction<Message, Group, MessageVO> merge = (msg, group) -> {
            MessageVO out = new MessageVO();
            out.setContent(msg.getContent());
            out.setGroupName(group.getGroupName());
            out.setUserId(msg.getUserId());
            return out;
        };

        Function<MessageVO, Integer> drivingUserKey = MessageVO::getUserId;
        Function<User, Integer> joiningUserKey = User::getUserId;

        BiFunction<MessageVO, User, MessageVO> merge2 = (msg, usr) -> {
            msg.setUserName(usr.getUserName());
            return msg;
        };

        Collection<MessageVO> result = new LinkedList<>();

        for (Message message : messages) {
            //怎么找
            Group group = groups.stream().filter(g -> Objects.equals(
                            joiningGroupKey.apply(g),
                            drivingGroupKey.apply(message)
                    ))
                    .findAny().get();
            //怎么合
            MessageVO merged = merge.apply(message, group);

            User user = users.stream().filter(u ->
                            Objects.equals(joiningUserKey.apply(u), drivingUserKey.apply(merged)))
                    .findAny().orElse(null);

            MessageVO merged2 = merge2.apply(merged, user);
            result.add(merged2);
        }
    }

}
