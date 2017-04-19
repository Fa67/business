package com.eayun.database.log.baseservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.log.dao.CloudRDSLogDao;
import com.eayun.database.log.model.BaseCloudRdsLog;
import com.eayun.database.log.model.CloudRDSLog;
import com.eayun.eayunstack.model.SwiftObject;
import com.eayun.eayunstack.service.OpenstackSwiftService;
import com.eayun.eayunstack.service.OpenstackTroveLogService;

@Transactional
@Service
public class BaseRDSLogService {
	private static final Logger log = LoggerFactory.getLogger(BaseRDSLogService.class);
	@Autowired
	private OpenstackTroveLogService openstackTroveLogService;
	@Autowired
	private OpenstackSwiftService openstackSwiftService;
	@Autowired 
	private CloudRDSLogDao rdsLogDao;
	
	/**
	 * <p>查询RDS实例储存于OBS上的的某一类型Trove日志</p>
	 * -----------------------------------
	 * 
	 * @author zhouhaitao
	 * @param page 			page 分页结果集	
	 * @param map 			实例日志（{dcId,prjId,rdsInstanceId}）
	 * @param queryMap 		分页信息	
	 * @return 
	 */
	public Page getLogByInstance(Page page,ParamsMap map,QueryMap queryMap){
		StringBuffer hql = new StringBuffer();
		Map<String,Object> params = map.getParams(); 
		String rdsId =  null;
		String type =  null;
		Date startDate = null;
		Date endDate = null;
		if(null != params){
			rdsId = String.valueOf(params.get("rdsInstanceId"));
			type = String.valueOf(params.get("logType"));
			startDate = DateUtil.strToDate(String.valueOf(params.get("startDate")));
			endDate = DateUtil.strToDate(String.valueOf(params.get("endDate")));
			endDate =DateUtil.addDay(endDate, new int []{0,0,1});
		}
		
		hql.append(" from BaseCloudRdsLog ");
		hql.append(" where rdsInstanceId = ? ");
		hql.append(" and logType = ? ");
		hql.append(" and publishTime >= ? ");
		hql.append(" and publishTime <= ? ");
		hql.append(" order by logName desc");
		return rdsLogDao.pagedQuery(hql.toString(), queryMap, new Object []{
				rdsId,type,startDate,endDate});
	}
	
	/**
	 * <p>发布RDS实例的某一类型的Trove日志</p>
	 * -----------------------------------
	 * 
	 * @author zhouhaitao
	 * @param rdsLog 		RDS实例日志	
	 * @param isSyncAll 	是否同步所有类型的日志	
	 */
	public void publishLog(CloudRDSLog rdsLog,boolean isSyncAll){
		if(checkRdsInstancePublishing(rdsLog.getRdsInstanceId())){
			throw new AppException("该实例的日志正在发布中，请稍等");
		}
		if(!isSyncAll){
			JSONObject data = new JSONObject();
			data.put("publish", 1);
			data.put("name", escapeType(rdsLog.getLogType()));
			openstackTroveLogService.publishLog(rdsLog.getDcId(),rdsLog.getPrjId(),rdsLog.getRdsInstanceId(),data);
			modifyInstancePublishState(rdsLog.getRdsInstanceId(),rdsLog.getLogType());
		}
		else{
			JSONObject data = new JSONObject();
			data.put("publish", 1);
			data.put("name", "slow_query");
			openstackTroveLogService.publishLog(rdsLog.getDcId(),rdsLog.getPrjId(),rdsLog.getRdsInstanceId(),data);
			data.put("name", "error");
			openstackTroveLogService.publishLog(rdsLog.getDcId(),rdsLog.getPrjId(),rdsLog.getRdsInstanceId(),data);
			data.put("name", "general");
			openstackTroveLogService.publishLog(rdsLog.getDcId(),rdsLog.getPrjId(),rdsLog.getRdsInstanceId(),data);
			
			modifyInstancePublishState(rdsLog.getRdsInstanceId(),ConstantClazz.RDS_LOG_TYPE_ALL);
		}
		if(!checkRdsLogByInstance(rdsLog.getRdsInstanceId())){
			modifyReadAcl(rdsLog.getDcId(), rdsLog.getPrjId());
		}
	}
	
