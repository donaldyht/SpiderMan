package com.soft.crawler.url;

import java.util.List;


import com.soft.crawler.plugin.util.Http;
import com.soft.crawler.xml.Rule;
import com.soft.crawler.xml.Rules;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-2-28 下午08:34:54
 */
public class SourceUrlChecker {
	public static Rule checkSourceUrl(Rules rules, String sourceUrl) {
		Rule defaultRule = new Rule();
		defaultRule.setHttpMethod(Http.Method.GET);
		if (sourceUrl != null) {
			// 判断下当前Target的sourceURL是否是我们要的来源URL
			if (rules == null)
				return defaultRule;
			else {
				List<Rule> ruleList = rules.getRule();
				if (ruleList == null || ruleList.isEmpty())
					return defaultRule;
				else {
					return UrlRuleChecker.check(sourceUrl, ruleList, rules.getPolicy());
				}
			}
		}

		return defaultRule;
	}
}
