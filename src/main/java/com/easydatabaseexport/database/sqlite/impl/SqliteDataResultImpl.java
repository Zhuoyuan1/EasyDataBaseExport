package com.easydatabaseexport.database.sqlite.impl;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.CommonDataBaseType;
import com.easydatabaseexport.core.DataResult;
import com.easydatabaseexport.database.service.AbstractDataResultImpl;
import com.easydatabaseexport.entities.ErrorMsg;
import com.easydatabaseexport.entities.IndexInfo;
import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.entities.TableType;
import com.easydatabaseexport.entities.TableTypeForSqlite;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.util.HtmlUtils;
import com.easydatabaseexport.util.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.Dimension;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * SqliteDataResultImpl
 *
 * @author lzy
 * @date 2023/3/2 10:37
 **/
@Log4j
public class SqliteDataResultImpl extends AbstractDataResultImpl implements DataResult {

    @Override
    public JScrollPane getDataCenterInfo(int width, int height) throws SQLException {
        String sql = "select * from '" + CommonConstant.DATABASE_NAME + "'.'" + CommonConstant.TABLE_NAME + "'";
        PreparedStatement ppst = CommonConstant.connection.prepareStatement(sql);
        ResultSet rs = ppst.executeQuery();
        DefaultTableModel tableModel = new DefaultTableModel();
        JTable table = new JTable(tableModel);
        ResultSetMetaData metaData = rs.getMetaData();
        // Names of columns
        Vector<String> columnNames = new Vector<String>();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }

