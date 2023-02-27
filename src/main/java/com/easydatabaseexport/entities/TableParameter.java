package com.easydatabaseexport.entities;

import com.easydatabaseexport.ui.export.config.Export;
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
    @Export(name = "序号")
    private String no;
    /**
     * 字段名
     **/
    @Export(name = "字段名")
    private String columnName;
    /**
     * 类型
     **/
    @Export(name = "类型")
    private String columnType;
    /**
     * 长度
     **/
    @Export(name = "长度")
    private String length;
    /**
     * 是否为空
     **/
    @Export(name = "是否为空")
    private String isNullAble;
    /**
     * 默认值
     **/
    @Export(name = "默认值")
    private String columnDefault;
    /**
     * 小数位
     **/
    @Export(name = "小数位")
    private String decimalPlaces;
    /**
     * 注释
     **/
    @Export(name = "注释")
    private String columnComment;

}
