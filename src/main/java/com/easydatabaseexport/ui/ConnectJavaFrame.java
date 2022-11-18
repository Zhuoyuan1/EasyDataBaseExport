package com.easydatabaseexport.ui;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.CommonDataBaseType;
import com.easydatabaseexport.core.DataResult;
import com.easydatabaseexport.entities.ErrorMsg;
import com.easydatabaseexport.entities.TableType;
import com.easydatabaseexport.factory.DataBaseAssemblyFactory;
import com.easydatabaseexport.factory.assembly.impl.ConDatabaseModeTableImpl;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.ui.component.CustomMenu;
import com.easydatabaseexport.ui.component.JCheckBoxTree;
import com.easydatabaseexport.ui.export.ExcelActionListener;
import com.easydatabaseexport.ui.export.HtmlActionListener;
import com.easydatabaseexport.ui.export.MarkdownActionListener;
import com.easydatabaseexport.ui.export.PdfActionListener;
import com.easydatabaseexport.ui.export.WordActionListener;
import com.easydatabaseexport.util.CheckUpdateUtil;
import com.easydatabaseexport.util.FileIniRead;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.SwingUtils;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;


/**
 * ConnectJavaFrame
 *
 * @author lzy
 * @date 2021/11/1 15:06
 **/
@Log
public class ConnectJavaFrame {

    private final DataResult dataResult;

    public ConnectJavaFrame() {
        this.dataResult = DataBaseAssemblyFactory.get(CommonConstant.DATA_BASE_TYPE).dataResult();
    }

