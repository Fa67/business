package com.eayun.physical.ecmccontroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.physical.ecmcservice.EcmcCabinetService;
import com.eayun.physical.model.BaseDcCabinet;
import com.eayun.physical.model.DcCabinet;

@Controller
@RequestMapping("/ecmc/physical/cabinet")
public class EcmcCabinetController {
	
	@SuppressWarnings("unused")
    private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	private EcmcCabinetService ecmcCabinetService;
	
	@Autowired
	private EcmcLogService ecmcLogService;
	

	
	/**
	 * 获取机柜列表
	 * */
	@SuppressWarnings("unchecked")
    @RequestMapping("/querycabinet")
	@ResponseBody
	public Object query(@RequestBody Map<String,Object> requestMap)throws AppException{
		Page page =new Page();
		//EayunResponseJson reJson = new EayunResponseJson();
		
			Map<String,Object> map=(Map<String,Object>)requestMap.get("params");
		
			
			String datacenterId = MapUtils.getString(map, "dataCenterId");
			String Name = MapUtils.getString(map, "cabinetName");
			int pageSize=MapUtils.getIntValue(requestMap, "pageSize");
			int pageNo=MapUtils.getIntValue(requestMap, "pageNumber");
		
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
			
			page=ecmcCabinetService.query(Name,datacenterId,queryMap);
			
		
		return page;
	}
	
	/**
	 * 删除机柜
	 * */
	@RequestMapping("/deletecabinet")
	@ResponseBody
	public Object delete(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson reJson = new EayunResponseJson();
		
			String cabinetId=MapUtils.getString(requestMap, "cabinetId");
			String dataCenterId=MapUtils.getString(requestMap, "dataCenterId");
			DcCabinet model = ecmcCabinetService.queryById(cabinetId);
			String result = ecmcCabinetService.delete(cabinetId,dataCenterId);
			if("success".equals(result)){
				
				reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				ecmcLogService.addLog("删除机柜", "机柜", model.getName(), null, 1, cabinetId, null);
			}else if("cant".equals(result)){
				
				reJson.setRespCode(ConstantClazz.WARNING_CODE);
				reJson.setMessage("需要删除的机柜中包含设备，不能删除");
				ecmcLogService.addLog("删除机柜", "机柜", model.getName(), null, 0, cabinetId, new AppException("机柜中包含设备"));
			}else{
				
				reJson.setRespCode(ConstantClazz.ERROR_CODE);
			}

		
		return reJson;
	}
	
	/**
	 * 根据机柜id获取机柜信息
	 * */
	@RequestMapping("/queryById")
	@ResponseBody
	public Object queryById(@RequestBody Map<String,Object> requestMap)throws AppException{
		
		EayunResponseJson reJson = new EayunResponseJson();
			
			String cabinetId = MapUtils.getString(requestMap, "cabinetId");
			
				DcCabinet model=ecmcCabinetService.queryById(cabinetId);
				reJson.setData(model);

		
		return reJson;
	}
	
	/**
	 * 更新机柜
	 * */
	@RequestMapping("/updatecabinet")
	@ResponseBody
	public Object update(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
			String cabinetId=MapUtils.getString(requestMap, "cabinetId");
			String dataCenterId=MapUtils.getString(requestMap, "dataCenterId");
			String cabinetName=MapUtils.getString(requestMap, "cabinetName");
			String totalCapacity=MapUtils.getString(requestMap, "totalCapacity");
			
			String code = ecmcCabinetService.update(cabinetId,dataCenterId,cabinetName,totalCapacity);
			if(code.equals("success")){
				resp.setRespCode(ConstantClazz.SUCCESS_CODE);
				ecmcLogService.addLog("更新机柜", "机柜", cabinetName, null, 1, cabinetId, null);
			}else if(code.equals("cant")){
				resp.setData("该机柜下有设备，不能更换数据中心");
				resp.setRespCode(ConstantClazz.ERROR_CODE);
				ecmcLogService.addLog("更新机柜", "机柜", cabinetName, null, 0, cabinetId, null);
			}

		
		return resp;
	}
	
