package com.soft.crawler.plugin.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.xpath.XPathFactoryImpl;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;
import com.greenpineyu.fel.function.CommonFunction;
import com.greenpineyu.fel.function.Function;
import com.soft.crawler.fetcher.Page;
import com.soft.crawler.plugin.util.xml.Attrs;
import com.soft.crawler.plugin.util.xml.Tags;
import com.soft.crawler.task.Task;
import com.soft.crawler.xml.Field;
import com.soft.crawler.xml.NSMap;
import com.soft.crawler.xml.Namespaces;
import com.soft.crawler.xml.Parsers;
import com.soft.crawler.xml.Target;

public class ModelParser extends DefaultHandler{

	private Task task = null;
	private Target target = null;
	private FelEngine fel = new FelEngineImpl();
	private Map<String, Object> finalFields = null;
	   
	public Map<String, Object> getFinalFields() {
	  return this.finalFields;
	}
	public void setFinalFields(Map<String, Object> finalFields) {
	  this.finalFields = finalFields;
	}
	
	private final static Function fun = new CommonFunction() {
		public String getName() {
			return "$output";
		}

		public Object call(Object[] arguments) {
			Object node = arguments[0];
			boolean keepHeader = false;
			if (arguments.length > 2)
				keepHeader = (Boolean) arguments[1];
			
			return ParserUtil.xml(node, keepHeader);
		}
	};
	
	private void init(Task task, Target target){
		this.task = task;
		this.target = target;
		
    	fel.addFun(fun);
    	Tags $Tags = Tags.me();
    	Attrs $Attrs = Attrs.me();
    	fel.getContext().set("$Tags", $Tags);
    	fel.getContext().set("$Attrs", $Attrs);
    	fel.getContext().set("$Util", CommonUtil.class);
    	fel.getContext().set("$ParserUtil", ParserUtil.class);
		fel.getContext().set("$target", this.target);
		fel.getContext().set("$task_url", this.task.url);
		fel.getContext().set("$source_url", this.task.sourceUrl);
	}
	
	public ModelParser(Task task, Target target) {
		init(task, target);
	}
	
	public List<Map<String, Object>> parse(Page page) throws Exception{
		String contentType = this.target.getModel().getCType();
		if (contentType == null || contentType.trim().length() == 0)
			contentType = page.getContentType();
		if (contentType == null)
			contentType = "text/html";
		boolean isXml = "xml".equalsIgnoreCase(contentType) || contentType.contains("text/xml") || contentType.contains("application/rss+xml") || contentType.contains("application/xml");
		boolean isJson = "json".equalsIgnoreCase(contentType) || contentType.contains("text/json") || contentType.contains("application/json");
		if (isXml)
			return parseXml(page, false);
		else if (isJson){
			
			return parseJson(page);
		} else {
			String isForceUseXmlParser = this.target.getModel().getIsForceUseXmlParser();
			if (!"1".equals(isForceUseXmlParser))
				return parseHtml(page);
			HtmlCleaner cleaner = new HtmlCleaner();
//			cleaner.getProperties().setTreatUnknownTagsAsContent(true);
			cleaner.getProperties().setTreatDeprecatedTagsAsContent(true);
			String isIgCom = this.target.getModel().getIsIgnoreComments();
			if ("1".equals(isIgCom) || "true".equals(isIgCom))
				//忽略注释
				cleaner.getProperties().setOmitComments(true);
			
			TagNode rootNode = cleaner.clean(page.getContent());
			String xml = ParserUtil.xml(rootNode, true);
			page.setContent(xml);
			return parseXml(page, true);
		}
	}

	private List<Map<String, Object>> parseJson(Page page) throws Exception {
		String content = page.getContent();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String isModelArray = target.getModel().getIsArray();
//		String modelJsonPath = target.getModel().getJsonPath();
		final List<Field> fields = target.getModel().getField();
		if ("1".equals(isModelArray) || "true".equals(isModelArray)) {
			List<Map> models = CommonUtil.parseArray(content, Map.class);
			for (Map model : models){
				list.add(parseJsonMap(model, fields));
			}
		}else{
			Map model = CommonUtil.parse(content, Map.class);
			list.add(parseJsonMap(model, fields));
		}
		return list;
	}
	
