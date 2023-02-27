package com.easydatabaseexport.util;

import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.log.LogManager;
import com.mysql.cj.util.StringUtils;
import lombok.extern.java.Log;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * WordReporter
 *
 * @author lzy
 * @date 2021/2/28 19:02
 **/
@Log
public class WordReporter {
    private String tempLocalPath;
    private XWPFDocument xwpfDocument = null;
    private FileInputStream inputStream = null;
    private OutputStream outputStream = null;

    public WordReporter() {

    }

    public WordReporter(String tempLocalPath) {
        this.tempLocalPath = tempLocalPath;
    }

    /**
     * 设置模板路径
     *
     * @param tempLocalPath
     */
    public void setTempLocalPath(String tempLocalPath) {
        this.tempLocalPath = tempLocalPath;
    }

    /**
     * 初始化
     *
     * @throws IOException
     */
    public void init() throws IOException {
        inputStream = new FileInputStream(new File(this.tempLocalPath));
        xwpfDocument = new XWPFDocument(inputStream);
    }

    /**
     * 导出方法
     *
     * @param params
     * @param tableIndex
     * @return
     * @throws Exception
     */
    public boolean export(List<Map<String, String>> params, Map<String, Object> paramsMap, int tableIndex) throws Exception {
        this.insertValueToTable(xwpfDocument, params, paramsMap, tableIndex);
        return true;
    }

    /**
     * 导出方法
     *
     * @param tableIndex
     * @return
     * @throws Exception
     */
    public boolean exportWORD(Map<String, List<TableParameter>> paramsMap, Map<String, List<IndexInfoVO>> indexMap, int tableIndex) throws Exception {
        if (indexMap.size() > 0) {
            this.insertValueToTableWORD(xwpfDocument, paramsMap, indexMap, tableIndex);
        } else {
            this.insertValueToTableWORD(xwpfDocument, paramsMap, tableIndex);
        }
        return true;
    }

