package com.eayun.monitor.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.monitor.bean.LdPoolIndicator;
import com.eayun.monitor.bean.MonitorAlarmUtil;
import com.eayun.monitor.dao.MonitorAlarmItemDao;
import com.eayun.monitor.model.BaseCloudLdpoolExp;
import com.eayun.monitor.model.CloudLdpoolExp;
import com.eayun.monitor.service.LdPoolAlarmMonitorService;

@Service
@Transactional
public class LdPoolAlarmMonitorServiceImpl implements LdPoolAlarmMonitorService {

	private static final Logger log = LoggerFactory.getLogger(LdPoolAlarmMonitorServiceImpl.class);
    
	@Autowired
    private MonitorAlarmItemDao monitorDao;
	
    @Autowired
    private JedisUtil           jedisUtil;
    
    @Autowired
    private MongoTemplate 		mongoTemplate;
    
    /**
     * 资源监控查询负载均衡列表页
     * @Author: duanbinbin
     * @param page
     * @param queryMap
     * @param projectId
     * @param poolName
     * @param mode
     * @return
     *<li>Date: 2017年3月8日</li>
     */
	@Override
	public Page getLdPoolMonitorList(Page page, QueryMap queryMap,
			String projectId, String poolName, String mode) {
		log.info("查询负载均衡资源监控列表");
		List<Object> list = new ArrayList<Object>();
        StringBuffer hql = new StringBuffer(
            "SELECT ld.pool_id,ld.pool_name,sub.gateway_ip,net.net_name, "
            + " flo.flo_ip,vip.vip_protocol,vip.protocol_port,lm.ldm_name");
        hql.append(" FROM cloud_ldpool ld ");
        hql.append(" LEFT JOIN cloud_subnetwork sub ON ld.subnet_id = sub.subnet_id ");
        hql.append(" LEFT JOIN cloud_network net ON sub.net_id = net.net_id ");
        hql.append(" LEFT JOIN cloud_ldvip vip ON ld.pool_id = vip.pool_id ");
        hql.append(" LEFT JOIN cloud_floatip flo ON ld.pool_id = flo.resource_id AND flo.resource_type = 'lb' ");
        hql.append(" LEFT JOIN (");										//应对目前负载均衡下可能有多个健康检查的情况，网络1.3版本上线前需对历史数据先做初始化，确保负载均衡下只有一个健康检查
        hql.append(" SELECT clm.ldm_id,clm.pool_id FROM cloud_ldpoolldmonitor clm GROUP BY clm.pool_id");
        hql.append(" ) AS clpm ON ld.pool_id = clpm.pool_id ");
        hql.append(" LEFT JOIN cloud_ldmonitor lm ON clpm.ldm_id = lm.ldm_id ");
        hql.append(" WHERE ld.is_visible = '1' AND (ld.pool_status = 'ACTIVE' OR ld.pool_status = 'PENDING_CREATE') ");
        hql.append(" AND ld.prj_id = ? ");
        list.add(projectId);
        hql.append(" AND ld.mode = ? ");
        list.add(mode);
        if (null != poolName && !poolName.trim().equals("")) {
        	poolName = poolName.replaceAll("\\_", "\\\\_");
            hql.append(" AND binary ld.pool_name LIKE ? ");
            list.add("%" + poolName + "%");
        }
        hql.append(" order by ld.create_time desc");
        page = monitorDao.pagedNativeQuery(hql.toString(), queryMap, list.toArray());
        List resultlist = (List) page.getResult();
        if(null != resultlist && !resultlist.isEmpty()){
        	for (int i = 0; i < resultlist.size(); i++) {
                Object[] objs = (Object[]) resultlist.get(i);
                LdPoolIndicator ldPool = new LdPoolIndicator();
                String ldPoolId = String.valueOf(objs[0]);
                ldPool.setLdPoolId(ldPoolId);
                ldPool.setLdPoolName(String.valueOf(objs[1]));
                String vmIp = String.valueOf(objs[2]==null?"--":objs[2]);
                ldPool.setVmIp(vmIp);
                ldPool.setNetName(String.valueOf(objs[3]));
                String floatIp = String.valueOf(objs[4]==null?"--":objs[4]);
                ldPool.setFloatIp(floatIp);
                String protocol = String.valueOf(objs[5]);
                Long port = Long.valueOf(String.valueOf(objs[6]));
                String config = protocol + "："+port;
                ldPool.setConfig(config);
                ldPool.setHealthMonitor(String.valueOf(objs[7]));
                ldPool.setMode(mode);
                
                int member = 0; 
                int expMember = 0;
                Double expMemberRatio = 0.00D;
                Double lastExpMemberRatio = 0.00D;
                int expMemberDiff = 0;
                
                try {
                	String jsonString = jedisUtil.get(RedisKey.MONITOR_EXP_LDPOOL+ldPoolId);
                    JSONObject json = JSONObject.parseObject(jsonString);
                    String lastString = jedisUtil.get(RedisKey.MONITOR_EXP_LDPOOL_LAST+ldPoolId);
                    JSONObject lastJson = JSONObject.parseObject(lastString);
                    
                    if(null != json && !json.isEmpty()){
                		member = json.getIntValue("member");
                		expMember = json.getIntValue("expMember");
                		expMemberRatio = json.getDouble("expMemberRatio");
                    }else{
                    	log.error("LdPool_ERROR：负载均衡查询最新redis指标失败，poolId:"+ldPoolId);
                    }
                    if(null != lastJson && !lastJson.isEmpty()){
                    	lastExpMemberRatio = lastJson.getDouble("expMemberRatio");
                    }else{
                    	log.error("LdPool_ERROR：负载均衡查询次新redis指标失败，poolId:"+ldPoolId);
                    }
                    expMemberRatio = (Double)(Math.round(expMemberRatio*100)/100.0);
                    lastExpMemberRatio = (Double)(Math.round(lastExpMemberRatio*100)/100.0);
                    if(expMemberRatio > lastExpMemberRatio){
                    	expMemberDiff = 1;
                    }else if(expMemberRatio < lastExpMemberRatio){
                    	expMemberDiff = -1;
                    }
                    ldPool.setMember(member);
                    ldPool.setExpMember(expMember);
                    ldPool.setExpMemberRatio(expMemberRatio);
                    ldPool.setExpMemberDiff(expMemberDiff);
                    
                    if(MonitorAlarmUtil.LDPOOL_MODE_MASTER.equals(mode)){	//主备模式
                    	int masterMember = 0;
                    	int expMaster = 0;
                    	Double expMasterRatio = 0.00D;
                    	Double LastExpMasterRatio = 0.00D;
                    	int expMasterDiff = 0;
                    	
                        int slaveMember = 0;
                        int expSalve = 0;
                        Double expSalveRatio = 0.00D;
                        Double lastExpSalveRatio = 0.00D;
                        int expSalveDiff = 0;
                        
                        if(null != json && !json.isEmpty()){
                        	masterMember = json.getIntValue("masterMember");
                        	expMaster = json.getIntValue("expMaster");
                        	expMasterRatio = json.getDouble("expMasterRatio");
                        	
                        	slaveMember = json.getIntValue("slaveMember");
                        	expSalve = json.getIntValue("expSalve");
                        	expSalveRatio = json.getDouble("expSalveRatio");
                        }
                        if(null != lastJson && !lastJson.isEmpty()){
                        	LastExpMasterRatio = lastJson.getDouble("expMasterRatio");
                        	lastExpSalveRatio = lastJson.getDouble("expSalveRatio");
                        }
                        
                        expMasterRatio = (Double)(Math.round(expMasterRatio*100)/100.0);
                        LastExpMasterRatio = (Double)(Math.round(LastExpMasterRatio*100)/100.0);
                        if(expMasterRatio > LastExpMasterRatio){
                        	expMemberDiff = 1;
                        }else if(expMasterRatio < LastExpMasterRatio){
                        	expMasterDiff = -1;
                        }
                        
                        expSalveRatio = (Double)(Math.round(expSalveRatio*100)/100.0);
                        lastExpSalveRatio = (Double)(Math.round(lastExpSalveRatio*100)/100.0);
                        if(expSalveRatio > lastExpSalveRatio){
                        	expSalveDiff = 1;
                        }else if(expSalveRatio < lastExpSalveRatio){
                        	expSalveDiff = -1;
                        }
                        ldPool.setMasterMember(masterMember);
                        ldPool.setExpMaster(expMaster);
                        ldPool.setExpMasterRatio(expMasterRatio);
                        ldPool.setExpMasterDiff(expMasterDiff);
                        
                        ldPool.setSlaveMember(slaveMember);
                        ldPool.setExpSalve(expSalve);
                        ldPool.setExpSalveRatio(expSalveRatio);
                        ldPool.setExpSalveDiff(expSalveDiff);
                    }
                } catch (Exception e) {
                    log.error("获取负载均衡redis指标失败", e);
                    throw new AppException("redis查询异常");
                }
                resultlist.set(i, ldPool);
            }
        }
		return page;
	}

