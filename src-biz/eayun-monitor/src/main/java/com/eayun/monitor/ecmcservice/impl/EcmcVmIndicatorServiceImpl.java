package com.eayun.monitor.ecmcservice.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.monitor.bean.EcmcVmIndicator;
import com.eayun.monitor.bean.MonitorMngData;
import com.eayun.monitor.dao.EcmcMonitorAlarmItemDao;
import com.eayun.monitor.ecmcservice.EcmcVmIndicatorService;

@Service
@Transactional
public class EcmcVmIndicatorServiceImpl implements EcmcVmIndicatorService {
	
	private static final Logger   log = LoggerFactory.getLogger(EcmcVmIndicatorServiceImpl.class);

	@Autowired
	private EcmcMonitorAlarmItemDao ecmcMonitorAlarmItemDao;
	@Autowired
    private MongoTemplate mongoTemplate;
	
	@Autowired
    private JedisUtil jedisUtil;
	
	private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	private static String[]       meters = new String[] { "cpu_util" , "memory.usage" , "disk.read.bytes.rate" ,
      "disk.write.bytes.rate" , "network.incoming.bytes.rate" , "network.outgoing.bytes.rate" };
	
	@Override
	public List<MonitorMngData> getInterval(String parentId) {
		log.info("查询redis中历史数据时间区间的值");
		return getMonitorMngData(parentId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Page getVmListforLive(Page page, QueryMap queryMap, String queryType,
			String queryName,String dcName,String orderBy,String sort) {
		log.info("查询资源监控实时指标数据");
		Map<String, Object> params = this.getVmListForMonitor(page , queryMap , queryType , queryName , dcName);
		List<Object> paramsList = (List<Object>) params.get("paramsList");
		javax.persistence.Query query = ecmcMonitorAlarmItemDao.createSQLNativeQuery(params.get("sql").toString(), paramsList.toArray());
		List datalist = (List) query.getResultList();
        for (int i = 0; i < datalist.size(); i++) {
        	//根据查询出的主机查询redis里的监控数据
        	
            Object[] objs = (Object[]) datalist.get(i);
            EcmcVmIndicator ecmcVmIndicator = new EcmcVmIndicator();
            ecmcVmIndicator.setVmId(String.valueOf(objs[0]));
            ecmcVmIndicator.setVmName(String.valueOf(objs[1]));
            ecmcVmIndicator.setNetName(String.valueOf(objs[2]));
            ecmcVmIndicator.setVmIp(String.valueOf(objs[3]));
            ecmcVmIndicator.setOsType(String.valueOf(objs[4]));
            ecmcVmIndicator.setDcName(String.valueOf(objs[5]));
            ecmcVmIndicator.setCusName(String.valueOf(objs[6]));
            ecmcVmIndicator.setPrjName(String.valueOf(objs[7]));
            try {
                Double cpu = jedisUtil.getDouble(RedisKey.MONITOR_CPU+ecmcVmIndicator.getVmId());
                Double ram = jedisUtil.getDouble(RedisKey.MONITOR_MEMORY+ecmcVmIndicator.getVmId());
                if(cpu > 100d){cpu = 100d;}
                if(ram > 100d){ram = 100d;}
                Double read = jedisUtil.getDouble(RedisKey.MONITOR_DISK_READ+ecmcVmIndicator.getVmId());
                Double write = jedisUtil.getDouble(RedisKey.MONITOR_DISK_WRITE+ecmcVmIndicator.getVmId());
                Double netin = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_INCOMING+ecmcVmIndicator.getVmId());
                Double netout = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_OUTGOING+ecmcVmIndicator.getVmId());
                
                Double lastcpu = jedisUtil.getDouble(RedisKey.MONITOR_CPU_LAST+ecmcVmIndicator.getVmId());
                Double lastram = jedisUtil.getDouble(RedisKey.MONITOR_MEMORY_LAST+ecmcVmIndicator.getVmId());
                if(lastcpu > 100d){lastcpu = 100d;}
                if(lastram > 100d){lastram = 100d;}
                Double lastread = jedisUtil.getDouble(RedisKey.MONITOR_DISK_READ_LAST+ecmcVmIndicator.getVmId());
                Double lastwrite = jedisUtil.getDouble(RedisKey.MONITOR_DISK_WRITE_LAST+ecmcVmIndicator.getVmId());
                Double lastnetin = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_INCOMING_LAST+ecmcVmIndicator.getVmId());
                Double lastnetout = jedisUtil.getDouble(RedisKey.MONITOR_NETWORK_OUTGOING_LAST+ecmcVmIndicator.getVmId());
                
                cpu = (Double)(Math.round(cpu*10)/10.0);
                ram = (Double)(Math.round(ram*10)/10.0);
                
                lastcpu = (Double)(Math.round(lastcpu*10)/10.0);
                lastram = (Double)(Math.round(lastram*10)/10.0);
                
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
                
            } catch (Exception e) {
                log.error("获取监控数据失败", e);
                throw new AppException("数据查询异常");
            }
            datalist.set(i, ecmcVmIndicator);
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

	@Override
	public Page getVmListforLast(Page page, QueryMap queryMap,
			String queryType, String queryName, Date endDate, int mins,
			String orderBy, String sort,String dcName) {
		log.info("获取资源监控历史平均数据，并排序");
		
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
        Map<String, Object> params = getVmListForMonitor(page , queryMap , queryType , queryName , dcName);
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
        List<String> vmIdList = new ArrayList<String>();
        for(int i = 0; i < datalist.size(); i++){
        	Object[] objs = (Object[]) datalist.get(i);
        	vmIdList.add(String.valueOf(objs[0]));
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
        	}
        }
        
        for (int i = 0; i < datalist.size(); i++) {
            Object[] objs = (Object[]) datalist.get(i);
            EcmcVmIndicator ecmcVmIndicator = new EcmcVmIndicator();
            
            String vmId = String.valueOf(objs[0]);
            ecmcVmIndicator.setVmId(vmId);
            ecmcVmIndicator.setVmName(String.valueOf(objs[1]));
            ecmcVmIndicator.setNetName(String.valueOf(objs[2]));
            ecmcVmIndicator.setVmIp(String.valueOf(objs[3]));
            ecmcVmIndicator.setOsType(String.valueOf(objs[4]));
            ecmcVmIndicator.setDcName(String.valueOf(objs[5]));
            ecmcVmIndicator.setCusName(String.valueOf(objs[6]));
            ecmcVmIndicator.setPrjName(String.valueOf(objs[7]));
            Date createTime = getVmCreateTime(vmId);
            
            Double cpu = 0.0;
            Double ram = 0.0;
            Double read = 0.0;
            Double write = 0.0;
            Double netin = 0.0;
            Double netout = 0.0;
            
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

	/**
	 * 查询出所有符合条件的云主机sql语句
	 * @param queryType
	 * @param queryName
	 * @param dcName
	 * @return
	 */
	public Map<String, Object> getVmListForMonitor(Page page, QueryMap queryMap , String queryType,String queryName,String dcName){
		List<Object> list = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
	            "select vm.vm_id,vm.vm_name,cnet.net_name,vm.vm_ip,sdt.node_name,dc.dc_name,cus.cus_org,prj.prj_name "
	            + " FROM cloud_vm vm LEFT JOIN cloud_network cnet ON vm.net_id = cnet.net_id LEFT JOIN sys_data_tree sdt ON vm.sys_type = sdt.node_id "
	            + " LEFT JOIN dc_datacenter dc ON vm.dc_id = dc.id "
	            + " LEFT JOIN cloud_project prj ON vm.prj_id = prj.prj_id "
	            + " LEFT JOIN sys_selfcustomer cus ON prj.customer_id = cus.cus_id "
	            + " WHERE vm.is_deleted = '0' and vm.is_visable = '1' ");
		
		if (null != queryName && !queryName.trim().equals("")) {
			if(queryType.equals("obj")){
				queryName = queryName.replaceAll("\\_", "\\\\_");
				//根据云主机名称模糊查询
	            hql.append(" and binary vm.vm_name like ?");
	            list.add("%" + queryName + "%");
	            
			}else if(queryType.equals("cus")){
				//根据所属客户精确查询
				String[] cusOrgs = queryName.split(",");
				hql.append(" and ( ");
				for(String org:cusOrgs){
					hql.append(" binary cus.cus_org = ? or ");
					list.add(org);
				}
				hql.append(" 1 = 2 ) ");
				
			}else if(queryType.equals("pro")){
				//根据项目名称精确查询
				String[] prjName = queryName.split(",");
				hql.append(" and ( ");
				for(String prj:prjName){
					hql.append(" binary prj.prj_name = ? or ");
					list.add(prj);
				}
				hql.append(" 1 = 2 ) ");
			}
			else{
				hql.append(" and  1 = 2 ");
			}
		}
		if (null != dcName && !dcName.trim().equals("")) {
            hql.append(" and binary dc.dc_name = ?");
            list.add(dcName);
        }
	    hql.append(" order by vm.create_time desc");
	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put("sql", hql.toString());
	    params.put("paramsList", list);
		return params;
	}

	/**
	 * 此方法与ECSC方法相比增加了15天和30天
	 * 加上磁盘使用率查询
	 * @param endTime
	 * @param count
	 * @param vmId
	 * @param type
	 * @return
	 */
	@Override
	public List<EcmcVmIndicator> getDataById(Date endTime, int count,
			String vmId, String type) {
		log.info("获取云主机资源监控信息用于图表显示");
        Date createTime = getVmCreateTime(vmId);
        Date firstDate = DateUtil.dateRemoveSec(endTime);
        Date lastDate = DateUtil.dateRemoveSec(endTime);    //计算获取数据的时间
        Date showDate = DateUtil.dateRemoveSec(endTime);    //图表显示时间
        
        String prefix = "";
        String prefixTwo = "";
        switch(type){
            case "monitorCPU":
                prefix = "cpu_util";
                break;
            case "monitorRam":
                prefix = "memory.usage";
                break;
            case "monitorDisk":
                prefix = "disk.read.bytes.rate";
            	prefixTwo = "disk.write.bytes.rate";
                break;
            case "monitorNet":
                prefix = "network.incoming.bytes.rate";
            	prefixTwo = "network.outgoing.bytes.rate";
                break;
            case "volumeused":			//磁盘使用率
                prefix = "volume.used";
                break;
          }
        
        String suffix = "";
        int size = 13;
        /**
         * 每个点的数据包含的设置的最小区间的个数
         * 如：30分钟共11个点，每个点是三个一分钟的平均数，最小区间是1分钟，个数是3
         *  12小时共13个点，每个点是一小时内12个5分钟的平均数，最小区间是5分钟，个数是12
         */
        
        /**
         * 2016-01-12:逻辑失误：N个点，但是只有N-1个最小区间合集，因此还要再向前取一个点数据范围的最小区间合集的
         * 每个数据点取的是上一个最小区间的数值
         */
        Calendar c = Calendar.getInstance();  
        c.setTime(firstDate); 
        int minute = c.get(c.MINUTE);
        int newMin = minute;
        /**
         * isminPrefix:最小区间时间是否向前取等，(即是<=X<还是<X<=)
         */
        boolean isminPrefix = true;
        boolean newData = false;
        int newDateCount = 15;
        switch(count){
            case 3:
                suffix = ".detail";
                size = 11;
                isminPrefix = true;
                showDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,-30});
                lastDate = DateUtil.addDay(showDate, new int[]{0,0,0,0,-3});
                break;
            case 5:
                suffix = ".detail";
                isminPrefix = true;
                showDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,-60});
                lastDate = DateUtil.addDay(showDate, new int[]{0,0,0,0,-5});
                break;
            case 30:
                suffix = ".3min";
                isminPrefix = false;
                showDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,-360});
                
                newMin = (minute/3)*3;
                firstDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,newMin-minute});
                lastDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,-390});
                break;
            case 60:
                suffix = ".5min";
                isminPrefix = false;
                showDate = DateUtil.addDay(firstDate, new int[]{0,0,0,-12});
                newMin = (minute/5)*5;
                firstDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,newMin-minute});
                lastDate = DateUtil.addDay(firstDate, new int[]{0,0,0,-13});
                break;
            case 120:
                suffix = ".10min";
                isminPrefix = false;
                showDate = DateUtil.addDay(firstDate, new int[]{0,0,0,-24});
                newMin = (minute/10)*10;
                firstDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,newMin-minute});
                lastDate = DateUtil.addDay(firstDate, new int[]{0,0,0,-26});
                break;
            case 720:
                suffix = ".1h";
                size = 15;
                isminPrefix = false;
                showDate = DateUtil.addDay(firstDate, new int[]{0,0,0,-168});
                
                firstDate = DateUtil.addDay(firstDate, new int[]{0,0,0,0,-minute});
                lastDate = DateUtil.addDay(firstDate, new int[]{0,0,0,-180});
                break;
            case 1440:
                newData = true;
                newDateCount = 15;
                break;
            case 2880:
                newData = true;
                newDateCount = 31;
                break;
          }
        
        List<EcmcVmIndicator> vmList = new ArrayList<>();
        
        Date now = new Date();
        if(newData){
        	String todayStr = format.format(endTime);
            try {
            	firstDate = format.parse(todayStr);
            } catch (ParseException e) {
                log.error(e.getMessage(),e);
            }
            lastDate = DateUtil.addDay(firstDate, new int[]{0,0,-newDateCount});
        	Sort sort = new Sort(Direction.DESC, "timestamp");
            Criteria criatira = new Criteria();
            criatira.andOperator(Criteria.where("vm_id").is(vmId), Criteria.where("timestamp").gt(lastDate), Criteria.where("timestamp").lte(firstDate));
            List<JSONObject> jsonlist = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,prefix+".1d");
            List<JSONObject> jsonlistTwo = new ArrayList<JSONObject>();
            if(!"".equals(prefixTwo)){
            	jsonlistTwo = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,prefixTwo+".1d");
            }
            for(int i = 0;i < newDateCount;i++){
            	EcmcVmIndicator vm = new EcmcVmIndicator();
            	
            	showDate = DateUtil.addDay(lastDate, new int[]{0,0,i});
            	vm.setTimestamp(showDate);
            	boolean isdata = false;
            	Double data = 0d;
            	boolean isdataTwo = false;
            	Double dataTwo = 0d;
            	if(!(jsonlist.isEmpty() || jsonlist.size() == 0)){
            		for(int j = 0 ;j < jsonlist.size();j++){
                        JSONObject json = jsonlist.get(j);
                        if(json.getDate("timestamp").equals(DateUtil.addDay(showDate, new int[]{0,0,1}))){
                        	data = json.getDouble("counter_volume");
                        	isdata = true;
                        	break;
                        }
                    }
            	}
            	if(!(jsonlistTwo.isEmpty() || jsonlistTwo.size() == 0)){
            		for(int j = 0 ;j < jsonlistTwo.size();j++){
                        JSONObject json = jsonlistTwo.get(j);
                        if(json.getDate("timestamp").equals(DateUtil.addDay(showDate, new int[]{0,0,1}))){
                        	dataTwo = json.getDouble("counter_volume");
                        	isdataTwo = true;
                        	break;
                        }
                    }
            	}
            	vm.setMongodb(isdata);
            	vm.setMongodbTwo(isdataTwo);
        		switch(type){
                case "monitorCPU":
                    if(data > 100d){data = 100d;}
                    vm.setCpu(data);
                    break;
                case "monitorRam":
                    if(data > 100d){data = 100d;}
                    vm.setRam(data);
                    break;
                case "monitorDisk":
                    vm.setDiskRead(data);
                    vm.setDiskWrite(dataTwo);
                    break;
                case "monitorNet":
                    vm.setNetIn(data);
                    vm.setNetOut(dataTwo);
                    break;
                case "volumeused":
                    if(data > 100d){data = 100d;}
                    vm.setVolumeUsed(data);
                    break;
              }
            vmList.add(i, vm);
            }
        	
        }else{
        	Sort sort = new Sort(Direction.DESC, "timestamp");
            Criteria criatira = new Criteria();
            criatira.andOperator(Criteria.where("vm_id").is(vmId), Criteria.where("timestamp").gte(lastDate), Criteria.where("timestamp").lte(firstDate));
            List<JSONObject> jsonlist = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,prefix+suffix);
            List<JSONObject> jsonlistTwo = new ArrayList<JSONObject>();
            if(!"".equals(prefixTwo)){
            	jsonlistTwo = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,prefixTwo+suffix);
            }
            
            boolean seconds = now.after(firstDate)&&DateUtil.addDay(now, new int[]{0,0,0,0,0,-20}).before(firstDate);
            
        	for (int i = 0 ;i < size;i++) {
            	EcmcVmIndicator vm = new EcmcVmIndicator();
                if(i == 0){
                    vm.setTimestamp(showDate);      //图表显示时间
                }else{
                    Date nDate = vmList.get(i-1).getTimestamp();    //list上一个对象的显示时间
                    nDate = DateUtil.addDay(nDate, new int[]{0,0,0,0,count});   //加上一个时间间隔
                    vm.setTimestamp(nDate);         //本条对象的显示时间
                }
                int diff = i*count;
                Date diffDate = DateUtil.addDay(lastDate, new int[]{0,0,0,0,diff});
                Date dDate = DateUtil.addDay(lastDate, new int[]{0,0,0,0,diff+count});
                Double data = 0d;
                /**
                 * 记录每个点里包含的最小时间单位中，有几个取到了数据，
                 * 如30min时，每3min一个点，记录用于获取改点数据的三个一分钟里实际取到的是几个
                 */
                int minTnter = 0;
                boolean isdata = false; //是否取到mongDB的数据，mongdb无数据则取左右数据补位，若mongdb数据即为0，则为0即可
                if(!(jsonlist.isEmpty() || jsonlist.size() == 0)){
                    for(int j = 0 ;j < jsonlist.size();j++){
                        JSONObject json = jsonlist.get(j);              //每个数据点取的应该是上一个最小区间的数值
                        if(isminPrefix){
                            if((json.getDate("timestamp").after(diffDate) || json.getDate("timestamp").equals(diffDate))
                                    && json.getDate("timestamp").before(dDate)){
                                data += json.getDouble("counter_volume");
                                isdata = true;
                                minTnter++;
                            }
                        }else{
                            if(json.getDate("timestamp").after(diffDate)
                                    && (json.getDate("timestamp").before(dDate)||json.getDate("timestamp").equals(dDate))){
                                data += json.getDouble("counter_volume");
                                isdata = true;
                                minTnter++;
                            }
                        }
                        
                    }
                }
                Double dataTwo = 0d;
                int minTnterTwo = 0;
                boolean isdataTwo = false; //是否取到mongDB的数据，mongdb无数据则取左右数据补位，若mongdb数据即为0，则为0即可
                if(!(jsonlistTwo.isEmpty() || jsonlistTwo.size() == 0)){
                    for(int j = 0 ;j < jsonlistTwo.size();j++){
                        JSONObject json = jsonlistTwo.get(j);              //每个数据点取的应该是上一个最小区间的数值
                        if(isminPrefix){
                            if((json.getDate("timestamp").after(diffDate) || json.getDate("timestamp").equals(diffDate))
                                    && json.getDate("timestamp").before(dDate)){
                                dataTwo += json.getDouble("counter_volume");
                                isdataTwo = true;
                                minTnterTwo++;
                            }
                        }else{
                            if(json.getDate("timestamp").after(diffDate)
                                    && (json.getDate("timestamp").before(dDate)||json.getDate("timestamp").equals(dDate))){
                                dataTwo += json.getDouble("counter_volume");
                                isdataTwo = true;
                                minTnterTwo++;
                            }
                        }
                        
                    }
                }
                
                if(minTnter != 0){
                    data=data/minTnter;
                }
                if(minTnterTwo != 0){
                    dataTwo=dataTwo/minTnterTwo;
                }
                vm.setMongodb(isdata);
                vm.setMongodbTwo(isdataTwo);
                switch(type){
                    case "monitorCPU":
                        if(seconds){            //当前时间位于离截止时间最近的取数时间的20秒内
                            if(i == size-1){
                                if(data == 0.0){//此时最后的一个数据若为0，则将其左边的数据补上
                                    data = vmList.get(i-1).getCpu();
                                }
                            }
                        }
                        if(data > 100d){data = 100d;}
                        vm.setCpu(data);
                        break;
                    case "monitorRam":
                        if(seconds){
                            if(i == size-1){
                                if(data == 0.0){
                                    data = vmList.get(i-1).getRam();
                                }
                            }
                        }
                        if(data > 100d){data = 100d;}
                        vm.setRam(data);
                        break;
                    case "monitorDisk":
                        if(seconds){
                            if(i == size-1){
                                if(data == 0.0){
                                    data = vmList.get(i-1).getDiskRead();
                                }
                                if(dataTwo == 0.0){
                                	dataTwo = vmList.get(i-1).getDiskWrite();
                                }
                            }
                        }
                        vm.setDiskRead(data);
                        vm.setDiskWrite(dataTwo);
                        break;
                    case "monitorNet":
                        if(seconds){
                            if(i == size-1){
                                if(data == 0.0){
                                    data = vmList.get(i-1).getNetIn();
                                }
                                if(dataTwo == 0.0){
                                	dataTwo = vmList.get(i-1).getNetOut();
                                }
                            }
                        }
                        vm.setNetIn(data);
                        vm.setNetOut(dataTwo);
                        break;
                    case "volumeused":
                        if(seconds){
                            if(i == size-1){
                                if(data == 0.0){
                                    data = vmList.get(i-1).getVolumeUsed();
                                }
                            }
                        }
                        if(data > 100d){data = 100d;}
                        vm.setVolumeUsed(data);
                        break;
                  }
                vmList.add(i, vm);
            }
        }
        for(int i = 0;i < vmList.size();i++){
        	EcmcVmIndicator vmdata = vmList.get(i);
            if(null!=createTime&&createTime.before(vmdata.getTimestamp())&&vmdata.getTimestamp().before(now)){
                
            
                if(!vmdata.isMongodb()){           //如果没有从Mongo里取到数据，则从左右补数
                    switch(type){
                        case "monitorCPU":
                            if(i == 0){
                                vmdata.setCpu(vmList.get(i+1).getCpu());
                            }else if(i > 0 && i < vmList.size() - 1){
                                Double data = (vmList.get(i-1).getCpu()+vmList.get(i+1).getCpu())/2d;
                                vmdata.setCpu(data);
                            }else if(i == vmList.size() - 1){
                                vmdata.setCpu(vmList.get(i-1).getCpu());
                            }
                            break;
                        case "monitorRam":
                            if(i == 0){
                                vmdata.setRam(vmList.get(i+1).getRam());
                            }else if(i > 0 && i < vmList.size() - 1){
                                Double data = (vmList.get(i-1).getRam()+vmList.get(i+1).getRam())/2d;
                                vmdata.setRam(data);
                            }else if(i == vmList.size() - 1){
                                vmdata.setRam(vmList.get(i-1).getRam());
                            }
                            break;
                        case "monitorDisk":
                            if(i == 0){
                                vmdata.setDiskRead(vmList.get(i+1).getDiskRead());
                            }else if(i > 0 && i < vmList.size() - 1){
                                Double data = (vmList.get(i-1).getDiskRead()+vmList.get(i+1).getDiskRead())/2d;
                                vmdata.setDiskRead(data);
                            }else if(i == vmList.size() - 1){
                                vmdata.setDiskRead(vmList.get(i-1).getDiskRead());
                            }
                            break;
                        case "monitorNet":
                            if(i == 0){
                                vmdata.setNetIn(vmList.get(i+1).getNetIn());
                            }else if(i > 0 && i < vmList.size() - 1){
                                Double data = (vmList.get(i-1).getNetIn()+vmList.get(i+1).getNetIn())/2d;
                                vmdata.setNetIn(data);
                            }else if(i == vmList.size() - 1){
                                vmdata.setNetIn(vmList.get(i-1).getNetIn());
                            }
                            break;
                        case "volumeused":
                            if(i == 0){
                                vmdata.setVolumeUsed(vmList.get(i+1).getVolumeUsed());
                            }else if(i > 0 && i < vmList.size() - 1){
                                Double data = (vmList.get(i-1).getVolumeUsed()+vmList.get(i+1).getVolumeUsed())/2d;
                                vmdata.setVolumeUsed(data);
                            }else if(i == vmList.size() - 1){
                                vmdata.setVolumeUsed(vmList.get(i-1).getVolumeUsed());
                            }
                            break;
                      }
                }
                if(!vmdata.isMongodbTwo()){
                	switch(type){
                    case "monitorDisk":
                        if(i == 0){
                            vmdata.setDiskWrite(vmList.get(i+1).getDiskWrite());
                        }else if(i > 0 && i < vmList.size() - 1){
                            Double data = (vmList.get(i-1).getDiskWrite()+vmList.get(i+1).getDiskWrite())/2d;
                            vmdata.setDiskWrite(data);
                        }else if(i == vmList.size() - 1){
                            vmdata.setDiskWrite(vmList.get(i-1).getDiskWrite());
                        }
                        break;
                    case "monitorNet":
                        if(i == 0){
                            vmdata.setNetOut(vmList.get(i+1).getNetOut());
                        }else if(i > 0 && i < vmList.size() - 1){
                            Double data = (vmList.get(i-1).getNetOut()+vmList.get(i+1).getNetOut())/2d;
                            vmdata.setNetOut(data);
                        }else if(i == vmList.size() - 1){
                            vmdata.setNetOut(vmList.get(i-1).getNetOut());
                        }
                        break;
                  }
                }
            }
        }
        
        return vmList;
	}

	@Override
	public List<MonitorMngData> getChartTypes(String parentId) {
		log.info("获取redis主机监控详情图表类别");
		return getMonitorMngData(parentId);
	}

	@Override
	public List<MonitorMngData> getChartTimes(String parentId) {
		log.info("获取redis主机监控详情时间范围");
		return getMonitorMngData(parentId);
	}
	
	/**
	 * 查询mongo基础数据的数据字典
	 * @param parentId
	 * @return
	 */
	public List<MonitorMngData> getMonitorMngData(String parentId) {
		List<MonitorMngData> monitorMngList = new ArrayList<MonitorMngData>();
		Set<String> mngDataSet = null;
		List<String> mngDataList = new ArrayList<String>();
		try {
			mngDataSet = jedisUtil.getSet(RedisKey.SYS_DATA_TREE_PARENT_NODEID+parentId);
			for(String mngData:mngDataSet){
				mngDataList.add(mngData);
			}
			Collections.sort(mngDataList);
			for(String mngData:mngDataList){
				String jsonMng = jedisUtil.get(RedisKey.SYS_DATA_TREE+mngData);
				
				JSONObject monitorMng = JSONObject.parseObject(jsonMng);
				MonitorMngData monitorMngData = new MonitorMngData();
				monitorMngData.setName(monitorMng.getString("nodeName"));
				monitorMngData.setNameEN(monitorMng.getString("nodeNameEn"));
				monitorMngData.setNodeId(monitorMng.getString("nodeId"));
				monitorMngData.setParam1(monitorMng.getString("para1"));
				monitorMngData.setParam2(monitorMng.getString("para2"));
				monitorMngData.setParentId(monitorMng.getString("parentId"));
				
				monitorMngList.add(monitorMngData);
			}
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw new AppException("查询redis数据异常："+parentId);
		}
		return monitorMngList;
	}

	@Override
	public EcmcVmIndicator getvmById(String vmId) {

        List<Object> list = new ArrayList<Object>();
        StringBuffer hql = new StringBuffer(
            "select vm.vm_id,vm.vm_name,cnet.net_name,vm.vm_ip,sdt.node_name "
            + " FROM cloud_vm vm LEFT JOIN cloud_network cnet ON vm.net_id = cnet.net_id LEFT JOIN sys_data_tree sdt ON vm.sys_type = sdt.node_id "
            + " WHERE vm.is_deleted = '0' ");
        hql.append(" and vm.vm_id = ?");
        list.add(vmId);
        javax.persistence.Query query = ecmcMonitorAlarmItemDao.createSQLNativeQuery(hql.toString(), list.toArray());
        EcmcVmIndicator vmIndicator = new EcmcVmIndicator();
        List vmlist = new ArrayList();
        if(null!=query ){
            vmlist = query.getResultList();
        }
        if(null != vmlist&& vmlist.size() == 1){
            Object[] objs = (Object[]) vmlist.get(0);
            vmIndicator.setVmId(String.valueOf(objs[0]));
            vmIndicator.setVmName(String.valueOf(objs[1]));
            vmIndicator.setNetName(String.valueOf(objs[2]));
            vmIndicator.setVmIp(String.valueOf(objs[3]));
            vmIndicator.setOsType(String.valueOf(objs[4]));
        }
        return vmIndicator;
	}
	private Date getVmCreateTime(String vmId){
		List<Object> list = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer(
	            "select vm.create_time  FROM cloud_vm vm "
	            + " WHERE vm.is_deleted = '0'  and vm.vm_id = ?");
        list.add(vmId);
        javax.persistence.Query query = ecmcMonitorAlarmItemDao.createSQLNativeQuery(hql.toString(), list.toArray());
        Date createTime = null;
        if(null!=query && query.getResultList().size() > 0){
            Object obj = (Object) query.getResultList().get(0);
            createTime = (Date)obj;
        }
        return createTime;
	}
}
