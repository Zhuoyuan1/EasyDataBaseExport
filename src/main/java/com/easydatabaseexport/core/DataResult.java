package com.easydatabaseexport.core;

import com.easydatabaseexport.entities.ErrorMsg;
import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DataResult 接口
 *
 * @author lzy
 * @date 2021/11/1 15:27
 **/
public interface DataResult {


    /**
     * 点击时，获取表信息和表结构信息
     *
     * @param tableName 表名
     * @param dataBase  数据库名
     * @return java.lang.String
     * @throws SQLException sql异常
     **/
    String getTableInfoAndStructureByClick(String tableName, String dataBase) throws SQLException;

    /**
     * 获取界面中间的数据
     *
     * @param width  宽
     * @param height 高
     * @return javax.swing.JScrollPane
     * @throws SQLException sql异常
     **/
    JScrollPane getDataCenterInfo(int width, int height) throws SQLException;

    /**
     * 获取界面右侧的数据 常规信息和DDL
     *
     * @param width  宽
     * @param height 高
     * @return javax.swing.JScrollPane
     **/
    JTabbedPane getRightInfo(int width, int height);

    /**
     * 初始化
     **/
    void initList();

    /**
     * 通过key 获取表结构信息
     *
     * @param databaseName 库名
     * @param tableName    表名
     * @return List<TableParameter>
     * @throws Exception sql异常
     **/
    default List<TableParameter> getTableStructureByKey(String databaseName, String tableName) throws Exception {
        return new ArrayList<>();
    }

    /**
     * 通过key 获取表结构信息
     *
     * @param databaseName 模式名
     * @param tableName    表名
     * @param catalog      库名
     * @return List<TableParameter>
     **/
    default List<TableParameter> getTableStructureByKeyForMode(String databaseName, String tableName, String catalog) {
        return new ArrayList<>();
    }

    /**
     * 单击时，中间数据变化
     **/
    void doListValueChanged();

    /**
     * 双击时，中间数据变化
     *
     * @throws SQLException sql异常
     **/
    void doListDataValueChanged() throws SQLException;

    /**
     * 检查表、库是否存在
     *
     * @param tableName    表名
     * @param databaseName 数据库名
     * @return java.util.Map<ErrorMsg>
     * @throws Exception 异常
     **/
    Map<String, ErrorMsg> checkExist(String tableName, String databaseName) throws Exception;

    /**
     * 获取所有数据库名称
     *
     * @return java.util.List<java.lang.String>
     **/
    List<String> getAllDataBaseName();

    /**
     * 根据 库名、表名 获取 所有索引
     *
     * @param dataBase  库名
     * @param tableName 表名
     * @return List<IndexInfoVO>
     */
    default List<IndexInfoVO> getIndexByKey(String dataBase, String tableName) {
        return new ArrayList<>();
    }

    /**
     * 根据 库名、模式、表名 获取 所有索引
     *
     * @param dataBase  模式名
     * @param tableName 表名
     * @param catalog   库名
     * @return List<IndexInfoVO>
     */
    default List<IndexInfoVO> getIndexByKeyForMode(String dataBase, String tableName, String catalog) {
        return new ArrayList<>();
    }
}
