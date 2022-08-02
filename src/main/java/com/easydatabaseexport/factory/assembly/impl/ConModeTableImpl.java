package com.easydatabaseexport.factory.assembly.impl;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.core.DataResult;
import com.easydatabaseexport.entities.TableTypeForMode;
import com.easydatabaseexport.factory.DataBaseFactory;
import com.easydatabaseexport.factory.assembly.DataBaseAssembly;

import javax.swing.ImageIcon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ConModeTableImpl
 * [连接-模式-表](con-mode-table)
 *
 * @author lzy
 * @date 2022/7/25 15:02
 **/
public class ConModeTableImpl implements DataBaseAssembly {

    @Override
    public List<ImageIcon> image() {
        List<ImageIcon> list = new ArrayList<>(3);
        list.add(new ImageIcon(ConDatabaseModeTableImpl.class.getResource("/" + CommonConstant.DATA_BASE_TYPE.toLowerCase() + ".png")));
        list.add(new ImageIcon(ConDatabaseModeTableImpl.class.getResource("/pattern.png")));
        list.add(new ImageIcon(ConDatabaseModeTableImpl.class.getResource("/table.png")));
        return list;
    }

    @Override
    public HashMap dataBaseInfo() {
        return new HashMap<String, List<TableTypeForMode>>(0);
    }

    @Override
    public DataResult dataResult() {
        return DataBaseFactory.get(CommonConstant.DATA_BASE_TYPE);
    }
}