	private Map<String, Object> parseJsonMap(Map item, final List<Field> fields){
		Map<String, Object> map = new HashMap<String, Object>();
		if (finalFields != null)
			map.putAll(finalFields);
		
		fel.getContext().set("$fields", map);
		for (Field field : fields){
			String key = field.getName();
			String isArray = field.getIsArray();
			String isMergeArray = field.getIsMergeArray();
			String isTrim = field.getIsTrim();
			String isParam = field.getIsParam();
			String isFinal = field.getIsFinal();
			String isForDigNewUrl = field.getIsForDigNewUrl();
			boolean isFinalParam = ("1".equals(isParam) || "true".equals(isParam)) && ("1".equals(isFinal) || "true".equals(isFinal));
			if (isFinalParam && finalFields != null && finalFields.containsKey(key))
				continue;
			
			Parsers parsers = field.getParsers();
			if (parsers == null)
				continue;
			
			List<com.soft.crawler.xml.Parser> parserList = parsers.getParser();
			if (parserList == null || parserList.isEmpty())
				continue;
			
			//field最终解析出来的结果
			List<Object> values = new ArrayList<Object>();
			for (int i = 0; i < parserList.size(); i++) {
				com.soft.crawler.xml.Parser parser = parserList.get(i);
				String skipErr = parser.getSkipErr();
//				String attribute = parser.getAttribute();
				String exp = parser.getExp();
				String regex = parser.getRegex();
				String skipRgxFail = parser.getSkipRgxFail();
				try {
					//第一步获得的是一个List<String>对象，交给下面的步骤进行解析
					List<Object> newValues = new ArrayList<Object>();
					for (Object nodeVal : values){
						newValues.add(nodeVal.toString());
					}
					//正则
					parseByRegex(regex, skipRgxFail, newValues);
					// EXP表达式
					fel.getContext().set("$this", item);
					parseByExp(exp, newValues);
					
					if (!newValues.isEmpty()) {
						values.clear();
						values.addAll(newValues);
					}
				} catch (Throwable e) {
					if ("1".equals(skipErr) || "true".equals(skipErr))
						continue;
					String parserInfo = CommonUtil.toJson(parser);
					String err = "parser->" + parserInfo + " of field->" + key +" failed";
					//listener.onError(Thread.currentThread(), task, err, e);
				}
			}
			
			try {
				if (values.isEmpty()) 
					values.add("");
				
				// 相同 key，若values不为空，继续沿用
				if (map.containsKey(key)){
					//将原来的值插入到前面
					Object obj = map.get(key);
					if (obj instanceof Collection) {
						values.addAll(0, (Collection<?>) obj);
					} else {
						values.add(0, obj);
					}
				}
				
				//数组的话，需要去除空元素
				if (values.size() >= 2){
					List<Object> noRepeatValues = new ArrayList<Object>();
					for (Iterator<Object> it = values.iterator(); it.hasNext(); ){
						Object obj = it.next();
						if (obj instanceof String) {
							if (((String)obj) == null || ((String)obj).trim().length() == 0)
								continue;
						}
						
						noRepeatValues.add(obj);
					}
					values.clear();
					values.addAll(noRepeatValues);
				}
				
				//如果设置了trim
				if ("1".equals(isTrim) || "true".equals(isTrim)) {
					List<String> results = new ArrayList<String>(values.size());
					for (Object obj : values){
						results.add(String.valueOf(obj).trim());
					}
					values.clear();
					values.addAll(results);
				}
				
				//如果是DigNewUrl
				if ("1".equals(isForDigNewUrl) || "true".equals(isForDigNewUrl)) {
					if ("1".equals(isArray)){
						for (Object val : values){
							task.digNewUrls.add(String.valueOf(val));
						}
					}else{
						if (!values.isEmpty())
							task.digNewUrls.add(String.valueOf(values.get(0)));
					}
				}
				
				Object value = null;
				if ("1".equals(isArray) || "true".equals(isArray)){
					List<Object> newValues = new ArrayList<Object>();
					for (Object val : values){
						if (values.size() == 1 && val.getClass().isArray()){
							Object[] newVals = (Object[])val;
							for (Object nv : newVals){
								if (nv == null || String.valueOf(nv).trim().length() == 0)
									continue;
								newValues.add(nv);
							}
						}
					}
					if (!newValues.isEmpty()){
						values.clear();
						values.addAll(newValues);
					}
					value = values;
					if ("1".equals(isMergeArray) || "true".equals(isMergeArray)){
						StringBuilder sb = new StringBuilder();
						for (Object val : values){
							sb.append(String.valueOf(val));
						}
						value = sb.toString();
					}else
						value = values;
				}else{
					if (values.isEmpty())
						value = "";
					else
						value = values.get(0);
				}
				
				if(isFinalParam){
					finalFields.put(key, value);
				}

				//最终完成
				map.put(key, value);
				
			} catch (Throwable e) {
				//listener.onError(Thread.currentThread(), task, "field->"+key+" parse failed cause->"+e.toString(), e);
			}
		}
		
		return map;
	}
	
