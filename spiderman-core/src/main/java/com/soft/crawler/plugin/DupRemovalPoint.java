package com.soft.crawler.plugin;

import java.util.Collection;

import com.soft.crawler.task.Task;


public interface DupRemovalPoint extends Point{
	
	Collection<Task> removeDuplicateTask(Task task, Collection<String> newUrls, Collection<Task> tasks);
}
