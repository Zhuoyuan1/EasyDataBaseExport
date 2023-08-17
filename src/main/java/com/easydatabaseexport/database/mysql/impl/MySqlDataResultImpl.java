package com.easydatabaseexport.database.mysql.impl;

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
 * MySqlDataResultImpl
 *
 * @author lzy
 * @date 2021/11/1 15:33
 **/
@Log4j
public class MySqlDataResultImpl extends AbstractDataResultImpl implements DataResult {

    @Override
    public JScrollPane getDataCenterInfo(int width, int height) throws SQLException {
        String sql = "select * from `" + CommonConstant.DATABASE_NAME + "`.`" + CommonConstant.TABLE_NAME + "`";
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

    private static final String sql = "SELECT COLUMN_NAME,COLUMN_TYPE,DATA_TYPE,CHARACTER_MAXIMUM_LENGTH as LENGTH, " +
            "IS_NULLABLE,COLUMN_DEFAULT,NUMERIC_SCALE as decimal_places,COLUMN_COMMENT " +
            "FROM INFORMATION_SCHEMA.COLUMNS " +
            "where table_schema = '?' AND table_name  = '?' order by ORDINAL_POSITION";

    @SneakyThrows
    @Override
    public void initList() {
        CommonConstant.tableParameterList.clear();
        String newSql = String.format(sql.replace("?", "%s"), CommonConstant.DATABASE_NAME, CommonConstant.TABLE_NAME);
        CommonConstant.tableParameterList.addAll(super.toList(new TableParameter(), newSql));
    }

    @Override
    public List<TableParameter> getTableStructureByKey(String databaseName, String tableName) throws Exception {
        String newSql = String.format(sql.replace("?", "%s"), databaseName, tableName);
        return super.toList(new TableParameter(), newSql);
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
        String sql = "SHOW CREATE TABLE `" + CommonConstant.DATABASE_NAME + "`.`" + CommonConstant.TABLE_NAME + "`";
        ResultSet rs = getResultSetBySql(sql);

        try {
            //rs相当于一个指针一样---指向了返回的结果集的第一行之前,而在之前已经遍历过到最后了，所以现在需要返回到原先的位置
            rs.beforeFirst();
        } catch (SQLException e1) {
            LogManager.writeLogFile(e1, log);
        }

        //获取返回的ResultSet内容
        try {
            while (rs.next()) {
                str.append(StringUtil.StringEqual(rs.getString(2)));
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
        String sql = "SHOW CREATE TABLE `" + dataBase + "`.`" + tableName + "`";
        ResultSet rs = null;
        Map<String, ErrorMsg> map = new HashMap<>(16);
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
            //JdbcRowSet jrs = new JdbcRowSetImpl(CommonConstant.connection);
            Statement stmt = CommonConstant.connection.createStatement();
            //执行查询所有数据库操作
            ResultSet rs = null;
            if (StringUtil.isEmpty(CommonConstant.DATABASE_NAME)) {
                rs = stmt.executeQuery("show databases");
            } else {
                rs = stmt.executeQuery("show databases like '" + CommonConstant.DATABASE_NAME + "'");
            }
            //处理表名
            while (rs.next()) {
                nameList.add(rs.getString(1));
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
        String sql = "SHOW INDEX FROM `" + dataBase + "`.`" + tableName + "`";

        List<IndexInfo> indexList = new ArrayList<>();
        List<IndexInfoVO> voList = new ArrayList<>();
        ResultSet rs = getResultSetBySql(sql);

        //获取返回的ResultSet内容
        try {
            IndexInfo indexInfo = null;
            while (rs.next()) {
                indexInfo = new IndexInfo();
                indexInfo.setTable(StringUtil.StringEqual(rs.getString(1)));
                indexInfo.setNonUnique(StringUtil.StringEqual(rs.getString(2)));
                indexInfo.setKeyName(StringUtil.StringEqual(rs.getString(3)));
                indexInfo.setSeqInIndex(StringUtil.StringEqual(rs.getString(4)));
                indexInfo.setColumnName(StringUtil.StringEqual(rs.getString(5)));
                indexInfo.setCollation(StringUtil.StringEqual(rs.getString(6)));
                indexInfo.setCardinality(StringUtil.StringEqual(rs.getString(7)));
                indexInfo.setSubPart(StringUtil.StringEqual(rs.getString(8)));
                indexInfo.setPacked(StringUtil.StringEqual(rs.getString(9)));
                indexInfo.setNull(StringUtil.StringEqual(rs.getString(10)));
                indexInfo.setIndexType(StringUtil.StringEqual(rs.getString(11)));
                indexInfo.setComment(StringUtil.StringEqual(rs.getString(12)));
                indexInfo.setIndexComment(StringUtil.StringEqual(rs.getString(13)));
                indexList.add(indexInfo);
            }
            //根据keyName分组
            Map<String, List<IndexInfo>> indexMap = indexList.stream().collect(Collectors.groupingBy(IndexInfo::getKeyName));
            for (Map.Entry<String, List<IndexInfo>> map : indexMap.entrySet()) {
                List<IndexInfo> list = map.getValue();
                if ("PRIMARY".equals(map.getKey())) {
                    list.forEach(e -> {
                        IndexInfoVO vo = new IndexInfoVO();
                        vo.setName(map.getKey());
                        vo.setIndexType("主键");
                        vo.setColumnName(e.getColumnName());
                        vo.setIndexMethod(e.getIndexType());
                        vo.setComment(e.getIndexComment());
                        voList.add(vo);
                    });
                } else {
                    IndexInfoVO vo = new IndexInfoVO();
                    vo.setName(map.getKey());
                    if ("0".equals(list.get(0).getNonUnique())) {
                        vo.setIndexType("UNIQUE");
                        vo.setIndexMethod(list.get(0).getIndexType());
                    } else {
                        if ("BTREE".equals(list.get(0).getIndexType()) || "HASH".equals(list.get(0).getIndexType())) {
                            vo.setIndexType("NORMAL");
                            vo.setIndexMethod(list.get(0).getIndexType());
                        } else {
                            vo.setIndexType(list.get(0).getIndexType());
                        }
                    }
                    List<String> names = list.stream().map(e -> "`" + e.getColumnName() + "`").collect(Collectors.toList());
                    vo.setColumnName(StringUtil.join(names, ","));
                    vo.setComment(list.get(0).getIndexComment());
                    voList.add(vo);
                }
            }
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
        List<IndexInfoVO> resultList = voList.stream().sorted(Comparator.comparing(IndexInfoVO::getName, (x, y) -> {
            if ("PRIMARY".equals(x) && "PRIMARY".equals(y)) {
                return 0;
            }
            if (x.equals(y)) {
                return 0;
            }
            if ("PRIMARY".equals(x)) {
                return -1;
            }
            return 1;
        })).collect(Collectors.toList());
        if (resultList.isEmpty()) {
            IndexInfoVO vo = new IndexInfoVO();
            vo.setComment("");
            vo.setName("");
            vo.setIndexType("");
            vo.setIndexMethod("");
            vo.setColumnName("");
            resultList.add(vo);
        }
        return resultList;
    }

    private void getTableInfoAndStructure(String name) throws SQLException {
        String sql = "select TABLE_NAME,TABLE_ROWS,ENGINE,AUTO_INCREMENT,ROW_FORMAT,UPDATE_TIME," +
                "CREATE_TIME,CHECK_TIME,INDEX_LENGTH,DATA_LENGTH,MAX_DATA_LENGTH,DATA_FREE,TABLE_COLLATION," +
                "CREATE_OPTIONS,TABLE_COMMENT,TABLE_SCHEMA  from information_schema.tables where table_schema in (?) and TABLE_ROWS is not null";

        String newSql = String.format(sql.replace("?", "%s"), name);
        List<TableType> list = super.toList(new TableType(), newSql);
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
        List<TableType> list = CommonDataBaseType.CON_DATABASE_TABLE_MAP.get(dataBase).stream().filter(v -> {
            String result = v.getTableName();
            int index = result.indexOf("[");
            result = result.substring(0, index);
            return tableName.equals(result);
        }).collect(Collectors.toList());
        if (!list.isEmpty()) {
            TableType tableType = list.get(0);
            stringBuilder.append("<html>").append(HtmlUtils.getHtml("表名")).append(StringUtil.StringEqual(tableType.getTableName()));
            stringBuilder.append(HtmlUtils.getHtml("行")).append(StringUtil.StringEqual(tableType.getTableRows()));
            stringBuilder.append(HtmlUtils.getHtml("引擎")).append(StringUtil.StringEqual(tableType.getEngine()));
            stringBuilder.append(HtmlUtils.getHtml("自动递增")).append(StringUtil.StringEqual(tableType.getAutoIncrement()));
            stringBuilder.append(HtmlUtils.getHtml("行格式")).append(StringUtil.StringEqual(tableType.getRowFormat()));
            stringBuilder.append(HtmlUtils.getHtml("修改日期")).append(StringUtil.StringEqual(tableType.getUpdateTime()));
            stringBuilder.append(HtmlUtils.getHtml("创建日期")).append(StringUtil.StringEqual(tableType.getCreateTime()));
            stringBuilder.append(HtmlUtils.getHtml("检查时间")).append(StringUtil.StringEqual(tableType.getCheckTime()));
            stringBuilder.append(HtmlUtils.getHtml("索引长度")).append(StringUtil.StringEqual(tableType.getIndexLength()));
            stringBuilder.append(HtmlUtils.getHtml("数据长度")).append(StringUtil.StringEqual(tableType.getDataLength()));
            stringBuilder.append(HtmlUtils.getHtml("最大数据长度")).append(StringUtil.StringEqual(tableType.getMaxDataLength()));
            stringBuilder.append(HtmlUtils.getHtml("数据可用空间")).append(StringUtil.StringEqual(tableType.getDataFree()));
            stringBuilder.append(HtmlUtils.getHtml("排序规则")).append(StringUtil.StringEqual(tableType.getTableCollation()));
            stringBuilder.append(HtmlUtils.getHtml("创建选项")).append(StringUtil.StringEqual(tableType.getCreateOptions()));
            stringBuilder.append(HtmlUtils.getHtml("注释")).append(StringUtil.StringEqual(tableType.getTableComment())).append("</html>");
        }
        return stringBuilder.toString();
    }

}
