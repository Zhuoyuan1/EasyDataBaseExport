package com.easydatabaseexport.util;

import com.easydatabaseexport.log.LogManager;
import lombok.extern.log4j.Log4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PingUtil
 *
 * @author lzy
 * @date 2021/5/31 15:44
 **/
@Log4j
public class PingUtil {

    public static final String LINUX = "Linux";
    public static final String WINDOWS = "Windows";
    /**
     * 排除host
     */
    public static final String LOCALHOST = "localhost";

    /*public static void main(String[] args) throws Exception {
        String ipAddress = "127.0.0.1";
        log.info(ping(ipAddress, 5, 3));
        log.info(pingForLinux(ipAddress, 5, 3));

//        String host = "192.168.1.192";
//        int port = 4001;
//
//        log.info(connect(host, port, 3000));

        String pingStr = "64 bytes from 127.0.0.1: icmp_seq=1 tttl=64 time=0.015 ms";
        log.info(getCheckResultForLinux(pingStr));
    }*/

    /**
     * 检测 ip 和 端口 是否能连接
     *
     * @param host
     * @param port
     * @param timeOut 多少毫秒超时
     * @return
     */
    public static boolean connect(String host, int port, int timeOut) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), timeOut);
            //boolean res = socket.isConnected();//通过现有方法查看连通状态
        } catch (IOException e) {
            //当连不通时，直接抛异常，异常捕获即可
            LogManager.writeLogFile(e, log);
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                LogManager.writeLogFile(e, log);
            }
        }
        return true;
    }

    /**
     * windows 和linux 总和
     *
     * @param ipAddress ip
     * @param pingTimes ping的次数
     * @param timeOut   多少秒超时
     * @return
     */
    public static boolean ping(String ipAddress, int pingTimes, int timeOut) {
        try {
            Properties props = System.getProperties(); //获得系统属性集
            String osName = props.getProperty("os.name"); //操作系统名称
            String osArch = props.getProperty("os.arch"); //操作系统构架
            String osVersion = props.getProperty("os.version"); //操作系统版本
            log.info(osName);
            log.info(osArch);
            log.info(osVersion);
            Boolean result = false;
            if (osName.contains(LINUX)) {
                result = pingForLinux(ipAddress, pingTimes, timeOut);
            } else if (osName.contains(WINDOWS)) {
                result = pingForWindows(ipAddress, pingTimes, timeOut);
            }
            return result;
        } catch (Exception ex) {
            LogManager.writeLogFile(ex, log);
            // 出现异常则返回假
            return false;
        }
    }

    /**
     * windows 相当于cmd运行 ping 127.0.0.1 -n 5 -w 3000
     *
     * @param ipAddress ip
     * @param pingTimes ping的次数
     * @param timeOut   多少秒超时
     * @return
     */
    public static boolean pingForWindows(String ipAddress, int pingTimes, int timeOut) {
        BufferedReader in = null;
        InputStreamReader inputStreamReader = null;
        Runtime r = Runtime.getRuntime();
        try {
            String pingCommand = "ping " + ipAddress + " -n " + pingTimes + " -w " + timeOut * 1000;
            // 执行命令并获取输出
            //log.info(pingCommand);
            Process p = r.exec(pingCommand);
            if (p == null) {
                return false;
            }
            // 逐行检查输出,计算类似出现=23ms TTL=62字样的次数
            inputStreamReader = new InputStreamReader(p.getInputStream());
            in = new BufferedReader(inputStreamReader);
            int connectedCount = 0;
            String line = null;
            while ((line = in.readLine()) != null) {
                connectedCount += getCheckResultForWindows(line);
            }
            // 出现的次数=测试次数则返回真
            return connectedCount == pingTimes;
        } catch (Exception ex) {
            LogManager.writeLogFile(ex, log);   // 出现异常则返回假
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            } catch (IOException e) {
                LogManager.writeLogFile(e, log);
            }
        }
    }


    /**
     * 相当于 Linux执行  timeout 10s ping 192.168.1.124 -c 5 -i 0
     *
     * @param ipAddress ip
     * @param pingTimes ping的次数
     * @param timeOut   多少秒超时
     * @return
     */
    public static boolean pingForLinux(String ipAddress, int pingTimes, int timeOut) {
        Process process = null;
        InputStreamReader r = null;
        LineNumberReader returnData = null;
        try {
            //String pingCommandStr = "timeout "+timeOut+"s "+"ping " + ipAddress + " -c " + pingTimes + " -i 0";
            String pingCommandStr = "ping " + ipAddress + " -c " + pingTimes;
            process = Runtime.getRuntime().exec(pingCommandStr);
            r = new InputStreamReader(process.getInputStream());
            returnData = new LineNumberReader(r);
            int connectedCount = 0;
            String line = "";
            while ((line = returnData.readLine()) != null) {
                //log.info("line1===="+line);
                connectedCount += getCheckResultForLinux(line);
            }
            //log.info("connectedCount===="+connectedCount);
            // 出现的次数=测试次数则返回真
            return connectedCount == pingTimes;
        } catch (IOException e) {
            LogManager.writeLogFile(e, log);
            return false;
        } finally {
            try {
                if (returnData != null) {
                    returnData.close();
                }
                if (r != null) {
                    r.close();
                }
            } catch (IOException e) {
                LogManager.writeLogFile(e, log);
            }
        }
    }

    //若line含有=18ms TTL=16字样,说明已经ping通,返回1,否則返回0.
    private static int getCheckResultForWindows(String line) {  // log.info("控制台输出的结果为:"+line);
        Pattern pattern = Pattern.compile("(\\d+ms)(\\s+)(TTL=\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            return 1;
        }
        return 0;
    }

    //若line含有ttl=64 time=0.015 ms字样,说明已经ping通,返回1,否則返回0.
    private static int getCheckResultForLinux(String line) {  // log.info("控制台输出的结果为:"+line);
        Pattern pattern = Pattern.compile("(ttl=\\d+)(\\s+)(time=+\\w)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            return 1;
        }
        return 0;
    }

