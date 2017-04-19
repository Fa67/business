package com.eayun.api.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.ApiException;
import com.eayun.virtualization.apiservice.VmApiService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring/*.xml"})
public class VmApiServiceTest {
	@Autowired
	private VmApiService vmApiService;
	
	@Test
	public void testCreateInstance(){
		JSONObject params = new JSONObject();
		JSONObject result = new JSONObject();
		try {
			params.put("DcId", "1604271035270");
			params.put("CusId", "40289095573bedea01573bf89b210006");
			params.put("VPCId", "a08244c4-f6a1-41e4-91d7-e845516aa9b1");
			params.put("MsubNetId", "a504bda5-8128-4c9d-abcd-2760d14591a6");
			params.put("UmsubNetId", "4b3c0917-a7f6-4eeb-b37f-83b823bfe947");
			params.put("FloatIP", "");
			params.put("ImageType", "1");
			params.put("ImageId", "005f2691-9de9-4b2e-a4ac-a0232057dd9b");
			params.put("Cpu", "2");
			params.put("Memory", "2");
			params.put("LoginMode", "passwd");
			params.put("Keypair", "");
			params.put("Password", "1234qwer,./");
			params.put("InstanceName", "Ubuntu1");
			params.put("InsanceRemark", "");
			params.put("SecurityGroupName", "0");
			params.put("Count", "1");
			params.put("PayType", "Pay_Month");
			params.put("Payduration", "13");
			result = vmApiService.createInstance(params);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		System.out.println(result);
	}
}
