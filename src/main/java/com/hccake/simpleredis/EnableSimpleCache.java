package com.hccake.simpleredis;

import com.hccake.simpleredis.config.AutoScanConfig;
import com.hccake.simpleredis.config.CachedDeriveDefinitionRegistry;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Hccake
 * @version 1.0
 * @date 2019/9/2 14:32
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({AutoScanConfig.class, CachedDeriveDefinitionRegistry.class})
public @interface EnableSimpleCache {
    /**
     * 未指定，默认从声明EnableSimpleCache所在类的package进行扫描
     */
    String[] value() default {};
}
