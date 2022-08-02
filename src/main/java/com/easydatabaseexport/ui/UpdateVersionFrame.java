package com.easydatabaseexport.ui;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.enums.UpdateEnum;
import com.easydatabaseexport.enums.YesNoEnum;
import com.easydatabaseexport.util.FileIniRead;
import com.easydatabaseexport.util.FileOperateUtil;
import com.easydatabaseexport.util.OpenUrl;
import lombok.SneakyThrows;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * UpdateVersionFrame
 *
 * @author lzy
 * @date 2022/5/20 13:40
 **/
public class UpdateVersionFrame {

    public UpdateVersionFrame() {

    }

    public void updateVersionFrame() {
        JFrame jFrame = new JFrame("更新");
        jFrame.setSize(250, 100);
        jFrame.setResizable(false);
        jFrame.setLayout(new BorderLayout());
        JPanel jPanel = new JPanel();
        JLabel jLabel = new JLabel("检测到有新的版本可用！");

        JPanel jPanel2 = new JPanel();
        JButton downLoadButton = new JButton("下载");
        JButton closeButton = new JButton("永久关闭提醒");

        jPanel2.add(downLoadButton);
        jPanel2.add(closeButton);
        // 读取ini文件配置
        CommonConstant.initCheckAllKey(CommonConstant.UPDATE);

        downLoadButton.addActionListener(new ActionListener() {
            @SneakyThrows
            @Override
            public void actionPerformed(ActionEvent e) {
                OpenUrl.openURL("https://gitee.com/lzy549876/EasyDataBaseExport");
                jFrame.dispose();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String value = YesNoEnum.NO_0.getValue();
                FileOperateUtil.writeData(FileOperateUtil.getSavePath() + FileIniRead.FILE_NAME, CommonConstant.INI_NODE_KEY,
                        UpdateEnum.UPDATE_VERSION.getKey(), value);
                CommonConstant.checkConfigIniFile();
                jFrame.dispose();
            }
        });
        jPanel.add(jLabel);
        jFrame.add(jPanel);
        jFrame.add(jPanel2, BorderLayout.SOUTH);
        //居中
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
    }

    /*public static void main(String[] args) {
        UpdateVersionFrame updateVersionFrame = new UpdateVersionFrame();
        updateVersionFrame.updateVersionFrame();
    }*/

}
