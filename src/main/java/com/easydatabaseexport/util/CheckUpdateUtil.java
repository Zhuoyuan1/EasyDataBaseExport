package com.easydatabaseexport.util;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.EnvironmentConstant;
import com.easydatabaseexport.enums.UpdateEnum;
import com.easydatabaseexport.enums.YesNoEnum;
import com.easydatabaseexport.ui.UpdateVersionFrame;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.net.URL;
import java.util.Objects;

/**
 * CheckUpdateUtil
 *
 * @author lzy
 * @date 2022/5/20 15:15
 **/
public class CheckUpdateUtil {

    /**
     * 自动检查更新
     **/
    public static void check() {
        //判断软件更新逻辑
        if (!EnvironmentConstant.FILE_MD5_VALUE.equals(FileOperateUtil.getRemoteFileMD5())) {
            if (!CommonConstant.configMap.containsKey(UpdateEnum.UPDATE_VERSION.getKey())) {
                UpdateVersionFrame updateVersion = new UpdateVersionFrame();
                updateVersion.updateVersionFrame();
            } else {
                if (YesNoEnum.YES_1.getValue().equals(CommonConstant.configMap.get(UpdateEnum.UPDATE_VERSION.getKey()))) {
                    UpdateVersionFrame updateVersion = new UpdateVersionFrame();
                    updateVersion.updateVersionFrame();
                }
            }
        }
    }

    /**
     * 手动点击更新
     **/
    public static void checkByClick() {
        //判断软件更新逻辑
        if (!EnvironmentConstant.FILE_MD5_VALUE.equals(FileOperateUtil.getRemoteFileMD5())) {
            UpdateVersionFrame updateVersion = new UpdateVersionFrame();
            updateVersion.updateVersionFrame();
        } else {
            URL url = CheckUpdateUtil.class.getResource("/success.png");
            ImageIcon success = new ImageIcon(Objects.requireNonNull(url));
            JOptionPane.showMessageDialog(null, "您当前使用的是最新版本", "", JOptionPane.PLAIN_MESSAGE, success);
        }
    }
}
