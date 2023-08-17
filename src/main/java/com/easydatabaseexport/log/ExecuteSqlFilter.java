package com.easydatabaseexport.log;

import com.alibaba.druid.filter.logging.Log4jFilter;

/**
 * @description: ExecuteSqlFilter
 * @author: lzy
 * @date: 2023/7/10 15:13
 **/
public class ExecuteSqlFilter extends Log4jFilter {

    public ExecuteSqlFilter() {
        this.setStatementExecutableSqlLogEnable(true);
        this.setResultSetLogEnabled(false);
        this.setConnectionLogEnabled(false);
        this.setDataSourceLogEnabled(false);
        this.setStatementCreateAfterLogEnabled(false);
        this.setStatementPrepareAfterLogEnabled(false);
        this.setStatementPrepareCallAfterLogEnabled(false);
        this.setStatementExecuteAfterLogEnabled(false);
        this.setStatementExecuteQueryAfterLogEnabled(false);
        this.setStatementExecuteUpdateAfterLogEnabled(false);
        this.setStatementExecuteBatchAfterLogEnabled(false);
        this.setStatementCloseAfterLogEnabled(false);
        this.setStatementParameterSetLogEnabled(false);
        this.setStatementParameterClearLogEnable(false);
        this.setStatementLogErrorEnabled(false);
    }
}
