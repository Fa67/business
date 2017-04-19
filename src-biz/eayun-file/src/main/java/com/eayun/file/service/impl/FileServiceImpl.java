package com.eayun.file.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.csource.fastdfs.UploadCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.file.dao.FileDao;
import com.eayun.file.model.BaseEayunFile;
import com.eayun.file.model.EayunFile;
import com.eayun.file.model.FileType;
import com.eayun.file.service.FileService;
@Service
@Transactional
public class FileServiceImpl implements FileService{
    
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9','a', 'b', 'c', 'd', 'e', 'f' };
    
    public static boolean isInit = false;
    
    @Autowired
    private FileDao fileDao;
    
    private StorageClient getStorageClient() throws IOException {
        TrackerClient tracker = new TrackerClient(); 
        TrackerServer trackerServer = tracker.getConnection(); 
        StorageServer storageServer = null;

        StorageClient storageClient = new StorageClient(trackerServer, storageServer); 
        return storageClient;
    }
    
    @Override
    public String uploadFile(File file , String userAccount) {
//        try {
//            FileInputStream in = new FileInputStream(file);
//        } catch (FileNotFoundException e) {
//            log.error("文件上传失败", e);
//        }
        return null;
    }
    @Override
    public String uploadFile(MultipartFile file , String userAccount) throws Exception{//上传
        String eayunFileId = "";
        try {
            String md5 = getMd5ByMultiFile(file);
            
            BaseEayunFile baseEayunFile = new BaseEayunFile();
            baseEayunFile.setFileCreatedate(new Date());
            baseEayunFile.setFileCode(md5);
            baseEayunFile.setFileUserName(userAccount);
            baseEayunFile.setFileSize(file.getSize());
            String name = file.getOriginalFilename();
            baseEayunFile.setFileName(name);
            
            List<BaseEayunFile> baseFileList = findListByMD5(md5);
            if(baseFileList.size() > 0){
                BaseEayunFile baseFile = baseFileList.get(0);
                
                baseEayunFile.setFileGroupname(baseFile.getFileGroupname());
                baseEayunFile.setFilePath(baseFile.getFilePath());
                baseEayunFile.setFileType(baseFile.getFileType());
                baseEayunFile.setFileSize(baseFile.getFileSize());
            }else{
                //init();
                StorageClient storageClient = getStorageClient();
                NameValuePair nvp [] = new NameValuePair[]{     //附加属性
                };
                
                InputStream is = file.getInputStream();
                String type = getFileType(is);
                if(type.equals("XLS_DOC")||type.equals("XLSX_DOCX")||type.equals("WPS")){
                    type = name.substring(name.lastIndexOf(".")+1);
                }
                byte[] fileBuff = file.getBytes(); 
                //上传文件
                String fileIds[] = storageClient.upload_file(fileBuff, type, nvp);
                String groupName = fileIds[0];
                String filePath = fileIds[1];
                
                baseEayunFile.setFileGroupname(groupName);
                baseEayunFile.setFilePath(filePath);
                baseEayunFile.setFileType(type);
            }
            baseEayunFile = addEayunFile(baseEayunFile);
            eayunFileId = baseEayunFile.getEayunFileId();
            
        } catch (NoSuchAlgorithmException e) {
            log.error("文件上传失败", e);
            throw e;
        }catch (MyException e) {
            log.error("文件上传失败", e);
            throw e;
        }
        return eayunFileId;
    }

