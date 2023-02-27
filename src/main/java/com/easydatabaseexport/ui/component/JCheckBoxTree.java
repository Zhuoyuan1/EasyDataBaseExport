package com.easydatabaseexport.ui.component;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.factory.DataBaseAssemblyFactory;
import com.easydatabaseexport.factory.assembly.DataBaseAssembly;
import com.easydatabaseexport.factory.assembly.impl.ConDatabaseModeTableImpl;
import javafx.util.Pair;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * JCheckBoxTree
 *
 * @author lzy
 * @date 2021/3/20 21:18
 **/
public class JCheckBoxTree extends JTree {
    public static void main(String[] args) {
        CheckNode root = new CheckNode("Root");
        CheckNode child1 = new CheckNode("child1");
        CheckNode child2 = new CheckNode("child2");
        child2.add(new CheckNode("child3"));
        child1.add(new CheckNode("child4"));
        root.add(child1);
        root.add(child2);
        JCheckBoxTree boxTree = new JCheckBoxTree(root);
        JFrame frame = new JFrame();
        frame.add(boxTree);
        frame.setVisible(true);
        frame.setSize(300, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static final long serialVersionUID = 1L;

    public JCheckBoxTree(CheckNode checkNode) {
        super(checkNode);
        this.setCellRenderer(new CheckRenderer());
        this.setShowsRootHandles(false);
        //this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION );
        //this.putClientProperty("JTree.lineStyle", "Angled");
        this.addLister(this);
    }

    /***
     * 添加点击事件使其选中父节点时
     * 子节点也选中
     * @param tree
     */
    private void addLister(final JTree tree) {
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int row = tree.getRowForLocation(e.getX(), e.getY());
                TreePath path = tree.getPathForRow(row);
                DefaultMutableTreeNode selectionNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (null == selectionNode) {
                    return;
                }
                if (path != null && null != selectionNode) {
                    CheckNode node = (CheckNode) path.getLastPathComponent();
                    node.setSelected(!node.isSelected);
                    /*if (node.getSelectionMode() == CheckNode.DIG_IN_SELECTION) {
                        if (node.isSelected) {
                            tree.expandPath(path);
                        } else {
                            if (!node.isRoot()) {
                                tree.collapsePath(path);
                            }
                        }
                    }*/
                    //响应事件更新树
                    ((DefaultTreeModel) tree.getModel()).nodeChanged(node);
                    tree.revalidate();
                    tree.repaint();
                }
            }

        });
    }

    public static Pair<JCheckBoxTree, CheckNode> searchTree(CheckNode root, String key) {
        Enumeration<DefaultMutableTreeNode> e = root.breadthFirstEnumeration();
        CheckNode rootNode = new CheckNode(CommonConstant.ROOT);
        DefaultMutableTreeNode node = e.nextElement();
        if (node.getLevel() == 0) {
            if (node.toString().contains(key)) {
                return new Pair<>(new JCheckBoxTree(root), root);
            }
        }
        if (DataBaseAssemblyFactory.get(CommonConstant.DATA_BASE_TYPE) instanceof ConDatabaseModeTableImpl) {
            //查找 (连接 -> 数据库 -> 模式 -> 表) 名称是否匹配上
            Enumeration<DefaultMutableTreeNode> enumeration = node.children();
            while (enumeration.hasMoreElements()) {
                DefaultMutableTreeNode database = enumeration.nextElement();
                CheckNode newDataBase = new CheckNode(database.toString());
                if (database.toString().contains(key)) {
                    rootNode.add(newDataBase);
                    Enumeration<DefaultMutableTreeNode> patterns = database.children();
                    while (patterns.hasMoreElements()) {
                        DefaultMutableTreeNode pattern = patterns.nextElement();
                        if (pattern.toString().contains(key)) {
                            CheckNode newPattern = new CheckNode(pattern.toString());
                            newDataBase.add(newPattern);
                            Enumeration<DefaultMutableTreeNode> tables = pattern.children();
                            while (tables.hasMoreElements()) {
                                DefaultMutableTreeNode table = tables.nextElement();
                                CheckNode newTable = new CheckNode(table.toString());
                                newPattern.add(newTable);
                            }
                        } else {
                            Enumeration<DefaultMutableTreeNode> tables = pattern.children();
                            List<String> list = new LinkedList<>();
                            while (tables.hasMoreElements()) {
                                DefaultMutableTreeNode table = tables.nextElement();
                                if (table.toString().contains(key)) {
                                    list.add(table.toString());
                                }
                            }
                            if (list.size() > 0) {
                                CheckNode newPattern = new CheckNode(pattern.toString());
                                list.forEach(v -> {
                                    newPattern.add(new CheckNode(v));
                                });
                                newDataBase.add(newPattern);
                            }
                        }
                    }
                } else {
                    Enumeration<DefaultMutableTreeNode> patterns = database.children();
                    while (patterns.hasMoreElements()) {
                        DefaultMutableTreeNode pattern = patterns.nextElement();
                        if (pattern.toString().contains(key)) {
                            CheckNode newPattern = new CheckNode(pattern.toString());
                            newDataBase.add(newPattern);
                            rootNode.add(newDataBase);
                            Enumeration<DefaultMutableTreeNode> tables = pattern.children();
                            while (tables.hasMoreElements()) {
                                DefaultMutableTreeNode table = tables.nextElement();
                                if (table.toString().contains(key)) {
                                    CheckNode newTable = new CheckNode(table.toString());
                                    newPattern.add(newTable);
                                }
                            }
                        } else {
                            Enumeration<DefaultMutableTreeNode> tables = pattern.children();
                            List<String> list = new LinkedList<>();
                            while (tables.hasMoreElements()) {
                                DefaultMutableTreeNode table = tables.nextElement();
                                if (table.toString().contains(key)) {
                                    list.add(table.toString());
                                }
                            }
                            if (list.size() > 0) {
                                CheckNode newPattern = new CheckNode(pattern.toString());
                                list.forEach(v -> {
                                    newPattern.add(new CheckNode(v));
                                });
                                newDataBase.add(newPattern);
                                rootNode.add(newDataBase);
                            }
                        }
                    }
                }
            }
        } else {
            //查找数据库名称是否匹配上
            getChildCheckNode(rootNode, key, root);
        }
        return new Pair<>(new JCheckBoxTree(rootNode), rootNode);
    }

    private static void getChildCheckNode(CheckNode result, String key, CheckNode target) {
        //查找数据库名称是否匹配上
        Enumeration<DefaultMutableTreeNode> enumeration = target.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode database = enumeration.nextElement();
            //不匹配就删除
            CheckNode newDataBase = new CheckNode(database.toString());
            if (database.toString().contains(key)) {
                result.add(newDataBase);
                Enumeration<DefaultMutableTreeNode> children = database.children();
                while (children.hasMoreElements()) {
                    DefaultMutableTreeNode table = children.nextElement();
                    if (table.toString().contains(key)) {
                        CheckNode newTable = new CheckNode(table.toString());
                        newDataBase.add(newTable);
                    }
                }
            } else {
                //如果数据库没有匹配到key，则搜索表
                Enumeration<DefaultMutableTreeNode> children = database.children();
                List<String> list = new LinkedList<>();
                while (children.hasMoreElements()) {
                    DefaultMutableTreeNode table = children.nextElement();
                    if (table.toString().contains(key)) {
                        list.add(table.toString());
                    }
                }
                if (list.size() > 0) {
                    result.add(newDataBase);
                    list.forEach(v -> {
                        newDataBase.add(new CheckNode(v));
                    });
                }
            }
        }
    }


    private class CheckRenderer extends JPanel implements TreeCellRenderer {
        private static final long serialVersionUID = 1L;

        protected JCheckBox check;

        protected TreeLabel label;

        public CheckRenderer() {
            setLayout(null);
            add(check = new JCheckBox());
            check.setBackground(Color.WHITE);
            add(label = new TreeLabel());
            label.setForeground(UIManager.getColor("Tree.textForeground"));
        }

        /**
         * 改变的节点的为JLabel和JChekBox的组合
         */
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean isSelected, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {
            String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, hasFocus);
            setEnabled(tree.isEnabled());
            check.setSelected(((CheckNode) value).isSelected());
            label.setFont(tree.getFont());
            label.setText(stringValue);
            label.setSelected(isSelected);
            label.setFocus(hasFocus);
            DataBaseAssembly dataBaseAssembly = DataBaseAssemblyFactory.get(CommonConstant.DATA_BASE_TYPE);
            List<ImageIcon> list = dataBaseAssembly.image();
            if (leaf && (((CheckNode) value).getParent() != null) && ((CheckNode) value).getParent().getParent() != null) {
                label.setIcon(list.size() == 4 ? list.get(3) : list.get(2));
            } else if (((CheckNode) value).isRoot()) {
                label.setIcon(list.get(0));
            } else {
                label.setIcon(list.get(1));
                if (((CheckNode) value).getLevel() == 2) {
                    if (Objects.nonNull(list.get(2))) {
                        label.setIcon(list.get(2));
                    }
                }
            }
            return this;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension dCheck = check.getPreferredSize();
            Dimension dLabel = label.getPreferredSize();
            return new Dimension(dCheck.width + dLabel.width,
                    (Math.max(dCheck.height, dLabel.height)));
        }

        @Override
        public void doLayout() {
            Dimension dCheck = check.getPreferredSize();
            Dimension dLabel = label.getPreferredSize();
            int yCheck = 0;
            int yLabel = 0;
            if (dCheck.height < dLabel.height) {
                yCheck = (dLabel.height - dCheck.height) / 2;
            } else {
                yLabel = (dCheck.height - dLabel.height) / 2;
            }
            check.setLocation(0, yCheck);
            check.setBounds(0, yCheck, dCheck.width, dCheck.height);
            label.setLocation(dCheck.width, yLabel);
            label.setBounds(dCheck.width, yLabel, dLabel.width,
                    dLabel.height);
        }

        @Override
        public void setBackground(Color color) {
            if (color instanceof ColorUIResource) {
                color = null;
            }
            super.setBackground(color);
        }

        private class TreeLabel extends JLabel {
            private static final long serialVersionUID = 1L;

            private boolean isSelected;

            private boolean hasFocus;

            public TreeLabel() {
            }

            @Override
            public void setBackground(Color color) {
                if (color instanceof ColorUIResource) {
                    color = null;
                }
                super.setBackground(color);
            }

            @Override
            public void paint(Graphics g) {
                String str;
                if ((str = getText()) != null) {
                    if (0 < str.length()) {
                        if (isSelected) {
                            g.setColor(UIManager
                                    .getColor("Tree.selectionBackground"));
                        } else {
                            g.setColor(UIManager
                                    .getColor("Tree.textBackground"));
                        }
                        Dimension d = getPreferredSize();
                        int imageOffset = 0;
                        Icon currentI = getIcon();
                        if (currentI != null) {
                            imageOffset = currentI.getIconWidth()
                                    + Math.max(0, getIconTextGap() - 1);
                        }
                        g.fillRect(imageOffset, 0, d.width - 1 - imageOffset,
                                d.height);
                        if (hasFocus) {
                            g.setColor(UIManager
                                    .getColor("Tree.selectionBorderColor"));
                            g.drawRect(imageOffset, 0, d.width - 1
                                    - imageOffset, d.height - 1);
                        }
                    }
                }
                super.paint(g);
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension retDimension = super.getPreferredSize();
                if (retDimension != null) {
                    retDimension = new Dimension(retDimension.width + 3,
                            retDimension.height);
                }
                return retDimension;
            }

            public void setSelected(boolean isSelected) {
                this.isSelected = isSelected;
            }

            public void setFocus(boolean hasFocus) {
                this.hasFocus = hasFocus;
            }
        }
    }

    public static class CheckNode extends DefaultMutableTreeNode {

        private static final long serialVersionUID = 1L;

        public final static int SINGLE_SELECTION = 0;

        public final static int DIG_IN_SELECTION = 4;

        protected int selectionMode;

        protected boolean isSelected;

        public CheckNode() {
            this(null);
        }

        public CheckNode(Object userObject) {
            this(userObject, true, false);
        }

        public CheckNode(Object userObject, boolean allowsChildren,
                         boolean isSelected) {
            super(userObject, allowsChildren);
            this.isSelected = isSelected;
            setSelectionMode(DIG_IN_SELECTION);
        }

        public void setSelectionMode(int mode) {
            selectionMode = mode;
        }

        public int getSelectionMode() {
            return selectionMode;
        }

        @Override
        public CheckNode getParent() {
            return ((CheckNode) super.getParent());
        }

        /**
         * 选中父节点时也级联选中子节点
         *
         * @param isSelected
         */
        @SuppressWarnings("unchecked")
        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
            if ((selectionMode == DIG_IN_SELECTION) && (children != null)) {
                Enumeration e = children.elements();
                while (e.hasMoreElements()) {
                    CheckNode node = (CheckNode) e.nextElement();
                    node.setSelected(isSelected);
                }
            }
        }

        public boolean isSelected() {
            return isSelected;
        }
    }
}
