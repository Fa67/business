package com.eayun.charge.service.impl;

import com.eayun.charge.bean.ChargeConstant;
import com.eayun.charge.dao.ChargeRecordDao;
import com.eayun.charge.model.BaseChargeRecord;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.charge.service.ChargeRecordService;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 计费清单记录Service
 *
 * @Filename: ChargeRecordServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月2日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Service
@Transactional
public class ChargeRecordServiceImpl implements ChargeRecordService {

    private static final Logger log = LoggerFactory.getLogger(ChargeRecordServiceImpl.class);

    @Autowired
    private ChargeRecordDao chargeRecordDao;

    @Override
    public ChargeRecord addChargeRecord(ChargeRecord chargeRecord) throws Exception {
        log.info("新增计费清单记录开始");
        try {
            BaseChargeRecord baseChargeRecord = new BaseChargeRecord();
            BeanUtils.copyPropertiesByModel(baseChargeRecord, chargeRecord);
            chargeRecordDao.save(baseChargeRecord);
            BeanUtils.copyPropertiesByModel(chargeRecord, baseChargeRecord);
        } catch (Exception e) {
            log.error("新增计费清单记录异常：", e);
            throw e;
        }
        return chargeRecord;
    }

    @Override
    public boolean updateChargeRecord(ChargeRecord chargeRecord) throws Exception {
        log.info("更新计费清单记录开始");
        boolean isSuccess = false;
        try {
            BaseChargeRecord baseChargeRecord = new BaseChargeRecord();
            BeanUtils.copyPropertiesByModel(baseChargeRecord, chargeRecord);
            chargeRecordDao.merge(baseChargeRecord);
            BeanUtils.copyPropertiesByModel(chargeRecord, baseChargeRecord);
            isSuccess = true;
            log.info("更新计费清单记录完成");
        } catch (Exception e) {
            log.error("更新计费清单记录异常", e);
            throw e;
        }
        return isSuccess;
    }

    @Override
    public ChargeRecord getSpecifiedChargeRecord(String datecenterId, String cusId, String resourceId) {
        log.info("根据资源ID查询资源状态为正常的有效计费清单记录");
        ChargeRecord chargeRecord = new ChargeRecord();
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseChargeRecord where datecenterId=? and cusId=? and resourceId=? and isValid='1' ");
        List<BaseChargeRecord> baseCRList = chargeRecordDao.find(sb.toString(), datecenterId, cusId, resourceId);
        if (baseCRList.isEmpty()) {
            return null;
        }
        for (BaseChargeRecord baseCR : baseCRList) {
            BeanUtils.copyPropertiesByModel(chargeRecord, baseCR);
            break;
        }
        return chargeRecord;
    }

    @Override
    public List<ChargeRecord> getSpecifiedChargeRecord(String datecenterId, String cusId, String resourceId, String isValid, String resourceStatus) {
        log.info("获取指定客户["+cusId+"]指定生效状态为["+isValid+"]，资源状态为["+resourceStatus+"]的计费清单记录列表");
        Object[] params = new Object[6];
        int index = 0;
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseChargeRecord where datecenterId=? and cusId=? and resourceId=? ");
        params[index]=datecenterId;
        index++;
        params[index]=cusId;
        index++;
        params[index]=resourceId;
        index++;
        if(!StringUtil.isEmpty(isValid) && !isValid.equals("null")){
            sb.append(" and isValid=? ");
            params[index] = isValid;
            index++;
        }
        if(!StringUtil.isEmpty(resourceStatus) && !resourceStatus.equals("null")){
            sb.append(" and resourceStatus=? ");
            params[index] = resourceStatus;
            index++;
        }
        Object[] args = new Object[index];
        System.arraycopy(params, 0, args, 0, index);
        List<BaseChargeRecord> baseCRList = chargeRecordDao.find(sb.toString(), args);
        List<ChargeRecord> crList = new ArrayList<>();
        for (BaseChargeRecord baseCR : baseCRList) {
            ChargeRecord chargeRecord = new ChargeRecord();
            BeanUtils.copyPropertiesByModel(chargeRecord, baseCR);
            crList.add(chargeRecord);
        }
        return crList;
    }

    @Override
    public void updateCusAllChargeFromTime(String cusId, Date opTime) throws Exception {
        //更新[指定客户]所有[有效的]计费清单记录的[开始计费时间]
        log.info("更新客户[" + cusId + "]所有计费有效、资源状态正常的计费清单记录的开始计费时间");
        StringBuffer sb = new StringBuffer();
        sb.append("update BaseChargeRecord set chargeFrom=? where cusId=? and isValid='1' and resourceStatus='0'");
        try {
            Query query = chargeRecordDao.createQuery(sb.toString(), opTime, cusId);
            int result = query.executeUpdate();
            log.info("成功更新计费清单记录[" + result + "]条");
        } catch (Exception e) {
            log.error("更新客户[" + cusId + "]所有有效的计费清单记录的开始计费时间异常", e);
            throw e;
        }

    }

