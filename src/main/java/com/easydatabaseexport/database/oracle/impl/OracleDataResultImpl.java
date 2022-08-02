package com.easydatabaseexport.database.oracle.impl;

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
import com.easydatabaseexport.factory.DataSourceFactory;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.ui.detector.ConnectDetector;
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
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * OracleDataResultImpl
 *
 * @author lzy
 * @date 2021/11/1 15:35
 **/
@Log
public class OracleDataResultImpl extends AbstractDataResultImpl implements DataResult {
    @Override
    public JScrollPane getDataCenterInfo(int width, int height) throws SQLException {

        String sql = "select * from " + CommonConstant.DATABASE_NAME + "." + CommonConstant.TABLE_NAME;

        PreparedStatement ppst = null;
        ResultSet rs = null;

        ppst = CommonConstant.connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs = ppst.executeQuery();
        //rs相当于一个指针一样---指向了返回的结果集的第一行之前,而在之前已经遍历过到最后了，所以现在需要返回到原先的位置
        rs.beforeFirst();

        DefaultTableModel tableModel = new DefaultTableModel();
        JTable table = new JTable(tableModel);
        ResultSetMetaData metaData = rs.getMetaData();
        // 字段名
        Vector<String> columnNames = new Vector<String>();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnName(i));
        }

        // 表数据
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
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);// 水平滚动条
        JScrollPane scroll = new JScrollPane(table);
        scroll.setSize(width, height);
        return scroll;
    }

    private final static String sql = "SELECT " +
            "    B.COLUMN_NAME, " +
            "    B.DATA_TYPE as COLUMN_TYPE, " +
            "    nvl(B.DATA_PRECISION, B.CHAR_LENGTH) as LENGTH, " +
            "    B.NULLABLE as IS_NULL_ABLE, " +
            "    B.DATA_DEFAULT as COLUMN_DEFAULT, " +
            "    B.DATA_SCALE as DECIMAL_PLACES, " +
            "    A.COMMENTS as COLUMN_COMMENT " +
            " FROM " +
            "    ALL_COL_COMMENTS A, " +
            "    ALL_TAB_COLUMNS B " +
            "WHERE 1=1 " +
            "  AND A.OWNER = B.OWNER " +
            "  AND A.TABLE_NAME = B.TABLE_NAME " +
            "  AND A.COLUMN_NAME = B.COLUMN_NAME " +
            "  AND A.OWNER = '%s'  " +
            "  AND A.TABLE_NAME = '%s'  " +
            "ORDER BY A.TABLE_NAME, B.COLUMN_ID ";

    @SneakyThrows
    @Override
    public void initList() {
        CommonConstant.tableParameterList.clear();
        String newSql = String.format(sql, CommonConstant.DATABASE_NAME, CommonConstant.TABLE_NAME);
        CommonConstant.tableParameterList.addAll(toList(new TableParameter(), newSql));
    }

    @Override
    public List<TableParameter> getTableStructureByKey(String databaseName, String tableName) throws Exception {
        String newSql = String.format(sql, databaseName, tableName);
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

    private String getDdlInfo() {
        StringBuilder str = new StringBuilder();
        String sql = "SELECT DBMS_METADATA.GET_DDL('TABLE', '" + CommonConstant.TABLE_NAME + "', '" + CommonConstant.DATABASE_NAME + "') AS DDL FROM DUAL";
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
                str.append(StringUtil.StringEqual(rs.getString(1)));
            }
        } catch (SQLException e) {
            LogManager.writeLogFile(e, log);
        }
        return str.toString();
    }

    //执行数据查询器
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
        String sql = "SELECT count(1) " +
                "FROM SYS.ALL_TAB_COLUMNS C, SYS.ALL_COL_COMMENTS COM " +
                "WHERE COM.OWNER(+) = C.OWNER AND COM.TABLE_NAME(+) = " +
                "C.TABLE_NAME AND COM.COLUMN_NAME(+) = C.COLUMN_NAME AND C.OWNER = '" + dataBase + "' AND " +
                "C.TABLE_NAME = '" + tableName + "'" + "ORDER BY C.TABLE_NAME, C.COLUMN_ID ASC ";
        ResultSet rs = null;
        Map<String, ErrorMsg> map = new HashMap<>(0);
        ErrorMsg msg = new ErrorMsg();
        try {
            PreparedStatement ppst = CommonConstant.connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = ppst.executeQuery();
            rs.last();
        } catch (SQLException e) {
            msg.setMessage("操作失败，原因" + e.getMessage()).setTitle("错误").setMessageType(JOptionPane.ERROR_MESSAGE);
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
            ResultSet rs = stmt.executeQuery("SELECT USERNAME FROM SYS.ALL_USERS ORDER BY USERNAME");
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
        String sql = "SELECT " +
                " I.TABLE_NAME, " +
                " I.INDEX_NAME, " +
                " I.OWNER, " +
                " I.INDEX_TYPE, " +
                " I.STATUS, " +
                " I.TABLE_OWNER, " +
                " IC.COLUMN_NAME, " +
                " IC.DESCEND, " +
                " I.TABLE_TYPE, " +
                " I.UNIQUENESS, " +
                " IE.COLUMN_EXPRESSION " +
                "FROM " +
                " SYS.DBA_INDEXES I, " +
                " SYS.DBA_IND_COLUMNS IC, " +
                " SYS.DBA_IND_EXPRESSIONS IE " +
                "WHERE " +
                "  IC.INDEX_OWNER ( + ) = I.OWNER  " +
                " AND IC.INDEX_NAME ( + ) = I.INDEX_NAME  " +
                " AND I.INDEX_TYPE != 'LOB'  " +
                " AND I.INDEX_TYPE != 'DOMAIN'  " +
                " AND I.INDEX_TYPE != 'CLUSTER'  " +
                " AND IE.INDEX_OWNER ( + ) = IC.INDEX_OWNER  " +
                " AND IE.INDEX_NAME ( + ) = IC.INDEX_NAME  " +
                " AND IE.COLUMN_POSITION ( + ) = IC.COLUMN_POSITION  " +
                " AND I.OWNER = '" + dataBase + "'  " +
                " AND I.TABLE_NAME = '" + tableName + "'  " +
                "ORDER BY " +
                " I.INDEX_NAME, " +
                " I.TABLE_NAME ASC, " +
                " IC.COLUMN_POSITION ASC ";
        List<IndexInfo> indexList = new ArrayList<>();
        List<IndexInfoVO> voList = new ArrayList<>();
        ResultSet rs = getResultSetBySql(sql);

        //获取返回的ResultSet内容
        try {
            IndexInfo indexInfo = null;
            while (rs.next()) {
                indexInfo = new IndexInfo();
                indexInfo.setTable(StringUtil.StringEqual(rs.getString(1)));
                indexInfo.setNonUnique(StringUtil.StringEqual(rs.getString(10)));
                indexInfo.setKeyName(StringUtil.StringEqual(rs.getString(2)));
                indexInfo.setIndexType(StringUtil.StringEqual(rs.getString(4)));

                indexInfo.setCollation(StringUtil.StringEqual(rs.getString(6)));
                //indexInfo.setCardinality(StringUtil.StringEqual(rs.getString(11)));
                if (Objects.isNull(rs.getString(11))) {
                    indexInfo.setColumnName(StringUtil.StringEqual(rs.getString(7)) + " " + rs.getString(8));
                } else {
                    indexInfo.setColumnName(StringUtil.StringEqual(rs.getString(11)) + " " + rs.getString(8));
                }
                indexInfo.setSubPart(StringUtil.StringEqual(rs.getString(8)));
                indexInfo.setPacked(StringUtil.StringEqual(rs.getString(9)));
                indexInfo.setNull(StringUtil.StringEqual(rs.getString(10)));
                //indexInfo.setIndexComment(StringUtil.StringEqual(rs.getString(2)));
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
                    if (!"NONUNIQUE".equals(list.get(0).getNonUnique())) {
                        vo.setIndexType("UNIQUE");
                        vo.setIndexMethod(list.get(0).getIndexType());
                    } else {
                        vo.setIndexType(list.get(0).getIndexType());
                        //vo.setIndexMethod(list.get(0).getIndexType());
                    }
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

    private void getTableInfoAndStructure(String name) throws SQLException {
        String sql = "SELECT T.TABLE_NAME, T.OWNER, T.NUM_ROWS, T.TABLESPACE_NAME, " +
                "( SELECT STATUS FROM ALL_OBJECTS O WHERE O.OWNER = T.OWNER AND O.OBJECT_NAME = T.TABLE_NAME AND OBJECT_TYPE = 'TABLE' ) STATUS," +
                " ( SELECT GENERATED FROM ALL_OBJECTS O WHERE O.OWNER = T.OWNER AND O.OBJECT_NAME = T.TABLE_NAME AND OBJECT_TYPE = 'TABLE' ) GENERATED, " +
                "T.CLUSTER_OWNER, T.CLUSTER_NAME, T.IOT_NAME, T.PCT_FREE, T.PCT_USED, T.INI_TRANS, T.MAX_TRANS, T.NEXT_EXTENT, T.MIN_EXTENTS, " +
                "T.MAX_EXTENTS, T.PCT_INCREASE, T.FREELISTS, T.FREELIST_GROUPS, T.LOGGING, T.BACKED_UP, T.BLOCKS, T.EMPTY_BLOCKS, " +
                "T.AVG_SPACE, T.CHAIN_CNT, T.AVG_ROW_LEN, T.AVG_SPACE_FREELIST_BLOCKS, T.NUM_FREELIST_BLOCKS, T.DEGREE, T.INSTANCES, " +
                "T.CACHE, T.TABLE_LOCK, T.SAMPLE_SIZE, T.LAST_ANALYZED, T.PARTITIONED, T.IOT_TYPE, T.OBJECT_ID_TYPE, T.TABLE_TYPE_OWNER, " +
                "T.TABLE_TYPE, T.TEMPORARY, T.SECONDARY, T.NESTED, T.BUFFER_POOL, T.ROW_MOVEMENT, T.GLOBAL_STATS, T.USER_STATS, " +
                "T.DURATION, T.SKIP_CORRUPT, T.MONITORING, T.DEPENDENCIES, T.STATUS DROP_STATUS, T.COMPRESSION, T.DROPPED, " +
                "TC.COMMENTS as COMMENTS FROM SYS.ALL_ALL_TABLES T, SYS.ALL_EXTERNAL_TABLES ET, SYS.ALL_TAB_COMMENTS TC WHERE T.IOT_NAME IS NULL " +
                "AND T.NESTED = 'NO' AND T.SECONDARY = 'N' AND NOT EXISTS (SELECT 1 FROM SYS.ALL_MVIEWS MV WHERE MV.OWNER = T.OWNER " +
                "AND MV.MVIEW_NAME = T.TABLE_NAME) AND TC.OWNER(+) = T.OWNER AND TC.TABLE_NAME(+) = T.TABLE_NAME " +
                "AND ET.TABLE_NAME(+) = T.TABLE_NAME AND ET.OWNER(+) = T.OWNER AND T.OWNER in (?) ORDER BY T.TABLE_NAME ASC";

        String newSql = String.format(sql.replace("?", "%s"), name);

        List<TableType> list = new ArrayList<>();
        List<TableTypeForMode> oracleList = toList(new TableTypeForMode(), newSql);
        oracleList.forEach(v -> {
            v.setTableSchema(v.getOwner());
            v.setTableName(StringUtil.StringEqual(v.getTableName()) + "[" + StringUtil.StringEqual(v.getComments()) + "]");
            TableType tableType = new TableType();
            tableType.setTableName(v.getTableName());
            tableType.setTableComment(StringUtil.StringEqual(v.getComments()));
            tableType.setTableSchema(StringUtil.StringEqual(v.getOwner()));
            list.add(tableType);
        });
        CommonDataBaseType.CON_DATABASE_TABLE_MAP = list.parallelStream().collect(Collectors.groupingBy(TableType::getTableSchema));
        CommonDataBaseType.CON_MODE_TABLE_MAP = oracleList.parallelStream().collect(Collectors.groupingBy(TableTypeForMode::getTableSchema));
    }

    //点击查询
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

    @SneakyThrows
    @Override
    public ResultSet getResultSetBySql(String sql, String... params) {
        //oracle使用PreparedStatement，每创建一个PreparedStatement会打开一个游标，当超过数据库设置最大的游标会报错
        ConnectDetector.dataSource.close(CommonConstant.connection);
        ConnectDetector.dataSource = DataSourceFactory.get(CommonConstant.DATA_BASE_TYPE);
        CommonConstant.connection = ConnectDetector.dataSource
                .getCreateConnection(ConnectDetector.urlText, ConnectDetector.userText, ConnectDetector.passwdText);
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
    public <T> List<T> toList(T t, String sql) throws SQLException {
        ConnectDetector.dataSource.close(CommonConstant.connection);
        ConnectDetector.dataSource = DataSourceFactory.get(CommonConstant.DATA_BASE_TYPE);
        CommonConstant.connection = ConnectDetector.dataSource
                .getCreateConnection(ConnectDetector.urlText, ConnectDetector.userText, ConnectDetector.passwdText);
        PreparedStatement ppst = null;
        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();
        ppst = CommonConstant.connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = ppst.executeQuery();
        while (rs.next()) {
            Map<String, String> resultMap = new HashMap<String, String>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i < columnCount + 1; i++) {
                resultMap.put(metaData.getColumnName(i), rs.getString(i));
            }
            resultList.add(resultMap);
        }
        return (List<T>) JSON.parseArray(JSON.toJSONString(resultList), t.getClass());
    }
}
