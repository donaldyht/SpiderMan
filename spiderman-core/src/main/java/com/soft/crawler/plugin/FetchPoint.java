package com.soft.crawler.plugin;


import com.soft.crawler.fetcher.FetchResult;
import com.soft.crawler.task.Task;


public interface FetchPoint extends Point{

//	void context(Task task) throws Exception;
	
	FetchResult fetch(Task task, FetchResult result) throws Exception;

}
