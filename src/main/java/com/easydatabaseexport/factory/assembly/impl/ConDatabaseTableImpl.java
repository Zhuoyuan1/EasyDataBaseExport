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

/**
 * ConDatabaseTableImpl
 * [连接-库-表](con-database-table)
 *
 * @author lzy
 * @date 2022/7/25 15:02
 **/
public class ConDatabaseTableImpl implements DataBaseAssembly {

    @Override
    public List<ImageIcon> image() {
        List<ImageIcon> list = new ArrayList<>(3);
        list.add(new ImageIcon(ConDatabaseModeTableImpl.class.getResource("/images/" + CommonConstant.DATA_BASE_TYPE.toLowerCase() + ".png")));
        list.add(new ImageIcon(ConDatabaseModeTableImpl.class.getResource("/images/database.png")));
        list.add(new ImageIcon(ConDatabaseModeTableImpl.class.getResource("/images/table.png")));
        return list;
    }

    @Override
    public HashMap dataBaseInfo() {
        return new HashMap<String, List<TableType>>(0);
    }

    @Override
    public DataResult dataResult() {
        return DataBaseFactory.get(CommonConstant.DATA_BASE_TYPE);
    }
}
