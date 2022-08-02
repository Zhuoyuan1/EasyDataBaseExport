package com.easydatabaseexport.common;

import com.easydatabaseexport.entities.TableType;
import com.easydatabaseexport.entities.TableTypeForMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CommonDataBaseType
 *
 * @author lzy
 * @date 2022/7/21 13:40
 **/
public class CommonDataBaseType {
    /**
     * [连接-库-表](con-database-table)表信息
     * 例：mysql
     **/
    public static Map<String, List<TableType>> CON_DATABASE_TABLE_MAP = new HashMap<>();
    /**
     * [连接-模式-表](con-mode-table)表信息
     * 例：oracle、达梦
     **/
    public static Map<String, List<TableTypeForMode>> CON_MODE_TABLE_MAP = new HashMap<>();
    /**
     * [连接-库-模式-表](con-database-mode-table)表信息
     * 例：sqlServer、postgreSql、kingBase8
     **/
    public static Map<String, Map<String, List<TableType>>> CON_DATABASE_MODE_TABLE_MAP = new HashMap<>();
}
