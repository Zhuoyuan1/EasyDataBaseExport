package com.easydatabaseexport.ui.menu;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

/**
 * @description: PatternMenus 模式右键菜单
 * @author: lzy
 * @date: 2023/7/11 19:45
 **/
public class PatternMenus extends JPopupMenu {

    public PatternMenus(JTree tree) {
        add(new JMenuItem("展开")).addActionListener(event -> {
            tree.expandPath(tree.getSelectionPath());
        });

        add(new JMenuItem("折叠")).addActionListener(event -> {
            tree.collapsePath(tree.getSelectionPath());
        });
    }
}
