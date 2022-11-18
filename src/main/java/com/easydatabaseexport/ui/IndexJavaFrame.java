package com.easydatabaseexport.ui;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.entities.ErrorMsg;
import com.easydatabaseexport.entities.IndexConfig;
import com.easydatabaseexport.enums.DataBaseType;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.ui.component.IndexMenu;
import com.easydatabaseexport.ui.component.IpPortJComboBox;
import com.easydatabaseexport.ui.component.ThreadDiag;
import com.easydatabaseexport.ui.detector.ConnectDetector;
import com.easydatabaseexport.util.AESCoder;
import com.easydatabaseexport.util.CheckUpdateUtil;
import com.easydatabaseexport.util.FileIniRead;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.SwingUtils;
import com.mysql.cj.util.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.codec.binary.Base64;

import javax.swing.*;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

/**
 * IndexJavaFrame
 *
 * @author lzy
 * @date 2021/11/1 15:03
 **/
@Log
public class IndexJavaFrame {

    private static final String IP_PORT_TIPS = "<html>请填写ip（域名）和端口。<br/>① ip+端口格式：x.x.x.x:xxx（例：192.168.0.1:3306） " +
            "<br/>② 域名+端口格式：xxx.xxx.xxx...:xxx（例：www.baidu.com:3306）";

    private static final Vector<DataBaseType> DATA_BASE_TYPES = new Vector<>(Arrays.asList(DataBaseType.values()));

    static final ImageIcon[] IMAGES = new ImageIcon[DATA_BASE_TYPES.size()];

    static {
        for (int i = 0; i < DATA_BASE_TYPES.size(); i++) {
            IMAGES[i] = new ImageIcon(IndexJavaFrame.class.getResource("/images/" + DATA_BASE_TYPES.get(i).name().toLowerCase() + ".png"));
            if (IMAGES[i] != null) {
                IMAGES[i].setDescription(DATA_BASE_TYPES.get(i).name());
            }
        }
    }

    public static void connectFrame() throws Exception {
        JFrame jFrame = new JFrame("连接");
        //添加配置文件逻辑判断
        List<String> configList = FileIniRead.getIniConf();

        byte[] key = AESCoder.readFileReturnByte();

        //数据库类型
        JComboBox<DataBaseType> dataBaseType = new JComboBox<DataBaseType>(DATA_BASE_TYPES);
        ComboBoxRenderer renderer = new ComboBoxRenderer();
        renderer.setPreferredSize(new Dimension(20, 29));
        dataBaseType.setRenderer(renderer);
        dataBaseType.setPreferredSize(new Dimension(230, 24));

        JLabel type = new JLabel("<html><span style='color:red'>*</span>数据库类型</html>");

        JLabel url = new JLabel("<html><span style='color:red'>*</span>连接地址【格式为ip:port】</html>");

        JLabel database = new JLabel("数据库");
        JTextField databaseN = new JTextField(20);
        databaseN.setPreferredSize(new Dimension(230, 24));
        databaseN.setText("");

        JLabel username = new JLabel("<html><span style='color:red'>*</span>用户名</html>");
        JTextField userN = new JTextField(20);
        userN.setPreferredSize(new Dimension(230, 24));
        userN.setText("root");

        JLabel passwd = new JLabel("密码");
        JPasswordField passwdP = new JPasswordField(20);
        passwdP.setPreferredSize(new Dimension(230, 24));
        passwdP.setText("root");

        JCheckBox jcb = new JCheckBox("显示密码");

        JButton testBtn = new JButton("测试连接");
        JButton confirmBtn = new JButton("确认连接");

        //批量添加 鼠标移入变手形
        SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, confirmBtn, testBtn, jcb);

