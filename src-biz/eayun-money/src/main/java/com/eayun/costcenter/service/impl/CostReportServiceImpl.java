package com.eayun.costcenter.service.impl;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.ScriptStyle;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.costcenter.bean.ConfigureBean;
import com.eayun.costcenter.dao.MoneyRecordDao;
import com.eayun.costcenter.model.BaseMoneyRecord;
import com.eayun.costcenter.model.MoneyRecord;
import com.eayun.costcenter.service.CostReportService;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderService;
/**
 * 费用报表实现类
 * @author xiangyu.cao@eayun.com
 *
 */
@Service
@Transactional
public class CostReportServiceImpl implements CostReportService {
	private static final Logger log = LoggerFactory
			.getLogger(CostReportServiceImpl.class);
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat formatExcelDate = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");
	@Autowired
	private MoneyRecordDao moneyRecordDao;
	@Autowired
	private DataCenterService dataCenterService;
	@Autowired
	private OrderService orderService;

	@Override
	public Page getReportListPage(Page page, String searchType,
			String monMonth, Date beginTime, Date endTime, String type,
			String productName, String resourceName, String cusId,
			QueryMap queryMap) throws Exception {
		if ("1".equals(searchType) && monMonth != null && monMonth.length() > 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			monMonth = sdf.format(DateUtil.timestampToDate(monMonth));
		}
		page = getReportListPage(page, type, searchType, monMonth, beginTime,
				endTime, productName, resourceName, cusId, queryMap);
		return page;
	}

	private Page getReportListPage(Page page, String type, String searchType,
			String monMonth, Date beginTime, Date endTime, String productName,
			String resourceName, String cusId, QueryMap queryMap)
			throws Exception {
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = genSql(type, searchType, monMonth, beginTime,
				endTime, productName, resourceName, cusId, list);
		page=moneyRecordDao.pagedQuery(sql.toString(), queryMap, list.toArray());
		List result = (List) page.getResult();
		for (int i = 0; i < result.size(); i++) {
			Object[] objs = (Object[]) result.get(i);
			MoneyRecord moneyRecord = new MoneyRecord();
			moneyRecord.setMonId(String.valueOf(objs[0]));
			moneyRecord.setMonTime((Date)objs[1]);
			moneyRecord.setIncomeType(String.valueOf(objs[2]));
			moneyRecord.setMonPaymonth(String.valueOf(objs[3]));
			moneyRecord.setProductName(String.valueOf(objs[4]));
			moneyRecord.setResourceId(String.valueOf(objs[5]));
			moneyRecord.setResourceName(String.valueOf(objs[6]));
			moneyRecord.setPayType(String.valueOf(objs[7]));
			moneyRecord.setMoney(new BigDecimal(String.valueOf(objs[8])));
			if ("1".equals(type)) {
				moneyRecord.setMoneyStr(formatTwo(String.valueOf(objs[8])));
			} else {
				moneyRecord.setMoneyStr(formatThree(String.valueOf(objs[8])));
			}
			moneyRecord.setPayState(String.valueOf(objs[9]));
			moneyRecord.setMonRealPay(new BigDecimal(String.valueOf(objs[10])));
			if ("1".equals(type)) {
				moneyRecord
						.setMonRealPayStr(formatTwo(String.valueOf(objs[10])));
			} else {
				moneyRecord.setMonRealPayStr(formatThree(String
						.valueOf(objs[10])));
			}
			if ("1".equals(type)) {
				moneyRecord.setMonArrearsMoney(formatTwo(moneyRecord.getMoney()
						.subtract(moneyRecord.getMonRealPay()).toString()));
			} else {
				moneyRecord.setMonArrearsMoney(moneyRecord
						.getMoney().subtract(moneyRecord.getMonRealPay())
						.toString());
			}

			moneyRecord.setOrderNo(String.valueOf(objs[11]));
			moneyRecord.setMonStart(objs[12] == null ? null : (Date)objs[12]);
			moneyRecord.setMonEnd(objs[13] == null ? null : (Date)objs[13]);
			moneyRecord.setMonConfigure(String.valueOf(objs[14]));
			moneyRecord.setResourceType(String.valueOf(objs[15]));
			if(PayType.PAYBEFORE.equals(type)){
				moneyRecord.setPrepaymentMoney(objs[16] == null ? null :new BigDecimal(formatTwo(String.valueOf(objs[16]))));
			}
			getResourceTypeName(moneyRecord);
			result.set(i, moneyRecord);
		}
		return page;
	}


