package com.eayun.schedule.service;


import com.alibaba.fastjson.JSONObject;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudVm;

public interface CloudVmService {
	
	/**
	 * 从消息队列取出对应key的的其中一条
	 * @param groupKey
	 * @return
	 */
	public String pop(String groupKey);
	
	/**
	 * 查询当前队列的长度
	 * @param groupKey
	 * @return
	 */
	public long size (String groupKey);
	
	/**
	 * 重新放入队列
	 * @param groupKey
	 * @param value
	 * @return
	 */
	public	boolean push(String groupKey,String value);
	
	/**
	 * 获取底层指定ID的资源<br>
	 * ------------------
	 * @author zhouhaitao
	 * 
	 * @param value
	 * @param json
	 * 
	 * @return
	 */
	public JSONObject get(JSONObject valueJson) throws Exception;
	
	/**
	 * 删除云主机
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @param isSoftDelete
	 * 				是否是软删除
	 * @return
	 */
	public boolean deleteVm(CloudVm vm,boolean isSoftDelete);
	
	/**
	 * 修改云主机信息
	 * @param cloudVm
	 * @return
	 */
	public boolean  updateVm(BaseCloudVm cloudVm);
	
	/**
	 * 同步底层项目下的云主机
	 * -----------------
	 * @param dataCenter
	 * @param prjId
	 */
	public void synchData(BaseDcDataCenter dataCenter,String prjId) throws Exception;
	
	/**
	 * 同步系统盘
	 * @author zhouhaitao
	 * @param volume
	 */
	public void addVoluneDB(BaseCloudVolume volume);
	
	/**
	 * 确认调整
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 */
	public void resized(CloudVm cloudVm) throws Exception;
	
	/**
	 * 同步创建中的云主机
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 */
	public boolean syncVmInBuild(CloudVm cloudVm ) throws Exception;
	
	/**
	 * 删除云主机
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @param isSoftDelete
	 * 				是否是软删除
	 * @return
	 */
	public boolean resumeVm(CloudVm vm) throws Exception;
	
	/**
	 * 云主机升级成功
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 */
	public void resize(CloudVm cloudVm) throws Exception;
	
}
