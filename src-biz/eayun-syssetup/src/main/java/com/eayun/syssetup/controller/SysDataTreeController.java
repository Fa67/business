package com.eayun.syssetup.controller;

import com.eayun.common.controller.BaseController;
import com.eayun.syssetup.service.SysDataTreeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2016/8/22.
 */
@Controller
@RequestMapping("/sysdatatree")
@Scope("prototype")
public class SysDataTreeController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(CloudModelController.class);
    @Autowired
    private SysDataTreeService sysDataTreeService;

    /**
     * 获取按需购买的最低额度
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getbuycondition", method = RequestMethod.POST)
    @ResponseBody
    public String getBuyCondition(HttpServletRequest request) {
        String limit = sysDataTreeService.getBuyCondition();
        return limit;
    }

    /**
     * 获取重新恢复资源的最低额度
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getrenewcondition", method = RequestMethod.POST)
    @ResponseBody
    public String getRenewCondition(HttpServletRequest request) {
        String limit = sysDataTreeService.getRenewCondition();
        return limit;
    }

    /**
     * 获取欠费或者到期的保留时长
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getrecoverytime", method = RequestMethod.POST)
    @ResponseBody
    public String getRecoveryTime(HttpServletRequest request) {
        String timeLength = sysDataTreeService.getRecoveryTime();
        return timeLength;
    }

    /**
     * 获取回收站的保留时长
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getretaintime", method = RequestMethod.POST)
    @ResponseBody
    public String getRetainTime(HttpServletRequest request) {
        String timeLength = sysDataTreeService.getRetainTime();
        return timeLength;
    }
}
