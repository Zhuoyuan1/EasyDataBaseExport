package com.easydatabaseexport.util;

import com.easydatabaseexport.EasyDataBaseExportMain;
import com.easydatabaseexport.entities.OSDetector;
import com.easydatabaseexport.enums.ConfigKeyEnum;
import com.easydatabaseexport.log.LogManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;

import javax.swing.filechooser.FileSystemView;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * FileOperateUtil
 *
 * @author lzy
 * @date 2021/5/31 15:29
 **/
public class FileOperateUtil {

    private static final Logger log = LogManager.getLogger(FileOperateUtil.class);

    /**
     * 保存文件到本地
     *
     * @param filepath
     * @param data
     * @return java.lang.String
     * @author lzy
     * @date 2021/5/31 15:30
     **/
    public static String saveFile(String filepath, byte[] data) {
        if (data != null) {
            /*if (OSDetector.isWindows()){
                filepath = "c:" + File.separator + filename;
            }else if (OSDetector.isLinux() || OSDetector.isMac()) {
                filepath = File.separator + filename;
            }*/
            File file = new File(filepath);
            if (!file.exists()) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    fos.write(data, 0, data.length);
                    fos.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (null != fos) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        return filepath;
    }

    /**
     * 获取我的文档路径
     *
     * @return java.lang.String
     * @author lzy
     * @date 2021/7/5 16:31
     **/
    public static String getSavePath() {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        File file = new File(fsv.getDefaultDirectory().getAbsoluteFile() + File.separator + ".EasyConnect");
        if (!file.exists()) {
            file.mkdir();//创建文件夹
        }
        return fsv.getDefaultDirectory().getAbsolutePath() + File.separator + ".EasyConnect" + File.separator;
    }

    /**
     * 调用系统命令 打开文件
     *
     * @param file
     * @author lzy
     * @date 2021/5/31 15:30
     **/
    public static void open(File file) {
        try {
            if (OSDetector.isWindows()) {
                Runtime.getRuntime().exec(new String[]
                        {"rundll32", "url.dll,FileProtocolHandler",
                                file.getAbsolutePath()});
            } else if (OSDetector.isLinux() || OSDetector.isMac()) {
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open",
                        file.getAbsolutePath()});
            } else {
                // Unknown OS, try with desktop
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                } else {
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * 保存ini文件方法
     *
     * @param path
     * @return void
     * @author lzy
     * @date 2021/7/5 16:35
     **/
    public static void saveIniFile(String path, String msg) throws Exception {
        File file = new File(path);
        if (file.exists()) {
        } else {
            InputStream inputStream = EasyDataBaseExportMain.class.getClassLoader().getResourceAsStream("database.ini");
            // 如果是resource目录下可以直接写文件名称
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
            saveFile(path, output.toByteArray());
        }
        if ("".equals(msg) || null == msg) return;
        //写数据
        writeData(path, ConfigKeyEnum.SYS.getKey(), "", msg);
    }

    /**
     * 写数据（父key存在时）
     *
     * @param address  文件路径
     * @param keyword  配置关键字 [sys]、[theme]
     * @param childKey 子节点key theme
     * @param msg      插入内容
     * @return void
     * @author lzy
     * @date 2021/7/6 8:54
     **/
    public static void writeData(String address, String keyword, String childKey, String msg) {
        try {
            if (ConfigKeyEnum.SYS.getKey().equals(keyword)) {
                Config cfg = FileIniRead.getDefaultConfig();
                // 生成配置文件的URL
                File iniFile = new File(address);
                URL url = iniFile.toURI().toURL();
                Ini ini = new Ini();
                ini.setConfig(cfg);
                try {
                    // 加载配置文件
                    ini.load(url);
                    // 读取 system
                    Profile.Section section = ini.get(keyword);
                    String[] strings = msg.split(" = ");
                    section.put(strings[0], strings[1]);
                    ini.store(iniFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Config cfg = FileIniRead.getDefaultConfig();
                // 生成配置文件的URL
                File iniFile = new File(address);
                URL url = iniFile.toURI().toURL();
                Ini ini = new Ini();
                ini.setConfig(cfg);
                try {
                    // 加载配置文件
                    ini.load(url);
                    // 读取 system
                    Profile.Section section = ini.get(keyword);
                    section.put(childKey, msg);
                    ini.store(iniFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 插入更新 ini
     *
     * @param address   ini地址
     * @param parentKey [sys]、[theme]、[config]
     * @param key       子节点key
     * @param value     子节点value
     */
    public static void writeNewData(String address, String parentKey, String key, String value) {
        try {
            Config cfg = FileIniRead.getDefaultConfig();
            // 生成配置文件的URL
            File iniFile = new File(address);
            URL url = iniFile.toURI().toURL();
            Ini ini = new Ini();
            ini.setConfig(cfg);
            // 加载配置文件
            ini.load(url);
            // 写入
            if (Objects.isNull(ini.get(parentKey))) {
                ini.add(parentKey, key, value);
            } else {
                Profile.Section section = ini.get(parentKey);
                section.put(key, value);
            }
            ini.store(iniFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 推荐此⽅法获取⽂件MD5
     *
     * @return String
     */
    public static String getLocalMd5File(String path) {
        String md5 = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            md5 = DigestUtils.md5Hex(IOUtils.toByteArray(fis));
            IOUtils.closeQuietly(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return md5;
    }

    public static String getRemoteFile(String urlText) {
        HttpURLConnection connection = null;
        StringBuilder retStr = new StringBuilder();
        try {
            URL url = new URL(urlText);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.54 Safari/537.36");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream in = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    retStr.append(lineTxt);
                }
                bufferedReader.close();
                in.close();
            } else {
                log.error("请求失败！");
            }
        } catch (Exception e) {
            LogManager.writeLogFile(e, log);
        }
        return retStr.toString();
    }
}
