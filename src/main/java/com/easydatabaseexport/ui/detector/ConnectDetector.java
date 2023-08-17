package com.easydatabaseexport.ui.detector;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.core.DataSource;
import com.easydatabaseexport.entities.ErrorMsg;
import com.easydatabaseexport.enums.DataBaseType;
import com.easydatabaseexport.factory.DataSourceFactory;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.ui.component.JCheckBoxTree;
import com.easydatabaseexport.util.PingUtil;
import com.easydatabaseexport.util.StringUtil;
import lombok.extern.log4j.Log4j;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ConnectDetector
 *
 * @author lzy
 * @date 2021/11/1 15:09
 **/
@Log4j
public class ConnectDetector {

    private static String URL;

    public static DataSource dataSource;
    public static String urlText;
    public static String userText;
    public static String passwdText;
    /**
     * 不需要填写数据库的
     **/
    private static final List<String> EXCLUDE_DATABASE = Arrays.asList(DataBaseType.MYSQL.name(), DataBaseType.SQLITE.name());
    /**
     * 个性化填写ip端口的
     **/
    private static final List<String> EXCLUDE_IPADDR = Collections.singletonList(DataBaseType.SQLITE.name());

    public ConnectDetector(String dataType) {
        //处理URL
        DataBaseType dataBaseType = DataBaseType.matchType(dataType);
        URL = Objects.requireNonNull(dataBaseType).getUrlString();
        //处理数据源
        dataSource = DataSourceFactory.get(dataType);
    }

    /**
     * 测试连接/确认连接
     *
     * @param originUrlText      ip和端口
     * @param originDatabaseText 数据库
     * @param userText           用户名
     * @param passwdText         密码
     * @return java.util.Map<ErrorMsg>
     * @author lzy
     * @date 2021/12/14 11:02
     **/
    public Map<String, ErrorMsg> connection(String originUrlText, String originDatabaseText, String userText, String passwdText, boolean confirm) {
        Map<String, ErrorMsg> map = commonTest(originUrlText, userText, originDatabaseText);
        if (map.size() > 0) {
            return map;
        }
        CommonConstant.ROOT = originUrlText;
        String urlText;
        if (EXCLUDE_IPADDR.contains(CommonConstant.DATA_BASE_TYPE)) {
            urlText = String.format(URL, originUrlText);
        } else {
            String[] ipPort = originUrlText.split(":");
            urlText = String.format(URL, ipPort[0], ipPort[1], originDatabaseText);
            if (!StringUtil.isEmpty(originDatabaseText)) {
                CommonConstant.DATABASE_NAME = originDatabaseText;
            }
        }
        ErrorMsg msg = new ErrorMsg();
        try {
            //获取数据库连接对象
            CommonConstant.connection = dataSource.getCreateConnection(urlText, userText, passwdText);
            URL url = JCheckBoxTree.class.getResource("/images/success.png");
            map.put(CommonConstant.SUCCESS, msg);
            if (confirm) {
                ConnectDetector.urlText = urlText;
                ConnectDetector.userText = userText;
                ConnectDetector.passwdText = passwdText;
                return map;
            }
            ImageIcon success = new ImageIcon(Objects.requireNonNull(url));
            msg.setImgIcon(success);
            msg.setMessage("连接成功！");
            msg.setMessageType(JOptionPane.PLAIN_MESSAGE);
            return map;
        } catch (Exception throwable) {
            msg.setMessage("连接失败，原因：" + Optional.ofNullable(throwable.getCause())
                    .orElse(throwable).getMessage()).setTitle("错误").setMessageType(JOptionPane.ERROR_MESSAGE);
            LogManager.writeLogFile(throwable, log);
            map.put(CommonConstant.FAIL, msg);
            return map;
        } finally {
            //测试连接需要释放资源
            if (!confirm) {
                dataSource.close(CommonConstant.connection);
            }
        }
    }

    /**
     * 校验用户输入信息
     *
     * @param originUrlText      ip和端口
     * @param userText           用户名
     * @param originDatabaseText 数据库
     * @return java.util.Map<ErrorMsg>
     * @author lzy
     * @date 2021/12/14 11:13
     **/
    private Map<String, ErrorMsg> commonTest(String originUrlText, String userText, String originDatabaseText) {
        Map<String, ErrorMsg> map = new HashMap<>(0);
        ErrorMsg msg = new ErrorMsg();
        if (StringUtil.isEmpty(originUrlText)) {
            msg.setMessage("ip和端口必填").setTitle("").setMessageType(JOptionPane.WARNING_MESSAGE);
            map.put(CommonConstant.FAIL, msg);
            return map;
        } else {
            if (!EXCLUDE_IPADDR.contains(CommonConstant.DATA_BASE_TYPE)) {
                //判断字符串是否存在英文字母
                if (CommonConstant.pattern.matcher(originUrlText).find()) {
                    if (!originUrlText.startsWith(PingUtil.LOCALHOST)) {
                        if (!Pattern.matches(CommonConstant.DOMAIN_NAME_PATTERN, originUrlText)) {
                            msg.setMessage("域名格式不正确").setTitle("").setMessageType(JOptionPane.WARNING_MESSAGE);
                            map.put(CommonConstant.FAIL, msg);
                            return map;
                        }
                    }
                } else {
                    if (!Pattern.matches(CommonConstant.IP_PORT, originUrlText)) {
                        msg.setMessage("ip和端口格式不正确").setTitle("").setMessageType(JOptionPane.WARNING_MESSAGE);
                        map.put(CommonConstant.FAIL, msg);
                        return map;
                    }
                }
            } else {
                originUrlText = originUrlText.replaceAll("\\\\", Matcher.quoteReplacement(File.separator));
            }
        }
        if (!EXCLUDE_DATABASE.contains(CommonConstant.DATA_BASE_TYPE)) {
            if (StringUtil.isEmpty(originDatabaseText)) {
                msg.setMessage("数据库必填").setTitle("").setMessageType(JOptionPane.WARNING_MESSAGE);
                map.put(CommonConstant.FAIL, msg);
                return map;
            }
        }
        if (!EXCLUDE_IPADDR.contains(CommonConstant.DATA_BASE_TYPE)) {
            if (StringUtil.isEmpty(userText)) {
                msg.setMessage("用户名必填").setTitle("").setMessageType(JOptionPane.WARNING_MESSAGE);
                map.put(CommonConstant.FAIL, msg);
                return map;
            }
            //优化连接 判断ip、端口是否连通
            String[] ipAndPort = originUrlText.split(":");
            boolean status = PingUtil.connect(ipAndPort[0], Integer.parseInt(ipAndPort[1]), 2000);
            if (!status) {
                msg.setMessage("连接失败，请检查ip和端口是否正确").setTitle("错误").setMessageType(JOptionPane.ERROR_MESSAGE);
                map.put(CommonConstant.FAIL, msg);
                return map;
            }
        }
        return map;
    }
}
