package com.soft.crawler.plugin.impl;

import java.util.ArrayList;
import java.util.Collection;


import com.soft.crawler.plugin.DupRemovalPoint;
import com.soft.crawler.plugin.util.Util;
import com.soft.crawler.task.Task;
import com.soft.crawler.url.SourceUrlChecker;
import com.soft.crawler.xml.Rule;
import com.soft.crawler.xml.Target;



public class DupRemovalPointImpl implements DupRemovalPoint {

	public synchronized Collection<Task> removeDuplicateTask(Task task, Collection<String> newUrls, Collection<Task> tasks) {

		Collection<Task> validTasks = new ArrayList<Task>();
		for (String url : newUrls) {
			Task newTask = new Task(url, null, task.url, task.site, 10);
			try {
				Target tgt = Util.matchTarget(newTask);
				Rule fromSourceRule = SourceUrlChecker.checkSourceUrl( task.site.getTargets().getSourceRules(), newTask.sourceUrl);
				// 如果是目标url，但不是来自来源url，跳过
				if (tgt != null && fromSourceRule == null) {
					continue;
				}

				validTasks.add(newTask);
			} catch (Exception e) {
			}
		}

		return validTasks;
	}

}