    private void insertValueToTableWORD(XWPFDocument xwpfDocument, Map<String, List<TableParameter>> paramsMap, Map<String, List<IndexInfoVO>> indexMap, int tableIndex) throws Exception {
        List<XWPFTable> tableList = xwpfDocument.getTables();
        if (tableList.size() <= tableIndex) {
            throw new Exception("tableIndex对应的表格不存在");
        }
        XWPFTable table = tableList.get(tableIndex);
        List<XWPFTableRow> rows = table.getRows();
        if (rows.size() < 2) {
            throw new Exception("tableIndex对应表格应该为2行");
        }

        Map<String, Object> paramsValueMap = new HashMap<>();
        List<Map<String, String>> list = new ArrayList<>();
        List<Map<String, String>> indexListMap = new ArrayList<>();
        int index = 0;
        int indexTwo = 0;

        Map<String, List<Map.Entry<String, List<TableParameter>>>> allMap = paramsMap.entrySet()
                .stream().collect(Collectors.groupingBy(v -> v.getKey().split("---")[0]));

        List<Integer> indexList = new ArrayList<>();
        for (Map.Entry<String, List<Map.Entry<String, List<TableParameter>>>> myMap : allMap.entrySet()) {
            //数据库名
            paramsValueMap.put("dataBase" + indexTwo, myMap.getKey());
            //放入索引
            if (index != 0) {
                indexList.add(index);
            }
            for (Map.Entry<String, List<TableParameter>> parameterMap : myMap.getValue()) {
                List<TableParameter> exportList = parameterMap.getValue();
                for (TableParameter tableParameter : exportList) {
                    Map<String, String> map = new HashMap<>();
                    map.put("no", tableParameter.getNo());
                    map.put("columnName", tableParameter.getColumnName());
                    map.put("columnType", tableParameter.getColumnType());
                    map.put("length", tableParameter.getLength());
                    map.put("isNullAble", tableParameter.getIsNullAble());
                    map.put("columnDefault", tableParameter.getColumnDefault());
                    map.put("decimalPlaces", tableParameter.getDecimalPlaces());
                    map.put("columnComment", tableParameter.getColumnComment());
                    list.add(map);
                }
                paramsValueMap.put("tableName" + index, parameterMap.getKey().split("---")[1]);
                if (indexMap.size() > 0) {
                    String name = parameterMap.getKey().split("\\[")[0];
                    List<IndexInfoVO> indexInfoVOList = indexMap.get(name);
                    for (int j = 0; j < indexInfoVOList.size(); j++) {
                        Map<String, String> map = new HashMap<>();
                        map.put("no", (j + 1) + "");
                        map.put("name", indexInfoVOList.get(j).getName());
                        map.put("columnName", indexInfoVOList.get(j).getColumnName());
                        map.put("indexType", indexInfoVOList.get(j).getIndexType());
                        map.put("indexMethod", indexInfoVOList.get(j).getIndexMethod());
                        map.put("comment", indexInfoVOList.get(j).getComment());
                        indexListMap.add(map);
                    }
                }
                index++;
            }
            indexTwo++;
        }

        //添加标题样式1
        //addCustomHeadingStyle(xwpfDocument,"标题 1", 1);
        int indexNumFor = 1;
        //首先生成足够多的table
        if (paramsMap.size() > 1) {
            //这里是需要设置到table的数组，更具数组长度创建多个table
            int i = 1, k = 2;
            for (int j = 1; j < paramsMap.size(); j++) {
                if (indexList.contains(j)) {
                    XWPFParagraph px = xwpfDocument.createParagraph();
                    XWPFRun runx = px.createRun();
                    //添加目录级别
                    CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance();
                    indentNumber.setVal(BigInteger.valueOf(0));
                    CTPPr ppr = CTPPr.Factory.newInstance();
                    ppr.setOutlineLvl(indentNumber);
                    px.getCTP().setPPr(ppr);
                    //设置左对齐
                    runx.setText("数据库：${dataBase" + indexNumFor + "}");
                    px.setAlignment(ParagraphAlignment.LEFT);
                    px.setPageBreak(true);
                    indexNumFor++;
                    //重置索引
                    k = 1;
                }
                /* ------------------索引表格----------------*/
                CTTbl ctTbl1 = CTTbl.Factory.newInstance();
                // 创建新的 CTTbl ， table
                ctTbl1.set(xwpfDocument.getTables().get(0).getCTTbl());
                // 复制原来的CTTbl
                IBody iBody1 = xwpfDocument.getTables().get(0).getBody();
                XWPFTable newTable1 = new XWPFTable(ctTbl1, iBody1);
                /* ------------------字段表格----------------*/
                CTTbl ctTbl = CTTbl.Factory.newInstance();
                // 创建新的 CTTbl ， table
                ctTbl.set(xwpfDocument.getTables().get(1).getCTTbl());
                // 复制原来的CTTbl
                IBody iBody = xwpfDocument.getTables().get(1).getBody();
                XWPFTable newTable = new XWPFTable(ctTbl, iBody);
                // 新增一个table，使用复制好的Cttbl
                XWPFParagraph xwpfParagraph = xwpfDocument.createParagraph();
                XWPFRun xwpfRun = xwpfParagraph.createRun();
                //设置左对齐
                xwpfParagraph.setAlignment(ParagraphAlignment.LEFT);
                xwpfRun.setText(k + "、${tableName" + j + "}");
                //添加目录级别
                CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance();
                indentNumber.setVal(BigInteger.valueOf(1));
                CTPPr ppr = CTPPr.Factory.newInstance();
                ppr.setOutlineLvl(indentNumber);
                xwpfParagraph.getCTP().setPPr(ppr);

                //设置分页
                if (!indexList.contains(j)) {
                    xwpfParagraph.setPageBreak(true);
                }
                //xwpfParagraph.setSpacingBefore(2000);
                //设置每页的title
                //xwpfRun.setBold(true);

                xwpfDocument.createTable();
                setDataToTable(indexListMap, newTable1, j);
                xwpfDocument.setTable(i + 1, newTable1);

                XWPFParagraph blankParagraph = xwpfDocument.createParagraph();
                blankParagraph.setSpacingBefore(newTable1.getNumberOfRows() > 1 ? newTable1.getNumberOfRows() * 300 + 200 : 100);
                // 创建一个空的Table
                xwpfDocument.createTable();
                // 将table设置到word中
                setDataToTable(list, newTable, j);
                xwpfDocument.setTable(i + 2, newTable);
                i += 2;
                k++;
            }
        }

        //替换段落变量
        replaceInPara(xwpfDocument, paramsValueMap);
        changeTable(tableList, indexListMap, 0);
        changeTable(tableList, list, 1);

    }

