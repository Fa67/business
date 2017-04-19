package com.eayun.virtualization.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.eayunstack.model.Firewall;
import com.eayun.eayunstack.service.OpenstackFirewallService;
import com.eayun.virtualization.dao.CloudFireWallDao;
import com.eayun.virtualization.dao.CloudFwPolicyDao;
import com.eayun.virtualization.model.BaseCloudFireWall;
import com.eayun.virtualization.model.BaseCloudFwPolicy;
import com.eayun.virtualization.model.BaseCloudFwRule;
import com.eayun.virtualization.model.CloudFireWall;
import com.eayun.virtualization.model.CloudFwPolicy;
import com.eayun.virtualization.model.CloudFwRule;
import com.eayun.virtualization.service.FireWallRuleService;
import com.eayun.virtualization.service.FireWallService;
import com.eayun.virtualization.service.FwPolicyService;
import com.eayun.virtualization.service.TagService;

@Service
@Transactional
public class FireWallServiceImpl implements FireWallService {
    private final static Logger log = LoggerFactory.getLogger(FireWallServiceImpl.class);
	@Autowired
	private OpenstackFirewallService fireWallService;
	@Autowired
	private TagService tagService;
	@Autowired
	private CloudFireWallDao fireWallDao;
	@Autowired
	private CloudFwPolicyDao fwpDao;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private FwPolicyService fwpService;
	@Autowired
	private FireWallRuleService fwrService;

	@Override
	public Page getFireWallList(Page page, String prjId, String dcId, String fireName, QueryMap queryMap)
			throws Exception {
		try {
			fireName = fireName.replaceAll("\\_", "\\\\_");
			int index = 0;
			Object[] args = new Object[3];
			StringBuffer sql = new StringBuffer();
			sql.append(
					"select fw.fw_id as fwId,fw.fw_name as fwName,fwp.fwp_id as fwpId,fwp.fwp_name as fwpName,fw.fw_status as fwStatus,fw.admin_stateup as adminStateup,cp.prj_name as prjName,fw.prj_id as prjId,dc.dc_name as dcName,fw.dc_id as dcId,fw.create_time as createTime ");
			sql.append(" from cloud_firewall fw,cloud_project cp ,cloud_fwpolicy fwp,dc_datacenter dc");
			sql.append(" where fw.prj_id=cp.prj_id");
			sql.append(" and fw.fwp_id=fwp.fwp_id");
			sql.append(" and fw.dc_id=dc.id");

			if (!"null".equals(prjId) && null != prjId && !"".equals(prjId) && !"undefined".equals(prjId)) {
				sql.append(" and fw.prj_id = ?");
				args[index] = prjId;
				index++;
			}
			if (!"null".equals(dcId) && null != dcId && !"".equals(dcId) && !"undefined".equals(dcId)) {
				sql.append(" and fw.dc_id = ?");
				args[index] = dcId;
				index++;
			}
			if (null != fireName && !"".equals(fireName)) {
				sql.append(" and binary fw.fw_name like ?");
				args[index] = "%" + fireName + "%";
				index++;
			}
			sql.append(" group by fw.fw_id order by fw.create_time desc ");

			Object[] params = new Object[index];
			System.arraycopy(args, 0, params, 0, index);

			page = fireWallDao.pagedNativeQuery(sql.toString(), queryMap, params);
			List newList = (List) page.getResult();
			for (int i = 0; i < newList.size(); i++) {
				Object[] objs = (Object[]) newList.get(i);
				CloudFireWall fireWall = new CloudFireWall();
				fireWall.setFwId(String.valueOf(objs[0]));
				fireWall.setFwName(String.valueOf(objs[1]));
				fireWall.setFwpId(String.valueOf(objs[2]));
				fireWall.setFwpName(String.valueOf(objs[3]));
				fireWall.setFwStatus(String.valueOf(objs[4]));
				fireWall.setAdminStateup(String.valueOf(objs[5]));
				fireWall.setPrjName(String.valueOf(objs[6]));
				fireWall.setPrjId(String.valueOf(objs[7]));
				fireWall.setDcName(String.valueOf(objs[8]));
				fireWall.setDcId(String.valueOf(objs[9]));
				fireWall.setCreateTime((Date) objs[10]);
				String tag = tagService.getResourceTagForShowcase("firewall", String.valueOf(objs[0]));
				fireWall.setTags(tag);
				fireWall.setStatusForDis(DictUtil.getStatusByNodeEn("fireWall", fireWall.getFwStatus()));
				newList.set(i, fireWall);

			}
		} catch (AppException e) {
			throw e;
		}
		return page;

	}

