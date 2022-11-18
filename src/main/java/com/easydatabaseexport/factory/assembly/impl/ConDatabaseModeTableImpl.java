package com.easydatabaseexport.factory.assembly.impl;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.core.DataResult;
import com.easydatabaseexport.entities.TableType;
import com.easydatabaseexport.factory.DataBaseFactory;
import com.easydatabaseexport.factory.assembly.DataBaseAssembly;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ConDatabaseModeTableImpl
 * [连接-库-模式-表](con-database-mode-table)
 *
 * @author lzy
 * @date 2022/7/25 15:03
 **/
public class ConDatabaseModeTableImpl implements DataBaseAssembly {

    @Override
    public List<ImageIcon> image() {
        List<ImageIcon> list = new ArrayList<>(4);
        list.add(new ImageIcon(ConDatabaseModeTableImpl.class.getResource("/images/" + CommonConstant.DATA_BASE_TYPE.toLowerCase() + ".png")));
        list.add(new ImageIcon(ConDatabaseModeTableImpl.class.getResource("/images/database.png")));
        list.add(new ImageIcon(ConDatabaseModeTableImpl.class.getResource("/images/pattern.png")));
        list.add(new ImageIcon(ConDatabaseModeTableImpl.class.getResource("/images/table.png")));
        return list;
    }

    @Override
    public HashMap dataBaseInfo() {
        return new HashMap<String, Map<String, List<TableType>>>(0);
    }

    @Override
    public DataResult dataResult() {
        return DataBaseFactory.get(CommonConstant.DATA_BASE_TYPE);
    }
}
