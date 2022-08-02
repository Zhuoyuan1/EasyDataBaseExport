package com.easydatabaseexport.ui.component;

import com.easydatabaseexport.ui.UploadSynFrame;
import com.easydatabaseexport.util.CheckUpdateUtil;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Menu
 *
 * @author lzy
 * @date 2021/5/16 14:28
 **/
public class IndexMenu {

    public static JMenuBar IndexMenu(JFrame mainJFrame) {
        /*
         * 创建一个菜单栏
         */
        JMenuBar menuBar = new JMenuBar();

        /*
         * 创建一级菜单
         */
        JMenu fileMenu = new JMenu("操作");
        // 一级菜单添加到菜单栏
        menuBar.add(fileMenu);

        /*
         * 创建 "文件" 一级菜单的子菜单
         */
        JMenuItem importNv = new JMenuItem("导入连接...");
        JMenuItem updateItem = new JMenuItem("检查更新");
        JMenuItem exitMenuItem = new JMenuItem("退出");

        fileMenu.add(importNv);
        fileMenu.add(updateItem);
        fileMenu.addSeparator();       // 添加一条分割线
        fileMenu.add(exitMenuItem);
        // 设置 "退出" 子菜单被点击的监听器
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        importNv.addActionListener(e -> {
            UploadSynFrame uploadSynFrame = new UploadSynFrame();
            uploadSynFrame.UpLoadFile(mainJFrame);
        });
        updateItem.addActionListener(e -> CheckUpdateUtil.checkByClick());
        return menuBar;

    }
}
