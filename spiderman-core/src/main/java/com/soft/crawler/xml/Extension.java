package com.soft.crawler.xml;

import java.util.ArrayList;
import java.util.List;

import com.soft.crawler.plugin.util.xml.AttrTag;

public class Extension {
	@AttrTag
	private String point;
	
	private List<Impl> impl = new ArrayList<Impl>();
	
	public String getPoint() {
		return point;
	}
	public void setPoint(String point) {
		this.point = point;
	}
	public List<Impl> getImpl() {
		return impl;
	}
	public void setImpl(List<Impl> impl) {
		this.impl = impl;
	}
	
}
