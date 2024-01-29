package com.zary.sniffer.util;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 字符串相关函数封装，继承自 org.apache.commons.lang3.StringUtils
 *
 * @author xulibo
 * @version 2017/9/13
 */
public class StringUtil extends StringUtils {
    /**
     * 分隔符：,
     */
    public static final String SPLIT_1 = ",";
    /**
     * 分隔符：|
     */
    public static final String SPLIT_2 = "|";
    /**
     * 分隔符：⊙
     */
    public static final String SPLIT_3 = "⊙";

    /**
     * 判断是否为浮点数
     *
     * @param cs
     * @return
     */
    public static boolean isDecimal(CharSequence cs) {
        return Pattern.matches(RegexUtil.REG_DECIMAL, cs);
    }

    /**
     * 判断是否为纯字母
     *
     * @param cs
     * @return
     */
    public static boolean isLetters(CharSequence cs) {
        return Pattern.matches(RegexUtil.REG_LETTERS, cs);
    }

    /**
     * 判断是否为空或者为null
     *
     * @param cs
     * @return
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * 去除字符串对象所有的空格
     *
     * @param str 需处理的字符串对象
     * @return 空串或处理后字符串
     */
    public static String trimAll(String str) {
        return StringUtils.isNoneEmpty(str) ? EMPTY : str.trim().replaceAll("\\s+", EMPTY);
    }

    /**
     * 获取全球唯一标识符
     *
     * @param isSplit 是否包含分隔符
     * @return
     */
    public static String getGuid(Boolean isSplit) {
        String guid = UUID.randomUUID().toString();
        if (!isSplit)
            guid = guid.replaceAll("-", "");
        return guid;
    }

    /**
     * 一个字符串是否是给定字典中某个字符串开头(忽略大小写)
     * @param source
     * @param heads
     * @return
     */
    public static boolean isStartWithAny(String source, String[] heads) {
        if (source != null && heads != null) {
            for (String head : heads) {
                if (source.trim().toLowerCase().startsWith(head.toLowerCase()))
                    return true;
            }
        }
        return false;
    }

    public static boolean isStartWithAny(String source, List<String> heads) {
        if (source != null && heads != null) {
            for (String head : heads) {
                if (source.trim().toLowerCase().startsWith(head.toLowerCase()))
                    return true;
            }
        }
        return false;
    }

    /**
     * 一个字符串是否是给定字典中某个字符串结尾(忽略大小写)
     * @param source
     * @param suffixs
     * @return
     */
    public static boolean isEndWithAny(String source, String[] suffixs) {
        if (source != null && suffixs != null) {
            for (String suffix : suffixs) {
                if (source.trim().toLowerCase().endsWith(suffix.toLowerCase()))
                    return true;
            }
        }
        return false;
    }
    public static boolean isEndWithAny(String source, List<String> suffixs) {
        if (source != null && suffixs != null) {
            for (String suffix : suffixs) {
                if (source.trim().toLowerCase().endsWith(suffix.toLowerCase()))
                    return true;
            }
        }
        return false;
    }


//    /**
//     * 字符串编码ISO-8859-1转GBK
//     * @param str 需转码字符串
//     * @return
//     * @throws UnsupportedEncodingException
//     */
//    public static String encodeIsoToGbk(String str) throws UnsupportedEncodingException {
//
//        return new String(str.getBytes(ECharSet.ISO_8859_1.getName()), "GBK");
//    }
//    /**
//     * 字符串编码ISO-8859-1转UTF-8
//     * @param str 需转码字符串
//     * @return
//     * @throws UnsupportedEncodingException
//     */
//    public static String encodeIsoToUtf8(String str) throws UnsupportedEncodingException{
//
//        return new String(str.getBytes(ECharSet.ISO_8859_1.getName()), ECharSet.UTF8.getName());
//    }
//    /**
//     * 字符串编码GBK转ISO-8859-1
//     * @param str 需转码字符串
//     * @return
//     * @throws UnsupportedEncodingException
//     */
//    public static String encodeGbkToIso(String str) throws UnsupportedEncodingException{
//
//        return new String(str.getBytes("GBK"), ECharSet.ISO_8859_1.getName());
//    }
//    /**
//     * 字符串编码GBK转UTF-8
//     * @param str 需转码字符串
//     * @return
//     * @throws UnsupportedEncodingException
//     */
//    public static String encodeGbkToUtf8(String str) throws UnsupportedEncodingException{
//
//        return new String(str.getBytes("GBK"), ECharSet.UTF8.getName());
//    }

}