        // Data of the table
        Vector<Vector<Object>> data = new Vector<Vector<Object>>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<Object>();
            for (int i = 1; i <= columnCount; i++) {
                vector.add(rs.getObject(i));
            }
            data.add(vector);
        }
        tableModel.setDataVector(data, columnNames);
        //设置宽度
        TableColumn column = null;
        int colunms = table.getColumnCount();
        for (int i = 0; i < colunms; i++) {
            column = table.getColumnModel().getColumn(i);
            /*将每一列的默认宽度设置为250*/
            column.setPreferredWidth(250);
        }
        // 水平滚动条
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setSize(width, height);
        return scroll;
    }

    private static final String sql = "SELECT name as COLUMN_NAME, " +
            "    type as COLUMN_TYPE, " +
            "    SUBSTR(type,instr(type,'(')+1 ,instr(type,')')-(instr(type,'(')+1) ) as length, " +
            "   case when `notnull` = 1 then 'NO' when `notnull` = 0 then 'YES' end as IS_NULLABLE , " +
            "    dflt_value as COLUMN_DEFAULT , " +
            "    '' as COLUMN_COMMENT " +
            "FROM " +
            "    pragma_table_info('?')";

    @SneakyThrows
    @Override
    public void initList() {
        CommonConstant.tableParameterList.clear();
        String newSql = String.format(sql.replace("?", "%s"), CommonConstant.TABLE_NAME);
        CommonConstant.tableParameterList.addAll(toListNoMode(new TableParameter(), newSql));
    }

    @Override
    public List<TableParameter> getTableStructureByKey(String databaseName, String tableName) throws Exception {
        String newSql = String.format(sql.replace("?", "%s"), tableName);
        return toListNoMode(new TableParameter(), newSql);
    }


    @Override
    public JTabbedPane getRightInfo(int width, int height) {
        //获取表名对应的信息
        JTabbedPane jPanel = new JTabbedPane(JTabbedPane.TOP);
        jPanel.setPreferredSize(new Dimension((int) (width * 9.6) / 10, (int) (height * 9.6) / 10));
        JTextArea ddl = new JTextArea(getDdlInfo());
        ddl.setEditable(false);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(ddl);
        scrollPane.setPreferredSize(new Dimension((int) (width * 9.6) / 10, (int) (height * 9.6) / 10));
        try {
            jPanel.addTab("常规", null, new JScrollPane(new JLabel((getTableInfoAndStructureByClick(
                    CommonConstant.TABLE_NAME, CommonConstant.DATABASE_NAME)))), "常规");
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
        jPanel.addTab("DDL", null, scrollPane, "DDL");
        return jPanel;
    }

    private String getDdlInfo() {
        StringBuilder str = new StringBuilder();
        String sql = "select sql from sqlite_master where name = '" + CommonConstant.TABLE_NAME + "'";
        ResultSet rs = getResultSetBySqlNoMode(sql);
        //获取返回的ResultSet内容
        try {
            while (rs.next()) {
                str.append(StringUtil.StringEqual(rs.getString(1)));
            }
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
        return str.toString();
    }

    /**
     * 执行数据查询器
     *
     * @author lzy
     * @date 2022/3/30 18:18
     **/
    @Override
    public void doListDataValueChanged() throws SQLException {

        CommonConstant.CENTERS.removeAll();
        JScrollPane scroll = getDataCenterInfo(CommonConstant.CENTERS.getWidth(), CommonConstant.CENTERS.getHeight());
        CommonConstant.CENTERS.add(scroll);
        CommonConstant.CENTERS.validate();
        CommonConstant.CENTERS.repaint();
    }

    @Override
    public Map<String, ErrorMsg> checkExist(String tableName, String dataBase) throws Exception {
        String sql = "select * from '" + dataBase + "'.'" + tableName + "'";
        ResultSet rs = null;
        Map<String, ErrorMsg> map = new HashMap<>(16);
        ErrorMsg msg = new ErrorMsg();
        try {
            PreparedStatement ppst = CommonConstant.connection.prepareStatement(sql);
            rs = ppst.executeQuery();
            rs.getRow();
        } catch (SQLException e) {
            msg.setMessage("操作失败，原因：" + e.getMessage()).setTitle("错误").setMessageType(JOptionPane.ERROR_MESSAGE);
            map.put(CommonConstant.FAIL, msg);
            LogManager.writeLogFile(e, log);
            return map;
        }
        map.put(CommonConstant.SUCCESS, msg);
        return map;
    }

    @Override
    public List<String> getAllDataBaseName() {
        List<String> nameList = new LinkedList<String>();
        try {
            //JdbcRowSet jrs = new JdbcRowSetImpl(CommonConstant.connection);
            Statement stmt = CommonConstant.connection.createStatement();
            //执行查询所有数据库操作
            ResultSet rs = stmt.executeQuery("PRAGMA database_list");

            //处理表名
            while (rs.next()) {
                nameList.add(rs.getString(2));
            }
            //处理拼接in中的参数
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < nameList.size(); i++) {
                if (i == nameList.size() - 1) {
                    stringBuilder.append("'").append(nameList.get(i)).append("'");
                } else {
                    stringBuilder.append("'").append(nameList.get(i)).append("'").append(",");
                }
            }
            getTableInfoAndStructure(stringBuilder.toString());
        } catch (Exception e) {
            LogManager.writeLogFile(e, log);
        }
        return nameList;
    }

    @Override
    public List<IndexInfoVO> getIndexByKey(String dataBase, String tableName) {
        String sql = "SELECT * FROM sqlite_master WHERE tbl_name = '" + tableName + "' and type = 'index' ";

        List<IndexInfo> indexList = new ArrayList<>();
        List<IndexInfoVO> voList = new ArrayList<>();
        ResultSet rs = getResultSetBySqlNoMode(sql);

        //获取返回的ResultSet内容
        try {
            IndexInfo indexInfo = null;
            while (rs.next()) {
                indexInfo = new IndexInfo();
                indexInfo.setKeyName(StringUtil.StringEqual(rs.getString(2)));
                indexInfo.setTable(StringUtil.StringEqual(rs.getString(3)));
                indexInfo.setColumnName(StringUtil.StringEqual(rs.getString(5)));
                indexList.add(indexInfo);
            }
            //根据keyName分组
            Map<String, List<IndexInfo>> indexMap = indexList.stream().collect(Collectors.groupingBy(IndexInfo::getKeyName));
            for (Map.Entry<String, List<IndexInfo>> map : indexMap.entrySet()) {
                List<IndexInfo> list = map.getValue();
                if (map.getKey().contains("autoindex")) {
                    list.forEach(e -> {
                        IndexInfoVO vo = new IndexInfoVO();
                        vo.setName(map.getKey());
                        vo.setIndexType("主键");
                        vo.setColumnName(getPkey(vo.getName()));
                        vo.setIndexMethod(e.getIndexType());
                        vo.setComment(e.getIndexComment());
                        voList.add(vo);
                    });
                } else {
                    IndexInfoVO vo = new IndexInfoVO();
                    vo.setName(map.getKey());
                    IndexInfo info = list.get(0);
                    vo.setColumnName(info.getColumnName());
                    vo.setComment(list.get(0).getIndexComment());
                    voList.add(vo);
                }
            }
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
        return voList;
    }

    private String getPkey(String name) {
        String sql = "select * from PRAGMA_index_info('" + name + "')";
        ResultSet rs = getResultSetBySqlNoMode(sql);
        //获取返回的ResultSet内容
        String column = "";
        try {
            if (rs.next()) {
                column = (StringUtil.StringEqual(rs.getString(3)));
            }
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
        return column;
    }

    private void getTableInfoAndStructure(String name) throws SQLException {
        String sql = "SELECT name AS TABLE_NAME, (CASE type   WHEN 'table' THEN 'Table'    " +
                "WHEN 'view' THEN 'View' END) AS ObjectType, sql AS CreateSQL, " + name + " as tableSchema FROM " +
                "sqlite_master WHERE type = 'table' ORDER BY type";
        List<TableType> list = toListNoMode(new TableType(), sql);
        list.forEach(v -> {
            v.setTableName(StringUtil.StringEqual(v.getTableName()) + "[" + StringUtil.StringEqual(v.getTableComment()) + "]");
        });
        CommonDataBaseType.CON_DATABASE_TABLE_MAP = list.parallelStream().collect(Collectors.groupingBy(TableType::getTableSchema, HashMap::new,
                Collectors.collectingAndThen(Collectors.toList(),
                        v -> v.stream().sorted(Comparator.comparing(TableType::getTableName)).collect(Collectors.toList())
                )));
    }

    /**
     * 点击查询
     *
     * @param tableName 表名
     * @param dataBase  库名
     * @return java.lang.String
     * @author lzy
     * @date 2022/3/30 18:18
     **/
    @Override
    public String getTableInfoAndStructureByClick(String tableName, String dataBase) throws SQLException {
        //获取返回的ResultSet内容
        StringBuilder stringBuilder = new StringBuilder();
        String sql = "SELECT m.name AS tableName, m.rootpage AS rootpage, " +
                "m.sql AS sql, (case when i.hasindexes > 0 then '是' else '否' end ) AS hasindexes, (case when t.hastriggers > 0 then '是' else '否' end ) " +
                "AS hastriggers FROM " + dataBase + ".sqlite_master m LEFT OUTER JOIN (SELECT tbl_name, (count(*) > 0) " +
                "hasindexes FROM " + dataBase + ".sqlite_master WHERE type = 'index' GROUP BY tbl_name) i ON m.name = " +
                "i.tbl_name LEFT OUTER JOIN (SELECT tbl_name, (count(*) > 0) hastriggers FROM " + dataBase + ".sqlite_master " +
                "WHERE type = 'trigger' GROUP BY tbl_name) " +
                "t ON m.name = t.tbl_name WHERE m.type = 'table' AND m.name = '" + tableName + "' ORDER BY m.name ASC";
        List<TableTypeForSqlite> list = toListNoMode(new TableTypeForSqlite(), sql);
        if (!list.isEmpty()) {
            TableTypeForSqlite tableType = list.get(0);
            stringBuilder.append("<html>").append(HtmlUtils.getHtml("表名")).append(StringUtil.StringEqual(tableType.getTableName()))
                    .append(HtmlUtils.getHtml("组")).append(StringUtil.StringEqual(tableType.getGroup()))
                    .append(HtmlUtils.getHtml("有索引")).append(StringUtil.StringEqual(tableType.getHasIndexes()))
                    .append(HtmlUtils.getHtml("有触发器")).append(StringUtil.StringEqual(tableType.getHasTriggers()))
                    .append(HtmlUtils.getHtml("根页面")).append(StringUtil.StringEqual(tableType.getRootPage()))
                    .append("</html>");
        }
        return stringBuilder.toString();
    }

}
