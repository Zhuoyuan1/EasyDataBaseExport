package com.easydatabaseexport.util;

import com.easydatabaseexport.common.PatternConstant;
import com.easydatabaseexport.log.LogManager;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ExportExcelUtil
 *
 * @author lzy
 * @date 2021/2/28 17:44
 **/
@Log4j
public class ExportExcelUtil<T> {

    private static final String regex = "(\\*|/|:|\\\\|\\?)";

    /**
     * 生成表头的样式
     *
     * @param workbook 工作簿
     * @return org.apache.poi.ss.usermodel.CellStyle
     **/
    private CellStyle getTableHeaderStyle(SXSSFWorkbook workbook) {
        // 生成一个样式
        CellStyle style = workbook.createCellStyle();
        // 设置这些样式
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        // 生成一个字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName("宋体");
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontHeightInPoints((short) 11);
        // 把字体应用到当前的样式
        style.setFont(font);
        return style;
    }

    /**
     * 生成表body的样式
     *
     * @param workbook 工作簿
     * @return org.apache.poi.ss.usermodel.CellStyle
     **/
    private CellStyle getTableBodyStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        // 生成另一个字体
        Font font = workbook.createFont();
        font.setFontHeightInPoints(XSSFFont.DEFAULT_FONT_SIZE);
        // 把字体应用到当前的样式
        style.setFont(font);

        // 设置这些样式
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 生成表中心样式
     *
     * @param workbook 工作簿
     * @return org.apache.poi.ss.usermodel.CellStyle
     **/
    private CellStyle getTableCenterStyle(SXSSFWorkbook workbook) {
        // 生成一个样式
        CellStyle style = workbook.createCellStyle();
        // 设置这些样式
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        //设置填充方案
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //设置自定义填充颜色
        style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        // 生成一个字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        // 把字体应用到当前的样式
        style.setFont(font);
        return style;
    }

