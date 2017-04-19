package com.eayun.virtualization.ecmcservice.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.BoolUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.CompletionHook;
import com.eayun.eayunstack.model.Member;
import com.eayun.eayunstack.service.OpenstackMemberService;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.monitor.service.AlarmService;
import com.eayun.virtualization.dao.CloudLdMemberDao;
import com.eayun.virtualization.dao.CloudLdPoolDao;
import com.eayun.virtualization.ecmcservice.EcmcLBMemberService;
import com.eayun.virtualization.ecmcvo.CloudLdmemberVoe;
import com.eayun.virtualization.ecmcvo.CreateLBMemberVO;
import com.eayun.virtualization.ecmcvo.UpdateLBMemberVo;
import com.eayun.virtualization.model.BaseCloudLdMember;
import com.eayun.virtualization.model.BaseCloudLdPool;
import com.eayun.virtualization.model.CloudLdMember;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.service.TagService;

@Service
@Transactional
public class EcmcLBMemberServiceImpl implements EcmcLBMemberService {
	private static final Log log = LogFactory.getLog(EcmcLBMemberServiceImpl.class);

	@Autowired
	private OpenstackMemberService openstackMemberService;
	@Autowired
	private CloudLdMemberDao cloudLdmemberDao;
	@Autowired
	private CloudLdPoolDao cloudLdpoolDao;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private TagService tagService;
	@Autowired
	private AlarmService alarmService;
	@Autowired
	private EcmcLogService ecmcLogService;

	public Page queryMember(ParamsMap paramsMap) throws AppException {
		try {
			Map<String, Object> params = paramsMap.getParams();
			StringBuffer sql = new StringBuffer();
			sql.append("select t.member_id, t.pool_id, t.prj_id, t.dc_id, t.create_name, t.member_address, t.protocol_port, t.member_weight,");
			sql.append(" t.member_status, t.admin_stateup, t.create_time, dc.dc_name, cp.prj_name, c.cus_id,c.cus_org,vm.vm_name,pool.pool_name");
			sql.append(" from cloud_ldmember t");
			sql.append(" left join dc_datacenter dc on t.dc_id = dc.id");
			sql.append(" left join cloud_project cp on t.prj_id = cp.prj_id");
			sql.append(" left join sys_selfcustomer c on cp.customer_id = c.cus_id");
			sql.append(" left join cloud_vm vm on t.member_address = vm.vm_ip");
			sql.append(" left join cloud_ldpool pool on t.pool_id = pool.pool_id");
			sql.append(" where 1 = 1");
			List<Object> values = new ArrayList<>();
			int idx = 0;
			if(StringUtils.isNotBlank((String)params.get("dcId"))){
				sql.append(" and t.dc_id = ?").append(++idx);
				values.add(params.get("dcId"));
			}
			if (params.containsKey("prjName") && StringUtils.isNotBlank((String)params.get("prjName"))){
				sql.append(" and cp.prj_name in(?").append(++idx).append(")");
				values.add(Arrays.asList(StringUtils.split((String)params.get("prjName"), ",")));
			}
			if (params.containsKey("cusOrg") && StringUtils.isNotBlank((String)params.get("cusOrg"))){
				sql.append(" and c.cus_org in(?").append(++idx).append(")");
				values.add(Arrays.asList(StringUtils.split((String)params.get("cusOrg"), ",")));
			}
			if(StringUtils.isNotBlank((String)params.get("vmName"))){
				sql.append(" and vm.vm_name like ?").append(++idx);
				values.add("%" + (String)params.get("vmName") + "%");
			}
			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(paramsMap.getPageNumber());
			queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
			Page page = cloudLdmemberDao.pagedNativeQuery(sql.toString(), queryMap, values.toArray());
			@SuppressWarnings("unchecked")
			List<Object> dataList = (List<Object>)page.getResult();
			for (int i = 0; i < dataList.size(); i++) {
				Object[] objs = (Object[])dataList.get(i);
				CloudLdMember member = new CloudLdMember();
				member.setMemberId(ObjectUtils.toString(objs[0]));
				member.setPoolId(ObjectUtils.toString(objs[1]));
				member.setPrjId(ObjectUtils.toString(objs[2]));
				member.setDcId(ObjectUtils.toString(objs[3]));
				member.setCreateName(ObjectUtils.toString(objs[4]));
				member.setMemberAddress(ObjectUtils.toString(objs[5]));
				member.setProtocolPort(objs[6] == null ? null : ((BigDecimal)objs[6]).longValue());
				member.setMemberWeight(objs[7] == null ? null : ((BigDecimal)objs[7]).longValue());
				member.setMemberStatus(ObjectUtils.toString(objs[8]));
				member.setAdminStateup((Character)objs[9]);
				member.setCreateTime(DateUtil.stringToDate(objs[10] == null ? "" : ObjectUtils.toString(objs[10])));
				member.setDcName(ObjectUtils.toString(objs[11]));
				member.setProjectName(ObjectUtils.toString(objs[12]));
				member.setCusId(ObjectUtils.toString(objs[13]));
				member.setCusOrg(ObjectUtils.toString(objs[14]));
				member.setVmName(ObjectUtils.toString(objs[15]));
				member.setPoolName(ObjectUtils.toString(objs[16]));
				dataList.set(i, member);
			}
			return page;
		} catch (Exception e) {
			log.error(e, e);
			return null;
		}
	}

