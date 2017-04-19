package com.eayun.virtualization.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.MockMultipartFile;
import com.eayun.eayunstack.model.KeyPairs;
import com.eayun.eayunstack.service.OpenstackSecretkeyService;
import com.eayun.file.model.EayunFile;
import com.eayun.file.service.FileService;
import com.eayun.log.service.LogService;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.dao.CloudSecretKeyDao;
import com.eayun.virtualization.dao.CloudSecretKeyVmDao;
import com.eayun.virtualization.ecmcservice.EcmcSecretKeyService;
import com.eayun.virtualization.model.BaseCloudSecretKey;
import com.eayun.virtualization.model.BaseSecretkeyVm;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudSecretKey;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.service.CloudSecretKeyService;
import com.eayun.virtualization.service.VmService;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2017年2月27日
 */
@Service
@Transactional
public class CloudSecretKeyServiceImpl implements CloudSecretKeyService {
	private static final Logger log = LoggerFactory.getLogger(CloudBatchResourceServiceImpl.class);
    @Autowired
    private OpenstackSecretkeyService secretKey;
    @Autowired
    private FileService fileservice;
    @Autowired
    private CloudSecretKeyDao secretkeyDao;
    @Autowired
    private CloudSecretKeyVmDao secretkeyVmDao;
    @Autowired
    private ProjectService projectservice;
    @Autowired
    private EcmcSecretKeyService ecmcSecretKeyService;
    @Autowired
    private LogService logService;
    @Autowired
    private VmService vmService;
    
    @Override
    public boolean checkName(String prjId, String dcId, String secretkeyId, String name) throws Exception {
        boolean isExist = false;
        try {
            StringBuffer sql = new StringBuffer();
            int index = 0;
            Object[] args = new Object[4];
            sql.append("select skey.secretkey_id,skey.secretkey_name from cloud_secretkey skey where 1=1 ");
            //允许和本条数据重名
            if (!"".equals(secretkeyId) && secretkeyId != null) {
                sql.append(" and skey.secretkey_id <> ? ");
                args[index] = secretkeyId.trim();
                index++;
            }
            // 数据中心
            if (!"".equals(dcId) && dcId != null) {
                sql.append(" and skey.dc_id = ? ");
                args[index] = dcId;
                index++;
            }
            // 项目
            if (!"".equals(prjId) && prjId != null) {
                sql.append(" and skey.prj_id = ? ");
                args[index] = prjId;
                index++;
            }
            // 名称
            if (!"".equals(name) && name != null) {
                sql.append("and binary skey.secretkey_name = ? ");
                args[index] = name.trim();
                index++;
            }
            Object[] params = new Object[index];
            System.arraycopy(args, 0, params, 0, index);
            javax.persistence.Query query = secretkeyDao.createSQLNativeQuery(sql.toString(), params);
            List listResult = query.getResultList();
    
            if (listResult.size() > 0) {
                isExist = true;// 返回true 代表存在此名称
            }
        } catch (Exception e) {
            throw e;
        }
        return isExist;
    }
    
    @Override
    public Page getSecretKeyList(Page page, String prjId, String dcId, String name, QueryMap queryMap)
            throws Exception {
        int index = 0;
        Object[] args = new Object[3];
        StringBuffer sql = new StringBuffer();
        sql.append("select skey.secretkey_id,skey.fingerprint,skey.secretkey_name,skey.secretkey_desc,skey.create_time,");
        sql.append(" (SELECT COUNT(sv_id) from secretkey_vm where secretkey_id = skey.secretkey_id) as countnum from cloud_secretkey skey where 1=1 ");
        if (!"null".equals(prjId) && null != prjId && !"".equals(prjId)) {
            sql.append(" and skey.prj_id = ? ");
            args[index] = prjId;
            index++;
        }
        if (!"null".equals(dcId) && null != dcId && !"".equals(dcId)) {
            sql.append(" and skey.dc_id = ? ");
            args[index] = dcId;
            index++;
        }
        if (null != name && !"".equals(name)) {
            sql.append(" and binary skey.secretkey_name like ?");
            args[index] = "%" + name + "%";
            index++;
        }
        sql.append(" order by skey.create_time desc ");

        Object[] params = new Object[index];
        System.arraycopy(args, 0, params, 0, index);

        page = secretkeyDao.pagedNativeQuery(sql.toString(), queryMap, params);
        List newList = (List) page.getResult();
        Object[] objs = null;
        CloudSecretKey secretkey = null;
        for (int i = 0; i < newList.size(); i++) {
            objs = (Object[]) newList.get(i);
            secretkey = new CloudSecretKey();
            secretkey.setSecretkeyId(String.valueOf(objs[0]));
            secretkey.setFingerPrint(String.valueOf(objs[1]));
            secretkey.setSecretkeyName(String.valueOf(objs[2]));
            secretkey.setSecretkeyDesc(String.valueOf(objs[3]));
            secretkey.setCreateTime((Date)objs[4]);
            secretkey.setCountnum(String.valueOf(objs[5]));
            
            newList.set(i, secretkey);
        }
        return page;
    }
    
