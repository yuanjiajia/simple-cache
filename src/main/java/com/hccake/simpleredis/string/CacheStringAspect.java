package com.hccake.simpleredis.string;

import com.hccake.simpleredis.RedisHelper;
import com.hccake.simpleredis.core.CacheOps;
import com.hccake.simpleredis.core.KeyGenerator;
import com.hccake.simpleredis.template.NormalTemplateMethod;
import com.hccake.simpleredis.template.TemplateMethod;
import com.hccake.simpleredis.function.ResultMethod;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author Hccake
 * @date 2019/8/31 18:01
 * @version 1.0
 */
@Aspect
@Component
@Slf4j
public class CacheStringAspect {

    /**
     * 模板方法
     */
    private static TemplateMethod templateMethod = new NormalTemplateMethod();

    @Autowired
    private RedisHelper redisHelper;

    @Pointcut("@annotation(com.hccake.simpleredis.Cached)")
    public void pointCut() {}

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {


        //获取目标方法
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        log.debug("=======The string cache aop is executed! method : {}", method.getName());

        //方法返回值
        Class<?> returnType = method.getReturnType();

        //根据方法的参数 以及当前类对象获得 keyGenerator
        Object target = point.getTarget();
        Object[] arguments = point.getArgs();
        KeyGenerator keyGenerator = new KeyGenerator(target, method, arguments);

        // 织入方法
        ResultMethod<Object> pointMethod = CacheOps.genPointMethodByPoint(point);

        //获取注解对象
        CacheForString cacheForString = AnnotationUtils.getAnnotation(method, CacheForString.class);

        //获取操作类
        CacheOps ops = new OpsForString(cacheForString, keyGenerator, pointMethod, returnType, redisHelper);

        //执行对应模板方法
        return templateMethod.runByOpType(ops, cacheForString.type());

    }




}
