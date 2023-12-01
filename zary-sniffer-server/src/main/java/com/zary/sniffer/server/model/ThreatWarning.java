package com.zary.sniffer.server.model;

import lombok.Data;

import java.util.Date;

/**
 * @Author weiyi
 * @create 2020/3/18 14:20
 */
@Data
public class ThreatWarning {

    private static final long serialVersionUID = 1L;

    /**
     * uid
     */
    private String uid;

    /**
     * appid
     */
    private String appid;

    /**
     * 终端指纹
     */
    private String fingerprint;

    /**
     * 终端ip
     */
    private String ip;

    /**
     * 登陆账号
     */
    private String loginname;

    /**
     * 地理位置
     */
    private String geo;
    /**
     * 索引dataoperate主键id
     */
    private String dataid;
    /**
     * 威胁级别
     */
    private Integer level;

    /**
     * 威胁类型
     */
    private Integer type;

    /**
     * 规则id
     */
    private String ruleid;

    /**
     * 攻击状态
     */
    private Integer attackstate;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 检测时间
     */
    private Date createtime;

    /**
     * 报文 暂存参数化sql
     */
    private String message;

    /**
     * 详情
     */
    private String detail;

    /**
     * 开始时间
     */
    private Date starttime;

    /**
     * 结束时间
     */
    private Date endtime;
    /**
     * 告警次数
     */
    private Integer alarmcount;

    public ThreatWarning() {
        attackstate = 0;
        alarmcount = 0;
        state = 0;
    }
}
