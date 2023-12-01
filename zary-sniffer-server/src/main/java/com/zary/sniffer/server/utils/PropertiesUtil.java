package com.zary.sniffer.server.utils;

import com.zary.sniffer.util.StringUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Properties配置文件处理
 *
 * @author xulibo
 * @version 2017/11/2
 */
public class PropertiesUtil {
    /**
     * 读取reource目录下配置文件
     *
     * @param resourceFilePath 相对resources目录，非/开头
     * @return
     * @throws IOException
     */
    public static Properties getProperties(String resourceFilePath) throws IOException {
        Properties props = new Properties();
        InputStream in = null;
        BufferedReader bf = null;
        try {
            in = PropertiesUtil.class.getClassLoader().getResourceAsStream(resourceFilePath);
            if (in != null) {
                bf = new BufferedReader(new InputStreamReader(in, "utf-8"));
                props.load(bf);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (bf != null) {
                bf.close();
            }
        }
        return props;
    }

    /**
     * 读取任意位置配置文件
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static Properties getPropertiesByPath(String filePath) throws IOException {
        Properties props = new Properties();
        File file = new File(filePath);
        FileInputStream inputStream = new FileInputStream(file);
        props.load(inputStream);
        inputStream.close();
        return props;
    }

    /**
     * 根据key获取value
     *
     * @param filePath
     * @param key
     * @return
     * @throws IOException
     */
    public static String getValue(String filePath, String key) throws IOException {
        Properties props = getProperties(filePath);
        return props.getProperty(key);
    }

    /**
     * 设置键值
     *
     * @param filePath
     * @param key
     * @param value
     * @throws IOException
     */
    public static void setValue(String filePath, String key, String value) throws IOException {
        OutputStream fos = null;
        OutputStreamWriter writer = null;
        try {
            Properties props = getProperties(filePath);
            props.setProperty(key, value);
            String realPath = PropertiesUtil.class.getClassLoader().getResource(filePath).getFile();
            fos = new FileOutputStream(realPath);
            writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

            props.store(writer, String.format("setValue:%s[%s]", key, value));
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                fos.close();
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 读取配置文件字符串写入到变量
     *
     * @param prop
     * @param str
     * @param key
     */
    public static String setPropString(Properties prop, String str, String key) {
        if (prop != null && !StringUtil.isEmpty(key)) {
            String value = prop.getProperty(key);
            return StringUtil.isEmpty(value) ? str : value;
        }
        return str;
    }

    /**
     * 读取配置文件数字写入到变量
     */
    public static Integer setPropInt(Properties prop, Integer num, String key) {
        if (prop != null && !StringUtil.isEmpty(key)) {
            String value = prop.getProperty(key);
            return StringUtil.isEmpty(value) ? num : Integer.parseInt(value);
        }
        return num;
    }
}
