package com.easydatabaseexport.entities;

import lombok.Data;

/**
 * TableTypeForMode
 *
 * @author lzy
 * @date 2022/3/17 14:02
 **/

@Data
public class TableTypeForMode {
    /**
     * 库名（虚拟字段，用于统计表的分组）
     **/
    private String tableSchema;
    /**
     * 表名
     **/
    private String tableName;
    /**
     * 所有者
     **/
    private String owner;
    /**
     * 行
     **/
    private String numRows;
    /**
     * 表空间名
     **/
    private String tablespaceName;
    /**
     * 状态
     **/
    private String status;
    /**
     * 已生成
     **/
    private String generated;
    /**
     * 群集所有者
     **/
    private String clusterOwner;
    /**
     * 群集名
     **/
    private String clusterName;
    /**
     * IOT名
     **/
    private String iotName;
    /**
     * % 可用
     **/
    private String pctFree;
    /**
     * % 已使用
     **/
    private String pctUsed;
    /**
     * 初始事务
     **/
    private String iniTrans;
    /**
     * 最大事务
     **/
    private String maxTrans;
    /**
     * 下一个区
     **/
    private String nextExtent;
    /**
     * 最小区
     **/
    private String minExtents;
    /**
     * 最大区
     **/
    private String maxExtents;
    /**
     * % 增加
     **/
    private String pctIncrease;
    /**
     * 可用列表
     **/
    private String freelists;
    /**
     * 可用列表组
     **/
    private String freelistGroups;
    /**
     * 记录
     **/
    private String logging;
    /**
     * 已备份
     **/
    private String backedUp;
    /**
     * 块
     **/
    private String blocks;
    /**
     * 空的块
     **/
    private String emptyBlocks;
    /**
     * 平均空间
     **/
    private String avgSpace;
    /**
     * 链数量
     **/
    private String chainCnt;
    /**
     * 平均行长度
     **/
    private String avgRowLen;
    /**
     * 可用列表中全部块的平均可用空间
     **/
    private String avgSpaceFreelistBlocks;
    /**
     * 可用列表中的块数
     **/
    private String numFreelistBlocks;
    /**
     * 度数
     **/
    private String degree;
    /**
     * 实例
     **/
    private String instances;
    /**
     * 缓存
     **/
    private String cache;
    /**
     * 表锁定
     **/
    private String tableLock;
    /**
     * 样本大小
     **/
    private String sampleSize;
    /**
     * 上次分析
     **/
    private String lastAnalyzed;
    /**
     * 分区
     **/
    private String partitioned;
    /**
     * IOT类型
     **/
    private String iotType;
    /**
     * 对象ID类型
     **/
    private String objectIdType;
    /**
     * 表类型所有者
     **/
    private String tableTypeOwner;
    /**
     * 表类型
     **/
    private String tableType;
    /**
     * 暂时
     **/
    private String temporary;
    /**
     * 次要
     **/
    private String secondary;
    /**
     * 嵌套
     **/
    private String nested;
    /**
     * 缓冲池
     **/
    private String bufferPool;
    /**
     * 行移动
     **/
    private String rowMovement;
    /**
     * 全局统计数据
     **/
    private String globalStats;
    /**
     * 用户统计数据
     **/
    private String userStats;
    /**
     * 期间
     **/
    private String duration;
    /**
     * 跳过损坏
     **/
    private String skipCorrupt;
    /**
     * 正在监控
     **/
    private String monitoring;
    /**
     * 依赖性
     **/
    private String dependencies;
    /**
     * 删除表状态
     **/
    private String dropStatus;
    /**
     * 压缩
     **/
    private String compression;
    /**
     * 已删除
     **/
    private String dropped;
    /**
     * 注释
     **/
    private String comments;
}
