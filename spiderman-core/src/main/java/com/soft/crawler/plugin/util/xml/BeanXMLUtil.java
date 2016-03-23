package com.soft.crawler.plugin.util.xml;

/**
 * 
   * @Name: BeanXMLUtil
   * @Description: XmlBean的工具类，读取xml文件的眼睛
   * @Author: spider（作者）
   * @Version: V1.00 （版本号）
   * @Create Date: 2016年2月16日上午10:10:16
   * @Parameters: 
   * @Return:
 */
public class BeanXMLUtil {
	
	public static XMLReader getBeanXMLReader(){
		return new BeanXMLReader();
	}
	
	public static XMLReader getBeanXMLReader(String content) {
		return new BeanXMLReader(content);
	}

}