	/**
	 * 新增机柜
	 * */
	@RequestMapping("/addcabinet")
	@ResponseBody
	public Object add(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
	
		BaseEcmcSysUser user=EcmcSessionUtil.getUser();
			
			String datacenterId=MapUtils.getString(requestMap, "dataCenterId");
			String cabinetName=MapUtils.getString(requestMap, "cabinetName");
			String totalCapacity=MapUtils.getString(requestMap, "totalCapacity");
			String cabinetNum=MapUtils.getString(requestMap, "cabinetNum");
			String [] ids=null;
				if(user!=null){
				ids=ecmcCabinetService.add(datacenterId,cabinetName,totalCapacity,cabinetNum,user.getAccount());
				resp.setRespCode(ConstantClazz.SUCCESS_CODE);
			
				}else{
					ids=ecmcCabinetService.add(datacenterId,cabinetName,totalCapacity,cabinetNum,"");
				}
				if(ids.length==1){
					ecmcLogService.addLog("新增机柜", "机柜", cabinetName, null, 1, ids[0], null);
				}else{
					for(int i=0;i<ids.length;i++){
						
						ecmcLogService.addLog("新增机柜", "机柜", cabinetName+"_"+(i+1), null, 1, ids[i], null);
					}
				}
				
				
		return resp;
	}
	
	/**
	 * 验证名称重复   
	 * */
	
	

	@RequestMapping("/checkNameExist")
	@ResponseBody
	public Object checkName(@RequestBody Map<String,Object> requestMap)throws AppException{
		
		EayunResponseJson resp =new EayunResponseJson();
		
		   String name=MapUtils.getString(requestMap, "cabinetName");
		   String num=MapUtils.getString(requestMap, "num");
		   String dataCenterId=MapUtils.getString(requestMap, "dataCenterId");
		   String id=MapUtils.getString(requestMap, "cabinetId");
		   resp.setRespCode(ConstantClazz.SUCCESS_CODE);
		  List<String> listr =new ArrayList<String>();
		   if(num!=null&&!num.equals("1")){
			  
				   for(int j=1;j<=Integer.parseInt(num);j++){
					   String a=name+"_"+j+"";
					   listr.add(a);
					 
				   }
				   List <BaseDcCabinet>li= ecmcCabinetService.checkNameExist(listr, dataCenterId);
				   if(li!=null&&li.size()>0){
					   resp.setRespCode(ConstantClazz.ERROR_CODE);
					   resp.setMessage("添加时验证名称重复");
				   }
			   
		   }else{
			   listr.add(name);
			   if(id != null){//编辑是判断
				   List<BaseDcCabinet> list= ecmcCabinetService.checkNameExist(name, dataCenterId,id);
				   if(list.size()>0){
				   resp.setRespCode(ConstantClazz.ERROR_CODE);
				   resp.setMessage("修改时验证名称重复");
				   }
				   return resp;
				   
			   }
			   List <BaseDcCabinet>li= ecmcCabinetService.checkNameExist(listr, dataCenterId);
			   if(li!=null&&li.size()>0){
				   resp.setRespCode(ConstantClazz.ERROR_CODE);
				   resp.setMessage("添加时验证名称重复");
			   }
			   }

		return resp;
	}

	/**
	 * 根据机柜id查询机柜使用详情
	 * */
	@SuppressWarnings("unchecked")
    @RequestMapping("/queryEquById")
	@ResponseBody
	public Object queryEquById(@RequestBody Map<String,Object> requestMap)throws AppException{
	//	EayunResponseJson resp =new EayunResponseJson();
		Page page=new Page();
		int pageSize=MapUtils.getIntValue(requestMap, "pageSize");
		int pageNo=MapUtils.getIntValue(requestMap, "pageNumber");
	
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
		
			Map<String,Object> map=(Map<String,Object>)requestMap.get("params");
			String id = MapUtils.getString(map, "cabientId");//机柜id
			String dcId = MapUtils.getString(map, "datacenterId");//数据中心id
			page=ecmcCabinetService.queryEquById(id,dcId,page,queryMap);
		return  page;
	}
	
