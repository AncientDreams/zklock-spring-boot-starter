package com.xiaoyu.zklock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * 分布式锁注解
 * </p>
 *
 * @author ZhangXianYu   Email: 1600501744@qq.com
 * @since 2020-04-06 13:15
 */
@Retention(value = RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Lock {

    /**
     * 超时时间，默认6秒超时释放锁！
     */
    int outTime() default 6000;

    /**
     * 锁的名称，请务必保证锁名称不重复
     */
    String lockName();
}
