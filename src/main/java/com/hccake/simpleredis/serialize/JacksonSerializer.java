package com.hccake.simpleredis.serialize;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author Hccake
 * @version 1.0
 * @date 2019/9/9 11:07
 */
@Component
public class JacksonSerializer implements CacheSerializer{

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 反序列化方法
     *
     * @param cacheData
     * @param type
     * @return
     * @throws IOException
     */
    @Override
    public  Object deserialize(String cacheData, Type type) throws IOException {
        return objectMapper.readValue(cacheData, CacheSerializer.getJavaType(type));
    }

    /**
     * 序列化方法
     *
     * @param cacheData
     * @return
     * @throws IOException
     */
    @Override
    public  String serialize(Object cacheData) throws IOException {
        return objectMapper.writeValueAsString(cacheData);
    }




}