    @Override
    public String uploadFile(InputStream in, String uploadFileName, long fileLength , String userAccount)  throws Exception{
        String eayunFileId = "";
        try {
            String md5 = getMd5ByInputStream(in);
            
            BaseEayunFile baseEayunFile = new BaseEayunFile();
            baseEayunFile.setFileCreatedate(new Date());
            baseEayunFile.setFileCode(md5);
            baseEayunFile.setFileUserName(userAccount);
            baseEayunFile.setFileSize(fileLength);
            baseEayunFile.setFileName(uploadFileName);
            
            List<BaseEayunFile> baseFileList = findListByMD5(md5);
            if(baseFileList.size() > 0){
                BaseEayunFile baseFile = baseFileList.get(0);
                
                baseEayunFile.setFileGroupname(baseFile.getFileGroupname());
                baseEayunFile.setFilePath(baseFile.getFilePath());
                baseEayunFile.setFileType(baseFile.getFileType());
                baseEayunFile.setFileSize(baseFile.getFileSize());
            }else{
                //init();
                StorageClient storageClient = getStorageClient();
                NameValuePair nvp [] = new NameValuePair[]{     //附加属性
                };
                
                String type = getFileType(in);
                if(type.equals("XLS_DOC")||type.equals("XLSX_DOCX")||type.equals("WPS")){
                    type = uploadFileName.substring(uploadFileName.lastIndexOf(".")+1);
                }
                //上传文件
                String fileIds[] = storageClient.upload_file(null, fileLength, new UploadFileSender(in), type, nvp);
                String groupName = fileIds[0];
                String filePath = fileIds[1];
                
                baseEayunFile.setFileGroupname(groupName);
                baseEayunFile.setFilePath(filePath);
                baseEayunFile.setFileType(type);
            }
            baseEayunFile = addEayunFile(baseEayunFile);
            eayunFileId = baseEayunFile.getEayunFileId();
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw e;
        }catch (MyException e) {
            log.error("文件上传失败", e);
            throw e;
        }
        return eayunFileId;
    }
    
    
    public String uploadFile(File file, String uploadFileName, String fileType, String userAccount)  throws Exception{
        String eayunFileId = "";
        InputStream inputStream = null;
        try {
            String md5 = getMd5ByInputStream(new FileInputStream(file));
            
            BaseEayunFile baseEayunFile = new BaseEayunFile();
            baseEayunFile.setFileCreatedate(new Date());
            baseEayunFile.setFileCode(md5);
            baseEayunFile.setFileUserName(userAccount);
            baseEayunFile.setFileSize(file.length());
            baseEayunFile.setFileName(uploadFileName);
            
            List<BaseEayunFile> baseFileList = findListByMD5(md5);
            if(baseFileList.size() > 0){
                BaseEayunFile baseFile = baseFileList.get(0);
                
                baseEayunFile.setFileGroupname(baseFile.getFileGroupname());
                baseEayunFile.setFilePath(baseFile.getFilePath());
                baseEayunFile.setFileType(baseFile.getFileType());
                baseEayunFile.setFileSize(baseFile.getFileSize());
            }else{
                //init();
                StorageClient storageClient = getStorageClient();
                NameValuePair nvp [] = new NameValuePair[]{     //附加属性
                };
                
                //上传文件
                inputStream = new FileInputStream(file);
//                String fileIds[] = storageClient.upload_file(null, file.length(), new UploadFileSender(inputStream), fileType, nvp);
                String fileIds[] = storageClient.upload_file(IOUtils.toByteArray(inputStream), fileType, nvp);
                String groupName = fileIds[0];
                String filePath = fileIds[1];
                
                baseEayunFile.setFileGroupname(groupName);
                baseEayunFile.setFilePath(filePath);
                baseEayunFile.setFileType(fileType);
            }
            baseEayunFile = addEayunFile(baseEayunFile);
            eayunFileId = baseEayunFile.getEayunFileId();
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw e;
        }catch (MyException e) {
            log.error("文件上传失败", e);
            throw e;
        } finally {
			IOUtils.closeQuietly(inputStream);
		}
        return eayunFileId;
    }
    
    
    private static class UploadFileSender implements UploadCallback {
        
        private InputStream inStream;  
        
        public UploadFileSender(InputStream inStream) {  
            this.inStream = inStream;  
        }  
        public int send(OutputStream out) throws IOException {  
            int readBytes;  
            while((readBytes = inStream.read()) > 0) {  
                out.write(readBytes);  
            }  
            return 0;  
        }  
    }
    
    @Override
    public InputStream downloadFile(String fileId)  throws Exception{//下载
        try {
            StorageClient storageClient = getStorageClient();
            
            BaseEayunFile baseFile = fileDao.findOne(fileId);
            byte[] buffer = storageClient.download_file(baseFile.getFileGroupname(), baseFile.getFilePath());
            InputStream in = new ByteArrayInputStream(buffer);
            return in;
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw e;
        }
    }
    /**
     * 删除
     * @param fileId
     * @return
     * @see com.eayun.file.service.FileService#deleteFile(java.lang.String)
     */
    @Override
    public boolean deleteFile(String fileId) throws Exception {      //删除
        BaseEayunFile baseFile = fileDao.findOne(fileId);
        boolean issuccess = false;
        if(null == baseFile){
            issuccess = false;
            throw new AppException("文件不存在！！！");
        }else{
            List<BaseEayunFile> baseFileList = findListByMD5(baseFile.getFileMD5());
            if(baseFileList.size() == 1){
                try {
                    //init();//初始化
                    StorageClient storageClient = getStorageClient();
                    int i = storageClient.delete_file(baseFile.getFileGroupname(), baseFile.getFilePath());
                    if(i ==0){
                        issuccess = true;
                        fileDao.delete(fileId);
                    }else{
                        issuccess = false;
                    }
                } catch (Exception e) {
                    log.error("文件删除失败", e);
                    throw e;
                }
            }else if(baseFileList.size() > 1){
                fileDao.delete(fileId);
                issuccess = true;
            }
        }
        return issuccess;
    }

    @Override
    public EayunFile findOneById(String fileId) {//获取一条存储信息
        BaseEayunFile baseFile = fileDao.findOne(fileId);
        EayunFile eayunFile = new EayunFile();
        BeanUtils.copyPropertiesByModel(eayunFile, baseFile);
        return eayunFile;
    }

