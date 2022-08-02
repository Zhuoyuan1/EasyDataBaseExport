package com.easydatabaseexport.entities;

import lombok.Data;

import java.util.List;

/**
 * DataBaseParameter
 *
 * @author lzy
 * @date 2022/7/28 15:31
 **/
@Data
public class DataBaseParameter {
    /**
     * 是否为第一张表
     **/
    private boolean isFirst = false;
    /**
     * 模式（数据库有模式）、库（无模式）
     **/
    private String databaseName;
    /**
     * 表名
     **/
    private String tableName;
    /**
     * 库
     **/
    private String catalog;
    /**
     * 表字段list
     **/
    private List<TableParameter> list;
}
