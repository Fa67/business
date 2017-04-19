package com.eayun.common.exception;

@SuppressWarnings("serial")
public class CuratorException extends RuntimeException{
	public CuratorException(String msg, Throwable ex) {
		super(msg,ex);
	}
	public CuratorException(String msg) {
		this(msg,null);
	}
}
