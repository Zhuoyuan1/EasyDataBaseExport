package com.easydatabaseexport.ui;

import com.easydatabaseexport.enums.ConfigKeyEnum;
import com.easydatabaseexport.enums.DataBaseType;
import com.easydatabaseexport.enums.NavicatVerEnum;
import com.easydatabaseexport.log.LogManager;
import com.easydatabaseexport.ui.component.LinkLabel;
import com.easydatabaseexport.util.*;
import lombok.extern.log4j.Log4j;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UploadSynFrame 同步navicat配置文件
 *
 * @author lzy
 * @date 2021/01/10 17:03
 */
@Log4j
public class UploadSynFrame {
    private static final double NAVICAT11 = 1.1D;

    private static DecodeNcx decodeNcx;

    public void upLoadFile(JFrame mainFrame) {
        JFrame jframe = new JFrame("导入连接向导");
        SwingUtils.changeLogo(jframe);
        jframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jframe.setLayout(new BorderLayout());
        JToolBar jToolBar = new JToolBar();
        jframe.setSize(400, 150);
        //jframe.setContentPane(jPanel);
        /*JRadioButton addButton = new JRadioButton("追加：添加记录到配置", true);
        JRadioButton updateButton = new JRadioButton("更新：更新目标和源记录相符的记录");
        JRadioButton addOrUpdateButton = new JRadioButton("追加或更新：如果目标存在相同记录，更新它。否则，添加它");
        JRadioButton copyButton = new JRadioButton("复制：删除目标全部记录，并从源重新导入");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(addButton);
        buttonGroup.add(updateButton);
        buttonGroup.add(addOrUpdateButton);
        buttonGroup.add(copyButton);
        jPanel.add(addButton);
        jPanel.add(updateButton);
        jPanel.add(addOrUpdateButton);
        jPanel.add(copyButton);*/
        JPanel filePanel = new JPanel(new GridBagLayout());
        LinkLabel explain = new LinkLabel("操作说明", "https://blog.csdn.net/kkk123445/article/details/122514124");
        explain.setHorizontalAlignment(SwingConstants.LEFT);
        filePanel.add(explain);
        JLabel jl = new JLabel("<html>&nbsp;&nbsp;&nbsp;&nbsp;导入ncx文件，请选择：</html>");
        jl.setHorizontalAlignment(SwingConstants.LEFT);
        filePanel.add(jl);
        JButton developer = new JButton("选择文件");
        developer.setHorizontalAlignment(SwingConstants.CENTER);
        jToolBar.add(developer);
        filePanel.add(jToolBar);
        jframe.add(filePanel);
        //文件上传功能
        developer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                /*if (addButton.isSelected()) {
                    name = ImportMode.ADD.name();
                } else if (updateButton.isSelected()) {
                    name = ImportMode.UPDATE.name();
                } else if (addOrUpdateButton.isSelected()) {
                    name = ImportMode.ADD_OR_UPDATE.name();
                } else if (copyButton.isSelected()) {
                    name = ImportMode.COPY.name();
                }*/
                eventOnImport(new JButton(), jframe, mainFrame);
            }
        });
        jframe.setResizable(false);
        jframe.setLocationRelativeTo(null);
        jframe.setVisible(true);
    }

    /**
     * 文件上传功能
     *
     * @param developer 按钮控件名称
     */
    public static void eventOnImport(JButton developer, JFrame jFrame, JFrame mainFrame) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        /** 过滤文件类型 * */
        FileNameExtensionFilter filter = new FileNameExtensionFilter("ncx", "ncx");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(developer);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            /** 得到选择的文件* */
            File file = chooser.getSelectedFile();
            if (file == null) {
                return;
            }
            try {
                FileOperateUtil.saveIniFile(FileOperateUtil.getSavePath() + "database.ini", "");
                // List<Map <连接名，Map<属性名，值>>> 要导入的连接
                List<Map<String, Map<String, String>>> configMap = new ArrayList<>();
                //1、创建一个DocumentBuilderFactory的对象
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                //2、创建一个DocumentBuilder的对象
                //创建DocumentBuilder对象
                DocumentBuilder db = dbf.newDocumentBuilder();
                //3、通过DocumentBuilder对象的parser方法加载xml文件到当前项目下
                Document document = db.parse(file);
                //获取所有Connections节点的集合
                NodeList connectList = document.getElementsByTagName("Connection");

                NodeList nodeList = document.getElementsByTagName("Connections");
                NamedNodeMap verMap = nodeList.item(0).getAttributes();
                double version = Double.parseDouble((verMap.getNamedItem("Ver").getNodeValue()));
                if (version <= NAVICAT11) {
                    decodeNcx = new DecodeNcx(NavicatVerEnum.native11.name());
                } else {
                    decodeNcx = new DecodeNcx(NavicatVerEnum.navicat12more.name());
                }
                //配置map
                Map<String, Map<String, String>> connectionMap = new HashMap<>();
                //遍历每一个Connections节点
                for (int i = 0; i < connectList.getLength(); i++) {
                    //通过 item(i)方法 获取一个Connection节点，nodelist的索引值从0开始
                    Node connect = connectList.item(i);
                    //获取Connection节点的所有属性集合
                    NamedNodeMap attrs = connect.getAttributes();
                    //遍历Connection的属性
                    Map<String, String> map = new HashMap<>(0);
                    for (int j = 0; j < attrs.getLength(); j++) {
                        //通过item(index)方法获取connect节点的某一个属性
                        Node attr = attrs.item(j);
                        map.put(attr.getNodeName(), attr.getNodeValue());
                    }
                    connectionMap.put(map.get("ConnectionName") + map.get("ConnType"), map);
                }
                configMap.add(connectionMap);
                writeConfigFile(configMap);
                SwingUtils.rebootFrame("导入成功", "", jFrame, mainFrame);
            } catch (Exception e) {
                LogManager.writeLogFile(e, log);
                JOptionPane.showMessageDialog(null, "导入失败！请导入正确的ncx文件！", "提示",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 写入配置文件
     *
     * @param list
     */
    private static void writeConfigFile(List<Map<String, Map<String, String>>> list) throws Exception {
        for (Map<String, Map<String, String>> map : list) {
            for (Map.Entry<String, Map<String, String>> valueMap : map.entrySet()) {
                Map<String, String> resultMap = valueMap.getValue();
                if (null == DataBaseType.matchType(resultMap.get("ConnType"))) {
                    continue;
                }
                String password = Base64.encodeBase64String(AESCoder.encrypt(decodeNcx
                        .decode(resultMap.getOrDefault("Password", "")).getBytes(), AESCoder.readFileReturnByte()));
                String msg = "";
                if (DataBaseType.SQLITE.name().equalsIgnoreCase(resultMap.get("ConnType"))) {
                    msg = resultMap.get("ConnectionName") + "/" + resultMap.get("DatabaseFileName").replaceAll(":", "|")
                            + "|" + StringUtil.stringNullForEmpty(resultMap.get("Database")) + "|" + resultMap.get("UserName") + "|" + resultMap.get("ConnType") + " = "
                            + password;
                } else {
                    msg = resultMap.get("ConnectionName") + "/" + resultMap.get("Host") + "|" + resultMap.get("Port")
                            + "|" + StringUtil.stringNullForEmpty(resultMap.get("Database")) + "|" + resultMap.get("UserName") + "|" + resultMap.get("ConnType") + " = "
                            + password;
                }
                FileOperateUtil.writeData(FileOperateUtil.getSavePath()
                        + FileIniRead.FILE_NAME, ConfigKeyEnum.SYS.getKey(), "", msg);
            }
        }
    }
}