	public CloudLdmemberVoe getMemberById(String memberId) {
		CloudLdmemberVoe cloudLdmemberVoe = new CloudLdmemberVoe();

		BaseCloudLdMember cloudLdmember = cloudLdmemberDao.findOne(memberId);
		BeanUtils.copyPropertiesByModel(cloudLdmemberVoe, cloudLdmember);
		// 1.资源池
		String poolName = "";
		BaseCloudLdPool cloudLdpool = cloudLdpoolDao.findOne(cloudLdmemberVoe.getPoolId());
		if (cloudLdpool != null) {
			poolName = cloudLdpool.getPoolName();
		}
		cloudLdmemberVoe.setPoolName(poolName);
		// 2.所属项目
		if (cloudLdpool != null) {
		    String projectName = cloudLdpoolDao.getProjectName(cloudLdpool.getPrjId());
	        cloudLdmemberVoe.setProjectName(projectName);
		}
		// 3.管理员状态
		cloudLdmemberVoe.setAdminStateupStr(BoolUtil.bool2Str(cloudLdmember.getAdminStateup()));

		return cloudLdmemberVoe;
	}

	/**
	 * 添加成员
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 * @throws AppException
	 */
	public BaseCloudLdMember createMember(CreateLBMemberVO member) throws AppException{
		
		BaseCloudLdPool pool = cloudLdpoolDao.findOne(member.getPoolId());
		if (pool == null) {
			throw new AppException("负载均衡["+ member.getPoolId() +"]池不存在");
		}
		
		BaseCloudLdMember resultData= null;
		JSONObject data=new JSONObject();
		JSONObject temp=new JSONObject();
		String rule = member.getRules();
		String [] rules = new String[]{};
		if(rule!=null && !"".equals(rule)){
			rules= rule.split(",");
		}
		for(int i=0;i<rules.length;i++){
			temp.put("protocol_port", member.getProtocolPort());
			temp.put("address", rules[i].substring(6));
			temp.put("pool_id", member.getPoolId());
			temp.put("weight", member.getMemberWeight());
			temp.put("admin_state_up", member.getAdminStateup());
			data.put("member", temp);
			Member result=openstackMemberService.create(pool.getDcId(), pool.getPrjId(), data);
			if (result!=null) {
				resultData=new BaseCloudLdMember();
				resultData.setMemberId(result.getId());
				resultData.setPoolId(result.getPool_id());
				resultData.setPrjId(pool.getPrjId());
				resultData.setDcId(pool.getDcId());
				
				//从session中获取当前用户名
				BaseEcmcSysUser user = EcmcSessionUtil.getUser();
				resultData.setCreateName(user == null ? null : user.getAccount());
				
				resultData.setMemberAddress(result.getAddress());
				resultData.setProtocolPort(Long.parseLong(result.getProtocol_port()));
				resultData.setMemberWeight(Long.parseLong(result.getWeight()));
				resultData.setMemberStatus(result.getStatus().toUpperCase());
				resultData.setCreateTime(new Date());
				resultData.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));
				
				cloudLdmemberDao.save(resultData);
				
				if(null!=result.getStatus()&&!"ACTIVE".equals(result.getStatus())){
					// 同步新增资源池状态
					JSONObject json =new JSONObject();
					json.put("memberId", resultData.getMemberId());
					json.put("dcId",resultData.getDcId());
					json.put("prjId", resultData.getPrjId());
					json.put("memberStatus", resultData.getMemberStatus());
					json.put("count", "0");
					try {
						jedisUtil.push(RedisKey.ldMemberKeyRefresh+resultData.getMemberId(), json.toJSONString());
					} catch (Exception e) {
						throw new AppException("同步新增资源池状态出错", e);
					}
				}
				
			}
		}
		return resultData;
	}

	@SuppressWarnings("rawtypes")
    @Override
	public List<BaseCloudLdMember> createMember(Map map) throws AppException {
		List<BaseCloudLdMember>  list = new ArrayList<BaseCloudLdMember>();
		String poolId = map.get("poolId")==null?"":map.get("poolId").toString();
		String dcId = map.get("dcId")==null?"":map.get("dcId").toString();
		String prjId = map.get("prjId")==null?"":map.get("prjId").toString();
		List<Map> memberList = (List<Map>) map.get("memberList");
		BaseCloudLdPool pool = cloudLdpoolDao.findOne(poolId);
		if (pool == null) {
			throw new AppException("负载均衡["+ poolId +"]池不存在");
		}

		JSONObject data=new JSONObject();
		JSONObject temp=new JSONObject();
		for (int i=0; i<memberList.size();i++){
			Map member = memberList.get(0);
			temp.put("protocol_port", member.get("protocolPort"));
			temp.put("address",member.get("memberAddress"));
			temp.put("pool_id",poolId);
			temp.put("weight", member.get("memberWeight"));
			temp.put("admin_state_up", "1");
			data.put("member", temp);
			Member result=openstackMemberService.create(pool.getDcId(), pool.getPrjId(), data);
			if (result!=null) {
				BaseCloudLdMember resultData=new BaseCloudLdMember();
				resultData.setMemberId(result.getId());
				resultData.setPoolId(poolId);
				resultData.setPrjId(pool.getPrjId());
				resultData.setDcId(pool.getDcId());

				//从session中获取当前用户名
				BaseEcmcSysUser user = EcmcSessionUtil.getUser();
				resultData.setCreateName(user == null ? null : user.getAccount());

				resultData.setMemberAddress(result.getAddress());
				resultData.setProtocolPort(Long.parseLong(result.getProtocol_port()));
				resultData.setMemberWeight(Long.parseLong(result.getWeight()));
				resultData.setMemberStatus(result.getStatus().toUpperCase());
				resultData.setCreateTime(new Date());
				resultData.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));

				cloudLdmemberDao.save(resultData);

				if(null!=result.getStatus()&&!"ACTIVE".equals(result.getStatus())){
					// 同步新增资源池状态
					JSONObject json =new JSONObject();
					json.put("memberId", resultData.getMemberId());
					json.put("dcId",resultData.getDcId());
					json.put("prjId", resultData.getPrjId());
					json.put("memberStatus", resultData.getMemberStatus());
					json.put("count", "0");
					try {
						jedisUtil.push(RedisKey.ldMemberKeyRefresh+resultData.getMemberId(), json.toJSONString());
					} catch (Exception e) {
						throw new AppException("同步新增资源池状态出错", e);
					}
				}
				list.add(resultData);
			}
		}

		return list;
	}

	@Override
	public List<CloudLdMember> addMember(CloudLdPool pool) throws AppException {
		List <CloudLdMember> memberList = null;
		JSONObject data=new JSONObject();
		JSONObject temp=new JSONObject();

		try {
			memberList = pool.getMembers();
			if(null!=memberList&&memberList.size()>0){
				int num = countMemberByPool(pool.getPoolId());
				if(memberList.size()>(50-num)){
					throw new AppException("error.openstack.message", new String[] {"已绑定成员："+num+"个（上限50个）" });
				}
				if("1".equals(pool.getMode())){//主备模式下的成员
					for(CloudLdMember member : memberList){
						if("Active".equals(member.getRole())){
							doSomeByActive(temp,member,pool,data);
						}else if("Backup".equals(member.getRole())){
							doSomeByBackup(temp,member,pool,data);
						}
					}
				}else{//普通模式下的成员
					for(CloudLdMember member : memberList){
						doSomeByNormal(temp,member,pool,data);
					}
				}
			}


		} catch (AppException e) {
			throw e;
		} catch(Exception e){
		    log.error(e.toString(),e);
			throw new AppException ("error.openstack.message");
		}

		return memberList;
	}

	private void doSomeByBackup(JSONObject temp, CloudLdMember member,CloudLdPool pool, JSONObject data) throws Exception{
		temp.put("protocol_port", member.getProtocolPort());
		temp.put("address", member.getMemberAddress());
		temp.put("pool_id", pool.getPoolId());
		temp.put("priority", member.getPriority());
		temp.put("admin_state_up", "1");
		data.put("member", temp);
		final Member result=openstackMemberService.create(pool.getDcId(), pool.getPrjId(), data);
		if (result!=null) {
			BaseCloudLdMember resultData= new BaseCloudLdMember();
			jedisUtil.set(RedisKey.MEMBER_ADD+result.getId(),"1");
			resultData.setMemberId(result.getId());
			resultData.setPoolId(result.getPool_id());
			resultData.setPrjId(pool.getPrjId());
			resultData.setDcId(pool.getDcId());
			resultData.setMemberAddress(result.getAddress());
			resultData.setProtocolPort(Long.parseLong(result.getProtocol_port()));
			resultData.setPriority(Integer.parseInt(result.getPriority()));
			resultData.setMemberStatus(result.getStatus().toUpperCase());
			resultData.setCreateTime(new Date());
			BaseEcmcSysUser user = EcmcSessionUtil.getUser();
			resultData.setCreateName(user==null?"":user.getAccount());
			resultData.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));
			resultData.setVmId(member.getVmId());
			resultData.setRole(member.getRole());
			cloudLdmemberDao.saveOrUpdate(resultData);
