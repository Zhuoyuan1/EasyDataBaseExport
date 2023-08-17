package com.easydatabaseexport.ui;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.PatternConstant;
import com.easydatabaseexport.enums.ConfigEnum;
import com.easydatabaseexport.enums.ConfigKeyEnum;
import com.easydatabaseexport.enums.UpdateEnum;
import com.easydatabaseexport.enums.YesNoEnum;
import com.easydatabaseexport.util.FileIniRead;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.StringUtil;
import com.easydatabaseexport.util.SwingUtils;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 配置UI
 *
 * @author lzy
 * @date 2021/12/1 15:06
 */
public class ConfigJavaFrame {

    private final static String EXPLAIN = "<html><font size=4>① 导出文档时，索引或字段表头选中，即显示；否则，不显示<br/>" +
            "② <font color=red>复选框选中</font>表头名称并单击<font color=red>右键</font>，可以修改名称和字段映射（字段映射指：表头名称和表体值的匹配关系）</font></html>";

    public ConfigJavaFrame() {

    }

    public void configFrame() {
        JFrame jFrame = new JFrame("配置");
        SwingUtils.changeLogo(jFrame);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jFrame.setSize(500, 360);
        jFrame.setLayout(new BorderLayout());
        JCheckBox yesButton = new JCheckBox(ConfigEnum.INDEX.getValue());
        //多sheet配置
        JCheckBox sheetYesButton = new JCheckBox(ConfigEnum.SHEET.getValue());
        JPanel jPanel2 = new JPanel();
        JButton confirmButton = new JButton("保存");
        JButton resetButton = new JButton("恢复默认");
        JButton closeButton = new JButton("关闭");
        JTextField jTextField = new JTextField(20);

        //增加鼠标手形
        SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, confirmButton, resetButton, closeButton, yesButton, sheetYesButton);

        jPanel2.add(confirmButton);
        jPanel2.add(resetButton);
        jPanel2.add(closeButton);
        // 读取ini文件配置
        CommonConstant.initCheckAllKey(CommonConstant.EXPORT);
        // 是否导出索引
        yesButton.setSelected(YesNoEnum.YES_1.getValue().equals(CommonConstant.configMap.get(ConfigEnum.INDEX.getKey())));
        // 是否多sheet
        sheetYesButton.setSelected(YesNoEnum.YES_1.getValue().equals(CommonConstant.configMap.get(ConfigEnum.SHEET.getKey())));
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
                //处理导出字段
                StringBuilder tableHead = new StringBuilder();
                StringBuilder indexTableHead = new StringBuilder();

                dealWith(totalSplitPane2.getTopComponent(), indexTableHead);
                indexTableHead.append(PatternConstant.MD_SPLIT);

                dealWith(totalSplitPane2.getBottomComponent(), tableHead);
                tableHead.append(PatternConstant.MD_SPLIT);
                //处理字段映射
                StringBuilder indexField = new StringBuilder();
                StringBuilder tableField = new StringBuilder();
                dealWithField(totalSplitPane2.getTopComponent(), indexField);
                dealWithField(totalSplitPane2.getBottomComponent(), tableField);

                if (Arrays.stream(indexTableHead.toString().split(PatternConstant.COMMON_SPLIT)).filter(StringUtil::isNotEmpty).allMatch(v -> v.endsWith(PatternConstant.HEAD_IGNORE))) {
                    JOptionPane.showMessageDialog(null, "请检查【索引表头】！注意：表头必须大于等于1个！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (Arrays.stream(tableHead.toString().split(PatternConstant.COMMON_SPLIT)).filter(v -> StringUtil.isNotEmpty(v) && !v.endsWith(PatternConstant.HEAD_IGNORE)).count() < 2) {
                    JOptionPane.showMessageDialog(null, "请检查【字段表头】！注意：表头必须大于等于2个！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                writeConfig(ConfigEnum.INDEX.getKey(), value);
                writeConfig(ConfigEnum.SHEET.getKey(), sheetValue);
                writeConfig(ConfigEnum.TABLE_HEAD.getKey(), tableHead.toString());
                writeConfig(ConfigEnum.INDEX_TABLE_HEAD.getKey(), indexTableHead.toString());
                writeConfig(ConfigEnum.DEFAULT_EXPORT_PATH.getKey(), path);
                writeConfig(ConfigEnum.INDEX_FIELD_INDEX.getKey(), indexField.toString());
                writeConfig(ConfigEnum.TABLE_FIELD_INDEX.getKey(), tableField.toString());
                CommonConstant.checkConfigIniFile();
                //改变表头
                PatternConstant.reCheckConfig();
                JOptionPane.showMessageDialog(null, "保存成功！", "提醒", JOptionPane.PLAIN_MESSAGE);
                jFrame.dispose();
            }
        });

