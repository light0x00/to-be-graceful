# To Be Graceful

> To be, or not to be, that is the question. 

## 快速上手

![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.light0x00/to-be-graceful/badge.svg) 
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

```xml
<dependency>
    <groupId>io.github.light0x00</groupId>
    <artifactId>to-be-graceful</artifactId>
    <version>0.0.1</version>
</dependency>
```

Join 

```java
List<Integer> drivingList = Arrays.asList(1, 3, 5, 7);
List<Integer> joiningList = Arrays.asList(1, 2, 5);

ArrayList<Integer> result = StreamX.of(drivingList)
        .join(JoinType.INNER_JOIN, joiningList, Function.identity(), Function.identity(),
                Integer::sum)
        .collect(ArrayList::new);
assertThat(result, hasItems(2, 10));
```

Filter\Map\Reduce

```java
Optional<Integer> result = StreamX.of(Arrays.asList(1, -2, 3, -4, 5, -6))
        .map(Math::abs)
        .filter(i -> i > 2 && i < 5)
        .reduce(Integer::sum);

assertTrue(result.isPresent());
assertTrue(result.get() == 7);
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
```

然而，更优雅的写法是：

```java
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
```



