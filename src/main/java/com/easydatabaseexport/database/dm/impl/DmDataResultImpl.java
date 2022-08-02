package com.easydatabaseexport.database.dm.impl;

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
 * DmDataResultImpl
 *
 * @author lzy
 * @date 2022/7/21 10:37
 **/
@Log
public class DmDataResultImpl extends AbstractDataResultImpl implements DataResult {

    @Override
    public JScrollPane getDataCenterInfo(int width, int height) throws SQLException {
        String sql = "SELECT * FROM " + CommonConstant.DATABASE_NAME + "." + CommonConstant.TABLE_NAME;

        PreparedStatement ppst = null;
        ResultSet rs = null;
        ppst = CommonConstant.connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs = ppst.executeQuery();
        //rs相当于一个指针一样---指向了返回的结果集的第一行之前,而在之前已经遍历过到最后了，所以现在需要返回到原先的位置
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
            //"    ALL_TAB_COMMENTS C " +
            "WHERE 1=1 " +
            //"  AND A.TABLE_NAME IN (SELECT U.TABLE_NAME FROM USER_ALL_TABLES U) " +
            //"  AND A.OWNER = B.OWNER " +
            "  AND A.TABLE_NAME = B.TABLE_NAME " +
            "  AND A.COLUMN_NAME = B.COLUMN_NAME " +
            //"  AND C.TABLE_NAME = A.TABLE_NAME " +
            //"  AND C.OWNER = A.OWNER " +
            "  AND A.SCHEMA_NAME = '%s'  " +
            "  AND B.owner = '%s'  " +
            "  AND A.TABLE_NAME = '%s'  " +
            "ORDER BY A.TABLE_NAME, B.COLUMN_ID ";

    @SneakyThrows
    @Override
    public void initList() {
        CommonConstant.tableParameterList.clear();
        String newSql = String.format(sql, CommonConstant.DATABASE_NAME, CommonConstant.DATABASE_NAME, CommonConstant.TABLE_NAME);
        CommonConstant.tableParameterList.addAll(super.toList(new TableParameter(), newSql));
    }

    @Override
    public List<TableParameter> getTableStructureByKey(String databaseName, String tableName) throws Exception {
        String newSql = String.format(sql, databaseName, databaseName, tableName);
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
        String sql = "SP_TABLEDEF('" + CommonConstant.DATABASE_NAME + "','" + CommonConstant.TABLE_NAME + "')";
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
            if (StringUtils.isNullOrEmpty(CommonConstant.DATABASE_NAME)) {
                rs = stmt.executeQuery("SELECT DISTINCT object_name FROM ALL_OBJECTS WHERE OBJECT_TYPE = 'SCH' ");
            } else {
                rs = stmt.executeQuery("SELECT DISTINCT object_name FROM ALL_OBJECTS WHERE OBJECT_TYPE = 'SCH' AND object_name = '" + CommonConstant.DATABASE_NAME + "'");
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
                " I.UNIQUENESS " +
                //" IE.COLUMN_EXPRESSION " +
                "FROM " +
                " SYS.DBA_INDEXES I, " +
                " SYS.DBA_IND_COLUMNS IC " +
                //" SYS.DBA_IND_EXPRESSIONS IE " +
                "WHERE " +
                "  IC.INDEX_OWNER ( + ) = I.OWNER  " +
                " AND IC.INDEX_NAME ( + ) = I.INDEX_NAME  " +
                " AND I.INDEX_TYPE != 'LOB'  " +
                " AND I.INDEX_TYPE != 'DOMAIN'  " +
                " AND I.INDEX_TYPE != 'CLUSTER'  " +
                //" AND IE.INDEX_OWNER ( + ) = IC.INDEX_OWNER  " +
                //" AND IE.INDEX_NAME ( + ) = IC.INDEX_NAME  " +
                //" AND IE.COLUMN_POSITION ( + ) = IC.COLUMN_POSITION  " +
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
                indexInfo.setColumnName(StringUtil.StringEqual(rs.getString(7)) + " " + rs.getString(8));
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
        String sql = "SELECT *  FROM ALL_TABLES A " +
                "LEFT JOIN (SELECT * FROM ALL_TAB_COMMENTS WHERE TABLE_TYPE = 'TABLE') B ON A.OWNER = B.OWNER AND A.TABLE_NAME = B.TABLE_NAME " +
                "WHERE A.OWNER IN (?) ";

        String newSql = String.format(sql.replace("?", "%s"), name);

        List<TableType> tlist = new ArrayList<>();
        List<TableTypeForMode> list = super.toList(new TableTypeForMode(), newSql);
        list.forEach(v -> {
            v.setTableSchema(v.getOwner());
            v.setTableName(StringUtil.StringEqual(v.getTableName()) + "[" + StringUtil.StringEqual(v.getComments()) + "]");
            TableType tableType = new TableType();
            tableType.setTableName(v.getTableName());
            tableType.setTableComment(StringUtil.StringEqual(v.getComments()));
            tableType.setTableSchema(StringUtil.StringEqual(v.getOwner()));
            tlist.add(tableType);
        });
        CommonDataBaseType.CON_DATABASE_TABLE_MAP = tlist.parallelStream().collect(Collectors.groupingBy(TableType::getTableSchema));
        CommonDataBaseType.CON_MODE_TABLE_MAP = list.parallelStream().collect(Collectors.groupingBy(TableTypeForMode::getTableSchema));
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
        if (CommonConstant.tableParameterList.isEmpty()) {
            CommonConstant.CENTERS.validate();
            CommonConstant.CENTERS.repaint();
            return;
        }
        JScrollPane jTable = getCenterInfo(CommonConstant.tableParameterList);
        jTable.setSize(CommonConstant.CENTERS.getWidth(), CommonConstant.CENTERS.getHeight());
        CommonConstant.CENTERS.add(jTable);
        CommonConstant.CENTERS.validate();
        CommonConstant.CENTERS.repaint();
    }

}
