package com.soft.crawler.plugin;

import java.util.Collection;

import com.soft.crawler.task.Task;


public interface TaskPushPoint extends Point{
	
	public Collection<Task> pushTask(Collection<Task> tasks) throws Exception;
	
}
