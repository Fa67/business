package com.eayun.virtualization.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.eayunstack.model.Rule;
import com.eayun.eayunstack.service.OpenstackSecurityGroupRuleService;
import com.eayun.eayunstack.service.OpenstackVmService;
import com.eayun.virtualization.dao.CloudSecurityGroupRuleDao;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudSecurityGroupRule;
import com.eayun.virtualization.model.BaseCloudVmSgroup;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.service.SecurityGroupRuleService;

/**
 * SecurityGroupRuleServiceImpl
 * 
 * @Filename: SecurityGroupRuleServiceImpl.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年10月28日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
@Service
@Transactional
public class SecurityGroupRuleServiceImpl implements SecurityGroupRuleService {
	@Autowired
	private CloudSecurityGroupRuleDao securityGroupRuleDao;
	@Autowired
	private OpenstackSecurityGroupRuleService openstackGroupRuleService;
	@Autowired
	private OpenstackVmService openstackVmService;

	/**
	 * 创建安全组规则
	 * 
	 * @param request
	 * @return 返回指定安全组规则实体
	 */
	public Rule createRule(HttpServletRequest request, String dcId,
			String prjId, String groupId, @RequestBody Map map) {
		// 1、提取HTML中model值
		String ICMPType = "";
		if (map.containsKey("icmptype")) {
			ICMPType = map.get("icmptype").toString();
		}
		String ICMPCode = "";
		if (map.containsKey("icmpcode")) {
			ICMPCode = map.get("icmpcode").toString();
		}
		String IPXieYi = "";
		if (map.containsKey("IPXieYi")) {
			IPXieYi = map.get("IPXieYi").toString();
		}
		String direction = "";
		if (map.containsKey("Direction")&& null!=map.get("Direction")) {
			direction = map.get("Direction").toString();
		}

		String port_range_min = "";
		if (map.containsKey("port_range_min")) {
			port_range_min = map.get("port_range_min").toString();
		}
		
		String port_range_max = "";
		if (map.containsKey("port_range_max")) {
			port_range_max = map.get("port_range_max").toString();
		}

		String from = "";
		if (map.containsKey("from")) {
			from = map.get("from").toString();
		}
		

		String remote_ip_prefix = "";
		if (map.containsKey("source_ip_address1")) {
			remote_ip_prefix = map.get("source_ip_address1").toString()+"."+map.get("source_ip_address2").toString()+"."+map.get("source_ip_address3").toString()+"."+map.get("source_ip_address4").toString()+"/"+map.get("source_ip_address5").toString();
		}
		String security_group_id = "";
		

		String ethertype = "";
		
		

		// 网络数据
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		// 如果选择的规则协议不是udp、tcp、icmp的类型，把其归属于tcp协议；
		String protocol = map.get("protocol").toString();
		if(ICMPType.equals("99")&&ICMPCode.equals("99")){
			protocol="ALL ICMP";
			ICMPType="";
			ICMPCode="";
		}
		String protocolExpand="";
		if(null!=map.get("protocolExpand")){
			 protocolExpand = map.get("protocolExpand").toString();
		}
		
		if (!protocol.equals("UDP") && !protocol.equals("TCP")
				&& !protocol.equals("ICMP") && !protocol.equals("ALL UDP")
				&& !protocol.equals("ALL TCP") && !protocol.equals("ALL ICMP")) {
			temp.put("protocol", "TCP");
		} else if (protocol.equals("ALL ICMP")) {
			temp.put("protocol", "ICMP");
		} else if (protocol.equals("ALL UDP")) {
			temp.put("protocol", "UDP");
		} else if (protocol.equals("ALL TCP")) {
			temp.put("protocol", "TCP");
		} else if (protocol.equals("TCP") || protocol.equals("UDP")) {
			temp.put("protocol", protocol);
		} else if (protocol.equals("ICMP")) {
			temp.put("protocol", "ICMP");
		}
		// 如果选择ICMP协议规则，设置类型、编码值；
		if (protocol.equals("ICMP")) {
			if (ICMPType != null && !ICMPType.equals("") && !ICMPType.equals("-1")) {
				temp.put("port_range_min", ICMPType);
			}
			if (ICMPCode != null && !ICMPType.equals("") && !ICMPCode.equals("-1")) {
				temp.put("port_range_max", ICMPCode);
			}
		}
		// 如果选择任何协议规则
		if (protocol.equals("其他协议")) {
			if (!IPXieYi.equals("-1")) {
				temp.put("protocol", IPXieYi);
			} else if (IPXieYi.equals("-1")) {
				// temp.put("protocol","None");
			}
		}
		// 检查选择方向是否为空
		if (direction != null && direction.equals("egress")) {
			temp.put("direction", direction);
		} else {
			temp.put("direction", "ingress");
		}

		// port_range_min检查端口的值是否存在，有值给他put进去；
		if (port_range_min != null && !port_range_min.equals("")) {
			temp.put("port_range_min", port_range_min);
		} else {
			if (protocol.equals("DNS")) {
				temp.put("port_range_min", "53");
				temp.put("port_range_max", "53");
			} else if (protocol.equals("HTTP")) {
				temp.put("port_range_min", "80");
				temp.put("port_range_max", "80");
			} else if (protocol.equals("HTTPS")) {
				temp.put("port_range_min", "443");
				temp.put("port_range_max", "443");
			} else if (protocol.equals("IMAP")) {
				temp.put("port_range_min", "143");
				temp.put("port_range_max", "143");
			} else if (protocol.equals("IMAPS")) {
				temp.put("port_range_min", "993");
				temp.put("port_range_max", "993");
			} else if (protocol.equals("LDAP")) {
				temp.put("port_range_min", "389");
				temp.put("port_range_max", "389");
			} else if (protocol.equals("MS SQL")) {
				temp.put("port_range_min", "1443");
				temp.put("port_range_max", "1443");
			} else if (protocol.equals("MYSQL")) {
				temp.put("port_range_min", "3306");
				temp.put("port_range_max", "3306");
			} else if (protocol.equals("POP3")) {
				temp.put("port_range_min", "110");
				temp.put("port_range_max", "110");
			} else if (protocol.equals("POP3S")) {
				temp.put("port_range_min", "995");
				temp.put("port_range_max", "995");
			} else if (protocol.equals("RDP")) {
				temp.put("port_range_min", "3389");
				temp.put("port_range_max", "3389");
			} else if (protocol.equals("SMTP")) {
				temp.put("port_range_min", "25");
				temp.put("port_range_max", "25");
			} else if (protocol.equals("SMTPS")) {
				temp.put("port_range_min", "465");
				temp.put("port_range_max", "465");
			} else if (protocol.equals("SSH")) {
				temp.put("port_range_min", "22");
				temp.put("port_range_max", "22");
			} else if (protocol.equals("ALL UDP")) {
				temp.put("port_range_min", "1");
				temp.put("port_range_max", "65535");
			} else if (protocol.equals("ALL TCP")) {
				temp.put("port_range_min", "1");
				temp.put("port_range_max", "65535");
			}
		}
		// port_range_max检查端口的值是否存在，有值给他put进去；
		if (port_range_max != null && !port_range_min.equals("")) {
			temp.put("port_range_max", port_range_max);
		}

		if (from.equals("CIDR")) {
			
			// 进行验证IP地址是IPv4与IPv6
			/*String pv4 = map.get("ipv4").toString();
			String pv6 = map.get("ipv6").toString();
			if (pv6.equals("true")) {
				temp.put("ethertype", "IPv6");
			}*/
			
			temp.put("remote_ip_prefix", remote_ip_prefix);
			temp.put("security_group_id", groupId);
		} else if (from.equals("SecurityGroup")) {
			if (map.containsKey("remote_group_id")) {
				security_group_id = map.get("remote_group_id").toString();
			}
			
			// security_group_id代表的是安全组自己 属性的id号；所属的那个项目下的这个安全组的id号；
			temp.put("security_group_id", groupId);
			// 这个属性是指在创建规则时，给这个规则赋予指向哪个安全组的id
			temp.put("remote_group_id", security_group_id);
			/*
			//去掉创建规则页面中“输入类型”字段
			if (map.containsKey("ethertype")) {
				ethertype = map.get("ethertype").toString();
			}
			temp.put("ethertype", "IPv4");*/
			
		}

		// 此处需要改进volume 变为 security_group;参考PDF
		data.put("security_group_rule", temp);

		// 创建安全组规则
		Rule result = openstackGroupRuleService.createRule(dcId, prjId, data);

		if (result != null) {
			BaseCloudSecurityGroupRule rule = new BaseCloudSecurityGroupRule();
			// 从OpenStack差寻到的数据 设置到DB的cloudResource表中，对应在DB中也保存一份；
			rule.setSgrId(result.getId());
			rule.setPrjId(result.getTenant_id());
			rule.setDcId(dcId);
			rule.setCreateTime(new Date());
			
			if(map.get("icmp")!=null){
			rule.setIcMp(map.get("icmp").toString());
			}
			// 从session中获取当前用户名
			SessionUserInfo sessionUser = (SessionUserInfo) request
					.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
			String userName = sessionUser.getUserName();
			rule.setCreateName(userName);
			rule.setSgId(result.getSecurity_group_id());
			if (null != result.getRemote_group_id()
					&& !"".equals(result.getRemote_group_id())) {
				rule.setRemoteGroupId(result.getRemote_group_id());
			}
			// 设置方向
			rule.setDirection(result.getDirection());
			// 设置IPv4 或 IPv6
			if (null != result.getEthertype()
					&& !"".equals(result.getEthertype())) {
				rule.setEthertype(result.getEthertype());
			}
			// 设置IP协议
			if (null != result.getProtocol()
					&& !"".equals(result.getProtocol())) {
				//rule.setProtocol(result.getProtocol());
				if("ALL ICMP".equals(protocol)){
					rule.setProtocol("ICMP");
				}else{
					rule.setProtocol(protocol);
				}
				
			}
			// 设置最小端口
			if (null != result.getPort_range_min()
					&& !"".equals(result.getPort_range_min())) {
				rule.setPortRangeMin(result.getPort_range_min());
			}
			// 设置最大端口
			if (null != result.getPort_range_max()
					&& !"".equals(result.getPort_range_max())) {
				rule.setPortRangeMax(result.getPort_range_max());
			}
			// 设置远程CIDR
			if (null != result.getRemote_ip_prefix()
					&& !"".equals(result.getRemote_ip_prefix())) {
				rule.setRemoteIpPrefix(result.getRemote_ip_prefix());
			}
			if(protocol.equals("DNS")||protocol.equals("HTTP")||protocol.equals("HTTPS")||protocol.equals("IMAP")||protocol.equals("IMAPS")||protocol.equals("LDAP")||protocol.equals("MS SQL")||protocol.equals("MYSQL")||protocol.equals("POP3")||protocol.equals("POP3S")||protocol.equals("RDP")||protocol.equals("SMTP")||protocol.equals("SMTPS")||protocol.equals("SSH")){
				rule.setProtocolExpand(protocol);
			}
			rule.setProtocolExpand(protocolExpand);
			securityGroupRuleDao.save(rule);

		}

		return result;

	}
	// 删除规则
	public boolean deleteGroupRule(String datacenterId, String id){
		boolean flag = openstackGroupRuleService.delete(datacenterId, null, id);
		if (flag) {
			if (securityGroupRuleDao.findOne(id) != null) {
				securityGroupRuleDao.delete(securityGroupRuleDao.findOne(id));
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public Page querySecurityGroupCloudHostList(String sgid,QueryMap qm) {
			Page page=null;
			StringBuffer sql=new StringBuffer();
			sql.append("select g.sg_id,g.vm_id,t.vm_name,t.vm_status ,net.subnet_name as subnetname,flo.flo_ip,net.cidr,t.vm_ip,t.prj_id,t.dc_id ,net.subnet_type,t.charge_state,selfsubnet.subnet_name as selfname,t.self_ip from cloud_vmsecuritygroup g"
						+" left join cloud_vm t on g.vm_id=t.vm_id"
						+" left join cloud_subnetwork net on net.subnet_id=t.subnet_id"
						+" left join cloud_floatip flo on flo.resource_id=t.vm_id"
						+" left join cloud_subnetwork selfsubnet on selfsubnet.subnet_id = t.self_subnetid"
						+" where t.is_deleted='0' and  t.is_visable = '1' and  g.sg_id=?");
						Object [] obj={sgid};
						page=	securityGroupRuleDao.pagedNativeQuery(sql.toString(), qm, obj);
						List<Object[]>list=(List<Object[]>) page.getResult();
						List pagelist= new ArrayList();
						for(int i=0;i<list.size();i++){
							 Object[] object=list.get(i);
							 Map<String, Object> map=new HashMap<String,Object>();
							 map.put("sgid", object[0]==null?"":object[0]);
							 map.put("vmid", object[1]==null?"":object[1]);
							 map.put("vmname", object[2]==null?"":object[2]);
							 map.put("vm_status", object[3]==null?"":object[3]);
							 if(object[3]!=null&&object[3].equals("RESUMING")||object[3]!=null&&object[3].equals("SOFT_RESUME")){
								 map.put("statusForDis", "恢复中");
							 }else if(object[3]!=null&&object[3].equals("ACTIVE")){
								 map.put("statusForDis", "运行中");
							 }else if(object[3]!=null&&object[3].equals("SHUTOFF")){
								 
									 map.put("statusForDis", "已关机");
								 
							 }else if(object[3]!=null&&object[3].equals("SHUTOFFING")){
								 map.put("statusForDis", "关机中");
							 }
							 else if(object[3]!=null&&object[3].equals("SUSPENDED")){
								 map.put("statusForDis", "暂停服务");
							 }
							 else if(object[3]!=null&&object[3].equals("SUSPENDEDING")){
								 map.put("statusForDis", "暂停服务中");
							 }else if(object[3]!=null&&object[3].equals("ERROR")){
								 map.put("statusForDis", "故障");
							 }else if(object[3]!=null&&object[3].equals("BUILDING")){
								 map.put("statusForDis", "创建中");
							 }else if(object[3]!=null&&object[3].equals("BUILD")){
								 map.put("statusForDis", "创建中");
							 }else if(object[3]!=null&&object[3].equals("SOFT_DELETED")){
								 map.put("statusForDis", "已删除");
								 
							 }else if(object[3]!=null&&object[3].equals("SOFT_DELETING")){
								 map.put("statusForDis", "删除中"); 
							 }
							 else if(object[3]!=null&&object[3].equals("STARTING")){
								 map.put("statusForDis", "启动中");
							 }else if (object[3]!=null&&object[3].equals("REBOOT")){
								 map.put("statusForDis", "重启中");
							 }else if(object[3]!=null&&object[3].equals("RESIZE")||object[3]!=null&&object[3].equals("VERIFY_RESIZE")||object[3]!=null&&object[3].equals("RESIZED")){
								 map.put("statusForDis", "升级中");
							 }
							 if(object[11]!=null&&(object[11].toString().equals("2")||object[11].toString().equals("3"))){
								 map.remove("statusForDis");
								 map.put("statusForDis", "已到期");
							 }
							 if(object[11]!=null&&object[11].toString().equals("1")){
								 map.remove("statusForDis");
								 map.put("statusForDis", "余额不足");
								 }
							 
							 map.put("subnet_name", object[4]==null?"":object[4]);
							 map.put("flo_ip", object[5]==null?"":object[5]);
							 map.put("cidr", object[6]==null?"":object[6]);
							 map.put("vm_ip", object[7]==null?"":object[7]);
							 map.put("prj_id", object[8]==null?"":object[8]);
							 map.put("dcid", object[9]==null?"":object[9]);
							 map.put("subnettype", object[10]==null?"":object[10]);
							 map.put("cheang_st", object[11]==null?"":object[11]);
							 map.put("subName",object[12]==null?"":object[12] );
							 map.put("subip",object[13]==null?"":object[13] );
							 pagelist.add(map);
							
						}
						page.setResult(pagelist);
			
		return page;
	}

	@Override
	public List getaddSecurityGroupCloudHostList(String sgid, String prjid,String sgname,String cusorg) {
		StringBuffer sql=new StringBuffer();
		sql.append("select v.vm_id,v.vm_name,net.subnet_name,flo.flo_ip,v.vm_ip,v.prj_id,v.dc_id,net.subnet_type,selfsubnet.subnet_name as sbname,v.self_ip"
				+"  from cloud_vm v   "
				+"  left join cloud_subnetwork net on v.subnet_id=net.subnet_id"
				+"  left join cloud_floatip flo on flo.resource_id=v.vm_id"
				+"	 left join cloud_subnetwork selfsubnet on selfsubnet.subnet_id = v.self_subnetid"
				+"  where  v.is_deleted='0' and v.charge_state='0' and v.is_visable = '1' and " 
				+" v.prj_id= ? and v.vm_id not in(select vm_id from cloud_vmsecuritygroup where sg_id= ?)");
		Object [] obj={prjid,sgid};
		List<Map> listdate=new ArrayList<>();
		List  list=securityGroupRuleDao.createSQLNativeQuery(sql.toString(), obj).getResultList();
	
				for(int i=0;i<list.size();i++){
					 Map<String, Object> map=new HashMap<String,Object>();
					 
					Object [] objdata=(Object[]) list.get(i);
					map.put("vmid", objdata[0]==null?"":objdata[0]);
					map.put("vmname", objdata[1]==null?"":objdata[1]);
					map.put("subnetname", objdata[2]==null?"":objdata[2]);
					map.put("floip", objdata[3]==null?"":objdata[3]);
					map.put("vmip", objdata[4]==null?"":objdata[4]);
					map.put("prjid", objdata[5]==null?"":objdata[5]);
					map.put("sgid", sgid);
					map.put("sgname", sgname);
					map.put("dcid", objdata[6]==null?"":objdata[6]);
					map.put("subnettype",objdata[7]==null?"":objdata[7] );
					map.put("suName",objdata[8]==null?"":objdata[8] );
					map.put("seleip",objdata[9]==null?"":objdata[9] );
					
					
					listdate.add(map);
				}
		return listdate;
	}

	@Override
	public void securityGroupsAddCloudHost(List<CloudVm> cloudvm, String sgId, String sgname) throws AppException {
		try {
			
			List<BaseCloudSecurityGroup> toBeAddList =new ArrayList<BaseCloudSecurityGroup>();
			BaseCloudSecurityGroup  basecsg=new BaseCloudSecurityGroup();
			//basecsg.setSgName(sgId);
			basecsg.setSgId(sgId);
			toBeAddList.add(basecsg);
			for(int i=0 ;i<cloudvm.size();i++){
				openstackVmService.editVmSecurityGroup(cloudvm.get(i), toBeAddList,null);//底层安全组添加云主机
				BaseCloudVmSgroup vsg=new BaseCloudVmSgroup(cloudvm.get(i).getVmId(),sgId);
				//cloudVmSecurityGroupDao.merge(vsg);
				securityGroupRuleDao.merge(vsg);
			}

			
		} catch (AppException e) {
			throw e;
			
		}
		
		
	}

	@Override
	public void securityGroupsRemoveCloudHost(CloudVm cloudcm, String sgId, String sgname) throws AppException {
		try{
		List<BaseCloudSecurityGroup> toBeDelList =new ArrayList<BaseCloudSecurityGroup>();
		BaseCloudSecurityGroup  basecsg=new BaseCloudSecurityGroup();
		//basecsg.setSgName(sgId);
		basecsg.setSgId(sgId);
		toBeDelList.add(basecsg);
		openstackVmService.editVmSecurityGroup(cloudcm, null,toBeDelList);//底层安全组移除云主机
		
		securityGroupRuleDao.deletedvmsgroup( cloudcm.getVmId(),sgId);
		}catch(AppException e){
			throw e;
			
		}
	}
	
}
