package com.easydatabaseexport.database.xugu.impl;

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
 * XuguDataResultImpl
 *
 * @author lzy
 * @date 2021/11/1 15:35
 **/
@Log
public class XuguDataResultImpl extends AbstractDataResultImpl implements DataResult {

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
        String sql = "select distinct col_name,type_name,'',SCALE,NOT_NULL,DEF_VAL,COMMENTS,A.COL_NO" +
                " FROM SYS_COLUMNS A  WHERE table_id=(select table_id " +
                "from SYS_tables where  table_name ='?'and schema_id in (select schema_id from SYS_schemas where schema_name ='?')) " +
                "ORDER BY COL_NO";
        String newSql = String.format(sql.replace("?", "%s"), CommonConstant.TABLE_NAME, CommonConstant.DATABASE_NAME);
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
                tableParameter.setIsNullAble(StringUtil.StringEqual("T".equals(rs.getString(5)) ? "是" : "否"));
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
        String sql = "select distinct col_name,type_name,'',SCALE,NOT_NULL,DEF_VAL,COMMENTS, A.COL_NO" +
                " FROM SYS_COLUMNS A  WHERE table_id=(select table_id " +
                "from SYS_tables where  table_name ='?'and schema_id in (select schema_id from SYS_schemas where schema_name ='?')) " +
                "ORDER BY COL_NO";
        String newSql = String.format(sql.replace("?", "%s"), tableName, databaseName);
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
                tableParameter.setIsNullAble(StringUtil.StringEqual("T".equals(rs.getString(5)) ? "是" : "否"));
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
     * 获取ddl
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
        String sql = "SELECT count(1) FROM SYS_tables t,SYS_schemas s where s.SCHEMA_NAME = '" + dataBase + "' AND t.TABLE_NAME = '" + tableName + "'";
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

            rs = stmt.executeQuery("SELECT S.SCHEMA_NAME FROM ALL_SCHEMAS S,ALL_USERS U WHERE S.USER_ID=U.USER_ID AND S.DB_ID=1 ORDER BY S.SCHEMA_ID ASC");

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
        String sql = "SELECT S.SCHEMA_NAME tableSchema FROM ALL_SCHEMAS S,ALL_USERS U WHERE S.USER_ID=U.USER_ID AND S.DB_ID=1 ORDER BY S.SCHEMA_ID ASC";
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

    enum IndexType {
        //Btree
        Btree("0");

        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        IndexType(String code) {
            this.code = code;
        }

        public static String getIndexTypeByCode(String code) {
            for (IndexType enumer : values()) {
                if (enumer.getCode().equals(code)) {
                    return enumer.name();
                }
            }
            return "";
        }
    }

    @Override
    public List<IndexInfoVO> getIndexByKeyForMode(String dataBase, String tableName, String catalog) {
        String sql = "select distinct tab.TABLE_NAME,idx.KEYS,idx.INDEX_NAME,idx.INDEX_TYPE,idx.IS_UNIQUE,idx.IS_PRIMARY,'' from SYS_indexes idx left join SYS_tables tab on tab.table_id=idx.table_id " +
                "left join SYS_schemas sch on tab.schema_id=sch.schema_id where sch.schema_name='?' and tab.table_name='?'";
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
                indexInfo.setIndexType(StringUtil.StringEqual(IndexType.getIndexTypeByCode(rs.getString(4))));
                indexInfo.setNonUnique("true".equals(StringUtil.StringEqual(rs.getString(5))) ? "是" : "否");
                indexInfo.setIsPk("t".equals(StringUtil.StringEqual(rs.getString(6))) ? "是" : "否");
                indexInfo.setIndexComment(StringUtil.StringEqual(rs.getString(7)));
                indexList.add(indexInfo);
            }
            //根据keyName分组
            Map<String, List<IndexInfo>> indexMap = indexList.stream().collect(Collectors.groupingBy(IndexInfo::getKeyName));
            for (Map.Entry<String, List<IndexInfo>> map : indexMap.entrySet()) {
                List<IndexInfo> list = map.getValue();
                if (map.getKey().startsWith("PK_") && list.size() > 0 && "是".equals(list.get(0).getIsPk())) {
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
        String sql = "select distinct t.TABLE_NAME,'',s.DB_ID,t.CREATE_TIME,t.COMMENTS,s.SCHEMA_NAME from SYS_tables t,SYS_schemas s " +
                "where s.schema_id = t.schema_id and s.DB_ID = 1 and s.schema_name='?' order by t.TABLE_NAME ";
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
                tableType.setTableName(StringUtil.StringEqual(rs.getString(1)) + "[" + StringUtil.StringEqual(rs.getString(5)) + "]");
                tableType.setObjectId(StringUtil.StringEqual(rs.getString(3)));
                tableType.setCreateTime(StringUtil.StringEqual(rs.getString(4)));
                tableType.setUpdateTime(StringUtil.StringEqual(rs.getString(4)));
                tableType.setTableRows(StringUtil.StringEqual(rs.getString(2)));
                tableType.setTableComment(StringUtil.StringEqual(rs.getString(5)));
                tableType.setTableSchema(StringUtil.StringEqual(rs.getString(6)));
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
        String sql = "SELECT t.TABLE_NAME,s.SCHEMA_NAME,t.COMMENTS from SYS_tables t,SYS_schemas s  where s.schema_id = t.schema_id and s.DB_ID = 1 " +
                "and s.schema_name='?' and t.table_name ='?' order by t.TABLE_NAME ";
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
                stringBuilder.append(HtmlUtils.getHtml("所属模式")).append(StringUtil.StringEqual(rs.getString(2)));
                stringBuilder.append(HtmlUtils.getHtml("表注释")).append(StringUtil.StringEqual(rs.getString(3))).append("</html>");
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
