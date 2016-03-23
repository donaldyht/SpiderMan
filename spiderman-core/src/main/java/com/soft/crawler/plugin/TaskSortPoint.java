package com.soft.crawler.plugin;

import java.util.Collection;

import com.soft.crawler.task.Task;


public interface TaskSortPoint extends Point{

	Collection<Task> sortTasks(Collection<Task> tasks) throws Exception;
	
}
