package com.eayun.price.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletOutputStream;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.price.bean.PriceTreeData;
import com.eayun.price.bean.UpgradeBean;
import com.eayun.price.model.BillingFactor;

public interface BillingFactorService {

	/**
	 * 查询所有计费因子分页列表
	 * @param page
	 * @param queryMap
	 * @param dcId
	 * @param billingFactor
	 * @param resourcesType
	 * @return
	 */
	public Page getFactorsPage(Page page, QueryMap queryMap, String dcId,
			String billingFactor, String resourcesType,String priceType);

	/**
	 * 查询一种计费单位在一个数据中心下的预付费或后付费价格
	 * @param dcId
	 * @param billingFactor
	 * @param resourcesType
	 * @param payType 
	 * @param billingUnit 
	 * @return
	 */
	public List<BillingFactor> getPricesByPayType(String dcId,
			String billingFactor, String resourcesType, String billingUnit, String payType);

	/**
	 * 查询所有的资源类型
	 * @return
	 */
	public List<PriceTreeData> getAllResourcesType(String priceType);

	/**
	 * 添加价格
	 * isDefault:为true表示是第一次有确认类型时默认添加的价格
	 * @param billingFactor
	 * @return
	 */
	public BillingFactor addFactorPrice(BillingFactor billingFactor,boolean  isDefault);

	/**
	 * 编辑价格
	 * @param billingFactor
	 */
	public void editFactorPrice(BillingFactor billingFactor);

	/**
	 * 删除价格
	 * @param id
	 */
	public void deleteFactorPrice(String id);
	
	/**
	 * 计算一次业务价格，返回总价
	 * @param paramBean
	 * @return
	 */
	public BigDecimal getPriceByFactor(ParamBean paramBean);

	/**
	 * 计算一次业务价格，返回总价及各计费单位价格（这里不包含批量相乘）,包含乘以包月数（小时数）
	 * @param paramBean
	 * @return
	 */
	public PriceDetails getPriceDetails(ParamBean paramBean);
	
	/**
	 * 计算一次升级业务的价格，返回总价
	 * @param upgradeBean
	 * @return
	 */
	public BigDecimal updateConfigPrice (UpgradeBean upgradeBean);

	/**
	 * 价格缓存同步
	 */
	public void syncFactorPrice();
	/**
	 * 删除一个镜像的所有价格
	 * @param imageId
	 */
	public void deleteImagePrice(String imageId ,String dcId);

	public void exportPriceSheets(ServletOutputStream outputStream, String dcId , String type);

	public void importPriceExcel(InputStream is,String dcId,String priceType)throws Exception ;
	
    public String getDcNameById(String dcId);
}
