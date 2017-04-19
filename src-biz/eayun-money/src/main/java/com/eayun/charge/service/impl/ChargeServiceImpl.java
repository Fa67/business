package com.eayun.charge.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.bean.ChargeConstant;
import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.charge.service.ChargeRecordService;
import com.eayun.charge.service.ChargeService;
import com.eayun.charge.service.ResourceCheckService;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.constant.TransType;
import com.eayun.costcenter.bean.RecordBean;
import com.eayun.costcenter.service.ChangeBalanceService;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.price.service.BillingFactorService;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 根据计费清单执行扣费的扣费Service
 *
 * @Filename: ChargeServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月3日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Service
@Transactional
public class ChargeServiceImpl implements ChargeService {

    @Autowired
    private BillingFactorService billingFactorService;

    @Autowired
    private ChangeBalanceService changeBalanceService;

    @Autowired
    private ChargeRecordService chargeRecordService;

    @Autowired
    private VmService vmService;

    @Autowired
    private CloudFloatIpService ipService;

    @Autowired
    private PoolService lbService;

    @Autowired
    private NetWorkService vpcService;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private VolumeService diskService;

    @Autowired
    private VpnService vpnService;

    @Autowired
    private ResourceCheckService resourceCheckService;
    
    @Autowired
    private RDSInstanceService rdsInstanceService;


    @Override
    public BigDecimal doCharge(Date currentTime, ChargeRecord chargeRecord) {
        BigDecimal cost = BigDecimal.ZERO;
        try {
            //对于有效且资源状态正常的资源做一下判断，资源是否存在
            if(chargeRecord.getIsValid().equals("1") && chargeRecord.getResourceStatus().equals("0")){
                ResourceCheckBean isExisted  = resourceCheckService.isExisted(chargeRecord.getResourceId(), chargeRecord.getResourceType());
                if(!isExisted.isExisted()){
                    //如果资源不存在，直接更新计费状态为9、9，结束本次计费
                    resourceCheckService.updateChargeRecordState(chargeRecord, "9", "9");
                    return cost;
                }
            }
            //获取资源计费时长
            int chargeableDuration = getChargeableDuration(currentTime, chargeRecord);

            //根据计费因子和计费时长获得价格和价格详情
            String billingFactorStr = chargeRecord.getBillingFactorStr();
            JSONObject json = JSONObject.parseObject(billingFactorStr);
            ParamBean paramBean = JSONObject.toJavaObject(json, ParamBean.class);
            paramBean.setDcId(chargeRecord.getDatecenterId());
            paramBean.setPayType(PayType.PAYAFTER);
            paramBean.setNumber(1);
            paramBean.setCycleCount(chargeableDuration);
            PriceDetails priceDetail = billingFactorService.getPriceDetails(paramBean);

            cost = priceDetail.getTotalPrice();

            //调用交易记录service插入一条扣费记录
            RecordBean tradingRecord = generateTradingRecord(currentTime, chargeRecord, paramBean, priceDetail);
            changeBalanceService.changeBalanceByCharge(tradingRecord);

            //更新下次开始计费时间和计费清单记录生效状态
            chargeRecord.setChargeFrom(currentTime);
            String resStatus = chargeRecord.getResourceStatus();
            if (resStatus.equals(ChargeConstant.RES_STATUS.DELETED) ||
                    resStatus.equals(ChargeConstant.RES_STATUS.UPGRADED) ||
                    resStatus.equals(ChargeConstant.RES_STATUS.RECYCLED)) {
                //如果是已经删除的资源删除后的计费，则需要计费后将记录设置为不生效。变配、放入回收站的同理。
                chargeRecord.setIsValid(ChargeConstant.RECORD_INVALID);
            }
            chargeRecordService.updateChargeRecord(chargeRecord);

        } catch (Exception e) {
            //一旦上面任一个方法有异常被捕获，则抛出RuntimeException
            throw new RuntimeException(e);
        }
        return cost;
    }

