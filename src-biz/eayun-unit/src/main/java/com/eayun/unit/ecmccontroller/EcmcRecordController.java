package com.eayun.unit.ecmccontroller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.bean.FileIdOrName;
import com.eayun.bean.NewAccessExcel;
import com.eayun.bean.NewRecordExcel;
import com.eayun.bean.NewWebExcel;
import com.eayun.bean.UnitWebSVOE;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.tools.ExcelTitle;
import com.eayun.common.tools.ExportDataToExcel;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.file.model.EayunFile;
import com.eayun.file.service.FileService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.unit.ecmcservice.EcmcRecordService;
import com.eayun.unit.model.BaseUnitInfo;
import com.eayun.unit.service.EcscRecordService;

@Controller
@RequestMapping("/ecmc/record")
@Scope("prototype")
public class EcmcRecordController {
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired public EcmcRecordService ecmcRecordService;
	
	@Autowired
	private EcscRecordService recordservice;
	@Autowired
	private FileService fileService;
	@Autowired
	private EcmcLogService ecmclogservice;
	
	
	@RequestMapping("/getecmcrecordlist")
	@ResponseBody
	public Object getecmcrecordlist(@RequestBody Map<String, Object> requstMap) throws Exception{
		
		Page page=null;
		log.info("开始查询备案列表");
		try{
			
			Map<String,Object> map=(Map<String,Object>)requstMap.get("params");
			String recordType=MapUtils.getString(map, "recordType");
			String status=MapUtils.getString(map, "state");
			String dcid=MapUtils.getString(map, "dcId");
			
			String queryName=MapUtils.getString(map, "queryName");
			
			int pageSize=MapUtils.getIntValue(requstMap, "pageSize");
			int pageNo=MapUtils.getIntValue(requstMap, "pageNumber");
		
			QueryMap queryMap=new QueryMap();
			if(pageNo==0){
				 queryMap.setPageNum(1);
			}else{
				queryMap.setPageNum(pageNo);
			}
			 if(pageSize==0){
				 queryMap.setCURRENT_ROWS_SIZE(10);
			 }else{
			 queryMap.setCURRENT_ROWS_SIZE(pageSize);
			 }
            page=ecmcRecordService.getecmcrecordlist(queryMap, recordType, status,dcid,queryName);
			}
		catch(AppException e){
			log.error(e,e);
		}
		 return page;
		
	}
	
	
	@RequestMapping("/getecmcrecordcount")
	@ResponseBody
	public Object  getecmcrecordcount() throws Exception{
		
		log.info("开始查询备案count数量");
		return ecmcRecordService.getecmcrecordcount();
		
	}
	
	
	
	
	/**
	 * 修改备案状态
	 * */
	@RequestMapping("/updaterecordstatus")
	@ResponseBody
	public Object updaterecordstatus(HttpServletResponse response,@RequestBody Map<String, Object> requstMap) throws Exception{
		EayunResponseJson resp=new EayunResponseJson();
		OutputStream out=null;
		InputStream inp=null;
		File file=null;
		log.info("开始修改备案");
		
		try{
		String status=MapUtils.getString(requstMap, "state");
		String status1=MapUtils.getString(requstMap, "state1");
		String id=MapUtils.getString(requstMap, "id");
		
		List<FileIdOrName> list =ecmcRecordService.updaterecord(id, status,status1);
		
		
		
		resp.setRespCode(ConstantClazz.SUCCESS_CODE);
		
		}catch(Exception e){
			resp.setRespCode(ConstantClazz.ERROR_CODE);
			throw e;	
		}finally {
			IOUtils.closeQuietly(inp);
			IOUtils.closeQuietly(out);
		}
		return resp;
		
	}
	
	
	
	
	/**
	 * 
	 * 管局结果
	 * 
	 * */
	
	@RequestMapping("/recordreturn")
	@ResponseBody
	public Object recordreturn(@RequestBody Map<String, Object> requstMap)throws Exception{
		
		
		
		
		EayunResponseJson resp=new EayunResponseJson();
		log.info("开始修改备案");
		try{
			UnitWebSVOE model=new UnitWebSVOE();
			BeanUtils.mapToBean(model,(Map<String,Object>)requstMap.get("data"));
			ecmcRecordService.recordRe(model);
			System.out.println(model.getApplyId());
		resp.setRespCode(ConstantClazz.SUCCESS_CODE);
		
		}catch(Exception e){
			resp.setRespCode(ConstantClazz.ERROR_CODE);
			throw e;	
		}
		return resp;
		
		
	}
	
	
	
	
	/**
	 * 删除备案信息
	 * */
	