    private void changeTable(List<XWPFTable> tableList, List<Map<String, String>> list, int index) throws Exception {
        //计no 1 出现的次数
        int indexNumber = 0;
        XWPFTable table = null;
        List<XWPFTableRow> rows;
        for (int i = 0, len = list.size(); i < len; i++) {
            if ("1".equals(list.get(i).get("no")) && i > 0) {
                indexNumber++;
            }
            if (indexNumber >= 1) {
                break;
            }
            //模板的那一行
            table = tableList.get(index);
            rows = table.getRows();
            XWPFTableRow tmpRow = rows.get(1);
            List<XWPFTableCell> tmpCells = tmpRow.getTableCells();
            Map<String, String> map = list.get(i);
            // 创建新的一行
            XWPFTableRow row = table.createRow();
            // 获取模板的行高 设置为新一行的行高
            row.setHeight(tmpRow.getHeight());
            List<XWPFTableCell> cells = row.getTableCells();
            for (int k = 0, klen = cells.size(); k < klen; k++) {
                XWPFTableCell tmpCell = null;
                String cellText = null;
                String cellTextKey = null;
                tmpCell = tmpCells.get(k);
                XWPFTableCell cell = cells.get(k);
                cellText = tmpCell.getText();
                if (!StringUtils.isNullOrEmpty(cellText)) {
                    //转换为mapkey对应的字段
                    cellTextKey = cellText.replace("$", "").replace("{", "").replace("}", "");
                    if (map.containsKey(cellTextKey)) {
                        // 填充内容 并且复制模板行的属性
                        setCellText(tmpCell, cell, map.get(cellTextKey));
                    }
                }
            }
        }
        table.removeRow(1);
    }

