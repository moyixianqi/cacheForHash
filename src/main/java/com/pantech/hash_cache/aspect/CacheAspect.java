package com.pantech.hash_cache.aspect;


import com.alibaba.fastjson.JSONObject;
import com.pantech.hash_cache.annotation.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * 缓存切面逻辑
 * -------------------------
 * Created by ywq on 2019-10-20
 */
@Aspect
@Component
@Log4j2
public class CacheAspect {

    private StringRedisTemplate redisTemplate;

    //用于SpEL表达式解析.
    private SpelExpressionParser spelExpressionParser;

    //用于获取方法参数定义名字.
    private DefaultParameterNameDiscoverer parameterNameDiscoverer;

    public CacheAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.spelExpressionParser = new SpelExpressionParser();
        this.parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    }

    //定义切点 CacheForHash
    @Pointcut("@annotation(com.pantech.hash_cache.annotation.CacheForHash)")
    public void cacheForHash() {
    }

    //定义切点 CachePutForHash
    @Pointcut("@annotation(com.pantech.hash_cache.annotation.CachePutForHash)")
    public void cachePutForHash() {
    }

    //定义切点 CacheEvictForHash
    @Pointcut("@annotation(com.pantech.hash_cache.annotation.CacheEvictForHash)")
    public void cacheEvictForHash() {
    }

    /**
     * 方法成功返回后 删除缓存
     *
     * @param joinPoint
     */
    @AfterReturning(pointcut = "cacheEvictForHash()")
    public void handleCacheEvictForHash(JoinPoint joinPoint) {
        //构建表达式计算上下文
        StandardEvaluationContext evaluationContext = getStandardEvaluationContext(joinPoint, null);
        //获取method 对象
        Method method = getMethod(joinPoint);
        //获取注解
        CacheEvictForHash cacheEvictForHash = method.getAnnotation(CacheEvictForHash.class);

        //获取redisKey
        String redisKey = getRedisKey(cacheEvictForHash.targetClass());

        //获取redis hash fieldKey 表达式
        String redisHashFieldKeyExp = cacheEvictForHash.field();
        String redisHashFieldKey = null;
        if (StringUtils.isNotEmpty(redisHashFieldKeyExp)) {
            //获取 redisFiledKey 值
            redisHashFieldKey = Objects.requireNonNull(spelExpressionParser.parseExpression(redisHashFieldKeyExp).getValue(evaluationContext)).toString();
        }

        //删除缓存
        redisTemplate.opsForHash().delete(redisKey, redisHashFieldKey);
    }


    /**
     * 方法成功返回后 更新缓存
     *
     * @param joinPoint
     * @param returnValue 返回值
     */
    @AfterReturning(pointcut = "cachePutForHash()", returning = "returnValue")
    public void handleCachePutForHash(JoinPoint joinPoint, Object returnValue) {
        //构建表达式计算上下文
        StandardEvaluationContext evaluationContext = getStandardEvaluationContext(joinPoint, returnValue);
        //获取method 对象
        Method method = getMethod(joinPoint);
        //获取注解
        CachePutForHash cachePutForHash = method.getAnnotation(CachePutForHash.class);

        //获取redisKey
        String redisKey = getRedisKey(cachePutForHash.targetClass());

        //获取redis hash fieldKey 表达式
        String redisHashFieldKeyExp = cachePutForHash.field();
        String redisHashFieldKey = null;
        if (StringUtils.isNotEmpty(redisHashFieldKeyExp)) {
            //获取 redisFiledKey 值
            redisHashFieldKey = Objects.requireNonNull(spelExpressionParser.parseExpression(redisHashFieldKeyExp).getValue(evaluationContext)).toString();
        }

        if (redisTemplate.hasKey(redisKey)) {
            //更新缓存
            redisTemplate.opsForHash().put(redisKey, redisHashFieldKey, JSONObject.toJSONString(returnValue));
        }
    }

    /**
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("cacheForHash()")
    public Object handleCacheForHash(ProceedingJoinPoint joinPoint) throws Throwable {

        //构建表达式计算上下文
        StandardEvaluationContext evaluationContext = getStandardEvaluationContext(joinPoint, null);
        //获取method 对象
        Method method = getMethod(joinPoint);
        //获取注解
        CacheForHash cacheForHash = method.getAnnotation(CacheForHash.class);

        //获取redisKey
        String redisKey = getRedisKey(cacheForHash.targetClass());

        //如果key 为空 则走数据库
        if ("".equals(redisKey)) {
            return joinPoint.proceed();
        }

        //获取redis hash fieldKey 表达式
        String redisHashFieldKeyExp = cacheForHash.field();
        String redisHashFieldKey = null;
        if (StringUtils.isNotEmpty(redisHashFieldKeyExp)) {
            //获取 redisFiledKey 值
            redisHashFieldKey = Objects.requireNonNull(spelExpressionParser.parseExpression(redisHashFieldKeyExp).getValue(evaluationContext)).toString();
        }

        //fieldKey 为空 则获取key Hash下所有缓存
        if (StringUtils.isEmpty(redisHashFieldKey)) {
            List<Object> objectList = redisTemplate.opsForHash().values(redisKey);

            //缓存获取为空则走数据库
            if (objectList == null || objectList.isEmpty()) {
                Object result = joinPoint.proceed();
                //缓存起来
                cache(result, redisKey);
                return result;
            }
            return parseToObjectList(objectList, cacheForHash.targetClass());
        }

        //redisKey 和 fieldKey 都不为空
        Object result = redisTemplate.opsForHash().get(redisKey, redisHashFieldKey);
        //如果结果不为空
        if (result != null) {
            return parseToObject(result, cacheForHash.targetClass());
        }
        //检测缓存是否存在 如果存在
        if (redisTemplate.hasKey(redisKey)) {
            return null;
        }
        //走数据库
        return joinPoint.proceed();
    }

    private String getRedisKey(Class targetClass) {
        CacheEntity cacheEntity = (CacheEntity) targetClass.getAnnotation(CacheEntity.class);
        if (cacheEntity == null) {
            throw new RuntimeException("实体没有CacheEntity注解!");
        }
        return cacheEntity.key();
    }

    /**
     * 解析成ObjectList
     *
     * @param objectList
     * @return
     */
    private List<Object> parseToObjectList(List<Object> objectList, Class targetClass) {
        return (List<Object>) objectList.stream().map(o -> JSONObject.parseObject((String) o, targetClass)).collect(Collectors.toList());
    }

    /**
     * 解析成Object
     *
     * @param object
     * @return
     */
    private Object parseToObject(Object object, Class targetClass) {
        return JSONObject.parseObject((String) object, targetClass);
    }


    /**
     * 将结果进行缓存
     *
     * @param result
     * @param redisKey
     */
    private void cache(Object result, String redisKey) throws RuntimeException {
        List list = (List) result;
        if (list == null || list.isEmpty()) {
            return;
        }
        Class clz = list.get(0).getClass();
        CacheEntity cacheEntity = (CacheEntity) clz.getAnnotation(CacheEntity.class);
        if (cacheEntity == null) {
            log.error("缓存实体没有 CacheEntity 注解!");
            return;
        }
        //获取组合key的连接符号
        String separator = cacheEntity.separator();

        //获取有CacheKey 注解的 order-field Map 映射
        Map<Integer, Field> orderFieldMap = getFields(clz);

        Map<String, String> resultMap = new HashMap<>();
        for (Object object : list) {
            resultMap.put(getFieldKey(object, orderFieldMap, separator), JSONObject.toJSONString(object));
        }
        redisTemplate.opsForHash().putAll(redisKey, resultMap);
    }

    /**
     * 获取fieldKey
     *
     * @param object        被缓存的对象
     * @param orderFieldMap
     * @param separator
     * @return
     */
    private String getFieldKey(Object object, Map<Integer, Field> orderFieldMap, String separator) {
        List<String> keys = new ArrayList<>();
        Collection<Field> fieldList = orderFieldMap.values();
        fieldList.forEach(field -> {
            try {
                keys.add((String) field.get(object));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return String.join(separator, keys);
    }

    /**
     * 获取所有带有CacheKey 的属性
     *
     * @param clz
     * @return
     */
    private Map<Integer, Field> getFields(Class clz) {
        Field[] fields = clz.getDeclaredFields();
        Map<Integer, Field> resultMap = new TreeMap<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(CacheField.class)) {
                CacheField cacheField = field.getAnnotation(CacheField.class);
                field.setAccessible(true);
                resultMap.put(cacheField.order(), field);
            }
        }
        return resultMap;
    }

    /**
     * 获取Method
     *
     * @param joinPoint 切点
     * @return 方法
     */
    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        return methodSignature.getMethod();
    }

    /**
     * 构建表达式上下文
     *
     * @param joinPoint 切点
     * @return 构建表达式上下文
     */
    private StandardEvaluationContext getStandardEvaluationContext(JoinPoint joinPoint, Object returnValue) {
        //构建表达式计算上下文
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext(joinPoint.getArgs());
        //将方法参数名和参数值放到系统上下文当中
        setContextVariables(evaluationContext, joinPoint, returnValue);
        return evaluationContext;
    }


    /**
     * 将方法参数名和参数值放到系统上下文当中
     *
     * @param joinPoint 切点
     */
    private void setContextVariables(StandardEvaluationContext standardEvaluationContext, JoinPoint joinPoint, Object result) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //获取方法参数名
        String[] paramNames = parameterNameDiscoverer.getParameterNames(methodSignature.getMethod());
        //获取参数值
        Object[] args = joinPoint.getArgs();

        if (paramNames == null || paramNames.length <= 0) {
            return;
        }
        //填充参数
        for (int i = 0; i < args.length; i++) {
            standardEvaluationContext.setVariable(paramNames[i], args[i]);
        }
        standardEvaluationContext.setVariable("result", result);
    }
}
