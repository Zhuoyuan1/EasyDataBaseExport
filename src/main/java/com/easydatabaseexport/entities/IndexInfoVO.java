package com.easydatabaseexport.entities;

import com.easydatabaseexport.ui.export.config.Export;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 索引导出信息
 *
 * @author lzy
 * @date 2021/12/20
 */
@Data
@Accessors(chain = true)
public class IndexInfoVO {
    /**
     * 索引名称
     */
    @Export(name = "名称")
    private String name;
    /**
     * 字段
     */
    @Export(name = "字段")
    private String columnName;
    /**
     * 索引类型
     */
    @Export(name = "索引类型")
    private String indexType;
    /**
     * 索引方法
     */
    @Export(name = "索引方法")
    private String indexMethod;
    /**
     * 注释
     */
    @Export(name = "注释")
    private String comment;
}