    private void insertValueToTableWORD(XWPFDocument xwpfDocument, Map<String, List<TableParameter>> paramsMap, int tableIndex) throws Exception {
        List<XWPFTable> tableList = xwpfDocument.getTables();
        if (tableList.size() <= tableIndex) {
            throw new Exception("tableIndex对应的表格不存在");
        }
        XWPFTable table = tableList.get(tableIndex);
        List<XWPFTableRow> rows = table.getRows();
        if (rows.size() < 2) {
            throw new Exception("tableIndex对应表格应该为2行");
        }

        Map<String, Object> paramsValueMap = new HashMap<>();
        List<Map<String, String>> list = new ArrayList<>();
        int index = 0;
        int indexTwo = 0;

        Map<String, List<Map.Entry<String, List<TableParameter>>>> allDataMap = paramsMap.entrySet()
                .stream().collect(Collectors.groupingBy(v -> v.getKey().split("---")[0]));

        Map<String, List<Map.Entry<String, List<TableParameter>>>> allMap = new LinkedHashMap<>();
        allDataMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEachOrdered(x -> allMap.put(x.getKey(), x.getValue()));

        List<Integer> indexList = new ArrayList<>();
        for (Map.Entry<String, List<Map.Entry<String, List<TableParameter>>>> myMap : allMap.entrySet()) {
            //数据库名
            paramsValueMap.put("dataBase" + indexTwo, myMap.getKey());
            //放入索引
            if (index != 0) {
                indexList.add(index);
            }
            for (Map.Entry<String, List<TableParameter>> parameterMap : myMap.getValue()) {
                List<TableParameter> exportList = parameterMap.getValue();
                for (TableParameter tableParameter : exportList) {
                    Map<String, String> map = new HashMap<>();
                    map.put("no", tableParameter.getNo());
                    map.put("columnName", tableParameter.getColumnName());
                    map.put("columnType", tableParameter.getColumnType());
                    map.put("length", tableParameter.getLength());
                    map.put("isNullAble", tableParameter.getIsNullAble());
                    map.put("columnDefault", tableParameter.getColumnDefault());
                    map.put("decimalPlaces", tableParameter.getDecimalPlaces());
                    map.put("columnComment", tableParameter.getColumnComment());
                    list.add(map);
                }
                paramsValueMap.put("tableName" + index, parameterMap.getKey().split("---")[1]);
                index++;
            }
            indexTwo++;
        }

        //添加标题样式1
        //addCustomHeadingStyle(xwpfDocument,"标题 1", 1);
        int indexNumFor = 1;
        //首先生成足够多的table
        if (paramsMap.size() > 1) {
            int k = 2;
            //这里是需要设置到table的数组，更具数组长度创建多个table
            for (int i = 1; i < paramsMap.size(); i++) {
                if (indexList.contains(i)) {
                    XWPFParagraph px = xwpfDocument.createParagraph();
                    XWPFRun runx = px.createRun();
                    //添加目录级别
                    CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance();
                    indentNumber.setVal(BigInteger.valueOf(0));
                    CTPPr ppr = CTPPr.Factory.newInstance();
                    ppr.setOutlineLvl(indentNumber);
                    px.getCTP().setPPr(ppr);
                    //设置左对齐
                    runx.setText("数据库：${dataBase" + indexNumFor + "}");
                    px.setAlignment(ParagraphAlignment.LEFT);
                    px.setPageBreak(true);
                    indexNumFor++;
                    //重置索引
                    k = 1;
                }
                CTTbl ctTbl = CTTbl.Factory.newInstance();
                // 创建新的 CTTbl ， table
                ctTbl.set(xwpfDocument.getTables().get(0).getCTTbl());
                // 复制原来的CTTbl
                IBody iBody = xwpfDocument.getTables().get(0).getBody();
                XWPFTable newTable = new XWPFTable(ctTbl, iBody);
                // 新增一个table，使用复制好的Cttbl
                XWPFParagraph xwpfParagraph = xwpfDocument.createParagraph();
                XWPFRun xwpfRun = xwpfParagraph.createRun();
                //设置左对齐
                xwpfParagraph.setAlignment(ParagraphAlignment.LEFT);
                xwpfRun.setText(k + "、${tableName" + i + "}");
                //添加目录级别
                CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance();
                indentNumber.setVal(BigInteger.valueOf(1));
                CTPPr ppr = CTPPr.Factory.newInstance();
                ppr.setOutlineLvl(indentNumber);
                xwpfParagraph.getCTP().setPPr(ppr);

                //设置分页
                if (!indexList.contains(i)) {
                    xwpfParagraph.setPageBreak(true);
                }
                //xwpfParagraph.setSpacingBefore(2000);
                //设置每页的title
                //xwpfRun.setBold(true);

                // 创建一个空的Table
                xwpfDocument.createTable();
                // 将table设置到word中
                setDataToTable(list, newTable, i);
                xwpfDocument.setTable(i, newTable);
                k++;
            }
        }

        //替换段落变量
        replaceInPara(xwpfDocument, paramsValueMap);

        //计no 1 出现的次数
        int indexNum = 0;
        for (int i = 0, len = list.size(); i < len; i++) {
            if ("1".equals(list.get(i).get("no")) && i > 0) {
                indexNum++;
            }
            if (indexNum >= 1) {
                break;
            }
            //模板的那一行
            table = tableList.get(0);
            rows = table.getRows();
            XWPFTableRow tmpRow = rows.get(1);
            List<XWPFTableCell> tmpCells = tmpRow.getTableCells();
            Map<String, String> map = list.get(i);
            // 创建新的一行
            XWPFTableRow row = table.createRow();
            // 获取模板的行高 设置为新一行的行高
            row.setHeight(tmpRow.getHeight());
            List<XWPFTableCell> cells = row.getTableCells();
            for (int k = 0, klen = cells.size(); k < klen; k++) {
                XWPFTableCell tmpCell = null;
                String cellText = null;
                String cellTextKey = null;
                tmpCell = tmpCells.get(k);
                XWPFTableCell cell = cells.get(k);
                cellText = tmpCell.getText();
                if (!StringUtils.isNullOrEmpty(cellText)) {
                    //转换为mapkey对应的字段
                    cellTextKey = cellText.replace("$", "").replace("{", "").replace("}", "");
                    if (map.containsKey(cellTextKey)) {
                        // 填充内容 并且复制模板行的属性
                        setCellText(tmpCell, cell, map.get(cellTextKey));
                    }
                }
            }
        }
        table.removeRow(1);
    }

