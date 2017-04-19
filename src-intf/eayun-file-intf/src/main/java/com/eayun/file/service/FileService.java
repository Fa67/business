package com.eayun.file.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

import com.eayun.file.model.EayunFile;

public interface FileService {
    
    /**
     * 文件类型转换为输入流上传
     * @param file
     * @return
     */
    public String uploadFile(File file , String userAccount);
    
    /**
     * 获取文件字节流上传
     * MultipartFile：(ECMC/ECSC均使用此类型传递文件)
     * @param file
     * @return
     * @throws IOException 
     */
    public String uploadFile(MultipartFile file , String userAccount) throws Exception;
    /**
     * 上传输入流
     * @param in
     * @param uploadFileName    原文件名
     * @param fileLength        文件大小
     * @return
     */
    public String uploadFile(InputStream in, String uploadFileName, long fileLength , String userAccount) throws Exception;
    
    /**
     * 
     * @param file 文件
     * @param uploadFileName 文件名称
     * @param fileType  文件类型
     * @param userAccount 上传人
     * @return
     */
    public String uploadFile(File file, String uploadFileName, String fileType, String userAccount)throws Exception;
    
    /**
     * 下载文件
     * @param fileId：存储文件记录id
     */
    public InputStream downloadFile(String fileId) throws Exception;
    /**
     * 删除文件
     * @param fileId：存储文件记录id
     * @return
     * true:    删除成功
     * false:   删除失败
     */
    public boolean deleteFile(String fileId) throws Exception;
    /**
     * 获取一条存储文件记录信息
     * @param fileId
     * @return
     */
    public EayunFile findOneById(String fileId);
}
