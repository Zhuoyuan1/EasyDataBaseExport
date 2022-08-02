package com.easydatabaseexport.database.sqlserver.impl;

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
import com.mysql.cj.util.StringUtils;
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
 * OracleDataResultImpl
 *
 * @author lzy
 * @date 2021/11/1 15:35
 **/
@Log
public class SqlServerDataResultImpl extends AbstractDataResultImpl implements DataResult {

    @Override
    public JScrollPane getDataCenterInfo(int width, int height) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("use ");
        stringBuilder.append("[").append(CommonConstant.TREE_DATABASE).append("]");
        String sql = "select * from [" + CommonConstant.DATABASE_NAME + "].[" + CommonConstant.TABLE_NAME + "]";

        PreparedStatement ppst = null;
        ResultSet rs = null;

        //首先执行选库操作
        if (!StringUtils.isNullOrEmpty(CommonConstant.DATABASE_NAME)) {
            ppst = CommonConstant.connection.prepareStatement(stringBuilder.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ppst.execute();
        }

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

        String sql = "SELECT c.name AS column_name, " +
                "t.name AS type_name, c.max_length,c.scale, case when c.is_nullable = 0 then N'否' else N'是' end, " +
                "d.definition AS default_value, " +
                "CAST ( p.[value] AS nvarchar ( 4000 ) ) AS comment " +
                "FROM sys.columns c LEFT JOIN sys.types t ON c.user_type_id = t.user_type_id " +
                "LEFT JOIN ( SELECT " +
                "so.object_id, sc.name AS default_schema, so.name AS default_name, " +
                "dc.definition FROM " +
                "sys.objects so LEFT JOIN sys.schemas sc ON sc.schema_id = so.schema_id " +
                "LEFT JOIN sys.default_constraints dc ON dc.object_id = so.object_id  " +
                "WHERE so.type = 'D' ) d ON d.object_id = c.default_object_id " +
                "LEFT JOIN sys.objects o ON o.object_id = c.object_id " +
                "LEFT JOIN sys.schemas s ON s.schema_id = o.schema_id " +
                "LEFT JOIN sys.extended_properties p ON p.major_id = c.object_id  " +
                "AND p.minor_id = c.column_id  " +
                "AND p.class = 1 AND p.name = 'MS_Description' " +
                "WHERE s.name = N'?' AND o.name = N'?'  " +
                "ORDER BY c.object_id ASC, c.column_id ASC ";
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
        //sqlserver要执行选库操作
        String stringBuilder = "use [" + catalog + "]";

        PreparedStatement ppst = CommonConstant.connection
                .prepareStatement(stringBuilder, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ppst.execute();

        List<TableParameter> tableParameterList = new ArrayList<>();
        String sql = "SELECT c.name AS column_name, " +
                "t.name AS type_name, c.max_length,c.scale, case when c.is_nullable = 0 then N'否' else N'是' end, " +
                "d.definition AS default_value, " +
                "CAST ( p.[value] AS nvarchar ( 4000 ) ) AS comment " +
                "FROM sys.columns c LEFT JOIN sys.types t ON c.user_type_id = t.user_type_id " +
                "LEFT JOIN ( SELECT " +
                "so.object_id, sc.name AS default_schema, so.name AS default_name, " +
                "dc.definition FROM " +
                "sys.objects so LEFT JOIN sys.schemas sc ON sc.schema_id = so.schema_id " +
                "LEFT JOIN sys.default_constraints dc ON dc.object_id = so.object_id  " +
                "WHERE so.type = 'D' ) d ON d.object_id = c.default_object_id " +
                "LEFT JOIN sys.objects o ON o.object_id = c.object_id " +
                "LEFT JOIN sys.schemas s ON s.schema_id = o.schema_id " +
                "LEFT JOIN sys.extended_properties p ON p.major_id = c.object_id  " +
                "AND p.minor_id = c.column_id  " +
                "AND p.class = 1 AND p.name = 'MS_Description' " +
                "WHERE s.name = N'?' AND o.name = N'?'  " +
                "ORDER BY c.object_id ASC, c.column_id ASC ";
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

    private String getDdlInfo() {
        StringBuilder str = new StringBuilder();
        String sql = "select  'create table [' + so.name + '] (' + o.list + ')'  " +
                "    + CASE WHEN tc.Constraint_Name IS NULL THEN '' ELSE 'ALTER TABLE ' + so.Name + ' ADD CONSTRAINT ' + tc.Constraint_Name  + ' PRIMARY KEY ' + ' (' + LEFT(j.List, Len(j.List)-1) + ')' END  " +
                "    TABLE_DDL " +
                " from    sys.objects so " +
                "cross apply " +
                "    (SELECT  " +
                "        '  ['+column_name+'] ' +  " +
                "        data_type + case data_type " +
                "            when 'sql_variant' then '' " +
                "            when 'text' then '' " +
                "            when 'ntext' then '' " +
                "            when 'xml' then '' " +
                "            when 'decimal' then '(' + cast(numeric_precision as varchar) + ', ' + cast(numeric_scale as varchar) + ')' " +
                "            else coalesce('('+case when character_maximum_length = -1 then 'MAX' else cast(character_maximum_length as varchar) end +')','') end + ' ' + " +
                "        case when exists (  " +
                "        select id from syscolumns " +
                "        where object_name(id)=so.name " +
                "        and name=column_name " +
                "        and columnproperty(id,name,'IsIdentity') = 1  " +
                "        ) then " +
                "        'IDENTITY(' +  " +
                "        cast(ident_seed(so.name) as varchar) + ',' +  " +
                "        cast(ident_incr(so.name) as varchar) + ')' " +
                "        else '' " +
                "        end + ' ' + " +
                "         (case when IS_NULLABLE = 'No' then 'NOT ' else '' end ) + 'NULL ' +  " +
                "          case when information_schema.columns.COLUMN_DEFAULT IS NOT NULL THEN 'DEFAULT '+ information_schema.columns.COLUMN_DEFAULT ELSE '' END + ', '  " +
                "     from information_schema.columns where table_name = so.name " +
                "     order by ordinal_position " +
                "    FOR XML PATH('')) o (list) " +
                "left join " +
                "    information_schema.table_constraints tc " +
                "on  tc.Table_name       = so.Name " +
                "AND tc.Constraint_Type  = 'PRIMARY KEY' " +
                "cross apply " +
                "    (select '[' + Column_Name + '], ' " +
                "     FROM   information_schema.key_column_usage kcu " +
                "     WHERE  kcu.Constraint_Name = tc.Constraint_Name " +
                "     ORDER BY " +
                "        ORDINAL_POSITION " +
                "     FOR XML PATH('')) j (list)  " +
                "where   type = 'U' " +
                " and schema_id = schema_id(N'?') " +
                " AND name=N'?' ";
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
        return str.toString();
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
        String sql = "SELECT count(1) FROM INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = N'" + dataBase + "' AND TABLE_NAME = N'" + tableName + "'";
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

            rs = stmt.executeQuery("SELECT name FROM sys.databases");

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
            //sqlserver 库 -> 模式 -> 表
            //根据所有模式 去查表
            getTableInfoByMode(stringBuilder.toString());

        } catch (Exception e) {
            LogManager.writeLogFile(e, log);
        }
        return nameList;
    }

    private void getTableInfoByMode(String modes) throws SQLException {
        String sql = "SELECT TABLE_SCHEMA FROM INFORMATION_SCHEMA.TABLES GROUP BY TABLE_SCHEMA";
        String[] str = modes.split(",");
        PreparedStatement ppst = null;
        //首先执行选库操作

        for (int i = 0; i < str.length; i++) {
            String newSql = "use [" + str[i] + "]";
            //执行选库
            ppst = CommonConstant.connection.prepareStatement(newSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ppst.execute();
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
        String sql = "SELECT o.name AS table_name,col.name AS column_name, o.type AS table_type, o.object_id, i.name, " +
                "Sort=CASE INDEXKEY_PROPERTY(ic.[object_id],ic.index_id,ic.index_column_id,'IsDescending') " +
                "  WHEN 1 THEN 'DESC' WHEN 0 THEN 'ASC' ELSE '' END, ic.partition_ordinal, i.type_desc, i.index_id, " +
                "CASE when i.is_unique = 0 then N'否' else N'是' end, i.is_primary_key, i.is_unique_constraint, i.fill_factor, i.data_space_id, i.ignore_dup_key, " +
                "CAST ( ep.value AS NVARCHAR ( MAX ) ) comment  " +
                "FROM sys.indexes i LEFT JOIN sys.all_objects o ON o.object_id = i.object_id LEFT JOIN sys.schemas s " +
                "ON s.schema_id = o.schema_id LEFT JOIN sys.index_columns ic ON ic.object_id = i.object_id  AND ic.index_id = i.index_id " +
                "LEFT JOIN sys.all_columns col ON ic.column_id = col.column_id  AND ic.object_id = col.object_id LEFT JOIN sys.xml_indexes xi " +
                "ON i.object_id = xi.object_id  AND i.index_id = xi.index_id LEFT JOIN sys.indexes pri ON xi.object_id = pri.object_id  " +
                "AND xi.using_xml_index_id = pri.index_id LEFT JOIN sys.key_constraints cons ON ( cons.parent_object_id = ic.object_id " +
                "AND cons.unique_index_id = i.index_id ) LEFT JOIN sys.extended_properties ep ON ( ( ( i.is_primary_key <> 1  " +
                "AND i.is_unique_constraint <> 1  AND ep.class = 7  AND i.object_id = ep.major_id  AND ep.minor_id = i.index_id  )  " +
                "OR ( ( i.is_primary_key = 1 OR i.is_unique_constraint = 1 )  AND ep.class = 1  AND cons.object_id = ep.major_id  AND ep.minor_id = 0  )  ) " +
                " AND ep.name = 'MS_Description'  ) LEFT JOIN sys.spatial_indexes si ON i.object_id = si.object_id  AND i.index_id = si.index_id " +
                "LEFT JOIN sys.spatial_index_tessellations sit ON i.object_id = sit.object_id  AND i.index_id = sit.index_id , sys.stats stat " +
                "LEFT JOIN sys.all_objects so ON ( stat.object_id = so.object_id )  " +
                "WHERE ( i.object_id = so.object_id OR i.object_id = so.parent_object_id )  AND i.name = stat.name  " +
                "AND i.index_id > 0 AND s.name = N'?'  AND o.name = N'?'  " +
                "AND o.type IN ( 'U', 'S' ) ORDER BY table_name, i.index_id, ic.key_ordinal, ic.index_column_id";
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
                indexInfo.setKeyName(StringUtil.StringEqual(rs.getString(5)));
                indexInfo.setSortType(StringUtil.StringEqual(rs.getString(6)));
                indexInfo.setIndexType(StringUtil.StringEqual(rs.getString(8)));
                indexInfo.setNonUnique(StringUtil.StringEqual(rs.getString(10)));
                indexInfo.setIsPk(StringUtil.StringEqual(rs.getString(11)));
                indexInfo.setIndexComment(StringUtil.StringEqual(rs.getString(16)));
                indexList.add(indexInfo);
            }
            //根据keyName分组
            Map<String, List<IndexInfo>> indexMap = indexList.stream().collect(Collectors.groupingBy(IndexInfo::getKeyName));
            for (Map.Entry<String, List<IndexInfo>> map : indexMap.entrySet()) {
                List<IndexInfo> list = map.getValue();
                if (map.getKey().startsWith("PK") && list.size() > 0 && "1".equals(list.get(0).getIsPk())) {
                    list.forEach(e -> {
                        IndexInfoVO vo = new IndexInfoVO();
                        vo.setName(map.getKey());
                        vo.setColumnName("[" + e.getColumnName() + "] " + e.getSortType());
                        vo.setIndexType(e.getIndexType());
                        vo.setComment("主键");
                        voList.add(vo);
                    });
                } else {
                    IndexInfoVO vo = new IndexInfoVO();
                    vo.setName(map.getKey());
                    vo.setIndexType(list.get(0).getIndexType());
                    List<String> names = list.stream().map(e -> "[" + e.getColumnName() + "] " + e.getSortType()).collect(Collectors.toList());
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
        String sql = "SELECT o.name, o.object_id, o.type_desc, o.create_date, o.modify_date, t.lock_escalation, " +
                "ct.is_track_columns_updated_on, st.row_count AS rows, CAST(ep.value AS NVARCHAR(MAX)) " +
                "AS comment, s.name AS schemaname, IDENT_CURRENT(QUOTENAME(s.name) + '.' + QUOTENAME(o.name)) AS current_value " +
                "FROM sys.objects o LEFT JOIN sys.schemas s ON o.schema_id = s.schema_id " +
                "LEFT JOIN sys.tables t ON o.object_id = t.object_id " +
                "LEFT JOIN sys.extended_properties ep ON (o.object_id = ep.major_id AND ep.class = 1 AND ep.minor_id = 0 AND ep.name = 'MS_Description') " +
                "LEFT JOIN (SELECT object_id, SUM(ROWS) row_count FROM sys.partitions WHERE index_id < 2 GROUP BY object_id) st ON " +
                "o.object_id = st.object_id LEFT JOIN sys.change_tracking_tables ct ON ct.object_id = o.object_id " +
                "WHERE (o.type = 'U' OR o.type = 'S') AND s.name = N'?' ORDER BY o.name ASC";
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
                tableType.setTableName(StringUtil.StringEqual(rs.getString(1)) + "[" + StringUtil.StringEqual(rs.getString(9)) + "]");
                tableType.setObjectId(StringUtil.StringEqual(rs.getString(2)));
                tableType.setCreateTime(StringUtil.StringEqual(rs.getString(4)));
                tableType.setUpdateTime(StringUtil.StringEqual(rs.getString(5)));
                tableType.setTableRows(StringUtil.StringEqual(rs.getString(8)));
                tableType.setTableComment(StringUtil.StringEqual(rs.getString(9)));
                tableType.setTableSchema(StringUtil.StringEqual(rs.getString(10)));
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
        String sql = "SELECT o.name, o.object_id, " +
                "o.create_date, o.modify_date, CAST ( ep.value AS NVARCHAR ( MAX ) ) AS comment " +
                "FROM sys.objects o " +
                "LEFT JOIN sys.schemas s ON o.schema_id = s.schema_id " +
                "LEFT JOIN sys.extended_properties ep ON ( o.object_id = ep.major_id AND ep.class = 1 AND ep.minor_id = 0 AND ep.name = 'MS_Description' ) " +
                "WHERE ( o.type = 'U' OR o.type = 'S' ) AND s.name = N'?' and o.name = N'?' ORDER BY o.name ASC";
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
                stringBuilder.append(HtmlUtils.getHtml("创建日期")).append(StringUtil.StringEqual(rs.getString(3)));
                stringBuilder.append(HtmlUtils.getHtml("修改日期")).append(StringUtil.StringEqual(rs.getString(4)));
                stringBuilder.append(HtmlUtils.getHtml("注释")).append(StringUtil.StringEqual(rs.getString(5))).append("</html>");
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

    @SneakyThrows
    @Override
    public void doListValueChanged() {
        //sqlserver要执行选库操作
        String stringBuilder = "use [" + CommonConstant.TREE_DATABASE + "]";

        PreparedStatement ppst = CommonConstant.connection
                .prepareStatement(stringBuilder, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ppst.execute();

        CommonConstant.RIGHT.removeAll();
        JTabbedPane jPanel = getRightInfo(CommonConstant.RIGHT.getWidth(), CommonConstant.RIGHT.getHeight());
        CommonConstant.RIGHT.add(jPanel);
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

}
