package com.soft.crawler.plugin;

import java.util.List;
import java.util.Map;

import com.soft.crawler.task.Task;

/**
 * TODO
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-2 下午07:01:00
 */
public interface PojoPoint extends Point{

	List<Object> mapping(Task task, Class<?> mappingClass, List<Map<String, Object>> models, List<Object> pojo);

}
