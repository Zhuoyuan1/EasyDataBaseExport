package com.easydatabaseexport.entities;

import lombok.Data;

/**
 * TableTypeForSqlite
 *
 * @author lzy
 * @date 2023/3/7 9:11
 **/
@Data
public class TableTypeForSqlite {
    /**
     * 表名
     **/
    private String tableName;
    /**
     * 组
     **/
    private String group;
    /**
     * 有索引
     **/
    private String hasIndexes;
    /**
     * 有触发器
     **/
    private String hasTriggers;
    /**
     * 根页面
     **/
    private String rootPage;
}
