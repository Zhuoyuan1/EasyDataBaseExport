package com.easydatabaseexport.common;

import java.util.Arrays;
import java.util.List;

/**
 * EnvironmentConstant
 *
 * @author lzy
 * @date 2022/7/27 11:14
 **/
public final class EnvironmentConstant {
    /**
     * 本版本专属MD5码 v0.1.6
     **/
    public static final String FILE_MD5_VALUE = "f0126a1ef8476742b2be01bce832136a";
    /**
     * 运行等级
     **/
    public static final String RUN_LEVEL = "run_level";
    /**
     * 模板文件
     **/
    public static final List<String> TEMPLATE_FILE = Arrays.asList("template_index.docx", "template.docx", "template.html", "template_diy.docx", "sub_template_diy.docx");
}
