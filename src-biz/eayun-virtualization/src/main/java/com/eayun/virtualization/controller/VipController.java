package com.eayun.virtualization.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.eayun.common.controller.BaseController;

@Controller
@RequestMapping("/cloud/loadbalance/vip")
@Scope("prototype")
public class VipController extends BaseController{
	
}
