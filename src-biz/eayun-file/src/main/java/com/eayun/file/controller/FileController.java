package com.eayun.file.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.controller.BaseController;
import com.eayun.file.model.EayunFile;
import com.eayun.file.service.FileService;


@Controller
@RequestMapping("/file")
public class FileController extends BaseController {
    
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    @Autowired
    private FileService          fileService;
    /**
     * 文件下载接口
     * @param request
     * @param response
     * @param fileId
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/down")
    @ResponseBody
    public String down(HttpServletRequest request, HttpServletResponse response,String fileId,String browser) throws Exception{
        log.info("下载文件开始");
        EayunFile file = fileService.findOneById(fileId);
        response.setCharacterEncoding("utf-8");
        response.setContentType("multipart/form-data");
        
        String fileName = "";
        if("Firefox".equals(browser)){
            fileName = new String(file.getFileName().getBytes(), "iso-8859-1");
        }else{
            fileName = URLEncoder.encode(file.getFileName(), "UTF-8") ;
        }
        
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        InputStream in = fileService.downloadFile(fileId);
        OutputStream os = response.getOutputStream();
        byte[] b = new byte[2048];
        int length;
        while ((length = in.read(b)) > 0) {
            os.write(b, 0, length);
        }
        os.close();
        in.close();
        return null;
    }
}
