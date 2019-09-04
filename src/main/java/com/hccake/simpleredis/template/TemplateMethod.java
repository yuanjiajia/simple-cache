package com.hccake.simpleredis.template;

import com.hccake.simpleredis.core.CacheOps;
import com.hccake.simpleredis.core.OpType;

import java.io.IOException;

/**
 * @author Hccake
 * @version 1.0
 * @date 2019/9/4 15:22
 */
public interface TemplateMethod {

    /**
     * 根据 操作类型 执行对应模板方法
     * @param ops  操作集
     * @param opType  操作类型
     * @return
     * @throws IOException
     */
    Object runByOpType(CacheOps ops, OpType opType) throws IOException;

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
    Object cached(CacheOps ops) throws IOException;


    /**
     * 缓存操作模板方法
     */
    Object cachePut(CacheOps ops) throws IOException;


    /**
     * 缓存删除的模板方法
     * 在目标方法执行后 执行删除
     */
    Object cacheDel(CacheOps ops) throws IOException;

}
