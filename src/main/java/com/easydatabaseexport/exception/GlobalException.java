package com.easydatabaseexport.exception;

/**
 * describe : 全局异常
 *
 * @author : Jia wei Wu
 * @version 1.0
 * @date : 2022/6/30 19:19
 */
public interface GlobalException {

    /**
     * 是否支持
     *
     * @param exception 异常
     * @return
     */
    boolean support(Throwable exception);

    /**
     * 异常处理
     *
     * @param t
     * @param exception
     */
    void handler(Thread t, Throwable exception);
}
