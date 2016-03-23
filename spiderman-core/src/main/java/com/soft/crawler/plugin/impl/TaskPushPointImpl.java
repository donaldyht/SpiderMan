package com.soft.crawler.plugin.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.soft.crawler.plugin.TaskPushPoint;
import com.soft.crawler.plugin.util.CommonUtil;
import com.soft.crawler.task.Task;
import com.soft.crawler.task.TaskQueue;
import com.soft.crawler.xml.ValidHost;
import com.soft.crawler.xml.ValidHosts;

public class TaskPushPointImpl implements TaskPushPoint{
	Logger logger=Logger.getLogger(TaskPushPointImpl.class); 
	
	public synchronized Collection<Task> pushTask(Collection<Task> validTasks) throws Exception{
		Collection<Task> newTasks = new ArrayList<Task>();
		for (Task task : validTasks){
			try{
				//如果不是在给定的合法host列表里则不给于抓取
				ValidHosts vhs = task.site.getValidHosts();
				if (vhs == null || vhs.getValidHost() == null || vhs.getValidHost().isEmpty()){
					if (!CommonUtil.isSameHost(task.site.getUrl(), task.url)) {
						continue;
					}
				}else{
					boolean isOk = false;
					String taskHost = new URL(task.url).getHost();
					for (ValidHost h : vhs.getValidHost()){
						if (taskHost.equals(h.getValue())){
							isOk = true;
							break;
						}
					}
					
					if (!isOk)
						continue;
				}
				
				
				
				//如果是有效的，或者是不严格的规则那么任务都可以进入队列
					boolean isOk = TaskQueue.pushTask(task);
					if (isOk){
						newTasks.add(task);
						logger.info("push task->"+task+" push the queue ... result -> " + isOk);
					}
					
			}catch(Exception e){
				continue;
			}
		}
		
		return newTasks;
	}
	
}