    /**
     * 主界面
     */
    public void mainFrame() {
        JFrame jFrame = new JFrame("EasyDataBaseExport");
        jFrame.setSize(CommonConstant.FRAME_WIDTH, CommonConstant.FRAME_HEIGHT);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置窗口最小的大小（不可再变小）
        jFrame.setMinimumSize(new Dimension(CommonConstant.FRAME_WIDTH, CommonConstant.FRAME_HEIGHT));
        JMenuBar menuBar = CustomMenu.CustomMenu();
        //处理是否填写过库名
        List<String> nameList = dataResult.getAllDataBaseName();
        JCheckBoxTree.CheckNode rootNode = new JCheckBoxTree.CheckNode(CommonConstant.ROOT);
        for (String s : nameList) {
            JCheckBoxTree.CheckNode dataBaseNode = new JCheckBoxTree.CheckNode(s);
            if (DataBaseAssemblyFactory.get(CommonConstant.DATA_BASE_TYPE) instanceof ConDatabaseModeTableImpl) {
                for (Map.Entry<String, Map<String, List<TableType>>> map : CommonDataBaseType.CON_DATABASE_MODE_TABLE_MAP.entrySet()) {
                    if (s.equals(map.getKey())) {
                        for (Map.Entry<String, List<TableType>> aMap : map.getValue().entrySet()) {
                            JCheckBoxTree.CheckNode model = new JCheckBoxTree.CheckNode(aMap.getKey());
                            dataBaseNode.add(model);
                            List<TableType> list = aMap.getValue();
                            list.forEach(v -> {
                                JCheckBoxTree.CheckNode root1 = new JCheckBoxTree.CheckNode(v.getTableName());
                                model.add(root1);
                            });
                        }
                    }
                }
            } else {
                for (Map.Entry<String, List<TableType>> map : CommonDataBaseType.CON_DATABASE_TABLE_MAP.entrySet()) {
                    //一个库中的表
                    if (s.equals(map.getKey())) {
                        List<TableType> list = map.getValue();
                        list.forEach(v -> {
                            JCheckBoxTree.CheckNode root1 = new JCheckBoxTree.CheckNode(v.getTableName());
                            dataBaseNode.add(root1);
                        });
                    }
                }
            }
            rootNode.add(dataBaseNode);
        }
        CommonConstant.root = new JCheckBoxTree(rootNode);

        //添加监听事件
        CommonConstant.root.addMouseListener(new MouseAdapter() {
            @SneakyThrows
            @Override
            public void mouseClicked(MouseEvent e) {
                DefaultMutableTreeNode selectionNode = (DefaultMutableTreeNode) CommonConstant.root.getLastSelectedPathComponent();
                if (e.getClickCount() > 0 && selectionNode != null) {
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectionNode.getParent();
                    int index = selectionNode.getUserObject().toString().lastIndexOf("[");
                    if (index <= 0) {
                        return;
                    }
                    CommonConstant.TABLE_NAME = selectionNode.getUserObject().toString().substring(0, index);
                    CommonConstant.DATABASE_NAME = parent.getUserObject().toString();
                    if (DataBaseAssemblyFactory.get(CommonConstant.DATA_BASE_TYPE) instanceof ConDatabaseModeTableImpl) {
                        DefaultMutableTreeNode dataBaseNode = (DefaultMutableTreeNode) parent.getParent();
                        CommonConstant.TREE_DATABASE = dataBaseNode.getUserObject().toString();
                    }
                    if (!parent.getUserObject().toString().equals(CommonConstant.root.getModel().getRoot().toString())) {
                        //先查数据库，保证 库、表、字段没被删除
                        Map<String, ErrorMsg> map = dataResult.checkExist(CommonConstant.TABLE_NAME, CommonConstant.DATABASE_NAME);
                        if (map.containsKey(CommonConstant.FAIL)) {
                            ErrorMsg msg = map.get(CommonConstant.FAIL);
                            JOptionPane.showMessageDialog(null, msg.getMessage(), msg.getTitle(), msg.getMessageType());
                            return;
                        }
                        if (e.getClickCount() == 1) {
                            dataResult.doListValueChanged();
                        } else if (e.getClickCount() == 2) {
                            dataResult.doListDataValueChanged();
                        }
                    }
                }
            }
        });
        // 创建一个水平方向的分割面板
        final JSplitPane hSplitPane = new JSplitPane();
        // 分隔条左侧的宽度为300像素
        hSplitPane.setDividerLocation(300);
        hSplitPane.setDividerSize(5);
        hSplitPane.setEnabled(false);
        // 在水平面板左侧添加一个标签组件
        JScrollPane rootJs = new JScrollPane(CommonConstant.root);
        hSplitPane.setLeftComponent(rootJs);
        JPanel panel = new JPanel(new BorderLayout());
        CommonConstant.RIGHT.setBorder(new TitledBorder("详细信息"));
        panel.add(CommonConstant.RIGHT, BorderLayout.CENTER);
        // 创建一个水平方向的分割面板
        final JSplitPane vSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT);
        // 分隔条左侧的高度为
        vSplitPane.setDividerLocation(900);
        vSplitPane.setResizeWeight(0.6);
        // 分隔条的宽度为
        vSplitPane.setDividerSize(10);
        // 提供UI小部件
        vSplitPane.setOneTouchExpandable(false);
        vSplitPane.setEnabled(false);
        // 在垂直面板上方添加一个标签组件
        vSplitPane.setLeftComponent(CommonConstant.CENTERS);
        // 在垂直面板下方添加一个标签组件
        vSplitPane.setRightComponent(panel);
        // 添加到水平面板的右侧
        hSplitPane.setRightComponent(vSplitPane);
        final JSplitPane totalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        totalSplitPane.setTopComponent(menuBar);
        totalSplitPane.setBottomComponent(hSplitPane);
        //去除菜单和内容的分割线
        totalSplitPane.setDividerSize(0);
        totalSplitPane.setEnabled(false);
        // 第一个标签下的JPanel
        //jTabbedpane.addTab(tabNames[0], null, hSplitPane, "first");// 加入第一个页面
        jFrame.add(totalSplitPane);
        menuBar.getMenu(0).getItem(0).addActionListener(e -> {
            CommonConstant.initByClose();
            jFrame.dispose();
            try {
                IndexJavaFrame.connectFrame();
            } catch (Exception ex) {
                LogManager.writeLogFile(ex, log);
            }
        });
        menuBar.getMenu(0).getItem(1).addActionListener(e -> {
            UploadSynFrame uploadSynFrame = new UploadSynFrame();
            uploadSynFrame.UpLoadFile(jFrame);
        });
        jFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                CommonConstant.RIGHT.removeAll();
                CommonConstant.CENTERS.removeAll();
            }
        });
        for (int i = 0; i < CommonConstant.THEMES.length; i++) {
            int finalI = i;
            menuBar.getMenu(1).getItem(i).addActionListener(new ActionListener() {
                @SneakyThrows
                @Override
                public void actionPerformed(ActionEvent e) {
                    UIManager.setLookAndFeel(CommonConstant.THEMES[finalI]);
                    SwingUtilities.updateComponentTreeUI(jFrame);
                    FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, "theme", "theme", String.valueOf(finalI));
                    JOptionPane.showMessageDialog(null, "主题已应用！", "", JOptionPane.PLAIN_MESSAGE);
                }
            });
        }
        menuBar.getMenu(1).getItem(CommonConstant.THEMES.length + 1).addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, "theme", "theme", "-1");
                SwingUtils.rebootFrame("还原成功", "", null, jFrame);
            }
        });
        //居中
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
        //当所有元素加载完成后，需要再进行主题渲染
        SwingUtilities.updateComponentTreeUI(jFrame);
        //添加导出excel按钮监听
        menuBar.getMenu(2).getItem(0).addActionListener(new ExcelActionListener(rootNode));
        //添加导出word按钮监听
        menuBar.getMenu(2).getItem(1).addActionListener(new WordActionListener(rootNode));
        //添加导出markdown按钮监听
        menuBar.getMenu(2).getItem(2).addActionListener(new MarkdownActionListener(rootNode));
        //添加导出html按钮监听
        menuBar.getMenu(2).getItem(3).addActionListener(new HtmlActionListener(rootNode));
        //添加导出pdf按钮监听
        menuBar.getMenu(2).getItem(4).addActionListener(new PdfActionListener(rootNode));

        CheckUpdateUtil.check();
    }

}
