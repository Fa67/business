package com.eayun.virtualization.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.BoolUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.CompletionHook;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.eayunstack.model.Member;
import com.eayun.eayunstack.service.OpenstackMemberService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.monitor.model.BaseCloudLdpoolExp;
import com.eayun.monitor.service.AlarmService;
import com.eayun.monitor.service.LdPoolAlarmMonitorService;
import com.eayun.virtualization.dao.CloudLdMemberDao;
import com.eayun.virtualization.model.*;
import com.eayun.virtualization.service.HealthMonitorService;
import com.eayun.virtualization.service.LdPoolMonitorService;
import com.eayun.virtualization.service.MemberService;
import com.eayun.virtualization.service.PoolService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;

import java.math.BigInteger;
import java.util.*;
@Service
@Transactional
public class MemberServiceImpl implements MemberService {
	private static final Logger log = LoggerFactory.getLogger(MemberServiceImpl.class);
	@Autowired
	private CloudLdMemberDao memberDao;
	@Autowired
	private OpenstackMemberService openStackService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private LdPoolMonitorService ldPoolMonitorService;
	@Autowired
	private OpenstackMemberService openstackMemberService;
	@Autowired
	private LdPoolAlarmMonitorService ldPoolAlarmMonitorService;
	@Autowired
	private HealthMonitorService healthMonitorService;
	@Autowired
	private PoolService poolService;
	@Autowired
	private AlarmService alarmService;

