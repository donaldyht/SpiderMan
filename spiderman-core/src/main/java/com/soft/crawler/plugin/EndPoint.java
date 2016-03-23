package com.soft.crawler.plugin;

import java.util.List;
import java.util.Map;

import com.soft.crawler.task.Task;


public interface EndPoint extends Point{

//	void context(Task task, List<Map<String, Object>> models) throws Exception;
	
	List<Map<String, Object>> complete(Task task, List<Map<String, Object>> models) throws Exception;

}
