package com.soft.crawler.plugin.impl;

import java.net.URL;


import com.soft.crawler.plugin.BeginPoint;
import com.soft.crawler.plugin.util.CommonUtil;
import com.soft.crawler.task.Task;
import com.soft.crawler.xml.ValidHost;
import com.soft.crawler.xml.ValidHosts;

public class BeginPointImpl implements BeginPoint{

	
	public Task confirmTask(Task task) throws Exception{
		//如果不是在给定的合法host列表里则不给于抓取
		ValidHosts vhs = task.site.getValidHosts();
//		System.out.println("*************************************");
//		System.out.println(vhs);
//		System.out.println("*************************************");
		if (vhs == null || vhs.getValidHost() == null || vhs.getValidHost().isEmpty()){
			if (!CommonUtil.isSameHost(task.site.getUrl(), task.url)) {
				return null;
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
				return null;
		}
		
		return task;
	}

}
