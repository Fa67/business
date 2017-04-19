package com.eayun.common.model;

import java.io.Serializable;

/**
 * 通用的返回前台的对象
 *                       
 * @Filename: EayunResponseJson.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月17日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class EayunResponseJson implements Serializable {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -3008359948845286845L;

    private String            respCode;
    private Object            data;
    private String            message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
