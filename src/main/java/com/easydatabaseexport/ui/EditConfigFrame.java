package com.easydatabaseexport.ui;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.PatternConstant;
import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.util.StringUtil;
import com.easydatabaseexport.util.SwingUtils;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import java.awt.Cursor;
import java.awt.Dimension;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * EditConfigFrame
 *
 * @author lzy
 * @date 2023/3/4 21:34
 **/
public class EditConfigFrame {

    public static Map<Object, Integer> map = new HashMap<>();
    public static final Map<String, String> indexFieldMap = new HashMap<>();
    public static final Map<String, String> tableFieldMap = new HashMap<>();

    static {
        Field[] indexFields = IndexInfoVO.class.getDeclaredFields();
        for (int i = 0; i < indexFields.length; i++) {
            indexFieldMap.put(indexFields[i].getName(), CommonConstant.INDEX_FINAL_HEAD_NAMES[i]);
        }
        Field[] tableFields = TableParameter.class.getDeclaredFields();
        for (int i = 0; i < tableFields.length; i++) {
            tableFieldMap.put(tableFields[i].getName(), CommonConstant.COLUMN_FINAL_HEAD_NAMES[i]);
        }
    }

    public EditConfigFrame() {

    }

    public void configFrame(JCheckBox head, Field[] fields) {
        JFrame jFrame = new JFrame();
        SwingUtils.changeLogo(jFrame);
        jFrame.setResizable(false);
        jFrame.setSize(300, 200);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JPanel jPanel = new JPanel();
        JLabel jLabel = new JLabel("【表头名称】");
        JTextField name = new JTextField();
        name.setPreferredSize(new Dimension(230, 26));
        name.setText(head.getText());
        jPanel.add(jLabel);
        jPanel.add(name);

        JLabel jLabel2 = new JLabel("【映射字段】");

        String[] str = Arrays.stream(fields).map(v -> {
            if (indexFieldMap.containsKey(v.getName())) {
                return v.getName() + " ===> " + indexFieldMap.get(v.getName());
            } else if (tableFieldMap.containsKey(v.getName())) {
                return v.getName() + " ===> " + tableFieldMap.get(v.getName());
            }
            return "";
        }).toArray(String[]::new);
        jPanel.add(jLabel2);
        JComboBox<String> jComboBox = new JComboBox<>(str);
        jComboBox.setPreferredSize(new Dimension(230, 26));
        jComboBox.setSelectedIndex(map.get(head));
        jPanel.add(jComboBox);

        JSplitPane allTotalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        allTotalSplitPane.setTopComponent(jPanel);
        allTotalSplitPane.setDividerSize(0);
        allTotalSplitPane.setResizeWeight(0.95);
        JPanel jPanel2 = new JPanel();
        JButton confirmButton = new JButton("确定");
        JButton closeButton = new JButton("关闭");
        SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, confirmButton, closeButton);

        jPanel2.add(confirmButton);
        jPanel2.add(closeButton);
        allTotalSplitPane.setBottomComponent(jPanel2);
        jFrame.add(allTotalSplitPane);
        confirmButton.addActionListener(e -> {
            String result = name.getText();
            if (StringUtil.isEmpty(result) || result.contains(PatternConstant.MD_SPLIT) || result.contains(PatternConstant.SLASH_SPLIT)) {
                JOptionPane.showMessageDialog(null, "表头不能为空，且不能出现|、\\等特殊字符！", "警告", JOptionPane.WARNING_MESSAGE);
                return;
            }
            map.put(head, jComboBox.getSelectedIndex());
            head.setText(result);
            jFrame.dispose();
        });
        closeButton.addActionListener(e -> jFrame.dispose());
        //居中
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }
}