        JPanel jPanel = new JPanel();
        jPanel.add(url);
        //如果 没有配置文件 则显示该输入框，如果有配置信息，则隐藏
        JTextField urlN = new JTextField(20);
        urlN.setPreferredSize(new Dimension(230, 24));
        //判断是否读取配置文件
        if (configList.isEmpty()) {
            urlN.setText("127.0.0.1:3306");
            databaseN.setText("");
            userN.setText("root");
            passwdP.setText("root");
            jPanel.add(urlN);
        } else {
            Vector<IndexConfig> ipPort = new Vector<>();
            int i = 0;
            Map<String, String> map = new LinkedHashMap<>();
            for (String config : configList) {
                StringBuilder stringBuilder = new StringBuilder();
                String[] strings = config.split("\\|");

                String ip;
                String group = "";
                if (strings[0].contains("/")) {
                    String[] names = strings[0].split("/");
                    group = names[0];
                    ip = names[1];
                } else {
                    ip = strings[0];
                }
                if (strings.length > 5) {
                    stringBuilder.append(strings[0]).append(CommonConstant.COLON).append(strings[1]).append(CommonConstant.SEPARATOR).append(strings[2]).append(CommonConstant.SEPARATOR).append(strings[3]).append(CommonConstant.SEPARATOR).append(strings[4]);
                    map.put(stringBuilder.toString(), strings[5]);
                    ipPort.add(new IndexConfig(ip + CommonConstant.COLON + strings[1], strings[2], strings[3], strings[4], strings[5], group));
                } else {
                    stringBuilder.append(strings[0]).append(CommonConstant.COLON).append(strings[1]).append(CommonConstant.SEPARATOR).append(strings[2]).append(CommonConstant.SEPARATOR).append(strings[3]);
                    map.put(stringBuilder.toString(), strings[4]);
                    ipPort.add(new IndexConfig(ip + CommonConstant.COLON + strings[1], strings[2], strings[3], "", strings[4], group));
                }
                if (i == 0) {
                    urlN.setText(ip + CommonConstant.COLON + strings[1]);
                    databaseN.setText(strings[2]);
                    userN.setText(strings[3]);
                    DataBaseType dataType = DataBaseType.matchType(strings[4]);
                    dataBaseType.setSelectedItem(dataType);
                    if (strings.length > 5) {
                        passwdP.setText(new String(AESCoder.decrypt(Base64.decodeBase64(strings[5]), key)));
                    } else {
                        passwdP.setText(new String(AESCoder.decrypt(Base64.decodeBase64(strings[4]), key)));
                    }
                }
                i++;
            }
            IpPortJComboBox jComboBox = new IpPortJComboBox(ipPort);
            //设置宽度
            jComboBox.setPreferredSize(new Dimension(230, 24));
            // 添加条目选中状态改变的监听器
            jComboBox.addItemListener(e -> {
                // 只处理选中的状态
                if (Objects.isNull(jComboBox.getSelectedItem())) {
                    return;
                }
                urlN.setText(jComboBox.getSelectedItem().toString());
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jComboBox.getSelectedItem() instanceof IndexConfig) {
                        String mapKey = ((IndexConfig) jComboBox.getSelectedItem()).toMyString();
                        String config = map.get(Objects.requireNonNull(mapKey));
                        if (!StringUtils.isNullOrEmpty(config)) {
                            String[] strings = mapKey.split("\\|");
                            databaseN.setText(strings[1]);
                            userN.setText(strings[2]);
                            if (strings.length == 4) {
                                DataBaseType dataType = DataBaseType.matchType(strings[3]);
                                dataBaseType.setSelectedItem(dataType);
                            } else {
                                dataBaseType.setSelectedIndex(0);
                            }
                            try {
                                passwdP.setText(new String(AESCoder.decrypt(Base64.decodeBase64(config), key)));
                            } catch (Exception exception) {
                                LogManager.writeLogFile(exception, log);
                            }
                        }
                    }
                }
            });
            jPanel.add(jComboBox);
            jComboBox.setToolTipText(IP_PORT_TIPS);
        }

        JMenuBar menuBar = IndexMenu.IndexMenu(jFrame);

        jPanel.add(type);
        jPanel.add(dataBaseType);
        jPanel.add(database);
        jPanel.add(databaseN);
        jPanel.add(username);
        jPanel.add(userN);
        jPanel.add(passwd);
        jPanel.add(jcb);
        jPanel.add(passwdP);
        jPanel.add(testBtn);
        jPanel.add(confirmBtn);

        final JSplitPane totalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        totalSplitPane.setTopComponent(menuBar);
        totalSplitPane.setBottomComponent(jPanel);
        //去除菜单和内容的分割线
        totalSplitPane.setDividerSize(0);
        totalSplitPane.setEnabled(true);
        jFrame.add(totalSplitPane);

        jFrame.setSize(265, 380);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setResizable(false);
        //居中
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        ActionListener actionListenerRe = e -> {
            String originUrlText = urlN.getText().trim();
            String originDatabaseText = databaseN.getText().trim();
            String userText = userN.getText().trim();
            String passwdText = new String(passwdP.getPassword());
            CommonConstant.DATA_BASE_TYPE = Objects.requireNonNull(dataBaseType.getSelectedItem()).toString();
            ConnectDetector connectTest = new ConnectDetector(CommonConstant.DATA_BASE_TYPE);
            Map<String, ErrorMsg> map = connectTest.connection(originUrlText, originDatabaseText, userText, passwdText, false);

            SwingUtilities.invokeLater(() -> {
                if (map.containsKey(CommonConstant.SUCCESS)) {
                    ErrorMsg msg = map.get(CommonConstant.SUCCESS);
                    JOptionPane.showMessageDialog(null, msg.getMessage(), msg.getTitle(), msg.getMessageType(), msg.getImgIcon());
                } else {
                    ErrorMsg msg = map.get(CommonConstant.FAIL);
                    JOptionPane.showMessageDialog(null, msg.getMessage(), msg.getTitle(), msg.getMessageType());
                }
            });
        };
        testBtn.addActionListener(actionListenerRe);

        confirmBtn.addActionListener(e -> {
            Runnable runnable = new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    String originUrlText = urlN.getText().trim();
                    String originDatabaseText = databaseN.getText().trim();
                    String userText = userN.getText().trim();
                    String passwdText = new String(passwdP.getPassword());
                    CommonConstant.DATA_BASE_TYPE = Objects.requireNonNull(dataBaseType.getSelectedItem()).toString();
                    ConnectDetector testConnect = new ConnectDetector(CommonConstant.DATA_BASE_TYPE);
                    Map<String, ErrorMsg> map = testConnect.connection(originUrlText, originDatabaseText, userText, passwdText, true);
                    if (map.containsKey(CommonConstant.SUCCESS)) {
                        jFrame.dispose();
                        String stringBuilder = originUrlText.replaceAll(CommonConstant.COLON, CommonConstant.SEPARATOR) + CommonConstant.SEPARATOR +
                                originDatabaseText + CommonConstant.SEPARATOR +
                                userText + CommonConstant.SEPARATOR +
                                CommonConstant.DATA_BASE_TYPE + " = " +
                                //加密哦！！！
                                Base64.encodeBase64String(AESCoder.encrypt(passwdText.getBytes(), key));
                        FileOperateUtil.saveIniFile(FileOperateUtil.getSavePath() + "database.ini", stringBuilder);
                        //需要在swing线程中运行
                        Thread.sleep(1000);
                        SwingUtilities.invokeLater(() -> {
                            ConnectJavaFrame javaFrame = new ConnectJavaFrame();
                            javaFrame.mainFrame();
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            ErrorMsg msg = map.get(CommonConstant.FAIL);
                            JOptionPane.showMessageDialog(null, msg.getMessage(), msg.getTitle(), msg.getMessageType());
                        });
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
            (new ThreadDiag(new JFrame(), thread, "正在加载中，请等待......")).start();
        });

        jcb.addItemListener(e -> {
            //得到产生的事件，这里只有复选框所以可以强制类型转换。
            JCheckBox jcb1 = (JCheckBox) e.getItem();
            // 如果被选中了，则显示明文密码
            if (jcb1.isSelected()) {
                //显示密码
                passwdP.setEchoChar((char) 0);
            } else {
                //隐藏密码
                passwdP.setEchoChar('•');
            }
        });

        urlN.setToolTipText(IP_PORT_TIPS);
        databaseN.setToolTipText("<html>请填写数据库。<br/>*** MySQL、SQLite非必填<br/>*** Oracle填写对应Navicat的服务名<html/>");
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(5000);

        //检查更新
        CheckUpdateUtil.check();
    }


    static class ComboBoxRenderer extends JLabel implements ListCellRenderer {
        public ComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        /*
         * This method finds the image and text corresponding
         * to the selected value and returns the label, set up
         * to display the text and image.
         */
        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            //Get the selected index. (The index param isn't
            //always valid, so just use the value.)
            int selectedIndex = DATA_BASE_TYPES.indexOf(DataBaseType.matchType(String.valueOf(value)));

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            //Set the icon and text. If icon was null, say so.
            ImageIcon icon = IMAGES[selectedIndex];
            String iconName = String.valueOf(value);
            setIcon(icon);
            if (icon != null) {
                setText(iconName);
                setFont(list.getFont());
            }
            return this;
        }
    }

}