	private StringBuffer genSql(String type,String searchType,
			String monMonth, Date beginTime, Date endTime, String productName,
			String resourceName, String cusId, List<Object> list) {
		StringBuffer sql = new StringBuffer();
		if(PayType.PAYBEFORE.equals(type)){
			sql.append("select mon.monId,mon.monTime,mon.incomeType,monPaymonth,mon.productName,mon.resourceId,mon.resourceName,mon.payType,mon.money,mon.payState,mon.monRealPay,mon.orderNo ,mon.monStart,mon.monEnd,mon.monConfigure,mon.resourceType,(select sum(money) from BaseMoneyRecord where payType='1' and orderNo=mon.orderNo) ");
		}else{
			sql.append("select mon.monId,mon.monTime,mon.incomeType,monPaymonth,mon.productName,mon.resourceId,mon.resourceName,mon.payType,mon.money,mon.payState,mon.monRealPay,mon.orderNo ,mon.monStart,mon.monEnd,mon.monConfigure,mon.resourceType"); // 定义一个基础查询语句
		}
		sql.append(" from BaseMoneyRecord mon ");
		sql.append("where mon.cusId = ? ");
		list.add(cusId);
		sql.append(" and mon.payType = ? ");
		list.add(type);
		if (null != beginTime&&("2".equals(searchType) ||PayType.PAYBEFORE.equals(type))) { // 验证开始时间格式和是否为空
			sql.append("and mon.monTime >= ? "); // 拼接查询语句
			list.add(beginTime);
		}
		if ( null != endTime&&("2".equals(searchType) ||PayType.PAYBEFORE.equals(type))) {
			sql.append("and mon.monTime < ? ");
			list.add(endTime);
		}
		if ("1".equals(searchType) && null != monMonth) {
			sql.append("and mon.monPaymonth = ? ");
			list.add(monMonth);
		}
		if (null != productName && productName.length() > 0) {
			sql.append(" and mon.productName like ? ");
			list.add("%" + productName + "%");
		}
		if (null != resourceName && resourceName.length() > 0) {
			sql.append(" and mon.resourceName like ? ");
			list.add("%" + resourceName + "%");
		}
		if(PayType.PAYBEFORE.equals(type)){
			sql.append(" and mon.isSuccess=? ");
			list.add("1");
			sql.append(" group by mon.orderNo ");
		}
		sql.append("order by mon.serialNumber desc");
		return sql;
	}

	private String formatTwo(String num) {
		if(num.indexOf(".")==-1){
			num=num+".000";
		}
		num=num.substring(0,num.indexOf(".")+3);
		return num;
	}

	private String formatThree(String num) {
		BigDecimal number = new BigDecimal(num);
		number = number.compareTo(BigDecimal.ZERO) == -1 ? number
				.multiply(new BigDecimal(-1)) : number;
		number = number.setScale(2, BigDecimal.ROUND_HALF_UP);
		DecimalFormat df = new DecimalFormat("0.000");
		String result = df.format(number.doubleValue());
		return result;
	}

	@Override
	public String getTotalCost(String searchType, String monMonth,
			Date beginTime, Date endTime, String type, String productName,
			String resourceName, String cusId) throws Exception {
		if ("1".equals(searchType) && monMonth != null && monMonth.length() > 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			monMonth = sdf.format(DateUtil.timestampToDate(monMonth));
		}
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = genTotalCostSql(type, searchType, monMonth, beginTime,
				endTime, productName, resourceName, cusId, list);
		List moneyRecordList = moneyRecordDao.find(
				sql.toString(), list.toArray());
		BigDecimal totalCost = new BigDecimal("0.000");
		for (Object obj: moneyRecordList) {
			if(null!=obj){
				BigDecimal bigdecimal=(BigDecimal)obj;
				totalCost=totalCost.add(bigdecimal);
			}
		}
		return PayType.PAYBEFORE.equals(type) ? formatTwo(totalCost.toString())
				: totalCost.toString();
	}

