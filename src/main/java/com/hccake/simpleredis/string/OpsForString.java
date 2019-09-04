package com.hccake.simpleredis.string;

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
public class OpsForString extends CacheOps {

    /**
     * 生成并返回一个 OpsForMultiString 对象
     * @param cacheForString  注解
     * @param keyGenerator    key 生成方法
     * @param pointMethod     织入方法
     * @param returnType     返回类型
     * @param redisHelper   缓存工具类
     */
    public OpsForString(CacheForString cacheForString, KeyGenerator keyGenerator, ResultMethod<Object> pointMethod, Class<?> returnType, RedisHelper redisHelper) {

        super(redisHelper, pointMethod, returnType);

        //缓存key
        String key = keyGenerator.getKey(cacheForString.key(), cacheForString.keyJoint());

        //缓存操作类型
        OpType opType = cacheForString.type();

        //CACHED缓存需要的属性
        if(OpType.CACHED.equals(opType)){
            //redis 分布式锁的 key
            String lockKey = key + RedisCons.LOCK_KEY_SUFFIX;
            this.setLockKey(lockKey);

            Supplier cacheQuery = () -> redisHelper.get(key);
            this.setCacheQuery(cacheQuery);

            Consumer<Object> cachePut = value -> redisHelper.setex(key, (String)value, cacheForString.ttl());
            this.setCachePut(cachePut);

        }

        // PUT 操作需要的属性
        if(OpType.PUT.equals(opType)) {
            Consumer<Object> cachePut = value -> redisHelper.setex(key, (String)value, cacheForString.ttl());
            this.setCachePut(cachePut);
        }

        // DEL 操作需要的属性
        if(OpType.DEL.equals(opType)) {
            VoidMethod cacheDel = () -> redisHelper.del(key);
            this.setCacheDel(cacheDel);
        }

    }

}
