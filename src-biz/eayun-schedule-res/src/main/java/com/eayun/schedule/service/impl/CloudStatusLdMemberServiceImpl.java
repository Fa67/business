package com.eayun.schedule.service.impl;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.service.MemberService;
import com.eayun.virtualization.service.PoolService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceSyncConstant;
import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.common.util.TransactionHookUtil;
import com.eayun.common.util.TransactionHookUtil.CompletionHook;
import com.eayun.common.util.TransactionHookUtil.Hook;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.service.OpenstackMemberService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.CloudLdMemberService;
import com.eayun.virtualization.dao.CloudLdMemberDao;
import com.eayun.virtualization.model.BaseCloudLdMember;
import com.eayun.virtualization.model.CloudLdMember;

@Transactional
@Service
public class CloudStatusLdMemberServiceImpl implements CloudLdMemberService{
    private static final Logger log = LoggerFactory.getLogger(CloudStatusLdMemberServiceImpl.class);
    @Autowired
    private JedisUtil jedisUtil; 
	@Autowired
	private OpenstackMemberService openStackMemberService;
	@Autowired
	private CloudLdMemberDao memberDao ;
	@Autowired
    private EcmcLogService ecmcLogService;
	@Autowired
	private MemberService memberService;
	@Autowired
	private PoolService poolService;
	@Autowired
    private SyncProgressUtil syncProgressUtil;
	@Override
	public String pop(String groupKey) {
		String value = null;
        try {
            value = jedisUtil.pop(groupKey);
        } catch (Exception e){
            log.error(e.getMessage(),e);
            return null;
        }
        return value;
	}

