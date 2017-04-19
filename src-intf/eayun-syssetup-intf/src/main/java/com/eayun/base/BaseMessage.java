package com.eayun.base;

import java.io.Serializable;

public class BaseMessage implements Serializable{

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 4313140047170542286L;

    private String errorMessage;
    
    private String errorCode;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    
}
