package com.soft.crawler.task;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;


import com.soft.crawler.url.UrlRuleChecker;
import com.soft.crawler.xml.Rule;
import com.soft.crawler.xml.Rules;

/**
 * 任务队列，阻塞+优先级排序
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-15 上午10:53:24
 */
public class TaskQueue {
	
	public void init(){
	}
	
	private static PriorityBlockingQueue<Task> queue = new PriorityBlockingQueue<Task>(5000, new Comparator<Task>(){
		public int compare(Task t1, Task t2) {
			if (t1.sort == t2.sort) return 0;
			return t1.sort > t2.sort ? 1 : -1;
		}
	});
	
	
	public static synchronized Task pollTask() throws Exception{
		return queue.poll();
	}
	
	/**
	 * 将任务压入队列，注意，在此之前要保证任务不重复
	 * @date 2013-1-15 上午10:57:47
	 * @param task
	 * @return
	 */
	public static synchronized boolean pushTask(Task task){
		
		if (task == null)
			return false;
		
		if (null == task.url || task.url.trim().length() == 0)
			return false;
		
		//检查是否匹配xml配置的url规则
		Rules rules = task.site.getQueueRules();
		Rule queueRule = UrlRuleChecker.check(task.url, rules.getRule(), rules.getPolicy()); 
		if (queueRule == null)
			return false;
		
		return queue.add(task);
	}
	
	public static synchronized void stop(){
		queue.clear();
		
	}
	
	public static int getSize(){
		return queue.size();
	}
}
