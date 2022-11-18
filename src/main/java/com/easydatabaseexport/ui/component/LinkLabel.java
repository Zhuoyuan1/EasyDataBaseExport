package com.easydatabaseexport.ui.component;

import com.easydatabaseexport.util.OpenUrl;
import lombok.SneakyThrows;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 超链接
 *
 * @author lzy
 */
public class LinkLabel extends JLabel {
    private final String text;
    private final String url;
    private boolean isSupported;

    public LinkLabel(String text, String url) {
        this.text = text;
        this.url = url;
        try {
            this.isSupported = Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE);
        } catch (Exception e) {
            this.isSupported = false;
        }
        setText(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setText(isSupported);
                if (isSupported) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setText(false);
            }

            @SneakyThrows
            @Override
            public void mouseClicked(MouseEvent e) {
                OpenUrl.openURL(LinkLabel.this.url);
            }
        });
    }

    private void setText(boolean b) {
        if (!b) {
            setText("<html><font color=blue><u>" + text);
        } else {
            setText("<html><font color=red><u>" + text);
        }
    }

    public static void main(String[] args) {
        JFrame jf = new JFrame();
        JPanel jp = new JPanel();
        jp.add(new LinkLabel("百度一下", "https://www.baidu.com"));
        jf.setContentPane(jp);
        jf.pack();
        jf.setVisible(true);
    }
}
