package com.eayun.virtualization.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.bean.ResourceCheckBean;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.Dictionary;
import com.eayun.common.constant.BillingCycleType;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.OrderType;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.costcenter.model.MoneyAccount;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.eayunstack.model.FloatIp;
import com.eayun.eayunstack.service.OpenstackFloatIpService;
import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.order.model.BaseOrderResource;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.price.service.BillingFactorService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.unit.model.BaseWebDataCenterIp;
import com.eayun.unit.service.EcscRecordService;
import com.eayun.virtualization.baseservice.BaseFloatIpService;
import com.eayun.virtualization.dao.CloudFloatIpDao;
import com.eayun.virtualization.dao.CloudOrderFloatIpDao;
import com.eayun.virtualization.model.BaseCloudBatchResource;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudOrderFloatIp;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudBatchResource;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudOrderFloatIp;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.service.CloudBatchResourceService;
import com.eayun.virtualization.service.CloudFloatIpService;
import com.eayun.virtualization.service.TagService;


/**
 * 浮动IP
 *
 * @Filename: FloatIpServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br> <li>Date: 2015年11月3日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Transactional
@Service
public class CloudFloatIpServiceImpl extends BaseFloatIpService implements CloudFloatIpService {

    private static final Logger log = LoggerFactory.getLogger(CloudFloatIpServiceImpl.class);

    @Autowired
    private CloudFloatIpDao cloudFloatIpDao;

    @Autowired
    private OpenstackFloatIpService openFloatIpService;

    @Autowired
    private TagService tagService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private BillingFactorService billingFactorService;

    @Autowired
    private CloudOrderFloatIpDao cloudOrderFloatIpDao;

    @Autowired
    private EayunRabbitTemplate eayunRabbitTemplate;

    @Autowired
    private CloudBatchResourceService cloudBatchResourceService;

    @Autowired
    private AccountOverviewService accountOverviewSerivce;
    @Autowired
    private MessageCenterService messageCenterService;
    
    @Autowired
	private EcscRecordService ecscRecordService;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Page getListByPrj(Page page, String projectId, QueryMap queryMap) {
        log.info("获取浮动IP列表");
        List<Object> list = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append(" select ");
        sql.append("  	flo.flo_id ,");
        sql.append("    flo.flo_ip ,");
        sql.append("    flo.resource_id ,");
        sql.append("    flo.resource_type ,");
        sql.append("    CASE WHEN resource_type = 'vm' THEN concat('云主机：', vm.vm_name) ");
        sql.append("         WHEN resource_type = 'lb' THEN concat('负载均衡：', lb.pool_name) ");
        sql.append("    ELSE '' END as resourceName,");
        sql.append("    CASE WHEN resource_type = 'vm' THEN concat('云主机（', vm.vm_name, '）') ");
        sql.append("         WHEN resource_type = 'lb' THEN concat('负载均衡（', lb.pool_name, '）') ");
        sql.append("    ELSE '' END as resName,");
        sql.append("    CASE WHEN resource_type = 'vm' THEN vm.vm_ip");
        sql.append("         WHEN resource_type = 'lb' THEN lb.vip_address");
        sql.append("    ELSE '' END as subnet,");
        sql.append("    flo.prj_id ,");
        sql.append("    flo.dc_id ,");
        sql.append("    flo.charge_state, ");
        sql.append("    flo.pay_type, ");
        sql.append("    flo.end_time, ");
        sql.append("    flo.create_time, ");
        sql.append("    project.customer_id ");
        sql.append(" from cloud_floatip flo  ");
        sql.append(" left join cloud_vm vm   ");
        sql.append("    ON flo.resource_id = vm.vm_id ");
        sql.append("    AND flo.resource_type = 'vm' ");
        sql.append(" left join ");
        sql.append("    cloud_project project ");
        sql.append(" on project.prj_id = flo.prj_id ");
        sql.append(" LEFT JOIN (    ");
        sql.append("    SELECT ");
        sql.append("    	pool.pool_id,");
        sql.append("    	pool.pool_name,");
        sql.append("    	vip.vip_address");
        sql.append("    FROM cloud_ldpool pool");
        sql.append("    LEFT JOIN cloud_ldvip vip");
        sql.append("    	ON vip.pool_id = pool.pool_id");
        sql.append(" ) lb   ");
        sql.append("   ON lb.pool_id = flo.resource_id ");
        sql.append("   AND flo.resource_type = 'lb' ");
        sql.append(" where 1=1   ");
        sql.append("    and flo.is_deleted = '0' ");
        sql.append("    and flo.prj_id = ?   ");
        sql.append("    and flo.is_visable='1' ");
        sql.append(" order by flo.create_time desc   ");

        list.add(projectId);

        page = cloudFloatIpDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
        List newlist = (List) page.getResult();
        for (int i = 0; i < newlist.size(); i++) {
            Object[] objs = (Object[]) newlist.get(i);
            CloudFloatIp floatIp = new CloudFloatIp();
            floatIp.setFloId(String.valueOf(objs[0]));
            floatIp.setFloIp(String.valueOf(objs[1]));
            floatIp.setResourceId(String.valueOf(objs[2]));
            floatIp.setResourceType(String.valueOf(objs[3]));
            floatIp.setResourceName(String.valueOf(objs[4]));
            floatIp.setResNameForRenew(String.valueOf(objs[5]));
            floatIp.setVmIp(String.valueOf(objs[6]));
            floatIp.setPrjId(String.valueOf(objs[7]));
            floatIp.setDcId(String.valueOf(objs[8]));
            floatIp.setChargeState(String.valueOf(objs[9]));
            floatIp.setPayType(String.valueOf(objs[10]));
            floatIp.setEndTime(objs[11] != null ? DateUtil.stringToDate(String.valueOf(objs[11])) : null);
            floatIp.setCreateTime(objs[12] != null ? DateUtil.stringToDate(String.valueOf(objs[12])) : null);
            floatIp.setCusId(String.valueOf(objs[13]));
            String tagsName = tagService.getResourceTagForShowcase("floatIP", floatIp.getFloId());
            floatIp.setTagsName(tagsName);
            String chargeName = "";
            if ("0".equals(floatIp.getChargeState())) {
                if (floatIp.getResourceId() != null && !"null".equals(floatIp.getResourceId())) {
                    chargeName = "已使用";
                } else {
                    chargeName = "未使用";
                }
            } else if ("1".equals(floatIp.getChargeState())) {
                chargeName = "余额不足";
            } else if ("2".equals(floatIp.getChargeState())) {
                chargeName = "已到期";
            }
            floatIp.setChargeStateName(chargeName);
            newlist.set(i, floatIp);
        }
        return page;
    }

    /**
     * 查询项目下未绑定云主机的浮动 IP列表
     * ------------------
     *
     * @param prjId
     * @return
     * @author zhouhaitao
     */
    @SuppressWarnings("unchecked")
    public List<BaseCloudFloatIp> getUnBindFloatIp(String prjId) {
        StringBuffer hql = new StringBuffer();
        hql.append(" from BaseCloudFloatIp ");
        hql.append(" where prjId = ? ");
        hql.append(" and resourceId is null ");
        hql.append(" and resourceType is null ");
        hql.append(" and isDeleted = '0' ");
        hql.append(" and chargeState = '0' ");
        hql.append(" and isVisable = '1' ");
        return cloudFloatIpDao.find(hql.toString(), new Object[]{prjId});

    }

    @Override
    public int getCountByPro(String prjId) {
        return cloudFloatIpDao.getCountByPro(prjId);
    }

    /**
     * 查询项目下的FloatIp的配额和使用情况
     *
     * @param prjId
     * @return
     * @author zhouhaitao
     */
    @SuppressWarnings("rawtypes")
    public CloudProject queryFloatIpQuatoByPrj(String prjId) {
        CloudProject project = new CloudProject();

        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT  ");
        sql.append("  	cf.usedCount, ");
        sql.append("   	cp.prj_id,");
        sql.append("   	cp.outerip");
        sql.append(" FROM cloud_project cp  ");
        sql.append(" LEFT JOIN ( ");
        sql.append("      select cf.prj_id,count(1) as  usedCount ");
        sql.append("      from cloud_floatip cf     ");
        sql.append("      where cf. is_deleted = ?  ");
        sql.append("      and   cf.prj_id = ?    ");
        sql.append("      ) as cf       ");
        sql.append("   ON cp.prj_id = cf.prj_id ");
        sql.append(" WHERE cp.prj_id = ? ");

        Query query = cloudFloatIpDao.createSQLNativeQuery(sql.toString(), new Object[]{'0', prjId, prjId});

        List list = query.getResultList();

        if (null != list && list.size() == 1) {
            Object[] obj = (Object[]) list.get(0);
            project.setOuterIPUse(Integer.parseInt(obj[0] == null ? "0" : String.valueOf(obj[0])));
            project.setProjectId(String.valueOf(obj[1]));
            project.setOuterIP(Integer.parseInt(String.valueOf(obj[2]) == null ? "0" : String.valueOf(obj[2])));
        }

        return project;
    }


    /**
     * 申请创建公网IP
     *
     * @param cloudFloatIp
     * @return
     * @author zhouhaitao
     */
    public CloudFloatIp createFloatIp(CloudFloatIp cloudFloatIp) throws AppException {
        BaseCloudNetwork network = (BaseCloudNetwork) cloudFloatIpDao.findUnique("from BaseCloudNetwork where dcId =? and routerExternal = ? ", new Object[]{cloudFloatIp.getDcId(), "1"});
        FloatIp floatip = openFloatIpService.allocateIp(cloudFloatIp.getDcId(), cloudFloatIp.getPrjId(), network.getNetId());
        if (null != floatip && !StringUtils.isEmpty(floatip.getId())) {
            BaseCloudFloatIp baseCloudFloatIp = new BaseCloudFloatIp();
            BeanUtils.copyPropertiesByModel(baseCloudFloatIp, cloudFloatIp);
            baseCloudFloatIp.setFloId(floatip.getId());
            baseCloudFloatIp.setFloIp(floatip.getIp());
            baseCloudFloatIp.setFloStatus("1");
            if (!StringUtils.isEmpty(floatip.getInstance_id())) {
                baseCloudFloatIp.setResourceId(floatip.getInstance_id());
                baseCloudFloatIp.setResourceType("vm");
                baseCloudFloatIp.setFloStatus("0");
            }
            baseCloudFloatIp.setNetId(network.getNetId());
            baseCloudFloatIp.setCreateTime(new Date());
            baseCloudFloatIp.setIsDeleted("0");
            baseCloudFloatIp.setIsVisable("0");
            cloudFloatIpDao.save(baseCloudFloatIp);
            BeanUtils.copyPropertiesByModel(cloudFloatIp, baseCloudFloatIp);

        }
        return cloudFloatIp;
    }

    /**
     * 释放公网IP
     * 调用地方：
     * ③公网ip过期处理(超过保留时长)（预付费），此时需要改变isDeleted为1，chargeState位3（到期超过保留时长已处理），floStatus为1（资源未使用）
     * @param cloudFloatIp
     * @return
     * @author zhouhaitao
     * @throws Exception 
     */
    public CloudFloatIp releaseFloatIp(CloudFloatIp cloudFloatIp) throws Exception{
        boolean flag = openFloatIpService.deallocateFloatIp(cloudFloatIp.getDcId(), cloudFloatIp.getPrjId(), cloudFloatIp.getFloId());
        if (flag) {
            BaseCloudFloatIp baseCloudFloatIp = cloudFloatIpDao.findOne(cloudFloatIp.getFloId());
            baseCloudFloatIp.setIsDeleted("1");
            baseCloudFloatIp.setFloStatus("1");//未占用
            baseCloudFloatIp.setResourceId(null);
            baseCloudFloatIp.setResourceType(null);
            baseCloudFloatIp.setDeleteTime(new Date());
            if(PayType.PAYBEFORE.equals(baseCloudFloatIp.getPayType())){
            	baseCloudFloatIp.setChargeState("3");
            }
            cloudFloatIpDao.saveOrUpdate(baseCloudFloatIp);
            BeanUtils.copyPropertiesByModel(cloudFloatIp, baseCloudFloatIp);
            tagService.refreshCacheAftDelRes("floatIP",cloudFloatIp.getFloId());
            
            ecscRecordService.deleteWebsiteByIP(baseCloudFloatIp.getFloIp());		//根据IP删除网站信息
        }
        return cloudFloatIp;
    }
    /**
     * 释放公网IP
     * 调用地方：
     * ①手动释放
     * ②修改公网ip(超过保留时长)（后付费）
     * @param cloudFloatIp
     * @return
     * @author zhouhaitao
     * @throws Exception 
     */
    public CloudFloatIp releaseFloatIpAfter(CloudFloatIp cloudFloatIp) throws Exception{
        boolean flag = openFloatIpService.deallocateFloatIp(cloudFloatIp.getDcId(), cloudFloatIp.getPrjId(), cloudFloatIp.getFloId());
        if (flag) {
            BaseCloudFloatIp baseCloudFloatIp = cloudFloatIpDao.findOne(cloudFloatIp.getFloId());
            baseCloudFloatIp.setIsDeleted("1");
            baseCloudFloatIp.setFloStatus("1");//未占用
            baseCloudFloatIp.setResourceId(null);
            baseCloudFloatIp.setResourceType(null);
            baseCloudFloatIp.setDeleteTime(new Date());
            cloudFloatIpDao.saveOrUpdate(baseCloudFloatIp);
            BeanUtils.copyPropertiesByModel(cloudFloatIp, baseCloudFloatIp);
            tagService.refreshCacheAftDelRes("floatIP",cloudFloatIp.getFloId());
            
            ecscRecordService.deleteWebsiteByIP(baseCloudFloatIp.getFloIp());		//根据IP删除网站信息
        }
        return cloudFloatIp;
    }

    /**
     * 查询项目下的网络列表
     *
     * @param prjId
     * @return
     * @author zhouhaitao
     */
    @SuppressWarnings("unchecked")
    public List<BaseCloudNetwork> getNetworkByPrj(String prjId) {
        StringBuffer hql = new StringBuffer();
        hql.append("from  BaseCloudNetwork where prjId= ? and chargeState ='0'");
        List<BaseCloudNetwork> list = cloudFloatIpDao.find(hql.toString(), new Object[]{prjId});

        return list;
    }

    /**
     * 查询网络下的子网
     *
     * @param netId
     * @return
     * @author zhouhaitao
     */
    @SuppressWarnings("unchecked")
    public List<BaseCloudSubNetWork> getSubnetByNetwork(String netId) {
        StringBuffer hql = new StringBuffer();
        hql.append("from  BaseCloudSubNetWork where netId= ? ");
        List<BaseCloudSubNetWork> list = cloudFloatIpDao.find(hql.toString(), new Object[]{netId});

        return list;
    }


    /**
     * 查询子网下指定的资源
     *
     * @param cloudFloatIp
     * @return
     * @author zhouhaitao
     */
    @SuppressWarnings("rawtypes")
    public List<CloudFloatIp> getResourceBySubnet(CloudFloatIp cloudFloatIp) {
        StringBuffer sql = new StringBuffer();
        List<CloudFloatIp> resourceList = new ArrayList<CloudFloatIp>();

        sql.append(" select  ");
        if ("vm".equals(cloudFloatIp.getResourceType())) {
            sql.append(" vm_id,vm_name,vm_ip,'' ");
            sql.append(" from cloud_vm ");
            sql.append(" where charge_state='0'");
            sql.append(" and subnet_id = ?  ");
            sql.append(" and is_deleted = '0' ");
            sql.append(" and (vm_status = 'ACTIVE' or vm_status ='SHUTOFF' or vm_status ='SUSPENDED') ");
            sql.append(" and vm_id not in ");
        } else if ("lb".equals(cloudFloatIp.getResourceType())) {
            sql.append(" pool.pool_id,");
            sql.append(" pool.pool_name,");
            sql.append(" vip.vip_address,");
            sql.append(" vip.port_id");
            sql.append(" from cloud_ldvip vip");
            sql.append(" left join cloud_ldpool pool");
            sql.append(" on pool.pool_id = vip.pool_id");
            sql.append(" where pool.charge_state='0'");
            sql.append(" and vip.subnet_id = ?");
            sql.append(" and vip.vip_status = 'ACTIVE'");
            sql.append(" and pool.pool_id not in");
        }
        sql.append(" (");
        sql.append(" select resource_id");
        sql.append(" from cloud_floatip ");
        sql.append(" where is_deleted = '0'");
        sql.append(" and resource_id is not null");
        sql.append(" and charge_state='0'");
        sql.append(" )");

        Query query = cloudFloatIpDao.createSQLNativeQuery(sql.toString(), new Object[]{cloudFloatIp.getSubnetIp()});
        List list = query.getResultList();
        if (null != list && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Object[] objs = (Object[]) list.get(i);
                CloudFloatIp floatip = new CloudFloatIp();
                floatip.setResourceId(String.valueOf(objs[0]));
                floatip.setResourceName(String.valueOf(objs[1]));
                floatip.setSubnetIp(String.valueOf(objs[2]));
                floatip.setPortId(String.valueOf(objs[3]));

                resourceList.add(floatip);
            }
        }
        return resourceList;
    }

    public String getIpInfoById(String id) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("select flo_ip from cloud_floatip where flo_id=?");
        javax.persistence.Query query = cloudFloatIpDao.createSQLNativeQuery(sb.toString(), id);
        List list = query.getResultList();
        String ip = "";
        for(int i=0;i<list.size();i++){
            ip = (String) list.get(i);
        }
        return ip;
    }

    /**
     * 公网IP绑定资源
     *
     * @param floatIp
     * @return
     * @author zhouhaitao
     */
    @Transactional(noRollbackFor=AppException.class)
    public CloudFloatIp bindResource(CloudFloatIp floatIp) {
        boolean flag = false;
        if ("vm".equals(floatIp.getResourceType())) {
            flag = openFloatIpService.addFloatIp(floatIp.getDcId(), floatIp.getPrjId(), floatIp.getResourceId(), floatIp.getVmIp(),floatIp.getFloIp());
        } else if ("lb".equals(floatIp.getResourceType())) {
            flag = openFloatIpService.bindLoadBalancerFloatIp(floatIp.getDcId(), floatIp.getPrjId(), floatIp.getPortId(), floatIp.getFloId());
        }

        if (flag) {
            BaseCloudFloatIp cloudFloatIp = cloudFloatIpDao.findOne(floatIp.getFloId());
            cloudFloatIp.setResourceId(floatIp.getResourceId());
            cloudFloatIp.setResourceType(floatIp.getResourceType());
            cloudFloatIp.setFloStatus("0");
            cloudFloatIpDao.saveOrUpdate(cloudFloatIp);
        }

        return floatIp;
    }

    /**
     * 公网IP解绑资源
     *
     * @param floatIp
     * @return
     * @author zhouhaitao
     */
    public CloudFloatIp unbundingResource(CloudFloatIp floatIp) {
        boolean flag = false;
        if ("vm".equals(floatIp.getResourceType())) {
            flag = openFloatIpService.removeFloatIp(floatIp.getDcId(), floatIp.getPrjId(), floatIp.getResourceId(), floatIp.getFloIp());
        } else if ("lb".equals(floatIp.getResourceType())) {
            flag = openFloatIpService.bindLoadBalancerFloatIp(floatIp.getDcId(), floatIp.getPrjId(), null, floatIp.getFloId());
        }

        if (flag) {
            BaseCloudFloatIp cloudFloatIp = cloudFloatIpDao.findOne(floatIp.getFloId());
            cloudFloatIp.setResourceId(null);
            cloudFloatIp.setResourceType(null);
            cloudFloatIp.setFloStatus("1");
            cloudFloatIpDao.saveOrUpdate(cloudFloatIp);
        }

        return floatIp;
    }
