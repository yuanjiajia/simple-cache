package com.hccake.simpleredis.config;

import com.hccake.simpleredis.Cached;
import com.hccake.simpleredis.EnableSimpleCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 *  派生性注解定义器
 * @author mingzhi.xie
 * @date 2019/9/4
 * @since 1.0
 */
@Slf4j
public class CachedDeriveDefinitionRegistry implements ImportBeanDefinitionRegistrar {

    private static final String RESOURCE_PATTERN = "/**/*.class";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> metadataAnnotationAttributes = importMetadata.getAnnotationAttributes(EnableSimpleCache.class.getName());

        CachedDeriveSource cachedDeriveSource = CachedDeriveSource.getInstance();
        if (metadataAnnotationAttributes == null) {
            // 未启用 EnableSimpleCache
            return;
        }
        cachedDeriveSource.setEnableSimpleCache(true);

        // 获取注册器所有属性
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(metadataAnnotationAttributes);

        // 获取扫描包
        Assert.notNull(annotationAttributes, "EnableSimpleCache annotation value() cloud not null.");
        String[] customPackage = annotationAttributes.getStringArray("value");

        if(ObjectUtils.isEmpty(customPackage)) {
            customPackage = new String[] {ClassUtils.getPackageName(importMetadata.getClassName())};
        }

        ArrayList<String> basePackages = new ArrayList<>(customPackage.length + 1);
        basePackages.add("com.hccake.simpleredis");
        String[] packages = basePackages.toArray(customPackage);

        // 从扫描包获取候选人
        ArrayList<? extends Class<?>> candidates = scanPackages(packages);
        cachedDeriveSource.setDeriveSource(candidates);
    }

    /**
     * 根据所有包名扫描出所有候选者们
     * @param basePackages  所有包名
     * @return              所有候选者们
     */
    private ArrayList<? extends Class<?>> scanPackages(String[] basePackages) {
        ArrayList<Class<?>> candidates = new ArrayList<>(16);
        for (String basePackage : basePackages) {
            candidates.addAll(findCandidateClasses(basePackage));
        }

        return candidates;
    }

    /**
     * 根据包名获取候选者们
     * @param basePackage   包名
     * @return              候选者们
     */
    private LinkedHashSet< ? extends Class<?>> findCandidateClasses(String basePackage) {
        LinkedHashSet<Class<?>> candidates = new LinkedHashSet<>(16);

        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + StringUtils.replace(basePackage, ".", "/") + RESOURCE_PATTERN;
        ResourceLoader resourceLoader = new DefaultResourceLoader();

        Resource[] resources = new Resource[0];
        try {
            resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(packageSearchPath);
        } catch (IOException e) {
            log.error("扫描 Cached 派生资源类异常", e);
        }

        MetadataReaderFactory readerFactory = new SimpleMetadataReaderFactory(resourceLoader);
        try {
            for (Resource resource : resources) {
                MetadataReader metadataReader = readerFactory.getMetadataReader(resource);
                ClassMetadata classMetadata = metadataReader.getClassMetadata();
                Class<?> clazz = ClassUtils.forName(classMetadata.getClassName(), getClass().getClassLoader());

                // 是否为候选者
                if (clazz.isAnnotationPresent(Cached.class)) {
                    candidates.add(clazz);
                    log.debug("扫描到符合要求 Cached 派生注解: {}", clazz);
                }
            }
        } catch (IOException e) {
            log.error("获取元数据读取器失败", e);
        } catch (ClassNotFoundException e) {
            log.error("未扫描到派生注解", e);
        }

        return candidates;
    }
}
