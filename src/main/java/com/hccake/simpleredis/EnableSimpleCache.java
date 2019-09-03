package com.hccake.simpleredis;

import com.hccake.simpleredis.config.AutoScanConfig;
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
@Import({AutoScanConfig.class})
public @interface EnableSimpleCache {
}
