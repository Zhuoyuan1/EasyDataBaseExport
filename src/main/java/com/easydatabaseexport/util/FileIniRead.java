package com.easydatabaseexport.util;

import com.easydatabaseexport.enums.ConfigKeyEnum;
import com.easydatabaseexport.log.LogManager;
import lombok.extern.log4j.Log4j;
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
@Log4j
public class FileIniRead {

    public final static String FILE_NAME = "database.ini";

    /**
     * 用户配置读取
     *
     * @return java.util.List<java.lang.String>
     **/
    public static List<String> getIniConf() {
        List<String> list = new ArrayList<>();
        try {
            Config cfg = getDefaultConfig();
            // 生成配置文件的URL
            File iniFile = new File(FileOperateUtil.getSavePath() + FILE_NAME);
            if (!iniFile.exists()) {
                return list;
            }
            URL url = iniFile.toURI().toURL();
            Ini ini = new Ini();
            ini.setConfig(cfg);
            // 加载配置文件
            ini.load(url);
            // 读取 system
            List<Profile.Section> sectionList = ini.getAll(ConfigKeyEnum.SYS.getKey());
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
     * 用户配置[sys] 删除
     **/
    public static void deleteIniConf(String configKey, List<String> keys) {
        try {
            Config cfg = getDefaultConfig();
            // 生成配置文件的URL
            File iniFile = new File(FileOperateUtil.getSavePath() + FILE_NAME);
            if (!iniFile.exists()) {
                return;
            }
            URL url = iniFile.toURI().toURL();
            Ini ini = new Ini();
            ini.setConfig(cfg);
            // 加载配置文件
            ini.load(url);
            // 读取 system
            List<Profile.Section> sectionList = ini.getAll(configKey);
            for (Profile.Section section : sectionList) {
                for (String key : section.keySet()) {
                    if (keys.contains(key)) {
                        //删除
                        section.put(key, null);
                        ini.store(iniFile);
                    }
                }
            }
        } catch (Exception e) {
            LogManager.writeLogFile(e, log);
        }
    }

    /**
     * 用户配置[sys] 插入
     **/
    public static void insertIniConf(Map<String, String> allKeysMap) {
        try {
            File iniFile = new File(FileOperateUtil.getSavePath() + FILE_NAME);
            if (!iniFile.exists()) {
                return;
            }
            Config cfg = FileIniRead.getDefaultConfig();
            URL url = iniFile.toURI().toURL();
            Ini ini = new Ini();
            ini.setConfig(cfg);
            // 加载配置文件
            ini.load(url);
            for (Map.Entry<String, String> map : allKeysMap.entrySet()) {
                //读取 system
                Profile.Section section = ini.get(ConfigKeyEnum.SYS.getKey());
                section.put(map.getKey(), map.getValue());
            }
            ini.store(iniFile);
        } catch (Exception e) {
            LogManager.writeLogFile(e, log);
        }
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
            Config cfg = getDefaultConfig();
            // 生成配置文件的URL
            File iniFile = new File(FileOperateUtil.getSavePath() + FILE_NAME);
            if (!iniFile.exists()) {
                return "-1";
            }
            URL url = iniFile.toURI().toURL();
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
            Config cfg = getDefaultConfig();
            // 生成配置文件的URL
            File iniFile = new File(FileOperateUtil.getSavePath() + FILE_NAME);
            if (!iniFile.exists()) {
                return map;
            }
            URL url = iniFile.toURI().toURL();
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

    public static Config getDefaultConfig() {
        Config cfg = new Config();
        // 设置Section允许出现重复
        cfg.setMultiSection(true);
        // 允许空的Section
        cfg.setEmptySection(true);
        return cfg;
    }

    /*public static void main(String[] args) {
        System.out.println(getConfig("theme"));
    }*/
}
