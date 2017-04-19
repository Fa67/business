package com.eayun.syssetup.ecmccontroller;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.syssetup.ecmcservice.EcmcCloudModelService;
import com.eayun.syssetup.model.CloudModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by eayun on 2016/5/6.
 */

@Controller
@RequestMapping("/ecmc/syssetup")
@Scope("prototype")
public class EcmcCloudModelController {
    private static final Logger log = LoggerFactory.getLogger(EcmcCloudModelController.class);

    @Autowired
    private EcmcCloudModelService ecmcCloudModelService;

    @RequestMapping(value="/getmodellistbycustomer", method = RequestMethod.POST)
    @ResponseBody
    public String getModelListByCustomer(HttpServletRequest request, @RequestBody Map<String, String> map){
        String customerId = map.get("cusId");
        EayunResponseJson json = new EayunResponseJson();
        List<CloudModel> modelList = ecmcCloudModelService.getModelListByCustomer(customerId);
        json.setData(modelList);
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        return JSONObject.toJSONString(json);
    }
}
