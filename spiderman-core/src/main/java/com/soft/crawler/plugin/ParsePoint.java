package com.soft.crawler.plugin;

import java.util.List;
import java.util.Map;


import com.soft.crawler.fetcher.Page;
import com.soft.crawler.task.Task;
import com.soft.crawler.xml.Target;


public interface ParsePoint extends Point{

//	void context(Task task, Target target, Page page) throws Exception;
	
	List<Map<String, Object>> parse(Task task, Target target, Page page, List<Map<String, Object>> models) throws Exception;

}