    /**
     * 添加一条存储记录
     * @param eayunFile
     * @return
     */
    private BaseEayunFile addEayunFile(BaseEayunFile baseEayunFile) {
        fileDao.saveEntity(baseEayunFile);
        return baseEayunFile;
    }

    /**
     * MD5码相同的文件存储记录列表
     * @param md5
     * @return
     */
    private List<BaseEayunFile> findListByMD5(String md5) {
        List<BaseEayunFile> baseFileList = fileDao.findListByMD5(md5);
        return baseFileList;
    }
    
    
    /**
     * 获取文件MD5码
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static String getMd5ByFile(File file) throws FileNotFoundException {  
        String value = null;  
        FileInputStream in = new FileInputStream(file);  
    try {  
        MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());  
        MessageDigest md5 = MessageDigest.getInstance("MD5");  
        md5.update(byteBuffer);  
        BigInteger bi = new BigInteger(1, md5.digest());  
        value = bi.toString(16);  
    } catch (Exception e) {
        log.error("获取文件MD5码失败", e);
    } finally {  
            if(null != in) {  
                try {  
                in.close();  
            } catch (IOException e) {  
                log.error(e.getMessage(),e);
            }  
        }  
    }  
    return value;  
    }  
   /**
    * 获取文件MD5码
    * @param path
    * @return
    * @throws IOException
    */
    public static String getMd5ByPath(String path) throws IOException{
        FileInputStream fis= new FileInputStream(path);    
        String md5 = DigestUtils.md5Hex(IOUtils.toByteArray(fis));    
        IOUtils.closeQuietly(fis);    
        return md5;
    }
    /**
     * 获取文件MD5码
     * @param in
     * @return
     * @throws IOException
     */
    public static String getMd5ByInputStream(InputStream in) throws IOException{
        String md5 = DigestUtils.md5Hex(IOUtils.toByteArray(in));    
        IOUtils.closeQuietly(in);    
        return md5;
    }
    /**
     * 获取文件MD5码
     * @param file
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String getMd5ByMultiFile(MultipartFile file) throws IOException, NoSuchAlgorithmException{
        byte[] fileBuff = file.getBytes();
        MessageDigest messagedigest = MessageDigest.getInstance("MD5");
        messagedigest.update(fileBuff);
        String multiMD5 = bufferToHex(messagedigest.digest());
        return multiMD5;
    }
    private static String bufferToHex(byte bytes[]) {
        int m = 0;
        int n = bytes.length;
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
         appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
     }
    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
     }
    /**
     * 获取文件类型
     * @param is
     * @return
     * @throws IOException
     */
    public static String getFileType(InputStream is) throws IOException{
        byte[] b = new byte[28];
        try {
            is.read(b, 0, 28);
            String fileHead = bytesToHexString(b);
            if (fileHead == null || fileHead.length() == 0) {  
                return null;  
            }  
            fileHead = fileHead.toUpperCase();  
            FileType[] fileTypes = FileType.values();  
              
            for (FileType type : fileTypes) {  
                if (fileHead.startsWith(type.getValue())) {  
                    return type.name();  
                }
            }  
            return "";
        } catch (IOException e) {
            log.error("获取文件类型失败", e);
        }finally {  
            if (is != null) {  
                try {  
                    is.close();  
                } catch (IOException e) {  
                    log.error(e.getMessage(),e); 
                    throw e;  
                }  
            }  
        } 
        return "";
    }
    
    /** 
     * 将文件头转换成16进制字符串 
     *  
     * @param 原生byte 
     * @return 16进制字符串 
     */  
    private static String bytesToHexString(byte[] src){  
          
        StringBuilder stringBuilder = new StringBuilder();     
        if (src == null || src.length <= 0) {     
            return null;     
        }     
        for (int i = 0; i < src.length; i++) {     
            int v = src[i] & 0xFF;     
            String hv = Integer.toHexString(v);     
            if (hv.length() < 2) {     
                stringBuilder.append(0);     
            }     
            stringBuilder.append(hv);     
        }     
        return stringBuilder.toString();     
    }
    
    public FileServiceImpl(){
        if(isInit){
            
        }else{
            ClassPathResource res = new ClassPathResource("db.properties");
            String path = "";
            try {
                path=res.getFile().getAbsolutePath();
            } catch (IOException e1) {
                log.error(e1.getMessage(),e1);
            }
            try {
                ClientGlobal.init(path);//初始化
                isInit = true;
            } catch (FileNotFoundException e) {
                log.error("初始化配置文件失败", e);
            } catch (IOException e) {
                log.error("初始化配置文件失败", e);
            } catch (MyException e) {
                log.error("初始化配置文件失败", e);
            }
        }
    }
}
