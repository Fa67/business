package com.eayun.monitor.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.eayun.monitor.dao.EcmcMonitorAlarmItemDao;
import com.eayun.monitor.ecmcservice.EcmcLdPoolMonitorService;
import com.eayun.monitor.model.BaseCloudLdpoolExp;
import com.eayun.monitor.model.CloudLdpoolExp;

@Service
@Transactional
public class EcmcLdPoolMonitorServiceImpl implements EcmcLdPoolMonitorService {

	private static final Logger   log = LoggerFactory.getLogger(EcmcLdPoolMonitorServiceImpl.class);

	@Autowired
	private EcmcMonitorAlarmItemDao ecmcMonitorAlarmItemDao;
	
	@Autowired
    private JedisUtil jedisUtil;
	
	@Autowired
    private MongoTemplate 		mongoTemplate;
	
	/**
	 * 查询运维负载均衡资源监控列表数据
	 * 负载均衡只有实时数据，无历史数据
	 * @Author: duanbinbin
	 * @param page
	 * @param queryMap
	 * @param queryType
	 * @param queryName
	 * @param orderBy
	 * @param sort
	 * @param projectId
	 * @param mode
	 * @return
	 *<li>Date: 2017年3月10日</li>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Page getEcmcLdPoolMonitorList(Page page, QueryMap queryMap,
			String queryType, String queryName, String orderBy, String sort,
			String dcName, String mode) {
		log.info("查询负载均衡资源监控实时指标数据，并排序");
		Map<String, Object> params = this.getLdPoolListForMonitor(queryType , queryName , dcName ,mode);
		List<Object> paramsList = (List<Object>) params.get("paramsList");
		javax.persistence.Query query = ecmcMonitorAlarmItemDao.createSQLNativeQuery(params.get("sql").toString(), paramsList.toArray());
		List resultlist = (List) query.getResultList();
		
		if(null != resultlist && !resultlist.isEmpty()){
        	for (int i = 0; i < resultlist.size(); i++) {
                Object[] objs = (Object[]) resultlist.get(i);
                LdPoolIndicator ldPool = new LdPoolIndicator();
                String ldPoolId = String.valueOf(objs[0]);
                ldPool.setLdPoolId(ldPoolId);
                ldPool.setLdPoolName(String.valueOf(objs[1]));
                ldPool.setMode(mode);
                ldPool.setNetName(String.valueOf(objs[2]));
                ldPool.setVmIp(String.valueOf(objs[3]));
                ldPool.setDcName(String.valueOf(objs[4]));
                ldPool.setPrjName(String.valueOf(objs[5]));
                ldPool.setCusName(String.valueOf(objs[6]));
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
                    log.error("获取运维负载均衡redis指标失败", e);
                    throw new AppException("redis查询异常");
                }
                resultlist.set(i, ldPool);
            }
        }
		if((null != orderBy && !("".equals(orderBy))) && (null != sort && !("".equals(sort)))){
        	final String order = orderBy;
            final String asc = sort;
            Collections.sort(resultlist,new Comparator<LdPoolIndicator>(){
                public int compare(LdPoolIndicator arg0, LdPoolIndicator arg1) {
                	double value0 = 0.0;
                	double value1 = 0.0;
                	switch(order){
                    case "member":
                    	value0 = arg0.getExpMemberRatio();
                    	value1 = arg1.getExpMemberRatio();
                        break;
                    case "master":
                    	value0 = arg0.getExpMasterRatio();
                    	value1 = arg1.getExpMasterRatio();
                        break;
                    case "salve":
                    	value0 = arg0.getExpSalveRatio();
                    	value1 = arg1.getExpSalveRatio();
                        break;
                    }
                	int result = 0;
                	switch(asc){
                	case "ASC":
                		result = new Double(value0).compareTo(new Double(value1));
                		break;
                    case "DESC":
                    	result = new Double(value1).compareTo(new Double(value0));
                    	break;
                	}
                    return result;
                }
            });
        }
        int pageSize = queryMap.getCURRENT_ROWS_SIZE();
        int pageNumber = queryMap.getPageNum();
        List<LdPoolIndicator> list = new ArrayList<LdPoolIndicator>();
        int start = (pageNumber-1)*pageSize;
        if(resultlist.size()>0){
            int end = start+pageSize;
            list = resultlist.subList(start, end < resultlist.size()?end:resultlist.size());
        }
        page = new Page(start, resultlist.size(), pageSize, list);
		return page;
	}

	private Map<String, Object> getLdPoolListForMonitor(String queryType,
			String queryName, String dcName, String mode) {
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql =new StringBuffer("SELECT ld.pool_id,ld.pool_name,net.net_name,sub.gateway_ip,");
        sql.append(" dc.dc_name,prj.prj_name,cus.cus_org ")
           .append(" FROM cloud_ldpool ld")
           .append(" LEFT JOIN cloud_subnetwork sub ON ld.subnet_id = sub.subnet_id")
           .append(" LEFT JOIN cloud_network net ON sub.net_id = net.net_id")
           .append(" LEFT JOIN dc_datacenter dc ON ld.dc_id = dc.id")
           .append(" LEFT JOIN cloud_project prj ON ld.prj_id = prj.prj_id ")
           .append(" LEFT JOIN sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
        sql.append(" WHERE ld.is_visible = '1' ");
		if (null != queryName && !queryName.trim().equals("")) {
			if(queryType.equals("obj")){
				queryName = queryName.replaceAll("\\_", "\\\\_");
				//根据负载均衡名称模糊查询
				sql.append(" and binary ld.pool_name like ?");
	            list.add("%" + queryName + "%");
	            
			}else if(queryType.equals("cus")){
				//根据所属客户精确查询
				String[] cusOrgs = queryName.split(",");
				sql.append(" and ( ");
				for(String org:cusOrgs){
					sql.append(" binary cus.cus_org = ? or ");
					list.add(org);
				}
				sql.append(" 1 = 2 ) ");
				
			}else if(queryType.equals("pro")){
				//根据项目名称精确查询
				String[] prjName = queryName.split(",");
				sql.append(" and ( ");
				for(String prj:prjName){
					sql.append(" binary prj.prj_name = ? or ");
					list.add(prj);
				}
				sql.append(" 1 = 2 ) ");
			}
			else{
				sql.append(" and  1 = 2 ");
			}
		}
		if (!StringUtil.isEmpty(dcName)) {
			sql.append(" and binary dc.dc_name = ?");
            list.add(dcName);
        }
		sql.append(" and ld.`mode` = ?");
        list.add(mode);
		sql.append(" order by ld.create_time desc");
	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put("sql", sql.toString());
	    params.put("paramsList", list);
		return params;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Page getEcmcLdPoolExpList(Page page, QueryMap queryMap,
			Date endTime, int cou, String poolId, String mode,
			String role, String memberName, String healthName, String isRepair) {
		log.info("运维查询负载均衡的成员异常记录");
		List<String> vmIds = new ArrayList<String>();
		List<String> heaIds = new ArrayList<String>();
		//	根据成员名称（即主机名称）查询主机id
		if(!StringUtil.isEmpty(memberName)){
			StringBuffer msql = new StringBuffer("SELECT vm.vm_id FROM cloud_vm vm WHERE vm.vm_name =? ");
			javax.persistence.Query vmIdsquery = ecmcMonitorAlarmItemDao.createSQLNativeQuery(msql.toString(),memberName);
			if(null != vmIdsquery && null != vmIdsquery.getResultList() && vmIdsquery.getResultList().size() > 0){
				vmIds = (List<String>)vmIdsquery.getResultList();
			}
		}
		//	根据健康检查名称查询健康检查id
		if(!StringUtil.isEmpty(healthName)){
			StringBuffer hsql = new StringBuffer("SELECT cld.ldm_id FROM cloud_ldmonitor cld WHERE cld.ldm_name = ?");
			javax.persistence.Query heaIdsquery = ecmcMonitorAlarmItemDao.createSQLNativeQuery(hsql.toString(),healthName);
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
		javax.persistence.Query query = ecmcMonitorAlarmItemDao.createSQLNativeQuery(msql.toString(),vmId);
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
		javax.persistence.Query query = ecmcMonitorAlarmItemDao.createSQLNativeQuery(hsql.toString(),healthId);
		if(null != query && null != query.getResultList() && query.getResultList().size() > 0){
			return (String) query.getResultList().get(0);
		}
		return "";
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
		StringBuffer sql =new StringBuffer("SELECT ld.pool_id,ld.pool_name,net.net_id,net.net_name,ld.mode,");
        sql.append(" dc.dc_name,prj.prj_name,cus.cus_org ")
           .append(" FROM cloud_ldpool ld")
           .append(" LEFT JOIN cloud_subnetwork sub ON ld.subnet_id = sub.subnet_id")
           .append(" LEFT JOIN cloud_network net ON sub.net_id = net.net_id")
           .append(" LEFT JOIN dc_datacenter dc ON ld.dc_id = dc.id")
           .append(" LEFT JOIN cloud_project prj ON ld.prj_id = prj.prj_id ")
           .append(" LEFT JOIN sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
        sql.append(" WHERE ld.is_visible = '1' AND (ld.pool_status = 'ACTIVE' OR ld.pool_status = 'PENDING_CREATE') ");
        sql.append(" AND ld.pool_id = ? ");
        list.add(ldPoolId);
        javax.persistence.Query query = ecmcMonitorAlarmItemDao.createSQLNativeQuery(sql.toString(), list.toArray());
        LdPoolIndicator ldPool = new LdPoolIndicator();
        List resultlist = new ArrayList();
        if(null!=query ){
        	resultlist = query.getResultList();
        }
        if(null != resultlist&& resultlist.size() == 1){
            Object[] objs = (Object[]) resultlist.get(0);
            ldPool.setLdPoolId(String.valueOf(objs[0]));
            ldPool.setLdPoolName(String.valueOf(objs[1]));
            ldPool.setNetId(String.valueOf(objs[2]));
            ldPool.setNetName(String.valueOf(objs[3]));
            ldPool.setMode(String.valueOf(objs[4]));
            ldPool.setDcName(String.valueOf(objs[5]));
            ldPool.setPrjName(String.valueOf(objs[6]));
            ldPool.setCusName(String.valueOf(objs[7]));
        }
        return ldPool;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<CloudLdpoolExp>> getMemAndHeaNameById(Date endTime, int cou,
			String poolId, String mode, String role, String memberName,
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
			javax.persistence.Query vmIdsquery = ecmcMonitorAlarmItemDao.createSQLNativeQuery(msql.toString(),memberName);
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
						List<Object[]> heasList = ecmcMonitorAlarmItemDao.createSQLNativeQuery(hsql.toString())
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
				List<Object[]> memsList = ecmcMonitorAlarmItemDao.createSQLNativeQuery(msql.toString())
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
        case 1440:
        	startTime = DateUtil.addDay(endTime,new int[]{0,0,-15});
            break;
        case 2880:
        	startTime = DateUtil.addDay(endTime,new int[]{0,0,-30});
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
