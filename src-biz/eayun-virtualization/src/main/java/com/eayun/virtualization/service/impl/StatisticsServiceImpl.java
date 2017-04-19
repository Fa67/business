package com.eayun.virtualization.service.impl;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.ScriptStyle;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.DateUtil;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.virtualization.bean.CloudDetails;
import com.eayun.virtualization.bean.CloudTypes;
import com.eayun.virtualization.dao.CloudVmDao;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.service.StatisticsService;
@Service
@Transactional
public class StatisticsServiceImpl implements StatisticsService {
    
    private static final Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    
    @Autowired
    private CloudVmDao cloudVmDao;
    
    @Autowired
    private CloudVolumeDao volumeDao;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
	private DataCenterService dataCenterService;

    /**
     * 返回云资源信息列表，并做分页
     * @param projectId
     * @param startTime
     * @param endTime
     * @return
     */
	@Override
    public Page getCloudVmResources(Page page ,String dcId , String cusId,Date startTime , Date endTime ,
    		String sort ,String orderBy, QueryMap queryMap) {
        log.info("获取云主机资源列表");
        Date today = new Date();
        if(endTime.after(today)){
            endTime = today;
        }
        List<CloudDetails> detailsList = getCloudVmDetailsList(dcId, cusId,startTime, endTime, sort, orderBy);
        List<CloudDetails> resultList = new ArrayList<CloudDetails>();
        
        int pageSize = queryMap.getCURRENT_ROWS_SIZE();
        int pageNumber = queryMap.getPageNum();
        
        int start = (pageNumber-1)*pageSize;
        if(detailsList.size()>0){
            int end = start+pageSize;
            resultList = detailsList.subList(start, end < detailsList.size()?end:detailsList.size());
        }
        page = new Page(start, detailsList.size(), pageSize, resultList);
        return page;
    }
    /**
     * 查询所有的云主机列表并排序
     * @param dcId
     * @param startTime
     * @param endTime
     * @param sort
     * @param orderBy
     * @return
     */
    public List<CloudDetails> getCloudVmDetailsList(String dcId ,String cusId, Date startTime, Date endTime,String sort ,String orderBy) {
    	StringBuffer inhql = new StringBuffer("SELECT vm.vm_name , vm.create_time , vm.delete_time , vm.vm_id , cf.flavor_vcpus, "
        		+ "cf.flavor_ram, sdt.node_name , vm.is_deleted FROM cloud_vm vm "
                + " LEFT JOIN sys_data_tree sdt ON vm.sys_type = sdt.node_id JOIN cloud_flavor cf ON vm.flavor_id = cf.flavor_id  AND vm.dc_id = cf.dc_id "
                + " JOIN cloud_project cp ON vm.prj_id = cp.prj_id WHERE 1 = 1 and vm.is_visable='1' ");
        List<Object> param = new ArrayList<Object>();
        inhql.append(" and vm.dc_id = ? ");
        param.add(dcId);
        
        inhql.append(" and cp.customer_id = ? ");
        param.add(cusId);
        
        if(startTime.after(new Date())){
        	inhql.append(" and vm.create_time > ? ");
        	param.add(new Date());
        }
        
        inhql.append(" and vm.create_time < ?");
        param.add(endTime);
        
        inhql.append(" AND ((vm.delete_time > ? AND vm.is_deleted ='1') OR vm.is_deleted ='2' OR vm.is_deleted ='0')");
        param.add(startTime);
        
        if("CPU".equals(orderBy)){
        	inhql.append(" ORDER BY cf.flavor_vcpus ");
        }else if("RAM".equals(orderBy)){
        	inhql.append(" ORDER BY cf.flavor_ram ");
        }else if("START".equals(orderBy) || "".equals(orderBy)){
        	inhql.append(" ORDER BY vm.create_time ");
        }
        
        if(!"HOURS".equals(orderBy) && ("DESC".equals(sort) || "".equals(sort))){
        	inhql.append(" DESC ");
        }else if(!"HOURS".equals(orderBy) && "ASC".equals(sort)){
        	inhql.append(" ASC ");
        }
        javax.persistence.Query query = cloudVmDao.createSQLNativeQuery(inhql.toString(), param.toArray());
        //page = cloudVmDao.pagedNativeQuery(insql, queryMap , param.toArray());
        //List inlist = (List) page.getResult();
        
        List<CloudDetails> detailsList = new ArrayList<CloudDetails>();
        List inlist = query.getResultList();
        for (int i = 0; i < inlist.size(); i++) {
            Object[] obj = (Object[]) inlist.get(i);
            CloudDetails details = new CloudDetails();
            
            details.setVmName(String.valueOf(obj[0]));
            Date createTime = (Date) obj[1];
            Date deleteTime = (Date) obj[2];
            String vmId = String.valueOf(obj[3]);
            details.setVmFlavorCpu(String.valueOf(obj[4]));
            details.setVmFlavorRam(String.valueOf(obj[5]));
            details.setVmFlavorSys(String.valueOf(obj[6]));//操作系统，暂不用
            String isDelete = String.valueOf(obj[7]);
            if("2".equals(isDelete)){		//回收站
            	deleteTime = null;
            }
            
            int vmvolCount = volumeDao.getVmCount(vmId);
            details.setVmvolCount(vmvolCount);
            
            long vmHour = 0;
            if(createTime.before(startTime)||createTime.equals(startTime)){
                if(null == deleteTime){
                    vmHour = DateUtil.dayToDay(startTime , endTime);
                    details.setStartTime(startTime);
                    details.setEndTime(endTime);
                }else if(deleteTime.before(startTime)||deleteTime.equals(startTime)){
                    
                    
                }else if(deleteTime.after(startTime)&&deleteTime.before(endTime)){
                    vmHour = DateUtil.dayToDay(startTime , deleteTime);
                    details.setStartTime(startTime);
                    details.setEndTime(deleteTime);
                }else if(deleteTime.equals(endTime)||deleteTime.after(endTime)){
                    vmHour = DateUtil.dayToDay(startTime , endTime);
                    details.setStartTime(startTime);
                    details.setEndTime(endTime);
                }
            }else if(createTime.after(startTime)&&createTime.before(endTime)){
                
                if(null == deleteTime){
                    vmHour = DateUtil.dayToDay(createTime , endTime);
                    details.setStartTime(createTime);
                    details.setEndTime(endTime);
                }else if(deleteTime.before(startTime)||deleteTime.equals(startTime)){
                    
                }else if(deleteTime.after(startTime)&&deleteTime.before(endTime)){
                    vmHour = DateUtil.dayToDay(createTime , deleteTime);
                    details.setStartTime(createTime);
                    details.setEndTime(deleteTime);
                }else if(deleteTime.equals(endTime)||deleteTime.after(endTime)){
                    vmHour = DateUtil.dayToDay(createTime , endTime);
                    details.setStartTime(createTime);
                    details.setEndTime(endTime);
                }
            }else{
            }
            details.setVmHour(vmHour);
            detailsList.add(details);
        }
        if((null != orderBy && "HOURS".equals(orderBy)) && (null != sort && !("".equals(sort)))){
            final String asc = sort;
            Collections.sort(detailsList,new Comparator<CloudDetails>(){
                public int compare(CloudDetails arg0, CloudDetails arg1) {
                	long value0 = 0;
                	long value1 = 0;
                	
                	value0 = arg0.getVmHour();
                	value1 = arg1.getVmHour();
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
    	return detailsList;
    }
    /**
     * 将得到的云硬盘列表分页
     * @param page
     * @param dcId
     * @param startTime
     * @param endTime
     * @param sort
     * @param orderBy
     * @param queryMap
     * @return
     */
    @Override
    public  Page getCloudVolumeResources(Page page ,String dcId, String cusId,Date startTime, Date endTime ,
    		String sort ,String orderBy, QueryMap queryMap) {
        log.info("获取云硬盘资源列表");
        Date today = new Date();
        if(endTime.after(today)){
            endTime = today;
        }
        List<CloudDetails> detailsList = getCloudVolumeDetails(dcId, cusId,startTime, endTime, sort, orderBy);
        List<CloudDetails> resultList = new ArrayList<CloudDetails>();
        
        int pageSize = queryMap.getCURRENT_ROWS_SIZE();
        int pageNumber = queryMap.getPageNum();
        
        int start = (pageNumber-1)*pageSize;
        if(detailsList.size()>0){
            int end = start+pageSize;
            resultList = detailsList.subList(start, end < detailsList.size()?end:detailsList.size());
        }
        page = new Page(start, detailsList.size(), pageSize, resultList);
        
        return page;
    }
    /**
     * 获取所有符合条件的云硬盘列表并排序
     * @param dcId
     * @param startTime
     * @param endTime
     * @param sort
     * @param orderBy
     * @return
     */
    public List<CloudDetails> getCloudVolumeDetails(String dcId , String cusId,Date startTime, Date endTime,String sort ,String orderBy) {
        List<CloudDetails> detailsList = new ArrayList<>();
        StringBuffer inhql = new StringBuffer("SELECT cv.vol_name , cv.vol_size , vm.vm_name , cv.create_time , cv.delete_time , cv.is_deleted  "
                + " FROM cloud_volume cv LEFT JOIN cloud_vm vm ON cv.vm_id = vm.vm_id "
                + " JOIN cloud_project cp ON cv.prj_id = cp.prj_id WHERE 1 = 1  and cv.is_visable='1' ");
        List<Object> param = new ArrayList<Object>();
        
        inhql.append(" AND cv.dc_id = ? ");
        param.add(dcId);
        
        inhql.append(" AND cp.customer_id = ? ");
        param.add(cusId);
        
        if(startTime.after(new Date())){
        	inhql.append(" and cv.create_time > ? ");
        	param.add(new Date());
        }
        
        inhql.append(" AND cv.create_time < ? ");
        param.add(endTime);
        
        inhql.append(" AND ((cv.delete_time > ? AND cv.is_deleted ='1') OR cv.is_deleted ='2' OR cv.is_deleted ='0') ");
        param.add(startTime);

        if("VOL".equals(orderBy)){
        	inhql.append(" ORDER BY cv.vol_size ");
        }else if("START".equals(orderBy) || "".equals(orderBy)){
        	inhql.append(" ORDER BY cv.create_time ");
        }
        
        if(!"HOURS".equals(orderBy) && ("DESC".equals(sort) || "".equals(sort))){
        	inhql.append(" DESC ");
        }else if(!"HOURS".equals(orderBy) && "ASC".equals(sort)){
        	inhql.append(" ASC ");
        }
        
        javax.persistence.Query inquery = cloudVmDao.createSQLNativeQuery(inhql.toString(), param.toArray());
        List inlist = inquery.getResultList();
        long volTimeHours = 0;
        for (int i = 0; i < inlist.size(); i++) {
            Object[] obj = (Object[]) inlist.get(i);
            CloudDetails details = new CloudDetails();
            
            details.setVolumeName(String.valueOf(obj[0]));
            
            details.setVolumeSize(Integer.parseInt(String.valueOf(obj[1])));
            details.setVolvmName(null == obj[2]?null:String.valueOf(obj[2]));
            Date createTime = (Date) obj[3];
            Date deleteTime = (Date) obj[4];
            String isDelete = String.valueOf(obj[5]);
            if("2".equals(isDelete)){		//回收站
            	deleteTime = null;
            }
            
            long volHour = 0;
            if(createTime.before(startTime)||createTime.equals(startTime)){
                if(null == deleteTime){
                    volHour = DateUtil.dayToDay(startTime , endTime);
                    details.setStartTime(startTime);
                    details.setEndTime(endTime);
                }else if(deleteTime.before(startTime)||deleteTime.equals(startTime)){
                    
                }else if(deleteTime.after(startTime)&&deleteTime.before(endTime)){
                    volHour = DateUtil.dayToDay(startTime , deleteTime);
                    details.setStartTime(startTime);
                    details.setEndTime(deleteTime);
                }else if(deleteTime.equals(endTime)||deleteTime.after(endTime)){
                    volHour = DateUtil.dayToDay(startTime , endTime);
                    details.setStartTime(startTime);
                    details.setEndTime(endTime);
                }
            }else if(createTime.after(startTime)&&createTime.before(endTime)){
                if(null == deleteTime){
                    volHour = DateUtil.dayToDay(createTime , endTime);
                    details.setStartTime(createTime);
                    details.setEndTime(endTime);
                }else if(deleteTime.before(startTime)||deleteTime.equals(startTime)){
                    
                }else if(deleteTime.after(startTime)&&deleteTime.before(endTime)){
                    volHour = DateUtil.dayToDay(createTime , deleteTime);
                    details.setStartTime(createTime);
                    details.setEndTime(deleteTime);
                }else if(deleteTime.equals(endTime)||deleteTime.after(endTime)){
                    volHour = DateUtil.dayToDay(createTime , endTime);
                    details.setStartTime(createTime);
                    details.setEndTime(endTime);
                }
            }else{
            }
            volTimeHours = volTimeHours + volHour;
            details.setVolHour(volHour);
            detailsList.add(details);
        }
        if((null != orderBy && "HOURS".equals(orderBy)) && (null != sort && !("".equals(sort)))){
            final String asc = sort;
            Collections.sort(detailsList,new Comparator<CloudDetails>(){
                public int compare(CloudDetails arg0, CloudDetails arg1) {
                	long value0 = 0;
                	long value1 = 0;
                	
                	value0 = arg0.getVolHour();
                	value1 = arg1.getVolHour();
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
        return detailsList;
    }
    @Override
    public CloudTypes getNet(String projectId, Date startTime, Date endTime) {
        log.info("获取网络流量统计资源列表");
        SimpleDateFormat formatss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat mat = new SimpleDateFormat("HH:mm:ss");
        Date now = new Date();
        Date today = new Date();
        CloudTypes type = new CloudTypes();
        String fName = "no";
        String uName = "no";
        String startStr = format.format(startTime);
        String endStr = format.format(endTime);
        String todayStr = format.format(today);
        Date start = null;
        Date end = null;
        Sort sort = new Sort(Direction.DESC, "timestamp");
        Criteria criatira = new Criteria();
        DecimalFormat df = new DecimalFormat("0");
        try {
            start = format.parse(startStr);
            end = format.parse(endStr);
            today = format.parse(todayStr);
        } catch (ParseException e) {
            log.error("网络流量统计资源：日期转换错误",e);
            log.error(e.toString(),e);
        }
        if(start!=null && start.equals(end)){  //所选时间范围在同一天
            criatira.andOperator(Criteria.where("project_id").is(projectId), Criteria.where("timestamp").gte(startTime), Criteria.where("timestamp").lt(endTime));
            List<JSONObject> upjsonlist = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,MongoCollectionName.BANDWIDTH_NETWORK_OUTGOING_DETAIL);
            List<JSONObject> downjsonlist = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,MongoCollectionName.BANDWIDTH_NETWORK_INCOMING_DETAIL);
            double updata = 0d;
            double downdata = 0d;
            String vm = "yes";
            String vml = "yes";
            if(!(upjsonlist.isEmpty() || upjsonlist.size() == 0)){
                for(int j = 0 ;j < upjsonlist.size();j++){
                    JSONObject json = upjsonlist.get(j);
                    updata += json.getDouble("counter_volume");
                }
            }else{
                vm = "no";
            }
            updata = Double.parseDouble(df.format(updata));
            if(!(downjsonlist.isEmpty() || downjsonlist.size() == 0)){
                for(int j = 0 ;j < downjsonlist.size();j++){
                    JSONObject json = downjsonlist.get(j);
                    downdata += json.getDouble("counter_volume");
                }
            }else{
                vml = "no";
            }
            downdata = Double.parseDouble(df.format(downdata));
            type.setNetUpFlowCount(updata);
            type.setNetDownFlowCount(downdata);
            List<CloudDetails> dlist = new ArrayList<CloudDetails>();
            CloudDetails details = new CloudDetails();
            details.setUpCount(updata);
            details.setDownCount(downdata);
            String strStart = formatss.format(startTime);
            String str = mat.format(endTime);
            details.setEveryDate(strStart+"--"+str);
            details.setVmName(vm);       //使用云主机名称表示上行流量是否从mongo采集到
            details.setVolumeName(vml);  //使用硬盘名称表示下行流量是否从mongo采集到
            dlist.add(details);
            
            type.setVmFlavorName(vm);    //云主机：上行
            type.setVolumeTypeNmae(vml);  //云硬盘：下行
            type.setDetailsList(dlist);
            return type;
        }else if(start!=null && start.after(end)){ //开始时间日期大于结束时间日期（此种情况不可能存在）
            
        }else{
            double updata = 0d;
            double downdata = 0d;
            List<CloudDetails> dlist = new ArrayList<CloudDetails>();
            
            criatira.andOperator(Criteria.where("project_id").is(projectId), Criteria.where("timestamp").gte(startTime), 
                Criteria.where("timestamp").lt(DateUtil.addDay(start, new int[]{0,0,1})));
            List<JSONObject> upFirstjson = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,MongoCollectionName.BANDWIDTH_NETWORK_OUTGOING_DETAIL);
            List<JSONObject> downFirstjson = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,MongoCollectionName.BANDWIDTH_NETWORK_INCOMING_DETAIL);
            
            double upcount = 0d;
            double downcount = 0d;
            String vm = "yes";
            String vml = "yes";
            if(!(upFirstjson.isEmpty() || upFirstjson.size() == 0)){
                for(int j = 0 ;j < upFirstjson.size();j++){
                    JSONObject json = upFirstjson.get(j);
                    upcount += json.getDouble("counter_volume");
                }
            }else{
                vm = "no";
            }
            updata += upcount;
            upcount = Double.parseDouble(df.format(upcount));
            if(!(downFirstjson.isEmpty() || downFirstjson.size() == 0)){
                for(int j = 0 ;j < downFirstjson.size();j++){
                    JSONObject json = downFirstjson.get(j);
                    downcount += json.getDouble("counter_volume");
                }
            }else{
                vml = "no";
            }
            downdata += downcount;
            downcount = Double.parseDouble(df.format(downcount));
            
            CloudDetails detail = new CloudDetails();
            detail.setUpCount(upcount);
            detail.setDownCount(downcount);
            String str = formatss.format(startTime);
            detail.setEveryDate(str+"--23:59:59");
            detail.setVmName(vm);       //使用云主机名称表示上行流量是否从mongo采集到
            detail.setVolumeName(vml);  //使用硬盘名称表示下行流量是否从mongo采集到
            dlist.add(0, detail);
            
            int i = 1;
            while(!(DateUtil.addDay(start, new int[]{0,0,i}).equals(end)||DateUtil.addDay(start, new int[]{0,0,i}).after(end))){
                CloudDetails details = new CloudDetails();
                String vmmidd = "yes";
                String vmlmidd = "yes";
                Criteria middcriatira = new Criteria();
                
                if(!today.equals(DateUtil.addDay(start, new int[]{0,0,i}))){//当前日期不在按照整天查询内
                    Sort middsort = new Sort(Direction.DESC, "date");
                    middcriatira.andOperator(Criteria.where("project_id").is(projectId), Criteria.where("date").is(DateUtil.addDay(start, new int[]{0,0,i})));
                    JSONObject upjson = mongoTemplate.findOne(new Query(middcriatira).with(middsort), JSONObject.class,"bandwidth.network.outgoing.summary");
                    JSONObject downjson = mongoTemplate.findOne(new Query(middcriatira).with(middsort), JSONObject.class,"bandwidth.network.incoming.summary");
                    double upmidcount = 0d;
                    double downmidcount = 0d;
                    
                    if( null != upjson && !upjson.isEmpty()){
                        upmidcount = upjson.getDouble("counter_volume");
                    }else{
                        vmmidd = "no";
                    }
                    updata += upmidcount;
                    upmidcount = Double.parseDouble(df.format(upmidcount));
                    if( null != downjson && !downjson.isEmpty()){
                        downmidcount = downjson.getDouble("counter_volume");
                    }else{
                        vmlmidd = "no";
                    }
                    downdata += downmidcount;
                    downmidcount = Double.parseDouble(df.format(downmidcount));
                    
                    details.setUpCount(upmidcount);
                    details.setDownCount(downmidcount);
                }else{
                    middcriatira.andOperator(Criteria.where("project_id").is(projectId), Criteria.where("timestamp").gte(today), 
                        Criteria.where("timestamp").lt(now));
                    List<JSONObject> upTodayjson = mongoTemplate.find(new Query(middcriatira).with(sort), JSONObject.class,MongoCollectionName.BANDWIDTH_NETWORK_OUTGOING_DETAIL);
                    List<JSONObject> downTodayjson = mongoTemplate.find(new Query(middcriatira).with(sort), JSONObject.class,MongoCollectionName.BANDWIDTH_NETWORK_INCOMING_DETAIL);
                    
                    double upTodaycount = 0d;
                    double downTodaycount = 0d;
                    if(!(upTodayjson.isEmpty() || upTodayjson.size() == 0)){
                        for(int j = 0 ;j < upTodayjson.size();j++){
                            JSONObject json = upTodayjson.get(j);
                            upTodaycount += json.getDouble("counter_volume");
                        }
                    }else{
                        vmmidd = "no";
                    }
                    updata += upTodaycount;
                    upTodaycount = Double.parseDouble(df.format(upTodaycount));
                    if(!(downTodayjson.isEmpty() || downTodayjson.size() == 0)){
                        for(int j = 0 ;j < downTodayjson.size();j++){
                            JSONObject json = downTodayjson.get(j);
                            downTodaycount += json.getDouble("counter_volume");
                        }
                    }else{
                        vmlmidd = "no";
                    }
                    downdata += downTodaycount;
                    downTodaycount = Double.parseDouble(df.format(downTodaycount));
                    
                    details.setUpCount(upTodaycount);
                    details.setDownCount(downTodaycount);
                    
                }
                String Strss = formatss.format(DateUtil.addDay(start, new int[]{0,0,i}));
                details.setEveryDate(Strss+"--23:59:59");
                details.setVmName(vmmidd);       //使用云主机名称表示上行流量是否从mongo采集到
                details.setVolumeName(vmlmidd);  //使用硬盘名称表示下行流量是否从mongo采集到
                
                dlist.add(i,details);
                i++;
            }
            Sort lastsort = new Sort(Direction.DESC, "timestamp");
            Criteria lastcriatira = new Criteria();
            lastcriatira.andOperator(Criteria.where("project_id").is(projectId), Criteria.where("timestamp").gte(end), Criteria.where("timestamp").lt(endTime));
            List<JSONObject> upLastjson = mongoTemplate.find(new Query(lastcriatira).with(lastsort), JSONObject.class,MongoCollectionName.BANDWIDTH_NETWORK_OUTGOING_DETAIL);
            List<JSONObject> downLastjson = mongoTemplate.find(new Query(lastcriatira).with(lastsort), JSONObject.class,MongoCollectionName.BANDWIDTH_NETWORK_INCOMING_DETAIL);
            
            double upLastcount = 0d;
            double downLastcount = 0d;
            String vmlast = "yes";
            String vmllast = "yes";
            if(!(upLastjson.isEmpty() || upLastjson.size() == 0)){
                for(int j = 0 ;j < upLastjson.size();j++){
                    JSONObject json = upLastjson.get(j);
                    upLastcount += json.getDouble("counter_volume");
                }
            }else{
                vmlast = "no";
            }
            updata += upLastcount;
            upLastcount = Double.parseDouble(df.format(upLastcount));
            if(!(downLastjson.isEmpty() || downLastjson.size() == 0)){
                for(int j = 0 ;j < downLastjson.size();j++){
                    JSONObject json = downLastjson.get(j);
                    downLastcount += json.getDouble("counter_volume");
                }
            }else{
                vmllast = "no";
            }
            downdata += downLastcount;
            downLastcount = Double.parseDouble(df.format(downLastcount));
            CloudDetails det = new CloudDetails();
            det.setUpCount(upLastcount);
            det.setDownCount(downLastcount);
            String strss = formatss.format(end);
            String endStrss = mat.format(endTime);
            det.setEveryDate(strss+"--"+endStrss);
            det.setVmName(vmlast);       //使用云主机名称表示上行流量是否从mongo采集到
            det.setVolumeName(vmllast);  //使用硬盘名称表示下行流量是否从mongo采集到
            dlist.add(i, det);
            for(CloudDetails dd:dlist){
                if(dd.getVmName().equals("yes")){
                    fName = "yes";
                    break;
                }
            }
            for(CloudDetails dd:dlist){
                if(dd.getVolumeName().equals("yes")){
                    uName = "yes";
                    break;
                }
            }
            updata = Double.parseDouble(df.format(updata));
            downdata = Double.parseDouble(df.format(downdata));
            
            type.setVmFlavorName(fName);    //云主机：上行
            type.setVolumeTypeNmae(uName);  //云硬盘：下行
            type.setNetUpFlowCount(updata);
            type.setNetDownFlowCount(downdata);
            type.setDetailsList(dlist);
            return type;
        }
        return null;
    }
    //获取符合条件的云主机个数
    private int getvmTypeCount(String projectId, Date startTime,Date endTime){
        List<Integer> countList = new ArrayList<>();
        int count = 0;
        Date today = new Date();
        if(endTime.after(today)){
            endTime = today;
        }
        StringBuffer hql = new StringBuffer("SELECT count(*) FROM BaseCloudVm vm WHERE 1 = 1 ");
        List<Object> paramList = new ArrayList<Object>();
        
        hql.append(" and vm.prjId = ? ");
        paramList.add(projectId);
        
        if(null != startTime && startTime.after(today)){
            hql.append(" and vm.createTime > ? ");
            paramList.add(today);
        }
        if (null != endTime) {
            hql.append(" and vm.createTime < ?");
            paramList.add(endTime);
        }
        if (null != startTime) {
            hql.append(" AND ((vm.deleteTime > ? AND vm.isDeleted ='1') OR vm.isDeleted ='2' OR vm.isDeleted ='0')");
            paramList.add(startTime);
        }
        String sql = hql.toString();
        countList = cloudVmDao.find(sql, paramList.toArray());
        Number num = (Number)countList.get(0);
        count = num.intValue();
        return count;
    }
    //获取符合条件的云硬盘个数
    private int getvolTypeCount(String prjId, Date startTime,Date endTime){
        List<Integer> countList = new ArrayList<>();
        int count = 0;
        Date today = new Date();
        if(endTime.after(today)){
            endTime = today;
        }
        StringBuffer hql = new StringBuffer("SELECT count(volId) FROM BaseCloudVolume cv where 1 = 1 ");
        List<Object> paramList = new ArrayList<Object>();
        hql.append(" and cv.prjId = ? ");
        paramList.add(prjId);
        
        if(null != startTime && startTime.after(today)){
            hql.append(" and cv.createTime > ? ");
            paramList.add(today);
        }
        if (null != endTime) {
            hql.append(" and cv.createTime < ?");
            paramList.add(endTime);
        }
        if (null != startTime) {
            hql.append(" AND ((cv.deleteTime > ? AND cv.isDeleted ='1') OR cv.isDeleted ='2' OR cv.isDeleted ='0')");
            paramList.add(startTime);
        }
        String sql = hql.toString();
        countList = volumeDao.find(sql, paramList.toArray());
        Number num = (Number)countList.get(0);
        count = num.intValue();
        return count;
    }
    public int getnetTypeCount(String projectId, Date startTime,Date endTime){
        int count = 0;
        Sort sort = new Sort(Direction.DESC, "timestamp");
        Criteria criatira = new Criteria(); 
        criatira.andOperator(Criteria.where("project_id").is(projectId), Criteria.where("timestamp").gte(startTime), Criteria.where("timestamp").lt(endTime));
        List<JSONObject> upjsonlist = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,MongoCollectionName.BANDWIDTH_NETWORK_OUTGOING_DETAIL);
        List<JSONObject> downjsonlist = mongoTemplate.find(new Query(criatira).with(sort), JSONObject.class,MongoCollectionName.BANDWIDTH_NETWORK_INCOMING_DETAIL);
        
        if(!(upjsonlist.isEmpty() || upjsonlist.size() == 0)){
            count = 1;
        }
        if(!(downjsonlist.isEmpty() || downjsonlist.size() == 0)){
            count = 1;
        }
        return count;
    }
    @Override
    public boolean getResourcesForExcel(String dcId,String projectId, Date startTime,Date endTime) {
        int vmTypeCount = getvmTypeCount(projectId, startTime, endTime);
        int volTypeCount = getvolTypeCount(projectId, startTime, endTime);
        int netTypeCount = getnetTypeCount(projectId, startTime,endTime);
        if(vmTypeCount==0&&volTypeCount==0&&netTypeCount==0){
            return false;
        }
        return true;
    }
    @Override
    public void exportSheets(OutputStream os,String dcId , String cusId,String projectId, Date startTime,
    		Date endTime , String sort ,String orderBy,String sortVol ,String orderByVol)throws Exception{
    	
    	List<CloudDetails> vmDetailList =  getCloudVmDetailsList(dcId, cusId,startTime, endTime,sort,orderBy);
    	List<CloudDetails> volDetailList = getCloudVolumeDetails(dcId, cusId,startTime, endTime,sortVol,orderByVol);
    	
    	List<CloudTypes> vmTypeList =  groupByVmList(vmDetailList);
    	List<CloudTypes> volTypeList = groupByVolList(volDetailList);
    	
        CloudTypes netType = getNet(projectId, startTime, endTime);
        try{
            WritableWorkbook workbook = Workbook.createWorkbook(os);
            String[] sheet = {"网络","云硬盘","云主机","汇总"};
            String[] vmHead = {"CPU：","内存：","数量：","累计时长："};
            String[] volHead = {"空间：","数量：","累计时长："};
            String[] netHead = {"公网网络","上行流量累计:","下行流量累计:"};
            
            String[] vmTitle = {"云主机名称","CPU","内存","挂接云硬盘数量","开始时间","截止时间","累计时长"};
            String[] volTitle = {"云硬盘名称","空间","挂接云主机名称","开始时间","截止时间","累计时长"};
            String[] netTitle = {"时间","上行流量","下行流量"};
            for(int i = 0; i < sheet.length;i++){
                WritableSheet ws = workbook.createSheet(sheet[i], 0);
                Label label = new Label(0, 0, sheet[i], setTitleStyle(18,"1"));
                ws.addCell(label);      //标题(如汇总、云主机、网络等)
                CellView cellView = new CellView();
                cellView.setAutosize(true); //宽度自适应
                if(i == 3){//汇总
                    ws.mergeCells(0, 0, 3, 0);//汇总第一行合并列
                    ws.setRowView(0, 600);   //第一行高度
                    int row = 0;
                    if(vmTypeList.size() > 0){
                        Label labeltype = new Label(0, row+1, "云主机", setTitleStyle(16,"2"));
                        ws.addCell(labeltype);
                        ws.setRowView(row+1, 400);
                        ws.mergeCells(0, row+1, 3, row+1);
                        for(int k = 0;k < vmTypeList.size();k++){
                            CloudTypes vmType = vmTypeList.get(k);
                            for(int j = 0;j < vmHead.length;j++){
                            	int width = 20;
                            	String content = vmHead[j];
                            	switch(j){
                                case 0:
                                	content = content+vmType.getVmFlavorCpu()+"核";
                                    break;
                                case 1:
                                	content = content+vmType.getVmFlavorRam()+"M";
                                    break;
                                case 2:
                                	content = content+vmType.getVmCount()+"台";
                                    break;
                                case 3:
                                	content = content+vmType.getVmTimeHours()+"小时";
                                	width = 30;
                                    break;
                              }
                            Label header = new Label(j, row+k+2, content, setSumStyle(12, false));
                            ws.setColumnView(j, width);
                            ws.setRowView(row+k+2,500);
                            ws.addCell(header);
                            }
                            
                        }
                        row = row + vmTypeList.size()+1;
                    }
                    //ws.mergeCells(0, row, 6, 0);
                    if(volTypeList.size() > 0){
                        Label labeltype = new Label(0, row+1, "云硬盘", setTitleStyle(16,"2"));
                        ws.addCell(labeltype);
                        ws.setRowView(row+1, 400);
                        ws.mergeCells(0, row+1, 3, row+1);
                        for(int k = 0;k < volTypeList.size();k++){
                            CloudTypes volType = volTypeList.get(k);
                            for(int j = 0;j < volHead.length;j++){
                            	int width = 20;
                            	String content = volHead[j];
                            	switch(j){
                                case 0:
                                	content = content+volType.getVolumeSize()+"G";
                                    break;
                                case 1:
                                	content = content+volType.getVolumeCount()+"块";
                                    break;
                                case 2:
                                	content = content+volType.getVolTimeHours()+"小时";
                                	width = 30;
                                    break;
                              }
                            Label header = new Label(j, row+k+2, content, setSumStyle(12, false));
                            ws.setColumnView(j, width);
                            ws.setRowView(row+k+2,500);
                            ws.addCell(header);
                            }
                            
                        }
                        row = row + volTypeList.size()+1;
                    }
                    //ws.mergeCells(0, row, 6, 0);
                    if(null != netType){//网络
                        Label labeltype = new Label(0, row+1, "网络", setTitleStyle(16,"2"));
                        ws.addCell(labeltype);
                        ws.setRowView(row+1, 400);
                        ws.mergeCells(0, row+1, 3, row+1);
                        
                        StringBuffer netHeads = new StringBuffer();
                        netHeads.append(netHead[0]+"        ");
                        netHeads.append(netHead[1]+(netType.getVolumeTypeNmae().equals("yes")?Math.round(netType.getNetDownFlowCount())+"MB        ":"--"));
                        netHeads.append(netHead[2]+(netType.getVmFlavorName().equals("yes")?Math.round(netType.getNetUpFlowCount())+"MB        ":"--"));
                        Label typeheader = new Label(0, row+2, netHeads.toString(), setSumStyle(12, false));
                        ws.addCell(typeheader);
                        ws.setRowView(row+2,500);
                        ws.mergeCells(0, row+2, 4 - 1, row+2);
                        
                    }
                }else if(i == 2){//云主机
                    ws.mergeCells(0, 0, 6, 0);
                    ws.setRowView(0, 400);   //第一行高度
                    int row = 0;
                    if(vmDetailList.size() > 0){
                    	for(int j = 0;j < vmTitle.length;j++){      //每种类型内列表表头
                            Label header = new Label(j, row+1, vmTitle[j], setStyle(11, true , "2"));
                            int width = 30;
                            if(j == 1 || j == 2 || j== 6){
                            	width = 15;
                            }else if(j == 3){
                            	width = 20;
                            }
                            ws.setColumnView(j, width);    //设置表头列的宽度
                            ws.setRowView(row+1,550);
                            ws.addCell(header);
                        }
                        for(int j = 0; j < vmDetailList.size(); j++){
                        	CloudDetails details = vmDetailList.get(j);
                                for(int o = 0;o < vmTitle.length;o++){  //每一列
                                    String cString = "";
                                    switch(o){
                                      case 0:
                                          cString = details.getVmName();
                                          break;
                                      case 1:
                                          cString = details.getVmFlavorCpu()+"核";
                                          break;
                                      case 2:
                                          cString = details.getVmFlavorRam()+"M";
                                          break;
                                      case 3:
                                          cString = String.valueOf(details.getVmvolCount());
                                          break;
                                      case 4:
                                          cString = DateUtil.dateToStringTwo(details.getStartTime());
                                          break;
                                      case 5:
                                          cString = DateUtil.dateToStringTwo(details.getEndTime());
                                          break;
                                      case 6:
                                          cString = String.valueOf(details.getVmHour());
                                          break;
                                    }
                                    Label content  = new Label(o,row+j+2,cString,setStyle(12,false , "3"));
                                    ws.addCell(content); 
                                }
                        }
                    }
                }else if(i == 1){//云硬盘
                    ws.mergeCells(0, 0, 5, 0);
                    ws.setRowView(0, 400);   //第一行高度
                    if(volDetailList.size() > 0){
                        int row = 0;
                        for(int j = 0;j < volTitle.length;j++){      //每种类型内列表表头
                            Label header = new Label(j, row+1, volTitle[j], setStyle(11, true , "2"));
                            //ws.setColumnView(j, cellView);//根据内容自动设置列宽 
                            int width = 30;
                            if(j == 1 || j == 5){
                            	width = 15;
                            }else if(j == 0){
                            	width = 45;
                            }
                            ws.setColumnView(j, width);    //设置表头列的宽度
                            ws.setRowView(row+1,550);
                            ws.addCell(header);
                        }
                        for(int j = 0; j < volDetailList.size(); j++){
                        	CloudDetails details = volDetailList.get(j);
                                for(int o = 0;o < volTitle.length;o++){  //每一列
                                    String cString = "";
                                    switch(o){
                                      case 0:
                                          cString = details.getVolumeName();
                                          break;
                                      case 1:
                                          cString = String.valueOf(details.getVolumeSize())+"G";
                                          break;
                                      case 2:
                                          cString = (null==details.getVolvmName()||details.getVolvmName().equals("null")?"":details.getVolvmName());
                                          break;
                                      case 3:
                                          cString = DateUtil.dateToStringTwo(details.getStartTime());
                                          break;
                                      case 4:
                                          cString = DateUtil.dateToStringTwo(details.getEndTime());
                                          break;
                                      case 5:
                                          cString = String.valueOf(details.getVolHour());
                                          break;
                                    }
                                    Label content  = new Label(o,row+j+2,cString,setStyle(12,false , "3"));
                                    //ws.setColumnView(o, cellView);
                                    ws.addCell(content); 
                                }
                        }
                    }
                }else if(i == 0){//网络
                    ws.mergeCells(0, 0, 5, 0);
                    ws.setRowView(0, 400);   //第一行高度
                    if(null != netType){
                        int row = 0;
                        StringBuffer netHeads = new StringBuffer();
                        netHeads.append(netHead[0]+"    ");
                        netHeads.append(netHead[1]+(netType.getVolumeTypeNmae().equals("yes")?Math.round(netType.getNetDownFlowCount())+"MB          ":"--"));
                        netHeads.append(netHead[2]+(netType.getVmFlavorName().equals("yes")?Math.round(netType.getNetUpFlowCount())+"MB":"--"));
                        Label typeheader = new Label(0, row+1, netHeads.toString(), setStyle(16, false , "1"));
                        ws.addCell(typeheader);
                        ws.mergeCells(0, row+1, 6 - 1, row+1);
                        ws.setRowView(row+1, 500);
                        List<CloudDetails> netDdtailsList = netType.getDetailsList();
                        for(int j = 0;j < netTitle.length;j++){      //每种类型内列表表头
                            Label header = new Label(2*j, row+2, netTitle[j], setStyle(11, true , "2"));
                             
                            //ws.setColumnView(j, cellView);//根据内容自动设置列宽 
                            ws.mergeCells(2*j, row+2, 2*(j+1)-1, row+2);
                            ws.setColumnView(j, 20);    //设置表头列的宽度
                            ws.setRowView(row+2,550);
                            ws.addCell(header);
                        }
                        for (int j = 0; j < netDdtailsList.size(); j++) {//每种类型内数据列表     每一行
                            CloudDetails details = netDdtailsList.get(netDdtailsList.size()-j-1);
                            for(int o = 0;o < netTitle.length;o++){  //每一列
                                String cString = "";
                                switch(o){
                                  case 0:
                                      cString = details.getEveryDate();
                                      break;
                                  case 1:
                                      if(details.getVolumeName().equals("no")){
                                          cString = String.valueOf("--");
                                      }else{
                                          cString = String.valueOf(Math.round(details.getDownCount()))+"MB";
                                      }
                                      break;
                                  case 2:
                                      if(details.getVmName().equals("no")){
                                          cString = String.valueOf("--");
                                      }else{
                                          cString = String.valueOf(Math.round(details.getUpCount()))+"MB";
                                      }
                                      break;
                                }
                                Label content  = new Label(2*o,row+j+3,cString,setStyle(12,false , "3"));
                                ws.mergeCells(2*o, row+j+3, 2*(o+1)-1, row+j+3);
                                ws.setColumnView(o, 20);
                                ws.setColumnView(o+3, 20);
                                //ws.setColumnView(o, cellView);
                                ws.addCell(content); 
                            }
                        }
                        Label diff = new Label(0, row + netDdtailsList.size()+4, "", setTitleStyle(18,"2"));
                        ws.addCell(diff);
                        ws.setRowView(row + netDdtailsList.size()+4 , 200);
                        row = row + netDdtailsList.size() + 3;
                    }
                }
            }
            workbook.write();
            workbook.close(); 
        }catch (Exception ex) {
            log.error("导出excel失败", ex);
            throw ex;
        }
    }
    private List<CloudTypes> groupByVmList(List<CloudDetails> list) {  
        List<CloudTypes> groupbyList = new ArrayList<CloudTypes>();  
        Map<String, CloudTypes> map = new HashMap<String, CloudTypes>();  
          
        for (CloudDetails detail : list) {  
            if (null != detail.getVmFlavorCpu() && null != detail.getVmFlavorRam() ) {
            	String cpu = detail.getVmFlavorCpu();
            	String ram = detail.getVmFlavorRam();
                String key = cpu + ":"  + ram;  
                if (map.containsKey(key)) {
                	map.get(key).setVmCount(map.get(key).getVmCount()+1);
                	Long hours = detail.getVmHour() + map.get(key).getVmTimeHours();
                	map.get(key).setVmTimeHours(hours);
                    map.put(key, map.get(key));  
                } else {
                	CloudTypes type = new CloudTypes();
                	type.setVmTimeHours(detail.getVmHour());
                	type.setVmCount(1);
                    map.put(key, type);  
                }  
            }  
        }  
        for (Map.Entry<String, CloudTypes> entry : map.entrySet()) {  
            String[] names = StringUtils.splitByWholeSeparator(entry.getKey(), ":");  
            CloudTypes type = new CloudTypes();  
            type.setVmFlavorCpu(names[0]);
            type.setVmFlavorRam(names[1]);
            type.setVmCount(entry.getValue().getVmCount());
            type.setVmTimeHours(entry.getValue().getVmTimeHours());
            groupbyList.add(type);  
        }  
        Collections.sort(groupbyList,new Comparator<CloudTypes>(){
            public int compare(CloudTypes arg0, CloudTypes arg1) {
            	int result = 0;
            	result = Integer.valueOf(arg0.getVmFlavorCpu()).compareTo(Integer.valueOf(arg1.getVmFlavorCpu()));
            	if(result == 0){
            		result = Integer.valueOf(arg0.getVmFlavorRam()).compareTo(Integer.valueOf(arg1.getVmFlavorRam()));
            	}
                return result;
            }
        });
        return groupbyList;  
    }
    private List<CloudTypes> groupByVolList(List<CloudDetails> list) {  
        List<CloudTypes> groupbyList = new ArrayList<CloudTypes>();  
        Map<String, CloudTypes> map = new HashMap<String, CloudTypes>();  
          
        for (CloudDetails detail : list) {
        	int size = detail.getVolumeSize();
            String key = String.valueOf(size);  
            if (map.containsKey(key)) {
            	map.get(key).setVolumeCount(map.get(key).getVolumeCount()+1);
            	Long hours = detail.getVolHour() + map.get(key).getVolTimeHours();
            	map.get(key).setVolTimeHours(hours);
                map.put(key, map.get(key));  
            } else {
            	CloudTypes type = new CloudTypes();
            	type.setVolTimeHours(detail.getVolHour());
            	type.setVolumeCount(1);
                map.put(key, type);  
            }
        }  
        for (Map.Entry<String, CloudTypes> entry : map.entrySet()) {  
            String size = entry.getKey();  
            CloudTypes type = new CloudTypes();  
            type.setVolumeSize(Integer.valueOf(size));
            type.setVolumeCount(entry.getValue().getVolumeCount());
            type.setVolTimeHours(entry.getValue().getVolTimeHours());
            groupbyList.add(type);  
        }  
        Collections.sort(groupbyList,new Comparator<CloudTypes>(){
            public int compare(CloudTypes arg0, CloudTypes arg1) {
            	int value0 = 0;
            	int value1 = 0;
            	value0 = arg0.getVolumeSize();
            	value1 = arg1.getVolumeSize();
            	int result = 0;
            	result = new Integer(value0).compareTo(new Integer(value1));
                return result;
            }
        });
        return groupbyList;  
    }  
    /**
     * 汇总页每行样式
     * @param fontSize
     * @param bold
     * @return
     * @throws Exception
     */
    private  WritableCellFormat setSumStyle(int fontSize,boolean bold) throws Exception {
        WritableFont he = new WritableFont(WritableFont.createFont("宋体"),// 字体
                fontSize,               // 字号
                WritableFont.NO_BOLD,   // 粗体
                false,                  // 斜体
                UnderlineStyle.NO_UNDERLINE, // 下划线
                Colour.BLACK, // 字体颜色
                ScriptStyle.NORMAL_SCRIPT);
        if (bold) {
            he.setBoldStyle(WritableFont.BOLD);//粗体
        }
        WritableCellFormat wcf = new WritableCellFormat(he);
        wcf.setAlignment(Alignment.LEFT); // 设置对齐方式
        wcf.setBorder(Border.NONE, BorderLineStyle.THIN);//边框
        //wcf.setBackground(Colour.GRAY_25);
        wcf.setVerticalAlignment(VerticalAlignment.CENTRE);//垂直居中
        return wcf;
    }
    
    /**
     * 第一行标题样式
     * 汇总（云主机、云硬盘）
     * 云主机
     * 云硬盘
     * @return
     * @throws Exception
     */
    private WritableCellFormat setTitleStyle(int size , String level) throws Exception {
        WritableFont he = new WritableFont(
                WritableFont.createFont("宋体"),// 字体
                size,                 // 字号
                WritableFont.BOLD,  // 粗体
                false,              // 斜体
                UnderlineStyle.NO_UNDERLINE, // 下划线
                Colour.BLACK,       // 字体颜色
                ScriptStyle.NORMAL_SCRIPT);
        WritableCellFormat wcf = new WritableCellFormat(he);
        if(level.equals("1")){
            wcf.setAlignment(Alignment.CENTRE);   // 设置对齐方式(水平居中)
            wcf.setVerticalAlignment(VerticalAlignment.CENTRE);//垂直居中
        }else if(level.equals("2")){
            wcf.setAlignment(Alignment.LEFT);   // 设置对齐方式
        }
        wcf.setBorder(Border.NONE, BorderLineStyle.NONE);//边框
        //wcf.setBackground(Colour.GRAY_25);//设置背景颜色
        return wcf;
    }
    /**
     * 行样式
     * @param fontSize
     * @param bold
     * @param level
     * @return
     * @throws Exception
     */
    private  WritableCellFormat setStyle(int fontSize,boolean bold , String level) throws Exception {
        WritableFont he = new WritableFont(WritableFont.createFont("宋体"),// 字体
                fontSize,               // 字号
                WritableFont.NO_BOLD,   // 粗体
                false,                  // 斜体
                UnderlineStyle.NO_UNDERLINE, // 下划线
                Colour.BLACK, // 字体颜色
                ScriptStyle.NORMAL_SCRIPT);
        
        if (bold) {
            he.setBoldStyle(WritableFont.BOLD);//粗体
        }
        WritableCellFormat wcf = new WritableCellFormat(he);
        
        if(level.equals("1")){
            wcf.setAlignment(Alignment.LEFT); // 设置对齐方式
            wcf.setBorder(Border.NONE, BorderLineStyle.THIN);//边框
            //wcf.setBackground(Colour.GREY_25_PERCENT);              //设置背景颜色
        }else if(level.equals("2")){
            //竖直方向居中对齐
            wcf.setVerticalAlignment(VerticalAlignment.CENTRE);//垂直居中
            wcf.setAlignment(Alignment.CENTRE);     // 设置对齐方式（水平居中）
            wcf.setBorder(Border.BOTTOM, BorderLineStyle.MEDIUM,Colour.BLUE2);
        }else if(level.equals("3")){
            wcf.setAlignment(Alignment.CENTRE); // 设置对齐方式
            wcf.setBorder(Border.ALL, BorderLineStyle.THIN);
        }
        return wcf;
    }
    @Override
    public String getProNameById(String projectId) {
        String proName = "";
        List<String> names = new ArrayList<String>();
        StringBuffer hql = new StringBuffer("select prjName from BaseCloudProject where projectId = ?");
        List<String> paramlist = new ArrayList<String>();
        paramlist.add(projectId);
        names = cloudVmDao.find(hql.toString(), projectId);
        proName = names.get(0);
        return proName;
    }
    @Override
    public String getDcNameById(String dcId) {
        String dcName = "";
        List<String> names = new ArrayList<String>();
        StringBuffer hql = new StringBuffer("select name from BaseDcDataCenter where id = ?");
        List<String> paramlist = new ArrayList<String>();
        paramlist.add(dcId);
        names = cloudVmDao.find(hql.toString(), dcId);
        dcName = names.get(0);
        return dcName;
    }
}
