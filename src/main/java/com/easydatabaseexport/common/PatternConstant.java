package com.easydatabaseexport.common;

import com.sun.deploy.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * PatternConstant
 *
 * @author lzy
 * @date 2022/11/10 15:00
 **/
public final class PatternConstant {

    /** 公共 */
    public static final String STRING_SPLIT =  "%s";
    public static final String MD_SPLIT = "|";
    public static final String MD_CENTER = ":--:";
    public static final String MD_LEFT = "----";
    public static final String HTML_TR = "<tr>%s</tr>";
    public static final String HTML_TH = "<th>%s</th>";
    public static final String HTML_TD = "<td>%s</td>";

    /** Markdown */
    public static final String TITLE = "# %s";
    public static final String CATALOG = "## %s";
    public static final String TABLE_HEADER = "|序号|字段名|类型|长度|是否为空|默认值|小数位|注释|";
    public static final String TABLE_BODY = "|%s|%s|%s|%s|%s|%s|%s|%s|";
    public static final String TABLE_SEPARATOR = "|:--:|----|----|----|----|----|----|----|";
    public static final String INDEX_TABLE_HEADER = "|名称|字段|索引类型|索引方法|注释|";
    public static final String INDEX_TABLE_BODY = "|%s|%s|%s|%s|%s|";
    public static final String INDEX_TABLE_SEPARATOR = "|:--:|----|----|----|----|";

    /** Html */
    public static final String HTML_TITLE = "<h1 id=\"{0}\">{0}</h1>";
    public static final String HTML_CATALOG = "<h2 id=\"{0}\">{1}</h2>";
    public static final String HTML_TABLE_HEADER = "<tr><th>序号</th><th>字段名</th><th>类型</th><th>长度</th><th>是否为空</th><th>默认值</th><th>小数位</th><th>注释</th></tr>";
    public static final String HTML_TABLE_BODY = "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>";
    public static final String HTML_INDEX_TABLE_HEADER = "<tr><th>名称</th><th>字段</th><th>索引类型</th><th>索引方法</th><th>注释</th></tr>";
    public static final String HTML_INDEX_TABLE_BODY = "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>";
    public static final String HTML_INDEX_ITEM = "<a href=\"#{0}\" title=\"{0}\">{1}</a>";

    public static void main(String[] args) {
        System.out.println(MD_SPLIT + StringUtils.join(Arrays.stream(CommonConstant.COLUMN_HEAD_NAMES).collect(Collectors.toList()),MD_SPLIT) + MD_SPLIT);
        System.out.println(MD_SPLIT + StringUtils.join(Arrays.stream(CommonConstant.COLUMN_HEAD_NAMES).map(v -> STRING_SPLIT).collect(Collectors.toList()),MD_SPLIT) + MD_SPLIT);
        System.out.println(MD_SPLIT + StringUtils.join(Arrays.stream(CommonConstant.COLUMN_HEAD_NAMES).map(v -> {
            if (CommonConstant.COLUMN_HEAD_NAMES[0].equals(v)) {
                return MD_CENTER;
            }
            return MD_LEFT;
        }).collect(Collectors.toList()), MD_SPLIT) + MD_SPLIT);
        System.out.println(MD_SPLIT + StringUtils.join(Arrays.stream(CommonConstant.INDEX_HEAD_NAMES).collect(Collectors.toList()),MD_SPLIT) + MD_SPLIT);
        System.out.println(MD_SPLIT + StringUtils.join(Arrays.stream(CommonConstant.INDEX_HEAD_NAMES).map(v -> STRING_SPLIT).collect(Collectors.toList()),MD_SPLIT) + MD_SPLIT);
        System.out.println(MD_SPLIT + StringUtils.join(Arrays.stream(CommonConstant.INDEX_HEAD_NAMES).map(v -> {
            if (CommonConstant.INDEX_HEAD_NAMES[0].equals(v)) {
                return MD_CENTER;
            }
            return MD_LEFT;
        }).collect(Collectors.toList()), MD_SPLIT) + MD_SPLIT);
        System.out.println(String.format(HTML_TR, StringUtils.join(Arrays.stream(CommonConstant.COLUMN_HEAD_NAMES).map(v -> String.format(HTML_TH, v)).collect(Collectors.toList()),"")));
        System.out.println(String.format(HTML_TR, StringUtils.join(Arrays.stream(CommonConstant.COLUMN_HEAD_NAMES).map(v -> HTML_TD).collect(Collectors.toList()),"")));
        System.out.println(String.format(HTML_TR, StringUtils.join(Arrays.stream(CommonConstant.INDEX_HEAD_NAMES).map(v -> String.format(HTML_TH, v)).collect(Collectors.toList()),"")));
        System.out.println(String.format(HTML_TR, StringUtils.join(Arrays.stream(CommonConstant.INDEX_HEAD_NAMES).map(v -> HTML_TD).collect(Collectors.toList()),"")));
    }
}
