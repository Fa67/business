package com.eayun.customer.ecmccontroller;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.tools.ExportDataToExcel;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.costcenter.bean.ExcelRecord;
import com.eayun.costcenter.bean.RecordBean;
import com.eayun.costcenter.model.MoneyRecord;
import com.eayun.costcenter.service.AccountOverviewService;
import com.eayun.costcenter.service.ChangeBalanceService;
import com.eayun.costcenter.service.CostReportService;
import com.eayun.customer.ecmcservice.ApiRestrictService;
import com.eayun.customer.ecmcservice.BlockCloudResService;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.customer.model.ApiCountRestrict;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.CusBlockResourceVoe;
import com.eayun.customer.model.Customer;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.service.EcmcSysUserService;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.invoice.service.InvoiceService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.notice.service.NoticeService;


@Controller
@RequestMapping("/ecmc/customer")
public class EcmcCustomerController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(EcmcCustomerController.class);
	@Autowired
	private EcmcCustomerService ecmcCustomerService;
	@Autowired
	private BlockCloudResService blockResourceService;
	@Autowired
    private EcmcLogService ecmcLogService;
	@Autowired
    private EcmcSysUserService ecmcSysUserService;
	@Autowired
    private ChangeBalanceService   changeBalanceService  ;
	@Autowired
	private CostReportService costReportService;
	@Autowired
	private AccountOverviewService accountOverviewService;
	@Autowired
	private MessageCenterService messageCenterService;
	@Autowired
	private ApiRestrictService apiRestrictService;
	@Autowired
	private InvoiceService invoiceService;
	
	private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd");
	
	@RequestMapping(value = "/customeroverview")
	@ResponseBody
	public Object customerOverview(){
		return ecmcCustomerService.getCustomerOverview();
	}
	
	@RequestMapping(value = "/checkcusorg")
	@ResponseBody
	public Object checkCusOrg(@RequestBody Map<String, Object> requestMap) {
		EayunResponseJson reJson = new EayunResponseJson();
		String cusOrg = MapUtils.getString(requestMap, "cusOrg");
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		if (!StringUtil.isEmpty(cusOrg)) {
			reJson.setRespCode(ecmcCustomerService.checkCusOrg(cusOrg) ? ConstantClazz.SUCCESS_CODE
					: ConstantClazz.ERROR_CODE);
		}
		return reJson;
	}
	
	@RequestMapping(value = "/checkcusphone")
	@ResponseBody
	public Object checkCusPhone(@RequestBody Map<String, Object> requestMap){
		EayunResponseJson reJson = new EayunResponseJson();
		String cusPhone = MapUtils.getString(requestMap, "cusPhone");
		String cusId = MapUtils.getString(requestMap, "cusId");
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		if (!StringUtil.isEmpty(cusPhone)) {
			reJson.setRespCode(ecmcCustomerService.checkCusPhone(cusPhone, cusId) ? ConstantClazz.SUCCESS_CODE
					: ConstantClazz.ERROR_CODE);
		}
		return reJson;
	}
	
	@RequestMapping(value = "/checkcusemail")
	@ResponseBody
	public Object checkCusEmail(@RequestBody Map<String, Object> requestMap){
		EayunResponseJson reJson = new EayunResponseJson();
		String cusEmail = MapUtils.getString(requestMap, "cusEmail");
		String cusId = MapUtils.getString(requestMap, "cusId");
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		if (!StringUtil.isEmpty(cusEmail)) {
			reJson.setRespCode(ecmcCustomerService.checkCusEmail(cusEmail, cusId) ? ConstantClazz.SUCCESS_CODE
					: ConstantClazz.ERROR_CODE);
		}
		return reJson;
	}
	
	@RequestMapping(value = "/checkcusadmin")
	@ResponseBody
	public Object checkCusAdmin(@RequestBody Map<String, Object> requestMap){
		EayunResponseJson reJson = new EayunResponseJson();
		String cusNumber = MapUtils.getString(requestMap, "cusNumber");
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		if (!StringUtil.isEmpty(cusNumber)) {
			reJson.setRespCode(ecmcCustomerService.checkCusAdmin(cusNumber.toLowerCase()) ? ConstantClazz.SUCCESS_CODE
					: ConstantClazz.ERROR_CODE);
		}
		return reJson;
	}
	
	@RequestMapping(value = "/checkcuscpname")
	@ResponseBody
	public Object checkCusCpname(@RequestBody Map<String, Object> requestMap){
		EayunResponseJson reJson = new EayunResponseJson();
		String cusCpname = MapUtils.getString(requestMap, "cusCpname");
		String cusId = MapUtils.getString(requestMap, "cusId");
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		if (!StringUtil.isEmpty(cusCpname)) {
			reJson.setRespCode(ecmcCustomerService.checkCusCpname(cusCpname, cusId) ? ConstantClazz.SUCCESS_CODE
					: ConstantClazz.ERROR_CODE);
		}
		return reJson;
	}
	
	@RequestMapping(value = "/getcustomerlist")
	@ResponseBody
	public Object getCustomerList(@RequestBody ParamsMap paramsMap) {
		QueryMap queryMap = new QueryMap();
		Map<String, Object> params = paramsMap.getParams();
		queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
		if (paramsMap.getPageSize() != null) {
			queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
		}
		String searchKey = MapUtils.getString(params, "searchKey");
		String isBlocked = MapUtils.getString(params, "isBlocked");
		try {
			return ecmcCustomerService.getCustomerList(searchKey, queryMap, isBlocked);
		} catch (Exception e) {
			throw e;
		}
	}
	
	@RequestMapping(value = "/getcuswithadminbyid")
	@ResponseBody
	public Object getCusWithAdminById(@RequestBody Map<String, Object> requestMap) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		String customerId = MapUtils.getString(requestMap, "cusId");
		if (!StringUtil.isEmpty(customerId)) {
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ecmcCustomerService.getCusWithAdminById(customerId));
			return reJson;
		}
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		return reJson;
	}
	
	@RequestMapping(value = "/getcustomerbyid")
	@ResponseBody
	public Object getCustomerById(@RequestBody Map<String, Object> requestMap) {
		EayunResponseJson reJson = new EayunResponseJson();
		String customerId = MapUtils.getString(requestMap, "cusId");
		if (!StringUtil.isEmpty(customerId)) {
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ecmcCustomerService.getCustomerById(customerId));
			return reJson;
		}
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		return reJson;
	}
	
	@RequestMapping(value = "/modifycustomer")
	@ResponseBody
	public Object modifyCustomer(@RequestBody Map<String, Object> requestMap) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		Customer customer = new Customer();
		BeanUtils.mapToBean(customer, requestMap);
		try {
			customer = ecmcCustomerService.updateCustomer(customer);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE );
			ecmcLogService.addLog("编辑客户信息", ConstantClazz.LOG_TYPE_CUSTOMER, customer.getCusOrg(), null, 1, customer.getCusId(), null);
			return reJson;
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("编辑客户信息", ConstantClazz.LOG_TYPE_CUSTOMER, customer.getCusOrg(), null, 0, customer.getCusId(), e);
			throw e;
		}
	}
	
	@RequestMapping(value = "/getallcustomerorg")
	@ResponseBody
	public Object getAllCustomerOrg() throws Exception{
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			reJson.setData(ecmcCustomerService.getAllCustomerOrg());
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			throw e;
		}
		return reJson;
	}
	
	/**
	 * 根据客户查询用户（Required by chenpengfei）
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/getuseraccountbycusid")
	@ResponseBody
	public Object getUserAccountByCusId(@RequestBody Map<String, Object> requestMap) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		String cusId = MapUtils.getString(requestMap, "cusId");
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		if (!StringUtil.isEmpty(cusId)) {
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ecmcCustomerService.getUserAccountByCusId(cusId));
		}
		return reJson;
	}
	
	/**
	 * 重置客户超级管理员账号密码（Required by chenpengfei）
	 * @return
	 */
	@RequestMapping(value = "/resetcusadminpass")
	@ResponseBody
	public Object resetCusAdminPass(@RequestBody Map<String, Object> requestMap) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		String cusId = MapUtils.getString(requestMap, "cusId");
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		if (!StringUtil.isEmpty(cusId)) {
			BaseCustomer customer = ecmcCustomerService.getCustomerById(cusId);
			if(customer != null){
				try{
					ecmcCustomerService.resetCusAdminPass(cusId);
					reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
					ecmcLogService.addLog("重置密码", ConstantClazz.LOG_TYPE_CUSTOMER, customer.getCusOrg(), null, 1,
							customer.getCusId(), null);
				}catch(Exception e){
					ecmcLogService.addLog("重置密码", ConstantClazz.LOG_TYPE_CUSTOMER, customer.getCusOrg(), null, 0,
							customer.getCusId(), e);
					throw e;
				}
			}
		}
		return reJson;
	}
	
	/**
	 * 冻结账户（Required by liyanchao）
	 * @return
	 */
	@RequestMapping(value = "/blockCustomer")
	@ResponseBody
	public Object blockCustomer(@RequestBody Map<String, Object> requestMap) throws Exception {
		log.info("****************冻结账户开始****************");
		EayunResponseJson reJson = new EayunResponseJson();
		String cusId = MapUtils.getString(requestMap, "cusId");
		CusBlockResourceVoe  cusBlockResource = new CusBlockResourceVoe();
		if (!StringUtil.isEmpty(cusId)) {
			cusBlockResource = blockResourceService.blockCloudResource(cusId);
			if(cusBlockResource.getIsBlocked() && cusBlockResource.getBlockopStatus()){
				reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				ecmcLogService.addLog("冻结账号",ConstantClazz.LOG_TYPE_CUSTOMER, cusBlockResource.getCusName(),null, 1,cusId,null);
			}else{
				reJson.setMessage(cusBlockResource.getErrorMsg());
				reJson.setRespCode(ConstantClazz.ERROR_CODE);
			}
			
		}
		log.info("****************冻结账户结束****************");
		return reJson;
	}
	/**
	 * 解冻账户（Required by liyanchao）
	 * @return
	 */
	@RequestMapping(value = "/unblockCustomer")
	@ResponseBody
	public Object unblockCustomer(@RequestBody Map<String, Object> requestMap) throws Exception {
		log.info("****************解冻账户开始****************");
		EayunResponseJson reJson = new EayunResponseJson();
		CusBlockResourceVoe blockResource = new CusBlockResourceVoe();
		String cusId = MapUtils.getString(requestMap, "cusId");
		if (!StringUtil.isEmpty(cusId)) {
			blockResource =blockResourceService.unblockCloudResource(cusId);
			if(null!=blockResource && !blockResource.getIsBlocked() && !blockResource.getBlockopStatus()){
				reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				ecmcLogService.addLog("恢复解冻",ConstantClazz.LOG_TYPE_CUSTOMER, blockResource.getCusName(),null, 1,cusId,null);
			}else{
			    if(blockResource != null){
			        reJson.setMessage(blockResource.getErrorMsg());
			    }
				reJson.setRespCode(ConstantClazz.ERROR_CODE);
			}
		}
		log.info("****************解冻账户结束****************");
		return reJson;
	}
	
	/**
	 * 调整账户金额（Required by liyanchao）
	 * @return
	 */
	@RequestMapping(value = "/changeBalance")
	@ResponseBody
	public Object changeBalance(@RequestBody RecordBean recordBean) throws Exception{
		//获取会话中的 用户
		BaseEcmcSysUser loginUser = EcmcSessionUtil.getUser();
		Customer customer = ecmcCustomerService.getCustomerById(recordBean.getCusId());
		Date exchangeTime = new Date();
		EayunResponseJson reJson = new EayunResponseJson();
		BigDecimal accountMoney = null;
		recordBean.setExchangeTime(exchangeTime);
		String ecmcRemark = "";
		String originEcmcRemark = recordBean.getEcmcRemark();
		if("1".equals(recordBean.getIncomeType())){//充值
			
			if("实际充值".equals(recordBean.getEcmcRemark())){
				ecmcRemark = "管理员"+loginUser.getName()+"为客户实际充值";
				recordBean.setEcmcRemark(ecmcRemark);
			}else if("额外赠送".equals(recordBean.getEcmcRemark())){
				ecmcRemark = "管理员"+loginUser.getName()+"为客户额外赠送";
				recordBean.setEcmcRemark(ecmcRemark);
			}else {//其它原因充值
				if(null != recordBean.getInputCause() && !"".equals(recordBean.getInputCause())){
					ecmcRemark = "管理员"+loginUser.getName()+"为客户充值<br>原因："+recordBean.getInputCause();
					recordBean.setEcmcRemark(ecmcRemark);
				}
			}
			
		}else{//扣费
			
			if(null != recordBean.getInputCause() && !"".equals(recordBean.getInputCause())){
				ecmcRemark = "管理员"+loginUser.getName()+"为客户扣费<br>原因："+recordBean.getInputCause();
				recordBean.setEcmcRemark(ecmcRemark);
			}
		}
		
		try {
			accountMoney = changeBalanceService.changeBalance(recordBean);
			if(null != accountMoney && "1".equals(recordBean.getIncomeType())){//充钱给客户发短信
				messageCenterService.accountPayMessage(recordBean.getCusId(), recordBean.getExchangeMoney(), accountMoney);
				//如果是实际充值，增加客户的可开票金额（累计金额）
				if("实际充值".equals(originEcmcRemark)){
				    invoiceService.incrBillableTotalAmount(recordBean.getCusId(), recordBean.getExchangeMoney());
				}
			}else if(null != accountMoney && "2".equals(recordBean.getIncomeType())){ //扣费发送短信消息
				messageCenterService.ecmcDeductionFund(recordBean.getCusId(), recordBean.getExchangeMoney(), accountMoney);
			}
			if(null != accountMoney){
				reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				ecmcLogService.addLog("调整账户金额",ConstantClazz.LOG_TYPE_CUSTOMER, customer.getCusName(),null, 1,recordBean.getCusId(),null);
			}
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			ecmcLogService.addLog("调整账户金额",ConstantClazz.LOG_TYPE_CUSTOMER, customer.getCusName(),null, 0,recordBean.getCusId(),e);
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
		}
		
		return reJson;
	}
	
	/**
	 * 过期资源列表
	 * @return 资源记录分页列表
	 * @throws Exception
	 */
	@RequestMapping(value = "/getexpireresourcelist")
	@ResponseBody
	public Object getExpireResourceList(@RequestBody ParamsMap map) throws Exception {
		String cusId = MapUtils.getString(map.getParams(), "cusId");
		int pageSize = map.getPageSize();
        pageSize = 5;
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
		if (!StringUtil.isEmpty(cusId)) {
			return ecmcCustomerService.getExpireResourceList(queryMap, cusId);
		}
		return null;
	}
	
	/**
	 * 获取工单关闭但未创建的客户数
	 * @param requestMap  请求参数
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getuncreatedcusnum")
	@ResponseBody
	public Object getUncreatedCusNum(@RequestBody Map<String, Object> requestMap) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.ERROR_CODE);
		try {
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ecmcCustomerService.getUncreatedCusNum());
		} catch (Exception e) {
			throw e;
		}
		return reJson;
	}

	
	/**
	 * 获取工单关闭但未创建的客户列表
	 * @param paramsMap 分页参数
	 * @return 未创建客户分页列表
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
    @RequestMapping(value = "/getuncreatedcuslist")
	@ResponseBody
	public Object getNotCreatedCusList(@RequestBody ParamsMap paramsMap) throws Exception {
		QueryMap queryMap = new QueryMap();
		Map<String, Object> params = paramsMap.getParams();
		queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
		if (paramsMap.getPageSize() != null) {
			queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
		}
		return ecmcCustomerService.getUncreatedCusList(queryMap);
	}
	
	/**
	 * 修改信用额度
	 * @param requestMap  请求参数
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/modifycreditlines")
	@ResponseBody
	public Object modifyCreditLines(@RequestBody Map<String, Object> requestMap) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.ERROR_CODE);

		String cusId = MapUtils.getString(requestMap, "cusId");
		String creditLinesStr = MapUtils.getString(requestMap, "creditLines");
		creditLinesStr = StringUtil.isEmpty(creditLinesStr) ? "0.00" : creditLinesStr;
		BigDecimal creditLines = new BigDecimal(creditLinesStr);
		try {
			BaseCustomer customer = ecmcCustomerService.updateCreditLines(cusId, creditLines);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("设置信用额度", ConstantClazz.LOG_TYPE_CUSTOMER, customer.getCusOrg(), null, 1,
					customer.getCusId(), null);
		} catch (Exception e) {
			ecmcLogService.addLog("设置信用额度", ConstantClazz.LOG_TYPE_CUSTOMER, null, null, 0, cusId, e);
			throw e;
		}
		return reJson;
	}
	
	/**
	 * 客户费用报表
	 * @param request request请求对象
	 * @param page 分页对象
	 * @param map 分页参数
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getcusreport", method = RequestMethod.POST)
	public Object getCusReport(HttpServletRequest request, Page page, @RequestBody ParamsMap map) throws Exception {
		Map<String, Object> params = map.getParams();
		String cusId = MapUtils.getString(params, "cusId");
		String type = MapUtils.getString(params, "type");
		String searchType = MapUtils.getString(params, "searchType");
		String begin = MapUtils.getString(params, "beginTime");
		String end = MapUtils.getString(params, "endTime");
		String monMonth = MapUtils.getString(params, "monMonth");
		String productName = MapUtils.getString(params, "productName");
		String resourceName = MapUtils.getString(params, "resourceName");
		Date beginTime = begin != null && begin.length() > 0 ? DateUtil.timestampToDate(begin) : null;
		Date endTime = end != null && end.length() > 0 ? DateUtil.timestampToDate(end) : null;
		int pageSize = map.getPageSize();
		int pageNumber = map.getPageNumber();
		QueryMap queryMap = new QueryMap();
		queryMap.setPageNum(pageNumber);
		queryMap.setCURRENT_ROWS_SIZE(pageSize);
		page = costReportService.getReportListPage(page, searchType, monMonth, beginTime, endTime, type, productName,
				resourceName, cusId, queryMap);
		return JSONObject.toJSONString(page);
	}
	
	/**
	 * 费用报表获取总消费
	 * @param request
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/gettotalcost", method = RequestMethod.POST)
	public String getTotalCost(HttpServletRequest request, @RequestBody Map<String, Object> params) throws Exception {
		String cusId = MapUtils.getString(params, "cusId");
		String type = MapUtils.getString(params, "type");
		String searchType = MapUtils.getString(params, "searchType");
		String begin = MapUtils.getString(params, "beginTime");
		String end = MapUtils.getString(params, "endTime");
		String monMonth = MapUtils.getString(params, "monMonth");
		String productName = MapUtils.getString(params, "productName");
		String resourceName = MapUtils.getString(params, "resourceName");
		Date beginTime = begin != null && begin.length() > 0 ? DateUtil.timestampToDate(begin) : null;
		Date endTime = end != null && end.length() > 0 ? DateUtil.timestampToDate(end) : null;
		String tatolCost = costReportService.getTotalCost(searchType, monMonth, beginTime, endTime, type, productName,
				resourceName, cusId);
		costReportService.changePrepaymentState("orderno");
		JSONObject json = new JSONObject();
		json.put("totalCost", tatolCost);
		return JSONObject.toJSONString(json);
	}
	
	/**
	 * 导出后付费订单Excel
	 * @param request
	 * @param response
	 * @param type
	 * @param searchType
	 * @param beginTime
	 * @param endTime
	 * @param monMonth
	 * @param productName
	 * @param resourceName
	 * @param browser
	 * @throws Exception
	 */
	@RequestMapping("/createpostpaidexcel")
	public void createPostPaidExcel(HttpServletRequest request, HttpServletResponse response, String type,
			String searchType, String beginTime, String endTime, String monMonth, String productName,
			String resourceName, String browser, String cusId) throws Exception {

		Date begin = beginTime == null ? null : DateUtil.timestampToDate(beginTime);
		Date end = endTime == null ? null : DateUtil.timestampToDate(endTime);
		Properties props = System.getProperties();
		String os = props.getProperty("os.name").toLowerCase();
		if (os.indexOf("windows") != -1) {
			productName = productName == null ? null : new String(productName.getBytes("ISO-8859-1"), "UTF-8");
			resourceName = resourceName == null ? null : new String(resourceName.getBytes("ISO-8859-1"), "UTF-8");
		} else {
			productName = productName == null ? null : new String(productName.getBytes("ISO-8859-1"), "UTF-8");
			resourceName = resourceName == null ? null : new String(resourceName.getBytes("ISO-8859-1"), "UTF-8");
		}
		String fileName = "";
		String name = "后付费资源费用报表_" + simpleDateFormat.format(new Date()) + ".xls";
		if ("Firefox".equals(browser)) {
			fileName = new String(name.getBytes(), "iso-8859-1");
		} else {
			fileName = URLEncoder.encode(name, "UTF-8");
		}
		response.setContentType("application/vnd.ms-excel");
		response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
		try {
			costReportService.exportPostPayExcel(response.getOutputStream(), type, searchType, begin, end, monMonth,
					productName, resourceName, cusId);
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 导出预付费订单Excel
	 * @param request
	 * @param response
	 * @param type
	 * @param beginTime
	 * @param endTime
	 * @param productName
	 * @param browser
	 * @param cusId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/createprepaidexcel")
	public String createPrepaymentExcel(HttpServletRequest request, HttpServletResponse response, String type,
			String beginTime, String endTime, String productName, String browser, String cusId) throws Exception {
		Date begin = beginTime == null ? null : DateUtil.timestampToDate(beginTime);
		Date end = endTime == null ? null : DateUtil.timestampToDate(endTime);

		Properties props = System.getProperties();
		String os = props.getProperty("os.name").toLowerCase();
		if (os.indexOf("windows") != -1) {
			productName = productName == null ? null : new String(productName.getBytes("ISO-8859-1"), "UTF-8");
		} else {
			productName = productName == null ? null : new String(productName.getBytes("ISO-8859-1"), "UTF-8");
		}
		String fileName = "";
		if ("Firefox".equals(browser)) {
			fileName = new String("易云公有云预付费资源费用报表.xls".getBytes(), "iso-8859-1");
		} else {
			fileName = URLEncoder.encode("易云公有云预付费资源费用报表.xls", "UTF-8");
		}
		response.setContentType("application/vnd.ms-excel");
		response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
		try {
			costReportService.exportPrepaymentExcel(response.getOutputStream(), type, begin, end, productName, cusId);
		} catch (Exception e) {
			throw e;
		}
		return null;
	}
	
    /**
     * 后付费报表详情
     * @param request
     * @param map
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    @ResponseBody
	@RequestMapping(value="/getpostpaydetail" , method = RequestMethod.POST)
	public String getPostpayDetail(HttpServletRequest request,@RequestBody Map map) throws Exception{
		String id=map.get("id").toString();
		MoneyRecord moneyRecord=costReportService.getPostpayDetail(id);
		return JSONObject.toJSONString(moneyRecord);
	}
    
    /**
     * 获取客户交易记录
     * @param request
     * @param page
     * @param map
     * @return
     * @throws Exception
     */
    @ResponseBody
	@RequestMapping("/getcusrecords")
	public String getCusRecords(HttpServletRequest request,Page page, @RequestBody ParamsMap map) throws Exception{
    	Map<String, Object> params = map.getParams();
    	String cusId=MapUtils.getString(params, "cusId");
		String begin=MapUtils.getString(params, "beginTime");
		String end=MapUtils.getString(params, "endTime");
		Date beginTime = begin!=null&&begin.length()>0?DateUtil.timestampToDate(begin):null;
	    Date endTime = end!=null&&end.length()>0?DateUtil.timestampToDate(end):null;
	    String incomeType=MapUtils.getString(params, "incomeType");
	    int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
		QueryMap queryMap=new QueryMap();
		queryMap.setPageNum(pageNumber);
		queryMap.setCURRENT_ROWS_SIZE(pageSize);
		try {
			page=accountOverviewService.getRecordPage(page, cusId, beginTime, endTime,incomeType, queryMap);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
	
		return JSONObject.toJSONString(page);
	}
	/**
     * 导出excel
     */
    @SuppressWarnings("unused")
    @RequestMapping("/createrecordexcel")
    public String createRecordExcel(HttpServletRequest request, HttpServletResponse response , String beginTime, 
                              String endTime ,String incomeType , String browser, String cusId) throws Exception {

        Date begin = beginTime==null?null:DateUtil.timestampToDate(beginTime);
        Date end = endTime==null?null:DateUtil.timestampToDate(endTime);
        Properties props=System.getProperties();
        String os = props.getProperty("os.name").toLowerCase();
        try {
            List<ExcelRecord> list = accountOverviewService.queryRecordExcel(incomeType,begin, end,cusId, true);
            ExportDataToExcel<ExcelRecord> excel = new ExportDataToExcel<ExcelRecord>();
            response.setContentType("application/vnd.ms-excel");
            
            String fileName = "";
            SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMdd");
            String now=sdf.format(new Date());
            String name="交易记录_"+now+".xls";
            if("Firefox".equals(browser)){
                fileName = new String(name.getBytes(), "iso-8859-1");
            }else{
                fileName = URLEncoder.encode(name, "UTF-8") ;
            }
            response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
            excel.exportData(list, response.getOutputStream(), "易云公有云账户交易记录");
        } catch (Exception e) {
            throw e;
        }
        return null;
    }
    /**
     * 同步mysq客户状态到redis
     * @param request
     * @param map
     * @return
     * @throws Exception
     */
	@ResponseBody
	@RequestMapping(value = "/synccustomer", method = RequestMethod.POST)
	public Object syncCustomer() throws AppException{
		log.info("*************同步客户冻结状态开始*************");
		EayunResponseJson reJson = new EayunResponseJson();
		if(ecmcCustomerService.syncCustomerBlockStatus()){
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("同步客户冻结", "客户状态", "同步客户冻结", null, 1, null, null);
		}else{
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("同步客户冻结", "客户状态", "同步客户冻结", null, 0, null, null);
		}
		log.info("*************同步客户冻结状态结束*************");
		return reJson;
	}
	
	
	/**
	 * 模糊查询客户关键字的不在黑名单的客户
	 * @param request request请求对象
	 * @param map 参数
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getCustExceptBlackCus", method = RequestMethod.POST)
	public Object getCustExceptBlackCus(HttpServletRequest request, @RequestBody Map map) throws Exception {
		String cusOrg = MapUtils.getString(map, "cusOrg");
		List<Customer> cusList = ecmcCustomerService.getCustExceptBlackCus(cusOrg);
		
		System.out.println(JSONObject.toJSONString(cusList));
		return JSONObject.toJSONString(cusList);
	}
	
	/**
	 * 获取api类别
	 * @param request request请求对象
	 * @param map 参数
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getapitype", method = RequestMethod.POST)
	public Object getApiType(HttpServletRequest request) {
		EayunResponseJson json=new EayunResponseJson();
		try {
			List<ApiCountRestrict> list=apiRestrictService.getApiType();
			json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			json.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return JSONObject.toJSONString(json);
	}
	
	/**
	 * 获取请求次数受限信息
	 * @param request request请求对象
	 * @param map 参数
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/getrestrictrequestcount", method = RequestMethod.POST)
	public Object getRestrictRequestCount(HttpServletRequest request,@RequestBody Map map) throws Exception{
		String cusId = MapUtils.getString(map, "cusId");
		String version = MapUtils.getString(map, "version");
		String apiType = MapUtils.getString(map, "apiType");
		List<ApiCountRestrict> list=apiRestrictService.getRestrictRequestCount(cusId, version, apiType);
		return JSONObject.toJSONString(list);
	}
	
	/**
	 * 修改请求次数受限信息
	 * @param request request请求对象
	 * @param map 参数
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/updateapirequestcount", method = RequestMethod.POST)
	public Object updateApiRequestCount(HttpServletRequest request,@RequestBody Map map){
		String cusId=MapUtils.getString(map, "cusId");
		String cusOrg=MapUtils.getString(map, "cusOrg");
		List<Map<String, Object>> list=(List<Map<String, Object>>) map.get("actionsList");
		EayunResponseJson json=new EayunResponseJson();
		try {
			apiRestrictService.updateRestrictRequestCount(list);
			ecmcLogService.addLog("编辑客户API访问限制", ConstantClazz.LOG_TYPE_APIRESTRICT, cusOrg, null, 1, cusId, null);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			ecmcLogService.addLog("编辑客户API访问限制", ConstantClazz.LOG_TYPE_APIRESTRICT, cusOrg, null, 0, cusId, e);
			log.error(e.getMessage(),e);
			json.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return JSONObject.toJSONString(json);
	}
	
}
