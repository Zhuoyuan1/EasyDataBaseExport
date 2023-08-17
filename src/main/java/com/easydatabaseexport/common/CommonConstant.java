package com.easydatabaseexport.common;

import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.enums.ConfigEnum;
import com.easydatabaseexport.enums.DataBaseType;
import com.easydatabaseexport.enums.UpdateEnum;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.ui.ConnectJavaFrame;
import com.easydatabaseexport.ui.component.JCheckBoxTree;
import com.easydatabaseexport.util.FileIniRead;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * CommonConstant
 *
 * @author lzy
 * @date 2021/11/1 14:51
 **/
@Log4j
public final class CommonConstant {

    public static final String IP_PORT = "^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])" +
            "\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5]):([0-9]|[1-9]" +
            "\\d|[1-9]\\d{2}|[1-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$";

    public static final String DOMAIN_NAME_PATTERN = "^(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}):([0-9]|[1-9]" +
            "\\d|[1-9]\\d{2}|[1-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$";

    public static final Pattern pattern = Pattern.compile("[a-zA-Z]");

    public static final String[] THEMES = new String[]{
            "org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel",
            "org.pushingpixels.substance.api.skin.SubstanceDustLookAndFeel",
            "org.pushingpixels.substance.api.skin.SubstanceEmeraldDuskLookAndFeel",
            "org.pushingpixels.substance.api.skin.SubstanceGeminiLookAndFeel",
            "org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel",
            "org.pushingpixels.substance.api.skin.SubstanceMagellanLookAndFeel",
            "org.pushingpixels.substance.api.skin.SubstanceMistAquaLookAndFeel",
            "org.pushingpixels.substance.api.skin.SubstanceModerateLookAndFeel",
            "org.pushingpixels.substance.api.skin.SubstanceNebulaBrickWallLookAndFeel",
            "org.pushingpixels.substance.api.skin.SubstanceNebulaLookAndFeel",
            "org.pushingpixels.substance.api.skin.SubstanceOfficeBlue2007LookAndFeel",
            "org.pushingpixels.substance.api.skin.SubstanceOfficeSilver2007LookAndFeel",
            "org.pushingpixels.substance.api.skin.SubstanceSaharaLookAndFeel",
            "org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel"
    };

    /**
     * 表结构
     **/
    public static List<TableParameter> tableParameterList = new ArrayList<>();
    /**
     * 表head
     **/
    public static String[] INDEX_HEAD_NAMES =
            {"名称", "字段", "索引类型", "索引方法", "注释"};
    public static String[] COLUMN_HEAD_NAMES =
            {"序号", "字段名", "类型", "长度", "是否为空", "默认值", "小数位", "注释"};
    /**
     * 不变的表头用于展示 和 配置选择
     **/
    public static String[] INDEX_CONFIG_HEAD_NAMES =
            INDEX_HEAD_NAMES.clone();
    public static String[] COLUMN_CONFIG_HEAD_NAMES =
            COLUMN_HEAD_NAMES.clone();
    public static final String[] INDEX_FINAL_HEAD_NAMES =
            Arrays.copyOf(INDEX_HEAD_NAMES, INDEX_HEAD_NAMES.length);
    public static final String[] COLUMN_FINAL_HEAD_NAMES =
            Arrays.copyOf(COLUMN_HEAD_NAMES, COLUMN_HEAD_NAMES.length);
    /**
     * 表名
     **/
    public static String TABLE_NAME = "";
    /**
     * 库名
     **/
    public static String DATABASE_NAME = "";
    /**
     * 树结构ROOT
     **/
    public static String ROOT = "";
    /**
     * 主题index
     **/
    public static int index = 0;

    /**
     * frame的宽
     */
    public static final int FRAME_WIDTH = 1500;
    /**
     * frame的高
     */
    public static final int FRAME_HEIGHT = 800;

    public static Connection connection = null;
    public static JCheckBoxTree root;
    public static final JPanel RIGHT = new JPanel();
    public static final JScrollPane CENTERS = new JScrollPane();

    public static final String SUCCESS = "true";
    public static final String FAIL = "false";

    public static String DATA_BASE_TYPE = DataBaseType.MYSQL.name();

    public static String TREE_DATABASE = "";

