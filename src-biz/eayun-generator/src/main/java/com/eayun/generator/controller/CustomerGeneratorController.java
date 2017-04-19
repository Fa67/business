package com.eayun.generator.controller;

import com.eayun.common.ConstantClazz;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.customer.model.Customer;
import com.eayun.generator.constant.CusGeneratorConstant;
import com.eayun.generator.service.CustomerGeneratorService;
import com.eayun.order.service.OrderService;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ZH.F on 2016/12/21.
 */
@Controller
@RequestMapping("/auto/customer")
public class CustomerGeneratorController {
    private final static Logger log = LoggerFactory.getLogger(CustomerGeneratorController.class);
    @Autowired
    private CustomerGeneratorService customerGeneratorService;

    @Autowired
    private JedisUtil jedisUtil;

    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "/bulkCreateProjects")
    @ResponseBody
    public Object createProject(HttpServletRequest request) throws Exception{
        EayunResponseJson reJson = new EayunResponseJson();
        Map params = new HashMap<>();
        params.put("cusOrg","autoGeneratedCus");//add sn
        params.put("cusNumber","autoGeneratedCus");//add sn
        params.put("cus_type",0);
        params.put("cusEmail","autoGeneratedCus");//add sn and @xxx.com
        params.put("cusCpname","autoGeneratedCus");//add sn
        params.put("imageCount",10);
        params.put("prjName","autoGeneratedCus_");//add sn
        params.put("dcId",CusGeneratorConstant.DC_ID);
        params.put("cpuCount","1000");
        params.put("memory","1000");
        params.put("hostCount","1000");
        params.put("diskCount","1000");
        params.put("diskSnapshot","1000");
        params.put("diskCapacity","1000");
        params.put("snapshotSize","1000");
        params.put("countBand","100");
        params.put("netWork","1000");
        params.put("routeCount","1000");
        params.put("subnetCount","1000");
        params.put("outerIP","1000");
        params.put("safeGroup","1000");
        params.put("quotaPool","1000");
        params.put("smsCount","1000");
        params.put("countVpn","1000");
        params.put("portMappingCount","1000");
        for(int i=0;i<100;i++){
            String snKey = CusGeneratorConstant.CUS_PREFIX + ":sn";
            jedisUtil.increase(snKey);
            String sn = jedisUtil.get(snKey);
            Customer customer = new Customer();
            BeanUtils.mapToBean(customer, params);
            customer.setCusOrg(CusGeneratorConstant.CUS_PREFIX+sn);
            customer.setCusName(CusGeneratorConstant.CUS_PREFIX+sn);
            customer.setCusNumber(CusGeneratorConstant.CUS_PREFIX+sn);
            customer.setCusEmail(CusGeneratorConstant.CUS_PREFIX+sn+"@elbarco.cn");
            customer.setCusCpname(CusGeneratorConstant.CUS_PREFIX+sn);
            CloudProject cloudProject = new CloudProject();
            BeanUtils.mapToBean(cloudProject, params);
            cloudProject.setPrjName(CusGeneratorConstant.CUS_PREFIX+"_"+sn);
            cloudProject.setProjectId(CusGeneratorConstant.PROJECT_ID+sn);
            try {
                Map<String,Object> returnMap = customerGeneratorService.createProject(cloudProject, customer, false);
                customer = (Customer) MapUtils.getObject(returnMap, "customer");
                BaseCloudProject project = (BaseCloudProject)MapUtils.getObject(returnMap, "project");
                log.info("创建客户"+customer.getCusNumber()+"和项目成功");
                reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            } catch (Exception e) {
                reJson.setRespCode(ConstantClazz.ERROR_CODE);
                log.error("创建客户"+customer.getCusNumber()+"和项目失败", e);
            }
        }
        reJson.setMessage("Bulk creating projects SUCCESS");
        return reJson;
    }

    @RequestMapping(value = "/bulkCreateOrders")
    @ResponseBody
    public Object bulkCreateOrders(HttpServletRequest request) throws Exception {
        EayunResponseJson resp = new EayunResponseJson();
        try{
            customerGeneratorService.bulkCreateOrders();
            resp.setRespCode(ConstantClazz.SUCCESS_CODE);
            resp.setMessage("Bulk order created successfully");
        }catch(Exception e){
            resp.setRespCode(ConstantClazz.ERROR_CODE);
        }
        return resp;
    }

    @RequestMapping(value = "/bulkCreateTradeRecords")
    @ResponseBody
    public Object bulkCreateTradeRecords(HttpServletRequest request) throws Exception {
        EayunResponseJson resp = new EayunResponseJson();
        try{
            customerGeneratorService.bulkCreateTradeRecords();
            resp.setRespCode(ConstantClazz.SUCCESS_CODE);
            resp.setMessage("Bulk trade records created successfully");
        }catch(Exception e){
            resp.setRespCode(ConstantClazz.ERROR_CODE);
        }
        return resp;
    }
    @RequestMapping(value = "/bulkCreateAlarmMessage")
    @ResponseBody
    public Object bulkCreateAlarmMessage(HttpServletRequest request) throws Exception {
        EayunResponseJson resp = new EayunResponseJson();
        try{
            customerGeneratorService.bulkCreateAlarmMessage();
            resp.setRespCode(ConstantClazz.SUCCESS_CODE);
            resp.setMessage("Bulk alarm message created successfully");
        }catch(Exception e){
            resp.setRespCode(ConstantClazz.ERROR_CODE);
        }
        return resp;
    }
}