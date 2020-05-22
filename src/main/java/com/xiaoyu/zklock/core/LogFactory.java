package com.xiaoyu.zklock.core;

import org.apache.commons.logging.Log;

/**
 * <p>
 * 自定义日志工厂，方便判断是否需要开启日志的输入！
 * 实现次接口就能打印日志信息。
 * </p>
 *
 * @author ZhangXianYu   Email: 1600501744@qq.com
 * @since 2020-05-21 9:32
 */
public interface LogFactory {


    /**
     * info
     *
     * @param logStr 日志
     */
    default void info(Object logStr) {
        Log log = setLog();
        if (log != null) {
            log.info(logStr);
        }
    }


    /**
     * error
     *
     * @param logStr 日志
     */
    default void error(Object logStr) {
        Log log = setLog();
        if (log != null) {
            log.error(logStr);
        }
    }

    /**
     * error
     *
     * @param var1 日志
     * @param var2 Throwable
     */
    default void error(Object var1, Throwable var2) {
        Log log = setLog();
        if (log != null) {
            log.error(var1, var2);
        }
    }

    /**
     * warn
     *
     * @param logStr 日志
     */
    default void warn(Object logStr) {
        Log log = setLog();
        if (log != null) {
            log.warn(logStr);
        }
    }

    /**
     * debug
     *
     * @param logStr 日志
     */
    default void debug(Object logStr) {
        Log log = setLog();
        if (log != null) {
            log.debug(logStr);
        }
    }

    /**
     * 获取类的日志对象
     *
     * @return 日志
     */
    Log setLog();
}