	/*
	 *根据prjId查询个数 
	 */
	public int getCountByPrjId(String prjId){
		return memberDao.getCountByPrjId(prjId);
	}
	
	
	@SuppressWarnings("rawtypes")
	public List<CloudLdMember> getMemberList(String poolId,String checkRole) throws Exception {
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
		sql.append("  	member.vm_id,                                           ");
		sql.append("  	member.pool_id,                                         ");
		sql.append("  	vm.vm_name,                                             ");
		sql.append("  	pool.pool_name,                                          ");
		sql.append("  	member.role,                                          ");
		sql.append("  	member.is_undertaker,                                          ");
		sql.append("  	member.priority                                          ");
		sql.append("  FROM                                                      ");
		sql.append("  	cloud_ldmember member                                   ");
		sql.append("  LEFT JOIN cloud_vm vm ON member.member_address = vm.vm_ip ");
		sql.append("  AND member.vm_id = vm.vm_id                               ");
		sql.append("  LEFT JOIN cloud_ldpool pool ON member.pool_id = pool.pool_id ");
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
		
		Query query = memberDao.createSQLNativeQuery(sql.toString(), new Object[]{poolId});
		
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
			cloudLdmember.setPoolName((String)obj[index++]);
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
	
	/**
	 * 添加成员
	 * 
	 * @author zhouhaitao
	 * @param pool
	 * @param sessionUser
	 */
	public List<CloudLdMember> addMember(CloudLdPool pool , SessionUserInfo sessionUser) throws AppException {
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
							doSomeByActive(temp,member,pool,data,sessionUser);
						}else if("Backup".equals(member.getRole())){
							doSomeByBackup(temp,member,pool,data,sessionUser);
						}
					}
				}else{//普通模式下的成员
					for(CloudLdMember member : memberList){
						doSomeByNormal(temp,member,pool,data,sessionUser);
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
	/**
	 * 添加成员(普通模式)
	 * @param temp
	 * @param member
	 * @param pool
	 * @param data
	 * @param sessionUser
	 * @throws Exception
	 */
	private void doSomeByNormal(JSONObject temp, CloudLdMember member, CloudLdPool pool, JSONObject data, SessionUserInfo sessionUser) throws Exception{
		log.info("memberId:"+member.getMemberId()+"开始添加成员(普通模式)");
		temp.put("protocol_port", member.getProtocolPort());
		temp.put("address", member.getMemberAddress());
		temp.put("pool_id", pool.getPoolId());
		temp.put("weight", member.getMemberWeight());
		temp.put("admin_state_up", "1");
		data.put("member", temp);
		final Member result=openStackService.create(pool.getDcId(), pool.getPrjId(), data);
		if (result!=null) {
			jedisUtil.set(RedisKey.MEMBER_ADD+result.getId(),"1");
			BaseCloudLdMember resultData= new BaseCloudLdMember();
			resultData.setMemberId(result.getId());
			resultData.setPoolId(result.getPool_id());
			resultData.setPrjId(pool.getPrjId());
			resultData.setDcId(pool.getDcId());
			resultData.setMemberAddress(result.getAddress());
			resultData.setProtocolPort(Long.parseLong(result.getProtocol_port()));
			resultData.setMemberWeight(Long.parseLong(result.getWeight()));
			resultData.setMemberStatus(result.getStatus().toUpperCase());
			resultData.setCreateTime(new Date());
			resultData.setCreateName(sessionUser.getUserName());
			resultData.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));
			resultData.setVmId(member.getVmId());
			resultData.setPriority(256);
			memberDao.saveOrUpdate(resultData);

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
	/**
	 * 添加从节点
	 * @param temp
	 * @param member
	 * @param pool
	 * @param data
	 * @param sessionUser
	 * @throws Exception
	 */
	private void doSomeByBackup(JSONObject temp, CloudLdMember member, CloudLdPool pool, JSONObject data, SessionUserInfo sessionUser) throws Exception{
		log.info("memberId:"+member.getMemberId()+"开始添加从节点");
		temp.put("protocol_port", member.getProtocolPort());
		temp.put("address", member.getMemberAddress());
		temp.put("pool_id", pool.getPoolId());
		temp.put("priority", member.getPriority());
		temp.put("admin_state_up", "1");
		data.put("member", temp);
		final Member result=openStackService.create(pool.getDcId(), pool.getPrjId(), data);
		if (result!=null) {
			jedisUtil.set(RedisKey.MEMBER_ADD+result.getId(),"1");
			BaseCloudLdMember resultData= new BaseCloudLdMember();
			resultData.setMemberId(result.getId());
			resultData.setPoolId(result.getPool_id());
			resultData.setPrjId(pool.getPrjId());
			resultData.setDcId(pool.getDcId());
			resultData.setMemberAddress(result.getAddress());
			resultData.setProtocolPort(Long.parseLong(result.getProtocol_port()));
			resultData.setPriority(Integer.parseInt(result.getPriority()));
			resultData.setMemberStatus(result.getStatus().toUpperCase());
			resultData.setCreateTime(new Date());
			resultData.setCreateName(sessionUser.getUserName());
			resultData.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));
			resultData.setVmId(member.getVmId());
			resultData.setRole(member.getRole());
			memberDao.saveOrUpdate(resultData);
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
	/**
	 * 发送同步消息
	 * @param resultData
	 * @throws Exception
	 */
	private void sendRedisSync(BaseCloudLdMember resultData) throws  Exception{
		JSONObject json =new JSONObject();
		json.put("memberId", resultData.getMemberId());
		json.put("dcId",resultData.getDcId());
		json.put("prjId", resultData.getPrjId());
		json.put("memberStatus", resultData.getMemberStatus());
		json.put("count", "0");
		jedisUtil.push(RedisKey.ldMemberKeyRefresh+resultData.getMemberId(), json.toJSONString());
	}
	@Override
	public void changeMembersStatusFromPoolByDb(String poolId) {
		log.info("开始修改负载均衡"+poolId+"下的成员(流量承担者)");
		memberDao.execSQL("UPDATE cloud_ldmember SET is_undertaker = '0' WHERE pool_id = ? AND role IS NOT NULL",poolId);
		List<BaseCloudLdMember> list=memberDao.find("from BaseCloudLdMember where poolId=? AND memberStatus!='INACTIVE' AND role !=null",poolId);
		List<BaseCloudLdMember>	canUseActiveList=new ArrayList<BaseCloudLdMember>();//状态正常的主节点
		List<BaseCloudLdMember>	canUseBackupList=new ArrayList<BaseCloudLdMember>();//状态正常从节点
		for(BaseCloudLdMember member:list){
			log.info("memberId:"+member.getMemberId()+",role:"+member.getRole()+",status:"+member.getMemberStatus()+",weight"+member.getMemberWeight()+",priority:"+member.getPriority()+",port:"+member.getProtocolPort()+".ip:"+member.getMemberAddress());
			if("Active".equals(member.getRole())){
				canUseActiveList.add(member);
			}else if("Backup".equals(member.getRole())){
				canUseBackupList.add(member);
			}
		}
		log.info("canUseActiveList.size()"+canUseActiveList.size());
		log.info("canUseBackupList.size()"+canUseBackupList.size());
		changeUndertaker(canUseActiveList,canUseBackupList);
	}
	/**
	 * 修改数据库中流量承担者
	 * @param canUseActiveList
	 * @param canUseBackupList
	 */
	private void changeUndertaker(List<BaseCloudLdMember> canUseActiveList,List<BaseCloudLdMember> canUseBackupList){
		if(canUseActiveList.size()>0){//存在状态正常的主节点
			for (BaseCloudLdMember baseCloudLdMember : canUseActiveList){
				baseCloudLdMember.setIsUndertaker(true);
				memberDao.saveOrUpdate(baseCloudLdMember);
			}
		}else{//如果不存在状态正常的主节点
			BaseCloudLdMember undertaker=null;
			List<BaseCloudLdMember> maxBackupsPriority=new ArrayList<BaseCloudLdMember>();//优先级相同的从节点
			if(canUseBackupList!=null&&canUseBackupList.size()>0){//如果存在可用的从节点
				//按照优先级排序,小的在前
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
				int priority=((BaseCloudLdMember)canUseBackupList.get(0)).getPriority();//获取最高的优先级
				for (BaseCloudLdMember baseCloudLdMember : canUseBackupList){
					int thisPriority=baseCloudLdMember.getPriority();
					if(thisPriority==priority){//如果存在相同的优先级,则add到该list中,用于后面判断ip
						maxBackupsPriority.add(baseCloudLdMember);
					}else{
						break;
					}
				}
				List<BaseCloudLdMember> maxBackupsIp=new ArrayList<BaseCloudLdMember>();//获取ip优先级最高的
				if(maxBackupsPriority.size()>1){//如果有多个
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
					String ip=((BaseCloudLdMember)maxBackupsPriority.get(0)).getMemberAddress();//获取优先级最高的ip
					for (BaseCloudLdMember baseCloudLdMember : maxBackupsPriority){
						String thisIp=baseCloudLdMember.getMemberAddress();
						if(thisIp.equals(ip)){
							maxBackupsIp.add(baseCloudLdMember);//将优先级最高的ip 放入list中
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
					memberDao.saveOrUpdate(undertaker);
				}
			}
			
		}
	}
	/**
	 * 添加主节点
	 * @param temp
	 * @param member
	 * @param pool
	 * @param data
	 * @param sessionUser
	 * @throws Exception
	 */
	private void doSomeByActive(JSONObject temp ,CloudLdMember member,CloudLdPool pool,JSONObject data,SessionUserInfo sessionUser) throws Exception{
		log.info("memberId:"+member.getMemberId()+"开始添加主节点");
		temp.put("protocol_port", member.getProtocolPort());
		temp.put("address", member.getMemberAddress());
		temp.put("pool_id", pool.getPoolId());
		temp.put("weight", member.getMemberWeight());
		temp.put("admin_state_up", "1");
		data.put("member", temp);
		final Member result=openStackService.create(pool.getDcId(), pool.getPrjId(), data);
		if (result!=null) {
			jedisUtil.set(RedisKey.MEMBER_ADD+result.getId(),"1");
			BaseCloudLdMember resultData= new BaseCloudLdMember();
			resultData.setMemberId(result.getId());
			resultData.setPoolId(result.getPool_id());
			resultData.setPrjId(pool.getPrjId());
			resultData.setDcId(pool.getDcId());
			resultData.setMemberAddress(result.getAddress());
			resultData.setProtocolPort(Long.parseLong(result.getProtocol_port()));
			resultData.setMemberWeight(Long.parseLong(result.getWeight()));
			resultData.setMemberStatus(result.getStatus().toUpperCase());
			resultData.setCreateTime(new Date());
			resultData.setCreateName(sessionUser.getUserName());
			resultData.setAdminStateup(BoolUtil.bool2char(result.isAdmin_state_up()));
			resultData.setVmId(member.getVmId());
			resultData.setRole(member.getRole());
			resultData.setPriority(256);
//			resultData.setIsUndertaker(true);
			memberDao.saveOrUpdate(resultData);
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
	/**
	 * 修改成员
	 * 
	 * @author zhouhaitao
	 * @param member
	 * @return
	 * @throws AppException
	 */
	public CloudLdMember update(final CloudLdMember member) throws Exception{
		jedisUtil.set(RedisKey.MEMBER_UPDATE+member.getMemberId(),"1");
		CloudLdMember resourceVoe = new CloudLdMember();
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		CloudLdPool cloudLdPool=poolService.getPoolById(member.getPoolId());
		temp.put("pool_id", member.getPoolId());
		if("0".equals(cloudLdPool.getMode())||"Active".equals(member.getRole())){
			temp.put("weight",  member.getMemberWeight());
		}
		if("Backup".equals(member.getRole())){
			temp.put("priority",  member.getPriority());
		}
		data.put("member", temp);
		
		Member result = openStackService.update(member.getDcId(), member.getPrjId(), data, member.getMemberId());
		if (result != null) {
			BaseCloudLdMember cloudResource = memberDao.findOne(member.getMemberId());
			cloudResource.setMemberWeight(Long.parseLong(result.getWeight()));
			cloudResource.setProtocolPort(Long.parseLong(result.getProtocol_port()));
			cloudResource.setPriority(Integer.parseInt(result.getPriority()));
			cloudResource.setMemberStatus(result.getStatus());
			memberDao.saveOrUpdate(cloudResource);
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
//			changeMembersStatusFromPoolByDb(member.getPoolId());
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
	
	/**
	 * 删除成员
	 * 
	 * @author zhouhaitao
	 * @param member
	 */
	public void deleteMember(final CloudLdMember member){
		try {
			jedisUtil.set(RedisKey.MEMBER_DELETE+member.getMemberId(),"1");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		log.info("开始删除成员");
		boolean flag = openStackService.delete(member.getDcId(), member.getPrjId(), member.getMemberId());
		if(flag){
			memberDao.delete(member.getMemberId());
//			changeMembersStatusFromPoolByDb(member.getPoolId());
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
	 * 同步修改成员信息
	 * 
	 * @author zhouhaitao
	 * @param cloudLdm
	 * @return
	 */
	public boolean updateLdMember(CloudLdMember cloudLdm){
		boolean flag = false ;
		try{
			BaseCloudLdMember ldm = memberDao.findOne(cloudLdm.getMemberId());
			ldm.setMemberStatus(cloudLdm.getMemberStatus());
			memberDao.saveOrUpdate(ldm);
			flag = true ;
		}catch(Exception e){
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 校验成员是否存在
	 * 
	 * @author zhouhaitao
	 * @param cloudLdm
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean checkMemberExsit(CloudLdMember cloudLdm){
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
		if(!StringUtils.isEmpty(cloudLdm.getMemberId())){
			hql.append(" and memberId <> ?");
			objs = new Object[]{
					cloudLdm.getPoolId(),
					cloudLdm.getMemberAddress(),
					cloudLdm.getProtocolPort(),
					cloudLdm.getVmId(),
					cloudLdm.getMemberId()
				};
		}
		
		List<BaseCloudLdMember> list = memberDao.find(hql.toString(), objs);
		return null==list||list.size()==0;
	}
	
	/**
	 * 查询子网下的主机信息
	 * 
	 * @author zhouhaitao
	 * @param cloudLdm
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public List<CloudLdMember> getMemeberListBySubnet(CloudLdMember cloudLdm) throws Exception{
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
		
		Query query = memberDao.createSQLNativeQuery(sql.toString(), new Object[]{cloudLdm.getSubnetId()});
		
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
	 * 查询当前资源池的总数
	 * ---------------------
	 * @author zhouhaitao
	 * @param poolId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private int countMemberByPool(String poolId){
		int num = 0;
		StringBuffer sql = new StringBuffer();
		sql.append(" select count(1) from cloud_ldmember where pool_id = ? ");
		Query query = memberDao.createSQLNativeQuery(sql.toString(), new Object[]{poolId});
		
		List listResult =query.getResultList();
		if(null!=listResult&&listResult.size()==1){
			BigInteger bi = (BigInteger)listResult.get(0);
			num = bi.intValue();
		}
		
		return num;
	}
	
	/**
	 * 删除主机时 级联删除成员信息
	 * 
	 * @author zhouhaitao
	 * @param vmId
	 */
	public void deleteMemberByVm(String vmId){
		StringBuffer hql = new StringBuffer("from BaseCloudLdMember where vmId = ? ");
		
		List<BaseCloudLdMember> memList = memberDao.find(hql.toString(), new Object[]{vmId});
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

	@Override
	public void changeMembersStatusFromPoolByOpenstack(String poolId) throws Exception {
		memberDao.execSQL("UPDATE cloud_ldmember SET is_undertaker = '0' WHERE pool_id = ?",poolId);
		List<BaseCloudLdMember> list=memberDao.find("from BaseCloudLdMember where poolId=? ",poolId);
		List<BaseCloudLdMember>	canUseActiveList=new ArrayList<BaseCloudLdMember>();//状态正常的主节点
		List<BaseCloudLdMember>	canUseBackupList=new ArrayList<BaseCloudLdMember>();//状态正常从节点
		for(BaseCloudLdMember member:list){
			JSONObject json=openstackMemberService.get(member.getDcId(),member.getMemberId());//从底层获取member状态信息
			if("INACTIVE".equals(json.getString("status"))){//状态为不活跃
				//TODO 插入一条异常信息
			}
			if("Active".equals(member.getRole())){
				canUseActiveList.add(member);
			}else if("Backup".equals(member.getRole())){
				canUseBackupList.add(member);
			}
			member.setMemberStatus(json.getString("status"));
			memberDao.saveOrUpdate(member);
		}
		changeUndertaker(canUseActiveList,canUseBackupList);
	}


	@Override
	public void changeMembersStatus(String poolId) throws Exception {
		memberDao.execSQL("UPDATE cloud_ldmember SET member_status = 'ACTIVE' WHERE pool_id = ?",poolId);
		changeMembersStatusFromPoolByDb(poolId);
	}


	@Override
	public void updateMember(BaseCloudLdMember baseCloudLdMember)
			throws Exception {
		if("INACTIVE".equals(baseCloudLdMember.getMemberStatus())){
			baseCloudLdMember.setIsUndertaker(false);
		}
		memberDao.execSQL("UPDATE cloud_ldmember SET member_status = ? , is_undertaker=? WHERE member_id = ? ", baseCloudLdMember.getMemberStatus(),baseCloudLdMember.getIsUndertaker(),baseCloudLdMember.getMemberId());
//		memberDao.saveOrUpdate(baseCloudLdMember);
//		changeMembersStatusFromPoolByDb(baseCloudLdMember.getPoolId());
	}
	
	public List<CloudLdMember> getMemberListByPool(String poolId) throws Exception{
		List<BaseCloudLdMember> list=memberDao.find("from BaseCloudLdMember where poolId=?", poolId);
		List<CloudLdMember> memberList=new ArrayList<CloudLdMember>();
		for (BaseCloudLdMember baseCloudLdMember : list) {
			CloudLdMember cloudLdMember=new CloudLdMember();
			BeanUtils.copyPropertiesByModel(cloudLdMember, baseCloudLdMember);
			memberList.add(cloudLdMember);
		}
		return memberList;
	}


	@Override
	public JSONObject get(JSONObject valueJson) throws Exception{
		JSONObject result = null ;
		if(null!=valueJson){
			JSONObject json = openstackMemberService.get(valueJson.getString("dcId"), 
					valueJson.getString("memberId"));
			if(null!=json){
				String jsonStr = json.toJSONString();
				boolean isDeleted=jsonStr.contains("NotFound");
				if(!isDeleted){
					result=json.getJSONObject(OpenstackUriConstant.MEMBER_DATA_NAME);
				}
				else{
					result =new JSONObject();
					result.put("deletingStatus", isDeleted+"");
				}
			}
		}
		return result;
		
	}


	/**
	 * 修改负载均衡成员信息
	 * @param cloudLdm
	 * @return
	 */
	@Override
	public CloudLdMember  updateMember(CloudLdMember cloudLdm){
		boolean flag = false ;
		CloudLdMember member=new CloudLdMember();
		try{
			BaseCloudLdMember ldm = memberDao.findOne(cloudLdm.getMemberId());
			if(ldm!=null&&ldm.getMemberId()!=null){
				ldm.setMemberStatus(cloudLdm.getMemberStatus());
				memberDao.saveOrUpdate(ldm);
			}
//			if("1".equals(cloudLdPool.getMode())){
//				changeMembersStatusFromPoolByDb(ldm.getPoolId());
//			}
			flag = true ;
			BeanUtils.copyPropertiesByModel(member, ldm);
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return member;
	}
}