    private RecordBean generateTradingRecord(Date currentTime, ChargeRecord chargeRecord, ParamBean paramBean, PriceDetails priceDetail) throws Exception {
        RecordBean rb = new RecordBean();

        String resType = chargeRecord.getResourceType();
        String resName = "";
        String resStatus = chargeRecord.getResourceStatus();
        String resId = chargeRecord.getResourceId();
        String resTypeCN = escapeResourceType(resType,resId);
        String dcId = chargeRecord.getDatecenterId();

        if(resStatus.equals(ChargeConstant.RES_STATUS.DELETED) || resStatus.equals(ChargeConstant.RES_STATUS.RECYCLED)){
            resName = chargeRecord.getResourceName()==null?"":chargeRecord.getResourceName();
        }else {
            resName = getResourceNameByTypeAndId(resType, chargeRecord.getResourceId());
        }

        if (resType.equals(ResourceType.VM)) {
            String osName = getOSNameByVmId(resId);
            rb.setImageName(osName);
        } else if(resType.equals(ResourceType.VPN)){
            String vpnInfo = getVPNInfoById(resId);
            rb.setVpnInfo(vpnInfo);

        }
        rb.setExchangeTime(currentTime);
        rb.setEcscRemark("消费-" + resTypeCN + "-按需付费");
        rb.setEcmcRemark("消费-" + resTypeCN + "-按需付费");
        rb.setExchangeMoney(priceDetail.getTotalPrice());
        rb.setProductName(resTypeCN + "-按需付费");
        rb.setOrderNo(chargeRecord.getOrderNumber());
        rb.setResourceId(resId);
        rb.setResourceName(resName);
        rb.setPayType(PayType.PAYAFTER);
        rb.setIncomeType("2");
        rb.setMonStart(chargeRecord.getChargeFrom());
        //费用截止时间，针对删除，放入回收站，则应当是操作时间，
        if (resStatus.equals(ChargeConstant.RES_STATUS.DELETED)
                || resStatus.equals(ChargeConstant.RES_STATUS.RECYCLED)) {
            rb.setMonEnd(chargeRecord.getChangeTime());
        } else {
            rb.setMonEnd(currentTime);
        }
        rb.setParamBean(paramBean);
        rb.setPriceDetails(priceDetail);
        rb.setCusId(chargeRecord.getCusId());
        rb.setResourceType(resType);
        rb.setOperType(TransType.EXPEND);
        rb.setDcId(dcId);
        return rb;
    }

    private String getOSNameByVmId(String resId) throws Exception {
        String osName = "";
        try {
            osName = vmService.getOSNameByVmId(resId);
        } catch (Exception e) {
            throw e;
        }
        return osName;
    }

    /**
     * 根据资源类型和资源ID查询资源名称
     *
     * @param resourceType
     * @param resourceId
     * @return
     */
    private String getResourceNameByTypeAndId(String resourceType, String resourceId) throws Exception {
        String resName = "";
        if (resourceType.equals(ResourceType.VM)) {
            resName = getVmNameById(resourceId);
        } else if (resourceType.equals(ResourceType.VDISK)) {
            resName = getVdisNameById(resourceId);
        } else if (resourceType.equals(ResourceType.DISKSNAPSHOT)) {
            resName = getDiskSnapshotNameById(resourceId);
        } else if (resourceType.equals(ResourceType.NETWORK)) {
            resName = getVPCNameById(resourceId);
        } else if (resourceType.equals(ResourceType.QUOTAPOOL)) {
            resName = getLBNameById(resourceId);
        } else if (resourceType.equals(ResourceType.FLOATIP)) {
            resName = getIpById(resourceId);
        } else if (resourceType.equals(ResourceType.VPN)) {
            resName = getVPNNameById(resourceId);
        } else if(resourceType.equals(ResourceType.RDS)){
        	resName = getRDSNameById(resourceId);
        }
        return resName;
    }
    private String getRDSNameById(String resourceId) throws Exception {
        String rdsName = "";
        try{
        	ResourceCheckBean resourceCheckBean=rdsInstanceService.isExistsByResourceId(resourceId);
        	if(resourceCheckBean!=null){
        		rdsName=resourceCheckBean.getResourceName();
        	}
        }catch (Exception e){
            throw e;
        }
        return rdsName;
    }
    private String getVPNNameById(String resourceId) throws Exception {
        String vpnName = "";
        try{
            vpnName = vpnService.getVpnNameById(resourceId);
        }catch (Exception e){
            throw e;
        }
        return vpnName;
    }

