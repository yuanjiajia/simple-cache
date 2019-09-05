package com.hccake.simpleredis.config;

import java.util.ArrayList;

/**
 *  派生注解资源
 * @author mingzhi.xie
 * @date 2019/9/4
 * @since 1.0
 */
public class CachedDeriveSource {

    private ArrayList<? extends Class<?>> deriveSource;

    private boolean enableSimpleCache;

    public static void setInstance(CachedDeriveSource instance) {
        CachedDeriveSource.instance = instance;
    }

    private static CachedDeriveSource instance = new CachedDeriveSource();

    private CachedDeriveSource() {}

    public static CachedDeriveSource getInstance() {
        return instance;
    }

    public ArrayList<? extends Class<?>> getDeriveSource() {
        return deriveSource;
    }

    public void setDeriveSource(ArrayList<? extends Class<?>> deriveSource) {
        this.deriveSource = deriveSource;
    }

    public boolean isEnableSimpleCache() {
        return enableSimpleCache;
    }

    public void setEnableSimpleCache(boolean enableSimpleCache) {
        this.enableSimpleCache = enableSimpleCache;
    }
}