	/**
	 * 重新放入队列
	 * @param groupKey
	 * @param value
	 * @return
	 */
	@Override
	public	boolean push(String groupKey,String value){
		boolean flag = false;
		try {
			flag=  jedisUtil.push(groupKey, value);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag;
	}
	
	/**
	 * 查询当前队列的长度
	 * @param groupKey
	 * @return
	 */
	public long size (String groupKey){
		return jedisUtil.sizeOfList(groupKey);
	}
	
	/**
	 * 获取底层指定ID的资源，底层异常为null
	 * ------------------
	 * @author zhouhaitao
	 * @param valueJson
	 * 
	 */
	@Override
	public JSONObject get(JSONObject valueJson) throws Exception{
		JSONObject result = null ;
		if(null!=valueJson){
			JSONObject json = openStackMemberService.get(valueJson.getString("dcId"), 
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
	public boolean  updateMember(CloudLdMember cloudLdm){
		boolean flag = false ;
		try{
			BaseCloudLdMember ldm = memberDao.findOne(cloudLdm.getMemberId());
			ldm.setMemberStatus(cloudLdm.getMemberStatus());
			memberDao.saveOrUpdate(ldm);
			CloudLdPool cloudLdPool=poolService.getLoadBalanceById(ldm.getPoolId());
			if("1".equals(cloudLdPool.getMode())){
				memberService.changeMembersStatusFromPoolByDb(ldm.getPoolId());
			}
			flag = true ;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag;
	}

	@Override
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {
		Map<String,CloudLdMember> dbMap=new HashMap<String,CloudLdMember>();             
		Map<String,BaseCloudLdMember> stackMap=new HashMap<String,BaseCloudLdMember>();      
		List<CloudLdMember> dbList=queryCloudLdmemberListByDcId(dataCenter.getId());
		List<BaseCloudLdMember> list=openStackMemberService.getStackList(dataCenter);
		                                                                         
		if(null!=dbList){                                                        
			for(BaseCloudLdMember bcldm:dbList){     
				CloudLdMember cldm=new CloudLdMember();
				BeanUtils.copyPropertiesByModel(cldm, bcldm);
				dbMap.put(cldm.getMemberId(), cldm);                                      
			}                                                                      
		}                                                                        
		long total = list == null ? 0L : list.size();
        syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.LB_MEMBER, total);                                                                         
		if(null!=list){                                                          
			for(final BaseCloudLdMember cldm:list){  
				//底层数据存在本地数据库中 更新本地数据                              
				if(dbMap.containsKey(cldm.getMemberId())){                       
//					updateCloudLdmemberFromStack(cldm);            
				}                                                                    
				else{                                                                
					jedisUtil.set(RedisKey.MEMBER_SYNC+cldm.getMemberId(),"1");
					memberDao.save(cldm);           
					TransactionHookUtil.registAfterCompletionHook(new CompletionHook() {

						@Override
						public void execute(int status) {
							try {
								if(0==status){
									jedisUtil.set(RedisKey.MEMBER_SYNC+cldm.getMemberId(),"2");
								}else{
									jedisUtil.delete(RedisKey.MEMBER_SYNC+cldm.getMemberId());
								}
							} catch (Exception e) {
								log.error(e.getMessage(),e);
							}
							
						}
					});
				}                                                                    
				stackMap.put(cldm.getMemberId(), cldm);  
				syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.LB_MEMBER);
			}                                                                      
		}                                                                        
		                                                                         
		if(null!=dbList){                                                        
			for(final CloudLdMember cldm:dbList){    
				BaseCloudLdMember mem = stackMap.get(cldm.getMemberId());
				Character adminStateUp=null;
				if(mem!=null){
					adminStateUp=mem.getAdminStateup();
				}
				//删除本地数据库中不存在于底层的数据                                 
				if(mem==null||!stackMap.containsKey(cldm.getMemberId())||'0'==adminStateUp){ 
					jedisUtil.set(RedisKey.MEMBER_SYNC+cldm.getMemberId(),"1");
					memberDao.delete(cldm.getMemberId());
					ecmcLogService.addLog("同步资源清除数据", toType(cldm), cldm.getVmName(), cldm.getPrjId(), 1, cldm.getMemberId(), null);
					
					JSONObject json = new JSONObject();
					json.put("resourceType", ResourceSyncConstant.LDMEMBER);
					json.put("resourceId", cldm.getMemberId());
					json.put("resourceName", StringUtil.isEmpty(cldm.getVmName())?ResourceSyncConstant.LDMEMBER:cldm.getVmName());
					json.put("synTime", new Date());
					jedisUtil.push(RedisKey.DATACENTER_SYNC_DELETED_RESOURCE, json.toJSONString());
					TransactionHookUtil.registAfterCompletionHook(new CompletionHook() {

						@Override
						public void execute(int status) {
							try {
								if(0==status){
									jedisUtil.set(RedisKey.MEMBER_SYNC+cldm.getMemberId(),"2");
								}else{
									jedisUtil.delete(RedisKey.MEMBER_SYNC+cldm.getMemberId());
								}
							} catch (Exception e) {
								log.error(e.getMessage(),e);
							}
							
						}
					});
				}                                                                    
			}                                                                      
		}  
	}

	@SuppressWarnings("unchecked")
	public List<CloudLdMember> queryCloudLdmemberListByDcId (String dcId){
//		StringBuffer hql = new StringBuffer();
//		hql.append(" from BaseCloudLdMember  ");
//		hql.append(" where dcId = ? ");
		List<CloudLdMember> list=new ArrayList<CloudLdMember>();
		StringBuffer sql=new StringBuffer();
		sql.append("select ");
		sql.append(" m.member_id,	");
		sql.append(" m.pool_id,");
		sql.append(" m.prj_id,");
		sql.append(" m.dc_id,");
		sql.append(" m.create_name,");
		sql.append(" m.member_address,");
		sql.append(" m.protocol_port,");
		sql.append(" m.member_weight,");
		sql.append(" m.member_status,");
		sql.append(" m.admin_stateup,");
		sql.append(" m.create_time,");
		sql.append(" m.reserve1,");
		sql.append(" m.reserve2,");
		sql.append(" m.reserve3,");
		sql.append(" m.vm_id,");
		sql.append(" m.role,");
		sql.append(" m.is_undertaker,");
		sql.append(" m.priority,");
		sql.append(" vm.vm_name	");
		sql.append(" from cloud_ldmember m");
		sql.append(" left join cloud_vm vm	");
		sql.append(" on vm.vm_id=m.vm_id	");
		sql.append(" where m.dc_id = ?");
		Query query=memberDao.createSQLNativeQuery(sql.toString(), dcId);
		List listResult =query.getResultList();
		for (int i = 0; i < listResult.size(); i++) {
			int  index = 0;
			Object [] obj = (Object [])listResult.get(i);
			CloudLdMember cloudLdmember = new CloudLdMember();
			cloudLdmember.setMemberId((String)obj[index++]);
			cloudLdmember.setPoolId((String)obj[index++]);
			cloudLdmember.setPrjId((String)obj[index++]);
			cloudLdmember.setDcId((String)obj[index++]);
			cloudLdmember.setCreateName((String)obj[index++]);
			cloudLdmember.setMemberAddress((String)obj[index++]);
			cloudLdmember.setProtocolPort(Long.parseLong(obj[index++] != null ? String.valueOf(obj[index-1]) : "0"));
			cloudLdmember.setMemberWeight(Long.parseLong(obj[index++] != null ? String.valueOf(obj[index-1]) : "0"));
			cloudLdmember.setMemberStatus((String)obj[index++]);
			cloudLdmember.setAdminStateup((Character)obj[index++]);
			cloudLdmember.setCreateTime((Date)obj[index++]);
			cloudLdmember.setReserve1((String)obj[index++]);
			cloudLdmember.setReserve2((String)obj[index++]);
			cloudLdmember.setReserve3((String)obj[index++]);
			cloudLdmember.setVmId((String)obj[index++]);
			cloudLdmember.setVmName((String)obj[index++]);
			list.add(cloudLdmember);
		}
		
		return list;
//		return memberDao.find(hql.toString(), new Object[]{dcId});
	}
	
	public boolean updateCloudLdmemberFromStack(BaseCloudLdMember cldm) throws  Exception{
		boolean flag = false ;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_ldmember set ");
			sql.append("	pool_id = ?,        ");
			sql.append("	prj_id = ?,         ");
			sql.append("	dc_id = ?,          ");
			sql.append("	member_address = ?, ");
//			sql.append("	subnet_id = ?, ");
			sql.append("	protocol_port = ?,  ");
			sql.append("	member_weight = ?,  ");
			sql.append("	member_status = ?,  ");
			sql.append("	admin_stateup = ?   ");
			sql.append(" where member_id = ? ");
			
			memberDao.execSQL(sql.toString(), new Object[]{
					cldm.getPoolId(),
					cldm.getPrjId(),
					cldm.getDcId(),
					cldm.getMemberAddress(),
//					cldm.getSubnetId(),
					cldm.getProtocolPort(),
					cldm.getMemberWeight(),
					cldm.getMemberStatus(),
					cldm.getAdminStateup()+"",
					cldm.getMemberId()
			});
			CloudLdPool cloudLdPool=poolService.getLoadBalanceById(cldm.getPoolId());
//			if("1".equals(cloudLdPool.getMode())){
//				memberService.changeMembersStatusFromPoolByDb(cldm.getPoolId());
//			}
			flag = true ;
		}catch(Exception e){
			flag = false;
			log.error(e.getMessage(),e);
			throw e;
		}
		
		return flag ;
	}
	
	/**
	 * 删除底层不存在的负载均衡成员
	 * @param cloudMember
	 * @return
	 */
	public boolean deleteMember (CloudLdMember cloudMember){
		boolean flag = false;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" delete from cloud_ldmember ");
			sql.append(" where member_id = ? ");
			
			memberDao.execSQL(sql.toString(), new Object[]{
					cloudMember.getMemberId()
			});
			flag = true;
		}catch(Exception e){
		    log.error(e.getMessage(),e);
			flag = false;
		}
		return flag;
	}
	/**
	 * 拼装同步删除发送日志的资源类型
	 * @author gaoxiang
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
}
