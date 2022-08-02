package com.easydatabaseexport.util;

import com.easydatabaseexport.log.LogManager;
import lombok.extern.java.Log;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FileIniRead
 *
 * @author lzy
 * @date 2021/7/5 21:19
 **/
@Log
public class FileIniRead {

    public final static String FILE_NAME = "database.ini";

    /**
     * 用户配置读取
     *
     * @return java.util.List<java.lang.String>
     * @author lzy
     * @date 2021/7/6 9:32
     **/
    public static List<String> getIniConf() {
        List<String> list = new ArrayList<>();
        try {
            Config cfg = new Config();
            // 生成配置文件的URL
            File iniFile = new File(FileOperateUtil.getSavePath() + FILE_NAME);
            if (!iniFile.exists()) {
                return list;
            }
            URL url = iniFile.toURI().toURL();
            // 设置Section允许出现重复
            cfg.setMultiSection(true);
            Ini ini = new Ini();
            ini.setConfig(cfg);
            // 加载配置文件
            ini.load(url);
            // 读取 system
            List<Profile.Section> sectionList = ini.getAll("sys");
            for (Profile.Section section : sectionList) {
                for (String key : section.keySet()) {
                    list.add(key + "|" + section.get(key));
                }
            }
        } catch (Exception e) {
            LogManager.writeLogFile(e, log);
        }
        return list;
    }

    /**
     * 主题读取
     *
     * @return java.lang.String
     * @author lzy
     * @date 2021/7/6 9:30
     **/
    public static String getIniThemeIndex() {
        try {
            Config cfg = new Config();
            // 生成配置文件的URL
            File iniFile = new File(FileOperateUtil.getSavePath() + FILE_NAME);
            if (!iniFile.exists()) {
                return "-1";
            }
            URL url = iniFile.toURI().toURL();
            // 设置Section允许出现重复
            cfg.setMultiSection(true);
            Ini ini = new Ini();
            ini.setConfig(cfg);
            // 加载配置文件
            ini.load(url);
            // 读取 system
            Profile.Section section = ini.get("theme");
            return section.get("theme");
        } catch (Exception e) {
            LogManager.writeLogFile(e, log);
            return "-1";
        }
    }

    /**
     * 公共读取配置文件方法
     *
     * @param name
     * @return
     */
    public static Map<String, String> getConfig(String name) {
        Map<String, String> map = new HashMap<>(0);
        try {
            Config cfg = new Config();
            // 生成配置文件的URL
            File iniFile = new File(FileOperateUtil.getSavePath() + FILE_NAME);
            if (!iniFile.exists()) {
                return map;
            }
            URL url = iniFile.toURI().toURL();
            // 设置Section允许出现重复
            cfg.setMultiSection(true);
            Ini ini = new Ini();
            ini.setConfig(cfg);
            // 加载配置文件
            ini.load(url);
            // 读取 system
            Profile.Section section = ini.get(name);
            if (null == section) {
                return map;
            }
            map = section;
        } catch (Exception e) {
            LogManager.writeLogFile(e, log);
            return map;
        }
        return map;
    }

    /*public static void main(String[] args) {
        System.out.println(getConfig("theme"));
    }*/
}
