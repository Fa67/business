/**
 * Eayun Software Inc.
 * Copyright (c) 2015 All Rights Reserved.
 */
package com.eayun.virtualization.ecmcservice.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.eayunstack.model.Pool;
import com.eayun.eayunstack.model.VIP;
import com.eayun.eayunstack.service.OpenstackPoolService;
import com.eayun.eayunstack.service.OpenstackVipService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.virtualization.dao.CloudLdMemberDao;
import com.eayun.virtualization.dao.CloudLdPoolDao;
import com.eayun.virtualization.dao.CloudLdVipDao;
import com.eayun.virtualization.ecmcservice.EcmcLBVipService;
import com.eayun.virtualization.model.BaseCloudLdPool;
import com.eayun.virtualization.model.BaseCloudLdVip;
import com.eayun.virtualization.model.CloudLdVip;
import com.eayun.virtualization.service.TagService;

/**
 *                       
 * @Filename: EcmcLBVipServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: fangjun.yang
 * @Email: fangjun.yang@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月8日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class EcmcLBVipServiceImpl implements EcmcLBVipService {

    private final static Logger  log = LoggerFactory.getLogger(EcmcLBVipServiceImpl.class);

    @Autowired
    private CloudLdVipDao        cloudLdVipDao;

    @Autowired
    private CloudLdPoolDao       cloudLdPoolDao;
    
    @Autowired
    private CloudLdMemberDao 	cloudLdMemberDao;

    @Autowired
    private OpenstackVipService  openstackVipService;

    @Autowired
    private OpenstackPoolService openstackPoolService;

    @Autowired
    private TagService           tagService;

    @Autowired
    private JedisUtil            jedisUtil;

    public boolean checkVipName(String datacenterId, String projectId, String vipName, String vipId) throws AppException {
        return cloudLdVipDao.countVipNameRepeat(datacenterId, projectId, vipName, vipId) > 0 ? true : false;
    }

    public Page querySubnetList(String datacenterId, String projectId, Integer pageNo, Integer pageSize) throws AppException {
        try {
            org.springframework.data.domain.Page<Map<String, Object>> queryPage = cloudLdVipDao.findSubNetwork(datacenterId, projectId, new PageRequest(pageNo - 1, pageSize, new Sort(new Order(Direction.DESC, "createTime"))));
            return new Page(Page.getStartOfPage(pageNo, pageSize), queryPage.getTotalElements(), pageSize, queryPage.getContent());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            //当执行出现未知异常时，打印异常信息，并返回空
            return null;
        }
    }

    public Page findVipList(ParamsMap paramsMap) throws AppException {
        List<Object> paramValues = new ArrayList<>(3);
        Map<String, Object> params = paramsMap.getParams();
        StringBuffer sql = new StringBuffer();
        sql.append("select cv.vip_name as vipName,cv.vip_address as vipAddress,cv.vip_protocol as vipProtocol,cv.protocol_port as protocolPort,");
        sql.append(" cs.subnet_name as subnetName, cs.gateway_ip as gatewayIp,cpool.pool_name as poolName,cv.connection_limit as connectionLimit,");
        sql.append(" cv.vip_status as vipStatus,cv.create_time as createTime, cp.prj_name as prjName,cv.admin_stateup as adminStateup,cv.prj_id as prjId,cv.vip_id as vipId,");
        sql.append(" dc.dc_name as dcName, ss.cus_id as cusId, ss.cus_org as cusOrg, cv.dc_id as dcId, cv.pool_id as poolId");
        sql.append(" from cloud_ldvip cv");
        sql.append(" left join cloud_project cp on cv.prj_id=cp.prj_id");
        sql.append(" left join sys_selfcustomer ss on cp.customer_id=ss.cus_id");
        sql.append(" left join cloud_subnetwork cs on cs.subnet_id=cv.subnet_id");
        sql.append(" left join cloud_ldpool cpool on cpool.pool_id = cv.pool_id");
        sql.append(" left join dc_datacenter dc on cv.dc_id = dc.id");
        sql.append(" where 1=1");
        int idx = 0;
        
        if(params != null && params.containsKey("dcId") && StringUtils.isNotBlank((String)params.get("dcId"))){
            sql.append(" and cv.dc_id= ?").append(++idx);
            paramValues.add(params.get("dcId"));
        }
        if(params != null && params.containsKey("prjName") && StringUtils.isNotBlank((String)params.get("prjName"))){
            sql.append(" and cp.prj_name in(?").append(++idx).append(")");
            paramValues.add(params.get("prjName"));
        }
        if(params != null && params.containsKey("cusOrg")  && StringUtils.isNotBlank((String)params.get("cusOrg"))){
            sql.append(" and ss.cus_org in(?").append(++idx).append(")");
            paramValues.add(params.get("cusOrg"));
        }
        if(params != null && params.containsKey("vipName") && StringUtils.isNotBlank((String)params.get("vipName"))){
            sql.append(" and cv.vip_name like ?").append(++idx);
            paramValues.add("%" + (String)params.get("vipName") + "%");
        }
        sql.append(" order by cv.create_time desc ");
        
        QueryMap queryMap = new QueryMap();
        queryMap.setPageNum(paramsMap.getPageNumber());
        queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
		Page page = cloudLdVipDao.pagedNativeQuery(sql.toString(), queryMap , paramValues.toArray());
		@SuppressWarnings("unchecked")
		List<Object> dataList = (List<Object>)page.getResult();
		for (int i = 0; i < dataList.size(); i++) {
			Object[] objs = (Object[])dataList.get(i);
			CloudLdVip vip = new CloudLdVip();
			vip.setVipName(ObjectUtils.toString(objs[0]));
			vip.setVipAddress(ObjectUtils.toString(objs[1]));
			vip.setVipProtocol(ObjectUtils.toString(objs[2]));
			vip.setProtocolPort(((BigDecimal)objs[3]).longValue());
			vip.setSubnetName(ObjectUtils.toString(objs[4]));
			vip.setGatewayIp(ObjectUtils.toString(objs[5]));
			vip.setPoolName(ObjectUtils.toString(objs[6]));
			vip.setConnectionLimit(((BigDecimal)objs[7]).longValue());
			vip.setVipStatus(ObjectUtils.toString(objs[8]));
			vip.setCreateTime(DateUtil.stringToDate(objs[9] == null ? "" : ObjectUtils.toString(objs[9])));
			vip.setPrjName(ObjectUtils.toString(objs[10]));
			vip.setAdminStateup(Character.valueOf((char)objs[11]));
			vip.setPrjId(ObjectUtils.toString(objs[12]));
			vip.setVipId(ObjectUtils.toString(objs[13]));
			vip.setDcName(ObjectUtils.toString(objs[14]));
			vip.setCusId(ObjectUtils.toString(objs[15]));
			vip.setCusOrg(ObjectUtils.toString(objs[16]));
			vip.setDcId(ObjectUtils.toString(objs[17]));
			vip.setPoolId(ObjectUtils.toString(objs[18]));
			dataList.set(i, vip);
		}
        return page;
    }

    public BaseCloudLdVip createVip(Map<String, String> params) throws AppException {
        BaseCloudLdVip resultData = null;
        try {
            JSONObject data = new JSONObject();
            JSONObject temp = new JSONObject();
            /*创建VIP的时候根据选择的资源池给VIP设置属性“protocol”的值；  开始*/
            String pool_id = params.get("pool_id");
            if (pool_id != null && !pool_id.equals("")) {
                Pool pool = null;
                pool = openstackPoolService.getById(params.get("datacenter"), params.get("project"), pool_id);
                temp.put("protocol", pool.getProtocol());
            }
            /*创建VIP的时候根据选择的资源池给VIP设置属性“protocol”的值；  结束*/

            temp.put("name", params.get("name"));
            temp.put("tenant_id", params.get("tenant_id"));
            temp.put("description", params.get("description"));
            temp.put("subnet_id", params.get("subnet_id"));
            temp.put("address", params.get("address"));
            //temp.put("protocol", params.get("protocol"));
            temp.put("protocol_port", params.get("protocol_port"));
            temp.put("pool_id", params.get("pool_id"));
            temp.put("session_persistence", params.get("session_persistence"));
            temp.put("connection_limit", params.get("connection_limit"));
            temp.put("admin_state_up", params.get("admin_state_up"));
            data.put("vip", temp);

            //1、判断vip名称是否重复
            boolean isExist = this.checkVipName(params.get("datacenter"), null, params.get("name"), null);
            if (isExist == true) {
                return null;
            }

            VIP result = openstackVipService.create(params.get("datacenter"), params.get("project"), data);
            if (result != null) {
                resultData = new BaseCloudLdVip();
                resultData.setCreateTime(new Date());

                resultData.setVipId(result.getId());
                resultData.setVipName(result.getName());
                resultData.setSubnetId(result.getSubnet_id());
                resultData.setPoolId(result.getPool_id());
                resultData.setPrjId(result.getTenant_id());
                resultData.setDcId(params.get("datacenter"));
                resultData.setCreateName(EcmcSessionUtil.getUser().getAccount());
                resultData.setProtocolPort(Long.parseLong(result.getProtocol_port()));
                resultData.setVipProtocol(result.getProtocol());
                resultData.setVipStatus(result.getStatus().toUpperCase());
                resultData.setConnectionLimit(Long.parseLong(result.getConnection_limit()));
                if (result.getAdmin_state_up()) {
                    resultData.setAdminStateup('1');
                } else {
                    resultData.setAdminStateup('0');
                }
                resultData.setVipAddress(result.getAddress());
                cloudLdVipDao.save(resultData);
                //同步Vip状态
                if (null != result.getStatus() && !"ACTIVE".equals(result.getStatus())) {
                    JSONObject json = new JSONObject();
                    json.put("vipId", resultData.getVipId());
                    json.put("dcId", resultData.getDcId());
                    json.put("prjId", resultData.getPrjId());
                    json.put("vipStatus", resultData.getVipStatus());
                    json.put("count", "0");
                    jedisUtil.push(RedisKey.ldVipKey, json.toJSONString());
                }

                //将VIPid 存入所选的资源池表中  update
                BaseCloudLdPool pool = cloudLdPoolDao.findOne(result.getPool_id());
                pool.setVipId(result.getId());
                cloudLdPoolDao.saveOrUpdate(pool);
            }
            return resultData;
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public boolean deleteVip(String datacenterId, String projectId, String id) throws AppException {
        try {
            //执行openstack删除操作成功后，进行后续操作
            if (openstackVipService.delete(datacenterId, projectId, id)) {
                //当删除数据库操作失败时，线程等待1秒后尝试重新删除，最多重试10次
                for (int i = 0; i < 10; i++) {
                    try {
                        cloudLdVipDao.delete(id);
                        // 删除资源后更新缓存接口
                        tagService.refreshCacheAftDelRes("ldVIP", id);
                        break;
                    } catch (Exception e) {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e2) {
                            log.error(e2.getMessage(), e2);
                        }
                    }
                }
            }
            return true;
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public BaseCloudLdVip updateVip(Map<String, String> params) throws AppException {
        JSONObject data = new JSONObject();
        JSONObject temp = new JSONObject();
        temp.put("name", params.get("name"));
        temp.put("connection_limit", params.get("connection_limit"));
        temp.put("admin_state_up", params.get("admin_state_up"));
        data.put("vip", temp);

        //1、判断vip名称是否重复
        if (this.checkVipName(params.get("datacenterId"), null, params.get("name"), params.get("id"))) {
            return null;
        }

        try {
            VIP result = openstackVipService.update(params.get("datacenterId"), params.get("projectId"), data, params.get("id"));
            if (result != null) {
                BaseCloudLdVip vip = new BaseCloudLdVip();
                vip = cloudLdVipDao.findOne(params.get("id"));
                vip.setVipStatus(result.getStatus());
                vip.setAdminStateup(Character.valueOf(params.get("admin_state_up").toCharArray()[0]));
                vip.setConnectionLimit(Long.parseLong(params.get("connection_limit")));
                vip.setVipName(params.get("name"));
                cloudLdVipDao.saveOrUpdate(vip);
                //同步Vip状态
                if (null != result.getStatus() && !"ACTIVE".equals(result.getStatus())) {
                    JSONObject json = new JSONObject();
                    json.put("vipId", vip.getVipId());
                    json.put("dcId", vip.getDcId());
                    json.put("prjId", vip.getPrjId());
                    json.put("vipStatus", vip.getVipStatus());
                    json.put("count", "0");
                    jedisUtil.addUnique(RedisKey.ldVipKey, json.toJSONString());
                }
                return vip;
            } else {
                return null;
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    public boolean existMember(String poolId) throws AppException {
    	return cloudLdMemberDao.countByPoolId(poolId) > 0 ? true : false;
    }

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

            VIP result = openstackVipService.create(vip.getDcId(), vip.getPrjId(), data);
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
                cloudLdVipDao.saveOrUpdate(resultData);

                if (null != result.getStatus()
                        && !"ACTIVE".equals(result.getStatus())) {
                    JSONObject json = new JSONObject();
                    json.put("vipId", resultData.getVipId());
                    json.put("dcId", resultData.getDcId());
                    json.put("prjId", resultData.getPrjId());
                    json.put("vipStatus", resultData.getVipStatus());
                    json.put("count", "0");
                    jedisUtil.push(RedisKey.ldVipKey,json.toJSONString());
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

    @Override
    public BaseCloudLdVip modifyVip(CloudLdVip ldVip) throws AppException {
        BaseCloudLdVip vip = null;
        JSONObject data = new JSONObject();
        JSONObject temp = new JSONObject();
        temp.put("connection_limit", ldVip.getConnectionLimit());
        data.put("vip", temp);

        VIP result = openstackVipService.update(ldVip.getDcId(), ldVip.getPrjId(), data, ldVip.getVipId());
        if (result != null) {
            vip = new BaseCloudLdVip();
            vip = cloudLdVipDao.findOne(ldVip.getVipId());
            vip.setVipStatus(result.getStatus());
            vip.setConnectionLimit(ldVip.getConnectionLimit());
            cloudLdVipDao.saveOrUpdate(vip);

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

    @Override
    public boolean deleteVip(CloudLdVip vip) throws AppException {
        if (openstackVipService.delete(vip.getDcId(), vip.getPrjId(), vip.getVipId())) {
            cloudLdVipDao.delete(vip.getVipId());

            tagService.refreshCacheAftDelRes("ldVIP", vip.getVipId());
            return true;
        }
        return false;
    }
}
