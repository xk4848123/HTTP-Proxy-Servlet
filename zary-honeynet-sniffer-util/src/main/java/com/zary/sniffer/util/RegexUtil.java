package com.zary.sniffer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式相关辅助函数
 *
 * @author xulibo
 * @version 2017/10/30
 */
public class RegexUtil {
    /**
     * 整型
     */
    public static final String REG_DIGITS = "[0-9]*";
    /**
     * 字母
     */
    public static final String REG_LETTERS = "[a-zA-Z]+";
    /**
     * 邮箱地址
     */
    public static final String REG_EMAIL = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
    /**
     * 身份证
     */
    public static final String REG_IDCARD = "[1-9]\\d{13,16}[a-zA-Z0-9]{1}";
    /**
     * 手机
     */
    public static final String REG_MOBILE = "^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$";
    /**
     * 固话
     */
    public static final String REG_PHONE = "\\d{3}-\\d{8}|\\d{4}-\\d{7,8}";
    /**
     * 正负整数
     */
    public static final String REG_INTEGER = "\\-?[1-9]\\d+";
    /**
     * 正负浮点数
     */
    public static final String REG_DECIMAL = "\\-?[1-9]\\d+(\\.\\d+)?";
    /**
     * 中文字符
     */
    public static final String REG_CHINESE = "[\\u4E00-\\u9FA5]+";
    /**
     * URL地址
     */
    public static final String REG_URLS = "((ht|f)tps?):\\/\\/[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-\\.,@?^=%&:\\/~\\+#]*[\\w\\-\\@?^=%&\\/~\\+#])?";
    /**
     * IP地址
     */
    public static final String REG_IP = "(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])";
    /**
     * 域名
     */
    public static final String REG_DOMAIN = "(\\w+\\.){1,2}[a-z]+";
    /**
     * 微信号
     */
    public static final String REG_WXID = "[a-zA-Z][a-zA-Z\\d_\\-]{5,19}";


    /**
     * 验证Email
     *
     * @param email xxx@xxx.xxx
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isEmail(String email) {
        return Pattern.matches(REG_EMAIL, email);
    }

    /**
     * 验证身份证号码
     *
     * @param idCard 居民身份证号码15位或18位，最后一位可能是数字或字母
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isIdCard(String idCard) {
        return Pattern.matches(REG_IDCARD, idCard);
    }

    /**
     * 验证手机号码
     *
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isMobile(String mobile) {
        return Pattern.matches(REG_MOBILE, mobile);
    }

    /**
     * 验证固定电话号码
     *
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isPhone(String phone) {
        return Pattern.matches(REG_PHONE, phone);
    }

    /**
     * 验证整数（正整数和负整数）
     *
     * @param digit 一位或多位0-9之间的整数
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isDigit(String digit) {
        return Pattern.matches(REG_DIGITS, digit);
    }

    /**
     * 验证整数和浮点数（正负整数和正负浮点数）
     *
     * @param decimals 一位或多位0-9之间的浮点数，如：1.23，233.30
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isDecimals(String decimals) {
        return Pattern.matches(REG_DECIMAL, decimals);
    }

    /**
     * 验证中文
     *
     * @param chinese 中文字符
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isChinese(String chinese) {
        return Pattern.matches(REG_CHINESE, chinese);
    }

    /**
     * 验证URL地址
     *
     * @param url 格式：http://blog.csdn.net:80/xyang81/article/details/7705960? 或 http://www.csdn.net:80
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isURL(String url) {
        return Pattern.matches(REG_URLS, url);
    }

    /**
     * 匹配IP地址(简单匹配，格式，如：192.168.1.1，127.0.0.1，没有匹配IP段的大小)
     *
     * @param ipAddress IPv4标准地址
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isIpAddress(String ipAddress) {
        return Pattern.matches(REG_IP, ipAddress);
    }

    /**
     * 判断是否域名
     *
     * @param domain
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isDomain(String domain) {
        return Pattern.matches(REG_DOMAIN, domain);
    }

    /**
     * 微信账号仅支持6-20个字母、数字、下划线或减号，以字母开头
     *
     * @param wxId 微信号
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isWxId(String wxId) {
        return Pattern.matches(REG_WXID, wxId);
    }

    /**
     * 返回所有匹配字符串
     * @param regex
     * @param input
     * @return
     */
    public static List<String> matchs(String regex, String input) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        List<String> res = new ArrayList<String>();
        if (null != m) {
            while (m.find()) {
                res.add(m.group());
            }
        }
        return res;
    }

    /**
     * 返回第一个匹配字符串
     * @param regex
     * @param input
     * @return
     */
    public static String matchFirst(String regex, String input) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        if (m.find())
            return m.group();
        return "";
    }

    /**
     * 返回所有匹配字符串的指定捕获组值集合
     * @param regex
     * @param input
     * @param groupIdx
     * @return
     */
    public static List<String> matchGroups(String regex, String input, Integer groupIdx) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        List<String> res = new ArrayList<String>();
        if (null != m) {
            while (m.find()) {
                res.add(m.group(groupIdx));
            }
        }
        return res;
    }

    /**
     * 返回所有匹配字符串的第一个捕获值
     * @param regex
     * @param input
     * @param groupIdx
     * @return
     */
    public static String matchGroupFirst(String regex, String input, Integer groupIdx) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        if (m.find())
            return m.group(groupIdx);
        return "";
    }
}
