package com.easydatabaseexport.util;

import com.easydatabaseexport.log.LogManager;
import lombok.extern.java.Log;

import java.lang.reflect.Method;

/**
 * 打开url链接
 *
 * @author lzy
 * @date 2021/5/31 11:45
 **/
@Log
public class OpenUrl {

    public static void openURL(String url) {
        try {
            browse(url);
        } catch (Exception e) {
            LogManager.writeLogFile(e, log);
        }
    }

    private static void browse(String url) throws Exception {
        //获取操作系统的名字
        String osName = System.getProperty("os.name", "");
        if (osName.startsWith("Mac OS")) {
            //苹果的打开方式
            Class fileMgr = Class.forName("com.apple.eio.FileManager");
            Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
            openURL.invoke(null, url);
        } else if (osName.startsWith("Windows")) {
            //windows的打开方式。
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
        } else {
            // Unix or Linux的打开方式
            String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
            String browser = null;
            for (int count = 0; count < browsers.length && browser == null; count++) {
                if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
                    browser = browsers[count];
                }
            }
            if (browser == null) {
                throw new Exception("Could not find web browser");
            } else {
                Runtime.getRuntime().exec(new String[]{browser, url});
            }
        }
    }
}
