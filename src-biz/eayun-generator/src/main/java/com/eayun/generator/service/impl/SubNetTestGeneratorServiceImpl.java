package com.eayun.generator.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.generator.constant.CusGeneratorConstant;
import com.eayun.generator.service.NetTestGeneratorService;
import com.eayun.generator.service.SubNetTestGeneratorService;
import com.eayun.virtualization.dao.CloudSubNetWorkDao;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudSubNetWork;

@Transactional
@Service
public class SubNetTestGeneratorServiceImpl implements
		SubNetTestGeneratorService {
	
	private static final Logger log = LoggerFactory.getLogger(SubNetTestGeneratorServiceImpl.class);
	
	@Autowired
    private  NetTestGeneratorService netTestGeneratorService;
	
	@Autowired
	private CloudSubNetWorkDao subNetDao;

	/**
	 * 每个压力测试下的客户项目的每个私有网络下创建5个子网
	 * @Author: duanbinbin
	 *<li>Date: 2016年12月22日</li>
	 */
	@Override
	public void createBatchSubnet() {
		
		List<BaseCloudNetwork> netList = netTestGeneratorService.getBatchNets();
		if(!netList.isEmpty()){
			for(int i = 0;i < 5;i++){
				for(BaseCloudNetwork net : netList){
					BaseCloudSubNetWork baseSubNet=new BaseCloudSubNetWork();
					baseSubNet.setPrjId(net.getPrjId());
					baseSubNet.setDcId(net.getDcId());
					baseSubNet.setNetId(net.getNetId());
					
					baseSubNet.setSubnetId(UUID.randomUUID().toString().replace("-", ""));
					baseSubNet.setSubnetName(netTestGeneratorService.getNameprefix(CusGeneratorConstant.BATCH_SUB));
					baseSubNet.setCreateTime(new Date());
					baseSubNet.setGatewayIp("10.0.0.1");
					baseSubNet.setIpVersion("4");
					baseSubNet.setDns("114.114.114.114");
					baseSubNet.setIsForbiddengw("0");
			    	baseSubNet.setInLabelRuleId(UUID.randomUUID().toString().replace("-", ""));
			    	baseSubNet.setOutLabelRuleId(UUID.randomUUID().toString().replace("-", ""));
			        baseSubNet.setPooldata("10.0.0.2,10.0.255.254");
			        
			        baseSubNet.setCidr("10.0.0.0/16");
			        if(i%2==0){
			        	baseSubNet.setSubnetType("0");
			        }else{
			        	baseSubNet.setSubnetType("1");
			        }
			        //创建子网
			        subNetDao.save(baseSubNet);
				}
			}
		}
	}
	
}
