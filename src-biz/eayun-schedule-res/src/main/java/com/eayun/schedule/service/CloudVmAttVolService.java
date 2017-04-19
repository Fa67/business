package com.eayun.schedule.service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.model.CloudVolume;

public interface CloudVmAttVolService {
	
	public String pop (String key);
	
	public JSONObject getVm(CloudVolume cloudVolume) throws Exception;
	
	public JSONObject getVol(CloudVolume cloudVolume) throws Exception;
	
	public void attach (CloudVolume cloudVolume);
	
	public boolean push (String key,String value);
	
	public long size (String groupKey);

	/**
	 * 同步创建中的云主机、云硬盘
	 * @param cloudVm
	 * @return
	 * @throws Exception
	 */
	public boolean syncVmAndVolumeInBuild(CloudVm cloudVm) throws Exception;

	/**
	 * 同步创建中的云硬盘
	 * @param cloudVm
	 * @return
	 * @throws Exception
	 */
	public boolean syncVolumeInBuild(CloudVm cloudVm) throws Exception;
	
	/**
	 * 同步挂载
	 * @param cloudVm
	 * @return
	 * @throws Exception
	 */
	public boolean syncVolumeAttchVm(CloudVm cloudVm) throws Exception;
	
	/**
	 * 查看是否同步全部完成
	 * @param cloudVm
	 * @return
	 * @throws Exception 
	 */
	public boolean syncAllSuccess(CloudVm cloudVm) throws Exception;


}
