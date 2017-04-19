package com.eayun.work.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.eayun.virtualization.model.CloudProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.EcmcRoleIds;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.SeqManager;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.customer.model.BaseUser;
import com.eayun.customer.model.Customer;
import com.eayun.customer.model.User;
import com.eayun.customer.serivce.UserService;
import com.eayun.ecmcrole.model.BaseEcmcSysRole;
import com.eayun.ecmcuser.model.EcmcSysUser;
import com.eayun.ecmcuser.service.EcmcSysUserService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.file.model.EayunFile;
import com.eayun.file.service.FileService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.work.dao.WorkFileDao;
import com.eayun.work.dao.WorkFlowDao;
import com.eayun.work.dao.WorkOpinionDao;
import com.eayun.work.dao.WorkQuotaDao;
import com.eayun.work.dao.WorkorderDao;
import com.eayun.work.ecmcservice.EcmcWorkMailService;
import com.eayun.work.ecmcservice.EcmcWorkorderService;
import com.eayun.work.model.BaseWorkFile;
import com.eayun.work.model.BaseWorkFlow;
import com.eayun.work.model.BaseWorkOpinion;
import com.eayun.work.model.BaseWorkQuota;
import com.eayun.work.model.BaseWorkorder;
import com.eayun.work.model.WorkFile;
import com.eayun.work.model.WorkOpinion;
import com.eayun.work.model.WorkQuota;
import com.eayun.work.model.WorkReport;
import com.eayun.work.model.WorkUtils;
import com.eayun.work.model.Workorder;
import com.eayun.work.service.OrderNumService;
@Service
@Transactional
public class EcmcWorkorderServiceImpl implements EcmcWorkorderService {
	@Autowired
	private WorkorderDao workDao;
	@Autowired
	private WorkOpinionDao workOpinionDao;
	@Autowired
	private WorkFlowDao workFlowDao; // 流程
	@Autowired
	private WorkFileDao workFileDao; // 附件
	@Autowired
	private WorkQuotaDao workQuotaDao; // 工单配额
	@Autowired
	private OrderNumService orderNumService; // 流水编号
	@Autowired
	private FileService fileService; // 附件
	@Autowired
	private EcmcSysUserService ecmcUserService;//ecmc用户
	@Autowired
	private EcmcCustomerService ecmcCusService;//客户
	@Autowired
	private EcmcWorkMailService ecmcWorkMailService;//工单邮件短信
	@Autowired
	private UserService userService;//ecsc用户
	@Override
	public List<SysDataTree> getDataTree(String parentId) {
		List<SysDataTree> dataList=WorkUtils.getDataTreeList(parentId);
		return dataList;
	}

