package com.eayun.balancechange.service.impl;

import com.eayun.accesskey.service.AccessKeyService;
import com.eayun.balancechange.service.PostpayResHandlerService;
import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.charge.service.ChargeRecordService;
import com.eayun.charge.service.ResourceCheckService;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.redis.JedisUtil;
import com.eayun.customer.model.CusServiceState;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.notice.model.MessageUserResour;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.obs.service.ObsOpenService;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.service.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 账户余额变动后对后付费资源的处理Service实现类
 *
 * @Filename: PostpayResHandlerServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月11日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Service
public class PostpayResHandlerServiceImpl implements PostpayResHandlerService {
    private static Logger log = LoggerFactory.getLogger(PostpayResHandlerServiceImpl.class);

    @Autowired
    private ChargeRecordService chargeRecordService;
    @Autowired
    private VmService vmService;
    @Autowired
    private VolumeService volumeService;
    @Autowired
    private SnapshotService snapshotService;
    @Autowired
    private NetWorkService networkService;
    @Autowired
    private PoolService lbService;
    @Autowired
    private CloudFloatIpService ipService;
    @Autowired
    private VpnService vpnService;
    @Autowired
    private ObsOpenService obsOpenService;
    @Autowired
    private AccessKeyService accessKeyService;
    @Autowired
    private JedisUtil jedisUtil;
    @Autowired
    private ResourceCheckService resourceCheckService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private MessageCenterService msgCenterService;
    @Autowired
    private RDSInstanceService rdsInstanceService;

    @Override
    public void recoverPostPayResource(String cusId) {
        log.info("执行恢复客户[" + cusId + "]下的所有服务受限的后付费资源");
        //1.根据客户ID查询计费清单记录中记录无效、资源状态正常的记录（此类状态的计费清单是由于限制资源服务导致）
        List<ChargeRecord> list = chargeRecordService.getChargeRecordsForPostpayResRecover(cusId, "0");
        Customer customer = customerService.findCustomerById(cusId);
        List<MessageUserResour> messageUserResoursList = new ArrayList<>();
        //2.根据资源ID、资源类型，调用业务接口恢复资源使用，资源计费状态要置为“0”，即正常
        for (ChargeRecord record : list) {
            String resType = record.getResourceType();
            String resId = record.getResourceId();

            //恢复资源前，判断资源似乎否存在，如果不存在，则将指定计费清单的状态置为isValid=9, resourceStatus=9，并跳过本资源的恢复
            ResourceCheckBean resourceCheckBean = resourceCheckService.isExisted(resId, resType);
            if(!resourceCheckBean.isExisted()){
                resourceCheckService.updateChargeRecordState(record, "9", "9");
                continue;
            }
            boolean recoverSucceed = true;
            if (ResourceType.VM.equals(resType)) {
                recoverSucceed = recoverVm(resId, true);
            } else if (ResourceType.VDISK.equals(resType)) {
                recoverSucceed = recoverVolume(resId);
            } else if (ResourceType.DISKSNAPSHOT.equals(resType)) {
                recoverSucceed = recoverSnapshot(resId);
            } else if (ResourceType.NETWORK.equals(resType)) {
                recoverSucceed = recoverNetwork(resId, true);
            } else if (ResourceType.QUOTAPOOL.equals(resType)) {
                recoverSucceed = recoverLoadBalance(resId, true);
            } else if (ResourceType.FLOATIP.equals(resType)) {
                recoverSucceed = recoverIp(resId);
            } else if (ResourceType.VPN.equals(resType)) {
                recoverSucceed = recoverVPN(resId, true);
            } else if(ResourceType.RDS.equals(resType)){
            	 recoverSucceed = recoverRDS(resId, true);
            }

            if(!recoverSucceed){
                MessageUserResour messageUserResour = new MessageUserResour();
                messageUserResour.setCusName(customer.getCusOrg());
                messageUserResour.setRecoveryTime(new Date());
                String resTypeCN  = getResourceTypeCN(resType);
                messageUserResour.setResourType(resTypeCN);
                messageUserResour.setResourId(resId);

                String resName = resourceCheckBean.getResourceName()==null?"":resourceCheckBean.getResourceName();
                messageUserResour.setResourname(resName);

                messageUserResoursList.add(messageUserResour);
            }
        }

        //恢复obs的ak/sk使用
        CusServiceState cusServiceState = obsOpenService.getObsByCusId(cusId);
        if (cusServiceState != null) {
            String obsState = cusServiceState.getObsState();
            if ("1".equals(obsState)) {
                //如果该客户已开通OBS，则需要恢复资源时恢复ak/sk的使用
                try {
                    log.info("执行恢复客户[" + cusId + "]下AK/SK使用开始");
                    accessKeyService.resumeAkExceptDefaultByCusId(cusId,"0");
                    log.info("执行恢复客户[" + cusId + "]下AK/SK使用完成");
                } catch (Exception e) {
                    MessageUserResour messageUserResour = new MessageUserResour();
                    messageUserResour.setCusName(customer.getCusOrg());
                    messageUserResour.setRecoveryTime(new Date());
                    messageUserResour.setResourType("对象存储");
                    messageUserResour.setResourId("对象存储");
                    messageUserResour.setResourname("对象存储");

                    messageUserResoursList.add(messageUserResour);
                    log.error("执行恢复客户[" + cusId + "]下AK/SK使用失败", e);
                }
            }
        }

        if(!messageUserResoursList.isEmpty()){
            //发送恢复失败的消息给后台管理员和运维人员
            msgCenterService.userResourRecoveryFail(messageUserResoursList);
        }

        try {
            //为了控制客户欠费超限只发送一次按需付费的资源被停用的消息，需要翔宇在redis中写一个key来表示是否发送过，如果发送过消息，则置为1，当客户恢复资源后，在这里把key删除掉
            jedisUtil.delete(RedisKey.OUT_RENTENTION_TIME + cusId);
        } catch (Exception e) {
            log.error("后付费资源恢复中删除客户是否发送过消息的RedisKey异常", e);
        }
    }