	/**
	 * <p>同步Swift的上RDS日志文件</p>
	 * -----------------------
	 * @author zhouhaitao
	 * @param rdsLog
	 */
	public void syncRdsLogFromSwift(CloudRDSLog rdsLog){
		List<SwiftObject> logList = openstackSwiftService.list(rdsLog.getDcId(),rdsLog.getPrjId(),
				rdsLog.getRdsInstanceId(),escapeType(rdsLog.getLogType()),rdsLog.getPublishFileCount());
		for(SwiftObject obs : logList){
			CloudRDSLog cloudRdsLog = handleSwift(rdsLog,obs);
			if(!checkLogExist(cloudRdsLog)){
				BaseCloudRdsLog  bcrl = new BaseCloudRdsLog();
				BeanUtils.copyPropertiesByModel(bcrl, cloudRdsLog);
				rdsLogDao.saveOrUpdate(bcrl);
			}
		}
	}
	/**
	 * <p>下载某一个OBS上的日志文件</p>
	 * -----------------------------------
	 * 
	 * @author zhouhaitao
	 * @param rdsLog 		RDS实例日志	
	 * @throws Exception 
	 */
	public String download(CloudRDSLog rdsLog) throws Exception{
		return openstackSwiftService.download(rdsLog.getDcId(), rdsLog.getPrjId());
	}
	
	/**
	 * 将上层业务的LOG的type转换成Stack底层的type
	 * @param cloudType
	 * @return
	 */
	public String escapeType(String cloudType){
		String stackType = null;
		if(ConstantClazz.RDS_LOG_TYPE_DBLOG.equals(cloudType)){
			stackType = "general";
		}
		else if(ConstantClazz.RDS_LOG_TYPE_SLOWLOG.equals(cloudType)){
			stackType = "slow_query";
		}
		else if(ConstantClazz.RDS_LOG_TYPE_ERRORLOG.equals(cloudType)){
			stackType = "error";
		}
		return stackType;
	}
	
	/**
	 * <p>将底层资源转义</p>
	 * -----------------------------
	 * @author zhouhaitao
	 * 
	 * @param type
	 * @param obs
	 * @return
	 */
	private CloudRDSLog handleSwift(CloudRDSLog rdsLog,SwiftObject obs){
		CloudRDSLog log = new CloudRDSLog();
		String name = obs.getName();
		String originalName = name.substring(name.lastIndexOf('/')+1);
		String suffix = originalName.substring(originalName.indexOf('-')+1);
		String createTime = suffix.substring(0, suffix.indexOf('.'));
		String url = ConstantClazz.RDS_LOG_SWIFT_CONTAINER_PREFIX+rdsLog.getPrjId()+"/" 
				+ rdsLog.getRdsInstanceId() + "/"+ConstantClazz.RDS_DATASTORE_MYSQL+"-"+
				escapeType(rdsLog.getLogType())+"/"+originalName;
		
		log.setRdsLogId(UUID.randomUUID()+"");
		log.setLogName(rdsLog.getLogType()+"_"+suffix.replaceAll("\\.","").replaceAll("-","")
				.replaceAll(":","").replaceAll(" ", "").replaceAll("T", ""));
		log.setPublishTime(DateUtil.formatUTCDate(createTime));
		log.setSize(obs.getBytes());
		log.setDcId(rdsLog.getDcId());
		log.setPrjId(rdsLog.getPrjId());
		log.setRdsInstanceId(rdsLog.getRdsInstanceId());
		log.setContainer(ConstantClazz.RDS_LOG_SWIFT_CONTAINER_PREFIX+rdsLog.getPrjId());
		log.setLogType(rdsLog.getLogType());
		log.setUrl(url);
		return log;
	}
	
	/**
	 * 校验是否日志文件是否存在
	 * 
	 * @author zhouhaitao
	 * @param rdsLog
	 * @return
	 */
	private boolean checkLogExist(CloudRDSLog rdsLog){
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudRdsLog ");
		hql.append(" where rdsInstanceId = ? ");
		hql.append(" and logType = ? ");
		hql.append(" and logName = ? ");
		
		Query query = rdsLogDao.createQuery(hql.toString(), new Object[]{
				rdsLog.getRdsInstanceId(),
				rdsLog.getLogType(),
				rdsLog.getLogName()});
		@SuppressWarnings("rawtypes")
		List list = query.list();
		return null!= list && list.size()>0;
	}
	
