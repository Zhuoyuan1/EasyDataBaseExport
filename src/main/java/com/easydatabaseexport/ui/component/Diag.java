package com.easydatabaseexport.ui.component;

import com.easydatabaseexport.util.SwingUtils;

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
import java.util.concurrent.CompletableFuture;

/**
 * Diag
 *
 * @author lzy
 * @date 2023/2/22 15:56
 **/
public class Diag {

    public Diag(CompletableFuture<Boolean> completableFuture, String message) {
        JFrame jFrame = new JFrame();
        SwingUtils.changeLogo(jFrame);
        JDialog clueDiag = new JDialog(jFrame, null, true);
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
                if (completableFuture.isDone()) {
                    clueDiag.dispose();
                }
            }
        };
        JLabel testLabel = new JLabel(message + "，请等待......", JLabel.CENTER);
        testPanel.add(testLabel, BorderLayout.SOUTH);
        clueDiag.getContentPane().add(testPanel);
        //显示提示框
        clueDiag.setSize(new Dimension(200, 80));
        clueDiag.setLocationRelativeTo(null);
        clueDiag.setResizable(false);
        clueDiag.setUndecorated(true);
        clueDiag.setVisible(true);
    }
}
