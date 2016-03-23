package com.soft.crawler.plugin.util;


import com.soft.crawler.fetcher.FetchRequest;
import com.soft.crawler.fetcher.FetchResult;
import com.soft.crawler.xml.Site;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-4-9 上午10:16:16
 */
public class HttpUtil {
	
	public static FetchResult fetch(FetchRequest req) throws Exception{
		return fetch(req, null);
	}
	
	public static FetchResult fetch(FetchRequest req, SpiderConfig config) throws Exception{
		PageFetcherImpl fetcher = new PageFetcherImpl();
		if (config == null) {
			config = new SpiderConfig();
			config.setCharset("utf-8");
			config.setIncludeHttpsPages(true);
		}
		fetcher.setConfig(config);
		Site site = new Site();
		fetcher.init(site);
		
		return fetcher.fetch(req);
	}
	
}