	@Override
	public BaseCloudFireWall addFireWall(String dcId, String prjId, String createName, String fireWallName,
			String policyId) throws AppException {
		BaseCloudFireWall cloudFirewall = null;
		try {
			// 防火墙数据
			JSONObject fw = new JSONObject();
			fw.put("name", fireWallName);
			fw.put("firewall_policy_id", policyId);
			fw.put("admin_state_up", "1");
			// 用于提交的完整数据
			JSONObject resultData = new JSONObject();
			resultData.put("firewall", fw);
			Firewall firewall = fireWallService.create(dcId, prjId, resultData);

			if (null != firewall) {
				cloudFirewall = new BaseCloudFireWall();
				cloudFirewall.setFwId(firewall.getId());
				cloudFirewall.setFwName(firewall.getName());
				cloudFirewall.setCreateName(createName);
				cloudFirewall.setCreateTime(new Date());
				cloudFirewall.setPrjId(prjId);
				cloudFirewall.setDcId(dcId);
				cloudFirewall.setAdminStateup("1");
				if (!StringUtils.isEmpty(firewall.getStatus())) {
					cloudFirewall.setFwStatus(firewall.getStatus().toUpperCase());
				}
				cloudFirewall.setFwpId(policyId);
				fireWallDao.save(cloudFirewall);
				// BaseCloudFwPolicy fwp=fwpDao.findOne(policyId);
				// fwp.setFwId(firewall.getId());
				// fwpDao.saveOrUpdate(fwp);

				if (null != cloudFirewall.getFwStatus() && !"ACTIVE".equals(cloudFirewall.getFwStatus())) {
					// TODO 同步新增防火墙状态
					JSONObject json = new JSONObject();
					json.put("fwId", cloudFirewall.getFwId());
					json.put("dcId", cloudFirewall.getDcId());
					json.put("prjId", cloudFirewall.getPrjId());
					json.put("fwStatus", cloudFirewall.getFwStatus());
					json.put("count", "0");
					jedisUtil.push(RedisKey.fwKey, json.toJSONString());
				}
			}

			return cloudFirewall;
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw new AppException("error.openstack.message");
		}
	}

	@Override
	public boolean deleteFireWall(final CloudFireWall fw) throws AppException {
		boolean isTrue = false;
		try {
			isTrue = fireWallService.delete(fw.getDcId(), fw.getPrjId(), fw.getFwId());
			if (isTrue) {
				final BaseCloudFireWall fireWall = fireWallDao.findOne(fw.getFwId());
				fireWall.setFwStatus("PENDING_DELETE");
				fireWallDao.saveOrUpdate(fireWall);
				tagService.refreshCacheAftDelRes("firewall", fw.getFwId());
				TransactionHookUtil.registAfterCommitHook(new Hook() {
					@Override
					public void execute() {
						
						JSONObject json = new JSONObject();
						json.put("fwId", fireWall.getFwId());
						json.put("dcId", fireWall.getDcId());
						json.put("prjId", fireWall.getPrjId());
						json.put("fwStatus", fireWall.getFwStatus());
						json.put("count", "0");
						jedisUtil.addUnique(RedisKey.fwKey, json.toJSONString());
					}
				});
				
			}
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw new AppException("error.openstack.message");
		}
		return isTrue;
	}

