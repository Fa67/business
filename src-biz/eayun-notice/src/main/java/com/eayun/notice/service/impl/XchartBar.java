package com.eayun.notice.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.knowm.xchart.BitmapEncoder;
//import org.jCharts.Chart;
//import org.jCharts.encoders.JPEGEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.DateUtil;
import com.eayun.file.service.FileService;
import com.eayun.notice.service.ExampleChart;


@Service
@Transactional
public class XchartBar implements ExampleChart {
	
	@Autowired
	public FileService fileservice;
	

	  public  String setBar(String[] name,Integer[] value) {
		  String fileId="";
		File tempFile = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString() + ".jpg");
		System.out.println(tempFile.getPath());
	    ExampleChart exampleChart = new XchartBar();
	    CategoryChart chart = exampleChart.getChart( name, value);
	    //FileOutputStream fos = new FileOutputStream("D:\\3.jpg");    
	    try {
			BitmapEncoder.saveJPGWithQuality(chart, tempFile.getPath(), 1.0f);
			fileId=this.uploadFile(tempFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   return fileId;
	  }

	  public CategoryChart getChart(String[] name,Integer[] value) {

	    // Create Chart
	    CategoryChart chart = new CategoryChartBuilder().width(800).height(600).title("Score Histogram").xAxisTitle("Score").yAxisTitle("Number").build();

	    // Customize Chart
	    chart.getStyler().setLegendPosition(LegendPosition.InsideNW);
	    chart.getStyler().setHasAnnotations(true);

	    // Series
	    chart.addSeries("test 1", Arrays.asList(name), Arrays.asList(value));

	    return chart;
	  }

	@Override
	public String uploadFile(File file) {

		String fileId = "";
		try {

			fileId = fileservice.uploadFile(file, file.getName(), "jpg", "OperateDataSendMailJob");
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		return fileId;
	}
	

}
