package com.eayun.generator.service;

import java.util.List;

import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudProject;

public interface NetTestGeneratorService {

	void createBatchNet();

	/**
	 * 查询出来本次压力测试批量创建的客户的项目
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年12月21日</li>
	 */
	List<CloudProject> getTestPrj();

	String getNameprefix(String type);

	List<BaseCloudNetwork> getBatchNets();

	List<BaseCloudVolume> getBatchVolumes();

	List<BaseCloudSubNetWork> getBatchSubnets();

}
