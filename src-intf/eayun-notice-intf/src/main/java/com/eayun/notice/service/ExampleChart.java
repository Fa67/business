package com.eayun.notice.service;

import java.io.File;

import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.XYChart;

public interface ExampleChart {
	
	public String setBar(String[] name,Integer[] value);
	
	public CategoryChart getChart(String[] name,Integer[] value);
	
	
	public String uploadFile(File file);
	

}
