package com.easydatabaseexport;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.PatternConstant;
import com.easydatabaseexport.exception.ExceptionHandler;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.ui.IndexJavaFrame;
import com.easydatabaseexport.util.AESCoder;
import com.easydatabaseexport.util.FileIniRead;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * EasyDataBaseExportMain
 *
 * @author lzy
 * @date 2021/11/1 14:54
 **/
@Log
public class EasyDataBaseExportMain {

    static {
        try {
            AESCoder.initKeyAndEndurance();
            //初始化ini文件
            CommonConstant.copySystemIniFile();
            //生成模板文件
            CommonConstant.copyTemplateFile();
            //检查ini配置
            CommonConstant.checkConfigIniFile();
            //改变参数
            PatternConstant.reCheckConfig();
        } catch (Exception e) {
            LogManager.writeLogFile(e, log);
        }
    }

    /**
     * 程序入口
     **/
    @SneakyThrows
    public static void main(String[] args) {
        //允许修改JFrame的标题栏
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        //启用跨平台的外观
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        //读取配置文件哦
        CommonConstant.index = Integer.parseInt(FileIniRead.getIniThemeIndex());
        SwingUtilities.invokeLater(() -> {
            try {
                if (CommonConstant.index >= 0 && CommonConstant.index < CommonConstant.THEMES.length) {
                    UIManager.setLookAndFeel(CommonConstant.THEMES[CommonConstant.index]);
                }
                IndexJavaFrame.connectFrame();
            } catch (Exception e) {
                LogManager.writeLogFile(e, log);
            }
        });
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
    }

}
