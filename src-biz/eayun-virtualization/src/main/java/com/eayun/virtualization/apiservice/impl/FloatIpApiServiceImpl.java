package com.eayun.virtualization.apiservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.charge.model.ChargeRecord;
import com.eayun.common.annotation.ApiService;
import com.eayun.common.constant.EayunQueueConstant;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.exception.AppException;
import com.eayun.common.template.EayunRabbitTemplate;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.eayunstack.model.FloatIp;
import com.eayun.eayunstack.service.OpenstackFloatIpService;
import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.price.bean.ParamBean;
import com.eayun.virtualization.apiservice.FloatIpApiService;
import com.eayun.virtualization.baseservice.BaseFloatIpService;
import com.eayun.virtualization.dao.CloudFloatIpDao;
import com.eayun.virtualization.dao.CloudOrderFloatIpDao;
import com.eayun.virtualization.model.BaseCloudBatchResource;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudOrderFloatIp;
import com.eayun.virtualization.model.CloudBatchResource;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudOrderFloatIp;
import com.eayun.virtualization.service.CloudBatchResourceService;

/**
 * 
 * 公网IpAPI业务<br>
 * -----------------
 * 
 * @author chengxiaodong
 * @date 2016-12-2
 *
 */
@ApiService
@Service
@Transactional
public class FloatIpApiServiceImpl extends BaseFloatIpService implements FloatIpApiService {
	private static final Logger log = LoggerFactory.getLogger(FloatIpApiServiceImpl.class);
	@Autowired
	private CloudFloatIpDao cloudFloatIpDao;
	@Autowired
	private CloudOrderFloatIpDao cloudOrderFloatIpDao;
	@Autowired
	private OpenstackFloatIpService openFloatIpService;
	@Autowired
	private CloudBatchResourceService cloudBatchResourceService;
    @Autowired
    private MessageCenterService messageCenterService;
    @Autowired
    private EayunRabbitTemplate eayunRabbitTemplate;
	/**
	 * 公网IP解绑资源
	 *
	 * @param floatIp
	 * @return
	 * @author chengxiaodong
	 */
	public CloudFloatIp unbundingResource(CloudFloatIp floatIp) {
		boolean flag = false;
		if ("vm".equals(floatIp.getResourceType())) {
			flag = openFloatIpService.removeFloatIp(floatIp.getDcId(), floatIp.getPrjId(), floatIp.getResourceId(),
					floatIp.getFloIp());
		} else if ("lb".equals(floatIp.getResourceType())) {
			flag = openFloatIpService.bindLoadBalancerFloatIp(floatIp.getDcId(), floatIp.getPrjId(), null,
					floatIp.getFloId());
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

	/**
	 * 解除已删除云主机与弹性公网IP的关系
	 */
	@Override
	public void refreshFloatIpByVm(String vmId) {
		StringBuilder hql = new StringBuilder();
		hql.append("from BaseCloudFloatIp where resourceId=? and resourceType=? and isDeleted ='0'");
		List<String> values = new ArrayList<>();
		values.add(vmId);
		values.add("vm");

		BaseCloudFloatIp baseCloudFloatIp = (BaseCloudFloatIp) cloudFloatIpDao.findUnique(hql.toString(),
				values.toArray());
		if (null != baseCloudFloatIp && !StringUtils.isEmpty(baseCloudFloatIp.getFloId())) {
			baseCloudFloatIp.setResourceId(null);
			baseCloudFloatIp.setResourceType(null);
			cloudFloatIpDao.merge(baseCloudFloatIp);
		}
	}

	/**
	 * 创建公网IP的订单<br>
	 * 
	 * @desc 暂时只是购买云主机调用<br>
	 *       -------------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudOrderFloatIp
	 *            公网IP的订单
	 */
	public void createFloatIpOrder(CloudOrderFloatIp cloudOrderFloatIp) {
		BaseCloudOrderFloatIp baseCloudOrderFloatIp = new BaseCloudOrderFloatIp();
		BeanUtils.copyPropertiesByModel(baseCloudOrderFloatIp, cloudOrderFloatIp);

		cloudOrderFloatIpDao.saveOrUpdate(baseCloudOrderFloatIp);
	}

	/**
	 * 根据订单创建弹性公网IP<br>
	 * 
	 * @desc 暂时 只提供给购买云主机购买公网IP使用<br>
	 *       ---------------------------------------
	 * 
	 * @author zhouhaitao
	 * @param orderNo
	 *            订单编号
	 * 
	 * @return 返回创建成功的公网IP列表
	 */
	public List<CloudFloatIp> createFloatIpByOrderno(String orderNo) {
		List<CloudFloatIp> floatIpList = new ArrayList<>();
		StringBuilder hql = new StringBuilder();
		hql.append("from BaseCloudOrderFloatIp where orderNo = ?");
		List<String> values = new ArrayList<>();
		values.add(orderNo);
		BaseCloudOrderFloatIp baseCloudOrderFloatIp = (BaseCloudOrderFloatIp) cloudOrderFloatIpDao
				.findUnique(hql.toString(), values.toArray());
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
				if (i != cloudOrderFloatIp.getProductCount() - 1) {
					floIds.append(",");
				}
			}

			baseCloudOrderFloatIp.setFloId(floIds.toString());

			cloudOrderFloatIpDao.saveOrUpdate(baseCloudOrderFloatIp);// 回写ip订单表
		} catch (Exception e) {
			log.error("创建公网ip失败：" + e.getMessage());
			throw e;
		}
		return floatIpList;

	}

