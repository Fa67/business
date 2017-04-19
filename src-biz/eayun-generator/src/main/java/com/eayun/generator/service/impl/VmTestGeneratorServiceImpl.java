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
import com.eayun.generator.service.VmTestGeneratorService;
import com.eayun.virtualization.dao.CloudVmDao;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.BaseCloudVm;

@Transactional
@Service
public class VmTestGeneratorServiceImpl implements VmTestGeneratorService {
	
	@Autowired
	private CloudVmDao cloudVmDao;
	
	@Autowired
    private  NetTestGeneratorService netTestGeneratorService;

	/**
	 * 每个压力测试的客户项目的每个子网下创建1个云主机
	 * @Author: duanbinbin
	 *<li>Date: 2016年12月22日</li>
	 */
	@Override
	public void createBatchVm() {
		List<BaseCloudSubNetWork> subList = netTestGeneratorService.getBatchSubnets();
		if(!subList.isEmpty()){
			String flavorId = "fefa95aa-d68b-4129-b2c1-0229f491e916";
			StringBuffer sb = new StringBuffer("SELECT flavor_id FROM cloud_flavor where dc_id=? limit 1 ");
			javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(sb.toString(), new Object[]{subList.get(0).getDcId()});
	        List<String> list = query.getResultList();
	        if(!list.isEmpty()){
	        	flavorId = list.get(0);
	        }
			int i=0;
			for(BaseCloudSubNetWork sub : subList){
				
				BaseCloudVm tempVm = new BaseCloudVm();
				tempVm.setVmId(UUID.randomUUID().toString().replace("-", ""));
				tempVm.setVmName(netTestGeneratorService.getNameprefix(CusGeneratorConstant.BATCH_VM));
				tempVm.setVmStatus("ACTIVE");
				tempVm.setHostId("74ea0139c3076f2a090da2b712c931629cfed9bc126370590ce47708");
				tempVm.setHostName("node-26.eayun.cn");
				tempVm.setCreateName(null);
				tempVm.setFlavorId(flavorId);
				tempVm.setCreateTime(new Date());
				tempVm.setDcId(sub.getDcId());
				tempVm.setPrjId(sub.getPrjId());
				tempVm.setNetId(sub.getNetId());
				tempVm.setOsType("0007002002001");
				tempVm.setSysType("0007002002001002");
				tempVm.setFromImageId("90b481d6-4130-4452-8db0-43721cfa1d06");
				tempVm.setIsDeleted("0");
				tempVm.setIsVisable("1");
				tempVm.setChargeState(CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE);
				if(i%2==0){
					tempVm.setEndTime(DateUtil.getExpirationDate(tempVm.getCreateTime(), i+1, DateUtil.PURCHASE));
					tempVm.setPayType("1");
				}else{
					tempVm.setPayType("2");
				}
				if("0".equals(sub.getSubnetType())){//自管
					tempVm.setSelfPortId("aee2b56a-088f-4763-b2ce-6ae5db0a11d8");
					tempVm.setSelfSubnetId(sub.getSubnetId());
				}else{//受管
					tempVm.setPortId("6dcf2029-5c15-4ee5-a41f-1ceba913b710");
					tempVm.setSubnetId(sub.getSubnetId());
				}
				tempVm.setVmIp("10.0.0.110");
				tempVm.setSelfIp(sub.getGatewayIp());
				tempVm.setVmFrom("publicImage");
				cloudVmDao.save(tempVm);
				i++;
			}
		}
		
	}

}
