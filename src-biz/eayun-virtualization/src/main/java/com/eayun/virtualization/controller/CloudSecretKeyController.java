package com.eayun.virtualization.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
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
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.file.model.EayunFile;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.CloudSecretKey;
import com.eayun.virtualization.service.CloudOrderVmService;
import com.eayun.virtualization.service.CloudSecretKeyService;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2017年2月27日
 */
@Controller
@RequestMapping("/safety/secretKey")
@Scope("prototype")
public class CloudSecretKeyController extends BaseController{

    private static final Logger log = LoggerFactory.getLogger(CloudSecretKeyController.class);
    @Autowired
    private CloudSecretKeyService secreKeyService;
    @Autowired
    private LogService logService;
    @Autowired
    private CloudOrderVmService cloudOrderVmService;
    /**
     * 查询防火墙列表
     * @param request
     * @param page
     * @param map
     * @return
     */
    @RequestMapping(value = "/getSecretKeyList")
    @ResponseBody
    public String getSecretKeyList(HttpServletRequest request, Page page, @RequestBody ParamsMap map) {
        try {
            log.info("查询密钥列表开始");
            String prjId = map.getParams().get("prjId").toString();
            String dcId = map.getParams().get("dcId").toString();
            String name = map.getParams().get("name").toString();
            int pageSize = map.getPageSize();
            int pageNumber = map.getPageNumber();

            QueryMap queryMap = new QueryMap();
            queryMap.setPageNum(pageNumber);
            queryMap.setCURRENT_ROWS_SIZE(pageSize);
            
            page = secreKeyService.getSecretKeyList(page, prjId, dcId, name, queryMap);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return JSONObject.toJSONString(page);

    }

    @RequestMapping(value = "/checkSecretKeyName")
    @ResponseBody
    public String checkName(HttpServletRequest request, @RequestBody Map<String, String> map){
        boolean result = false;
        try {
            String prjId = map.get("prjId");
            String dcId = map.get("dcId");
            String secretkeyId = map.get("secretkeyId");
            String name = map.get("name");
            result = secreKeyService.checkName(prjId, dcId, secretkeyId, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JSONObject.toJSONString(result);
    }
    
    @RequestMapping(value = "/addSecretKey")
    @ResponseBody
    public String createSecretKey(HttpServletRequest request, @RequestBody Map map){
        log.info("创建密钥开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        String dcId = map.get("dc_id").toString();
        String prjId = map.get("prj_id").toString();
        Map<String, String> parmes = (Map<String, String>)map.get("secretkey");
        try{
            CloudSecretKey model = secreKeyService.create(dcId, prjId, parmes,cusId);
            if(model!=null){
                logService.addLog("创建SSH密钥", ConstantClazz.LOG_TYPE_KEYPAIRS, model.getSecretkeyName(), prjId, ConstantClazz.LOG_STATU_SUCCESS, null);
            }else{
                logService.addLog("创建SSH密钥", ConstantClazz.LOG_TYPE_KEYPAIRS, null, prjId, ConstantClazz.LOG_STATU_ERROR, null);
            }
            return JSONObject.toJSONString(model);
        }catch (Exception e) {
            logService.addLog("创建SSH密钥", ConstantClazz.LOG_TYPE_KEYPAIRS, null, prjId, ConstantClazz.LOG_STATU_ERROR, e);
            throw e;
        }
    }
    
    @RequestMapping(value = "/updateSecretKey")
    @ResponseBody
    public String updateSecretKey(HttpServletRequest request, @RequestBody Map map){
        log.info("修改密钥开始");
        String secretkeyId=  MapUtils.getString(map,"secretkeyId");
        String secretkeyDesc=  MapUtils.getString(map,"secretkeyDesc");
        String secretkeyName=  MapUtils.getString(map,"secretkeyName");
        String prjId=  MapUtils.getString(map,"prjId");
        try{
            Boolean fag = secreKeyService.update(secretkeyId, secretkeyName, secretkeyDesc);
            logService.addLog("修改SSH密钥", ConstantClazz.LOG_TYPE_KEYPAIRS, secretkeyName, prjId, ConstantClazz.LOG_STATU_SUCCESS, null);
            return fag.toString();
        }catch (Exception e) {
            logService.addLog("修改SSH密钥", ConstantClazz.LOG_TYPE_KEYPAIRS, secretkeyName, prjId, ConstantClazz.LOG_STATU_ERROR, e);
            throw e;
        }
    }
    
    @RequestMapping(value = "/deleteSecretKey")
    @ResponseBody
    public String deleteSecretKey(HttpServletRequest request, @RequestBody Map map){
        log.info("删除密钥开始");
        EayunResponseJson resp=new EayunResponseJson();
        String secretkeyId=  MapUtils.getString(map,"secretkeyId");
        String dcId=  MapUtils.getString(map,"dcId");
        String prjId=  MapUtils.getString(map,"prjId");
        try{
        	
        	boolean fag= cloudOrderVmService.checkOrderVmBySecretkeyId(secretkeyId);
        	if(fag){
        		  resp.setRespCode(ConstantClazz.ERROR_CODE);
        		  resp.setMessage("您有待创建主机占用该SSH密钥，无法删除");
        		  return JSONObject.toJSONString(resp);
        	}
    	   secreKeyService.delete(dcId, prjId, secretkeyId);
    	  
    	   resp.setRespCode(ConstantClazz.SUCCESS_CODE);
    	   logService.addLog("删除SSH密钥", ConstantClazz.LOG_TYPE_KEYPAIRS, MapUtils.getString(map,"secretkeyName"), prjId, ConstantClazz.LOG_STATU_SUCCESS, null);
        }catch(Exception e){
    	   resp.setRespCode(ConstantClazz.ERROR_CODE);
    	   logService.addLog("删除SSH密钥", ConstantClazz.LOG_TYPE_KEYPAIRS, MapUtils.getString(map,"secretkeyName"), prjId, ConstantClazz.LOG_STATU_ERROR, e);
    	   throw new AppException("删除密钥异常",e);
        }
        return JSONObject.toJSONString(resp);
    }
    
    /**
     * 下载私钥
     * @param request
     * @param response
     */
    @RequestMapping(value = "/downPrivateKeyFile")
    public void downRecordFile(HttpServletRequest request,HttpServletResponse response) {
        OutputStream out = null;
        InputStream inputStream = null;
        try {
            EayunFile file = secreKeyService.getFileBean(request.getParameter("fileid"));
            // 将ContentType设为"pem"。
            response.setContentType("text/plain");
            response.setHeader("content-disposition", "attachment;filename="+file.getFileName());  
            out = response.getOutputStream();
            inputStream = secreKeyService.downloadFile(request.getParameter("fileid"));
            
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
   
    @RequestMapping(value = "/getVmUserDate")
    @ResponseBody
    public String getVmUserDate(HttpServletRequest request, @RequestBody Map<String, String> map){
        return JSONObject.toJSONString(secreKeyService.getVmUserData(map.get("dcId"), map.get("prjId"), map.get("vmId")));
    }
    
    @RequestMapping(value = "/getsecrekeyByid")
    @ResponseBody
    public String getSecrekeyById(HttpServletRequest request, @RequestBody Map<String, String> map){
    	return secreKeyService.getById(map.get("secretkeyId"));
    }
    
    @RequestMapping(value = "/getVmList")
    @ResponseBody
    public String getVmList(HttpServletRequest request, @RequestBody Map<String, Object> map){
        try {
            String secretkeyId = MapUtils.getString(map, "secretkeyId");
            String prjId = MapUtils.getString(map, "prjId");
            String type = MapUtils.getString(map, "type");
            if("unbind".equals(type)){
                return JSONObject.toJSONString(secreKeyService.getUnbindKeyVmList(secretkeyId, prjId));
            }else{
                return JSONObject.toJSONString(secreKeyService.getBindKeyVmList(secretkeyId, prjId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
	@RequestMapping(value ="/getsecretkeyByIdAndVmList")
	@ResponseBody
	public String getsecretkeyByIdAndVmList(HttpServletRequest request, Page page, @RequestBody ParamsMap map) {
		try {
			log.info("查询密钥云主机列表开始");
			String secretkeyId = map.getParams().get("secretkeyId").toString();

			int pageSize = 5;
			int pageNumber = map.getPageNumber();

			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
			page = secreKeyService.getByIdAndVmlist(secretkeyId, queryMap);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return JSONObject.toJSONString(page);
	}

	@RequestMapping(value ="/bindSecretkeyTovm")
    @ResponseBody
    public String bindSecretkeyTovm(HttpServletRequest request,@RequestBody Map<String, Object> map){
	    log.info("绑定/解绑密钥到云主机");
        String dcId = map.get("dcId").toString();
        String prjId = map.get("prjId").toString();
        String secretkeyId = map.get("secretkeyId").toString();
        List list = (ArrayList)map.get("cloudhostlist");
        List<String> strs = new ArrayList<>();
        for(int i=0;i<list.size();i++){
            Map<String, String> maps = (HashMap<String, String>)list.get(i);
            strs.add(maps.get("vmid"));
        }
        Map<String, String> returnmap = new HashMap<>();
        try {
            returnmap = secreKeyService.BindSecretkeyToVm(dcId, prjId, secretkeyId, strs);
            for(String vmid : strs){
                //logService.addLog("绑定云主机", ConstantClazz.LOG_TYPE_KEYPAIRS, vmid, prjId, ConstantClazz.LOG_STATU_SUCCESS, null);
            }
        } catch (Exception e) {
            for(String vmid : strs){
                //logService.addLog("绑定云主机", ConstantClazz.LOG_TYPE_KEYPAIRS, vmid, prjId, ConstantClazz.LOG_STATU_ERROR, e);
            }
            throw e;
        }
        return JSONObject.toJSONString(returnmap);
    }
    
	@RequestMapping(value ="/UnbundlingKeyTovm")
    @ResponseBody
    public String UnbundlingKetToVm(HttpServletRequest request,@RequestBody Map<String, Object> map){
	    log.info("解绑密钥到云主机");
		EayunResponseJson rej=new EayunResponseJson();
		String returndateStr="";
		rej.setRespCode(ConstantClazz.SUCCESS_CODE);
		String dcId = map.get("dcId").toString();
        String prjId = map.get("prjId").toString();
        String secretkeyId = map.get("secretkeyId").toString();
        String vmId = map.get("vmId").toString();
        String name = map.get("secretkeyName").toString();
        try{
        	returndateStr =secreKeyService.unBindSecretkeyToVm(dcId, prjId, secretkeyId, vmId);
        	logService.addLog("解绑云主机", ConstantClazz.LOG_TYPE_KEYPAIRS, name, prjId, ConstantClazz.LOG_STATU_SUCCESS, null);
        }catch(Exception e){
            logService.addLog("解绑云主机", ConstantClazz.LOG_TYPE_KEYPAIRS, name, prjId, ConstantClazz.LOG_STATU_ERROR, e);
        	throw new AppException("解绑异常", e);
        }
        if("".equals(returndateStr)){
        	rej.setRespCode(ConstantClazz.ERROR_CODE);
        }
        return JSONObject.toJSONString(rej);
    }
    
	/**
	 * 查询项目下的SSH 密钥
	 * @param request
	 * @param dcId
	 * @param prjId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getsecretlistbyprj" ,method = RequestMethod.POST)
    @ResponseBody
    public String getSecretKeyListByPrj(HttpServletRequest request,@RequestBody Map<String, Object> map) throws Exception{
		String dcId = map.get("dcId").toString();
        String prjId = map.get("prjId").toString();
		return JSONObject.toJSONString(secreKeyService.getAllSecretkey(dcId, prjId));
    }
	
	
}
