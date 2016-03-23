package com.soft.crawler.plugin.impl;


import com.soft.crawler.plugin.TargetPoint;
import com.soft.crawler.plugin.util.Util;
import com.soft.crawler.task.Task;
import com.soft.crawler.xml.Target;

public class TargetPointImpl implements TargetPoint{

	
	public synchronized Target confirmTarget(Task task, Target target) throws Exception {
		return Util.matchTarget(task);
	}

}
