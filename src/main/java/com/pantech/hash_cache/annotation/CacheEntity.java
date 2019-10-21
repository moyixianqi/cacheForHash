package com.pantech.hash_cache.annotation;


import java.lang.annotation.*;

/**
 * Description:
 * 描述该实体开启redis hash缓存
 * -------------------------
 * Created by ywq on 2019-10-20
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CacheEntity {

    //redisKey
    String key() default "";

    //redisFieldKey 的分隔符
    String separator() default "_";
}