	private StringBuffer genTotalCostSql(String type, String searchType,
			String monMonth, Date beginTime, Date endTime, String productName,
			String resourceName, String cusId, List<Object> list) {
		StringBuffer sql=new StringBuffer();
		if(PayType.PAYBEFORE.equals(type)){
			sql.append(" select (select sum(money) from BaseMoneyRecord where payType='1' and orderNo=mon.orderNo)");
		}else{
			sql.append(" select sum(money) ");
		}
		sql.append(" from BaseMoneyRecord mon ");
		sql.append("where mon.cusId = ? ");
		list.add(cusId);
		sql.append(" and mon.payType = ? ");
		list.add(type);
		if ("2".equals(searchType) && null != beginTime || "1".equals(type)) { // 验证开始时间格式和是否为空
			sql.append("and mon.monTime >= ? "); // 拼接查询语句
			list.add(beginTime);
		}
		if ("2".equals(searchType) && null != endTime || "1".equals(type)) {
			sql.append("and mon.monTime < ? ");
			list.add(endTime);
		}
		if ("1".equals(searchType) && null != monMonth) {
			sql.append("and mon.monPaymonth = ? ");
			list.add(monMonth);
		}
		if (null != productName && productName.length() > 0) {
			sql.append(" and mon.productName like ? ");
			list.add("%" + productName + "%");
		}
		if (null != resourceName && resourceName.length() > 0) {
			sql.append(" and mon.resourceName like ? ");
			list.add("%" + resourceName + "%");
		}
		if(PayType.PAYBEFORE.equals(type)){
			sql.append(" and mon.isSuccess=? ");
			list.add("1");
			sql.append(" group by mon.orderNo ");
		}
		sql.append("order by mon.serialNumber desc");
		return sql;
	}

	@Override
	public List<MoneyRecord> queryPostPay(String type, String searchType,
			Date beginTime, Date endTime, String monMonth, String productName,
			String resourceName, String cusId) throws Exception{
		if ("1".equals(searchType) && monMonth != null && monMonth.length() > 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			monMonth = sdf.format(DateUtil.timestampToDate(monMonth));
		}
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = genSql(type, searchType, monMonth, beginTime,
				endTime, productName, resourceName, cusId, list);
		List result = moneyRecordDao.find(
				sql.toString(), list.toArray());
		List<MoneyRecord> monRecordList = new ArrayList<MoneyRecord>();
		for (int i = 0; i < result.size(); i++) {
			Object[] objs = (Object[]) result.get(i);
			MoneyRecord moneyRecord = new MoneyRecord();
			moneyRecord.setMonId(String.valueOf(objs[0]));
			moneyRecord.setMonTime(simpleDateFormat.parse(String
					.valueOf(objs[1])));
			moneyRecord.setIncomeType(String.valueOf(objs[2]));
			moneyRecord.setMonPaymonth(String.valueOf(objs[3]));
			moneyRecord.setProductName(String.valueOf(objs[4]));
			moneyRecord.setResourceId(String.valueOf(objs[5]));
			moneyRecord.setResourceName(String.valueOf(objs[6]));
			moneyRecord.setPayType(String.valueOf(objs[7]));
			moneyRecord.setMoney(new BigDecimal(String.valueOf(objs[8])));
			if ("1".equals(type)) {
				moneyRecord.setMoneyStr(formatTwo(String.valueOf(objs[8])));
			} else {
				moneyRecord.setMoneyStr(String.valueOf(objs[8]));
			}
			moneyRecord.setPayState(String.valueOf(objs[9]));
			moneyRecord.setMonRealPay(new BigDecimal(String.valueOf(objs[10])));
			if ("1".equals(type)) {
				moneyRecord
						.setMonRealPayStr(formatTwo(String.valueOf(objs[10])));
			} else {
				moneyRecord.setMonRealPayStr(String
						.valueOf(objs[10]));
			}
			if ("1".equals(type)) {
				moneyRecord.setMonArrearsMoney(formatTwo(moneyRecord.getMoney()
						.subtract(moneyRecord.getMonRealPay()).toString()));
			} else {
				moneyRecord.setMonArrearsMoney(moneyRecord
						.getMoney().subtract(moneyRecord.getMonRealPay())
						.toString());
			}

			moneyRecord.setOrderNo(String.valueOf(objs[11]));
			moneyRecord.setMonStart(objs[12] == null ? null : simpleDateFormat
					.parse(String.valueOf(objs[12])));
			moneyRecord.setMonEnd(objs[13] == null ? null : simpleDateFormat
					.parse(String.valueOf(objs[13])));
			moneyRecord.setMonConfigure(String.valueOf(objs[14]));
			moneyRecord.setResourceType(String.valueOf(objs[15]));
			if(PayType.PAYBEFORE.equals(type)){
				moneyRecord.setPrepaymentMoney(objs[16] == null ? null :new BigDecimal(String.valueOf(objs[16])));
			}
			getResourceTypeName(moneyRecord);
			monRecordList.add(i, moneyRecord);
		}
		return monRecordList;
	}

