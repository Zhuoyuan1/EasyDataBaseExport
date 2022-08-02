package com.easydatabaseexport.entities;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * IndexInfo 索引信息
 *
 * @author lzy
 * @date 2021/12/20
 */
@Data
@Accessors(chain = true)
public class IndexInfo {
    /**
     * 表的名称。
     */
    private String table;
    /**
     * 主键。
     */
    private String isPk;
    /**
     * 如果索引不能包括重复词，则为0。如果可以，则为1。
     */
    private String nonUnique;
    /**
     * 索引的名称。
     */
    private String keyName;
    /**
     * 索引中的列序列号，从1开始。
     */
    private String seqInIndex;
    /**
     * 列名称。
     */
    private String columnName;
    /**
     * 列以什么方式存储在索引中。在MySQL中，有值‘A’（升序）或NULL（无分类）。
     */
    private String collation;
    /**
     * 索引中唯一值的数目的估计值。通过运行ANALYZE TABLE或myisamchk -a可以更新。基数根据被存储为整数的统计数据来计数，所以即使对于小型表，该值也没有必要是精确的。基数越大，当进行联合时，MySQL使用该索引的机 会就越大。
     */
    private String cardinality;
    /**
     * 如果列只是被部分地编入索引，则为被编入索引的字符的数目。如果整列被编入索引，则为NULL。
     */
    private String subPart;
    /**
     * 指示关键字如何被压缩。如果没有被压缩，则为NULL。
     */
    private String packed;
    /**
     * 如果列含有NULL，则含有YES。如果没有，则该列含有NO。
     */
    private String Null;
    /**
     * 用过的索引方法（BTREE, FULLTEXT, HASH, RTREE）。
     */
    private String indexType;
    /**
     * 顺序。
     */
    private String sortType;
    /**
     * 注释
     */
    private String comment;
    /**
     * 索引注释
     */
    private String indexComment;
}