	private List<Map<String, Object>> parseXml(Page page, boolean isFromHtml) throws Exception{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		String isIgCom = this.target.getModel().getIsIgnoreComments();
		if ("1".equals(isIgCom) || "true".equals(isIgCom))
			//忽略注释
			factory.setIgnoringComments(true);
		//忽略空元素
		factory.setIgnoringElementContentWhitespace(true);
        factory.setNamespaceAware(!isFromHtml); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        String validXml = ParserUtil.checkUnicodeString(page.getContent());
        fel.getContext().set("$page_content", validXml);
    	Document doc = builder.parse(new ByteArrayInputStream(validXml.getBytes("UTF-8")));
        XPathFactory xfactory = XPathFactoryImpl.newInstance();
        XPath xpathParser = xfactory.newXPath();
        //设置命名空间
        xpathParser.setNamespaceContext(new NamespaceContext() {
            public String getPrefix(String uri) {
                throw new UnsupportedOperationException();
            }
            public Iterator<?> getPrefixes(String uri) {
                throw new UnsupportedOperationException();
            }
			public String getNamespaceURI(String prefix) {
				if (prefix == null) 
					throw new NullPointerException("Null prefix");
				else {
		        	Namespaces nss = target.getModel().getNamespaces();
		        	if (nss != null) {
			        	List<NSMap> nsList = nss.getNamespace();
			        	if (nsList != null) {
				        	for (NSMap ns : nsList){
				        		if (prefix.equals(ns.getPrefix()))
				        			return ns.getUri();
				        	}
			        	}
		        	}
		        }
				
				try {
					return "http://www." + new URI(task.site.getUrl()).getHost();
				} catch (URISyntaxException e) {
					return task.site.getUrl();
				}
//		        return XMLConstants.NULL_NS_URI;
			}
		});
        
        final List<Field> fields = target.getModel().getField();
		String isModelArray = target.getModel().getIsArray();
		String modelXpath = target.getModel().getXpath();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if ("1".equals(isModelArray) || "tre".equals(isModelArray)){
			XPathExpression expr = xpathParser.compile(modelXpath);
	        Object result = expr.evaluate(doc, XPathConstants.NODESET);
//		    listener.onInfo(Thread.currentThread(), task, "modelXpath -> " + modelXpath + " parse result -> " + result);
	        if (result != null){
		        NodeList nodes = (NodeList) result;
		        if (nodes.getLength() > 0){
			        for (int i = 0; i < nodes.getLength(); i++) {
						list.add(parseXmlMap(nodes.item(i), xpathParser, fields));
			        }
		        }
	        }
		}else{
			list.add(parseXmlMap(doc, xpathParser, fields));
		}
		return list;
	}
	
	private Map<String, Object> parseXmlMap(Object item, XPath xpathParser, final List<Field> fields) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (finalFields != null)
			map.putAll(finalFields);
		
