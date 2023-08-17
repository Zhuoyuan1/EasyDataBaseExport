package com.easydatabaseexport.log;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.EnvironmentConstant;
import com.easydatabaseexport.enums.LevelEnum;
import com.easydatabaseexport.util.FileOperateUtil;
import org.apache.log4j.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * LogManager
 *
 * @author lzy
 * @date 2022/6/30 16:15
 **/
public class LogManager {

    /**
     * 获取日志
     *
     * @param clazz class
     * @return org.apache.log4j.Logger
     **/
    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }

    /**
     * 写入日志文件
     *
     * @param exception 异常
     * @param logger    日志
     **/
    public static void writeLogFile(Exception exception, Logger logger) {
        genLogStr(exception, logger);
    }

    public static void genLogStr(Throwable exception, Logger logger) {
        StringWriter trace = new StringWriter();
        exception.printStackTrace(new PrintWriter(trace));
        logger.error(trace.toString());
        if (CommonConstant.configMap.containsKey(EnvironmentConstant.RUN_LEVEL) && CommonConstant.configMap.get(EnvironmentConstant.RUN_LEVEL).equals(LevelEnum.DEV.getLevel())) {
            File file = new File(System.getProperty("user.home"));
            SwingUtilities.invokeLater(() -> {
                int n = JOptionPane.showConfirmDialog(null, "出现错误！\n错误日志已保存到：./logs/edbe_error_log.log\n是否立即打开目录查看日志？"
                        , "错误", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                if (n == JOptionPane.YES_OPTION) {
                    FileOperateUtil.open(file);
                }
            });
        }
    }
}