	@Override
	public boolean getFireWallByName(Map map) throws AppException {
		boolean isExist = false;
		try {
			String dcId = null;
			String fwName = null;
			String fwId = null;
			String prjId = null;
			if (null == map.get("project")) {
				dcId = map.get("dcId").toString();
				prjId = map.get("prjId").toString();
				fwName = map.get("fwName").toString();
				fwId = map.get("fwId") != null ? map.get("fwId").toString() : null;
			} else {
				Map project = (Map) map.get("project");
				prjId = project.get("projectId").toString();
				dcId = project.get("dcId").toString();
				fwName = map.get("name").toString();
			}

			StringBuffer sql = new StringBuffer();
			int index = 0;
			Object[] args = new Object[4];
			sql.append("select fire.fw_id,fire.fw_name from cloud_firewall fire where 1=1 ");
			// 数据中心
			if (!"".equals(dcId) && dcId != null && !"undefined".equals(dcId) && !"null".equals(dcId)) {
				sql.append("and fire.dc_id = ? ");
				args[index] = dcId;
				index++;
			}
			// 项目
			if (!"".equals(prjId) && prjId != null && !"undefined".equals(prjId) && !"null".equals(prjId)) {
				sql.append("and fire.prj_id = ? ");
				args[index] = prjId;
				index++;
			}
			// 防火墙名称
			if (!"".equals(fwName) && fwName != null) {
				sql.append("and binary fire.fw_name = ? ");
				args[index] = fwName.trim();
				index++;
			}

			// 防火墙ID
			if (!"".equals(fwId) && fwId != null && !"undefined".equals(fwId) && !"null".equals(fwId)) {
				sql.append("and fire.fw_id <> ? ");
				args[index] = fwId.trim();
				index++;
			}

			Object[] params = new Object[index];
			System.arraycopy(args, 0, params, 0, index);
			javax.persistence.Query query = fireWallDao.createSQLNativeQuery(sql.toString(), params);
			List listResult = query.getResultList();

			if (listResult.size() > 0) {
				isExist = true;// 返回true 代表存在此名称
			}
		} catch (AppException e) {
			throw e;
		}
		return isExist;
	}

	@Override
	public boolean updateFireWall(CloudFireWall fw) throws AppException {
		try {
			JSONObject fireWall = new JSONObject();
			fireWall.put("name", fw.getFwName());
			fireWall.put("firewall_policy_id", fw.getFwpId());
			fireWall.put("admin_state_up", "1");
			// 用于提交的完整数据
			JSONObject resultData = new JSONObject();
			resultData.put("firewall", fireWall);
			Firewall fire = fireWallService.update(fw.getDcId(), fw.getPrjId(), resultData, fw.getFwId());

			if (null != fire) {
				BaseCloudFireWall baseCloudFireWall = fireWallDao.findOne(fw.getFwId());
				baseCloudFireWall.setFwName(fire.getName());
				baseCloudFireWall.setFwpId(fw.getFwpId());
				fireWallDao.saveOrUpdate(baseCloudFireWall);
				return true;
			}
		} catch (AppException e) {
			throw e;
		}
		return false;
	}

	@Override
	public int countFireWallByPrjId(String prjId) throws AppException {
		int countFirewall = fireWallDao.countFireWallByPrjId(prjId);
		return countFirewall;
	}

	public boolean updateFw(CloudFireWall cloudFw) throws Exception {
		boolean flag = false;
		try {
			BaseCloudFireWall baseCloudFireWall = fireWallDao.findOne(cloudFw.getFwId());
			baseCloudFireWall.setFwStatus(cloudFw.getFwStatus());

			fireWallDao.saveOrUpdate(baseCloudFireWall);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		    flag = false;
			throw e;
		}

		return flag;
	}

	@SuppressWarnings("unchecked")
	public List<BaseCloudFireWall> queryCloudFirewallListByDcId(String dcId) {
		StringBuffer hql = new StringBuffer();
		hql.append(" from BaseCloudFireWall where dcId = ? ");
		return fireWallDao.find(hql.toString(), new Object[] { dcId });
	}

	public boolean updateCloudFirewallFromStack(BaseCloudFireWall firewall) {
		boolean flag = false;
		try {
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_firewall set ");
			sql.append("	fw_name = ?,       ");
			sql.append("	prj_id = ?,        ");
			sql.append("	dc_id = ?,         ");
			sql.append("	description = ?,   ");
			sql.append("	is_shared = ?,     ");
			sql.append("	admin_stateup = ?, ");
			sql.append("	fw_status = ?,     ");
			sql.append("	fwp_id = ?        ");
			sql.append(" where fw_id = ? ");

			fireWallDao.executeUpdate(sql.toString(),
					new Object[] { firewall.getFwName(), firewall.getPrjId(), firewall.getDcId(),
							firewall.getDescription(), firewall.getIsShared(), firewall.getAdminStateup(),
							firewall.getFwStatus(), firewall.getFwpId(), firewall.getFwId() });
			flag = true;
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			flag = false;
			throw e;
		}

		return flag;
	}

