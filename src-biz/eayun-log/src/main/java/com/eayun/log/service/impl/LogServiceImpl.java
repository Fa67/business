package com.eayun.log.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.HttpUtil;
import com.eayun.common.util.SessionUtil;
import com.eayun.log.bean.ExcelLog;
import com.eayun.log.model.BaseSysLog;
import com.eayun.log.model.SysLog;
import com.eayun.log.service.LogService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.CloudProject;

/**
 * 
 *                       
 * @Filename: LogServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author:wang ning
 * @Email: ning.wang@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月14日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Transactional
@Service
public class LogServiceImpl implements LogService {
    private static final Logger log = LoggerFactory.getLogger(LogServiceImpl.class);

    @Autowired
    private ProjectService projectService;
    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public void addLog(String actItem, String resourceType, String resourceName, String prjId,
                       String statu, Exception e) {
        String actPerson = "";
        String cusId = "";
        
        HttpSession session = SessionUtil.getSession();
        if (session != null) {
        	SessionUserInfo sessionUserInfo = (SessionUserInfo)session.getAttribute(
        			ConstantClazz.SYS_SESSION_USERINFO);
        	if (sessionUserInfo != null) {
        		actPerson = sessionUserInfo.getUserName();
        		cusId = sessionUserInfo.getCusId();
        	}
        }
        this.addLog(actItem, actPerson, resourceType, resourceName, prjId, cusId, statu, e);
    }

    @Override
    public void addLog(String actItem, String actPerson, String resourceType, String resourceName,
                       String prjId, String cusId, String statu, String operatorIp, Exception e) {
        log.info("添加日志");
        BaseSysLog sysLog = new BaseSysLog();
        String id = UUID.randomUUID().toString().replace("-", "");
        sysLog.setId(id);
        sysLog.setActItem(actItem);
        sysLog.setActPerson(actPerson);
        sysLog.setActTime(new Date());
        sysLog.setCusId(cusId);
        sysLog.setPrjId(null != prjId?prjId:"");
        sysLog.setResourceName(resourceName);
        sysLog.setStatu(statu);
        sysLog.setResourceType(resourceType);
        sysLog.setIp(operatorIp);

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

                sysLog.setDetail(appException.getErrorMessage()+detail);
            } else {
                sysLog.setDetail(e.getMessage());
            }
        }
        mongoTemplate.insert(sysLog, MongoCollectionName.LOG_ECSC);
    }

    /**
     * 添加ecsc日志接口，改为将日志存储到mongo中
     * @param actItem
     * @param actPerson
     * @param resourceType
     * @param resourceName
     * @param prjId
     * @param cusId
     * @param statu
     * @param e
     */
    @Override
    public void addLog(String actItem, String actPerson, String resourceType, String resourceName,
                       String prjId, String cusId, String statu, Exception e) {
        log.info("添加日志");
        BaseSysLog sysLog = new BaseSysLog();
        String id = UUID.randomUUID().toString().replace("-", "");
        sysLog.setId(id);
        sysLog.setActItem(actItem);
        sysLog.setActPerson(actPerson);
        sysLog.setActTime(new Date());
        sysLog.setCusId(cusId);
        sysLog.setPrjId(null != prjId?prjId:"");
        sysLog.setResourceName(resourceName);
        sysLog.setStatu(statu);
        sysLog.setResourceType(resourceType);
        HttpServletRequest request = SessionUtil.getRequest(false);
        if(null != request){
        	sysLog.setIp(HttpUtil.getRequestIP(request));
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
                
                sysLog.setDetail(appException.getErrorMessage()+detail);
            } else {
                sysLog.setDetail(e.getMessage());
            }
        }
        mongoTemplate.insert(sysLog, MongoCollectionName.LOG_ECSC);
    }

    /**
     * 查询日志分页，mongo
     */
	@SuppressWarnings("unchecked")
    @Override
	public Page getLogListMongo(Page page, Date beginTime, Date endTime,
			String actItem, String statu, String prjId, String cusId,
			String resourceType, String resourceName, String ip,String operator,
			QueryMap queryMap) throws Exception {
		Query query = genQuery(beginTime, endTime, actItem, statu, prjId,
				resourceType, resourceName, ip,cusId, operator);
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
	 * 查询mongo条件
	 */
	private Query genQuery(Date beginTime, Date endTime, String actItem,
			String statu, String prjId, String resourceType,
			String resourceName, String ip,String cusId) {
		Query query=new Query();
		query.addCriteria(Criteria.where("cusId").is(cusId));
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
		return query;
	}

    /**
     * 查询mongo条件
     */
    private Query genQuery(Date beginTime, Date endTime, String actItem,
                           String statu, String prjId, String resourceType,
                           String resourceName, String ip,String cusId, String operator) {
        Query query=new Query();
        query.addCriteria(Criteria.where("cusId").is(cusId));
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
        if (null != operator && !operator.trim().equals("")) {
            if ("1".equals(operator)){
                query.addCriteria(Criteria.where("actPerson").is("API"));
            }else {
                query.addCriteria(Criteria.where("actPerson").ne("API"));
            }
        }
        return query;
    }
	/**
	 * mongo分页查询ecsc日志信息
	 * @param query 查询条件
	 * @param queryMap 分页参数(包括当前页,每页显示条数)
	 * @return
	 * @throws Exception
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
	 * 导出查询
	 * @param query
	 * @return
	 * @throws Exception
	 */
	private List<JSONObject> findAllByOperation(Query query) throws Exception{
		Sort sort=new Sort(Direction.DESC, "actTime");
        List<JSONObject> list=mongoTemplate.find(query.with(sort), JSONObject.class,MongoCollectionName.LOG_ECSC);
		return list;
	}

	/**
	 * 导出日志，查询mongo
	 */
	@Override
	public List<ExcelLog> queryLogExcelFromMongo(Date beginTime, Date endTime,
			String actItem, String statu, String prjId, String cusId) throws Exception {
		
		Query query=genQuery(beginTime, endTime, actItem, statu, prjId,
				null, null, null,cusId);
		List<JSONObject> list=findAllByOperation(query);
		List<ExcelLog> newList=new ArrayList<ExcelLog>();
		for (JSONObject json : list) {
            ExcelLog excelLog = new ExcelLog();
            excelLog.setActItem(json.getString("actItem"));
            excelLog.setActPerson(json.getString("actPerson"));
            Date date=json.getDate("actTime");
            excelLog.setActTime(DateUtil.dateToStringTwo(date));
            if (json.getString("prjId") == null || json.getString("prjId").equals("null")) {
                excelLog.setDcName("");
            } else {
            	CloudProject cloudProject=projectService.findProject(json.getString("prjId"));
                excelLog.setDcName(cloudProject==null?"":cloudProject.getDcName());
            }
            if (json.getString("resourceName") == null || json.getString("resourceName").equals("null")) {
                excelLog.setResourceName("");
            } else {
                excelLog.setResourceName(json.getString("resourceName"));
            }
            if (json.getString("resourceType") == null || json.getString("resourceType").equals("null")) {
                excelLog.setResourceType("");
            } else {
                excelLog.setResourceType(json.getString("resourceType"));
            }

            if (json.getString("statu").equals("0")) {
                excelLog.setStatu("失败");
            } else if (json.getString("statu").equals("1")) {
                excelLog.setStatu("已完成");
            } else {
                excelLog.setStatu("");
            }
            newList.add(excelLog);
        }
		return newList;
	}

	@Override
	public List<SysLog> getLastLogs(SessionUserInfo sessionUser) {
		log.info("获取登录客户最新8条日志...");
		Sort sort = new Sort(Direction.DESC, "actTime");
		Query query = new Query();
		query.addCriteria(Criteria.where("cusId").is(sessionUser.getCusId()));
		query.addCriteria(Criteria.where("actItem").ne("登录").andOperator(Criteria.where("actItem").ne("退出")));
		query.addCriteria(Criteria.where("resourceType").exists(true));
		query.skip(0);// skip相当于从那条记录开始
        query.limit(8);// 从skip开始,取多少条记录
		List<SysLog> jsonlist = mongoTemplate.find(query.with(sort), SysLog.class,MongoCollectionName.LOG_ECSC);
		return jsonlist;
	}
}