//			changeMembersStatusFromPoolByDb(result.getPool_id());
			if(null!=result.getStatus()&&!"ACTIVE".equals(result.getStatus())){
				sendRedisSync(resultData);
			}
			TransactionHookUtil.registAfterCompletionHook(new CompletionHook() {
				
				@Override
				public void execute(int status) {
					try {
						if(status==0){
							jedisUtil.set(RedisKey.MEMBER_ADD+result.getId(),"2");
						}else{
							jedisUtil.delete(RedisKey.MEMBER_ADD+result.getId());
						}
					} catch (Exception e) {
						log.error(e.getMessage(),e);
					}
				}
			});
		}
	}

	private void doSomeByActive(JSONObject temp, CloudLdMember member,
			CloudLdPool pool, JSONObject data) throws Exception{
		temp.put("protocol_port", member.getProtocolPort());
		temp.put("address", member.getMemberAddress());
		temp.put("pool_id", pool.getPoolId());
		temp.put("weight", member.getMemberWeight());
		temp.put("admin_state_up", "1");
		data.put("member", temp);
		final Member result=openstackMemberService.create(pool.getDcId(), pool.getPrjId(), data);
		if (result!=null) {
			BaseCloudLdMember resultData= new BaseCloudLdMember();
			jedisUtil.set(RedisKey.MEMBER_ADD+result.getId(),"1");
			resultData.setMemberId(result.getId());
			resultData.setPoolId(result.getPool_id());
			resultData.setPrjId(pool.getPrjId());
			resultData.setDcId(pool.getDcId());
			resultData.setMemberAddress(result.getAddress());
			resultData.setProtocolPort(Long.parseLong(result.getProtocol_port()));
			resultData.setMemberWeight(Long.parseLong(result.getWeight()));
			resultData.setMemberStatus(result.getStatus().toUpperCase());
			resultData.setCreateTime(new Date());
			BaseEcmcSysUser user = EcmcSessionUtil.getUser();
			resultData.setCreateName(user==null?"":user.getAccount());
			resultData.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));
			resultData.setVmId(member.getVmId());
			resultData.setRole(member.getRole());
			resultData.setPriority(256);