    /**
     * 增加自定义标题样式。这里用的是stackoverflow的源码
     *
     * @param docxDocument 目标文档
     * @param strStyleId   样式名称
     * @param headingLevel 样式级别
     */
    private static void addCustomHeadingStyle(XWPFDocument docxDocument, String strStyleId, int headingLevel) {

        CTStyle ctStyle = CTStyle.Factory.newInstance();
        ctStyle.setStyleId(strStyleId);

        CTString styleName = CTString.Factory.newInstance();
        styleName.setVal(strStyleId);
        ctStyle.setName(styleName);

        CTDecimalNumber indentNumber = CTDecimalNumber.Factory.newInstance();
        indentNumber.setVal(BigInteger.valueOf(headingLevel));

        // lower number > style is more prominent in the formats bar
        ctStyle.setUiPriority(indentNumber);


        // style defines a heading of the given level
        CTPPrGeneral ppr = CTPPrGeneral.Factory.newInstance();
        ppr.setOutlineLvl(indentNumber);
        ctStyle.setPPr(ppr);

        XWPFStyle style = new XWPFStyle(ctStyle);

        // is a null op if already defined
        XWPFStyles styles = docxDocument.createStyles();

        style.setType(STStyleType.PARAGRAPH);
        styles.addStyle(style);

    }

    private void setDataToTable(List<Map<String, String>> list, XWPFTable table, int j) throws Exception {
        //计no 1 出现的次数
        int index = 0;
        for (int i = 0, len = list.size(); i < len; i++) {
            if ("1".equals(list.get(i).get("no")) && i > 0) {
                index++;
            }
            if (index != j) {
                continue;
            }
            List<XWPFTableRow> rows = table.getRows();
            XWPFTableRow tmpRow = rows.get(1);
            List<XWPFTableCell> tmpCells = tmpRow.getTableCells();
            Map<String, String> map = list.get(i);
            // 创建新的一行
            XWPFTableRow row = table.createRow();
            // 获取模板的行高 设置为新一行的行高
            row.setHeight(tmpRow.getHeight());
            List<XWPFTableCell> cells = row.getTableCells();
            for (int k = 0, klen = cells.size(); k < klen; k++) {
                XWPFTableCell tmpCell = null;
                String cellText = null;
                String cellTextKey = null;
                tmpCell = tmpCells.get(k);
                XWPFTableCell cell = cells.get(k);
                cellText = tmpCell.getText();
                if (!StringUtils.isNullOrEmpty(cellText)) {
                    //转换为mapkey对应的字段
                    cellTextKey = cellText.replace("$", "").replace("{", "").replace("}", "");
                    if (map.containsKey(cellTextKey)) {
                        // 填充内容 并且复制模板行的属性
                        setCellText(tmpCell, cell, map.get(cellTextKey));
                    }
                }
            }
        }
        table.removeRow(1);
    }

