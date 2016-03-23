package com.soft.crawler.spider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;


import com.soft.crawler.fetcher.FetchResult;
import com.soft.crawler.fetcher.Page;
import com.soft.crawler.plugin.DigPoint;
import com.soft.crawler.plugin.DupRemovalPoint;
import com.soft.crawler.plugin.FetchPoint;
import com.soft.crawler.plugin.ParsePoint;
import com.soft.crawler.plugin.TargetPoint;
import com.soft.crawler.plugin.TaskPushPoint;
import com.soft.crawler.plugin.impl.BeginPointImpl;
import com.soft.crawler.plugin.impl.DigPointImpl;
import com.soft.crawler.plugin.impl.DupRemovalPointImpl;
import com.soft.crawler.plugin.impl.FetchPointImpl;
import com.soft.crawler.plugin.impl.ParsePointImpl;
import com.soft.crawler.plugin.impl.TargetPointImpl;
import com.soft.crawler.plugin.impl.TaskPushPointImpl;
import com.soft.crawler.plugin.util.CommonUtil;
import com.soft.crawler.task.Task;
import com.soft.crawler.url.SourceUrlChecker;
import com.soft.crawler.xml.Field;
import com.soft.crawler.xml.Rule;
import com.soft.crawler.xml.Rules;
import com.soft.crawler.xml.Target;


public class Spider {
	Logger logger=Logger.getLogger(Spider.class); 
	public Task task;
	
	public void init(Task task) {
		this.task = task;
	}
	
	public void run() throws Exception {
		try {
			//扩展点：begin 蜘蛛开始
			BeginPointImpl beginPoint=new BeginPointImpl();
			task=beginPoint.confirmTask(task);
			if (task == null) 
				return ;
			
			//扩展点：fetch 获取HTTP内容
			FetchResult result = null;
			FetchPoint fetchPoint = new FetchPointImpl();
			result = fetchPoint.fetch(task, result);
			//logger.info(result.getPage().getContent());
			
			if (result == null) 
				return ;
			
			//扩展点：dig new url 发觉新URL
			Collection<String> newUrls = null;
			DigPoint digPoint = new DigPointImpl();
			newUrls = digPoint.digNewUrls(result, task, newUrls);
			Iterator<String> its = newUrls.iterator();
			handleNewUrls(newUrls);
			Page page = result.getPage();
			//logger.info(page.getContent());
			if (page == null) {
				return ;
			}
			
			
			//扩展点：target 确认是否有目标配置匹配当前URL
			Target target = null;
			TargetPoint targetPoint = new TargetPointImpl();
			target = targetPoint.confirmTarget(task, target);
			if (target == null) {
				return ;
			}
			
			task.target = target;
						
			//检查sourceUrl
			Rules rules = task.site.getTargets().getSourceRules();
			Rule sourceRule = SourceUrlChecker.checkSourceUrl(rules, task.sourceUrl);
			if (sourceRule == null){
				return ;
			}
			
			//扩展点：parse 把已确认好的目标页面解析成为Map对象
			List<Map<String, Object>> models = null;
			ParsePoint parsePoint = new ParsePointImpl();
			models = parsePoint.parse(task, target, page, models);
			if (models == null || models.size()==0) {
				return ;
			}
			
			for (Iterator<Map<String, Object>> _it = models.iterator(); _it.hasNext(); ){
				 Map<String,Object> model = _it.next();
				 for (Iterator<Field> it = target.getModel().getField().iterator(); it.hasNext(); ){
					 Field f = it.next();
					 //去掉那些被定义成 参数 的field
					 if ("1".equals(f.getIsParam()) || "true".equals(f.getIsParam()))
						 model.remove(f.getName());
				 }
				model.put("source_url", task.sourceUrl);
				model.put("task_url", task.url);
			}
			
			//TODO: 数据处理
			onParse(task, models);
			
			if (task.digNewUrls != null && !task.digNewUrls.isEmpty()) {
				Set<String> urls = new HashSet<String>(task.digNewUrls.size());
				for (String s : task.digNewUrls){
					if (s == null || s.trim().length() == 0){
						continue;
					}
					urls.add(s);
				}
				
				if (!urls.isEmpty()) {
					handleNewUrls(urls);
					task.digNewUrls.clear();
					task.digNewUrls = null;
				}
			}
			
			
		}  catch(Exception e){
			//logger.error(e);
			throw e;
		}
	}

	private void handleNewUrls(Collection<String> newUrls) throws Exception {
		if (newUrls != null && !newUrls.isEmpty()){

		}else{
			newUrls = new ArrayList<String>();
		}
	
		//扩展点：dup_removal URL去重,然后变成Task
		Collection<Task> validTasks = null;
		DupRemovalPoint dupRemovalPoint =new DupRemovalPointImpl();
		validTasks = dupRemovalPoint.removeDuplicateTask(task, newUrls, validTasks);
		if (validTasks == null || validTasks.size()==0){
			return ;
		}
	
		//扩展点：task_push 将任务放入队列
		pushTask(validTasks);
	
}

	public Collection<Task> pushTask(Collection<Task> validTasks) throws Exception {
		TaskPushPoint point=new TaskPushPointImpl();
		point.pushTask(validTasks);
		return validTasks;
	}
	
	public void onParse(Task task, List<Map<String, Object>> models) {
		try {
			for (int i = 0; i < models.size(); i++) {
				Map<String, Object> map = models.get(i);
				StringBuilder sb = new StringBuilder();
				for (Iterator<Entry<String,Object>> it = map.entrySet().iterator(); it.hasNext();){
					Entry<String,Object> e = it.next();
					boolean isBlank = false;
					
					if (e.getValue() == null)
						isBlank = true;
					else if (e.getValue() instanceof String && ((String)e.getValue()).trim().length() == 0)
						isBlank = true;
					else if (e.getValue() instanceof List && ((ArrayList<?>)e.getValue()).isEmpty())
						isBlank = true;
					else if (e.getValue() instanceof List && !((ArrayList<?>)e.getValue()).isEmpty()) {
						if (((ArrayList<?>)e.getValue()).size() == 1 && String.valueOf(((ArrayList<?>)e.getValue()).get(0)).trim().length() == 0)
						isBlank = true;
					}
						
					if (isBlank){
						if (sb.length() > 0)
							sb.append("_");
							sb.append(e.getKey());
					}
				}
				
				String content = CommonUtil.toJson(map);
				logger.info(content);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
