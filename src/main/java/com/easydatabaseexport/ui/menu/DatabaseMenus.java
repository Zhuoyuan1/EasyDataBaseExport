package com.easydatabaseexport.ui.menu;

import com.easydatabaseexport.ui.component.JCheckBoxTree;
import com.easydatabaseexport.ui.export.ExcelActionListener;
import com.easydatabaseexport.ui.export.HtmlActionListener;
import com.easydatabaseexport.ui.export.MarkdownActionListener;
import com.easydatabaseexport.ui.export.PdfActionListener;
import com.easydatabaseexport.ui.export.WordSuperActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

/**
 * @description: DatabaseMenus 数据库右键菜单
 * @author: lzy
 * @date: 2023/7/11 19:45
 **/
public class DatabaseMenus extends JPopupMenu {

    public DatabaseMenus(JTree tree) {
        add(new JMenuItem("展开")).addActionListener(event -> {
            tree.expandPath(tree.getSelectionPath());
        });

        add(new JMenuItem("折叠")).addActionListener(event -> {
            tree.collapsePath(tree.getSelectionPath());
        });

        JMenu jMenu = new JMenu("导出");

        jMenu.add(new JMenuItem("生成Excel")).addActionListener(new ExcelActionListener((JCheckBoxTree.CheckNode) tree.getModel().getRoot()));

        jMenu.add(new JMenuItem("生成Word")).addActionListener(new WordSuperActionListener((JCheckBoxTree.CheckNode) tree.getModel().getRoot()));

        jMenu.add(new JMenuItem("生成Markdown")).addActionListener(new MarkdownActionListener((JCheckBoxTree.CheckNode) tree.getModel().getRoot()));

        jMenu.add(new JMenuItem("生成Html")).addActionListener(new HtmlActionListener((JCheckBoxTree.CheckNode) tree.getModel().getRoot()));

        jMenu.add(new JMenuItem("生成Pdf")).addActionListener(new PdfActionListener((JCheckBoxTree.CheckNode) tree.getModel().getRoot()));

        add(jMenu);
    }

}