	@Override
	public Workorder addWorkorder(Workorder workorder) throws Exception{

		Date date = new Date();
		//-----添加工单--------
		BaseWorkorder baseWork = new BaseWorkorder();
		String workId = SeqManager.getSeqMang().getSeqForDate();//工单id
		String flowId = SeqManager.getSeqMang().getSeqForDate();// 流程id
		String workNum = orderNumService.getOrderNum("SJ", "yyyyMMdd", 8);//工单编号
		workorder.setWorkId(workId);
		workorder.setWorkNum(workNum);
		workorder.setFlowId(flowId);
		workorder.setWorkCreTime(date);

		BeanUtils.copyPropertiesByModel(baseWork, workorder);
		baseWork = workDao.save(baseWork);
		// ------流程------
		BaseWorkFlow baseWorkFlow = new BaseWorkFlow();// 封装流程
		baseWorkFlow.setFlowId(flowId);
		baseWorkFlow.setWorkId(workId);
		baseWorkFlow.setFlowRespondFalg("0");
		baseWorkFlow.setFlowCreTime(date);
		baseWorkFlow.setFlowState("0");
		baseWorkFlow = (BaseWorkFlow) workFlowDao.saveEntity(baseWorkFlow);
		//------回复------
		workorder = this.extendWorkorder(workorder);//放入工单扩展字段
		BaseWorkOpinion baseWorkOpinion = new BaseWorkOpinion();
		baseWorkOpinion.setCreUser(workorder.getWorkCreUser());
		baseWorkOpinion.setFlag(workorder.getWorkFalg());
		baseWorkOpinion.setOpinionContent(workorder.getWorkContent());
		baseWorkOpinion.setOpinionTime(date);
		baseWorkOpinion.setWorkId(workId);
		baseWorkOpinion.setEcmcCre("0");
		baseWorkOpinion.setCreUserName(workorder.getWorkCreUserName());
		workOpinionDao.save(baseWorkOpinion);
		WorkOpinion workOpinion = new WorkOpinion();
		BeanUtils.copyPropertiesByModel(workOpinion, baseWorkOpinion);

		//-------扩展属性结束
		//---发送邮件
		ecmcWorkMailService.sendMailAndSms(workorder, workOpinion);
		return workorder;
	}
	@Override
	public Workorder acceptanceWork(Map<String, String> map) throws Exception {
		String userId = map.get("userId");
		String workId = map.get("workId");
		String workHeadUserName = map.get("workHeadUserName");
		Date date = new Date();
		BaseWorkorder baseWorkorder = workDao.findOne(workId);
		if("1".equals(baseWorkorder.getWorkFalg())){
			return null;
		}
		baseWorkorder.setWorkHeadUser(userId);
		baseWorkorder.setWorkFalg("1");
		baseWorkorder.setSendMesFlag("1");
		baseWorkorder.setWorkHeadUserName(workHeadUserName);
		workDao.merge(baseWorkorder);
		//得到流程
		BaseWorkFlow baseWorkFlow = workFlowDao.findOne(baseWorkorder.getFlowId());
		//修改流程的开始时间和状态
		baseWorkFlow.setFlowBeginTime(date);
		baseWorkFlow.setFlowFlag(baseWorkorder.getWorkFalg());
		baseWorkFlow.setUserIdHead(userId);
		if(baseWorkorder.getWorkType().contains(WorkUtils.SPETYPEPARID)){
			//特殊类型的工单不需要等待响应
			baseWorkFlow.setFlowRespondFalg("1");//直接是响应状态
			baseWorkFlow.setFlowRespondTime(date);//响应时间.
			baseWorkorder.setWorkEcscFalg("1");
			baseWorkorder.setSendMesFlag("2");
			workDao.merge(baseWorkorder);
		}
		workFlowDao.merge(baseWorkFlow);
		//---添加回复
		BaseWorkOpinion baseWrokOpinion = new BaseWorkOpinion();
		baseWrokOpinion.setCreUser(userId);
		baseWrokOpinion.setFlag(baseWorkorder.getWorkFalg());
		baseWrokOpinion.setOpinionContent("正在受理，请等待响应！");
		baseWrokOpinion.setOpinionTime(date);
		baseWrokOpinion.setReplyUser(baseWorkorder.getWorkCreUser());
		baseWrokOpinion.setWorkId(workId);
		baseWrokOpinion.setEcmcCre("0");
		baseWrokOpinion.setCreUserName(workHeadUserName);
		baseWrokOpinion.setReplyUserName(baseWorkorder.getWorkCreUserName());
		workOpinionDao.save(baseWrokOpinion);
		//------发送邮件
		Workorder workorder = new Workorder();
		BeanUtils.copyPropertiesByModel(workorder, baseWorkorder);

		WorkOpinion workOpinion = new WorkOpinion();
		BeanUtils.copyPropertiesByModel(workOpinion, baseWrokOpinion);

		//放入扩展属性，--发送短信和邮件使用
		workorder = this.extendWorkorder(workorder);
		//-------扩展属性结束
		ecmcWorkMailService.sendMailAndSms(workorder, workOpinion);

		return workorder;
	}
	@SuppressWarnings("unchecked")
	@Override
	public Workorder findWorkByWorkId(String workId) {
		BaseWorkorder baseWork = workDao.findOne(workId);
		Workorder workorder = new Workorder();
		BeanUtils.copyPropertiesByModel(workorder, baseWork);
		//申请客户
		Customer cus = ecmcCusService.getCustomerById(workorder.getApplyCustomer());
		if(!StringUtil.isEmpty(cus.getCusId())){//是本系统客户
			workorder.setWorkCusName(cus.getCusOrg());
		}else{
			workorder.setWorkCusName(workorder.getApplyCustomer());
		}
		//申请人帐号
		if(!baseWork.getWorkType().equals(WorkUtils.REGISTERTYPE)){
			User user=userService.findUserById(workorder.getWorkApplyUser());
			if(!StringUtil.isEmpty(user.getUserAccount())){//是本系统用户
				workorder.setWorkApplyUserName(user.getUserAccount());
			}else{
				workorder.setWorkApplyUserName(workorder.getWorkApplyUserName());
			}
		}
		//得到责任人信息
		if(workorder.getWorkHeadUser()!=null){
			EcmcSysUser baseEcmcUser =ecmcUserService.findUserById(workorder.getWorkHeadUser());
			workorder.setWorkHeadUserName(baseEcmcUser!=null?baseEcmcUser.getName():workorder.getWorkHeadUserName());
		}
		//创建人
		if(!workorder.getWorkCreRole().equals("2")){
			EcmcSysUser baseEcmcUser =ecmcUserService.findUserById(workorder.getWorkCreUser());
			workorder.setWorkCreUserName(baseEcmcUser!=null?baseEcmcUser.getName():workorder.getWorkCreUserName());
		}else{
			if(!WorkUtils.REGISTERTYPE.equals(baseWork.getWorkType())){
				BaseUser user = userService.findUserById(workorder.getWorkCreUser());
				workorder.setWorkCreUserName(user.getUserAccount());
			}else{
				workorder.setWorkCreUserName(null);
			}
		}
		//配额是获取项目id
		if(workorder.getWorkType().equals(WorkUtils.QUOTATYPE)){//配额
			StringBuffer hql = new StringBuffer();
			hql.append("select prj_id from work_quota where work_id= ?");
			List<String> values = new ArrayList<String>();
			values.add(workId);
			Query query = workDao.createSQLNativeQuery(hql.toString(), values.toArray());
			List<Object> list = query.getResultList();
			if(list!=null && list.size()>0){
				for (Object object : list) {
					workorder.setPrjId(String.valueOf(object));
				}
			}
		}
		BaseWorkFlow baseWorkFlow = workFlowDao.findOne(workorder.getFlowId());
		workorder.setFlowBeginTime(baseWorkFlow.getFlowBeginTime());
		workorder.setEndtime(baseWorkFlow.getEndtime());
		workorder.setFlowRespondFalg(baseWorkFlow.getFlowRespondFalg());
		workorder.setFlowRespondTime(baseWorkFlow.getFlowRespondTime());
		workorder.setWorkFalgName(WorkUtils.getEcmcFlagMap().get(workorder.getWorkFalg()));
		workorder.setWorkTypeName(WorkUtils.getAllSysTreeMap().get(workorder.getWorkType()));
		workorder.setWorkLevelName(WorkUtils.getDateTreeMap(WorkUtils.LEVELPARID).get(workorder.getWorkLevel()));
		workorder.setNewDate(new Date());
		return workorder;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WorkOpinion> getWorkOpinionList(String workId) {
		Workorder workorder = this.findWorkByWorkId(workId);
		// ----获取此工单的沟通记录
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseWorkOpinion where workId=? and opinionContent !=null order by opinionTime desc");
		List<String> values = new ArrayList<String>();
		values.add(workId);
		List<BaseWorkOpinion> baseWorkOpinionList = workOpinionDao.find(hql.toString(), values.toArray());
		List<WorkOpinion> workOpinionList = new ArrayList<WorkOpinion>();
		//获取用户信息
		List<EcmcSysUser> ecmcList = ecmcUserService.findUserByPermission(null,"java:WorkorderAllUser");//
		
		Map<String,String> ecmcMap = new HashMap<String,String>();
		for (EcmcSysUser ecmcSysUser : ecmcList) {
			ecmcMap.put(ecmcSysUser.getId(), ecmcSysUser.getName());
		}

		for (BaseWorkOpinion baseWorkOpinion : baseWorkOpinionList) {
			WorkOpinion workOpinion = new WorkOpinion();
			BeanUtils.copyPropertiesByModel(workOpinion, baseWorkOpinion);
			String replyUserName = ecmcMap.get(workOpinion.getReplyUser());
			String creUserName=ecmcMap.get(workOpinion.getCreUser());
			if("0".equals(workOpinion.getEcmcCre())){//ecmc创建
				workOpinion.setCreUserName(StringUtil.isEmpty(creUserName)?workOpinion.getCreUserName():creUserName);
				if("2".equals(workorder.getWorkCreRole())){
					workOpinion.setReplyUserName("客户");
				}else{
					workOpinion.setReplyUserName(StringUtil.isEmpty(replyUserName)?workOpinion.getReplyUserName():replyUserName);
				}
			}else{
				workOpinion.setCreUserName("客户");
				workOpinion.setReplyUserName(StringUtil.isEmpty(replyUserName)?workOpinion.getReplyUserName():replyUserName);
			}
			workOpinion.setFlagName(WorkUtils.getEcmcFlagMap().get(workOpinion.getFlag()));//ecmc状态名称
			List<WorkFile> WorkFilelist = this .getWorkFileListByOpinionId(workOpinion.getOpinionId());
			workOpinion.setWorkFile(WorkFilelist);
			workOpinionList.add(workOpinion);
		}
		if (workorder.getWorkType().equals(WorkUtils.QUOTATYPE)) {// 配额类工单
			WorkOpinion workOpinion1 = new WorkOpinion();
			workOpinion1.setCreUserName("客户");
			workOpinion1.setOpinionTime(workorder.getWorkCreTime());
			// 得到配额
			WorkQuota workQuota = this.findWorkQuotaByWorkId(workId);
			workOpinion1.setWorkQuota(workQuota);
			workOpinion1.setFlagName(WorkUtils.getEcmcFlagMap().get("0"));//待处理
			int index = workOpinionList.size();
			if(index>0 && (workorder.getWorkCreTime().equals(workOpinionList.get(index-1).getOpinionTime())
					|| workorder.getWorkCreTime().after(workOpinionList.get(index-1).getOpinionTime()))){
				workOpinionList.add(index-1, workOpinion1);
			}else{
				workOpinionList.add(index, workOpinion1);
			}
		}
		if(!StringUtil.isEmpty(workorder.getWorkHighly())){
			WorkOpinion workOpinion2 = new WorkOpinion();
			workOpinion2.setCreUser(workorder.getWorkApplyUser());
			workOpinion2.setReplyUser(workorder.getWorkHeadUser());
			workOpinion2.setCreUserName("客户");
			workOpinion2.setOpinionTime(workorder.getWorkCreTime());
			workOpinion2.setFlagName(WorkUtils.getEcmcFlagMap().get(workorder.getWorkFalg()));
			String replyUserName = ecmcMap.get(workorder.getWorkHeadUser());
			workOpinion2.setReplyUserName(StringUtil.isEmpty(replyUserName)?workorder.getWorkHeadUserName():replyUserName);
			String content="客户对您的评价为："+ ("1".equals(workorder.getWorkHighly())?"满意":"不满意");
			workOpinion2.setOpinionContent(content);
			workOpinionList.add(0,workOpinion2);
		}
		return workOpinionList;
	}
	@SuppressWarnings("unchecked")
	private List<WorkFile> getWorkFileListByOpinionId(String opinionId) {
		StringBuffer hql = new StringBuffer();
		List<String> values = new ArrayList<String>();
		hql.append("from BaseWorkFile where opinionId=?");
		values.add(opinionId);
		List<BaseWorkFile> baseWorkFileList = workFileDao.find(hql.toString(),
				opinionId);
		List<WorkFile> workFileList = new ArrayList<WorkFile>();
		for (BaseWorkFile baseWorkFile : baseWorkFileList) {
			WorkFile workFile = new WorkFile();
			BeanUtils.copyPropertiesByModel(workFile, baseWorkFile);
			// ---调用借口得到附件信息
			EayunFile eayunFile = fileService.findOneById(workFile.getSaccId());
			workFile.setEayunFile(eayunFile);
			workFileList.add(workFile);
		}
		return workFileList;
	}

	@Override
	public Page getNotDoneWorkList(Page page,ParamsMap paramsMap) {
		paramsMap.getParams().put("work", "notDoneWork");
		page=this.getWorkList(page,paramsMap);
		return page;
	}
	@Override
	public Page getDoneWorkList(Page page,ParamsMap paramsMap) {
		paramsMap.getParams().put("work", "doneWork");
		page=this.getWorkList(page,paramsMap);
		return page;
	}
	@SuppressWarnings("unchecked")
	private Page getWorkList(Page page,ParamsMap paramsMap){
		String workTitle=(String) paramsMap.getParams().get("workTitle");//工单标题
		Object bTime=paramsMap.getParams().get("beginTime");
		Object eTime=paramsMap.getParams().get("endTime");
		Date beginTime = bTime==null?null:DateUtil.timestampToDate(String.valueOf(bTime)); //开始时间
		Date endTime = eTime==null?null:DateUtil.timestampToDate(String.valueOf(eTime));//结束时间
		String applyCustomer=(String) paramsMap.getParams().get("applyCustomer"); //客户id
		String workType=(String) paramsMap.getParams().get("workType"); //工单类别
		String workLevel=(String) paramsMap.getParams().get("workLevel"); //工单级别
		String workFalg=(String) paramsMap.getParams().get("workFalg"); //工单状态
		String workHeadUser=(String) paramsMap.getParams().get("workHeadUser"); //责任人id
		String parentId = (String) paramsMap.getParams().get("parentId");//父类id
        String workNum = (String) paramsMap.getParams().get("workNum");//工单编号
        String workPhone = (String) paramsMap.getParams().get("workPhone");//联系人电话
        String workEmail = (String) paramsMap.getParams().get("workEmail");//联系人邮箱
        String cusCpname = (String) paramsMap.getParams().get("cusCpname");//联系人邮箱
        String workCusName = (String) paramsMap.getParams().get("workCusName");//联系人姓名

		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("select");
		sql.append(" wo.workid AS workId,");
		sql.append(" wo.order_num AS workNum,");
		sql.append(" wo.title AS workTitle,");
		sql.append(" wo.order_type AS workType,");
		sql.append(" wo.order_level AS workLevel,");
		sql.append(" cus.cus_id AS applyCustomer,");
		sql.append(" cus.cus_org AS workCusName,");
		sql.append(" wo.flag AS workFalg,");
		sql.append(" wo.head_user AS workHeadUser,");
		sql.append(" wo.head_username AS workHeadUserName,");
		sql.append(" wo.cre_date AS workCreTime,");
		sql.append(" workFlow.begintime AS flowBeginTime,");
		sql.append(" workFlow.endtime AS endtime,");
		sql.append(" workFlow.responsetime AS flowRespondTime,");
		sql.append(" workFlow.isrespstate AS flowRespondFalg,");
		sql.append(" sdt.node_name AS workTypeName,");
		sql.append(" sdt1.node_name AS workLevelName,");
		sql.append(" wo.order_flag AS workCreRole,");
		sql.append(" wo.apply_customer,");
		sql.append(" wo.cre_user");
		sql.append(" FROM workorder AS wo");
		sql.append(" LEFT JOIN ecmc_sys_user AS ecmcUser ON wo.head_user = ecmcUser.id");
		sql.append(" LEFT JOIN sys_selfuser AS ecscUser ON wo.apply_user = ecscUser.user_id");
		sql.append(" LEFT JOIN sys_selfcustomer AS cus ON wo.apply_customer = cus.cus_id");
		sql.append(" LEFT JOIN workorder_assess AS workFlow ON wo.workorder_assess_id = workFlow.id");
		sql.append(" LEFT JOIN sys_data_tree AS sdt ON wo.order_type = sdt.node_id");
		sql.append(" LEFT JOIN sys_data_tree AS sdt1 ON wo.order_level = sdt1.node_id");
		sql.append(" WHERE");
		if(!StringUtil.isEmpty(workFalg)){
			sql.append(" wo.flag = ?");
			list.add(workFalg);
		}else{
			if(paramsMap.getParams().get("work").equals("notDoneWork")){
				//未完成
				sql.append(" wo.flag in (0,1,2)");
			}else{
				//已完成
				sql.append(" wo.flag in (3,4,5)");
			}
		}
		if(!StringUtil.isEmpty(workTitle)){//标题
			sql.append(" and wo.title like ?");
			list.add("%"+workTitle+"%");
		}
		if(beginTime!=null){
			sql.append(" and wo.cre_date >= ?");
			list.add(beginTime);
		}
		if(endTime!=null){
			sql.append(" and wo.cre_date <= ?");
			list.add(endTime);
		}
		if(!StringUtil.isEmpty(applyCustomer)){//申请客户
			sql.append(" and (cus.cus_org like ? or wo.apply_customer like ?)");
			list.add("%" + applyCustomer + "%");
			list.add("%" + applyCustomer + "%");
		}
		if(!StringUtil.isEmpty(workType)){//类别
			sql.append(" and wo.order_type = ?");
			list.add(workType);
		}
		if(!StringUtil.isEmpty(workLevel)){//级别
			sql.append(" and wo.order_level = ?");
			list.add(workLevel);
		}

		if(!StringUtil.isEmpty(workHeadUser) && workHeadUser.equals("T-1000")){
			sql.append(" and wo.head_user is null");
		}else if (!StringUtil.isEmpty(workHeadUser) && !workHeadUser.equals("T-1000")){
			sql.append(" and wo.head_user = ?");
			list.add(workHeadUser);
		}
		if(!StringUtil.isEmpty(parentId) && !parentId.contains(",")){
			sql.append(" and sdt.parent_id=?");
			list.add(parentId);
		}
        if(!StringUtil.isEmpty(workNum)){//联系电话
            sql.append(" and wo.order_num like ?");
            list.add("%"+workNum+"%");
        }
        if(!StringUtil.isEmpty(workPhone)){//联系邮箱
            sql.append(" and wo.phone like ?");
            list.add("%"+workPhone+"%");
        }
        if(!StringUtil.isEmpty(workEmail)){//联系邮箱
            sql.append(" and wo.work_email like ?");
            list.add("%"+workEmail+"%");
        }
        if(!StringUtil.isEmpty(cusCpname)){//公司中文名称
            sql.append(" and cus.cus_cpname like ?");
            list.add("%"+cusCpname+"%");
        }
        if(!StringUtil.isEmpty(workCusName)){//联系人姓名
        	sql.append(" and cus.cus_name like ?");
        	list.add("%"+workCusName+"%");
        }
		sql.append(" order by");
		if(paramsMap.getParams().get("work").equals("notDoneWork")){
			//未完成
			sql.append(" wo.work_complain desc ,");
		}
		sql.append(" wo.flag asc, wo.cre_date desc");

		QueryMap queryMap = new QueryMap();
		int pageSize = paramsMap.getPageSize();
		int pageNumber = paramsMap.getPageNumber();
		queryMap.setPageNum(pageNumber);
		queryMap.setCURRENT_ROWS_SIZE(pageSize);
		page=workDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
		List<Object> ObjList = (List<Object>) page.getResult();
		//状态名称
		Map<String,String> ecmcFlagMap=WorkUtils.getEcmcFlagMap();
		if(ObjList!=null && ObjList.size()>0){
			int i=0;
			Date date=new Date();
			for (Object object : ObjList) {
				Object[] obj = (Object[]) object;
				Workorder workorder = new Workorder();
				workorder.setWorkId((String)obj[0]);
				workorder.setWorkNum((String)obj[1]);
				workorder.setWorkTitle((String)obj[2]);
				workorder.setWorkType((String)obj[3]);
				workorder.setWorkLevel((String)obj[4]);
				workorder.setApplyCustomer((String)obj[5]);
				workorder.setWorkCusName(obj[5]==null?(String)obj[18]:(obj[6]==null?null:(String)obj[6]));
				workorder.setWorkFalg((String)obj[7]);
				workorder.setWorkHeadUser((String)obj[8]);
				workorder.setWorkHeadUserName(obj[8]==null?"------":(obj[9]==null?(String)obj[8]:(String)obj[9]));
				workorder.setWorkCreTime((Date)obj[10]);
				workorder.setFlowBeginTime((Date)obj[11]);
				workorder.setEndtime((Date)obj[12]);
				workorder.setFlowRespondTime((Date)obj[13]);
				workorder.setFlowRespondFalg((String)obj[14]);
				workorder.setWorkTypeName((String)obj[15]);
				workorder.setWorkLevelName((String)obj[16]);
				workorder.setWorkCreRole((String)obj[17]);
				workorder.setWorkCreUser(obj[19]==null?null:(String)obj[19]);
				workorder.setWorkFalgName(ecmcFlagMap.get(workorder.getWorkFalg()));
				workorder.setNewDate(date);
				ObjList.set(i, workorder);
				i++;
			}
		}
		return page;
	}
	@Override
	public Workorder updateEcmcWorkorder(Map<String, String> map) throws Exception {
		String workId = map.get("workId");
		String workTitle = map.get("workTitle");
		String workContent = map.get("workContent");
		String userId = map.get("userId");
		String userName = map.get("userName");
		Workorder workorder = new Workorder();
		BaseWorkorder baseWork = workDao.findOne(workId);
		Workorder oldWorkorder = new Workorder();
		BeanUtils.copyPropertiesByModel(oldWorkorder, baseWork);
		boolean bool = false;
		StringBuffer content = new StringBuffer();
		if(!workTitle.equals(baseWork.getWorkTitle())){
			content.append("工单标题：‘"+baseWork.getWorkTitle()+"’");
			content.append("变更为：‘"+workTitle+"’");
			baseWork.setWorkTitle(workTitle);
			bool=true;
		}
		if(!workContent.equals(baseWork.getWorkContent())){
			content.append("工单内容：‘"+baseWork.getWorkContent()+"’");
			content.append("变更为：‘"+workContent+"’");
			baseWork.setWorkContent(workContent);
			bool=true;
		}
		BeanUtils.copyPropertiesByModel(workorder, baseWork);
		workorder = this.extendWorkorder(workorder);
		//获取操作者信息
		EcmcSysUser ecmcUser= ecmcUserService.findUserById(userId);
		if(bool){
			workDao.merge(baseWork);
			//---发送邮件
			BaseWorkOpinion baseWorkOpinion = new BaseWorkOpinion();
			baseWorkOpinion.setWorkId(baseWork.getWorkId());
			baseWorkOpinion.setCreUser(userId);
			baseWorkOpinion.setCreUserName(userName);
			baseWorkOpinion.setEcmcCre("0");
			baseWorkOpinion.setFlag(baseWork.getWorkFalg());
			baseWorkOpinion.setOpinionContent(content.toString());
			baseWorkOpinion.setOpinionState(null);
			baseWorkOpinion.setOpinionTime(new Date());
			baseWorkOpinion.setReplyUser(baseWork.getWorkCreUser());
			baseWorkOpinion.setReplyUserName(baseWork.getWorkCreUserName());
			workOpinionDao.save(baseWorkOpinion);
			ecmcWorkMailService.sendEmailMessageForEdit(workorder, oldWorkorder,baseWorkOpinion,ecmcUser);
		}
		return workorder;
	}

	@Override
	public Workorder updateEcmcWorkForWorkLevel(Map<String, String> map) throws Exception {
		String workId = map.get("workId");
		String workLevel = map.get("workLevel");
		String userId = map.get("userId");
		String userName = map.get("userName");
		BaseWorkorder baseWorkorder = workDao.findOne(workId);
		Workorder oldWorkorder = new Workorder();
		BeanUtils.copyPropertiesByModel(oldWorkorder, baseWorkorder);
		if(workLevel.equals(baseWorkorder.getWorkLevel())){
			return oldWorkorder;
		}
		oldWorkorder = this.extendWorkorder(oldWorkorder);
		BaseWorkOpinion baseWorkOpinion= new BaseWorkOpinion();
		baseWorkOpinion.setCreUser(userId);
		baseWorkOpinion.setEcmcCre("0");
		baseWorkOpinion.setFlag(oldWorkorder.getWorkFalg());
		baseWorkOpinion.setWorkId(workId);
		baseWorkOpinion.setOpinionTime(new Date());
		Map<String,String> levelMap = WorkUtils.getDateTreeMap(WorkUtils.LEVELPARID);
		String replyUser = baseWorkorder.getWorkCreUser();
		baseWorkOpinion.setReplyUser(replyUser);
		baseWorkOpinion.setCreUserName(userName);
		baseWorkOpinion.setReplyUserName(baseWorkorder.getWorkCreUserName());
		baseWorkOpinion.setOpinionContent("工单级别已从"+oldWorkorder.getWorkLevelName()+"更改为"+levelMap.get(workLevel));
		workOpinionDao.save(baseWorkOpinion);

		Workorder workorder = new Workorder();
		baseWorkorder.setWorkLevel(workLevel);
		BeanUtils.copyPropertiesByModel(workorder, baseWorkorder);
		workorder = this.extendWorkorder(workorder);

		baseWorkorder=(BaseWorkorder) workDao.merge(baseWorkorder);

		EcmcSysUser ecmcUser = ecmcUserService.findUserById(userId);

		ecmcWorkMailService.sendEmailMessageForEdit(workorder, oldWorkorder,baseWorkOpinion, ecmcUser);
		return workorder;
	}

	@Override
	public Workorder auditNotPassWork(Map<String, String> map) throws Exception {
		String workId = map.get("workId");
		String userId = map.get("userId");
		String beCntent = map.get("cusReason");
		String userName = map.get("userName");
		//获取工单信息
		BaseWorkorder baseWorkorder = workDao.findOne(workId);
		//新添加回复
		BaseWorkOpinion baseWorkOpinion = new BaseWorkOpinion();
		baseWorkOpinion.setOpinionId(SeqManager.getSeqMang().getSeqForDate());
		baseWorkOpinion.setCreUser(userId);
		baseWorkOpinion.setReplyUserName(userName);
		baseWorkOpinion.setOpinionTime(new Date());
		baseWorkOpinion.setWorkId(workId);
		baseWorkOpinion.setReplyUser(baseWorkorder.getWorkApplyUser());
		baseWorkOpinion.setReplyUserName(baseWorkorder.getWorkApplyUser());
		baseWorkOpinion.setFlag(baseWorkorder.getWorkFalg());
		baseWorkOpinion.setOpinionContent("您的"+baseWorkorder.getWorkTitle()+"工单未通过审核");
		baseWorkOpinion.setEcmcCre("0");
		workOpinionDao.save(baseWorkOpinion);
		WorkOpinion workOpinion = new WorkOpinion();
		BeanUtils.copyPropertiesByModel(workOpinion, baseWorkOpinion);
		//判断工工单是否是注册类
		String prjId=null;
		if(WorkUtils.REGISTERTYPE.equals(baseWorkorder.getWorkType())){
			//得到客户信息
			Customer customer = ecmcCusService.getCustomerById(baseWorkorder.getApplyCustomer());
			customer.setCusFalg('2');
			customer.setCusReason(beCntent);
			ecmcCusService.updateCustomer(customer);
		}else if(WorkUtils.QUOTATYPE.equals(baseWorkorder.getWorkType())){
			//配额累工单
			List<String> list = new ArrayList<>();
			list.add(baseWorkorder.getWorkId());
			BaseWorkQuota baseWorkQuota= (BaseWorkQuota) workQuotaDao.findUnique("from BaseWorkQuota where workId=?" ,list.toArray());
			prjId=baseWorkQuota.getPrjId();
		}
		//修改工单信息
		baseWorkorder.setSendMesFlag("3");
		baseWorkorder.setWorkState("2");
		//发送邮件和短信
		Workorder workorder = new Workorder();
		BeanUtils.copyPropertiesByModel(workorder, baseWorkorder);
		//------扩展属性---
		workorder = this.extendWorkorder(workorder);
		//------扩展属性结束----
		if(WorkUtils.REGISTERTYPE.equals(baseWorkorder.getWorkType())){
			ecmcWorkMailService.sendMailAndSms(workorder, workOpinion);
		}
		baseWorkorder.setSendMesFlag("4");
		workDao.merge(baseWorkorder);
		BeanUtils.copyPropertiesByModel(workorder, baseWorkorder);

		workorder.setPrjId(prjId);
		return workorder;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WorkReport> countAllUserAcceptWorkorder(Map<String, String> map) {
		String workType = map.get("workType");
		String preiodType = map.get("preiodType");
		String parentId = map.get("parentId");
		StringBuffer sql = new StringBuffer();
		sql.append("select wo.head_user,count(wo.workid) as num,wo.head_username workHeadUserName");
		sql .append(" from workorder wo");
		sql .append(" left join workorder_assess wa on wa.id=wo.workorder_assess_id");
		sql .append(" left join ecmc_sys_user ecmcUser on ecmcUser.id = wo.head_user");
		sql .append(" left join sys_data_tree AS sdt ON wo.order_type = sdt.node_id");
		sql .append(" where wo.flag='3'");
		List<Object> values =  new ArrayList<>();
		if(!StringUtil.isEmpty(workType)){
			sql.append(" and wo.order_type =?");
			values.add(workType);
		}
		if(!StringUtil.isEmpty(parentId) && !parentId.contains(",")){
			sql.append(" and sdt.parent_id=?");
			values.add(parentId);
		}
		Date date = new Date();
		if("week".equals(preiodType)){//一周
			sql.append("and wa.endtime between ? and ?");
			values.add(WorkUtils.getWeekBegTime(date));
			values.add(date);
		}else if("month".equals(preiodType)){
			sql.append("and wa.endtime between ? and ?");
			values.add(WorkUtils.getMonthBegTime(date));
			values.add(date);
		}else{
			sql.append("and wa.endtime between ? and ?");
			values.add(WorkUtils.getYearBegTime(date));
			values.add(date);
		}
		sql.append(" group by wo.head_user");
		Query query = workDao.createSQLNativeQuery(sql.toString(),values.toArray());
		List<Object> list = query.getResultList();
		List<WorkReport> workList=new ArrayList<WorkReport>();
		for (Object object : list) {
			Object[] obj = (Object[]) object;
			WorkReport workReport = new WorkReport();
			workReport.setWorkHeadUser(String.valueOf(obj[0]));
			workReport.setNum(Integer.valueOf(obj[1].toString()));
			workReport.setWorkHeadUserName(obj[2]==null?String.valueOf(obj[0]):String.valueOf(obj[2]));
			workList.add(workReport);
		}

		return workList;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public List<WorkReport> countUserAcceptWorkorder(Map<String, String> map) {
		String headUser = map.get("headUser");
		String preiodType = map.get("preiodType");
		String parentId = map.get("parentId");
		StringBuffer sql = new StringBuffer();
		sql .append("select wa.begintime, wa.endtime, wa.responsetime, wo.head_user,");
		sql .append(" sdr.node_id orderType, sdr1.node_id orderLevel,wo.head_username,");
		sql .append(" sdr.node_name orderTypeName,sdr1.node_name orderLevelName");
		sql .append(" from workorder wo");
		sql .append(" left join workorder_assess wa on wo.workid = wa.work_id and wa.id=wo.workorder_assess_id");
		sql .append(" left join sys_data_tree sdr on sdr.node_id = wo.order_type");
		sql .append(" left join sys_data_tree sdr1 on sdr1.node_id = wo.order_level");
		sql .append(" left join ecmc_sys_user ecmcUser on ecmcUser.id = wo.head_user");
		sql .append(" where wo.flag='3' and wo.head_user=?");
		Date date = new Date();
		List<Object> values = new ArrayList<Object>();
		values.add(headUser);
		if(!StringUtil.isEmpty(parentId) && !parentId.contains(",")){
			sql.append(" and sdr.parent_id=?");
			values.add(parentId);
		}
		if("week".equals(preiodType)){//一周
			sql.append("and wa.endtime between ? and ?");
			values.add(WorkUtils.getWeekBegTime(date));
			values.add(date);
		}else if("month".equals(preiodType)){
			sql.append("and wa.endtime between ? and ?");
			values.add(WorkUtils.getMonthBegTime(date));
			values.add(date);
		}else{
			sql.append("and wa.endtime between ? and ?");
			values.add(WorkUtils.getYearBegTime(date));
			values.add(date);
		}
		//查询指定人员完成的工单
		Query query = workDao.createSQLNativeQuery(sql.toString(),values.toArray());
		List<Object> list = query.getResultList();
		List<WorkReport> workList=new ArrayList<WorkReport>();
		Map<String,String> numMap= new HashMap<String,String>();
		int doneNum =0;//按时完成数
		int notDoneNum=0;//未按时完成数
		String value="";
		for (Object object : list) {
			Object[] obj = (Object[]) object;
			Date beginTime =  DateUtil.stringToDate(String.valueOf(obj[0]));//获取结束时间
			Date endtime =  DateUtil.stringToDate(String.valueOf(obj[1]));//获取结束时间
			Date respondTime =DateUtil.stringToDate(String.valueOf(obj[2]));//获取响应时间
			int diff =0;

			if(beginTime.after(respondTime)){
				diff = (int) (endtime.getTime()-beginTime.getTime());
			}else{
				diff = (int) (endtime.getTime()-respondTime.getTime());
			}
			if(numMap.get(String.valueOf(obj[4]))==null){
				WorkReport workReport = new WorkReport();
				workReport.setBeginTime(beginTime);
				workReport.setEndtime(endtime);
				workReport.setRespondTime(respondTime);
				workReport.setWorkHeadUser(String.valueOf(obj[3]));
				workReport.setWorkType(String.valueOf(obj[4]));
				workReport.setWorkLevel(String.valueOf(obj[5]));
				workReport.setWorkHeadUserName(obj[6]==null ?String.valueOf(obj[3]):String.valueOf(obj[6]));
				workReport.setWorkTypeName(String.valueOf(obj[7]));
				workReport.setWorkLevelName(String.valueOf(obj[8]));
				workList.add(workReport);
				if(diff>(WorkUtils.getLevelMap().get(String.valueOf(obj[5])))){//受理时间比较
					value ="0-1";//未按时完成
				}else{
					value ="1-0";//按时完成
				}
				numMap.put(String.valueOf(obj[4]), value);
			}else{
				value = numMap.get(String.valueOf(obj[4]));
				doneNum = Integer.parseInt(value.split("-")[0]);//分割数组，下标为0代表按时完成个数，下标为1代表未按时完成个数
				notDoneNum = Integer.parseInt(value.split("-")[1]);
				if(diff>(WorkUtils.getLevelMap().get(String.valueOf(obj[5])))){//受理时间比较
					value = doneNum + "-" + (notDoneNum+1);//未按时完成
				}else{
					value = (doneNum+1) + "-" + notDoneNum;//按时完成
				}
				numMap.put(String.valueOf(obj[4]), value);
			}
		}
		if(workList!=null && workList.size()>0){
			int index=0;
			for(WorkReport workRep : workList){
				int workDoneNum = Integer.valueOf(numMap.get(workRep.getWorkType()).split("-")[0]);
				int workNotDoneNum = Integer.valueOf(numMap.get(workRep.getWorkType()).split("-")[1]);
				workRep.setDoneNum(workDoneNum);
				workRep.setNotDoneNum(workNotDoneNum);
				workList.set(index, workRep);
				index++;
			}
		}
		return workList;
	}
	@Override
	public List<SysDataTree> getNoDoneFlagList() {
		return WorkUtils.getNoDoneFlagList();
	}

	@Override
	public List<SysDataTree> getDoneFlagList() {
		return WorkUtils.getDoneFlagList();
	}

	@Override
	public List<SysDataTree> getWorkFlagList() {
		return WorkUtils.getEcscFlagList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<EcmcSysUser> getWorkHeadList(String type,String parentId) {
		StringBuffer hql = new StringBuffer();
		hql.append("select wo.head_user,wo.head_userName");
		hql.append(" from workorder wo");
		hql.append(" left join sys_data_tree sdt on wo.order_type = sdt.node_id");
		hql.append(" where 1=1");
		List<String> values = new ArrayList<String>();
		if(!parentId.contains(",")){
			hql.append(" and sdt.parent_id=?");
			values.add(parentId);
		}
		if ("Y".equals(type)) {// 已完成的负责人
			hql.append(" and wo.flag in('3','4','5')");
		} else if ("N".equals(type)) {// 未完成的负责人
			hql.append(" and wo.flag in ('0', '1', '2')");
		}
		hql.append(" group by wo.head_user");
		List<EcmcSysUser> sysUserList=new ArrayList<EcmcSysUser>();
		Query query= workDao.createSQLNativeQuery(hql.toString(),values.toArray());
		List<Object> list = query.getResultList();
		for (Object object : list) {
			EcmcSysUser ecmcSysUser = new EcmcSysUser();
			Object[] obj = (Object[]) object;
			ecmcSysUser.setId(String.valueOf(obj[0]==null?"T-1000":obj[0]));
			ecmcSysUser.setName(String.valueOf(obj[1]==null ?"------":obj[1]));
			sysUserList.add(ecmcSysUser);
		}
		return sysUserList;
	}
	private Workorder extendWorkorder(Workorder workorder){
		if(workorder.getApplyCustomer()!=null){
			Customer ecscCus = ecmcCusService.getCustomerById(workorder.getApplyCustomer());//申请客户
			if(ecscCus!=null){
				workorder.setWorkCusName(StringUtil.isEmpty(ecscCus.getCusId()) ? workorder.getApplyCustomer():ecscCus.getCusOrg());//所属客户
			}else{
				workorder.setWorkCusName(workorder.getApplyCustomer());
			}
		}
		BaseWorkFlow baseWorkFlow = workFlowDao.findOne(workorder.getFlowId());
		workorder.setFlowBeginTime(baseWorkFlow.getFlowBeginTime());
		workorder.setFlowRespondTime(baseWorkFlow.getFlowRespondTime());
		workorder.setEndtime(baseWorkFlow.getEndtime());
		workorder.setFlowRespondFalg(baseWorkFlow.getFlowRespondFalg());
		workorder.setWorkFalgName(WorkUtils.getEcmcFlagMap().get(workorder.getWorkFalg()));//ecmc状态名称
		workorder.setWorkEcscFalgName(WorkUtils.getEcscFlagMap().get(workorder.getWorkEcscFalg()));
		workorder.setWorkLevelName(WorkUtils.getDateTreeMap(WorkUtils.LEVELPARID).get(workorder.getWorkLevel()));//级别名称
		workorder.setWorkTypeName(WorkUtils.getAllSysTreeMap().get(workorder.getWorkType()));//类别名称
		workorder.setNewDate(new Date());
		return workorder;
	}

	/*
	 * 因一个配额工单只有一条配额记录，所以取第一条
	 */
	@SuppressWarnings("unchecked")
	private WorkQuota findWorkQuotaByWorkId(String workId) {
		StringBuffer hql = new StringBuffer();
		hql.append("from BaseWorkQuota where workId=?");
		List<WorkQuota> workQuotaList = workQuotaDao.find(hql.toString(),workId);
		WorkQuota workQuota = new WorkQuota();
		if (workQuotaList.size() > 0) {
			BaseWorkQuota baseWorkQuota = workQuotaList.get(0);
			BeanUtils.copyPropertiesByModel(workQuota, baseWorkQuota);
		}
		return workQuota;
	}
	/*
	 * 工单有回复的状态变化
	 * 工单状态，回复中工单最新状态
	 * 1-1：判断是否改变响应状态，修改响应状态与时间
	 * 2-2：无变化
	 *
	 * 1-2：工单变更为已解决，修改流程结束时间
	 * 2-1：打回--添加新流程，修改旧流程
	 * 2-3：已完成,无特殊操作
	 * ？-4：已取消,无特殊操作
	 */
	@Override
	public WorkOpinion addEcmcWorkopinion(WorkOpinion workOpinion) throws Exception {
		//得到工单信息opinionState
		BaseWorkorder baseWorkorder = workDao.findOne(workOpinion.getWorkId());
		if(!baseWorkorder.getWorkEcscFalg().equals(workOpinion.getWorkEcscFalg())){
			return null;
		}
		if(StringUtil.isEmpty(baseWorkorder.getSendMesFlag())){
			baseWorkorder.setSendMesFlag("0");
		}
		String headUser =baseWorkorder.getWorkHeadUser();
		if(workOpinion.getCreUser().equals(baseWorkorder.getWorkHeadUser())){//当前登陆者@责任人
			workOpinion.setReplyUser(baseWorkorder.getWorkCreUser());
			workOpinion.setReplyUserName(baseWorkorder.getWorkCreUserName());
		}else{//当前登陆者@创建者
			workOpinion.setReplyUser(headUser!=null?headUser:null);
			workOpinion.setReplyUserName(baseWorkorder.getWorkHeadUserName());
		}
		workOpinion.setOpinionTime(new Date());
		//得到工单的流程
		BaseWorkFlow baseWorkFlow = workFlowDao.findOne(baseWorkorder.getFlowId());
		//工单状态发生改变或者是未响应状态
		boolean bool = false;
		//变更带反馈
		if("2".equals(baseWorkorder.getWorkCreRole()) && ("1".equals(baseWorkorder.getWorkFalg()) && "1".equals(baseWorkorder.getWorkEcscFalg()))){
			bool=true;
		}
		if(!baseWorkorder.getWorkFalg().equals(workOpinion.getFlag()) || "0".equals(baseWorkFlow.getFlowRespondFalg()) || bool){
			this.editWorkorderForAddWorkOpinion(baseWorkorder,workOpinion,baseWorkFlow);
		}
		BaseWorkOpinion baseWorkOpinion = new BaseWorkOpinion();
		BeanUtils.copyPropertiesByModel(baseWorkOpinion, workOpinion);
		workOpinionDao.save(baseWorkOpinion);
		workOpinion.setWorkTitle(baseWorkorder.getWorkTitle());
		return workOpinion;
	}
	private void editWorkorderForAddWorkOpinion(BaseWorkorder baseWorkorder,WorkOpinion workOpinion,BaseWorkFlow baseWorkFlow)throws Exception{
		// 1-1：判断是否改变响应状态
		boolean bool=true;
		if(!"2".equals(baseWorkorder.getWorkCreRole()) && baseWorkorder.getWorkFalg().equals(workOpinion.getFlag())){
			bool=false;//变更为待反馈，不是ecsc创建的也不发送
		}
		if("1".equals(workOpinion.getFlag())&& "0".equals(baseWorkFlow.getFlowRespondFalg())){
			bool=false;//响应不发送邮件
			//工单最新状态为处理中，并且未响应
			//角色为客服人员，回复时，相应状态不变更
			boolean roleFlag = true;
			List<BaseEcmcSysRole> roles = EcmcSessionUtil.getUserRoles();
			if(!roles.isEmpty()){
				for(BaseEcmcSysRole sysRole:roles){
					if(sysRole.getId().equals(EcmcRoleIds.CUSTOMER_SERVICE)){
						roleFlag = false;
					}
				}
			}
			if(roleFlag){
				baseWorkFlow.setFlowRespondFalg("1");//已响应状态
			}else{
				baseWorkFlow.setFlowRespondFalg("0");//未响应状态
			}
			baseWorkorder.setWorkEcscFalg("1");//ecsc状态变成处理中
			baseWorkFlow.setFlowRespondTime(workOpinion.getOpinionTime());//响应时间
			workFlowDao.merge(baseWorkFlow);
		}else if(!baseWorkorder.getWorkFalg().equals(workOpinion.getFlag())){//工单最新状态发生变化
			baseWorkorder.setWorkFalg(workOpinion.getFlag());//修改工单状态
			baseWorkFlow.setFlowFlag(workOpinion.getFlag());//修改流程状态
			if("2".equals(workOpinion.getFlag())){
				baseWorkFlow.setEndtime(workOpinion.getOpinionTime());
				baseWorkorder.setWorkEcscFalg("3");
				baseWorkorder.setSendMesFlag("3");
			}else if("1".equals(workOpinion.getOpinionState())){//工单打回
				baseWorkorder.setSendMesFlag("2");
				baseWorkorder.setWorkEcscFalg("1");//处理中
				baseWorkorder.setWorkFalg("1");//处理中
				//新建流程
				Date date = new Date();
				//计算以前的时间差
				long  now = baseWorkFlow.getEndtime().getTime()-baseWorkFlow.getFlowBeginTime().getTime();
				//响应时间应该不便
				long responseTime = baseWorkFlow.getFlowRespondTime().getTime()-baseWorkFlow.getFlowBeginTime().getTime();
				baseWorkFlow = new BaseWorkFlow();
				baseWorkFlow.setFlowBeginTime(new Date(date.getTime()-now));
				baseWorkFlow.setFlowRespondTime(new Date(baseWorkFlow.getFlowBeginTime().getTime()+responseTime));
				baseWorkFlow.setFlowCreTime(workOpinion.getOpinionTime());
				baseWorkFlow.setFlowFlag(workOpinion.getFlag());
				baseWorkFlow.setFlowId(SeqManager.getSeqMang().getSeqForDate());
				baseWorkFlow.setFlowRespondFalg("1");
				baseWorkFlow.setFlowState("2");//回退
				baseWorkFlow.setUserIdHead(workOpinion.getReplyUser());
				baseWorkFlow.setWorkId(workOpinion.getWorkId());
				workFlowDao.save(baseWorkFlow);
			}else if("4".equals(workOpinion.getFlag())){//取消--取消
				baseWorkFlow.setEndtime(workOpinion.getOpinionTime());
				baseWorkorder.setWorkEcscFalg("7");
			}else if("3".equals(workOpinion.getFlag())){//完成--关闭
				baseWorkFlow.setEndtime(workOpinion.getOpinionTime());
				baseWorkorder.setWorkEcscFalg("5");
			}
			workFlowDao.merge(baseWorkFlow);//修改流程
			baseWorkorder.setWorkFalg(workOpinion.getFlag());//改变工单状态
			workOpinion.setLogName(WorkUtils.getLogNameMap().get(baseWorkorder.getWorkFalg()));
		}else{
			baseWorkorder.setWorkEcscFalg("2");
			baseWorkorder.setSendMesFlag("3");
		}
		baseWorkorder.setFlowId(baseWorkFlow.getFlowId());
		workDao.merge(baseWorkorder);//修改工单
		if(bool){
			//---发送邮件
			Workorder workorder = new Workorder();
			BeanUtils.copyPropertiesByModel(workorder, baseWorkorder);
			//------扩展属性---
			workorder = this.extendWorkorder(workorder);
			//-----扩展属性结束
			ecmcWorkMailService.sendMailAndSms(workorder, workOpinion);
		}
	}
	@Override
	public Workorder trunToOtherUser(Map<String, String> map) throws Exception {
		String workId = map.get("workId");
		String headUserId = map.get("headUserId");
        String headUserName = map.get("headUserName");
		String userId = map.get("userId");
		String userName = map.get("userName");//操作者名称

		//得到工单信息
		BaseWorkorder baseWork = workDao.findOne(workId);

		//得工单对应的流程信息
		BaseWorkFlow baseWorkFlow = (BaseWorkFlow) workFlowDao.findOne(baseWork.getFlowId());
		//责任人信息
		EcmcSysUser baseEcmcUser = ecmcUserService.findUserById(headUserId);
		Date date = new Date();
		//结束此流程
		baseWorkFlow.setEndtime(date);
		workFlowDao.merge(baseWorkFlow);
		//新启动流程
		BaseWorkFlow newBaseWorkFlow= new BaseWorkFlow();
		newBaseWorkFlow.setFlowBeginTime(date);
		newBaseWorkFlow.setFlowCreTime(date);
		newBaseWorkFlow.setFlowFlag(baseWork.getWorkFalg());
		String flowId=SeqManager.getSeqMang().getSeqForDate();
		newBaseWorkFlow.setFlowId(flowId);
		newBaseWorkFlow.setFlowRespondFalg("0");
		newBaseWorkFlow.setFlowState("1");//转办
		newBaseWorkFlow.setUserIdHead(headUserId);
		newBaseWorkFlow.setWorkId(workId);
		workFlowDao.save(newBaseWorkFlow);
		Workorder workorder = new Workorder();
		BeanUtils.copyPropertiesByModel(workorder, baseWork);
		workorder = this.extendWorkorder(workorder);
		if(!userId.equals(baseWork.getWorkHeadUser())){//操作者不是责任人---指派
			//给前责任人回复
			BaseWorkOpinion baseWorkOpinion = new BaseWorkOpinion();
			baseWorkOpinion.setCreUser(userId);
			baseWorkOpinion.setFlag(baseWork.getWorkFalg());
			baseWorkOpinion.setOpinionContent("工单["+baseWork.getWorkTitle()+"]已指派给["+baseEcmcUser.getName()+"]正在等待其响应！");
			baseWorkOpinion.setOpinionState("0");//转办
			baseWorkOpinion.setOpinionTime(date);
			baseWorkOpinion.setReplyUser(baseWork.getWorkHeadUser());
			baseWorkOpinion.setWorkId(workId);
			baseWorkOpinion.setEcmcCre("0");
            baseWorkOpinion.setCreUserName(userName);
            baseWorkOpinion.setReplyUserName(baseWork.getWorkHeadUserName());
			workOpinionDao.save(baseWorkOpinion);
			//---发送邮件
			WorkOpinion workOpinion = new WorkOpinion();
			BeanUtils.copyPropertiesByModel(workOpinion, baseWorkOpinion);
			ecmcWorkMailService.sendMailAndSms(workorder, workOpinion);
		}
		//修改工单责任人，发送邮件的状态
		baseWork.setFlowId(flowId);
		baseWork.setWorkHeadUser(headUserId);
        baseWork.setWorkHeadUserName(headUserName);
		baseWork.setSendMesFlag("2");
		workDao.merge(baseWork);
		//给新的责任人回复
		BaseWorkOpinion baseWorkOpinion = new BaseWorkOpinion();
		baseWorkOpinion.setCreUser(userId);
		baseWorkOpinion.setFlag(baseWork.getWorkFalg());
		baseWorkOpinion.setOpinionContent("工单["+baseWork.getWorkTitle()+"]已指派给["+baseEcmcUser.getName()+"]正在等待其响应！");
		baseWorkOpinion.setOpinionState("0");//转办
		baseWorkOpinion.setOpinionTime(date);
		baseWorkOpinion.setReplyUser(baseWork.getWorkHeadUser());
		baseWorkOpinion.setWorkId(workId);
		baseWorkOpinion.setEcmcCre("0");
        baseWorkOpinion.setCreUserName(userName);
        baseWorkOpinion.setReplyUserName(headUserName);
		workOpinionDao.save(baseWorkOpinion);
		WorkOpinion workOpinion = new WorkOpinion();
		BeanUtils.copyPropertiesByModel(workOpinion, baseWorkOpinion);

		//------扩展属性---
		workorder = this.extendWorkorder(workorder);
		//-----扩展属性结束
		//---发送邮件
		ecmcWorkMailService.sendMailAndSms(workorder, workOpinion);
		return workorder;
	}

	@Override
	public Workorder auditPassWork(Map<String, String> map) throws Exception {
		String workId = map.get("workId");
		String userId = map.get("userId");
		String cusReason = map.get("cusReason");
		String userName = map.get("userName");
		//获取工单信息
		BaseWorkorder baseWorkorder = workDao.findOne(workId);
		//新添加回复
		BaseWorkOpinion baseWorkOpinion = new BaseWorkOpinion();
		baseWorkOpinion.setOpinionId(SeqManager.getSeqMang().getSeqForDate());
		baseWorkOpinion.setCreUser(userId);
		baseWorkOpinion.setCreUserName(userName);
		baseWorkOpinion.setOpinionTime(new Date());
		baseWorkOpinion.setWorkId(workId);
		baseWorkOpinion.setReplyUser(baseWorkorder.getWorkApplyUser());
		baseWorkOpinion.setReplyUserName(baseWorkorder.getWorkApplyUserName());
		baseWorkOpinion.setFlag(baseWorkorder.getWorkFalg());
		baseWorkOpinion.setOpinionContent("您的"+baseWorkorder.getWorkTitle()+"工单已通过审核");
		baseWorkOpinion.setEcmcCre("0");
		
		workOpinionDao.save(baseWorkOpinion);
		WorkOpinion workOpinion = new WorkOpinion();
		BeanUtils.copyPropertiesByModel(workOpinion, baseWorkOpinion);
		//判断工工单是否是注册类
		String prjId=null;
		if(WorkUtils.REGISTERTYPE.equals(baseWorkorder.getWorkType())){
			//得到客户信息
			Customer customer = ecmcCusService.getCustomerById(baseWorkorder.getApplyCustomer());
			customer.setCusFalg('1');
			customer.setCusReason(cusReason);
			ecmcCusService.updateCustomer(customer);
		}else if(WorkUtils.QUOTATYPE.equals(baseWorkorder.getWorkType())){
			//配额累工单
			List<String> list = new ArrayList<>();
			list.add(baseWorkorder.getWorkId());
			BaseWorkQuota baseWorkQuota= (BaseWorkQuota) workQuotaDao.findUnique("from BaseWorkQuota where workId=?" ,list.toArray());
			prjId=baseWorkQuota.getPrjId();
		}
		//修改工单信息
		baseWorkorder.setSendMesFlag("3");
		baseWorkorder.setWorkState("1");
		//发送邮件和短信
		Workorder workorder = new Workorder();
		BeanUtils.copyPropertiesByModel(workorder, baseWorkorder);
		//------扩展属性---
		workorder = this.extendWorkorder(workorder);
		//------扩展属性结束----
		if(WorkUtils.REGISTERTYPE.equals(baseWorkorder.getWorkType())){
			ecmcWorkMailService.sendMailAndSms(workorder, workOpinion);
		}
		baseWorkorder.setSendMesFlag("4");
		workDao.merge(baseWorkorder);
		BeanUtils.copyPropertiesByModel(workorder, baseWorkorder);
		workorder.setPrjId(prjId);
		return workorder;
	}

	/**
	 * 获取ecmc有接受未按时受理和响应的工单时接受邮件的用户
	 */
	@Override
	public List<EcmcSysUser> getEcmcAdmin() {
		List<EcmcSysUser> ecmcUserList =ecmcUserService.findUserByPermission(null,"java:acceptOrderIfNoOneProcess");
		return ecmcUserList;
	}
	/**
	 * 获取ecmc有普通工单接受邮件的用户
	 *
	 */
	@Override
	public List<EcmcSysUser> getEcmcAdminAndCpis(String userName) {
		List<EcmcSysUser> ecmcUserList=ecmcUserService.findUserByPermission(userName,"java:acceptMormalOrder");
		return ecmcUserList;
	}

	@Override
	public List<EcmcSysUser> getEcmcAdminAndCpis() {
		List<EcmcSysUser> ecmcUserList=ecmcUserService.findUserByPermission(null,"java:trueToOtherUser");
		return ecmcUserList;
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Workorder> getWorkorderListByFlag() {
		StringBuffer sql = new StringBuffer();
		sql.append("select");
		sql.append(" wo.workid AS workId,");
		sql.append(" wo.order_num AS workNum,");
		sql.append(" wo.title AS workTitle,");
		sql.append(" wo.order_type AS workType,");
		sql.append(" wo.order_level AS workLevel,");
		sql.append(" wo.apply_customer AS applyCustomer,");
		sql.append(" cus.cus_org AS workCusName,");
		sql.append(" wo.flag AS workFalg,");
		sql.append(" wo.head_user AS workHeadUser,");
		sql.append(" ecmcUser.`name` AS workHeadUserName,");
		sql.append(" wo.cre_date AS workCreTime,");
		sql.append(" workFlow.begintime AS flowBeginTime,");
		sql.append(" workFlow.endtime AS endtime,");
		sql.append(" workFlow.responsetime AS flowRespondTime,");
		sql.append(" workFlow.isrespstate AS flowRespondFalg,");
		sql.append(" sdt.node_name AS workTypeName,");
		sql.append(" sdt1.node_name AS workLevelName,");
		sql.append(" wo.send_mes_flag AS sendMesFlag,");
		sql.append(" wo.content AS workContent");
		sql.append(" FROM workorder AS wo");
		sql.append(" LEFT JOIN ecmc_sys_user AS ecmcUser ON wo.head_user = ecmcUser.id");
		sql.append(" LEFT JOIN sys_selfuser AS ecscUser ON wo.apply_user = ecscUser.user_id");
		sql.append(" LEFT JOIN sys_selfcustomer AS cus ON wo.apply_customer = cus.cus_id");
		sql.append(" LEFT JOIN workorder_assess AS workFlow ON workFlow.id=wo.workorder_assess_id");
		sql.append(" LEFT JOIN sys_data_tree AS sdt ON wo.order_type = sdt.node_id");
		sql.append(" LEFT JOIN sys_data_tree AS sdt1 ON wo.order_level = sdt1.node_id");
		sql.append(" WHERE");
		sql.append(" wo.flag in ('0','1') and wo.send_mes_flag !='2'");
		Query query = workDao.createSQLNativeQuery(sql.toString());
		List<Object> objList = query.getResultList();
		//状态名称
		List<Workorder> workList = new ArrayList<Workorder>();
		Map<String,String> ecmcFlagMap=WorkUtils.getEcmcFlagMap();
		for (Object object2 : objList) {
			Object[] obj = (Object[]) object2;
			Workorder workorder = new Workorder();
			workorder.setWorkId((String)obj[0]);
			workorder.setWorkNum((String)obj[1]);
			workorder.setWorkTitle((String)obj[2]);
			workorder.setWorkType((String)obj[3]);
			workorder.setWorkLevel((String)obj[4]);
			workorder.setApplyCustomer((String)obj[5]);
			//申请客户不显示ID，名称为空就显示空
			workorder.setWorkCusName(obj[6]==null?"":(String)obj[6]);
			workorder.setWorkFalg((String)obj[7]);
			workorder.setWorkHeadUser((String)obj[8]);
			workorder.setWorkHeadUserName(obj[9]==null?"------":(String)obj[9]);
			workorder.setWorkCreTime((Date)obj[10]);
			workorder.setFlowBeginTime((Date)obj[11]);
			workorder.setEndtime((Date)obj[12]);
			workorder.setFlowRespondTime((Date)obj[13]);
			workorder.setFlowRespondFalg((String)obj[14]);
			workorder.setWorkTypeName((String)obj[15]);
			workorder.setWorkLevelName((String)obj[16]);
			workorder.setSendMesFlag(obj[17]==null?"0":(String)obj[17]);
			workorder.setWorkContent(obj[18]==null?"":(String)obj[18]);
			workorder.setWorkFalgName(ecmcFlagMap.get(workorder.getWorkFalg()));
			workList.add(workorder);
		}

		return workList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int getWorkCountForFlag(Map<String,String> map) {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(wo.workid) from workorder wo");
		sql.append(" left join sys_data_tree sdt on wo.order_type = sdt.node_id");
		sql.append(" where wo.flag='0' ");
		List<String> values = new ArrayList<>();
		String parentId = null;
		if(map.get("parentId") != null)
			parentId = map.get("parentId");
		if(!StringUtil.isEmpty(parentId) && !parentId.contains(",")){
			sql.append(" and sdt.parent_id=?");
			values.add(parentId);
		}
		Query query = workDao.createSQLNativeQuery(sql.toString(),values.toArray());
		List<Object> objList = query.getResultList();
		Object obj=objList.get(0);
		int count=obj==null?0:Integer.valueOf(String.valueOf(obj));
		return count;
	}

	@Override
	public List<SysDataTree> getWorkFlagListForOrdinary(String workFalg) {
		List<SysDataTree> list = new ArrayList<SysDataTree>();
		
		if("0".equals(workFalg)){
			SysDataTree sys = new SysDataTree();
			sys.setNodeId("0");
			sys.setNodeName("待处理");
			list.add(sys);
		}else if("1".equals(workFalg)){
			SysDataTree sys = new SysDataTree();
			sys.setNodeId("1");
			sys.setNodeName("处理中");
			list.add(sys);
			SysDataTree sysDataTree = new SysDataTree();
			sysDataTree.setNodeId("3");
			sysDataTree.setNodeName("已完成");
			list.add(sysDataTree);
		}else if("2".equals(workFalg)){
			SysDataTree sys1 = new SysDataTree();
			sys1.setNodeId("1");
			sys1.setNodeName("处理中");
			list.add(sys1);
			SysDataTree sys = new SysDataTree();
			sys.setNodeId("2");
			sys.setNodeName("已解决");
			list.add(sys);
			SysDataTree sysDataTree = new SysDataTree();
			sysDataTree.setNodeId("3");
			sysDataTree.setNodeName("已完成");
			list.add(sysDataTree);
		}
		SysDataTree sys1 = new SysDataTree();
		sys1.setNodeId("4");
		sys1.setNodeName("已取消");
		list.add(sys1);
		return list;
	}

	@Override
	public void editWorkSendMessage(String workId, String sendMessage) {
		BaseWorkorder baseWorkorder = workDao.findOne(workId);
		baseWorkorder.setSendMesFlag(sendMessage);
		workDao.merge(baseWorkorder);
	}
	@Override
	public User findUserByUserid(String userId) {
		if(userId!=null){
			User user = userService.findUserById(userId);
			return user;
		}
		return null;
	}

	@Override
	public int countWorkByCusId(String cusId, String flag) {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(wo.workid) from workorder wo");
		sql.append(" left join sys_data_tree sdt on wo.order_type = sdt.node_id");
		sql.append(" where order_type != '0007001003002' ");
		if(!flag.equals("-1"))
			sql.append(" and wo.flag = " + flag);
		List<String> values = new ArrayList<>();
		/* 工单申请客户ID  */
		if(cusId != null){
			sql.append(" and wo.apply_customer = ?");
			values.add(cusId);
		}
		Query query = workDao.createSQLNativeQuery(sql.toString(),values.toArray());
		@SuppressWarnings("unchecked")
		List<Object> objList = query.getResultList();
		Object obj=objList.get(0);
		int count=obj==null?0:Integer.valueOf(String.valueOf(obj));
		return count;
	}

	@Override
	public CloudProject getStatisticsByWorkId(String workId) {
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseWorkOpinion where workId = ? and opinionContent != null and (opinionState = null or opinionState ='1') order by opinionTime");
		List<String> values = new ArrayList<String>();
		values.add(workId);
		List<BaseWorkOpinion> baseWorkOpinionList = workOpinionDao.find(hql.toString(), values.toArray());
		BaseWorkOpinion opinion = null;
		CloudProject cloudProject = null;
		try {
			if (baseWorkOpinionList != null && baseWorkOpinionList.size() > 0) {
				opinion = baseWorkOpinionList.get(0);
				String opinionContent = opinion.getOpinionContent();
				JSONObject contentJson = JSONObject.parseObject(opinionContent);
				cloudProject = JSON.parseObject(contentJson.toJSONString(), CloudProject.class);
			}
		} catch (JSONException e) {
		}
		return cloudProject;
	}
}
