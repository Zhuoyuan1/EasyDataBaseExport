package com.easydatabaseexport.ui;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.PatternConstant;
import com.easydatabaseexport.enums.ConfigKeyEnum;
import com.easydatabaseexport.util.AESCoder;
import com.easydatabaseexport.util.FileIniRead;
import com.easydatabaseexport.util.SwingUtils;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * EditUrlFrame 编辑连接
 *
 * @author lzy
 * @date 2022/12/1 11:17
 **/
public class EditUrlFrame {

    private final Map<String, String> configMap = new HashMap<>();

    private final String URL_NAME = "ip：%s\n端口：%s\n数据库：%s\n用户名：%s\n密码：%s";

    private boolean isMove = false;

    public EditUrlFrame() {

    }

    @SneakyThrows
    public void edit(JFrame mainFrame) {
        JFrame jframe = new JFrame("编辑连接");
        SwingUtils.changeLogo(jframe);
        jframe.setLayout(new BorderLayout());
        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jframe.setResizable(false);
        jframe.setSize(600, 500);
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        DefaultTableModel tableModel = new DefaultTableModel();
        JTable table = createTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("删除");
        JMenu topMoveMenu = new JMenu("上移");
        JMenu downMoveMenu = new JMenu("下移");
        menuBar.add(menu);
        menuBar.add(topMoveMenu);
        menuBar.add(downMoveMenu);
        menu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                deleteRow(table, tableModel);
                //删除菜单点击后不选中
                menu.setSelected(false);
            }
        });

        topMoveMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                move(MoveType.TOP, table, tableModel);
                topMoveMenu.setSelected(false);
            }
        });

        downMoveMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                move(MoveType.DOWN, table, tableModel);
                downMoveMenu.setSelected(false);
            }
        });

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem delete = new JMenuItem("删除");
        JMenuItem topMove = new JMenuItem("上移");
        JMenuItem downMove = new JMenuItem("下移");
        JMenuItem detail = new JMenuItem("查看详情");
        popupMenu.add(delete);
        popupMenu.add(topMove);
        popupMenu.add(downMove);
        popupMenu.add(detail);
        jframe.add(popupMenu);
        jframe.add(menuBar, BorderLayout.NORTH);
        jframe.add(scroll);

        //增加鼠标右键功能
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                if (SwingUtilities.isRightMouseButton(me)) {
                    int row = table.rowAtPoint(me.getPoint());
                    int column = table.columnAtPoint(me.getPoint());

                    if (!table.isRowSelected(row)) {
                        table.changeSelection(row, column, false, false);
                    }

                    popupMenu.show(table, me.getX(), me.getY());
                }
            }
        });

        //右键删除按钮
        delete.addActionListener(e -> {
            deleteRow(table, tableModel);
        });
        //右键上移按钮
        topMove.addActionListener(e -> {
            move(MoveType.TOP, table, tableModel);
        });
        //右键下移按钮
        downMove.addActionListener(e -> {
            move(MoveType.DOWN, table, tableModel);
        });

        //加密密钥
        byte[] keyByte = AESCoder.readFileReturnByte();
        //查看密码按钮
        detail.addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectRows = table.getSelectedRow();
                if (selectRows < 0) {
                    return;
                }
                String url = (String) tableModel.getValueAt(selectRows, 0);
                String message = "";
                String[] strings = url.split(PatternConstant.COMMON_SPLIT);
                if (strings.length > 5) {
                    message = String.format(URL_NAME, strings[1], strings[2], strings[3], strings[4], new String(AESCoder.decrypt(Base64.decodeBase64(configMap.get(url)), keyByte)));
                } else {
                    message = String.format(URL_NAME, strings[0], strings[1], strings[2], strings[3], new String(AESCoder.decrypt(Base64.decodeBase64(configMap.get(url)), keyByte)));
                }
                JTextArea ta = new JTextArea();
                ta.setText(message);
                ta.setWrapStyleWord(true);
                ta.setLineWrap(true);
                ta.setCaretPosition(0);
                ta.setEditable(false);
                JOptionPane.showMessageDialog(null, new JScrollPane(ta), "", JOptionPane.INFORMATION_MESSAGE);
                table.clearSelection();
            }
        });

        JPanel button = new JPanel();
        JButton allEmptyButton = new JButton("全部删除");
        JButton resetButton = new JButton("重置");
        JButton confirmButton = new JButton("保存");
        JButton closeButton = new JButton("关闭");
        //增加鼠标手形
        SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, confirmButton, closeButton, allEmptyButton, resetButton);
        button.add(allEmptyButton);
        button.add(resetButton);
        button.add(confirmButton);
        button.add(closeButton);
        jframe.add(button, BorderLayout.SOUTH);
        jframe.setLocationRelativeTo(null);
        jframe.setVisible(true);
        allEmptyButton.addActionListener(e -> {
            confirm(jframe, mainFrame, new ArrayList<>(configMap.keySet()), "是否全部删除？");
        });
        resetButton.addActionListener(e -> {
            jframe.dispose();
            EditUrlFrame editUrlFrame = new EditUrlFrame();
            editUrlFrame.edit(mainFrame);
        });
        confirmButton.addActionListener(e -> {
            List<String> keys = new ArrayList<String>();
            for (int i = 0; i < table.getRowCount(); i++) {
                String key = (String) table.getValueAt(i, 0);
                keys.add(key);
            }
            //人性化处理
            if (keys.isEmpty()) {
                confirm(jframe, mainFrame, new ArrayList<>(configMap.keySet()), "是否全部删除？");
            } else {
                if (!isMove) {
                    //取交集的补集
                    List<String> deletedKeys = (List<String>) CollectionUtils.disjunction(new ArrayList<>(configMap.keySet()), keys);
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String key : deletedKeys) {
                        stringBuilder.append(key).append("\n");
                    }
                    if (deletedKeys.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "未做任何修改！", "提示",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    confirm(jframe, mainFrame, deletedKeys, "是否删除以下连接配置？\n" + stringBuilder);
                } else {
                    //移动过数据 逻辑：由于打乱了顺序，顾全部删除，重新插入
                    Map<String, String> allKeysMap = new LinkedHashMap<>();
                    for (String key : keys) {
                        allKeysMap.put(key, configMap.get(key));
                    }
                    deleteAndInsert(jframe, mainFrame, new ArrayList<>(configMap.keySet()), allKeysMap);
                }
            }
        });
        closeButton.addActionListener(e -> jframe.dispose());
    }

    private void move(MoveType type, JTable table, DefaultTableModel tableModel) {
        int[] selectRows = table.getSelectedRows();
        //1.只能移动单条数据 2.顶部不能再上移 3.底部不能再下移
        if (selectRows.length <= 0) {
            table.clearSelection();
        } else {
            int index = selectRows[0];
            if (type.equals(MoveType.TOP) && selectRows[0] - 1 >= 0) {
                tableModel.moveRow(index, index, index - 1);
                table.setRowSelectionInterval(index - 1, index - 1);
            } else {
                if (!type.equals(MoveType.DOWN) || selectRows[0] + 1 >= tableModel.getRowCount()) {
                    table.clearSelection();
                    return;
                }
                tableModel.moveRow(index, index, index + 1);
                table.setRowSelectionInterval(index + 1, index + 1);
            }
            if (!isMove) {
                isMove = true;
            }
        }
    }

    private void deleteRow(JTable table, DefaultTableModel tableModel) {
        int[] selectRows = table.getSelectedRows();
        if (selectRows.length <= 0) {
            return;
        }
        int selectRowsIndex = 0;
        for (int selectRow : selectRows) {
            //因删除table的一行后，数据的索引发生了改变
            tableModel.removeRow(selectRow - selectRowsIndex);
            selectRowsIndex++;
        }
        //清除table的选择项
        table.clearSelection();
    }

    private JTable createTable(DefaultTableModel tableModel) {
        Vector<Vector<Object>> data = new Vector<>();
        Vector<String> columnNames = new Vector<String>();
        for (String config : FileIniRead.getIniConf()) {
            Vector<Object> vector = new Vector<Object>();
            vector.add(dealWithConfig(config));
            data.add(vector);
        }
        columnNames.add("连接");
        tableModel.setDataVector(data, columnNames);
        JTable table = new JTable(tableModel);
        //禁止移动列
        table.getTableHeader().setReorderingAllowed(false);
        table.setDragEnabled(false);
        table.setRowHeight(30);
        //设置宽度
        TableColumn column = null;
        for (int i = 0; i < table.getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);
            column.setCellEditor(new MyCell());
            column.setCellRenderer(new MyCell());
        }
        return table;
    }

    /**
     * 公共删除提示框
     *
     * @param jframe      当前页面
     * @param mainFrame   主页面
     * @param deletedKeys 删除的key
     * @param message     信息
     **/
    private void confirm(JFrame jframe, JFrame mainFrame, List<String> deletedKeys, String message) {
        int n = JOptionPane.showConfirmDialog(null, message, "提醒", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            FileIniRead.deleteIniConf(ConfigKeyEnum.SYS.getKey(), deletedKeys);
            SwingUtils.rebootFrame("保存成功", "", jframe, mainFrame);
        }
    }

    /**
     * 删除并重新插入
     *
     * @param jframe     当前页面
     * @param mainFrame  主界面
     * @param keys       用户编辑前所有的key
     * @param allKeysMap 剩余的key
     **/
    private void deleteAndInsert(JFrame jframe, JFrame mainFrame, List<String> keys, Map<String, String> allKeysMap) {
        int n = JOptionPane.showConfirmDialog(null, "是否保存修改？", "提醒", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            FileIniRead.deleteIniConf(ConfigKeyEnum.SYS.getKey(), keys);
            //重新写入
            FileIniRead.insertIniConf(allKeysMap);
            SwingUtils.rebootFrame("保存成功", "", jframe, mainFrame);
        }
    }

    /**
     * 处理配置
     *
     * @param config 完整配置文件
     * @author lzy
     **/
    private String dealWithConfig(String config) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] strings = config.split(PatternConstant.COMMON_SPLIT);
        if (strings.length > 5) {
            stringBuilder.append(strings[0]).append(CommonConstant.SEPARATOR).append(strings[1]).append(CommonConstant.SEPARATOR)
                    .append(strings[2]).append(CommonConstant.SEPARATOR).append(strings[3]).append(CommonConstant.SEPARATOR).append(strings[4]);
            configMap.put(stringBuilder.toString(), strings[5]);
        } else {
            stringBuilder.append(strings[0]).append(CommonConstant.SEPARATOR).append(strings[1])
                    .append(CommonConstant.SEPARATOR).append(strings[2]).append(CommonConstant.SEPARATOR).append(strings[3]);
            configMap.put(stringBuilder.toString(), strings[4]);
        }
        return stringBuilder.toString();
    }

    enum MoveType {
        //上移
        TOP,
        //下移
        DOWN
    }
}
