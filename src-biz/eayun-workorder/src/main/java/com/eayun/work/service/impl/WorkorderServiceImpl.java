package com.eayun.work.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.exception.AppException;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.work.model.*;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.SeqManager;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.model.Customer;
import com.eayun.customer.model.User;
import com.eayun.customer.serivce.UserService;
import com.eayun.file.model.EayunFile;
import com.eayun.file.service.FileService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.work.dao.WorkFileDao;
import com.eayun.work.dao.WorkFlowDao;
import com.eayun.work.dao.WorkOpinionDao;
import com.eayun.work.dao.WorkQuotaDao;
import com.eayun.work.dao.WorkorderDao;
import com.eayun.work.service.OrderNumService;
import com.eayun.work.service.WorkMailService;
import com.eayun.work.service.WorkorderService;

@Service
@Transactional
public class WorkorderServiceImpl implements WorkorderService {

	@Autowired
	private WorkorderDao workDao;
	@Autowired
	private OrderNumService orderNumService; // 流水编号
	@Autowired
	private WorkFlowDao workFlowDao; // 流程
	@Autowired
	private WorkOpinionDao workOpinionDao; // 回复
	@Autowired
	private UserService userService; // 用户
	@Autowired
	private WorkMailService workMailService; // 用户
	@Autowired
	private WorkFileDao workFileDao; // 附件
	@Autowired
	private WorkQuotaDao workQuotaDao; // 工单配额
	@Autowired
	private FileService fileService; // 工单配额
	@Autowired
	private ProjectService prjectService;

	/*
	 * 辅助方法--参数
	 */

