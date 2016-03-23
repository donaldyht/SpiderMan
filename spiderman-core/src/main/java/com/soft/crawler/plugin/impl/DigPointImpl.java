package com.soft.crawler.plugin.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.soft.crawler.fetcher.FetchRequest;
import com.soft.crawler.fetcher.FetchResult;
import com.soft.crawler.fetcher.Page;
import com.soft.crawler.plugin.DigPoint;
import com.soft.crawler.plugin.util.DefaultLinkNormalizer;
import com.soft.crawler.plugin.util.LinkNormalizer;
import com.soft.crawler.plugin.util.UrlUtils;
import com.soft.crawler.plugin.util.Util;
import com.soft.crawler.task.Task;
import com.soft.crawler.url.UrlRuleChecker;
import com.soft.crawler.xml.Model;
import com.soft.crawler.xml.Rule;
import com.soft.crawler.xml.Rules;
import com.soft.crawler.xml.Target;

public class DigPointImpl implements DigPoint{

	public Collection<String> digNewUrls(FetchResult result, Task task, Collection<String> urls) throws Exception {
		return this.digNewUrls(result, task);
	}

	private Collection<String> digNewUrls(FetchResult result, Task task) throws Exception{
		if (result == null)
			return null;
		
		Collection<String> urls = new ArrayList<String>();
		
		//如果是30X跳转，则将其跳转的URL作为新的URL加入进来
		String moveUrl = result.getMovedToUrl();
		if (moveUrl != null){
			if (!moveUrl.equals(task.url))
				urls.add(moveUrl);
		}
		
		//如果当前URL里没有任何页面内容，就无需进一步解析内容里的URL了
		if (result.getPage() == null) 
			return urls;
		String html = result.getPage().getContent();
		if (html == null) 
			return urls;
		
		boolean isDig = false;
		Rules rules = task.site.getTargets().getSourceRules();
		if (rules != null && rules.getRule() != null && !rules.getRule().isEmpty()){
			//用来记录分页里已经解析的url
			Set<String> visitedUrls = new HashSet<String>();
			visitedUrls.add(task.url);
			
			for (Rule r : rules.getRule()){
				Model digModel = r.getDigUrls();
				Model nextPage = r.getNextPage();
				if (digModel != null || nextPage != null) {
					//只要有一个Rule定义了digUrls或者nextPage，那么就被认为已经挖掘过了，这样就不会获取所有的URL
					isDig = true;
				}
				
				//判断当前url是否是sourceUrl,只有当前url是sourceUrl时才需要去获取新URL
				Rule sourceRule = UrlRuleChecker.check(task.url, Arrays.asList(r), "and");
				if (sourceRule == null)
					continue;
				
				Map<String, Object> finalFields = new HashMap<String,Object>();
				
				Collection<String> newUrls=null;//将声明移到if语句外，容纳每页的url
				//判断是否定义了digUrls
				if (digModel != null) {
					//设置当前任务的httpMethod
					task.httpMethod = sourceRule.getHttpMethod();
					
					//构造一个目标
					Target tgt = new Target();
					tgt.setName("dig_urls");
					tgt.setModel(digModel);
					newUrls = UrlUtils.digUrls(result.getPage(), task, r, tgt, finalFields);
	//				System.out.println("newUrls->"+newUrls);
	//				System.out.println("from->"+task.url);
					//解析Model获得urls
					urls.addAll(newUrls);
				}
				
				//如果配置了下一页，则进入递归解析
				if (nextPage != null) {
						parseNextPage(r, task, result.getPage(), urls, visitedUrls, finalFields);
				}
			}
			
		}
		
		//如果没有配置任何的digUrls和nextPage,就使用默认的策略，从当前URL里面获取所有URL
		if (!isDig){
			urls.addAll(UrlUtils.findAllUrls(html, task.url));
		}
		
		//修复URL
		String hostUrl = new StringBuilder("http://").append(new URL(task.site.getUrl()).getHost()).append("/").toString();
		List<String> newUrls = new ArrayList<String>(urls.size());
		for (String url : urls) {
			LinkNormalizer ln = new DefaultLinkNormalizer(hostUrl);
			String newUrl = ln.normalize(url);
//			String newUrl = URLCanonicalizer.getCanonicalURL(ln.normalize(url));
			if (newUrl.startsWith("mailto:"))
				continue;
			//去重复
			if (newUrls.contains(newUrl))
				continue;
			
			newUrls.add(newUrl);
		}
		
		return newUrls;
	}
	
	//递归的额关键是 Page
	public void parseNextPage(Rule rule, Task task, Page page, Collection<String> urls, Set<String> visitedUrls, Map<String, Object> finalFields) throws Exception{
//		System.out.println("parse.next->"+page.getUrl());
		Model mdl = rule.getNextPage();
		if (mdl == null)
			return ;

		Target tgt = new Target();
		tgt.setName("dig_urls");
		tgt.setModel(mdl);

		//解析Model获得next URL
//		System.out.println("page--!!!!!!----->"+page.getUrl());
		Collection<String> nextUrls = UrlUtils.digUrls(page, task, rule, tgt,  finalFields);
//		System.out.println("visitedUrls-->>>>>>>>>>>>!!!!!!!!!!!!!!" + visitedUrls);
		System.out.println("\tsource digNextUrl->" + nextUrls + " from->" + page.getUrl());
		if (nextUrls == null || nextUrls.isEmpty())
			return ;
		String nextUrl = new ArrayList<String>(nextUrls).get(0);
		if (nextUrl == null || nextUrl.trim().length() == 0)
			return ;

		if (visitedUrls.contains(nextUrl)){
			return ;
		}

		//解析nextPage,找出里面的目标URL
		Task nextTask = new Task(nextUrl, task.httpMethod, task.url, task.site, 0);

		FetchRequest req = new FetchRequest();
		req.setUrl(nextUrl);
		FetchResult fr =  new FetchPointImpl().fetch(task, req);
		if (fr == null || fr.getPage() == null)
			return ;

		//记录已经访问过该url，下次不要重复访问它
		visitedUrls.add(nextUrl);
		Page nextPageResult = fr.getPage();
		if (nextPageResult.getContent() == null || nextPageResult.getContent().trim().length() == 0)
			return;

		//暂时使用默认的发现新URL的逻辑
		Collection<String> _urls = Util.findAllLinkHref(nextPageResult.getContent(), task.url);
//		System.out.println("NEXTPAGE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!-------- newUrls-------->" + _urls + ", \tfrom->"+nextUrl);
		urls.addAll(_urls);

		//递归
		parseNextPage(rule, nextTask, nextPageResult, urls, visitedUrls, finalFields);
	}
	
}
