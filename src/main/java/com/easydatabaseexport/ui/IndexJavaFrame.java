package com.easydatabaseexport.ui;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.PatternConstant;
import com.easydatabaseexport.entities.ErrorMsg;
import com.easydatabaseexport.entities.IndexConfig;
import com.easydatabaseexport.entities.OSDetector;
import com.easydatabaseexport.enums.DataBaseType;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.ui.component.ComboBoxRenderer;
import com.easydatabaseexport.ui.component.IndexMenu;
import com.easydatabaseexport.ui.component.IpPortJComboBox;
import com.easydatabaseexport.ui.component.ThreadDiag;
import com.easydatabaseexport.ui.detector.ConnectDetector;
import com.easydatabaseexport.util.AESCoder;
import com.easydatabaseexport.util.CheckUpdateUtil;
import com.easydatabaseexport.util.FileIniRead;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.StringUtil;
import com.easydatabaseexport.util.SwingUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import org.apache.commons.codec.binary.Base64;

import javax.swing.*;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
@Log4j
public class IndexJavaFrame {

    private static final String IP_PORT_TIPS = "<html>请填写ip（域名）和端口。<br/>① ip+端口格式：x.x.x.x:xxx（例：192.168.0.1:3306） " +
            "<br/>② 域名+端口格式：xxx.xxx.xxx...:xxx（例：www.baidu.com:3306）";

    private static final Vector<DataBaseType> DATA_BASE_TYPES = new Vector<>(Arrays.asList(DataBaseType.values()));

    private static final ImageIcon[] IMAGES = new ImageIcon[DATA_BASE_TYPES.size()];

    /**
     * 原始url
     **/
    private static String originUrlText;
    /**
     * 数据库
     **/
    private static String originDatabaseText;
    /**
     * 用户名
     **/
    private static String userText;
    /**
     * 密码
     **/
    private static String passwdText;
    /**
     * 用户选择索引
     **/
    private static int selectedIndex = 0;
    /**
     * 输入框宽度
     **/
    private static final int WIDTH = 230;
    /**
     * 输入框高度
     **/
    private static final int HEIGHT = 26;