		fel.getContext().set("$fields", map);
		for (Field field : fields){
			String key = field.getName();
			//是否数组
			String isArray = field.getIsArray();
			//是否合并数组
			String isMergeArray = field.getIsMergeArray();
			String isTrim = field.getIsTrim();
			String isParam = field.getIsParam();
			String isFinal = field.getIsFinal();
			String isForDigNewUrl = field.getIsForDigNewUrl();
			
			boolean isFinalParam = ("1".equals(isParam) || "true".equals(isParam)) && ("1".equals(isFinal) || "true".equals(isFinal));
			if (isFinalParam && finalFields != null && finalFields.containsKey(key))
				continue;
			
			Parsers parsers = field.getParsers();
			if (parsers == null)
				continue;
			
			List<com.soft.crawler.xml.Parser> parserList = parsers.getParser();
			if (parserList == null || parserList.isEmpty())
				continue;
			
			//field最终解析出来的结果
			List<Object> values = new ArrayList<Object>();
			for (int i = 0; i < parserList.size(); i++) {
				com.soft.crawler.xml.Parser parser = parserList.get(i);
				String skipErr = parser.getSkipErr();
				String xpath = parser.getXpath();
				String attribute = parser.getAttribute();
				String exp = parser.getExp();
				String regex = parser.getRegex();
				String skipRgxFail = parser.getSkipRgxFail();
				try {
					if (xpath != null && xpath.trim().length() > 0) {
						
						XPathExpression expr = xpathParser.compile(xpath);
				        Object result = expr.evaluate(item, XPathConstants.NODESET);
				        
						if (result == null)
							continue;
						
						NodeList nodes = (NodeList) result;
						if (nodes.getLength() == 0)
							continue;
						
						if (attribute != null && attribute.trim().length() > 0){
							for (int j = 0; j < nodes.getLength(); j++){
								Node node = nodes.item(j);
								Element e = (Element)node;
								String attrVal = e.getAttribute(attribute);
								values.add(attrVal);
							}
							
							//正则
							parseByRegex(regex, skipRgxFail, values);
							// EXP表达式
							parseByExp(exp, values);
						}else if (xpath.endsWith("/text()")){
							expr = xpathParser.compile(xpath.replace("/text()", ""));
					        result = expr.evaluate(item, XPathConstants.NODESET);
							if (result == null)
								continue;
							
							nodes = (NodeList) result;
							if (nodes.getLength() == 0)
								continue;
							
							for (int j = 0; j < nodes.getLength(); j++){
								Node node = nodes.item(j);
								String nodeValue = node.getTextContent();
								values.add(nodeValue);
							}
							//正则
							parseByRegex(regex, skipRgxFail, values);
							// EXP表达式
							parseByExp(exp, values);
						} else {
							for (int j = 0; j < nodes.getLength(); j++){
								Node node = nodes.item(j);
								values.add(node);
							}
							// 此种方式获取到的Node节点大部分都不是字符串，因此先执行表达式后执行正则
							// EXP表达式
							parseByExp(exp, values);
							//正则
							parseByRegex(regex, skipRgxFail, values);
						}
					}else{
						List<Object> newValues = new ArrayList<Object>(values.size());
						for (Object obj : values){
							newValues.add(obj.toString());
						}
						//正则
						parseByRegex(regex, skipRgxFail, newValues);
						// EXP表达式
						parseByExp(exp, newValues);
						
						if (!newValues.isEmpty()) {
							values.clear();
							values.addAll(newValues);
						}
					}
				} catch (Throwable e) {
					if ("1".equals(skipErr) || "true".equals(skipErr))
						continue;
					//listener.onError(Thread.currentThread(), task, "key->"+key +" parse failed cause->"+e.toString(), e);
				}
			}
			
			try {
				if (values.isEmpty()) 
					values.add("");
				
				// 相同 key，若values不为空，继续沿用
				if (map.containsKey(key)){
					//将原来的值插入到前面
					Object obj = map.get(key);
					if (obj instanceof Collection) {
						values.addAll(0, (Collection<?>) obj);
					} else {
						values.add(0, obj);
					}
				}
				
				//数组的话，需要去除空元素
				if (values.size() >= 2){
					List<Object> noRepeatValues = new ArrayList<Object>();
					for (Iterator<Object> it = values.iterator(); it.hasNext(); ){
						Object obj = it.next();
						if (obj instanceof String) {
							if (((String)obj) == null || ((String)obj).trim().length() == 0)
								continue;
						}
						
						noRepeatValues.add(obj);
					}
					values.clear();
					values.addAll(noRepeatValues);
				}
				
				//如果设置了trim
				if ("1".equals(isTrim) || "true".equals(isTrim)) {
					List<String> results = new ArrayList<String>(values.size());
					for (Object obj : values){
						results.add(String.valueOf(obj).trim());
					}
					values.clear();
					values.addAll(results);
				}
				
				//如果是DigNewUrl
				if ("1".equals(isForDigNewUrl) || "true".equals(isForDigNewUrl)) {
					if ("1".equals(isArray)){
						for (Object val : values){
							task.digNewUrls.add(String.valueOf(val));
						}
					}else{
						if (!values.isEmpty())
							task.digNewUrls.add(String.valueOf(values.get(0)));
					}
				}
				
				Object value = null;
				if ("1".equals(isArray) || "true".equals(isArray)){
					List<Object> newValues = new ArrayList<Object>();
					for (Object val : values){
						if (values.size() == 1 && val.getClass().isArray()){
							Object[] newVals = (Object[])val;
							for (Object nv : newVals){
								if (nv == null || String.valueOf(nv).trim().length() == 0)
									continue;
								newValues.add(nv);
							}
						}
					}
					if (!newValues.isEmpty()){
						values.clear();
						values.addAll(newValues);
					}
					value = values;
					if ("1".equals(isMergeArray) || "true".equals(isMergeArray)){
						StringBuilder sb = new StringBuilder();
						for (Object val : values){
							sb.append(String.valueOf(val));
						}
						value = sb.toString();
					}else
						value = values;
				}else{
					if (values.isEmpty())
						value = "";
					else
						value = values.get(0);
				}
				
				if(isFinalParam){
					finalFields.put(key, value);
				}

				//最终完成
				map.put(key, value);
			} catch (Throwable e) {
				//listener.onError(Thread.currentThread(), task, "field->"+key+" parse failed cause->"+e.toString(), e);
			}
		}
		
