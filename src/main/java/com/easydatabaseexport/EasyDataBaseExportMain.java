package com.easydatabaseexport;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.PatternConstant;
import com.easydatabaseexport.exception.ExceptionHandler;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.ui.IndexJavaFrame;
import com.easydatabaseexport.util.AESCoder;
import com.easydatabaseexport.util.FileIniRead;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.util.Enumeration;

/**
 * EasyDataBaseExportMain
 *
 * @author lzy
 * @date 2021/11/1 14:54
 **/
@Log4j
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
        //字体设置
        //initGlobalFont(new Font("alias", Font.BOLD, 13));
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

    /**
     * 统一设置字体，父界面设置之后，所有由父界面进入的子界面都不需要再次设置字体
     *
     * @param font 字体
     **/
    private static void initGlobalFont(Font font) {
        FontUIResource fontRes = new FontUIResource(font);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }
    }

}