	@Override
	public MoneyRecord getPostpayDetail(String id) {
		BaseMoneyRecord baseMoneyRecord = moneyRecordDao.findOne(id);
		MoneyRecord moneyRecord = new MoneyRecord();
		BeanUtils.copyPropertiesByModel(moneyRecord, baseMoneyRecord);
		moneyRecord.setMoneyStr(formatThree(baseMoneyRecord.getMoney()
				.toString()));
		moneyRecord.setAccountBalanceStr(formatThree(baseMoneyRecord
				.getAccountBalance().toString()));
		moneyRecord.setMonRealPayStr(formatThree(baseMoneyRecord
				.getMonRealPay().toString()));
		moneyRecord.setMonArrearsMoney(formatThree(baseMoneyRecord.getMoney()
				.subtract(baseMoneyRecord.getMonRealPay()).toString()));
		JSONArray array = JSONArray.parseArray(baseMoneyRecord
				.getMonConfigure());
		List<ConfigureBean> list = new ArrayList<ConfigureBean>();
		for (int i = 0; i < array.size(); i++) {
			JSONObject json = (JSONObject) array.get(i);
			ConfigureBean configureBean = new ConfigureBean();
			configureBean.setName(json.getString("name"));
			configureBean.setPrice(json.getString("price"));
			configureBean.setUnits(json.getString("units"));
			list.add(configureBean);
		}
		moneyRecord.setConfigList(list);
		getResourceTypeName(moneyRecord);
		if (baseMoneyRecord.getDcId() != null
				&& baseMoneyRecord.getDcId().length() > 0) {
			BaseDcDataCenter baseDcDataCenter = dataCenterService
					.getById(baseMoneyRecord.getDcId());
			moneyRecord.setDcName(baseDcDataCenter.getName());
		}
		return moneyRecord;
	}

