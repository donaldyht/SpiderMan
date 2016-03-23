package com.soft.crawler.xml;

import com.soft.crawler.plugin.util.xml.AttrTag;


public class Site {

	@AttrTag
	private String name;//网站名
	
	@AttrTag
	private String country;//网站所属国家
	
	private ValidHosts validHosts;//限制在这些host里面抓取数据
	
	private Options options;//一些其他的业务数据
	
	@AttrTag
	private String url;//网站url
	
	@AttrTag
	private String httpMethod;
	
	@AttrTag
	private String userAgent = "";//爬虫一些标识
	
	@AttrTag
	private String includeHttps; //是否抓取https页
	
	
	@AttrTag
	private String timeout;//HTTP请求最大等待时间
	
	@AttrTag
	private String reqDelay = "200";//每个请求的延迟时间
	
	@AttrTag 
	private String charset;//网站内容字符集
	
	private Seeds seeds ;
	
	private Headers headers = new Headers();//HTTP头
	
	private Cookies cookies = new Cookies();//HTTP Cookie
	
	private Rules queueRules;//允许进入抓取队列的url规则
	
	private Targets targets ;//抓取目标

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getTimeout() {
		return this.timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}


	public ValidHosts getValidHosts() {
		return this.validHosts;
	}

	public void setValidHosts(ValidHosts validHosts) {
		this.validHosts = validHosts;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getIncludeHttps() {
		return includeHttps;
	}

	public void setIncludeHttps(String includeHttps) {
		this.includeHttps = includeHttps;
	}

	public String getUserAgent() {
		return this.userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}


	public Options getOptions() {
		return this.options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}


	public String getReqDelay() {
		return this.reqDelay;
	}

	public void setReqDelay(String reqDelay) {
		this.reqDelay = reqDelay;
	}


	public Targets getTargets() {
		return targets;
	}

	public void setTargets(Targets targets) {
		this.targets = targets;
	}


	public Rules getQueueRules() {
		return queueRules;
	}

	public void setQueueRules(Rules queueRules) {
		this.queueRules = queueRules;
	}

	public String getCharset() {
		return this.charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public Seeds getSeeds() {
		return this.seeds;
	}

	public void setSeeds(Seeds seeds) {
		this.seeds = seeds;
	}

	public Headers getHeaders() {
		return this.headers;
	}

	public void setHeaders(Headers headers) {
		this.headers = headers;
	}

	public Cookies getCookies() {
		return this.cookies;
	}

	public void setCookies(Cookies cookies) {
		this.cookies = cookies;
	}
	
	public String getHttpMethod() {
		return this.httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getOption(String name){
		if (options == null)
			return null;
		
		for (Option option: options.getOption()) {
			if (option == null || option.getName() == null || option.getName().trim().length() == 0)
				continue;
			if (!option.getName().equals(name))
				continue;
			
			return option.getValue();
		}
		
		return null;
	}
	
	

	}
