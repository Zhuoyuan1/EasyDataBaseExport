package com.easydatabaseexport.ui;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.PatternConstant;
import com.easydatabaseexport.enums.ConfigEnum;
import com.easydatabaseexport.enums.YesNoEnum;
import com.easydatabaseexport.util.FileIniRead;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.StringUtil;
import com.easydatabaseexport.util.SwingUtils;
import lombok.SneakyThrows;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
        SwingUtils.changeLogo(jFrame);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jFrame.setSize(500, 300);
        jFrame.setLayout(new BorderLayout());
        JCheckBox yesButton = new JCheckBox(ConfigEnum.INDEX.getValue());
        //多sheet配置
        JCheckBox sheetYesButton = new JCheckBox(ConfigEnum.SHEET.getValue());
        JPanel jPanel2 = new JPanel();
        JButton confirmButton = new JButton("保存");
        JButton closeButton = new JButton("关闭");
        JTextField jTextField = new JTextField(20);

        //增加鼠标手形
        SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, confirmButton, closeButton, yesButton, sheetYesButton);

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
        //默认导出路径
        String path = CommonConstant.configMap.get(ConfigEnum.DEFAULT_EXPORT_PATH.getKey());
        JSplitPane allTotalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane totalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane totalSplitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane totalSplitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
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
                String path = jTextField.getText();
                StringBuilder tableHead = new StringBuilder();
                StringBuilder indexTableHead = new StringBuilder();

                dealWith(totalSplitPane2.getTopComponent(), indexTableHead);
                indexTableHead.append(PatternConstant.MD_SPLIT);

                dealWith(totalSplitPane2.getBottomComponent(), tableHead);
                tableHead.append(PatternConstant.MD_SPLIT);

                if (Arrays.stream(indexTableHead.toString().split(PatternConstant.COMMON_SPLIT)).noneMatch(StringUtil::isNotEmpty)) {
                    JOptionPane.showMessageDialog(null, "请检查【索引表头】！注意：表头必须大于等于1个！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (Arrays.stream(tableHead.toString().split(PatternConstant.COMMON_SPLIT)).filter(StringUtil::isNotEmpty).count() < 2) {
                    JOptionPane.showMessageDialog(null, "请检查【字段表头】！注意：表头必须大于等于2个！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        ConfigEnum.INDEX.getKey(), value);
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        ConfigEnum.SHEET.getKey(), sheetValue);
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        ConfigEnum.TABLE_HEAD.getKey(), tableHead.toString());
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        ConfigEnum.INDEX_TABLE_HEAD.getKey(), indexTableHead.toString());
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        ConfigEnum.DEFAULT_EXPORT_PATH.getKey(), path);
                CommonConstant.checkConfigIniFile();
                //改变表头
                PatternConstant.reCheckConfig();
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
        totalSplitPane2.setDividerSize(0);
        totalSplitPane3.setDividerSize(0);
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("【Word、Markdown、Html、Pdf】"));
        topPanel.add(yesButton);
        totalSplitPane.setTopComponent(topPanel);
        JPanel topPanel1 = new JPanel();
        topPanel1.setLayout(new FlowLayout(FlowLayout.LEFT));
        topPanel1.add(new JLabel("【Excel】"));
        topPanel1.add(sheetYesButton);
        JPanel topPanel2 = new JPanel();
        topPanel2.add(new JLabel("【默认导出路径】"));
        topPanel2.setLayout(new FlowLayout(FlowLayout.LEFT));

        jTextField.setPreferredSize(new Dimension(230, 24));
        jTextField.setEditable(false);
        jTextField.setText(path);
        topPanel2.add(jTextField);
        JButton chooser = new JButton("选择");
        SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, sheetYesButton, chooser);
        chooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(jTextField.getText());
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showSaveDialog(null);
                fileChooser.setDialogTitle("选择默认导出路径");
                if (result == JFileChooser.APPROVE_OPTION) {
                    jTextField.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });
        topPanel2.add(chooser);
        totalSplitPane.setBottomComponent(topPanel1);
        totalSplitPane3.setTopComponent(totalSplitPane);
        totalSplitPane3.setBottomComponent(topPanel2);

        JPanel header = new JPanel();
        header.setLayout(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("【索引表头】"));
        //添加索引表头
        Arrays.stream(CommonConstant.INDEX_FINAL_HEAD_NAMES).filter(StringUtil::isNotEmpty).forEach(v -> {
            JCheckBox head = new JCheckBox(v);
            head.setSelected(indexHead.contains(v));
            SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, head);
            header.add(ConfigEnum.INDEX_TABLE_HEAD.getKey(), head);
        });
        totalSplitPane2.setTopComponent(header);

        JPanel header2 = new JPanel();
        header2.setLayout(new FlowLayout(FlowLayout.LEFT));
        header2.add(new JLabel("【字段表头】"));
        //添加字段表头
        Arrays.stream(CommonConstant.COLUMN_FINAL_HEAD_NAMES).filter(StringUtil::isNotEmpty).forEach(v -> {
            JCheckBox head = new JCheckBox(v);
            head.setSelected(tableHead.contains(v));
            SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, head);
            header2.add(ConfigEnum.TABLE_HEAD.getKey(), head);
        });
        totalSplitPane2.setBottomComponent(header2);

        allTotalSplitPane.setTopComponent(totalSplitPane3);
        allTotalSplitPane.setBottomComponent(totalSplitPane2);
        jFrame.add(allTotalSplitPane);
        jFrame.add(jPanel2, BorderLayout.SOUTH);
        jFrame.setResizable(false);
        //居中
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

    /**
     * 处理表头
     *
     * @param component
     * @param stringBuilder 字符串
     * @return void
     **/
    private void dealWith(Component component, StringBuilder stringBuilder) {
        if (component instanceof JPanel) {
            for (Component index : ((JPanel) component).getComponents()) {
                if (index instanceof JCheckBox) {
                    JCheckBox jCheckBox = (JCheckBox) index;
                    if (jCheckBox.isSelected()) {
                        stringBuilder.append(PatternConstant.MD_SPLIT).append(jCheckBox.getText());
                    }
                }
            }
        }
    }

}
