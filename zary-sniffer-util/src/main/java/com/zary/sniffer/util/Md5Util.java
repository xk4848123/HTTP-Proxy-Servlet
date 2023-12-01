package com.zary.sniffer.util;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;


/**
 * com.zx.lib.utils.encryptToStr
 *
 * @author xulibo
 * @version 2017/9/20
 */
public class Md5Util {
    /**
     * 获取文本md5
     *
     * @param input
     * @return
     */
    public static String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes("utf-8"));
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //32位加密
            return buf.toString();
        } catch (Exception e) {
            throw new RuntimeException("md5 error.",e);
        }
    }

    /**
     * 获取文件md5
     *
     * @param filePath
     * @return
     */
    public static String getFileMd5(String filePath) {
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            BigInteger bigInt = new BigInteger(1, md.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            throw new RuntimeException("file md5 error:"+filePath,e);
        }
    }
}
