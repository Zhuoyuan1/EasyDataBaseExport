package com.easydatabaseexport.factory.assembly;

import com.easydatabaseexport.core.DataResult;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 获取右键菜单列表
     *
     * @return List<JPopupMenu>
     **/
    Map<String, JPopupMenu> getMenuList(JTree jtree);
}
