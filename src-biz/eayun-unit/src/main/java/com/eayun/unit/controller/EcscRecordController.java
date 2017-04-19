package com.eayun.unit.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.BeanUtils;
import com.eayun.log.service.LogService;
import com.eayun.sys.model.BaseSysDataTree;
import com.eayun.unit.model.BaseApplyInfo;
import com.eayun.unit.model.BaseWebSiteInfo;
import com.eayun.unit.model.RecordMultipartFile;
import com.eayun.unit.service.EcscRecordService;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年12月20日
 */

@Controller
@RequestMapping("/ecsc/record")
public class EcscRecordController extends BaseController{
	
	private static final Logger log = LoggerFactory.getLogger(EcscRecordController.class);
	@Autowired
	private LogService logService;
	@Autowired
	private EcscRecordService recordservice;
	@Autowired
    private MongoTemplate mongoTemplate;
	/**
	 * 行政区域查询
	 * @param parentcode
	 * @return
	 */
	@RequestMapping(value = "/getAreaList")
	@ResponseBody
	public String getAreaList(@RequestBody Map<String, String > params){
		return JSONObject.toJSONString(recordservice.getAreaList(params.get("parentcode")));
	}
	@RequestMapping(value = "/getAreaName")
	@ResponseBody
	public String getAreaName(@RequestBody Map<String, String > params){
		return JSONObject.toJSONString(recordservice.getAreaName(params.get("code")));
	}
	
	@RequestMapping(value = "/isSelect")
	@ResponseBody
	public String isSelect(HttpServletRequest request,@RequestBody Integer status){
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        int count = recordservice.selectCount(cusId,status);
        return JSONObject.toJSONString(count);
	}
	