	/**
	 * 检查选中数据中心下面的可用资源
	 * @param request
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/checkDataCenter")
	@ResponseBody
	public Object checkResourcesOfCheckedDataCenter(@RequestBody Map<String,Object> requestMap)throws AppException{

		EayunResponseJson resp =new EayunResponseJson();
		
			String dcid = MapUtils.getString(requestMap, "dataCenterId");//数据中心id
			String cabinetid = MapUtils.getString(requestMap, "cabinetId");//机柜id
			BaseDcDataCenter dataCenter = ecmcCabinetService.getDateCenterById(dcid);
			int count = ecmcCabinetService.getCountByHql(dcid);//该数据中心已用容量
			int canUse = dataCenter.getCabinetCapacity().intValue()-count;//该数据中心下面可用容量
		
			if("undefined".equals(cabinetid)||"".equals(cabinetid)){//说明是增加

			}else{//说明是修改
				if(canUse>0){
					
				}else if(canUse==0){//用完了
					count = ecmcCabinetService.getCountByHql(dcid,cabinetid);
					if(count==1){//说明该机柜本就存在在该数据中心下面
						canUse=-1;//用-1表示本就存在于该数据中心
					}
				}
			}
			resp.setData(canUse);
			return resp;

		
	}
	
	/**
	 * 获取数据中心下所有的可用的机柜
	 * */
	@RequestMapping("/getcanUseCabinet")
	@ResponseBody
	public Object getcanUseCabinet(@RequestBody Map<String,Object> requestMap)throws AppException{
		
		EayunResponseJson resp =new EayunResponseJson();
		
		
			String datacenterid = MapUtils.getString(requestMap, "dataCenterId");
			String equipmentId = MapUtils.getString(requestMap, "cabinetId");//设备id
			resp.setData(ecmcCabinetService.getCabinet(datacenterid,equipmentId));
			

		return resp;
	}
	
	/**
	 * 检查机柜规格是否合法
	 * */
	@RequestMapping("/checkTotalCapacity")
	@ResponseBody
	public Object checkTotalCapacity(@RequestBody Map<String,Object> requestMap)throws AppException{
		
		EayunResponseJson resp =new EayunResponseJson();
		boolean flag=true;
		String message="";
		
			String id = MapUtils.getString(requestMap, "cabinetId");//机柜id
			String totalCapacity = MapUtils.getString(requestMap, "totalCapacity");//机柜规格
			int editTotalCapacity=new Integer(totalCapacity);
			int maxUsedLocation = ecmcCabinetService.getMaxUsedLocation(id);//使用的最大位置
			if((editTotalCapacity) < (maxUsedLocation) && maxUsedLocation!=-1){//修改后 的规格不合法
				flag=false;
				message="不能小于已使用的最大位置："+maxUsedLocation;
				resp.setRespCode(ConstantClazz.ERROR_CODE);
			}
			if(maxUsedLocation==-1){//说明该机柜是空的，
				flag=true;
				message="设置成功";
				resp.setRespCode(ConstantClazz.SUCCESS_CODE);
			}
			resp.setData(flag);
			resp.setMessage(message);
			
		
		return resp;
	}
	
	/**
	 * 获取机柜中的可用位置
	 * */
	@RequestMapping("getStateByCabinet")
	@ResponseBody
	public Object getStateByCabinet(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp =new EayunResponseJson();
		
			String cabinetId = MapUtils.getString(requestMap, "cabinetId");
			String spec = MapUtils.getString(requestMap, "spec");
			String dataCenterId = MapUtils.getString(requestMap, "dataCenterId");
			String id = MapUtils.getString(requestMap, "id");
			resp.setData(ecmcCabinetService.getstateByCabinet(dataCenterId,cabinetId,spec,id)); 

		return resp;
	}
	
	/**
//	 * 获取数据中心下所有的机柜
//	 * */
	@RequestMapping("getcabinet")
	@ResponseBody
	public Object getcabinet(@RequestBody Map<String,Object> requestMap)throws AppException{
		JSONObject object = new JSONObject();
		
			String datacenterid =MapUtils.getString(requestMap, "dataCenterId");
			
			ecmcCabinetService.getCabinet(datacenterid,null);

		return object;
	}

	
}
