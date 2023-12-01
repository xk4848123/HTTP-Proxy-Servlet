package com.zary.sniffer.server.model;

import com.zary.sniffer.core.enums.EnumActionType;
import com.zx.lib.utils.LogUtil;
import com.zx.lib.utils.StringUtil;
import lombok.Data;
import org.slf4j.Logger;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class ThreatRules {
    static Logger logger = LogUtil.getLogger(ThreatRules.class);

    private String uid;

    private String name;

    private String fingerprint;

    private String description;

    private int level;

    private int state;

    private int type;

    private String typename;

    private String ip;

    private String loginname;

    private String appids;

    private String actions;

    private String tables;

    private int resultrows;

    private Boolean executestate;

    private int samesqlcount;

    private int ipcount;

    private int ratecount;

    private int intervaltime;

    private String createrid;

    private Date createtime;

    private String url;

    private String ext;

    public ThreatRules() {
        this.ipcount = 0;
        this.samesqlcount = 0;
        this.resultrows = 0;
        this.ratecount = 0;
        this.intervaltime = 0;
    }

    private String[] multiTable;

    private List<String> listOperate;

    private List<String> listAppId;

    /**
     * 从枚举中获取具体执行动作
     *
     * @param actions
     * @return
     */
    public List<String> getOperates(String actions) {
        List<String> listSpacificAction = new ArrayList<>();
        if (StringUtil.isNotEmpty(actions)) {
            String[] arrAction = actions.split(",");
            for (String action : arrAction) {
                String[] spacificActions = EnumActionType.getActions(Integer.parseInt(action));
                for (String item :
                        spacificActions) {
                    listSpacificAction.add(item);
                }

            }
        }
        return listSpacificAction;
    }

    /**
     * 组合table=operate 例: USER=SELECT
     * @param tables
     * @param listOperate
     * @return
     */
    public List<String> getTablePulsOperates(List<String> listOperate,String... tables) {
        List<String> listTablePlusAction = new ArrayList<>();
        if (null == tables || tables.length == 0) {
            return listTablePlusAction;
        }
        for (String table :
                tables) {
            if (StringUtil.isNotEmpty(table) && listOperate != null && listOperate.size() > 0) {
                for (String operate :
                        listOperate) {
                    listTablePlusAction.add(table.toUpperCase() + "=" + operate);
                }
            }
        }

        return listTablePlusAction;
    }
}