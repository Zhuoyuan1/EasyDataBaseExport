package com.easydatabaseexport.util;

import com.easydatabaseexport.common.CommonConstant;
import com.easydatabaseexport.common.EnvironmentConstant;
import com.easydatabaseexport.enums.UpdateEnum;
import com.easydatabaseexport.enums.YesNoEnum;
import com.easydatabaseexport.ui.UpdateVersionFrame;
import com.microsoft.sqlserver.jdbc.StringUtils;

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

    private static final String MD5_URL = "https://gitee.com/lzy549876/EasyDataBaseExport/raw/main/MD5.txt";

    /**
     * 自动检查更新
     **/
    public static void check() {
        //永久不提醒 -> 忽略当前版本 -> 比对md5
        if (CommonConstant.configMap.containsKey(UpdateEnum.UPDATE_VERSION.getKey())) {
            //如果永不提醒，则不需要请求
            if (YesNoEnum.NO_0.getValue().equals(CommonConstant.configMap.get(UpdateEnum.UPDATE_VERSION.getKey()))) {
                return;
            }
        }
        //请求当前最新版本
        String result = FileOperateUtil.getRemoteFile(MD5_URL);
        if (StringUtils.isEmpty(result)) {
            return;
        }
        //忽略当前版本
        if (CommonConstant.configMap.containsKey(UpdateEnum.VERSIONS.getKey())) {
            String versions = CommonConstant.configMap.get(UpdateEnum.VERSIONS.getKey());
            if (versions.contains(result)) {
                return;
            }
        }
        //若版本md5不同，则跳出更新
        if (!EnvironmentConstant.FILE_MD5_VALUE.equals(result)) {
            UpdateVersionFrame updateVersion = new UpdateVersionFrame();
            updateVersion.updateVersionFrame(result);
        }
    }

    /**
     * 手动点击更新（手动点击权限大于所有-永久不提醒、版本忽略将无效）
     **/
    public static void checkByClick() {
        //判断软件更新逻辑
        String result = FileOperateUtil.getRemoteFile(MD5_URL);
        if (StringUtils.isEmpty(result)) {
            return;
        }
        if (!EnvironmentConstant.FILE_MD5_VALUE.equals(result)) {
            UpdateVersionFrame updateVersion = new UpdateVersionFrame();
            updateVersion.updateVersionFrame(result);
        } else {
            URL url = CheckUpdateUtil.class.getResource("/images/success.png");
            ImageIcon success = new ImageIcon(Objects.requireNonNull(url));
            JOptionPane.showMessageDialog(null, "您当前使用的是最新版本", "", JOptionPane.PLAIN_MESSAGE, success);
        }
    }
}
