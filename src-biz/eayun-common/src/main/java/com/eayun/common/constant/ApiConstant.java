package com.eayun.common.constant;

/**
 * Created by Administrator on 2016/11/13.
 */

/**
 * 保存一些常量,接口统一管理
 */
public interface ApiConstant {

    /**====================================整体中用到的属性常量  开始=======================================*/
    /**
     * Request ID
     */
    String TAG_REQUEST_ID = "job_Id";
    /**
     * Log Identify
     */
    String LOG_IDENTIFY = "log";
    /**
     * Collection Name
     */
    String API_LOG_COLLECTION_NAME = "api.service.log" ;
    /**
     * Post Identify
     */
    String TAG_API_SERVICE_PARAMS_JSONOBJECT  = "post_body" ;
    /**
     * Action Tag
     */
    String TAG_ACTION = "Action" ;
    /**
     * Class Tag
     */
    String TAG_CLASS_NAME = "cName" ;
    /**
     * Method Tag
     */
    String TAG_METHOD_NAME = "mName" ;
    /**
     * Cus Tag
     */
    String TAG_CUS_ID = "CusId" ;
    /**
     * DC ID Tag
     */
    String TAG_DC_ID = "DcId" ;
    /**
     * DC NAME Tag
     */
    String TAG_DC_NAME = "DcName" ;
    /**
     * Resource Tag
     */
    String TAG_RESOURCE_TYPE = "resourceType" ;
    /**
     * Api Name Tag
     */
    String TAG_API_NAME = "apiName" ;
    /**
     * Base Api Service Package
     */
    String API_SERVICE_PACKAGE_NAME = "com.eayun" ;
    /**
     * Error Code & Message File
     */
    String ERROR_CODE_MESSAGE_MAPPING_PROPERTY_FILE = "EayunApiErrorCodeMessageMapping.properties" ;
    /**
     * Current API Version
     */
    String VERSION_PREFIX = "V1/,V2/" ;
    
    /**
     * url请求Header中content-type的值
     */
    final static String CONTENT_TYPE= "text/json; charset=utf-8";
    /**====================================整体中用到的属性常量  结束=======================================*/

    /**====================================公共框架处理错误代码  开始=======================================*/

    /**
     * 请求内容不是合法的JSON格式字符串
     */
    String HTTP_BODY_ERROR_CODE = "100032" ;
    /**
     * 其他异常（除已经设计好的异常类型之外）
     */
    String SERVER_RUNTIME_ERROR_CODE = "500001" ;
    /**
     * Action类型不存在，代码层面判断是否配置
     */
    String SYSTEM_ACTION_NOT_SET_ERROR_CODE = "100004" ;

    /**====================================公共框架处理错误代码  结束=======================================*/

    /**
     * 系统维护中
     */
    final static String	SERVER_MAINTENANCE_ERROR_CODE = "100001";
    /**
     * Action未设置
     */
    String NOT_SET_ACTION_ERROR_CODE = "100002" ;
    /**
     * Accesskey未设置
     */
    String NOT_SET_ACCESSKEY_ERROR_CODE = "100003" ;
    /**
     * AccessKey 不存在
     */
    final static String ACCESSKEY_NOT_EXIST_ERROR_CODE = "100005";		
    /**
     * 该AccessKey处于禁用状态
     */
    final static String ACCESSKEY_IS_DISABLED_ERROR_CODE = "100006";	
    /**
     * 找不到AK对应的SK
     */
    final static String UNABLE_FIND_SK_AK_ERROR_CODE = "100007";	
    /**
     * //该Action处于禁用状态
     */
    final static String ACTION_IS_DISABLED_ERROR_CODE = "100008";		
    /**
     * 	API访问次数已超过配额
     */
    final static String API_REQUEST_QUOTA_ERROR_CODE = "100009";	
    /**
     * 	Signature不匹配
     */
    final static String SIGNATURE_NOT_MATCH_ERROR_CODE = "100010";	
    /**
     * 签名信息未设置
     */
    String NOT_SET_SIGNATURE_ERROR_CODE = "100011" ;
    /**
     * Signature 格式错误
     */
    final static String SIGNATURE_FORMAT_ERROR_CODE = "100012";			
    /**
     * Signature超时
     */
    final static String SIGNATURE_EXPIRED_ERROR_CODE = "100013";		
    /**
     * 时间戳未设置
     */
    String NOT_SET_Timestamp_ERROR_CODE = "100014" ;
    /**
     * Timestamp格式错误
     */
    String Timestamp_FORMAT_ERROR_CODE = "100015" ;
    /**
     * 用户使用了不支持的Http Method（当前 TOP 只支持POST）
     */
    final static String HTTP_METHOD_NOT_SUPPORTED_ERROR_CODE = "100016";
    /**
     * 账户已冻结
     */
    final static String CUS_FROZEN_ERROR_CODE = "100018";		
    /**
     * Region数据中心不存在
     */
    final static String REGION_NOT_EXIST_ERROR_CODE = "100020"; 		
    /**
     * Region不合法
     */
    final static String REGION_FORMAT_ERROR_CODE = "100021";	
    /**
     * 数据中心未设置
     */
    String NOT_SET_REGION_ERROR_CODE = "100022" ;
    /**
     * 请求结构Head部分格式错误
     */
    final static String REQUEST_HEAD_FORMAT_ERROR_CODE = "100028";
    /**
     * 请求body部分格式错误
     */
    final static String REQUEST_BODY_FORMAT_ERROR_CODE = "100032";
    /**
     * 账户已禁用
     */
    final static String ACCOUNT_IS_DISABLED = "100035";
    /**
     * IP已禁用
     */
    final static String IP_IS_DISABLED = "100036";
    
    /**====================================服务端错误代码  开始=======================================*/
    /**
     * 内部错误
     */
    final static String INTERNAL_ERROR_CODE = "500001";					
    
    /**====================================服务端错误代码  结束=======================================*/

    final static String API_ZB_NODE_ID = "0010003003" ;
    final static String API_MONITORINGALARM_CORRECT_ZB = "0010003003001" ;
    final static String API_MONITORINGALARM_AVAILABILITY_ZB = "0010003003002" ;
    final static String API_MONITORINGALARM_REQUESTSNUMBER_ZB = "0010003003003" ;
    final static String API_MONITORINGALARM_DEALTIME_ZB = "0010003003004" ;
}