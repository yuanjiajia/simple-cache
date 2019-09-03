## 后续功能点
1. MGET 支持
2. HMGET 支持
3. page 扩展


## 扩展考虑
1. RedisHelper 目前默认使用 spring-data-redis, 应可支持使用jedis等
2. 序列化方式应支持自定义，目前默认使用jackson
3. key过期时间，锁过期时间等一些配置信息，应该动态可配
