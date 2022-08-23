# To Be Graceful

> To be, or not to be, that is the question.

## 快速上手

![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.light0x00/to-be-graceful/badge.svg)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

```xml

<dependency>
    <groupId>io.github.light0x00</groupId>
    <artifactId>to-be-graceful</artifactId>
    <version>0.0.2</version>
</dependency>
```

Join

```java
List<Integer> drivingList=Arrays.asList(1,3,5,7);
        List<Integer> joiningList=Arrays.asList(1,2,5);

        ArrayList<Integer> result=StreamX.of(drivingList)
        .join(JoinType.INNER_JOIN,joiningList,Function.identity(),Function.identity(),
        Integer::sum)
        .collect(ArrayList::new);
        assertThat(result,hasItems(2,10));
```

Filter\Map\Reduce

```java
Optional<Integer> result=StreamX.of(Arrays.asList(1,-2,3,-4,5,-6))
        .map(Math::abs)
        .filter(i->i>2&&i< 5)
        .reduce(Integer::sum);

        assertTrue(result.isPresent());
        assertTrue(result.get()==7);
```

## 解决了什么痛点？

考虑这样一个场景，我们正在编写一个聊天室功能，很自然地，会有 User、Message、Group 等实体，如下所示：

```java

@AllArgsConstructor
@Data
static class User {
    private Integer userId;
    private String userName;
}

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
```

现在需要提供一个接口，返回群组里的聊天记录，如下所示，一条聊天记录中的字段来自于 User、Message、Group。

```java

@Data
static class MessageVO {
    private Integer userId;
    private String userName;
    private String groupName;
    private String content;
}
```

按照一般的写法如下：

```java
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
        MessageVO msgVO = mergedMsgAndGroup.apply(message, group);
        //合并用户信息
        User user = users.stream().filter(u ->
                        Objects.equals(joiningUserKey.apply(u), drivingUserKey.apply(msgVO)))
                .findAny().orElse(null);

        //将合并后的结果放入结果集
        MessageVO merged2 = mergedMsgAndUser.apply(msgVO, user);
        result.add(merged2);
    }
}
```

然而，更优雅的写法是：

```java
List<MessageVO> result = StreamX.of(messages)
.join(JoinType.INNER_JOIN, groups, Message::getGroupId, Group::getGroupId,
    (msg, group) -> {
        MessageVO out = new MessageVO();
        out.setContent(msg.getContent());
        out.setGroupName(group.getGroupName());
        out.setUserId(msg.getUserId());
        return out;
    })
.joinAsItself(JoinType.INNER_JOIN, users, MessageVO::getUserId, User::getUserId,
    (msg, usr) -> {
        msg.setUserName(usr.getUserName());
    })
.collect(ArrayList::new);
```