    private String getResourceTypeCN(String resType) {
        String resTypeCN = "";
        if (ResourceType.VM.equals(resType)) {
            resTypeCN = "云主机";
        } else if (ResourceType.VDISK.equals(resType)) {
            resTypeCN = "云硬盘";
        } else if (ResourceType.DISKSNAPSHOT.equals(resType)) {
            resTypeCN = "云硬盘备份";
        } else if (ResourceType.NETWORK.equals(resType)) {
            resTypeCN = "私有网络";
        } else if (ResourceType.QUOTAPOOL.equals(resType)) {
            resTypeCN = "负载均衡器";
        } else if (ResourceType.FLOATIP.equals(resType)) {
            resTypeCN = "弹性公网IP";
        } else if (ResourceType.VPN.equals(resType)) {
            resTypeCN = "VPN";
        } else if(ResourceType.RDS.equals(resType)){
        	resTypeCN ="MySQL";
        }
        return resTypeCN;
    }
    private boolean recoverRDS(String resId, boolean isResumable) {
        log.info("恢复RDS[" + resId + "]开始");
        boolean recoverSucceed = false;
        try {
        	rdsInstanceService.modifyStateForRdsInstance(resId, "0", null,false,isResumable);
            log.info("恢复RDS[" + resId + "]完成");
            recoverSucceed = true;
        } catch (Exception e) {
            log.error("恢复RDS[" + resId + "]失败", e);
        }
        return recoverSucceed;
    }
    private boolean recoverVPN(String resId, boolean isResumable) {
        log.info("恢复VPN[" + resId + "]开始");
        boolean recoverSucceed = false;
        try {
            vpnService.modifyStateForVPN(resId, "0", null, false, isResumable);
            log.info("恢复VPN[" + resId + "]完成");
            recoverSucceed = true;
        } catch (Exception e) {
            log.error("恢复VPN[" + resId + "]失败", e);
        }
        return recoverSucceed;
    }

    private boolean recoverIp(String resId) {
        log.info("恢复弹性公网IP[" + resId + "]开始");
        boolean recoverSucceed = false;
        try {
            ipService.modifyStateForFloatIp(resId, "0", null);
            log.info("恢复弹性公网IP[" + resId + "]完成");
            recoverSucceed = true;
        } catch (Exception e) {
            log.error("恢复弹性公网IP[" + resId + "]失败", e);
        }
        return recoverSucceed;
    }