	@Override
	public CloudFireWall getFwById(String fwId) throws AppException {
		StringBuffer sql = new StringBuffer();
		sql.append("select fw.fw_id as fwId,fw.fw_name as fwName,fwp.fwp_id as fwpId,fwp.fwp_name as fwpName,fw.fw_status as fwStatus,fw.admin_stateup as adminStateup,"
				+ "cp.prj_name as prjName,fw.prj_id as prjId,dc.dc_name as dcName,fw.dc_id as dcId,fw.create_time as createTime,fw.create_name as createName,fw.description as description ");
		sql.append(" from cloud_firewall fw,cloud_project cp ,cloud_fwpolicy fwp,dc_datacenter dc");
		sql.append(" where fw.prj_id=cp.prj_id");
		sql.append(" and fw.fwp_id=fwp.fwp_id");
		sql.append(" and fw.dc_id=dc.id and fw.fw_id = ? ");
		sql.append(" group by fw.fw_id order by fw.create_time desc ");
		
		List list = fireWallDao.createSQLNativeQuery(sql.toString(), fwId).getResultList();
		CloudFireWall fireWall = null;
		if (list!=null && list.size()>0) {
			Object[] objs = (Object[]) list.get(0);
			fireWall = new CloudFireWall();
			fireWall.setFwId(String.valueOf(objs[0]));
			fireWall.setFwName(String.valueOf(objs[1]));
			fireWall.setFwpId(String.valueOf(objs[2]));
			fireWall.setFwpName(String.valueOf(objs[3]));
			fireWall.setFwStatus(String.valueOf(objs[4]));
			fireWall.setAdminStateup(String.valueOf(objs[5]));
			fireWall.setPrjName(String.valueOf(objs[6]));
			fireWall.setPrjId(String.valueOf(objs[7]));
			fireWall.setDcName(String.valueOf(objs[8]));
			fireWall.setDcId(String.valueOf(objs[9]));
			fireWall.setCreateTime((Date) objs[10]);
			fireWall.setCreateName(String.valueOf(objs[11]));
			fireWall.setDescription(objs[12]!=null?String.valueOf(objs[12]):null);
			String tag = tagService.getResourceTagForShowcase("firewall", String.valueOf(objs[0]));
			fireWall.setTags(tag);
			Firewall fw = fireWallService.getById(fireWall.getDcId(), fireWall.getPrjId(), fireWall.getFwId());
			if(!fireWall.getFwStatus().equals(fw.getStatus())){
				fireWallDao.executeUpdate(" update BaseCloudFireWall set fwStatus = ? where fwId = ? ", fw.getStatus(),fw.getId());
				fireWall.setFwStatus(fw.getStatus());
            }
			fireWall.setStatusForDis(DictUtil.getStatusByNodeEn("fireWall", fireWall.getFwStatus()));
		}
		return fireWall;
	}
	
	@Override
	public BaseCloudFireWall createFwAndFwpAndFwR(Map<String, Object> map, String createName) throws AppException {
		String fwName = map.get("name").toString();
		map.put("name", map.get("rname"));//规则名
		BaseCloudFwRule rule = null;
		BaseCloudFwPolicy policy = null;
		try{
			rule = fwrService.addFwRule(createName, map);// 创建规则
			Map<String, String> project = (Map<String, String>)map.get("project");
			policy = fwpService.addFwPolicyRule(project.get("dcId").toString(), project.get("projectId").toString(), createName,
					fwName + "policy",rule.getFwrId());// 创建策略
			return addFireWall(project.get("dcId").toString(), project.get("projectId").toString(), createName, fwName.toString(), policy.getFwpId());
		}catch(Exception e){
			if(rule!=null){
				if(policy!=null){
					CloudFwPolicy fwp = new CloudFwPolicy();
					fwp.setFwpId(policy.getFwpId());
					fwp.setDcId(policy.getDcId());
					fwp.setPrjId(policy.getPrjId());
					fwpService.releaseFwRule(fwp);//解绑规则
				}
				CloudFwRule fwr = new CloudFwRule();
				fwr.setDcId(rule.getDcId());
				fwr.setPrjId(rule.getPrjId());
				fwr.setFwrId(rule.getFwrId());
				fwrService.deleteFwRule(fwr);//删除规则
			}
			if(policy!=null){
				CloudFwPolicy fwp = new CloudFwPolicy();
				fwp.setFwpId(policy.getFwpId());
				fwp.setDcId(policy.getDcId());
				fwp.setPrjId(policy.getPrjId());
				fwpService.deleteFwp(fwp);//删除策略
			}
			throw e;
		}
	}