	@RequestMapping("/deletedrecord")
	@ResponseBody
	public Object deletedrecord(@RequestBody Map<String, Object> requstMap) throws Exception{
		EayunResponseJson resp=new EayunResponseJson();
		log.info("开始删除备案");
		try{
		String id=MapUtils.getString(requstMap, "id");
		ecmcRecordService.deletedrecord(id);
		resp.setRespCode(ConstantClazz.SUCCESS_CODE);
		}catch(Exception e){
			resp.setRespCode(ConstantClazz.ERROR_CODE);
			throw e;	
		}
		return resp;
		
	}

	
	
	/**
	 * 获取备案详情
	 * */
	
	
	@RequestMapping("/getrecordByid")
	@ResponseBody
	public Object getbyId(@RequestBody Map<String, Object> requstMap) throws Exception{
		log.info("开始获取备案详情");
		String id=MapUtils.getString(requstMap, "id");
		
		return ecmcRecordService.getbyid(id);
		
	}
	
	/**
	 * 下载
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/downRecordFile")
	public void downRecordFile(HttpServletRequest request,HttpServletResponse response) throws Exception{
		log.info("img");
		OutputStream out = null;
		InputStream inputStream = null;
		try {
			inputStream = recordservice.downloadFile(request.getParameter("fileid"));
			// 将ContentType设为"image/jpeg"，让浏览器识别图像格式。
    		response.setContentType("image/jpg");
			out = response.getOutputStream();
			IOUtils.copy(inputStream, out);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.closeQuietly(out);
		}
	}
	
	
	
	private File file(List<FileIdOrName> list) throws Exception{
		File tempFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString() + ".zip");
		ZipOutputStream zipOutputStream = null;
		InputStream input = null;
		List<InputStream> inlist = new ArrayList<InputStream>();
		try {
			zipOutputStream = new ZipOutputStream(new FileOutputStream(tempFile));

			for (int i = 0; i < list.size(); i++) {

				FileIdOrName model = list.get(i);
				try {
					
					EayunFile eayunfile=fileService.findOneById(model.getId());
					if(null!=eayunfile){
						input = recordservice.downloadFile(model.getId());
						
						inlist.add(input);
						zipOutputStream.putNextEntry(new ZipEntry(new String (model.getName().getBytes())  +"."+eayunfile.getFileType()));
						IOUtils.copy(input, zipOutputStream);
					}
					
				} catch (Exception e) {
					log.warn("下载文件出错 文件ID："+model.getId(), e);
					
				}
			}
		} finally {
			IOUtils.closeQuietly(zipOutputStream);
			for(int k=0;k<inlist.size();k++){
				IOUtils.closeQuietly(inlist.get(k));
			}
				
		}

		return tempFile;
	}
	
	
	
	/**
	 * 下载
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/downzip")
	
	public void downzip(HttpServletResponse response,HttpServletRequest request,String id) throws Exception{
		
		OutputStream out=null;
		InputStream inp=null;
		File file=null;
		log.info("开始修改备案");
		
		try{
		
		
		
			
		List<FileIdOrName> list =ecmcRecordService.updaterecord(id);
		BaseUnitInfo info=ecmcRecordService.getUnitInfo(id);
	
			file=this.file(list);
	
		
		if(null!=file){
			String filename=info.getCusOrg()+".zip";
			response.setHeader("content-disposition", "attachment;filename=" +new String(filename.getBytes("utf-8"), "ISO8859-1"));  
		 inp=new FileInputStream(file);
		out=response.getOutputStream();
		IOUtils.copy(inp, out);
		out.flush();
		}
		
		
		
		}catch(Exception e){
			
			throw e;
		}finally {
			IOUtils.closeQuietly(inp);
			IOUtils.closeQuietly(out);
		}
	
		
	}
	

	@RequestMapping("/updatedetail")
	@ResponseBody
	public Object updatedetail(@RequestBody Map<String, Object> requstMap)throws Exception{
		EayunResponseJson resp=new EayunResponseJson();
		log.info("开始修改备案");
		try{
			BaseUnitInfo model=new BaseUnitInfo();
			BeanUtils.mapToBean(model,(Map<String,Object>)requstMap.get("model"));
			BaseUnitInfo info=ecmcRecordService.updatedetail(model);
			if(model.getRecordNo().equals(info.getRecordNo())){
				resp.setRespCode(ConstantClazz.SUCCESS_CODE);
			}else{
				resp.setRespCode(ConstantClazz.ERROR_CODE);
			}
		
		}catch(Exception e){
			resp.setRespCode(ConstantClazz.ERROR_CODE);
			throw e;	
		}
		return resp;
		
	}
	
	
	

	@RequestMapping("/updatedetailbyweb")
	@ResponseBody
	public Object updatedetailweb(@RequestBody Map<String, Object> requstMap)throws Exception{
		EayunResponseJson resp=new EayunResponseJson();
		log.info("开始修改备案");
		Boolean fag =null;
		try{
			UnitWebSVOE model=new UnitWebSVOE();
			BeanUtils.mapToBean(model,(Map<String,Object>)requstMap.get("model"));
			fag=ecmcRecordService.updatedetail(model);
			if(fag){
				resp.setRespCode(ConstantClazz.SUCCESS_CODE);
			}else{
				resp.setRespCode(ConstantClazz.ERROR_CODE);
			}
		
		}catch(Exception e){
			resp.setRespCode(ConstantClazz.ERROR_CODE);
			throw e;	
		}
		return resp;
		
	}
	
	@RequestMapping("/exportNewRecordExcel")
    public String exportNewRecordExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("导出首次备案信息数据");
        String time = DateUtil.dateToStr(new Date());
        OutputStream out = null;
        BufferedWriter bw = null;
        OutputStreamWriter osw=null;
        try {
            List<NewRecordExcel> list = ecmcRecordService.getNewRecordExcel(request.getParameter("applyId"));//获取数据
            if(list==null || list.size()<=0){
                throw new AppException("找不到该用户的备案信息");
            }
            response.setContentType("text/plain");
            NewRecordExcel newRecord = list.get(0);
            String name = newRecord.getRecordCusName()+"_首次备案_"+time+".csv";
            if("Firefox".equals(request.getParameter("browser"))){
                name = new String(name.getBytes(), "ISO-8859-1");
            }else{
                name = URLEncoder.encode(name, "UTF-8") ;
            }
            response.addHeader("Content-Disposition", "attachment;filename=" + name);

            out = response.getOutputStream();
            osw = new OutputStreamWriter(out);
            bw =new BufferedWriter(osw);
            if(list!=null && !list.isEmpty()){
                Field[] fields = newRecord.getClass().getDeclaredFields();
                for(Field field : fields){
                    field.setAccessible(true);
                    ExcelTitle title = field.getAnnotation(ExcelTitle.class);
                    if (title == null || title.name() == null || "".equals(title.name())) {
                        bw.append(field.getName()+",");
                    } else {
                        bw.append(title.name()+",");
                    }
                }
                bw.append("\n");
                for(NewRecordExcel data : list){
                    bw.append(" ,");
                    bw.append(" ,");
                    bw.append(" ,");
                    bw.append(data.getRecordId()+",");
                    bw.append(data.getHeadName()+",");
                    bw.append(data.getUnitName()+",");
                    bw.append(data.getUnitNature()+",");
                    bw.append(data.getProvince()+",");
                    bw.append(data.getCity()+",");
                    bw.append(data.getCounty()+",");
                    bw.append(data.getUnitAddress()+",");
                    bw.append(data.getCertificateType()+",");
                    bw.append(data.getCertificateNo()+",");
                    bw.append(data.getCertificateAddress()+",");
                    bw.append(data.getRecordWay()+",");
                    bw.append((data.getRemark()==null?"":data.getRemark())+",");
                    bw.append(data.getDutyName()+",");
                    bw.append(data.getPhone()+",");
                    bw.append(data.getDutyPhone()+",");
                    bw.append(data.getDutyEmail()+",");
                    bw.append(data.getMsn()+",");
                    bw.append((data.getQq()==null?"":data.getQq())+",");
                    bw.append(data.getDutyCertificateType()+",");
                    bw.append(data.getDutyCertificateNo()+",");
                    
                    bw.append(data.getWebName()+",");
                    bw.append((data.getWebRecordNo()==null?"":data.getWebRecordNo())+",");
                    bw.append(data.getWebId()+",");
                    bw.append(data.getServiceContent()+",");
                    bw.append(data.getWebLanguage()+",");
                    bw.append(data.getDomainUrl()+",");
                    bw.append(data.getDomainName()+",");
                    bw.append((data.getWebRemark()==null?"":data.getWebRemark())+",");
                    bw.append(data.getWebSpecial()+",");
                    bw.append(data.getSpecialNo()+",");
                    bw.append(data.getSpecialFile()+",");
                    bw.append(data.getWebDutyName()+",");
                    bw.append(data.getWebPhone()+",");
                    bw.append(data.getWebDutyPhone()+",");
                    bw.append(data.getWebDutyEmail()+",");
                    bw.append(data.getWebMSN()+",");
                    bw.append((data.getWebQQ()==null?"":data.getWebQQ())+",");
                    bw.append(data.getWebDutyCertificateType()+",");
                    bw.append(data.getWebDutyCertificateNo()+",");
                    bw.append(data.getWebAddress()+",");
                    bw.append(data.getAccessType()+",");
                    bw.append(data.getIPPeriod());
                    bw.append("\n");
                }
            }
            ecmclogservice.addLog("导出数据", "备案","首次备案", null, 1,request.getParameter("applyId"), null);
            out.flush();
        } catch (Exception e) {
            log.error("导出首次备案信息数据excel失败", e);
            ecmclogservice.addLog("导出数据", "备案","首次备案", null, 0,request.getParameter("applyId"), e);
            throw e;
        } finally{
            if(bw!=null){
                try {
                    bw.close();
                    bw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(osw!=null){
                try {
                    osw.close();
                    osw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(out!=null){
                try {
                    out.close();
                    out=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
        }
        return null;
    }
	@RequestMapping("/exportNewAccressExcel")
    public String exportNewAccressExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("导出新增接入备案信息数据");
        String time = DateUtil.dateToStr(new Date());
        OutputStream out = null;
        BufferedWriter bw = null;
        OutputStreamWriter osw=null;
        try {
            List<NewAccessExcel> list = ecmcRecordService.getNewAccessExcel(request.getParameter("applyId"));//获取数据
            if(list==null || list.size()<=0){
                throw new AppException("找不到该用户的备案信息");
            }
            response.setContentType("text/plain");
            NewAccessExcel newRecord = list.get(0);
            String name = newRecord.getRecordCusName()+"_新增接入_"+time+".csv";
            if("Firefox".equals(request.getParameter("browser"))){
                name = new String(name.getBytes(), "ISO-8859-1");
            }else{
                name = URLEncoder.encode(name, "UTF-8") ;
            }
            response.addHeader("Content-Disposition", "attachment;filename=" + name);

            out = response.getOutputStream();
            osw = new OutputStreamWriter(out);
            bw =new BufferedWriter(osw);
            if(list!=null && !list.isEmpty()){
                Field[] fields = newRecord.getClass().getDeclaredFields();
                for(Field field : fields){
                    field.setAccessible(true);
                    ExcelTitle title = field.getAnnotation(ExcelTitle.class);
                    if (title == null || title.name() == null || "".equals(title.name())) {
                        bw.append(field.getName()+",");
                    } else {
                        bw.append(title.name()+",");
                    }
                }
                bw.append("\n");
                for(NewAccessExcel data : list){
                    bw.append((data.getPassword()==null?"":data.getPassword())+",");
                    bw.append((data.getWebRecordNo()==null?"":data.getWebRecordNo())+",");
                    bw.append(" ,");
                    bw.append("admin,");
                    bw.append(data.getWebAddress()+",");
                    bw.append(data.getAccessType()+",");
                    bw.append(data.getIPPeriod());
                    bw.append("\n");
                }
            }
            ecmclogservice.addLog("导出数据", "备案","新增接入", null, 1,request.getParameter("applyId"), null);
            out.flush();
        } catch (Exception e) {
            log.error("导出新增接入备案信息数据excel失败", e);
            ecmclogservice.addLog("导出数据", "备案","新增接入", null, 0,request.getParameter("applyId"), e);
            throw e;
        } finally{
            if(bw!=null){
                try {
                    bw.close();
                    bw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(osw!=null){
                try {
                    osw.close();
                    osw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(out!=null){
                try {
                    out.close();
                    out=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
        }
        return null;
    }
	@RequestMapping("/exportNewWebExcel")
    public String exportNewWebExcel(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("导出新增网站备案信息数据");
        String time = DateUtil.dateToStr(new Date());
        OutputStream out = null;
        BufferedWriter bw = null;
        OutputStreamWriter osw=null;
        try {
            List<NewWebExcel> list = ecmcRecordService.getNewWebExcel(request.getParameter("applyId"));//获取数据
            if(list==null || list.size()<=0){
                throw new AppException("找不到该用户的备案信息");
            }
            response.setContentType("text/plain");
            NewWebExcel newRecord = list.get(0);
            String name = newRecord.getRecordCusName()+"_新增网站_"+time+".csv";
            if("Firefox".equals(request.getParameter("browser"))){
                name = new String(name.getBytes(), "ISO-8859-1");
            }else{
                name = URLEncoder.encode(name, "UTF-8") ;
            }
            response.addHeader("Content-Disposition", "attachment;filename=" + name);
            
            out = response.getOutputStream();
            osw = new OutputStreamWriter(out);
            bw =new BufferedWriter(osw);
            if(list!=null && !list.isEmpty()){
                Field[] fields = newRecord.getClass().getDeclaredFields();
                for(Field field : fields){
                    field.setAccessible(true);
                    ExcelTitle title = field.getAnnotation(ExcelTitle.class);
                    if (title == null || title.name() == null || "".equals(title.name())) {
                        bw.append(field.getName()+",");
                    } else {
                        bw.append(title.name()+",");
                    }
                }
                bw.append("\n");
                for(NewWebExcel data : list){
                    bw.append((data.getPassword()==null?"":data.getPassword())+",");
                    bw.append(data.getStatus()+",");
                    bw.append("admin,");
                    bw.append(data.getRecordNo()+",");
                    bw.append(data.getWebName()+",");
                    bw.append((data.getWebRecordNo()==null?"":data.getWebRecordNo())+",");
                    bw.append(data.getWebId()+",");
                    bw.append(data.getServiceContent()+",");
                    bw.append(data.getWebLanguage()+",");
                    bw.append(data.getDomainUrl()+",");
                    bw.append(data.getDomainName()+",");
                    bw.append((data.getWebRemark()==null?"":data.getWebRemark())+",");
                    bw.append(data.getWebSpecial()+",");
                    bw.append(data.getSpecialNo()+",");
                    bw.append(data.getSpecialFile()+",");
                    bw.append(data.getWebDutyName()+",");
                    bw.append(data.getWebPhone()+",");
                    bw.append(data.getWebDutyPhone()+",");
                    bw.append(data.getWebDutyEmail()+",");
                    bw.append(data.getWebMSN()+",");
                    bw.append((data.getWebQQ()==null?"":data.getWebQQ())+",");
                    bw.append(data.getWebDutyCertificateType()+",");
                    bw.append(data.getWebDutyCertificateNo()+",");
                    bw.append(data.getWebAddress()+",");
                    bw.append(data.getAccessType()+",");
                    bw.append(data.getIPPeriod());
                    bw.append("\n");
                }
            }
            ecmclogservice.addLog("导出数据", "备案","新增网站", null, 1,request.getParameter("applyId"), null);
            out.flush();
        } catch (Exception e) {
            log.error("导出新增网站备案信息数据excel失败", e);
            ecmclogservice.addLog("导出数据", "备案","新增网站", null, 0,request.getParameter("applyId"), e);
            throw e;
        } finally{
            if(bw!=null){
                try {
                    bw.close();
                    bw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(osw!=null){
                try {
                    osw.close();
                    osw=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
            if(out!=null){
                try {
                    out.close();
                    out=null;
                } catch (IOException e) {
                    e.printStackTrace();
                } 
            }
        }
        return null;
    }
   
}
