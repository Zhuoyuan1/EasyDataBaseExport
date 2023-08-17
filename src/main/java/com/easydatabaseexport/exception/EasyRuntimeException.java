package com.easydatabaseexport.exception;

import com.easydatabaseexport.log.LogManager;
import org.apache.log4j.Logger;

/**
 * describe :
 *
 * @author : Jia wei Wu
 * @version 1.0
 * @date : 2022/6/30 19:19
 */
public class EasyRuntimeException implements GlobalException {

    private static final Logger log = LogManager.getLogger(EasyRuntimeException.class);

    /**
     * 是否支持
     *
     * @param exception 异常
     * @return
     */
    @Override
    public boolean support(Throwable exception) {
        return exception != null;
    }

    /**
     * 异常处理
     *
     * @param t
     * @param exception
     */
    @Override
    public void handler(Thread t, Throwable exception) {
        LogManager.genLogStr(exception, log);
    }
}
