package com.eayun.virtualization.controller;

import java.util.ArrayList;
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

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.annotation.GeneralMethod;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.log.service.LogService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.model.CloudImage;
import com.eayun.virtualization.service.ImageService;

@Controller
@RequestMapping("/cloud/image")
@Scope("prototype")
public class ImageController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(ImageController.class);
    @Autowired
    private ImageService imageService;  
    @Autowired
    private LogService logService;
   


   /**查询自定义镜像列表
    * @author Chengxiaodong
    * @param request
    * @param datacenterId
    * @param projectId
    * @param name
    * @param page
    * @return
    */
   
    @RequestMapping(value= "/getImageList" , method = RequestMethod.POST)
    @ResponseBody
    public String getImageList(HttpServletRequest request,Page page,@RequestBody ParamsMap map){
        try {
        	log.info("查询镜像列表开始");
            String prjId = map.getParams().get("prjId").toString();
            String dcId=map.getParams().get("dcId").toString();
            String imageName=map.getParams().get("name").toString();
            int pageSize = map.getPageSize();
            int pageNumber = map.getPageNumber();
            
            QueryMap queryMap=new QueryMap();
            queryMap.setPageNum(pageNumber);
            queryMap.setCURRENT_ROWS_SIZE(pageSize);
            page=imageService.getImageList(page,prjId,dcId,imageName,queryMap);
        } catch (Exception e) {
            log.error(e.toString(),e);
        }
        return JSONObject.toJSONString(page);
       
    }
    
    
    /**
     * 验证重名
     * @author Chengxiaodong
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/getImageByName" , method = RequestMethod.POST)
    @ResponseBody
    public String getImageByName(HttpServletRequest request,@RequestBody CloudImage image){
		boolean isTrue=false;
    	try{
    		log.info("验证镜像重名开始");
    		isTrue=imageService.getImageByName(image);
    	}catch(AppException e){
    		throw e;
    	}
		return JSONObject.toJSONString(isTrue);
    	
    }
    
    
    /**
     * 删除镜像
     * @author Chengxiaodong
     * @param request
     * @param image
     * @return
     */
    @RequestMapping(value= "/deleteImage" , method = RequestMethod.POST)
    @ResponseBody
    public String deleteImage(HttpServletRequest request,@RequestBody CloudImage image){
    	boolean isTrue=false;
    	try{
    		log.info("删除镜像开始");
    		isTrue=imageService.deleteImage(image);
    		logService.addLog("删除镜像",ConstantClazz.LOG_TYPE_MIRROR,image.getImageName(),image.getPrjId(),ConstantClazz.LOG_STATU_SUCCESS,null);
    	}catch(AppException e){
    		logService.addLog("删除镜像",ConstantClazz.LOG_TYPE_MIRROR,image.getImageName(),image.getPrjId(),ConstantClazz.LOG_STATU_ERROR,e);
    		throw e;
    	}
		return JSONObject.toJSONString(isTrue);
    }
    
    
    /**
     * 编辑镜像
     * @author Chengxiaodong
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/updateImage" , method = RequestMethod.POST)
    @ResponseBody
     public String updateImage(HttpServletRequest request,@RequestBody CloudImage image)throws AppException{
    	boolean isTrue=false;
    	 try{
    		 log.info("编辑镜像开始");
        	 isTrue=imageService.updateImage(image);
        	 logService.addLog("编辑镜像",ConstantClazz.LOG_TYPE_MIRROR,image.getImageName(),image.getPrjId(),ConstantClazz.LOG_STATU_SUCCESS,null);
    	 }catch(AppException e){
    		 logService.addLog("编辑镜像",ConstantClazz.LOG_TYPE_MIRROR,image.getImageName(),image.getPrjId(),ConstantClazz.LOG_STATU_ERROR,e);
    		 throw e;
    	 }
	 return JSONObject.toJSONString(isTrue);
    	 
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
    		dataList = imageService.getOsTypeList();
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
    		dataList = imageService.getMarketTypeList();
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			json.setData(dataList);
		}catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
	    	log.error(e.toString(),e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
    
    
    /**查询公共镜像列表
     * @author Chengxiaodong
     * @param request
     * @param datacenterId
     * @param name
     * @param page
     * @return
     */
    
     @RequestMapping(value= "/getPublicImageList" , method = RequestMethod.POST)
     @ResponseBody
     public String getPublicImageList(HttpServletRequest request,Page page,@RequestBody ParamsMap map){
         try {
         	log.info("查询公共镜像列表开始");
             String dcId=map.getParams().get("dcId").toString();
             String imageName=map.getParams().get("name").toString();
             String isUse=map.getParams().get("isUse").toString();
             String sysType=map.getParams().get("sysType").toString();
             
             int pageSize = map.getPageSize();
             int pageNumber = map.getPageNumber();
             
             QueryMap queryMap=new QueryMap();
             queryMap.setPageNum(pageNumber);
             queryMap.setCURRENT_ROWS_SIZE(pageSize);
             page=imageService.getPublicImageList(page,dcId,imageName,isUse,sysType,queryMap);
         } catch (Exception e) {
             log.error(e.toString(),e);
         }
         return JSONObject.toJSONString(page);
        
     }
     
     
     /**查询市场镜像列表
      * @author Chengxiaodong
      * @param request
      * @param datacenterId
      * @param name
      * @param page
      * @return
      */
     
      @RequestMapping(value= "/getMarketImageList" , method = RequestMethod.POST)
      @ResponseBody
      public String getMarketImageList(HttpServletRequest request,Page page,@RequestBody ParamsMap map){
          try {
          	log.info("查询市场镜像列表开始");
              String dcId=map.getParams().get("dcId").toString();
              String imageName=map.getParams().get("name").toString();
              String isUse=map.getParams().get("isUse").toString();
              String sysType=map.getParams().get("sysType").toString();
              String professionType=map.getParams().get("professionType").toString();
              
              int pageSize = map.getPageSize();
              int pageNumber = map.getPageNumber();
              
              QueryMap queryMap=new QueryMap();
              queryMap.setPageNum(pageNumber);
              queryMap.setCURRENT_ROWS_SIZE(pageSize);
              page=imageService.getMarketImageList(page,dcId,imageName,isUse,sysType,professionType,queryMap);
          } catch (Exception e) {
              log.error(e.toString(),e);
          }
          return JSONObject.toJSONString(page);
         
      }
      
      
      /**
       * 查询市场镜像详情
       * @author chengxiaodong
       * @param request
       * @param map
       * @return
       */
      @RequestMapping(value = "/getMarketImagebyId" , method = RequestMethod.POST)
      @ResponseBody
      @GeneralMethod
      public String  getImageById(HttpServletRequest request,@RequestBody Map map){
      	log.info("查看市场镜像详情");
      	String imageId = null == map.get("imageId")?"":map.get("imageId").toString();
      	
      	EayunResponseJson json = new EayunResponseJson();
      	CloudImage cloudImage = null;
      	try {
      		cloudImage = imageService.getMarketImageById(imageId);
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
    
}
