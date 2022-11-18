package com.easydatabaseexport.ui.export;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.EnvironmentConstant;
import com.easydatabaseexport.common.PatternConstant;
import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.ui.AbstractActionListener;
import com.easydatabaseexport.ui.component.JCheckBoxTree;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.StringUtil;
import lombok.SneakyThrows;

import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HtmlActionListener
 *
 * @author lzy
 * @date 2022/11/10 9:25
 **/
public class HtmlActionListener extends AbstractActionListener implements ActionListener {

    public HtmlActionListener(final JCheckBoxTree.CheckNode root) {
        super.root = root;
        super.suffix = ExportFileType.HTML.getSuffix();
    }

    @SneakyThrows
    @Override
    public void export(File file) {
        Map<String, List<Map.Entry<String, List<TableParameter>>>> allMap = listMap.entrySet()
                .stream().collect(Collectors.groupingBy(v -> v.getKey().split("---")[0]));
        StringBuilder htmlText = new StringBuilder();
        StringBuilder catalogue = new StringBuilder();
        for (Map.Entry<String, List<Map.Entry<String, List<TableParameter>>>> myMap : allMap.entrySet()) {
            //数据库名
            String database = myMap.getKey();
            String title = MessageFormat.format(PatternConstant.HTML_TITLE, "数据库：" + database);
            //数据库名-目录
            catalogue.append("<li>").append(MessageFormat.format(PatternConstant.HTML_INDEX_ITEM, "数据库：" + database, "数据库：" + database)).append("<ol>");
            htmlText.append(title).append("\n");
            for (Map.Entry<String, List<TableParameter>> parameterMap : myMap.getValue()) {
                //表名
                String tableName = parameterMap.getKey().split("---")[1];
                //表名-目录
                catalogue.append("<li>").append(MessageFormat.format(PatternConstant.HTML_INDEX_ITEM, database + tableName, tableName));
                htmlText.append(MessageFormat.format(PatternConstant.HTML_CATALOG, database + tableName, tableName)).append("\n<p></p>");
                //索引Table
                if (indexMap.size() > 0) {
                    htmlText.append("<table>\n");
                    htmlText.append(PatternConstant.HTML_INDEX_TABLE_HEADER);
                    String name = parameterMap.getKey().split("\\[")[0];
                    List<IndexInfoVO> indexInfoVOList = indexMap.get(name);
                    for (int j = 0; j < indexInfoVOList.size(); j++) {
                        htmlText.append(String.format(PatternConstant.HTML_INDEX_TABLE_BODY, getIndexValues(indexInfoVOList.get(j))));
                    }
                    htmlText.append("</table>\n");
                    htmlText.append("\n<p></p>");
                }

                //字段Table
                htmlText.append("<table>\n");
                htmlText.append(PatternConstant.HTML_TABLE_HEADER);
                List<TableParameter> exportList = parameterMap.getValue();
                for (int i = 0; i < exportList.size(); i++) {
                    htmlText.append(String.format(PatternConstant.HTML_TABLE_BODY, getColumnValues((i + 1) + "", exportList.get(i))));
                }
                htmlText.append("</table>\n");
            }
            htmlText.append("<p></p>");
            catalogue.append("</ol>");
        }
        catalogue.append("</li>");

        String filePath = FileOperateUtil.getSavePath() + CommonConstant.templateDir + File.separator + EnvironmentConstant.TEMPLATE_FILE.get(2);
        FileInputStream inputStream = new FileInputStream(filePath);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        String str = result.toString(StandardCharsets.UTF_8.name());

        str = str.replace("${data}", htmlText).replace("${catalogue}", catalogue);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true), StandardCharsets.UTF_8.name()));
        writer.write(str);
        writer.close();
        inputStream.close();
        result.close();
    }

    @Override
    public Object[] getIndexValues(IndexInfoVO indexInfoVO) {
        Object[] values = new Object[5];
        values[0] = StringUtil.stringEqualHtml(indexInfoVO.getName());
        values[1] = StringUtil.stringEqualHtml(indexInfoVO.getColumnName());
        values[2] = StringUtil.stringEqualHtml(indexInfoVO.getIndexType());
        values[3] = StringUtil.stringEqualHtml(indexInfoVO.getIndexMethod());
        values[4] = StringUtil.stringEqualHtml(indexInfoVO.getComment());
        return values;
    }

    /**
     * 表结构和表索引数据组装
     **/
    @Override
    public boolean dataAssemble() {
        return dataAssembleAndJudge(root);
    }
}
