package com.soft.crawler.xml;

import com.soft.crawler.plugin.util.xml.AttrTag;

/**
 * 要抓取的目标 
 * @author weiwei l.weiwei@163.com
 * @date 2013-2-28 上午11:56:27
 */
public class Target {

	/**
	 * 目标名
	 */
	@AttrTag
	private String name;
	
	/**
	 * 目标页面的url规则
	 */
	private Rules urlRules ;
	
	/**
	 * 目标的数据模型
	 */
	private Model model;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Rules getUrlRules() {
		return this.urlRules;
	}

	public void setUrlRules(Rules urlRules) {
		this.urlRules = urlRules;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
