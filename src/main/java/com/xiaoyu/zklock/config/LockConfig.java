package com.xiaoyu.zklock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 锁配置文件属性
 * </p>
 *
 * @author ZhangXianYu   Email: 1600501744@qq.com
 * @since 2020-05-08 15:10
 */
@ConfigurationProperties(prefix = LockConfig.PREFIX)
public class LockConfig {

    static final String PREFIX = "xiaoyu.lock";

    private String zkAddress;

    private String timeOut;

    private boolean enableLog;

    public String getTimeOut() {
        if("0".equals(timeOut) || StringUtils.isEmpty(timeOut)) {
            return "60000";
        }
        return timeOut;
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public boolean isEnableLog() {
        if(StringUtils.isEmpty(enableLog)) {
            return false;
        }
        return enableLog;
    }

    public void setEnableLog(boolean enableLog) {
        this.enableLog = enableLog;
    }
}
