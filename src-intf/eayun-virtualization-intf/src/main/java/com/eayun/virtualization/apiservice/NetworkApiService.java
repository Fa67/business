package com.eayun.virtualization.apiservice;

import com.eayun.virtualization.model.CloudNetWork;

public interface NetworkApiService {

	/**
	 * 根据项目ID和网络ID查询私有网络
	 * --------------------------
	 * @author zhouhaitao
	 * 
	 * @param prjId				项目ID
	 * @param netId				私有网络ID
	 * @return
	 */
	public CloudNetWork queryNetworkByPrjIdAndNetId(String prjId,String netId);
}
