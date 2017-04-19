package com.eayun.virtualization.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.virtualization.model.CloudOrderVpn;
import com.eayun.virtualization.model.CloudVpn;

public interface VpnService {
    /**
     * 为了同步而改变上层数据库数据状态
     * @author gaoxiang
     * @param cloudVpn
     * @return
     */
    public boolean modifyStatusForSynchronization (CloudVpn cloudVpn);
    /**
     * 获取vpn列表页数据
     * @author gaoxiang
     * @param page
     * @param paramsMap
     * @return
     */
    public Page getVpnList(Page page, ParamsMap paramsMap);
    /**
     * 检验vpn名称重复的接口
     * @author gaoxiang
     * @param cloudVpn
     * @return
     */
    public boolean checkVpnNameExist(Map<String, String> map) throws Exception;
    /**
     * 获取vpn详情页信息
     * @author gaoxiang
     * @param vpnId
     * @return
     */
    public CloudVpn getVpnInfo(String vpnId);
    /**
     * 获取vpn的价格接口
     * @author gaoxiang
     * @param cloudOrderVpn
     * @return
     */
    public BigDecimal getPrice(CloudOrderVpn cloudOrderVpn);
    
    public String buyVpn(CloudOrderVpn cloudOrderVpn, SessionUserInfo sessionUser) throws Exception;
    
    public CloudOrderVpn createVpn(CloudOrderVpn cloudOrderVpn);
    /**
     * vpn创建失败的底层回滚
     * @author gaoxiang
     * @param cloudOrderVpn
     * @param createStep
     * @author gaoxiang
     */
    public void vpnCreateCallback(CloudOrderVpn cloudOrderVpn, int createStep) throws Exception;
    /**
     * vpn创建成的业务处理
     * @author gaoxiang
     * @param dcId
     * @param orderNo
     * @param cusId
     * @param vpnId
     * @param vpnName
     * @param payType
     * @throws Exception
     */
    public void vpnCreateSuccessHandle(String dcId, String orderNo, String cusId, String vpnId, String vpnName, String payType) throws Exception;
    
    public CloudVpn updateVpn(CloudVpn cloudVpn);
    
    public boolean deleteVpn(CloudOrderVpn cloudOrderVpn);
    /**
     * 通过项目id获取私有网络数据
     * @author gaoxiang
     * @param prjId
     * @return
     */
    public List<Map<String, Object>> getCloudNetworkList(String prjId);
    /**
     * 根据项目查询VPN使用量
     * @author liuzhuangzhuang
     * @param prjId
     * @return
     */
    public int getCountByPrjId(String prjId);
    /**
     * 根据vpnId获取本端私有网络名称和网关以及对端网关的字符串，供翔宇的费用报表模块使用
     * @author gaoxiang
     * @param vpnId
     * @return
     * @throws Exception
     */
    public String getVpnInfoById(String vpnId) throws Exception ;

    boolean modifyStateForVPN(String resourceId, String chargeState, Date endTime, boolean isLimit, boolean isResumable);
    /**
     * 获取不同计费条件下的vpn列表接口，供翔宇的计费报表使用
     * @author gaoxiang
     * @param prjId
     * @param chargeState
     * @param payType
     * @param endTime
     * @return
     */
    public List<CloudVpn> findVpnByCharge(String prjId, String chargeState, String payType, Date endTime);
    /**
     * 从消息队列取出对应key的的其中一条
     * @param groupKey
     * @return
     */
    public String pop(String groupKey);

    boolean checkVpnOrderExist(String var1) throws Exception;

    /**
     * 续费VPN<br/>
     * 支付成功后由订单推送续费支付成功消息，ResourceRenewConsumer监听消息做后续处理
     * @author zhangfan
     * @param sessionUser
     * @param params
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    Map<String, String> renewVpn(SessionUserInfo sessionUser, Map params) throws Exception;
    
    /**
     * 获取vpn项目下的配额
     * @author gaoxiang
     * @param prjId
     * @return
     */
    public int getVpnQuotasByPrjId(String prjId);

    /**
     * 根据vpnId获取vpn名称
     * @author zhangfan
     * @param vpnId
     * @return
     */
    String getVpnNameById(String vpnId);

    /**
     * 根据vpn id获取CloudVpn
     * @author zhangfan
     * @param vpnId
     * @return
     */
    CloudVpn getVpnById(String vpnId);
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
}
