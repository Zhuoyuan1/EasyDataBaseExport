package com.easydatabaseexport.ui.export;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.PatternConstant;
import com.easydatabaseexport.core.DataResult;
import com.easydatabaseexport.entities.IndexInfoVO;
import com.easydatabaseexport.entities.TableParameter;
import com.easydatabaseexport.enums.ConfigEnum;
import com.easydatabaseexport.enums.YesNoEnum;
import com.easydatabaseexport.factory.DataBaseAssemblyFactory;
import com.easydatabaseexport.factory.assembly.impl.ConDatabaseModeTableImpl;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.ui.ConnectJavaFrame;
import com.easydatabaseexport.ui.component.Diag;
import com.easydatabaseexport.ui.component.JCheckBoxTree;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * AbstractActionListener
 *
 * @author lzy
 * @date 2022/7/28 11:39
 **/
@Log4j
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
     * 导出 表信息 集合
     **/
    protected Map<String, List<TableParameter>> listMap = new LinkedHashMap<>();
    /**
     * 导出 索引 集合
     **/
    protected Map<String, List<IndexInfoVO>> indexMap = new HashMap<>(16);

    /**
     * 是否处理处理数据
     **/
    private boolean isProcess = false;
    private boolean selectMode = false;
    private int i = 2;
    private int j = 1;

    /**
     * 定义每个线程处理多少表
     **/
    private static final Integer pageSize = 20;

    private void getProcess(JCheckBoxTree.CheckNode node) {
        TreeNode[] nodes = node.getPath();
        if (nodes.length < 3) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "数据库[" + nodes[1] + "]中，不存在任何表", "错误", JOptionPane.ERROR_MESSAGE);
            });
            isProcess = false;
        } else {
            isProcess = true;
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
                IntStream.range(0, list.size()).forEach(x -> {
                    list.get(x).setNo(String.valueOf(x + 1));
                });
            }
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
        return dataAssembleAndJudge(null != ConnectJavaFrame.cacheRootNode && ConnectJavaFrame.cacheRootNode.getChildCount() > 0
                ? ConnectJavaFrame.cacheRootNode : root);
    }

    /**
     * 适用于字段、索引数据表格
     **/
    public boolean dataAssembleAndJudge(JCheckBoxTree.CheckNode root) {
        selectMode = DataBaseAssemblyFactory.get(CommonConstant.DATA_BASE_TYPE) instanceof ConDatabaseModeTableImpl;
        if (selectMode) {
            i = 3;
            j = 2;
        }
        Enumeration<?> e = root.breadthFirstEnumeration();
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
            clear();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, "未选择左侧要导出的库或表！！！", "错误", JOptionPane.ERROR_MESSAGE);
            });
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
    public abstract boolean export(File file);

    @SneakyThrows
    @Override
    public void actionPerformed(ActionEvent ev) {
        CompletableFuture<Boolean> dataFuture = CompletableFuture.supplyAsync(this::dataAssemble);
        new Diag(dataFuture, "数据准备中");
        if (dataFuture.get()) {
            return;
        }
        // 创建线程池对象
        JFileChooser chooser = new JFileChooser();
        if (CommonConstant.configMap.containsKey(ConfigEnum.DEFAULT_EXPORT_PATH.getKey())) {
            chooser.setCurrentDirectory(new File(CommonConstant.configMap.get(ConfigEnum.DEFAULT_EXPORT_PATH.getKey())));
        }
        //自定义选择框标题
        chooser.setDialogTitle("保存文件");
        chooser.setSelectedFile(new File("表结构信息-" + System.currentTimeMillis() + suffix));
        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() -> export(file));
                //遮罩
                new Diag(completableFuture, "正在导出中");
                SwingUtilities.invokeLater(new Runnable() {
                    @SneakyThrows
                    @Override
                    public void run() {
                        if (completableFuture.get()) {
                            int n = JOptionPane.showConfirmDialog(null, "导出成功！\n文件已保存到："
                                    + file.getAbsolutePath() + "\n是否立即打开查看文件？", "成功", JOptionPane.YES_NO_OPTION);
                            if (n == JOptionPane.YES_OPTION) {
                                FileOperateUtil.open(file);
                            }
                        }
                    }
                });
            } catch (Exception ex) {
                LogManager.writeLogFile(ex, log);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, "导出失败！请联系开发者，邮箱：963565242@qq.com",
                            "导出失败", JOptionPane.ERROR_MESSAGE);
                });
            }
        }
        clear();
    }

    @SneakyThrows
    public Object[] getIndexValues(IndexInfoVO indexInfoVO) {
        Object[] values = new Object[PatternConstant.indexFields.length];
        for (int k = 0; k < PatternConstant.indexFields.length; k++) {
            Field field = PatternConstant.indexFields[k];
            String fieldName = field.getName();
            String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase()
                    + fieldName.substring(1);
            Method getMethod = indexInfoVO.getClass().getMethod(getMethodName);
            values[k] = dealWith(String.valueOf(getMethod.invoke(indexInfoVO)));
        }
        return values;
    }

    /**
     * 处理空串或null字符
     *
     * @param source 源字符
     * @return java.lang.String
     **/
    public String dealWith(String source) {
        return StringUtil.stringNullForEmpty(source);
    }

    @SneakyThrows
    public Object[] getColumnValues(TableParameter tableParameter) {
        Object[] values = new Object[PatternConstant.tableFields.length];
        for (int k = 0; k < PatternConstant.tableFields.length; k++) {
            Field field = PatternConstant.tableFields[k];
            String fieldName = field.getName();
            String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase()
                    + fieldName.substring(1);
            Method getMethod = tableParameter.getClass().getMethod(getMethodName);
            values[k] = StringUtil.StringEqual(String.valueOf(getMethod.invoke(tableParameter)));
        }
        return values;
    }

    private void clear() {
        listMap.clear();
        indexMap.clear();
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
