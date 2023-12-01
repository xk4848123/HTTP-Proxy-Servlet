package com.zary.sniffer.server.utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.*;
import com.zary.sniffer.server.model.IPEntity;
import com.zx.lib.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IP工具类
 *
 * @ClassName IPUtils
 * @Author qgp
 * @Date 2019/9/24 17:51
 **/
public class IPUtils {
    private static final String SEPARATOR = File.separator;
    private static Logger log = LoggerFactory.getLogger(IPUtils.class);
    private static Pattern innerPattern = Pattern.compile("^(127\\.0\\.0\\.1)|(localhost)|(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|(172\\.((1[6-9])|(2\\d)|(3[01]))\\.\\d{1,3}\\.\\d{1,3})|(192\\.168\\.\\d{1,3}\\.\\d{1,3})$");


    /**
     * 全局静态变量，DatabaseReader，保证类加载时加载一次
     */
    private static DatabaseReader reader;
    private static String INTERVAL_CONCAT = " ";

    static {
        try {
            InputStream in = IPUtils.class.getClassLoader().getResourceAsStream("GeoLite2-City.mmdb");
            log.info("-------加载文件");
            reader = new DatabaseReader.Builder(in).build();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * 根据ip获取国家或地区详细信息
     */
    public static String getIPDesc(String ip) {
        return getIPDesc(ip, false);
    }

    public static boolean isInnerIP(String ip) {
        Matcher match = innerPattern.matcher(ip);
        return match.find();
    }

    /**
     * 根据ip获取国家或地区详细信息
     *
     * @param ip
     * @param isCountry 只显示国家
     * @return
     */
    public static String getIPDesc(String ip, Boolean isCountry) {
        boolean isInner = isInnerIP(ip);
        if (isInner) {
            return "LAN";
        }
        IPEntity ipEntity = getIPMsg(ip);
        String returnValue = "";
        try {
            if (isCountry || ipEntity.getCountryName() == null || !ipEntity.getCountryName().equals("中国")) {
                returnValue = ipEntity.getCountryName() == null ? "未知" : ipEntity.getCountryName();
            } else {
                if (StringUtil.isEmpty(ipEntity.getCountryName())) {
                    returnValue = "";
                } else {
                    if (StringUtil.isEmpty(ipEntity.getProvinceName())) {
                        returnValue = ipEntity.getCountryName();
                    } else {
                        // if (StringUtil.isEmpty(ipEntity.getCityName())) {
                        //     returnValue = ipEntity.getCountryName().concat(INTERVAL_CONCAT).concat(ipEntity.getProvinceName());
                        // } else {
                        //     returnValue = ipEntity.getCountryName().concat(INTERVAL_CONCAT).
                        //             concat(ipEntity.getProvinceName().concat(INTERVAL_CONCAT).concat(ipEntity.getCityName()));
                        // }
                        returnValue = ipEntity.getCountryName().concat(INTERVAL_CONCAT).concat(ipEntity.getProvinceName());
                    }
                }
            }

        } catch (Exception e) {
            returnValue = "";
        }
        return returnValue
                .replace("台湾", "中国".concat(INTERVAL_CONCAT).concat("台湾"))
                .replace("中华民国", "中国".concat(INTERVAL_CONCAT).concat("台湾"))
                .replace("香港", "中国".concat(INTERVAL_CONCAT).concat("香港"))
                .replace("澳门", "中国".concat(INTERVAL_CONCAT).concat("台湾"))
                .replace("大韩民国", "韩国")
                .replace("中国".concat(INTERVAL_CONCAT), "")
                .replace("广西壮族自治区", "广西")
                .replace("内蒙古自治区", "内蒙古")
                .replace("西藏自治区", "西藏")
                .replace("宁夏回族自治区", "宁夏")
                .replace("新疆维吾尔自治区", "新疆")
//                .replace("中国", "")
                .replace("北京".concat(INTERVAL_CONCAT), "")
                .replace("上海".concat(INTERVAL_CONCAT), "")
                .replace("重庆".concat(INTERVAL_CONCAT), "")
                .replace("天津".concat(INTERVAL_CONCAT), "");

    }


    /**
     * 根据ip获取国家CODE
     *
     * @param ip
     * @return
     */
    public static String getIPCode(String ip) {
        IPEntity ipEntity = getIPMsg(ip);
        String returnValue = "";
        try {
            returnValue = ipEntity.getCountryCode() == null ? "OTHER" : ipEntity.getCountryCode();

        } catch (Exception e) {
            returnValue = "OTHER";
        }
        return returnValue
                .replace("TW", "CN")
                .replace("MO", "CN")
                .replace("HK", "CN").toLowerCase();

    }

    /**
     * 解析IP
     *
     * @param ip
     * @return
     */
    public static IPEntity getIPMsg(String ip) {

        IPEntity msg = new IPEntity();

        try {
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            Country country = response.getCountry();
            Subdivision subdivision = response.getMostSpecificSubdivision();
            City city = response.getCity();
            Postal postal = response.getPostal();
            Location location = response.getLocation();

            msg.setCountryName(country.getNames().get("zh-CN"));
            if (StringUtil.isNotEmpty(msg.getCountryName())) {
                msg.setCountryName(msg.getCountryName());
                msg.setCountryName(msg.getCountryName());
            }
            msg.setCountryCode(country.getIsoCode());
            msg.setProvinceName(subdivision.getNames().get("zh-CN"));
            if (StringUtil.isNotEmpty(msg.getProvinceName())) {
                msg.setProvinceName(msg.getProvinceName().replace("台北市", "台湾省").replace("省", "").replace("市", ""));
            }
            msg.setProvinceCode(subdivision.getIsoCode());
            msg.setCityName(city.getNames().get("zh-CN"));
            if (StringUtil.isNotEmpty(msg.getCityName())) {
                msg.setCityName(city.getNames().get("zh-CN").replace("市", ""));
            }
            msg.setPostalCode(postal.getCode());
            //经度
            msg.setLongitude(location.getLongitude());
            //纬度
            msg.setLatitude(location.getLatitude());

        } catch (IOException e) {

        } catch (GeoIp2Exception e) {

        }

        return msg;
    }
}