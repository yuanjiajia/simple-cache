package com.hccake.simpleredis.template;

import com.hccake.simpleredis.core.CacheOps;
import com.hccake.simpleredis.core.RedisCons;
import com.hccake.simpleredis.function.ResultMethod;
import com.hccake.simpleredis.serialize.CacheSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

/**
 * 缓存操作的模板方法
 * 批量的
 *
 * @author wubo, Hccake
 */
@Component("multiTemplateMethod")
public class MultiTemplateMethod extends AbstractTemplateMethod {


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

        //返回数据
        List result = new ArrayList<>();

        //缓存查询方法
        Supplier<List<String>> cacheQuery = ops.cacheQuery();
        //返回数据类型
        Class<?> dataClazz = ops.getReturnType();


        //1.==================尝试从缓存获取数据==========================
        List<String> cacheDatas = cacheQuery.get();

        //用一个Map  key:null值的角标  value：存储后续数据库中查出的值
        Map<Integer, String> emptyKeyMap = new HashMap<>();

        //循环缓存中的数据  序列化为返回数据类型
        String cacheData;
        for (int i = 0; i < cacheDatas.size(); i++) {
            cacheData = cacheDatas.get(i);
            if (cacheData == null) {
                emptyKeyMap.put(i, null);
                result.add(null);
            }else{
                result.add(ops.nullValue(cacheData) ? null : cacheSerializer.deserialize(cacheData, dataClazz));
            }
        }

        //如果全部都存在缓存中 则直接返回
        if(emptyKeyMap.size() == 0){
            return cacheDatas;
        }


        //2.==========如果缓存为空  则需查询数据库并更新===============
        //尝试获取锁，只允许一个线程更新缓存
        if (ops.lock("1")) {

            //TODO 这里应该传入动态的参数 而不应该全量查询
            //从数据库查询数据
            List dbDatas = (List)ops.pointMethod().run();

            Object data;
            String value;
            Integer index;

            Iterator it = emptyKeyMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, String> entry = (Map.Entry) it.next();
                index = entry.getKey();

                data = dbDatas.get(index);
                result.set(index, data);

                //如果数据库中没数据，填充一个String，防止缓存击穿
                value = data == null? RedisCons.NULL_VALUE : cacheSerializer.serialize(data);
                emptyKeyMap.put(index, value);
            }

            //设置缓存
            ops.cachePut().accept(emptyKeyMap);

            //解锁
            ops.unlock("1");
        }

        //返回数据
        return result;
    }


    /**
     * 缓存操作模板方法
     */
    @Override
    public Object cachePut(CacheOps ops) throws IOException {

        //先执行目标方法  并拿到返回值
        ResultMethod<Object> pointMethod = ops.pointMethod();
        List dbDatas = (List)pointMethod.run();


        List<String> cacheDatas = new ArrayList<>();

        Object dbData;
        String cacheData;
        for (int i = 0; i < dbDatas.size(); i++) {
            dbData = dbDatas.get(i);
            //如果数据库中没数据，填充一个String，防止缓存击穿
            cacheData = dbData == null? RedisCons.NULL_VALUE : cacheSerializer.serialize(dbData);
            cacheDatas.add(cacheData);
        }
        //将返回值放置入缓存中
        ops.cachePut().accept(cacheDatas);

        return dbDatas;
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
