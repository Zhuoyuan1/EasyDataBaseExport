package com.easydatabaseexport.factory.assembly.impl;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.core.DataResult;
import com.easydatabaseexport.entities.TableTypeForMode;
import com.easydatabaseexport.enums.RightMenu;
import com.easydatabaseexport.factory.DataBaseFactory;
import com.easydatabaseexport.factory.assembly.DataBaseAssembly;
import com.easydatabaseexport.ui.menu.ConnectMenus;
import com.easydatabaseexport.ui.menu.PatternMenus;
import com.easydatabaseexport.ui.menu.TableMenus;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        list.add(new ImageIcon(ConDatabaseModeTableImpl.class.getResource("/images/" + CommonConstant.DATA_BASE_TYPE.toLowerCase() + ".png")));
        list.add(new ImageIcon(ConDatabaseModeTableImpl.class.getResource("/images/pattern.png")));
        list.add(new ImageIcon(ConDatabaseModeTableImpl.class.getResource("/images/table.png")));
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

    @Override
    public Map<String, JPopupMenu> getMenuList(JTree jtree) {
        Map<String, JPopupMenu> map = new HashMap<>();
        map.put(RightMenu.connect.name(), new ConnectMenus(jtree));
        map.put(RightMenu.pattern.name(), new PatternMenus(jtree));
        map.put(RightMenu.table.name(), new TableMenus(jtree));
        return map;
    }
}
