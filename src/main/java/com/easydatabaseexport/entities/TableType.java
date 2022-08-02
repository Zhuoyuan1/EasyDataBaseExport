package com.easydatabaseexport.entities;

import lombok.Data;

/**
 * TableType
 *
 * @author lzy
 * @date 2021/2/27 23:57
 **/
@Data
public class TableType {
    /**
     * 库
     **/
    private String tableCatalog;
    /**
     * objectId
     **/
    private String objectId;
    /**
     * 库名（虚拟字段，用于统计表的分组）
     **/
    private String tableSchema;
    /**
     * 表名
     **/
    private String tableName;
    /**
     * 行
     **/
    private String tableRows;
    /**
     * 引擎
     **/
    private String engine;
    /**
     * 自动递增
     **/
    private String autoIncrement;
    /**
     * 行格式
     **/
    private String rowFormat;
    /**
     * 修改日期
     **/
    private String updateTime;
    /**
     * 创建日期
     **/
    private String createTime;
    /**
     * 检查时间
     **/
    private String checkTime;
    /**
     * 索引长度
     **/
    private String indexLength;
    /**
     * 数据长度
     **/
    private String dataLength;
    /**
     * 最大数据长度
     **/
    private String maxDataLength;
    /**
     * 数据可用空间
     **/
    private String dataFree;
    /**
     * 排序规则
     **/
    private String tableCollation;
    /**
     * 创建选项
     **/
    private String createOptions;
    /**
     * 注释 参数
     **/
    private String tableComment;

}
