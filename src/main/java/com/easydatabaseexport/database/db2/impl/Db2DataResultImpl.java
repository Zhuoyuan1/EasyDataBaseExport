package com.easydatabaseexport.database.db2.impl;

import com.alibaba.fastjson.JSON;
import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.CommonDataBaseType;
import com.easydatabaseexport.core.DataResult;
import com.easydatabaseexport.database.service.AbstractDataResultImpl;
import com.easydatabaseexport.entities.ErrorMsg;
import com.easydatabaseexport.entities.IndexInfo;
import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.entities.TableType;
import com.easydatabaseexport.entities.TableTypeForMode;
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
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Db2DataResultImpl
 *
 * @author lzy
 * @date 2022/7/21 10:37
 **/
@Log
public class Db2DataResultImpl extends AbstractDataResultImpl implements DataResult {

    @Override
    public JScrollPane getDataCenterInfo(int width, int height) throws SQLException {
        String sql = "select * from " + CommonConstant.DATABASE_NAME + "." + CommonConstant.TABLE_NAME;

        PreparedStatement ppst = null;
        ResultSet rs = null;

        ppst = CommonConstant.connection.prepareStatement(sql);
        rs = ppst.executeQuery();
        //rs.beforeFirst();

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

    private static final String sql = "SELECT TABSCHEMA,TABNAME,COLNAME COLUMN_NAME,COLNO,TYPESCHEMA,TYPENAME COLUMN_TYPE,\"LENGTH\"," +
            "\"SCALE\" DECIMAL_PLACES,TYPESTRINGUNITS,STRINGUNITSLENGTH,\"DEFAULT\" COLUMN_DEFAULT,\"NULLS\" IS_NULL_ABLE," +
            "REMARKS COLUMN_COMMENT FROM " +
            " SYSCAT.COLUMNS c " +
            "WHERE TABSCHEMA = '?' " +
            " AND TABNAME = '?' order by COLNO";

    @SneakyThrows
    @Override
    public void initList() {
        CommonConstant.tableParameterList.clear();
        String newSql = String.format(sql.replace("?", "%s"), CommonConstant.DATABASE_NAME, CommonConstant.TABLE_NAME);
        CommonConstant.tableParameterList.addAll(toList(new TableParameter(), newSql));
    }

    @Override
    public List<TableParameter> getTableStructureByKey(String databaseName, String tableName) throws Exception {
        String newSql = String.format(sql.replace("?", "%s"), databaseName, tableName);
        return toList(new TableParameter(), newSql);
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

    @Override
    @SneakyThrows
    public ResultSet getResultSetBySql(String sql, String... params) {
        PreparedStatement ppst = null;
        ppst = CommonConstant.connection.prepareStatement(sql);
        ResultSet rs = null;
        for (int i = 1; i <= params.length; i++) {
            ppst.setString(i, params[i - 1]);
        }
        rs = ppst.executeQuery();
        return rs;
    }

    private long getDB2DDL(String db2lookinfoParams) {
        long opToken = 0;
        try {
            CallableStatement cstmt;
            ResultSet rs;
            cstmt = CommonConstant.connection.prepareCall("CALL SYSPROC.DB2LK_GENERATE_DDL(?, ?)");
            cstmt.setString(1, db2lookinfoParams);
            cstmt.registerOutParameter(2, Types.BIGINT);
            cstmt.executeUpdate();
            opToken = cstmt.getLong(2);
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
        return opToken;

    }

    private String getDdlInfo() {
        StringBuilder str = new StringBuilder();
        String sql = "-e -x -td ; -t " + CommonConstant.DATABASE_NAME + "." + CommonConstant.TABLE_NAME;

        long opToken = getDB2DDL(sql);

        String db2Sql = "SELECT SQL_STMT FROM SYSTOOLS.DB2LOOK_INFO WHERE OP_TOKEN = %s ORDER BY OP_SEQUENCE WITH UR";

        //获取返回的ResultSet内容
        try {
            ResultSet ddlRs = getResultSetBySql(String.format(db2Sql, opToken));
            while (ddlRs.next()) {
                str.append(StringUtil.StringEqual(ddlRs.getString(1)));
            }
            //清理
            CallableStatement clean = CommonConstant.connection.prepareCall("CALL SYSPROC.DB2LK_CLEAN_TABLE(?)");
            clean.setLong(1, opToken);
            clean.executeUpdate();
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
        String sql = "select * from syscat.tables where TABSCHEMA = '" + dataBase + "' and TABNAME = '" + tableName + "'";
        ResultSet rs = null;
        Map<String, ErrorMsg> map = new HashMap<>(0);
        ErrorMsg msg = new ErrorMsg();
        try {
            PreparedStatement ppst = CommonConstant.connection.prepareStatement(sql);
            rs = ppst.executeQuery();
            //rs.last();
            if (rs.next()) {
                map.put(CommonConstant.SUCCESS, msg);
            } else {
                map.put(CommonConstant.FAIL, msg);
            }
            return map;
        } catch (SQLException e) {
            msg.setMessage("操作失败，原因：" + e.getMessage()).setTitle("错误").setMessageType(JOptionPane.ERROR_MESSAGE);
            map.put(CommonConstant.FAIL, msg);
            LogManager.writeLogFile(e, log);
            return map;
        }
    }

    @Override
    public List<String> getAllDataBaseName() {
        List<String> nameList = new LinkedList<String>();
        try {
            //JdbcRowSet jrs = new JdbcRowSetImpl(CommonConstant.connection);
            Statement stmt = CommonConstant.connection.createStatement();
            //执行查询所有数据库操作
            ResultSet rs = null;
            if (StringUtils.isNullOrEmpty(CommonConstant.DATABASE_NAME)) {
                rs = stmt.executeQuery("SELECT SCHEMANAME FROM syscat.SCHEMATA ");
            } else {
                rs = stmt.executeQuery("SELECT SCHEMANAME FROM syscat.SCHEMATA ");
                //rs = stmt.executeQuery("SELECT SCHEMANAME FROM syscat.SCHEMATA where OWNER = '" + ConnectDetector.userText.toUpperCase() + "'");
            }
            //处理表名
            while (rs.next()) {
                nameList.add(rs.getString(1).trim());
            }
            //处理拼接in中的参数
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < nameList.size(); i++) {
                if (i == nameList.size() - 1) {
                    stringBuilder.append("'").append(nameList.get(i).trim()).append("'");
                } else {
                    stringBuilder.append("'").append(nameList.get(i).trim()).append("'").append(",");
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
        String sql = "SELECT TABNAME,UNIQUERULE,INDNAME,IID,COLNAMES,INDEXTYPE,REMARKS " +
                "FROM syscat.INDEXES i WHERE TABSCHEMA = '" + dataBase + "' AND TABNAME = '" + tableName + "'";

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
                indexInfo.setIndexType(StringUtil.StringEqual(rs.getString(6)));
                indexInfo.setComment(StringUtil.StringEqual(rs.getString(7)));
                indexList.add(indexInfo);
            }
            //根据keyName分组
            indexList.forEach(index -> {
                IndexInfoVO vo = new IndexInfoVO();
                vo.setName(index.getKeyName());
                if ("P".equals(index.getNonUnique())) {
                    vo.setIndexMethod("主键");
                } else if ("D".equals(index.getNonUnique())) {
                    vo.setIndexMethod("普通索引");
                } else if ("U".equals(index.getNonUnique())) {
                    vo.setIndexMethod("唯一索引");
                }
                vo.setColumnName(index.getColumnName());
                vo.setIndexType(index.getIndexType());
                vo.setComment(index.getIndexComment());
                voList.add(vo);
            });
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
        return voList;
    }

    private void getTableInfoAndStructure(String name) throws SQLException {
        String sql = "select TABSCHEMA as tableSchema,TABNAME tableName,OWNER,OWNERTYPE,\"TYPE\",STATUS,BASE_TABSCHEMA,BASE_TABNAME," +
                "ROWTYPESCHEMA,ROWTYPENAME,CREATE_TIME,ALTER_TIME,INVALIDATE_TIME,STATS_TIME,COLCOUNT,TABLEID," +
                "TBSPACEID,CARD,NPAGES,MPAGES,FPAGES,NPARTITIONS,NFILES,TABLESIZE,OVERFLOW,TBSPACE,INDEX_TBSPACE," +
                "LONG_TBSPACE,PARENTS,CHILDREN,SELFREFS,KEYCOLUMNS,KEYINDEXID,KEYUNIQUE,CHECKCOUNT,DATACAPTURE," +
                "CONST_CHECKED,PMAP_ID,PARTITION_MODE,LOG_ATTRIBUTE,PCTFREE,APPEND_MODE,REFRESH,REFRESH_TIME," +
                "\"LOCKSIZE\",\"VOLATILE\",ROW_FORMAT,PROPERTY,STATISTICS_PROFILE,COMPRESSION,ROWCOMPMODE,ACCESS_MODE," +
                "CLUSTERED,ACTIVE_BLOCKS,DROPRULE,MAXFREESPACESEARCH,AVGCOMPRESSEDROWSIZE,AVGROWCOMPRESSIONRATIO,AVGROWSIZE," +
                "PCTROWSCOMPRESSED,LOGINDEXBUILD,CODEPAGE,COLLATIONSCHEMA,COLLATIONNAME,COLLATIONSCHEMA_ORDERBY," +
                "COLLATIONNAME_ORDERBY,ENCODING_SCHEME,PCTPAGESSAVED,LAST_REGEN_TIME,SECPOLICYID,PROTECTIONGRANULARITY," +
                "AUDITPOLICYID,AUDITPOLICYNAME,AUDITEXCEPTIONENABLED,\"DEFINER\",ONCOMMIT,LOGGED,ONROLLBACK,LASTUSED,CONTROL," +
                "TEMPORALTYPE,TABLEORG,EXTENDED_ROW_SIZE,PCTEXTENDEDROWS,REMARKS as comments " +
                "from syscat.tables where TABSCHEMA in (?) ";

        String newSql = String.format(sql.replace("?", "%s"), name);
        List<TableType> list = new ArrayList<>();
        List<TableTypeForMode> db2List = toList(new TableTypeForMode(), newSql);
        db2List.forEach(v -> {
            v.setTableName(StringUtil.StringEqual(v.getTableName()) + "[" + StringUtil.StringEqual(v.getComments()) + "]");
            TableType tableType = new TableType();
            tableType.setTableName(v.getTableName());
            tableType.setTableComment(StringUtil.StringEqual(v.getComments()));
            tableType.setTableSchema(StringUtil.StringEqual(v.getTableSchema()));
            list.add(tableType);
        });
        CommonDataBaseType.CON_DATABASE_TABLE_MAP = list.parallelStream().collect(Collectors.groupingBy(TableType::getTableSchema));
        CommonDataBaseType.CON_MODE_TABLE_MAP = db2List.parallelStream().collect(Collectors.groupingBy(TableTypeForMode::getTableSchema));
    }

    @Override
    public <T> List<T> toList(T t, String sql) throws SQLException {
        PreparedStatement ppst = null;
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        ppst = CommonConstant.connection.prepareStatement(sql);
        ResultSet rs = ppst.executeQuery();
        while (rs.next()) {
            Map<String, String> resultMap = new HashMap<String, String>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i < columnCount + 1; i++) {
                resultMap.put(metaData.getColumnLabel(i), Optional.ofNullable(rs.getString(i)).orElse("").trim());
            }
            resultList.add(resultMap);
        }
        return (List<T>) JSON.parseArray(JSON.toJSONString(resultList), t.getClass());
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
        List<TableTypeForMode> list = CommonDataBaseType.CON_MODE_TABLE_MAP.get(dataBase).stream().filter(v -> {
            String result = v.getTableName();
            int index = result.indexOf("[");
            result = result.substring(0, index);
            return tableName.equals(result);
        }).collect(Collectors.toList());
        if (!list.isEmpty()) {
            TableTypeForMode tableType = list.get(0);
            stringBuilder.append("<html>").append(HtmlUtils.getHtml("表名")).append(StringUtil.StringEqual(tableType.getTableName()));
            stringBuilder.append(HtmlUtils.getHtml("所有者")).append(StringUtil.StringEqual(tableType.getOwner()));
            stringBuilder.append(HtmlUtils.getHtml("行")).append(StringUtil.StringEqual(tableType.getNumRows()));
            stringBuilder.append(HtmlUtils.getHtml("表空间名")).append(StringUtil.StringEqual(tableType.getTablespaceName()));
            stringBuilder.append(HtmlUtils.getHtml("状态")).append(StringUtil.StringEqual(tableType.getStatus()));
            stringBuilder.append(HtmlUtils.getHtml("已生成")).append(StringUtil.StringEqual(tableType.getGenerated()));
            stringBuilder.append(HtmlUtils.getHtml("群集所有者")).append(StringUtil.StringEqual(tableType.getClusterOwner()));
            stringBuilder.append(HtmlUtils.getHtml("群集名")).append(StringUtil.StringEqual(tableType.getClusterName()));
            stringBuilder.append(HtmlUtils.getHtml("IOT名")).append(StringUtil.StringEqual(tableType.getIotName()));
            stringBuilder.append(HtmlUtils.getHtml("% 可用")).append(StringUtil.StringEqual(tableType.getPctFree()));
            stringBuilder.append(HtmlUtils.getHtml("% 已使用")).append(StringUtil.StringEqual(tableType.getPctUsed()));
            stringBuilder.append(HtmlUtils.getHtml("初始事务")).append(StringUtil.StringEqual(tableType.getIniTrans()));
            stringBuilder.append(HtmlUtils.getHtml("最大事务")).append(StringUtil.StringEqual(tableType.getMaxTrans()));
            stringBuilder.append(HtmlUtils.getHtml("下一个区")).append(StringUtil.StringEqual(tableType.getNextExtent()));
            stringBuilder.append(HtmlUtils.getHtml("最小区")).append(StringUtil.StringEqual(tableType.getMinExtents()));
            stringBuilder.append(HtmlUtils.getHtml("最大区")).append(StringUtil.StringEqual(tableType.getMaxExtents()));
            stringBuilder.append(HtmlUtils.getHtml("% 增加")).append(StringUtil.StringEqual(tableType.getPctIncrease()));
            stringBuilder.append(HtmlUtils.getHtml("可用列表")).append(StringUtil.StringEqual(tableType.getFreelists()));
            stringBuilder.append(HtmlUtils.getHtml("可用列表组")).append(StringUtil.StringEqual(tableType.getFreelistGroups()));
            stringBuilder.append(HtmlUtils.getHtml("记录")).append(StringUtil.StringEqual(tableType.getLogging()));
            stringBuilder.append(HtmlUtils.getHtml("已备份")).append(StringUtil.StringEqual(tableType.getBackedUp()));
            stringBuilder.append(HtmlUtils.getHtml("块")).append(StringUtil.StringEqual(tableType.getBlocks()));
            stringBuilder.append(HtmlUtils.getHtml("空的块")).append(StringUtil.StringEqual(tableType.getEmptyBlocks()));
            stringBuilder.append(HtmlUtils.getHtml("平均空间")).append(StringUtil.StringEqual(tableType.getAvgSpace()));
            stringBuilder.append(HtmlUtils.getHtml("链数量")).append(StringUtil.StringEqual(tableType.getChainCnt()));
            stringBuilder.append(HtmlUtils.getHtml("平均行长度")).append(StringUtil.StringEqual(tableType.getAvgRowLen()));
            stringBuilder.append(HtmlUtils.getHtml("可用列表中全部块的平均可用空间")).append(StringUtil.StringEqual(tableType.getAvgSpaceFreelistBlocks()));
            stringBuilder.append(HtmlUtils.getHtml("可用列表中的块数")).append(StringUtil.StringEqual(tableType.getNumFreelistBlocks()));
            stringBuilder.append(HtmlUtils.getHtml("度数")).append(StringUtil.StringEqual(tableType.getDegree()));
            stringBuilder.append(HtmlUtils.getHtml("实例")).append(StringUtil.StringEqual(tableType.getInstances()));
            stringBuilder.append(HtmlUtils.getHtml("缓存")).append(StringUtil.StringEqual(tableType.getCache()));
            stringBuilder.append(HtmlUtils.getHtml("表锁定")).append(StringUtil.StringEqual(tableType.getTableLock()));
            stringBuilder.append(HtmlUtils.getHtml("样本大小")).append(StringUtil.StringEqual(tableType.getSampleSize()));
            stringBuilder.append(HtmlUtils.getHtml("上次分析")).append(StringUtil.StringEqual(tableType.getLastAnalyzed()));
            stringBuilder.append(HtmlUtils.getHtml("分区")).append(StringUtil.StringEqual(tableType.getPartitioned()));
            stringBuilder.append(HtmlUtils.getHtml("IOT类型")).append(StringUtil.StringEqual(tableType.getIotType()));
            stringBuilder.append(HtmlUtils.getHtml("对象ID类型")).append(StringUtil.StringEqual(tableType.getObjectIdType()));
            stringBuilder.append(HtmlUtils.getHtml("表类型所有者")).append(StringUtil.StringEqual(tableType.getTableTypeOwner()));
            stringBuilder.append(HtmlUtils.getHtml("表类型")).append(StringUtil.StringEqual(tableType.getTableType()));
            stringBuilder.append(HtmlUtils.getHtml("暂时")).append(StringUtil.StringEqual(tableType.getTemporary()));
            stringBuilder.append(HtmlUtils.getHtml("次要")).append(StringUtil.StringEqual(tableType.getSecondary()));
            stringBuilder.append(HtmlUtils.getHtml("嵌套")).append(StringUtil.StringEqual(tableType.getNested()));
            stringBuilder.append(HtmlUtils.getHtml("缓冲池")).append(StringUtil.StringEqual(tableType.getBufferPool()));
            stringBuilder.append(HtmlUtils.getHtml("行移动")).append(StringUtil.StringEqual(tableType.getRowMovement()));
            stringBuilder.append(HtmlUtils.getHtml("全局统计数据")).append(StringUtil.StringEqual(tableType.getGlobalStats()));
            stringBuilder.append(HtmlUtils.getHtml("用户统计数据")).append(StringUtil.StringEqual(tableType.getUserStats()));
            stringBuilder.append(HtmlUtils.getHtml("期间")).append(StringUtil.StringEqual(tableType.getDuration()));
            stringBuilder.append(HtmlUtils.getHtml("跳过损坏")).append(StringUtil.StringEqual(tableType.getSkipCorrupt()));
            stringBuilder.append(HtmlUtils.getHtml("正在监控")).append(StringUtil.StringEqual(tableType.getMonitoring()));
            stringBuilder.append(HtmlUtils.getHtml("依赖性")).append(StringUtil.StringEqual(tableType.getDependencies()));
            stringBuilder.append(HtmlUtils.getHtml("删除表状态")).append(StringUtil.StringEqual(tableType.getDropStatus()));
            stringBuilder.append(HtmlUtils.getHtml("压缩")).append(StringUtil.StringEqual(tableType.getCompression()));
            stringBuilder.append(HtmlUtils.getHtml("已删除")).append(StringUtil.StringEqual(tableType.getDropped()));
            stringBuilder.append(HtmlUtils.getHtml("注释")).append(StringUtil.StringEqual(tableType.getComments()));
            stringBuilder.append("</html>");
        }
        return stringBuilder.toString();
    }
}
