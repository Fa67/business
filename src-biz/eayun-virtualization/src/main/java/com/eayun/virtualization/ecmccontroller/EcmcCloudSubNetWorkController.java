package com.eayun.virtualization.ecmccontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.virtualization.ecmcservice.EcmcCloudFloatIPService;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudVm;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年4月6日
 */
@Controller
@RequestMapping("/ecmc/cloud/cloudnetwork")
public class EcmcCloudSubNetWorkController {

	public final static Logger log = LoggerFactory.getLogger(EcmcCloudSubNetWorkController.class);
	
	@Autowired
	private EcmcCloudFloatIPService cloudfloatipservice;
	
	/**
	 * 分页查询子网网段内所有IP地址
	 * @param pageSize
	 * @param pageNo
	 * @param cidr
	 * @return
	 */
	@RequestMapping("/queryIPList")
    @ResponseBody
	public Object getIPList(@RequestBody ParamsMap params){
		log.info("分页查询子网网段内所有IP地址");
		int pagesize = params.getPageSize() == null ? 20 : params.getPageSize().intValue();
		int pageno = params.getPageNumber() == null ? 1 : params.getPageNumber().intValue();
		String cidr = params.getParams().get("cidr") == null ? null : params.getParams().get("cidr").toString();
		Map<String, Object> map = new HashMap<String,Object>();
		Map<String, Object> mapaddress = pageAddresses(pagesize, pageno, cidr);
		List<String> strs = (ArrayList<String>)mapaddress.get("date");
		try {
			log.info(strs.get(0)+"/"+strs.get(strs.size()-1));
			List<Map<String, Object>> list = cloudfloatipservice.getIPList(strs.get(0), strs.get(strs.size()-1));
			Iterator it = strs.iterator();//动态移除list元素必须使用迭代  
			for(Map<String, Object> mapa : list){
				while(it.hasNext()) {
					String str = (String)it.next();
					if(mapa.get("floIp").equals(str)){
						it.remove();
					}           
				}
			}
			Map<String, Object> newmap = null;
			for(int i=0;i<strs.size();i++){
				newmap = new HashMap<String,Object>();
				newmap.put("floId", null);
				newmap.put("floIp", strs.get(i));
				newmap.put("prjName", null);
				newmap.put("createTime", null);
				newmap.put("netId", null);
				newmap.put("vmId", null);
				newmap.put("dcname", null);
				list.add(newmap);
			}
			Page resultPage = new Page(pagesize*(pageno-1), Long.parseLong(mapaddress.get("totalSize").toString()), pagesize, list);
			map.put("CODE", ConstantClazz.SUCCESS_CODE);
			map.put("DATE", resultPage);
		} catch (Exception e) {
			log.error(e.toString(),e);
			map.put("CODE", ConstantClazz.ERROR_CODE);
			map.put("DATE", e.getMessage());
		}
		return map;
	}
	/**
	 * 分页解析网段内地址
	 * @param pageSize
	 * @param pageNo
	 * @param cidr
	 * @return
	 */
	public Map<String, Object> pageAddresses(int pageSize,int pageNo,String cidr){
		SubnetUtils sub = new SubnetUtils(cidr);
		String[] strs = sub.getInfo().getAllAddresses();
		Map<String, Object> map = new HashMap<String,Object>();
		log.info("共："+strs.length+"个IP");
		int pagecount = strs.length%pageSize == 0 ? strs.length/pageSize : (strs.length/pageSize+1);
		log.info("每页"+pageSize+"个，共："+pagecount+" 页");
		int lastPageNosize = strs.length-strs.length/pageSize*pageSize;//最后一页的条数
		List<String> pages = new ArrayList<String>();
		int length = pageSize;
		if(pageNo>strs.length/pageSize){
			length = lastPageNosize;
		}
		for(int i=0;i<length;i++){
			pages.add(strs[pageSize*(pageNo-1)+i]);
		}
		map.put("count", pagecount);
		map.put("totalSize", strs.length);
		map.put("date", pages);
		return map;
	}
	
	/**
	 * 获取IP关联对象（云主机）
	 * @param ip
	 * @param vmId
	 * @return
	 */
	@RequestMapping("/getVmByVmId")
    @ResponseBody
	public Object getVmByVmId(@RequestBody Map<String, String> params){
		log.info("获取IP关联对象（云主机）");
		Map<String, Object> map = new HashMap<String,Object>();
		try {
			List<BaseCloudVm> vmlist = cloudfloatipservice.findFloatIpOne(params.get("vmId"));
			map.put("CODE", ConstantClazz.SUCCESS_CODE);
			map.put("DATE", vmlist);
		} catch (Exception e) {
			log.error(e.toString(),e);
			map.put("CODE", ConstantClazz.ERROR_CODE);
			map.put("DATE", e.getMessage());
		}
		return map;
	}
	
}
