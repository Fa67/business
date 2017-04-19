package com.eayun.price.controller;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.PayType;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.price.bean.PriceTreeData;
import com.eayun.price.bean.PriceUtil;
import com.eayun.price.bean.UpgradeBean;
import com.eayun.price.model.BillingFactor;
import com.eayun.price.service.BillingFactorService;

@Controller
@RequestMapping("/billing/factor")
public class BillingFactorController {

private static final Logger log = LoggerFactory.getLogger(BillingFactorController.class);
	
	@Autowired
    private BillingFactorService billingFactorService;
	
	@Autowired
    private EcmcLogService ecmcLogService;
	
	@RequestMapping("/getfactorsbytypedcid")
    @ResponseBody
    public String getFactorsByTypeDcId (HttpServletRequest request , Page page , @RequestBody ParamsMap map) {
        log.info("查询所有计费因子列表");
        String dcId = null == map.getParams().get("dcId")?"":map.getParams().get("dcId").toString();
        String billingFactor = 
        		null == map.getParams().get("billingFactor")?"":map.getParams().get("billingFactor").toString();
        String resourcesType = 
        		null == map.getParams().get("resourcesType")?"":map.getParams().get("resourcesType").toString();
        String priceType = null == map.getParams().get("priceType")?"":map.getParams().get("priceType").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        page = billingFactorService.getFactorsPage(page , queryMap , dcId , billingFactor , resourcesType,priceType);
        return JSONObject.toJSONString(page);
    }
	/**
	 * 若为查询镜像价格时：
	 * 	billingFactor为null
	 * 	resourcesType="IMAGE"
	 * 	billingUnit为镜像id
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping("/getpricesbypaytype")
    @ResponseBody
    public String getPricesByPayType (HttpServletRequest request , @RequestBody Map map) {
        log.info("根据选定的数据中心下的计费单位查询预付费或后付费的价格");
        EayunResponseJson json = new EayunResponseJson();
        String dcId = null == map.get("dcId")?"":map.get("dcId").toString();
        String billingFactor = 
        		null == map.get("billingFactor")?"":map.get("billingFactor").toString();
        String resourcesType = 
        		null == map.get("resourcesType")?"":map.get("resourcesType").toString();
        String billingUnit = 
        		null == map.get("billingUnit")?"":map.get("billingUnit").toString();
        String payType  = 
        		null == map.get("payType")?"":map.get("payType").toString();
        
        List<BillingFactor> factorList = new ArrayList<BillingFactor>();
        try {
			factorList = billingFactorService.getPricesByPayType(dcId , billingFactor , resourcesType , billingUnit , payType);
			json.setData(factorList);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.toString());
        	log.error(e.toString(),e);
		}
        return JSONObject.toJSONString(json);
    }
	@RequestMapping("/getallresourcestype")
    @ResponseBody
    public String getAllResourcesType (HttpServletRequest request , @RequestBody Map map) {
        log.info("查询出所有的计费资源类型");
        String priceType = null == map.get("priceType")?"":map.get("priceType").toString();
        EayunResponseJson json = new EayunResponseJson();
        List<PriceTreeData> typeList = new ArrayList<PriceTreeData>();
        try {
			typeList = billingFactorService.getAllResourcesType(priceType);
			json.setData(typeList);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.toString());
        	log.error(e.toString(),e);
		}
        return JSONObject.toJSONString(json);
    }
	
	@RequestMapping("/addfactorprice")
    @ResponseBody
    public String addFactorPrice (HttpServletRequest request , @RequestBody BillingFactor billingFactor) {
        log.info("添加计费因子价格");
        EayunResponseJson json = new EayunResponseJson();
        String busiName = "配置预付费价格_";
        if(PayType.PAYAFTER.equals(billingFactor.getPayType())){
        	busiName = "配置后付费价格_";
        }
        String resourceName = billingFactor.getFactorName();
        String resourceId = billingFactor.getId();
        if(PriceUtil.IMAGE_PRICE_TYPE.equals(billingFactor.getResourcesType())){
        	resourceName = billingFactor.getUnitName();
            resourceId = billingFactor.getFactorUnit();
        }
        try {
        	billingFactor = billingFactorService.addFactorPrice(billingFactor,false);
            if(!PriceUtil.IMAGE_PRICE_TYPE.equals(billingFactor.getResourcesType())){
            	resourceId = billingFactor.getId();
            }
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog(busiName+"添加", "价格配置", resourceName, null, 1, resourceId, null);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.toString());
        	ecmcLogService.addLog(busiName+"添加", "价格配置", resourceName, null, 0, resourceId, e);
        	log.error(e.toString(),e);
		}
        return JSONObject.toJSONString(json);
    }
	
	@RequestMapping("/editfactorprice")
    @ResponseBody
    public String editFactorPrice  (HttpServletRequest request , @RequestBody BillingFactor billingFactor) {
        log.info("编辑计费因子价格");
        EayunResponseJson json = new EayunResponseJson();
        String busiName = "配置预付费价格_";
        if(PayType.PAYAFTER.equals(billingFactor.getPayType())){
        	busiName = "配置后付费价格_";
        }
        String resourceName = billingFactor.getFactorName();
        String resourceId = billingFactor.getId();
        if(PriceUtil.IMAGE_PRICE_TYPE.equals(billingFactor.getResourcesType())){
        	resourceName = billingFactor.getUnitName();
            resourceId = billingFactor.getFactorUnit();
        }
        try {
			billingFactorService.editFactorPrice(billingFactor);
			if(!PriceUtil.IMAGE_PRICE_TYPE.equals(billingFactor.getResourcesType())){
            	resourceId = billingFactor.getId();
            }
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog(busiName+"编辑", "价格配置", resourceName, null, 1,resourceId, null);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.toString());
        	ecmcLogService.addLog(busiName+"编辑", "价格配置", resourceName, null, 0, resourceId, e);
        	log.error(e.toString(),e);
		}
        return JSONObject.toJSONString(json);
    }
	
	@RequestMapping("/deletefactorprice")
    @ResponseBody
    public String deleteFactorPrice (HttpServletRequest request , @RequestBody  Map map) {
        log.info("删除计费因子价格");
        EayunResponseJson json = new EayunResponseJson();
        String id = null == map.get("id")?"":map.get("id").toString();
        String imageId = null == map.get("imageId")?"":map.get("imageId").toString();
        String factorName = null == map.get("factorName")?"":map.get("factorName").toString();
        
        String payType = null == map.get("payType")?"":map.get("payType").toString();
        String busiName = "配置预付费价格_";
        if(PayType.PAYAFTER.equals(payType)){
        	busiName = "配置后付费价格_";
        }
        String resourceId = id;
        if(!StringUtil.isEmpty(imageId)){
        	resourceId = imageId;
        }
        try {
			billingFactorService.deleteFactorPrice(id);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog(busiName+"删除", "价格配置", factorName, null, 1, resourceId, null);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.toString());
        	ecmcLogService.addLog(busiName+"删除", "价格配置", factorName, null, 0, resourceId, e);
        	log.error(e.toString(),e);
		}
        return JSONObject.toJSONString(json);
    }
	/**
	 * 获取续费资源的价钱
	 * **/
	@RequestMapping("/getPriceByFactor")
    @ResponseBody
    public String getPriceByFactor (HttpServletRequest request , @RequestBody  ParamBean paramBean) throws AppException{
        log.info("获取续费资源的价钱");
        EayunResponseJson json = new EayunResponseJson();
        BigDecimal price = null;
        try {
        	price = billingFactorService.getPriceByFactor(paramBean);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			json.setMessage(price.toString());
		}catch (AppException e) {
		    log.error(e.toString(),e);
			json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
		}catch (Exception e) {
		    log.error(e.toString(),e);
			json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
		}
        return JSONObject.toJSONString(json);
    }
	
