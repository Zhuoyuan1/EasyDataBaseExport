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
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.ui.component.JCheckBoxTree;
import com.easydatabaseexport.ui.component.ThreadDiag;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

/**
 * AbstractActionListener
 *
 * @author lzy
 * @date 2022/7/28 11:39
 **/
@Log
public abstract class AbstractActionListener implements ActionListener {
    /**
     * 数据树
     **/
    protected JCheckBoxTree.CheckNode root;
    /**
     * 导出文件后缀
     **/
    protected String suffix;
    /**
     * 导出excel 集合
     **/
    public List<TableParameter> exportList = new ArrayList<>();
    /**
     * 导出word 表信息 集合
     **/
    public Map<String, List<TableParameter>> listMap = new LinkedHashMap<>();
    /**
     * 导出word 索引 集合
     **/
    public Map<String, List<IndexInfoVO>> indexMap = new HashMap<>(0);

    /**
     * 是否处理处理数据
     **/
    public static boolean isProcess = false;
    public static boolean selectMode = false;
    public int i = 2;
    public int j = 1;

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
            t.setNo(nodes[i].toString()).setColumnComment("isTableNameBlank");
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
            //赋值序号
            AtomicReference<Integer> no = new AtomicReference<>(0);
            IntStream.range(0, exportList.size()).forEach(x -> {
                if ("isTableNameBlank".equals(exportList.get(x).getColumnComment())) {
                    no.set(1);
                    return;
                }
                exportList.get(x).setNo(String.valueOf(no.get()));
                no.getAndSet(no.get() + 1);
            });
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
            //赋值序号
            for (Map.Entry<String, List<TableParameter>> map : listMap.entrySet()) {
                //赋值序号
                List<TableParameter> list = map.getValue();
                AtomicReference<Integer> no = new AtomicReference<>(0);
                IntStream.range(0, list.size()).forEach(x -> {
                    no.getAndSet(no.get() + 1);
                    list.get(x).setNo(String.valueOf(no));
                });
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
     * 第一步：数据组装（注意：逻辑取反，不能导出的情况为true；反之，为false）
     * 期待您的实现（目前Word、Excel、Markdown、Html、Pdf）
     *
     * @return boolean
     **/
    public boolean dataAssemble() {
        Enumeration e = root.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            JCheckBoxTree.CheckNode node = (JCheckBoxTree.CheckNode) e.nextElement();
            if (node.isSelected() && !node.children().hasMoreElements()) {
                generateExportList(DataBaseAssemblyFactory.get(CommonConstant.DATA_BASE_TYPE).dataResult(), node);
                if (!isProcess) {
                    return true;
                }
            }
        }
        if (exportList.size() <= 0) {
            JOptionPane.showMessageDialog(null, "未选择左侧要导出的库或表！！！", "错误", JOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }

    /**
     * 适用于字段、索引数据表格
     **/
    public boolean dataAssembleAndJudge(JCheckBoxTree.CheckNode root) {
        Enumeration e = root.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            JCheckBoxTree.CheckNode node = (JCheckBoxTree.CheckNode) e.nextElement();
            if (node.isSelected() && !node.children().hasMoreElements()) {
                generateExportListMap(DataBaseAssemblyFactory.get(CommonConstant.DATA_BASE_TYPE).dataResult(), node);
                if (!isProcess) {
                    return true;
                }
            }
        }
        if (listMap.size() <= 0) {
            JOptionPane.showMessageDialog(null, "未选择左侧要导出的库或表！！！", "错误", JOptionPane.ERROR_MESSAGE);
            return true;
        }
        return false;
    }


    /**
     * 第二步：数据导出
     * 期待您的实现（目前Word、Excel、Markdown、Html、Pdf）
     *
     * @param file 文件
     **/
    public abstract void export(File file);

    @SneakyThrows
    @Override
    public void actionPerformed(ActionEvent ev) {
        Runnable runnable = () -> {
            if (dataAssemble()) {
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("表结构信息-" + System.currentTimeMillis() + suffix));
            int result = chooser.showSaveDialog(null);
            chooser.setDialogTitle("保存文件");//自定义选择框标题
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    export(file);
                    int n = JOptionPane.showConfirmDialog(null, "导出成功！\n文件已保存到："
                            + file.getAbsolutePath() + "\n是否立即打开查看文件？", "成功", JOptionPane.YES_NO_OPTION);
                    if (n == 0) {
                        FileOperateUtil.open(file);
                    }
                } catch (Exception ex) {
                    LogManager.writeLogFile(ex, log);
                    JOptionPane.showMessageDialog(null, "导出失败！请联系开发者，邮箱：963565242@qq.com",
                            "导出失败", JOptionPane.ERROR_MESSAGE);
                }
            }
            exportList.clear();
            listMap.clear();
            indexMap.clear();
        };
        Thread thread = new Thread(runnable);
        thread.start();
        (new ThreadDiag(new JFrame(), thread, "正在导出中，请等待......")).start();
    }

    public Object[] getIndexValues(IndexInfoVO indexInfoVO) {
        Object[] values = new Object[5];
        values[0] = StringUtil.stringNullForEmpty(indexInfoVO.getName());
        values[1] = StringUtil.stringNullForEmpty(indexInfoVO.getColumnName());
        values[2] = StringUtil.stringNullForEmpty(indexInfoVO.getIndexType());
        values[3] = StringUtil.stringNullForEmpty(indexInfoVO.getIndexMethod());
        values[4] = StringUtil.stringNullForEmpty(indexInfoVO.getComment());
        return values;
    }

    public Object[] getColumnValues(String number, TableParameter tableParameter) {
        Object[] values = new Object[8];
        values[0] = number;
        values[1] = tableParameter.getColumnName();
        values[2] = StringUtil.StringEqual(tableParameter.getColumnType());
        values[3] = StringUtil.StringEqual(tableParameter.getLength());
        values[4] = StringUtil.StringEqual(tableParameter.getIsNullAble());
        values[5] = StringUtil.StringEqual(tableParameter.getColumnDefault());
        values[6] = StringUtil.StringEqual(tableParameter.getDecimalPlaces());
        values[7] = StringUtil.StringEqual(tableParameter.getColumnComment());
        return values;
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
