package com.eayun.virtualization.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.common.util.DateUtil;
import com.eayun.virtualization.dao.CloudFlavorDao;
import com.eayun.virtualization.dao.CloudSecretKeyDao;
import com.eayun.virtualization.dao.CloudSecretKeyVmDao;
import com.eayun.virtualization.dao.CloudVmDao;
import com.eayun.virtualization.dao.CloudVolumeDao;
import com.eayun.virtualization.ecmcservice.EcmcSecretKeyService;
import com.eayun.virtualization.ecmcvo.CloudSecretKeyVoe;
import com.eayun.virtualization.ecmcvo.SecretKeyListVoe;
import com.eayun.virtualization.ecmcvo.SecretKeyVm;
import com.eayun.virtualization.model.BaseCloudFlavor;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.BaseSecretkeyVm;


@Service
@Transactional
public class EcmcSecretKeyServiceImpl implements EcmcSecretKeyService {

	@Autowired
	public CloudSecretKeyDao secretKeyDao;

	@Autowired
	public CloudSecretKeyVmDao secretKeyVmDao;
	@Autowired
	public CloudVmDao cloudVmDao;
	@Autowired
	public CloudFlavorDao cloudFlavorDao;
	
	@Autowired
	public CloudVolumeDao cloudVolumeDao;

	@Override
	public Page getecscsecretkeylist(QueryMap queryMap, String prjName, String dcid, String cusName, String queryName)
			throws AppException {
		List list = new ArrayList<>();
		Page page = null;
		int index = 0;
		Object[] args = new Object[4];
		StringBuffer sql = new StringBuffer(
				" select sk.secretkey_id as skid,sk.secretkey_name as skanem,pr.prj_name as prjname,sys.cus_org as cusorg,dc.dc_name as dcname,sk.create_time as ctime from cloud_secretkey sk");
		sql.append(" left join cloud_project pr on pr.prj_id=sk.prj_id");
		sql.append(" left join sys_selfcustomer sys on sys.cus_id=pr.customer_id");
		sql.append(" left join dc_datacenter dc on dc.id=sk.dc_id");
		sql.append(" where 1=1");
		if (null != prjName&&!"".equals(prjName)) {
			sql.append(" and pr.prj_name in (?").append(index+1).append(") ");
			args[index] = Arrays.asList(StringUtils.split(prjName, ","));
			index++;
		}
		if (null != dcid&&!"".equals(dcid)) {
			sql.append(" and sk.dc_id=?").append(index+1);
		
			args[index] = dcid;
			index++;
		}
		if (null != cusName&&!"".equals(cusName)) {
			sql.append(" and sys.cus_org in (?").append(index+1).append(") ");
			args[index] = Arrays.asList(StringUtils.split(cusName, ","));
			index++;
		}
		if (null != queryName&&!"".equals(queryName)) {
			sql.append(" and sk.secretkey_name like?").append(index+1).append(" ");
			queryName = queryName.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
			queryName = "%" + queryName + "%";
			args[index] = queryName;
			index++;
		}
		sql.append(" order by sk.create_time desc  ");
		 Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		page = secretKeyDao.pagedNativeQuery(sql.toString(), queryMap, params);
		List pagelist = (List) page.getResult();

		// List<SecretKeyListVoe> newList = new ArrayList<SecretKeyListVoe>();

		for (int i = 0; i < pagelist.size(); i++) {
			Object obj[] = (Object[]) pagelist.get(i);
			SecretKeyListVoe voe = new SecretKeyListVoe();
			voe.setSkid(ObjectUtils.toString(obj[0]));
			voe.setSkname(ObjectUtils.toString(obj[1]));
			voe.setPrjname(ObjectUtils.toString(obj[2]));
			voe.setCusorg(ObjectUtils.toString(obj[3]));
			voe.setDcname(ObjectUtils.toString(obj[4]));
			voe.setSktime(DateUtil.stringToDate(ObjectUtils.toString(obj[5])));
			voe.setVmCount(0);
			List<BaseSecretkeyVm> listvmsk= secretKeyVmDao.getVmListByskId(ObjectUtils.toString(obj[0]));
			
			if(listvmsk.size()>0){
				voe.setVmCount(listvmsk.size());
			}
			pagelist.set(i, voe);

		}
		return page;
	}

