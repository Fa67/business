package com.eayun.generator.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.generator.constant.CusGeneratorConstant;
import com.eayun.generator.service.NetTestGeneratorService;
import com.eayun.generator.service.VolumeTestGeneratorService;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudProject;

@Transactional
@Service
public class VolumeTestGeneratorServiceImpl implements
		VolumeTestGeneratorService {
	
	@Autowired
    private  NetTestGeneratorService netTestGeneratorService;

	@Autowired
	private CloudVolumeDao volumeDao;
	
	/**
	 * 压力测试的每个客户项目下创建50个云硬盘
	 * @Author: duanbinbin
	 *<li>Date: 2016年12月22日</li>
	 */
	@Override
	public void createBatchVolume(boolean isSystem) {
		
		List<CloudProject> prjList = netTestGeneratorService.getTestPrj();
		if(!prjList.isEmpty()){
			for(CloudProject pro:prjList){
				for(int i = 0;i<50;i++){
					
					
					BaseCloudVolume volume=new BaseCloudVolume();
					volume.setVolId(UUID.randomUUID().toString().replace("-", ""));
					volume.setVolName(netTestGeneratorService.getNameprefix(CusGeneratorConstant.BATCH_VOLUME));
					volume.setCreateTime(new Date());
					volume.setCreateName(pro.getCustomerName());
					volume.setDcId(pro.getDcId());
					volume.setPrjId(pro.getProjectId());
					if(isSystem){
						volume.setVolBootable("1");
					}else{
						volume.setVolBootable("0");
					}
					volume.setDiskFrom("blank");
					volume.setVolSize(20);
					volume.setVolStatus("AVAILABLE");
					volume.setVolDescription("不描述");
					volume.setIsDeleted("0");
					volume.setChargeState(CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE);
					if(i%2==0){
						volume.setPayType("1");
						volume.setEndTime(DateUtil.getExpirationDate(volume.getCreateTime(), i+1, DateUtil.PURCHASE));
					}else{
						volume.setPayType("2");
					}
					volume.setIsVisable("1");
					volumeDao.save(volume);
				}
			}
		}
	}

}
