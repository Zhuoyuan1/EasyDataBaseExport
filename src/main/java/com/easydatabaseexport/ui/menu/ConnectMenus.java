package com.easydatabaseexport.ui.menu;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.Objects;

/**
 * @description: ConnectMenus 连接右键菜单
 * @author: lzy
 * @date: 2023/7/11 19:44
 **/
public class ConnectMenus extends JPopupMenu {

    public ConnectMenus(JTree tree) {
        add(new JMenuItem("展开所有")).addActionListener(event -> {
            expandTree(tree, Objects.requireNonNull(tree.getSelectionPath()));
        });

        add(new JMenuItem("折叠所有")).addActionListener(event -> {
            collapseTree(tree, Objects.requireNonNull(tree.getSelectionPath()));
        });

    }

    private void expandTree(JTree tree, TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() > 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandTree(tree, path);
            }
        }
        tree.expandPath(parent);
    }

    private void collapseTree(JTree tree, TreePath parent) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
        if (node.getChildCount() > 0) {
            for (Enumeration<?> e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                collapseTree(tree, path);
            }
        }
        if (!node.isRoot()) {
            tree.collapsePath(parent);
        }
    }

}
