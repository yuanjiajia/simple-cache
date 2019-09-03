package com.hccake.simpleredis.core;

import lombok.Getter;

/**
 * @author Hccake
 * @version 1.0
 * @date 2019/9/2 20:50
 * 对缓存的几种操作类型
 */
@Getter
public enum OpType {

    /**
     * 先查询缓存，如果有则直接返回
     * 若没有，则执行目标方法，获取返回值后存入缓存
     */
    CACHED,

    /**
     * 执行目标方法后，获取返回值存入缓存
     */
    PUT,

    /**
     * 执行目标方法后，删除缓存
     */
    DEL;

}