	/**
	 * 查询负载均衡下成员的异常记录列表
	 * @Author: duanbinbin
	 * @param page
	 * @param queryMap
	 * @param cusId
	 * @param endTime
	 * @param cou
	 * @param poolId
	 * @param mode
	 * @param role
	 * @param memberId
	 * @param healthId
	 * @param isisRepair
	 * @return
	 *<li>Date: 2017年3月8日</li>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Page getLdPoolExpList(Page page, QueryMap queryMap, String cusId,
			Date endTime, int cou, String poolId, String mode, String role,
			String memberName, String healthName, String isRepair) {
		log.info("查询负载均衡的成员异常记录");
		List<Object> paramList = new ArrayList<Object>();
        StringBuffer sb = new StringBuffer("");
        sb.append(" SELECT ld.pool_id FROM cloud_ldpool ld ");
        sb.append(" LEFT JOIN cloud_project pro ON ld.prj_id = pro.prj_id ");
        sb.append(" WHERE ld.pool_id = ? AND pro.customer_id = ? ");
        paramList.add(poolId);
        paramList.add(cusId);
        javax.persistence.Query query = monitorDao.createSQLNativeQuery(sb.toString(), paramList.toArray());
        if(null==query || query.getResultList().isEmpty()){
        	return null;
        }
		
        List<String> vmIds = new ArrayList<String>();
		List<String> heaIds = new ArrayList<String>();
		//	根据成员名称（即主机名称）查询主机id
		if(!StringUtil.isEmpty(memberName)){
			StringBuffer msql = new StringBuffer("SELECT vm.vm_id FROM cloud_vm vm WHERE vm.vm_name =? ");
			javax.persistence.Query vmIdsquery = monitorDao.createSQLNativeQuery(msql.toString(),memberName);
			if(null != vmIdsquery && null != vmIdsquery.getResultList() && vmIdsquery.getResultList().size() > 0){
				vmIds = (List<String>)vmIdsquery.getResultList();
			}
		}
		//	根据健康检查名称查询健康检查id
		if(!StringUtil.isEmpty(healthName)){
			StringBuffer hsql = new StringBuffer("SELECT cld.ldm_id FROM cloud_ldmonitor cld WHERE cld.ldm_name = ?");
			javax.persistence.Query heaIdsquery = monitorDao.createSQLNativeQuery(hsql.toString(),healthName);
			if(null != heaIdsquery && null != heaIdsquery.getResultList() && heaIdsquery.getResultList().size() > 0){
				heaIds = (List<String>)heaIdsquery.getResultList();
			}
		}
		
		org.springframework.data.mongodb.core.query.Query queryMongo = 
				this.getExpMongoQuery(endTime, cou, poolId, mode, role, vmIds, heaIds, isRepair);
		Sort sort = new Sort(Direction.DESC, "expTime");
		
		Integer pageSize=queryMap.getCURRENT_ROWS_SIZE();
		Integer pageNum=queryMap.getPageNum();
		queryMongo.skip(pageSize*pageNum-pageSize);			// skip相当于从那条记录开始
		queryMongo.limit(queryMap.getCURRENT_ROWS_SIZE());	// 从skip开始,取多少条记录
        long totalSize=mongoTemplate.count(queryMongo, MongoCollectionName.MONITOR_LD_POOL_MEMBER_EXP);
        int startIndex = Page.getStartOfPage(pageNum, pageSize);
        
        page=new Page(startIndex, totalSize, pageSize, mongoTemplate.find(queryMongo.with(sort), 
        		BaseCloudLdpoolExp.class,MongoCollectionName.MONITOR_LD_POOL_MEMBER_EXP));
        
        List<BaseCloudLdpoolExp> baseList = (List<BaseCloudLdpoolExp>) page.getResult();
        List<CloudLdpoolExp> list=new ArrayList<CloudLdpoolExp>();
		if(!baseList.isEmpty() && baseList.size() > 0){
			for(BaseCloudLdpoolExp baseExp:baseList){
				CloudLdpoolExp ldexp = new CloudLdpoolExp();
				BeanUtils.copyPropertiesByModel(ldexp, baseExp);
				
				String vmId = baseExp.getVmId();
				if(!StringUtil.isEmpty(vmId)){
					String memName = this.getVmNameById(vmId);
					ldexp.setMemberName(memName);
				}else{
					log.error("该成员异常记录主机ID为空，MemberId:"+baseExp.getMemberId());
				}
				String healthId = baseExp.getHealthId();
				if(!StringUtil.isEmpty(healthId)){
					String heaName = this.getHealthNameById(healthId);
					ldexp.setHealthName(heaName);
				}else{
					log.error("该成员异常记录健康检查ID为空，MemberId:"+baseExp.getMemberId());
				}
				String rolee = baseExp.getRole();
				if(MonitorAlarmUtil.LDPOOL_MODE_MASTER.equals(mode) && 
        				MonitorAlarmUtil.MEMBER_ROLE_ACTIVE.equals(rolee)){
        			ldexp.setRoleName("主节点");
        		}else if(MonitorAlarmUtil.LDPOOL_MODE_MASTER.equals(mode) && 
        				MonitorAlarmUtil.MEMBER_ROLE_BACKUP.equals(rolee)){
        			ldexp.setRoleName("从节点");
        		}
				list.add(ldexp);
			}
		}
		page.setResult(list);
		return page;
	}
	/**
	 * 根据云主机id查询名称（即成员名称）
	 * @Author: duanbinbin
	 * @param vmId
	 * @return
	 *<li>Date: 2017年3月21日</li>
	 */
	private String getVmNameById(String vmId){
		StringBuffer msql = new StringBuffer("SELECT vm.vm_name FROM cloud_vm vm WHERE vm.vm_id = ? ");
		javax.persistence.Query query = monitorDao.createSQLNativeQuery(msql.toString(),vmId);
		if(null != query && null != query.getResultList() && query.getResultList().size() > 0){
			return (String) query.getResultList().get(0);
		}
		return "";
	}
	/**
	 * 根据健康检查id查询名称
	 * @Author: duanbinbin
	 * @param healthId
	 * @return
	 *<li>Date: 2017年3月21日</li>
	 */
	private String getHealthNameById(String healthId){
		StringBuffer hsql = new StringBuffer("SELECT cld.ldm_name FROM cloud_ldmonitor cld WHERE cld.ldm_id = ?");
		javax.persistence.Query query = monitorDao.createSQLNativeQuery(hsql.toString(),healthId);
		if(null != query && null != query.getResultList() && query.getResultList().size() > 0){
			return (String) query.getResultList().get(0);
		}
		return "";
	}
	
