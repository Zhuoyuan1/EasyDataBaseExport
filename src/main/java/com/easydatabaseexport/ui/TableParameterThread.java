package com.easydatabaseexport.ui;

import com.easydatabaseexport.core.DataResult;
import com.easydatabaseexport.entities.TableParameter;
import lombok.extern.java.Log;

import java.util.List;

/**
 * TableParameterThread
 *
 * @author lzy
 * @date 2022/7/28 15:25
 **/
@Log
public class TableParameterThread implements Runnable {

    private List<TableParameter> dataList;
    private final DataResult dataResult;

    public TableParameterThread(DataResult dataResult, List<TableParameter> dataList) {
        this.dataList = dataList;
        this.dataResult = dataResult;
    }

    public List<TableParameter> getTableParameterList() {
        return dataList;
    }

    public void setTableParameterList(List<TableParameter> dataList) {
        this.dataList = dataList;
    }

    @Override
    public void run() {
        /*if (StringUtil.isEmpty(v.getCatalog())) {
            v.set(dataResult.getTableStructureByKey(v.getDatabaseName(), v.getTableName()));
        } else {
            v.getList().addAll(dataResult.getTableStructureByKeyForMode(v.getDatabaseName(), v.getTableName(), v.getCatalog()));
        }*/
    }
}
