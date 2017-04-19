package com.eayun.common.exception;


import com.eayun.common.tools.ErrorsUtil;

@SuppressWarnings("serial")
public class AppException extends RuntimeException {
	private String ErrorMessage = null;

	private String[] argsMessage = null;

	private Throwable myException;
	
	private String [] originMessage = null;

	/**
	 * @param msg
	 */
	public AppException(String msg) {
		this(msg, null,null);
	}

	/**
	 * 
	 * @param msg
	 * @param args
	 */
	public AppException(String msg, String[] args) {
		/**
         *传入带BOSS名称的异常,String数组长度为3 如：
         *throw (new AppException("error.boss.message", new String[] {   
	     *result, (String) output.get("SVC_ERR_MSG32"),"s5584PhoneCallWS" })) 
         */
		this(msg, null, args);
	}

	/**
	 * @param msg
	 * @param ex
	 */
	public AppException(String msg, Throwable ex) {
		this(msg,ex,null);
	}

	/**
	 * 
	 * @param msg
	 * @param ex
	 * @param args
	 */
	public AppException(String msg, Throwable ex, String[] args) {
		super(msg);
		ErrorsUtil u=new ErrorsUtil();		
		this.ErrorMessage = msg;
		this.originMessage = args;
		if(args!=null){
			this.argsMessage = u.transfer(args);
		}
		if(ex==null){
			myException=this;
		}else{
			myException = ex;
		}
	}

	/**
	 * @return java.lang.String
	 */
	public String getErrorMessage() {
		return ErrorMessage;
	}

	/**
	 * @return java.lang.Throwable
	 */
	public Throwable getMyException() {
		return myException;
	}

	public String[] getArgsMessage() {
		return argsMessage;
	}

	public String[] getOriginMessage() {
		return originMessage;
	}
	
}