//			resultData.setIsUndertaker(true);
			cloudLdmemberDao.saveOrUpdate(resultData);
//			changeMembersStatusFromPoolByDb(resultData.getPoolId());
			if(null!=result.getStatus()&&!"ACTIVE".equals(result.getStatus())){
				sendRedisSync(resultData);
			}
			TransactionHookUtil.registAfterCompletionHook(new CompletionHook() {
				
				@Override
				public void execute(int status) {
					try {
						if(status==0){
							jedisUtil.set(RedisKey.MEMBER_ADD+result.getId(),"2");
						}else{
							jedisUtil.delete(RedisKey.MEMBER_ADD+result.getId());
						}
					} catch (Exception e) {
						log.error(e.getMessage(),e);
					}
				}
			});
		}
	}

	private void changeMembersStatusFromPoolByDb(String poolId) {
		cloudLdmemberDao.execSQL("UPDATE cloud_ldmember SET is_undertaker = '0' WHERE pool_id = ? AND role IS NOT NULL",poolId);
		List<BaseCloudLdMember> list=cloudLdmemberDao.find("from BaseCloudLdMember where poolId=? AND memberStatus!='INACTIVE' AND role !=null",poolId);
		List<BaseCloudLdMember>	canUseActiveList=new ArrayList<BaseCloudLdMember>();//状态正常的主节点
		List<BaseCloudLdMember>	canUseBackupList=new ArrayList<BaseCloudLdMember>();//状态正常从节点
		for(BaseCloudLdMember member:list){
			if("Active".equals(member.getRole())){
				canUseActiveList.add(member);
			}else if("Backup".equals(member.getRole())){
				canUseBackupList.add(member);
			}
		}
		changeUndertaker(canUseActiveList,canUseBackupList);
	}

	private void changeUndertaker(List<BaseCloudLdMember> canUseActiveList,
			List<BaseCloudLdMember> canUseBackupList) {
		if(canUseActiveList.size()>0){
			for (BaseCloudLdMember baseCloudLdMember : canUseActiveList){
				baseCloudLdMember.setIsUndertaker(true);
				cloudLdmemberDao.saveOrUpdate(baseCloudLdMember);
			}
		}else{
			BaseCloudLdMember undertaker=null;
			List<BaseCloudLdMember> maxBackupsPriority=new ArrayList<BaseCloudLdMember>();
			Collections.sort(canUseBackupList, new Comparator(){
				@Override
				public int compare(Object o1, Object o2) {
					BaseCloudLdMember member1=(BaseCloudLdMember)o1;
					BaseCloudLdMember member2=(BaseCloudLdMember)o2;
					if(member1.getPriority()>member2.getPriority()){
						return 1;
					}else if(member1.getPriority()==member2.getPriority()){
						return 0;
					}else{
						return -1;
					}
				}
			});
			if(canUseBackupList!=null&&canUseBackupList.size()>0){
				int priority=((BaseCloudLdMember)canUseBackupList.get(0)).getPriority();
				for (BaseCloudLdMember baseCloudLdMember : canUseBackupList){
					int thisPriority=baseCloudLdMember.getPriority();
					if(thisPriority==priority){
						maxBackupsPriority.add(baseCloudLdMember);
					}else{
						break;
					}
				}
				List<BaseCloudLdMember> maxBackupsIp=new ArrayList<BaseCloudLdMember>();
				if(maxBackupsPriority.size()>1){
					Collections.sort(maxBackupsPriority, new Comparator(){
						@Override
						public int compare(Object o1, Object o2) {
							BaseCloudLdMember member1=(BaseCloudLdMember)o1;
							BaseCloudLdMember member2=(BaseCloudLdMember)o2;
							String ip1=member1.getMemberAddress();
							String ip2=member2.getMemberAddress();
							return ip1.compareTo(ip2);
						}
					});
					String ip=((BaseCloudLdMember)maxBackupsPriority.get(0)).getMemberAddress();
					for (BaseCloudLdMember baseCloudLdMember : maxBackupsPriority){
						String thisIp=baseCloudLdMember.getMemberAddress();
						if(thisIp.equals(ip)){
							maxBackupsIp.add(baseCloudLdMember);
						}else{
							break;
						}
					}
					if(maxBackupsIp.size()>0){
						Collections.sort(maxBackupsIp, new Comparator(){
							@Override
							public int compare(Object o1, Object o2) {
								BaseCloudLdMember member1=(BaseCloudLdMember)o1;
								BaseCloudLdMember member2=(BaseCloudLdMember)o2;
								long port1=member1.getProtocolPort();
								long port2=member2.getProtocolPort();
								if(port1>port2){
									return 1;
								}else{
									return -1;
								}
							}
						});
						undertaker = maxBackupsIp.get(0);
					}else{
						undertaker = maxBackupsPriority.get(0);
					}
				}else{
					undertaker = canUseBackupList.get(0);
				}
				if(undertaker!=null){
					undertaker.setIsUndertaker(true);
					cloudLdmemberDao.saveOrUpdate(undertaker);
				}
			}
			
		}
	}

	private void doSomeByNormal(JSONObject temp, CloudLdMember member,
			CloudLdPool pool, JSONObject data) throws Exception{
			temp.put("protocol_port", member.getProtocolPort());
			temp.put("address", member.getMemberAddress());
			temp.put("pool_id", pool.getPoolId());
			temp.put("weight", member.getMemberWeight());
			temp.put("admin_state_up", "1");
			data.put("member", temp);
			final Member result=openstackMemberService.create(pool.getDcId(), pool.getPrjId(), data);
			if (result!=null) {
				BaseCloudLdMember resultData= new BaseCloudLdMember();
				jedisUtil.set(RedisKey.MEMBER_ADD+result.getId(),"1");
				resultData.setMemberId(result.getId());
				resultData.setPoolId(result.getPool_id());
				resultData.setPrjId(pool.getPrjId());
				resultData.setDcId(pool.getDcId());
				resultData.setMemberAddress(result.getAddress());
				resultData.setProtocolPort(Long.parseLong(result.getProtocol_port()));
				resultData.setMemberWeight(Long.parseLong(result.getWeight()));
				resultData.setMemberStatus(result.getStatus().toUpperCase());
				resultData.setCreateTime(new Date());
				BaseEcmcSysUser user = EcmcSessionUtil.getUser();
				resultData.setCreateName(user==null?"":user.getAccount());
				resultData.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));
				resultData.setVmId(member.getVmId());
				resultData.setPriority(256);
				cloudLdmemberDao.saveOrUpdate(resultData);
				if(null!=result.getStatus()&&!"ACTIVE".equals(result.getStatus())){
					sendRedisSync(resultData);
				}
				TransactionHookUtil.registAfterCompletionHook(new CompletionHook() {
					
					@Override
					public void execute(int status) {
						try {
							if(status==0){
								jedisUtil.set(RedisKey.MEMBER_ADD+result.getId(),"2");
							}else{
								jedisUtil.delete(RedisKey.MEMBER_ADD+result.getId());
							}
						} catch (Exception e) {
							log.error(e.getMessage(),e);
						}
					}
				});
			}
	}

	private void sendRedisSync(BaseCloudLdMember resultData) throws Exception{
			JSONObject json =new JSONObject();
			json.put("memberId", resultData.getMemberId());
			json.put("dcId",resultData.getDcId());
			json.put("prjId", resultData.getPrjId());
			json.put("memberStatus", resultData.getMemberStatus());
			json.put("count", "0");
			jedisUtil.push(RedisKey.ldMemberKeyRefresh+resultData.getMemberId(), json.toJSONString());
	}

	@Override
	public boolean checkMemberExsit(CloudLdMember cloudLdm) throws AppException {
		StringBuffer hql = new StringBuffer();
		Object [] objs =new Object[]{
				cloudLdm.getPoolId(),
				cloudLdm.getMemberAddress(),
				cloudLdm.getProtocolPort(),
				cloudLdm.getVmId()
		};
		hql.append(" from BaseCloudLdMember ");
		hql.append(" where poolId = ?");
		hql.append(" and memberAddress = ?");
		hql.append(" and protocolPort = ?");
		hql.append(" and vmId = ?");
		if(!org.apache.commons.lang3.StringUtils.isEmpty(cloudLdm.getMemberId())){
			hql.append(" and memberId <> ?");
			objs = new Object[]{
					cloudLdm.getPoolId(),
					cloudLdm.getMemberAddress(),
					cloudLdm.getProtocolPort(),
					cloudLdm.getVmId(),
					cloudLdm.getMemberId()
			};
		}

		List<BaseCloudLdMember> list = cloudLdmemberDao.find(hql.toString(), objs);
		return null==list||list.size()==0;
	}

    @Override
    public List<CloudLdMember> getMemberList(String poolId,String checkRole) throws AppException {
        StringBuffer sql = new StringBuffer();
        List<CloudLdMember> list = new ArrayList<CloudLdMember>();

        sql.append("  SELECT                                                    ");
        sql.append("  	member.dc_id,                                           ");
        sql.append("  	member.prj_id,                                          ");
        sql.append("  	member.member_id,                                       ");
        sql.append("  	member.member_address,                                  ");
        sql.append("  	member.member_status,                                   ");
        sql.append("  	member.member_weight,                                   ");
        sql.append("  	member.protocol_port,                                   ");
        sql.append("  	member.vm_id,                                          ");
        sql.append("  	member.pool_id,                                         ");
        sql.append("  	vm.vm_name,                                              ");
		sql.append("  	member.role,                                          ");
		sql.append("  	member.is_undertaker,                                          ");
		sql.append("  	member.priority                                          ");
        sql.append("  FROM                                                      ");
        sql.append("  	cloud_ldmember member                                   ");
        sql.append("  LEFT JOIN cloud_vm vm ON member.member_address = vm.vm_ip ");
        sql.append("  AND member.vm_id = vm.vm_id                               ");
        sql.append("  WHERE member.pool_id = ?                                  ");
        if("Active".equals(checkRole)){
        	sql.append("  AND  member.role = 'Active' and member.is_undertaker='0'                                 ");
        }else if("ActiveIsUndertaker".equals(checkRole)){
        	sql.append("  AND member.role = 'Active'  AND member.is_undertaker='1'                             ");
        }else if("Backup".equals(checkRole)){
        	sql.append("  AND  member.role = 'Backup' and member.is_undertaker='0'                                   ");
        }else if("BackupIsUndertaker".equals(checkRole)){
        	sql.append("  AND member.role = 'Backup'  AND member.is_undertaker='1'                             ");
        }
        sql.append("  order by member.create_time desc ");

        Query query = cloudLdmemberDao.createSQLNativeQuery(sql.toString(), new Object[]{poolId});

        List listResult =query.getResultList();
        for (int i=0;i<listResult.size();i++) {
            int  index = 0;
            Object [] obj = (Object [])listResult.get(i);
            CloudLdMember cloudLdmember = new CloudLdMember();
            cloudLdmember.setDcId((String)obj[index++]);
            cloudLdmember.setPrjId((String)obj[index++]);
            cloudLdmember.setMemberId((String)obj[index++]);
            cloudLdmember.setMemberAddress((String)obj[index++]);
            cloudLdmember.setMemberStatus((String)obj[index++]);
            cloudLdmember.setMemberWeight(Long.parseLong(obj[index++] != null ? String.valueOf(obj[index-1]) : "0"));
            cloudLdmember.setProtocolPort(Long.parseLong(obj[index++] != null ? String.valueOf(obj[index-1]) : "0"));
            cloudLdmember.setVmId((String)obj[index++]);
            cloudLdmember.setPoolId((String)obj[index++]);
            cloudLdmember.setVmName((String)obj[index++]);
            cloudLdmember.setRole(String.valueOf(obj[index++]));
			String isUndertaker=String.valueOf(obj[index++]);
			cloudLdmember.setIsUndertaker(isUndertaker.equals("1")?true:false);
			Object priority=obj[index++];
			cloudLdmember.setPriority(priority!=null?(int)priority:256);
            cloudLdmember.setStatusForMember(DictUtil.getStatusByNodeEn("ldMember", cloudLdmember.getMemberStatus()));

            list.add(cloudLdmember);
        }

        return list;
    }

    @Override
    public CloudLdMember update(final CloudLdMember member) throws Exception {
    	jedisUtil.set(RedisKey.MEMBER_UPDATE+member.getMemberId(),"1");
        CloudLdMember resourceVoe = new CloudLdMember();
        JSONObject data = new JSONObject();
        JSONObject temp = new JSONObject();
        BaseCloudLdPool cloudLdPool = cloudLdpoolDao.findOne(member.getPoolId());
        temp.put("pool_id", member.getPoolId());
        if("0".equals(cloudLdPool.getMode())||"Active".equals(member.getRole())){
			temp.put("weight",  member.getMemberWeight());
		}
		if("Backup".equals(member.getRole())){
			temp.put("priority",  member.getPriority());
		}
        data.put("member", temp);

        Member result = openstackMemberService.update(member.getDcId(), member.getPrjId(), data, member.getMemberId());
        if (result != null) {
            BaseCloudLdMember cloudResource = cloudLdmemberDao.findOne(member.getMemberId());
            cloudResource.setMemberWeight(Long.parseLong(result.getWeight()));
            cloudResource.setProtocolPort(Long.parseLong(result.getProtocol_port()));
            cloudResource.setPriority(Integer.parseInt(result.getPriority()));
            cloudResource.setMemberStatus(result.getStatus());
            cloudLdmemberDao.saveOrUpdate(cloudResource);
            BeanUtils.copyPropertiesByModel(resourceVoe, cloudResource);
            TransactionHookUtil.registAfterCompletionHook(new CompletionHook() {
				
				@Override
				public void execute(int status) {
					try {
						if(status==0){
							jedisUtil.set(RedisKey.MEMBER_UPDATE+member.getMemberId(),"2");
						}else{
							jedisUtil.delete(RedisKey.MEMBER_UPDATE+member.getMemberId());
						}
					} catch (Exception e) {
						log.error(e.getMessage(),e);
					}
				}
			});
//            changeMembersStatusFromPoolByDb(member.getPoolId());
            if(null!=result.getStatus()&&!"ACTIVE".equals(result.getStatus())){
                JSONObject json =new JSONObject();
                json.put("memberId", cloudResource.getMemberId());
                json.put("dcId",cloudResource.getDcId());
                json.put("prjId", cloudResource.getPrjId());
                json.put("memberStatus", cloudResource.getMemberStatus());
                json.put("count", "0");
                jedisUtil.push(RedisKey.ldMemberKeyRefresh+cloudResource.getMemberId(), json.toJSONString());
            }

        }
        return resourceVoe;
    }

    @Override
    public void deleteMember(final CloudLdMember member) throws AppException{
    	try {
			jedisUtil.set(RedisKey.MEMBER_DELETE+member.getMemberId(),"1");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
        boolean flag = openstackMemberService.delete(member.getDcId(), member.getPrjId(), member.getMemberId());
        if(flag){
            cloudLdmemberDao.delete(member.getMemberId());
//            changeMembersStatusFromPoolByDb(member.getPoolId());
			alarmService.clearExpAfterDeleteMember(member.getMemberId());
			TransactionHookUtil.registAfterCompletionHook(new CompletionHook() {
				
				@Override
				public void execute(int status) {
					try {
						if(status==0){
							jedisUtil.set(RedisKey.MEMBER_DELETE+member.getMemberId(),"2");
							jedisUtil.addToSet(RedisKey.MEMBER_DELETE_POOLID+member.getPoolId(),member.getMemberId());
						}else{
							jedisUtil.delete(RedisKey.MEMBER_DELETE+member.getMemberId());
						}
					} catch (Exception e) {
						log.error(e.getMessage(),e);
					}
				}
			});
        }
    }

    /**
	 * 查询当前资源池的总数
	 * ---------------------
	 * @author zhouhaitao
	 * @param pool
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private int countMemberByPool(String poolId){
		int num = 0;
		StringBuffer sql = new StringBuffer();
		sql.append(" select count(1) from cloud_ldmember where pool_id = ? ");
		Query query = cloudLdmemberDao.createSQLNativeQuery(sql.toString(), new Object[]{poolId});

		List listResult =query.getResultList();
		if(null!=listResult&&listResult.size()==1){
			BigInteger bi = (BigInteger)listResult.get(0);
			num = bi.intValue();
		}

		return num;
	}

	/**
	 * 修改成员
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public CloudLdmemberVoe updateMember(UpdateLBMemberVo vo) throws AppException {
		
		BaseCloudLdMember member = cloudLdmemberDao.findOne(vo.getMemberId());
		if (member == null) {
			throw new AppException("成员["+ vo.getMemberId() +"]不存在");
		}
		
		CloudLdmemberVoe resourceVoe = null;
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		
		temp.put("pool_id", vo.getPoolId());
		temp.put("weight", vo.getWeight());
		temp.put("admin_state_up", vo.getAdminStateUp());
		data.put("member", temp);

		
		Member result = openstackMemberService.update(member.getDcId(), member.getPrjId(), data, vo.getMemberId());
		
		if (result != null) {
			resourceVoe = new CloudLdmemberVoe();
			// 从数据库中查询该id的实体
			member.setMemberWeight(vo.getWeight());
			member.setPoolId(vo.getPoolId());
			member.setMemberWeight(vo.getWeight());
			member.setAdminStateup(vo.getAdminStateUp());
			member.setMemberStatus(result.getStatus());
			cloudLdmemberDao.saveOrUpdate(member);
			
			if(null!=result.getStatus()&&!"ACTIVE".equals(result.getStatus())){
				// 启动资源池的自动任务
				JSONObject json =new JSONObject();
				json.put("memberId", member.getMemberId());
				json.put("dcId", member.getDcId());
				json.put("prjId", member.getPrjId());
				json.put("memberStatus", member.getMemberStatus());
				json.put("count", "0");
				try {
					jedisUtil.push(RedisKey.ldMemberKeyRefresh+member.getMemberId(), json.toJSONString());
				} catch (Exception e) {
					log.error(e.getMessage(),e);
				}
			}
			
		}
		return resourceVoe;
		
	}

	/**
	 * 删除一条成员
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean deleteMember(String memberId) throws AppException {
		
		BaseCloudLdMember member = cloudLdmemberDao.findOne(memberId);
		if (member == null) {
			throw new AppException("成员["+ memberId +"]不存在");
		}
		
		// 执行openstack删除操作成功后，进行后续操作
		if (openstackMemberService.delete(member.getDcId(), member.getPrjId(), memberId)) {
			cloudLdmemberDao.delete(memberId);
			//删除资源后更新缓存接口
			tagService.refreshCacheAftDelRes("ldMember", memberId);
			return true;
		}
		
		return false;
		
	}
	
	public boolean checkMemberPort(String address, Long protocolPort, String memberId) throws AppException {
		if(StringUtils.isBlank(address)){
			return false;
		}
		List<String> addresses = Arrays.asList(StringUtils.split(address, ","));
		return cloudLdmemberDao.countMemberPort(addresses, protocolPort, memberId) > 0 ? true : false;
	}
	
	public List<CloudLdMember> getMemeberListBySubnet(CloudLdMember cloudLdm) throws Exception {
		StringBuffer sql = new StringBuffer();
		List<CloudLdMember> memberList = new ArrayList<CloudLdMember>();
		
		sql.append("  SELECT                  ");
		sql.append("  	vm_id,              ");
		sql.append("  	vm_name,              ");
		sql.append("  	vm.vm_ip,             ");
		sql.append("  	vm.subnet_id          ");
		sql.append("  FROM                    ");
		sql.append("  	cloud_vm vm           ");
		sql.append("  WHERE                   ");
		sql.append("  	1 = 1                 ");
		sql.append("  AND vm.is_deleted = '0' ");
		sql.append("  AND vm.subnet_id = ?    ");
		sql.append("  AND (vm.vm_status ='ACTIVE'        ");
		sql.append("       or vm.vm_status ='SHUTOFF'    ");
		sql.append("       or vm.vm_status ='SUSPENDED') ");
		
		Query query = cloudLdmemberDao.createSQLNativeQuery(sql.toString(), new Object[]{cloudLdm.getSubnetId()});
		
		@SuppressWarnings("rawtypes")
		List listResult =query.getResultList();
		for (int i=0;i<listResult.size();i++) {
			int  index = 0;
			Object [] obj = (Object [])listResult.get(i);
			CloudLdMember cloudLdmember = new CloudLdMember();
			cloudLdmember.setVmId((String)obj[index++]);
			cloudLdmember.setVmName((String)obj[index++]);
			cloudLdmember.setMemberAddress((String)obj[index++]);
			cloudLdmember.setSubnetId((String)obj[index++]);
			
			memberList.add(cloudLdmember);
		}
		return memberList;
	}

	/**
	 * 根据云主机删除成员
	 * @Author: duanbinbin
	 * @param vmId
	 *<li>Date: 2016年4月26日</li>
	 */
	@SuppressWarnings("unchecked")
	public void deleteMemberByVm(String vmId){
		StringBuffer hql = new StringBuffer("from BaseCloudLdMember where vmId = ? ");

		List<BaseCloudLdMember> memList = cloudLdmemberDao.find(hql.toString(), new Object[]{vmId});
		if(null!=memList && memList.size()>0){
			for(BaseCloudLdMember member :memList){
				CloudLdMember ldMember = new CloudLdMember();
				ldMember.setDcId(member.getDcId());
				ldMember.setPrjId(member.getPrjId());
				ldMember.setMemberId(member.getMemberId());
				deleteMember(ldMember);
			}
		}


	}
	
	public void deleteMemberWithoutStack(CloudLdMember cloudLdMember) throws Exception {
		cloudLdmemberDao.delete(cloudLdMember.getMemberId());
		ecmcLogService.addLog("同步资源清除数据", toType(cloudLdMember), "成员", cloudLdMember.getPrjId(), 1, cloudLdMember.getMemberId(), null);
		
		JSONObject json = new JSONObject();
		json.put("resourceType", ResourceSyncConstant.LDMEMBER);
		json.put("resourceId", cloudLdMember.getMemberId());
		json.put("resourceName", cloudLdMember.getVmName());
		json.put("synTime", new Date());
		jedisUtil.push(RedisKey.MEMBER_STAUS_SYNC_DELETED_RESOURCE, json.toJSONString());
		alarmService.clearExpAfterDeleteMember(cloudLdMember.getMemberId());
	}
	/**
	 * 拼装同步删除发送日志的资源类型
	 * @param member
	 * @return
	 */
	private String toType(BaseCloudLdMember member) {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer resourceType = new StringBuffer();
        resourceType.append(ResourceSyncConstant.LDMEMBER);
        if(null != member && null != member.getCreateTime()){
        	resourceType.append(ResourceSyncConstant.SEPARATOR).append("创建时间：").append(sdf.format(member.getCreateTime()));
        }
        return resourceType.toString();
	}
	@Override
	public void changeMembersStatus(String poolId) throws Exception {
		cloudLdmemberDao.execSQL("UPDATE cloud_ldmember SET member_status = 'ACTIVE' WHERE pool_id = ?",poolId);
		changeMembersStatusFromPoolByDb(poolId);
	}
}
