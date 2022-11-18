package com.easydatabaseexport.common;

import com.easydatabaseexport.enums.LevelEnum;

import java.util.Arrays;
import java.util.List;

/**
 * EnvironmentConstant
 *
 * @author lzy
 * @date 2022/7/27 11:14
 **/
public class EnvironmentConstant {
    /**
     * 本版本专属MD5码
     **/
    public static final String FILE_MD5_VALUE = "17735ee05f26de8be10c5b69e54fbe37";
    /**
     * 运行等级
     **/
    public static final String RUN_LEVEL = LevelEnum.PROD.getLevel();
    /**
     * 模板文件
     **/
    public static final List<String> TEMPLATE_FILE = Arrays.asList("template_index.docx", "template.docx", "template.html");
}
