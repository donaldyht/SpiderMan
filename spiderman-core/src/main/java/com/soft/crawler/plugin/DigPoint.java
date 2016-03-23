package com.soft.crawler.plugin;

import java.util.Collection;


import com.soft.crawler.fetcher.FetchResult;
import com.soft.crawler.task.Task;


public interface DigPoint extends Point{

//	void context(FetchResult result, Task task) throws Exception;
	
	Collection<String> digNewUrls(FetchResult result, Task task, Collection<String> urls) throws Exception;

}
