package com.easydatabaseexport.ui.component;

import com.easydatabaseexport.enums.DataBaseType;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.util.Vector;

/**
 * ComboBoxRenderer
 *
 * @author lzy
 * @date 2022/11/30 17:55
 **/
public class ComboBoxRenderer<T> extends JLabel implements ListCellRenderer<T> {

    private final ImageIcon[] images;
    private final Vector<T> data;

    public ComboBoxRenderer(Vector<T> data, ImageIcon[] images) {
        this.data = data;
        this.images = images;
        setOpaque(true);
        setHorizontalAlignment(SwingConstants.LEFT);
        setVerticalAlignment(SwingConstants.CENTER);
    }

    /**
     * This method finds the image and text corresponding
     * to the selected value and returns the label, set up
     * to display the text and image.
     **/
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        //Get the selected index. (The index param isn't
        //always valid, so just use the value.)
        int selectedIndex = data.indexOf(DataBaseType.matchType(String.valueOf(value)));

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        //Set the icon and text. If icon was null, say so.
        ImageIcon icon = images[selectedIndex];
        String iconName = String.valueOf(value);
        setIcon(icon);
        if (icon != null) {
            setText(iconName);
            setFont(list.getFont());
        }
        return this;
    }
}