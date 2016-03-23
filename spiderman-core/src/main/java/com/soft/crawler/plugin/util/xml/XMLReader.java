package com.soft.crawler.plugin.util.xml;


public interface XMLReader {

	<T> T readOne() throws Exception;

	public void setClass(String key, Class<?> clazz);
	
	public void setClass(Class<?> clazz);
	
	public void setBeanName(String beanName);
	
	public void setRootElementName(String name);
}
