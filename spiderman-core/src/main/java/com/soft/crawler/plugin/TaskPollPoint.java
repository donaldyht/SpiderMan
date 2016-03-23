package com.soft.crawler.plugin;

import com.soft.crawler.task.Task;


public interface TaskPollPoint extends Point{

	Task pollTask() throws Exception;

}
