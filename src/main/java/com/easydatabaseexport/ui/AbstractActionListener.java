package com.easydatabaseexport.ui;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.core.DataResult;
import com.easydatabaseexport.entities.DataBaseParameter;
import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.enums.ConfigEnum;
import com.easydatabaseexport.enums.YesNoEnum;
import com.easydatabaseexport.factory.DataBaseAssemblyFactory;
import com.easydatabaseexport.factory.assembly.impl.ConDatabaseModeTableImpl;
import com.easydatabaseexport.ui.component.JCheckBoxTree;
import lombok.SneakyThrows;

import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AbstractActionListener
 *
 * @author lzy
 * @date 2022/7/28 11:39
 **/
public abstract class AbstractActionListener {

    /**
     * 导出excel 集合
     **/
    public static List<TableParameter> exportList = new ArrayList<>();
    /**
     * 导出word 表信息 集合
     **/
    public static Map<String, List<TableParameter>> listMap = new LinkedHashMap<>();
    /**
     * 导出word 索引 集合
     **/
    public static Map<String, List<IndexInfoVO>> indexMap = new HashMap<>(0);

    /**
     * 是否处理处理数据
     **/
    public static boolean isProcess = false;
    public static boolean selectMode = false;
    public static int i = 2;
    public static int j = 1;

    /**
     * 定义每个线程处理多少表
     **/
    private static final Integer pageSize = 20;

    private void getProcess(JCheckBoxTree.CheckNode node) {
        TreeNode[] nodes = node.getPath();
        if (nodes.length < 3) {
            JOptionPane.showMessageDialog(null, "数据库[" + nodes[1] + "]中，不存在任何表", "错误", JOptionPane.ERROR_MESSAGE);
            isProcess = false;
        } else {
            isProcess = true;
        }
        selectMode = DataBaseAssemblyFactory.get(CommonConstant.DATA_BASE_TYPE) instanceof ConDatabaseModeTableImpl;
        if (selectMode) {
            i = 3;
            j = 2;
        }
    }

    @SneakyThrows
    public void generateExportList(DataResult dataResult, JCheckBoxTree.CheckNode node) {
        getProcess(node);
        if (isProcess) {
            TreeNode[] nodes = node.getPath();
            TableParameter t = new TableParameter();
            t.setColumnName(nodes[i].toString()).setColumnComment("isTableNameBlank");
            exportList.add(t);
            int index = nodes[i].toString().lastIndexOf("[");
            String str = nodes[i].toString().substring(0, index);
            DataBaseParameter tableParameter = new DataBaseParameter();
            tableParameter.setDatabaseName(nodes[j].toString());
            tableParameter.setTableName(str);
            if (selectMode) {
                exportList.addAll(dataResult.getTableStructureByKeyForMode(nodes[j].toString(), str, nodes[1].toString()));
            } else {
                exportList.addAll(dataResult.getTableStructureByKey(nodes[j].toString(), str));
            }
        }
    }

    @SneakyThrows
    public void generateExportListMap(DataResult dataResult, JCheckBoxTree.CheckNode node) {
        getProcess(node);
        if (isProcess) {
            TreeNode[] nodes = node.getPath();
            int index = nodes[i].toString().lastIndexOf("[");
            String str = nodes[i].toString().substring(0, index);
            if (selectMode) {
                listMap.put(nodes[j].toString() + "---" + nodes[i].toString(), dataResult.getTableStructureByKeyForMode(nodes[j].toString(), str, nodes[1].toString()));
            } else {
                listMap.put(nodes[j].toString() + "---" + nodes[i].toString(), dataResult.getTableStructureByKey(nodes[j].toString(), str));
            }
            CommonConstant.checkConfigIniFile();
            if (CommonConstant.configMap.containsKey(ConfigEnum.INDEX.getKey())) {
                if (YesNoEnum.YES_1.getValue().equals(CommonConstant.configMap.get(ConfigEnum.INDEX.getKey()))) {
                    if (selectMode) {
                        indexMap.put(nodes[j].toString() + "---" + str, dataResult.getIndexByKeyForMode(nodes[j].toString(), str, nodes[1].toString()));
                    } else {
                        indexMap.put(nodes[j].toString() + "---" + str, dataResult.getIndexByKey(nodes[j].toString(), str));
                    }
                }
            }
        }
    }

    /**
     * 计算出需要多少个线程
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> splitList(List<T> list) {
        int listSize = list.size();
        int page = (listSize + (pageSize - 1)) / pageSize;
        List<List<T>> listArray = new ArrayList<List<T>>();
        for (int i = 0; i < page; i++) {
            List<T> subList = new ArrayList<T>();
            for (int j = 0; j < listSize; j++) {
                int pageIndex = ((j + 1) + (pageSize - 1)) / pageSize;
                if (pageIndex == (i + 1)) {
                    subList.add(list.get(j));
                }
                if ((j + 1) == ((j + 1) * pageSize)) {
                    break;
                }
            }
            listArray.add(subList);
        }
        return listArray;
    }

}