//----------------------------------------陈鹏飞--------------------------

    /**
     * @param cloudOrderFloatIp 浮动ip订单数据
     * @param isCreateOrder     是否创建订单
     * @return
     */

    public CloudOrderFloatIp buyFloatIp(CloudOrderFloatIp cloudOrderFloatIp, boolean isCreateOrder) throws Exception {
        List<CloudFloatIp> list = new ArrayList<>();
        int ipSurplus = this.findFloIpSurplus(cloudOrderFloatIp.getPrjId());
        try {
            if (ipSurplus >= cloudOrderFloatIp.getProductCount()) {//判断配额
                if (isCreateOrder) {//生成订单--自调用
                    ParamBean paramBean = this.fromParamBean(cloudOrderFloatIp);
                    PriceDetails priceDetails = billingFactorService.getPriceDetails(paramBean);//得出单个资源应付的价钱
                    BigDecimal price = this.getPrice(cloudOrderFloatIp);//billingFactorService.getPriceByFactor(paramBean).setScale(2, RoundingMode.FLOOR);//得出总价
                    if (PayType.PAYBEFORE.equals(cloudOrderFloatIp.getPayType()) && price.compareTo(cloudOrderFloatIp.getPrice()) != 0) {
                        CloudFloatIp cloudFloatIp = new CloudFloatIp();
                        cloudFloatIp.setErrMsg("您的订单金额发生变动，请重新确认订单！");
                        cloudFloatIp.setBtnFlag("1");
                        list.add(0, cloudFloatIp);
                        cloudOrderFloatIp.setCloudFloatIpList(list);
                        return cloudOrderFloatIp;
                    }
                    MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(cloudOrderFloatIp.getCusId());
                    // XXX 开通服务余额限定值
                    SysDataTree sysDataTree = DictUtil.getDataTreeByNodeId(Dictionary.buyNodeId);
                    BigDecimal createResourceLimitedMoney = new BigDecimal(sysDataTree.getPara1());
                    if (accountMoney.getMoney().compareTo(createResourceLimitedMoney) < 0
                    		&& PayType.PAYAFTER.equals(cloudOrderFloatIp.getPayType())) {
                        CloudFloatIp cloudFloatIp = new CloudFloatIp();
                        cloudFloatIp.setErrMsg("您的账户余额不足" + createResourceLimitedMoney + "元，请充值后操作!");
                        cloudFloatIp.setBtnFlag("0");
                        list.add(0, cloudFloatIp);
                        cloudOrderFloatIp.setCloudFloatIpList(list);
                        return cloudOrderFloatIp;
                    }
                    cloudOrderFloatIp.setPrice(price);
                    Order order = this.fromOrder(cloudOrderFloatIp, priceDetails, price);//生成订单
                    cloudOrderFloatIp.setOrderNo(order.getOrderNo());
                    cloudOrderFloatIp.setOrderType(order.getOrderType());
                    cloudOrderFloatIp.setCreateTime(new Date());
                }
                BaseCloudOrderFloatIp baseCloudOrderFloatIp = new BaseCloudOrderFloatIp();
                BeanUtils.copyPropertiesByModel(baseCloudOrderFloatIp, cloudOrderFloatIp);
                cloudOrderFloatIpDao.saveEntity(baseCloudOrderFloatIp);//创建弹性公网id订单
                if (PayType.PAYBEFORE.equals(cloudOrderFloatIp.getPayType())) {//包年包月
                    /*拼装弹性公网ip的预付费order类*/
                    
                } else if (PayType.PAYAFTER.equals(cloudOrderFloatIp.getPayType()) && isCreateOrder) {//按需计费
                    try {
                        list = this.addFloatIp(cloudOrderFloatIp.getOrderNo(), isCreateOrder);
                    } catch (AppException e) {
                        log.error(e.getMessage(), e);
                        throw new Exception(e);
                    }
                }
                return cloudOrderFloatIp;
            } else {
                CloudFloatIp cloudFloatIp = new CloudFloatIp();
                cloudFloatIp.setErrMsg("您的公网IP数量配额不足，请提交工单申请配额");
                cloudFloatIp.setBtnFlag("0");
                list.add(0, cloudFloatIp);
                cloudOrderFloatIp.setCloudFloatIpList(list);
                return cloudOrderFloatIp;
            }
        } catch (Exception e) {
            log.error(e.toString(),e);
            if ("余额不足".equals(e.getMessage())&&PayType.PAYBEFORE.equals(cloudOrderFloatIp.getPayType())) {
                CloudFloatIp cloudFloatIp = new CloudFloatIp();
                cloudFloatIp.setErrMsg("您的余额发生变动，请重新确认订单");
                cloudFloatIp.setBtnFlag("1");
                list.add(0, cloudFloatIp);
                cloudOrderFloatIp.setCloudFloatIpList(list);
                return cloudOrderFloatIp;
            }else{
                throw e;
            }
        }
    }

    ;
    /*
    拼接ParamBean``
     */

    private ParamBean fromParamBean(CloudOrderFloatIp cloudOrderFloatIp) {
        ParamBean paramBean = new ParamBean();
        paramBean.setIpCount(1);//弹性公网ip个数
        paramBean.setNumber(cloudOrderFloatIp.getProductCount());//批量创建的数量
        paramBean.setDcId(cloudOrderFloatIp.getDcId());
        paramBean.setPayType(cloudOrderFloatIp.getPayType());
        paramBean.setCycleCount(cloudOrderFloatIp.getBuyCycle());
        return paramBean;
    }

    /*
    拼接订单数据-并生成订单
     */
    private Order fromOrder(CloudOrderFloatIp cloudOrderFloatIp, PriceDetails priceDetails, BigDecimal price) throws Exception {
        Order order = new Order();
        StringBuilder prodName = new StringBuilder("公网IP");
        if (PayType.PAYAFTER.equals(cloudOrderFloatIp.getPayType())) {
            prodName.append("-按需付费");
        } else {
            prodName.append("-包年包月");
            order.setPaymentAmount(price.setScale(2, RoundingMode.FLOOR));
            order.setAccountPayment(cloudOrderFloatIp.getAccountPayment().setScale(2, RoundingMode.FLOOR));
            order.setThirdPartPayment(cloudOrderFloatIp.getThirdPartPayment().setScale(2, RoundingMode.FLOOR));
        }
        order.setOrderType(OrderType.NEW);
        order.setProdName(prodName.toString());
        order.setDcId(cloudOrderFloatIp.getDcId());
        order.setProdCount(cloudOrderFloatIp.getProductCount());
        order.setProdConfig("数据中心：" + cloudOrderFloatIp.getDcName());
        order.setPayType(cloudOrderFloatIp.getPayType());
        order.setBuyCycle(cloudOrderFloatIp.getBuyCycle());
        order.setUnitPrice(priceDetails.getIpPrice());
        order.setResourceType(ResourceType.FLOATIP);
        order.setUserId(cloudOrderFloatIp.getCreUser());
        order.setCusId(cloudOrderFloatIp.getCusId());
        order.setBillingCycle(BillingCycleType.HOUR);
        order = orderService.createOrder(order);
        return order;
    }

    /**
     * 根据订单或者批量释放弹性公网IP
     *
     * @param orderNo
     * @return
     */
    public void releaseFloatIpByOrderNo(String orderNo) throws Exception {
            List<MessageOrderResourceNotice> list = new ArrayList<>();
            StringBuffer hql = new StringBuffer("from BaseCloudOrderFloatIp where orderNo =?");
            BaseCloudOrderFloatIp baseCloudOrderFloatIp = (BaseCloudOrderFloatIp) cloudOrderFloatIpDao.findUnique(hql.toString(), new Object[]{orderNo});
            List<BaseCloudBatchResource> cloudBatResList = cloudBatchResourceService.queryListByOrder(orderNo);
            for (BaseCloudBatchResource baseCloudBatchResource : cloudBatResList) {
            	if(CloudBatchResource.RESOURCE_FLOATIP.equals(baseCloudBatchResource.getResourceType())){
            		CloudFloatIp cloudFloatIp = new CloudFloatIp();
            		cloudFloatIp.setFloId(baseCloudBatchResource.getResourceId());
            		cloudFloatIp.setDcId(baseCloudOrderFloatIp.getDcId());
            		cloudFloatIp.setPrjId(baseCloudOrderFloatIp.getPrjId());
            		boolean flag = openFloatIpService.deallocateFloatIp(cloudFloatIp.getDcId(), cloudFloatIp.getPrjId(), cloudFloatIp.getFloId());
            		if(!flag){
            			MessageOrderResourceNotice messageOrderResourceNotice = new MessageOrderResourceNotice();
            			messageOrderResourceNotice.setResourceName(cloudFloatIp.getFloIp());
            			messageOrderResourceNotice.setResourceId(cloudFloatIp.getFloId());
            			messageOrderResourceNotice.setOrderNo(orderNo);
            			messageOrderResourceNotice.setResourceType(ResourceType.getName(ResourceType.FLOATIP));
            			list.add(messageOrderResourceNotice);
            		}else{//删除数据库中的数据，与批量创建表中的数据
            			
            			cloudFloatIpDao.delete(baseCloudBatchResource.getResourceId());
            			
            			CloudBatchResource cloudBatchResource = new CloudBatchResource();
            			
            			cloudBatchResource.setResourceId(baseCloudBatchResource.getResourceId());
            			cloudBatchResource.setOrderNo(orderNo);
            			cloudBatchResourceService.delete(cloudBatchResource);
            		}
            	}
            }
            if(list.size()>0){
                messageCenterService.delecteResourFailMessage(list, orderNo);
            }
    }

    ;


    public CloudOrderFloatIp renewFloatIp(CloudOrderFloatIp cloudOrderFloatIp, String cusId) throws Exception {
        ParamBean paramBean = this.fromParamBean(cloudOrderFloatIp);
        PriceDetails priceDetails = billingFactorService.getPriceDetails(paramBean);
        BigDecimal price = billingFactorService.getPriceByFactor(paramBean);//得出总价
        Order order = this.fromOrder(cloudOrderFloatIp, priceDetails, price);
        cloudOrderFloatIp.setOrderNo(order.getOrderNo());
        cloudOrderFloatIp.setCusId(cusId);
        cloudOrderFloatIp.setCreateTime(new Date());
        BaseCloudOrderFloatIp baseCloudOrderFloatIp = new BaseCloudOrderFloatIp();
        BeanUtils.copyPropertiesByModel(baseCloudOrderFloatIp, cloudOrderFloatIp);
        cloudOrderFloatIpDao.saveEntity(baseCloudOrderFloatIp);
        return cloudOrderFloatIp;
    }

    @Override
    public int findFloIpSurplus(String prjId) {
        /*StringBuilder sql = new StringBuilder();
        sql.append("SELECT cp.outerip-(COUNT(cf.flo_id)+SUM(IFNULL(oi.prod_count,0))) as ipSurplus");
        sql.append(" FROM cloud_project cp"); and is_visable='1'
        sql.append(" LEFT JOIN cloud_floatip cf ON cf.prj_id = cp.prj_id and cf.is_deleted='0'");
        sql.append(" LEFT JOIN cloudorder_floatip cof on cof.prj_id = cp.prj_id");
        sql.append(" LEFT JOIN order_info oi ON oi.order_no=cof.order_no and oi.order_state in('1','2')");
        sql.append(" where cp.prj_id=?");
        List<String> values = new ArrayList<>();
        values.add(prjId);
        Query query = cloudFloatIpDao.createSQLNativeQuery(sql.toString(), values.toArray());
        List<Object> list = query.getResultList();
        int ipSurplus = Integer.valueOf(String.valueOf(list.get(0)));
        return ipSurplus;*/
        
        int quota =  getQuotaOfFloatIp(prjId);
        int exist =  cloudFloatIpDao.getCountByPrjIdVisibled(prjId);
        int order =  getFloatCountInOrder(prjId);
        return quota - exist - order;
    }
    /**
     * 获取订单中待创建或待支付的弹性公网ip
     * @author gaoxiang
     * @param prjId
     * @return
     */
    private int getFloatCountInOrder(String prjId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select ");
        sql.append("    cof.product_count ");
        sql.append(" from ");
        sql.append("    cloudorder_floatip cof ");
        sql.append(" left join ");
        sql.append("    order_info oi ");
        sql.append(" on ");
        sql.append("    cof.order_no = oi.order_no ");
        sql.append(" where ");
        sql.append("    cof.prj_id = ? ");
        sql.append(" and ");
        sql.append("    oi.order_type = '0' ");
        sql.append(" and ");
        sql.append("    oi.order_state in ('1', '2') ");
        sql.append(" and ");
        sql.append("    oi.resource_type in ('0', '5') ");
        List<String> values = new ArrayList<String>();
        values.add(prjId);
        Query query = cloudFloatIpDao.createSQLNativeQuery(sql.toString(), values.toArray());
        int total = 0;
        List list = query.getResultList();
        for (int i =0 ; i< list.size();i++) {
            total += (Integer) list.get(i);
        }
        return total;
    }
    /**
     * 获取已经创建的弹性公网ip
     * @author gaoxiang
     * @param prjId
     * @return
     */
    private int getQuotaOfFloatIp(String prjId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" select ");
        sql.append("    outerip ");
        sql.append(" from ");
        sql.append("    cloud_project ");
        sql.append(" where ");
        sql.append("    prj_id = ? ");
        List<String> values = new ArrayList<String>();
        values.add(prjId);
        Query query = cloudFloatIpDao.createSQLNativeQuery(sql.toString(), values.toArray());
        List<Object> list = query.getResultList();
        int quota = Integer.valueOf(list.get(0).toString());
        return quota;
    }

    private BaseCloudBatchResource fromBaseCloudBatchResource(CloudFloatIp cloudFloatIp, CloudOrderFloatIp cloudOrderFloatIp) {
        BaseCloudBatchResource baseCloudBatchResource = new BaseCloudBatchResource();
        baseCloudBatchResource.setResourceId(cloudFloatIp.getFloId());
        baseCloudBatchResource.setResourceType(CloudBatchResource.RESOURCE_FLOATIP);
        baseCloudBatchResource.setOrderNo(cloudOrderFloatIp.getOrderNo());
        cloudBatchResourceService.save(baseCloudBatchResource);
        return baseCloudBatchResource;
    }

    @Override
    public List<CloudFloatIp> addFloatIp(String orderNo, boolean isCreateOrder) throws Exception {
        List<CloudFloatIp> floatIpList = new ArrayList<>();
        StringBuilder hql = new StringBuilder();
        boolean bool = false;
        hql.append("from BaseCloudOrderFloatIp where orderNo = ?");
        List<String> values = new ArrayList<>();
        values.add(orderNo);
        BaseCloudOrderFloatIp baseCloudOrderFloatIp = (BaseCloudOrderFloatIp) cloudOrderFloatIpDao.findUnique(hql.toString(), values.toArray());
        CloudOrderFloatIp cloudOrderFloatIp = new CloudOrderFloatIp();
        BeanUtils.copyPropertiesByModel(cloudOrderFloatIp, baseCloudOrderFloatIp);
        Date endTime = null;
        if (PayType.PAYBEFORE.equals(cloudOrderFloatIp.getPayType())) {
            endTime = DateUtil.getExpirationDate(new Date(), cloudOrderFloatIp.getBuyCycle(), DateUtil.PURCHASE);
        }
        try {
            StringBuffer floIds = new StringBuffer();
            for (int i = 0; i < cloudOrderFloatIp.getProductCount(); i++) {
                CloudFloatIp cloudFloatIp = new CloudFloatIp();
                BeanUtils.copyPropertiesByModel(cloudFloatIp, cloudOrderFloatIp);
                cloudFloatIp.setIsVisable("0");
                cloudFloatIp.setEndTime(endTime);
                cloudFloatIp.setChargeState("0");
                cloudFloatIp = this.createFloatIp(cloudFloatIp);
                this.fromBaseCloudBatchResource(cloudFloatIp, cloudOrderFloatIp);
                floatIpList.add(cloudFloatIp);
                floIds.append(cloudFloatIp.getFloId());
                if(i!=cloudOrderFloatIp.getProductCount()-1){
                    floIds.append(",");
                }
            }
            bool = true;//资源开通成功

            if (bool && isCreateOrder) {
                //修改弹性公网ip的显现状态为"1"展现(自创建)
                List<String> values1 = new ArrayList<>();
                StringBuilder hql1 = new StringBuilder("update BaseCloudFloatIp set isVisable='1'");
                hql1.append(" where floId in (select resourceId from BaseCloudBatchResource where resourceType=? and orderNo =?)");
                values1.add(CloudBatchResource.RESOURCE_FLOATIP);
                values1.add(orderNo);
                int i = cloudFloatIpDao.executeUpdate(hql1.toString(), values1.toArray());
                cloudBatchResourceService.deleteByOrder(orderNo);//全部创建成功后删除批量创建中间记录
            }
            if (PayType.PAYAFTER.equals(cloudOrderFloatIp.getPayType()) && isCreateOrder) {//按需付费，发送计费消息
                this.sendMessage(floatIpList, EayunQueueConstant.QUEUE_BILL_RESOURCE_PURCHASE,
                        cloudOrderFloatIp.getCusId(), cloudOrderFloatIp.getOrderNo());
            } else if (PayType.PAYBEFORE.equals(cloudOrderFloatIp.getPayType())) {//包年包月

            }

            baseCloudOrderFloatIp.setFloId(floIds.toString());

            cloudOrderFloatIpDao.saveOrUpdate(baseCloudOrderFloatIp);//回写ip订单表
        } catch (Exception e) {
            bool = false;
            log.error("创建公网ip失败："+e.getMessage());
            if(isCreateOrder){
                this.releaseFloatIpByOrderNo(orderNo);
            }
//            throw e;
        } finally {
            if (isCreateOrder) {//生成订单
                orderService.completeOrder(orderNo, bool, this.getBaseOrderResList(floatIpList, orderNo));
                if(!bool){
                    messageCenterService.addResourFailMessage(cloudOrderFloatIp.getOrderNo(), cloudOrderFloatIp.getCusId());
                }
            }
        }
        return floatIpList;
    }

    @Override
    public CloudFloatIp udpateFloatIp(String orderNo) throws Exception {
        StringBuilder hql = new StringBuilder("from BaseCloudOrderFloatIp where orderNo = ?");
        List<String> values = new ArrayList<>();
        values.add(orderNo);
        //得到续费时长
        BaseCloudOrderFloatIp baseCloOrdFloIp = (BaseCloudOrderFloatIp) cloudOrderFloatIpDao.findUnique(hql.toString(), values.toArray());
        int buyCycle = baseCloOrdFloIp.getBuyCycle();
        //计算下次到期时间
        BaseCloudFloatIp baseCloudFloatIp = cloudFloatIpDao.findOne(baseCloOrdFloIp.getFloId());
        Date endTime = DateUtil.getExpirationDate(new Date(), buyCycle, DateUtil.RENEWAL);
        baseCloudFloatIp.setEndTime(endTime);
        baseCloudFloatIp.setChargeState("0");
        baseCloudFloatIp = (BaseCloudFloatIp) cloudFloatIpDao.merge(baseCloudFloatIp);
        CloudFloatIp cloudFloatIp = new CloudFloatIp();
        BeanUtils.copyPropertiesByModel(cloudFloatIp, baseCloudFloatIp);
        //改变订单状态
        List<CloudFloatIp> cloudFloatIpList = new ArrayList<>();
        cloudFloatIpList.add(cloudFloatIp);
        List<BaseOrderResource> baseOrderResourceList = this.getBaseOrderResList(cloudFloatIpList, orderNo);
        orderService.completeOrder(baseCloOrdFloIp.getOrderNo(), true, baseOrderResourceList);
        return cloudFloatIp;
    }

    private List<BaseOrderResource> getBaseOrderResList(List<CloudFloatIp> cloudFloatIpList, String orderNo) {
        List<BaseOrderResource> list = new ArrayList<>();
        for (CloudFloatIp cloudFloatIp : cloudFloatIpList) {
            BaseOrderResource baseOrderResource = new BaseOrderResource();
            baseOrderResource.setOrderNo(orderNo);
            baseOrderResource.setResourceId(cloudFloatIp.getFloId());
            baseOrderResource.setResourceName(cloudFloatIp.getFloIp());
            list.add(baseOrderResource);
        }
        return list;
    }

    @Override
    public CloudFloatIp modifyStateForFloatIp(String floId, String resourceState, Date endTime) throws Exception {
        BaseCloudFloatIp basecloudFloatIp = cloudFloatIpDao.findOne(floId);
        basecloudFloatIp.setChargeState(resourceState);
        if (endTime != null) {
            basecloudFloatIp.setEndTime(endTime);
        }
        cloudFloatIpDao.merge(basecloudFloatIp);
        CloudFloatIp cloudFloatIp = new CloudFloatIp();
        BeanUtils.copyPropertiesByModel(cloudFloatIp, basecloudFloatIp);
        return cloudFloatIp;
    }

    @Override
    public CloudFloatIp deleteFloatIp(CloudFloatIp cloudFloatIp, String cusId) throws Exception {
        Date date =cloudFloatIp.getDeleteTime();//自调用没有删除时间，到期删除有到期时间
        
        /**
    	 * 判断资源是否有未完成的订单 --@author zhouhaitao
    	 */
        if(null == cusId && checkFloatIpOrderExist(cloudFloatIp.getFloId())){
        	throw new AppException("该资源有未完成的订单，请取消订单后再进行删除操作！");
        }
        //释放
        this.releaseFloatIpAfter(cloudFloatIp);
        if(date!=null){//到期删除，修改删除时间，过滤掉分钟和秒
            cloudFloatIp.setDeleteTime(date);
//            DateUtil.
        }
        //发送消息
        List<CloudFloatIp> floatIpList = new ArrayList<>();
        floatIpList.add(cloudFloatIp);
        if (PayType.PAYAFTER.equals(cloudFloatIp.getPayType())) {
        	if(null == cusId){
        		if(StringUtil.isEmpty(cusId)){
                    StringBuffer sql = new StringBuffer("select customer_id from cloud_project where prj_id=?");
                    Query query = cloudFloatIpDao.createSQLNativeQuery(sql.toString(),new Object[]{cloudFloatIp.getPrjId()});
                    if(query.getResultList().size()>0){
                        cusId = query.getResultList().get(0).toString();
                    }
                }
        		this.sendMessage(floatIpList, EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE, cusId, null);
        	}else{
        		for(int i = 0; i < floatIpList.size(); i++){
        			CloudFloatIp floatIp = floatIpList.get(i);
        			ChargeRecord record = new ChargeRecord();
    				
    				record.setResourceId(floatIp.getFloId());
    				record.setOpTime(floatIp.getDeleteTime());
    				record.setDatecenterId(floatIp.getDcId());
    				record.setCusId(cusId);
    				record.setResourceType(ResourceType.FLOATIP);
    				eayunRabbitTemplate.send(EayunQueueConstant.QUEUE_BILL_RESOURCE_RESTRICT, JSONObject.toJSONString(record));
        		}
        	}
            
        }
        return cloudFloatIp;
    }

    @Override
    public void sendMessage(List<CloudFloatIp> cloudFloatIpList, String messKey, String cusId, String orderNo) throws Exception {
        for (int i = 0; i < cloudFloatIpList.size(); i++) {
            CloudFloatIp cloudFloatIp = cloudFloatIpList.get(i);
            ParamBean paramBean = this.setParamBean(1, 1, cloudFloatIp.getDcId(), cloudFloatIp.getPayType());
            ChargeRecord chargeRecord = new ChargeRecord();
            chargeRecord.setParam(paramBean);
            chargeRecord.setDatecenterId(cloudFloatIp.getDcId());
            chargeRecord.setOrderNumber(orderNo);
            chargeRecord.setCusId(cusId);
            chargeRecord.setResourceName(cloudFloatIp.getFloIp());
            chargeRecord.setResourceId(cloudFloatIp.getFloId());
            chargeRecord.setResourceType(ResourceType.FLOATIP);
            if(EayunQueueConstant.QUEUE_BILL_RESOURCE_DELETE.equals(messKey)){
                chargeRecord.setOpTime(cloudFloatIp.getDeleteTime());
            }else{
                chargeRecord.setChargeFrom(new Date());
            }
            eayunRabbitTemplate.send(messKey, JSONObject.toJSONString(chargeRecord));
        }
    }

    @Override
    public BigDecimal getPrice(CloudOrderFloatIp cloudOrderFloatIp) {
        ParamBean paramBean = this.fromParamBean(cloudOrderFloatIp);
        BigDecimal price = billingFactorService.getPriceByFactor(paramBean);//得出总价
        if(PayType.PAYBEFORE.equals(cloudOrderFloatIp.getPayType())){
            return price.setScale(2, RoundingMode.FLOOR);
        }
        return price;
    }

    @Override
    public CloudFloatIp getCloudFloatIpByResId(String resourceId, String resourceType) {
        StringBuilder hql = new StringBuilder();
        hql.append("from BaseCloudFloatIp where resourceId=? and resourceType=? and isDeleted ='0'");
        List<String> values = new ArrayList<>();
        values.add(resourceId);
        values.add(resourceType);
        BaseCloudFloatIp baseCloudFloatIp = (BaseCloudFloatIp) cloudFloatIpDao.findUnique(hql.toString(), values.toArray());
        CloudFloatIp cloudFloatIp = new CloudFloatIp();
        if(null != baseCloudFloatIp){
        	BeanUtils.copyPropertiesByModel(cloudFloatIp, baseCloudFloatIp);
        }
        return cloudFloatIp;
    }

    /**
     * Date endTime,---到期时间
     * String prjId,---项目id
     * String chargeState,---浮动ip状态
     * String isDelete,---是否删除
     * String payType---付款方式
     *
     * @param map
     * @return
     */
    @Override
    public List<CloudFloatIp> getCloudFloatIpByMap(Map<String, Object> map) {
        List<Object> values = new ArrayList<>();
        StringBuilder hql = new StringBuilder();
        hql.append("from BaseCloudFloatIp where 1=1");
        Object time = map.get("endTime");
        if (time != null) {
            hql.append(" and endTime <= ?");
            values.add(map.get("endTime"));
        }
        if (map.get("prjId") != null) {
            hql.append(" and prjId = ?");
            values.add(map.get("prjId"));
        }
        if (map.get("chargeState") != null) {
            hql.append(" and chargeState= ?");
            values.add(map.get("chargeState"));
        }
        if (map.get("isDelete") != null) {
            hql.append(" and isDeleted= ?");
            values.add(map.get("isDelete"));
        }
        if (map.get("payType") != null) {
            hql.append(" and  payType = ?");
            values.add(map.get("payType"));
        }
        hql.append(" and isVisable='1' ");
        List<BaseCloudFloatIp> cloudList = cloudFloatIpDao.find(hql.toString(), values.toArray());
        List<CloudFloatIp> list = new ArrayList<>();
        for (BaseCloudFloatIp baseCloudFloatIp : cloudList) {
            CloudFloatIp cloudFloatIp = new CloudFloatIp();
            BeanUtils.copyPropertiesByModel(cloudFloatIp, baseCloudFloatIp);
            list.add(cloudFloatIp);
        }
        return list;
    }

    @Override
    public CloudOrderFloatIp getCloudOrderByOrderNo(String orderNo) {
        StringBuffer hql = new StringBuffer("from BaseCloudOrderFloatIp where orderNo =?");
        List<String> values = new ArrayList<>();
        values.add(orderNo);
        BaseCloudOrderFloatIp baseCloudOrderFloatIp = (BaseCloudOrderFloatIp) cloudOrderFloatIpDao.findUnique(hql.toString(), values.toArray());
        CloudOrderFloatIp cloudOrderFloatIp = new CloudOrderFloatIp();
        BeanUtils.copyPropertiesByModel(cloudOrderFloatIp, baseCloudOrderFloatIp);
//        cloudOrderFloatIp.setCofId(null);
        return cloudOrderFloatIp;
    }

    /**
     * 根据订单标号查询指定资源是否存在
     * @param orderNo
     * @return
     */

    @Override
    public boolean isExistsByOrderNo(String orderNo) {
        CloudOrderFloatIp orderFloatIp=this.getCloudOrderByOrderNo(orderNo);
        if (!StringUtil.isEmpty(orderFloatIp.getFloId())){
            StringBuffer strb = new StringBuffer("from BaseCloudFloatIp where floId = ? and isVisable='1' and isDeleted='0'");
            String floId = orderFloatIp.getFloId();
            List<BaseCloudFloatIp> baseCloudOrderFloatIpList=cloudFloatIpDao.find(strb.toString(),floId);
            if(null != baseCloudOrderFloatIpList && !baseCloudOrderFloatIpList.isEmpty()){
                return true;
            }
        }
        return false;

    }
    /**
     * 根据资源id查询指定资源是否存在
     * @param floId
     * @return
     */

    @Override
    public ResourceCheckBean isExistsByResourceId(String floId) {
        ResourceCheckBean resourceCheckBean = new ResourceCheckBean();
        resourceCheckBean.setExisted(false);
        BaseCloudFloatIp baseCloudFloatIp=cloudFloatIpDao.findOne(floId);
        if(baseCloudFloatIp!=null &&"1".equals(baseCloudFloatIp.getIsVisable())&& "0".equals(baseCloudFloatIp.getIsDeleted())){//生效
            resourceCheckBean.setResourceName(baseCloudFloatIp.getFloIp());
            resourceCheckBean.setExisted(true);
        }
        return resourceCheckBean;
    }

    private ParamBean setParamBean(int ipCount, int number, String dcId, String payType) {
        ParamBean paramBean = new ParamBean();
        paramBean.setIpCount(ipCount);
        paramBean.setNumber(number);
        paramBean.setDcId(dcId);
        paramBean.setPayType(payType);
        return paramBean;
    }

    /**
     * @param floId
     * @return CloudFloatIp
     */
    public CloudFloatIp findFloatIpById(String floId) {
        BaseCloudFloatIp basecloudFloatIp = cloudFloatIpDao.findOne(floId);
        CloudFloatIp cloudFloatIp = new CloudFloatIp();
        BeanUtils.copyPropertiesByModel(cloudFloatIp, basecloudFloatIp);
        return cloudFloatIp;
    }
