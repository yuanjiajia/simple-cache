package com.hccake.simpleredis.serialize;

import java.io.IOException;

/**
 * @author Hccake
 * @version 1.0
 * @date 2019/9/9 11:09
 */
public interface CacheSerializer {


    /**
     * 序列化方法
     *
     * @param cacheData
     * @return
     * @throws IOException
     */
    String serialize(Object cacheData) throws IOException;


    /**
     * 反序列化方法
     *
     * @param cacheData
     * @param clazz
     * @return
     * @throws IOException
     */
    Object deserialize(String cacheData, Class<?> clazz) throws IOException;

}
