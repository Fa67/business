package com.eayun.generator.service;

public interface VolumeTestGeneratorService {

	/**
	 * 压力测试的每个客户项目下创建50个云硬盘
	 * @Author: duanbinbin
	 * @param isSystem
	 *<li>Date: 2016年12月22日</li>
	 */
	void createBatchVolume(boolean isSystem);

}