	@Override
	public boolean deleteFwAndFwpAndFwr(Map<String, String> map) throws AppException {
		Firewall fireWall = null;
		CloudFireWall fw = new CloudFireWall();
		fw.setDcId(map.get("dcId"));
		fw.setPrjId(map.get("prjId"));
		fw.setFwId(map.get("fwId"));
		fireWall = fireWallService.getById(fw.getDcId(), fw.getPrjId(), fw.getFwId());
		if(!"ACTIVE".equals(fireWall.getStatus()) && !"ERROR".equals(fireWall.getStatus())){
			throw new AppException("只有正常状态和错误状态下的防火墙允许删除。");
		}
		boolean fwResult = deleteFireWall(fw);//删除防火墙
		while(true){//等待底层防火墙删除
			try{
				fireWall = fireWallService.getById(fw.getDcId(), fw.getPrjId(), fw.getFwId());
				if(null==fireWall){
					break;
				}
				try {
					log.debug("等待底层删除防火墙");
					Thread.sleep(500);
				} catch (InterruptedException e) {
				    log.error(e.getMessage(), e);
				}
			}catch(AppException e){
				//出现异常说明防火墙已被删除
				break;
			}
			
		}
		
		if(!fwResult)return false;
		List<BaseCloudFwRule> listcr = fwpService.getRuleByFwpId(map.get("fwpId"));//获取策略下所有规则
		CloudFwPolicy fwp = new CloudFwPolicy();
		fwp.setFwpId(map.get("fwpId"));
		fwp.setDcId(map.get("dcId"));
		fwp.setPrjId(map.get("prjId"));
		fwpService.releaseFwRule(fwp);//解绑规则
		CloudFwRule fwr = null;
		boolean fwrResult = true;
		for(BaseCloudFwRule bcfwr : listcr){
			fwr = new CloudFwRule();
			fwr.setDcId(bcfwr.getDcId());
			fwr.setPrjId(bcfwr.getPrjId());
			fwr.setFwrId(bcfwr.getFwrId());
			fwrResult = fwrService.deleteFwRule(fwr);//删除规则
			if(!fwrResult){
				break;
			}
		}
		if(!fwrResult)return false;
		
		boolean fwpResulr = fwpService.deleteFwp(fwp);//删除策略
		
		if(fwrResult && fwpResulr && fwResult){
			return true;
		}else{
			throw new AppException("同时删除防火墙及对应的策略和规则失败");
		}
	}
	
	@Override
	public boolean updateFwNameorDesc(CloudFireWall fw) throws AppException {
		try {
			JSONObject fireWall = new JSONObject();
			fireWall.put("name", fw.getFwName());
			fireWall.put("description", fw.getDescription());
			// 用于提交的完整数据
			JSONObject resultData = new JSONObject();
			resultData.put("firewall", fireWall);
			Firewall fire = fireWallService.update(fw.getDcId(), fw.getPrjId(), resultData, fw.getFwId());

			if (null != fire) {
				BaseCloudFireWall baseCloudFireWall = fireWallDao.findOne(fw.getFwId());
				baseCloudFireWall.setFwName(fire.getName());
				baseCloudFireWall.setDescription(fire.getDescription());
				fireWallDao.saveOrUpdate(baseCloudFireWall);//修改本地数据库
				return true;
			}
		} catch (AppException e) {
			throw e;
		}
		return false;
	}
	
}
