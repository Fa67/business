package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Flavor;
import com.eayun.eayunstack.model.Image;
import com.eayun.eayunstack.model.InterfaceAttachment;
import com.eayun.eayunstack.model.SecurityGroup;
import com.eayun.eayunstack.model.Vm;
import com.eayun.virtualization.model.BaseCloudFlavor;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.CloudVm;

public interface OpenstackVmService extends OpenstackBaseService<Vm> {

	/**
	 * 创建模板
	 * --------------
	 * @author zhouhaitao
	 * @param cloudFlavor
	 * 			云主机的模板信息
	 * @return
	 * 
	 * @throws AppException
	 */
	public Flavor createFlavor(BaseCloudFlavor cloudFlavor) throws AppException;
	
	/**
	 * 创建云主机
	 * ---------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 *            云主机的创建信息
	 * @param flavorId 
	 * 			     云主机类型Id
	 * @param vmList
	 * 			  创建成功的云主机	
	 * @return
	 * 
	 * @throws AppException
	 */
	public String  createVm(CloudVm cloudVm,String flavorId,List<Vm> vmList) throws AppException;
	
	/**
	 * 编辑云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public boolean modifyVm(CloudVm cloudVm) throws AppException;
	
	/**
	 * 删除云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public boolean softDeleteVm(CloudVm cloudVm) throws AppException;
	
	/**
	 * 启动云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean restartVm(CloudVm cloudVm) throws AppException;
	
	/**
	 * 关闭云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean shutdownVm(CloudVm cloudVm) throws AppException;
	
	/**
	 * 软重启云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean softRestartVm(CloudVm cloudVm) throws AppException;
	
	/**
	 * 硬重启云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean hardRestartVm(CloudVm cloudVm) throws AppException;
	
	/**
	 * 创建自定义镜像
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return 
	 * 
	 * @throws AppException
	 */
	public Image createSnapshot(CloudVm cloudVm) throws AppException;
	
	/**
	 * 挂起云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean suspendVm(CloudVm cloudVm) throws AppException;
	
	/**
	 * 恢复云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean resumeVm(CloudVm cloudVm) throws AppException;
	
	/**
	 * 重建云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean rebuildVm(CloudVm cloudVm) throws AppException;
	
	/**
	 * 调整云主机大小
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean resizeVm(CloudVm cloudVm) throws AppException;
	
	/**
	 * 确认调整云主机大小
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean confirmResizeVm(CloudVm cloudVm) throws AppException;
	
	/**
	 * 取消调整云主机大小
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean revertResizeVm(CloudVm cloudVm) throws AppException;
	
	/**
	 * 打开云主机控制台
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public String consoleVm(CloudVm cloudVm) throws AppException;
	
	/**
	 * 获取云主机日志
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public String getVmLogs(CloudVm cloudVm) throws AppException;
	
	/**
	 * 编辑安全组
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * @param adds
	 * @param dels
	 * 
	 * @throws AppException
	 */
	public boolean editVmSecurityGroup(CloudVm cloudVm,List<BaseCloudSecurityGroup> adds,List<BaseCloudSecurityGroup> dels) throws AppException;
	
	/**
	 * 修改主机密码
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * @param data
	 * 
	 * @throws AppException
	 */
	public void modifyVmPassword(CloudVm cloudVm,JSONObject data)throws AppException;

	public JSONObject log(String datacenterId, String projectId, String id)
			throws AppException;

	/**
	 * 删除指定id的模板（flavor）
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean deleteFlavor(String datacenterId, String projectId, String id)
			throws AppException;

	public List<SecurityGroup> listSecurityGroupForVm(String datacenterId,
			String projectId, String id) throws AppException;
	
	public JSONObject get(String dcId,String prjId,String vmId)throws Exception;
	
	/**                                                        
	 * 查询底层 数据中心的云资源                             
	 * -------------------                                   
	 * @author zhouhaitao                                    
	 * @param dataCenter                                     
	 * @return                                               
	 */                                                      
	public List<JSONObject> getStackList (BaseDcDataCenter dataCenter,String prjId)throws Exception ;

	public <T> T json2bean(JSONObject jSONObject, Class<T> clazz);
	
	/**
	 * 云主机绑定端口
	 * 
	 * @author zhouhaitao
	 * 
	 * @param dcId
	 * 			数据中心ID
	 * @param prjId
	 * 			项目ID
	 * @param vmId
	 * 			云主机ID
	 * @param netId
	 * 			网络ID
	 * @param subnetId
	 * 			子网ID
	 * @param sgIds
	 * 			安全组ID
	 * @return
	 * @throws AppException
	 */
	public InterfaceAttachment bindPort(String dcId, String prjId ,String vmId ,String netId,String subnetId,String [] sgIds) throws AppException;
	
	/**
	 * 删除云主机关联的端口
	 * 
	 * @author zhouhaitao
	 * --------------------
	 * @param dcId
	 * 			数据中心ID
	 * @param prjId
	 * 			项目ID
	 * @param vmId
	 * 			云主机ID
	 * @param portId
	 * 			端口ID
	 * @return
	 * 
	 */
	public boolean unbindPort(String dcId,String prjId, String vmId, String portId) throws AppException;
	
	
	/**
	 * 根据过滤条件查询主机列表
	 * 
	 * @author zhouhaitao
	 * 
	 * @param dcId
	 * 			数据中心ID
	 * @param prjId
	 * 			项目ID
	 * @param url
	 * 			url过滤条件
	 * @return
	 */
	public List<Vm> list(String dcId, String prjId,String url);
	
	/**
	 * 恢复 软删除的云主机
	 * ---------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 			
	 * @return
	 */
	public boolean restorVm(CloudVm cloudVm);
	
	/**
	 * 强制删除云主机
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * @return
	 */
	public boolean forceDelete(CloudVm cloudVm);
	
	/**                                                                                                         
	 * 获取底层项目下的云主机                                                               
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 * @throws Exception 
	 *                                                                                                        
	 */                                                                                                       
	public List<JSONObject> getSoftDeletedList(String dcId,String prjId,String url) throws Exception;
}