///** Linux start **/
//[root@iZuf6g20pbfuqtc2santjkZ ~]# ping -c 5 -i 0  127.0.0.1
//    PING 127.0.0.1 (127.0.0.1) 56(84) bytes of data.
//64 bytes from 127.0.0.1: icmp_seq=1 ttl=64 time=0.015 ms
//64 bytes from 127.0.0.1: icmp_seq=2 ttl=64 time=0.003 ms
//64 bytes from 127.0.0.1: icmp_seq=3 ttl=64 time=0.002 ms
//64 bytes from 127.0.0.1: icmp_seq=4 ttl=64 time=0.002 ms
//64 bytes from 127.0.0.1: icmp_seq=5 ttl=64 time=0.002 ms
//
//--- 127.0.0.1 ping statistics ---
//            5 packets transmitted, 5 received, 0% packet loss, time 0ms
//    rtt min/avg/max/mdev = 0.002/0.004/0.015/0.005 ms, ipg/ewma 0.021/0.009 ms
//[root@iZuf6g20pbfuqtc2santjkZ ~]# ping -c 5 -i 0 192.168.1.2
//    PING 192.168.1.2 (192.168.1.2) 56(84) bytes of data.
//
//--- 192.168.1.2 ping statistics ---
//            5 packets transmitted, 0 received, 100% packet loss, time 59ms
///** Linux end **/
//
///** windows  start **/
//
//C:\Users\Administrator>ping -n 5 -w 1000 192.168.1.1
//
//正在 Ping 192.168.1.1 具有 32 字节的数据:
//来自 192.168.1.1 的回复: 字节=32 时间<1ms TTL=64
//来自 192.168.1.1 的回复: 字节=32 时间<1ms TTL=64
//来自 192.168.1.1 的回复: 字节=32 时间<1ms TTL=64
//来自 192.168.1.1 的回复: 字节=32 时间<1ms TTL=64
//来自 192.168.1.1 的回复: 字节=32 时间<1ms TTL=64
//
//192.168.1.1 的 Ping 统计信息:
//数据包: 已发送 = 5，已接收 = 5，丢失 = 0 (0% 丢失)，
//往返行程的估计时间(以毫秒为单位):
//最短 = 0ms，最长 = 0ms，平均 = 0ms
//
//C:\Users\Administrator>ping -n 5 -w 1000 192.168.2.1
//
//正在 Ping 192.168.2.1 具有 32 字节的数据:
//请求超时。
//请求超时。
//请求超时。
//请求超时。
//请求超时。
//
//        192.168.2.1 的 Ping 统计信息:
//数据包: 已发送 = 5，已接收 = 0，丢失 = 5 (100% 丢失)，
///** windows  end **/
}
