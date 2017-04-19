package com.eayun.generator.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.CloudResourceUtil;
import com.eayun.generator.constant.CusGeneratorConstant;
import com.eayun.generator.service.NetTestGeneratorService;
import com.eayun.generator.service.SnapTestGeneratorService;
import com.eayun.virtualization.dao.CloudSnapshotDao;
import com.eayun.virtualization.model.BaseCloudSnapshot;
import com.eayun.virtualization.model.BaseCloudVolume;

@Transactional
@Service
public class SnapTestGeneratorServiceImpl implements SnapTestGeneratorService {

	@Autowired
	private CloudSnapshotDao snapDao;
	
	@Autowired
    private  NetTestGeneratorService netTestGeneratorService;
	
	/**
	 * 压力测试下的每个客户项目的每个云硬盘下创建5个备份
	 * @Author: duanbinbin
	 *<li>Date: 2016年12月22日</li>
	 */
	@Override
	public void createBatchSnap() {
		
		List<BaseCloudVolume> volList = netTestGeneratorService.getBatchVolumes();
		if(!volList.isEmpty()){
			for(int i = 0;i < 5 ;i++){
				for(BaseCloudVolume vol : volList){
					
					BaseCloudSnapshot snapshot=new BaseCloudSnapshot();
					snapshot.setSnapId(UUID.randomUUID().toString().replace("-", ""));
					snapshot.setSnapName(netTestGeneratorService.getNameprefix(CusGeneratorConstant.BATCH_SNAP));
					snapshot.setCreateTime(new Date());
					snapshot.setCreateName(null);
					snapshot.setPrjId(vol.getPrjId());
					snapshot.setDcId(vol.getDcId());
					snapshot.setSnapSize(vol.getVolSize());
					snapshot.setSnapType("0");
					snapshot.setVolId(vol.getVolId());
					snapshot.setSnapStatus("AVAILABLE");
					snapshot.setSnapDescription("无描述");
					snapshot.setPayType("2");
					snapshot.setChargeState(CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE);
					snapshot.setIsDeleted("0");
					snapshot.setIsVisable("1");
					
					snapDao.save(snapshot);
				}
			}
		}
	}

}
