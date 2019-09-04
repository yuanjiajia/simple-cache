package com.hccake.simpleredis.hash;

import com.hccake.simpleredis.RedisHelper;
import com.hccake.simpleredis.core.CacheOps;
import com.hccake.simpleredis.core.KeyGenerator;
import com.hccake.simpleredis.core.OpType;
import com.hccake.simpleredis.core.RedisCons;
import com.hccake.simpleredis.function.ResultMethod;
import com.hccake.simpleredis.function.VoidMethod;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Hccake
 * @version 1.0
 * @date 2019/9/2 15:33
 */
public class OpsForHash extends CacheOps {

    /**
     * 生成并返回一个 OpsForHash 对象
     * @param cacheForHash  注解
     * @param keyGenerator    key 生成方法
     * @param pointMethod     织入方法
     * @param returnType     返回类型
     * @param redisHelper   缓存工具类
     */
    public OpsForHash(CacheForHash cacheForHash, KeyGenerator keyGenerator, ResultMethod<Object> pointMethod, Class<?> returnType, RedisHelper redisHelper) {

        super(redisHelper, pointMethod, returnType);

        //缓存key
        String key = keyGenerator.getKey(cacheForHash.key(), cacheForHash.keyJoint());
        String field = keyGenerator.parseSpEL(cacheForHash.field());

        //缓存操作类型
        OpType opType = cacheForHash.type();

        //CACHED缓存需要的属性
        if(OpType.CACHED.equals(opType)){
            //redis 分布式锁的 key
            String lockKey = key + field + RedisCons.LOCK_KEY_SUFFIX;
            this.setLockKey(lockKey);

            Supplier cacheQuery = () -> redisHelper.hget(key, field);
            this.setCacheQuery(cacheQuery);

            Consumer<Object> cachePut = value -> redisHelper.hset(key, field, (String)value);
            this.setCachePut(cachePut);
        }

        // PUT 操作需要的属性
        if(OpType.PUT.equals(opType)) {
            Consumer<Object> cachePut = value -> redisHelper.hset(key, field, (String)value);
            this.setCachePut(cachePut);
        }

        // DEL 操作需要的属性
        if(OpType.DEL.equals(opType)) {
            VoidMethod cacheDel = () -> redisHelper.hdel(key, field);
            this.setCacheDel(cacheDel);
        }

    }


}