/*******************公网Ip续费开始******************/
    /**
     * 公网Ip续费，提交按钮校验当前公网Ip是否有未完成订单，以及当前账户金额是否充足与提交订单
     *
     * @param map
     * @param userId
     * @return JSONObject
     * @author liyanchao
     */
    public JSONObject renewFloatIpOrderConfirm(Map<String, String> map, String userId, String userName, String cusId) throws Exception {
        JSONObject jsonResult = new JSONObject();

        boolean flag = false;
        flag = this.checkFloatIpOrderExist(map.get("floId").toString());
        if (!flag) {
            jsonResult = this.createFloatIpRenewOrder(map, userId, userName, cusId);
        } else {
            jsonResult.put("respCode", 1);
            jsonResult.put("message", "您当前有未完成订单，不允许提交新订单！");
        }
        return jsonResult;

    }

    /**
     * @param map
     * @param userId
     * @return JSONObject
     * @throws Exception
     * @author liyanchao
     */
    private JSONObject createFloatIpRenewOrder(Map<String, String> map, String userId, String userName, String cusId) throws Exception {

        JSONObject jsonResult = new JSONObject();
        String aliPay = (String) map.get("aliPay");//支付宝付款金额
        String accountPay = (String) map.get("accountPay");//余额付款金额
        String totalPay = (String) map.get("totalPay");
        String isAccountPay = (String) map.get("isCheck");
        String floId = (String) map.get("floId");

        BigDecimal orgTotalPay = new BigDecimal(totalPay);
        BigDecimal price = null;
        price = billingFactorService.getPriceByFactor(organizParamBean(map));
        price = price.setScale(2,BigDecimal.ROUND_FLOOR);
        if (orgTotalPay.compareTo(price) == 0) {
            if ("false".equals(isAccountPay) || null == isAccountPay) {// 直接"创建订单，跳向支付宝支付页面！";
            	if(aliPay.compareTo("0")==0){//说明没有勾选余额支付，且支付金额为零
            		Order order = orderService.createOrder(organizOrder(map, userId, cusId));
                    this.saveCloudOrderFloatIp(order, floId, userId);//创建订单后，回写业务信息
    				
    				jsonResult.put("respCode", 10);
    				jsonResult.put("message", order.getProdName());
    			}else{//跳向支付宝--没用余额
    				Order order = orderService.createOrder(organizOrder(map, userId, cusId));
                    this.saveCloudOrderFloatIp(order, floId, userId);//创建订单后，回写业务信息
    				
    				jsonResult.put("respCode", 0);
    				jsonResult.put("message", order.getOrderNo());
    			}
            	
            } else {//勾选余额支付
                BigDecimal nowAccountMoney = null;
                MoneyAccount accountMoney = accountOverviewSerivce.getAccountInfo(cusId);
                nowAccountMoney = accountMoney.getMoney();

                if (nowAccountMoney.compareTo(new BigDecimal(accountPay)) >= 0) {//当前账户余额>=余额支付的金额
                    if (new BigDecimal(aliPay).compareTo(new BigDecimal(0)) == 0) {//如果未用支付宝，只用余额支付
                        // "创建订单，直接跳转订单完成页面"
                        Order order = orderService.createOrder(organizOrder(map, userId, cusId));
                        this.saveCloudOrderFloatIp(order, floId, userId);//创建订单后，回写业务信息
                        jsonResult.put("respCode", 10);
                        jsonResult.put("message", order.getProdName());

                    } else {//有用余额+支付宝 混合支付
                        // “调用接口创建订单，跳转支付宝支付页面”
                        Order order = orderService.createOrder(organizOrder(map, userId, cusId));
                        this.saveCloudOrderFloatIp(order, floId, userId);//创建订单后，回写业务信息
                        jsonResult.put("respCode", 0);
                        jsonResult.put("message", order.getOrderNo());
                    }
                } else {//“账户余额发生变动，请重新确认订单！”;
                    jsonResult.put("respCode", 3);
                    jsonResult.put("message", "您的余额发生变动，请重新确认订单！");
                }
            }

        } else {//产品金额发生变动
            jsonResult.put("respCode", 2);
            jsonResult.put("message", "您的订单金额发生变动，请重新确认订单！");
        }

        return jsonResult;
    }

    /**
     * 组织订单参数
     **/
    private Order organizOrder(Map<String, String> map, String userId, String cusId) {
        //map.get("totalPay") :续费的总价格 //map.get("accountPay") 余额支付的金额
        BaseCloudFloatIp baseFloatIp = cloudFloatIpDao.findOne(map.get("floId").toString());

        Order order = new Order();
        order.setOrderType(OrderType.RENEW);
        order.setProdName("公网IP-续费");
        order.setDcId(baseFloatIp.getDcId());
        order.setProdCount(1);
        StringBuffer buf = new StringBuffer();
        buf.append("数据中心：" + map.get("dcName") + "<br>");
        buf.append("IP地址：" + baseFloatIp.getFloIp());
        order.setProdConfig(buf.toString());
        order.setPayType(PayType.PAYBEFORE);
        order.setBuyCycle(Integer.parseInt(map.get("buyCycle").toString()));
        order.setUnitPrice(new BigDecimal(map.get("totalPay").toString()));
        order.setResourceType(ResourceType.FLOATIP);
        order.setPaymentAmount(new BigDecimal(map.get("totalPay").toString()));
        order.setAccountPayment(new BigDecimal(map.get("accountPay").toString()));
        order.setThirdPartPayment(new BigDecimal(map.get("totalPay").toString()).subtract(new BigDecimal(map.get("accountPay").toString())));

        JSONObject params = new JSONObject();
        params.put("resourceId", map.get("floId"));
        params.put("resourceName", "");
        params.put("resourceType", ResourceType.FLOATIP);
        params.put("expirationDate", baseFloatIp.getEndTime());
        params.put("duration", map.get("buyCycle"));
        params.put("operatorIp", map.get("operatorIp"));
        order.setParams(params.toJSONString());

        order.setCusId(cusId);
        order.setUserId(userId);

        return order;
    }

    /**
     * 创建订单后，回写业务信息
     **/
    private BaseCloudFloatIp saveCloudOrderFloatIp(Order order, String floId, String userId) {
        BaseCloudFloatIp baseFloatIp = cloudFloatIpDao.findOne(floId);
        BaseCloudOrderFloatIp orderFloatIp = new BaseCloudOrderFloatIp();
        orderFloatIp.setOrderNo(order.getOrderNo());
        orderFloatIp.setFloId(floId);
        orderFloatIp.setCreateTime(new Date());
        orderFloatIp.setDcId(baseFloatIp.getDcId());
        orderFloatIp.setPrjId(baseFloatIp.getPrjId());
        orderFloatIp.setProductCount(1);
        orderFloatIp.setCreUser(userId);
        orderFloatIp.setBuyCycle(order.getBuyCycle());
        orderFloatIp.setPayType(baseFloatIp.getPayType());
        orderFloatIp.setOrderType(order.getOrderType());
        orderFloatIp.setPrice(order.getPaymentAmount());
        cloudOrderFloatIpDao.saveEntity(orderFloatIp);
        return baseFloatIp;
    }

    /**
     * 组织公网ip计费参数
     **/
    private ParamBean organizParamBean(Map<String, String> map) {
        ParamBean paramBean = new ParamBean();
        paramBean.setIpCount(1);//弹性公网ip个数
        paramBean.setNumber(Integer.parseInt(map.get("number")));
        paramBean.setDcId(map.get("dcId"));
        paramBean.setPayType(map.get("payType"));
        paramBean.setCycleCount(Integer.parseInt(map.get("cycleCount")));
        return paramBean;
    }
    /**************************公网ip续费结束************************/
    
    /**
     * 解除已删除云主机与弹性公网IP的关系
     * @param vmId  云主机ID
     */
    public void refreshFloatIpByVm(String vmId){
    	StringBuilder hql = new StringBuilder();
        hql.append("from BaseCloudFloatIp where resourceId=? and resourceType=? and isDeleted ='0'");
        List<String> values = new ArrayList<>();
        values.add(vmId);
        values.add("vm");
        
        BaseCloudFloatIp baseCloudFloatIp = (BaseCloudFloatIp) cloudFloatIpDao.findUnique(hql.toString(), values.toArray());
        if(null != baseCloudFloatIp && !StringUtils.isEmpty(baseCloudFloatIp.getFloId())){
        	baseCloudFloatIp.setResourceId(null);
        	baseCloudFloatIp.setResourceType(null);
        	cloudFloatIpDao.merge(baseCloudFloatIp);
        }
    }
    /**
     * 重新下单时，获取订单的原始配置数据
     * @author gaoxiang
     * @param orderNo
     * @return
     */
    @Override
    public CloudOrderFloatIp getOrderFloatIpByOrderNo(String orderNo){
        StringBuffer hql = new StringBuffer();
        hql.append("from BaseCloudOrderFloatIp where orderNo = ?");
        BaseCloudOrderFloatIp orderFloatIp = (BaseCloudOrderFloatIp)cloudOrderFloatIpDao.findUnique(hql.toString(), orderNo);
        CloudOrderFloatIp cloudOrderFloatIp = new CloudOrderFloatIp();
        BeanUtils.copyPropertiesByModel(cloudOrderFloatIp, orderFloatIp);
        return cloudOrderFloatIp;
    }

	@Override
	public boolean checkFloWebSite(String floIp) throws Exception {
		List<BaseWebDataCenterIp> list = ecscRecordService.getWebDataCenterIp(floIp);
		if(null == list || list.isEmpty()){
			return false;
		}
		return true;
	}
}