	/**
	 * 申请创建公网IP
	 *
	 * @param cloudFloatIp
	 * @return
	 * @author zhouhaitao
	 */
	public CloudFloatIp createFloatIp(CloudFloatIp cloudFloatIp) throws AppException {
		BaseCloudNetwork network = (BaseCloudNetwork) cloudFloatIpDao.findUnique(
				"from BaseCloudNetwork where dcId =? and routerExternal = ? ",
				new Object[] { cloudFloatIp.getDcId(), "1" });
		FloatIp floatip = openFloatIpService.allocateIp(cloudFloatIp.getDcId(), cloudFloatIp.getPrjId(),
				network.getNetId());
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

	private BaseCloudBatchResource fromBaseCloudBatchResource(CloudFloatIp cloudFloatIp,
			CloudOrderFloatIp cloudOrderFloatIp) {
		BaseCloudBatchResource baseCloudBatchResource = new BaseCloudBatchResource();
		baseCloudBatchResource.setResourceId(cloudFloatIp.getFloId());
		baseCloudBatchResource.setResourceType(CloudBatchResource.RESOURCE_FLOATIP);
		baseCloudBatchResource.setOrderNo(cloudOrderFloatIp.getOrderNo());
		cloudBatchResourceService.save(baseCloudBatchResource);
		return baseCloudBatchResource;
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
		BaseCloudOrderFloatIp baseCloudOrderFloatIp = (BaseCloudOrderFloatIp) cloudOrderFloatIpDao
				.findUnique(hql.toString(), new Object[] { orderNo });
		List<BaseCloudBatchResource> cloudBatResList = cloudBatchResourceService.queryListByOrder(orderNo);
		for (BaseCloudBatchResource baseCloudBatchResource : cloudBatResList) {
			if (CloudBatchResource.RESOURCE_FLOATIP.equals(baseCloudBatchResource.getResourceType())) {
				CloudFloatIp cloudFloatIp = new CloudFloatIp();
				cloudFloatIp.setFloId(baseCloudBatchResource.getResourceId());
				cloudFloatIp.setDcId(baseCloudOrderFloatIp.getDcId());
				cloudFloatIp.setPrjId(baseCloudOrderFloatIp.getPrjId());
				boolean flag = openFloatIpService.deallocateFloatIp(cloudFloatIp.getDcId(), cloudFloatIp.getPrjId(),
						cloudFloatIp.getFloId());
				if (!flag) {
					MessageOrderResourceNotice messageOrderResourceNotice = new MessageOrderResourceNotice();
					messageOrderResourceNotice.setResourceName(cloudFloatIp.getFloIp());
					messageOrderResourceNotice.setResourceId(cloudFloatIp.getFloId());
					messageOrderResourceNotice.setOrderNo(orderNo);
					messageOrderResourceNotice.setResourceType(ResourceType.getName(ResourceType.FLOATIP));
					list.add(messageOrderResourceNotice);
				} else {
					// 删除数据库中的数据，与批量创建表中的数据
					cloudFloatIpDao.delete(baseCloudBatchResource.getResourceId());

					CloudBatchResource cloudBatchResource = new CloudBatchResource();

					cloudBatchResource.setResourceId(baseCloudBatchResource.getResourceId());
					cloudBatchResource.setOrderNo(orderNo);
					cloudBatchResourceService.delete(cloudBatchResource);
				}
			}
		}
		if (list.size() > 0) {
			messageCenterService.delecteResourFailMessage(list, orderNo);
		}
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
    
    private ParamBean setParamBean(int ipCount, int number, String dcId, String payType) {
        ParamBean paramBean = new ParamBean();
        paramBean.setIpCount(ipCount);
        paramBean.setNumber(number);
        paramBean.setDcId(dcId);
        paramBean.setPayType(payType);
        return paramBean;
    }
}
