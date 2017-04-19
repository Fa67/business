package com.eayun.notice.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.eayun.common.exception.AppException;
import com.eayun.notice.model.MessageCloudDataBaseDeletedFailModel;
import com.eayun.notice.model.MessageCloudDataBaseRollBackModel;
import com.eayun.notice.model.MessageEcscToMailEcmc;
import com.eayun.notice.model.MessageExpireRenewResourcesModel;
import com.eayun.notice.model.MessageOperateModel;
import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.notice.model.MessagePayAsYouGoResourcesStopModel;
import com.eayun.notice.model.MessagePayUpperLimitResourcesStopModel;
import com.eayun.notice.model.MessageResourcesExpiredModel;
import com.eayun.notice.model.MessageResourcesStopModel;
import com.eayun.notice.model.MessageStackSynFailResour;
import com.eayun.notice.model.MessageUnitModel;
import com.eayun.notice.model.MessageUserResour;
import com.eayun.order.model.Order;

public interface MessageCenterService {
	
	/**
	 * 到期续费
	 * @param List<ExpireRenewResourcesModel> 资源停用类
	 * @param cusId  客户id
	 * 
	 * */
	public void  expireRenewMessage (List<MessageExpireRenewResourcesModel> resourList, String cusId ) 
			throws AppException;

	/**
	 * 资源过期
	 * @param List<ResourcesExpiredModel> 资源停用类
	 * @param cusId  客户id
	 * 
	 * */
	public void resourExpiredMessage (List<MessageResourcesExpiredModel> resourList,String cusid) 
			throws AppException;
	
	/**
	 * 资源停用
	 * @param List<ResourcesStopModel> 资源停用类
	 * @param cusId  客户id
	 * 
	 * */
	public void resourStopMessage (List<MessageResourcesStopModel> resourList,String cusid) 
			throws AppException;

	/**
	 * 付费资源欠费上限停用通知
	 * @param List<PayAsYouGoResourcesStopModel> 资源停用类
	 * @param cusId  客户id
	 
	 * */
	public void payAsYouGoResourStopMessage (List<MessagePayAsYouGoResourcesStopModel> resourList,String cusid) 
			throws AppException;
	
	/**
	 * 按需付费资源停用 
	 @param  List<PayUpperLimitResourcesStopModel> 资源停用类
	 * @param cusId  客户id
	 * 
	 * */
	public void payUpperLimitResourStopMessage (List<MessagePayUpperLimitResourcesStopModel> resourList,String cusid) 
			throws AppException;

	/**
	 * 余额不足
	 
	 * @param cusId  客户id
	
	 * */
	public void balanLackceMessage ( String cusid) 
			throws AppException;
	/**
	 * 账号欠费
	 * @param cusId  客户id
	 * @param resourcesCount 资源数量
	 * */
	public void accountArrearsMessage ( String cusid,int resourcesCount) 
			throws AppException;
	
	
	/**
	 * 账户充值
	 * * @param payMoney  充值金额
	 * @param balance  当前余额
	 * @param cusId  客户id
	 * */
	public void accountPayMessage ( String cusid,BigDecimal payMoney,BigDecimal balance) 
			throws AppException;

	/**
	 * 账户冻结
	 * @param cusId  客户id
	 * */
	public void accountFrozenMessage ( String cusid) 
			throws AppException;
	
	/**
	 * 账户恢复
	 * @param cusId  客户id
	 * */
	public void accountRecoveryMessage (String cusid) 
			throws AppException;

/**
 * 新订单提醒
 * @param order 实体
 * */
	public void newOrderMessage (Order order)throws AppException;
			

	/**
	 * ECMC扣除客户资金
	 * 
	 * */
	
	public void ecmcDeductionFund( String cusid,BigDecimal deductionMoney,BigDecimal balance)
			throws AppException;
	
	
/**
 * 新增资源失败
 * @param orderNo 订单编号
 * @param cusId  客户id
 * */
	public void addResourFailMessage (String orderNo,String cusId) 
			throws AppException;
/**
 * 底层删除资源失败
 * @param List<MessageOrderResourceNotice> orderRe  资源信息
 * @param orderNo 订单编号
 * */
	public void delecteResourFailMessage (List<MessageOrderResourceNotice> orderRe,String orderNo) 
			throws AppException;

/**
 * 用户资源恢复失败
 *  @param List<MessageUserResour> userResour  用户资源信息
 * */
	public void userResourRecoveryFail(List<MessageUserResour> userResour) throws AppException;
	