	/**
	 * <p>设置Trove日志的Container的访问权限</p>
	 * ---------------------------
	 * @author zhouhaitao
	 * 
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 */
	public void modifyReadAcl(String dcId,String prjId){
		log.info("修改项目下Trove日志的Container的访问权限");
		JSONObject data = new JSONObject();
		data.put("X-Container-Read",".r:*");
		String container = ConstantClazz.RDS_LOG_SWIFT_CONTAINER_PREFIX+prjId;
		openstackSwiftService.update(dcId, prjId,container, data);
	}
	
	/**
	 * <p>校验RDS实例是否有日志信息</p>
	 * -----------------------------
	 * @author zhouhaitao
	 * @param rdsInstanceId
	 * @return
	 */
	public boolean checkRdsLogByInstance(String rdsInstanceId){
		return rdsLogDao.countRdsLogByInstance(rdsInstanceId) >0;
	}
	
	/**
	 * <p>根据发布状态查询RDS实例列表</p>
	 * -------------------------------
	 * @author zhouhaitao
	 * 
	 * @param isPublishing   false发布完成     true发布中
	 * @return
	 * 
	 */
	public List <CloudRDSInstance> queryRdsInstanceForPublish(boolean isPublishing){
		StringBuffer sql = new StringBuffer();
		
		sql.append("			SELECT                                           			   ");
		sql.append("				dc_id,                                         			   ");
		sql.append("				prj_id,                                        			   ");
		sql.append("				rds_id,                                        			   ");
		sql.append("				log_publishing                                 			   ");
		sql.append("			FROM                                             			   ");
		sql.append("				cloud_rdsinstance                              			   ");
		sql.append("			WHERE                                            			   ");
		sql.append("				is_deleted = '0'                               			   ");
		sql.append("			AND is_visible = '1'                             			   ");
		if(isPublishing){
			sql.append("			AND log_publishing IS NOT NULL                    		   ");
		}
		else{
			sql.append("			AND rds_status <> 'BLOCKED'                      			   ");
			sql.append("			AND rds_status <> 'FAILED'                       			   ");
			sql.append("			AND rds_status <> 'ERROR'                        			   ");
			sql.append("			AND rds_status <> 'SHUTDOWN'                     			   ");
			sql.append("			AND (log_publishing is null or log_publishing = '')		   ");
		}
		
		javax.persistence.Query query = rdsLogDao.createSQLNativeQuery(sql.toString());
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if(null != result && result.size() > 0){
			List<CloudRDSInstance> list =  new ArrayList<CloudRDSInstance>();
			for(int i = 0 ;i < result.size() ; i++){
				CloudRDSInstance rdsInstance = new CloudRDSInstance();
				Object [] objs = (Object [])result.get(i);
				rdsInstance.setDcId(String.valueOf(objs[0]));
				rdsInstance.setPrjId(String.valueOf(objs[1]));
				rdsInstance.setRdsId(String.valueOf(objs[2]));
				rdsInstance.setLogPublishing(null !=objs[3]?String.valueOf(objs[3]):null);
				
				list.add(rdsInstance);
			}
			
			return list;
		}
		
		return null;
	}
	
	/**
	 * <p>修改RDS实例的日志发布状态</p>
	 * -----------------------------------
	 * @author zhouhaitao
	 * @param rdsInstanceId				RDS实例ID
	 * @param logType					RDS日志类型
	 */
	public void modifyInstancePublishState(String rdsInstanceId,String logType){
		StringBuffer sql = new StringBuffer();
		sql.append("update cloud_rdsinstance set log_publishing = ? where rds_id = ?");
		rdsLogDao.execSQL(sql.toString(), new Object[]{logType,rdsInstanceId});
	}
	
	/**
	 * <p>校验日志是否发布中</p>
	 * @author zhouhaitao
	 * ----------------------------
	 * @param rdsInstanceId
	 * @return
	 */
	public boolean checkRdsInstancePublishing(String rdsInstanceId){
		StringBuffer sql = new StringBuffer();
		
		sql.append("select rds_id,log_publishing from cloud_rdsinstance where rds_id = ?");
		javax.persistence.Query query = rdsLogDao.createSQLNativeQuery(sql.toString(),new Object[]{rdsInstanceId});
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if(null != result && result.size() == 1){
			Object [] objs = (Object [])result.get(0);
			String logPublishing = (null!=objs[1]?String.valueOf(objs[1]):null);
			
			return !StringUtil.isEmpty(logPublishing);
		}
		
		return false;
	}
}
