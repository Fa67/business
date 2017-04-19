package com.eayun.charge.bean;

/**
 * 后付费资源计费相关枚举项
 *
 * @Filename: ChargeConstant.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月4日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public class ChargeConstant {
    public interface RES_STATUS {
        String NORMAL = "0";
        String DELETED = "1";
        String UPGRADED = "2";
        String RECYCLED = "3";
    }

    public interface OBS_STATS_TYPE {
        String STORAGE = "STORAGE";
        String USED = "USED";
        String CDN_BACK_ORIGIN = "CDN_BACK_ORIGIN";
        String CLOUD_RES_CHARGE_DONE = "CLOUD_RES_CHARGE_DONE";
        String CDN_DETAIL_GATHER_DONE = "CDN_DETAIL_GATHER_DONE";
    }

    public static String RECORD_VALID = "1";
    public static String RECORD_INVALID = "0";

    public interface CHARGE_RECORD_OP_TYPE {
        String PURCHASE="PURCHASE";
        String UPGRADE="UPGRADE";
        String DELETE="DELETE";
        String RESTRICT="RESTRICT";
        String RECOVER="RECOVER";
        String UNBLOCK="UNBLOCK";
        String RECYCLE="RECYCLE";
        String RESTORE="RESTORE";
        String OBS="OBS";
    }
}