	@RequestMapping("/getPriceDetails")
	@ResponseBody
	public String getPriceDetails (HttpServletRequest request , @RequestBody  ParamBean paramBean) throws AppException {
		log.info("返回总价和每一种计费单位的价钱（不乘以批量数）");
		EayunResponseJson json = new EayunResponseJson();
		try {
			PriceDetails priceDetails= billingFactorService.getPriceDetails(paramBean);
			if(paramBean.getPayType().equals(PayType.PAYAFTER)){
				BigDecimal minValue = new BigDecimal(0.01);
				if(priceDetails.getTotalPrice().compareTo(BigDecimal.ZERO)>0 &&
						priceDetails.getTotalPrice().compareTo(minValue)<0){
					priceDetails.setTotalPrice(minValue);
				}
			}
			priceDetails.setTotalPrice(priceDetails.getTotalPrice().setScale(2, RoundingMode.FLOOR));
			json.setData(priceDetails);
		} catch (AppException e) {
		    log.error(e.toString(),e);
			json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
		} catch (Exception e) {
			json.setMessage(e.getMessage());
			log.error(e.toString(),e);
		}
		return JSONObject.toJSONString(json);
	}
	
	/**
	 * 获取升级后的资源差价
	 * 
	 * @param request
	 * @param upgradeBean
	 * @return
	 */
	@RequestMapping("/getUpgradePrice")
	@ResponseBody
	public String getUpgradePrice (HttpServletRequest request , @RequestBody  UpgradeBean upgradeBean) throws AppException {
		log.info("返回升级后的资源差价");
		EayunResponseJson json = new EayunResponseJson();
		try {
			BigDecimal prices= billingFactorService.updateConfigPrice(upgradeBean);
			prices = prices.setScale(2, RoundingMode.FLOOR);
			json.setData(prices);
		} catch (AppException e) {
		    log.error(e.toString(),e);
			json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
		} catch (Exception e) {
			json.setMessage(e.getMessage());
			log.error(e.toString(),e);
		}
		return JSONObject.toJSONString(json);
	}
	@RequestMapping("/syncfactorprice")
    @ResponseBody
    public String syncFactorPrice (HttpServletRequest request) {
        log.info("价格缓存同步开始");
        EayunResponseJson json = new EayunResponseJson();
        try {
        	billingFactorService.syncFactorPrice();
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.toString());
        	log.error(e.toString(),e);
		}
        return JSONObject.toJSONString(json);
    }
	
	/**
     * 导出价格excel
     */
    @RequestMapping("/createpriceexcel")
    public String createPriceExcel(HttpServletRequest request, HttpServletResponse response ,
    		String dcId ,String browser , String type) throws Exception {
    	Date now = new Date();
    	String strDte = DateUtil.dateToStr(now);
    	strDte = strDte.replaceAll("-", "");
    	String pType = "基础资源";
    	if(PriceUtil.priceType.CLOUD.toString().equals(type)){
    		pType = "云数据库";
    	}
        String fileName = "";
        String dcName = billingFactorService.getDcNameById(dcId);
        if("Firefox".equals(browser)){
            fileName = new String((dcName+pType+"价格表_"+strDte+".xls").getBytes(), "iso-8859-1");
        }else{
            fileName = URLEncoder.encode(dcName+pType+"价格表_"+strDte+".xls", "UTF-8") ;
        }
        
        response.setContentType("application/vnd.ms-excel");
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        try {
        	billingFactorService.exportPriceSheets(response.getOutputStream(),dcId,type);
        	ecmcLogService.addLog("导出数据", "价格配置", fileName, null, 1,null, null);
        } catch (Exception e) {
        	ecmcLogService.addLog("导出数据", "价格配置", fileName, null, 0, null, e);
            log.error("导出价格excel失败", e);
            throw e;
        }
        return null;
    }
	
    @RequestMapping(value = "/importpriceexcel",method = RequestMethod.POST)
    @ResponseBody
    public String importPriceExcel(MultipartHttpServletRequest request) throws Exception {
    	log.info("导入价格配置表");
    	EayunResponseJson json = new EayunResponseJson();
    	String dcId = null != request.getParameter("dcId")?request.getParameter("dcId"):"";
    	String priceType = null != request.getParameter("priceType")?request.getParameter("priceType"):"";
    	String fileName = "";
    	try {
    		Iterator<String> it = request.getFileNames();
    		while(it.hasNext()){
    			MultipartFile multipartFile = request.getFile(it.next());
    			fileName = multipartFile.getOriginalFilename();
    			InputStream is = multipartFile.getInputStream();
    			billingFactorService.importPriceExcel(is,dcId,priceType);
    		}
			
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("导入数据", "价格配置", fileName, null, 1,null, null);
		} catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
	    	ecmcLogService.addLog("导入数据", "价格配置", fileName, null, 0, null, e);
			log.error(e.toString(),e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    }
}
