package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.virtualization.dao.TagDao;
import com.eayun.virtualization.dao.TagGroupDao;
import com.eayun.virtualization.dao.TagResourceDao;
import com.eayun.virtualization.ecmcservice.EcmcTagService;
import com.eayun.virtualization.model.BaseTag;
import com.eayun.virtualization.model.BaseTagGroup;
import com.eayun.virtualization.model.BaseTagResource;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年5月4日
 */
@Service
@Transactional
public class EcmcTagServiceImpl implements EcmcTagService {
    private static final Logger log = LoggerFactory.getLogger(EcmcTagServiceImpl.class);
	@Autowired
    private JedisUtil jedisUtil;
	@Autowired
    private TagResourceDao  tagResourceDao;
	@Autowired
    private TagGroupDao  tagGroupDao;
	@Autowired
    private TagDao tagDao;

	@Override
	public String synchronizeRedisWithDB() {
		String msg = "";
        try {
            clearDBDataAboutTagResource();
            clearCacheAboutTagBeforeSync();
            
            syncTagGroup();
            syncTag();
            syncTagResource();
            
            msg=ConstantClazz.SUCCESS_CODE;
        } catch (Exception e) {
            log.error(e.toString(),e);
            msg=e.getMessage();
        }
        return msg;
	}
	@SuppressWarnings("rawtypes")
    private void clearDBDataAboutTagResource() {
        String[] resTypeArray = {"vm","volume","diskSnapshot","privateImage",
                                 "securityGroup","network","subNetwork","route",
                                 "firewall","firewallPolicy","firewallRule","ldPool",
                                 "ldMember","ldVIP","ldMonitor","floatIP"};
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<resTypeArray.length; i++){
            String resType = resTypeArray[i];
            String idColumn = getColOfID(resType);
            String tableName = getTableName(resType);
            sb.append("select tgres_id from sys_tagresource where tgres_resourcetype='"+resType+"' and tgres_resourceid not in (")
            .append("select "+idColumn+" from "+tableName+") ")
            .append("Union ");
        }
        String sql = sb.substring(0, sb.length() - 6);
        Query query = tagResourceDao.createSQLNativeQuery(sql);
        List list = query.getResultList();
        for(int i=0;i<list.size();i++){
            String id = list.get(i).toString();
            tagResourceDao.delete(id);
        }
    }
	 /**
     * 获取不同资源类型的ID字段名称
     * @param resType
     * @return
     */
    private String getColOfID(String resType) {
        if (resType.equals("vm")) {
            return "vm_id";
        } else if (resType.equals("volume")) {
            return "vol_id";
        } else if (resType.equals("diskSnapshot")) {
            return "snap_id";
        } else if (resType.equals("privateImage")) {
            return "image_id";
        } else if (resType.equals("securityGroup")) {
            return "sg_id";
        } else if (resType.equals("network")) {
            return "net_id";
        } else if (resType.equals("subNetwork")) {
            return "subnet_id";
        } else if (resType.equals("route")) {
            return "route_id";
        } else if (resType.equals("firewall")) {
            return "fw_id";
        } else if (resType.equals("firewallPolicy")) {
            return "fwp_id";
        } else if (resType.equals("firewallRule")) {
            return "fwr_id";
        } else if (resType.equals("ldPool")) {
            return "pool_id";
        } else if (resType.equals("ldMember")) {
            return "member_id";
        } else if (resType.equals("ldVIP")) {
            return "vip_id";
        } else if (resType.equals("ldMonitor")) {
            return "ldm_id";
        } else if (resType.equals("floatIP")) {
            return "flo_id";
        } else {
            return "";
        }
    }
    /**
     * 获取不同资源类型的表名
     * @param resType
     * @return
     */
    private String getTableName(String resType) {
        //得到了资源类型和资源的表名，查询出我们所要的字段。项目表是cloud_project，其他资源表均有prj_id与之关联
        if (resType.equals("vm")) {
            return "cloud_vm";
        } else if (resType.equals("volume")) {
            return "cloud_volume";
        } else if (resType.equals("diskSnapshot")) {
            return "cloud_disksnapshot";
        } else if (resType.equals("privateImage")) {
            return "cloud_image";
        } else if (resType.equals("securityGroup")) {
            return "cloud_securitygroup";
        } else if (resType.equals("network")) {
            return "cloud_network";
        } else if (resType.equals("subNetwork")) {
            return "cloud_subnetwork";
        } else if (resType.equals("route")) {
            return "cloud_route";
        } else if (resType.equals("firewall")) {
            return "cloud_firewall";
        } else if (resType.equals("firewallPolicy")) {
            return "cloud_fwpolicy";
        } else if (resType.equals("firewallRule")) {
            return "cloud_fwrule";
        } else if (resType.equals("ldPool")) {
            return "cloud_ldpool";
        } else if (resType.equals("ldMember")) {
            return "cloud_ldmember";
        } else if (resType.equals("ldVIP")) {
            return "cloud_ldvip";
        } else if (resType.equals("ldMonitor")) {
            return "cloud_ldmonitor";
        } else if (resType.equals("ldListener")) {
            return "cloud_ldlistener";
        } else if (resType.equals("floatIP")) {
            return "cloud_floatip";
        } else {
            return "";
        }
    }

    private void clearCacheAboutTagBeforeSync() throws Exception{
        //通过KEYS pattern找到所有的相关key，并del掉
        String[] keys = {RedisKey.CUS_TGRP+"*",RedisKey.TAG_GROUP+"*",
                         RedisKey.TGRP_TG+"*",RedisKey.TAG+"*",
                         RedisKey.TAG_RES+"*",RedisKey.RES_TAG+"*"};
        for (String key : keys) {
            Set<String> idSet = jedisUtil.keys(key);
            for(String id : idSet){
                jedisUtil.delete(id);
            }
            idSet.clear();
        }
    }
    private void syncTagGroup() throws Exception {
        Iterator<BaseTagGroup> iterator = tagGroupDao.findAll().iterator();
        List<String> tgrpIdList = new ArrayList<String>();
        while(iterator.hasNext()){
            BaseTagGroup baseTagGroup = iterator.next();
            String tagGrpJSON = JSONObject.toJSONString(baseTagGroup);
            String cusId = baseTagGroup.getCusId();
            String id = baseTagGroup.getId();
            tgrpIdList.add(id);
            
            jedisUtil.addToSet(RedisKey.CUS_TGRP+cusId, id);
            jedisUtil.set(RedisKey.TAG_GROUP +id, tagGrpJSON);
        }
    }
    private void syncTag() throws Exception {
        Iterator<BaseTag> iterator = tagDao.findAll().iterator();
        while(iterator.hasNext()){
            BaseTag baseTag = iterator.next();
            String id = baseTag.getId();
            String tgrpId = baseTag.getGroupId();
            String tagJSON = JSONObject.toJSONString(baseTag);
            
            jedisUtil.addToSet(RedisKey.TGRP_TG +tgrpId, id);
            jedisUtil.set(RedisKey.TAG +id, tagJSON);
        }
    }
    private void syncTagResource() throws Exception {
        Iterator<BaseTagResource> iterator = tagResourceDao.findAll().iterator();
        while(iterator.hasNext()){
            BaseTagResource baseTagRes = iterator.next();
            String tagId = baseTagRes.getTagId();
            String resId = baseTagRes.getResourceId();
            String resType = baseTagRes.getResourceType();
            
            jedisUtil.addToSet(RedisKey.TAG_RES +tagId, resType+":"+resId);
            jedisUtil.addToSet(RedisKey.RES_TAG +resType+":"+resId, tagId);
        }
    }
}
