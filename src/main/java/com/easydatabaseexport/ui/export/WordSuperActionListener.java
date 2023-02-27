package com.easydatabaseexport.ui.export;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.Includes;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.Rows;
import com.deepoove.poi.data.Tables;
import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.EnvironmentConstant;
import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.ui.component.JCheckBoxTree;
import com.easydatabaseexport.ui.export.config.ExportFileType;
import com.easydatabaseexport.util.AddToTopic;
import com.easydatabaseexport.util.FileOperateUtil;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WordSuperActionListener 自定义表头版 word导出
 *
 * @author lzy
 * @date 2022/11/24 15:03
 **/
@Log
public class WordSuperActionListener extends AbstractActionListener implements ActionListener {

    public WordSuperActionListener(final JCheckBoxTree.CheckNode root) {
        super.root = root;
        super.suffix = ExportFileType.WORD.getSuffix();
    }

    /**
     * Word导出
     **/
    @SneakyThrows
    @Override
    public boolean export(File file) {
        String filePath = FileOperateUtil.getSavePath() + CommonConstant.templateDir + File.separator + EnvironmentConstant.TEMPLATE_FILE.get(3);
        String subFile = FileOperateUtil.getSavePath() + CommonConstant.templateDir + File.separator + EnvironmentConstant.TEMPLATE_FILE.get(4);
        File importWordFile = new File(filePath);
        Map<String, List<Map.Entry<String, List<TableParameter>>>> allMap = listMap.entrySet()
                .stream().collect(Collectors.groupingBy(v -> v.getKey().split("---")[0]));
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> myDataMap = new HashMap<>(2);
        //索引表头
        RowRenderData indexHeaderRow = Rows.of(CommonConstant.INDEX_HEAD_NAMES).center().textBold().textColor("000000").bgColor("bfbfbf").create();
        //字段表头
        RowRenderData tableHeaderRow = Rows.of(CommonConstant.COLUMN_HEAD_NAMES).center().textBold().textColor("000000").bgColor("bfbfbf").create();
        for (Map.Entry<String, List<Map.Entry<String, List<TableParameter>>>> myMap : allMap.entrySet()) {
            //数据库名
            String database = myMap.getKey();
            int i = 1;
            for (Map.Entry<String, List<TableParameter>> parameterMap : myMap.getValue()) {
                //初始化容量 3/0.75 + 1
                Map<String, Object> tableData = new HashMap<>(8);
                //索引Table
                if (indexMap.size() > 0) {
                    String name = parameterMap.getKey().split("\\[")[0];
                    List<IndexInfoVO> indexInfoVOList = indexMap.get(name);
                    List<RowRenderData> rowList = getIndexValues(indexInfoVOList, indexHeaderRow);
                    tableData.put("indexTable", Tables.create(rowList.toArray(new RowRenderData[0])));
                }
                if (i == 1) {
                    Map<String, String> map = new HashMap<>(2);
                    map.put("dataBase", database);
                    tableData.put("ifDatabase", map);
                }
                //表名
                String tableName = parameterMap.getKey().split("---")[1];
                tableData.put("number", i);
                tableData.put("name", tableName);
                List<TableParameter> tableParameterList = parameterMap.getValue();
                List<RowRenderData> rowList = getColumnValues(tableParameterList, tableHeaderRow);
                tableData.put("table", Tables.create(rowList.toArray(new RowRenderData[0])));
                i++;
                list.add(tableData);
            }
        }
        myDataMap.put("mydata", Includes.ofLocal(subFile).setRenderModel(list).create());
        /*根据模板生成文档*/
        XWPFTemplate template = XWPFTemplate.compile(importWordFile).render(myDataMap);
        //添加目录
        AddToTopic.generateTOC(template.getXWPFDocument(), file.getAbsolutePath());
        return Boolean.TRUE;
    }

    @SneakyThrows
    public List<RowRenderData> getColumnValues(List<TableParameter> list, RowRenderData tableHeaderRow) {
        List<RowRenderData> rowRenderDataList = new ArrayList<>();
        rowRenderDataList.add(tableHeaderRow);
        for (TableParameter tableParameter : list) {
            String[] values = Arrays.stream(getColumnValues(tableParameter)).toArray(String[]::new);
            rowRenderDataList.add(Rows.of(values).center().create());
        }
        return rowRenderDataList;
    }


    @SneakyThrows
    public List<RowRenderData> getIndexValues(List<IndexInfoVO> list, RowRenderData tableHeaderRow) {
        List<RowRenderData> rowRenderDataList = new ArrayList<>();
        rowRenderDataList.add(tableHeaderRow);
        if (list.isEmpty()) {
            String[] values = Arrays.stream(getIndexValues(new IndexInfoVO())).toArray(String[]::new);
            rowRenderDataList.add(Rows.of(values).center().create());
            return rowRenderDataList;
        }
        for (IndexInfoVO indexInfoVO : list) {
            String[] values = Arrays.stream(getIndexValues(indexInfoVO)).toArray(String[]::new);
            rowRenderDataList.add(Rows.of(values).center().create());
        }
        return rowRenderDataList;
    }
}