    private boolean recoverLoadBalance(String resId, boolean isResumable) {
        log.info("恢复负载均衡[" + resId + "]开始");
        boolean recoverSucceed = false;
        try {
            lbService.modifyStateForLdPool(resId, "0", null, false, isResumable);
            log.info("恢复负载均衡[" + resId + "]完成");
            recoverSucceed = true;
        } catch (Exception e) {
            log.error("恢复负载均衡[" + resId + "]失败", e);
        }
        return recoverSucceed;
    }

    private boolean recoverNetwork(String resId, boolean isResumable) {
        log.info("恢复私有网络[" + resId + "]开始");
        boolean recoverSucceed = false;
        try {
            networkService.modifyStateForNetWork(resId, "0", null, false, isResumable);
            log.info("恢复私有网络[" + resId + "]完成");
            recoverSucceed = true;
        } catch (Exception e) {
            log.error("恢复私有网络[" + resId + "]失败", e);
        }
        return recoverSucceed;
    }

    private boolean recoverSnapshot(String resId) {
        log.info("恢复云硬盘备份[" + resId + "]开始");
        boolean recoverSucceed = false;
        try {
            snapshotService.modifyStateForSnap(resId, "0", false);
            log.info("恢复云硬盘备份[" + resId + "]完成");
            recoverSucceed = true;
        } catch (Exception e) {
            log.error("恢复云硬盘备份[" + resId + "]失败", e);
        }
        return recoverSucceed;
    }

    private boolean recoverVolume(String resId) {
        log.info("恢复云硬盘[" + resId + "]开始");
        boolean recoverSucceed = false;
        try {
            volumeService.modifyStateForVol(resId, "0", false);
            log.info("恢复云硬盘[" + resId + "]完成");
            recoverSucceed = true;
        } catch (Exception e) {
            log.error("恢复云硬盘[" + resId + "]失败", e);
        }
        return recoverSucceed;
    }

    private boolean recoverVm(String resId, boolean isResumable) {
        log.info("恢复云主机[" + resId + "]开始");
        boolean recoverSucceed = false;
        try {
            vmService.modifyStateForVm(resId, "0", null, false, isResumable);
            CloudVolume cloudVolume = volumeService.getOsVolumeByVmId(resId);
            if (cloudVolume != null && cloudVolume.getVolId() != null) {
                volumeService.modifyStateForVol(cloudVolume.getVolId(), "0", false);
            }
            log.info("恢复云主机[" + resId + "]完成");
            recoverSucceed = true;
        } catch (Exception e) {
            log.error("恢复云主机[" + resId + "]失败", e);
        }
        return recoverSucceed;
    }

    @Override
    public void modifyResourceStatus(String cusId) {
        log.info("执行恢复客户[" + cusId + "]下的所有的后付费资源的资源表中的计费状态（余额不足->正常）");
        //1.根据客户ID查询计费清单记录中记录有效、资源状态正常的记录（此类状态的计费清单是由于限制资源服务导致）
        List<ChargeRecord> list = chargeRecordService.getChargeRecordsForPostpayResRecover(cusId, "1");
        //2.根据资源ID、资源类型，调用业务接口恢复资源使用，资源计费状态要置为“0”，即正常，不需要恢复服务使用
        for (ChargeRecord record : list) {
            String resType = record.getResourceType();
            String resId = record.getResourceId();
            if (ResourceType.VM.equals(resType)) {
                recoverVm(resId, false);
            } else if (ResourceType.VDISK.equals(resType)) {
                recoverVolume(resId);
            } else if (ResourceType.DISKSNAPSHOT.equals(resType)) {
                recoverSnapshot(resId);
            } else if (ResourceType.NETWORK.equals(resType)) {
                recoverNetwork(resId, false);
            } else if (ResourceType.QUOTAPOOL.equals(resType)) {
                recoverLoadBalance(resId, false);
            } else if (ResourceType.FLOATIP.equals(resType)) {
                recoverIp(resId);
            } else if (ResourceType.VPN.equals(resType)) {
                recoverVPN(resId, false);
            } else if(ResourceType.RDS.equals(resType)){
            	recoverRDS(resId,false);
            }
        }
    }


}
