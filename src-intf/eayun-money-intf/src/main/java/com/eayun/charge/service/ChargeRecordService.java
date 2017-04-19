package com.eayun.charge.service;

import com.eayun.charge.model.ChargeRecord;

import java.util.Date;
import java.util.List;

/**
 * 计费清单记录Service
 *
 * @Filename: ChargeRecordService.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月2日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public interface ChargeRecordService {

    /**
     * 新建一条计费清单记录
     *
     * @param chargeRecord
     * @return
     * @throws Exception
     */
    ChargeRecord addChargeRecord(ChargeRecord chargeRecord) throws Exception;

    /**
     * 更新计费清单记录
     *
     * @param chargeRecord
     * @return
     * @throws Exception
     */
    boolean updateChargeRecord(ChargeRecord chargeRecord) throws Exception;

    /**
     * 获取指定的有效的计费清单记录
     *
     * @param datecenterId
     * @param cusId
     * @param resourceId
     * @return
     */
    ChargeRecord getSpecifiedChargeRecord(String datecenterId, String cusId, String resourceId);

    /**
     * 获取指定的无效的计费清单记录，用于恢复资源服务查询计费清单时使用
     *
     * @param datecenterId
     * @param cusId
     * @param resourceId
     * @return
     */
    ChargeRecord getSpecifiedInValidChargeRecord(String datecenterId, String cusId, String resourceId);

    /**
     * 更新指定客户下所有有效计费清单记录的开始计费时间
     *
     * @param cusId
     * @param opTime
     * @throws Exception
     */
    void updateCusAllChargeFromTime(String cusId, Date opTime) throws Exception;

    /**
     * 获取指定客户下的所有有效的计费清单记录列表
     *
     * @param cusId
     * @param currentTime
     * @return
     * @throws Exception
     */
    List<ChargeRecord> getAllValidChargeRecordByCusId(String cusId, Date currentTime) throws Exception;

    /**
     * 获取指定客户下的所有有效的计费清单记录列表
     *
     * @param cusId
     * @return
     * @throws Exception
     */
    List<ChargeRecord> getAllValidChargeRecordByCusId(String cusId) throws Exception;

    /**
     * 获取指定客户下资源状态为正常，记录无效的计费清单记录
     * @param cusId
     * @return
     * @throws Exception
     */
    List<ChargeRecord> getInvalidNormalChargeRecordByCusId(String cusId) throws Exception;

    /**
     * 获取指定客户下资源状态为正常，记录有效的计费清单记录
     * @param cusId
     * @return
     * @throws Exception
     */
    List<ChargeRecord> getValidNormalChargeRecordByCusId(String cusId) throws Exception;

    /**
     * 获取指定客户下已被放入回收站的计费清单记录，用户资源在回收站中恢复时使用
     * @param datecenterId
     * @param cusId
     * @param resourceId
     * @return
     */
    ChargeRecord getRecycledChargeRecord(String datecenterId, String cusId, String resourceId);

    /**
     * 为余额变动引起的后付费资源的恢复（状态恢复、服务恢复）获取指定客户指定生效状态的计费清单
     * @param cusId
     * @param isValid
     * @return
     */
    List<ChargeRecord> getChargeRecordsForPostpayResRecover(String cusId, String isValid);

    List<ChargeRecord> getSpecifiedChargeRecord(String datecenterId, String cusId, String resourceId, String s, String s1);
}