    /**
     * 循环填充表格内容
     *
     * @param xwpfDocument
     * @param params
     * @param tableIndex
     * @throws Exception
     */
    private void insertValueToTable(XWPFDocument xwpfDocument, List<Map<String, String>> params, Map<String, Object> paramsMap, int tableIndex) throws Exception {
        replaceInPara(xwpfDocument, paramsMap);
        List<XWPFTable> tableList = xwpfDocument.getTables();
        if (tableList.size() <= tableIndex) {
            throw new Exception("tableIndex对应的表格不存在");
        }
        XWPFTable table = tableList.get(tableIndex);
        List<XWPFTableRow> rows = table.getRows();
        if (rows.size() < 2) {
            throw new Exception("tableIndex对应表格应该为2行");
        }

        //模板的那一行
        XWPFTableRow tmpRow = rows.get(1);
        List<XWPFTableCell> tmpCells = null;
        List<XWPFTableCell> cells = null;
        XWPFTableCell tmpCell = null;
        tmpCells = tmpRow.getTableCells();


        String cellText = null;
        String cellTextKey = null;
        Map<String, Object> totalMap = null;
        for (int i = 0, len = params.size(); i < len; i++) {
            Map<String, String> map = params.get(i);
            // 创建新的一行
            XWPFTableRow row = table.createRow();
            // 获取模板的行高 设置为新一行的行高
            row.setHeight(tmpRow.getHeight());
            cells = row.getTableCells();
            for (int k = 0, klen = cells.size(); k < klen; k++) {
                tmpCell = tmpCells.get(k);
                XWPFTableCell cell = cells.get(k);
                cellText = tmpCell.getText();
                if (!StringUtils.isNullOrEmpty(cellText)) {
                    //转换为mapkey对应的字段
                    cellTextKey = cellText.replace("$", "").replace("{", "").replace("}", "");
                    if (map.containsKey(cellTextKey)) {
                        // 填充内容 并且复制模板行的属性
                        setCellText(tmpCell, cell, map.get(cellTextKey));
                    }
                }
            }

        }
        // 删除模版行
        table.removeRow(1);
    }

    /**
     * 替换段落里面的变量
     *
     * @param doc    要替换的文档
     * @param params 参数
     */
    private void replaceInPara(XWPFDocument doc, Map<String, Object> params) {
        for (XWPFParagraph para : doc.getParagraphs()) {
            this.replaceInPara(para, params);
        }
    }

    /**
     * 替换段落里面的变量
     *
     * @param para   要替换的段落
     * @param params 参数
     */
    private void replaceInPara(XWPFParagraph para, Map<String, Object> params) {
        List<XWPFRun> runs;
        Matcher matcher;
        this.replaceText(para);//如果para拆分的不对，则用这个方法修改成正确的
        if (this.matcher(para.getParagraphText()).find()) {
            runs = para.getRuns();
            for (int i = 0; i < runs.size(); i++) {
                XWPFRun run = runs.get(i);
                String runText = run.toString();
                matcher = this.matcher(runText);
                if (matcher.find()) {
                    while ((matcher = this.matcher(runText)).find()) {
                        runText = matcher.replaceFirst(Matcher.quoteReplacement(String.valueOf(params.get(matcher.group(1)))));
                    }
                    //直接调用XWPFRun的setText()方法设置文本时，在底层会重新创建一个XWPFRun，把文本附加在当前文本后面，
                    //所以我们不能直接设值，需要先删除当前run,然后再自己手动插入一个新的run。
                    para.removeRun(i);
                    para.insertNewRun(i).setText(runText);
                    if (runText.startsWith("数据库")) {
                        List<XWPFRun> runs1 = para.getRuns();
                        XWPFRun runX = runs1.get(i);
                        runX.setBold(false);
                        runX.setFontFamily("宋体");
                        runX.setFontSize(18);
                    }
                }
            }
        }
    }

    /**
     * 正则匹配字符串
     *
     * @param str
     * @return
     */
    private Matcher matcher(String str) {
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher;
    }

    /**
     * 合并runs中的内容
     *
     * @param para
     * @return
     */
    private List<XWPFRun> replaceText(XWPFParagraph para) {
        List<XWPFRun> runs = para.getRuns();
        String str = "";
        boolean flag = false;
        for (int i = 0; i < runs.size(); i++) {
            XWPFRun run = runs.get(i);
            String runText = run.toString();
            if (flag || runText.equals("${")) {
                str = str + runText;
                flag = true;
                para.removeRun(i);
                if (runText.equals("}")) {
                    flag = false;
                    para.insertNewRun(i).setText(str);
                    str = "";
                }
                i--;
            }

        }
        return runs;
    }

