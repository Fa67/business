package com.eayun.virtualization.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudOrderFloatIp;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudOrderFloatIp;
import com.eayun.virtualization.model.CloudProject;

public interface CloudFloatIpService {

    /**
     * 浮动IP分页列表
     * @param page
     * @param projectId
     * @param queryMap
     * @return
     */
    public Page getListByPrj(Page page , String projectId , QueryMap queryMap);
    
    /**
     * 查询项目下未绑定云主机的浮动 IP列表
     * ------------------
     * @author zhouhaitao
     * @param prjId
     * 
     * @return
     */
    public List<BaseCloudFloatIp> getUnBindFloatIp(String prjId);
    /**
     * 项目下浮动IP个数
     * @param prjId
     * @return
     */
    public int getCountByPro(String prjId);
    
    /**
     * 查询项目的公网IP的配额信息和使用情况
     * @author zhouhaitao
     * 
     * @param prjId
     * @return
     */
    public CloudProject queryFloatIpQuatoByPrj(String prjId);
    
    /**
     * 申请创建公网IP
     * @author zhouhaitao
     * 
     * @param cloudFloatIp
     * @return
     */
    public CloudFloatIp createFloatIp(CloudFloatIp cloudFloatIp);
    
    /**
     * 释放公网IP
     * @author zhouhaitao
     * 
     * @param cloudFloatIp
     * @return
     * @throws Exception 
     */
    public CloudFloatIp releaseFloatIp(CloudFloatIp cloudFloatIp) throws Exception;
    
    /**
     * 查询项目下的网络列表
     * @author zhouhaitao
     * 
     * @param prjId
     * @return
     */
    public List<BaseCloudNetwork> getNetworkByPrj(String prjId);
    
    /**
     * 查询网络下的子网
     * @author zhouhaitao
     * 
     * @param netId
     * @return
     */
    public List<BaseCloudSubNetWork> getSubnetByNetwork(String netId);
    
    
    /**
     * 查询子网下指定的资源
     * @author zhouhaitao
     * 
     * @param cloudFloatIp
     * @return
     */
    public List<CloudFloatIp> getResourceBySubnet(CloudFloatIp cloudFloatIp);
    
    /**
     * 公网IP绑定资源
     * 
     * @author zhouhaitao
     * @param floatIp
     * @return
     */
    public CloudFloatIp bindResource(CloudFloatIp floatIp);
    
    /**
     * 公网IP解绑资源
     * 
     * @author zhouhaitao
     * @param floatIp
     * @return
     */
    public CloudFloatIp unbundingResource(CloudFloatIp floatIp);


    String getIpInfoById(String id) throws Exception;
/*
-------------------------------------------------陈鹏飞--------------------------------------
 */

    /**
     *
     * @param cloudOrderFloatIp 浮动ip订单数据
     * @param isCreateOrder 是否创建订单
     * @return
     */

    public CloudOrderFloatIp buyFloatIp(CloudOrderFloatIp cloudOrderFloatIp,boolean isCreateOrder) throws Exception;

    /**
     *	根据订单释放弹性公网IP
     * @param orderNo 订单编号
     * @return
     */
    public void releaseFloatIpByOrderNo(String orderNo) throws Exception;

    /**
     * 弹性公网ip续费
     * @param cloudOrderFloatIp 弹性公网订单对象
     * @return
     */
    public CloudOrderFloatIp renewFloatIp(CloudOrderFloatIp cloudOrderFloatIp,String cusId) throws Exception;

    /**
     * 获取弹性公网id剩余配额数
     * @param prjId 项目id
     * @return
     */
    public  int findFloIpSurplus(String prjId);

    /**
     * 批量创建弹性公网IP
     * @param orderNo 订单编号
     * @param isCreateOrder 是否生成订单
     * @return
     * @throws Exception
     */
    public List<CloudFloatIp> addFloatIp(String orderNo,boolean isCreateOrder) throws Exception;

    /**
     *续费修改
     * @param orderNo 订单编号
     * @return
     * @throws Exception
     */
    public CloudFloatIp udpateFloatIp(String orderNo)throws Exception;

    /**
     *
     * @param floId 弹性公网ip的id
     * @param resourceState 状态
     * @param endTime 结束时间
     * @return
     * @throws Exception
     */
    public CloudFloatIp modifyStateForFloatIp(String floId, String resourceState, Date endTime)throws Exception;

    /**
     * 删除公网ip
     * @param cloudFloatIp
     * @return
     * @throws Exception
     */
    public CloudFloatIp deleteFloatIp(CloudFloatIp cloudFloatIp,String cusId)throws Exception;

    /**
     * 发送开始计费的消息
     * @param cloudFloatIpList 需要发送消息的弹性公网IP
     * @param messKey 发送消息的key
     * @param cusId 客户id
     * @param cusId 订单编号
     * @throws Exception
     */
    public void sendMessage(List<CloudFloatIp> cloudFloatIpList,String messKey,String cusId,String orderNo)throws Exception;

    /**
     * 获取弹性公网ip总价
     * @param cloudOrderFloatIp
     * @return
     */
    public BigDecimal getPrice(CloudOrderFloatIp cloudOrderFloatIp);

    /**
     * 根据资源id和资源类型过去弹性公网ip
     * @param resourceId 资源id
     * @param resourceType 资源类型
     * @return
     */
    public CloudFloatIp getCloudFloatIpByResId(String resourceId,String resourceType);

    /**
     * map中的key
     * Date endTime,---到期时间
     * String prjId,---项目id
     * String chargeState,---浮动ip状态
     * String isDelete,---是否删除
     * String payType---付款方式
     * @param map
     * @return
     */
    public List<CloudFloatIp> getCloudFloatIpByMap(Map<String,Object> map);

    /**
     * 根据订单标号查询公网ip订单
     * @param orderNo
     * @return
     */
    public CloudOrderFloatIp getCloudOrderByOrderNo(String orderNo);

    /**
     * 根据公网ip查询是否存在续费的订单
     * @param floId
     * @return
     */
    public boolean checkFloatIpOrderExist(String floId);

    /**
     * 根据订单标号查询指定资源是否存在
     * @param orderNo
     * @return
     */
    public boolean isExistsByOrderNo(String orderNo);

    /**
     * 根据资源id查询指定资源是否存在
     * @param floId
     * @return
     */
    public ResourceCheckBean isExistsByResourceId(String floId);
    /**
	 * 公网Ip续费，提交按钮校验当前公网Ip是否有未完成订单，以及当前账户金额是否充足与提交订单	
	 * @author liyanchao	
	 * @param map
	 * @param userId
	 * @param userName
	 * @param cusId
	 * @return JSONObject
	 */
	public JSONObject renewFloatIpOrderConfirm(Map<String ,String> map,String userId,String userName,String cusId)throws Exception;
	/**
     * @param floId
     * @return CloudFloatIp
    */
    public CloudFloatIp findFloatIpById(String floId);
    
    /**
     * 解除已删除云主机与弹性公网IP的关系
     * @param vmId  云主机ID
     */
    public void refreshFloatIpByVm(String vmId);
    /**
     * 重新下单时，获取订单的原始配置数据
     * @author gaoxiang
     * @param orderNo
     * @return
     */
    public CloudOrderFloatIp getOrderFloatIpByOrderNo(String orderNo);

	public boolean checkFloWebSite(String floIp) throws Exception;
}