    private String getVPNInfoById(String resourceId) throws Exception{
        // e.g “本端VPC：Private_01(192.168.3.3)，对端网关：bbb(192.168.2.3)”
        String vpnInfo = "";
        try{
            vpnInfo = vpnService.getVpnInfoById(resourceId);
        }catch (Exception e){
            throw e;
        }
        return vpnInfo;
    }

    private String getIpById(String resourceId) throws Exception {
        String ip = "";
        try {
            ip = ipService.getIpInfoById(resourceId);
        } catch (Exception e) {
            throw e;
        }
        return ip;
    }

    private String getLBNameById(String resourceId) throws Exception {
        String lbName = "";
        try {
            lbName = lbService.getLBNameById(resourceId);
        } catch (Exception e) {
            throw e;
        }
        return lbName;
    }

    private String getVPCNameById(String resourceId) throws Exception {
        String vpcName = "";
        try {
            vpcName = vpcService.getVPCNameById(resourceId);
        } catch (Exception e) {
            throw e;
        }
        return vpcName;
    }

    private String getDiskSnapshotNameById(String resourceId) throws Exception {
        String snapshotName = "";
        try {
            snapshotName = snapshotService.getSnapshotNameById(resourceId);
        } catch (Exception e) {
            throw e;
        }
        return snapshotName;
    }


    private String getVdisNameById(String resourceId) throws Exception {
        String diskName = "";
        try {
            diskName = diskService.getVolumeNameById(resourceId);
        } catch (Exception e) {
            throw e;
        }
        return diskName;
    }

    private String getVmNameById(String resourceId) throws Exception {
        String vmName = "";
        try {
            CloudVm vm = vmService.getById(resourceId);
            if (vm != null) {
                vmName = vm.getVmName();
            }
        } catch (Exception e) {
            throw e;
        }
        return vmName;
    }
    
    private String escapeResourceType(String resourceType,String resId) {//rds新增参数resId,为了判断rds是主库还是从库
        String resTypeCN = "";
        if (resourceType.equals(ResourceType.VM)) {
            resTypeCN = "云主机";
        } else if (resourceType.equals(ResourceType.VDISK)) {
            resTypeCN = "云硬盘";
        } else if (resourceType.equals(ResourceType.DISKSNAPSHOT)) {
            resTypeCN = "云硬盘备份";
        } else if (resourceType.equals(ResourceType.NETWORK)) {
            resTypeCN = "私有网络";
        } else if (resourceType.equals(ResourceType.QUOTAPOOL)) {
            resTypeCN = "负载均衡器";
        } else if (resourceType.equals(ResourceType.FLOATIP)) {
            resTypeCN = "弹性公网IP";
        } else if (resourceType.equals(ResourceType.VPN)) {
            resTypeCN = "VPN";
        } else if(resourceType.equals(ResourceType.RDS)){
        	boolean result=rdsInstanceService.isMasterRdsInstance(resId);
        	if(result){
        		resTypeCN = "MySQL主库实例";
        	}else{
        		resTypeCN = "MySQL从库实例";
        	}
        }
        return resTypeCN;
    }

    private int getChargeableDuration(Date currentTime, ChargeRecord chargeRecord) {
        int duration = 0;
        String resStatus = chargeRecord.getResourceStatus();
        if (resStatus.equals(ChargeConstant.RES_STATUS.NORMAL)
                || resStatus.equals(ChargeConstant.RES_STATUS.UPGRADED)) {
            Date chargeFrom = chargeRecord.getChargeFrom();
            long timeSpan = currentTime.getTime() - chargeFrom.getTime();
            double hours = (double) timeSpan / (1000 * 60 * 60);
            duration = (int) Math.ceil(hours);
        } else if (resStatus.equals(ChargeConstant.RES_STATUS.DELETED)
                || resStatus.equals(ChargeConstant.RES_STATUS.RECYCLED)) {
            Date chargeFrom = chargeRecord.getChargeFrom();
            Date chargeTo = chargeRecord.getChangeTime();
            long timeSpan = chargeTo.getTime() - chargeFrom.getTime();
            double hours = (double) timeSpan / (1000 * 60 * 60);
            duration = (int) Math.ceil(hours);
        }
        return duration;
    }
}
