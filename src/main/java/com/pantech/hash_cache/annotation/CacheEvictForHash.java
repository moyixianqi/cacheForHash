package com.pantech.hash_cache.annotation;

import java.lang.annotation.*;

/**
 * Description:
 * 注解在方法上用于删除缓存
 * -------------------------
 * Created by ywq on 2019-10-20
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CacheEvictForHash {
    //字段 支持spEL 表达式, 如果该属性为空则返回并且缓存key下的所有数据
    String field() default "";

    //目标类
    Class targetClass();
}
