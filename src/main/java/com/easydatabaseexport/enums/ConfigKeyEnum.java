package com.easydatabaseexport.enums;

/**
 * ConfigKeyEnum
 *
 * @author lzy
 * @date 2023/3/3 11:27
 **/
public enum ConfigKeyEnum {
    //连接配置
    SYS("sys"),
    //导出配置
    CONFIG("config"),
    //主题配置
    THEME("theme");

    private final String key;

    ConfigKeyEnum(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