	private void getResourceTypeName(MoneyRecord moneyRecord) {
		if (ResourceType.VM.equals(moneyRecord.getResourceType())) {
			moneyRecord.setResourceTypeStr("云主机");
		} else if (ResourceType.VDISK.equals(moneyRecord.getResourceType())) {
			moneyRecord.setResourceTypeStr("云硬盘");
		} else if (ResourceType.DISKSNAPSHOT.equals(moneyRecord
				.getResourceType())) {
			moneyRecord.setResourceTypeStr("云硬盘备份");
		} else if (ResourceType.NETWORK.equals(moneyRecord.getResourceType())) {
			moneyRecord.setResourceTypeStr("私有网络");
		} else if (ResourceType.QUOTAPOOL.equals(moneyRecord.getResourceType())) {
			moneyRecord.setResourceTypeStr("负载均衡器");
		} else if (ResourceType.OBS.equals(moneyRecord.getResourceType())) {
			moneyRecord.setResourceTypeStr("对象存储");
		} else if (ResourceType.VPN.equals(moneyRecord.getResourceType())) {
			moneyRecord.setResourceTypeStr("VPN");
		} else if (ResourceType.FLOATIP.equals(moneyRecord.getResourceType())) {
			moneyRecord.setResourceTypeStr("公网IP");
		}else if (ResourceType.RDS.equals(moneyRecord.getResourceType())){
			moneyRecord.setResourceTypeStr("实例");
		}
	}

	@Override
	public void exportPostPayExcel(OutputStream os, String type, String searchType,
			Date beginTime, Date endTime, String monMonth, String productName,
			String resourceName, String cusId) throws Exception {

		List<MoneyRecord> list = queryPostPay(type, searchType, beginTime,
				endTime, monMonth, productName, resourceName, cusId);
		try {
			WritableWorkbook workbook = Workbook.createWorkbook(os);
			String sheet = "易云公有云后付费资源费用报表";
			String[] netTitle = { "计费时间", "账期", "产品名称", "资源id/名称", "付款方式",
					"应付金额", "支付状态" };
			WritableSheet ws = workbook.createSheet(sheet, 0);
			Label label = new Label(0, 0, sheet, setTitleStyle(20, "1"));
			ws.addCell(label); // 标题(如汇总、云主机、网络等)
			CellView cellView = new CellView();
			cellView.setAutosize(true); // 宽度自适应
			ws.mergeCells(0, 0, 13, 0);
			// ws.setRowView(0, 400); //第一行高度
			if (null != list) {
				int row = 0;
				StringBuffer netHeads = new StringBuffer();
				netHeads.append(formatExcelDate.format(beginTime));
				netHeads.append("至");
				netHeads.append(formatExcelDate.format(DateUtil.addDay(endTime, new int[]{0,0,0,0,0,-1})));
				netHeads.append("后付费资源消费总计：￥");
				netHeads.append(getTotalCost(searchType, monMonth, beginTime,
						endTime, type, productName, resourceName, cusId));
				Label typeheader = new Label(0, row + 1, netHeads.toString(),
						setStyle(16, false, "1"));
				ws.addCell(typeheader);
				ws.mergeCells(0, row + 1, 13, 0);
				ws.setRowView(row + 1, 500);
				for (int j = 0; j < netTitle.length; j++) { // 每种类型内列表表头
					Label header = new Label(2 * j, row + 2, netTitle[j],
							setStyle(11, true, "2"));

					// ws.setColumnView(j, cellView);//根据内容自动设置列宽
					ws.mergeCells(2 * j, row + 2, 2 * (j + 1) - 1, row + 2);
					ws.setColumnView(j, 20); // 设置表头列的宽度
					ws.setRowView(row + 2, 550);
					ws.addCell(header);
				}
				for (int j = 0; j < list.size(); j++) {// 每种类型内数据列表 每一行
					MoneyRecord details = list.get(j);
					for (int o = 0; o < netTitle.length; o++) { // 每一列
						String cString = "";
						switch (o) {
						case 0:
							cString = formatExcelDate.format(details
									.getMonTime());
							break;
						case 1:
							cString = details.getMonPaymonth();
							break;
						case 2:
							cString = details.getProductName();
							break;
						case 3:
							if (ResourceType.OBS.equals(details
									.getResourceType())) {
								cString = "对象存储服务";
							} else {
								cString = details.getResourceId() + "\n"
										+ details.getResourceName();
							}
							break;
						case 4:
							cString = "1".equals(details.getPayType()) ? "预付费"
									: "后付费";
							break;
						case 5:
							cString = "￥"
									+ details.getMoney().toString();
							break;
						case 6:
							String payState = details.getPayState();
							if ("1".equals(payState)) {
								payState = "已支付";
							} else if ("2".equals(payState)) {
								String arrears = details.getMoney()
										.subtract(details.getMonRealPay())
										.toString();
								payState = "已欠费" + "\n" + "￥" + arrears;
							}
							cString = payState;
							break;
						}
						Label content = new Label(2 * o, row + j + 3, cString,
								setStyle(12, false, "3"));
						ws.mergeCells(2 * o, row + j + 3, 2 * (o + 1) - 1, row
								+ j + 3);
						ws.setColumnView(o, 20);
						ws.setColumnView(6, 50);
//						ws.setColumnView(3, cellView);
						ws.setRowView(j+3,600);
						ws.addCell(content);
					}
				}
				Label diff = new Label(0, row + list.size() + 4, "",
						setTitleStyle(18, "2"));
				ws.addCell(diff);
				ws.setRowView(row + list.size() + 4, 200);
				row = row + list.size() + 3;
			}
			workbook.write();
			workbook.close();
		} catch (Exception ex) {
			log.error("导出excel失败", ex);
			throw ex;
		}
	}

