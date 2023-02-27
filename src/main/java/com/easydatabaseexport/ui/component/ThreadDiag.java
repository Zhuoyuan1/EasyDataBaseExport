package com.easydatabaseexport.ui.component;

import com.easydatabaseexport.util.SwingUtils;
import lombok.SneakyThrows;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

/**
 * @author lzy
 * @date 2021/7/8 18:10
 **/
public class ThreadDiag extends Thread {
    /**
     * 实际调用时就是TestThread事务处理线程
     */
    private Thread currentThread = null;
    /**
     * 提示框的提示信息
     */
    private String messages = "";
    /**
     * 提示框的父窗体
     */
    private JFrame parentFrame = null;
    /**
     * “线程正在运行”提示框
     */
    private JDialog clueDiag = null;

    public ThreadDiag(JFrame parentFrame, Thread currentThread, String messages) {
        this.parentFrame = parentFrame;
        this.currentThread = currentThread;
        this.messages = messages;
        SwingUtils.changeLogo(parentFrame);
        //初始化提示框
        initDiag();
    }

    protected void initDiag() {
        clueDiag = new JDialog(parentFrame, null, true);
        clueDiag.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        //关闭按钮失效
        clueDiag.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        URL wait = ThreadDiag.class.getResource("/images/wait.gif");
        Image image = Toolkit.getDefaultToolkit().getImage(wait);

        JPanel testPanel = new JPanel(new BorderLayout()) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.drawImage(image, 35, 15, this);
            }
        };
        JLabel testLabel = new JLabel(messages, JLabel.CENTER);
        testPanel.add(testLabel, BorderLayout.SOUTH);
        clueDiag.getContentPane().add(testPanel);
        //启动关闭提示框线程
        (new DisposeDiag()).start();
    }


    @Override
    public void run() {
        //显示提示框
        clueDiag.setSize(new Dimension(200, 80));
        clueDiag.setResizable(false);
        clueDiag.setLocationRelativeTo(null);
        clueDiag.setUndecorated(true);
        clueDiag.setVisible(true);
    }

    class DisposeDiag extends Thread {
        @SneakyThrows
        @Override
        public void run() {
            //等待事务处理线程结束
            currentThread.join();
            //关闭提示框
            clueDiag.dispose();
        }
    }
}
