package com.easydatabaseexport.ui;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.enums.UpdateEnum;
import com.easydatabaseexport.enums.YesNoEnum;
import com.easydatabaseexport.util.FileIniRead;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.OpenUrl;
import com.easydatabaseexport.util.SwingUtils;
import lombok.SneakyThrows;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * UpdateVersionFrame
 *
 * @author lzy
 * @date 2022/5/20 13:40
 **/
public class UpdateVersionFrame {

    public UpdateVersionFrame() {

    }

    public void updateVersionFrame(String newestVersion) {
        String versionIllustrate = FileOperateUtil.getRemoteFile("https://gitee.com/lzy549876/EasyDataBaseExport/raw/main/version.txt");
        JFrame jFrame = new JFrame("更新");
        SwingUtils.changeLogo(jFrame);
        jFrame.setSize(350, 380);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jFrame.setResizable(false);
        jFrame.setLayout(new BorderLayout());
        JPanel jPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane();
        JTextArea jTextArea = new JTextArea("检测到有新的版本可用！\n版本特点：\n " + versionIllustrate.replaceAll("\\\\n", "\n"));
        jTextArea.setWrapStyleWord(true);
        jTextArea.setFont(new Font(null, Font.PLAIN, 15));
        jTextArea.setLineWrap(true);
        jTextArea.setCaretPosition(0);
        jTextArea.setEditable(false);

        scrollPane.setViewportView(jTextArea);
        scrollPane.setPreferredSize(new Dimension(300, 300));

        JPanel button = new JPanel();
        JButton downLoadButton = new JButton("下载");
        JButton ignoreButton = new JButton("忽略当前更新");
        JButton closeButton = new JButton("永久关闭提醒");

        button.add(downLoadButton);
        button.add(ignoreButton);
        button.add(closeButton);

        ignoreButton.setToolTipText("只忽略当前最新的版本，下次版本更新后，继续提醒");
        closeButton.setToolTipText("永远关闭，以后版本更新将不再提醒");
        SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, downLoadButton, ignoreButton, closeButton);
        // 读取ini文件配置
        CommonConstant.initCheckAllKey(CommonConstant.UPDATE);

        downLoadButton.addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                OpenUrl.openURL("https://gitee.com/lzy549876/EasyDataBaseExport");
                jFrame.dispose();
            }
        });

        ignoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String value = newestVersion;
                if (CommonConstant.configMap.containsKey(UpdateEnum.VERSIONS.getKey())) {

                    value = (CommonConstant.configMap.get(UpdateEnum.VERSIONS.getKey()).isEmpty() ? "" :
                            CommonConstant.configMap.get(UpdateEnum.VERSIONS.getKey()) + ",") + newestVersion;
                }
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        UpdateEnum.VERSIONS.getKey(), value);
                CommonConstant.checkConfigIniFile();
                jFrame.dispose();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String value = YesNoEnum.NO_0.getValue();
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        UpdateEnum.UPDATE_VERSION.getKey(), value);
                CommonConstant.checkConfigIniFile();
                jFrame.dispose();
            }
        });
        jPanel.add(scrollPane);
        jFrame.add(jPanel);
        jFrame.add(button, BorderLayout.SOUTH);
        //居中
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

    /*public static void main(String[] args) {
        UpdateVersionFrame updateVersionFrame = new UpdateVersionFrame();
        updateVersionFrame.updateVersionFrame();
    }*/

}
