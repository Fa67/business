package com.eayun.virtualization.apiservice.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.annotation.ApiService;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.eayunstack.service.OpenstackVolumeService;
import com.eayun.virtualization.apiservice.VolumeApiService;
import com.eayun.virtualization.baseservice.BaseVolumeService;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.service.TagService;

/**
 * 
 * 云硬盘API业务<br>
 * -----------------
 * @author chengxiaodong
 * @date 2016-12-2
 *
 */
@ApiService
@Service
@Transactional
public class VolumeApiServiceImpl extends BaseVolumeService implements VolumeApiService{
	
	@Autowired
	private CloudVolumeDao volumeDao;
	@Autowired
	private OpenstackVolumeService openStackVolumeService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private TagService tagService;
	
	

	
	
	



	
}
