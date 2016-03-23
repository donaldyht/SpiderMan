package com.soft.crawler.plugin.impl;

import java.util.List;
import java.util.Map;


import com.soft.crawler.plugin.EndPoint;
import com.soft.crawler.task.Task;

public class EndPointImpl implements EndPoint{
	

	public void destroy() {
	}

	public List<Map<String, Object>> complete(Task task, List<Map<String, Object>> dataMap) throws Exception {
		return dataMap;
	}

}
