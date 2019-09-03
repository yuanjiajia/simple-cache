
package com.hccake.simpleredis;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * redicache 工具类
 */
@Component
public class RedisHelper {

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;


    //=============================common============================

    /**
     * 指定缓存失效时间
     *
     * @param key  键
     * @param time 时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效 失效时间为负数，说明该主键未设置失效时间（失效时间默认为-1）
     */
    public long ttl(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true 存在 false 不存在
     */
    public boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存
     *
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    //============================String=============================
    /**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */
 /*   public <T> T get(String key, Class<T> clazz){
        ValueOperations<String, T> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }*/

    /**
     * 普通缓存获取
     *
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }


    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 批量缓存get
     *
     * @param keys
     * @return
     */
    public List<String> mget(List<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }


    /**
     * 批量缓存set
     *
     * @param map
     * @return
     */
    public boolean mset(Map<String, String> map) {
        try {
            redisTemplate.opsForValue().multiSet(map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 如果不存在则设置值，存在不做任何操作
     *
     * @param key   键
     * @param value 值
     * @return true成功 false 失败
     */
    public boolean setnx(String key, String value) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean setex(String key, String value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 如果不存在则设置值，并设置更新时间，存在不做任何操作
     *
     * @param key   键
     * @param value 值
     * @return true成功 false 失败
     */
    public boolean setexnx(String key, String value, long time) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, time, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 递增 此时value值必须为int类型 否则报错
     *
     * @param key     键
     * @param incrNum 正则递增 负则递减
     * @return
     */
    public long incrby(String key, long incrNum) {
        return redisTemplate.opsForValue().increment(key, incrNum);
    }


    /**
     * 递增 此时value值必须为int类型 否则报错
     * 顺便更新存活时间
     *
     * @param key     键
     * @param incrNum 正则递增 负则递减
     * @return
     */
    public long incrbyex(String key, long incrNum, long time) {

        Long increment = redisTemplate.opsForValue().increment(key, incrNum);
        expire(key, time);
        return increment;
    }


    //================================Map=================================
    /**
     * HashGet
     * @param key 键 不能为null
     * @param field 项 不能为null
     * @param clazz 返回类型
     * @return T
     */
/*    public <T> T hget(String key, String field, Class<T> clazz){
        HashOperations<String, String, T> hashOperations = redisTemplate.opsForHash();
        return hashOperations.get(key, field);
    }*/

    /**
     * HashGet
     *
     * @param key
     * @param field
     * @return String
     */
    public String hget(String key, String field) {
        HashOperations<String, String, String> stringObjectObjectHashOperations = redisTemplate.opsForHash();
        return stringObjectObjectHashOperations.get(key, field);
    }


    /**
     * 获取hashKey对应的所有键值
     *
     * @param key        键
     * @param hashFields 域
     * @return 对应的多个键值
     */
    public List<String> hmget(String key, List<String> hashFields) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        return hashOperations.multiGet(key, hashFields);
    }


    /**
     * 获取hashKey对应的所有键值
     * @param key 键
     * @param hashFields 域
     * @return 对应的多个键值
     */
  /*  public List<String> hmget(String key, List<String> hashFields){
        return this.hmget(key, hashFields);
    }*/


    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<String, String> hgetAll(String key) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        return hashOperations.entries(key);
    }


    /**
     * 获取hashKey对应的所有键值
     * @param key 键
     * @return 对应的多个键值
     */
/*    public Map<String, String> hgetAll(String key){
        return this.hgetAll(key, String.class);
    }*/


    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public <T> boolean hmset(String key, Map<String, String> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     *
     * @param key  键
     * @param map  对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public <T> boolean hmsetEx(String key, Map<String, String> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param field 项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hset(String key, String field, String value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在将创建
     *
     * @param key   键
     * @param field 项
     * @param value 值
     * @param time  时间(秒)  注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public boolean hsetex(String key, String field, String value, long time) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     *
     * @param key  键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     *
     * @param key  键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hexists(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增 如果不存在,就会创建一个 并把新增后的值返回
     *
     * @param key  键
     * @param item 项
     * @param num  要增加几(大于0)
     * @return
     */
    public double hincrby(String key, String item, long num) {
        return redisTemplate.opsForHash().increment(key, item, num);
    }

    /**
     * 浮点数递增
     * @param key
     * @param num
     * @return
     */
    public Double incrbyfloat(String key, double num) {
        return redisTemplate.opsForValue().increment(key, num);
    }
}
