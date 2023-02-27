package com.easydatabaseexport.common;

import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.enums.ConfigEnum;
import com.easydatabaseexport.ui.export.config.Export;
import com.easydatabaseexport.util.StringUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * PatternConstant
 *
 * @author lzy
 * @date 2022/11/10 15:00
 **/
public final class PatternConstant {

    /**
     * 公共
     */
    public static final String STRING_SPLIT = "%s";
    public static final String MD_SPLIT = "|";
    public static final String COMMON_SPLIT = "\\|";
    public static final String MD_CENTER = ":--:";
    public static final String MD_LEFT = "----";
    public static final String HTML_TR = "<tr>%s</tr>";
    public static final String HTML_TH = "<th>%s</th>";
    public static final String HTML_TD = "<td>%s</td>";

    /**
     * Markdown
     */
    public static final String TITLE = "# %s";
    public static final String CATALOG = "## %s";
    public static String TABLE_HEADER = "|序号|字段名|类型|长度|是否为空|默认值|小数位|注释|";
    public static String TABLE_BODY = "|%s|%s|%s|%s|%s|%s|%s|%s|";
    public static String TABLE_SEPARATOR = "|:--:|----|----|----|----|----|----|----|";
    public static String INDEX_TABLE_HEADER = "|名称|字段|索引类型|索引方法|注释|";
    public static String INDEX_TABLE_BODY = "|%s|%s|%s|%s|%s|";
    public static String INDEX_TABLE_SEPARATOR = "|:--:|----|----|----|----|";

    /**
     * Html
     */
    public static final String HTML_TITLE = "<h1 id=\"{0}\">{0}</h1>";
    public static final String HTML_CATALOG = "<h2 id=\"{0}\">{1}</h2>";
    public static final String HTML_INDEX_ITEM = "<a href=\"#{0}\" title=\"{0}\">{1}</a>";
    public static String HTML_TABLE_HEADER = "<tr><th>序号</th><th>字段名</th><th>类型</th><th>长度</th><th>是否为空</th><th>默认值</th><th>小数位</th><th>注释</th></tr>";
    public static String HTML_TABLE_BODY = "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>";
    public static String HTML_INDEX_TABLE_HEADER = "<tr><th>名称</th><th>字段</th><th>索引类型</th><th>索引方法</th><th>注释</th></tr>";
    public static String HTML_INDEX_TABLE_BODY = "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>";

    /**
     * 需要导出索引字段
     **/
    public static Field[] indexFields = IndexInfoVO.class.getDeclaredFields();
    /**
     * 需要导出表字段
     **/
    public static Field[] tableFields = TableParameter.class.getDeclaredFields();

    /**
     * 读取配置文件，并根据配置文件进行赋值
     **/
    public static void reCheckConfig() {
        //默认导出所有表头
        if (CommonConstant.configMap.containsKey(ConfigEnum.INDEX_TABLE_HEAD.getKey())) {
            String indexHeader = CommonConstant.configMap.get(ConfigEnum.INDEX_TABLE_HEAD.getKey());
            CommonConstant.INDEX_HEAD_NAMES = Arrays.stream(indexHeader.split(COMMON_SPLIT)).filter(StringUtil::isNotEmpty).toArray(String[]::new);
            INDEX_TABLE_HEADER = MD_SPLIT + StringUtil.join(Arrays.stream(CommonConstant.INDEX_HEAD_NAMES).collect(Collectors.toList()), MD_SPLIT) + MD_SPLIT;
            INDEX_TABLE_BODY = MD_SPLIT + StringUtil.join(Arrays.stream(CommonConstant.INDEX_HEAD_NAMES).map(v -> STRING_SPLIT).collect(Collectors.toList()), MD_SPLIT) + MD_SPLIT;
            INDEX_TABLE_SEPARATOR = MD_SPLIT + StringUtil.join(Arrays.stream(CommonConstant.INDEX_HEAD_NAMES).map(v -> {
                if (CommonConstant.INDEX_HEAD_NAMES[0].equals(v)) {
                    return MD_CENTER;
                }
                return MD_LEFT;
            }).collect(Collectors.toList()), MD_SPLIT) + MD_SPLIT;
            HTML_INDEX_TABLE_HEADER = String.format(HTML_TR, StringUtil.join(Arrays.stream(CommonConstant.INDEX_HEAD_NAMES).map(v -> String.format(HTML_TH, v)).collect(Collectors.toList()), ""));
            HTML_INDEX_TABLE_BODY = String.format(HTML_TR, StringUtil.join(Arrays.stream(CommonConstant.INDEX_HEAD_NAMES).map(v -> HTML_TD).collect(Collectors.toList()), ""));

            /* 初始化处理: 需要导出的字段 */
            Field[] indexAllFields = IndexInfoVO.class.getDeclaredFields();
            Field[] indexFields = new Field[CommonConstant.INDEX_HEAD_NAMES.length];
            int i = 0;
            for (Field field : indexAllFields) {
                Export export = field.getAnnotation(Export.class);
                if (PatternConstant.INDEX_TABLE_HEADER.contains(export.name())) {
                    indexFields[i] = field;
                    i++;
                }
            }
            PatternConstant.indexFields = indexFields;

        }
        if (CommonConstant.configMap.containsKey(ConfigEnum.TABLE_HEAD.getKey())) {
            String tableHeader = CommonConstant.configMap.get(ConfigEnum.TABLE_HEAD.getKey());
            //改变表头
            CommonConstant.COLUMN_HEAD_NAMES = Arrays.stream(tableHeader.split(COMMON_SPLIT)).filter(StringUtil::isNotEmpty).toArray(String[]::new);
            //改变导出参数
            TABLE_HEADER = MD_SPLIT + StringUtil.join(Arrays.stream(CommonConstant.COLUMN_HEAD_NAMES).collect(Collectors.toList()), MD_SPLIT) + MD_SPLIT;
            TABLE_BODY = MD_SPLIT + StringUtil.join(Arrays.stream(CommonConstant.COLUMN_HEAD_NAMES).map(v -> STRING_SPLIT).collect(Collectors.toList()), MD_SPLIT) + MD_SPLIT;
            TABLE_SEPARATOR = MD_SPLIT + StringUtil.join(Arrays.stream(CommonConstant.COLUMN_HEAD_NAMES).map(v -> {
                if (CommonConstant.COLUMN_HEAD_NAMES[0].equals(v)) {
                    return MD_CENTER;
                }
                return MD_LEFT;
            }).collect(Collectors.toList()), MD_SPLIT) + MD_SPLIT;
            HTML_TABLE_HEADER = String.format(HTML_TR, StringUtil.join(Arrays.stream(CommonConstant.COLUMN_HEAD_NAMES).map(v -> String.format(HTML_TH, v)).collect(Collectors.toList()), ""));
            HTML_TABLE_BODY = String.format(HTML_TR, StringUtil.join(Arrays.stream(CommonConstant.COLUMN_HEAD_NAMES).map(v -> HTML_TD).collect(Collectors.toList()), ""));

            Field[] tableAllFields = TableParameter.class.getDeclaredFields();
            Field[] tableFields = new Field[CommonConstant.COLUMN_HEAD_NAMES.length];
            int j = 0;
            for (Field field : tableAllFields) {
                Export export = field.getAnnotation(Export.class);
                if (PatternConstant.TABLE_HEADER.contains(export.name())) {
                    tableFields[j] = field;
                    j++;
                }
            }
            PatternConstant.tableFields = tableFields;
        }
    }
}
