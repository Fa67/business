package com.eayun.virtualization.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.CloudNetWork;
import com.eayun.virtualization.model.CloudOrderNetWork;

/**
 * NetWorkService
 * 
 * @Filename: NetWorkService.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年11月9日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
public interface NetWorkService {
    
    public List<BaseCloudNetwork> getCloudNetworkList(String sql,Object... values);
    /**
     * 获取项目下的网络列表
     * @param page
     * @param paramsMap
     * @return
     */
    public Page getNetWorkListByPrjId(Page page,ParamsMap paramsMap) throws Exception;
    /**
     * 验证网络名称
     * @param netId
     * @param netName
     * @param dcId
     * @return
     */
    public boolean checkNetWorkName(String netId,String netName,String dcId);
    /**
     * 新购网络
     * @author gaoxiang
     * @param cloudOrderNetWork
     * @param sessionUser
     * @return
     */
    public String buyNetWork(CloudOrderNetWork cloudOrderNetWork, SessionUserInfo sessionUser) throws Exception;
    /**
     * 新增网络
     * @param cloudOrderNetWork
     * @return
     */
    public CloudOrderNetWork addNetWork(CloudOrderNetWork cloudOrderNetWork);
    /**
     * 变更私有网络配置
     * @param cloudOrderNetWork
     * @param sessionUser
     * @return
     */
    public String changeNetWork(CloudOrderNetWork cloudOrderNetWork, SessionUserInfo sessionUser) throws Exception;
    /**
     * 编辑私有网络名称
     * @author gaoxiang
     * @param cloudNetWork
     * @return
     */
    public CloudNetWork updateNetWorkName(CloudNetWork cloudNetWork);
    /**
     * 编辑网络
     * @param cloudOrderNetWork
     * @return
     */
    public CloudOrderNetWork updateNetWork(CloudOrderNetWork cloudOrderNetWork);
    /**
     * 删除网络
     * @param cloudNetWork
     * @return
     */
    public boolean delNetWorkByNetId(CloudNetWork cloudNetWork) throws AppException;
    /**
     * 续费私有网络
     * @author gaoxiang
     * @param cloudOrderNetWork
     * @param sessionUser
     * @return
     */
    public boolean renewNetWork(CloudOrderNetWork cloudOrderNetWork, SessionUserInfo sessionUser) throws Exception;
    /**
     * 改变私有网络计费状态
     * @author gaoxiang
     * @param resourceId	资源id
     * @param chargeState	资源需要改变成的计费状态
     * @param endTime		新的到期时间，如果是后付费或不需要设定，传null
     * @param isRestict		是否限制服务
     * @param isResumable	是否恢复服务
     */
    public void modifyStateForNetWork(String resourceId, String chargeState, Date endTime, boolean isRestict, boolean isResumable);
    /**
     * 获取订单的全部价格
     * @author gaoxiang
     * @param cloudOrderNetWork
     * @return
     */
    public BigDecimal getPrice(CloudOrderNetWork cloudOrderNetWork);
    /**
     * 查询指定网络
     * @author gaoxiang
     * @param netId
     * @return
     */
    public CloudNetWork findNetWorkByNetId(String netId);
    /**
     * 查询项目下私有网络配额 
     * @author gaoxiang
     * @param prjId
     * @return
     */
    public int getNetworkQuotasByPrjId(String prjId);
    /**
     * 查询项目下私有网络带宽配额
     * @author gaoxiang
     * @param prjId
     * @return
     */
    public int getBandQuotasByPrjId(String prjId);
    /**
     * 查询项目下的网络已使用量
     * @param prjId
     * @return
     */
    public int findNetWorkCountByPrjId(String prjId);

    String getVPCNameById(String id) throws Exception;
    /**
     * 获取不同计费条件下的私有网络列表接口，供翔宇的计费报表使用
     * @author gaoxiang
     * @param prjId
     * @param chargeState
     * @param payType
     * @param endTime
     * @return
     */
    public List<CloudNetWork> findNetWorkByCharge(String prjId, String chargeState, String payType, Date endTime);

    /**
     * 检查当前是否已存在私有网络续费或变配的未完成订单
     * @author zhangfan
     * @param netId
     * @return
     * @throws Exception
     */
    boolean checkNetworkOrderExist(String netId) throws Exception;

    /**
     * 续费私有网络<br/>
     * 支付成功后由订单推送续费支付成功消息，ResourceRenewConsumer监听消息做后续处理
     * @author zhangfan
     * @param sessionUser
     * @param params
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    Map<String, String> renewNetwork(SessionUserInfo sessionUser, Map params) throws Exception;
    /**
     * 根据routeId查询指定网络
     * @param routeId
     * @return
     */
	public Map<String, Object> findNetWorkByRouteId(String routeId);
	/**
	 * 根据订单编号查询指定资源是否存在
	 * @author gaoxiang
	 * @param orderNo
	 * @return
	 */
	public boolean isExistsByOrderNo(String orderNo);
	/**
	 * 根据资源id查询指定资源是否存在
	 * @author gaoxiang
	 * @param resourceId
	 * @return
	 */
	public ResourceCheckBean isExistsByResourceId(String resourceId);
	/**
	 * 根据项目id获取设置了网关的网络列表，供购买云数据库调用
	 * @author gaoxiang
	 * @param prjId
	 * @return CloudNetWork
	 */
	public List<CloudNetWork> getNetworkListByPrjIdAndRouteIdNotNull (String prjId);
}