    static {
        for (int i = 0; i < DATA_BASE_TYPES.size(); i++) {
            IMAGES[i] = new ImageIcon(Objects.requireNonNull(IndexJavaFrame.class.getResource("/images/" + DATA_BASE_TYPES.get(i).name().toLowerCase() + ".png")));
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

        SwingUtils.changeLogo(jFrame);

        //数据库类型
        JComboBox<DataBaseType> dataBaseType = new JComboBox<DataBaseType>(DATA_BASE_TYPES);
        ComboBoxRenderer<DataBaseType> renderer = new ComboBoxRenderer<DataBaseType>(DATA_BASE_TYPES, IMAGES);
        renderer.setPreferredSize(new Dimension(20, 27));
        dataBaseType.setRenderer(renderer);
        dataBaseType.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JLabel type = new JLabel("<html><span style='color:red'>*</span>数据库类型</html>");

        JLabel url = new JLabel("<html><span style='color:red'>*</span>连接地址【格式为ip:port】</html>");

        JLabel database = new JLabel("数据库");
        JTextField databaseN = new JTextField();
        databaseN.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        databaseN.setText("");

        JLabel username = new JLabel("<html><span style='color:red'>*</span>用户名</html>");
        JTextField userN = new JTextField();
        userN.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        userN.setText("root");

        JLabel passwd = new JLabel("密码");
        JPasswordField passwdP = new JPasswordField();
        passwdP.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        passwdP.setText("root");

        JCheckBox jcb = new JCheckBox("显示密码");

        JButton testBtn = new JButton("测试连接");
        JButton confirmBtn = new JButton("确认连接");

        //批量添加 鼠标移入变手形
        SwingUtils.addHandCursorLister(Cursor.HAND_CURSOR, confirmBtn, testBtn, jcb);

        JPanel jPanel = new JPanel();
        jPanel.add(url);
        //如果 没有配置文件 则显示该输入框，如果有配置信息，则隐藏
        JTextField urlN = new JTextField();
        urlN.setPreferredSize(new Dimension(WIDTH, HEIGHT));
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
                String[] strings = config.split(PatternConstant.COMMON_SPLIT);

                String ip;
                String group = "";
                boolean isSqlite = (OSDetector.isMac() || OSDetector.isLinux()) && strings[3].equals(DataBaseType.SQLITE.name());
                if (strings[0].contains("/")) {
                    if (isSqlite) {
                        ip = strings[0];
                    } else {
                        String[] names = strings[0].split("/");
                        group = names[0];
                        ip = names[1];
                    }
                } else {
                    ip = strings[0];
                }
                if (strings.length > 5) {
                    stringBuilder.append(strings[0]).append(CommonConstant.COLON).append(strings[1]).append(CommonConstant.SEPARATOR).append(strings[2]).append(CommonConstant.SEPARATOR).append(strings[3]).append(CommonConstant.SEPARATOR).append(strings[4]);
                    map.put(stringBuilder.toString(), strings[5]);
                    ipPort.add(new IndexConfig(ip + CommonConstant.COLON + strings[1], strings[2], strings[3], strings[4], strings[5], group));
                } else {
                    if (isSqlite) {
                        stringBuilder.append(strings[0]).append(CommonConstant.SEPARATOR).append(strings[1]).append(CommonConstant.SEPARATOR)
                                .append(strings[2]).append(CommonConstant.SEPARATOR).append(strings[3]);
                        map.put(stringBuilder.toString(), strings[4]);
                        ipPort.add(new IndexConfig(ip, strings[1], strings[2], strings[3], strings[4], group, true));
                    } else {
                        stringBuilder.append(strings[0]).append(CommonConstant.COLON).append(strings[1]).append(CommonConstant.SEPARATOR)
                                .append(strings[2]).append(CommonConstant.SEPARATOR).append(strings[3]);
                        map.put(stringBuilder.toString(), strings[4]);
                        ipPort.add(new IndexConfig(ip + CommonConstant.COLON + strings[1], strings[2], strings[3], "", strings[4], group));
                    }
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
            jComboBox.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            // 添加条目选中状态改变的监听器
            jComboBox.addItemListener(e -> {
                // 只处理选中的状态
                if (Objects.isNull(jComboBox.getSelectedItem())) {
                    return;
                }
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (jComboBox.getSelectedItem() instanceof IndexConfig) {
                        urlN.setText(jComboBox.getSelectedItem().toString());
                        String mapKey = ((IndexConfig) jComboBox.getSelectedItem()).toMyString();
                        String config = map.get(Objects.requireNonNull(mapKey));
                        if (!StringUtil.isEmpty(config)) {
                            String[] strings = mapKey.split(PatternConstant.COMMON_SPLIT);
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
            //还原选择索引
            jComboBox.setSelectedIndex(selectedIndex);
            jComboBox.setToolTipText(IP_PORT_TIPS);
            jPanel.add(jComboBox);
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
        final JSplitPane indexSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel btnPanel = new JPanel();
        btnPanel.add(testBtn);
        btnPanel.add(confirmBtn);
        indexSplitPane.setDividerSize(0);
        indexSplitPane.setDividerLocation(280);
        indexSplitPane.setOneTouchExpandable(false);
        indexSplitPane.setEnabled(true);
        indexSplitPane.setTopComponent(jPanel);
        indexSplitPane.setBottomComponent(btnPanel);

        final JSplitPane totalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        totalSplitPane.setTopComponent(menuBar);
        totalSplitPane.setBottomComponent(indexSplitPane);
        //去除菜单和内容的分割线
        totalSplitPane.setDividerSize(0);
        totalSplitPane.setEnabled(true);

        ActionListener actionListenerRe = e -> {
            Map<String, ErrorMsg> map = changeConnectParameter(jPanel, urlN, databaseN, userN, passwdP, dataBaseType, false);

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
                    Map<String, ErrorMsg> map = changeConnectParameter(jPanel, urlN, databaseN, userN, passwdP, dataBaseType, true);
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
        userN.setToolTipText("<html>请填写用户名。<br/>*** SQLite非必填<html/>");
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(5000);

        jFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int resizeWidth = WIDTH;
                if (265 != jFrame.getWidth() || jFrame.getHeight() != 390) {
                    resizeWidth = jFrame.getWidth() - 30;
                }
                for (Component component : jPanel.getComponents()) {
                    if (component instanceof JTextField) {
                        JTextField jTextField = (JTextField) component;
                        jTextField.setPreferredSize(new Dimension(resizeWidth, HEIGHT));
                    } else if (component instanceof JComboBox) {
                        JComboBox jComboBox = (JComboBox) component;
                        jComboBox.setPreferredSize(new Dimension(resizeWidth, HEIGHT));
                    }
                }
                indexSplitPane.setDividerLocation(jFrame.getHeight() - 110);
                btnPanel.setSize(251, 51);
            }
        });

        jFrame.add(totalSplitPane);

        jFrame.setSize(265, 390);
        jFrame.setMinimumSize(new Dimension(265, 390));
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setResizable(true);
        //居中
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        //检查更新
        CheckUpdateUtil.check();
    }

    private static Map<String, ErrorMsg> changeConnectParameter(JPanel jPanel, JTextField urlN, JTextField databaseN,
                                                                JTextField userN, JPasswordField passwdP, JComboBox<DataBaseType> dataBaseType, boolean confirm) {
        Component component = jPanel.getComponent(1);
        if (component instanceof JTextField) {
            originUrlText = urlN.getText().trim();
        } else {
            IpPortJComboBox comboBox = (IpPortJComboBox) component;
            if (confirm) {
                selectedIndex = comboBox.getSelectedIndex();
            }
            originUrlText = comboBox.getText().trim();
        }
        originDatabaseText = databaseN.getText().trim();
        userText = userN.getText().trim();
        passwdText = new String(passwdP.getPassword());
        CommonConstant.DATA_BASE_TYPE = Objects.requireNonNull(dataBaseType.getSelectedItem()).toString();
        ConnectDetector connect = new ConnectDetector(CommonConstant.DATA_BASE_TYPE);
        return connect.connection(originUrlText, originDatabaseText, userText, passwdText, confirm);
    }


}