	/**
	 * 添加成员异常信息记录
	 * @Author: duanbinbin
	 * @param cloudLdpoolExp
	 * @param isExp
	 *<li>Date: 2017年3月21日</li>
	 */
	@Override
	public void addCloudLdpoolExp(BaseCloudLdpoolExp cloudLdpoolExp,Boolean isExp) {
		Query query=new Query();
		Sort sort = new Sort(Direction.DESC, "expTime");
		query.addCriteria(Criteria.where("memberId").is(cloudLdpoolExp.getMemberId()));
		query.addCriteria(Criteria.where("isRepair").is("1"));
		mongoTemplate.updateFirst(query.with(sort), 									//查询出最新的一条并修改状态
        		 new Update().set("isRepair", "0"), 
        		 MongoCollectionName.MONITOR_LD_POOL_MEMBER_EXP);
		if(isExp){
			mongoTemplate.insert(cloudLdpoolExp, MongoCollectionName.MONITOR_LD_POOL_MEMBER_EXP);
		}
		
	}
	/**
	 * 监控详情
	 * @Author: duanbinbin
	 * @param ldPoolId
	 * @return
	 *<li>Date: 2017年3月21日</li>
	 */
	@Override
	public LdPoolIndicator getLdPoolDetailById(String ldPoolId) {
		List<Object> list = new ArrayList<Object>();
        StringBuffer hql = new StringBuffer(
            "SELECT ld.pool_id,ld.pool_name,sub.gateway_ip,net.net_id,net.net_name,ld.mode ");
        hql.append(" FROM cloud_ldpool ld ");
        hql.append(" LEFT JOIN cloud_subnetwork sub ON ld.subnet_id = sub.subnet_id ");
        hql.append(" LEFT JOIN cloud_network net ON sub.net_id = net.net_id ");
        hql.append(" WHERE ld.is_visible = '1' AND (ld.pool_status = 'ACTIVE' OR ld.pool_status = 'PENDING_CREATE') ");
        hql.append(" AND ld.pool_id = ? ");
        list.add(ldPoolId);
        javax.persistence.Query query = monitorDao.createSQLNativeQuery(hql.toString(), list.toArray());
        LdPoolIndicator ldPool = new LdPoolIndicator();
        List resultlist = new ArrayList();
        if(null!=query ){
        	resultlist = query.getResultList();
        }
        if(null != resultlist&& resultlist.size() == 1){
            Object[] objs = (Object[]) resultlist.get(0);
            ldPool.setLdPoolId(String.valueOf(objs[0]));
            ldPool.setLdPoolName(String.valueOf(objs[1]));
            String vmIp = String.valueOf(objs[2]==null?"--":objs[2]);
            ldPool.setVmIp(vmIp);
            ldPool.setNetId(String.valueOf(objs[3]));
            ldPool.setNetName(String.valueOf(objs[4]));
            ldPool.setMode(String.valueOf(objs[5]));
        }
        return ldPool;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<CloudLdpoolExp>> getNameListById(Date endTime,
			int cou, String poolId, String mode, String role, String memberName,
			String healthName, String isRepair) {
		Map<String, List<CloudLdpoolExp>> result = new HashMap<String, List<CloudLdpoolExp>>();
		List<CloudLdpoolExp> memList = new 	ArrayList<CloudLdpoolExp>();
		List<CloudLdpoolExp> heaList = new 	ArrayList<CloudLdpoolExp>();
		
		List<String> vmIds = new ArrayList<String>();
		List<String> heaIds = new ArrayList<String>();
		org.springframework.data.mongodb.core.query.Query queryMemName = 
				this.getExpMongoQuery(endTime, cou, poolId, mode, role, vmIds, heaIds, isRepair);
		Sort sort = new Sort(Direction.DESC, "expTime");
		List<BaseCloudLdpoolExp> expList = mongoTemplate.find(queryMemName.with(sort), 
				BaseCloudLdpoolExp.class,MongoCollectionName.MONITOR_LD_POOL_MEMBER_EXP);
		
		if(!StringUtil.isEmpty(memberName)){
			StringBuffer msql = new StringBuffer("SELECT vm.vm_id FROM cloud_vm vm WHERE vm.vm_name =? ");
			javax.persistence.Query vmIdsquery = monitorDao.createSQLNativeQuery(msql.toString(),memberName);
			if(null != vmIdsquery && null != vmIdsquery.getResultList() && vmIdsquery.getResultList().size() > 0){
				vmIds = (List<String>)vmIdsquery.getResultList();
				org.springframework.data.mongodb.core.query.Query queryHeaName = 
						this.getExpMongoQuery(endTime, cou, poolId, mode, role, vmIds, heaIds, isRepair);
				List<BaseCloudLdpoolExp> HeaExpList = mongoTemplate.find(queryHeaName.with(sort), 
						BaseCloudLdpoolExp.class,MongoCollectionName.MONITOR_LD_POOL_MEMBER_EXP);
				
				List<String> hIds = new ArrayList<String>();
				if(!HeaExpList.isEmpty() && HeaExpList.size() > 0){
					for(BaseCloudLdpoolExp baseExp : HeaExpList){
						String heaId = baseExp.getHealthId();
						if(!hIds.contains(heaId) && !StringUtil.isEmpty(heaId)){
							hIds.add(heaId);
						}
					}
					if(!hIds.isEmpty() && hIds.size() > 0){
						StringBuffer hsql = new StringBuffer("SELECT cld.ldm_id, cld.ldm_name FROM cloud_ldmonitor cld ");
						hsql.append(" WHERE cld.ldm_id in(:hIds) ");
						hsql.append(" GROUP BY cld.ldm_name ORDER BY cld.ldm_name ");
						List<Object[]> heasList = monitorDao.createSQLNativeQuery(hsql.toString())
								.unwrap(org.hibernate.Query.class).setParameterList("hIds", hIds).list();
						
						if(!heasList.isEmpty() && heasList.size() > 0){
							for(int i = 0;i < heasList.size();i++){
								CloudLdpoolExp exp = new CloudLdpoolExp();
								Object[] objs = (Object[])heasList.get(i);
								String heaId = String.valueOf(objs[0]);
								String heaName = String.valueOf(objs[1]);
								exp.setHealthId(heaId);
								exp.setHealthName(heaName);
								heaList.add(exp);
							}
						}
					}
					
				}
			}
		}
		List<String> vIds = new ArrayList<String>();
		if(!expList.isEmpty() && expList.size() > 0){
			for(BaseCloudLdpoolExp baseExp : expList){
				String vmId = baseExp.getVmId();
				if(!vIds.contains(vmId) && !StringUtil.isEmpty(vmId)){
					vIds.add(vmId);
				}
			}
			if(!vIds.isEmpty() && vIds.size() > 0){
				StringBuffer msql = new StringBuffer("SELECT vm.vm_id, vm.vm_name FROM cloud_vm vm ");
				msql.append(" WHERE vm.vm_id in(:vIds) ");
				msql.append(" GROUP BY vm.vm_name ORDER BY vm.vm_name ");
				List<Object[]> memsList = monitorDao.createSQLNativeQuery(msql.toString())
						.unwrap(org.hibernate.Query.class).setParameterList("vIds", vIds).list();
				
				if(!memsList.isEmpty() && memsList.size() > 0){
					for(int i = 0;i < memsList.size();i++){
						CloudLdpoolExp exp = new CloudLdpoolExp();
						Object[] objs = (Object[])memsList.get(i);
						String vmId = String.valueOf(objs[0]);
						String memName = String.valueOf(objs[1]);
						exp.setVmId(vmId);
						exp.setMemberName(memName);
						memList.add(exp);
					}
				}
			}
		}
		result.put("memList", memList);
		result.put("heaList", heaList);
		return result;
	}
	
	private org.springframework.data.mongodb.core.query.Query getExpMongoQuery(Date endTime, int cou, String poolId, String mode,
			String role, List<String> vmIds, List<String> heaIds, String isRepair) {
		Date startTime = DateUtil.addDay(endTime,new int[]{0,0,0,0,-30});
		switch(cou){
        case 3:
        	startTime = DateUtil.addDay(endTime,new int[]{0,0,0,0,-30});
            break;
        case 5:
        	startTime = DateUtil.addDay(endTime,new int[]{0,0,0,-1});
            break;
        case 30:
        	startTime = DateUtil.addDay(endTime,new int[]{0,0,0,-6});
            break;
        case 60:
        	startTime = DateUtil.addDay(endTime,new int[]{0,0,0,-12});
            break;
        case 120:
        	startTime = DateUtil.addDay(endTime,new int[]{0,0,-1});
            break;
        case 720:
        	startTime = DateUtil.addDay(endTime,new int[]{0,0,-7});
            break;
		}
		org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query();
		query.addCriteria(Criteria.where("expTime").gt(startTime).andOperator(Criteria.where("expTime").lt(endTime)));
		query.addCriteria(Criteria.where("mode").is(mode));
		query.addCriteria(Criteria.where("poolId").is(poolId));
		if(!StringUtil.isEmpty(role) && MonitorAlarmUtil.LDPOOL_MODE_MASTER.equals(mode)){
			query.addCriteria(Criteria.where("role").is(role));
        }
		if(!StringUtil.isEmpty(isRepair)){
			query.addCriteria(Criteria.where("isRepair").is(isRepair));
        }
		if(!vmIds.isEmpty() && vmIds.size() > 0){
			query.addCriteria(Criteria.where("vmId").in(vmIds));
        }
		if(!heaIds.isEmpty() && heaIds.size() > 0){
			query.addCriteria(Criteria.where("healthId").in(heaIds));
        }
		return query;
	}
}
