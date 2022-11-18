package com.easydatabaseexport.enums;

/**
 * UpdateEnum
 *
 * @author lzy
 * @date 2022/5/20 13:44
 **/
public enum UpdateEnum {
    //索引
    UPDATE_VERSION("update", "是否自动检查更新");

    private final String key;
    private final String value;

    UpdateEnum(String key, String value) {
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
        for (UpdateEnum enumer : values()) {
            if (enumer.getKey().equals(key)) {
                return enumer.getValue();
            }
        }
        return "";
    }
}