        resetButton.addActionListener(e -> {
            int n = JOptionPane.showConfirmDialog(null, "是否全部恢复为默认值？", "提醒", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                List<String> keys = Arrays.stream(ConfigEnum.values()).map(ConfigEnum::getKey).collect(Collectors.toList());
                //加入更新的key
                keys.add(UpdateEnum.UPDATE_VERSION.getKey());
                keys.add(UpdateEnum.VERSIONS.getKey());
                FileIniRead.deleteIniConf(ConfigKeyEnum.CONFIG.getKey(), keys);

                writeConfig(ConfigEnum.TABLE_HEAD.getKey(), ConfigEnum.TABLE_HEAD.getValue());
                writeConfig(ConfigEnum.INDEX_TABLE_HEAD.getKey(), ConfigEnum.INDEX_TABLE_HEAD.getValue());
                writeConfig(ConfigEnum.DEFAULT_EXPORT_PATH.getKey(), ConfigEnum.DEFAULT_EXPORT_PATH.getValue());
                writeConfig(ConfigEnum.INDEX_FIELD_INDEX.getKey(), ConfigEnum.INDEX_FIELD_INDEX.getValue());
                writeConfig(ConfigEnum.TABLE_FIELD_INDEX.getKey(), ConfigEnum.TABLE_FIELD_INDEX.getValue());
                CommonConstant.checkConfigIniFile();
                //改变表头
                PatternConstant.reCheckConfig();
                JOptionPane.showMessageDialog(null, new JLabel("<html>恢复成功！"), "提醒", JOptionPane.PLAIN_MESSAGE);
                jFrame.dispose();
            }
        });

        closeButton.addActionListener(e -> jFrame.dispose());
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
        JPanel header2 = new JPanel();
        header.setLayout(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("【索引表头】"));
        //右键编辑弹框
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem edit = new JMenuItem("修改...");
        popupMenu.add(edit);
        edit.addActionListener(e -> {
            JCheckBox newHead = (JCheckBox) popupMenu.getInvoker();
            Field[] fields;
            if (newHead.getParent().equals(header)) {
                fields = PatternConstant.INDEX_ALL_FIELDS;
            } else {
                fields = PatternConstant.TABLE_ALL_FIELDS;
            }
            EditConfigFrame configFrame = new EditConfigFrame();
            configFrame.configFrame(newHead, fields);
        });
        boolean hasConfig = CommonConstant.configMap.containsKey(ConfigEnum.INDEX_FIELD_INDEX.getKey());
        List<Integer> indexList = new ArrayList<>();
        if (hasConfig) {
            indexList = Arrays.stream(CommonConstant.configMap.get(ConfigEnum.INDEX_FIELD_INDEX.getKey()).split(PatternConstant.COMMON_SPLIT))
                    .map(Integer::parseInt).collect(Collectors.toList());
        }
        //添加索引表头
        List<Integer> finalIndexList = indexList;
        Arrays.stream(CommonConstant.INDEX_CONFIG_HEAD_NAMES).filter(StringUtil::isNotEmpty).forEach(consumerWithIndex((v, index) -> {
            if (hasConfig) {
                dealWithHead(popupMenu, header, v, finalIndexList.get(index));
            } else {
                dealWithHead(popupMenu, header, v, index);
            }
        }));
        header.add(new JLabel("<html> 默认表头依次为：<font color=red>"
                + StringUtil.join(Arrays.stream(CommonConstant.INDEX_FINAL_HEAD_NAMES).collect(Collectors.toList()), "、") + "</font></html>"));
        totalSplitPane2.setResizeWeight(0.2);
        totalSplitPane2.setTopComponent(header);

