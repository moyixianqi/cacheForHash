package com.pantech.hash_cache.annotation;


import java.lang.annotation.*;

/**
 * Description:
 * 描述该字段当作 redis hash filedKey
 * -------------------------
 * Created by ywq on 2019-10-20
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CacheField {
    int order() default 0;
}
