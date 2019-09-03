package com.hccake.simpleredis.core;

/**
 * 常用的一些标识
 * @author Hccake
 * TODO 可配置
 */
public class RedisCons {

    /**
     * 默认分隔符
     */
    public static final String DELIMITER= ":";
    /**
     * 空值标识
     */
    public static final String NULL_VALUE = "N_V";
    /**
     * 默认超时时间
     */
    public static final long EXPIRE_TIME = 86400L;

    /**
     * redis锁 后缀
     */
    public static final String LOCK_KEY_SUFFIX = "locked";
    /**
     * 锁的超时时间
     */
    public static final long LOCKED_TIME = 30L;
}
