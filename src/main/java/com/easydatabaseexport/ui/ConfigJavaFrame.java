package com.easydatabaseexport.ui;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.enums.ConfigEnum;
import com.easydatabaseexport.enums.YesNoEnum;
import com.easydatabaseexport.util.FileIniRead;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.SwingUtils;
import lombok.SneakyThrows;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 配置UI
 *
 * @author lzy
 * @date 2021/12/1 15:06
 */
public class ConfigJavaFrame {

    public ConfigJavaFrame() {

    }

    public void configFrame() {
        JFrame jFrame = new JFrame("配置");
        jFrame.setSize(250, 250);
        jFrame.setResizable(false);
        jFrame.setLayout(new BorderLayout());
        JPanel jPanel = new JPanel();
        JLabel jLabel = new JLabel(ConfigEnum.INDEX.getValue());
        JRadioButton yesButton = new JRadioButton("是");
        JRadioButton noButton = new JRadioButton("否");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(yesButton);
        buttonGroup.add(noButton);
        //多sheet配置
        JLabel sheetLabel = new JLabel(ConfigEnum.SHEET.getValue());
        JRadioButton sheetYesButton = new JRadioButton("是");
        JRadioButton sheetNoButton = new JRadioButton("否");
        ButtonGroup sheetButtonGroup = new ButtonGroup();
        sheetButtonGroup.add(sheetYesButton);
        sheetButtonGroup.add(sheetNoButton);
        JPanel jPanel2 = new JPanel();
        JButton confirmButton = new JButton("保存");
        JButton closeButton = new JButton("关闭");

        //增加鼠标手形
        SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, confirmButton, closeButton,
                yesButton, noButton, sheetYesButton, sheetNoButton);

        jPanel2.add(confirmButton);
        jPanel2.add(closeButton);
        // 读取ini文件配置
        CommonConstant.initCheckAllKey(CommonConstant.EXPORT);
        // 是否导出索引
        if (YesNoEnum.YES_1.getValue().equals(CommonConstant.configMap.get(ConfigEnum.INDEX.getKey()))) {
            yesButton.setSelected(true);
        } else {
            noButton.setSelected(true);
        }
        // 是否多sheet
        if (YesNoEnum.YES_1.getValue().equals(CommonConstant.configMap.get(ConfigEnum.SHEET.getKey()))) {
            sheetYesButton.setSelected(true);
        } else {
            sheetNoButton.setSelected(true);
        }
        confirmButton.addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                String value;
                String sheetValue;
                if (yesButton.isSelected()) {
                    value = YesNoEnum.YES_1.getValue();
                } else {
                    value = YesNoEnum.NO_0.getValue();
                }
                if (sheetYesButton.isSelected()) {
                    sheetValue = YesNoEnum.YES_1.getValue();
                } else {
                    sheetValue = YesNoEnum.NO_0.getValue();
                }
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        ConfigEnum.INDEX.getKey(), value);
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        ConfigEnum.SHEET.getKey(), sheetValue);
                CommonConstant.checkConfigIniFile();
                JOptionPane.showMessageDialog(null, "保存成功！", "提醒", JOptionPane.PLAIN_MESSAGE);
                jFrame.dispose();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.dispose();
            }
        });
        jPanel.add(new JLabel("【Word】配置"));
        jPanel.add(new JLabel("<html></br>--------------------------------------------</br></html>"));
        jPanel.add(jLabel);
        jPanel.add(yesButton);
        jPanel.add(noButton);
        jPanel.add(new JLabel("【Excel】配置"));
        jPanel.add(new JLabel("<html></br>--------------------------------------------</br></html>"));
        jPanel.add(sheetLabel);
        jPanel.add(sheetYesButton);
        jPanel.add(sheetNoButton);
        jFrame.add(jPanel);
        jFrame.add(jPanel2, BorderLayout.SOUTH);
        //居中
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

}
