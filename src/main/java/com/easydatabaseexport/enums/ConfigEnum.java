package com.easydatabaseexport.enums;

import com.easydatabaseexport.common.PatternConstant;

import javax.swing.filechooser.FileSystemView;

/**
 * 该类为ini配置文件中的 key枚举
 *
 * @author lzy
 */
public enum ConfigEnum {
    //索引
    INDEX("index", "导出索引"),
    SHEET("sheet", "多sheet"),
    TABLE_HEAD("table_head", PatternConstant.TABLE_HEADER),
    INDEX_TABLE_HEAD("index_table_head", PatternConstant.INDEX_TABLE_HEADER),
    DEFAULT_EXPORT_PATH("default_export_path", FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath());

    private final String key;
    private final String value;

    ConfigEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getValueByKey(String key) {
        for (ConfigEnum enumer : values()) {
            if (enumer.getKey().equals(key)) {
                return enumer.getValue();
            }
        }
        return "";
    }

}