	@Override
	public CloudSecretKeyVoe getSrcretKeyById(String skid) throws AppException {
		CloudSecretKeyVoe voe = new CloudSecretKeyVoe();
		
		StringBuffer sql = new StringBuffer(
				" select sk.secretkey_id as skid,sk.secretkey_name as skanem,pr.prj_name as prjname,sys.cus_org as cusorg,dc.dc_name as dcname,sk.create_time as ctime,sk.fingerprint,sk.public_key,sk.secretkey_desc,sk.prj_id,sk.dc_id"
				+ " from cloud_secretkey sk");
		sql.append(" left join cloud_project pr on pr.prj_id=sk.prj_id");
		sql.append(" left join sys_selfcustomer sys on sys.cus_id=pr.customer_id");
		sql.append(" left join dc_datacenter dc on dc.id=sk.dc_id");
		sql.append(" where 1=1 and sk.secretkey_id=?");
		
		List list=secretKeyDao.createSQLNativeQuery(sql.toString(),new String []{skid} ).getResultList();
		
		for(int k=0;k<list.size();k++){
			Object[] obj=(Object[]) list.get(k);
			voe.setSecretkeyId(ObjectUtils.toString(obj[0]));
			voe.setSecretkeyName(ObjectUtils.toString(obj[1]));
			voe.setPrjname(ObjectUtils.toString(obj[2]));
			voe.setCusorg(ObjectUtils.toString(obj[3]));
			voe.setDcname(ObjectUtils.toString(obj[4]));
			voe.setCreateTime(DateUtil.stringToDate(ObjectUtils.toString(obj[5])));
			voe.setFingerPrint(ObjectUtils.toString(obj[6]));
			voe.setPublicKey(ObjectUtils.toString(obj[7]));
			voe.setSecretkeyDesc(ObjectUtils.toString(obj[8]));
			voe.setPrjId(ObjectUtils.toString(obj[9]));
			voe.setDcId(ObjectUtils.toString(obj[10]));
		}
		
		
		
		return voe;
	}



	@Override
	public Page getSrcretKeyByIdAndVmList(String skid,QueryMap queryMap) throws AppException {
		StringBuffer sql=new StringBuffer("SELECT DISTINCT vm.vm_id,vm.vm_name,vm.vm_status,vm.vm_ip,vm.sys_type,vm.os_type,	fla.flavor_vcpus,fla.flavor_ram,");
		sql.append(" fla.flavor_disk,flo.flo_ip,vol.vol_size, vm.self_ip,img.image_name,subnet.route_id,vm.charge_state FROM secretkey_vm sk");
		sql.append(" 	left join cloud_vm vm on sk.vm_id=vm.vm_id");
		sql.append(" 	 LEFT JOIN cloud_flavor fla ON vm.flavor_id = fla.flavor_id");
		sql.append(" 	 LEFT JOIN cloud_network net on vm.net_id=net.net_id");
		sql.append(" 	  LEFT JOIN cloud_floatip flo ON vm.vm_id = flo.resource_id  AND flo.resource_type = 'vm' AND flo.is_deleted = '0'");
		sql.append(" 	   LEFT JOIN cloud_subnetwork subnet on subnet.subnet_id = vm.subnet_id");
		sql.append(" 	   LEFT JOIN cloud_image img ON img.image_id = vm.from_imageid ");
		sql.append(" 	    LEFT JOIN (SELECT sum(vol_size) as vol_size,vm_id FROM cloud_volume WHERE vol_bootable = '0' GROUP BY vm_id ) AS vol ON vm.vm_id=vol.vm_id " );
		sql.append(" where vm.is_deleted = '0' and vm.is_visable = '1' and sk.secretkey_id = ? ");
		Page page= secretKeyDao.pagedNativeQuery(sql.toString(), queryMap, new String[]{skid});
		List list=(List) page.getResult();
		List<SecretKeyVm> datelist=new ArrayList<>();
	
		for(int i=0;i<list.size();i++){
			
			if(null==list.get(i))continue;
			
			Object [] obj=(Object[]) list.get(i);
			
			SecretKeyVm svm=new SecretKeyVm();
			svm.setSkId(skid);
			svm.setVmId(ObjectUtils.toString(obj[0]));
			svm.setVmname(ObjectUtils.toString(obj[1]));
			
			svm.setVmip(ObjectUtils.toString(obj[3]));
			String systemType = String.valueOf(obj[4]);
            if (!StringUtils.isEmpty(systemType) && !"null".equals(systemType)) {
                svm.setOsType(DictUtil.getDataTreeByNodeId(systemType).getNodeName());
            }
			svm.setCpus(ObjectUtils.toString(obj[6])==""?0:Integer.parseInt(ObjectUtils.toString(obj[6])));
			svm.setRams(ObjectUtils.toString(obj[7])==""?0:Integer.parseInt(ObjectUtils.toString(obj[7]))/1024);
			svm.setSysdisks(ObjectUtils.toString(obj[8])==""?0:Integer.parseInt(ObjectUtils.toString(obj[8])));
			svm.setFloip(ObjectUtils.toString(obj[9]));
			svm.setDatedisk(ObjectUtils.toString(obj[10])==""?0:Integer.parseInt(ObjectUtils.toString(obj[10])));
			
			svm.setNetIp(ObjectUtils.toString(obj[11]));
			if("".equals(ObjectUtils.toString(obj[13]))){
				svm.setIsroute(false);
			}else{
				svm.setIsroute(true);
			}
			if(CloudResourceUtil.CLOUD_CHARGESTATE_NORMAL_CODE.equals(ObjectUtils.toString(obj[14]))){
			    svm.setStatus(DictUtil.getStatusByNodeEn("vm", ObjectUtils.toString(obj[2])));
			}else{
			    svm.setStatus(CloudResourceUtil.escapseChargeState(ObjectUtils.toString(obj[14])));
			}
			svm.setChargeState(ObjectUtils.toString(obj[14]));
            if("已关机".equals(svm.getStatus())){
                svm.setIsremoveVm(true);
            }else{
                svm.setIsremoveVm(false);
            }
            
			datelist.add(svm);
		
			
		}
		
		
		page.setResult(datelist);
		
		
		
		
		return page;
	}
	
	
}
