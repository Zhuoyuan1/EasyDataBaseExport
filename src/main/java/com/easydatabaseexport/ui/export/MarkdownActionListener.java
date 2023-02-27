package com.easydatabaseexport.ui.export;

import com.easydatabaseexport.common.PatternConstant;
import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.ui.component.JCheckBoxTree;
import com.easydatabaseexport.ui.export.config.ExportFileType;
import com.easydatabaseexport.util.StringUtil;
import lombok.SneakyThrows;

import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MarkdownActionListener
 *
 * @author lzy
 * @date 2022/11/10 9:25
 **/
public class MarkdownActionListener extends AbstractActionListener implements ActionListener {

    public MarkdownActionListener(final JCheckBoxTree.CheckNode root) {
        super.root = root;
        super.suffix = ExportFileType.MARKDOWN.getSuffix();
    }

    @SneakyThrows
    @Override
    public boolean export(File markdownFile) {
        Map<String, List<Map.Entry<String, List<TableParameter>>>> allMap = listMap.entrySet()
                .stream().collect(Collectors.groupingBy(v -> v.getKey().split("---")[0]));
        try (BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(markdownFile, true), StandardCharsets.UTF_8.name()))) {
            for (Map.Entry<String, List<Map.Entry<String, List<TableParameter>>>> myMap : allMap.entrySet()) {
                //数据库名
                String database = myMap.getKey();
                String title = String.format(PatternConstant.TITLE, "数据库：" + database);
                fileWriter.write(title);
                writeLineSeparator(fileWriter, 2);
                for (Map.Entry<String, List<TableParameter>> parameterMap : myMap.getValue()) {
                    //表名
                    String tableName = parameterMap.getKey().split("---")[1];
                    fileWriter.write(String.format(PatternConstant.CATALOG, tableName));
                    writeLineSeparator(fileWriter, 1);
                    //索引Table
                    if (indexMap.size() > 0) {
                        fileWriter.write(PatternConstant.INDEX_TABLE_HEADER);
                        writeLineSeparator(fileWriter, 1);
                        fileWriter.write(PatternConstant.INDEX_TABLE_SEPARATOR);
                        writeLineSeparator(fileWriter, 1);
                        String name = parameterMap.getKey().split("\\[")[0];
                        List<IndexInfoVO> indexInfoVOList = indexMap.get(name);
                        if (!indexInfoVOList.isEmpty()) {
                            for (IndexInfoVO indexInfoVO : indexInfoVOList) {
                                fileWriter.write(String.format(PatternConstant.INDEX_TABLE_BODY, getIndexValues(indexInfoVO)));
                                writeLineSeparator(fileWriter, 1);
                            }
                        } else {
                            fileWriter.write(String.format(PatternConstant.INDEX_TABLE_BODY, getIndexValues(new IndexInfoVO())));
                            writeLineSeparator(fileWriter, 1);
                        }
                        writeLineSeparator(fileWriter, 1);
                    }
                    writeLineSeparator(fileWriter, 2);
                    fileWriter.write(PatternConstant.TABLE_HEADER);
                    writeLineSeparator(fileWriter, 1);
                    fileWriter.write(PatternConstant.TABLE_SEPARATOR);
                    writeLineSeparator(fileWriter, 1);
                    //字段Table
                    List<TableParameter> exportList = parameterMap.getValue();
                    for (TableParameter tableParameter : exportList) {
                        fileWriter.write(String.format(PatternConstant.TABLE_BODY, getColumnValues(tableParameter)));
                        writeLineSeparator(fileWriter, 1);
                    }
                    writeLineSeparator(fileWriter, 2);
                }
            }
        }
        return Boolean.TRUE;
    }

    private void writeLineSeparator(BufferedWriter fileWriter, int number) throws IOException {
        for (int i = 0; i < number; i++) {
            fileWriter.write(System.lineSeparator());
        }
    }

    @Override
    public String dealWith(String source) {
        return StringUtil.stringEqualHtml(source);
    }
}
