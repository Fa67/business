package com.eayun.virtualization.service.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.eayunstack.model.VIP;
import com.eayun.eayunstack.service.OpenstackVipService;
import com.eayun.virtualization.dao.CloudLdVipDao;
import com.eayun.virtualization.model.BaseCloudLdVip;
import com.eayun.virtualization.model.CloudLdVip;
import com.eayun.virtualization.service.TagService;
import com.eayun.virtualization.service.VipService;

/**
 * VipServiceImpl
 * 
 * @Filename: VipServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年11月11日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
@Service
@Transactional
public class VipServiceImpl implements VipService {
	private static final Logger log = LoggerFactory
			.getLogger(VipServiceImpl.class);
	@Autowired
	private CloudLdVipDao vipDao;
	@Autowired
	private OpenstackVipService openstackService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private TagService tagService;


	/* 根据id找entity */
	public BaseCloudLdVip getVipById(String vipId) {
		return vipDao.findOne(vipId);
	}

	public boolean updateVip(CloudLdVip cloudLdv) {
		boolean flag = false;
		try {
			BaseCloudLdVip ldv = vipDao.findOne(cloudLdv.getVipId());
			ldv.setVipStatus(cloudLdv.getVipStatus());
			vipDao.saveOrUpdate(ldv);
			flag = true;
		} catch (Exception e) {
		    log.error(e.toString(),e);
			flag = false;
		}
		return flag;
	}


	/**
	 * 添加vip
	 * 
	 * @author zhouhaitao
	 * @param vip
	 * @throws AppException
	 */
	public BaseCloudLdVip addVip(CloudLdVip vip) throws AppException{
		BaseCloudLdVip resultData = null;
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		try {
			
			temp.put("protocol", vip.getVipProtocol());
			temp.put("name", vip.getVipName());
			temp.put("subnet_id", vip.getSubnetId());
			temp.put("protocol_port", vip.getProtocolPort());
			temp.put("pool_id", vip.getPoolId());
			temp.put("connection_limit", vip.getConnectionLimit());
			temp.put("admin_state_up", "1");
			data.put("vip", temp);

			VIP result = openstackService.create(vip.getDcId(), vip.getPrjId(), data);
			if (result != null) {
				resultData = new BaseCloudLdVip();
				resultData.setVipId(result.getId());
				resultData.setVipName(result.getName());
				resultData.setSubnetId(result.getSubnet_id());
				resultData.setPoolId(result.getPool_id());
				resultData.setPrjId(result.getTenant_id());
				resultData.setDcId(vip.getDcId());
				resultData.setCreateName(vip.getCreateName());
				resultData.setCreateTime(new Date());
				resultData.setProtocolPort(Long.parseLong(result
						.getProtocol_port()));
				resultData.setVipProtocol(result.getProtocol());
				resultData.setVipStatus(result.getStatus().toUpperCase());
				resultData.setConnectionLimit(Long.parseLong(result
						.getConnection_limit()));
				resultData.setAdminStateup(result.getAdmin_state_up() ? '1': '0');
				resultData.setVipAddress(result.getAddress());
				resultData.setPortId(result.getPort_id());
				vipDao.saveOrUpdate(resultData);

				if (null != result.getStatus()
						&& !"ACTIVE".equals(result.getStatus())) {
					JSONObject json = new JSONObject();
					json.put("vipId", resultData.getVipId());
					json.put("dcId", resultData.getDcId());
					json.put("prjId", resultData.getPrjId());
					json.put("vipStatus", resultData.getVipStatus());
					json.put("count", "0");
					jedisUtil.push(RedisKey.ldVipKey,json.toJSONString());
				}else if(null != result.getStatus()
						&& "ACTIVE".equals(result.getStatus())){
					jedisUtil.set(RedisKey.CLOUDLDVIPSYNC+resultData.getVipId(), "ACTIVE");
				}
				
			}

		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.toString(),e);
			throw new AppException("error.openstack.message");
		}

		return resultData;

	}

	/**
	 * 修改vip
	 * 
	 * @author zhouhaitao
	 * @param vip
	 * @throws AppException
	 */
	@Transactional(noRollbackFor=AppException.class)
	public BaseCloudLdVip modifyVip(CloudLdVip ldVip) {
		BaseCloudLdVip vip = null;
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("connection_limit", ldVip.getConnectionLimit());
		data.put("vip", temp);

		VIP result = openstackService.update(ldVip.getDcId(), ldVip.getPrjId(), data, ldVip.getVipId());
		if (result != null) {
			vip = new BaseCloudLdVip();
			vip = vipDao.findOne(ldVip.getVipId());
			vip.setVipStatus(result.getStatus());
			vip.setConnectionLimit(ldVip.getConnectionLimit());
			vipDao.saveOrUpdate(vip);

			if (null != result.getStatus()&& !"ACTIVE".equals(result.getStatus())) {
				JSONObject json = new JSONObject();
				json.put("vipId", vip.getVipId());
				json.put("dcId", vip.getDcId());
				json.put("prjId", vip.getPrjId());
				json.put("vipStatus", vip.getVipStatus());
				json.put("count", "0");
				jedisUtil.addUnique(RedisKey.ldVipKey,json.toJSONString());
			}
		}
		return vip;
	}
	
	/**
	 * 删除vip
	 * 
	 * @author zhouhaitao
	 * @param vip
	 * @return
	 */
	public boolean deleteVip(CloudLdVip vip) {
		if (openstackService.delete(vip.getDcId(), vip.getPrjId(), vip.getVipId())) {
			vipDao.delete(vip.getVipId());
			
			tagService.refreshCacheAftDelRes("ldVIP", vip.getVipId());
			return true;
		}
		return false;
	}


}
