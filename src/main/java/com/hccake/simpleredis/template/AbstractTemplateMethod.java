package com.hccake.simpleredis.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hccake.simpleredis.core.CacheOps;
import com.hccake.simpleredis.core.OpType;

import java.io.IOException;

/**
 * 缓存操作的模板方法
 *
 * @author wubo, Hccake
 */
public abstract class AbstractTemplateMethod implements TemplateMethod{

    /**
     * jackson序列化
     */
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
     * 根据 操作类型 执行对应模板方法
     * @param ops  操作集
     * @param opType  操作类型
     * @return
     * @throws IOException
     */
    @Override
    public Object runByOpType(CacheOps ops, OpType opType) throws IOException {
        switch (opType) {
            case CACHED:
                return cached(ops);
            case PUT:
                return cachePut(ops);
            case DEL:
                return cacheDel(ops);
            default:
                return null;
        }
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
    @Override
    public abstract Object cached(CacheOps ops) throws IOException ;


    /**
     * 缓存操作模板方法
     */
    @Override
    public abstract Object cachePut(CacheOps ops) throws IOException;



    /**
     * 缓存删除的模板方法
     * 在目标方法执行后 执行删除
     */
    @Override
    public abstract Object cacheDel(CacheOps ops) throws IOException;


}
