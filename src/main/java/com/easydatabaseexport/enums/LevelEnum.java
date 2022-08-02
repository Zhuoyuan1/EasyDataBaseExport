package com.easydatabaseexport.enums;

/**
 * LevelEnum
 *
 * @author lzy
 * @date 2022/7/27 11:17
 **/
public enum LevelEnum {
    //测试
    TEST("test", "测试"),
    //开发
    DEV("dev", "开发"),
    //生产
    PROD("prod", "生产");

    private final String level;
    private final String desc;

    private LevelEnum(String level, String desc) {
        this.level = level;
        this.desc = desc;
    }

    public String getLevel() {
        return level;
    }

    public String getDesc() {
        return desc;
    }
}
