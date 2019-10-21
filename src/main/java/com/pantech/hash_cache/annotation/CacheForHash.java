package com.pantech.hash_cache.annotation;

import java.lang.annotation.*;

/**
 * Description:
 * 注解在方法上用户走缓存（必要时更新缓存）,如实体有CacheEntity注解并且key不为空，但是redis没有相应缓存的时候会初始化缓存
 * -------------------------
 * Created by ywq on 2019-10-20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CacheForHash {
    //字段 支持spEL 表达式, 如果该属性为空则返回并且缓存key下的所有数据
    String field() default "";

    //目标类
    Class targetClass();
}
