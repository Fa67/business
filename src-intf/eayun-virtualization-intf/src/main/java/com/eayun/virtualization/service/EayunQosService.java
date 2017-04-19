package com.eayun.virtualization.service;

import com.eayun.eayunstack.model.EayunQos;
import com.eayun.virtualization.model.BaseCloudRoute;

public interface EayunQosService {
	
	/**
	 * 新增EayunQos 信息
	 * @param route
	 * @return
	 */
	public void createQos(BaseCloudRoute route);
	
	/**
	 * 修改EayunQos 信息
	 * @param route
	 * @return
	 */
	public void updateQos(BaseCloudRoute route,int perRate);
	
	/**
	 * 根据qosId查询底层的QOS
	 * @param route
	 * @return
	 */
	public EayunQos getQosById(BaseCloudRoute route);
	
	/**
	 * 修改QOS的目标路由 target_id
	 * @param route
	 */
	public void modifyTarget (BaseCloudRoute route);
	
	/**
	 * 提供给私有网络更改带宽的接口
	 * @author gaoxiang
	 * @param dcId
	 * @param qosId
	 * @param rate
	 */
	public void changeQos(String dcId, String qosId, int rate);
}
