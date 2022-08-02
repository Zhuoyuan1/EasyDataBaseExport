package com.easydatabaseexport.util;

import com.easydatabaseexport.entities.OSDetector;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

/**
 * AESCoder
 *
 * @author lzy
 * @date 2021/7/5 15:56
 **/
public class AESCoder {

    static {
        Security.setProperty("crypto.policy", "unlimited");
    }

    private static final String string = "THpZMTg3cndldW9makAjISg=";

    /**
     * 密钥算法
     */
    public static final String KEY_ALGORITHM = "AES";
    /**
     * 存放key的文件名
     **/
    public static final String KET_FILE_NAME = "key.property";

    /**
     * 加密/解密算法 / 工作模式 / 填充方式
     * Java 7支持PKCS5PADDING填充方式
     * Bouncy Castle支持PKCS7Padding填充方式
     */
    public static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    /**
     * 转换密钥
     *
     * @param key 二进制密钥
     * @return Key 密钥
     */
    private static Key toKey(byte[] key) {
        // 实例化DES密钥材料
        SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
        return secretKey;
    }

    /**
     * 解密
     *
     * @param data 待解密数据
     * @param key  密钥
     * @return byte[] 解密数据
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 还原密钥
        Key k = toKey(key);
        /*
         * 实例化
         * 使用PKCS7Padding填充方式，按如下方式实现
         * Cipher.getInstance(CIPHER_ALGORITHM, "BC");
         */
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        // 初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, k);
        // 执行操作
        return cipher.doFinal(data);
    }

    /**
     * 加密
     *
     * @param data 待加密数据
     * @param key  密钥
     * @return byte[] 加密数据
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 还原密钥
        Key k = toKey(key);
        /*
         * 实例化
         * 使用PKCS7Padding填充方式，按如下方式实现
         * Cipher.getInstance(CIPHER_ALGORITHM, "BC");
         */
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        // 初始化，设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, k);
        // 执行操作
        return cipher.doFinal(data);
    }

    /**
     * 生成密钥 <br>
     *
     * @return byte[] 二进制密钥
     * @throws Exception
     */
    public static byte[] initKey() throws Exception {
        // 实例化
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        // AES 要求密钥长度为128位、192位或256位（AES-256 加密对java版本有要求）
        kg.init(128);
        // 生成秘密密钥
        SecretKey secretKey = kg.generateKey();
        // 获得密钥的二进制编码形式
        return secretKey.getEncoded();
    }

    public static void initKeyAndEndurance() throws Exception {
        File file = new File(FileOperateUtil.getSavePath() + KET_FILE_NAME);
        if (!file.exists()) {
            byte[] key = initKey();
            FileOperateUtil.saveFile(FileOperateUtil.getSavePath() + KET_FILE_NAME, Base64.encodeBase64String(key).getBytes());
        }
        if (OSDetector.isWindows()) {
            //设置只读
            Runtime.getRuntime().exec("attrib " + "\"" + file.getAbsolutePath() + "\"" + " +R");
            //设置隐藏
            Runtime.getRuntime().exec("attrib " + "\"" + file.getAbsolutePath() + "\"" + " +H");
        } else {
            file.setReadOnly();
        }
    }

    public static byte[] readFileReturnByte() throws Exception {
        File file = new File(FileOperateUtil.getSavePath() + KET_FILE_NAME);
        if (file.exists()) {
            List<String> mPaths = new ArrayList<>();
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = br.readLine()) != null) {
                mPaths.add(line);
            }
            fis.close();
            return Base64.decodeBase64(mPaths.get(0));
        }
        return null;
    }

    /*public static void main(String[] args) throws Exception {
        String inputStr = "root";
        byte[] inputData = inputStr.getBytes();
        System.err.println("原文:\t" + inputStr);
        // 初始化密钥
        byte[] key = AESCoder.initKey();
        System.err.println("密钥:\t" + Base64.encodeBase64String(key));
        // 加密
        inputData = AESCoder.encrypt(inputData, key);
        String m = Base64.encodeBase64String(inputData);
        System.err.println("加密后:\t" + m);
        // 解密
        byte[] outputData = AESCoder.decrypt(Base64.decodeBase64(m), key);
        String outputStr = new String(outputData);
        System.err.println("解密后:\t" + outputStr);
    }*/
}
