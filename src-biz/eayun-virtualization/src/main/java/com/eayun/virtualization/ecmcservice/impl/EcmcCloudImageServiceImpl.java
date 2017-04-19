package com.eayun.virtualization.ecmcservice.impl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.ClientResponseException;
import org.openstack4j.api.exceptions.ServerResponseException;
import org.openstack4j.model.common.Payload;
import org.openstack4j.model.common.Payloads;
import org.openstack4j.model.image.ContainerFormat;
import org.openstack4j.model.image.DiskFormat;
import org.openstack4j.model.image.builder.ImageBuilder;
import org.openstack4j.openstack.OSFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.eayunstack.model.Image;
import com.eayun.eayunstack.service.OpenstackImageService;
import com.eayun.eayunstack.service.impl.OpenstackBaseServiceImpl;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.price.model.BillingFactor;
import com.eayun.price.service.BillingFactorService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.dao.CloudImageDao;
import com.eayun.virtualization.ecmcservice.EcmcCloudImageService;
import com.eayun.virtualization.ecmcservice.EcmcCloudVmService;
import com.eayun.virtualization.model.BaseCloudImage;
import com.eayun.virtualization.model.CloudImage;
import com.eayun.virtualization.service.TagService;

@Service
@Transactional
public class EcmcCloudImageServiceImpl extends OpenstackBaseServiceImpl<Image> implements EcmcCloudImageService {
	
	private static final Logger log = LoggerFactory.getLogger(EcmcCloudImageServiceImpl.class);
	
	@Autowired
	private OpenstackImageService openstackImageService;
	
	@Autowired
	private TagService tagService;
	
	@Autowired
	private CloudImageDao cloudImageDao;
	
	@Autowired
	private JedisUtil jedisUtil;
	
	@Autowired
	private DataCenterService dataCenterService;
	
	@Autowired
	private BillingFactorService  billFactorService;
	
