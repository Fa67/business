package com.eayun.log.ecmcsevice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

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
import com.eayun.common.exception.AppException;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.HttpUtil;
import com.eayun.common.util.SessionUtil;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.dao.EcmcLogDao;
import com.eayun.log.dao.SysLogDao;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.log.model.BaseSysLog;
import com.eayun.log.model.OperLog;
import com.eayun.log.model.OperLogPname;
import com.eayun.log.model.SysLog;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.CloudProject;
import com.mongodb.WriteResult;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年3月29日
 */
@Transactional
@Service
public class EcmcLogServiceImpl implements EcmcLogService{

	private static final Logger log = LoggerFactory.getLogger(EcmcLogServiceImpl.class);
	@Autowired
	private EcmcLogDao ecmclogdao; 
	@Autowired
    private SysLogDao logDao;
	@Autowired
	private ProjectService projectService;
	@Autowired
    private MongoTemplate mongoTemplate;
	

	/**
	 * 添加ECMC日志
	 */
	@Override
	public void addLog(String busiName, String resourceType, String resourceName, String prjId, Integer returnFlag,
			String resourceId,Exception e) {
		log.info("添加ECMC日志构造方法");
        OperLog sysLog = new OperLog();
        String id = UUID.randomUUID().toString().replace("-", "");
        sysLog.setId(id);
        BaseEcmcSysUser user = EcmcSessionUtil.getUser();
        sysLog.setBusiName(busiName);
        if(user!=null){
        	sysLog.setOperId(user.getAccount());
        	sysLog.setMesCode(user.getName());
        }
        sysLog.setOperDate(new Date());
        sysLog.setPrjId(prjId);
        sysLog.setResourceID(resourceId);
        sysLog.setResourceName(resourceName);
        sysLog.setReturnFlag(returnFlag);
        sysLog.setResourceType(resourceType);
        HttpServletRequest request = SessionUtil.getRequest(false);
        if(request!=null){
        	sysLog.setIpAddr(HttpUtil.getRequestIP(request));
        	sysLog.setTemp5(request.getRequestURI());
        	sysLog.setFileName(request.getRequestURL().toString());
        }
        if (e != null) {
            if (e instanceof AppException) {
                AppException appException = (AppException) e;
                String[] args = appException.getArgsMessage();
                StringBuffer detail = new StringBuffer();
                if(args!=null && args.length>0){
                    detail.append(":");
                    for(int i=0;i<args.length;i++){
                        detail.append(args[i]);
                        if(i!=args.length-1){
                            detail.append(",");
                        }
                    }
                }
                
                sysLog.setTemp6(appException.getErrorMessage()+detail);
            } else {
                sysLog.setTemp6(e.getMessage());
            }
        }
        mongoTemplate.insert(sysLog, MongoCollectionName.LOG_ECMC);
	}
	/**
	 * 定时删除ECMC日志方法
	 */
	@Override
	public void deleteLog(Date date) {
		log.info("删除ECMC日志");
		if(null==date){
	        Date d = new Date();
	        date = DateUtil.addDay(d, new int[] {0,0,-180});
		}
        
        Query query = new Query();
        query.addCriteria(Criteria.where("operDate").lt(date));
        WriteResult result = mongoTemplate.remove(query, JSONObject.class, MongoCollectionName.LOG_ECMC);
        log.info("删除ECMC日志 "+result.getN()+" 条");
	}
	
