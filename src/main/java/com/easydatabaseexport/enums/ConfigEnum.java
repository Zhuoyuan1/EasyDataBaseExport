package com.easydatabaseexport.enums;

/**
 * 该类为ini配置文件中的 key枚举
 *
 * @author lzy
 */
public enum ConfigEnum {
    //索引
    INDEX("index", "是否导出索引："),
    SHEET("sheet", "是否导出多sheet：");

    private final String key;
    private final String value;

    private ConfigEnum(String key, String value) {
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
