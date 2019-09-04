package com.hccake.simpleredis.core;

import com.hccake.simpleredis.RedisHelper;
import com.hccake.simpleredis.function.ResultMethod;
import com.hccake.simpleredis.function.VoidMethod;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Hccake
 * @version 1.0
 * @date 2019/9/2 15:19
 */
public abstract class CacheOps {

    /**
     * 基本构造方法
     * @param redisHelper  缓存工具类
     * @param pointMethod 返回数据类型
     * @param returnType 织入方法
     */
    public CacheOps(RedisHelper redisHelper, ResultMethod<Object> pointMethod, Class<?> returnType) {
        this.redisHelper = redisHelper;
        this.returnType = returnType;
        this.pointMethod = pointMethod;
    }

    /**
     * 缓存连接工具类
     */
    private RedisHelper redisHelper;

    /**
     * 织入方法
     *
     * @return ResultMethod
     */
    private ResultMethod<Object> pointMethod;

    /**
     * 数据类型
     */
    private Class<?> returnType;



    /**
     * 缓存分布式锁的key
     *
     * @return String
     */
    private String lockKey;

    /**
     * 从Redis中获取缓存数据的操作
     *
     * @return Supplier
     */
    private Supplier<Object> cacheQuery;

    /**
     * 向缓存写入数据
     *
     * @return Consumer
     */
    private Consumer<Object> cachePut;

    /**
     * 删除缓存数据
     *
     * @return VoidMethod
     */
    private VoidMethod cacheDel;

    /**
     * 将point的织入方法 封装成函数
     * @param point
     * @return
     */
    public static ResultMethod<Object> genPointMethodByPoint(ProceedingJoinPoint point) {
        ResultMethod<Object> pointMethod = () -> {
            try {
                return point.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return null;
        };

        return pointMethod;
    }


    public Supplier cacheQuery() {
        return cacheQuery;
    }

    public ResultMethod<Object> pointMethod() {
        return pointMethod;
    }

    public Consumer<Object> cachePut() {
        return cachePut;
    }

    public VoidMethod cacheDel() {
        return cacheDel;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setRedisHelper(RedisHelper redisHelper) {
        this.redisHelper = redisHelper;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    public void setCacheQuery(Supplier<Object> cacheQuery) {
        this.cacheQuery = cacheQuery;
    }

    public void setPointMethod(ResultMethod<Object> pointMethod) {
        this.pointMethod = pointMethod;
    }

    public void setCachePut(Consumer<Object> cachePut) {
        this.cachePut = cachePut;
    }

    public void setCacheDel(VoidMethod cacheDel) {
        this.cacheDel = cacheDel;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }


    /**
     * 检查缓存数据是否是空值
     *
     * @param cacheData
     * @return
     */
    public boolean nullValue(Object cacheData) {
        return RedisCons.NULL_VALUE.equals(cacheData);
    }
    /**
     * 上锁
     *
     * @return
     */
    public Boolean lock() {
        return redisHelper.setexnx(lockKey, "1", RedisCons.LOCKED_TIME);
    }

    /**
     * 释放Redis锁
     */
    public void unlock() {
        redisHelper.del(lockKey);
    }


}
