# To Be Graceful

> To be, or not to be, that is the question. 

## 快速上手

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



