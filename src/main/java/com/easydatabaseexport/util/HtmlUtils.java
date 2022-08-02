package com.easydatabaseexport.util;

/**
 * HtmlUtils
 *
 * @author lzy
 * @date 2022/3/18 9:00
 **/
public class HtmlUtils {
    private final static String HEAD_HTML = "<div style='color:#808080;font-size:12px;'> %s ：</div>";

    public static String getHtml(String value) {
        return String.format(HEAD_HTML, value);
    }
}
