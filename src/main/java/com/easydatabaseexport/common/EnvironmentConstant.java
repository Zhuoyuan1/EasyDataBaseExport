package com.easydatabaseexport.common;

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
    public static final String FILE_MD5_VALUE = "f1abda441b476213d1fda2d0f4c2acba";
    /**
     * 运行等级
     **/
    public static final String RUN_LEVEL = "dev";
    /**
     * 模板文件
     **/
    public static final List<String> TEMPLATE_FILE = Arrays.asList("template_index.docx", "template.docx");
}
