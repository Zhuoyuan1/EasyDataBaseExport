package com.easydatabaseexport.util;

import com.easydatabaseexport.log.LogManager;
import lombok.extern.java.Log;
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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ExportExcelUtil
 *
 * @author lzy
 * @date 2021/2/28 17:44
 **/
@Log
public class ExportExcelUtil<T> {

    /**
     * <p>
     * 导出无头部标题行Excel <br>
     * 时间格式默认：yyyy-MM-dd hh:mm:ss <br>
     * </p>
     *
     * @param title   表格标题
     * @param dataset 数据集合
     * @param out     输出流
     */
    public void exportExcel(String title, Collection<T> dataset, OutputStream out) {
        exportExcel(title, null, dataset, out, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * <p>
     * 导出带有头部标题行的Excel <br>
     * 时间格式默认：yyyy-MM-dd hh:mm:ss <br>
     * </p>
     *
     * @param title   表格标题
     * @param headers 头部标题集合
     * @param dataset 数据集合
     * @param out     输出流
     */
    public void exportExcel(String title, String[] headers, Collection<T> dataset, OutputStream out, boolean isMoreSheet) {
        if (!isMoreSheet) {
            exportExcel(title, headers, dataset, out, "yyyy-MM-dd HH:mm:ss");
        } else {
            exportExcelMoreSheet(headers, dataset, out, "yyyy-MM-dd HH:mm:ss");
        }
    }

    private static final String regex = "(\\*|/|:|\\\\|\\?)";

    /**
     * <p>
     * 多sheet导出
     * </p>
     *
     * @param headers 表格头部标题集合
     * @param dataset 需要显示的数据集合,集合中一定要放置符合JavaBean风格的类的对象。此方法支持的
     *                JavaBean属性的数据类型有基本数据类型及String,Date
     * @param out     与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param pattern 如果有时间数据，设定输出格式。默认为"yyyy-MM-dd hh:mm:ss"
     */
    private void exportExcelMoreSheet(String[] headers, Collection<T> dataset, OutputStream out, String pattern) {
        // 声明一个工作薄
        SXSSFWorkbook workbook = new SXSSFWorkbook();
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
        // 生成并设置另一个样式
        CellStyle style2 = workbook.createCellStyle();
        style2.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style2.setBorderBottom(BorderStyle.THIN);
        style2.setBorderLeft(BorderStyle.THIN);
        style2.setBorderRight(BorderStyle.THIN);
        style2.setBorderTop(BorderStyle.THIN);
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setVerticalAlignment(VerticalAlignment.CENTER);
        // 生成另一个字体
        Font font2 = workbook.createFont();
        font2.setFontHeightInPoints(XSSFFont.DEFAULT_FONT_SIZE);
        // 把字体应用到当前的样式
        style2.setFont(font2);

        // 设置这些样式
        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 生成一个表格
        Sheet sheet = null;
        //第一行
        Row row = null;

        // 遍历集合数据，产生数据行
        Iterator<T> it = dataset.iterator();
        int index = 0;
        int sheetIndex = 0;
        T t;
        Field[] fields;
        Field field;
        XSSFRichTextString richString;
        Pattern p = Pattern.compile("^//d+(//.//d+)?$");
        Matcher matcher;
        String fieldName;
        String getMethodName;
        Cell cell = null;
        Class tCls;
        Method getMethod;
        Object value;
        String textValue;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        while (it.hasNext()) {
            if (index != 0) {
                row = sheet.createRow(index);
            }
            index++;
            t = it.next();
            // 利用反射，根据JavaBean属性的先后顺序，动态调用getXxx()方法得到属性值
            fields = t.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if (index != 0 && null != row) {
                    cell = row.createCell(i);
                    cell.setCellStyle(style2);
                }
                field = fields[i];
                fieldName = field.getName();
                getMethodName = "get" + fieldName.substring(0, 1).toUpperCase()
                        + fieldName.substring(1);
                try {
                    tCls = t.getClass();
                    getMethod = tCls.getMethod(getMethodName);
                    value = getMethod.invoke(t);
                    if ("isTableNameBlank".equals(value)) {
                        Method method = tCls.getMethod("getNo");
                        Object tableNameValue = method.invoke(t);
                        String tableSheetName = tableNameValue.toString().replaceAll("\\[", "(")
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
                        //删除掉最后一行
                        if (sheetIndex > 1) {
                            Sheet lastSheet = workbook.getSheetAt(sheetIndex - 2);
                            lastSheet.removeRow(lastSheet.getRow(lastSheet.getLastRowNum()));
                        }
                        sheet.setDefaultColumnWidth(20);
                        // 产生表格标题行
                        row = sheet.createRow(0);
                        Cell cellHeader;
                        for (int j = 0; j < headers.length; j++) {
                            cellHeader = row.createCell(j);
                            cellHeader.setCellStyle(style);
                            cellHeader.setCellValue(new XSSFRichTextString(headers[j]));
                        }
                        index = 1;
                        continue;
                    }
                    // 判断值的类型后进行强制类型转换
                    textValue = null;
                    if (index != 0 && null != cell) {
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
                            matcher = p.matcher(textValue);
                            if (matcher.matches()) {
                                // 是数字当作double处理
                                cell.setCellValue(Double.parseDouble(textValue));
                            } else {
                                richString = new XSSFRichTextString(textValue);
                                cell.setCellValue(richString);
                            }
                        }
                    }
                } catch (Exception e) {
                    LogManager.writeLogFile(e, log);
                }
            }
        }
        try {
            try {
                workbook.write(out);
            } catch (IOException e) {
                LogManager.writeLogFile(e, log);
            }
        } finally {
            workbook.dispose();
        }
    }

