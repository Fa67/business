package com.eayun.physical.ecmccontroller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.SeqManager;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.physical.ecmcservice.EcmcDcServerModelService;
import com.eayun.physical.model.BaseDcServer;
import com.eayun.physical.model.DcServerModel;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年4月5日
 */
@Controller
@RequestMapping("/ecmc/physical/servermodel")
public class EcmcDcServerModelController {

	private final Log log = LogFactory.getLog(EcmcDcServerModelController.class);
	@Autowired
	private EcmcDcServerModelService dcservermodelservice;
	@Autowired
	private EcmcLogService  ecmcLogService;
	
	/**
	 * 分页查询型号集合
	 * @param serverName
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 */
	@RequestMapping("/querylist")
	@ResponseBody
	public Object queryDcServerModelList(Page page,@RequestBody ParamsMap mapparams) throws AppException{
		log.info("分页查询型号集合");
		try {
			String serverName = mapparams.getParams().get("serverName") == null ? null : mapparams.getParams().get("serverName").toString();
			QueryMap querymap = new QueryMap();
			querymap.setPageNum(mapparams.getPageNumber());
			querymap.setCURRENT_ROWS_SIZE(mapparams.getPageSize());
			page = dcservermodelservice.queryDcServerModelList(serverName, page,querymap);
			
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw new AppException("ecmc.physical.servermodel",e);
		}
		return page;
	}
	/**
	 * 添加型号对象
	 * @param model
	 * @return
	 */
	@RequestMapping("/createModel")
	@ResponseBody
	public Object createModel(HttpServletRequest request,@RequestBody DcServerModel model){
		log.info("添加型号对象");
		EayunResponseJson res = new EayunResponseJson();
		try {
			
			
			model.setId(SeqManager.getSeqMang().getSeqForDate());
			model.setCreUser(EcmcSessionUtil.getUser() != null ? EcmcSessionUtil.getUser().getAccount() : null);
			model.setCreDate(new Date());
			dcservermodelservice.addDcServerModel(model);
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setMessage("添加服务器型号成功");
			ecmcLogService.addLog("添加服务器型号", "服务器型号", model.getName(), null, 1, model.getId(), null);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("添加失败："+e.getMessage());
			ecmcLogService.addLog("添加服务器型号", "服务器型号", model.getName(), null, 0, model.getId(), e);
		}
		return res;
	}
	
	/**
	 * 修改型号对象
	 * @param model
	 * @return
	 */
	@RequestMapping("/updateModel")
	@ResponseBody
	public Object updateModel(@RequestBody DcServerModel model){
		log.info("修改型号对象");
		EayunResponseJson res = new EayunResponseJson();
		try {
			dcservermodelservice.updateDcServerModel(model);
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setMessage("修改型号成功");
			
			ecmcLogService.addLog("修改服务器型号", "服务器型号", model.getName(), null, 1, model.getId(), null);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("修改失败："+e.getMessage());
			ecmcLogService.addLog("修改服务器型号", "服务器型号", model.getName(), null, 0, model.getId(), e);
		}
		return res;
	}
	/**
	 * 批量删除型号
	 * @param ids
	 * @return
	 */
	@RequestMapping("/deleteModel")
	@ResponseBody
	public Object deleteModel(@RequestBody Map<String, String> params){
		log.info("批量删除型号");
		EayunResponseJson res = new EayunResponseJson();
		String id="";
		DcServerModel Model= null;
		try {
			String ids=params.get("ids");
			if(!"".equals(ids)){
				Model=dcservermodelservice.getById(ids);
			int num = dcservermodelservice.deleteDcServerModels(ids);
			
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setMessage("删除服务器型号,成功 "+num+" 条");
		
			if(ids.length()>100){
				 id=ids.substring(1, 100);
			}else{
				id=ids;
			}
			if(Model!=null){
				ecmcLogService.addLog("删除服务器型号", "服务器型号", Model.getName(), null, 1,id, null);
			}
			
			}
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("删除失败："+e.getMessage());
			if(Model!=null){
				
			}else{
				ecmcLogService.addLog("删除服务器型号", "服务器型号", null, null, 1, id, e);
			}
			
		}
		return res;
	}
	
	/**
	 * 检查服务器型号是否已经使用
	 * @param request
	 * @return
	 */
	@RequestMapping("/checkUseOrNo")
	@ResponseBody
	public Object checkUseOrNo(@RequestBody Map<String, String> params){
		log.info("检查服务器型号是否已经使用");
		EayunResponseJson res = new EayunResponseJson();
		try {
			List<BaseDcServer> list = dcservermodelservice.checkUseOrNo(params.get("DcServerModelID"));
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			if(list.size()==0){
				res.setData(false);
			}else{
				res.setData(true);
				res.setRespCode(ConstantClazz.ERROR_CODE);
			}
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("操作失败："+e.getMessage());
		}
		return res;
	}
	
	/**
	 * 添加时判断名称是否存在
	 *
	 */
	@RequestMapping("/checkNameExist")
	@ResponseBody
	public Object checkNameExist(@RequestBody Map<String, String> params){
		log.info("判断名称是否存在");
		EayunResponseJson res = new EayunResponseJson();
		try {
			List<DcServerModel> list = dcservermodelservice.checkByName(params.get("name"));
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			if(list!=null && list.size()>0){
				res.setData(true);
			}else{
				res.setData(false);
			}
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("操作失败："+e.getMessage());
		}
		return res;
	}
	
	/**
	 * 修改时判断存储是否存在
	 * */
	@RequestMapping("/checkNameExistOfEdit")
	@ResponseBody
	public Object checkNameExistOfEdit(@RequestBody Map<String, String> params){
		log.info("判断存储是否存在");
		EayunResponseJson res = new EayunResponseJson();
		try {
			List<DcServerModel> list = dcservermodelservice.checkByNameNoID(params.get("name"), params.get("id"));
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			if(list!=null && list.size()>0){
				res.setData(true);
			}else{
				res.setData(false);
			}
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("操作失败："+e.getMessage());
		}
		return res;
	}
	/**
	 * 根据ID 获取单个对象
	 * @param prames
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getDcServerOne")
	@ResponseBody
	public Object getDcServerOne(@RequestBody Map<String, String> prames)throws AppException{
		log.info("根据ID 获取单个对象");
		return dcservermodelservice.getById(prames.get("id"));
	}
	
}
