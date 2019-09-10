package com.hccake.simpleredis.multistring;

import com.hccake.simpleredis.RedisHelper;
import com.hccake.simpleredis.core.CacheOps;
import com.hccake.simpleredis.core.KeyGenerator;
import com.hccake.simpleredis.function.ResultMethod;
import com.hccake.simpleredis.template.TemplateMethod;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * @author Hccake
 * @date 2019/8/31 18:01
 * @version 1.0
 */
@Aspect
@Component
@Slf4j
public class CacheMultiStringAspect {

    /**
     * 模板方法
     */
    @Resource(name = "multiTemplateMethod")
    private TemplateMethod templateMethod;

    @Autowired
    private RedisHelper redisHelper;

    @Pointcut("@annotation(com.hccake.simpleredis.multistring.CacheForMultiString)")
    public void pointCut() {}

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {


        //获取目标方法
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        log.debug("=======The string cache aop is executed! method : {}", method.getName());

        //参照 fastJson TypeRefence的代码
        //因为multi操作 返回值一定是 List<T> 所以直接取第一个就可以
        Type type = method.getGenericReturnType();
        Type dataType = type.getTypeName().equals("void")? null: ((ParameterizedType)type).getActualTypeArguments()[0];

        //根据方法的参数 以及当前类对象获得 keyGenerator
        Object target = point.getTarget();
        Object[] arguments = point.getArgs();
        KeyGenerator keyGenerator = new KeyGenerator(target, method, arguments);

        // 织入方法
        ResultMethod<Object> pointMethod = CacheOps.genPointMethodByPoint(point);

        //获取注解对象
        CacheForMultiString cacheAnnotation = AnnotationUtils.getAnnotation(method, CacheForMultiString.class);

        int paramIndex = cacheAnnotation.multiBy();
        Collection<String> multiByItem = (Collection<String>)arguments[paramIndex];

        //获取操作类
        CacheOps ops = new OpsForMultiString(cacheAnnotation, keyGenerator, pointMethod, dataType, redisHelper, multiByItem);

        //执行对应模板方法
        return templateMethod.runByOpType(ops, cacheAnnotation.type());

    }




}
