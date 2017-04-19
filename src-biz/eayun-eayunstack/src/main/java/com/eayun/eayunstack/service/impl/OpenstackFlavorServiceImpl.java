package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Flavor;
import com.eayun.eayunstack.service.OpenstackFlavorService;
import com.eayun.eayunstack.service.RestService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudFlavor;

@Service
public class OpenstackFlavorServiceImpl extends OpenstackBaseServiceImpl<Flavor>  implements OpenstackFlavorService {
	@Autowired
	private RestService restService ;
	
	public List<BaseCloudFlavor> getStackList (BaseDcDataCenter dataCenter){
		List <BaseCloudFlavor> list = new ArrayList<BaseCloudFlavor>();                                         
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.COMPUTE_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.FLAVOR_URI+"/detail");                                           
		List<JSONObject> result = restService.list(restTokenBean,"flavors");
                                                                                                            
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				Flavor data = restService.json2bean(jsonObject,                                             
						Flavor.class);                                                                            
				BaseCloudFlavor ccn=new BaseCloudFlavor(data,dataCenter.getId());                                 
					list.add(ccn);                                                                                    
				}                                                                                                   
			}                                                                                                     
		return list;   
	}
}
