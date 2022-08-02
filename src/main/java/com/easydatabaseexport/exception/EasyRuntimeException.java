package com.easydatabaseexport.exception;

import com.easydatabaseexport.common.EnvironmentConstant;
import com.easydatabaseexport.enums.LevelEnum;
import com.easydatabaseexport.log.LogManager;

import javax.swing.JOptionPane;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

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
        return Exception.class.isAssignableFrom(exception.getClass());
    }

    /**
     * 异常处理
     *
     * @param t
     * @param exception
     */
    @Override
    public void handler(Thread t, Throwable exception) {
        StringWriter trace = new StringWriter();
        exception.printStackTrace(new PrintWriter(trace));
        log.severe(trace.toString());
        if (LevelEnum.DEV.getLevel().equals(EnvironmentConstant.RUN_LEVEL)) {
            JOptionPane.showMessageDialog(null, trace.toString(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