	/**
	 * 第一行标题样式 汇总（云主机、云硬盘） 云主机 云硬盘
	 * 
	 * @return
	 * @throws Exception
	 */
	private WritableCellFormat setTitleStyle(int size, String level)
			throws Exception {
		WritableFont he = new WritableFont(WritableFont.createFont("宋体"),// 字体
				size, // 字号
				WritableFont.BOLD, // 粗体
				false, // 斜体
				UnderlineStyle.NO_UNDERLINE, // 下划线
				Colour.BLACK, // 字体颜色
				ScriptStyle.NORMAL_SCRIPT);
		WritableCellFormat wcf = new WritableCellFormat(he);
		if (level.equals("1")) {
			wcf.setAlignment(Alignment.CENTRE); // 设置对齐方式(水平居中)
			wcf.setVerticalAlignment(VerticalAlignment.CENTRE);// 垂直居中
		} else if (level.equals("2")) {
			wcf.setAlignment(Alignment.LEFT); // 设置对齐方式
		}
		wcf.setBorder(Border.NONE, BorderLineStyle.NONE);// 边框
		// wcf.setBackground(Colour.GRAY_25);//设置背景颜色
		return wcf;
	}

	/**
	 * 行样式
	 * 
	 * @param fontSize
	 * @param bold
	 * @param level
	 * @return
	 * @throws Exception
	 */
	private WritableCellFormat setStyle(int fontSize, boolean bold, String level)
			throws Exception {
		WritableFont he = new WritableFont(WritableFont.createFont("宋体"),// 字体
				fontSize, // 字号
				WritableFont.NO_BOLD, // 粗体
				false, // 斜体
				UnderlineStyle.NO_UNDERLINE, // 下划线
				Colour.BLACK, // 字体颜色
				ScriptStyle.NORMAL_SCRIPT);

		if (bold) {
			he.setBoldStyle(WritableFont.BOLD);// 粗体
		}
		WritableCellFormat wcf = new WritableCellFormat(he);

		if (level.equals("1")) {
			wcf.setAlignment(Alignment.LEFT); // 设置对齐方式
			wcf.setBorder(Border.NONE, BorderLineStyle.THIN);// 边框
			// wcf.setBackground(Colour.GREY_25_PERCENT); //设置背景颜色
		} else if (level.equals("2")) {
			// 竖直方向居中对齐
			wcf.setVerticalAlignment(VerticalAlignment.CENTRE);// 垂直居中
			wcf.setAlignment(Alignment.CENTRE); // 设置对齐方式（水平居中）
			wcf.setBorder(Border.BOTTOM, BorderLineStyle.MEDIUM, Colour.BLUE2);
		} else if (level.equals("3")) {
			wcf.setAlignment(Alignment.CENTRE); // 设置对齐方式
			wcf.setBorder(Border.ALL, BorderLineStyle.THIN);
			wcf.setVerticalAlignment(VerticalAlignment.CENTRE);// 垂直居中
		}
		wcf.setWrap(true);
		return wcf;
	}