	/**
	 * 从mongo中查询ECSC日志并分页
	 */
	@SuppressWarnings("unchecked")
    @Override
	public Page getLogListMongo(Page page, Date beginTime, Date endTime,
			String actItem, String statu, String prjId, String resourceType,
			String ip, String resourceName, String operator, QueryMap queryMap) throws Exception{
		
		Query query = genQuery(beginTime, endTime, actItem, statu, 
				prjId, resourceType, ip,resourceName, operator);   //获取query
		page=findByPage(query,queryMap);

		List<JSONObject> newlist = (List<JSONObject>) page.getResult();
		List<SysLog> list=new ArrayList<SysLog>();
		 for (JSONObject json : newlist) {
	            SysLog log = new SysLog();
	            log.setId(json.getString("_id"));
	            log.setActItem(json.getString("actItem"));
	            log.setActPerson(json.getString("actPerson"));
	            log.setActTime(json.getDate("actTime"));
	            log.setPrjId(json.getString("prjId"));
	            log.setCusId(json.getString("cusId"));
	            log.setStatu(json.getString("statu"));
	            log.setResourceType(json.getString("resourceType")==null||json.getString("resourceType").length()<=0?null:json.getString("resourceType"));
	            log.setResourceName(json.getString("resourceName")==null||json.getString("resourceName").length()<=0?null:json.getString("resourceName"));
	            if(json.getString("prjId")!=null&&json.getString("prjId").length()>0){
	            	CloudProject cloudProject=projectService.findProject(json.getString("prjId"));
	            	log.setPrjName(cloudProject==null?null:cloudProject.getPrjName());
	            	log.setDcName(cloudProject==null?null:cloudProject.getDcName());
	            }else{
	            	log.setPrjName(null);
	            	log.setDcName(null);
	            }
	            log.setIp(json.getString("ip"));
	            list.add(log);
	        }
		 page.setResult(list);
		return page;
	}
	/**
	 * 组合ECSC日志查询条件
	 */
	private Query genQuery(Date beginTime, Date endTime, String actItem, String statu,
			String prjId, String resourceType, String ip, String resourceName, String operator) {
		
		Query query=new Query();
		if (null != beginTime&&null != endTime) { //验证开始时间格式和是否为空
			query.addCriteria(Criteria.where("actTime").gt(beginTime).andOperator(Criteria.where("actTime").lt(endTime)));
        }
        if (null != actItem && !actItem.trim().equals("")) {
        	actItem = actItem.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("{", "\\{").replace("}", "\\}").replace("[", "\\[").replace("]", "\\]").replace("?", "\\?").replace("|", "\\|").replace("*", "\\*").replace("$", "\\$").replace("^", "\\^").replace("+", "\\+");
        	Pattern pattern = Pattern.compile(actItem, Pattern.CASE_INSENSITIVE);
        	query.addCriteria(Criteria.where("actItem").regex(pattern));
        }
        if (null != statu && !statu.trim().equals("")) {
        	query.addCriteria(Criteria.where("statu").is(statu));
        }
        
        if (null != resourceType && !resourceType.trim().equals("")) {
        	query.addCriteria(Criteria.where("resourceType").is(resourceType));
        }
        if (null != resourceName && !resourceName.trim().equals("")) {
        	resourceName = resourceName.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("{", "\\{").replace("}", "\\}").replace("[", "\\[").replace("]", "\\]").replace("?", "\\?").replace("|", "\\|").replace("*", "\\*").replace("$", "\\$").replace("^", "\\^").replace("+", "\\+");
        	Pattern pattern = Pattern.compile(resourceName, Pattern.CASE_INSENSITIVE);
        	query.addCriteria(Criteria.where("resourceName").regex(pattern));
        }
        if (null != ip && !ip.trim().equals("")) {
        	ip = ip.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("{", "\\{").replace("}", "\\}").replace("[", "\\[").replace("]", "\\]").replace("?", "\\?").replace("|", "\\|").replace("*", "\\*").replace("$", "\\$").replace("^", "\\^").replace("+", "\\+");
        	Pattern pattern = Pattern.compile(ip, Pattern.CASE_INSENSITIVE);
        	query.addCriteria(Criteria.where("ip").regex(pattern));
        }
        if (null != prjId && !prjId.trim().equals("")) {    
            if(prjId.equals("NO")){         //无项目
            	query.addCriteria(Criteria.where("prjId").is(""));
            }else{                          //选中项目
            	query.addCriteria(Criteria.where("prjId").is(prjId));
            }
        }
		if ("1".equals(operator)){
			//只展示API类型的日志数据
			query.addCriteria(Criteria.where("actPerson").is("API"));
		}else {
			//展示全部操作者的数据
			query.addCriteria(Criteria.where("actPerson").ne("API"));
		}
		return query;
	}
	/**
	 * 查询ECSC-mongo日志数据
	 */
	private Page findByPage(Query query,QueryMap queryMap) throws Exception{
		Sort sort = new Sort(Direction.DESC, "actTime");
		Integer pageSize=queryMap.getCURRENT_ROWS_SIZE();
		Integer pageNum=queryMap.getPageNum();
		query.skip(pageSize*pageNum-pageSize);// skip相当于从那条记录开始
        query.limit(queryMap.getCURRENT_ROWS_SIZE());// 从skip开始,取多少条记录
        long totalSize=mongoTemplate.count(query, MongoCollectionName.LOG_ECSC);
        int startIndex = Page.getStartOfPage(pageNum, pageSize);
        Page page=new Page(startIndex, totalSize, pageSize, mongoTemplate.find(query, JSONObject.class,MongoCollectionName.LOG_ECSC));
        page.setResult(mongoTemplate.find(query.with(sort), JSONObject.class,MongoCollectionName.LOG_ECSC));
		return page;
	}
	/**
	 * 迁移ECSC日志
	 */
	@SuppressWarnings("unchecked")
    @Override
	public boolean syncLog() throws Exception{
		List<Object> maxlog=logDao.find("select max(actTime) from BaseSysLog");
		if(null == maxlog || maxlog.isEmpty()){
			return true;
		}
		Date maxDate=(Date)maxlog.get(0);
		
		mongoTemplate.remove(new Query(Criteria.where("actTime").lte(maxDate)), MongoCollectionName.LOG_ECSC);
		List<Object> syslog=logDao.find("select min(actTime) from BaseSysLog");
		Date start=(Date)syslog.get(0);
		Date end = new Date();
		long days=(end.getTime()-start.getTime())/(86400000)+1;
		for (int i = 0; i < days; i++) {
			end=DateUtil.addDay(start, new int[]{0,0,1});
			String hql="from BaseSysLog where actTime >=? and actTime < ?";
			List<BaseSysLog> list=logDao.find(hql,start,end);
			for (BaseSysLog baseSysLog : list) {
				mongoTemplate.insert(baseSysLog, MongoCollectionName.LOG_ECSC);
			}
			start=DateUtil.addDay(start, new int[]{0,0,1});
		}
		return true;
	}

