package com.soft.crawler.plugin.util;

import java.io.Serializable;

public class PicsModel implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String extName = null;
	private int width = 0;
	private int height = 0;
	private String color = null;
	private String size = null;

	public PicsModel() {
	}

	public String getExtName() {
		return this.extName;
	}

	public void setExtName(String extName) {
		this.extName = extName;
	}

	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getColor() {
		return this.color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getSize() {
		return this.size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}