    /**
     * <p>
     * 通用Excel导出方法,利用反射机制遍历对象的所有字段，将数据写入Excel文件中 <br>
     * 此版本生成2007以上版本的文件 (文件后缀：xlsx)
     * </p>
     *
     * @param title   表格标题名
     * @param headers 表格头部标题集合
     * @param dataset 需要显示的数据集合,集合中一定要放置符合JavaBean风格的类的对象。此方法支持的
     *                JavaBean属性的数据类型有基本数据类型及String,Date
     * @param out     与输出设备关联的流对象，可以将EXCEL文档导出到本地文件或者网络中
     * @param pattern 如果有时间数据，设定输出格式。默认为"yyyy-MM-dd hh:mm:ss"
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void exportExcel(String title, String[] headers, Collection<T> dataset, OutputStream out, String pattern) {
        // 声明一个工作薄
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        // 生成一个表格
        Sheet sheet = workbook.createSheet(title);
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth(20);
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
        // 生成并设置另一个样式
        CellStyle style2 = workbook.createCellStyle();
        style2.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style2.setBorderBottom(BorderStyle.THIN);
        style2.setBorderLeft(BorderStyle.THIN);
        style2.setBorderRight(BorderStyle.THIN);
        style2.setBorderTop(BorderStyle.THIN);
        style2.setAlignment(HorizontalAlignment.CENTER);
        style2.setVerticalAlignment(VerticalAlignment.CENTER);
        // 生成另一个字体
        Font font2 = workbook.createFont();
        font2.setFontHeightInPoints(XSSFFont.DEFAULT_FONT_SIZE);
        // 把字体应用到当前的样式
        style2.setFont(font2);

        // 生成一个样式
        CellStyle style3 = workbook.createCellStyle();
        // 设置这些样式
        style3.setBorderBottom(BorderStyle.THIN);
        style3.setBorderLeft(BorderStyle.THIN);
        style3.setBorderRight(BorderStyle.THIN);
        style3.setBorderTop(BorderStyle.THIN);
        style3.setAlignment(HorizontalAlignment.CENTER);
        style3.setVerticalAlignment(VerticalAlignment.CENTER);
        //设置填充方案
        style3.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //设置自定义填充颜色
        style3.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        // 生成一个字体
        Font font1 = workbook.createFont();
        font1.setBold(true);
        font1.setFontHeightInPoints((short) 14);
        // 把字体应用到当前的样式
        style3.setFont(font1);

        // 产生表格标题行
        Row row = sheet.createRow(0);
        Cell cellHeader;
        for (int i = 0; i < headers.length; i++) {
            cellHeader = row.createCell(i);
            cellHeader.setCellStyle(style);
            cellHeader.setCellValue(new XSSFRichTextString(headers[i]));
        }

        // 遍历集合数据，产生数据行
        Iterator<T> it = dataset.iterator();
        int index = 0;
        T t;
        Field[] fields;
        Field field;
        XSSFRichTextString richString;
        Pattern p = Pattern.compile("^//d+(//.//d+)?$");
        Matcher matcher;
        String fieldName;
        String getMethodName;
        Cell cell;
        Class tCls;
        Method getMethod;
        Object value;
        String textValue;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        while (it.hasNext()) {
            index++;
            row = sheet.createRow(index);
            t = it.next();
            // 利用反射，根据JavaBean属性的先后顺序，动态调用getXxx()方法得到属性值
            fields = t.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                cell = row.createCell(i);
                cell.setCellStyle(style2);
                field = fields[i];
                fieldName = field.getName();
                getMethodName = "get" + fieldName.substring(0, 1).toUpperCase()
                        + fieldName.substring(1);
                try {
                    tCls = t.getClass();
                    getMethod = tCls.getMethod(getMethodName);
                    value = getMethod.invoke(t);
                    if ("isTableNameBlank".equals(value)) {
                        row.setHeight((short) 500);
                        cell = row.getCell(0);
                        cell.setCellStyle(style3);
                        CellRangeAddress region = new CellRangeAddress(index, index, 0, i);
                        sheet.addMergedRegion(region);
                        continue;
                    }
                    // 判断值的类型后进行强制类型转换
                    textValue = null;
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
                        matcher = p.matcher(textValue);
                        if (matcher.matches()) {
                            // 是数字当作double处理
                            cell.setCellValue(Double.parseDouble(textValue));
                        } else {
                            richString = new XSSFRichTextString(textValue);
                            cell.setCellValue(richString);
                        }
                    }
                } catch (Exception e) {
                    LogManager.writeLogFile(e, log);
                }
            }
        }
        try {
            try {
                workbook.write(out);
            } catch (IOException e) {
                LogManager.writeLogFile(e, log);
            }
        } finally {
            workbook.dispose();
        }
    }
}
