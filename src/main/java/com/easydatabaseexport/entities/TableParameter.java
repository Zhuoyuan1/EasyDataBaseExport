package com.easydatabaseexport.entities;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * TableParameter
 *
 * @author lzy
 * @date 2021/2/28 0:12
 **/
@Data
@Accessors(chain = true)
public class TableParameter {
    /**
     * 序号
     **/
    private String no;
    /**
     * 字段名
     **/
    private String columnName;
    /**
     * 数据类型
     **/
    private String columnType;
    /**
     * 字段类型
     **/
    //private String dataType;
    /**
     * 长度
     **/
    private String length;
    /**
     * 是否为空
     **/
    private String isNullAble;
    /**
     * 默认值
     **/
    private String columnDefault;
    /**
     * 小数位
     **/
    private String decimalPlaces;
    /**
     * 注释
     **/
    private String columnComment;

}
