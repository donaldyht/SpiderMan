package com.soft.crawler.xml;

import com.soft.crawler.plugin.util.xml.AttrTag;

/**
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-7 下午08:10:09
 */
public class Header {

	@AttrTag
	private String name;
	
	@AttrTag
	private String value;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
