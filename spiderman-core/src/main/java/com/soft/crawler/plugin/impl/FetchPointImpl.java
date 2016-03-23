package com.soft.crawler.plugin.impl;

import org.apache.log4j.Logger;

import com.soft.crawler.fetcher.FetchRequest;
import com.soft.crawler.fetcher.FetchResult;
import com.soft.crawler.plugin.FetchPoint;
import com.soft.crawler.plugin.util.CommonUtil;
import com.soft.crawler.plugin.util.Http;
import com.soft.crawler.plugin.util.PageFetcherImpl;
import com.soft.crawler.plugin.util.SpiderConfig;
import com.soft.crawler.spider.Spider;
import com.soft.crawler.task.Task;


public class FetchPointImpl implements FetchPoint{
	Logger logger=Logger.getLogger(FetchPointImpl.class); 

	public FetchResult fetch(Task task, FetchResult result) throws Exception {
			
				PageFetcherImpl fetcher = new PageFetcherImpl();
				SpiderConfig config = new SpiderConfig();
				//config.setUserAgentString(User_Agent_List.getRadomUserAgent());
				if (task.site.getCharset() != null && task.site.getCharset().trim().length() > 0)
					config.setCharset(task.site.getCharset());
				if ("1".equals(task.site.getIncludeHttps()) || "true".equals(task.site.getIncludeHttps()))
					config.setIncludeHttpsPages(true);
				String sdelay = task.site.getReqDelay();
				if (sdelay == null || sdelay.trim().length() == 0)
					sdelay = "200";
				
				int delay = CommonUtil.toSeconds(sdelay).intValue()*1000;
				if (delay < 0)
					delay = 200;
				
				config.setPolitenessDelay(delay);
				
				String timeout = task.site.getTimeout();
				if (timeout != null && timeout.trim().length() > 0){
					int to = CommonUtil.toSeconds(sdelay).intValue()*1000;
					if (to > 0)
						config.setConnectionTimeout(to);
				}
				
				fetcher.setConfig(config);
				fetcher.init(task.site);
				
			
			String url = task.url.replace(" ", "%20");
			
			FetchRequest req = new FetchRequest();
			req.setUrl(url);
			//设置请求方式 get post
			if (CommonUtil.isSamePath(task.site.getUrl(), task.url)) {
				req.setHttpMethod(task.site.getHttpMethod());
			}else{
				req.setHttpMethod(Http.Method.GET);
			}
			
			return fetcher.fetch(req);
	}
	
	
	public FetchResult fetch(Task task,FetchRequest req) throws Exception {
		
		PageFetcherImpl fetcher = new PageFetcherImpl();
		SpiderConfig config = new SpiderConfig();
		//config.setUserAgentString(User_Agent_List.getRadomUserAgent());
		if (task.site.getCharset() != null && task.site.getCharset().trim().length() > 0)
			config.setCharset(task.site.getCharset());
		if ("1".equals(task.site.getIncludeHttps()) || "true".equals(task.site.getIncludeHttps()))
			config.setIncludeHttpsPages(true);
		String sdelay = task.site.getReqDelay();
		if (sdelay == null || sdelay.trim().length() == 0)
			sdelay = "200";
		
		int delay = CommonUtil.toSeconds(sdelay).intValue()*1000;
		if (delay < 0)
			delay = 200;
		
		config.setPolitenessDelay(delay);
		
		String timeout = task.site.getTimeout();
		if (timeout != null && timeout.trim().length() > 0){
			int to = CommonUtil.toSeconds(sdelay).intValue()*1000;
			if (to > 0)
				config.setConnectionTimeout(to);
		}
		
		fetcher.setConfig(config);
		fetcher.init(task.site);
		//设置请求方式 get post
		if (CommonUtil.isSamePath(task.site.getUrl(), task.url)) {
			req.setHttpMethod(task.site.getHttpMethod());
		}else{
			req.setHttpMethod(Http.Method.GET);
		}
	return fetcher.fetch(req);
}
}
