package com.eayun.schedule.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Query;

import org.apache.commons.net.util.SubnetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.SyncProgress.SyncByDatacenterTypes;
import com.eayun.common.constant.SyncProgress.SyncType;
import com.eayun.common.exception.AppException;
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.service.OpenstackNetworkService;
import com.eayun.schedule.service.CloudOutIpService;
import com.eayun.virtualization.dao.CloudOutIpDao;
import com.eayun.virtualization.model.CloudOutIp;
import com.eayun.virtualization.model.CloudSubNetWork;

@Transactional
@Service
public class CloudOutIpServiceImpl implements CloudOutIpService {
	
	@Autowired
	private OpenstackNetworkService openstackService;
	@Autowired
	private CloudOutIpDao cloudOutIpDao;
	@Autowired
    private SyncProgressUtil syncProgressUtil;
	/**
	 * 同步底层数据中心下的OutIp
	 * -----------------
	 * @param dataCenter
	 */
	@Override
	public void synchData(BaseDcDataCenter dataCenter) throws Exception {
		deleteOutIpByDcId(dataCenter.getId());
		
		List<CloudSubNetWork> subList = queryOutSubnet(dataCenter.getId());
		String netId = null;
		//初始总数设为0
		syncProgressUtil.initResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.OUT_IP, 0L);
		if(null!=subList){
			for(CloudSubNetWork subnet:subList){
				netId = subnet.getNetId();
				List<String> ips  = decomposeAddress(subnet.getCidr(),subnet.getPooldata());
				long incrTotal = ips == null ? 0L : ips.size();
				//动态增加外网ip总数
		        syncProgressUtil.incrResourceTotal(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.OUT_IP, incrTotal);
				for(String ip :ips){
					CloudOutIp outip = new CloudOutIp();
					outip.setIpId(UUID.randomUUID().toString());
					outip.setDcId(dataCenter.getId());
					outip.setNetId(subnet.getNetId());
					outip.setSubnetId(subnet.getSubnetId());
					outip.setIpVersion("4");
					outip.setIpAddress(ip);
					insertOutIp(outip);
					syncProgressUtil.incrResourceDone(dataCenter.getId(), SyncType.DATA_CENTER, SyncByDatacenterTypes.OUT_IP);
				}
			}
		}
		
