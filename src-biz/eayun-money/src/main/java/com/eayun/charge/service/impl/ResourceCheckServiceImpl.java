package com.eayun.charge.service.impl;

import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.charge.service.ChargeRecordService;
import com.eayun.charge.service.ResourceCheckService;
import com.eayun.common.constant.ResourceType;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.virtualization.service.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by ZH.F on 2016/10/17.
 */
@Service
@Transactional
public class ResourceCheckServiceImpl implements ResourceCheckService {

    private Logger log = LoggerFactory.getLogger(ResourceCheckService.class);

    @Autowired
    private ChargeRecordService chargeRecordService;

    @Autowired
    private NetWorkService netWorkService;

    @Autowired
    private PoolService poolService;

    @Autowired
    private VpnService vpnService;

    @Autowired
    private CloudFloatIpService ipService;

    @Autowired
    private VolumeService volumeService;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private VmService vmService;
    
    @Autowired
    private RDSInstanceService rdsInstanceService;

    @Override
    public ResourceCheckBean isExisted(String resId, String resType) {
        //TODO 根据资源类型调用指定资源的判断是否存在的接口
        ResourceCheckBean resourceCheckBean = null;
        if(ResourceType.VM.equals(resType)){
            resourceCheckBean = vmService.isExistsByResourceId(resId);
        }else if(ResourceType.VDISK.equals(resType)){
            resourceCheckBean = volumeService.isExistsByResourceId(resId);
        }else if(ResourceType.DISKSNAPSHOT.equals(resType)){
            resourceCheckBean = snapshotService.isExistsByResourceId(resId);
        }else if(ResourceType.NETWORK.equals(resType)){
            resourceCheckBean = netWorkService.isExistsByResourceId(resId);
        }else if(ResourceType.QUOTAPOOL.equals(resType)){
            resourceCheckBean = poolService.isExistsByResourceId(resId);
        }else if(ResourceType.FLOATIP.equals(resType)){
            resourceCheckBean = ipService.isExistsByResourceId(resId);
        }else if(ResourceType.VPN.equals(resType)){
            resourceCheckBean = vpnService.isExistsByResourceId(resId);
        } else if(ResourceType.RDS.equals(resType)){
        	resourceCheckBean = rdsInstanceService.isExistsByResourceId(resId);
        }
        return resourceCheckBean;
    }

    @Override
    public void updateChargeRecordState(ChargeRecord chargeRecord, String isValid, String resStatus) {
        try {
            chargeRecord.setIsValid(isValid);
            chargeRecord.setResourceStatus(resStatus);
            chargeRecordService.updateChargeRecord(chargeRecord);
        } catch (Exception e) {
            log.error("更新计费清单记录isValid和resourceStatus失败",e);
        }

    }
}
