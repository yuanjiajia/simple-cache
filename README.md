#  使用注解操作redis缓存，目前支持String 以及 Hash类型

## 注解操作类型说明
> 目前分为三种通用的操作类型

1. Cached
    1. 先查询缓存 若不为空 直接返回
    2. 若缓存为空，则调用代理方法，获取结果集
    3. 将结果集置入缓存，以便下次读取
    
2. CachedPut
    1. 执行代理方法
    2. 将代理方法的返回结果置入缓存
    
3. CacheDel
    1. 执行代理方法
    2. 将对应缓存删除
    
    
## 如何使用

1. 准备工作  
    引入依赖后，在配置类上添加注解`@EnableSimpleCache`, 即可开启使用
    > 注：redis 配置按照 spring-data-redis， 如果已经配置过，无需改动

    ```java
    @EnableSimpleRedis
    @SpringBootApplication
    public class SimpleCacheDemoApplication {
    
        public static void main(String[] args) {
            SpringApplication.run(SimpleCacheDemoApplication.class, args);
        }
    }
    ```

2. 在service实现类的方法上添加注解即可

    ```java
    @CacheForString(type = OpType.CACHED, key = "redisKey here", keyJoint = "#p0 + 'lisi'")
    public User getUser(String userName){
        return new User("zhangsan", 18);
    }
    ```

    其中type为操作类型， key为redisKey, keyJoint为非必传参数，如果key是动态key, 则需传入此参数，值为spel表达式
    ，方法执行时，会自动解析，并将其和key前缀拼接，使用：作为分隔符