	/**
	 * 备案列表
	 * @param request
	 * @param page
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/getApplyList")
	@ResponseBody
	public String getApplyList(HttpServletRequest request, Page page, @RequestBody ParamsMap map) {
		try {
			log.info("查询申请备案列表开始");
			SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
	        String cusId = sessionUser.getCusId();
			String recordType = map.getParams().get("recordType").toString();
			String status = map.getParams().get("status").toString();
			int pageSize = 5;
			int pageNumber = map.getPageNumber();

			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
			page = recordservice.getrecordList(page, recordType, status, cusId, queryMap);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return JSONObject.toJSONString(page);

	}
	
	/**
	 * 查询单个备案申请
	 * @param request
	 * @param applyId
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/getApplyOne")
	@ResponseBody
	public String getApplyById(HttpServletRequest request,@RequestBody String applyId) throws Exception{
		return JSONObject.toJSONString(recordservice.getApplyOneDetail(applyId));
	}
	
	
	/**
	 * 查询主体详情
	 * @param unitId
	 * 
	 * */
	@RequestMapping(value = "/getUnitOne")
	@ResponseBody
	public String getUnitById(HttpServletRequest request,@RequestBody String unitId) throws Exception{
		return JSONObject.toJSONString(recordservice.getUnitOneDetail(unitId));
	}
	
	
	/**
	 * 上传
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/uploadRecordFile")
	@ResponseBody
	public String uploadRecordFile(MultipartHttpServletRequest request){
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUser.getUserId();
        Iterator<String> itr=request.getFileNames();
        List<RecordMultipartFile> multipartFiles = new ArrayList<RecordMultipartFile>();
        RecordMultipartFile recordfile = new RecordMultipartFile();
        if(itr==null){
        	return "";
        }else{
            while (itr.hasNext()) {
                recordfile.setMultipartfile(request.getFile(itr.next()));
                recordfile.setType(request.getParameter("type")); 
                multipartFiles.add(recordfile);
            }
        }
        List<Map<String, String>> maplist = recordservice.uploadRecordFile(multipartFiles, userId);
        return JSONObject.toJSONString(maplist);
	}
	/**
	 * 下载
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/downRecordFile")
	public void downRecordFile(HttpServletRequest request,HttpServletResponse response) {
		OutputStream out = null;
		InputStream inputStream = null;
		try {
			// 将ContentType设为"image/jpeg"，让浏览器识别图像格式。
    		response.setContentType("image/jpg");
			out = response.getOutputStream();
			inputStream = recordservice.downloadFile(request.getParameter("fileid"));
			IOUtils.copy(inputStream, out);
			// 强行将缓冲区的内容输入到页面
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(out!=null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(inputStream!=null){
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	@RequestMapping(value = "/deleteRecordFile")
	@ResponseBody
	public String deleteRecordFile(HttpServletRequest request,@RequestBody Map<String, String> params){
		boolean result = false;
		try {
			result = recordservice.deleteFile(params.get("fileId"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSONObject.toJSONString(result);
	}
	
	@RequestMapping(value = "/addfirstrecord")
	@ResponseBody
	public String addFirstRecord(HttpServletRequest request, @RequestBody Map<String, Object> map) {

		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		if(sessionUser==null || sessionUser.getUserName()==null){
			throw new AppException("登录信息过期，请重新登录!");
		}
		map.put("cusId", sessionUser.getCusId());
		map.put("cusOrg", sessionUser.getCusOrg());
		BaseApplyInfo aw = null;
		List<Map<String, Object>> listmap = (List<Map<String, Object>>)map.get("webList");
		BaseWebSiteInfo website = null;
		String IP = "";
		for(int i=0;i<listmap.size();i++){
			if(i==0){
				Map<String, Object> m = listmap.get(i);
				website = new BaseWebSiteInfo();
				BeanUtils.mapToBean(website, m);
				IP = website.getServiceIp();
			}
		}
		try {
			aw = recordservice.addfirstrecord(map);
			logService.addLog("提交资料(首次备案)", "备案", IP, null, ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("提交资料(首次备案)", "备案", IP, null, ConstantClazz.LOG_STATU_ERROR, e);
			e.printStackTrace();
		}
		return JSONObject.toJSONString(aw);
	}
	@RequestMapping(value = "/deleteFirsRecord")
	@ResponseBody
	public String deleteFirsRecord(@RequestBody Map<String, String> map){
		boolean istrue = false;
		try {
			recordservice.deletefirstrecord(map.get("apply_id"));
			istrue = true;
			logService.addLog("删除备案", "备案", map.get("unitName").toString(), null, ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("删除备案", "备案", map.get("unitName").toString(), null, ConstantClazz.LOG_STATU_ERROR, e);
			e.printStackTrace();
		}
		return JSONObject.toJSONString(istrue);
	}
	/**
	 * 新增接入
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/addAccessRecord")
	@ResponseBody
	public String addAccessRecord(HttpServletRequest request,@RequestBody Map<String, Object> map){
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		if(sessionUser==null || sessionUser.getUserName()==null){
			throw new AppException("登录信息过期，请重新登录!");
		}
		map.put("cusId", sessionUser.getCusId());
		map.put("cusOrg", sessionUser.getCusOrg());
		BaseApplyInfo applyinfo = new BaseApplyInfo();
		List<Map<String, Object>> listmap = (List<Map<String, Object>>)map.get("webList");
		BaseWebSiteInfo website = null;
		String IP = "";
		for(int i=0;i<listmap.size();i++){
			if(i==0){
				Map<String, Object> m = listmap.get(i);
				website = new BaseWebSiteInfo();
				BeanUtils.mapToBean(website, m);
				IP = website.getServiceIp();
			}
		}
		try {
			applyinfo = recordservice.addAccessRecord(map);
			logService.addLog("提交资料(新增接入)", "备案", IP, null, ConstantClazz.LOG_STATU_SUCCESS, null);
		} catch (Exception e) {
			logService.addLog("提交资料(新增接入)", "备案", IP, null, ConstantClazz.LOG_STATU_ERROR, e);
			e.printStackTrace();
		}
		return JSONObject.toJSONString(applyinfo);
	}
	
	
	/**
	 * 查询客户备案成功列表
	 * */
	@RequestMapping(value="/getrecordList")
	@ResponseBody
	public String getRecordList(HttpServletRequest request,@RequestBody Map<String, Object> requstMap){
		
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		if(sessionUser==null || sessionUser.getUserName()==null){
			throw new AppException("登录信息过期，请重新登录!");
		}
		String cusId=sessionUser.getCusId();
		Page page=null;
		Map<String,Object> map=(Map<String,Object>)requstMap.get("params");
		int pageSize=MapUtils.getIntValue(map, "pageSize");
		int pageNo=MapUtils.getIntValue(map, "pageNumber");
	
		QueryMap queryMap=new QueryMap();
		if(pageNo==0){
			 queryMap.setPageNum(1);
		}else{
			queryMap.setPageNum(pageNo);
		}
		 if(pageSize==0){
			 queryMap.setCURRENT_ROWS_SIZE(20);
		 }else{
		 queryMap.setCURRENT_ROWS_SIZE(pageSize);
		 }
		
		try {
			page= recordservice.getrecordListapply(cusId,queryMap);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return JSONObject.toJSONString(page);
		
	}
	@RequestMapping(value = "/getIP")
	@ResponseBody
	public String getIP(HttpServletRequest request,@RequestBody Map<String, String> parms) throws Exception{
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		if(sessionUser==null || sessionUser.getUserName()==null){
			throw new AppException("登录信息过期，请重新登录!");
		}
		String cusId=sessionUser.getCusId();
		return JSONObject.toJSONString(recordservice.getFloatIp(cusId, parms.get("resource_type"),parms.get("dc_Id")));
	}
	
	@RequestMapping(value = "/getCusEmail")
	@ResponseBody
	public String getCusEmail(HttpServletRequest request)throws Exception{
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		if(sessionUser==null || sessionUser.getUserName()==null){
			throw new AppException("登录信息过期，请重新登录!");
		}
		return JSONObject.toJSONString(recordservice.getCusEmail(sessionUser.getCusId()));
	}
	
	/**
	 * @param httpArg
	 *            :参数
	 * @return 返回结果
	 * @throws UnsupportedEncodingException 
	 */
	@RequestMapping(value = "/getPhoneAddress")
	@ResponseBody
	public String requestPhoneAddress(HttpServletRequest request,@RequestBody String phone) {
		//String httpUrl = "http://apis.baidu.com/apistore/mobilephoneservice/mobilephone";
		String httpUrl = "https://tcc.taobao.com/cc/json/mobile_tel_segment.htm";
	    BufferedReader reader = null;
	    String result = null;
	    StringBuffer sbf = new StringBuffer();
	    httpUrl = httpUrl + "?tel=" + phone;
	    String phoneAddress = "";
	    try {
	        URL url = new URL(httpUrl);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod("GET");
	        connection.connect();
	        InputStream is = connection.getInputStream();
	        reader = new BufferedReader(new InputStreamReader(is, "GB2312"));
	        String strRead = null;
	        while ((strRead = reader.readLine()) != null) {
	            sbf.append(strRead);
	            sbf.append("\r\n");
	        }
	        is.close();
	        reader.close();
	        result = sbf.toString();
	        String[] strs = result.split("=");
		    net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(strs[1]);  
	        Map<String, Object> mapJson = net.sf.json.JSONObject.fromObject(jsonObject);  
	        phoneAddress = mapJson.get("province").toString();
	    } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	    } catch (Exception e) {
	    	e.printStackTrace();
		}
	    Map<String, String> map = new HashMap<String, String>();
	    map.put("phoneAddress", phoneAddress);
	    return JSONObject.toJSONString(map);
	}
	
	@RequestMapping(value = "/downRecordQuestionFile")
    public void downRecordQuestionFile(HttpServletRequest request,HttpServletResponse response) {
        OutputStream out = null;
        InputStream inputStream = null;
        try {
            response.setContentType("text/plain");
            response.setHeader("content-disposition", "attachment;filename="+request.getParameter("name")+".doc");  
            out = response.getOutputStream();
            URL url = new URL(request.getParameter("link"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept-Charset", "utf-8");
            connection.setRequestProperty("contentType", "utf-8");
            connection.connect();
            inputStream = connection.getInputStream();
            
            IOUtils.copy(inputStream, out);
            // 强行将缓冲区的内容输入到页面
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
	
	@RequestMapping(value = "/gettestmongo")
    @ResponseBody
    public String getTestMongo(){
	    BaseSysDataTree sysdata = new BaseSysDataTree();
        sysdata.setNodeName("testLiu");
        sysdata.setFlag("true");
        mongoTemplate.insert(sysdata, "liu.jin.gang.test");
        System.currentTimeMillis();
        BaseSysDataTree s = mongoTemplate.findOne(new Query(), BaseSysDataTree.class, "liu.jin.gang.test");
        return JSONObject.toJSONString(s);
    }
}
