package com.soft.crawler.xml;

import com.soft.crawler.plugin.util.xml.AttrTag;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-3-8 上午11:18:09
 */
public class ValidHost {

	@AttrTag
	private String value;

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "ValidHost [value=" + this.value + "]";
	}
	
}