    /**
     * 复制模板行的属性
     *
     * @param tmpCell
     * @param cell
     * @param text
     * @throws Exception
     */
    private void setCellText(XWPFTableCell tmpCell, XWPFTableCell cell, String text) throws Exception {

        CTTc cttc2 = tmpCell.getCTTc();
        CTTcPr ctPr2 = cttc2.getTcPr();
        CTTc cttc = cell.getCTTc();
        CTTcPr ctPr = cttc.addNewTcPr();
        if (ctPr2.getTcW() != null) {
            ctPr.addNewTcW().setW(ctPr2.getTcW().getW());
        }
        if (ctPr2.getVAlign() != null) {
            ctPr.addNewVAlign().setVal(ctPr2.getVAlign().getVal());
        }
        if (cttc2.getPList().size() > 0) {
            CTP ctp = cttc2.getPList().get(0);
            if (ctp.getPPr() != null) {
                if (ctp.getPPr().getJc() != null) {
                    cttc.getPList().get(0).addNewPPr().addNewJc()
                            .setVal(ctp.getPPr().getJc().getVal());
                }
            }
        }
        if (ctPr2.getTcBorders() != null) {
            ctPr.setTcBorders(ctPr2.getTcBorders());
        }

        XWPFParagraph tmpP = tmpCell.getParagraphs().get(0);
        XWPFParagraph cellP = cell.getParagraphs().get(0);
        XWPFRun tmpR = null;
        if (tmpP.getRuns() != null && tmpP.getRuns().size() > 0) {
            tmpR = tmpP.getRuns().get(0);
        }
        XWPFRun cellR = cellP.createRun();
        cellR.setText(text);
        // 复制字体信息
        if (tmpR != null) {
            if (!cellR.isBold()) {
                cellR.setBold(tmpR.isBold());
            }
            cellR.setItalic(tmpR.isItalic());
            cellR.setUnderline(tmpR.getUnderline());
            cellR.setColor(tmpR.getColor());
            cellR.setTextPosition(tmpR.getTextPosition());
            if (tmpR.getFontSizeAsDouble() != -1) {
                cellR.setFontSize(tmpR.getFontSizeAsDouble());
            }
            if (tmpR.getFontFamily() != null) {
                cellR.setFontFamily(tmpR.getFontFamily());
            }
            if (tmpR.getCTR() != null) {
                if (tmpR.getCTR().isSetRPr()) {
                    CTRPr tmpRPr = tmpR.getCTR().getRPr();
                    if (tmpRPr.getRFontsList().size() > 0) {
                        CTFonts tmpFonts = tmpRPr.getRFontsList().get(0);
                        CTRPr cellRPr = cellR.getCTR().isSetRPr() ? cellR
                                .getCTR().getRPr() : cellR.getCTR().addNewRPr();
                        CTFonts cellFonts = cellRPr.getRFontsList().size() > 0 ? cellRPr
                                .getRFontsList().get(0) : cellRPr.addNewRFonts();
                        cellFonts.setAscii(tmpFonts.getAscii());
                        cellFonts.setAsciiTheme(tmpFonts.getAsciiTheme());
                        cellFonts.setCs(tmpFonts.getCs());
                        cellFonts.setCstheme(tmpFonts.getCstheme());
                        cellFonts.setEastAsia(tmpFonts.getEastAsia());
                        cellFonts.setEastAsiaTheme(tmpFonts.getEastAsiaTheme());
                        cellFonts.setHAnsi(tmpFonts.getHAnsi());
                        cellFonts.setHAnsiTheme(tmpFonts.getHAnsiTheme());
                        cellFonts.setHint(tmpFonts.getHint());
                    }
                }
            }

        }
        // 复制段落信息
        cellP.setAlignment(tmpP.getAlignment());
        cellP.setVerticalAlignment(tmpP.getVerticalAlignment());
        cellP.setBorderBetween(tmpP.getBorderBetween());
        cellP.setBorderBottom(tmpP.getBorderBottom());
        cellP.setBorderLeft(tmpP.getBorderLeft());
        cellP.setBorderRight(tmpP.getBorderRight());
        cellP.setBorderTop(tmpP.getBorderTop());
        cellP.setPageBreak(tmpP.isPageBreak());
        if (tmpP.getCTP() != null) {
            if (tmpP.getCTP().getPPr() != null) {
                CTPPr tmpPPr = tmpP.getCTP().getPPr();
                CTPPr cellPPr = cellP.getCTP().getPPr() != null ? cellP
                        .getCTP().getPPr() : cellP.getCTP().addNewPPr();
                // 复制段落间距信息
                CTSpacing tmpSpacing = tmpPPr.getSpacing();
                if (tmpSpacing != null) {
                    CTSpacing cellSpacing = cellPPr.getSpacing() != null ? cellPPr
                            .getSpacing() : cellPPr.addNewSpacing();
                    if (tmpSpacing.getAfter() != null) {
                        cellSpacing.setAfter(tmpSpacing.getAfter());
                    }
                    if (tmpSpacing.getAfterAutospacing() != null) {
                        cellSpacing.setAfterAutospacing(tmpSpacing
                                .getAfterAutospacing());
                    }
                    if (tmpSpacing.getAfterLines() != null) {
                        cellSpacing.setAfterLines(tmpSpacing.getAfterLines());
                    }
                    if (tmpSpacing.getBefore() != null) {
                        cellSpacing.setBefore(tmpSpacing.getBefore());
                    }
                    if (tmpSpacing.getBeforeAutospacing() != null) {
                        cellSpacing.setBeforeAutospacing(tmpSpacing
                                .getBeforeAutospacing());
                    }
                    if (tmpSpacing.getBeforeLines() != null) {
                        cellSpacing.setBeforeLines(tmpSpacing.getBeforeLines());
                    }
                    if (!Objects.equals(tmpSpacing.getLine(), new BigInteger("0"))) {
                        cellSpacing.setLine(tmpSpacing.getLine());
                    }
                    if (tmpSpacing.getLineRule() != null) {
                        cellSpacing.setLineRule(tmpSpacing.getLineRule());
                    }
                }
                // 复制段落缩进信息
                CTInd tmpInd = tmpPPr.getInd();
                if (tmpInd != null) {
                    CTInd cellInd = cellPPr.getInd() != null ? cellPPr.getInd()
                            : cellPPr.addNewInd();
                    if (tmpInd.getFirstLine() != null) {
                        cellInd.setFirstLine(tmpInd.getFirstLine());
                    }
                    if (tmpInd.getFirstLineChars() != null) {
                        cellInd.setFirstLineChars(tmpInd.getFirstLineChars());
                    }
                    if (tmpInd.getHanging() != null) {
                        cellInd.setHanging(tmpInd.getHanging());
                    }
                    if (tmpInd.getHangingChars() != null) {
                        cellInd.setHangingChars(tmpInd.getHangingChars());
                    }
                    if (tmpInd.getLeft() != null) {
                        cellInd.setLeft(tmpInd.getLeft());
                    }
                    if (tmpInd.getLeftChars() != null) {
                        cellInd.setLeftChars(tmpInd.getLeftChars());
                    }
                    if (tmpInd.getRight() != null) {
                        cellInd.setRight(tmpInd.getRight());
                    }
                    if (tmpInd.getRightChars() != null) {
                        cellInd.setRightChars(tmpInd.getRightChars());
                    }
                }
            }
        }
    }

    /**
     * 收尾方法
     *
     * @param outDocPath
     * @return
     * @throws IOException
     */
    public XWPFDocument generate(String outDocPath) throws IOException {
        outputStream = new FileOutputStream(outDocPath);
        xwpfDocument.write(outputStream);
        this.close(outputStream);
        this.close(inputStream);
        return xwpfDocument;
    }

    /**
     * 关闭输入流
     *
     * @param is
     */
    private void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                LogManager.writeLogFile(e, log);
            }
        }
    }

    /**
     * 关闭输出流
     *
     * @param os
     */
    private void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                LogManager.writeLogFile(e, log);
            }
        }
    }
}
