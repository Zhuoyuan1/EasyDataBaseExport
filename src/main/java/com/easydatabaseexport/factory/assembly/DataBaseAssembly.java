package com.easydatabaseexport.factory.assembly;

import com.easydatabaseexport.core.DataResult;

import javax.swing.ImageIcon;
import java.util.HashMap;
import java.util.List;

/**
 * DataBaseAssemblyList 装配清单
 *
 * @author lzy
 * @date 2022/7/25 14:49
 **/
public interface DataBaseAssembly {
    /**
     * 装配图标
     *
     * @return List<ImageIcon>
     **/
    List<ImageIcon> image();

    /**
     * 装配数据存储中心
     *
     * @return HashMap
     **/
    HashMap dataBaseInfo();

    /**
     * 装配数据处理中心
     *
     * @return DataResult
     **/
    DataResult dataResult();
}