    /**
     * 小数点数字
     */
    private static final Pattern pattern = Pattern.compile("^//d+(//.//d+)?$");
    /**
     * 简单时间格式化
     */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 单元格写入值
     *
     * @param cell  单元格
     * @param value 反射获得的值
     **/
    @SneakyThrows
    private void setCellValue(Cell cell, Object value) {
        if (null == cell) {
            throw new Exception("Cell is null, can not set value!");
        }
        String textValue = null;
        XSSFRichTextString richString;
        Matcher matcher;
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Float) {
            textValue = String.valueOf(value);
            cell.setCellValue(textValue);
        } else if (value instanceof Double) {
            textValue = String.valueOf(value);
            cell.setCellValue(textValue);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        }
        if (value instanceof Boolean) {
            textValue = "是";
            if (!(Boolean) value) {
                textValue = "否";
            }
        } else if (value instanceof Date) {
            textValue = sdf.format((Date) value);
        } else {
            // 其它数据类型都当作字符串简单处理
            if (value != null) {
                textValue = value.toString();
            }
        }
        if (textValue != null) {
            matcher = pattern.matcher(textValue);
            if (matcher.matches()) {
                // 是数字当作double处理
                cell.setCellValue(Double.parseDouble(textValue));
            } else {
                richString = new XSSFRichTextString(textValue);
                cell.setCellValue(richString);
            }
        }
    }

    /**
     * 写入word，并关闭流
     *
     * @param workbook 工作簿
     * @param out      输出流
     **/
    private void outAndCloseWorkbook(SXSSFWorkbook workbook, OutputStream out) {
        try {
            try {
                workbook.write(out);
            } catch (IOException e) {
                LogManager.writeLogFile(e, log);
            }
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                LogManager.writeLogFile(e, log);
            }
            workbook.dispose();
        }
    }

    /**
     * 创建标题
     *
     * @param sheet   sheet
     * @param headers 表头
     * @param style   样式
     **/
    private void createTableRow(Sheet sheet, String[] headers, CellStyle style) {
        Row row = sheet.createRow(0);
        for (int j = 0; j < headers.length; j++) {
            Cell cellHeader = row.createCell(j);
            cellHeader.setCellStyle(style);
            cellHeader.setCellValue(new XSSFRichTextString(headers[j]));
        }
    }

    /**
     * <p>
     * 通用Excel导出方法,利用反射机制遍历对象的所有字段，将数据写入Excel文件中 <br>
     * 此版本生成2007以上版本的文件 (文件后缀：xlsx)
     * </p>
     *
     * @param title   表格sheet名
     * @param headers 表格头部标题集合
     * @param dataset 需要显示的数据集合,集合中一定要放置符合JavaBean风格的类的对象。此方法支持的
     *                JavaBean属性的数据类型有基本数据类型及String,Date
     * @param out     与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void exportExcel(String title, String[] headers, Map<String, List<T>> dataset, OutputStream out, boolean isMoreSheet) {
        try {
            // 声明一个工作薄
            SXSSFWorkbook workbook = new SXSSFWorkbook();
            // 生成一个表格
            Sheet sheet = null;
            // 生成表中心样式
            CellStyle style3 = null;
            if (!isMoreSheet) {
                sheet = workbook.createSheet(title);
                // 设置表格默认列宽度为15个字节
                sheet.setDefaultColumnWidth(20);
                style3 = getTableCenterStyle(workbook);
            }
            // 生成一个样式
            CellStyle style = getTableHeaderStyle(workbook);
            // 生成并设置另一个样式
            CellStyle style2 = getTableBodyStyle(workbook);
            // 产生表格标题行
            Row row = null;
            if (!isMoreSheet) {
                //创建标题
                createTableRow(sheet, headers, style);
            }
            int index = 0;
            int sheetIndex = 0;

            for (Map.Entry<String, List<T>> map : dataset.entrySet()) {
                // 遍历集合数据，产生数据行
                Iterator<T> it = map.getValue().iterator();
                T t;
                Field[] fields;
                Field field;
                String fieldName;
                String getMethodName;
                Cell cell;
                Class tCls;
                Method getMethod;
                Object value;
                //先创建表名
                if (isMoreSheet) {
                    //先创建sheet名
                    String tableSheetName = map.getKey().replaceAll("\\[", "(")
                            .replaceAll("]", ")").replaceAll(regex, "-");
                    sheetIndex++;
                    if (tableSheetName.length() > 31) {
                        tableSheetName = tableSheetName.substring(0, 31);
                    }
                    if (null != workbook.getSheet(tableSheetName)) {
                        tableSheetName += "(" + sheetIndex + ")";
                        if (tableSheetName.length() > 31) {
                            tableSheetName = "表名+表注释太长-" + UUID.randomUUID();
                        }
                    }
                    sheet = workbook.createSheet(tableSheetName);
                    sheet.setDefaultColumnWidth(20);
                    // 产生表格标题行
                    createTableRow(sheet, headers, style);
                    //重置索引
                    index = 0;
                } else {
                    index++;
                    row = sheet.createRow(index);
                    value = map.getKey();
                    for (int i = 0; i < headers.length; i++) {
                        row.createCell(i);
                    }
                    cell = row.getCell(0);
                    cell.setCellStyle(style3);
                    setCellValue(cell, value);
                    row.setHeight((short) 500);
                    CellRangeAddress region = new CellRangeAddress(index, index, 0, headers.length - 1);
                    sheet.addMergedRegion(region);
                }
                while (it.hasNext()) {
                    index++;
                    row = sheet.createRow(index);
                    t = it.next();
                    // 利用反射，根据JavaBean属性的先后顺序，动态调用getXxx()方法得到属性值
                    fields = PatternConstant.tableFields;
                    for (int i = 0; i < fields.length; i++) {
                        cell = row.createCell(i);
                        cell.setCellStyle(style2);
                        field = fields[i];
                        fieldName = field.getName();
                        getMethodName = "get" + fieldName.substring(0, 1).toUpperCase()
                                + fieldName.substring(1);
                        tCls = t.getClass();
                        getMethod = tCls.getMethod(getMethodName);
                        value = getMethod.invoke(t);
                        // 判断值的类型后进行强制类型转换
                        setCellValue(cell, value);
                    }
                }
            }
            outAndCloseWorkbook(workbook, out);
        } catch (Exception e) {
            LogManager.writeLogFile(e, log);
        }
    }
}
