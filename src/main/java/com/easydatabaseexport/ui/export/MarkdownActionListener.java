package com.easydatabaseexport.ui.export;

import com.easydatabaseexport.common.PatternConstant;
import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.ui.AbstractActionListener;
import com.easydatabaseexport.ui.component.JCheckBoxTree;
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
    public void export(File markdownFile) {
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
                        for (int j = 0; j < indexInfoVOList.size(); j++) {
                            fileWriter.write(String.format(PatternConstant.INDEX_TABLE_BODY, getIndexValues(indexInfoVOList.get(j))));
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
                    for (int i = 0; i < exportList.size(); i++) {
                        fileWriter.write(String.format(PatternConstant.TABLE_BODY, getColumnValues((i + 1) + "", exportList.get(i))));
                        writeLineSeparator(fileWriter, 1);
                    }
                    writeLineSeparator(fileWriter, 2);
                }
            }
        }
    }

    private void writeLineSeparator(BufferedWriter fileWriter, int number) throws IOException {
        for (int i = 0; i < number; i++) {
            fileWriter.write(System.lineSeparator());
        }
    }

    /**
     * 表结构和表索引数据组装
     **/
    @Override
    public boolean dataAssemble() {
        return dataAssembleAndJudge(root);
    }
}
