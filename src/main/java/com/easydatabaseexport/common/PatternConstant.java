package com.easydatabaseexport.common;

import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.enums.ConfigEnum;
import com.easydatabaseexport.util.FileIniRead;
import com.easydatabaseexport.util.FileOperateUtil;
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
    public static final String SLASH_SPLIT = "\\";
    public static final String HEAD_IGNORE = "_ignore";
    public static final String COMMON_SPLIT = "\\|";
    public static final String MD_CENTER = ":----:";
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
    public static String TABLE_SEPARATOR = "|:----:|----|----|----|----|----|----|----|";
    public static String INDEX_TABLE_HEADER = "|名称|字段|索引类型|索引方法|注释|";
    public static String INDEX_TABLE_BODY = "|%s|%s|%s|%s|%s|";
    public static String INDEX_TABLE_SEPARATOR = "|:----:|----|----|----|----|";

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
    public static final Field[] INDEX_ALL_FIELDS = IndexInfoVO.class.getDeclaredFields();
    /**
     * 需要导出表字段
     **/
    public static Field[] tableFields = TableParameter.class.getDeclaredFields();
    public static final Field[] TABLE_ALL_FIELDS = TableParameter.class.getDeclaredFields();

    /**
     * 读取配置文件，并根据配置文件进行赋值
     **/
    public static void reCheckConfig() {
        //默认导出所有表头
        if (CommonConstant.configMap.containsKey(ConfigEnum.INDEX_TABLE_HEAD.getKey())) {
            String indexHeader = CommonConstant.configMap.get(ConfigEnum.INDEX_TABLE_HEAD.getKey());
            String allHead = MD_SPLIT + StringUtil.join(Arrays.stream(CommonConstant.INDEX_FINAL_HEAD_NAMES).collect(Collectors.toList()), MD_SPLIT) + MD_SPLIT;
            String[] values = indexHeader.split(COMMON_SPLIT);
            String indexStr = init(values.length, ConfigEnum.INDEX_TABLE_HEAD.getKey(), allHead, CommonConstant.INDEX_FINAL_HEAD_NAMES.length);
            if (StringUtil.isNotEmpty(indexStr)) {
                indexHeader = indexStr;
            }
            CommonConstant.INDEX_HEAD_NAMES = Arrays.stream(indexHeader.split(COMMON_SPLIT)).filter(v -> !v.endsWith(HEAD_IGNORE) && StringUtil.isNotEmpty(v)).toArray(String[]::new);
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

            Field[] indexFields = new Field[CommonConstant.INDEX_HEAD_NAMES.length];

            String[] indexHeads = new String[INDEX_ALL_FIELDS.length];
            String[] configColumnHeads = CommonConstant.configMap.get(ConfigEnum.INDEX_TABLE_HEAD.getKey()).substring(1).split(PatternConstant.COMMON_SPLIT);

            for (int i = 0; i < configColumnHeads.length; i++) {
                if (StringUtil.isEmpty(configColumnHeads[i])) {
                    indexHeads[i] = CommonConstant.INDEX_FINAL_HEAD_NAMES[i];
                } else {
                    indexHeads[i] = configColumnHeads[i];
                }
            }

            if (!CommonConstant.configMap.containsKey(ConfigEnum.INDEX_FIELD_INDEX.getKey())) {
                genFields(indexHeads, indexFields, INDEX_ALL_FIELDS);
            } else {
                Integer[] indexes = Arrays.stream(CommonConstant.configMap.get(ConfigEnum.INDEX_FIELD_INDEX.getKey())
                        .split(PatternConstant.COMMON_SPLIT)).map(Integer::parseInt).toArray(Integer[]::new);
                int j = 0;
                for (int i = 0; i < indexes.length; i++) {
                    if (!indexHeads[i].endsWith(HEAD_IGNORE)) {
                        indexFields[j] = INDEX_ALL_FIELDS[indexes[i]];
                        j++;
                    }
                }
            }
            CommonConstant.INDEX_CONFIG_HEAD_NAMES = indexHeads;
            PatternConstant.indexFields = indexFields;

        }
        if (CommonConstant.configMap.containsKey(ConfigEnum.TABLE_HEAD.getKey())) {
            String tableHeader = CommonConstant.configMap.get(ConfigEnum.TABLE_HEAD.getKey());
            String[] values = tableHeader.split(COMMON_SPLIT);
            String allHead = MD_SPLIT + StringUtil.join(Arrays.stream(CommonConstant.COLUMN_FINAL_HEAD_NAMES).collect(Collectors.toList()), MD_SPLIT) + MD_SPLIT;
            String tableStr = init(values.length, ConfigEnum.TABLE_HEAD.getKey(), allHead, CommonConstant.COLUMN_FINAL_HEAD_NAMES.length);
            if (StringUtil.isNotEmpty(tableStr)) {
                tableHeader = tableStr;
            }
            //改变表头
            CommonConstant.COLUMN_HEAD_NAMES = Arrays.stream(tableHeader.split(COMMON_SPLIT)).filter(v -> !v.endsWith(HEAD_IGNORE) && StringUtil.isNotEmpty(v)).toArray(String[]::new);
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


            Field[] tableFields = new Field[CommonConstant.COLUMN_HEAD_NAMES.length];
            String[] columnHeads = new String[TABLE_ALL_FIELDS.length];
            String[] configColumnHeads = CommonConstant.configMap.get(ConfigEnum.TABLE_HEAD.getKey()).substring(1).split(PatternConstant.COMMON_SPLIT);

            for (int i = 0; i < configColumnHeads.length; i++) {
                if (StringUtil.isEmpty(configColumnHeads[i])) {
                    columnHeads[i] = CommonConstant.COLUMN_FINAL_HEAD_NAMES[i];
                } else {
                    columnHeads[i] = configColumnHeads[i];
                }
            }
            if (!CommonConstant.configMap.containsKey(ConfigEnum.TABLE_FIELD_INDEX.getKey())) {
                genFields(columnHeads, tableFields, TABLE_ALL_FIELDS);
            } else {
                Integer[] indexes = Arrays.stream(CommonConstant.configMap.get(ConfigEnum.TABLE_FIELD_INDEX.getKey())
                        .split(PatternConstant.COMMON_SPLIT)).map(Integer::parseInt).toArray(Integer[]::new);
                int j = 0;
                for (int i = 0; i < indexes.length; i++) {
                    if (!columnHeads[i].endsWith(HEAD_IGNORE)) {
                        tableFields[j] = TABLE_ALL_FIELDS[indexes[i]];
                        j++;
                    }
                }
            }
            CommonConstant.COLUMN_CONFIG_HEAD_NAMES = columnHeads;
            PatternConstant.tableFields = tableFields;

        }
        CommonConstant.checkConfigIniFile();
    }

    private static String init(int valuesLength, String key, String allHead, int length) {
        if (valuesLength != length + 1) {
            FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                    key, allHead);
            CommonConstant.checkConfigIniFile();
            return CommonConstant.configMap.get(key);
        }
        return "";
    }

    private static void genFields(String[] heads, Field[] indexFields, Field[] indexAllFields) {
        int j = 0;
        for (int i = 0; i < heads.length; i++) {
            if (!heads[i].endsWith(HEAD_IGNORE)) {
                indexFields[j] = indexAllFields[i];
                j++;
            }
        }
    }
}
