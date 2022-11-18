package com.easydatabaseexport.database.service;

import com.alibaba.fastjson.JSON;
import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.core.DataResult;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.util.DataUtils;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AbstractDataResultImpl
 *
 * @author lzy
 * @date 2022/3/15 16:17
 **/
@Log
public abstract class AbstractDataResultImpl implements DataResult {

    @SneakyThrows
    public ResultSet getResultSetBySql(String sql, String... params) {
        PreparedStatement ppst = null;
        ppst = CommonConstant.connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = null;
        for (int i = 1; i <= params.length; i++) {
            ppst.setString(i, params[i - 1]);
        }
        rs = ppst.executeQuery();
        return rs;
    }

    public <T> List<T> toList(T t, String sql) throws SQLException {
        PreparedStatement ppst = null;
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        ppst = CommonConstant.connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ppst.executeQuery();
        while (rs.next()) {
            Map<String, String> resultMap = new HashMap<String, String>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i < columnCount + 1; i++) {
                resultMap.put(metaData.getColumnLabel(i), rs.getString(i));
            }
            resultList.add(resultMap);
        }
        return (List<T>) JSON.parseArray(JSON.toJSONString(resultList), t.getClass());
    }

    @Override
    public void doListValueChanged() {
        CommonConstant.RIGHT.removeAll();
        JTabbedPane jPanel = getRightInfo(CommonConstant.RIGHT.getWidth(), CommonConstant.RIGHT.getHeight());
        CommonConstant.RIGHT.add(jPanel);
        //CommonConstant.RIGHT.setLayout(new FlowLayout(FlowLayout.LEFT));
        CommonConstant.RIGHT.validate();
        CommonConstant.RIGHT.repaint();

        initList();
        CommonConstant.CENTERS.removeAll();
        JScrollPane jTable = getCenterInfo(CommonConstant.tableParameterList);
        jTable.setSize(CommonConstant.CENTERS.getWidth(), CommonConstant.CENTERS.getHeight());
        CommonConstant.CENTERS.add(jTable);
        CommonConstant.CENTERS.validate();
        CommonConstant.CENTERS.repaint();
    }


    public JScrollPane getCenterInfo(List<TableParameter> tableParameterList) {
        Object[][] obj = DataUtils.toArray(tableParameterList);
        JTable table = new JTable(obj, CommonConstant.COLUMN_HEAD_NAMES);
        // 创建表格标题对象
        JTableHeader head = table.getTableHeader();
        // 设置表头大小
        head.setPreferredSize(new Dimension(head.getWidth(), 20));
        // 设置表格字体
        head.setFont(new Font("", Font.PLAIN, 15));

        TableColumn column = null;
        int colunms = table.getColumnCount();
        for (int i = 0; i < colunms; i++) {
            column = table.getColumnModel().getColumn(i);
            /*将每一列的默认宽度设置为200*/
            if (i == 1) {
                column.setPreferredWidth(300);
            } else if (i == 2) {
                column.setPreferredWidth(200);
            } else {
                column.setPreferredWidth(100);
            }
        }

        /*用JScrollPane装载JTable，这样超出范围的列就可以通过滚动条来查看*/
        return new JScrollPane(table);
    }
}
