package com.eayun.virtualization.apiservice;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.ApiException;

public interface VmApiService {
	
	/**
	 * 创建云主机的API<br>
	 * -------------
	 * @author zhouhaitao
	 * @param params
	 * @return
	 */
	public JSONObject createInstance (JSONObject params) throws ApiException;
	
	/**
	 * 调整云主机大小的API<br>
	 * -------------
	 * @author zhouhaitao
	 * @param params
	 * @return
	 */
	public JSONObject resizeInstance (JSONObject params)throws ApiException;
	
	/**
	 * 云主机加入安全组
	 * 
	 * @author liuzhuangzhuang
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public JSONObject instanceLeaveSecurityGroup(JSONObject params) throws ApiException;
	
	
	/**
	 * 关闭云主机的API
	 * @author chengxiaodong
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public JSONObject shutDownInstance (JSONObject params)throws ApiException;
	
	/**
	 * 开启指定云主机的API
	 * @author chengxiaodong
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public JSONObject startInstance (JSONObject params)throws ApiException;
	
	/**
	 * 重启云主机的API
	 * @author chengxiaodong
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public JSONObject rebootInstance (JSONObject params)throws ApiException;
	
	/**
	 * 删除云主机的API
	 * @author chengxiaodong
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public JSONObject deleteInstance (JSONObject params)throws ApiException;
	
	/**
	 * 云主机离开安全组
	 * 
	 * @author liuzhuangzhuang
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public JSONObject instanceJoinSecurityGroup(JSONObject params) throws ApiException;
	
	
	/**
	 * 查询云主机的API
	 * @author gaoxiang
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public JSONObject describeInstances (JSONObject params) throws ApiException;
	
	/**
	 * 修改云主机的API
	 * @author gaoxiang
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public JSONObject modifyInstance (JSONObject params) throws ApiException;
	
	/**
	 * 修改云主机子网的API
	 * @author gaoxiang
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public JSONObject modifyInstanceSubnet (JSONObject params) throws ApiException;
	/**
	 * 查询安全组信息
	 * @author liuzhuangzhuang
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public JSONObject describeSecurityGroups(JSONObject params) throws ApiException;
}
