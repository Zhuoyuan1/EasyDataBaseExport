package com.easydatabaseexport.util;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.ui.IndexJavaFrame;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SwingUtils
 *
 * @author lzy
 */
@Log
public class SwingUtils {

    private static final Timer TIMER = new Timer(1000, new Work());
    private static final JFrame TIMER_FRAME = new JFrame();
    private static final JLabel J_LABEL = new JLabel("", JLabel.CENTER);
    private static final AtomicInteger SECONDS = new AtomicInteger(2);

    static class Work implements ActionListener {
        @SneakyThrows
        @Override
        public void actionPerformed(ActionEvent event) {
            J_LABEL.setText("");
            J_LABEL.setText("正在重启中，倒计时 " + SECONDS.getAndDecrement() + " s......");
            if (SECONDS.get() == -2) {
                TIMER_FRAME.dispose();
                TIMER.stop();
                //读取配置文件哦
                CommonConstant.index = Integer.parseInt(FileIniRead.getIniThemeIndex());
                SwingUtilities.invokeLater(() -> {
                    try {
                        CommonConstant.initByReboot();
                        SECONDS.set(2);
                        TIMER_FRAME.setTitle("正在重启中，倒计时 " + SECONDS.getAndDecrement() + " s......");
                        IndexJavaFrame.connectFrame();
                    } catch (Exception e) {
                        LogManager.writeLogFile(e, log);
                    }
                });
            }
        }
    }

    /**
     * 重启
     *
     * @param jFrame    当前页面
     * @param mainFrame 主页面
     */
    public static void rebootFrame(String msg, String title, JFrame jFrame, JFrame mainFrame) {
        int n = JOptionPane.showConfirmDialog(null, msg + "，是否立即重启？", title, JOptionPane.YES_NO_OPTION);
        if (n == 0) {
            if (null != jFrame) {
                jFrame.dispose();
            }
            if (null != mainFrame) {
                mainFrame.dispose();
            }
            JPanel jPanel = new JPanel(new BorderLayout());
            jPanel.add(J_LABEL, BorderLayout.CENTER);
            TIMER_FRAME.add(jPanel);
            TIMER_FRAME.setTitle("重启");
            TIMER_FRAME.setSize(350, 100);
            TIMER_FRAME.setLocationRelativeTo(null);
            TIMER_FRAME.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            TIMER_FRAME.setResizable(false);
            TIMER_FRAME.setVisible(true);
            TIMER.start();//开启定时器
        }
    }

    /**
     * 批量增加鼠标移入监听
     *
     * @param components 组件
     * @return void
     **/
    public static void addHandCursorLister(int type, Component... components) {
        //是否支持
        boolean isSupported = Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
        if (isSupported) {
            for (Component component : components) {
                component.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        component.setCursor(new Cursor(type));
                    }
                });
            }
        }
    }
}
