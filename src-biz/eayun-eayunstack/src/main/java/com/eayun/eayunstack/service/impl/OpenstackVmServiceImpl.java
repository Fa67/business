package com.eayun.eayunstack.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.CloudResourceUtil;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Flavor;
import com.eayun.eayunstack.model.Image;
import com.eayun.eayunstack.model.InterfaceAttachment;
import com.eayun.eayunstack.model.Port;
import com.eayun.eayunstack.model.SecurityGroup;
import com.eayun.eayunstack.model.Vm;
import com.eayun.eayunstack.model.Volume;
import com.eayun.eayunstack.service.OpenstackVmService;
import com.eayun.eayunstack.service.OpenstackVolumeService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudFlavor;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudVm;
import com.sun.mail.util.BASE64EncoderStream;

@Service
public class OpenstackVmServiceImpl extends OpenstackBaseServiceImpl<Vm>
		implements OpenstackVmService {

	private static final Logger log = LoggerFactory
			.getLogger(OpenstackVmServiceImpl.class);

	@Autowired
	private OpenstackVolumeService openstackVolumeService;
	
	/**
	 * 创建模板
	 * --------------
	 * @author zhouhaitao
	 * @param cloudFlavor
	 * 			云主机的模板信息
	 * @return
	 * 
	 * @throws AppException
	 */
	public Flavor createFlavor(BaseCloudFlavor cloudFlavor) throws AppException{
		log.info("创建云主机模板");
		Flavor flavor = null;
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("name", cloudFlavor.getFlavorName());
		temp.put("vcpus", cloudFlavor.getFlavorVcpus());
		temp.put("ram", cloudFlavor.getFlavorRam());
		temp.put("disk", cloudFlavor.getFlavorDisk());
		data.put("flavor", temp);

		RestTokenBean restTokenBean = getRestTokenBean(cloudFlavor.getDcId(), cloudFlavor.getPrjId(),
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		
		restTokenBean.setUrl(OpenstackUriConstant.FLAVOR_URI);
		JSONObject result = restService.create(restTokenBean, "flavor", data);
		if (result != null) {
			flavor = restService.json2bean(result, Flavor.class);
		}
		return flavor;
	}
	
	
	/**
	 * 创建云主机
	 * -----------------
	 * @author zhouhaitao
	 * @param cloudVm 
	 * 			           创建云主机的信息
	 * @param flavorId 
	 * 				云主机类型Id
	 * @param vmList
	 * 				创建成功的云主机列表
	 * @return
	 * 
	 * @throws AppException
	 */
	@Override
	public String createVm(CloudVm cloudVm,String flavorId,List<Vm> vmList) throws AppException {
		log.info("创建云主机");
		String errMsg  = null;
		String userData = "";
		JSONObject object = new JSONObject();
		JSONObject port = new JSONObject();
		
		int number =cloudVm.getNumber();
		object.put("flavorRef", flavorId);
		try{
			RestTokenBean restTokenBean = getRestTokenBean(cloudVm.getDcId(), cloudVm.getPrjId(),
					OpenstackUriConstant.COMPUTE_SERVICE_URI);
			
			if(ConstantClazz.DICT_CLOUD_OS_LINUX_NODE_ID.equals(cloudVm.getOsType())){
				if(ConstantClazz.VM_LOGIN_TYPE_PWD.equals(cloudVm.getLoginType())){
					userData =  linuxPwd(cloudVm);
					log.info("*********************云主机的密码为："+cloudVm.getPassword());
				}
				else if(ConstantClazz.VM_LOGIN_TYPE_SSH.equals(cloudVm.getLoginType())){
					userData =  linuxSSH(cloudVm);
					log.info("*********************云主机的PUBLIC_KEY为："+cloudVm.getSecretPublicKey());
				}
			}
			
			for (int i = 1; i < number + 1; i++) {
				Vm vm = null;
				Port portData = null;
				if (vmList == null) {
					vmList = new ArrayList<Vm>();
				}
				if(!StringUtils.isEmpty(cloudVm.getSubnetId())){
					portData = createPort(cloudVm.getDcId(),cloudVm.getPrjId(),cloudVm.getNetId(),cloudVm.getSubnetId(),new String []{cloudVm.getSgId()});
					port.put("port", portData.getId());
				}
				
				JSONObject[] nets = {port};
				object.put("networks", nets);
				
				if (number > 1) {
					object.put("name", cloudVm.getVmName()+"_"+ i);
				} else {
					object.put("name", cloudVm.getVmName());
				}
				JSONObject  volume = new JSONObject ();
				volume .put("source_type", "image");
				volume .put("destination_type", "volume");
				volume .put("delete_on_termination", "true");
				volume .put("uuid", cloudVm.getFromImageId());
				volume .put("volume_size", cloudVm.getDisks());
				volume .put("boot_index", "0");
				
				JSONObject [] volumes = {volume};
				object.put("block_device_mapping_v2", volumes);
				if(ConstantClazz.DICT_CLOUD_OS_LINUX_NODE_ID.equals(cloudVm.getOsType())){
					object.put("user_data", userData);
				}
				else if (ConstantClazz.DICT_CLOUD_OS_WINDOWS_NODE_ID.equals(cloudVm.getOsType())){
					JSONObject win = new JSONObject();
					win.put("admin_pass", cloudVm.getPassword());
					object.put("metadata", win);
				}
				JSONObject data = new JSONObject();
				data.put("server", object);
				
				restTokenBean.setUrl(OpenstackUriConstant.VM_BOOT_URI);
				JSONObject result = restService.create(restTokenBean,
						OpenstackUriConstant.VM_DATA_NAME, data);
				log.info("创建返回云主机结果："+result);
				if (result != null) {
					try{
						vm = restService.json2bean(result, Vm.class);
					}catch(Exception e){
						vm.setId(result.getString("id"));
						e.printStackTrace();
						throw e;
					}finally{
						vm.setPortId(portData.getId());
						vm.setIp(portData.getFixed_ips()[0].getIp_address());
						vmList.add(vm);
					}
				}
			}
		}catch(AppException e){
		    log.error(e.getMessage(),e);
		    errMsg = e.getMessage();
			throw e;
		}
		catch(Exception e){
		    log.error(e.getMessage(),e);
		    errMsg = e.getMessage();
			throw new AppException(e.getMessage());
		}
		return errMsg;
	}
	
	/**
	 * 创建子网对应的端口
	 * @param cloudVm
	 * @return
	 */
	private Port createPort(String dcId,String prjId,String netId,String subnetId,String[] sgIds){
		Port port = null;
		JSONObject data  = new JSONObject ();
		JSONObject portJson  = new JSONObject ();
		JSONObject subnet  = new JSONObject ();
		subnet.put("subnet_id",subnetId );
		JSONObject subnets [] ={subnet};
		portJson.put("fixed_ips",subnets );
		portJson.put("network_id",netId );
		portJson.put("admin_state_up","true" );
		portJson.put("security_groups",sgIds );
		data.put("port", portJson);
		
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.PORT_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.PORT_DATA_NAME, data);
		
		if(result!=null){
			port = restService.json2bean(result, Port.class);
		}
		return port ;
	}
	
	/**
	 * Linux 设置指定用户的密码 
	 * @param cloudVm
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	private String linuxPwd(CloudVm cloudVm) throws UnsupportedEncodingException{
		String cloudPwd = CloudResourceUtil.handleLinuxPwd(cloudVm.getPassword());
		
		StringBuffer cloudConfig = new StringBuffer();
		cloudConfig.append("#cloud-config\n");
		cloudConfig.append("user: ").append(cloudVm.getUsername()).append("\n");
		cloudConfig.append("password: ").append(cloudPwd).append("\n");
		cloudConfig.append("chpasswd: ").append("\n");
		cloudConfig.append(" expire: false\n");
		
		byte [] bytes = BASE64EncoderStream.encode(cloudConfig.toString().getBytes("utf-8"));
		String str = new String(bytes);
		return str;
	}
	
	/**
	 * Linux 设置主机的SSH密钥
	 * @param cloudVm
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	private String linuxSSH(CloudVm cloudVm) throws UnsupportedEncodingException{
		StringBuffer cloudConfig = new StringBuffer();
		cloudConfig.append("#cloud-config\n");
		cloudConfig.append("user: ").append(cloudVm.getUsername()).append("\n");
		cloudConfig.append("chpasswd: ").append("\n");
		cloudConfig.append(" expire: false\n");
		cloudConfig.append("ssh_authorized_keys:").append("\n");
		cloudConfig.append("- ").append(cloudVm.getSecretPublicKey()).append("\n");
		
		byte [] bytes = BASE64EncoderStream.encode(cloudConfig.toString().getBytes("utf-8"));
		String str = new String(bytes,"UTF-8");
		return str;
	}
	
	/**
	 * 编辑云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public boolean modifyVm(CloudVm cloudVm) throws AppException{
		JSONObject edit = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("name", cloudVm.getVmName());
		edit.put("server", temp);

		RestTokenBean restTokenBean = getRestTokenBean(cloudVm.getDcId(), cloudVm.getPrjId(),
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.VM_URI + "/" + cloudVm.getVmId());

		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.VM_DATA_NAME, edit);
		if (result != null) {
			return true;
		}

		return false;
	}
	
	/**
	 * 软删除云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @throws AppException
	 */
	public boolean softDeleteVm(CloudVm cloudVm) throws AppException{
		RestTokenBean restTokenBean = getRestTokenBean(cloudVm.getDcId(), cloudVm.getPrjId(),
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		
		restTokenBean.setUrl(OpenstackUriConstant.VM_URI + "/" + cloudVm.getVmId());
		
		return restService.delete(restTokenBean);

	
	}
	
	/**
	 * 启动云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean restartVm(CloudVm cloudVm) throws AppException{
		JSONObject data = new JSONObject();
		data.put("os-start", "null");

		return operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), data) != null;
	}
	
	/**
	 * 关闭云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean shutdownVm(CloudVm cloudVm) throws AppException{
		JSONObject data = new JSONObject();
		data.put("os-stop", "null");

		return operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), data) != null;
	}
	
	/**
	 * 软重启云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean softRestartVm(CloudVm cloudVm) throws AppException{
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("type", "SOFT");
		data.put("reboot", temp);
		return operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), data) != null;
	}
	
	/**
	 * 硬重启云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean hardRestartVm(CloudVm cloudVm) throws AppException{
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("type", "HARD");
		data.put("reboot", temp);
		return operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), data) != null;
	}
	
	/**
	 * 创建自定义镜像
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return 
	 * 
	 * @throws AppException
	 */
	public Image createSnapshot(CloudVm cloudVm) throws AppException{
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("name", cloudVm.getImageName());
		data.put("createImage", temp);

		operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), data);

		RestTokenBean restTokenBean = getRestTokenBean(cloudVm.getDcId(), cloudVm.getPrjId(),
				OpenstackUriConstant.IMAGE_SERVICE_URI);
		
		restTokenBean.setUrl(OpenstackUriConstant.IMAGE_URI);
		
		List<JSONObject> list = restService.list(restTokenBean,
				OpenstackUriConstant.IMAGE_DATA_NAMES);
		for (JSONObject jsonObject : list) {
			Image image = restService.json2bean(jsonObject, Image.class);
			if (cloudVm.getVmId().equals(image.getInstance_uuid())&&cloudVm.getImageName().equals(image.getName())) {
				return image;
			}
		}
		return null;
	}
	
	/**
	 * 挂起云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean suspendVm(CloudVm cloudVm) throws AppException{
		JSONObject data = new JSONObject();
		data.put("suspend", "null");

		return operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), data) != null;
	}
	
	/**
	 * 恢复云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean resumeVm(CloudVm cloudVm) throws AppException{
		JSONObject data = new JSONObject();
		data.put("resume", "null");
		return operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), data) != null;
	}
	
	/**
	 * 重建云主机
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean rebuildVm(CloudVm cloudVm) throws AppException{
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("imageRef", cloudVm.getFromImageId());
		data.put("rebuild", temp);
		return operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), data) != null;
	}
	
	/**
	 * 调整云主机大小
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean resizeVm(CloudVm cloudVm) throws AppException{
		JSONObject resize = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("flavorRef", cloudVm.getResizeId());
		resize.put("resize", temp);

		return operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), resize) != null;
	}
	
	/**
	 * 确认调整云主机大小
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean confirmResizeVm(CloudVm cloudVm) throws AppException{
		JSONObject confirm = new JSONObject();
		confirm.put("confirmResize", "null");

		return operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), confirm) != null;
	}
	
	/**
	 * 取消调整云主机大小
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean revertResizeVm(CloudVm cloudVm) throws AppException{
		JSONObject confirm = new JSONObject();
		confirm.put("revertResize", "null");

		return operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), confirm) != null;
	}
	
	/**
	 * 打开云主机控制台
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public String consoleVm(CloudVm cloudVm) throws AppException{
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("type", "novnc");
		data.put("os-getVNCConsole", temp);

		JSONObject result = operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), data);

		return result.getJSONObject("console").getString("url");
	
	}
	
	/**
	 * 获取云主机日志
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public String getVmLogs(CloudVm cloudVm) throws AppException{
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("length", 50);
		data.put("os-getConsoleOutput", temp);
		
		JSONObject result = operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), data);
		
		return result.getString("output");
	}
	
	/**
	 * 编辑云主机安全组
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * @param adds
	 * @param dels
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean editVmSecurityGroup(CloudVm cloudVm,List<BaseCloudSecurityGroup> adds,List<BaseCloudSecurityGroup> dels) throws AppException{
		RestTokenBean restTokenBean = getRestTokenBean(cloudVm.getDcId(), cloudVm.getPrjId(),
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean
				.setUrl(OpenstackUriConstant.VM_URI + "/" + cloudVm.getVmId() + "/action");
	
		boolean addFlag = addVmSecurityGroup(restTokenBean,adds);
		boolean delFlag = removeVmSecurityGroup(restTokenBean,dels);
		return addFlag&&delFlag;
			
	}
	
	/**
	 * 新增云主机的安全组
	 * ------------------
	 * @author zhouhaitao
	 * @param restTokenBean
	 * @param adds
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean addVmSecurityGroup(RestTokenBean restTokenBean,List<BaseCloudSecurityGroup> adds) throws AppException{
		boolean flag= false;
		if(null!= adds&& adds.size()>0){
			for(BaseCloudSecurityGroup sg:adds){
				JSONObject data=new JSONObject();
				JSONObject temp=new JSONObject();
				temp.put("name", sg.getSgId());
				data.put("addSecurityGroup",temp);
				restService.operate(restTokenBean,data);
			}
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 删除关联云主机的安全组
	 * ------------------
	 * @author zhouhaitao
	 * @param restTokenBean
	 * @param dels
	 * 
	 * @return
	 * 
	 * @throws AppException
	 */
	public boolean removeVmSecurityGroup(RestTokenBean restTokenBean,List<BaseCloudSecurityGroup> dels) throws AppException{
		boolean flag= false;
		if(null!= dels&& dels.size()>0){
			for(BaseCloudSecurityGroup sg:dels){
				JSONObject data=new JSONObject();
				JSONObject temp=new JSONObject();
				temp.put("name", sg.getSgId());
				data.put("removeSecurityGroup",temp);
				restService.operate(restTokenBean,data);
			}
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 查询底层指定id的资源
	 * -----------------
	 * @author zhouhaitao
	 * @param dcId
	 * @param prjId
	 * @param vmId
	 */
	public JSONObject get(String dcId,String prjId,String vmId) throws Exception{
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		JSONObject json = restService.getJSONById(restTokenBean,OpenstackUriConstant.VM_URI + "/",vmId);

		return json;
	}
	
	/**
	 * 私有方法，用于将JSONObject对象中的一些无法自动转换的参数，手动设置到java对象中
	 * 
	 * @param vm
	 * @param object
	 */
	private void initData(Vm vm, JSONObject object) {
		vm.setDiskConfig(object.getString("OS-DCF:diskConfig"));
		vm.setAvailability_zone(object.getString("OS-EXT-AZ:availability_zone"));
		vm.setHost(object.getString("OS-EXT-SRV-ATTR:host"));
		vm.setHypervisor_hostname(object
				.getString("OS-EXT-SRV-ATTR:hypervisor_hostname"));
		vm.setInstance_name(object.getString("OS-EXT-SRV-ATTR:instance_name"));
		vm.setPower_state(object.getString("OS-EXT-STS:power_state"));
		vm.setVm_state(object.getString("OS-EXT-STS:vm_state"));
		vm.setLaunched_at(object.getString("OS-SRV-USG:launched_at"));
		JSONArray jsonList = object.getJSONArray("os-extended-volumes:volumes_attached");
		List<String> volumeList = new ArrayList<String>();
		if (jsonList != null && jsonList.size() > 0) {
			for (Object data : jsonList) {
				volumeList.add(((JSONObject) data).getString("id"));
			}
		}
		vm.setVolumes_attached(volumeList);
	}

	private List<Vm> list(RestTokenBean restTokenBean, String url)
			throws AppException {
		List<Vm> list = null;
		// 获取云主机信息
		restTokenBean.setUrl(url);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.VM_DATA_NAMES);
		// 获取当前项目下的云硬盘信息
		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<Vm>();
				}
				if(null==jsonObject.getString("image")||"".equals(jsonObject.getString("image"))){
					JSONObject imageJson =new JSONObject();
					imageJson.put("id", "");
					jsonObject.put("image", imageJson);
				}
				// json转换为java对象
				Vm vm = restService.json2bean(jsonObject, Vm.class);
				// 控制云主机创建时间不显示TZ字母
				vm.setCreated(vm.getCreated().replace("T", " ")
						.replace("Z", ""));
				// 对无法自动转换的数据手动转换
				initData(vm, jsonObject);
				list.add(vm);
			}
		}
		return list;
	}

	public List<Vm> list(String datacenterId, String projectId)
			throws AppException {
		// 初始化opentack平台连接
		if (projectId == null || projectId.equals("")) {
			return null;
		}
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		return list(restTokenBean, OpenstackUriConstant.VM_DETAIL_URI);
	}

	public List<Vm> listAll(String datacenterId) throws AppException {
		List<Vm> list = null;

		List<CloudProject> projectList = projectService
				.getProjectListByDataCenter(datacenterId);

		// 项目列表非空并且长度大于0时
		if (projectList != null && projectList.size() > 0) {
			if (list == null) {
				list = new ArrayList<Vm>();
			}
			RestTokenBean restTokenBean = null;
			for (BaseCloudProject cloudProject : projectList) {
				if (restTokenBean == null) {
					restTokenBean = getRestTokenBean(datacenterId,
							cloudProject.getProjectId(),
							OpenstackUriConstant.COMPUTE_SERVICE_URI);
				} else {
					restTokenBean.setTenantId(cloudProject.getProjectId());
				}

				List<Vm> vmList = list(restTokenBean,
						OpenstackUriConstant.VM_DETAIL_URI);
				if (vmList != null && vmList.size() > 0) {
					list.addAll(vmList);
				}
			}
		}

		return list;
	}

	public Vm getById(String datacenterId, String projectId, String id)
			throws AppException {
		Vm result = null;

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		// 执行具体业务操作，并获取返回结果
		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.VM_URI + "/",
				OpenstackUriConstant.VM_DATA_NAME, id);
		List<Volume> volumeList = openstackVolumeService.list(datacenterId,
				projectId);
		if (json != null) {
			result = restService.json2bean(json, Vm.class);
			initData(result, json);
			if (result.getVolumes_attached() != null
					&& result.getVolumes_attached().size() > 0) {
				List<Volume> volumes = new ArrayList<Volume>();
				for (String volumeId : result.getVolumes_attached()) {
					if (volumeList != null && volumeList.size() > 0) {
						for (Volume volume : volumeList) {
							if (volume.getId().equals(volumeId)) {
								volumes.add(volume);
							}
						}
					}
				}
				result.setVolumes(volumes);
			}
			result.setFlavor(flavor(restTokenBean, result.getFlavor().getId()));
		}

		return result;
	}

	public List<Vm> create(String datacenterId, String projectId,
			String flavorId, HttpServletRequest request) throws AppException {
		List<Vm> list = null;
		// 网络数据
		JSONObject net = new JSONObject();
		net.put("uuid", request.getParameter("network"));
		JSONObject[] nets = { net };
		// 云主机内层数据
		JSONObject object = new JSONObject();
		object.put("imageRef", request.getParameter("image"));
		object.put("networks", nets);
		int number = Integer.parseInt(request.getParameter("number"));
		object.put("flavorRef", flavorId);

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);

		for (int i = 1; i < number + 1; i++) {
			if (list == null) {
				list = new ArrayList<Vm>();
			}
			// 设置云主机名称
			if (number > 1) {
				object.put("name", request.getParameter("name") + i);
			} else {
				object.put("name", request.getParameter("name"));
			}
			// 用于提交的完整数据
			JSONObject data = new JSONObject();
			data.put("server", object);
			// 执行创建操作
			restTokenBean.setUrl(OpenstackUriConstant.VM_URI);
			JSONObject result = restService.create(restTokenBean,
					OpenstackUriConstant.VM_DATA_NAME, data);
			if (result != null) {
				Vm vm = restService.json2bean(result, Vm.class);
				list.add(vm);
			}
		}

		return list;
	}

	public boolean  delete(String datacenterId, String projectId, String id)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.VM_URI + "/" + id);
		// 执行具体业务操作，并获取返回结果
		return restService.delete(restTokenBean);

	}

	public Vm update(String datacenterId, String projectId, JSONObject data,
			String id) throws AppException {
		Vm vm = null;

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.VM_URI + "/" + id);
		// 执行具体业务操作，并获取返回结果
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.VM_DATA_NAME, data);
		// 将获取的JSONObject对象转换为model包中定义的与之对应的java对象
		vm = restService.json2bean(result, Vm.class);

		return vm;
	}

	private JSONObject operate(String datacenterId, String projectId,
			String id, JSONObject data) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean
				.setUrl(OpenstackUriConstant.VM_URI + "/" + id + "/action");
		// 执行具体业务操作，并获取返回结果
		JSONObject result = restService.operate(restTokenBean, data);

		return result;
	}

	public boolean start(String datacenterId, String projectId, String id)
			throws AppException {
		// 根据操作类型设置request body 对象
		JSONObject data = new JSONObject();
		data.put("os-start", "null");

		return operate(datacenterId, projectId, id, data) != null;

	}

	public boolean shutoff(String datacenterId, String projectId, String id)
			throws AppException {
		// 根据操作类型设置request body 对象
		JSONObject data = new JSONObject();
		data.put("os-stop", "null");

		return operate(datacenterId, projectId, id, data) != null;
	}

	public boolean suspend(String datacenterId, String projectId, String id)
			throws AppException {
		JSONObject data = new JSONObject();
		data.put("suspend", "null");

		return operate(datacenterId, projectId, id, data) != null;
	}

	public boolean resume(String datacenterId, String projectId, String id)
			throws AppException {
		JSONObject data = new JSONObject();
		data.put("resume", "null");
		return operate(datacenterId, projectId, id, data) != null;
	}

	public boolean softReboot(String datacenterId, String projectId, String id)
			throws AppException {
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("type", "SOFT");
		data.put("reboot", temp);
		return operate(datacenterId, projectId, id, data) != null;
	}

	public boolean hardReboot(String datacenterId, String projectId, String id)
			throws AppException {
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("type", "HARD");
		data.put("reboot", temp);
		return operate(datacenterId, projectId, id, data) != null;
	}

	public boolean edit(String datacenterId, String projectId, String id,
			String name) throws AppException {
		JSONObject edit = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("name", name);
		edit.put("server", temp);

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.VM_URI + "/" + id);

		// 执行具体业务操作，并获取返回结果
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.VM_DATA_NAME, edit);
		if (result != null) {
			return true;
		}

		return false;
	}

	public boolean resize(String datacenterId, String projectId, String id,
			String flavorId) throws AppException {
		// resize
		JSONObject resize = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("flavorRef", flavorId);
		resize.put("resize", temp);

		return operate(datacenterId, projectId, id, resize) != null;
	}

	public boolean confirmResize(String datacenterId, String projectId,
			String id) throws AppException {
		JSONObject confirm = new JSONObject();
		confirm.put("confirmResize", "null");

		return operate(datacenterId, projectId, id, confirm) != null;
	}

	public boolean revertResize(String datacenterId, String projectId, String id)
			throws AppException {
		JSONObject confirm = new JSONObject();
		confirm.put("revertResize", "null");

		return operate(datacenterId, projectId, id, confirm) != null;
	}

	public boolean rebuild(String datacenterId, String projectId, String id,
			JSONObject data) throws AppException {
		return operate(datacenterId, projectId, id, data) != null;
	}

	public Image snapshot(String datacenterId, String projectId, String id,
			String name) throws AppException {
		// 根据操作类型设置request body 对象
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("name", name);
		data.put("createImage", temp);

		operate(datacenterId, projectId, id, data);

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.IMAGE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.IMAGE_URI);
		List<JSONObject> list = restService.list(restTokenBean,
				OpenstackUriConstant.IMAGE_DATA_NAMES);
		for (JSONObject jsonObject : list) {
			// 将获取的JSONObject对象转换为model包中定义的与之对应的java对象
			Image image = restService.json2bean(jsonObject, Image.class);
			if (id.equals(image.getInstance_uuid())) {
				return image;
			}
		}
		return null;

	}

	public String console(String datacenterId, String projectId, String id)
			throws AppException {
		// 根据操作类型设置request body 对象
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("type", "novnc");
		data.put("os-getVNCConsole", temp);

		JSONObject result = operate(datacenterId, projectId, id, data);

		return result.getJSONObject("console").getString("url");
	}

	public JSONObject log(String datacenterId, String projectId, String id)
			throws AppException {
		JSONObject data = new JSONObject();
		JSONObject temp = new JSONObject();
		temp.put("length", 50);
		data.put("os-getConsoleOutput", temp);

		return operate(datacenterId, projectId, id, data);
	}

	public List<SecurityGroup> listSecurityGroupForVm(String datacenterId,
			String projectId, String id) throws AppException {
		List<SecurityGroup> list = null;

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.VM_URI + "/" + id
				+ "/os-security-groups");
		// 执行具体业务操作，并获取返回结果
		List<JSONObject> result = restService.list(restTokenBean,
				"security_groups");
		if (result != null && result.size() > 0) {
			if (list == null) {
				list = new ArrayList<SecurityGroup>();
			}
			for (JSONObject jsonObject : result) {
				list.add(restService.json2bean(jsonObject, SecurityGroup.class));
			}
		}
		return list;

	}

	/**
	 * 获取指定id的flavor的详情
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	private Flavor flavor(RestTokenBean restTokenBean, String id)
			throws AppException {
		Flavor flavor = null;
		restTokenBean.setUrl(OpenstackUriConstant.FLAVOR_URI + "/" + id);
		JSONObject result = restService.get(restTokenBean, "flavor");
		if (result != null) {
			flavor = restService.json2bean(result, Flavor.class);
		}
		return flavor;
	}

	public boolean deleteFlavor(String datacenterId, String projectId, String id)
			throws AppException {

		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.FLAVOR_URI + "/" + id);
		boolean result = restService.delete(restTokenBean);
		return result;
	}

	@Override
	public Vm create(String datacenterId, String projectId, JSONObject data)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.VM_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.VM_DATA_NAME, data);
		if (result != null) {
			Vm vm = restService.json2bean(result, Vm.class);
			return vm;
		}
		return null;
	}

	/**                                                                                                         
	 * 获取底层项目下的云主机                                                               
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 * @throws Exception 
	 *                                                                                                        
	 */                                                                                                       
	public List<JSONObject> getStackList(BaseDcDataCenter dataCenter,String prjId) throws Exception {                                  
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),prjId,                                              
				OpenstackUriConstant.COMPUTE_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.VM_DETAIL_URI);                                           
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.VM_DATA_NAMES);
                                                                                                            
		return result;                                                                                            
	}      
	
	/**                                                                                                         
	 * 获取底层项目下的云主机                                                               
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 * @throws Exception 
	 *                                                                                                        
	 */                                                                                                       
	public List<JSONObject> getSoftDeletedList(String dcId,String prjId,String url) throws Exception {                                  
		RestTokenBean restTokenBean = getRestTokenBean(dcId,prjId,                                              
				OpenstackUriConstant.COMPUTE_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.VM_DETAIL_URI+url);                                           
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.VM_DATA_NAMES);
		
		return result;                                                                                            
	}      
	
	public <T> T json2bean(JSONObject jSONObject, Class<T> clazz){
		return restService.json2bean(jSONObject, clazz);
	}
	
	/**
	 * 云主机绑定端口
	 * 
	 * @author zhouhaitao
	 * 
	 * @param dcId
	 * 			数据中心ID
	 * @param prjId
	 * 			项目ID
	 * @param vmId
	 * 			云主机ID
	 * @param netId
	 * 			网络ID
	 * @param subnetId
	 * 			子网ID
	 * @param sgId
	 * 			安全组ID数组
	 * @return
	 * @throws AppException
	 */
	public InterfaceAttachment bindPort(String dcId, String prjId ,String vmId ,String netId,String subnetId,String [] sgIds) throws AppException{
		Port selfPort = createPort(dcId,prjId,netId,subnetId,sgIds);
		
		JSONObject port = new JSONObject();
		JSONObject data = new JSONObject();
		port.put("port_id", selfPort.getId());
		data.put("interfaceAttachment", port);
			
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.VM_URI+"/"+vmId+OpenstackUriConstant.OS_INTERFACE_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.OS_INTERFACE_NAME, data);
		
		if (result != null) {
			InterfaceAttachment interAtta = restService.json2bean(result, InterfaceAttachment.class);
			return interAtta;
		}
		return null;
	}
	
	/**
	 * 删除云主机关联的端口
	 * 
	 * @author zhouhaitao
	 * --------------------
	 * @param dcId
	 * 			数据中心ID
	 * @param prjId
	 * 			项目ID
	 * @param vmId
	 * 			云主机ID
	 * @param portId
	 * 			端口ID
	 * @return
	 * 
	 */
	public boolean unbindPort(String dcId,String prjId, String vmId, String portId) throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(dcId, prjId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.VM_URI+"/"+vmId+OpenstackUriConstant.OS_INTERFACE_URI+"/"+portId);
		
		return restService.delete(restTokenBean);
	}
	
	/**
	 * 根据过滤条件查询主机列表
	 * 
	 * @author zhouhaitao
	 * 
	 * @param dcId
	 * 			数据中心ID
	 * @param prjId
	 * 			项目ID
	 * @param url
	 * 			过滤条件
	 * @return
	 */
	public List<Vm> list(String datacenterId, String projectId,String url)
			throws AppException {
		if (projectId == null || projectId.equals("")) {
			return null;
		}
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.COMPUTE_SERVICE_URI);
		String vmUrl = OpenstackUriConstant.VM_DETAIL_URI+url;
		return list(restTokenBean, vmUrl);
	}
	
	/**
	 * 恢复 软删除的云主机
	 * ---------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * 			
	 * @return
	 */
	public boolean restorVm(CloudVm cloudVm){
		JSONObject data = new JSONObject();
		data.put("restore", "null");

		return operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), data) != null;
	}
	
	/**
	 * 强制删除云主机
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * @return
	 */
	public boolean forceDelete(CloudVm cloudVm){
		JSONObject data = new JSONObject();
		data.put("forceDelete", "null");

		return operate(cloudVm.getDcId(), cloudVm.getPrjId(), cloudVm.getVmId(), data) != null;
	}
	
	/**
	 * 修改主机密码
	 * ------------------
	 * @author zhouhaitao
	 * @param cloudVm
	 * @param data
	 * 
	 * @throws AppException
	 */
	public void modifyVmPassword(CloudVm cloudVm,JSONObject data)throws AppException{
		if(ConstantClazz.DICT_CLOUD_OS_LINUX_NODE_ID.equals(cloudVm.getOsType())){
			RestTokenBean restTokenBean = getRestTokenBean(cloudVm.getDcId(),cloudVm.getPrjId(),
					OpenstackUriConstant.COMPUTE_SERVICE_URI);
	        restTokenBean.setUrl(OpenstackUriConstant.SSH_SECRETKEY_BIND_URL + "/" + cloudVm.getVmId());
	        restService.update(restTokenBean,null, data);
			
		}
		
		JSONObject mdData = new JSONObject();
		JSONObject metadata = new JSONObject();
		metadata.put("reset_password", System.currentTimeMillis()+"");
		if(ConstantClazz.DICT_CLOUD_OS_WINDOWS_NODE_ID.equals(cloudVm.getOsType())){
			metadata.put("admin_pass", cloudVm.getPassword());
		}
		mdData.put("metadata", metadata);
		modifyMetadata(cloudVm.getDcId(),cloudVm.getPrjId(),cloudVm.getVmId(),mdData);
	}
	
	/**
	 * <p>修改云主机的的Metadata信息</p>
	 * 
	 * @author zhouhaitao
	 * @param dcId
	 * @param prjId
	 * @param vmId
	 * @param data
	 */
	public void modifyMetadata(String dcId,String prjId,String vmId,JSONObject data){
		RestTokenBean restTokenBean = getRestTokenBean(dcId,prjId,OpenstackUriConstant.COMPUTE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.VM_URI + "/" + vmId + OpenstackUriConstant.SSH_METADATA_URL );
        restService.create(restTokenBean, OpenstackUriConstant.SSH_METADATA_DATA_NAME, data);
	}
	
}