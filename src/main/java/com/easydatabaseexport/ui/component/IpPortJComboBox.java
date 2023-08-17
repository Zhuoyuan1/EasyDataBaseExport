package com.easydatabaseexport.ui.component;

import com.easydatabaseexport.entities.IndexConfig;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

/**
 * IpPortJComboBox
 *
 * @author lzy
 * @date 2022/11/1 11:44
 **/
public class IpPortJComboBox extends JComboBox<IndexConfig> {
    private final JTextField comboBoxTextField = (JTextField) this.getEditor().getEditorComponent();
    private DefaultComboBoxModel<IndexConfig> initComboBoxModel = null;
    private final JComboBox<IndexConfig> thisComboBox = this;
    private Vector<IndexConfig> content = null;
    //记录输入索引位置
    private static int index = 0;


    public IpPortJComboBox(Vector<IndexConfig> items) {
        super(items);
        initMyJComboBox(items);
    }

    private void initMyJComboBox() {
        this.setEditable(true);
        openMatchKey();
    }

    private void initMyJComboBox(Vector<IndexConfig> tempcontent) {
        initMyJComboBox();
        updateDefaultModel(tempcontent);
    }

    private void setMatchKey(boolean isAction, char c) {
        StringBuilder str = new StringBuilder(getText().trim());
        StringBuilder str2 = new StringBuilder(getText().trim());
        //由于keyListen监听的是键盘输入时，就执行，因此JTextField并没有被赋新的值
        index = comboBoxTextField.getCaretPosition();
        //这里匹配做了拼接
        if (!isAction) {
            str2.insert(index, c);
        }
        if ("".contentEquals(str)) {
            recoverModel();
        } else {
            setItems(getMatchKey(str2.toString()));
        }
        refresh(str.toString());
    }

    private void openMatchKey() {
        comboBoxTextField.addKeyListener(new KeyListener() {
            //当键盘按下时触发
            @Override
            public void keyPressed(KeyEvent arg0) {
            }

            //释放密钥时调用
            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            //使用该方法原因：keyTyped只要键盘输入，就触发并且优先级高
            //键入键后调用
            @Override
            public void keyTyped(KeyEvent arg0) {
                //非动作键，输入关键字
                char c = arg0.getKeyChar();
                //判断输入的关键字来决定是否开启自定义匹配；否则，正常匹配
                boolean isMatch = c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z'
                        || c >= '0' && c <= '9' || c == ':' || c == '.';
                if (isMatch) {
                    //自定义匹配
                    setMatchKey(arg0.isActionKey(), c);
                } else {
                    //正常匹配
                    setMatchKey(true, c);
                }
                //没有匹配到结果，就不展示
                if (thisComboBox.getModel().getSize() > 0) {
                    thisComboBox.showPopup();
                }
            }
        });
    }

    private Vector<IndexConfig> getMatchKey(String target) {
        Vector<IndexConfig> lenovo = new Vector<IndexConfig>();
        for (int i = 0; i < content.size(); i++) {
            if (content.elementAt(i).getUrl().contains(target)) {
                lenovo.add(content.elementAt(i));
            }
        }
        return lenovo;
    }

    public void setText(String text) {
        comboBoxTextField.setText(text);
    }

    public String getText() {
        return comboBoxTextField.getText();
    }

    public void setItems(Vector<IndexConfig> items) {
        this.setModel(new DefaultComboBoxModel<IndexConfig>(items));
    }

    public void updateDefaultModel(Vector<IndexConfig> newContent) {
        content = newContent;
        initComboBoxModel = new DefaultComboBoxModel<IndexConfig>(content);
        this.setModel(initComboBoxModel);
    }

    private void recoverModel() {
        this.setModel(initComboBoxModel);
    }

    private void refresh(String text) {
        this.setSelectedIndex(-1);
        comboBoxTextField.setText(text);
        comboBoxTextField.setCaretPosition(index);
    }

}