    public static final String SEPARATOR = "|";
    public static final String COLON = ":";

    /**
     * 导出配置
     */
    public static Map<String, String> configMap = new ConcurrentHashMap<>(16);
    public static final String INI_NODE_KEY = "config";
    public static final String EXPORT = "export";
    public static final String UPDATE = "update";

    /**
     * 模板存放目录
     **/
    public static final String templateDir = "template";

    public static void checkConfigIniFile() {
        //读取配置文件
        configMap = FileIniRead.getConfig(INI_NODE_KEY);
    }

    public static void writeNewKey(String key, String value) {
        if (StringUtil.isEmpty(value)) {
            value = "0";
        }
        //写入新的key，默认赋值为0
        FileOperateUtil.writeNewData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, INI_NODE_KEY, key, value);
    }

    public static void initCheckAllKey(String key) {
        checkConfigIniFile();
        //说明是旧版本，写入没有的配置
        if (UPDATE.equals(key)) {
            for (UpdateEnum updateEnum : UpdateEnum.values()) {
                if (!configMap.containsKey(updateEnum.getKey())) {
                    if (updateEnum.getKey().equals(UpdateEnum.UPDATE_VERSION.getKey())) {
                        writeNewKey(updateEnum.getKey(), "1");
                    }
                }
            }
        } else if (EXPORT.equals(key)) {
            for (ConfigEnum configEnum : ConfigEnum.values()) {
                if (!configMap.containsKey(configEnum.getKey())) {
                    if (!configEnum.getKey().equals(ConfigEnum.INDEX.getKey()) &&
                            !configEnum.getKey().equals(ConfigEnum.SHEET.getKey())) {
                        writeNewKey(configEnum.getKey(), configEnum.getValue());
                    } else {
                        writeNewKey(configEnum.getKey(), null);
                    }
                }
            }
        }
        //重新读取配置文件
        checkConfigIniFile();
    }

    public static void initByClose() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                LogManager.writeLogFile(ex, log);
            }
        }
        connection = null;
        tableParameterList.clear();
        CommonDataBaseType.CON_DATABASE_TABLE_MAP.clear();
        CommonDataBaseType.CON_MODE_TABLE_MAP.clear();
        CommonDataBaseType.CON_DATABASE_MODE_TABLE_MAP.clear();
        ConnectJavaFrame.cacheRootNode = null;
        TABLE_NAME = "";
        DATABASE_NAME = "";
        TREE_DATABASE = "";
        ROOT = "";
        RIGHT.removeAll();
        CENTERS.removeAll();
    }

    @SneakyThrows
    public static void initByReboot() {
        if (index < 0) {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } else if (index < THEMES.length) {
            try {
                UIManager.setLookAndFeel(THEMES[index]);
            } catch (Exception e) {
                LogManager.writeLogFile(e, log);
            }
        }
        initByClose();
    }

    public static void copyTemplateFile() {
        String template = FileOperateUtil.getSavePath() + templateDir;
        File file = new File(template);
        if (!file.exists()) {
            file.mkdir();
        }
        for (int i = 0; i < EnvironmentConstant.TEMPLATE_FILE.size(); i++) {
            saveFile(template, EnvironmentConstant.TEMPLATE_FILE.get(i), true);
        }
    }

    public static void copySystemIniFile() {
        saveFile(FileOperateUtil.getSavePath(), "database.ini", false);
    }

    public static void saveFile(String dir, String path, boolean isOverride) {
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        if (!isOverride) {
            File file = new File(dir + File.separator + path);
            if (file.exists()) {
                return;
            }
        }
        try {
            outputStream = new FileOutputStream(dir + File.separator + path);
            // 模板文件输入输出地址 读取resources下文件
            //返回读取指定资源的输入流
            inputStream = CommonConstant.class.getClassLoader().getResourceAsStream(path);
            byte[] buffer = new byte[4096];
            int n = 0;
            while (-1 != (n = inputStream.read(buffer))) {
                outputStream.write(buffer, 0, n);
            }
            outputStream.flush();
        } catch (Exception ex) {
            LogManager.writeLogFile(ex, log);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    LogManager.writeLogFile(ex, log);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    LogManager.writeLogFile(ex, log);
                }
            }
        }
    }
}
