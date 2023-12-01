package com.zary.sniffer.server.model;

import lombok.Data;

/**
 * @ClassName IPEntity
 * @Author qgp
 * @Date 2019/9/24 17:50
 **/
@Data
public class IPEntity {

    //国家
    String countryName;
    //国家代码
    String countryCode;

    //省份
    String provinceName;
    String provinceCode;

    //城市名称
    String cityName;

    //邮政编码
    String postalCode;

    //经度
    Double longitude;
    //纬度
    Double latitude;

}