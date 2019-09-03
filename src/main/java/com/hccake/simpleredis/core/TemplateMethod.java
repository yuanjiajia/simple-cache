package com.hccake.simpleredis.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hccake.simpleredis.function.ResultMethod;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * 缓存操作的模板方法
 *
 * @author wubo, Hccake
 */
public class TemplateMethod {

    /**
     * jackson序列化
     */
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    /**
     * 根据 操作类型 执行对应模板方法
     * @param ops  操作集
     * @param opType  操作类型
     * @return
     * @throws IOException
     */
    public static Object runByOpType(CacheOps ops, OpType opType) throws IOException {
        switch (opType) {
            case CACHED:
                return TemplateMethod.cached(ops);
            case PUT:
                return TemplateMethod.cachePut(ops);
            case DEL:
                return TemplateMethod.cacheDel(ops);
            default:
                return null;
        }
    }




    /**
     * 反序列化方法
     *
     * @param cacheData
     * @param clazz
     * @return
     * @throws IOException
     */
    public static Object deserialization(String cacheData, Class<?> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(cacheData, clazz);
    }

    /**
     * 序列化方法
     *
     * @param cacheData
     * @return
     * @throws IOException
     */
    public static String serialize(Object cacheData) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(cacheData);
    }


    /**
     * cached 类型的模板方法
     * 1. 先查缓存 若有数据则直接返回
     * 2. 尝试获取锁 若成功执行目标方法（一般是去查数据库）
     * 3. 将数据库获取到数据同步至缓存
     *
     * @param ops
     * @return
     * @throws IOException
     */
    public static Object cached(CacheOps ops) throws IOException {

        //缓存查询方法
        Supplier<String> cacheQuery = ops.cacheQuery();
        //返回数据类型
        Class<?> dataClazz = ops.getReturnType();


        //1.==================尝试从缓存获取数据==========================
        String cacheData = cacheQuery.get();
        //如果是空值  则return null | 不是空值且不是null 则直接返回
        if (ops.nullValue(cacheData)) {
            return null;
        } else if (cacheData != null) {
            return deserialization(cacheData, dataClazz);
        }


        //2.==========如果缓存为空  则需查询数据库并更新===============
        Object dbData = null;
        //尝试获取锁，只允许一个线程更新缓存
        if (ops.lock()) {
            //有可能其他线程已经更新缓存，这里再次判断缓存是否为空
            cacheData = cacheQuery.get();
            if (cacheData == null) {
                //从数据库查询数据
                dbData = ops.pointMethod().run();
                //如果数据库中没数据，填充一个String，防止缓存击穿
                cacheData = dbData == null ? RedisCons.NULL_VALUE : serialize(dbData);
                //设置缓存
                ops.cachePut().accept(cacheData);
            }
            //解锁
            ops.unlock();
            //返回数据
            return dbData;
        } else {
            cacheData = cacheQuery.get();
        }

        //自旋时间内未获取到锁，或者数据库中数据为空，返回null
        if (cacheData == null || ops.nullValue(cacheData)) {
            return null;
        }
        return deserialization(cacheData, dataClazz);
    }


    /**
     * 缓存操作模板方法
     */
    public static Object cachePut(CacheOps ops) throws IOException {

        //先执行目标方法  并拿到返回值
        ResultMethod<Object> pointMethod = ops.pointMethod();
        Object data = pointMethod.run();

        //将返回值放置入缓存中
        String cacheData = data == null ? RedisCons.NULL_VALUE : serialize(data);
        ops.cachePut().accept(cacheData);

        return data;
    }


    /**
     * 缓存删除的模板方法
     * 在目标方法执行后 执行删除
     */
    public static Object cacheDel(CacheOps ops) throws IOException {

        //先执行目标方法  并拿到返回值
        ResultMethod<Object> pointMethod = ops.pointMethod();
        Object data = pointMethod.run();

        //将删除缓存
        ops.cacheDel().run();

        return data;
    }


}
