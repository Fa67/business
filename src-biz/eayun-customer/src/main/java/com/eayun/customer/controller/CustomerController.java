package com.eayun.customer.controller;

import java.io.IOException;
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
import com.eayun.common.controller.BaseController;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.log.service.LogService;
/**
 * 客户管理
 * @Filename: CustomerController.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年10月20日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/sys/customer")
@Scope("prototype")
public class CustomerController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService     customerService;
    
    @Autowired
	private LogService logService;

    /**
     * 添加客户
     * @param request
     * @param customer
     * @return
     * @throws Exception
     * @throws IOException 
     */
    @RequestMapping(value = "/addCustomer" , method = RequestMethod.POST)
    @ResponseBody
    public String addCustomer(HttpServletRequest request, @RequestBody Customer customer) throws IOException {
        log.info("添加客户开始");
        try {
            customerService.addCustomer(customer);
        } catch (Exception e) {
            log.error("添加客户失败", e);
            throw e;
        }
        return JSONObject.toJSONString("success");
    }

    /**
     * 查询客户
     * @param request
     * @param customer
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getListByCustomer" , method = RequestMethod.POST)
    @ResponseBody
    public String getListByCustomer(HttpServletRequest request, @RequestBody Customer customer) {
        List<Customer> cusList = customerService.getListByCustomer(customer);
        return JSONObject.toJSONString(cusList);
    }

    /**
     * 修改客户
     * @param request
     * @param customer
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateCustomer" , method = RequestMethod.POST)
    @ResponseBody
    public String updateCustomer(HttpServletRequest request, @RequestBody Customer customer) {
        log.info("更新客户信息开始");
        try {
            customerService.updateCustomer(customer);
            logService.addLog("修改公司信息", "公司信息", null, null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
            log.error("更新客户信息失败", e);
            logService.addLog("修改公司信息", "公司信息", null, null,ConstantClazz.LOG_STATU_ERROR, e);
            throw e;
        }
        return JSONObject.toJSONString("success");
    }

    /**
     * 根据id查询客户
     * @param request
     * @param cusId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/findCustomerById" , method = RequestMethod.POST)
    @ResponseBody
    public String findCustomerById(HttpServletRequest request , @RequestBody Map map) {
        log.info("获取客户信息开始");
        String cusId = map.get("cusId").toString();
        Customer customer = customerService.findCustomerById(cusId);
        return JSONObject.toJSONString(customer);
    }

    /**
     * 删除客户(预留,没有使用场景。)
     * @param request
     * @param customer
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/deleteCustomer" , method = RequestMethod.POST)
    @ResponseBody
    public boolean deleteCustomer(HttpServletRequest request, @RequestBody Customer customer) {
        log.info("删除客户信息开始");
        boolean b = false;
        try {
            b = customerService.deleteCustomer(customer);
        } catch (Exception e) {
            log.error("删除客户信息失败", e);
            throw e;
        }
        return b;
    }
    /**
     * 获取当前登录用户所属客户公司的信息
     * @param request
     * @return
     */
    @RequestMapping(value = "/findCustomerByUser" , method = RequestMethod.POST)
    @ResponseBody
    public String findCustomerByUser(HttpServletRequest request) {
        log.info("获取客户信息开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        Customer customer  = customerService.findCustomerById(cusId);
        return JSONObject.toJSONString(customer);
    }
    /**
     * 校验公司中文名称
     * @param request
     * @param params
     * @return
     */
    @SuppressWarnings("rawtypes")
    @RequestMapping(value = "/checkcusCpname" , method = RequestMethod.POST)
    @ResponseBody
    public String checkcusCpname(HttpServletRequest request, @RequestBody Map params) {
        log.info("校验公司中文名称");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String modelCusid = sessionUser.getCusId();
        String cusCpname = params.get("cusCpname").toString();
        boolean isSameName = customerService.checkCpname(modelCusid, cusCpname);
        return JSONObject.toJSONString(isSameName);
    }
}