	@Override
	public void exportPrepaymentExcel(OutputStream os, String type,
			Date begin, Date end, String productName, String cusId)
			throws Exception {
		List<MoneyRecord> list = queryPostPay(type, null,begin,
				end, null,productName,null, cusId);
		try {
			WritableWorkbook workbook = Workbook.createWorkbook(os);
			String sheet = "易云公有云预付费资源费用报表";
			String[] netTitle = { "计费时间", "产品名称", "订单号", "付款方式",
					"应付金额" };
			WritableSheet ws = workbook.createSheet(sheet, 0);
			Label label = new Label(0, 0, sheet, setTitleStyle(20, "1"));
			ws.addCell(label); // 标题(如汇总、云主机、网络等)
			CellView cellView = new CellView();
			cellView.setAutosize(true); // 宽度自适应
			ws.mergeCells(0, 0, 9, 0);
			// ws.setRowView(0, 400); //第一行高度
			if (null != list) {
				int row = 0;
				StringBuffer netHeads = new StringBuffer();
				netHeads.append(formatExcelDate.format(begin));
				netHeads.append("至");
				netHeads.append(formatExcelDate.format(DateUtil.addDay(end, new int[]{0,0,0,0,0,-1})));
				netHeads.append("预付费资源消费总计：￥");
				netHeads.append(getTotalCost(null, null, begin,
						end, type, productName, null, cusId));
				Label typeheader = new Label(0, row + 1, netHeads.toString(),
						setStyle(16, false, "1"));
				ws.addCell(typeheader);
				ws.mergeCells(0, row + 1, 9, 0);
				ws.setRowView(row + 1, 500);
				for (int j = 0; j < netTitle.length; j++) { // 每种类型内列表表头
					Label header = new Label(2 * j, row + 2, netTitle[j],
							setStyle(11, true, "2"));

					// ws.setColumnView(j, cellView);//根据内容自动设置列宽
					ws.mergeCells(2 * j, row + 2, 2 * (j + 1) - 1, row + 2);
					ws.setColumnView(j, 20); // 设置表头列的宽度
					ws.setRowView(row + 2, 550);
					ws.addCell(header);
				}
				for (int j = 0; j < list.size(); j++) {// 每种类型内数据列表 每一行
					MoneyRecord details = list.get(j);
					for (int o = 0; o < netTitle.length; o++) { // 每一列
						String cString = "";
						switch (o) {
						case 0:
							cString = formatExcelDate.format(details
									.getMonTime());
							break;
						case 1:
							cString = details.getProductName();
							break;
						case 2:
							cString = details.getOrderNo();
							break;
						case 3:
							cString = "1".equals(details.getPayType()) ? "预付费"
									: "后付费";
							break;
						case 4:
							cString = "￥"
									+ formatTwo(details.getPrepaymentMoney().toString());
							break;
						}
						Label content = new Label(2 * o, row + j + 3, cString,
								setStyle(12, false, "3"));
						ws.mergeCells(2 * o, row + j + 3, 2 * (o + 1) - 1, row
								+ j + 3);
						ws.setColumnView(o, 20);
						ws.setColumnView(o + 3, 20);
						// ws.setColumnView(o, cellView);
						ws.addCell(content);
					}
				}
				Label diff = new Label(0, row + list.size() + 4, "",
						setTitleStyle(18, "2"));
				ws.addCell(diff);
				ws.setRowView(row + list.size() + 4, 200);
				row = row + list.size() + 3;
			}
			workbook.write();
			workbook.close();
		} catch (Exception ex) {
			log.error("导出excel失败", ex);
			throw ex;
		}
		
	}

