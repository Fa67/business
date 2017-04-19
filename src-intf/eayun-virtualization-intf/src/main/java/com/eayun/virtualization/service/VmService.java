package com.eayun.virtualization.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudImage;
import com.eayun.virtualization.model.CloudNetWork;
import com.eayun.virtualization.model.CloudOrderVm;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudSecretKey;
import com.eayun.virtualization.model.CloudSecurityGroup;
import com.eayun.virtualization.model.CloudSubNetWork;
import com.eayun.virtualization.model.CloudVm;

/**
 * VmService
 *                       
 * @Filename: VmService.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月1日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public interface VmService {
    
    /**
     * 未删除的虚机列表
     * 
     * @param prjId
     * @return
     */
    public List<CloudVm> getUnDeletedVmListByProject(String prjId);
	
	/**
	 * 查询云主机列表
	 * ------------
	 * @author zhouhaitao
	 * @param page 分页结果集
	 * @param map 查询条件
	 * @param sessionUser 当前用户
	 * @param queryMap 分页条件
	 * @return 
	 * @throws AppException
	 */
	public Page listVm(Page page,ParamsMap map,SessionUserInfo sessionUser, QueryMap queryMap)throws Exception;
	
	/**
	 * 查询回收站的云主机列表
	 * ------------
	 * @author zhouhaitao
	 * @param page 分页结果集
	 * @param map 查询条件
	 * @param sessionUser 当前用户
	 * @param queryMap 分页条件
	 * @return 
	 * @throws AppException
	 */
	public Page getRecycleVmList(Page page,ParamsMap map,SessionUserInfo sessionUser, QueryMap queryMap)throws Exception;
	
	/**
	 * 校验当前主机的受管子网是否可以切换
	 * <p>1. 没有绑定公网IP</p>
	 * <p>2. 没有关联负载均衡器的成员</p>
	 * <p>3. 没有作为端口映射的对应</p>
	 * <p>满足以上3点，返回false;否则 返回 true</p>
	 * ---------------------------------------
	 * @author zhouhaitao
	 * @param vm
	 * @return
	 */
	public boolean checkVmIpUsed (CloudVm vm);
    
    public CloudVm findVm(String vmId);

    
    /**
     * 查询云主机详情
     * @param dcId
     * @param prjId
     * @param vmId
     * @return
     */
	public CloudVm getById(String vmId)throws Exception;
	
    /**
     * 云业务点击左侧树“云主机”，查询数据中心--项目的方法
     * 
     * @param request
     * @param sessionUser
     * 			当前登录用户
     * @return
     */
    public List<CloudProject> findDcAndPrj(SessionUserInfo sessionUser);
    
    /**
     * 
     * @param page
     * @param object
     * @param string
     * @param object2
     * @param queryMap
     * @return
     */
	public Page listDisk(Page page, Object object, String string,Object object2, QueryMap queryMap);
	
	/**
	 * 查询当前用户所管理的项目及配置信息 
	 * -----------------
	 * @author zhouhaitao
	 * @param sessionUser
	 * 				当前登录用户
	 * @return
	 * 
	 */
	public List<CloudProject> getProListByCustomer(SessionUserInfo sessionUser);
	
	/**
	 * 校验云主机在同数据中心下是否重名
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 		请求参数
	 * @return
	 */
	public boolean checkVmExistByName (CloudVm cloudVm);
	
	/**
	 * 获取CPU配置信息列表
	 * ------------------
	 * @author zhouhaitao
	 * @return
	 */
	public List<SysDataTree> getCpuList ();
	
	/**
	 * 根据CPU获取内存配置信息列表
	 * ------------------
	 * @author zhouhaitao
	 * @param cpuId
	 * @return
	 */
	public List<SysDataTree> getRamListByCpu (String cpuId);
	
	/**
	 * 查询云主机创建的系统类型
	 * -----------------
	 * @author zhouhaitao
	 * @return
	 * 
	 */
	public List<SysDataTree> getOsList();
	
	/**
	 * 根据系统类型获取操作系统列表
	 * ------------------
	 * @author zhouhaitao
	 * @param osId
	 * @return
	 */
	public List<SysDataTree> getSysTypeList (String osId);
	
	/**
	 * 获取项目下的镜像
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * @return
	 */
	public List<CloudImage> getImageList (CloudVm cloudVm);
	
	/**
	 * 获取项目下的子网列表
	 * ------------------
	 * @author zhouhaitao
	 * @param prjId
	 * @return
	 */
	public List<BaseCloudSubNetWork> getSubNetList (String prjId);
	
	/**
	 * 编辑云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void modifyVm(CloudVm cloudVm)throws AppException ;
	
	/**
	 * 删除云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * @param sessionUser
	 * 
	 * @throws AppException
	 * @throws Exception 
	 */
	public void deleteVm(CloudVm cloudVm,SessionUserInfo sessionUser) throws AppException, Exception;
	
	/**
	 * 启动云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void restartVm(CloudVm cloudVm)throws AppException;
	
	/**
	 * 关闭云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void shutdownVm(CloudVm cloudVm)throws AppException;
	
	/**
	 * 软重启云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void softRestartVm(CloudVm cloudVm)throws AppException;
	
	/**
	 * 硬重启云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void hardRestartVm(CloudVm cloudVm)throws AppException;
	
	/**
	 * 创建自定义镜像
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * @param userInfo
	 * 
	 * @throws AppException
	 */
	public void createSnapshot(CloudVm cloudVm,SessionUserInfo userInfo)throws AppException;
	
	/**
	 * 挂起云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void suspendVm(CloudVm cloudVm)throws AppException;
	
	/**
	 * 恢复云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void resumeVm(CloudVm cloudVm)throws AppException;
	
	/**
	 * 调整云主机大小
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 				升级主机信息
	 * @throws Exception
	 */
	public void resizeVm(CloudVm cloudVm)throws Exception;
	
	/**
	 * 确认调整云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void confirmResizeVm(CloudVm cloudVm)throws AppException;
	
	/**
	 * 取消调整云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void revertResizeVm(CloudVm cloudVm)throws AppException;
	
	/**
	 * 打开云主机控制台
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public String consoleVm(CloudVm cloudVm)throws AppException;
	
	/**
	 * 获取云主机日志
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public String getVmLogs(CloudVm cloudVm)throws AppException;
	
	/**
	 * 编辑云主机安全组
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public void editVmSecurityGroup(CloudVm cloudVm)throws AppException;
	
	/**
	 * 
	 * @author zhouhaitao
	 * @param prjId
	 * @return
	 */
	public List<BaseCloudVm> queryVmListByPrjId(String prjId);
	
	/**
	 * 查询云主机状态列表
	 * ------------------
	 * @author zhouhaitao
	 * 
	 * @return
	 */
	public List<SysDataTree> getVmStatusList ();
	
	/**
	 * 查询操作系统型号列表
	 * ------------------
	 * @author zhouhaitao
	 * 
	 * @return
	 */
	public List<SysDataTree> getVmSysList ();

	/**
	 * 查询项目下的未关联云主机的安全组信息
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public List<BaseCloudSecurityGroup> getSecurityGroupByPrj(CloudVm cloudVm);
	
	/**
	 * 查询项目下的已关联云主机的安全组信息
	 * ------------------
	 * @author zhouhaitao
	 * @param vmId
	 * 
	 * @throws AppException
	 */
	public List<BaseCloudSecurityGroup> getSecurityGroupByVm(String vmId);
	
	/**
	 * 查询项目下项目下云主机相关配额详情
	 * @param prjId
	 * @return
	 */
	public CloudProject queryPrjQuato(String prjId);
	
	public Map<String,Object> getStackList(BaseDcDataCenter dataCenter ,String prjId)throws Exception;
	
	public int getUnDeletedVmCountByProject(String prjId);

	public List<CloudVm> getCanBindCloudVmList(String prjId)throws Exception;
	
	/**
	 * @author zhouhaitao
	 * 
	 * @param resourceId
	 * 				云主机资源ID
	 * @param resourceState
	 * 				资源需要改变成的状态
	 * @param date
	 * 				资源的新到期时间   后付费 不需要此参数
	 * @param isShutdown
	 * 				是否停止服务    true需要停止服务
	 * @param isResumable
	 * 				是否开启服务  true需要启动服务
	 * 
	 * @return 
	 * 		
	 */
	public boolean modifyStateForVm(String resourceId, String resourceState,Date date, boolean isShutdown,boolean isResumable);
	
	/**
	 * <p>购买云主机--提交订单</p>
	 * 
	 * @author zhouhaitao
	 * @param cloudOrder
	 * 			提交的云主机订单信息
	 * @param user
	 * 			当前登录用户信息
	 * @return
	 * 			错误信息标示，成功 返回 <code>null</code>
	 */
	public String buyVm(CloudOrderVm cloudOrder, SessionUserInfo user)throws Exception;
	
	/**
	 * 查询云主机的操作系统名称
	 * 
	 * @param vmId
	 * @return
	 * @throws Exception
	 */
	public String getOSNameByVmId(String vmId) throws Exception;
	
	/**
	 * 资源创建接口
	 * --------------
	 * @author zhouhaitao
	 * @param order
	 * 			云主机订单信息
	 * @return 
	 * 
	 * @throws AppException
	 * 
	 */
	public List<BaseCloudVm> createVm(CloudOrderVm order) throws AppException;
	
	/**
	 * 资源升级接口
	 * --------------
	 * @author zhouhaitao
	 * @param order
	 * 			云主机订单信息
	 * @param user
	 * 			用户信息
	 * 
	 * @throws AppException
	 * 
	 * @return
	 * 
	 */
	public String resizeVm(CloudOrderVm order ,SessionUserInfo user) throws Exception;
	
	/**
	 * <p>根据受管子网查询对应的主机</p>
	 * 
	 * @author zhouhaitao
	 * @param subnetId
	 * 				受管子网ID
	 * @return
	 */
	public List<BaseCloudVm> queryVmListBySubnet(String subnetId);
	
	/**
	 * 查询项目下的网络列表
	 * 
	 * @author zhouhaitao
	 * @param prjId
	 * @return
	 */
	public List<CloudNetWork> queryNetListByPrjId(String prjId);
	
	/**
	 * 查询项目下的安全组列表
	 * 
	 * @author zhouhaitao
	 * @param prjId
	 * @return
	 */
	public List<CloudSecurityGroup> querySgListByPrjId(String prjId);
	
	/**
	 * 获取到期时间<=endTime的状态为state的isDelete的payType主机信息
	 * @param prjId 项目id
	 * @param endTime 到期时间
	 * @param state	正常/已过期/已欠费
	 * @param isDelete 是否删除 true为删除
	 * @param payType	付款方式 1为预付费 2为后付费
	 * @param cusState  客户状态 0为正常 1为已冻结  null为所有
	 * @return
	 * @throws Exception
	 */
	public List<CloudVm> queryNormalVm(String prjId,Date endTime,String state,boolean isDelete,boolean isRecycle ,String payType,String cusState) throws Exception;
					
	/**
	 * 
	 * 订单资源创建成功处理
	 * 
	 * @author zhouhaitao
	 * 
	 * @param orderVm
	 *            订单信息
	 * @param floatIpList
	 *            创建成功的公网IP列表
	 * @param vmList
	 *            创建成功的云主机列表
	 */
	public void allVmSuccessHnadler(CloudOrderVm orderVm, List<CloudFloatIp> floatIpList, List<BaseCloudVm> vmList)throws Exception;
	
	
	
	
	/**
	 * <p>
	 * 资源创建过程中失败处理
	 * </p>
	 * --------------------------
	 * 
	 * @author zhouhaitao
	 * @param order
	 *            订单信息
	 * @param floatIpList
	 *            已经创建成功的公网IP列表
	 * @param vmList
	 *            已经创建成功的主机列表
	 * 
	 * @throws Exception
	 */
	public void createFailHandler(CloudOrderVm order, List<BaseCloudVm> vmList)throws Exception;
	
	/**
	 * 云主机限制服务 计费队列(type = "restrict")
	 * 云主机恢复服务 计费队列(type = "recover")
	 * 云主机放入回收站 计费队列(type = "recycle")
	 * 云主机回收站中还原 计费队列(type = "restore")
	 * 云主机回彻底删除 计费队列(type = "delete")
	 * 
	 * @param cloudVm
	 */
	public void vmOptionCharge(CloudVm cloudVm,String type);
	
	/**
	 * 云主机修改子网
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 				主机信息
	 */
	public void modifySubnet(CloudVm cloudVm);
	
	/**
	 * 查询镜像的信息
	 * 
	 * @author zhouhaitao
	 * @param imageId
	 * @return
	 */
	public CloudImage getImageById(String imageId);
	
	/**
	 *
	 * 恢复云主机（从回收站）
	 * 
	 * @param cloudVm
	 * @param sessionUser
	 * @throws Exception 
	 */
	public void restoreVm(CloudVm cloudVm, SessionUserInfo sessionUser) throws Exception;
	
	/**
	 * 云主机升级成功处理
	 * 
	 * @param cloudVm
	 */
	public void upgradeSuccessHandler(CloudVm cloudVm) throws Exception;
	
	/**
	 * 云主机升级失败处理
	 * 
	 * @param cloudVm
	 */
	public void upgradeFailHandler(CloudVm cloudVm) throws Exception;
	
	/**
	 * 查询主机是否有正在处理的订单
	 * 
	 * @author zhouhaitao
	 * @param vmId
	 * @return
	 */
	public boolean checkVmOrderExsit(String vmId);
	
	/**
	 * 云主机续费，提交按钮校验当前主机是否有未完成订单，以及当前账户金额是否充足与提交订单	
	 * 
	 * @author liyanchao	
	 * @param map
	 * @param ParamBean
	 * @param userId
	 * @return JSONObject
	 */
	public JSONObject renewVmOrderConfirm(Map<String ,String> map,String userId ,String userName,String cusId)throws Exception;
	
	/**
	 * 根据云主机ID查询云主机的计费队列需要的信息
	 * 
	 * @param vmId
	 * @return
	 */
	public CloudVm queryVmChargeById(String vmId);
	
	/**
	 * 查询购买类型
	 * 
	 * @return
	 */
	public List<SysDataTree> queryBuyCycleType();
	
	/**
	 * 查询购买周期时长
	 * @param nodeId
	 * @return
	 */
	public List<SysDataTree> queryBuyCycleList(String nodeId);
	
	/**
	 * 根据订单编号查询订单信息
	 * @param orderNo
	 * @return
	 */
	public CloudOrderVm queryCloudOrderByOrderNo(String orderNo);
	
	/**
     * 查询网络下的子网
     * @param subnet
     * @return
     */
	public List<CloudSubNetWork> querySubnetByNet(CloudSubNetWork subnet);
	
	/**
	 * 查询回收站云主机的信息
	 * @param vmId
	 * @return
	 */
	public CloudVm getRecycleVmById(String vmId);
	
	/**
	 * 判断项目的创建中的自定义镜像
	 * 
	 * @param vmId
	 * @return
	 */
	public boolean checkCreatingImageCount(String vmId);
	
	/**
	 * 查询云主机计费因子价格（乘以数量）和总价
	 * @param paramBean
	 * @return
	 */
	public PriceDetails getPriceDetails(ParamBean paramBean);
	
	/**
	 * 查询该订单对应的升级或续费资源是否存在
	 * -----------------------
	 * @author zhouhaitao
	 * @param orderNo
	 * @return
	 */
	public boolean isExistsByOrderNo(String orderNo);
	
	/**
	 * 查询资源是否存在的接口
	 * 
	 * @author zhouhaitao
	 * @param resId
	 * @return
	 */
	public ResourceCheckBean isExistsByResourceId(String resId);
	
	/**
	 * 查询已在回收站过期的云主机列表
	 * 
	 * @author zhouhaitao
	 * @param seconds
	 * @return
	 */
	public List<CloudVm> queryRecycleVmList(long seconds);
	
	/**
	 * 获取市场镜像的业务类型信息列表 <br>
	 * ------------------
	 * 
	 * @author zhouhaitao
	 * @return
	 */
	public List<SysDataTree> getMarketImageTypeList();

	
	/**
	 * 根据指定镜像id查询该镜像实际创建了多少台主机（包括底层同步上来的）
	 * @param imageId
	 * @return
	 */
	public int countVmByImageId(String imageId);
	
	/**
	 * <p>
	 * 云主机云硬盘创建挂载过程中失败处理
	 * </p>
	 * --------------------------
	 * 
	 * @author chengxiaodong
	 * @param order
	 *            订单信息
	 * @param floatIpList
	 *            已经创建成功的公网IP列表
	 * @param vmList
	 *            已经创建成功的主机列表
	 * 
	 * @throws Exception
	 */
	public void createFailVmsAndVolumes(CloudOrderVm order, List<BaseCloudVm> vmList,List<BaseCloudVolume> volList);
	
	
	/**
	 * 云主机购买数据盘全部成功
	 * @author chengxiaodong
	 * @param orderVm
	 * @param floatIpList
	 * @param vmList
	 * @param volList
	 * @throws Exception
	 */
	public void allVmAndVolumesSuccessHnadler(CloudOrderVm orderVm, List<CloudFloatIp> floatIpList, List<BaseCloudVm> vmList,List<BaseCloudVolume> volList)throws Exception;
	
	/**
	 * <p>查询项目下未关联制定云主机的SSH密钥列表</p>
	 * ------------------------
	 * @author zhouhaitao
	 * 
	 * @param prjId			项目ID
	 * @param vmId			云主机ID
	 * @return
	 */
	public List<CloudSecretKey> getUnbindSecretkeyByPrj(String prjId,String vmId);
	
	/**
	 * <p>查询云主机关联制定云主机的SSH密钥列表</p>
	 * ------------------------
	 * @author zhouhaitao
	 * 
	 * @param prjId			项目ID
	 * @param vmId			云主机ID
	 * @return
	 */
	public List<CloudSecretKey> getBindSecretkeyByVm(String vmId);
	
	/**
	 * <p>绑定/解绑SSH密钥</p>
	 * -------------------------
	 * @author zhouhaitao
	 * 
	 * @param cloudVm
	 */
	public void editSecretKey(CloudVm cloudVm);
	
	/**
	 * <p>查询云主机链接路由的情况</p>
	 * ------------------------------
	 * @author zhouhaitao
	 * @param vmId
	 * @return
	 */
	public CloudVm queryRouteByVm(String vmId);
	
	/**
	 * <p>修改云主机密码</p>
	 * ------------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 */
	public void modifyPwd(CloudVm cloudVm);
}