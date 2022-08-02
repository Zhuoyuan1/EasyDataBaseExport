package com.easydatabaseexport.exception;

import java.util.Collections;
import java.util.List;

/**
 * describe :
 *
 * @author : Jia wei Wu
 * @version 1.0
 * @date : 2022/6/30 19:18
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static List<GlobalException> globalExceptionList = Collections.singletonList(new EasyRuntimeException());

    /**
     * describe 异常处理器
     *
     * @param exception 异常
     * @return
     * @throws
     * @author Jia wei Wu
     * @date 2022/6/30 19:26
     **/
    public static void handler(Thread t, Throwable exception) {
        for (GlobalException globalException : globalExceptionList) {
            if (globalException.support(exception)) {
                globalException.handler(t, exception);
            }
        }
    }

    /**
     * Method invoked when the given thread terminates due to the
     * given uncaught exception.
     * <p>Any exception thrown by this method will be ignored by the
     * Java Virtual Machine.
     *
     * @param t the thread
     * @param e the exception
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        handler(t, e);
    }
}