	@Override
	public MoneyRecord changePrepaymentState(String orderNo) throws Exception {
		List<Object> list=new ArrayList<Object>();
		list.add(PayType.PAYBEFORE);
		list.add(orderNo);
		List<BaseMoneyRecord> moneyRecordList=moneyRecordDao.find("from BaseMoneyRecord where payType=? and orderNo=? order by serialNumber desc ", list.toArray());
		MoneyRecord moneyRecord=new MoneyRecord();
		if(moneyRecordList!=null&&moneyRecordList.size()>0){
			BaseMoneyRecord baseMoneyRecord=moneyRecordList.get(0);
			baseMoneyRecord.setIsSuccess("1");
			baseMoneyRecord=(BaseMoneyRecord) moneyRecordDao.merge(baseMoneyRecord);
			BeanUtils.copyPropertiesByModel(moneyRecord, baseMoneyRecord);
		}
		return moneyRecord;
	}

	@Override
	public List<MoneyRecord> getArrearsListByCusId(String cusId) throws Exception {
		log.info("获取客户["+cusId+"]的欠费费用报表记录");
		//获取pay_status=2的记录
		StringBuffer sb = new StringBuffer();
		sb.append(" from BaseMoneyRecord where payState=? and cusId=? order by serialNumber asc");
		List<BaseMoneyRecord> baseMoneyRecordListist =  moneyRecordDao.find(sb.toString(), "2", cusId);
		List<MoneyRecord> recordList = new ArrayList<>();
		for(BaseMoneyRecord baseMoneyRecord:baseMoneyRecordListist){
			MoneyRecord record = new MoneyRecord();
			BeanUtils.copyProperties(record, baseMoneyRecord);
			recordList.add(record);
		}
		return recordList;
	}

	@Override
	public void updateMoneyRecord(MoneyRecord moneyRecord) throws Exception {
		log.info("更新交易记录开始");
		BaseMoneyRecord baseMoneyRecord = new BaseMoneyRecord();
		BeanUtils.copyPropertiesByModel(baseMoneyRecord, moneyRecord);
		moneyRecordDao.merge(baseMoneyRecord);
	}

	@Override
	public Order getPrepaymentDetails(String orderNo) throws Exception {
		Order order=orderService.getOrderByNo(orderNo);
		String money=order.getAccountPayment()!=null?order.getAccountPayment().toString():"0.000";
		money=formatTwo(money);
		order.setAccountPayment(new BigDecimal(money));
		String paymentAmount=order.getPaymentAmount()!=null?order.getPaymentAmount().toString():"0.000";
		order.setPaymentAmount(new BigDecimal(formatTwo(paymentAmount)));
		String thirdPartPayment=order.getThirdPartPayment()!=null?order.getThirdPartPayment().toString():"0.000";
		order.setThirdPartPayment(new BigDecimal(formatTwo(thirdPartPayment)));
		return order;
	}

	@Override
	public boolean orderIsBelong(String orderNo) throws Exception {
		return orderService.isOrderBelongsToCurrCus(orderNo);
	}

	@Override
	public BigDecimal getCostForObs(String cusId, Date begin, Date end,
			String type) throws Exception {
		String hql="select monConfigure from BaseMoneyRecord where monTime>=? and monTime<? and cusId=? and productName=?";
		List<Object> params=new ArrayList<Object>();
		params.add(begin);
		params.add(end);
		params.add(cusId);
		params.add("对象存储-按需付费");
		List result=moneyRecordDao.find(hql, params.toArray());
		BigDecimal cost=new BigDecimal(0.000);
		for (Object object : result) {
			JSONArray array=JSONArray.parseArray(object.toString());
			for (Object obj : array) {
				JSONObject json=JSONObject.parseObject(obj.toString());
				String name=json.getString("name");
				if("obs".equals(type)){
					if("存储空间".equals(name)||"下载流量".equals(name)||"请求次数".equals(name)){
						BigDecimal price=json.getBigDecimal("price");
						cost=cost.add(price);
					}
				}else if("cdn".equals(type)){
					if("CDN下载流量".equals(name)||"CDN动态请求数".equals(name)||"CDN-HTTPS请求数".equals(name)){
						BigDecimal price=json.getBigDecimal("price");
						cost=cost.add(price);
					}
				}
			}
		}
		return cost;
	}

}
