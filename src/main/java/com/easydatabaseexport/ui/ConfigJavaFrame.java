package com.easydatabaseexport.ui;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.PatternConstant;
import com.easydatabaseexport.enums.ConfigEnum;
import com.easydatabaseexport.enums.YesNoEnum;
import com.easydatabaseexport.util.FileIniRead;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.StringUtil;
import com.easydatabaseexport.util.SwingUtils;
import com.mysql.cj.util.StringUtils;
import lombok.SneakyThrows;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

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
        jFrame.setSize(500, 250);
        //jFrame.setResizable(false);
        jFrame.setLayout(new BorderLayout());
        JCheckBox yesButton = new JCheckBox(ConfigEnum.INDEX.getValue());
        //多sheet配置
        JCheckBox sheetYesButton = new JCheckBox(ConfigEnum.SHEET.getValue());
        JPanel jPanel2 = new JPanel();
        JButton confirmButton = new JButton("保存");
        JButton closeButton = new JButton("关闭");

        //增加鼠标手形
        SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, confirmButton, closeButton);

        jPanel2.add(confirmButton);
        jPanel2.add(closeButton);
        // 读取ini文件配置
        CommonConstant.initCheckAllKey(CommonConstant.EXPORT);
        // 是否导出索引
        yesButton.setSelected(YesNoEnum.YES_1.getValue().equals(CommonConstant.configMap.get(ConfigEnum.INDEX.getKey())));
        // 是否多sheet
        sheetYesButton.setSelected(YesNoEnum.YES_1.getValue().equals(CommonConstant.configMap.get(ConfigEnum.SHEET.getKey())));
        //赋值
        String indexHead = CommonConstant.configMap.get(ConfigEnum.INDEX_TABLE_HEAD.getKey());
        String tableHead = CommonConstant.configMap.get(ConfigEnum.TABLE_HEAD.getKey());
        JSplitPane allTotalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane totalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane totalSplitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
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
                StringBuilder tableHead = new StringBuilder();
                StringBuilder indexTableHead = new StringBuilder();

                Component component1 = totalSplitPane2.getTopComponent();
                if (component1 instanceof JPanel) {
                    for (Component index : ((JPanel) component1).getComponents()) {
                        if (index instanceof JCheckBox) {
                            JCheckBox jCheckBox = (JCheckBox) index;
                            if (jCheckBox.isSelected()) {
                                indexTableHead.append(PatternConstant.MD_SPLIT).append(jCheckBox.getText());
                            }
                        }
                    }
                }
                indexTableHead.append(PatternConstant.MD_SPLIT);

                Component component = totalSplitPane2.getBottomComponent();
                if (component instanceof JPanel) {
                    for (Component index : ((JPanel) component).getComponents()) {
                        if (index instanceof JCheckBox) {
                            JCheckBox jCheckBox = (JCheckBox) index;
                            if (jCheckBox.isSelected()) {
                                tableHead.append(PatternConstant.MD_SPLIT).append(jCheckBox.getText());
                            }
                        }
                    }
                }
                tableHead.append(PatternConstant.MD_SPLIT);

                System.out.println(tableHead);
                System.out.println(indexTableHead);
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        ConfigEnum.INDEX.getKey(), value);
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        ConfigEnum.SHEET.getKey(), sheetValue);
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        ConfigEnum.TABLE_HEAD.getKey(), tableHead.toString().length() == 1 ? "-1" : tableHead.toString());
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        ConfigEnum.INDEX_TABLE_HEAD.getKey(), indexTableHead.toString().length() == 1 ? "-1" : indexTableHead.toString());
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
        allTotalSplitPane.setDividerSize(0);
        totalSplitPane.setDividerSize(0);
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("【Word、Markdown、Html、Pdf】"));
        topPanel.add(yesButton);
        totalSplitPane.setTopComponent(topPanel);
        JPanel topPanel1 = new JPanel();
        topPanel1.add(new JLabel("【Excel】"));
        topPanel1.add(sheetYesButton);
        totalSplitPane.setBottomComponent(topPanel1);
        allTotalSplitPane.setTopComponent(totalSplitPane);

        totalSplitPane2.setDividerSize(0);
        JPanel header = new JPanel();
        header.add(new JLabel("【索引表头】"));
        //添加索引表头
        Arrays.stream(CommonConstant.INDEX_HEAD_NAMES).filter(StringUtil::isNotEmpty).forEach(v -> {
            JCheckBox head = new JCheckBox(v);
            head.setSelected(indexHead.contains(v));
            header.add(ConfigEnum.INDEX_TABLE_HEAD.getKey(), head);
        });
        totalSplitPane2.setTopComponent(header);

        JPanel header2 = new JPanel();
        header2.add(new JLabel("【字段表头】"));
        //添加字段表头
        Arrays.stream(CommonConstant.COLUMN_HEAD_NAMES).filter(StringUtil::isNotEmpty).forEach(v -> {
            JCheckBox head = new JCheckBox(v);
            head.setSelected(tableHead.contains(v));
            header2.add(ConfigEnum.TABLE_HEAD.getKey(), head);
        });
        totalSplitPane2.setBottomComponent(header2);

        allTotalSplitPane.setTopComponent(totalSplitPane);
        allTotalSplitPane.setBottomComponent(totalSplitPane2);
        totalSplitPane.setEnabled(false);
        totalSplitPane2.setEnabled(false);
        allTotalSplitPane.setEnabled(false);
        jFrame.add(allTotalSplitPane);
        jFrame.add(jPanel2, BorderLayout.SOUTH);
        //居中
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

    public static void main(String[] args) {
        ConfigJavaFrame v = new ConfigJavaFrame();
        v.configFrame();
    }

}