	/**
	 * 底层同步资源不一致
	 * @param List<stackSynFailResour> stacksynResour  底层同步资源 
	 * 
	 * */
	public void stackSynFail(List<MessageStackSynFailResour> stackResour) throws AppException;
	
	
	
	/**
	 * ECSC用户充值 ECMC后台接收短信
	 * */
	
	public void ecscToMailEcmc(MessageEcscToMailEcmc ecsctoecmc,String sendWho)throws AppException;
	
	
	/**
	 * 发票（已开票）发票处理完成通知
	 * @param money 金额
	 * @param receiptType 发票类型 
	 * @param receiptRise 发票抬头
	 * @param address 地址
	 * @param status 状态
	 * @param receipttime 发票申请时间
	 * */
	
	public void yesOpenReceipt(String cusId,BigDecimal money,String receiptType,String receiptRise,String address,String status,Date receiptTime)throws AppException;
	
	/**
	 * 发票（取消）ECMC工作人员取消客户发票申请
	 * @param money 金额
	 * @param receiptType 发票类型 
	 * @param receiptRise 发票抬头
	 * @param address 地址
	 * @param status 状态
	 * @param receipttime 发票申请时间
	 * @param CancelReceiptRe 发票取消原因
	 * */
	
	public void ecmcCancelReceipt(String cusId,BigDecimal money,String receiptType,String receiptRise,String address,String status,Date receiptTime,String CancelReceiptRe)throws AppException;
	
	/**
	 * 发票（新增）新增开票申请
	
	 * @param receipttime 发票申请时间
	 * @param money 金额
	 * @param receiptType 发票类型 
	 * @param receiptRise 发票抬头
	 * @param address 地址  
	 * @param status 状态
	 * 
	
	 * */
	
	public void newReceiptInfo(String cusId,BigDecimal money,String receiptType,String receiptRise,String address,String status,Date receiptTime)throws AppException;
	
	
	
	/**
	 * 云数据库手动备份-底层删除失败
	 * 
	 * @param cusId 发客户ID
	 * @param resourList 云数据库删除异常类
	 * 
	 * */
	public void cloudDataBaseBackupDeletedFail(String prjId,List<MessageCloudDataBaseDeletedFailModel> resourList)throws AppException;
	
	/**
	 * 云数据库自动备份未执行
	 * 
	 * @param cusId 发客户ID
	 * @param cloudDataBaseName 云数据库名称
	 * @param BackupStartTime 自动备份时间
	 * 
	 * */
	public  void cloudDataBaseBackupNoStart(String projectid,String cloudDataBaseName,Date BackupStartTime)throws AppException;
	
	
	
	/**
	 * 云数据库root密码生成
	 * 
	 * @param cusId 发客户ID
	 * @param cloudDataBaseName 云数据库名称
	 * @param rootPass root密码
	 * 
	 * */
	public  void addCloudDataBaseRootPassWord(String cusId,String cloudDataBaseName,String rootPass)throws AppException;
	
	/**
	 * 云数据库root密码重置
	 * 
	 * @param cusId 发客户ID
	 * @param cloudDataBaseName 云数据库名称
	 * @param 
	 * 
	 * */
	public  void ResetcloudDataBaseRootPassWord(String cusId,String cloudDataBaseName)throws AppException;
	
	
	/**
	 * 云数据库普通账号密码修改
	 * 
	 * @param cusId 发客户ID
	 * @param cloudDataBaseName 云数据库名称
	 * @param cloudDataUserName 数据库下用户名称
	 * 
	 * */
	public  void cloudDataBaseNoAdminPassWordUpdate(String cusId,String cloudDataBaseName,String cloudDataUserName)throws AppException;
	
	
	/**
	 * 云数据库实例升降规格失败-底层回滚失败
	 * 
	 * @param cusId 发客户ID
	 * @param 
	 * @param resourList 云数据库消息拼装类
	 * 
	 * */
	public  void cloudDataBaseRollBackFail(String cusId,List<MessageCloudDataBaseRollBackModel>  resourList)throws AppException;
	
	
	
	
	/**
	 * 备案状态修改发送邮件短信
	 * 
	 * */
	public void unitStatusToMailAndSms(String phone,String mail,String userName,List<MessageUnitModel> model)throws AppException;
	
	
	 /**
	 * 备案状态修改发送邮件短信
	*
	* */
	public void newAddUnitTomail(List<MessageUnitModel> model)throws AppException;
		    
	/**
	 * 运营邮件
	 * 
	 * */
	public void OperateMail(MessageOperateModel model) throws AppException;
}
