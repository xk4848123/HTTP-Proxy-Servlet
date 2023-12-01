package com.zary.sniffer.server.scheduler;

import com.zary.sniffer.server.ServerMain;
import com.zary.sniffer.server.config.ServerConfig;
import com.zary.xcore.XMain;
import com.zx.lib.utils.LogUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

/**
 * @author wanght
 * @date 2020-04-20 15:00:00
 * 授权检查
 * 启动时立即执行一次
 */
public class AuthInspect implements Job {
    /**
     * 日志对象
     */
    static Logger logger = LogUtil.getLogger(AuthInspect.class);
    /**
     * 配置参数
     */
    static ServerConfig CONFIG = null;
    /**
     * 授权文件保存地址
     */
    static final String FILE_PATH = "/opt/zary-adm/temp/LICENSE";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (CONFIG == null) {
            CONFIG = (ServerConfig) context.getJobDetail().getJobDataMap().get("config");
        }
        executeNoJob(null);
    }

    /**
     * 非计划任务执行
     * @param config
     */
    public void executeNoJob(ServerConfig config){
        try {
            long startTimeStamp = System.currentTimeMillis();
            logger.info("判断授权状态,时间:" + startTimeStamp);
            if (config != null){
                CONFIG = config;
            }
            setAuthStatus();
            logger.info("授权判断完成,耗时:" + (System.currentTimeMillis() - startTimeStamp) / 1000 + "s");
        } catch (Exception e) {
            logger.error("判断授权异常.", e);
        }
    }

    /**
     * 检查授权状态赋值给全局变量
     */
    private void setAuthStatus(){
        boolean checkResult = false;
        try{
            checkResult = XMain.check(FILE_PATH);
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        ServerMain.AUTH_STATUS = checkResult;
    }
}