		if(null!=netId){
			List<JSONObject> jsonList = openstackService.getPortByNet(dataCenter, netId, "network:dhcp");
			if(null!=jsonList){
				for(JSONObject json : jsonList){
					if (null != json) {
						JSONArray ipArrays = json.getJSONArray("fixed_ips");
						if (null != ipArrays && ipArrays.size() > 0) {
							for(int i=0;i<ipArrays.size();i++){
								JSONObject ipJson = ipArrays.getJSONObject(i);
								if (null != ipJson) {
									CloudOutIp outIp = new CloudOutIp();
									outIp.setSubnetId(ipJson.getString("subnet_id"));;
									outIp.setIpAddress(ipJson.getString("ip_address"));
									outIp.setUsedType(json.getString("device_owner"));
									
									updateOutIpUsedType(outIp);
								}
							}
						}
					}
				}
			}
		}
		
	}
	
	@SuppressWarnings("rawtypes")
	private List<CloudSubNetWork> queryOutSubnet(String dcId){
		StringBuffer sql = new StringBuffer ();
		List<CloudSubNetWork> subList = new ArrayList<CloudSubNetWork>();
		sql.append("  select ");
		sql.append("   	subnet_id,");
		sql.append("   	net_id,");
		sql.append("   	cidr,");
		sql.append("   	pooldata");
		sql.append("  from cloud_subnetwork");
		sql.append("  where net_id in  ");
		sql.append("  (");
		sql.append("     select net_id from cloud_network where dc_id = ?");
		sql.append("     and router_external = '1' ");
		sql.append("  )");
		
		Query query = cloudOutIpDao.createSQLNativeQuery(sql.toString(), new Object[]{dcId});
		List resultList = query.getResultList();
		for(int i =0;i<resultList.size();i++){
			int index =0;
			Object objs [] = (Object [])resultList.get(i);
			CloudSubNetWork network = new CloudSubNetWork();
			
			network.setSubnetId(String.valueOf(objs[index++]));
			network.setNetId(String.valueOf(objs[index++]));
			network.setCidr(String.valueOf(objs[index++]));
			network.setPooldata(String.valueOf(objs[index++]));
			
			subList.add(network);
		}
		
		return subList;
	}

	
	/**
	 * 删除数据中心的所有OutIp
	 * @param dcId
	 * @return
	 */
	private boolean deleteOutIpByDcId(String dcId){
		boolean flag = false;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" delete from cloud_outip where dc_id = ?");
			cloudOutIpDao.execSQL(sql.toString(), new Object[]{dcId});
			flag = true;
		}catch(Exception e){
			flag = false;
			throw e;
		}
		return flag;
	}
	
	private boolean updateOutIpUsedType(CloudOutIp outIp){
		boolean flag = false;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" update cloud_outip set used_type = ? where subnet_id =? and ip_address = ?");
			cloudOutIpDao.execSQL(sql.toString(), new Object[]{outIp.getUsedType(),outIp.getSubnetId(),outIp.getIpAddress()});
			flag = true;
		}catch(Exception e){
			flag = false;
			throw e;
		}
		return flag;
	}
	
	/**
	 * 解析IP
	 * @param cidr
	 * @param pooldate
	 * @return
	 * @throws AppException
	 */
	public List<String> decomposeAddress(String cidr,String pooldate)throws AppException{
		if(cidr==null || pooldate==null)throw new AppException("error.globe.system",new String[]{"cidr is null or pooldate is null"});
		SubnetUtils sub = new SubnetUtils(cidr);
		String[] strs = sub.getInfo().getAllAddresses();
		List<String> iplist = new ArrayList<String>();
		for(int i=0;i<strs.length;i++){
			if(ipIsValid(pooldate,strs[i])){
				iplist.add(strs[i]);
			}
		}
		
		return iplist;
	}
	
	public boolean insertOutIp(CloudOutIp outIp){
		boolean flag = false;
		try{
			StringBuffer sql = new StringBuffer();
			sql.append(" insert into cloud_outip (ip_id,dc_id,net_id,subnet_id,ip_version,ip_address) values (?,?,?,?,?,?)");
			cloudOutIpDao.execSQL(sql.toString(), new Object[]{
					outIp.getIpId(),
					outIp.getDcId(),
					outIp.getNetId(),
					outIp.getSubnetId(),
					outIp.getIpVersion(),
					outIp.getIpAddress()
				});
			flag = true;
		}catch(Exception e){
			flag = false;
			throw e;
		}
		return flag;
	}
	
	/**  
     * 判断IP是否在指定范围；  
     */  
    public boolean ipIsValid(String ipSectionstr, String ip) {   
    	boolean flag = false;
        if (ipSectionstr == null)   
            throw new NullPointerException("IP段不能为空！");   
        if (ip == null)   
            throw new NullPointerException("IP不能为空！");   
        ip = ip.trim();   
        final String REGX_IP = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";   
        final String REGX_IPB = REGX_IP + "\\," + REGX_IP;   
        if (!ip.matches(REGX_IP))   
            return false; 
        String [] ipSections = ipSectionstr.split(";");  
        if(null!=ipSections&&ipSections.length>0){
        	for(String ipSection :ipSections){
        		flag = checkIpInPool(REGX_IPB,ipSection,ip);
        		if(flag){
        			break;
        		}
        		
        	}
        }
        return flag;   
    }
    
    private boolean checkIpInPool(String REGX_IPB,String ipSection,String ip){
    	if (!ipSection.matches(REGX_IPB))   
            return false;   
        int idx = ipSection.indexOf(',');   
        String[] sips = ipSection.substring(0, idx).split("\\.");   
        String[] sipe = ipSection.substring(idx + 1).split("\\.");   
        String[] sipt = ip.split("\\.");   
        long ips = 0L, ipe = 0L, ipt = 0L;   
        for (int i = 0; i < 4; ++i) {   
            ips = ips << 8 | Integer.parseInt(sips[i]);   
            ipe = ipe << 8 | Integer.parseInt(sipe[i]);   
            ipt = ipt << 8 | Integer.parseInt(sipt[i]);   
        }   
        if (ips > ipe) {   
            long t = ips;   
            ips = ipe;   
            ipe = t;   
        }   
        return ips <= ipt && ipt <= ipe;
    }
}