		return map;
	}
	
	private List<Map<String, Object>> parseHtml(Page page) throws Exception{
		HtmlCleaner cleaner = new HtmlCleaner();
//		cleaner.getProperties().setTreatUnknownTagsAsContent(true);
		String isIgCom = this.target.getModel().getIsIgnoreComments();
		if ("1".equals(isIgCom) || "true".equals(isIgCom))
			//忽略注释
			cleaner.getProperties().setOmitComments(true);
		cleaner.getProperties().setTreatDeprecatedTagsAsContent(true);
		String html = page.getContent();
		fel.getContext().set("$page_content", html);
		TagNode rootNode = cleaner.clean(html);
        final List<Field> fields = target.getModel().getField();
		String isModelArray = target.getModel().getIsArray();
		String modelXpath = target.getModel().getXpath();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if ("1".equals(isModelArray) || "tre".equals(isModelArray)){
			Object[] nodeVals = rootNode.evaluateXPath(modelXpath);
	        if (nodeVals != null && nodeVals.length > 0){
		        for (int i = 0; i < nodeVals.length; i++) {
					list.add(parseHtmlMap(nodeVals[i], fields));
		        }
	        }
		}else{
			list.add(parseHtmlMap(rootNode, fields));
		}
		
		return list;
	}
	
	private Map<String, Object> parseHtmlMap(Object item, final List<Field> fields){
		Map<String, Object> map = new HashMap<String, Object>();
		if (finalFields != null)
			map.putAll(finalFields);
		
		fel.getContext().set("$fields", map);
		
		for (Field field : fields){
			String key = field.getName();
			String isArray = field.getIsArray();
			String isMergeArray = field.getIsMergeArray();
			String isTrim = field.getIsTrim();
			String isParam = field.getIsParam();
			String isFinal = field.getIsFinal();
			String isForDigNewUrl = field.getIsForDigNewUrl();
			boolean isFinalParam = ("1".equals(isParam) || "true".equals(isParam)) && ("1".equals(isFinal) || "true".equals(isFinal));
			if (isFinalParam && finalFields != null && finalFields.containsKey(key))
				continue;
			
			Parsers parsers = field.getParsers();
			if (parsers == null)
				continue;
			
			List<com.soft.crawler.xml.Parser> parserList = parsers.getParser();
			if (parserList == null || parserList.isEmpty())
				continue;
			
			//field最终解析出来的结果
			List<Object> values = new ArrayList<Object>();
			for (int i = 0; i < parserList.size(); i++) {
				com.soft.crawler.xml.Parser parser = parserList.get(i);
				String skipErr = parser.getSkipErr();
				String xpath = parser.getXpath();
				String attribute = parser.getAttribute();
				String exp = parser.getExp();
				String regex = parser.getRegex();
				String skipRgxFail = parser.getSkipRgxFail();
				try {
					if (xpath != null && xpath.trim().length() > 0) {
						TagNode tag = (TagNode)item;
						Object[] nodeVals = tag.evaluateXPath(xpath);
						if (nodeVals == null || nodeVals.length == 0)
							continue;
						
						if (attribute != null && attribute.trim().length() > 0){
							for (Object nodeVal : nodeVals){
								TagNode node = (TagNode)nodeVal;
								String attrVal = node.getAttributeByName(attribute);
								values.add(attrVal);
							}
							//正则
							parseByRegex(regex, skipRgxFail, values);
							// EXP表达式
							parseByExp(exp, values);
						}else if (xpath.endsWith("/text()")){
							for (Object nodeVal : nodeVals){
								values.add(nodeVal.toString());
							}
							
							//正则
							parseByRegex(regex, skipRgxFail, values);
							
							// EXP表达式
							parseByExp(exp, values);
						}else {
							for (Object nodeVal : nodeVals){
								TagNode node = (TagNode)nodeVal;
								values.add(node);
							}
							
							// 此种方式获取到的Node节点大部分都不是字符串，因此先执行表达式后执行正则
							// EXP表达式
							parseByExp(exp, values);
							
							//正则
							parseByRegex(regex, skipRgxFail, values);
						}
					}else {
						
						//第一步获得的是一个List<String>对象，交给下面的步骤进行解析
						List<Object> newValues = new ArrayList<Object>();
						for (Object nodeVal : values){
							newValues.add(nodeVal.toString());
						}
						//正则
						parseByRegex(regex, skipRgxFail, newValues);
						// EXP表达式
						parseByExp(exp, newValues);
						
						if (!newValues.isEmpty()) {
							values.clear();
							values.addAll(newValues);
						}
					}
				} catch (Throwable e) {
					if ("1".equals(skipErr) || "true".equals(skipErr))
						continue;
					String parserInfo = CommonUtil.toJson(parser);
					String err = "parser->" + parserInfo + " of field->" + key +" failed";
					//listener.onError(Thread.currentThread(), task, err, e);
				}
			}
			
			try {
				if (values.isEmpty()) 
					values.add("");
				
				// 相同 key，若values不为空，继续沿用
				if (map.containsKey(key)){
					//将原来的值插入到前面
					Object obj = map.get(key);
					if (obj instanceof Collection) {
						values.addAll(0, (Collection<?>) obj);
					} else {
						values.add(0, obj);
					}
				}
				
				//数组的话，需要去除空元素
				if (values.size() >= 2){
					List<Object> noRepeatValues = new ArrayList<Object>();
					for (Iterator<Object> it = values.iterator(); it.hasNext(); ){
						Object obj = it.next();
						if (obj instanceof String) {
							if (((String)obj) == null || ((String)obj).trim().length() == 0)
								continue;
						}
						
						noRepeatValues.add(obj);
					}
					values.clear();
					values.addAll(noRepeatValues);
				}
				
				//如果设置了trim
				if ("1".equals(isTrim) || "true".equals(isTrim)) {
					List<String> results = new ArrayList<String>(values.size());
					for (Object obj : values){
						results.add(String.valueOf(obj).trim());
					}
					values.clear();
					values.addAll(results);
				}
				
				//如果是DigNewUrl
				if ("1".equals(isForDigNewUrl) || "true".equals(isForDigNewUrl)) {
					if ("1".equals(isArray)){
						for (Object val : values){
							task.digNewUrls.add(String.valueOf(val));
						}
					}else{
						if (!values.isEmpty())
							task.digNewUrls.add(String.valueOf(values.get(0)));
					}
				}
				
				Object value = null;
				if ("1".equals(isArray) || "true".equals(isArray)){
					List<Object> newValues = new ArrayList<Object>();
					for (Object val : values){
						if (values.size() == 1 && val.getClass().isArray()){
							Object[] newVals = (Object[])val;
							for (Object nv : newVals){
								if (nv == null || String.valueOf(nv).trim().length() == 0)
									continue;
								newValues.add(nv);
							}
						}
					}
					if (!newValues.isEmpty()){
						values.clear();
						values.addAll(newValues);
					}
					value = values;
					if ("1".equals(isMergeArray) || "true".equals(isMergeArray)){
						StringBuilder sb = new StringBuilder();
						for (Object val : values){
							sb.append(String.valueOf(val));
						}
						value = sb.toString();
					}else
						value = values;
				}else{
					if (values.isEmpty())
						value = "";
					else
						value = values.get(0);
				}
				
				if(isFinalParam){
					finalFields.put(key, value);
				}

				//最终完成
				map.put(key, value);
				
			} catch (Throwable e) {
				//listener.onError(Thread.currentThread(), task, "field->"+key+" parse failed cause->"+e.toString(), e);
			}
		}
		
		return map;
	}
	
	private void parseByExp(String exp, Collection<Object> list) {
		if (exp == null || exp.trim().length() == 0)
			return ;
		
		List<Object> newValue = new ArrayList<Object>();
		if (list == null || list.isEmpty()){
			try {
	    		Object newVal = fel.eval(exp);
				if (newVal != null) {
					if (newVal instanceof Collection)
						newValue.addAll((Collection<?>)newVal);
					else
						newValue.add(newVal);
				}
			} catch (Throwable e){
				//listener.onError(Thread.currentThread(), task, "exp->"+exp+" eval failed", e);
			}
		} else {
			for (Object val : list){
				boolean isValBlank = false;
				if (val != null){
					if (val instanceof String && ((String)val).trim().length() == 0){
						isValBlank = true;
					}else {
						fel.getContext().set("$this", val);
					}
				}
				try {
		    		Object newVal = fel.eval(exp);
					if (newVal != null) {
						if (newVal instanceof Collection)
							newValue.addAll((Collection<?>)newVal);
						else
							newValue.add(newVal);
					}
				} catch (Throwable e){
				} finally {
					fel.getContext().set("$this", "");//解析完表达式之后要重置这个this变量
				}
			}
		}
		
		if (!newValue.isEmpty()){
			list.clear();
			list.addAll(newValue);
		}
	}
	
	private void parseByRegex(String regex, String skipRgxFail, Collection<Object> list) {
		if (regex == null || regex.trim().length() == 0)
			return ;
		List<Object> newVals = new ArrayList<Object>(list.size());
		for (Object obj : list) {
			try {
				String input = (String)obj;
				if (input == null || input.trim().length() == 0)
					continue;
				List<String> vals = CommonUtil.findByRegex(input, regex);
				//如果REGEX找不到
				if (vals == null) {
					if ("1".equals(skipRgxFail) || "true".equals(skipRgxFail))
						continue;
					
					newVals.add("");
				} else {
					for (String val : vals){
						if (val == null || val.trim().length() == 0){
							if ("1".equals(skipRgxFail) || "true".equals(skipRgxFail))
								continue;
							val = "";
						}
						newVals.add(val);
					}
				}
			} catch (Throwable e){
				if ("1".equals(skipRgxFail) || "true".equals(skipRgxFail))
					continue;
				newVals.add("");
			}
		}
		
		if (!newVals.isEmpty()){
			list.clear();
			list.addAll(newVals);
		}
	}


}
