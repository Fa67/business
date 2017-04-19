package com.eayun.virtualization.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.model.CloudOrderLdPool;

/**
 * PoolService
 * 
 * @Filename: PoolService.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年11月11日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
public interface PoolService {
	
	/**
	 * 查询负载均衡器的列表
	 * 
	 * @author zhouhaitao
	 * @param page
	 * @param datacenterId
	 * @param projectId
	 * @param name
	 * @param queryMap
	 * @return
	 * @throws Exception
	 */
	public Page getPoolList(Page page, String datacenterId,String projectId,String name,QueryMap queryMap) throws Exception;
	
	/**
	 * 校验重名
	 * 
	 * @author zhouhaitao
	 * @param pool
	 * @return
	 */
	public boolean checkPoolNameExsit(CloudLdPool pool);
	
	/**
	 * 购买负载均衡器
	 * 
	 * @author gaoxiang
	 * @param cloudOrderPool
	 * @return
	 */
	public String buyBalancer(CloudOrderLdPool cloudOrderPool, SessionUserInfo sessionUser) throws Exception;
	/**
	 * 创建负载均衡器
	 * 
	 * @author zhouhaitao
	 * @param cloudOrderPool
	 * @param sessionUser
	 * @throws AppException
	 */
	public CloudOrderLdPool createBalancer(CloudOrderLdPool cloudOrderPool, SessionUserInfo sessionUser) throws AppException ;
	/**
	 * 申请变配负载均衡器
	 * 
	 * @author gaoxiang
	 * @param cloudOrderPool
	 * @param sessionUser
	 * @return
	 */
	public String changeBalancer(CloudOrderLdPool cloudOrderPool, SessionUserInfo sessionUser) throws Exception;
	/**
     * 创建负载均衡器成功后的业务处理
     * @author gaoxiang
     * @param orderNo           订单编号
     * @param poolId            资源id
     * @param poolName          资源名称
     * @param connectionLimit   最大连接数
     * @param payType           付款方式
     * @throws Exception
     */
	public void poolCreateSuccessHandle(String dcId, String OrderNo, String cusId, String PoolId, String PoolName, Long connectionLimist, String payType) throws Exception;
	/**
	 * 修改负载均衡器名称
	 * @author gaoxiang
	 * @param cloudPool
	 * @return
	 * @throws AppException
	 */
	public CloudLdPool updateBalancerName(CloudLdPool cloudPool) throws AppException;
	/**
	 * 修改负载均衡器
	 * 
	 * @author zhouhaitao
	 * @param cloudOrderPool
	 * @throws AppException
	 */
	public CloudOrderLdPool updateBalancer(CloudOrderLdPool cloudOrderPool) throws AppException ;
	/**
	 * 删除负载均衡器
	 * 
	 * @author zhouhaitao
	 * @param pool
	 * @throws AppException
	 */
	public boolean deleteBalancer(CloudLdPool pool) throws AppException ;
	/**
	 * 续费负载均衡器
	 * 
	 * @param cloudOrderPool
	 * @param sessionUser
	 * @return
	 */
	public boolean renewBalancer(CloudOrderLdPool cloudOrderPool, SessionUserInfo sessionUser) throws Exception;
	/**
	 * 改变负载均衡器计费状态
	 * 
	 * @param resourceId
	 * @param chargeState
	 * @param endTime
	 * @param isRestrict
	 * @param isResumable
	 */
	public void modifyStateForLdPool(String resourceId, String chargeState, Date endTime, boolean isRestrict, boolean isResumable);
	/**
	 * 前台获取价格接口
	 * @author gaoxiang
	 * @param cloudOrderLdPool
	 * @return
	 */
	public BigDecimal getPrice(CloudOrderLdPool cloudOrderLdPool);
	/**
	 * 查询负载均衡器的详情
	 * 
	 * @author zhouhaitao
	 * @param poolId
	 * @return
	 */
	public CloudLdPool getLoadBalanceById(String poolId);
	/**
	 * 查询项目下负载均衡配额
	 * @author gaoxiang
	 * @param prjId
	 * @return
	 */
	public int getPoolQuotasByPrjId(String prjId);
	/**
	 *根据prjId查询个数 
	 */
	public int getCountByPrjId(String prjId);

	String getLBNameById(String lbId) throws Exception;
	/**
	 * 获取不同计费条件下的负载均衡器列表接口，供翔宇的计费报表使用
	 * @author gaoxiang
	 * @param prjId
	 * @param chargeState
	 * @param payType
	 * @param endTime
	 * @return
	 */
	public List<CloudLdPool> findLdPoolByCharge(String prjId, String chargeState, String payType, Date endTime);

    /**
     * 续费负载均衡器，仅作必要校验及提交订单<br/>
     * 支付成功后由订单推送续费支付成功消息，ResourceRenewConsumer监听消息做后续处理
     * @author zhangfan
     * @param sessionUser
     * @param params
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    Map<String, String> renewBalancer(SessionUserInfo sessionUser, Map params) throws Exception;

    /**
     * 检查当前是否已存在负载均衡续费或变配的未完成订单
     * @author zhangfan
     * @param lbId 负载均衡器ID
     * @return
     * @throws Exception
     */
    boolean checkLbOrderExist(String lbId) throws Exception;
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
     * 创建负载均衡器成功后的业务处理(为资源状态同步)
     * @author 曹翔宇
     * @throws Exception
     */
	public void poolCreateSuccessHandleForSync(String poolId,String orderNo,String cusId,long connectionLimit) throws Exception;
    /**
     * 根据id查负载均衡
     * @param poolId
     * @return
     * @throws Exception
     */
    public CloudLdPool getPoolById(String poolId) throws Exception;
}
