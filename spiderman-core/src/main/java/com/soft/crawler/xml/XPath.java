package com.soft.crawler.xml;

import com.soft.crawler.plugin.util.xml.AttrTag;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-9 下午12:40:57
 */
public class XPath {
	@AttrTag
	private String attr;
	
	@AttrTag
	private String value;

	public String getAttr() {
		return this.attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
