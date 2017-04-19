package com.eayun.virtualization.ecmcservice.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.virtualization.dao.CloudOutIpDao;
import com.eayun.virtualization.ecmcservice.EcmcCloudOutIpService;
import com.eayun.virtualization.model.BaseCloudOutIp;
import com.eayun.virtualization.model.CloudOutIp;

/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月13日
 */
@Service
@Transactional
public class EcmcCloudOutIpServiceImpl implements EcmcCloudOutIpService {

	private static final Logger log = LoggerFactory.getLogger(EcmcCloudOutIpServiceImpl.class);
	@Autowired
	private CloudOutIpDao outipdao;
	@Override
	public Page list(Page page, String datacenterId, String usestauts, String distribution, String ip,String[] prjName,String[] cusName,
			QueryMap querymap) throws AppException {
		StringBuffer sql = new StringBuffer();
		List<Object> parmes = new ArrayList<Object>();
		sql.append("select outip.ip_id as ipId,outip.dc_id as dcId,outip.net_id as netId,outip.subnet_id as subnetId,outip.used_type as usedType,"
				+ "outip.ip_version as ipVersion,outip.ip_address as ipAddress,outip.create_name as createName,outip.create_time as createTime,"
				+ " floatip.resource_type as resourceType,floatip.resource_id as resourceId,dcs.dc_name as dcName,project.prj_name as prjName,"
				+ " CASE WHEN floatip.resource_type = 'vm' THEN vm.vm_name ELSE ld.pool_name END as resName,"
				+ "cus.cus_org as cusorg,floatip.prj_id as prjId  ,route.route_id as route_id from cloud_outip outip ");
		sql.append(" left join cloud_floatip floatip on outip.ip_address = floatip.flo_ip   and outip.dc_id=floatip.dc_id and outip.net_id=floatip.net_id and floatip.is_deleted=0 and floatip.is_visable='1'");
		sql.append(" left join dc_datacenter dcs on outip.dc_id = dcs.id ");
		sql.append(" left join cloud_route route on route.gateway_ip=outip.ip_address and route.dc_id=outip.dc_id and route.net_id=outip.net_id");
		sql.append(" left join cloud_project project on (project.prj_id = floatip.prj_id or project.prj_id= route.prj_id) ");
		sql.append(" left join sys_selfcustomer cus on cus.cus_id = project.customer_id ");
		sql.append(" left join cloud_vm vm on floatip.resource_id = vm.vm_id ");
		sql.append(" left join cloud_ldpool ld on floatip.resource_id = ld.pool_id ");
		sql.append(" where 1=1 ");
		int index = 1;
		//根据项目名称 多个查询
  		if (prjName!=null && prjName.length>0) {
  			sql.append("and project.prj_name in (?").append(index).append(") ");
  			parmes.add(Arrays.asList(prjName));
  			index++;
  		}
  		//根据客户姓名查询
  		if (cusName!=null && cusName.length>0) {
  			sql.append("and cus.cus_org in (?").append(index).append(") ");
  			parmes.add(Arrays.asList(cusName));
  			index++;
  		}
		if (null!=datacenterId && !"null".equals(datacenterId) && !"".equals(datacenterId) && !"undefined".equals(datacenterId)) {
			if(index==1){
				sql.append(" and outip.dc_id = ?");
			}else{
				sql.append(" and outip.dc_id = (?").append(index).append(") ");
			}
			parmes.add(datacenterId);
		}
	    if(null!=ip && !"null".equals(ip) && !"".equals(ip) && !"undefined".equals(ip)){
	    	sql.append(" and outip.ip_address like ?");
	    	parmes.add("%"+ip+"%");
	    }
	    if (null!=distribution && !"null".equals(distribution) && !"".equals(distribution) && !"undefined".equals(distribution)) {//已分配资源的
	 		if("1".equals(distribution)){
	 			sql.append(" and (floatip.prj_id is not null or outip.used_type is not null or route.route_id is not null) ");//项目ID不为空 表示已分配或者底层占用不为空
	 		}else{
	 			sql.append(" and (floatip.prj_id is null and outip.used_type is null and route.route_id is null) ");
	 		}
		}
	    if (null!=usestauts && !"null".equals(usestauts) && !"".equals(usestauts) && !"undefined".equals(usestauts)) {
	    	if("1".equals(usestauts)){
	    		sql.append(" and floatip.resource_id is not null  ");//资源ID不为空表示已使用或者底层占用不为空
	    	}else if("3".equals(usestauts)){
	    		sql.append("  and  route.route_id is not null ");
	    		
	    	}else if("4".equals(usestauts)){
	    		sql.append("  and  outip.used_type is not null ");
	    	}
	    	
	    	else{
	    		sql.append(" and (floatip.resource_id is null and outip.used_type is null and route.route_id is  null) ");
	    	}
		}
	    
	    
	    sql.append(" order by outip.create_time desc ");
	    
	    page = outipdao.pagedNativeQuery(sql.toString(), querymap, parmes.toArray());
	    List newlist = (List) page.getResult();
	    CloudOutIp outip = null;
	    int a = newlist.size();
        for (int i = 0; i < a; i++) {
            Object[] objs = (Object[]) newlist.get(i);
            outip = new CloudOutIp();
            outip.setIpId(ObjectUtils.toString(objs[0]));
            outip.setDcId(ObjectUtils.toString(objs[1]));
            outip.setNetId(ObjectUtils.toString(objs[2]));
            outip.setSubnetId(ObjectUtils.toString(objs[3]));
            outip.setUsedType(ObjectUtils.toString(objs[4]));
            outip.setIpVersion(ObjectUtils.toString(objs[5]));
            outip.setIpAddress(ObjectUtils.toString(objs[6]));
            outip.setCreateName(ObjectUtils.toString(objs[7]));
            outip.setCreateTime(ObjectUtils.toString(objs[8]));
            outip.setResourceType(ObjectUtils.toString(objs[9]));
            outip.setResourceId(ObjectUtils.toString(objs[10]));
            outip.setDcName(ObjectUtils.toString(objs[11]));
            outip.setPrjName(ObjectUtils.toString(objs[12]));
            outip.setResoutceName(ObjectUtils.toString(objs[13]));
            outip.setUseName(ObjectUtils.toString(objs[14]));
            outip.setPrjId(ObjectUtils.toString(objs[15]));
            outip.setRouteId(ObjectUtils.toString(objs[16]));
            newlist.set(i, outip);
        }
		return page;
	}
	
