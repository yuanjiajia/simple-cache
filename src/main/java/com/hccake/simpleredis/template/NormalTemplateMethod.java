package com.hccake.simpleredis.template;

import com.hccake.simpleredis.core.CacheOps;
import com.hccake.simpleredis.core.RedisCons;
import com.hccake.simpleredis.function.ResultMethod;
import com.hccake.simpleredis.serialize.CacheSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * 缓存操作的模板方法
 *
 * @author wubo, Hccake
 */
@Component("normalTemplateMethod")
public class NormalTemplateMethod extends AbstractTemplateMethod{


    @Autowired
    private CacheSerializer cacheSerializer;

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
    public Object cached(CacheOps ops) throws IOException {

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
            return cacheSerializer.deserialize(cacheData, dataClazz);
        }


        //2.==========如果缓存为空  则需查询数据库并更新===============
        Object dbData = null;
        //尝试获取锁，只允许一个线程更新缓存
        if (ops.lock("1")) {
            //有可能其他线程已经更新缓存，这里再次判断缓存是否为空
            cacheData = cacheQuery.get();
            if (cacheData == null) {
                //从数据库查询数据
                dbData = ops.pointMethod().run();
                //如果数据库中没数据，填充一个String，防止缓存击穿
                cacheData = dbData == null ? RedisCons.NULL_VALUE : cacheSerializer.serialize(dbData);
                //设置缓存
                ops.cachePut().accept(cacheData);
            }
            //解锁
            ops.unlock("1");
            //返回数据
            return dbData;
        } else {
            cacheData = cacheQuery.get();
        }

        //自旋时间内未获取到锁，或者数据库中数据为空，返回null
        if (cacheData == null || ops.nullValue(cacheData)) {
            return null;
        }
        return cacheSerializer.deserialize(cacheData, dataClazz);
    }


    /**
     * 缓存操作模板方法
     */
    @Override
    public Object cachePut(CacheOps ops) throws IOException {

        //先执行目标方法  并拿到返回值
        ResultMethod<Object> pointMethod = ops.pointMethod();
        Object data = pointMethod.run();

        //将返回值放置入缓存中
        String cacheData = data == null ? RedisCons.NULL_VALUE : cacheSerializer.serialize(data);
        ops.cachePut().accept(cacheData);

        return data;
    }


    /**
     * 缓存删除的模板方法
     * 在目标方法执行后 执行删除
     */
    @Override
    public Object cacheDel(CacheOps ops) throws IOException {

        //先执行目标方法  并拿到返回值
        ResultMethod<Object> pointMethod = ops.pointMethod();
        Object data = pointMethod.run();

        //将删除缓存
        ops.cacheDel().run();

        return data;
    }


}