	/**
	 * 迁移ECMC日志
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
    @Override
	public boolean syncEcmcLog() throws Exception {
		List<Object> maxlog=logDao.find("select max(operDate) from OperLog");
		if(null == maxlog || maxlog.isEmpty()){
			return true;
		}
		Date maxDate=(Date)maxlog.get(0);
		
		mongoTemplate.remove(new Query(Criteria.where("operDate").lte(maxDate)), MongoCollectionName.LOG_ECMC);
		List operLog=ecmclogdao.find("select min(operDate) from OperLog");
		Date start=(Date)operLog.get(0);
		Date end = new Date();
		long days=(end.getTime()-start.getTime())/(86400000)+1;
		int j=0;
		for (int i = 0; i < days; i++) {
			end=DateUtil.addDay(start, new int[]{0,0,1});
			String hql="from OperLog where operDate >=? and operDate < ?";
			List<OperLog> list=ecmclogdao.find(hql,start,end);
			for (OperLog log : list) {
				mongoTemplate.insert(log, MongoCollectionName.LOG_ECMC);
			}
			j+=list.size();
			start=DateUtil.addDay(start, new int[]{0,0,1});
		}
		return true;
	}

	/**
	 * 从mongo中查询ECMC日志并分页
	 */
	@SuppressWarnings("unchecked")
    @Override
	public Page getEcmcLogListMongo(Page page, Date beginTime, Date endTime,
			String actItem, String status, String prjId, String resourceType,
			String ip, String resourceName, QueryMap queryMap) throws Exception {
		
		Query query = genEcmcLogQuery(beginTime, endTime, actItem, status, 
				prjId, resourceType, ip,resourceName);   //获取query
		page=findEcmcByPage(query,queryMap);
		List<JSONObject> newlist = (List<JSONObject>) page.getResult();
		List<OperLogPname> list=new ArrayList<OperLogPname>();
		 for (JSONObject json : newlist) {
			 	OperLogPname log = new OperLogPname();
			 	log.setId(json.getString("_id"));
	            log.setBusiName(json.getString("busiName"));
	            log.setOperId(json.getString("operId"));
	            log.setOperDate(json.getDate("operDate"));
	            log.setPrjId(json.getString("prjId"));
	            log.setReturnFlag(json.getInteger("returnFlag"));
	            log.setIpAddr(json.getString("ipAddr"));
	            log.setResourceType(json.getString("resourceType")==null||json.getString("resourceType").length()<=0?null:json.getString("resourceType"));
	            log.setResourceName(json.getString("resourceName")==null||json.getString("resourceName").length()<=0?null:json.getString("resourceName"));
	            log.setResourceID(json.getString("resourceID"));
	            log.setTemp5(json.getString("temp5"));
	            if(json.getString("prjId")!=null&&json.getString("prjId").length()>0){
	            	CloudProject cloudProject=projectService.findProject(json.getString("prjId"));
	            	log.setPrjName(cloudProject==null?null:cloudProject.getPrjName());
	            }else{
	            	log.setPrjName(null);
	            }
	            list.add(log);
	        }
		 page.setResult(list);
		return page;
	}
	/**
	 * 组合ECMC-mongo日志查询条件
	 */
	private Query genEcmcLogQuery(Date beginTime, Date endTime, String actItem, String status,
			String prjId, String resourceType, String ip, String resourceName) {
		
		Query query=new Query();
		if (null != beginTime&&null != endTime) { //验证开始时间格式和是否为空
			query.addCriteria(Criteria.where("operDate").gt(beginTime).andOperator(Criteria.where("operDate").lt(endTime)));
        }
        if (null != actItem && !actItem.trim().equals("")) {
        	actItem = actItem.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("{", "\\{").replace("}", "\\}").replace("[", "\\[").replace("]", "\\]").replace("?", "\\?").replace("|", "\\|").replace("*", "\\*").replace("$", "\\$").replace("^", "\\^").replace("+", "\\+");
        	Pattern pattern = Pattern.compile(actItem, Pattern.CASE_INSENSITIVE);
        	query.addCriteria(Criteria.where("busiName").regex(pattern));
        }
        if (null != status && !status.trim().equals("")) {
        	query.addCriteria(Criteria.where("returnFlag").is(Integer.parseInt(status)));
        }
        
        if (null != resourceType && !resourceType.trim().equals("")) {
        	query.addCriteria(Criteria.where("resourceType").is(resourceType));
        }
        if (null != resourceName && !resourceName.trim().equals("")) {
        	resourceName = resourceName.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("{", "\\{").replace("}", "\\}").replace("[", "\\[").replace("]", "\\]").replace("?", "\\?").replace("|", "\\|").replace("*", "\\*").replace("$", "\\$").replace("^", "\\^").replace("+", "\\+");
        	Pattern pattern = Pattern.compile(resourceName, Pattern.CASE_INSENSITIVE);
        	query.addCriteria(Criteria.where("resourceName").regex(pattern));
        }
        if (null != ip && !ip.trim().equals("")) {
        	ip = ip.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("{", "\\{").replace("}", "\\}").replace("[", "\\[").replace("]", "\\]").replace("?", "\\?").replace("|", "\\|").replace("*", "\\*").replace("$", "\\$").replace("^", "\\^").replace("+", "\\+");
        	Pattern pattern = Pattern.compile(ip, Pattern.CASE_INSENSITIVE);
        	query.addCriteria(Criteria.where("ipAddr").regex(pattern));
        }
        if (null != prjId && !prjId.trim().equals("")) {    
            if(prjId.equals("NO")){         //无项目
            	query.addCriteria(Criteria.where("prjId").is(""));
            }else{                          //选中项目
            	query.addCriteria(Criteria.where("prjId").is(prjId));
            }
        }
		return query;
	}
	/**
	 * ECMC-mongo日志查询数据
	 */
	private Page findEcmcByPage(Query query,QueryMap queryMap) throws Exception{
		Sort sort = new Sort(Direction.DESC, "operDate");
		Integer pageSize=queryMap.getCURRENT_ROWS_SIZE();
		Integer pageNum=queryMap.getPageNum();
		query.skip(pageSize*pageNum-pageSize);// skip相当于从那条记录开始
        query.limit(queryMap.getCURRENT_ROWS_SIZE());// 从skip开始,取多少条记录
        long totalSize=mongoTemplate.count(query, MongoCollectionName.LOG_ECMC);
        int startIndex = Page.getStartOfPage(pageNum, pageSize);
        Page page=new Page(startIndex, totalSize, pageSize, mongoTemplate.find(query, JSONObject.class,MongoCollectionName.LOG_ECMC));
        page.setResult(mongoTemplate.find(query.with(sort), JSONObject.class,MongoCollectionName.LOG_ECMC));
		return page;
	}

	/**
	 * ECMC-mongo日志详情
	 */
	@Override
	public OperLogPname getOneEcmcLogFromMongo(String id) throws AppException {
		log.info("查询ECMC日志详情");
		OperLogPname log=mongoTemplate.findById(id, OperLogPname.class, MongoCollectionName.LOG_ECMC);
		if(null!=log&&null!=log.getPrjId()&&log.getPrjId().length()>0){
        	CloudProject cloudProject=projectService.findProject(log.getPrjId());
        	log.setPrjName(cloudProject==null?null:cloudProject.getPrjName());
        }else if(log != null){
        	log.setPrjName(null);
        }
		return log;
	}
}
