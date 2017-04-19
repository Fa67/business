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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.monitor.bean.EcmcVmIndicator;
import com.eayun.monitor.bean.MonitorAlarmUtil;
import com.eayun.monitor.dao.EcmcMonitorAlarmItemDao;
import com.eayun.monitor.ecmcservice.EcmcInstanceMonitorService;

@Service
@Transactional
public class EcmcInstanceMonitorServiceImpl implements
		EcmcInstanceMonitorService {

	private static final Logger   log = LoggerFactory.getLogger(EcmcInstanceMonitorServiceImpl.class);

	@Autowired
	private EcmcMonitorAlarmItemDao ecmcMonitorAlarmItemDao;
	
	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Autowired
    private JedisUtil jedisUtil;
	
	private static String[]       meters = new String[] { "cpu_util" , "memory.usage" , "disk.read.bytes.rate" ,
	      "disk.write.bytes.rate" , "network.incoming.bytes.rate" , "network.outgoing.bytes.rate" , "volume.used"};
	
	@SuppressWarnings("unchecked")
	@Override
	public Page getInstanceListforEcmcLive(Page page, QueryMap queryMap,
			String queryType, String queryName, String dcName, String orderBy,
			String sort, String versionId) {
		log.info("查询云数据库资源监控实时指标数据");
		Map<String, Object> params = this.getInstanceListForMonitor(queryType , queryName , dcName ,versionId);
		List<Object> paramsList = (List<Object>) params.get("paramsList");
		javax.persistence.Query query = ecmcMonitorAlarmItemDao.createSQLNativeQuery(params.get("sql").toString(), paramsList.toArray());
		List datalist = (List) query.getResultList();
		if(null != datalist && !datalist.isEmpty()){
			for (int i = 0; i < datalist.size(); i++) {
				Object[] objs = (Object[]) datalist.get(i);
	            EcmcVmIndicator ecmcVmIndicator = new EcmcVmIndicator();
	            String instanceId = String.valueOf(objs[0]);
	            ecmcVmIndicator.setInstanceId(instanceId);
	            ecmcVmIndicator.setInstanceName(String.valueOf(objs[1]));
	            ecmcVmIndicator.setIsMaster(String.valueOf(objs[2]));
	            String vmId = String.valueOf(objs[3]);
	            ecmcVmIndicator.setVmId(vmId);
	            String datastoreName = MonitorAlarmUtil.transferDatastoreName(String.valueOf(objs[5]));
	            String dvName = datastoreName+String.valueOf(objs[4]);
	            ecmcVmIndicator.setDataVersionName(dvName);
	            ecmcVmIndicator.setDcName(String.valueOf(objs[6]));
	            ecmcVmIndicator.setCusName(String.valueOf(objs[7]));
	            ecmcVmIndicator.setPrjName(String.valueOf(objs[8]));
	            
	            try {
	                Double cpu = jedisUtil.getDouble(RedisKey.MONITOR_CPU+vmId);
	                Double ram = jedisUtil.getDouble(RedisKey.MONITOR_MEMORY+vmId);
	                Double volused = jedisUtil.getDouble(RedisKey.MONITOR_VOLUME_USED+vmId);
	                if(cpu > 100d){cpu = 100d;}
	                if(ram > 100d){ram = 100d;}
	                if(volused > 100d){volused = 100d;}
	                Double read = jedisUtil.getDouble(RedisKey.MONITOR_DISK_READ+vmId);
	                Double write = jedisUtil.getDouble(RedisKey.MONITOR_DISK_WRITE+vmId);
	                Double netin = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_INCOMING+vmId);
	                Double netout = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_OUTGOING+vmId);
	                
	                Double lastcpu = jedisUtil.getDouble(RedisKey.MONITOR_CPU_LAST+vmId);
	                Double lastram = jedisUtil.getDouble(RedisKey.MONITOR_MEMORY_LAST+vmId);
	                Double lastvolused = jedisUtil.getDouble(RedisKey.MONITOR_VOLUME_USED_LAST+vmId);
	                if(lastcpu > 100d){lastcpu = 100d;}
	                if(lastram > 100d){lastram = 100d;}
	                if(lastvolused > 100d){lastram = 100d;}
	                Double lastread = jedisUtil.getDouble(RedisKey.MONITOR_DISK_READ_LAST+vmId);
	                Double lastwrite = jedisUtil.getDouble(RedisKey.MONITOR_DISK_WRITE_LAST+vmId);
	                Double lastnetin = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_INCOMING_LAST+vmId);
	                Double lastnetout = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_OUTGOING_LAST+vmId);
	                
	                cpu = (Double)(Math.round(cpu*10)/10.0);
	                ram = (Double)(Math.round(ram*10)/10.0);
	                volused = (Double)(Math.round(volused*10)/10.0);
	                
	                lastcpu = (Double)(Math.round(lastcpu*10)/10.0);
	                lastram = (Double)(Math.round(lastram*10)/10.0);
	                lastvolused = (Double)(Math.round(lastvolused*10)/10.0);
	                
	                read = (Double)(Math.round(read*10000)/10000.0);
	                write = (Double)(Math.round(write*10000)/10000.0);
	                netin = (Double)(Math.round(netin*10000)/10000.0);
	                netout = (Double)(Math.round(netout*10000)/10000.0);
	                
	                lastread = (Double)(Math.round(lastread*10000)/10000.0);
	                lastwrite = (Double)(Math.round(lastwrite*10000)/10000.0);
	                lastnetin = (Double)(Math.round(lastnetin*10000)/10000.0);
	                lastnetout = (Double)(Math.round(lastnetout*10000)/10000.0);
	                
	                int cpuDiff = 0;
	                int ramDiff = 0;
	                int volusedDiff = 0;
	                int netinDiff = 0;
	                int netoutDiff = 0;
	                int writeDiff = 0;
	                int readDiff = 0;
	                if(cpu > lastcpu){
	                    cpuDiff = 1;
	                }else if(cpu < lastcpu){
	                    cpuDiff = -1;
	                }
	                if(ram > lastram){
	                    ramDiff = 1;
	                }else if(ram < lastram){
	                    ramDiff = -1;
	                }
	                if(volused > lastvolused){
	                	volusedDiff = 1;
	                }else if(volused < lastvolused){
	                	volusedDiff = -1;
	                }
	                if(netin > lastnetin){
	                    netinDiff = 1;
	                }else if(netin < lastnetin){
	                    netinDiff = -1;
	                }
	                if(netout > lastnetout){
	                    netoutDiff = 1;
	                }else if(netout < lastnetout){
	                    netoutDiff = -1;
	                }
	                if(write > lastwrite){
	                    writeDiff = 1;
	                }else if(write < lastwrite){
	                    writeDiff = -1;
	                }
	                if(read > lastread){
	                    readDiff = 1;
	                }else if(read < lastread){
	                    readDiff = -1;
	                }
	                
	                ecmcVmIndicator.setCpu(cpu);
	                ecmcVmIndicator.setCpuDiff(cpuDiff);
	                ecmcVmIndicator.setRam(ram);
	                ecmcVmIndicator.setRamDiff(ramDiff);
	                ecmcVmIndicator.setDiskRead(read);
	                ecmcVmIndicator.setReadDiff(readDiff);
	                ecmcVmIndicator.setDiskWrite(write);
	                ecmcVmIndicator.setWriteDiff(writeDiff);
	                ecmcVmIndicator.setNetIn(netin);
	                ecmcVmIndicator.setNetinDiff(netinDiff);
	                ecmcVmIndicator.setNetOut(netout);
	                ecmcVmIndicator.setNetoutDiff(netoutDiff);
	                ecmcVmIndicator.setVolumeUsed(volused);
	                ecmcVmIndicator.setVolumeUsedDiff(volusedDiff);
	                
	            } catch (Exception e) {
	                log.error("获取运维实例监控数据失败", e);
	                throw new AppException("数据查询异常");
	            }
	            datalist.set(i, ecmcVmIndicator);
			}
		}
		if((null != orderBy && !("".equals(orderBy))) && (null != sort && !("".equals(sort)))){
        	final String order = orderBy;
            final String asc = sort;
            Collections.sort(datalist,new Comparator<EcmcVmIndicator>(){
                public int compare(EcmcVmIndicator arg0, EcmcVmIndicator arg1) {
                	double value0 = 0.0;
                	double value1 = 0.0;
                	switch(order){
                    case "cpu":
                    	value0 = arg0.getCpu();
                    	value1 = arg1.getCpu();
                        break;
                    case "ram":
                    	value0 = arg0.getRam();
                    	value1 = arg1.getRam();
                        break;
                    case "read":
                    	value0 = arg0.getDiskRead();
                    	value1 = arg1.getDiskRead();
                        break;
                    case "write":
                    	value0 = arg0.getDiskWrite();
                    	value1 = arg1.getDiskWrite();
                        break;
                    case "incomming":
                    	value0 = arg0.getNetIn();
                    	value1 = arg1.getNetIn();
                        break;
                    case "outgoing":
                    	value0 = arg0.getNetOut();
                    	value1 = arg1.getNetOut();
                        break;
                    case "volumeused":
                    	value0 = arg0.getVolumeUsed();
                    	value1 = arg1.getVolumeUsed();
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
        List<EcmcVmIndicator> resultList = new ArrayList<EcmcVmIndicator>();
        int start = (pageNumber-1)*pageSize;
        if(datalist.size()>0){
            int end = start+pageSize;
            resultList = datalist.subList(start, end < datalist.size()?end:datalist.size());
        }
        page = new Page(start, datalist.size(), pageSize, resultList);
		return page;
	}
	/**
	 * 查询出所有符合条件的数据库实例SQL
	 * @Author: duanbinbin
	 * @param page
	 * @param queryMap
	 * @param queryType
	 * @param queryName
	 * @param dcName
	 * @return
	 *<li>Date: 2017年3月9日</li>
	 */
	public Map<String, Object> getInstanceListForMonitor(String queryType,
			String queryName,String dcName, String versionId){
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql =new StringBuffer("SELECT rds.rds_id,rds.rds_name,rds.is_master,rds.vm_id,");
        sql.append(" cdv.name AS verName ,cd.`name` AS dataName,dc.dc_name,cus.cus_org,prj.prj_name ")
           .append(" FROM cloud_rdsinstance rds ")
           .append(" LEFT JOIN cloud_datastoreversion cdv ON rds.version_id = cdv.id ")
           .append(" LEFT JOIN cloud_datastore cd ON cdv.datastore_id = cd.id ")
           .append(" LEFT JOIN dc_datacenter dc ON rds.dc_id = dc.id ")
           .append(" LEFT JOIN cloud_project prj ON rds.prj_id = prj.prj_id ")
           .append(" LEFT JOIN sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
        sql.append(" WHERE rds.is_deleted = '0' and rds.is_visible = '1' ");
		if (null != queryName && !queryName.trim().equals("")) {
			if(queryType.equals("obj")){
				queryName = queryName.replaceAll("\\_", "\\\\_");
				//根据实例名称模糊查询
				sql.append(" and binary rds.rds_name like ?");
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
		if(!StringUtil.isEmpty(versionId)){
			sql.append(" and rds.version_id = ?");
            list.add(versionId);
		}
		sql.append(" order by rds.create_time desc");
	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put("sql", sql.toString());
	    params.put("paramsList", list);
		return params;
	}
	@SuppressWarnings("unchecked")
	@Override
	public Page getInstanceListforEcmcLast(Page page, QueryMap queryMap,
			String queryType, String queryName, Date endDate, int mins,
			String orderBy, String sort, String dcName, String versionId) {
		log.info("获取实例资源监控历史平均数据，并排序");
		int count = mins/60;
		Date startDate = null;
		endDate = DateUtil.dateRemoveSec(endDate);
		String suffix = "";
        switch(count){
            case 0:
            	suffix = ".detail";
            	startDate = DateUtil.addDay(endDate, new int[]{0,0,0,0,-30});
                break;
            case 1:
            	suffix = ".3min";
            	startDate = DateUtil.addDay(endDate, new int[]{0,0,0,-1});
                break;
            case 6:
            	suffix = ".10min";
            	startDate = DateUtil.addDay(endDate, new int[]{0,0,0,-6});
                break;
            case 12:
            	suffix = ".10min";
            	startDate = DateUtil.addDay(endDate, new int[]{0,0,0,-12});
                break;
            case 24:
            	suffix = ".1h";
            	startDate = DateUtil.addDay(endDate, new int[]{0,0,-1});
                break;
            case 168:
            	suffix = ".1d";
            	startDate = DateUtil.addDay(endDate, new int[]{0,0,-7});
                break;
            case 360:
            	suffix = ".1d";
            	startDate = DateUtil.addDay(endDate, new int[]{0,0,-15});
                break;
            case 720:
            	suffix = ".1d";
            	startDate = DateUtil.addDay(endDate, new int[]{0,0,-30});
                break;
          }
        Map<String, Object> params = this.getInstanceListForMonitor(queryType , queryName , dcName ,versionId);
        List<Object> paramsList = (List<Object>) params.get("paramsList");
        javax.persistence.Query query = ecmcMonitorAlarmItemDao.createSQLNativeQuery(params.get("sql").toString(), paramsList.toArray());
		List datalist = (List) query.getResultList();
        
        List<EcmcVmIndicator> list = new ArrayList<EcmcVmIndicator>();
        List<EcmcVmIndicator> resultList = new ArrayList<EcmcVmIndicator>();
        
        List<JSONObject> cputotallist = new ArrayList<JSONObject>();  
        List<JSONObject> ramtotallist = new ArrayList<JSONObject>();
        List<JSONObject> readtotallist = new ArrayList<JSONObject>();
        List<JSONObject> writetotallist = new ArrayList<JSONObject>();
        List<JSONObject> netintotallist = new ArrayList<JSONObject>();
        List<JSONObject> netouttotallist = new ArrayList<JSONObject>();
        
        List<JSONObject> volumeusedlist = new ArrayList<JSONObject>();
        
        List<String> vmIdList = new ArrayList<String>();
        for(int i = 0; i < datalist.size(); i++){
        	Object[] objs = (Object[]) datalist.get(i);
        	vmIdList.add(String.valueOf(objs[3]));
        }
        Aggregation aggs = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("vm_id").in(vmIdList)),
                Aggregation.match(Criteria.where("timestamp").gt(startDate)),
                Aggregation.match(Criteria.where("timestamp").lte(endDate)),
                Aggregation.group("vm_id").avg("counter_volume").as("total")
                );
        for (String meter : meters) {
        	AggregationResults<JSONObject> totalresult = mongoTemplate.aggregate(aggs , meter + suffix, JSONObject.class);
    		switch(meter){
            case "cpu_util":
            	cputotallist = totalresult.getMappedResults();
                break;
            case "memory.usage":
            	ramtotallist = totalresult.getMappedResults();
                break;
            case "disk.read.bytes.rate":
            	readtotallist = totalresult.getMappedResults();
                break;
            case "disk.write.bytes.rate":
            	writetotallist = totalresult.getMappedResults();
                break;
            case "network.incoming.bytes.rate":
            	netintotallist = totalresult.getMappedResults();
                break;
            case "network.outgoing.bytes.rate":
            	netouttotallist = totalresult.getMappedResults();
                break;
            case "volume.used":
            	volumeusedlist = totalresult.getMappedResults();
                break;
        	}
        }
        
        for (int i = 0; i < datalist.size(); i++) {
            Object[] objs = (Object[]) datalist.get(i);
            EcmcVmIndicator ecmcVmIndicator = new EcmcVmIndicator();
            String instanceId = String.valueOf(objs[0]);
            ecmcVmIndicator.setInstanceId(instanceId);
            ecmcVmIndicator.setInstanceName(String.valueOf(objs[1]));
            ecmcVmIndicator.setIsMaster(String.valueOf(objs[2]));
            String vmId = String.valueOf(objs[3]);
            ecmcVmIndicator.setVmId(vmId);
            String datastoreName = MonitorAlarmUtil.transferDatastoreName(String.valueOf(objs[5]));
            String dvName = datastoreName+String.valueOf(objs[4]);
            ecmcVmIndicator.setDataVersionName(dvName);
            ecmcVmIndicator.setDcName(String.valueOf(objs[6]));
            ecmcVmIndicator.setCusName(String.valueOf(objs[7]));
            ecmcVmIndicator.setPrjName(String.valueOf(objs[8]));
            
            Date createTime = this.getInstanceCreateTime(instanceId);
            
            Double cpu = 0.0;
            Double ram = 0.0;
            Double read = 0.0;
            Double write = 0.0;
            Double netin = 0.0;
            Double netout = 0.0;
            
            Double volumeused = 0.0;
            
            if(endDate.before(createTime)){
            	ecmcVmIndicator.setMongodb(false);
            }else{
            	ecmcVmIndicator.setMongodb(true);
            }
            for (String meter : meters) {
            	switch(meter){
                case "cpu_util":
                	if(cputotallist.size() > 0){
                		for(JSONObject json : cputotallist){
                			if(vmId.equals(json.get("_id"))){
                				cpu = json.getDouble("total");
                				if(cpu > 100d){
                					cpu = 100d;
                				}
                				break;
                			}
                		}
                    }
                	ecmcVmIndicator.setCpu(cpu);
                    break;
                case "memory.usage":
                	if(ramtotallist.size() > 0){
                		for(JSONObject json : ramtotallist){
                			if(vmId.equals(json.get("_id"))){
                				ram = json.getDouble("total");
                				if(ram > 100d){
                					ram = 100d;
                				}
                				break;
                			}
                		}
                    }
                	ecmcVmIndicator.setRam(ram);
                    break;
                case "disk.read.bytes.rate":
                	if(readtotallist.size() > 0){
                		for(JSONObject json : readtotallist){
                			if(vmId.equals(json.get("_id"))){
                				read = json.getDouble("total");
                				break;
                			}
                		}
                    }
                	ecmcVmIndicator.setDiskRead(read);
                    break;
                case "disk.write.bytes.rate":
                	if(writetotallist.size() > 0){
                		for(JSONObject json : writetotallist){
                			if(vmId.equals(json.get("_id"))){
                				write = json.getDouble("total");
                				break;
                			}
                		}
                    }
                	ecmcVmIndicator.setDiskWrite(write);
                    break;
                case "network.incoming.bytes.rate":
                	if(netintotallist.size() > 0){
                		for(JSONObject json : netintotallist){
                			if(vmId.equals(json.get("_id"))){
                				netin = json.getDouble("total");
                				break;
                			}
                		}
                    }
                	ecmcVmIndicator.setNetIn(netin);
                    break;
                case "network.outgoing.bytes.rate":
                	if(netouttotallist.size() > 0){
                		for(JSONObject json : netouttotallist){
                			if(vmId.equals(json.get("_id"))){
                				netout = json.getDouble("total");
                				break;
                			}
                		}
                    }
                	ecmcVmIndicator.setNetOut(netout);
                    break;
                case "volume.used":
                	if(volumeusedlist.size() > 0){
                		for(JSONObject json : volumeusedlist){
                			if(vmId.equals(json.get("_id"))){
                				volumeused = json.getDouble("total");
                				if(volumeused > 100d){
                					volumeused = 100d;
                				}
                				break;
                			}
                		}
                    }
                	ecmcVmIndicator.setVolumeUsed(volumeused);
                    break;
              }
            }
            list.add(ecmcVmIndicator);
        }
        if((null != orderBy && !("".equals(orderBy))) && (null != sort && !("".equals(sort)))){
        	final String order = orderBy;
            final String asc = sort;
            Collections.sort(list,new Comparator<EcmcVmIndicator>(){
                public int compare(EcmcVmIndicator arg0, EcmcVmIndicator arg1) {
                	double value0 = 0.0;
                	double value1 = 0.0;
                	switch(order){
                    case "cpu":
                    	value0 = arg0.getCpu();
                    	value1 = arg1.getCpu();
                        break;
                    case "ram":
                    	value0 = arg0.getRam();
                    	value1 = arg1.getRam();
                        break;
                    case "read":
                    	value0 = arg0.getDiskRead();
                    	value1 = arg1.getDiskRead();
                        break;
                    case "write":
                    	value0 = arg0.getDiskWrite();
                    	value1 = arg1.getDiskWrite();
                        break;
                    case "incomming":
                    	value0 = arg0.getNetIn();
                    	value1 = arg1.getNetIn();
                        break;
                    case "outgoing":
                    	value0 = arg0.getNetOut();
                    	value1 = arg1.getNetOut();
                        break;
                    case "volumeused":
                    	value0 = arg0.getVolumeUsed();
                    	value1 = arg1.getVolumeUsed();
                        break;
                    }
                	int result = 0;
                	if(!arg0.isMongodb()){
                		value0 = -1;
                	}
                	if(!arg1.isMongodb()){
                		value1 = -1;
                	}
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
        
        int start = (pageNumber-1)*pageSize;
        if(list.size()>0){
            int end = start+pageSize;
            resultList = list.subList(start, end < list.size()?end:list.size());
        }
        page = new Page(start, list.size(), pageSize, resultList);
		return page;
	}

	private Date getInstanceCreateTime(String instanceId){
		List<Object> list = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
	            "select rds.create_time  FROM cloud_rdsinstance rds "
	            + " WHERE rds.is_deleted = '0'  and rds.rds_id = ?");
        list.add(instanceId);
        javax.persistence.Query query = ecmcMonitorAlarmItemDao.createSQLNativeQuery(hql.toString(), list.toArray());
        Date createTime = null;
        if(null!=query && query.getResultList().size() > 0 ){
            Object obj = (Object) query.getResultList().get(0);
            createTime = (Date)obj;
        }
        return createTime;
	}
	@Override
	public EcmcVmIndicator getInstancedetailById(String instanceId) {
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = 
				new StringBuffer("SELECT rds.rds_id,rds.rds_name,rds.is_master,rds.vm_id,");
        sql.append(" cdv.name AS verName ,cd.`name` AS dataName ")
           .append(" FROM cloud_rdsinstance rds ")
           .append(" LEFT JOIN cloud_datastoreversion cdv ON rds.version_id = cdv.id ")
           .append(" LEFT JOIN cloud_datastore cd ON cdv.datastore_id = cd.id ");
        sql.append(" WHERE rds.is_deleted = '0' and rds.is_visible = '1' and rds.rds_id = ? ");
        list.add(instanceId);
        javax.persistence.Query query = ecmcMonitorAlarmItemDao.createSQLNativeQuery(sql.toString(), list.toArray());
        EcmcVmIndicator instance = new EcmcVmIndicator();
        List resultlist = new ArrayList();
        if(null!=query ){
        	resultlist = query.getResultList();
        }
        if(null != resultlist&& resultlist.size() == 1){
            Object[] objs = (Object[]) resultlist.get(0);
            instance.setInstanceId(String.valueOf(objs[0]));
            instance.setInstanceName(String.valueOf(objs[1]));
            instance.setIsMaster(String.valueOf(objs[2]));
            instance.setVmId(String.valueOf(objs[3]));
            String datastoreName = MonitorAlarmUtil.transferDatastoreName(String.valueOf(objs[5]));
            instance.setDataVersionName(datastoreName+String.valueOf(objs[4]));
        }
        return instance;
	}
}