	/**
	 * 状态Map
	 *
	 * @return
	 */
	private static Map<String, String> getEcmcFalgMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("0", "待处理");
		map.put("1", "处理中");
		map.put("2", "以解决");
		map.put("3", "已完结");
		map.put("4", "已取消");
		map.put("5", "已删除");
		return map;
	}

	private static Map<String, String> getFalgMap() {
		Map<String, String> map = new HashMap<String, String>();
		/**
		 * ecsc ecmc 待受理 待受理 处理中 处理中 待反馈 处理中 待确认 已解决 待评价 已解决 已关闭 已完成(已取消)
		 */
		map.put("0", "待受理");
		map.put("1", "处理中");
		map.put("2", "待反馈");
		map.put("3", "待确认");
		map.put("4", "待评价");
		map.put("5", "已关闭");
		map.put("6", "已删除");
		map.put("7", "已取消");
		return map;
	}

	private static String workType1 = "0007001003"; // 特殊工单的父类id
	private static String workType2 = "0007001002"; // 正常工单的父类id
	private static String quotaType = "0007001003001"; // 配额类工单
	@SuppressWarnings("unused")
	private static String registerType = "0007001003002"; // 注册工单

	/**
	 * 获取工单类型
	 */
	private Map<String, String> getDateTreeMap(String parentId) {
		List<SysDataTree> dataList = this.getDataTree(parentId);
		Map<String, String> map = new HashMap<>();
		if (dataList != null && dataList.size() > 0) {
			for (SysDataTree sysDataTree : dataList) {
				map.put(sysDataTree.getNodeId(), sysDataTree.getNodeName());
			}
		}
		return map;
	}

	private String getDateTreeById(String nodeId) {
		SysDataTree SysDate = DictUtil.getDataTreeByNodeId(nodeId);
		return SysDate.getNodeName();
	}

	/**
	 * 修改项目字段(是否有配额类工单)
	 *
	 * @param prjId
	 */
	private void updPrjIsHasWorkByPrjId(String falg, String prjId) {
		StringBuffer sql = new StringBuffer();
		sql.append("update cloud_project set is_haswork=? where prj_id=?");
		List<String> values = new ArrayList<String>();
		values.add(falg);
		values.add(prjId);
		workDao.execSQL(sql.toString(), values.toArray());
	}

	/*
	 * 辅助方法结束
	 */

	@Override
	public Workorder addWorkorder(Workorder workorder) throws Exception {
		String str = orderNumService.getOrderNum("SJ", "yyyyMMdd", 8);// 生成工单标号
		workorder.setWorkNum(str);
		String workId = SeqManager.getSeqMang().getSeqForDate();
		workorder.setWorkId(workId);
		// workorder.setWorkFalg(ZERO);//默认
		workorder.setWorkCreTime(new Date());
		workorder.setWorkLevel("0007001001001");// ecsc默认工单级别为1级
		String flowId = SeqManager.getSeqMang().getSeqForDate();// 流程id
		workorder.setFlowId(flowId);
		workorder.setSendMesFlag("0");

		BaseWorkorder baseWorkorder = new BaseWorkorder();
		BeanUtils.copyPropertiesByModel(baseWorkorder, workorder);
		baseWorkorder = workDao.save(baseWorkorder);
		// ------流程------
		BaseWorkFlow baseWorkFlow = new BaseWorkFlow();// 封装流程
		baseWorkFlow.setFlowId(flowId);
		baseWorkFlow.setWorkId(workId);
		baseWorkFlow.setFlowCreTime(baseWorkorder.getWorkCreTime());
		baseWorkFlow = (BaseWorkFlow) workFlowDao.saveEntity(baseWorkFlow);
		// ------回复------
		WorkOpinion workOpinion = this.addWorkOpinion(workorder, workorder.getWorkContent());

		workorder.setWorkTypeName(getDateTreeById(workorder.getWorkType()));// 工单类别名称
		workorder.setWorkLevelName("一级");// 工单级别名称
		workorder
				.setWorkFalgName(getEcmcFalgMap().get(workorder.getWorkFalg()));// ecmc状态名称
		workorder
				.setWorkFalgName(getFalgMap().get(workorder.getWorkEcscFalg()));// ecsc状态名称
		workorder.setOpinionId(workOpinion.getOpinionId());
		// 发送短信
		workMailService.addWorkSendMail(workorder, workorder.getWorkCreUser());
		BeanUtils.copyPropertiesByModel(workorder, baseWorkorder);
		return workorder;
	}

	// 配额
	@Override
	public Workorder addQuotaWorkorder(Workorder workorder, WorkQuota workQuota) throws Exception {
		workorder.setSendMesFlag("0");
		workorder = this.addWorkorder(workorder);
		// 无附件，要给配额表插入数据，并修改项目中是否有配额工单为处理的字段
		workQuota.setWorkId(workorder.getWorkId());
		BaseWorkQuota baseWorkQuota = new BaseWorkQuota();
		BeanUtils.copyPropertiesByModel(baseWorkQuota, workQuota);
		workQuotaDao.save(baseWorkQuota);
		//新添加回复
		BaseWorkOpinion baseWorkOpinion = new BaseWorkOpinion();
		baseWorkOpinion.setOpinionId(SeqManager.getSeqMang().getSeqForDate());
		baseWorkOpinion.setCreUser(workorder.getWorkCreUser());
		baseWorkOpinion.setCreUserName(workorder.getWorkApplyUserName());
		baseWorkOpinion.setOpinionTime(new Date());
		baseWorkOpinion.setWorkId(workorder.getWorkId());
		baseWorkOpinion.setFlag(workorder.getWorkFalg());
		// 根据prjId得到project信息
		CloudProject cloudProject = prjectService.findProject(workQuota.getPrjId());
		StringBuffer opinionContent = new StringBuffer();
		opinionContent.append("{");
		if (baseWorkQuota.getQuotaVm() != 0) {
			opinionContent.append(",hostCount:" + cloudProject.getHostCount());
		}
		if (baseWorkQuota.getQuotaCpu() != 0) {
			opinionContent.append(",cpuCount:" + cloudProject.getCpuCount());
		}
		if (baseWorkQuota.getQuotaMemory() != 0) {
			opinionContent.append(",memory:" + cloudProject.getMemory());
		}
		if (baseWorkQuota.getQuotaMemory() != 0) {
			opinionContent.append(",diskCount:" + cloudProject.getDiskCount());
		}
		if (baseWorkQuota.getQuotaDiskSize() != 0) {
			opinionContent.append(",diskCapacity:" + cloudProject.getDiskCapacity());
		}
		if (baseWorkQuota.getQuotaSnapshot() != 0) {
			opinionContent.append(",diskSnapshot:" + cloudProject.getDiskSnapshot());
		}
		if (baseWorkQuota.getQuotaShotSize() != 0) {
			opinionContent.append(",snapshotSize:" + cloudProject.getSnapshotSize());
		}
		if (baseWorkQuota.getQuotaBand() != 0) {
			opinionContent.append(",countBand:" + cloudProject.getCountBand());
		}
		if (baseWorkQuota.getQuotaNet() != 0) {
			opinionContent.append(",netWork:" + cloudProject.getNetWork());
		}
		if (baseWorkQuota.getQuotaSubnet() != 0) {
			opinionContent.append(",subnetCount:" + cloudProject.getSubnetCount());
		}
		if (baseWorkQuota.getQuotaFloatIp() != 0) {
			opinionContent.append(",outerIP:" + cloudProject.getOuterIP());
		}
		if (baseWorkQuota.getQuotaRoute() != 0) {
			opinionContent.append(",routeCount:" + cloudProject.getRouteCount());
		}
		if (baseWorkQuota.getQuotaSecGroup() != 0) {
			opinionContent.append(",safeGroup:" + cloudProject.getSafeGroup());
		}
		if (baseWorkQuota.getQuotaBalance() != 0) {
			opinionContent.append(",quotaPool:" + cloudProject.getQuotaPool());
		}
		if (baseWorkQuota.getQuotaVpn() != 0) {
			opinionContent.append(",countVpn:" + cloudProject.getCountVpn());
		}
		if (baseWorkQuota.getQuotaPortMapping() != 0) {
			opinionContent.append(",portMappingCount:" + cloudProject.getPortMappingCount());
		}
		if (baseWorkQuota.getQuotaSms() != 0) {
			opinionContent.append(",smsCount:" + cloudProject.getSmsCount());
		}
		if (baseWorkQuota.getQuotaMasterInstance() != 0) {
			opinionContent.append(",maxMasterInstance:" + cloudProject.getMaxMasterInstance());
		}
		if (baseWorkQuota.getQuotaSlaveInstance() != 0) {
			opinionContent.append(",maxSlaveIOfCluster:" + cloudProject.getMaxSlaveIOfCluster());
		}
		if (baseWorkQuota.getQuotaBackupByHand() != 0) {
			opinionContent.append(",maxBackupByHand:" + cloudProject.getMaxBackupByHand());
		}
		if (baseWorkQuota.getQuotaBackupByAuto() != 0) {
			opinionContent.append(",maxBackupByAuto:" + cloudProject.getMaxBackupByAuto());
		}
		opinionContent.append("}");
		opinionContent.delete(1, 2);// 删除第一项之前的逗号
		baseWorkOpinion.setOpinionContent(opinionContent.toString());
		baseWorkOpinion.setEcmcCre("1");
		workOpinionDao.save(baseWorkOpinion);

		this.updPrjIsHasWorkByPrjId("1", workQuota.getPrjId());// 修改项目字段
		// 回复表再次添加数据，拼装配额----未做
		return workorder;
	}

	// 注册
	@Override
	public Workorder addRegisterWorkorder(Customer customer) throws Exception {
		Workorder workorder = new Workorder();
		workorder.setApplyCustomer(customer.getCusId());
		workorder.setWorkType("0007001003002");// 注册
		workorder.setWorkTitle("注册使用易云公有云");
		workorder.setWorkPhoneTime("1");
		workorder.setWorkState("0");
		workorder.setWorkPhone(customer.getCusPhone());
		workorder.setWorkEmail(customer.getCusEmail());
		workorder.setSendMesFlag("0");
		workorder = this.addWorkorder(workorder);
		return workorder;
	}

	/**
	 * 工单处理流程
	 */
	@Override
	public Workorder updateWorkorder(Workorder workorder) {
		BaseWorkorder baseWorkorder = new BaseWorkorder();
		BeanUtils.copyPropertiesByModel(baseWorkorder, workorder);
		workDao.saveOrUpdate(baseWorkorder);
		return workorder;
	}

	@Override
	public boolean deleteWorkorder(Workorder workorder) {
		workorder.setWorkLevelName(getDateTreeMap("0007001001").get(
				"0007001001001"));// 工单级别名称
		workorder.setWorkTypeName(getDateTreeById(workorder.getWorkType()));// 工单类别名称
		BaseWorkorder baseWorkorder = new BaseWorkorder();
		BeanUtils.copyPropertiesByModel(baseWorkorder, workorder);
		workDao.delete(baseWorkorder);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Page getWorkorderList(Page page, ParamsMap paramsMap) {
		String workNum = paramsMap.getParams().get("workNum").toString();
		Date beginTime = DateUtil.timestampToDate(paramsMap.getParams()
				.get("beginTime").toString());
		Date endTime = DateUtil.timestampToDate(paramsMap.getParams()
				.get("endTime").toString());
		String keyWord = paramsMap.getParams().get("keyWord").toString();
		String workFalg = paramsMap.getParams().get("workFalg").toString();
		String workType = String.valueOf(paramsMap.getParams().get("workType"));
		String userId = paramsMap.getParams().get("userId").toString();
		String type = paramsMap.getParams().get("type").toString();
		QueryMap queryMap = new QueryMap();
		int pageSize = paramsMap.getPageSize();
		int pageNumber = paramsMap.getPageNumber();
		queryMap.setPageNum(pageNumber);
		queryMap.setCURRENT_ROWS_SIZE(pageSize);

		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseWorkorder bw where bw.workCreUser=? and bw.workEcscFalg !=? ");
		List<String> values = new ArrayList<String>();
		values.add(userId);
		values.add("6");
		if (!StringUtil.isEmpty(workNum)) {
			hql.append(" and workNum = ?");
			values.add(workNum);
		}
		if (!StringUtil.isEmpty(keyWord)) {
			hql.append(" and (workTitle like ? )");
			values.add("%" + keyWord + "%");
			// values.add("%" + keyWord + "%"); or workContent like ?
		}
		if (beginTime != null) {
			hql.append(" and workCreTime >= '" + beginTime + "'");
		}
		if (endTime != null) {
			hql.append(" and workCreTime < '" + endTime + "'");
		}
		if (!StringUtil.isEmpty(workType)) {
			hql.append(" and workType = ?");
			values.add(workType);
		}
		if (type.equals("unHandle")) {
			hql.append(" and workEcscFalg in ('2','3','4')");
		} else {
			if (!StringUtil.isEmpty(workFalg)) {
				hql.append(" and workEcscFalg = ?");
				values.add(workFalg);
			}
		}
		hql.append(" order by workEcscFalg asc, workCreTime desc");
		page = workDao.pagedQuery(hql.toString(), queryMap, values.toArray());
		Map<String, String> dataTreeMap = getDateTreeMap(workType2);// 工单类型的父类id
		Map<String, String> dataTreeMap1 = getDateTreeMap(workType1);// 特殊工艺工单类型的父类id
		dataTreeMap.putAll(dataTreeMap1);
		List<BaseWorkorder> list = (List<BaseWorkorder>) page.getResult();
		Map<String, String> falgMap = getFalgMap();
		if (list != null && list.size() > 0) {
			int i = 0;
			for (BaseWorkorder baseWorkorder : list) {
				Workorder work = new Workorder();
				BeanUtils.copyPropertiesByModel(work, baseWorkorder);
				work.setWorkTypeName(dataTreeMap.get(work.getWorkType()));
				work.setWorkFalgName(falgMap.get(baseWorkorder
						.getWorkEcscFalg()));
				work.setWorkLevelName(getDateTreeById(work.getWorkLevel()));
				if (!work.getWorkType().equals(WorkUtils.REGISTERTYPE)) {
					User user = userService.findUserById(work.getWorkApplyUser());
					work.setWorkCusName(user.getCusName() != null ? user.getCusName() : "");
				}
				list.set(i, work);
				i++;
			}
		}
		return page;
	}

	@Override
	public Workorder findWorkorderByWorkId(String workId) {
		BaseWorkorder baseWorkorder = workDao.findOne(workId);
		BaseWorkFlow baseWorkFlow = workFlowDao.findOne(baseWorkorder
				.getFlowId());
		Workorder workorder = new Workorder();
		BeanUtils.copyPropertiesByModel(workorder, baseWorkorder);
		workorder.setFlowRespondFalg(baseWorkFlow.getFlowRespondFalg());
		workorder.setWorkFalgName(getEcmcFalgMap().get(workorder.getWorkFalg()));
		workorder.setWorkEcscFalgName(getFalgMap().get(workorder.getWorkEcscFalg()));
		workorder.setWorkTypeName(getDateTreeById(workorder.getWorkType()));
		workorder.setWorkLevelName(getDateTreeById(workorder.getWorkLevel()));
		if (!workorder.getWorkType().equals(WorkUtils.REGISTERTYPE)) {
			User user = userService.findUserById(workorder.getWorkApplyUser());
			workorder.setWorkApplyUserName(user.getUserAccount());
			workorder.setWorkCusName(user.getCusName() != null ? user.getCusName() : "");
		}
		if (baseWorkorder.getWorkType().equals(quotaType)) {
			WorkQuota workQuota = this.findWorkQuotaByWorkId(workId);
			workorder.setPrjId(workQuota.getPrjId());
		}
		return workorder;
	}

	@SuppressWarnings("unused")
	@Override
	public List<SysDataTree> getDataTree(String parentId) {
		// 获取问题类别
		String hql = "from BaseSysDataTree where parentId =? order by sort";
		List<String> values = new ArrayList<String>();
		values.add(parentId);
		List<SysDataTree> dataList = new ArrayList<SysDataTree>();
		dataList = DictUtil.getDataTreeByParentId(parentId);
		return dataList;
	}

	/**
	 * 处理消息回复处理的实现方法
	 *
	 * @param workorder
	 * @param content
	 * @return
	 * @throws Exception
	 */
	@Override
	public WorkOpinion addWorkOpinion(Workorder workorder, String content) throws Exception {
		//当前时间
		Date date = new Date();
		boolean bool = false;
		Workorder oldWorkorder = this.findWorkorderByWorkId(workorder.getWorkId());
		workorder.setLogType(oldWorkorder.getWorkEcscFalg());
		if ("2".equals(oldWorkorder.getWorkFalg()) && "1".equals(workorder.getWorkFalg())) {//工单打回
			//计算以前的时间差
			BaseWorkFlow baseWorkFlow = workFlowDao.findOne(workorder.getFlowId());
			long now = baseWorkFlow.getEndtime().getTime() - baseWorkFlow.getFlowBeginTime().getTime();
			//响应时间应该不便
			long responseTime = baseWorkFlow.getFlowRespondTime().getTime() - baseWorkFlow.getFlowBeginTime().getTime();
			baseWorkFlow = new BaseWorkFlow();
			baseWorkFlow.setFlowBeginTime(new Date(date.getTime() - now));
			baseWorkFlow.setFlowRespondTime(new Date(baseWorkFlow.getFlowBeginTime().getTime() + responseTime));
			baseWorkFlow.setFlowCreTime(date);
			baseWorkFlow.setFlowFlag(workorder.getWorkFalgName());
			baseWorkFlow.setFlowId(SeqManager.getSeqMang().getSeqForDate());
			baseWorkFlow.setFlowRespondFalg("1");
			baseWorkFlow.setFlowState("2");//回退
			baseWorkFlow.setUserIdHead(workorder.getWorkHeadUser());
			baseWorkFlow.setWorkId(workorder.getWorkId());
			workFlowDao.save(baseWorkFlow);
			workorder.setFlowId(baseWorkFlow.getFlowId());
			bool = true;
		}
		workorder = this.updateWorkorderForFalg(workorder);
		if (StringUtil.isEmpty(content)) {
			content = null;
		}
		if (workorder.getWorkEcscFalg().equals("2")) {// 待反馈
			workorder.setWorkEcscFalg("1");
			workorder.setSendMesFlag("3");// 改变发送短信和邮件的状态，
			workorder = this.updateWorkorderForFalg(workorder);
		} else if (workorder.getWorkEcscFalg().equals("4")) {// 待评价
			workorder.setSendMesFlag("3");
			workorder.setWorkTypeName(getDateTreeById(workorder.getWorkType()));// 工单类别名称
			workorder.setWorkLevelName("一级");// 工单级别名称
			workorder.setWorkFalgName(getEcmcFalgMap().get(
					workorder.getWorkFalg()));// ecmc状态名称
			workorder.setWorkFalgName(getFalgMap().get(
					workorder.getWorkEcscFalg()));// ecsc状态名称
			workMailService.updWorkEcscFalgSendMail(workorder, workorder.getWorkCreUser());
		}
		WorkOpinion workOpinion = new WorkOpinion();
		workOpinion.setCreUser(workorder.getWorkApplyUser());
		workOpinion.setOpinionContent(content);
		workOpinion.setOpinionTime(date);
		workOpinion.setReplyUser(workorder.getWorkHeadUser());
		workOpinion.setWorkId(workorder.getWorkId());
		workOpinion.setFlag(workorder.getWorkFalg());
		workOpinion.setEcmcCre("1");
		workOpinion.setReplyUserName(workorder.getWorkHeadUserName());
		workOpinion.setCreUserName(workorder.getWorkCreUserName());
		if (bool) {
			workOpinion.setOpinionState("1");//回退的回复
		}
		BaseWorkOpinion baseWorkOpinion = new BaseWorkOpinion();
		BeanUtils.copyPropertiesByModel(baseWorkOpinion, workOpinion);
		baseWorkOpinion = (BaseWorkOpinion) workOpinionDao.saveEntity(baseWorkOpinion);
		BeanUtils.copyPropertiesByModel(workOpinion, baseWorkOpinion);
		// ---发送邮件
		workMailService.addWorkOpinionSendMail(workOpinion, workorder, workOpinion.getCreUser());
		return workOpinion;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WorkOpinion> getWorkOpinionList(String workId) {
		Workorder workorder = this.findWorkorderByWorkId(workId);
		// ----获取此工单的沟通记录
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseWorkOpinion where workId=? and opinionContent !=null and (opinionState = null or opinionState ='1') order by opinionTime");
		List<String> values = new ArrayList<String>();
		values.add(workId);
		List<BaseWorkOpinion> baseWorkOpinionList = workOpinionDao.find(hql.toString(), values.toArray());
		List<WorkOpinion> workOpinionList = new ArrayList<WorkOpinion>();
		for (BaseWorkOpinion baseWorkOpinion : baseWorkOpinionList) {
			WorkOpinion workOpinion = new WorkOpinion();
			BeanUtils.copyPropertiesByModel(workOpinion, baseWorkOpinion);
			if ("0".equals(workOpinion.getEcmcCre())) {//是
				workOpinion.setIsEcmcCre(false);
				workOpinion.setCreUserName("运维工程师");
			} else {
				workOpinion.setIsEcmcCre(true);
				workOpinion.setCreUserName("我");
			}
			List<WorkFile> WorkFilelist = this.getWorkFileListByWorkId(workOpinion.getOpinionId());
			workOpinion.setWorkFile(WorkFilelist);
			workOpinionList.add(workOpinion);
		}
		if (workorder.getWorkType().equals(quotaType)) {// 配额类工单
			WorkOpinion workOpinion1 = new WorkOpinion();
			workOpinion1.setIsEcmcCre(true);
			workOpinion1.setCreUserName("我");
			workOpinion1.setOpinionTime(workorder.getWorkCreTime());
			// 得到配额
			WorkQuota workQuota = this.findWorkQuotaByWorkId(workId);
			workOpinion1.setWorkQuota(workQuota);
			if (workOpinionList.size() >= 1) {
				workOpinionList.add(1, workOpinion1);
			} else {
				workOpinionList.add(0, workOpinion1);
			}
		}
		return workOpinionList;
	}

	/*
	 * 因一个配额工单只有一条配额记录，所以取第一条
	 */
	@SuppressWarnings("unchecked")
	private WorkQuota findWorkQuotaByWorkId(String workId) {
		StringBuffer hql = new StringBuffer();
		hql.append("from BaseWorkQuota where workId=?");
		List<WorkQuota> workQuotaList = workQuotaDao.find(hql.toString(),
				workId);
		WorkQuota workQuota = new WorkQuota();
		if (workQuotaList.size() > 0) {
			BaseWorkQuota baseWorkQuota = workQuotaList.get(0);
			BeanUtils.copyPropertiesByModel(workQuota, baseWorkQuota);
		}
		return workQuota;
	}

	@Override
	public Workorder updateWorkorderForFalg(Workorder workorder)
			throws Exception {
		Workorder oldWork = this.findWorkorderByWorkId(workorder.getWorkId());
		if (oldWork.getWorkType().equals(quotaType)) {// 配额类工单
			// 获取项目id
			StringBuffer hql = new StringBuffer();
			hql.append("from BaseWorkQuota where workId=?");
			BaseWorkQuota baseWorkQuota = (BaseWorkQuota) workQuotaDao
					.findUnique(hql.toString(), oldWork.getWorkId());
			String workEcscFalg = workorder.getWorkEcscFalg();
			if (workEcscFalg.equals("4") || workEcscFalg.equals("5")
					|| workEcscFalg.equals("6") || workEcscFalg.equals("7")) {
				this.updPrjIsHasWorkByPrjId("0", baseWorkQuota.getPrjId());
			}
		}
		// 0:待受理1:处理中2:待反馈3:待确认 4:待评价5:已关闭 6:已删除7:已取消--ecsc
		// 0:待处理1:处理中2:已解决3:已完结 4:已取消--ecmc
		if (workorder.getWorkEcscFalg().equals("7")) {// 取消操作
			workorder.setWorkFalg("4");
			BaseWorkFlow baseWorkFlow = workFlowDao.findOne(workorder.getFlowId());
			baseWorkFlow.setEndtime(new Date());
			workFlowDao.merge(baseWorkFlow);
			// ---发送邮件
			workMailService.updateWorkorderForFalg(workorder, oldWork,
					workorder.getWorkCreUser());
			/*
			 * } else if (workorder.getWorkEcscFalg().equals("6")) {//删除
			 * if(workorder.getWorkFalg().equals("3")){
			 * workorder.setWorkFalg("3"); }else{ workorder.setWorkFalg("4"); }
			 */

		}
		BaseWorkorder baseWorkorder = new BaseWorkorder();
		BeanUtils.copyPropertiesByModel(baseWorkorder, workorder);
		workDao.merge(baseWorkorder);
		return workorder;
	}

	@Override
	public Workorder updateWorkForFc(String workId) throws Exception {
		Workorder workorder = this.findWorkorderByWorkId(workId);
		workorder.setWorkComplain("1");// 已投诉
		workorder.setWorkLevel("0007001001005");// 0级
		BaseWorkorder baseWorkorder = new BaseWorkorder();
		BeanUtils.copyPropertiesByModel(baseWorkorder, workorder);
		workDao.merge(baseWorkorder);
		//---添加回复
		BaseWorkOpinion baseWrokOpinion = new BaseWorkOpinion();
		baseWrokOpinion.setCreUser(workorder.getWorkHeadUser());
		baseWrokOpinion.setFlag(workorder.getWorkFalg());
		baseWrokOpinion.setOpinionContent("您的工单已投诉成功，正在加急处理中，请稍后！");
		baseWrokOpinion.setOpinionTime(new Date());
		baseWrokOpinion.setReplyUser(workorder.getWorkCreUser());
		baseWrokOpinion.setWorkId(workId);
		baseWrokOpinion.setEcmcCre("0");
		baseWrokOpinion.setCreUserName(workorder.getWorkHeadUserName());
		baseWrokOpinion.setReplyUserName(workorder.getWorkCreUserName());
		workOpinionDao.save(baseWrokOpinion);
		workMailService.updWorkForFcSendMail(workorder,
				workorder.getWorkCreUser());
		return workorder;
	}

	@Override
	public List<WorkFile> addWorkFile(Iterator<String> itr,
									  MultipartHttpServletRequest request, String userId)
			throws Exception {
		List<WorkFile> workFileList = new ArrayList<WorkFile>();
		while (itr.hasNext()) {
			MultipartFile multipartFile = request.getFile(itr.next());
			String opinionId = request.getParameter("opinionId");
			WorkFile workFile = new WorkFile();
			workFile.setMultipartFile(multipartFile);
			workFile.setOpinionId(opinionId);
			// ---调用上传接口上传附件
			String fileId = fileService.uploadFile(workFile.getMultipartFile(),
					userId);
			workFile.setSaccId(fileId);
			BaseWorkFile baseWorkFile = new BaseWorkFile();
			BeanUtils.copyPropertiesByModel(baseWorkFile, workFile);
			workFileDao.save(baseWorkFile);
			workFileList.add(workFile);
		}
		return workFileList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WorkFile> getWorkFileListByWorkId(String opinionId) {
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

	@SuppressWarnings("unchecked")
	@Override
	public int unHandleWorkCount(Map<String, String> map) {
		StringBuffer sql = new StringBuffer();
		List<String> values = new ArrayList<String>();
		sql.append("select count(workid) from workorder where cre_user=? and work_falg in");
		if (map.get("range") != null && !StringUtil.isEmpty(map.get("range"))) {
			sql.append(" ('0','1','2','3')");
		} else {
			sql.append(" ('2','3','4')");
		}
		values.add(map.get("userId"));
		Query query = workDao.createSQLNativeQuery(sql.toString(),
				values.toArray());
		List<Object> list = query.getResultList();
		int count = Integer.valueOf(String.valueOf(list.get(0)));
		return count;
	}

	@SuppressWarnings({"unchecked", "static-access"})
	@Override
	public List<Workorder> unHandleWorkNum(Map<String, String> map) {
		StringBuffer sql = new StringBuffer();
		List<String> values = new ArrayList<String>();
		sql.append("select work_falg,count(workid) from workorder where cre_user=? and work_falg in ('2','3','4') group by work_falg order by work_falg");
		values.add(map.get("userId"));
		Query query = workDao.createSQLNativeQuery(sql.toString(),
				values.toArray());
		List<Object> list = query.getResultList();
		// --------前台永远显示3条
		List<Workorder> webWorkList = this.getWebWorkList();
		for (Workorder workorder : webWorkList) {
			for (Object object : list) {
				Object[] obj = (Object[]) object;
				String workFalg = String.valueOf(obj[0]);
				if (workFalg.equals(workorder.getWorkEcscFalg())) {
					workorder.setNum(Integer.valueOf(String.valueOf(obj[1])));
				}
			}
		}

		return webWorkList;
	}

	// -------前台pop使用
	private static List<Workorder> getWebWorkList() {
		List<Workorder> webWorkList = new ArrayList<Workorder>();
		Workorder workorder1 = new Workorder();
		workorder1.setWorkEcscFalg("2");
		workorder1.setWorkFalgName(getFalgMap().get(
				workorder1.getWorkEcscFalg()));
		workorder1.setNum(0);
		Workorder workorder2 = new Workorder();
		workorder2.setWorkEcscFalg("3");
		workorder2.setWorkFalgName(getFalgMap().get(
				workorder2.getWorkEcscFalg()));
		workorder2.setNum(0);
		Workorder workorder3 = new Workorder();
		workorder3.setWorkEcscFalg("4");
		workorder3.setWorkFalgName(getFalgMap().get(
				workorder3.getWorkEcscFalg()));
		workorder3.setNum(0);
		webWorkList.add(workorder1);
		webWorkList.add(workorder2);
		webWorkList.add(workorder3);
		return webWorkList;
	}

	@Override
	public SessionUserInfo getUserInfo(SessionUserInfo session) {
		User user = userService.findUserById(session.getUserId());
		if (user.getIsMailValid()) {
			session.setEmail(user.getUserEmail());
		} else {
			session.setEmail(null);
		}
		if (user.getIsPhoneValid()) {
			session.setPhone(user.getUserPhone());
		} else {
			session.setPhone(null);
		}
		return session;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Workorder> getWorkordersByState(WorkOrderState state) throws AppException {
		//当前时间
//		Date date = new Date() ;
		if (WorkOrderState.PENDING.equals(state)) {
			List<BaseWorkorder> satisfyWorkorder = new ArrayList<>();
			//当前状态处于待确认状态的话，执行如下操作
			String hql = " from BaseWorkorder where workFalg = '2' ";
			List<BaseWorkorder> baseWorkorders = workDao.find(hql);
			for (BaseWorkorder baseWorkorder : baseWorkorders) {
				//按照倒序查询并且排列对应的回复消息
				List<BaseWorkOpinion> baseWorkOpinions =
						workOpinionDao.findByCreDateDesc(baseWorkorder.getWorkId());
				if (checkWorkOpinion(baseWorkOpinions)) {
					satisfyWorkorder.add(baseWorkorder);
				}
			}
			return getByBaseWorkorder(satisfyWorkorder);
		}
		if (WorkOrderState.EVALUATING.equals(state)) {
			long betweenTime = 7 * 24 * 60 * 60 * 1000;
			//当前状态处于待评价状态的话，执行如下操作
			String evaluatingSql =
					" select w from BaseWorkorder as w, BaseWorkOpinion as o " +
							" where w.workId = o.workId  " +
							" and o.flag = '3' and w.workCreRole='2' and w.workEcscFalg='4' and o.opinionTime <= ? ";
			//查询符合条件的工单
			List<BaseWorkorder> baseWorkorders = workDao.createQuery(evaluatingSql, new Date(new Date().getTime() - betweenTime)).list();//相差两分钟
//			(evaluatingSql).setResultTransformer(Transformers.aliasToBean(BaseWorkorder.class)).list()
			return getByBaseWorkorder(baseWorkorders);
		}
		return null;
	}

	/**
	 * 判断检查当前工单所处的状态是否真正的为 “待确认” 的状态
	 *
	 * @param opinions
	 * @return
	 * @throws AppException
	 */
	private boolean checkWorkOpinion(List<BaseWorkOpinion> opinions) throws AppException {
		//检查该工单当前所处的状态是否真正为“待确认”的状态,"0"首先判断第一项
		if (!"2".equals(opinions.get(0).getFlag())) {
			//若最新的状态不为2，则表示当前的工单状态还没有到达“待确认”
			return false;
		} else {
			//下一条回复的状态不为2，则表示第一条回复的时候为状态更新的时间
			return checkWorkOpinionInner(opinions, 1);
		}
	}

	/**
	 * 判断具体的消息回复列表是否满足“待确认”状态规则
	 *
	 * @param opinions
	 * @param foot
	 * @return
	 * @throws AppException
	 */
	private boolean checkWorkOpinionInner(List<BaseWorkOpinion> opinions, Integer foot) throws AppException {
		long betweenTime = 7 * 24 * 60 * 60 * 1000;
		if (!"2".equals(opinions.get(foot).getFlag())) {
			if (new Date().getTime() - opinions.get(foot - 1).getOpinionTime().getTime() >= betweenTime) {
				return true;
			} else {
				return false;
			}
		} else {
			checkWorkOpinionInner(opinions, foot + 1);
		}
		return false;
	}

	/**
	 * 封装工单详情
	 *
	 * @param workorders
	 * @return
	 * @throws AppException
	 */
	private List<Workorder> getByBaseWorkorder(List<BaseWorkorder> workorders) throws AppException {
		List<Workorder> workorderList = new ArrayList<>();
		for (BaseWorkorder baseWorkorder : workorders) {
			Workorder workorder = new Workorder();
			BaseWorkFlow baseWorkFlow = workFlowDao.findOne(baseWorkorder.getFlowId());
			BeanUtils.copyPropertiesByModel(workorder, baseWorkorder);
			workorder.setFlowRespondFalg(baseWorkFlow.getFlowRespondFalg());
			workorder.setWorkFalgName(getEcmcFalgMap().get(workorder.getWorkFalg()));
			workorder.setWorkEcscFalgName(getFalgMap().get(workorder.getWorkEcscFalg()));
			workorder.setWorkTypeName(getDateTreeById(workorder.getWorkType()));
			workorder.setWorkLevelName(getDateTreeById(workorder.getWorkLevel()));
			if (!workorder.getWorkType().equals(WorkUtils.REGISTERTYPE)) {
				User user = userService.findUserById(workorder.getWorkApplyUser());
				workorder.setWorkApplyUserName(user.getUserAccount());
				workorder.setWorkCusName(user.getCusName() != null ? user.getCusName() : "");
			}
			if (baseWorkorder.getWorkType().equals(quotaType)) {
				WorkQuota workQuota = this.findWorkQuotaByWorkId(baseWorkorder.getWorkId());
				workorder.setPrjId(workQuota.getPrjId());
			}
			if (userService.findBaseUserById(workorder.getWorkCreUser()) != null) {
				workorderList.add(workorder);
			}
		}
		return workorderList;
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