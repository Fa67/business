package com.eayun.virtualization.ecmccontroller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.ecmcservice.EcmcCloudImageService;
import com.eayun.virtualization.model.CloudImage;

/**
 * ECMC镜像接口
 * @Filename: EcmcCloudImageController.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月27日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/ecmc/cloud/image")
@Scope("prototype")
public class EcmcCloudImageController {
	
	public final static Logger log = LoggerFactory.getLogger(EcmcCloudImageController.class);
	
	@Autowired
	private EcmcCloudImageService ecmcCloudImageService;
	
	@Autowired
    private EcmcLogService ecmcLogService;

	/**
	 * 查询自定义镜像列表
	 * @param request
	 * @param page
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/getimagepagelist" , method = RequestMethod.POST)
    @ResponseBody
    public String  getImagePageList(HttpServletRequest request,Page page,@RequestBody ParamsMap map){
    	log.info("查询自定义镜像列表开始");
    	String dcId = "";
    	String queryType = "";
    	String queryName = "";
    	String sourceType="";
    	if(null != map.getParams()){
    		dcId = null == map.getParams().get("dcId")?"":map.getParams().get("dcId").toString();
        	queryType = null == map.getParams().get("queryType")?"":map.getParams().get("queryType").toString();
        	//三类：模糊查询：名称（imageName）
        	//多选精确查询：项目（prjName）、客户（cusOrg）
        	queryName = null == map.getParams().get("queryName")?"":map.getParams().get("queryName").toString();
        	sourceType = null == map.getParams().get("sourceType")?"":map.getParams().get("sourceType").toString();
    	}
    	int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        try {
			page = ecmcCloudImageService.getImagePageList(page,queryMap,dcId,sourceType,queryType,queryName);
		} catch (Exception e) {
			log.error(e.toString(),e);
			throw e;
		}
    	return JSONObject.toJSONString(page);
    }
	
	/**
	 * 查询公共镜像
	 * @Author: chengxiaodong
	 * @param request
	 * @param page
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/getconimagepagelist" , method = RequestMethod.POST)
    @ResponseBody
    public String  getConImagePageList(HttpServletRequest request,Page page,@RequestBody ParamsMap map){
    	log.info("查询公共镜像列表开始");
    	String dcId = "";
    	String sysType = "";
    	String imageName = "";
    	String isUse="";
    	
    	if(null != map.getParams()){
    		dcId = null == map.getParams().get("dcId")?"":map.getParams().get("dcId").toString();
        	sysType = null == map.getParams().get("sysType")?"":map.getParams().get("sysType").toString();
        	imageName = null == map.getParams().get("imageName")?"":map.getParams().get("imageName").toString();
        	isUse=null==map.getParams().get("isUse")?"":map.getParams().get("isUse").toString();
    	}
    	int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        try {
			page = ecmcCloudImageService.getConImagePageList(page,queryMap,dcId,sysType,imageName,isUse);
		} catch (Exception e) {
			log.error(e.toString(),e);
			throw e;
		}
    	return JSONObject.toJSONString(page);
    }
	/**
	 * 创建公共镜像
	 * @Author:chengxiaodong
	 * @param request
	 * @param cloudImage
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/createpublicimage" , method = RequestMethod.POST)
    @ResponseBody
    public String  createPublicImage(MultipartHttpServletRequest request) throws Exception{
    	log.info("创建公共镜像开始");
    	EayunResponseJson json = new EayunResponseJson();
    	InputStream is=null;
    	CloudImage cloudImage = new CloudImage();
    	String dcId = null != request.getParameter("dcId")?request.getParameter("dcId"):"";
    	String imageName = null != request.getParameter("imageName")?request.getParameter("imageName"):"";
    	String osType = null != request.getParameter("osType")?request.getParameter("osType"):"";
    	String sysType = null != request.getParameter("sysType")?request.getParameter("sysType"):"";
    	String diskFormat = null != request.getParameter("diskFormat")?request.getParameter("diskFormat"):"";
    	String createType = null != request.getParameter("createType")?request.getParameter("createType"):"";
    	String imageUrl = null != request.getParameter("imageUrl")?request.getParameter("imageUrl"):"";
    	String minCpu = null != request.getParameter("minCpu")?request.getParameter("minCpu"):"0";
    	String minRam = null != request.getParameter("minRam")?request.getParameter("minRam"):"0";
    	String minDisk = null != request.getParameter("minDisk")?request.getParameter("minDisk"):"0";
    	String sysDetail=null != request.getParameter("sysDetail")?request.getParameter("sysDetail"):"";
    	
    	String maxCpu = request.getParameter("maxCpu");
    	String maxRam = request.getParameter("maxRam");
    	String desc=null!=request.getParameter("imageDescription")?request.getParameter("imageDescription"):"";
    	cloudImage.setDcId(dcId);
    	cloudImage.setImageName(imageName);
    	cloudImage.setSysDetail(sysDetail);
    	cloudImage.setOsType(osType);
    	cloudImage.setSysType(sysType);
    	cloudImage.setDiskFormat(diskFormat);
    	cloudImage.setCreateType(createType);
    	cloudImage.setImageUrl(imageUrl);
    	cloudImage.setMinCpu(Long.valueOf(minCpu));
    	cloudImage.setMinRam(Long.valueOf(minRam));
    	cloudImage.setMinDisk(Long.valueOf(minDisk));
    	cloudImage.setMaxCpu(maxCpu != null && !maxCpu.equals("")?Integer.valueOf(maxCpu):null);
    	cloudImage.setMaxRam(maxRam != null && !maxRam.equals("")?Integer.valueOf(maxRam)*1024:null);
    	cloudImage.setImageIspublic('1');
    	cloudImage.setImageDescription(desc);
    	
    	try {
    		if("1".equals(cloudImage.getCreateType())){
    			Iterator<String> itr=request.getFileNames();
    	        if(itr==null || !itr.hasNext()){
    	        	return "";
    	        }
    			MultipartFile multipartFile = request.getFile(itr.next());
    			is = multipartFile.getInputStream();
        	}
    		cloudImage = ecmcCloudImageService.createPublicImage(cloudImage,is);
    		json.setData(cloudImage);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("创建镜像", ConstantClazz.LOG_TYPE_MIRROR_PUBLIC, cloudImage.getImageName(), null, 1, cloudImage.getImageId(), null);
		}catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("创建镜像", ConstantClazz.LOG_TYPE_MIRROR_PUBLIC, cloudImage.getImageName(), null, 0, null, e);
			log.error(e.toString(),e);
	        throw e;
	    } catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
	    	ecmcLogService.addLog("创建镜像", ConstantClazz.LOG_TYPE_MIRROR_PUBLIC, cloudImage.getImageName(), null, 0, null, e);
			log.error(e.toString(),e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	/**
	 * 编辑自定义镜像
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudImage
	 * @return
	 *<li>Date: 2016年4月28日</li>
	 */
	@RequestMapping(value = "/updatepersonimage" , method = RequestMethod.POST)
    @ResponseBody
    public String  updatePersonImage(HttpServletRequest request,@RequestBody CloudImage cloudImage){
    	log.info("编辑自定义镜像开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		ecmcCloudImageService.updatePersonImage(cloudImage);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_CUS, cloudImage.getImageName(), cloudImage.getPrjId(), 1, cloudImage.getImageId(), null);
		}catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_CUS, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
	        throw e;
	    } catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_CUS, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	
	/**
	 * 编辑公共镜像
	 * @Author: chengxiaodong
	 * @param request
	 * @param cloudImage
	 * @return
	 */
	@RequestMapping(value = "/updatepublicimage" , method = RequestMethod.POST)
    @ResponseBody
    public String  updatePublicImage(HttpServletRequest request,@RequestBody CloudImage cloudImage){
    	log.info("编辑公共镜像开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		ecmcCloudImageService.updatePublicImage(cloudImage);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_PUBLIC, cloudImage.getImageName(), cloudImage.getPrjId(), 1, cloudImage.getImageId(), null);
		}catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_PUBLIC, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
			log.error(e.toString(),e);
	        throw e;
	    } catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_PUBLIC, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	/**
	 * 删除镜像
	 * @author chengxiaodong
	 * @param request
	 * @param cloudImage
	 * @return
	 */
	@RequestMapping(value = "/deleteimage" , method = RequestMethod.POST)
    @ResponseBody
    public String  deleteImage(HttpServletRequest request,@RequestBody CloudImage cloudImage){
    	log.info("删除镜像开始");
    	EayunResponseJson json = new EayunResponseJson();
    	String imageType="";
    	try {
    		if("1".equals(cloudImage.getImageIspublic().toString())){
    			imageType=ConstantClazz.LOG_TYPE_MIRROR_PUBLIC;
    		}else if("2".equals(cloudImage.getImageIspublic().toString())){
    			imageType=ConstantClazz.LOG_TYPE_MIRROR_CUS;
    		}else if("3".equals(cloudImage.getImageIspublic().toString())){
    			imageType=ConstantClazz.LOG_TYPE_MIRROR_MARKET;
    		}else if("9".equals(cloudImage.getImageIspublic().toString())){
    			imageType=ConstantClazz.LOG_TYPE_MIRROR_UNCLASSIFIED;
    		}else{
    			imageType=ConstantClazz.LOG_TYPE_MIRROR;
    		}
    		
    		ecmcCloudImageService.deleteImage(cloudImage);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("删除镜像", imageType, cloudImage.getImageName(), cloudImage.getPrjId(), 1, cloudImage.getImageId(), null);
		}catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("删除镜像", imageType, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
	        throw e;
	    } catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("删除镜像", imageType, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	@RequestMapping(value = "/getimagebyid" , method = RequestMethod.POST)
    @ResponseBody
    public String  getImageById(HttpServletRequest request,@RequestBody Map map){
    	log.info("查看镜像详情");
    	String imageId = null == map.get("imageId")?"":map.get("imageId").toString();
    	String imageType = null == map.get("imageType")?"":map.get("imageType").toString();
    	EayunResponseJson json = new EayunResponseJson();
    	CloudImage cloudImage = null;
    	try {
    		if("1".equals(imageType)){
    			cloudImage = ecmcCloudImageService.getPublicImageById(imageId);
    		}else if("2".equals(imageType)){
    			cloudImage = ecmcCloudImageService.getPersonImageById(imageId);
    		}else if("3".equals(imageType)){
    			cloudImage = ecmcCloudImageService.getMarketImageById(imageId);
    		}
    		json.setData(cloudImage);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		}catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
	        throw e;
	    } catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	
	/**
	 * 获取镜像格式列表
	 * @Author: duanbinbin
	 * @param request
	 * @return
	 *<li>Date: 2016年4月28日</li>
	 */
	@RequestMapping(value = "/getimageformat" , method = RequestMethod.POST)
    @ResponseBody
    public String  getImageFormat(HttpServletRequest request){
    	log.info("获取镜像格式列表开始");
    	List<SysDataTree> list = new ArrayList<SysDataTree>();
    	EayunResponseJson json = new EayunResponseJson();
    	try{
    		list=ecmcCloudImageService.getImageFormatList();
    		json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	
	
	/**
	 * 验证镜像名称
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudImage
	 * @return
	 *<li>Date: 2016年4月28日</li>
	 */
	@RequestMapping(value = "/checkimagename" , method = RequestMethod.POST)
    @ResponseBody
    public String  checkImageName(HttpServletRequest request,@RequestBody CloudImage cloudImage){
    	log.info("验证镜像名称开始");
    	boolean isok = false;
    	EayunResponseJson json = new EayunResponseJson();
    	try{
    		isok=ecmcCloudImageService.checkImageName(cloudImage);
    		json.setData(isok);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	
	
	/**
	 * 通过项目Id获取镜像数目
	 * @param request
	 * @param map
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/getimagecountbyprjid" , method = RequestMethod.POST)
	@ResponseBody
	public String getImageCountByPrjId(HttpServletRequest request, @RequestBody Map<String, String> map) throws AppException {
		String projectId = map.get("prjId");
		EayunResponseJson json = new EayunResponseJson();
		int count = ecmcCloudImageService.getImageCountByPrjId(projectId);
		json.setData(count);
		json.setRespCode(ConstantClazz.SUCCESS_CODE);
		return JSONObject.toJSONString(json);
	}
	
	
	
	/**
	 * 启用镜像
	 * @author chengxiaodong 
	 * @param request
	 * @param cloudImage
	 * @return
	 */
	@RequestMapping(value = "/useimage" , method = RequestMethod.POST)
    @ResponseBody
    public String  useImage(HttpServletRequest request,@RequestBody CloudImage cloudImage){
    	log.info("启用镜像开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		ecmcCloudImageService.useImage(cloudImage);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			
			if("1".equals(cloudImage.getImageIspublic().toString())){
				ecmcLogService.addLog("启用镜像", ConstantClazz.LOG_TYPE_MIRROR_PUBLIC, cloudImage.getImageName(), cloudImage.getPrjId(), 1, cloudImage.getImageId(), null);
			}else if("3".equals(cloudImage.getImageIspublic().toString())){
				ecmcLogService.addLog("启用镜像", ConstantClazz.LOG_TYPE_MIRROR_MARKET, cloudImage.getImageName(), cloudImage.getPrjId(), 1, cloudImage.getImageId(), null);
			}
			
		}catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			if("1".equals(cloudImage.getImageIspublic().toString())){
				ecmcLogService.addLog("启用镜像", ConstantClazz.LOG_TYPE_MIRROR_PUBLIC, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
			}else if("3".equals(cloudImage.getImageIspublic().toString())){
				ecmcLogService.addLog("启用镜像", ConstantClazz.LOG_TYPE_MIRROR_MARKET, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
			}
			
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	
	
	/**
	 * 停用镜像
	 * @author chengxiaodong 
	 * @param request
	 * @param cloudImage
	 * @return
	 */
	@RequestMapping(value = "/closeimage" , method = RequestMethod.POST)
    @ResponseBody
    public String  closeImage(HttpServletRequest request,@RequestBody CloudImage cloudImage){
    	log.info("停用镜像开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		ecmcCloudImageService.closeImage(cloudImage);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			
			if("1".equals(cloudImage.getImageIspublic().toString())){
				ecmcLogService.addLog("停用镜像", ConstantClazz.LOG_TYPE_MIRROR_PUBLIC, cloudImage.getImageName(), cloudImage.getPrjId(), 1, cloudImage.getImageId(), null);
			}else if("3".equals(cloudImage.getImageIspublic().toString())){
				ecmcLogService.addLog("停用镜像", ConstantClazz.LOG_TYPE_MIRROR_MARKET, cloudImage.getImageName(), cloudImage.getPrjId(), 1, cloudImage.getImageId(), null);
			}
			
		}catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			if("1".equals(cloudImage.getImageIspublic().toString())){
				ecmcLogService.addLog("停用镜像", ConstantClazz.LOG_TYPE_MIRROR_PUBLIC, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
			}else if("3".equals(cloudImage.getImageIspublic().toString())){
				ecmcLogService.addLog("停用镜像", ConstantClazz.LOG_TYPE_MIRROR_MARKET, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
			}
			
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	
	/**
	 * 查询镜像系统列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getostypelist" , method = RequestMethod.POST)
    @ResponseBody
    public String  getOsTypeList(HttpServletRequest request){
    	log.info("查询镜像系统类型列表");
    	List<SysDataTree> dataList = new ArrayList<SysDataTree>();
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		dataList = ecmcCloudImageService.getOsTypeList();
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			json.setData(dataList);
		}catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
	    	log.error(e.toString(),e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	/**
	 * 查询镜像业务类别
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/getmarkettypelist" , method = RequestMethod.POST)
    @ResponseBody
    public String  getMarketTypeList(HttpServletRequest request){
    	log.info("查询市场镜像业务类别列表");
    	List<SysDataTree> dataList = new ArrayList<SysDataTree>();
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		dataList = ecmcCloudImageService.getMarketTypeList();
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			json.setData(dataList);
		}catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
	    	log.error(e.toString(),e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	
	/**
	 * 查询市场镜像
	 * @Author: chengxiaodong
	 * @param request
	 * @param page
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/getmarketimagepagelist" , method = RequestMethod.POST)
    @ResponseBody
    public String  getMarketImagePageList(HttpServletRequest request,Page page,@RequestBody ParamsMap map){
    	log.info("查询市场镜像列表开始");
    	String dcId = "";
    	String professionType = "";
    	String sysType="";
    	String imageName = "";
    	String isUse="";
    	
    	if(null != map.getParams()){
    		dcId = null == map.getParams().get("dcId")?"":map.getParams().get("dcId").toString();
    		professionType = null == map.getParams().get("professionType")?"":map.getParams().get("professionType").toString();
    		sysType = null == map.getParams().get("sysType")?"":map.getParams().get("sysType").toString();
        	imageName = null == map.getParams().get("imageName")?"":map.getParams().get("imageName").toString();
        	isUse=null==map.getParams().get("isUse")?"":map.getParams().get("isUse").toString();
    	}
    	int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        try {
			page = ecmcCloudImageService.getMarketImagePageList(page,queryMap,dcId,professionType,sysType,imageName,isUse);
		} catch (Exception e) {
			log.error(e.toString(),e);
			throw e;
		}
    	return JSONObject.toJSONString(page);
    }
	
	
	
	/**
	 * 上传市场镜像
	 * @Author: chengxiaodong
	 * @param request
	 * @param cloudImage
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/createmarketimage" , method = RequestMethod.POST)
    @ResponseBody
    public String  createMarketImage(MultipartHttpServletRequest request) throws Exception{
    	log.info("创建市场镜像开始");
    	EayunResponseJson json = new EayunResponseJson();
    	InputStream is=null;
    	CloudImage cloudImage = new CloudImage();
    	String dcId = null != request.getParameter("dcId")?request.getParameter("dcId"):"";
    	String imageName = null != request.getParameter("imageName")?request.getParameter("imageName"):"";
    	String osType = null != request.getParameter("osType")?request.getParameter("osType"):"";
    	String sysType = null != request.getParameter("sysType")?request.getParameter("sysType"):"";
    	String diskFormat = null != request.getParameter("diskFormat")?request.getParameter("diskFormat"):"";
    	String createType = null != request.getParameter("createType")?request.getParameter("createType"):"";
    	String imageUrl = null != request.getParameter("imageUrl")?request.getParameter("imageUrl"):"";
    	String minCpu = null != request.getParameter("minCpu")?request.getParameter("minCpu"):"0";
    	String minRam = null != request.getParameter("minRam")?request.getParameter("minRam"):"0";
    	String minDisk = null != request.getParameter("minDisk")?request.getParameter("minDisk"):"0";
    	
    	String sysDetail=null != request.getParameter("sysDetail")?request.getParameter("sysDetail"):"";
    	String provider=null != request.getParameter("provider")?request.getParameter("provider"):"";
    	String professionType=null != request.getParameter("professionType")?request.getParameter("professionType"):"";
    	String integratedSoftware=null != request.getParameter("integratedSoftware")?request.getParameter("integratedSoftware"):"";
    	String sysdiskSize=null != request.getParameter("sysdiskSize")?request.getParameter("sysdiskSize"):"0";
    	
    	String maxCpu = request.getParameter("maxCpu");
    	String maxRam = request.getParameter("maxRam");
    	
    	cloudImage.setDcId(dcId);
    	cloudImage.setImageName(imageName);
    	cloudImage.setOsType(osType);
    	cloudImage.setSysType(sysType);
    	cloudImage.setDiskFormat(diskFormat);
    	cloudImage.setCreateType(createType);
    	cloudImage.setImageUrl(imageUrl);
    	cloudImage.setMinCpu(Long.valueOf(minCpu));
    	cloudImage.setMinRam(Long.valueOf(minRam));
    	cloudImage.setMinDisk(Long.valueOf(minDisk));
    	cloudImage.setMaxCpu(maxCpu != null && !maxCpu.equals("")?Integer.valueOf(maxCpu):null);
    	cloudImage.setMaxRam(maxRam != null && !maxRam.equals("")?Integer.valueOf(maxRam)*1024:null);
    	cloudImage.setImageIspublic('3');
    	
    	cloudImage.setSysDetail(sysDetail);
    	cloudImage.setProvider(provider);
    	cloudImage.setProfessionType(professionType);
    	cloudImage.setIntegratedSoftware(integratedSoftware);
    	cloudImage.setSysdiskSize(Long.valueOf(sysdiskSize));
    	
    	
    	try {
    		if("1".equals(cloudImage.getCreateType())){
    			Iterator<String> itr=request.getFileNames();
    	        if(itr==null || !itr.hasNext()){
    	        	return "";
    	        }
    			MultipartFile multipartFile = request.getFile(itr.next());
    			is = multipartFile.getInputStream();
        	}
    		cloudImage = ecmcCloudImageService.createMarketImage(cloudImage,is);
    		json.setData(cloudImage);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("创建镜像", ConstantClazz.LOG_TYPE_MIRROR_MARKET, cloudImage.getImageName(), null, 1, cloudImage.getImageId(), null);
		}catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("创建镜像", ConstantClazz.LOG_TYPE_MIRROR_MARKET, cloudImage.getImageName(), null, 0, null, e);
			log.error(e.toString(),e);
	        throw e;
	    } catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
	    	ecmcLogService.addLog("创建镜像", ConstantClazz.LOG_TYPE_MIRROR_MARKET, cloudImage.getImageName(), null, 0, null, e);
			log.error(e.toString(),e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	
	
	
	/**
	 * 编辑市场镜像
	 * @Author: chengxiaodong
	 * @param request
	 * @param cloudImage
	 * @return
	 */
	@RequestMapping(value = "/updatemarketimage" , method = RequestMethod.POST)
    @ResponseBody
    public String  updateMarketImage(HttpServletRequest request,@RequestBody CloudImage cloudImage){
    	log.info("编辑市场镜像开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		ecmcCloudImageService.updateMarketImage(cloudImage);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_MARKET, cloudImage.getImageName(), cloudImage.getPrjId(), 1, cloudImage.getImageId(), null);
		}catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_MARKET, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
			log.error(e.toString(),e);
	        throw e;
	    } catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_MARKET, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	/**
	 * 编辑市场镜像描述
	 * @Author: chengxiaodong
	 * @param request
	 * @param cloudImage
	 * @return
	 */
	@RequestMapping(value = "/updatemarketimagedesc" , method = RequestMethod.POST)
    @ResponseBody
    public String  updateMarketImageDesc(HttpServletRequest request,@RequestBody CloudImage cloudImage){
    	log.info("编辑市场镜像开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		ecmcCloudImageService.updateMarketImageDesc(cloudImage);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_MARKET, cloudImage.getImageName(), cloudImage.getPrjId(), 1, cloudImage.getImageId(), null);
		}catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_MARKET, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
			log.error(e.toString(),e);
	        throw e;
	    } catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_MARKET, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	
	
	/**
	 * 查询未分类镜像
	 * @Author: chengxiaodong
	 * @param request
	 * @param page
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/getunclassifiedimagepagelist" , method = RequestMethod.POST)
    @ResponseBody
    public String  getUnclassifiedImagePageList(HttpServletRequest request,Page page,@RequestBody ParamsMap map){
    	log.info("查询未分类镜像列表开始");
    	String dcId = "";
    	String imageName = "";
    	
    	
    	if(null != map.getParams()){
    		dcId = null == map.getParams().get("dcId")?"":map.getParams().get("dcId").toString();
        	imageName = null == map.getParams().get("imageName")?"":map.getParams().get("imageName").toString();
        	
    	}
    	int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        try {
			page = ecmcCloudImageService.getUnclassifiedImagePageList(page,queryMap,dcId,imageName);
		} catch (Exception e) {
			log.error(e.toString(),e);
			throw e;
		}
    	return JSONObject.toJSONString(page);
    }
	
	
	/**
	 * 编辑未分类镜像
	 * @Author: chengxiaodong
	 * @param request
	 * @param cloudImage
	 * @return
	 */
	@RequestMapping(value = "/updateunclassifiedimage" , method = RequestMethod.POST)
    @ResponseBody
    public String  updateUnclassifiedImage(HttpServletRequest request,@RequestBody CloudImage cloudImage){
    	log.info("编辑公共镜像开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		ecmcCloudImageService.updateUnclassifiedImage(cloudImage);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_UNCLASSIFIED, cloudImage.getImageName(), cloudImage.getPrjId(), 1, cloudImage.getImageId(), null);
		}catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_UNCLASSIFIED, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
			log.error(e.toString(),e);
	        throw e;
	    } catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("编辑镜像", ConstantClazz.LOG_TYPE_MIRROR_UNCLASSIFIED, cloudImage.getImageName(), cloudImage.getPrjId(), 0, cloudImage.getImageId(), e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	
	
}
