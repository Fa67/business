package com.eayun.customer.ecmccontroller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;  
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.experimental.results.ResultMatchers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;  
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.testng.Reporter;
import org.testng.annotations.Test;

import com.alibaba.fastjson.JSON;
import com.eayun.api.test.AbstrackApiTests;

@Test(dependsOnGroups="init")
public class EcmcCustomerControllerTest extends AbstrackApiTests {

	private void testCustomerOverview() {
		
	}

	@Test
	public void testCheckCusOrg() {
		
	}

	@Test
	public void testCheckCusPhone() {
		
	}

	@Test
	public void testCheckCusEmail() {
		
	}

	@Test
	public void testCheckCusAdmin() {
		
	}

	@Test
	public void testCheckCusCpname() {
		
	}

	@Test
	public void testGetCustomerList() throws Exception {
		ResultActions resultActions = getMockMvc().perform(post("/ecmc/customer/getcustomerlist")
				.content("{}").contentType(MediaType.APPLICATION_JSON))
		.andDo(print()).andExpect(status().isOk())
		.andExpect(jsonPath("$.result").isArray())
		.andExpect(jsonPath("$.pageSize").value(20));
		
		// Reporter.log(resultActions.andReturn().getResponse().getContentAsString());
	}

	@Test
	public void testGetCusWithAdminById() throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("cusId", "40289090576a284901576a31f2da0000");
		
		getMockMvc().perform(post("/ecmc/customer/getcuswithadminbyid")
				.content(JSON.toJSONString(params)).contentType(MediaType.APPLICATION_JSON))
		.andDo(print()).andExpect(status().isOk())
		.andExpect(jsonPath("$.data").exists());
	}

	@Test
	public void testGetCustomerById() {
		
	}

	@Test
	public void testModifyCustomer() {
		
	}

	@Test
	public void testGetAllCustomerOrg() {
		
	}

	@Test
	public void testGetUserAccountByCusId() {
		
	}

	@Test
	public void testResetCusAdminPass() {
		
	}

	@Test
	public void testBlockCustomer() {
		
	}

	@Test
	public void testUnblockCustomer() {
		
	}

	@Test
	public void testChangeBalance() {
		
	}

	@Test
	public void testGetExpireResourceList() {
		
	}

	@Test
	public void testGetUncreatedCusNum() {
		
	}

	@Test
	public void testGetNotCreatedCusList() {
		
	}

	@Test
	public void testModifyCreditLines() {
		
	}

	@Test
	public void testGetCusReport() {
		
	}

	@Test
	public void testGetTotalCost() {
		
	}

	@Test
	public void testCreatePostPaidExcel() {
		
	}

	@Test
	public void testCreatePrepaymentExcel() {
		
	}

	@Test
	public void testGetPostpayDetail() {
		
	}

	@Test
	public void testGetCusRecords() {
		
	}

	@Test
	public void testCreateRecordExcel() {
		
	}

}
