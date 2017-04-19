package com.eayun.obs.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.obs.service.ObsGetAllUsersService;

@Service
@Transactional
public class ObsGetAllUsersServiceImpl implements ObsGetAllUsersService {
	private static final Logger log = LoggerFactory.getLogger(ObsGetAllUsersServiceImpl.class);
	@Autowired
	private JedisUtil jedisUtil;

	public List<String> getObsAllUsers() throws Exception {
		Set<String> keys=jedisUtil.keys(RedisKey.OBSUSER_CUSID+"*");
		List<String> obsUsers=new ArrayList<String>();
		for (String key : keys) {
			String obsUser=jedisUtil.get(key);
			JSONObject json=JSONObject.parseObject(obsUser);
			String cusId=json.getString("cusId");
			obsUsers.add(cusId);
			log.info("当前获取的obs用户id为:"+cusId);
		}
		return obsUsers;
	}

}
