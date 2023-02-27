package com.easydatabaseexport.ui.component;

import com.easydatabaseexport.ui.ConfigJavaFrame;
import com.easydatabaseexport.util.CheckUpdateUtil;
import com.easydatabaseexport.util.OpenUrl;
import com.easydatabaseexport.util.SwingUtils;
import lombok.SneakyThrows;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Menu
 *
 * @author lzy
 * @date 2021/5/16 14:28
 **/
public class CustomMenu {

    public static JMenuBar CustomMenu() {
        /*
         * 创建一个菜单栏
         */
        JMenuBar menuBar = new JMenuBar();

        /*
         * 创建一级菜单
         */
        JMenu fileMenu = new JMenu("操作");
        JMenu themeMenu = new JMenu("主题");
        JMenu exportMenu = new JMenu("导出");

        // 一级菜单添加到菜单栏
        menuBar.add(fileMenu);
        menuBar.add(themeMenu);
        menuBar.add(exportMenu);
        addAboutBar(menuBar);

        /*
         * 创建 "文件" 一级菜单的子菜单
         */
        JMenuItem closeMenu = new JMenuItem("关闭连接");
        JMenuItem importNv = new JMenuItem("导入连接...");
        JMenuItem editNv = new JMenuItem("编辑连接...");
        JMenuItem exitMenuItem = new JMenuItem("退出");
        // 子菜单添加到一级菜单
        fileMenu.add(closeMenu);
        fileMenu.add(importNv);
        fileMenu.add(editNv);
        fileMenu.addSeparator();       // 添加一条分割线
        fileMenu.add(exitMenuItem);

        JMenuItem theme1 = new JMenuItem("商务蓝");
        JMenuItem theme2 = new JMenuItem("沙漠黄");
        JMenuItem theme3 = new JMenuItem("翡翠绿");
        JMenuItem theme4 = new JMenuItem("淡雅青");
        JMenuItem theme5 = new JMenuItem("石墨黑");
        JMenuItem theme6 = new JMenuItem("深海蓝");
        JMenuItem theme7 = new JMenuItem("薄雾灰");
        JMenuItem theme8 = new JMenuItem("温和灰");
        JMenuItem theme9 = new JMenuItem("星云黄");
        JMenuItem theme10 = new JMenuItem("星云青");
        JMenuItem theme11 = new JMenuItem("Office蓝 2007");
        JMenuItem theme12 = new JMenuItem("Office银 2007");
        JMenuItem theme13 = new JMenuItem("玻璃青");
        JMenuItem theme14 = new JMenuItem("黄昏黑");
        JMenuItem theme15 = new JMenuItem("还原默认");

        themeMenu.add(theme1);
        themeMenu.add(theme2);
        themeMenu.add(theme3);
        themeMenu.add(theme4);
        themeMenu.add(theme5);
        themeMenu.add(theme6);
        themeMenu.add(theme7);
        themeMenu.add(theme8);
        themeMenu.add(theme9);
        themeMenu.add(theme10);
        themeMenu.add(theme11);
        themeMenu.add(theme12);
        themeMenu.add(theme13);
        themeMenu.add(theme14);
        themeMenu.addSeparator();
        themeMenu.add(theme15);

        JMenuItem exportExcel = new JMenuItem("生成Excel");
        JMenuItem exportWord = new JMenuItem("生成Word");
        JMenuItem exportMarkdown = new JMenuItem("生成Markdown");
        JMenuItem exportHtml = new JMenuItem("生成Html");
        JMenuItem exportPdf = new JMenuItem("生成Pdf");
        JMenuItem configMenu = new JMenuItem("配置...");

        exportMenu.add(exportExcel);
        exportMenu.add(exportWord);
        exportMenu.add(exportMarkdown);
        exportMenu.add(exportHtml);
        exportMenu.add(exportPdf);
        exportMenu.addSeparator();
        exportMenu.add(configMenu);

        // 设置 "退出" 子菜单被点击的监听器
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // 打开配置页
        configMenu.addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> {
                    ConfigJavaFrame configJavaFrame = new ConfigJavaFrame();
                    configJavaFrame.configFrame();
                });
            }
        });

        return menuBar;

    }

    public static JMenuBar addAboutBar(JMenuBar mainBar) {
        JMenu aboutMenu = new JMenu("帮助");
        /*
         * 创建 "关于" 一级菜单的子菜单
         */
        JMenuItem aboutItem = new JMenuItem("关于");
        JMenuItem updateItem = new JMenuItem("检查更新");
        aboutMenu.add(updateItem);
        aboutMenu.addSeparator();
        aboutMenu.add(aboutItem);
        mainBar.add(aboutMenu);

        // 设置 复选框子菜单 状态改变 监听器
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame jFrame = new JFrame("关于");
                SwingUtils.changeLogo(jFrame);
                jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                jFrame.setResizable(false);
                JButton supportButton = new JButton("支持作者");
                JButton aboutButton = new JButton("关于工具");
                String stringBuilder = "<html>声明：" +
                        "<p>&nbsp;&nbsp;&nbsp;&nbsp;本工具主要辅助开发人员快速、高效形成<br>数据库设计文档，同时也方便用户快速<br>查看表结构和生成文档。" +
                        "<br><font size=4 color=red>【版权归作者所有，切勿商业用途】</font></html>";
                String str = "作者：像风一样   qq: 963565242";
                JLabel js = new JLabel(stringBuilder);
                JLabel js1 = new JLabel(str);
                JPanel jPanel = new JPanel();
                JPanel jPanel1 = new JPanel();
                jPanel.add(js);
                jPanel.add(js1);
                jPanel1.add(supportButton);
                jPanel1.add(aboutButton);
                jPanel.add(jPanel1);
                jFrame.add(jPanel);
                jFrame.setSize(300, 210);
                jFrame.setLocationRelativeTo(null);
                jFrame.setVisible(true);
                ActionListener aboutListener = e1 -> {
                    OpenUrl.openURL("https://www.likethewind.top");
                };
                aboutButton.addActionListener(aboutListener);
                ActionListener supportListener = e1 -> {
                    JFrame j = new JFrame("支持");
                    SwingUtils.changeLogo(j);
                    j.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    JButton starButton = new JButton("Gitee to Star");
                    JButton clickButton = new JButton("CSDN博客一键三连");
                    JButton close = new JButton("全部关闭");
                    JPanel jPanel2 = new JPanel();
                    jPanel2.add(starButton);
                    jPanel2.add(clickButton);
                    jPanel2.add(close);
                    //增加鼠标手形
                    SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, starButton, clickButton, close);
                    JLabel text = new JLabel("<html><ol><li><em>Star</em> 并 分享这个项目 \uD83D\uDE80</li>" +
                            "<li>CSDN博客一键三连 \uD83D\uDE04</li>" +
                            "<li>通过以下二维码一次性捐款。 我多半会买一杯 <s>咖啡</s> 茶。\uD83C\uDF75</li></ol></html>");
                    //添加按钮监听
                    starButton.addActionListener(l -> OpenUrl.openURL("https://gitee.com/lzy549876/EasyDataBaseExport"));
                    clickButton.addActionListener(l -> OpenUrl.openURL("https://blog.csdn.net/kkk123445/article/details/115748954"));
                    close.addActionListener(l -> {
                        jFrame.dispose();
                        j.dispose();
                    });
                    ImageIcon wxzsmIcon = new ImageIcon(new ImageIcon(CustomMenu.class.getResource("/images/wxzsm.png")).getImage().getScaledInstance(200, 200, Image.SCALE_DEFAULT));
                    ImageIcon zfbIcon = new ImageIcon(new ImageIcon(CustomMenu.class.getResource("/images/zfb.png")).getImage().getScaledInstance(200, 200, Image.SCALE_DEFAULT));
                    JLabel jLabel = new JLabel(wxzsmIcon);
                    JLabel jLabel2 = new JLabel(zfbIcon);
                    JPanel jp = new JPanel();
                    jp.add(text);
                    jp.add(jLabel);
                    jp.add(jLabel2);
                    j.add(jp);
                    j.add(jPanel2, BorderLayout.SOUTH);
                    j.setSize(500, 380);
                    j.setLocationRelativeTo(null);
                    j.setResizable(false);
                    j.setVisible(true);
                };
                supportButton.addActionListener(supportListener);
                SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, supportButton, aboutButton);
            }
        });

        updateItem.addActionListener(e -> CheckUpdateUtil.checkByClick());

        return mainBar;
    }
}
