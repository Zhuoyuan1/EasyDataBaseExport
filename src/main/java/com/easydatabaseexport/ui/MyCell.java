package com.easydatabaseexport.ui;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.EventObject;

/**
 * MyCell
 *
 * @author lzy
 * @date 2023/3/3 17:18
 **/
public class MyCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return false;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return initPanel(table, value, isSelected);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return initPanel(table, value, isSelected);
    }

    private JPanel initPanel(JTable table, Object value, boolean isSelected) {
        JPanel jp = new JPanel(new BorderLayout());
        jp.add(new JLabel(value.toString()), BorderLayout.WEST);
        jp.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        jp.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return jp;
    }
}