    @Override
    public List<ChargeRecord> getAllValidChargeRecordByCusId(String cusId, Date currentTime) throws Exception {
        log.info("获取客户[" + cusId + "]下所有有效的计费清单记录");
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseChargeRecord where cusId = ? and isValid = '1' and chargeFrom < ?");
        List<BaseChargeRecord> baseChargeRecordList = chargeRecordDao.find(sb.toString(), cusId, currentTime);
        List<ChargeRecord> chargeRecordList = new ArrayList<>();
        for (BaseChargeRecord baseChargeRecord: baseChargeRecordList) {
            ChargeRecord cr = new ChargeRecord();
            BeanUtils.copyPropertiesByModel(cr, baseChargeRecord);
            chargeRecordList.add(cr);

        }
        return chargeRecordList;
    }

    @Override
    public List<ChargeRecord> getAllValidChargeRecordByCusId(String cusId) throws Exception {
        log.info("获取客户[" + cusId + "]下所有有效的计费清单记录");
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseChargeRecord where cusId = ? and isValid = '1' and resourceStatus='0' ");
        List<BaseChargeRecord> baseChargeRecordList = chargeRecordDao.find(sb.toString(), cusId);
        List<ChargeRecord> chargeRecordList = new ArrayList<>();
        for (BaseChargeRecord baseChargeRecord: baseChargeRecordList) {
            ChargeRecord cr = new ChargeRecord();
            BeanUtils.copyPropertiesByModel(cr, baseChargeRecord);
            chargeRecordList.add(cr);

        }
        return chargeRecordList;
    }

    @Override
    public List<ChargeRecord> getInvalidNormalChargeRecordByCusId(String cusId) throws Exception {
        log.info("获取客户[" + cusId + "]下所有资源状态为正常的无效的计费清单记录");
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseChargeRecord where cusId = ? and isValid = '0' and resourceStatus='0'");
        List<BaseChargeRecord> baseChargeRecordList = chargeRecordDao.find(sb.toString(), cusId);
        List<ChargeRecord> chargeRecordList = new ArrayList<>();
        for (BaseChargeRecord baseChargeRecord: baseChargeRecordList) {
            ChargeRecord cr = new ChargeRecord();
            BeanUtils.copyPropertiesByModel(cr, baseChargeRecord);
            chargeRecordList.add(cr);
        }
        return chargeRecordList;
    }

    @Override
    public List<ChargeRecord> getValidNormalChargeRecordByCusId(String cusId) throws Exception {
        log.info("获取客户[" + cusId + "]下所有资源状态为正常的有效的计费清单记录");
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseChargeRecord where cusId = ? and isValid = '1' and resourceStatus='0'");
        List<BaseChargeRecord> baseChargeRecordList = chargeRecordDao.find(sb.toString(), cusId);
        List<ChargeRecord> chargeRecordList = new ArrayList<>();
        for (BaseChargeRecord baseChargeRecord: baseChargeRecordList) {
            ChargeRecord cr = new ChargeRecord();
            BeanUtils.copyPropertiesByModel(cr, baseChargeRecord);
            chargeRecordList.add(cr);
        }
        return chargeRecordList;
    }

    @Override
    public ChargeRecord getRecycledChargeRecord(String datecenterId, String cusId, String resourceId) {
        log.info("根据资源ID查询该资源对应的无效计费清单记录");
        ChargeRecord chargeRecord = new ChargeRecord();
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseChargeRecord where datecenterId=? and cusId=? and resourceId=? and resourceStatus=? ");
        List<BaseChargeRecord> baseCRList = chargeRecordDao.find(sb.toString(), datecenterId, cusId, resourceId, ChargeConstant.RES_STATUS.RECYCLED);
        if (baseCRList.isEmpty()) {
            return null;
        }
        for (BaseChargeRecord baseCR : baseCRList) {
            BeanUtils.copyPropertiesByModel(chargeRecord, baseCR);
            break;
        }
        return chargeRecord;
    }

    @Override
    public List<ChargeRecord> getChargeRecordsForPostpayResRecover(String cusId, String isValid) {
        log.info("获取客户[" + cusId + "]下所有指定的计费清单记录用于余额变动引起的后续处理");
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseChargeRecord where cusId = ? and isValid = ? and resourceStatus='0'");
        List<BaseChargeRecord> baseChargeRecordList = chargeRecordDao.find(sb.toString(), cusId, isValid);
        List<ChargeRecord> chargeRecordList = new ArrayList<>();
        for (BaseChargeRecord baseChargeRecord: baseChargeRecordList) {
            ChargeRecord cr = new ChargeRecord();
            BeanUtils.copyPropertiesByModel(cr, baseChargeRecord);
            chargeRecordList.add(cr);
        }
        return chargeRecordList;
    }

    @Override
    public ChargeRecord getSpecifiedInValidChargeRecord(String datecenterId, String cusId, String resourceId) {
        log.info("根据资源ID查询该资源对应的无效计费清单记录");
        ChargeRecord chargeRecord = new ChargeRecord();
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseChargeRecord where datecenterId=? and cusId=? and resourceId=? and isValid='0' ");
        List<BaseChargeRecord> baseCRList = chargeRecordDao.find(sb.toString(), datecenterId, cusId, resourceId);
        if (baseCRList.isEmpty()) {
            return null;
        }
        for (BaseChargeRecord baseCR : baseCRList) {
            BeanUtils.copyPropertiesByModel(chargeRecord, baseCR);
            break;
        }
        return chargeRecord;
    }
}
