package com.eayun.virtualization.thread;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.MongoCollectionName;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.eayunstack.service.OpenstackMemberService;
import com.eayun.monitor.bean.LdPoolMonitorDetail;
import com.eayun.monitor.model.BaseCloudLdpoolExp;
import com.eayun.monitor.service.LdPoolAlarmMonitorService;
import com.eayun.virtualization.model.BaseCloudLdMember;
import com.eayun.virtualization.model.CloudLdMember;
import com.eayun.virtualization.model.CloudLdMonitor;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.service.HealthMonitorService;
import com.eayun.virtualization.service.MemberService;

public class MemberHealthMonitorThread  implements Runnable{
	private final Logger log = LoggerFactory
			.getLogger(MemberHealthMonitorThread.class);
	private CloudLdPool cloudLdPool;
	private MemberService memberService;
	private HealthMonitorService healthMonitorService;
	private OpenstackMemberService openstackMemberService;
	private JedisUtil jedisUtil;
	private LdPoolAlarmMonitorService ldPoolAlarmMonitorService;
	private MongoTemplate mongoTemplate;
	private Date jobStartTime;
	private  int membersCount=0;
	private  int backupCount=0;
	private  int activeCount=0;
	private  int inactiveCount=0;
	private  int inactiveBackupCount=0;
	private  int inactiveActiveCount=0;
	public MemberHealthMonitorThread(CloudLdPool cloudLdPool,MemberService memberService,HealthMonitorService healthMonitorService,OpenstackMemberService openstackMemberService,JedisUtil jedisUtil,LdPoolAlarmMonitorService ldPoolAlarmMonitorService,MongoTemplate mongoTemplate,Date jobStartTime) {
		this.cloudLdPool=cloudLdPool;
		this.memberService = memberService;
		this.healthMonitorService=healthMonitorService;
		this.openstackMemberService=openstackMemberService;
		this.jedisUtil  = jedisUtil;
		this.ldPoolAlarmMonitorService=ldPoolAlarmMonitorService;
		this.mongoTemplate=mongoTemplate;
		this.jobStartTime=jobStartTime;
	}
	@Override
	public void run() {
		try {
			log.info("开始进行poolId:"+cloudLdPool.getPoolId()+"的成员状态计划任务,当前时间:"+DateUtil.dateToString(new Date()));
			String poolStatus=jedisUtil.get(RedisKey.DELETE_POOL+cloudLdPool.getPoolId());
			if(!StringUtil.isEmpty(poolStatus)){
				log.info("负载均衡:"+cloudLdPool.getPoolId()+"正在被删除,删除状态为:"+poolStatus);
				return;
			}
			//获取该负载均衡下所有的成员
			List<CloudLdMember> cloudLdMemberList = memberService.getMemberListByPool(cloudLdPool.getPoolId());
			Set<String> deleteSuccess=jedisUtil.getSet(RedisKey.MEMBER_DELETE_POOLID+cloudLdPool.getPoolId());//人为删除操作(已删除成功)
			//如果该负载均衡下的成员已删除成功
			if(deleteSuccess!=null&&deleteSuccess.size()>0){
				//为主备模式的负载均衡,则重新选是否为流量承担者
				if("1".equals(cloudLdPool.getMode())){
					memberService.changeMembersStatusFromPoolByDb(cloudLdPool.getPoolId());
				}
				//清理redis中删除的成员
				for (String memId : deleteSuccess) {
					jedisUtil.delete(RedisKey.MEMBER_DELETE+memId);
					jedisUtil.removeFromSet(RedisKey.MEMBER_DELETE_POOLID+cloudLdPool.getPoolId(),memId);
				}
			}
			log.info("数据库中该负载均衡下的成员数量:"+(cloudLdMemberList!=null&&cloudLdMemberList.size()>0?cloudLdMemberList.size():0));
			//获取该负载均衡关联的健康检查
			CloudLdMonitor cloudLdMonitor=healthMonitorService.getHealthMonitorByPool(cloudLdPool);
			//遍历每一个成员
			for (CloudLdMember cloudLdMember : cloudLdMemberList) {
				membersCount++;
				changeMemberStatus(cloudLdMember,cloudLdMonitor);
				String add=jedisUtil.get(RedisKey.MEMBER_ADD+cloudLdMember.getMemberId());//人为添加操作
				String delete=jedisUtil.get(RedisKey.MEMBER_DELETE+cloudLdMember.getMemberId());//人为删除操作
				String update=jedisUtil.get(RedisKey.MEMBER_UPDATE+cloudLdMember.getMemberId());//人为更新操作
				String unbind=jedisUtil.get(RedisKey.MEMBER_UNBIND_POOLiD+cloudLdPool.getPoolId());//人为解绑操作
				String sync=jedisUtil.get(RedisKey.MEMBER_SYNC+cloudLdMember.getMemberId());//24h同步添加/删除操作
				if("1".equals(add)||"1".equals(delete)||"1".equals(update)||"1".equals(unbind)||"1".equals(sync)){//如果人为(添加成员,删除成员,更新成员,解绑健康检查)操作||24h同步刚开始还未结束
					//添加异常记录并更新监控信息中需要的值
					setMonitorNum(cloudLdMonitor, cloudLdMember);
					continue;
				}else if("2".equals(add)||"2".equals(delete)||"2".equals(update)||"2".equals(unbind)||"2".equals(sync)){//如果人为(添加成员,删除成员,更新成员,解绑健康检查)操作||24h已成功结束
					if("2".equals(unbind)){//如果是解绑,则将所有成员状态改为正常,并修改流量承担者
						memberService.changeMembersStatus(cloudLdPool.getPoolId());
						cloudLdMember.setMemberStatus("ACTIVE");
					}else{//其余情况只重新选择流量承担者
						memberService.changeMembersStatusFromPoolByDb(cloudLdPool.getPoolId());
					}
					//添加异常记录并更新监控信息中需要的值
					setMonitorNum(cloudLdMonitor, cloudLdMember);
					//清理redis中没用的key
					jedisUtil.delete(RedisKey.MEMBER_ADD+cloudLdMember.getMemberId());
					jedisUtil.delete(RedisKey.MEMBER_DELETE+cloudLdMember.getMemberId());
					jedisUtil.delete(RedisKey.MEMBER_UPDATE+cloudLdMember.getMemberId());
					jedisUtil.delete(RedisKey.MEMBER_UNBIND_POOLiD+cloudLdPool.getPoolId());
					jedisUtil.delete(RedisKey.MEMBER_SYNC+cloudLdMember.getMemberId());
					continue;
				}
				//如果数据库库中状态为创建中||更新中,则该成员状态不做任何改变,等待下一分钟再改变
				if("PENDING_CREATE".equals(cloudLdMember.getMemberStatus())||"PENDING_UPDATE".equals(cloudLdMember.getMemberStatus())){
					continue;
				}
				//如果未绑定健康检查,则将每一个成员状态设置为正常,为了给彬彬的异常接口时使用,如果不手动设置状态,会导致由INACTIVE变成ACTIVE时,仍会给彬彬发送异常信息
				if(cloudLdMonitor==null||StringUtil.isEmpty(cloudLdMonitor.getLdmId())){
					cloudLdMember.setMemberStatus("ACTIVE");
				}
				//如果成员数量>0并且关联了健康检查
				if(cloudLdMonitor!=null&&!StringUtil.isEmpty(cloudLdMonitor.getLdmId())){
				try {
						log.info("底层开始获取成员信息时间:"+DateUtil.dateToString(new Date()));
						JSONObject jsonMember = openstackMemberService.get(cloudLdMember.getDcId(),cloudLdMember.getMemberId());
						log.info(JSONObject.toJSONString(jsonMember));
						log.info("底层结束获取成员信息时间:"+DateUtil.dateToString(new Date()));
						JSONObject jsonMem=JSONObject.parseObject(jsonMember.getString("member"));
						//只有在底层状态和数据库状态不一致时,才会去改变数据库状态和流量承担者
						if(jsonMem!=null&&!cloudLdMember.getMemberStatus().equals(jsonMem.getString("status"))){
							log.info("成员id:"+cloudLdMember.getMemberId()+",端口号:"+cloudLdMember.getProtocolPort()+",IP:"+cloudLdMember.getMemberAddress()+",数据库状态现在为:"+cloudLdMember.getMemberStatus()+",底层状态为:"+jsonMem.getString("status"));
							BaseCloudLdMember baseCloudLdMember=new BaseCloudLdMember();
							BeanUtils.copyPropertiesByModel(baseCloudLdMember, cloudLdMember);
							cloudLdMember.setMemberStatus(jsonMem.getString("status"));
							baseCloudLdMember.setMemberStatus(jsonMem.getString("status"));
							memberService.updateMember(baseCloudLdMember);
							memberService.changeMembersStatusFromPoolByDb(cloudLdPool.getPoolId());
						}
				} catch (Exception e) {
					log.error(e.getMessage(),e);
				}
				log.info("结束改变成员状态信息时间:"+DateUtil.dateToString(new Date()));
			}
			setMonitorNum(cloudLdMonitor, cloudLdMember);
		}
		//插入一条监控信息
		insertMonitor();
		log.info("结束poolId:"+cloudLdPool.getPoolId()+"的成员状态计划任务,当前时间:"+DateUtil.dateToString(new Date()));
	} catch (Exception e) {
		log.error(e.getMessage(),e);
	}
}
	private void setMonitorNum(CloudLdMonitor cloudLdMonitor,
			CloudLdMember cloudLdMember) {
		if("1".equals(cloudLdPool.getMode())){
			if("Backup".equals(cloudLdMember.getRole())){
				backupCount++;
			}else if("Active".equals(cloudLdMember.getRole())){
				activeCount++;
			}
		}
		if("INACTIVE".equals(cloudLdMember.getMemberStatus())){
			inactiveCount++;
				insertMemberExp(cloudLdMonitor,
						cloudLdMember,true);
			if("Backup".equals(cloudLdMember.getRole())){
				inactiveBackupCount++;
			}else if("Active".equals(cloudLdMember.getRole())){
				inactiveActiveCount++;
			}
		}else if("ACTIVE".equals(cloudLdMember.getMemberStatus())){
				insertMemberExp(cloudLdMonitor,
						cloudLdMember,false);
		}
	}
	/**
	 * 原5s一次的状态刷新
	 * @param cloudLdMember
	 */
	private void changeMemberStatus(CloudLdMember cloudLdMember,CloudLdMonitor cloudLdMonitor) {
		String value = null ;
		boolean isSync = false;
		try{
			value = jedisUtil.pop(RedisKey.ldMemberKeyRefresh+cloudLdMember.getMemberId());
			JSONObject valueJson = JSONObject.parseObject(value);
			CloudLdMember cloudLdm = JSON.parseObject(value, CloudLdMember.class);
			if(null!=value){
				log.info("从负载均衡成员队列中取出："+value);
				JSONObject json = memberService.get(valueJson);
				log.info("底层返回JSON:"+json);
				String stackStatus = json.getString("status");
				if (!StringUtils.isEmpty(stackStatus)) {
					stackStatus = stackStatus.toUpperCase();
					if(!stackStatus.equals(cloudLdm.getMemberStatus())){
						cloudLdm.setMemberStatus(stackStatus);
						cloudLdMember.setMemberStatus(stackStatus);//将更新的状态赋给cloudLdMember
						isSync = true;
						CloudLdMember member=memberService.updateMember(cloudLdm);
						if("INACTIVE".equals(stackStatus)&&cloudLdMonitor!=null){
							insertMemberExp(cloudLdMonitor, member, true);
						}
					}
				}
				
				if(isSync){
					log.info("负载均衡成员ID："+cloudLdm.getMemberId()+"状态刷新成功，移除任务调度！");
				}
				else {
					int count = cloudLdm.getCount();
					if(count>100){
						log.info("负载均衡成员ID："+cloudLdm.getMemberId()+"已执行"+count+"次状态未刷新，移除任务调度！");
					}else{
						valueJson.put("count", count+1);
						log.info("负载均衡成员ID："+cloudLdm.getMemberId()+"状态未刷新，等待下次调度！");
						jedisUtil.push(RedisKey.ldMemberKeyRefresh+cloudLdMember.getMemberId(), valueJson.toJSONString());
					}
				}
			}
			
		}
		catch(Exception e){
		    log.error(e.getMessage(),e);
			if(null!= value ){
				try {
					jedisUtil.push(RedisKey.ldMemberKeyRefresh+cloudLdMember.getMemberId(), value);
					JSONObject valueJson = JSONObject.parseObject(value);
					cloudLdMember.setMemberStatus(valueJson.getString("status"));
				} catch (Exception e1) {
					log.error(e1.getMessage(),e1);
				}
			}
		}
	}
	/**
	 * 插入一条监控信息
	 * @throws Exception
	 */
	private void insertMonitor() throws Exception{
		log.info("开始插入mongo/redis监控信息");
		LdPoolMonitorDetail mongoDetail=new LdPoolMonitorDetail();
		Date now =new Date();
		mongoDetail.setRealTime(now);
		mongoDetail.setTimestamp(DateUtil.dateRemoveSec(jobStartTime));
		mongoDetail.setLdPoolId(cloudLdPool.getPoolId());
		mongoDetail.setMode(cloudLdPool.getMode());
		mongoDetail.setProjectId(cloudLdPool.getPrjId());
		mongoDetail.setMember(membersCount);
		mongoDetail.setMasterMember(activeCount);
		mongoDetail.setSlaveMember(backupCount);
		mongoDetail.setExpMember(inactiveCount);
		mongoDetail.setExpMaster(inactiveActiveCount);
		mongoDetail.setExpSalve(inactiveBackupCount);
		mongoDetail.setExpMemberRatio((membersCount==0?0:(double)inactiveCount/membersCount)*100);
		mongoDetail.setExpMasterRatio((activeCount==0?0:(double)inactiveActiveCount/activeCount)*100);
		mongoDetail.setExpSalveRatio((backupCount==0?0:(double)inactiveBackupCount/backupCount)*100);
		mongoTemplate.insert(mongoDetail, MongoCollectionName.MONITOR_LD_POOL_DETAIL);
		log.info("mongo"+JSONObject.toJSONString(mongoDetail));
		
		LdPoolMonitorDetail redisDetail=new LdPoolMonitorDetail();
		redisDetail.setMode(cloudLdPool.getMode());
		redisDetail.setMember(membersCount);
		redisDetail.setMasterMember(activeCount);
		redisDetail.setSlaveMember(backupCount);
		redisDetail.setExpMember(inactiveCount);
		redisDetail.setExpMaster(inactiveActiveCount);
		redisDetail.setExpSalve(inactiveBackupCount);
		redisDetail.setExpMemberRatio((membersCount==0?0:(double)inactiveCount/membersCount)*100);
		redisDetail.setExpMasterRatio((activeCount==0?0:(double)inactiveActiveCount/activeCount)*100);
		redisDetail.setExpSalveRatio((backupCount==0?0:(double)inactiveBackupCount/backupCount)*100);
		String oldValue=jedisUtil.get(RedisKey.MONITOR_EXP_LDPOOL+cloudLdPool.getPoolId());
		if(!StringUtil.isEmpty(oldValue)){
			jedisUtil.set(RedisKey.MONITOR_EXP_LDPOOL_LAST+cloudLdPool.getPoolId(), oldValue);
		}
		jedisUtil.set(RedisKey.MONITOR_EXP_LDPOOL+cloudLdPool.getPoolId(), JSONObject.toJSONString(redisDetail));
		log.info("redis:"+JSONObject.toJSONString(redisDetail));
		
	}
	/**
	 * 每一个成员的异常信息
	 * @param cloudLdMonitor
	 * @param cloudLdMember
	 * @param baseCloudLdMember
	 * @param isExp
	 */
	private void insertMemberExp(CloudLdMonitor cloudLdMonitor,
			CloudLdMember cloudLdMember, boolean isExp) {
		log.info("开始插入一条异常信息,memberId"+cloudLdMember.getMemberId()+",memberStatus:"+cloudLdMember.getMemberStatus()+",isExp:"+isExp);
		BaseCloudLdpoolExp baseCloudLdpoolExp=new BaseCloudLdpoolExp();
		Date now=new Date();
		baseCloudLdpoolExp.setRealTime(now);
		baseCloudLdpoolExp.setTimestamp(DateUtil.dateRemoveSec(jobStartTime));
		baseCloudLdpoolExp.setHealthId(cloudLdMonitor.getLdmId());
		baseCloudLdpoolExp.setMemberId(cloudLdMember.getMemberId());
		if(isExp){
			baseCloudLdpoolExp.setRole(cloudLdMember.getRole());
			baseCloudLdpoolExp.setExpTime(now);//异常时间,因没有接口,暂时写成当前时间
			String details="";
			if("TCP".equals(cloudLdMonitor.getLdmType())){
				details="每间隔"+cloudLdMonitor.getLdmDelay()+"秒检测一次TCP端口，重试"+cloudLdMonitor.getMaxRetries()+"次且超时"+cloudLdMonitor.getLdmTimeout()+"秒仍无响应。";
			}else if("PING".equals(cloudLdMonitor.getLdmType())){
				details="每间隔"+cloudLdMonitor.getLdmDelay()+"秒PING该成员一次，重试"+cloudLdMonitor.getMaxRetries()+"次且超时"+cloudLdMonitor.getLdmTimeout()+"秒仍无响应。";
			}else if("HTTP".equals(cloudLdMonitor.getLdmType())){
				details="每间隔"+cloudLdMonitor.getLdmDelay()+"秒检测一次HTTP URL是否可以访问，重试"+cloudLdMonitor.getMaxRetries()+"次且超时"+cloudLdMonitor.getLdmTimeout()+"秒仍无响应。";
			}
			baseCloudLdpoolExp.setExpDetails(details);
			baseCloudLdpoolExp.setIsRepair("1");
			baseCloudLdpoolExp.setTimestamp(new Date());
			baseCloudLdpoolExp.setPoolId(cloudLdMember.getPoolId());
			baseCloudLdpoolExp.setProjectId(cloudLdMember.getPrjId());
			baseCloudLdpoolExp.setMode(cloudLdPool.getMode());
			baseCloudLdpoolExp.setPort(cloudLdMember.getProtocolPort());
			baseCloudLdpoolExp.setVmId(cloudLdMember.getVmId());
			baseCloudLdpoolExp.setVmIp(cloudLdMember.getMemberAddress());
		}
		ldPoolAlarmMonitorService.addCloudLdpoolExp(baseCloudLdpoolExp, isExp);
	}
}
