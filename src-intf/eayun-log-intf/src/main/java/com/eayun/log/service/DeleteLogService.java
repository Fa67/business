package com.eayun.log.service;


public interface DeleteLogService {
    
    /**
     * 删除日志
     * 删除当前时间60天之前的日志
     */
    public void deleteLog();

}
