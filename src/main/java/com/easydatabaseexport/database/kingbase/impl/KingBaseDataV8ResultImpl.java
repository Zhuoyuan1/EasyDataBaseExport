package com.easydatabaseexport.database.kingbase.impl;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.CommonDataBaseType;
import com.easydatabaseexport.core.DataResult;
import com.easydatabaseexport.database.service.AbstractDataResultImpl;
import com.easydatabaseexport.entities.ErrorMsg;
import com.easydatabaseexport.entities.IndexInfo;
import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.entities.TableType;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.util.HtmlUtils;
import com.easydatabaseexport.util.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * KingBaseDataV8ResultImpl
 *
 * @author lzy
 * @date 2022/07/21 10:03
 **/
@Log
public class KingBaseDataV8ResultImpl extends AbstractDataResultImpl implements DataResult {

    @Override
    public JScrollPane getDataCenterInfo(int width, int height) throws SQLException {

        String sql = "select * from \"" + CommonConstant.DATABASE_NAME + "\".\"" + CommonConstant.TABLE_NAME + "\"";

        PreparedStatement ppst = null;
        ResultSet rs = null;

        ppst = CommonConstant.connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs = ppst.executeQuery();
        rs.beforeFirst();

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

    @Override
    public void initList() {
        CommonConstant.tableParameterList.clear();
        String sql = "SELECT  " +
                " col.COLUMN_NAME, " +
                " col.udt_name, " +
                " COALESCE(col.numeric_precision, COALESCE(col.datetime_precision, COALESCE(col.numeric_precision, COALESCE(col.character_maximum_length, 0)))), " +
                " COALESCE(col.numeric_scale, 0), " +
                " col.is_nullable, " +
                " col.column_default AS col_default, " +
                " col_description ( C.oid, col.ordinal_position ) AS COMMENT " +
                "FROM " +
                " information_schema.COLUMNS AS col " +
                " LEFT JOIN pg_namespace ns ON ns.nspname = col.table_schema " +
                " LEFT JOIN pg_class C ON col.TABLE_NAME = C.relname  " +
                " AND C.relnamespace = ns.oid " +
                "WHERE " +
                " col.table_schema = '?'  " +
                " AND col.TABLE_NAME = '?'  " +
                "ORDER BY " +
                " col.table_schema, " +
                " col.TABLE_NAME, " +
                " col.ordinal_position";
        String newSql = String.format(sql.replace("?", "%s"), CommonConstant.DATABASE_NAME, CommonConstant.TABLE_NAME);
        ResultSet rs = getResultSetBySql(newSql);

        try {
            //rs相当于一个指针一样---指向了返回的结果集的第一行之前,而在之前已经遍历过到最后了，所以现在需要返回到原先的位置
            rs.beforeFirst();
        } catch (SQLException e1) {
            LogManager.writeLogFile(e1, log);
        }

        //获取返回的ResultSet内容
        try {
            TableParameter tableParameter = null;
            while (rs.next()) {
                tableParameter = new TableParameter();
                tableParameter.setColumnName(StringUtil.StringEqual(rs.getString(1)));
                tableParameter.setColumnType(StringUtil.StringEqual(rs.getString(2)));
                tableParameter.setLength(StringUtil.StringEqual(rs.getString(3)));
                tableParameter.setDecimalPlaces(StringUtil.StringEqual(rs.getString(4)));
                tableParameter.setIsNullAble(StringUtil.StringEqual(rs.getString(5)));
                tableParameter.setColumnDefault(StringUtil.StringEqual(rs.getString(6)));
                tableParameter.setColumnComment(StringUtil.StringEqual(rs.getString(7)));
                CommonConstant.tableParameterList.add(tableParameter);
            }
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
    }

    @SneakyThrows
    @Override
    public List<TableParameter> getTableStructureByKeyForMode(String databaseName, String tableName, String catalog) {

        List<TableParameter> tableParameterList = new ArrayList<>();
        String sql = "SELECT  " +
                " col.COLUMN_NAME, " +
                " col.udt_name, " +
                " COALESCE(col.numeric_precision, COALESCE(col.datetime_precision, COALESCE(col.numeric_precision, COALESCE(col.character_maximum_length, 0)))), " +
                " COALESCE(col.numeric_scale, 0), " +
                " col.is_nullable, " +
                " col.column_default AS col_default, " +
                " col_description ( C.oid, col.ordinal_position ) AS COMMENT " +
                "FROM " +
                " information_schema.COLUMNS AS col " +
                " LEFT JOIN pg_namespace ns ON ns.nspname = col.table_schema " +
                " LEFT JOIN pg_class C ON col.TABLE_NAME = C.relname  " +
                " AND C.relnamespace = ns.oid " +
                "WHERE " +
                " col.table_schema = '?'  " +
                " AND col.TABLE_NAME = '?'  " +
                "ORDER BY " +
                " col.table_schema, " +
                " col.TABLE_NAME, " +
                " col.ordinal_position ";
        String newSql = String.format(sql.replace("?", "%s"), databaseName, tableName);
        ResultSet rs = getResultSetBySql(newSql);
        try {
            //rs相当于一个指针一样---指向了返回的结果集的第一行之前,而在之前已经遍历过到最后了，所以现在需要返回到原先的位置
            rs.beforeFirst();
        } catch (SQLException e1) {
            LogManager.writeLogFile(e1, log);
        }

        //获取返回的ResultSet内容
        try {
            TableParameter tableParameter = null;
            while (rs.next()) {
                tableParameter = new TableParameter();
                tableParameter.setColumnName(StringUtil.StringEqual(rs.getString(1)));
                tableParameter.setColumnType(StringUtil.StringEqual(rs.getString(2)));
                tableParameter.setLength(StringUtil.StringEqual(rs.getString(3)));
                tableParameter.setDecimalPlaces(StringUtil.StringEqual(rs.getString(4)));
                tableParameter.setIsNullAble(StringUtil.StringEqual(rs.getString(5)));
                tableParameter.setColumnDefault(StringUtil.StringEqual(rs.getString(6)));
                tableParameter.setColumnComment(StringUtil.StringEqual(rs.getString(7)));
                tableParameterList.add(tableParameter);
            }
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
        return tableParameterList;
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

    /**
     * 有空再看<a href="https://blog.csdn.net/qiuchenjun/article/details/121858010">
     **/
    private String getDdlInfo() {
       /* StringBuilder str = new StringBuilder();
        String sql = "";
        String newSql = String.format(sql.replace("?", "%s"), CommonConstant.DATABASE_NAME, CommonConstant.TABLE_NAME);
        ResultSet rs = getResultSetBySql(newSql);

        try {
            //rs相当于一个指针一样---指向了返回的结果集的第一行之前,而在之前已经遍历过到最后了，所以现在需要返回到原先的位置
            rs.beforeFirst();
        } catch (SQLException e1) {
            LogManager.writeLogFile(e1, log);
        }

        //获取返回的ResultSet内容
        try {
            while (rs.next()) {
                str.append(StringUtil.StringEqual(rs.getString(1)));
            }
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
        return str.toString();*/
        return "";
    }

    /**
     * 执行数据查询器
     *
     * @return void
     * @author lzy
     * @date 2022/3/30 18:16
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
        String sql = "SELECT count(1) FROM INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = '" + dataBase + "' AND TABLE_NAME = '" + tableName + "'";
        ResultSet rs = null;
        Map<String, ErrorMsg> map = new HashMap<>(0);
        ErrorMsg msg = new ErrorMsg();
        try {
            PreparedStatement ppst = CommonConstant.connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = ppst.executeQuery();
            rs.last();
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
            Statement stmt = CommonConstant.connection.createStatement();
            //执行查询所有数据库操作
            ResultSet rs = null;

            rs = stmt.executeQuery("SELECT d.datname FROM pg_database d where d.datistemplate = 'f' order by d.datname");

            //处理表名
            while (rs.next()) {
                nameList.add(rs.getString(1));
            }
            //处理拼接in中的参数
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < nameList.size(); i++) {
                if (i == nameList.size() - 1) {
                    stringBuilder.append(nameList.get(i));
                } else {
                    stringBuilder.append(nameList.get(i)).append(",");
                }
            }
            //postgresql 库 -> 模式 -> 表
            //根据所有模式 去查表
            if ("".equals(CommonConstant.DATABASE_NAME)) {
                getTableInfoByMode(stringBuilder.toString());
            } else {
                nameList.clear();
                nameList.add(CommonConstant.DATABASE_NAME);
                getTableInfoByMode(CommonConstant.DATABASE_NAME);
            }

        } catch (Exception e) {
            LogManager.writeLogFile(e, log);
        }
        return nameList;
    }

    private void getTableInfoByMode(String modes) throws SQLException {
        String sql = "SELECT n.nspname AS tableSchema " +
                "FROM pg_attribute " +
                "A JOIN pg_class C ON A.attrelid = C.oid " +
                "JOIN pg_namespace n ON C.relnamespace = n.oid " +
                "WHERE A.attnum > 0 GROUP BY " +
                "n.nspname ORDER BY nspname";
        String[] str = modes.split(",");
        //首先执行选库操作
        for (int i = 0; i < str.length; i++) {
            //执行sql
            List<TableType> list = super.toList(new TableType(), sql);
            if (!list.isEmpty()) {
                int finalI = i;
                list.forEach(v -> {
                    try {
                        getTableInfoAndStructure(v.getTableSchema(), str[finalI]);
                    } catch (SQLException e) {
                        LogManager.writeLogFile(e, log);
                    }
                });
            }
        }
    }

    @Override
    public List<IndexInfoVO> getIndexByKeyForMode(String dataBase, String tableName, String catalog) {
        String sql = "SELECT ct.relname AS TABLE_NAME," +
                " pg_get_indexdef(i.indexrelid) col_name, " +
                " ci.relname AS index_name, " +
                " am.amname AS index_type, " +
                " i.indisunique AS is_unique, " +
                " i.indisprimary AS is_primary, " +
                " obj_description ( indexrelid ) AS COMMENT  " +
                "FROM " +
                " pg_index i " +
                " LEFT JOIN pg_class ct ON ct.oid = i.indrelid " +
                " LEFT JOIN pg_class ci ON ci.oid = i.indexrelid " +
                " LEFT JOIN pg_namespace tns ON tns.oid = ct.relnamespace " +
                " LEFT JOIN pg_namespace ins ON ins.oid = ci.relnamespace " +
                " LEFT JOIN pg_tablespace ts ON ci.reltablespace = ts.oid " +
                " LEFT JOIN pg_am am ON ci.relam = am.oid " +
                " LEFT JOIN pg_depend dep ON dep.classid = ci.tableoid  " +
                " AND dep.objid = ci.oid  " +
                " AND dep.refobjsubid = '0' " +
                " LEFT JOIN pg_constraint con ON con.tableoid = dep.refclassid  " +
                " AND con.oid = dep.refobjid " +
                " LEFT JOIN pg_roles pa ON pa.oid = ci.relowner  " +
                "WHERE " +
                " ins.nspname = '?'  " +
                " AND ct.relname = '?'  " +
                "ORDER BY " +
                " ct.relname, " +
                " ins.nspname, " +
                " ci.relname";
        String newSql = String.format(sql.replace("?", "%s"), dataBase, tableName);
        List<IndexInfo> indexList = new ArrayList<>();
        List<IndexInfoVO> voList = new ArrayList<>();
        ResultSet rs = getResultSetBySql(newSql);

        //获取返回的ResultSet内容
        try {
            IndexInfo indexInfo = null;
            while (rs.next()) {
                indexInfo = new IndexInfo();
                indexInfo.setTable(StringUtil.StringEqual(rs.getString(1)));
                indexInfo.setColumnName(StringUtil.StringEqual(rs.getString(2)));
                indexInfo.setKeyName(StringUtil.StringEqual(rs.getString(3)));
                //indexInfo.setSortType(StringUtil.StringEqual(rs.getString(6)));
                indexInfo.setIndexType(StringUtil.StringEqual(rs.getString(4)));
                indexInfo.setNonUnique("t".equals(StringUtil.StringEqual(rs.getString(5))) ? "是" : "否");
                indexInfo.setIsPk("t".equals(StringUtil.StringEqual(rs.getString(6))) ? "是" : "否");
                indexInfo.setIndexComment(StringUtil.StringEqual(rs.getString(7)));
                indexList.add(indexInfo);
            }
            //根据keyName分组
            Map<String, List<IndexInfo>> indexMap = indexList.stream().collect(Collectors.groupingBy(IndexInfo::getKeyName));
            for (Map.Entry<String, List<IndexInfo>> map : indexMap.entrySet()) {
                List<IndexInfo> list = map.getValue();
                if (map.getKey().endsWith("_pkey") && list.size() > 0 && "是".equals(list.get(0).getIsPk())) {
                    list.forEach(e -> {
                        IndexInfoVO vo = new IndexInfoVO();
                        vo.setName(map.getKey());
                        vo.setColumnName(e.getColumnName());
                        vo.setIndexType(e.getIndexType());
                        vo.setComment("主键");
                        voList.add(vo);
                    });
                } else {
                    IndexInfoVO vo = new IndexInfoVO();
                    vo.setName(map.getKey());
                    vo.setIndexType(list.get(0).getIndexType());
                    List<String> names = list.stream().map(IndexInfo::getColumnName).collect(Collectors.toList());
                    vo.setColumnName(StringUtil.join(names, ","));
                    vo.setComment(list.get(0).getIndexComment());
                    voList.add(vo);
                }
            }
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
        return voList;
    }

    public void getTableInfoAndStructure(String name, String tableCatalog) throws SQLException {
        String sql = "SELECT c.oid, n.nspname AS schemaname, c.relname AS tablename, c.relacl, pg_get_userbyid(c.relowner) AS tableowner, " +
                "obj_description(c.oid) AS description, c.relkind, ci.relname As cluster, c.relhasindex AS hasindexes, " +
                "c.relhasrules AS hasrules, t.spcname AS tablespace, c.reloptions AS param, c.relhastriggers AS hastriggers, " +
                "c.relpersistence AS unlogged, ft.ftoptions, fs.srvname, c.relispartition, pg_get_expr(c.relpartbound, c.oid) " +
                "AS relpartbound, c.reltuples, ((SELECT count(*) FROM pg_inherits WHERE inhparent = c.oid) > 0) AS inhtable, " +
                "i2.nspname AS inhschemaname, i2.relname AS inhtablename FROM pg_class c LEFT JOIN pg_namespace n ON n.oid =" +
                " c.relnamespace LEFT JOIN pg_tablespace t ON t.oid = c.reltablespace LEFT JOIN (pg_inherits i INNER JOIN pg_class c2 " +
                "ON i.inhparent = c2.oid LEFT JOIN pg_namespace n2 ON n2.oid = c2.relnamespace) i2 ON i2.inhrelid = c.oid LEFT JOIN pg_index ind " +
                "ON(ind.indrelid = c.oid) and (ind.indisclustered = 't') LEFT JOIN pg_class ci ON ci.oid = ind.indexrelid LEFT JOIN pg_foreign_table " +
                "ft ON ft.ftrelid = c.oid LEFT JOIN pg_foreign_server fs ON ft.ftserver = fs.oid WHERE ((c.relkind = 'r'::\"char\") " +
                "OR (c.relkind = 'f'::\"char\") OR (c.relkind = 'p'::\"char\")) AND n.nspname = '?' ORDER BY schemaname, tablename ";
        String newSql = String.format(sql.replace("?", "%s"), name);
        ResultSet rs = getResultSetBySql(newSql);

        List<TableType> list = new ArrayList<>();

        try {
            //rs相当于一个指针一样---指向了返回的结果集的第一行之前,而在之前已经遍历过到最后了，所以现在需要返回到原先的位置
            rs.beforeFirst();
        } catch (SQLException e1) {
            LogManager.writeLogFile(e1, log);
        }

        //获取返回的ResultSet内容
        try {
            TableType tableType = null;
            while (rs.next()) {
                tableType = new TableType();
                tableType.setTableName(StringUtil.StringEqual(rs.getString(3)) + "[" + StringUtil.StringEqual(rs.getString(6)) + "]");
                tableType.setObjectId(StringUtil.StringEqual(rs.getString(1)));
                tableType.setCreateTime(StringUtil.StringEqual(rs.getString(4)));
                tableType.setUpdateTime(StringUtil.StringEqual(rs.getString(5)));
                tableType.setTableRows(StringUtil.StringEqual(rs.getString(8)));
                tableType.setTableComment(StringUtil.StringEqual(rs.getString(9)));
                tableType.setTableSchema(StringUtil.StringEqual(rs.getString(2)));
                list.add(tableType);
            }
            if (CommonDataBaseType.CON_DATABASE_MODE_TABLE_MAP.containsKey(tableCatalog)) {
                CommonDataBaseType.CON_DATABASE_MODE_TABLE_MAP.get(tableCatalog).putAll(list.parallelStream().collect(Collectors.groupingBy(TableType::getTableSchema)));
            } else {
                CommonDataBaseType.CON_DATABASE_MODE_TABLE_MAP.put(tableCatalog, list.parallelStream().collect(Collectors.groupingBy(TableType::getTableSchema)));
            }
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
    }

    /**
     * 点击查询
     *
     * @param tableName 表名
     * @param dataBase  库名
     * @return String
     **/
    @Override
    public String getTableInfoAndStructureByClick(String tableName, String dataBase) throws SQLException {
        String sql = "SELECT  " +
                "C.relname AS tablename, " +
                "C.oid, " +
                "pg_get_userbyid ( C.relowner ) AS tableowner, " +
                "C.reltuples, " +
                "C.relkind, " +
                "obj_description ( C.oid ) AS description " +
                "FROM " +
                " pg_class " +
                " C LEFT JOIN pg_namespace n ON n.oid = C.relnamespace " +
                "WHERE " +
                " ( " +
                "  ( C.relkind = 'r' :: \"char\" )  " +
                "  OR ( C.relkind = 'f' :: \"char\" )  " +
                "  OR ( C.relkind = 'p' :: \"char\" )  " +
                " )  " +
                " AND n.nspname = '?'  " +
                " AND C.relname = '?'  " +
                "ORDER BY " +
                " tablename";
        String newSql = String.format(sql.replace("?", "%s"), dataBase, tableName);
        ResultSet rs = getResultSetBySql(newSql);

        try {
            //rs相当于一个指针一样---指向了返回的结果集的第一行之前,而在之前已经遍历过到最后了，所以现在需要返回到原先的位置
            rs.beforeFirst();
        } catch (SQLException e1) {
            LogManager.writeLogFile(e1, log);
        }

        //获取返回的ResultSet内容
        StringBuilder stringBuilder = new StringBuilder();
        try {
            while (rs.next()) {
                stringBuilder.append("<html>").append(HtmlUtils.getHtml("表名")).append(StringUtil.StringEqual(rs.getString(1)));
                stringBuilder.append(HtmlUtils.getHtml("OID")).append(StringUtil.StringEqual(rs.getString(2)));
                stringBuilder.append(HtmlUtils.getHtml("所有者")).append(StringUtil.StringEqual(rs.getString(3)));
                stringBuilder.append(HtmlUtils.getHtml("行")).append(StringUtil.StringEqual(rs.getString(4)));
                stringBuilder.append(HtmlUtils.getHtml("表类型")).append("r".equals(StringUtil.StringEqual(rs.getString(5))) ? "常规" :
                        "p".equals(StringUtil.StringEqual(rs.getString(5))) ? "分区" : "外部");
                stringBuilder.append(HtmlUtils.getHtml("表注释")).append(StringUtil.StringEqual(rs.getString(6))).append("</html>");
            }
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
        return stringBuilder.toString();
    }

    @Override
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

}