        header2.setLayout(new FlowLayout(FlowLayout.LEFT));
        header2.add(new JLabel("【字段表头】"));
        boolean hasTableConfig = CommonConstant.configMap.containsKey(ConfigEnum.TABLE_FIELD_INDEX.getKey());
        Integer[] tableIndex = new Integer[0];
        if (hasConfig) {
            tableIndex = Arrays.stream(CommonConstant.configMap.get(ConfigEnum.TABLE_FIELD_INDEX.getKey()).split(PatternConstant.COMMON_SPLIT))
                    .map(Integer::parseInt).toArray(Integer[]::new);
        }
        //添加字段表头
        Integer[] finalTableIndex = tableIndex;
        Arrays.stream(CommonConstant.COLUMN_CONFIG_HEAD_NAMES).filter(StringUtil::isNotEmpty).forEach(consumerWithIndex((v, index) -> {
            if (hasTableConfig) {
                dealWithHead(popupMenu, header2, v, finalTableIndex[index]);
            } else {
                dealWithHead(popupMenu, header2, v, index);
            }
        }));
        header2.add(new JLabel("<html>默认表头依次为：<font color=red>" + StringUtil.join(Arrays.stream(CommonConstant.COLUMN_FINAL_HEAD_NAMES)
                .collect(Collectors.toList()), "、") + "</font></html>"));
        totalSplitPane2.setBottomComponent(header2);
        allTotalSplitPane.setTopComponent(totalSplitPane3);
        allTotalSplitPane.setBottomComponent(totalSplitPane2);
        // 设置提示说明
        header.setToolTipText(EXPLAIN);
        header2.setToolTipText(EXPLAIN);
        jFrame.add(allTotalSplitPane);
        jFrame.add(jPanel2, BorderLayout.SOUTH);
        jFrame.setResizable(false);
        //居中
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

    private void writeConfig(String key, String value) {
        FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                key, value);
    }

    private void dealWithField(Component component, StringBuilder stringBuilder) {
        if (!EditConfigFrame.map.isEmpty()) {
            if (component instanceof JPanel) {
                for (Component index : ((JPanel) component).getComponents()) {
                    if (index instanceof JCheckBox) {
                        JCheckBox jCheckBox = (JCheckBox) index;
                        stringBuilder.append(EditConfigFrame.map.get(jCheckBox)).append(PatternConstant.MD_SPLIT);
                    }
                }
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
    }

    // 工具方法
    public static <T> Consumer<T> consumerWithIndex(BiConsumer<T, Integer> consumer) {
        class Obj {
            int i;
        }
        Obj obj = new Obj();
        return t -> {
            int index = obj.i++;
            consumer.accept(t, index);
        };
    }

    private void dealWithHead(JPopupMenu popupMenu, JPanel header, String v, int valueIndex) {
        JCheckBox head = new JCheckBox(v);
        if (v.endsWith(PatternConstant.HEAD_IGNORE)) {
            int index = v.lastIndexOf(PatternConstant.HEAD_IGNORE);
            v = v.substring(0, index);
            head.setText(v);
            head.setSelected(false);
        } else {
            head.setSelected(true);
        }
        SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, head);
        head.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                if (SwingUtilities.isRightMouseButton(me) && head.isSelected()) {
                    popupMenu.show(head, me.getX(), me.getY());
                }
            }
        });
        EditConfigFrame.map.put(head, valueIndex);
        header.add(head);
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
                    stringBuilder.append(PatternConstant.MD_SPLIT).append(jCheckBox.getText());
                    if (!jCheckBox.isSelected()) {
                        stringBuilder.append(PatternConstant.HEAD_IGNORE);
                    }
                }
            }
        }
    }

}