	@Autowired
	private EcmcCloudVmService   vmService;
	
	
	
	
	@Override
	public Page getImagePageList(Page page, QueryMap queryMap, String dcId,String sourceType,
			String queryType, String queryName) {
		log.info("查询自定义镜像列表");
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("ci.image_id, ");
		sql.append("ci.image_name, ");
		sql.append("ci.dc_id, ");
		sql.append("ci.prj_id, ");
		sql.append("ci.image_status, ");
		sql.append("ci.sys_type, ");
		sql.append("ci.created_time, ");
		sql.append("ci.image_ispublic, ");
		sql.append("sdt.node_name, ");
		sql.append("dc.dc_name, ");
		sql.append("prj.prj_name, ");
		sql.append("cus.cus_org, ");
		sql.append("ci.image_description, ");
		sql.append("ci.min_cpu, ");
		sql.append("ci.min_ram, ");
		sql.append("ci.min_disk, ");
		sql.append("ci.source_id, ");
		sql.append("img.image_name as sourceName, ");
		sql.append("img.image_ispublic as sourceType,  ");
		sql.append("vmnum.num as vmNum ");
		sql.append("FROM cloud_image ci ");
		sql.append("JOIN cloud_image img ON ci.source_id=img.image_id ");
		sql.append("LEFT JOIN dc_datacenter dc ON ci.dc_id = dc.id ");
		sql.append("LEFT JOIN cloud_project prj ON ci.prj_id = prj.prj_id ");
		sql.append("LEFT JOIN sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
		sql.append("LEFT JOIN sys_data_tree sdt  ON ci.sys_type = sdt.node_id ");
		sql.append("LEFT JOIN ");
		sql.append("( ");
		sql.append("SELECT COUNT(vm.vm_id) AS num,vm.from_imageid ");
		sql.append("FROM cloud_vm vm WHERE vm.is_visable='1' and ( vm.is_deleted = '0' or vm.is_deleted='2' ) GROUP BY vm.from_imageid ");
		sql.append(") ");
		sql.append("AS vmnum ON ci.image_id = vmnum.from_imageid ");
		sql.append("WHERE ci.image_ispublic = '2' ");
		if(!"".equals(dcId) && null != dcId){
			sql.append("and ci.dc_id = ? ");
			list.add(dcId);
		}
		if(!"".equals(sourceType) && null != sourceType){
			sql.append("and img.image_ispublic = ? ");
			list.add(sourceType);
		}
		if (null != queryName && !queryName.trim().equals("")) {
			if(queryType.equals("imageName")){
				queryName = queryName.replaceAll("\\_", "\\\\_");
				//根据名称模糊查询
				sql.append(" and binary ci.image_name like ?");
	            list.add("%" + queryName + "%");
	            
			}else if(queryType.equals("cusOrg")){
				//根据所属客户精确查询
				String[] cusOrgs = queryName.split(",");
				sql.append(" and ( ");
				for(String org:cusOrgs){
					sql.append(" binary cus.cus_org = ? or ");
					list.add(org);
				}
				sql.append(" 1 = 2 ) ");
				
			}else if(queryType.equals("prjName")){
				//根据项目名称精确查询
				String[] prjName = queryName.split(",");
				sql.append(" and ( ");
				for(String prj:prjName){
					sql.append(" binary prj.prj_name = ? or ");
					list.add(prj);
				}
				sql.append(" 1 = 2 ) ");
			}
			else{
				sql.append(" and  1 = 2 ");
			}
		}
		sql.append("ORDER BY ci.dc_id ,ci.prj_id , ci.created_time DESC ");
		page = cloudImageDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
		List resultList = (List) page.getResult();
		for(int i = 0; i < resultList.size(); i++){
			Object[] obj = (Object[]) resultList.get(i);
			CloudImage cloudImage = new CloudImage();
			cloudImage.setImageId(String.valueOf(obj[0]));
			cloudImage.setImageName(String.valueOf(obj[1]));
			cloudImage.setDcId(String.valueOf(obj[2]));
			cloudImage.setPrjId(null != obj[3]?String.valueOf(obj[3]):"");
			cloudImage.setImageStatus(String.valueOf(obj[4]));
			cloudImage.setSysType(String.valueOf(obj[5]));
			cloudImage.setCreatedTime((Date)obj[6]);
			cloudImage.setImageIspublic((Character)obj[7]);
			cloudImage.setSysTypeName(String.valueOf(obj[8]));
			cloudImage.setDcName(String.valueOf(obj[9]));
			cloudImage.setPrjName(String.valueOf(obj[10]));
			cloudImage.setCusOrg(String.valueOf(obj[11]));
			cloudImage.setImageDescription(String.valueOf(null !=obj[12]?obj[12]:""));
			cloudImage.setMinCpu(Long.parseLong(null != obj[13] ? String.valueOf(obj[13]) : "0"));
			int number=Integer.parseInt(null != obj[14] ? String.valueOf(obj[14]) : "0");
			if(0==number){
				cloudImage.setMinRam(Long.parseLong(null != obj[14] ? String.valueOf(obj[14]) : "0")/1024);//库里存的是MB，转换为GB
			}else{
				if((Integer.parseInt(null != obj[14] ? String.valueOf(obj[14]) : "0")%1024)==0){
					cloudImage.setMinRam(Long.parseLong(null != obj[14] ? String.valueOf(obj[14]) : "0")/1024);
				}else{
					cloudImage.setMinRam(Long.parseLong(null != obj[14] ? String.valueOf(obj[14]) : "0")/1024+1);//库里存的是MB，转换为GB,向上取整，例如：1000MB=1GB
				}	
			}
			cloudImage.setMinDisk(Long.parseLong(null != obj[15] ? String.valueOf(obj[15]) : "0"));
			cloudImage.setSourceId(String.valueOf(obj[16]));
			cloudImage.setSourceName(String.valueOf(obj[17]));
			cloudImage.setSourceType(String.valueOf(obj[18]));
			cloudImage.setVmNum(Integer.parseInt(null != obj[19] ? String.valueOf(obj[19]) : "0"));
			cloudImage.setStatusForDis(DictUtil.getStatusByNodeEn("image", cloudImage.getImageStatus()));
			resultList.set(i, cloudImage);
		}
		return page;
	}

	/**
	 * 查询公共镜像列表
	 * @Author: duanbinbin
	 * @param page
	 * @param queryMap
	 * @param dcId
	 * @param sysType
	 * @param imageName
	 * @return
	 *<li>Date: 2016年4月28日</li>
	 */
	@Override
	public Page getConImagePageList(Page page, QueryMap queryMap, String dcId,
			String sysType, String imageName,String isUse) {
		log.info("查询公共镜像列表");
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("ci.image_id, ");
		sql.append("ci.image_name, ");
		sql.append("ci.dc_id, ");
		sql.append("ci.image_status, ");
		sql.append("ci.sys_type, ");
		sql.append("ci.created_time, ");
		sql.append("ci.image_ispublic, ");
		sql.append("ci.min_cpu, ");
		sql.append("ci.min_ram, ");
		sql.append("ci.min_disk, ");
		sql.append("ci.disk_format, ");
		sql.append("sdt.node_name, ");
		sql.append("vmnum.num, ");
		sql.append("dc.dc_name, ");
		sql.append("ci.os_type, ");
		sql.append("ci.image_description, ");
		sql.append("ci.max_cpu, ");
		sql.append("ci.max_ram, ");
		sql.append("ci.sys_detail, ");
		sql.append("ci.is_use ");
		sql.append("FROM cloud_image ci ");
		sql.append("LEFT JOIN dc_datacenter dc ON ci.dc_id = dc.id ");
		sql.append("LEFT JOIN sys_data_tree sdt  ON ci.sys_type = sdt.node_id ");
		sql.append("LEFT JOIN ");
		sql.append("( ");
		sql.append("SELECT COUNT(vm.vm_id) AS num,vm.from_imageid ");
		sql.append("FROM cloud_vm vm WHERE vm.is_visable='1' and ( vm.is_deleted = '0' or vm.is_deleted='2' ) GROUP BY vm.from_imageid ");
		sql.append(") ");
		sql.append("AS vmnum ON ci.image_id = vmnum.from_imageid ");
		sql.append("WHERE ci.image_ispublic = '1' ");
		if(!"".equals(dcId)){
			sql.append("and ci.dc_id = ? ");
			list.add(dcId);
		}
		if(!"".equals(sysType)){
			if(ConstantClazz.DICT_CLOUD_OS_WINDOWS_NODE_ID.equals(sysType)){
				sql.append("and ci.os_type = ? ");
			}else{
				sql.append("and ci.sys_type = ? ");
			}
			
			list.add(sysType);
		}
		if(!"".equals(imageName) && null != imageName){
			imageName = imageName.replaceAll("\\_", "\\\\_");
			sql.append("and binary ci.image_name like ? ");
			list.add("%" + imageName + "%");
		}
		if(!"".equals(isUse) && null != isUse){
			sql.append("and ci.is_use = ? ");
			if("1".equals(isUse)){
				sql.append("and ci.image_status = 'ACTIVE' ");
			}
			list.add(isUse);
			
		}
		sql.append("ORDER BY ci.dc_id,ci.sys_type,ci.created_time DESC ");
		page = cloudImageDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
		List resultList = (List) page.getResult();
		for(int i = 0; i < resultList.size(); i++){
			Object[] obj = (Object[]) resultList.get(i);
			CloudImage cloudImage = new CloudImage();
			cloudImage.setImageId(String.valueOf(obj[0]));
			cloudImage.setImageName(String.valueOf(obj[1]));
			cloudImage.setDcId(String.valueOf(obj[2]));
			cloudImage.setImageStatus(String.valueOf(obj[3]));
			cloudImage.setSysType(String.valueOf(obj[4]));
			cloudImage.setCreatedTime((Date)obj[5]);
			cloudImage.setImageIspublic((Character)obj[6]);
			cloudImage.setMinCpu(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0"));
			int number=Integer.parseInt(null != obj[8] ? String.valueOf(obj[8]) : "0");
			if(0==number){
				cloudImage.setMinRam(Long.parseLong(null != obj[8] ? String.valueOf(obj[8]) : "0")/1024);//库里存的是MB，转换为GB
			}else{
				if((Integer.parseInt(null != obj[8] ? String.valueOf(obj[8]) : "0")%1024)==0){
					cloudImage.setMinRam(Long.parseLong(null != obj[8] ? String.valueOf(obj[8]) : "0")/1024);
				}else{
					cloudImage.setMinRam(Long.parseLong(null != obj[8] ? String.valueOf(obj[8]) : "0")/1024+1);//库里存的是MB，转换为GB,向上取整，例如：1000MB=1GB
				}	
			}
			cloudImage.setMinDisk(Long.parseLong(null != obj[9] ? String.valueOf(obj[9]) : "0"));
			cloudImage.setDiskFormat(String.valueOf(obj[10]));
			cloudImage.setSysTypeName(String.valueOf(obj[11]));
			cloudImage.setVmNum(Integer.parseInt(null != obj[12] ? String.valueOf(obj[12]) : "0"));
			cloudImage.setDcName(String.valueOf(obj[13]));
			cloudImage.setOsType(String.valueOf(obj[14]));
			cloudImage.setImageDescription(String.valueOf(obj[15]));
			
			cloudImage.setMaxCpu(null == obj[16] || "".equals(String.valueOf(obj[16]))? null : Integer.valueOf(String.valueOf(obj[16])));
			cloudImage.setMaxRam(null == obj[17] || "".equals(String.valueOf(obj[17]))? null : Integer.valueOf(String.valueOf(obj[17]))/1024);
			cloudImage.setSysDetail(String.valueOf(obj[18]));
			cloudImage.setIsUse(null!=obj[19]?((Character)obj[19]):null);
			//cloudImage.setStatusForDis(DictUtil.getStatusByNodeEn("image", cloudImage.getImageStatus()));
			cloudImage.setStatusForDis(escapseImageState(cloudImage));
			
			resultList.set(i, cloudImage);
		}
		return page;
	}

	//镜像状态转换
	private String escapseImageState(CloudImage cloudImage) {
		String status="";
		
		if(null!=cloudImage.getIsUse()&&"0".equals(cloudImage.getIsUse().toString())){
			status="未启用";
		}else if(null!=cloudImage.getIsUse()&&"1".equals(cloudImage.getIsUse().toString())&&"ACTIVE".equals(cloudImage.getImageStatus())){
			status="正常";
		}else if(null!=cloudImage.getIsUse()&&"2".equals(cloudImage.getIsUse().toString())){
			status="已停用";
		}else{
			status=DictUtil.getStatusByNodeEn("image", cloudImage.getImageStatus());
		}
		
		
		return status;
	}

	/**
	 * 上传公共镜像
	 * @Author: duanbinbin
	 * @param cloudImage
	 * @return
	 *<li>Date: 2016年4月28日</li>
	 */
	@Override
	public CloudImage createPublicImage(CloudImage cloudImage,InputStream is) {
		BaseEcmcSysUser ecmcSysUser = EcmcSessionUtil.getUser();
		String name = cloudImage.getImageName();
		String imageIspublic =  "";
		String imageFormat = cloudImage.getDiskFormat();
		String url =  cloudImage.getImageUrl();
		String description=cloudImage.getImageDescription();
		long mincpu = cloudImage.getMinCpu();
		String osType = cloudImage.getOsType();
		
		long minram = cloudImage.getMinRam();
		long mindisk = cloudImage.getMinDisk();
		
		//2、调用底层新增镜像
		Map<String,String> map =new HashMap<String, String>();
		map.put("name",name);
		map.put("isPublic", "");
		map.put("imageFormat", imageFormat);
		map.put("minDisk", String.valueOf(mindisk));
		map.put("minRam", String.valueOf(minram*1024));//前台传的都为GB，需转换为MB
		map.put("description", description);
		map.put("url", url);
		org.openstack4j.model.image.Image image = null;
		image = createImageBySdk(cloudImage.getDcId(),map,is);
		if(null != image){
			BaseCloudImage baseCloudImage = new BaseCloudImage();
			baseCloudImage.setImageId(image.getId());
			baseCloudImage.setImageName(name);
			baseCloudImage.setSysDetail(cloudImage.getSysDetail());
			baseCloudImage.setIsUse('0');
			baseCloudImage.setDcId(cloudImage.getDcId());
			baseCloudImage.setDiskFormat(imageFormat.toLowerCase());
			baseCloudImage.setOwnerId(image.getOwner());
			baseCloudImage.setOsType(osType);
			baseCloudImage.setSysType(cloudImage.getSysType());
			baseCloudImage.setMinDisk(mindisk);
			baseCloudImage.setMinRam(minram*1024);
			baseCloudImage.setImageIspublic('1');//公共镜像
			if(null!=url&&!"null".equals(url)&&!"".equals(url)){
				baseCloudImage.setImageUrl(url);
			}
			baseCloudImage.setCreateName(ecmcSysUser.getAccount());
			baseCloudImage.setCreatedTime(new Date());
			baseCloudImage.setImageStatus(image.getStatus().value().toUpperCase());
			baseCloudImage.setImageSize(new BigDecimal(image.getSize()));
			baseCloudImage.setContainerFormat(image.getContainerFormat().toString());
			baseCloudImage.setMinCpu(mincpu);
			
			baseCloudImage.setMaxCpu(cloudImage.getMaxCpu());
			baseCloudImage.setMaxRam(cloudImage.getMaxRam());
			baseCloudImage.setImageDescription(description);
			cloudImageDao.saveEntity(baseCloudImage);
			BeanUtils.copyPropertiesByModel(cloudImage, baseCloudImage);
			
			//增加默认价格
			BillingFactor  billingFactor=new BillingFactor();
			billingFactor.setResourcesType("IMAGE");
			billingFactor.setFactorUnit(baseCloudImage.getImageId());
			billingFactor.setStartNum(Long.parseLong("1"));
			billingFactor.setEndNum(Long.parseLong("-1"));
			billingFactor.setBillingFactor(null);
			billingFactor.setDcId(baseCloudImage.getDcId());
			billingFactor.setPrice(new BigDecimal(0));
			billFactorService.addFactorPrice(billingFactor, true);
		}
		
		return cloudImage;
	}
	/**
	 * SDK方式上传公共镜像
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年4月28日</li>
	 */
	private org.openstack4j.model.image.Image createImageBySdk(String dcId,Map<String, String> map,InputStream is){
		Payload<URL> payload = null;
		Payload<InputStream> payloadStream = null;
		org.openstack4j.model.image.Image image = null;
		try {
			// 设置初始参数
			ImageBuilder createImageBuilder = Builders.image()
					.name(map.get("name")).isPublic(true)
					.minDisk(Long.parseLong(map.get("minDisk")))
					.minRam(Long.parseLong(map.get("minRam")))
					.diskFormat(DiskFormat.valueOf(map.get("imageFormat")));
			// 设置容器格式
			if (map.get("imageFormat").toUpperCase().contains("AKI")) {
				createImageBuilder.containerFormat(ContainerFormat.AKI);
			} else if (map.get("imageFormat").toUpperCase().contains("ARI")) {
				createImageBuilder.containerFormat(ContainerFormat.ARI);
			} else if (map.get("imageFormat").toUpperCase().contains("AMI")) {
				createImageBuilder.containerFormat(ContainerFormat.AMI);
			} else if (map.get("imageFormat").toUpperCase().contains("OVF")) {
				createImageBuilder.containerFormat(ContainerFormat.OVF);
			} else {
				createImageBuilder.containerFormat(ContainerFormat.BARE);
			}
			// 文件方式创建镜像
			if (is != null) {
				payloadStream = Payloads.create(is);
				image = initOsClient(dcId).images().create(
						createImageBuilder.build(), payloadStream);
				// url方式创建镜像
			} else {
				payload = Payloads.create(new URL(map.get("url")));
				image = initOsClient(dcId).images().create(
						createImageBuilder.build(), payload);
			}
		} catch (ClientResponseException e) {
			throw new AppException("error.openstack.message",
					new String[] { e.getMessage() });
		}catch(ServerResponseException e){
			e.getMessage();
			log.error(e.getMessage());
			if("Internal Server Error".equals(e.getMessage())){
				String [] args= new String []{"Internal Server Error"};
				throw new AppException("error.openstack.message", args);
			}
			
		} catch(AppException e){
			throw e;
		}catch (Exception e) {
			throw new AppException("error.openstack.message",
					new String[] { "创建镜像失败，请检查输入数据！" });
		}
		return image;
	}
	protected OSClient initOsClient(String datacenterId) throws AppException {
		try {
			BaseDcDataCenter dcDatacenter = dataCenterService.getById(datacenterId);
			String projectId = dcDatacenter.getOsAdminProjectId();

			long startTime = System.currentTimeMillis();

			OSClient osClient = OSFactory
					.builder()
					.endpoint(dcDatacenter.getDcAddress())
					.credentials(dcDatacenter.getVCenterUsername(),
							dcDatacenter.getVCenterPassword())
					.tenantId(projectId).authenticate();

			long endTime = System.currentTimeMillis();
			long spendTime = (endTime - startTime) / 1000;
			log.info("The time spent to build connection:" + spendTime
					+ " seconds.");

			if (osClient != null) {
				log.info("connect to virtualPlatform:success!!!");
			}
			return osClient;
		} catch (Exception e) {
			throw new AppException("error.validate.message", new String[] {
					"" + 401, "操作openstack平台时出错，请检查！" });
		}
	}

	/**
	 * 修改镜像
	 * @Author: duanbinbin
	 * @param cloudImage
	 *<li>Date: 2016年4月28日</li>
	 */
	@Override
	public boolean updatePersonImage(CloudImage cloudImage) throws AppException {
		log.info("编辑自定义镜像");
		boolean isTrue=false;
		try{
			
	        String imageName =cloudImage.getImageName();
	        String description =cloudImage.getImageDescription();
	        //拼装数据传给底层
	        JSONObject net = new JSONObject();			
			net.put("op", "replace");
			net.put("path", "/name");		
			net.put("value", imageName);
			net.put("description", description);
			JSONArray  resultData = new JSONArray();		
			resultData.add(net);
			Image img=openstackImageService.update(cloudImage, resultData.toString());
			if(null!=img){
				BaseCloudImage baseCloudImage = cloudImageDao.findOne(img.getId());
				baseCloudImage.setImageName(img.getName());
				baseCloudImage.setImageDescription(description);
				cloudImageDao.merge(baseCloudImage);
				isTrue=true;
			}
		}catch(AppException e){
			throw e;
		}
		return isTrue;
	}
	/**
	 * 编辑公共镜像
	 * @Author: duanbinbin
	 * @param cloudImage
	 * @return
	 * @throws AppException
	 *<li>Date: 2016年4月28日</li>
	 */
	@Override
	public boolean updatePublicImage(CloudImage cloudImage) throws AppException {
		log.info("编辑公共镜像");
		boolean isTrue=false;
		try{
			
	        String imageName =cloudImage.getImageName();
	        long mincpu = cloudImage.getMinCpu();
	        long minram = cloudImage.getMinRam();
	        long mindisk = cloudImage.getMinDisk();
	        //拼装数据传给底层
	        JSONObject name = new JSONObject();			
	        name.put("op", "replace");
	        name.put("path", "/name");		
	        name.put("value", imageName);
			JSONObject ram = new JSONObject();			
			ram.put("op", "replace");
			ram.put("path", "/min_ram");		
			ram.put("value", minram*1024);
			JSONObject disk = new JSONObject();			
			disk.put("op", "replace");
			disk.put("path", "/min_disk");		
			disk.put("value", mindisk);
			JSONArray  resultData = new JSONArray();		
			resultData.add(name);
			resultData.add(ram);
			resultData.add(disk);
			Image img=openstackImageService.update(cloudImage, resultData.toString());
			if(null!=img){
				BaseCloudImage baseCloudImage = cloudImageDao.findOne(img.getId());
				baseCloudImage.setImageName(img.getName());
				baseCloudImage.setMinCpu(mincpu);
				baseCloudImage.setMinRam(minram*1024);
				baseCloudImage.setMinDisk(mindisk);
				baseCloudImage.setOsType(cloudImage.getOsType());
				baseCloudImage.setSysType(cloudImage.getSysType());
				
				baseCloudImage.setMaxCpu(null == cloudImage.getMaxCpu()?null : cloudImage.getMaxCpu());
				baseCloudImage.setMaxRam(null == cloudImage.getMaxRam()?null : cloudImage.getMaxRam()*1024);
				if(null==baseCloudImage.getCreatedTime()||"".equals(baseCloudImage.getCreatedTime().toString())){
					baseCloudImage.setCreatedTime(new Date());
				}
				
				
				baseCloudImage.setSysDetail(cloudImage.getSysDetail());
				baseCloudImage.setImageIspublic(cloudImage.getImageIspublic());
				
				if("1".equals(cloudImage.getImageIspublic().toString())){
					baseCloudImage.setImageDescription(cloudImage.getImageDescription());
					baseCloudImage.setProfessionType(null);
					baseCloudImage.setSysdiskSize(null );
					baseCloudImage.setProvider(null);
					baseCloudImage.setIntegratedSoftware(null);
				}else if("3".equals(cloudImage.getImageIspublic().toString())){
					baseCloudImage.setImageDescription(null);
					baseCloudImage.setProfessionType(cloudImage.getProfessionType());
					baseCloudImage.setSysdiskSize(null == cloudImage.getSysdiskSize()?null :cloudImage.getSysdiskSize());
					baseCloudImage.setProvider(cloudImage.getProvider());
					baseCloudImage.setIntegratedSoftware(cloudImage.getIntegratedSoftware());
				}
				
				baseCloudImage.setUpdatedTime(new Date());
				cloudImageDao.merge(baseCloudImage);
				isTrue=true;
			}
		}catch(AppException e){
			throw e;
		}
		return isTrue;
	}

	/**
	 * 删除镜像
	 * @Author: chengxiaodong
	 * @param cloudImage
	 */
	@Override
	public boolean deleteImage(CloudImage cloudImage) throws AppException {
		log.info("删除镜像");
		boolean isok = false;
		try {
			BaseCloudImage baseCloudImage=cloudImageDao.findOne(cloudImage.getImageId());
			if(null==baseCloudImage){
				throw new AppException("error.openstack.message", new String[]{"此镜像已被删除"});
			}else if(null!=baseCloudImage&&"DELETING".equals(baseCloudImage.getImageStatus())){
				throw new AppException("error.openstack.message", new String[]{"镜像正在删除中，请稍后"});
			}
			
			if(("1".equals(baseCloudImage.getImageIspublic().toString())||"3".equals(baseCloudImage.getImageIspublic().toString()))&&"1".equals(baseCloudImage.getIsUse().toString())){
				throw new AppException("error.openstack.message", new String[]{"请停用镜像后再操作"});
			}
			
			int countVm=vmService.countVmByImageId(baseCloudImage.getImageId());
			
			if(countVm>0){
				throw new AppException("error.openstack.message", new String[]{"有基于该镜像创建的云主机无法删除"});
			}
			
			if(("1".equals(baseCloudImage.getImageIspublic().toString())||"3".equals(baseCloudImage.getImageIspublic().toString()))){
				int countImage=cloudImageDao.countImageBySourceId(baseCloudImage.getImageId());
				
				if(countImage>0){
					throw new AppException("error.openstack.message", new String[]{"有基于该镜像创建的自定义镜像，无法删除"});
				}
			}
			
			
			boolean isExist = checkOrderVmByImage(cloudImage.getImageId());
			
			if(isExist){
				throw new AppException("error.openstack.message", new String[]{"用户有待创建的主机占用该镜像，无法删除"});
			}
			isok = openstackImageService.delete(cloudImage.getDcId(), cloudImage.getPrjId(), cloudImage.getImageId());
			if(isok){
				if("2".equals(cloudImage.getImageIspublic().toString())){
					baseCloudImage.setImageStatus("DELETING");
					cloudImageDao.merge(baseCloudImage);
					
					tagService.refreshCacheAftDelRes("privateImage",cloudImage.getImageId());
					
					JSONObject json =new JSONObject();
					json.put("imageId",baseCloudImage.getImageId());
					json.put("dcId",baseCloudImage.getDcId());
					json.put("prjId",baseCloudImage.getPrjId());
					json.put("imageStatus",baseCloudImage.getImageStatus());
					json.put("count", "0");
					jedisUtil.addUnique(RedisKey.imageKey, json.toJSONString());
				}else{
					cloudImageDao.delete(cloudImage.getImageId());
					billFactorService.deleteImagePrice(cloudImage.getImageId(), cloudImage.getDcId());
				}
			}
		} catch (AppException e) {
		    log.error(e.toString(),e);
			throw e;
		}
		return isok;
	}

	/**
	 * 查看自定义镜像详情
	 * @Author: duanbinbin
	 * @param imageId
	 * @return
	 *<li>Date: 2016年4月28日</li>
	 */
	@Override
	public CloudImage getPersonImageById(String imageId) {
		log.info("查看自定义镜像详情");
		CloudImage image = new CloudImage();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("ci.image_id, ");
		sql.append("ci.image_name, ");
		sql.append("ci.image_status, ");
		sql.append("ci.sys_type, ");
		sql.append("ci.created_time, ");
		sql.append("ci.image_description, ");
		sql.append("sdt.node_name, ");
		sql.append("vmnum.num, ");
		sql.append("dc.dc_name, ");
		sql.append("prj.prj_name, ");
		sql.append("cus.cus_org, ");
		sql.append("ci.dc_id, ");
		sql.append("ci.prj_id, ");
		sql.append("ci.min_cpu, ");
		sql.append("ci.min_ram, ");
		sql.append("ci.min_disk, ");
		sql.append("ci.max_cpu, ");
		sql.append("ci.max_ram, ");
		sql.append("ci.source_id, ");
		sql.append("img.image_name as sourceName, ");
		sql.append("img.image_ispublic as sourceType  ");
		sql.append("FROM cloud_image ci ");
		sql.append("JOIN cloud_image img ON ci.source_id=img.image_id ");
		sql.append("LEFT JOIN dc_datacenter dc ON ci.dc_id = dc.id ");
		sql.append("LEFT JOIN cloud_project prj ON ci.prj_id = prj.prj_id ");
		sql.append("LEFT JOIN sys_selfcustomer cus ON prj.customer_id = cus.cus_id ");
		sql.append("LEFT JOIN sys_data_tree sdt  ON ci.sys_type = sdt.node_id ");
		sql.append("LEFT JOIN ");
		sql.append("( ");
		sql.append("SELECT COUNT(vm.vm_id) AS num,vm.from_imageid ");
		sql.append("FROM cloud_vm vm WHERE vm.is_deleted = '0' or vm.is_deleted='2' GROUP BY vm.from_imageid ");
		sql.append(") ");
		sql.append("AS vmnum ON ci.image_id = vmnum.from_imageid ");
		sql.append("WHERE ci.image_ispublic = '2' ");
		sql.append("AND ci.image_id = ?");
		
		javax.persistence.Query query = cloudImageDao.createSQLNativeQuery(sql.toString(), imageId);
		if(null != query && query.getResultList().size() > 0){
			Object[] obj = (Object[]) query.getResultList().get(0);
			image.setImageId(String.valueOf(obj[0]));
			image.setImageName(String.valueOf(obj[1]));
			image.setImageStatus(String.valueOf(obj[2]));
			image.setSysType(String.valueOf(obj[3]));
			image.setCreatedTime((Date)obj[4]);
			image.setImageDescription(String.valueOf(obj[5]));
			image.setSysTypeName(String.valueOf(obj[6]));
			image.setVmNum(Integer.parseInt(null != obj[7] ? String.valueOf(obj[7]) : "0"));
			image.setDcName(String.valueOf(obj[8]));
			image.setPrjName(String.valueOf(obj[9]));
			image.setCusOrg(String.valueOf(obj[10]));
			image.setDcId(String.valueOf(obj[11]));
			image.setPrjId(String.valueOf(obj[12]));
			image.setMinCpu(Long.parseLong(null != obj[13] ? String.valueOf(obj[13]) : "0"));
			int number=Integer.parseInt(null != obj[14] ? String.valueOf(obj[14]) : "0");
			if(0==number){
				image.setMinRam(Long.parseLong(null != obj[14] ? String.valueOf(obj[14]) : "0")/1024);//库里存的是MB，转换为GB
			}else{
				if((Integer.parseInt(null != obj[14] ? String.valueOf(obj[14]) : "0")%1024)==0){
					image.setMinRam(Long.parseLong(null != obj[14] ? String.valueOf(obj[14]) : "0")/1024);
				}else{
					image.setMinRam(Long.parseLong(null != obj[14] ? String.valueOf(obj[14]) : "0")/1024+1);//库里存的是MB，转换为GB,向上取整，例如：1000MB=1GB
				}	
			}
			image.setMinDisk(Long.parseLong(null != obj[15] ? String.valueOf(obj[15]) : "0"));
			image.setMaxCpu(null == obj[16] || "".equals(String.valueOf(obj[16]))? null : Integer.valueOf(String.valueOf(obj[16])));
			image.setMaxRam(null == obj[17] || "".equals(String.valueOf(obj[17]))? null : Integer.valueOf(String.valueOf(obj[17]))/1024);
			image.setSourceId(String.valueOf(obj[18]));
			image.setSourceName(String.valueOf(obj[19]));
			image.setSourceType(String.valueOf(obj[20]));
			image.setStatusForDis(DictUtil.getStatusByNodeEn("image", image.getImageStatus()));
		}
		
		return image;
		
	}

	/**
	 * 查看公共镜像详情
	 * @Author: duanbinbin
	 * @param imageId
	 * @return
	 *<li>Date: 2016年4月28日</li>
	 */
	@Override
	public CloudImage getPublicImageById(String imageId) {
		log.info("查看公共镜像详情");
		CloudImage image = new CloudImage();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("ci.image_id, ");
		sql.append("ci.image_name, ");
		sql.append("ci.image_status, ");
		sql.append("ci.os_type, ");
		sql.append("ci.sys_type, ");
		sql.append("ci.created_time, ");
		sql.append("ci.min_cpu, ");
		sql.append("ci.min_ram, ");
		sql.append("ci.min_disk, ");
		sql.append("ci.disk_format, ");
		sql.append("sdt.node_name, ");
		sql.append("vmnum.num ,");
		sql.append("dc.dc_name, ");
		sql.append("ci.dc_id, ");
		sql.append("ci.image_description, ");
		sql.append("ci.max_cpu, ");
		sql.append("ci.max_ram, ");
		sql.append("ci.sys_detail, ");
		sql.append("ci.is_use ");
		sql.append("FROM cloud_image ci ");
		sql.append("LEFT JOIN dc_datacenter dc ON ci.dc_id = dc.id ");
		sql.append("LEFT JOIN sys_data_tree sdt  ON ci.sys_type = sdt.node_id ");
		sql.append("LEFT JOIN ");
		sql.append("( ");
		sql.append("SELECT COUNT(vm.vm_id) AS num,vm.from_imageid ");
		sql.append("FROM cloud_vm vm WHERE vm.is_deleted = '0' or vm.is_deleted='2' GROUP BY vm.from_imageid ");
		sql.append(") ");
		sql.append("AS vmnum ON ci.image_id = vmnum.from_imageid ");
		sql.append("WHERE ci.image_ispublic = '1' ");
		sql.append("AND ci.image_id = ?");
		
		javax.persistence.Query query = cloudImageDao.createSQLNativeQuery(sql.toString(), imageId);
		
		if(null != query && query.getResultList().size() > 0){
			Object[] obj = (Object[]) query.getResultList().get(0);
			image.setImageId(String.valueOf(obj[0]));
			image.setImageName(String.valueOf(obj[1]));
			image.setImageStatus(String.valueOf(obj[2]));
			String osType = String.valueOf(obj[3]);
			image.setOsType(osType);
			image.setSysType(String.valueOf(obj[4]));
			image.setCreatedTime((Date)obj[5]);
			image.setMinCpu(Long.parseLong(null != obj[6] ? String.valueOf(obj[6]) : "0"));
			int number=Integer.parseInt(null != obj[7] ? String.valueOf(obj[7]) : "0");
			if(0==number){
				image.setMinRam(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0")/1024);//库里存的是MB，转换为GB
			}else{
				if((Integer.parseInt(null != obj[7] ? String.valueOf(obj[7]) : "0")%1024)==0){
					image.setMinRam(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0")/1024);
				}else{
					image.setMinRam(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0")/1024+1);//库里存的是MB，转换为GB,向上取整，例如：1000MB=1GB
				}	
			}
			image.setMinDisk(Long.parseLong(null != obj[8] ? String.valueOf(obj[8]) : "0"));
			image.setDiskFormat(String.valueOf(obj[9]));
			image.setSysTypeName(String.valueOf(obj[10]));
			image.setVmNum(Integer.parseInt(null != obj[11] ? String.valueOf(obj[11]) : "0"));
			image.setDcName(String.valueOf(obj[12]));
			image.setDcId(String.valueOf(obj[13]));
			image.setImageDescription(String.valueOf(obj[14]));
			image.setMaxCpu(null == obj[15] || "".equals(String.valueOf(obj[15]))? null : Integer.valueOf(String.valueOf(obj[15])));
			image.setMaxRam(null == obj[16] || "".equals(String.valueOf(obj[16]))? null : Integer.valueOf(String.valueOf(obj[16]))/1024);
			image.setSysDetail(String.valueOf(obj[17]));
			image.setIsUse((Character)obj[18]);
			if(null!=osType&&!"".equals(osType)&&!"null".equals(osType)){
				image.setOsTypeName(DictUtil.getDataTreeByNodeId(osType).getNodeName());
			}
			image.setStatusForDis(escapseImageState(image));
			
		}
		return image;
	}

	/**
	 * 获取镜像格式列表
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年4月28日</li>
	 */
	@Override
	public List<SysDataTree> getImageFormatList() {
		return getSysDataListByParentId("0007002016");
	}
	public List<SysDataTree> getSysDataListByParentId(String parentId){
		return DictUtil.getDataTreeByParentId(parentId);
	}

	/**
	 * 校验镜像名称（包括自定义镜像和公共镜像）
	 * @Author: duanbinbin
	 * @param cloudImage
	 * @return
	 *<li>Date: 2016年4月28日</li>
	 */
	@Override
	public boolean checkImageName(CloudImage cloudImage) {
		log.info("验证镜像名称");
		boolean isok = false;
		int count = 1;
		String name = null != cloudImage.getImageName()? cloudImage.getImageName():"";
		String imageId = null != cloudImage.getImageId()?cloudImage.getImageId():"";
		String dcId = null != cloudImage.getDcId()?cloudImage.getDcId():"";
		String prjId = null != cloudImage.getPrjId()? cloudImage.getPrjId():"";
		String imageIspublic = null != cloudImage.getImageIspublic()? Character.toString(cloudImage.getImageIspublic()):"";
		
		if("".equals(imageId)){
			//新增
			if("1".equals(imageIspublic)||"3".equals(imageIspublic)){
				//公共镜像
				count = cloudImageDao.getCountByNameDcId(name, dcId);
			}else if("2".equals(imageIspublic)){
				//自定义镜像
				count = cloudImageDao.getCountByNamePrjId(name, prjId);
			}
		}else{
			//编辑
			if("1".equals(imageIspublic)||"3".equals(imageIspublic)){
				//公共镜像
				count = cloudImageDao.getCountByNameDcIdImageId(name, dcId,imageId);
			}else if("2".equals(imageIspublic)){
				//自定义镜像
				count = cloudImageDao.getCountByNamePrjIdImageId(name, prjId,imageId);
			}else if("9".equals(imageIspublic)){
				count=0;
			}
		}
		if(count == 0){
			isok = true;
		}
		return isok;
	}

	@Override
	public int getImageCountByPrjId(String prjId) {
		int countImage = cloudImageDao.getImageCountByPrjId(prjId, '2');
		return countImage;
	}
	
	/**
	 * 根据镜像ID查询正在处理中的云主机列表
	 * ------------------------------
	 * @author zhouhaitao
	 * @param imageId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private boolean checkOrderVmByImage(String imageId){
		boolean isExist = false;
		StringBuffer sql = new StringBuffer();
		sql.append("	SELECT                                                 ");
		sql.append("		cov.ordervm_id                                     ");
		sql.append("	FROM                                                   ");
		sql.append("		cloudorder_vm cov                                  ");
		sql.append("	LEFT JOIN order_info oi ON oi.order_no = cov.order_no  ");
		sql.append("	WHERE                                                  ");
		sql.append("		image_id = ?                                       ");
		sql.append("	AND oi.order_state in ('2','1');                       ");
		
		javax.persistence.Query query = cloudImageDao.createSQLNativeQuery(sql.toString(), new Object[]{imageId});
		List list = query.getResultList();
		isExist = list!=null && list.size()>0;
		return isExist;
	}

	
	/**
	 * 启用镜像的方法
	 */
	public boolean useImage(CloudImage cloudImage) {
		BaseCloudImage baseImage=cloudImageDao.findOne(cloudImage.getImageId());
		baseImage.setIsUse('1');
		cloudImageDao.saveOrUpdate(baseImage);
		return true;
	}
	

	/**
	 * 停用镜像的方法
	 */
	@Override
	public boolean closeImage(CloudImage cloudImage) {
		BaseCloudImage baseImage=cloudImageDao.findOne(cloudImage.getImageId());
		baseImage.setIsUse('2');
		cloudImageDao.saveOrUpdate(baseImage);
		return true;
	}

	@Override
	public List<SysDataTree> getOsTypeList() {
		List<SysDataTree> sysList = new ArrayList<SysDataTree>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT node_id,node_name, parent_id ");
		sql.append(" FROM sys_data_tree WHERE ");
		sql.append(" node_id = ? OR	parent_id = ? ");
		javax.persistence.Query query = cloudImageDao.createSQLNativeQuery(sql.toString(), new Object[]{ConstantClazz.DICT_CLOUD_OS_WINDOWS_NODE_ID,ConstantClazz.DICT_CLOUD_OS_LINUX_NODE_ID});
		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if (result != null && result.size() > 0) {
			for (int i = 0; i < result.size(); i++) {
				int index = 0;
				Object[] objs = (Object[]) result.get(i);
				SysDataTree sdt = new SysDataTree();
				sdt.setNodeId(String.valueOf(objs[index++]));
				sdt.setNodeName(String.valueOf(objs[index++]));
				sdt.setParentId(String.valueOf(objs[index++]));
				sysList.add(sdt);
			}
		}
		return sysList;
	}

	@Override
	public List<SysDataTree> getMarketTypeList() {
		return getMonitorMngData(RedisNodeIdConstant.MARKET_BUSINESS_TYPE);
	}
	
	public List<SysDataTree> getMonitorMngData(String parentId) {
        List<SysDataTree> dataList = new ArrayList<SysDataTree>();
        Set<String> mngDataSet = null;
        List<String> mngDataList = new ArrayList<String>();
        try {
            mngDataSet = jedisUtil.getSet(RedisKey.SYS_DATA_TREE_PARENT_NODEID + parentId);
            for (String mngData : mngDataSet) {
                mngDataList.add(mngData);
            }
            Collections.sort(mngDataList);
            for (String mngData : mngDataList) {
                String jsonMng = jedisUtil.get(RedisKey.SYS_DATA_TREE + mngData);

                JSONObject monitorMng = JSONObject.parseObject(jsonMng);
                SysDataTree treeData = new SysDataTree();
                treeData.setNodeName(monitorMng.getString("nodeName"));
                treeData.setNodeId(monitorMng.getString("nodeId"));
                treeData.setParentId(monitorMng.getString("parentId"));
                dataList.add(treeData);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new AppException("查询redis数据异常：" + parentId);
        }
        return dataList;
    }

	
	/**
	 * 查询市场镜像列表
	 */
	@Override
	public Page getMarketImagePageList(Page page, QueryMap queryMap,
			String dcId, String professionType,String sysType, String imageName, String isUse) {
		log.info("查询市场镜像列表");
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("ci.image_id, ");
		sql.append("ci.image_name, ");
		sql.append("ci.dc_id, ");
		sql.append("ci.image_status, ");
		sql.append("ci.sys_type, ");
		sql.append("ci.created_time, ");
		sql.append("ci.image_ispublic, ");
		sql.append("ci.min_cpu, ");
		sql.append("ci.min_ram, ");
		sql.append("ci.min_disk, ");
		sql.append("ci.disk_format, ");
		sql.append("sdt.node_name, ");
		sql.append("vmnum.num, ");
		sql.append("dc.dc_name, ");
		sql.append("ci.os_type, ");
		sql.append("ci.image_description, ");
		sql.append("ci.max_cpu, ");
		sql.append("ci.max_ram, ");
		sql.append("ci.sys_detail, ");
		sql.append("ci.is_use, ");
		sql.append("ci.sysdisk_size, ");
		sql.append("ci.provider, ");
		sql.append("ci.integrated_software, ");
		sql.append("ci.profession_type, ");
		sql.append("ci.marketimage_depict, ");
		sql.append("tree.node_name as treeName ");
		sql.append("FROM cloud_image ci ");
		sql.append("LEFT JOIN dc_datacenter dc ON ci.dc_id = dc.id ");
		sql.append("LEFT JOIN sys_data_tree sdt  ON ci.sys_type = sdt.node_id ");
		sql.append("LEFT JOIN sys_data_tree tree ON ci.profession_type = tree.node_id ");
		sql.append("LEFT JOIN ");
		sql.append("( ");
		sql.append("SELECT COUNT(vm.vm_id) AS num,vm.from_imageid ");
		sql.append("FROM cloud_vm vm WHERE vm.is_visable='1' and ( vm.is_deleted = '0' or vm.is_deleted='2' ) GROUP BY vm.from_imageid ");
		sql.append(") ");
		sql.append("AS vmnum ON ci.image_id = vmnum.from_imageid ");
		sql.append("WHERE ci.image_ispublic = '3' ");
		if(!"".equals(dcId)){
			sql.append("and ci.dc_id = ? ");
			list.add(dcId);
		}
		if(!"".equals(professionType)){
			sql.append("and ci.profession_type = ? ");
			list.add(professionType);
		}
		if(!"".equals(sysType)){
			if(ConstantClazz.DICT_CLOUD_OS_WINDOWS_NODE_ID.equals(sysType)){
				sql.append("and ci.os_type = ? ");
			}else{
				sql.append("and ci.sys_type = ? ");
			}
			list.add(sysType);
		}
		if(!"".equals(imageName) && null != imageName){
			imageName = imageName.replaceAll("\\_", "\\\\_");
			sql.append("and binary ci.image_name like ? ");
			list.add("%" + imageName + "%");
		}
		if(!"".equals(isUse) && null != isUse){
			sql.append("and ci.is_use = ? ");
			if("1".equals(isUse)){
				sql.append("and ci.image_status = 'ACTIVE' ");
			}
			list.add(isUse);
			
		}
		sql.append("ORDER BY ci.dc_id,ci.sys_type,ci.created_time DESC ");
		page = cloudImageDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
		List resultList = (List) page.getResult();
		for(int i = 0; i < resultList.size(); i++){
			Object[] obj = (Object[]) resultList.get(i);
			CloudImage cloudImage = new CloudImage();
			cloudImage.setImageId(String.valueOf(obj[0]));
			cloudImage.setImageName(String.valueOf(obj[1]));
			cloudImage.setDcId(String.valueOf(obj[2]));
			cloudImage.setImageStatus(String.valueOf(obj[3]));
			cloudImage.setSysType(String.valueOf(obj[4]));
			cloudImage.setCreatedTime((Date)obj[5]);
			cloudImage.setImageIspublic((Character)obj[6]);
			cloudImage.setMinCpu(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0"));
			int number=Integer.parseInt(null != obj[8] ? String.valueOf(obj[8]) : "0");
			if(0==number){
				cloudImage.setMinRam(Long.parseLong(null != obj[8] ? String.valueOf(obj[8]) : "0")/1024);//库里存的是MB，转换为GB
			}else{
				if((Integer.parseInt(null != obj[8] ? String.valueOf(obj[8]) : "0")%1024)==0){
					cloudImage.setMinRam(Long.parseLong(null != obj[8] ? String.valueOf(obj[8]) : "0")/1024);
				}else{
					cloudImage.setMinRam(Long.parseLong(null != obj[8] ? String.valueOf(obj[8]) : "0")/1024+1);//库里存的是MB，转换为GB,向上取整，例如：1000MB=1GB
				}	
			}
			cloudImage.setMinDisk(Long.parseLong(null != obj[9] ? String.valueOf(obj[9]) : "0"));
			cloudImage.setDiskFormat(String.valueOf(obj[10]));
			cloudImage.setSysTypeName(String.valueOf(obj[11]));
			cloudImage.setVmNum(Integer.parseInt(null != obj[12] ? String.valueOf(obj[12]) : "0"));
			cloudImage.setDcName(String.valueOf(obj[13]));
			cloudImage.setOsType(String.valueOf(obj[14]));
			cloudImage.setImageDescription(String.valueOf(obj[15]));
			
			cloudImage.setMaxCpu(null == obj[16] || "".equals(String.valueOf(obj[16]))? null : Integer.valueOf(String.valueOf(obj[16])));
			cloudImage.setMaxRam(null == obj[17] || "".equals(String.valueOf(obj[17]))? null : Integer.valueOf(String.valueOf(obj[17]))/1024);
			cloudImage.setSysDetail(String.valueOf(obj[18]));
			cloudImage.setIsUse((Character)obj[19]);
			cloudImage.setSysdiskSize(Long.parseLong(null != obj[20] ? String.valueOf(obj[20]) : "0"));
			cloudImage.setProvider(String.valueOf(obj[21]));
			cloudImage.setIntegratedSoftware(String.valueOf(obj[22]));
			cloudImage.setProfessionType(String.valueOf(obj[23]));
			cloudImage.setMarketimageDepict(null!=obj[24]?String.valueOf(obj[24]):"");
			cloudImage.setProfessionName(String.valueOf(obj[25]));
			cloudImage.setStatusForDis(escapseImageState(cloudImage));
			
			resultList.set(i, cloudImage);
		}
		return page;
	}

	
	/**
	 * 查询未分类镜像列表
	 */
	@Override
	public Page getUnclassifiedImagePageList(Page page, QueryMap queryMap,
			String dcId, String imageName) {
		
		log.info("查询未分类镜像列表");
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("ci.image_id, ");
		sql.append("ci.image_name, ");
		sql.append("ci.dc_id, ");
		sql.append("ci.image_status, ");
		sql.append("ci.created_time, ");
		sql.append("ci.image_ispublic, ");
		sql.append("ci.min_cpu, ");
		sql.append("ci.min_ram, ");
		sql.append("ci.min_disk, ");
		sql.append("ci.disk_format, ");
		sql.append("dc.dc_name, ");
		sql.append("ci.image_description, ");
		sql.append("ci.is_use ");
		sql.append("FROM cloud_image ci ");
		sql.append("LEFT JOIN dc_datacenter dc ON ci.dc_id = dc.id ");
		sql.append("WHERE ci.image_ispublic = '9' ");
		if(!"".equals(dcId)){
			sql.append("and ci.dc_id = ? ");
			list.add(dcId);
		}
		
		if(!"".equals(imageName) && null != imageName){
			imageName = imageName.replaceAll("\\_", "\\\\_");
			sql.append("and binary ci.image_name like ? ");
			list.add("%" + imageName + "%");
		}
		sql.append("ORDER BY ci.dc_id,ci.created_time DESC ");
		page = cloudImageDao.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
		List resultList = (List) page.getResult();
		for(int i = 0; i < resultList.size(); i++){
			Object[] obj = (Object[]) resultList.get(i);
			CloudImage cloudImage = new CloudImage();
			cloudImage.setImageId(String.valueOf(obj[0]));
			cloudImage.setImageName(String.valueOf(obj[1]));
			cloudImage.setDcId(String.valueOf(obj[2]));
			cloudImage.setImageStatus(String.valueOf(obj[3]));
			cloudImage.setCreatedTime((Date)obj[4]);
			cloudImage.setImageIspublic((Character)obj[5]);
			cloudImage.setMinCpu(Long.parseLong(null != obj[6] ? String.valueOf(obj[6]) : "0"));
			int number=Integer.parseInt(null != obj[7] ? String.valueOf(obj[7]) : "0");
			if(0==number){
				cloudImage.setMinRam(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0")/1024);//库里存的是MB，转换为GB
			}else{
				if((Integer.parseInt(null != obj[7] ? String.valueOf(obj[7]) : "0")%1024)==0){
					cloudImage.setMinRam(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0")/1024);
				}else{
					cloudImage.setMinRam(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0")/1024+1);//库里存的是MB，转换为GB,向上取整，例如：1000MB=1GB
				}	
			}
			cloudImage.setMinDisk(Long.parseLong(null != obj[8] ? String.valueOf(obj[8]) : "0"));
			cloudImage.setDiskFormat(String.valueOf(obj[9]));
			cloudImage.setDcName(String.valueOf(obj[10]));
			cloudImage.setImageDescription(String.valueOf(obj[11]));
			cloudImage.setIsUse((Character)obj[12]);
			cloudImage.setStatusForDis(escapseImageState(cloudImage));
			
			resultList.set(i, cloudImage);
		}
		return page;
		
	}

	

	
	/**
	 * 编辑市场镜像
	 * @Author: chengxiaodong
	 * @param cloudImage
	 * @return
	 * @throws AppException
	 */
	@Override
	public boolean updateMarketImage(CloudImage cloudImage) throws AppException {
		log.info("编辑市场镜像");
		boolean isTrue=false;
		try{
			
	        String imageName =cloudImage.getImageName();
	        long mincpu = cloudImage.getMinCpu();
	        long minram = cloudImage.getMinRam();
	        long mindisk = cloudImage.getMinDisk();
	        //拼装数据传给底层
	        JSONObject name = new JSONObject();			
	        name.put("op", "replace");
	        name.put("path", "/name");		
	        name.put("value", imageName);
			JSONObject ram = new JSONObject();			
			ram.put("op", "replace");
			ram.put("path", "/min_ram");		
			ram.put("value", minram*1024);
			JSONObject disk = new JSONObject();			
			disk.put("op", "replace");
			disk.put("path", "/min_disk");		
			disk.put("value", mindisk);
			JSONArray  resultData = new JSONArray();		
			resultData.add(name);
			resultData.add(ram);
			resultData.add(disk);
			Image img=openstackImageService.update(cloudImage, resultData.toString());
			if(null!=img){
				BaseCloudImage baseCloudImage = cloudImageDao.findOne(img.getId());
				baseCloudImage.setImageName(img.getName());
				baseCloudImage.setMinCpu(mincpu);
				baseCloudImage.setMinRam(minram*1024);
				baseCloudImage.setMinDisk(mindisk);
				baseCloudImage.setOsType(cloudImage.getOsType());
				baseCloudImage.setSysType(cloudImage.getSysType());
				
				baseCloudImage.setMaxCpu(null == cloudImage.getMaxCpu()?null : cloudImage.getMaxCpu());
				baseCloudImage.setMaxRam(null == cloudImage.getMaxRam()?null : cloudImage.getMaxRam()*1024);
				if(null==baseCloudImage.getCreatedTime()||"".equals(baseCloudImage.getCreatedTime().toString())){
					baseCloudImage.setCreatedTime(new Date());
				}
				
				
				baseCloudImage.setSysDetail(cloudImage.getSysDetail());
				baseCloudImage.setImageIspublic(cloudImage.getImageIspublic());
				
				if("1".equals(cloudImage.getImageIspublic().toString())){
					baseCloudImage.setImageDescription(cloudImage.getImageDescription());
					baseCloudImage.setProfessionType(null);
					baseCloudImage.setSysdiskSize(null );
					baseCloudImage.setProvider(null);
					baseCloudImage.setIntegratedSoftware(null);
				}else if("3".equals(cloudImage.getImageIspublic().toString())){
					baseCloudImage.setImageDescription(null);
					baseCloudImage.setProfessionType(cloudImage.getProfessionType());
					baseCloudImage.setSysdiskSize(null == cloudImage.getSysdiskSize()?null :cloudImage.getSysdiskSize());
					baseCloudImage.setProvider(cloudImage.getProvider());
					baseCloudImage.setIntegratedSoftware(cloudImage.getIntegratedSoftware());
				}
				
				baseCloudImage.setUpdatedTime(new Date());
				cloudImageDao.merge(baseCloudImage);
				isTrue=true;
			}
		}catch(AppException e){
			throw e;
		}
		return isTrue;
	}


	
	/**
	 * 上传市场镜像
	 * @Author: chengxiaodong
	 * @param cloudImage
	 * @return
	 */
	@Override
	public CloudImage createMarketImage(CloudImage cloudImage,InputStream is) {
		BaseEcmcSysUser ecmcSysUser = EcmcSessionUtil.getUser();
		String name = cloudImage.getImageName();
		String imageIspublic =  "";
		String imageFormat = cloudImage.getDiskFormat();
		String url =  cloudImage.getImageUrl();
		long mincpu = cloudImage.getMinCpu();
		String osType = cloudImage.getOsType();
		
		long minram = cloudImage.getMinRam();
		long mindisk = cloudImage.getMinDisk();
		
		//2、调用底层新增镜像
		Map<String,String> map =new HashMap<String, String>();
		map.put("name",name);
		map.put("isPublic", "");
		map.put("imageFormat", imageFormat);
		map.put("minDisk", String.valueOf(mindisk));
		map.put("minRam", String.valueOf(minram*1024));//前台传的都为GB，需转换为MB
		map.put("description", null);
		map.put("url", url);
		org.openstack4j.model.image.Image image = null;
		image = createImageBySdk(cloudImage.getDcId(),map,is);
		if(null != image){
			BaseCloudImage baseCloudImage = new BaseCloudImage();
			baseCloudImage.setImageId(image.getId());
			baseCloudImage.setImageName(name);
			baseCloudImage.setDcId(cloudImage.getDcId());
			baseCloudImage.setDiskFormat(imageFormat.toLowerCase());
			baseCloudImage.setOwnerId(image.getOwner());
			baseCloudImage.setOsType(osType);
			baseCloudImage.setSysType(cloudImage.getSysType());
			baseCloudImage.setMinDisk(mindisk);
			baseCloudImage.setMinRam(minram*1024);
			baseCloudImage.setImageIspublic('3');//市场镜像
			if(null!=url&&!"null".equals(url)&&!"".equals(url)){
				baseCloudImage.setImageUrl(url);
			}
			baseCloudImage.setCreateName(ecmcSysUser.getAccount());
			baseCloudImage.setCreatedTime(new Date());
			baseCloudImage.setImageStatus(image.getStatus().value().toUpperCase());
			baseCloudImage.setImageSize(new BigDecimal(image.getSize()));
			baseCloudImage.setContainerFormat(image.getContainerFormat().toString());
			baseCloudImage.setMinCpu(mincpu);
			baseCloudImage.setMaxCpu(cloudImage.getMaxCpu());
			baseCloudImage.setMaxRam(cloudImage.getMaxRam());
			
			baseCloudImage.setSysDetail(cloudImage.getSysDetail());
			baseCloudImage.setIsUse('0');
			baseCloudImage.setProvider(cloudImage.getProvider());
			baseCloudImage.setProfessionType(cloudImage.getProfessionType());
			baseCloudImage.setIntegratedSoftware(cloudImage.getIntegratedSoftware());
			baseCloudImage.setSysdiskSize(cloudImage.getSysdiskSize());
			
			cloudImageDao.saveEntity(baseCloudImage);
			BeanUtils.copyPropertiesByModel(cloudImage, baseCloudImage);
			
			//增加默认价格
			BillingFactor  billingFactor=new BillingFactor();
			billingFactor.setResourcesType("IMAGE");
			billingFactor.setFactorUnit(baseCloudImage.getImageId());
			billingFactor.setStartNum(Long.parseLong("1"));
			billingFactor.setEndNum(Long.parseLong("-1"));
			billingFactor.setBillingFactor(null);
			billingFactor.setDcId(baseCloudImage.getDcId());
			billingFactor.setPrice(new BigDecimal(0));
			billFactorService.addFactorPrice(billingFactor, true);
		}
		
		return cloudImage;
	}

	
	/**
	 * 编辑未分类镜像
	 * @Author: chengxiaodong
	 * @param cloudImage
	 * @return
	 * @throws AppException
	 */
	@Override
	public boolean updateUnclassifiedImage(CloudImage cloudImage) throws AppException {
		log.info("编辑未分类镜像");
		boolean isTrue=false;
		try{
			
	        String imageName =cloudImage.getImageName();
	        long mincpu = cloudImage.getMinCpu();
	        long minram = cloudImage.getMinRam();
	        long mindisk = cloudImage.getMinDisk();
	        //拼装数据传给底层
	        JSONObject name = new JSONObject();			
	        name.put("op", "replace");
	        name.put("path", "/name");		
	        name.put("value", imageName);
			JSONObject ram = new JSONObject();			
			ram.put("op", "replace");
			ram.put("path", "/min_ram");		
			ram.put("value", minram*1024);
			JSONObject disk = new JSONObject();			
			disk.put("op", "replace");
			disk.put("path", "/min_disk");		
			disk.put("value", mindisk);
			JSONArray  resultData = new JSONArray();		
			resultData.add(name);
			resultData.add(ram);
			resultData.add(disk);
			Image img=openstackImageService.update(cloudImage, resultData.toString());
			if(null!=img){
				BaseCloudImage baseCloudImage = cloudImageDao.findOne(img.getId());
				baseCloudImage.setImageName(img.getName());
				baseCloudImage.setMinCpu(mincpu);
				baseCloudImage.setMinRam(minram*1024);
				baseCloudImage.setMinDisk(mindisk);
				baseCloudImage.setOsType(cloudImage.getOsType());
				baseCloudImage.setSysType(cloudImage.getSysType());
				
				baseCloudImage.setMaxCpu(null == cloudImage.getMaxCpu()?null : cloudImage.getMaxCpu());
				baseCloudImage.setMaxRam(null == cloudImage.getMaxRam()?null : cloudImage.getMaxRam()*1024);
				if(null==baseCloudImage.getCreatedTime()||"".equals(baseCloudImage.getCreatedTime().toString())){
					baseCloudImage.setCreatedTime(new Date());
				}
				
				
				baseCloudImage.setSysDetail(cloudImage.getSysDetail());
				baseCloudImage.setImageIspublic(cloudImage.getImageIspublic());
				
				if("1".equals(cloudImage.getImageIspublic().toString())){
					baseCloudImage.setImageDescription(cloudImage.getImageDescription());
					baseCloudImage.setProfessionType(null);
					baseCloudImage.setSysdiskSize(null );
					baseCloudImage.setProvider(null);
					baseCloudImage.setIntegratedSoftware(null);
				}else if("3".equals(cloudImage.getImageIspublic().toString())){
					baseCloudImage.setProfessionType(cloudImage.getProfessionType());
					baseCloudImage.setSysdiskSize(null == cloudImage.getSysdiskSize()?null :cloudImage.getSysdiskSize());
					baseCloudImage.setProvider(cloudImage.getProvider());
					baseCloudImage.setIntegratedSoftware(cloudImage.getIntegratedSoftware());
				}
				
				baseCloudImage.setUpdatedTime(new Date());
				cloudImageDao.merge(baseCloudImage);
				
				
				//初次确定镜像类型增加默认价格
				BillingFactor  billingFactor=new BillingFactor();
				billingFactor.setResourcesType("IMAGE");
				billingFactor.setFactorUnit(baseCloudImage.getImageId());
				billingFactor.setStartNum(Long.parseLong("1"));
				billingFactor.setEndNum(Long.parseLong("-1"));
				billingFactor.setBillingFactor(null);
				billingFactor.setDcId(baseCloudImage.getDcId());
				billingFactor.setPrice(new BigDecimal(0));
				billFactorService.addFactorPrice(billingFactor, true);
				
				isTrue=true;
			}
		}catch(AppException e){
			throw e;
		}
		return isTrue;
	}
	
	
	
	
	/**
	 * 查看市场镜像详情
	 * @Author: chengxiaodong
	 * @param imageId
	 * @return
	 */
	@Override
	public CloudImage getMarketImageById(String imageId) {
		log.info("查看市场镜像详情");
		CloudImage image = new CloudImage();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ");
		sql.append("ci.image_id, ");
		sql.append("ci.image_name, ");
		sql.append("ci.image_status, ");
		sql.append("ci.os_type, ");
		sql.append("ci.sys_type, ");
		sql.append("ci.created_time, ");
		sql.append("ci.min_cpu, ");
		sql.append("ci.min_ram, ");
		sql.append("ci.min_disk, ");
		sql.append("ci.disk_format, ");
		sql.append("sdt.node_name, ");
		sql.append("vmnum.num ,");
		sql.append("dc.dc_name, ");
		sql.append("ci.dc_id, ");
		sql.append("ci.image_description, ");
		sql.append("ci.max_cpu, ");
		sql.append("ci.max_ram, ");
		sql.append("ci.sys_detail, ");
		sql.append("ci.is_use, ");
		sql.append("ci.sysdisk_size, ");
		sql.append("ci.provider, ");
		sql.append("ci.integrated_software, ");
		sql.append("ci.profession_type, ");
		sql.append("ci.marketimage_depict, ");
		sql.append("tree.node_name as treeName ");
		sql.append("FROM cloud_image ci ");
		sql.append("LEFT JOIN dc_datacenter dc ON ci.dc_id = dc.id ");
		sql.append("LEFT JOIN sys_data_tree sdt  ON ci.sys_type = sdt.node_id ");
		sql.append("LEFT JOIN sys_data_tree tree ON ci.profession_type = tree.node_id ");
		sql.append("LEFT JOIN ");
		sql.append("( ");
		sql.append("SELECT COUNT(vm.vm_id) AS num,vm.from_imageid ");
		sql.append("FROM cloud_vm vm WHERE vm.is_deleted = '0' or vm.is_deleted='2' GROUP BY vm.from_imageid ");
		sql.append(") ");
		sql.append("AS vmnum ON ci.image_id = vmnum.from_imageid ");
		sql.append("WHERE ci.image_ispublic = '3' ");
		sql.append("AND ci.image_id = ?");
		
		javax.persistence.Query query = cloudImageDao.createSQLNativeQuery(sql.toString(), imageId);
		
		if(null != query && query.getResultList().size() > 0){
			Object[] obj = (Object[]) query.getResultList().get(0);
			image.setImageId(String.valueOf(obj[0]));
			image.setImageName(String.valueOf(obj[1]));
			image.setImageStatus(String.valueOf(obj[2]));
			String osType = String.valueOf(obj[3]);
			image.setOsType(osType);
			image.setSysType(String.valueOf(obj[4]));
			image.setCreatedTime((Date)obj[5]);
			image.setMinCpu(Long.parseLong(null != obj[6] ? String.valueOf(obj[6]) : "0"));
			int number=Integer.parseInt(null != obj[7] ? String.valueOf(obj[7]) : "0");
			if(0==number){
				image.setMinRam(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0")/1024);//库里存的是MB，转换为GB
			}else{
				if((Integer.parseInt(null != obj[7] ? String.valueOf(obj[7]) : "0")%1024)==0){
					image.setMinRam(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0")/1024);
				}else{
					image.setMinRam(Long.parseLong(null != obj[7] ? String.valueOf(obj[7]) : "0")/1024+1);//库里存的是MB，转换为GB,向上取整，例如：1000MB=1GB
				}	
			}
			image.setMinDisk(Long.parseLong(null != obj[8] ? String.valueOf(obj[8]) : "0"));
			image.setDiskFormat(String.valueOf(obj[9]));
			image.setSysTypeName(String.valueOf(obj[10]));
			image.setVmNum(Integer.parseInt(null != obj[11] ? String.valueOf(obj[11]) : "0"));
			image.setDcName(String.valueOf(obj[12]));
			image.setDcId(String.valueOf(obj[13]));
			image.setImageDescription(String.valueOf(obj[14]));
			image.setMaxCpu(null == obj[15] || "".equals(String.valueOf(obj[15]))? null : Integer.valueOf(String.valueOf(obj[15])));
			image.setMaxRam(null == obj[16] || "".equals(String.valueOf(obj[16]))? null : Integer.valueOf(String.valueOf(obj[16]))/1024);
			image.setSysDetail(String.valueOf(obj[17]));
			image.setIsUse((Character)obj[18]);
			image.setSysdiskSize(Long.parseLong(null != obj[19] ? String.valueOf(obj[19]) : "0"));
			image.setProvider(String.valueOf(obj[20]));
			image.setIntegratedSoftware(String.valueOf(obj[21]));
			image.setProfessionType(String.valueOf(obj[22]));
			image.setMarketimageDepict(null!=obj[23]?String.valueOf(obj[23]):"");
			image.setProfessionName(String.valueOf(obj[24]));
			if(null!=osType&&!"".equals(osType)&&!"null".equals(osType)){
				image.setOsTypeName(DictUtil.getDataTreeByNodeId(osType).getNodeName());
			}
			image.setStatusForDis(escapseImageState(image));
			
		}
		return image;
	}

	
	/**
	 * 编辑市场镜像描述
	 */
	@Override
	public boolean updateMarketImageDesc(CloudImage cloudImage) {
		BaseCloudImage baseCloudImage = cloudImageDao.findOne(cloudImage.getImageId());
		baseCloudImage.setMarketimageDepict(cloudImage.getMarketimageDepict());
		cloudImageDao.saveOrUpdate(baseCloudImage);
		return true;
	}
	
	
	

}