	/**  
     * 判断IP是否在指定范围；  
     */  
    public static boolean ipIsValid(String ipSectionstr, String ip) {   
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
    
    private static boolean  checkIpInPool(String REGX_IPB,String ipSection,String ip){
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
    
    public static void main(String[] args) {   
        if (ipIsValid("192.168.1.1,192.168.111.2", "192.168.23.154")) {   
            System.out.println("ip属于该网段");   
        } else  
            System.out.println("ip不属于该网段");   
    }  
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
	@Override
	public boolean createOutIp(String cidr,String pooldate,String dcId,String netId,String subnetId,String routeId,String ipVersion) throws AppException {
		try{
			BaseCloudOutIp outip = null;
			List<String> ips = decomposeAddress(cidr, pooldate);
			List<BaseCloudOutIp> outips = new ArrayList<BaseCloudOutIp>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String createTime = sdf.format(new Date());
			for(String ip : ips){
				outip = new BaseCloudOutIp();
				outip.setDcId(dcId);
				outip.setNetId(netId);
				outip.setSubnetId(subnetId);
				outip.setIpVersion(ipVersion);
				outip.setIpAddress(ip);
				outip.setCreateTime(createTime);
				outip.setCreateName(EcmcSessionUtil.getUser() != null ? EcmcSessionUtil.getUser().getAccount() : null);
				outips.add(outip);
			}
			outipdao.save(outips);
			return true;
		}catch(AppException e){
			throw e;
		}
	}
	@Override
	public boolean updateOutIp(BaseCloudOutIp outip) throws AppException {
		try {
			BaseCloudOutIp newoutip = outipdao.findOne(outip.getIpId());
			if(newoutip!=null){
				outipdao.saveOrUpdate(outip);
				return true;
			}
		} catch (AppException e) {
			throw e;
		}
		return false;
	}
	@Override
	public boolean deleteOutIp(String subnetId) throws AppException {
		try {
			int num = outipdao.createSQLNativeQuery("delete from cloud_outip where subnet_id = ?",subnetId).executeUpdate();
			log.info("共删除："+num+"个");
			return true;
		} catch (Exception e) {
			throw new AppException("ERROR.OUTIP.DELETE:"+e.getMessage());
		}
	}
	
	@Override
	public CloudOutIp queryByOne(String id) throws AppException {
		StringBuffer sql = new StringBuffer();
		sql.append("select   outip.ip_id as ipId,outip.dc_id as dcId,outip.net_id as netId,outip.subnet_id as subnetId,outip.used_type "
				+ "as usedType,outip.ip_version as ipVersion,outip.ip_address as ipAddress,outip.create_name as createName,outip.create_time"
				+ " as createTime, floatip.resource_type as resourceType,floatip.resource_id as resourceId,dcs.dc_name as dcName,project.prj_name "
				+ " as prjName, CASE WHEN floatip.resource_type = 'vm' THEN vm.vm_name ELSE ld.pool_name END as resName,cus.cus_name as cusName,"
				+ "CASE WHEN floatip.resource_type = 'vm' THEN vm.vm_ip ELSE vip.vip_address END as vmIp ,route.route_id as route_id,route.route_name as routeName from cloud_outip outip   ");
		sql.append(" 	left join cloud_floatip floatip on outip.ip_address = floatip.flo_ip  and outip.dc_id=floatip.dc_id and outip.net_id=floatip.net_id and floatip.is_deleted=0 and floatip.is_visable='1'");
		sql.append(" 	left join dc_datacenter dcs on outip.dc_id = dcs.id   ");
		sql.append("    left join cloud_project project on project.prj_id = floatip.prj_id  ");
		sql.append("    left join sys_selfcustomer cus on cus.cus_id = project.customer_id   ");
		sql.append("    left join cloud_route route on route.gateway_ip=outip.ip_address and route.dc_id=outip.dc_id and route.net_id=outip.net_id");
		sql.append("    left join cloud_vm vm on floatip.resource_id = vm.vm_id   ");
		sql.append("    left join cloud_ldpool ld on floatip.resource_id = ld.pool_id  ");
		sql.append("    left join cloud_ldvip vip on vip.pool_id = ld.pool_id    ");
		sql.append(" where  outip.ip_id = ? ");
	
		
		List list = outipdao.createSQLNativeQuery(sql.toString(), id).getResultList();
		CloudOutIp outip = null;
		if(list!=null && list.size()>0){
			Object[] objs = (Object[]) list.get(0);
			outip = new CloudOutIp();
            outip.setIpId(ObjectUtils.toString(objs[0]));
            outip.setDcId(ObjectUtils.toString(objs[1]));
            outip.setNetId(ObjectUtils.toString(objs[2]));
            outip.setSubnetId(ObjectUtils.toString(objs[3]));
            outip.setUsedType(ObjectUtils.toString(objs[4]));
            outip.setIpVersion(ObjectUtils.toString(objs[5]));
            outip.setIpAddress(ObjectUtils.toString(objs[6]));
            
            outip.setCreateName(ObjectUtils.toString(objs[7]));
            outip.setCreateTime(ObjectUtils.toString(objs[8]));
            outip.setResourceType(ObjectUtils.toString(objs[9]));
            outip.setResourceId(ObjectUtils.toString(objs[10]));
            outip.setDcName(ObjectUtils.toString(objs[11]));
            outip.setPrjName(ObjectUtils.toString(objs[12]));
            outip.setResoutceName("".equals(ObjectUtils.toString(objs[13]))?ObjectUtils.toString(objs[17]):ObjectUtils.toString(objs[13]));
            outip.setUseName(ObjectUtils.toString(objs[14]));
            outip.setVmIp(ObjectUtils.toString(objs[15]));
            outip.setRouteId(ObjectUtils.toString(objs[16]));
            list.set(0, outip);
		}
		return outip;
	}
}
