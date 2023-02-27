package com.easydatabaseexport.util;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.ui.IndexJavaFrame;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import javax.swing.ImageIcon;
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
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SwingUtils
 *
 * @author lzy
 */
@Log
public class SwingUtils {

    private static Timer TIMER = null;
    private static JFrame TIMER_FRAME = null;
    private static final AtomicInteger SECONDS = new AtomicInteger(2);

    static class Work implements ActionListener {

        private final JLabel jLabel;

        public Work(JLabel jLabel) {
            this.jLabel = jLabel;
        }

        @SneakyThrows
        @Override
        public void actionPerformed(ActionEvent event) {
            jLabel.setText("正在重启中，倒计时 " + SECONDS.getAndDecrement() + " s......");
            if (SECONDS.get() == -2) {
                if (Objects.nonNull(TIMER_FRAME)) {
                    TIMER_FRAME.dispose();
                }
                TIMER.stop();
                //读取配置文件哦
                CommonConstant.index = Integer.parseInt(FileIniRead.getIniThemeIndex());
                SwingUtilities.invokeLater(() -> {
                    try {
                        CommonConstant.initByReboot();
                        IndexJavaFrame.connectFrame();
                    } catch (Exception e) {
                        LogManager.writeLogFile(e, log);
                    }
                });
                SECONDS.set(2);
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
            if (Objects.nonNull(jFrame)) {
                jFrame.dispose();
            }
            if (Objects.nonNull(mainFrame)) {
                mainFrame.dispose();
            }
            JPanel jPanel = new JPanel(new BorderLayout());
            JLabel jLabel = new JLabel("", JLabel.CENTER);
            jPanel.add(jLabel, BorderLayout.CENTER);
            TIMER_FRAME = new JFrame();
            SwingUtils.changeLogo(TIMER_FRAME);
            TIMER_FRAME.add(jPanel);
            TIMER_FRAME.setTitle("重启");
            TIMER_FRAME.setSize(350, 100);
            TIMER_FRAME.setLocationRelativeTo(null);
            TIMER_FRAME.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            TIMER_FRAME.setResizable(false);
            TIMER_FRAME.setVisible(true);
            TIMER = new Timer(1000, new Work(jLabel));
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

    @SneakyThrows
    public static void changeLogo(JFrame frame) {
        //更换logo
        URL url = IndexJavaFrame.class.getResource("/images/logo.png");
        if (Objects.isNull(url)) {
            throw new RuntimeException("找不到logo图标！");
        }
        ImageIcon arrowIcon = new ImageIcon(url);
        frame.setIconImage(arrowIcon.getImage());
    }
}
