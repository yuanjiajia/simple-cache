package com.hccake.simpleredis.config;

import com.hccake.simpleredis.string.CacheStringAspect;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Map;

/**
 *  Cached 上下文完全准备但未刷新监听器
 * @author mingzhi.xie
 * @date 2019/9/4
 * @since 1.0
 */
@Slf4j
public class CachedApplicationListener implements ApplicationListener {

    private CachedDeriveSource cachedDeriveSource = CachedDeriveSource.getInstance();

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (!cachedDeriveSource.isEnableSimpleCache()) {
            // 未启用 EnableSimpleCache
            log.debug("未启用 EnableSimpleCache");
            return;
        }

        if (event instanceof ApplicationPreparedEvent) {
            onApplicationPreparedEvent((ApplicationPreparedEvent) event);
        }
    }

    private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
        ConfigurableApplicationContext context = event.getApplicationContext();
        context.addBeanFactoryPostProcessor(new CachedPostProcessor());
    }

    private class CachedPostProcessor implements BeanFactoryPostProcessor {

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            try {
                Method declaredMethod = CacheStringAspect.class.getDeclaredMethod("pointCut");
                modifyExpression(declaredMethod);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        @SuppressWarnings("unchecked")
        private void modifyExpression(Method cacheHashPointCut) throws Exception {
            // 获取注解代理类
            Pointcut pointcut = cacheHashPointCut.getAnnotation(Pointcut.class);
            InvocationHandler handler = Proxy.getInvocationHandler(pointcut);
            Field field = handler.getClass().getDeclaredField("memberValues");
            field.setAccessible(true);
            Map<String, String> values = (Map<String, String>) field.get(handler);

            // 拼接表达式
            ArrayList<? extends Class<?>> deriveSource = cachedDeriveSource.getDeriveSource();
            int size = deriveSource.size();
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < size; i++) {
                if (i == 0) {
                    buffer.append("@annotation(").append(deriveSource.get(i).getName()).append(")");
                } else {
                    buffer.append(" || @annotation(").append(deriveSource.get(i).getName()).append(")");
                }
            }

            values.put("value", buffer.toString());
            log.debug("Modify expression is [{}]",buffer.toString());
        }
    }
}
