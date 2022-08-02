package com.easydatabaseexport.log;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

/**
 * LogManager
 *
 * @author lzy
 * @date 2022/6/30 16:15
 **/
public class LogManager {

    static {
        try {
            // 读取配置文件
            InputStream inputStream = ClassLoader.getSystemResourceAsStream("log.properties");
            java.util.logging.LogManager logManager = java.util.logging.LogManager.getLogManager();
            // 重新初始化日志属性并重新读取日志配置。
            logManager.readConfiguration(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取日志
     *
     * @param clazz class
     * @return java.util.logging.Logger
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
        StringWriter trace = new StringWriter();
        exception.printStackTrace(new PrintWriter(trace));
        logger.severe(trace.toString());
    }
}