    @Override
    public CloudSecretKey create(String datacenterId, String projectId, Map<String, String> parmes,String cusId) throws AppException {
        CloudProject cp = projectservice.findProject(projectId);
        int usednum = getAllSecretkey(projectId);
        if(usednum>=cp.getSshKeyCount()){
            throw new AppException("SSH密钥数量配额不足");
        }
        CloudSecretKey secretkey = null;
        BaseCloudSecretKey basesecretkey = null;
        KeyPairs keypairs = null;
        InputStream myIn = null;
        try {
            if(checkName(projectId, datacenterId, null, parmes.get("name"))){
                throw new AppException("SSH密钥名称重复");
            }
            String uuid = UUID.randomUUID().toString().replace("-", "");
            JSONObject data = new JSONObject();
            data.put("name", uuid);
            if(parmes.get("publicKey")!=null && !"".equals(parmes.get("publicKey"))){
                data.put("public_key", parmes.get("publicKey"));
            }
            data.put("type", "ssh");
            data.put("user_id", projectId);
            JSONObject resultData = new JSONObject();
            resultData.put("keypair", data);
            keypairs = secretKey.create(datacenterId, projectId, resultData);
            String privateKeyFileId = "";
            if(keypairs!=null && keypairs.getPrivate_key()!=null && !"".equals(keypairs.getPrivate_key())){//把私钥上传成文件
                myIn = new ByteArrayInputStream(keypairs.getPrivate_key().getBytes());
                MultipartFile mfile = new MockMultipartFile(keypairs.getName(),parmes.get("name")+".pem",".pem", myIn);
                keypairs.setPrivate_key(privateKeyFileId);//清除私钥
                privateKeyFileId = fileservice.uploadFile(mfile, cusId);//上传私钥到文件服务器
                keypairs.setPrivate_key(privateKeyFileId);//私钥设置为文件ID
                Timer timer = new Timer();  //定时任务
                final String fileId = privateKeyFileId;
                timer.schedule(new TimerTask() {  
                    public void run() {  
                        try {
                            deleteSecretKeyFile(fileId);
                        } catch (Exception e) {
                            log.info("删除私钥文件出现异常："+e.getMessage());
                        } 
                    }  
                }, 10*60*1000);// 设定指定的时间time,此处为10分钟  
            }
            if(keypairs!=null){
                basesecretkey = new BaseCloudSecretKey();
                secretkey = new CloudSecretKey();
                //保存的数据
                basesecretkey.setSecretkeyId(uuid);
                basesecretkey.setSecretkeyName(parmes.get("name"));
                basesecretkey.setPublicKey(keypairs.getPublic_key().replace("\n", ""));
                basesecretkey.setDcId(datacenterId);
                basesecretkey.setPrjId(projectId);
                basesecretkey.setFingerPrint(keypairs.getFingerprint());
                basesecretkey.setSecretkeyDesc("");
                basesecretkey.setCreateTime(new Date());
                secretkeyDao.saveEntity(basesecretkey);
                //返回的数据
                secretkey.setSecretkeyId(uuid);
                secretkey.setSecretkeyName(parmes.get("name"));
                secretkey.setPublicKey(keypairs.getPublic_key().replace("\n", ""));
                secretkey.setDcId(datacenterId);
                secretkey.setPrjId(projectId);
                secretkey.setFingerPrint(keypairs.getFingerprint());
                secretkey.setSecretkeyDesc("");
                secretkey.setCreateTime(basesecretkey.getCreateTime());
                secretkey.setPrivateKeyFileId(keypairs.getPrivate_key());
            }
        } catch (AppException e) {
            if(keypairs!=null){
                secretKey.delete(datacenterId, projectId, keypairs.getName());//出现异常删除底层的创建
            }
            throw e;
        }catch (Exception e) {
            if(keypairs!=null){
                secretKey.delete(datacenterId, projectId, keypairs.getName());//出现异常删除底层的创建
            }
            throw new AppException("创建密钥失败:"+e.getMessage());
        }finally {
            if(myIn!=null){
                try {
                    myIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return secretkey;
    }
    
    @Override
    public InputStream downloadFile(String fileId) throws Exception {
        return fileservice.downloadFile(fileId);
    }
    
    @Override
    public boolean deleteSecretKeyFile(String fileId) throws Exception {
        return fileservice.deleteFile(fileId);
    }
    
    @Override
    public String getVmUserData(String datacenterId, String projectId, String vm_id) throws AppException {
        return secretKey.getVmUserData(datacenterId, projectId, vm_id);
    }

    @Override
    public boolean update(String secretkeyId,String name,String desc) throws AppException {
        try {
            BaseCloudSecretKey secretkey = secretkeyDao.findOne(secretkeyId);
            if(name!=null){
                if(checkName(secretkey.getPrjId(), secretkey.getDcId(), secretkeyId, name)){
                    throw new AppException("SSH密钥名称重复");
                }
                secretkey.setSecretkeyName(name);
            }
            secretkey.setSecretkeyDesc(desc);
            
            secretkeyDao.saveOrUpdate(secretkey);
            return true;
        } catch (Exception e) {
            throw new AppException("修改密钥失败："+e.getMessage());
        }
    }
    
    @Override
    public boolean delete(String dcId,String prjId,String secretkeyId) throws AppException {
    	boolean reFag=false;
     	try {
             secretKey.delete(dcId, prjId, secretkeyId);
     	}catch (Exception e) {
        }
     	try{
            secretkeyDao.delete(secretkeyId);
            reFag= true;
        }catch (Exception e) {
             throw new AppException("删除密钥失败："+e.getMessage());
        }
     	return reFag;
    }

	@Override
	public String getById(String secretkeyId) throws AppException {
		List list=new ArrayList<>();
		list.add(ecmcSecretKeyService.getSrcretKeyById(secretkeyId));
		return JSONObject.toJSONString(list);
	}

	@Override
	public List getUnbindKeyVmList(String secretkeyId, String prjId) throws Exception {
	    StringBuffer sql=new StringBuffer();
        sql.append("select v.vm_id,v.vm_name,net.subnet_name,flo.flo_ip,v.vm_ip,v.prj_id,v.dc_id,net.subnet_type,selfsubnet.subnet_name as sbname,v.self_ip,v.vm_status,net.route_id"
                +"  from cloud_vm v "
                +"  left join cloud_subnetwork net on v.subnet_id=net.subnet_id "
                +"  left join cloud_floatip flo on flo.resource_id=v.vm_id"
                +"  left join cloud_subnetwork selfsubnet on selfsubnet.subnet_id = v.self_subnetid"
                +"  where  v.is_deleted='0' and v.charge_state='0' and v.is_visable = '1' and v.os_type = '0007002002002' " 
                +" and v.vm_status != 'ERROR' and v.vm_status != 'SUSPENDED' and v.charge_state != '1' and v.charge_state != '2' and v.vm_status != 'SOFT_DELETED' and "
                +" v.prj_id= ? and v.vm_id not in(select vm_id from secretkey_vm where secretkey_id = ?)");
        Object [] obj={prjId,secretkeyId};
        List<Map> listdate=new ArrayList<>();
        List list = secretkeyDao.createSQLNativeQuery(sql.toString(), obj).getResultList();
    
        for(int i=0;i<list.size();i++){
            Map<String, Object> map=new HashMap<String,Object>();
             
            Object [] objdata=(Object[]) list.get(i);
            map.put("vmid", objdata[0]==null?"":objdata[0]);
            map.put("vmname", objdata[1]==null?"":objdata[1]);
            map.put("subnetname", objdata[2]==null?"":objdata[2]);
            map.put("floip", objdata[3]==null?"":objdata[3]);
            map.put("vmip", objdata[4]==null?"":objdata[4]);
            map.put("prjid", objdata[5]==null?"":objdata[5]);
            map.put("dcid", objdata[6]==null?"":objdata[6]);
            map.put("subnettype",objdata[7]==null?"":objdata[7] );
            map.put("suName",objdata[8]==null?"":objdata[8] );
            map.put("seleip",objdata[9]==null?"":objdata[9] );
            map.put("status",objdata[10]==null?"":objdata[10] );
            map.put("routeId",objdata[11]==null?"":objdata[11] );
            listdate.add(map);
        }
        return listdate;
	}
	
	@Override
	public List getBindKeyVmList(String secretkeyId, String prjId) throws Exception {
	    StringBuffer sql=new StringBuffer();
        sql.append("select v.vm_id,v.vm_name,net.subnet_name,flo.flo_ip,v.vm_ip,v.prj_id,v.dc_id,net.subnet_type,selfsubnet.subnet_name as sbname,v.self_ip,v.vm_status,net.route_id"
                +"  from cloud_vm v "
                +"  left join cloud_subnetwork net on v.subnet_id=net.subnet_id"
                +"  left join cloud_floatip flo on flo.resource_id=v.vm_id"
                +"  left join cloud_subnetwork selfsubnet on selfsubnet.subnet_id = v.self_subnetid"
                +"  where  v.is_deleted='0' and v.charge_state='0' and v.is_visable = '1' and v.os_type = '0007002002002' " 
                +" and v.vm_status != 'ERROR' and v.vm_status != 'SUSPENDED' and v.charge_state != '1' and v.charge_state != '2' and v.vm_status != 'SOFT_DELETED' and "
                +" v.prj_id= ? and v.vm_id in(select vm_id from secretkey_vm where secretkey_id = ?)");
        Object [] obj={prjId,secretkeyId};
        List<Map> listdate=new ArrayList<>();
        List list = secretkeyDao.createSQLNativeQuery(sql.toString(), obj).getResultList();
    
        for(int i=0;i<list.size();i++){
            Map<String, Object> map=new HashMap<String,Object>();
             
            Object [] objdata=(Object[]) list.get(i);
            map.put("vmid", objdata[0]==null?"":objdata[0]);
            map.put("vmname", objdata[1]==null?"":objdata[1]);
            map.put("subnetname", objdata[2]==null?"":objdata[2]);
            map.put("floip", objdata[3]==null?"":objdata[3]);
            map.put("vmip", objdata[4]==null?"":objdata[4]);
            map.put("prjid", objdata[5]==null?"":objdata[5]);
            map.put("dcid", objdata[6]==null?"":objdata[6]);
            map.put("subnettype",objdata[7]==null?"":objdata[7] );
            map.put("suName",objdata[8]==null?"":objdata[8] );
            map.put("seleip",objdata[9]==null?"":objdata[9] );
            map.put("status",objdata[10]==null?"":objdata[10] );
            map.put("routeId",objdata[11]==null?"":objdata[11] );
            listdate.add(map);
        }
        return listdate;
	}
	
	@Override
	public List<BaseCloudSecretKey> getunBindVmSecretkey(String vmId, String prjId) throws Exception {
	    StringBuffer sql=new StringBuffer();
	    sql.append("select key.secretkey_id,key.secretkey_name from cloud_secretkey key "
	            + " key.prj_id = ? and key.secretkey_id not in(select secretkey_id from secretkey_vm where vm_id = ?) ");
	    
	    Object [] obj={prjId,vmId};
        List<BaseCloudSecretKey> list = secretkeyDao.createSQLNativeQuery(sql.toString(), obj).getResultList();
        return list;
	}
	
	@Override
	public List<BaseCloudSecretKey> getBindVmSecretkey(String vmId, String prjId) throws Exception {
	    StringBuffer sql=new StringBuffer();
        sql.append("select key.secretkey_id,key.secretkey_name from cloud_secretkey key "
                + " key.prj_id = ? and key.secretkey_id in(select secretkey_id from secretkey_vm where vm_id = ?) ");
        
        Object [] obj={prjId,vmId};
        List<BaseCloudSecretKey> list = secretkeyDao.createSQLNativeQuery(sql.toString(), obj).getResultList();
        return list;
	}
	
	@Override
	public Page getByIdAndVmlist(String secretkeyId,QueryMap queryMap) throws AppException {
	
		return ecmcSecretKeyService.getSrcretKeyByIdAndVmList(secretkeyId,queryMap);
	}
	
	@Override
    public String BindSecretkeyToVm(String dcId, String prjId, String secretkeyId, String vmId) throws AppException {
        String returndata = "";
        try{
            List<BaseSecretkeyVm> vmlist = secretkeyVmDao.getVmListBySecretkeyIdAndVmId(secretkeyId, vmId);
            if(vmlist!=null && vmlist.size()>0){
                throw new AppException("云主机已绑定该密钥");
            }
            BaseSecretkeyVm SecretkeyVm = null;
            BaseCloudSecretKey secretkey = secretkeyDao.findOne(secretkeyId);//查询出这个密钥的公钥
            //yaml转map
            Yaml yaml = new Yaml(); 
            String userdata = secretKey.getVmUserData(dcId, prjId, vmId);//获取当前云主机的userData
            if(userdata!=null && !"".equals(userdata)){
                Map<String, Object> map = (HashMap<String, Object>) yaml.load(userdata);
                List<String> ssh_authorized_keys = (ArrayList<String>)map.get("ssh_authorized_keys");//获取当前云主机已绑定的密钥
                if(ssh_authorized_keys!=null){
                    ssh_authorized_keys.add(secretkey.getPublicKey());
                }else{
                    ssh_authorized_keys = new ArrayList<>();
                    ssh_authorized_keys.add(secretkey.getPublicKey());
                }
                
                map.put("ssh_authorized_keys",ssh_authorized_keys);
                String maptostring = yaml.dumpAsMap(map);
                
                JSONObject data = new JSONObject();
                data.put("user_data", Base64Utils.encodeToString(maptostring.getBytes()));
                returndata = secretKey.bindSecretKey(dcId, prjId, vmId, data);//绑定密钥到云主机
                //上层添加关系
                SecretkeyVm = new BaseSecretkeyVm();
                SecretkeyVm.setVmId(vmId);
                SecretkeyVm.setSecretkeyId(secretkeyId);
                secretkeyVmDao.save(SecretkeyVm);
            }
        }catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException("云主机绑定密钥失败："+e.getMessage());
        }
        return returndata;
    }
    @Override
    public String unBindSecretkeyToVm(String dcId, String prjId, String secretkeyId, String vmId) throws AppException {
        String returndata = "";
        try{
            BaseCloudSecretKey secretkey = secretkeyDao.findOne(secretkeyId);//查询出这个密钥的公钥
            //yaml转map
            Yaml yaml = new Yaml(); 
            String userdata = secretKey.getVmUserData(dcId, prjId, vmId);//获取当前云主机的userData
            Map<String, Object> map = (HashMap<String, Object>) yaml.load(userdata);
            List<String> ssh_authorized_keys_deleted = (ArrayList<String>)map.get("ssh_authorized_keys_deleted");//获取当前云主机需要删除的密钥
            if(ssh_authorized_keys_deleted!=null){
                ssh_authorized_keys_deleted.add(secretkey.getPublicKey());
            }else{
                ssh_authorized_keys_deleted = new ArrayList<>();
                ssh_authorized_keys_deleted.add(secretkey.getPublicKey());
            }
            map.put("ssh_authorized_keys_deleted",ssh_authorized_keys_deleted);
            String maptostring = yaml.dumpAsMap(map);
            
            JSONObject data = new JSONObject();
            data.put("user_data", Base64Utils.encodeToString(maptostring.getBytes()));
            returndata = secretKey.bindSecretKey(dcId, prjId, vmId, data);//云主机删除密钥
            //解除上层关系
            secretkeyVmDao.deleteByVmId(vmId,secretkeyId);
        }catch (AppException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("云主机解绑密钥失败："+e.getMessage());
        }
        return returndata;
    }
    @Override
    public Map<String, String> BindSecretkeyToVm(String dcId,String prjId,String secretkeyId, List<String> vmIds) throws AppException {
        Map<String, String> returnmap = new HashMap<>();
        String returndata = "";
        int successnum = 0;//添加个数
        int errornum = 0;//失败个数
        try{
            BaseCloudSecretKey secretkey = secretkeyDao.findOne(secretkeyId);//查询出这个密钥的公钥
            List<BaseSecretkeyVm> vmlist = secretkeyVmDao.getVmListByskId(secretkeyId);//查询密钥下已绑定的云主机ID
            List<String> oldVmId = new ArrayList<>();//旧的云主机
            for(BaseSecretkeyVm vm : vmlist){
                oldVmId.add(vm.getVmId());
            }
            List<String> repeatVmId = new ArrayList<>();
            //yaml转map
            Yaml yaml = new Yaml(); 
            for(String vmId : vmIds){
                for(BaseSecretkeyVm vm : vmlist){
                    if(vm.getVmId().equals(vmId)){
                        repeatVmId.add(vmId);//重复的云主机可以不变
                    }
                }
            }
            vmIds.removeAll(repeatVmId);//新的云主机去掉重复的表示要新增的
            oldVmId.removeAll(repeatVmId);//旧的云主机去掉重复的表示要删除的
            BaseSecretkeyVm SecretkeyVm = null;
            Map<String, Object> map = null;
            List<String> ssh_authorized_keys = null;
            JSONObject data = null;
            String userdata = null;
            int i=0;
            for(String vmId : vmIds){
                CloudVm cloudVm = vmService.queryRouteByVm(vmId);
                if(cloudVm!=null){
                    if(!"SHUTOFF".equals(cloudVm.getVmStatus())){
                        errornum++;
                        continue;
                    }
                    if(cloudVm.getRouteId()==null || "".equals(cloudVm.getRouteId())){
                        errornum++;
                        continue;
                    }
                }else{
                    errornum++;
                    continue;
                }
                userdata = secretKey.getVmUserData(dcId, prjId, vmId);//获取当前云主机的userData
                if(userdata!=null && !"".equals(userdata)){
                    map = (HashMap<String, Object>) yaml.load(userdata);//这儿用替换是因为，不用转换会报错。
                    ssh_authorized_keys = (ArrayList<String>)map.get("ssh_authorized_keys");//获取当前云主机已绑定的密钥
                    if(ssh_authorized_keys!=null){
                        ssh_authorized_keys.add(secretkey.getPublicKey());
                    }else{
                        ssh_authorized_keys = new ArrayList<>();
                        ssh_authorized_keys.add(secretkey.getPublicKey());
                    }
                    map.put("ssh_authorized_keys",ssh_authorized_keys);
                    String maptostring = yaml.dumpAsMap(map);//转成yaml格式
                    
                    data = new JSONObject();
                    data.put("user_data", Base64Utils.encodeToString(maptostring.getBytes()));
                    try{
                        returndata = secretKey.bindSecretKey(dcId, prjId, vmId, data);//绑定密钥到云主机
                        if(returndata!=null && !"".equals(returndata)){
                            successnum++;
                        }else{
                            errornum++;
                        }
                      //上层添加关系
                        SecretkeyVm = new BaseSecretkeyVm();
                        SecretkeyVm.setVmId(vmId);
                        SecretkeyVm.setSecretkeyId(secretkeyId);
                        secretkeyVmDao.save(SecretkeyVm);
                    }catch(Exception e){
                        errornum++;
                    }
                    if(i==0){
                        logService.addLog("绑定云主机", ConstantClazz.LOG_TYPE_KEYPAIRS, secretkey.getSecretkeyName(), prjId, ConstantClazz.LOG_STATU_SUCCESS, null);
                    }
                    i++;
                }else{
                    errornum++;
                }
            }
            List<String> ssh_authorized_keys_deleted = null;
            int j=0;
            for(String vmId : oldVmId){
                CloudVm cloudVm = vmService.queryRouteByVm(vmId);
                if(cloudVm!=null){
                    if(!"SHUTOFF".equals(cloudVm.getVmStatus())){
                        errornum++;
                        continue;
                    }
                    if(cloudVm.getRouteId()==null && "".equals(cloudVm.getRouteId())){
                        errornum++;
                        continue;
                    }
                }else{
                    errornum++;
                    continue;
                }
                userdata = secretKey.getVmUserData(dcId, prjId, vmId);//获取当前云主机的userData
                if(userdata!=null && !"".equals(userdata)){
                    map = (HashMap<String, Object>) yaml.load(userdata);
                    ssh_authorized_keys_deleted = (ArrayList<String>)map.get("ssh_authorized_keys_deleted");//获取当前云主机需要删除的密钥
                    if(ssh_authorized_keys_deleted!=null){
                        ssh_authorized_keys_deleted.add(secretkey.getPublicKey());
                    }else{
                        ssh_authorized_keys_deleted = new ArrayList<>();
                        ssh_authorized_keys_deleted.add(secretkey.getPublicKey());
                    }
                    data = new JSONObject();
                    map.put("ssh_authorized_keys_deleted",ssh_authorized_keys_deleted);
                    String maptostring = yaml.dumpAsMap(map);
                    
                    data.put("user_data", Base64Utils.encodeToString(maptostring.getBytes()));
                    try{
                        returndata = secretKey.bindSecretKey(dcId, prjId, vmId, data);//云主机删除密钥
                        if(returndata!=null && !"".equals(returndata)){
                            successnum++;
                        }else{
                            errornum++;
                        }
                        //解除上层关系
                        secretkeyVmDao.deleteByVmId(vmId,secretkeyId);
                    }catch(Exception e){
                        errornum++;
                    }
                    if(j==0){
                        logService.addLog("解绑云主机", ConstantClazz.LOG_TYPE_KEYPAIRS, secretkey.getSecretkeyName(), prjId, ConstantClazz.LOG_STATU_SUCCESS, null);
                    }
                    j++;
                }else{
                    errornum++;
                }
            }
        }catch (AppException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("云主机绑定解绑密钥失败："+e.getMessage());
        }
        returnmap.put("successnum", successnum+"");
        returnmap.put("errornum", errornum+"");
        return returnmap;
    }

	@Override
	public Boolean CanceSecretkeyToVm(String dcId, String prjId, String vmId) throws AppException {
		Boolean retuFag=true;
		List<BaseSecretkeyVm> list = secretkeyVmDao.getVmListByvmId(vmId);
		List<String >listskId =new ArrayList<>();
		for (BaseSecretkeyVm vmlist : list) {
			if (null != vmlist) {
				String secretkeyId = vmlist.getSecretkeyId();
				listskId.add(secretkeyId);

			}
		}
		
		this.BindVmToSecretkey(dcId,prjId,vmId,listskId);
		return retuFag;
	}
    
	@Override
	public String BindVmToSecretkey(String dcId, String prjId, String vmId, List<String> secretkeyIds)
	        throws AppException {
	    String returndata = "";
        try{
            //yaml转map
            Yaml yaml = new Yaml(); 
            List<BaseSecretkeyVm> vmlist = secretkeyVmDao.getVmListByvmId(vmId);//查询云主机下的密钥
            List<String> oldSecretkeyId = new ArrayList<>();//旧的密钥ID
            for(BaseSecretkeyVm vm : vmlist){
                oldSecretkeyId.add(vm.getSecretkeyId());
            }
            List<String> repeatSecretkeyId = new ArrayList<>();//重复的密钥
            for(String secretkeyId : secretkeyIds){
                for(BaseSecretkeyVm vm : vmlist){
                    if(vm.getSecretkeyId().equals(secretkeyId)){
                        repeatSecretkeyId.add(secretkeyId);//重复的密钥可以不变
                    }
                }
            }
            secretkeyIds.removeAll(repeatSecretkeyId);//新的密钥去掉重复的表示要新增的
            oldSecretkeyId.removeAll(repeatSecretkeyId);//旧的密钥去掉重复的表示要删除的
            BaseSecretkeyVm SecretkeyVm = null;
            //云主机新增密钥
            String userdata = secretKey.getVmUserData(dcId, prjId, vmId);//获取当前云主机的userData
            Map<String, Object> map = (HashMap<String, Object>) yaml.load(userdata);
            List<String> ssh_authorized_keys = (ArrayList<String>)map.get("ssh_authorized_keys");//获取当前云主机已绑定的密钥
            BaseCloudSecretKey cloudSecretKey = new BaseCloudSecretKey();
            if(ssh_authorized_keys==null){
                ssh_authorized_keys = new ArrayList<>();
            }
            for(String secretkeyId : secretkeyIds){
                cloudSecretKey = secretkeyDao.findOne(secretkeyId);
                ssh_authorized_keys.add(cloudSecretKey.getPublicKey());
                //上层添加关系
                SecretkeyVm = new BaseSecretkeyVm();
                SecretkeyVm.setVmId(vmId);
                SecretkeyVm.setSecretkeyId(secretkeyId);
                secretkeyVmDao.save(SecretkeyVm);
            }
            
            //云主机删除密钥
            List<String> ssh_authorized_keys_deleted = (ArrayList<String>)map.get("ssh_authorized_keys_deleted");//获取当前云主机需要删除的密钥
            if(ssh_authorized_keys_deleted==null){
                ssh_authorized_keys_deleted = new ArrayList<>();
            }
            for(String secretkeyId : oldSecretkeyId){
                cloudSecretKey = secretkeyDao.findOne(secretkeyId);
                ssh_authorized_keys_deleted.add(cloudSecretKey.getPublicKey());
                //解除上层关系
                secretkeyVmDao.deleteByVmId(vmId,secretkeyId);
            }
            map.put("ssh_authorized_keys",ssh_authorized_keys);
            map.put("ssh_authorized_keys_deleted",ssh_authorized_keys_deleted);
            String maptostring = yaml.dumpAsMap(map);
            
            JSONObject data = new JSONObject();
            data.put("user_data", Base64Utils.encodeToString(maptostring.getBytes()));
            returndata = secretKey.bindSecretKey(dcId, prjId, vmId, data);//云主机删除密钥
            
        }catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException("密钥绑定解绑云主机失败："+e.getMessage());
        }
        return returndata;
	}
	
	@Override
	public String unBindSecretkeyForVm(String dcId, String prjId, String vmId) throws AppException {
	    String returndata = "";
        try{
            //yaml转map
            Yaml yaml = new Yaml(); 
            List<BaseSecretkeyVm> vmlist = secretkeyVmDao.getVmListByvmId(vmId);//查询云主机下的密钥
            List<String> oldSecretkeyId = new ArrayList<>();//旧的密钥ID
            for(BaseSecretkeyVm vm : vmlist){
                oldSecretkeyId.add(vm.getSecretkeyId());
            }
            String userdata = secretKey.getVmUserData(dcId, prjId, vmId);//获取当前云主机的userData
            Map<String, Object> map = (HashMap<String, Object>) yaml.load(userdata);
            List<String> ssh_authorized_keys_deleted = (ArrayList<String>)map.get("ssh_authorized_keys_deleted");//获取当前云主机需要删除的密钥
            if(ssh_authorized_keys_deleted==null){
                ssh_authorized_keys_deleted = new ArrayList<>();
            }
            BaseCloudSecretKey cloudSecretKey = null;
            for(String secretkeyId : oldSecretkeyId){
                cloudSecretKey = secretkeyDao.findOne(secretkeyId);
                ssh_authorized_keys_deleted.add(cloudSecretKey.getPublicKey());
                //解除上层关系
                secretkeyVmDao.deleteByVmId(vmId,secretkeyId);
            }
            map.put("ssh_authorized_keys_deleted",ssh_authorized_keys_deleted);
            String maptostring = yaml.dumpAsMap(map);
            
            JSONObject deletedata = new JSONObject();
            deletedata.put("user_data", Base64Utils.encodeToString(maptostring.getBytes()));
            returndata = secretKey.bindSecretKey(dcId, prjId, vmId, deletedata);//云主机删除密钥
        }catch (AppException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("云主机解绑所有密钥失败："+e.getMessage());
        }
        return returndata;
	}
	
	@Override
	public List<BaseCloudSecretKey> getAllSecretkey(String dcId, String prjId) throws Exception {
	    return secretkeyDao.getBaseCloudSecretKeyList(dcId, prjId);
	}
	
	@Override
	public int getAllSecretkey(String prjId) {
	    return secretkeyDao.countSecretKeyByPrjId(prjId);
	}
	
	@Override
	public EayunFile getFileBean(String fileId) {
	    return fileservice.findOneById(fileId);
	}
	
	@Override
	public BaseCloudSecretKey getSecretKeyById(String secretKeyId){
		return secretkeyDao.findOne(secretKeyId);
	}
}
