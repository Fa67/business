package com.eayun.syssetup.service;

/**
 * Created by Administrator on 2016/8/22.
 */
public interface SysDataTreeService {
    /**
     * 获取按需购买的最低额度
     * @return
     */
    public String getBuyCondition();

    /**
     * 获取重新恢复的额度
     * @return
     */
    public String getRenewCondition();
    /**
     * 获取欠费或者到期的保留时长
     * @return
     */
    public String getRecoveryTime();
    /**
     * 获取回收站的保留时长
     * @return
     */
    public String getRetainTime();
}
