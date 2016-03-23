package com.soft.crawler.plugin;


import com.soft.crawler.task.Task;
import com.soft.crawler.xml.Target;


public interface TargetPoint extends Point{

//	void context(Task task) throws Exception;
	Target confirmTarget(Task task, Target target) throws Exception;

}
