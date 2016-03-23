package com.soft.crawler.plugin.util.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.soft.crawler.plugin.util.CommonUtil;


/**
 * 强大的XML标签过滤【通过正则】
 */
public class Tags {
	
	
	private String xml = null;//需要操作的xml文本
	private Boolean empty = false;//是否清空标签内的内容
	private Collection<String> kps = new HashSet<String>();//保留的标签缓存
	private Collection<String> rms = new HashSet<String>();//删除的标签缓存
	
	/**
	 * 构造一个Tags实例对象
	 * @return
	 */
	public static Tags me(){
		return new Tags();
	}
	
	/**
	 * 设置需要操作的XML文本
	 * @param xml
	 * @return
	 */
	public Tags xml(String xml){
		this.xml = xml;
		return this;
	}
	
	/**
	 * 切换到Attrs，切换之前会执行清除标签操作
	 * @return
	 */
	public Attrs Attrs(){
		exe();
		return Attrs.me().xml(xml);
	}
	
	/**
	 * 清空当前指定标签内的所有内容
	 * @return
	 */
	public Tags empty(){
		this.empty = true;
		return this;
	}
	
	/**
	 * 删除所有标签【保留标签里的内容】
	 * @return
	 */
	public Tags rm(){
		xml = cleanXmlTags(xml, false);
		return this;
	}
	
	/**
	 * 删除指定标签
	 * @param tag
	 * @return
	 */
	public Tags rm(String tag){
		this.rms.add(tag);
		return this;
	}
	
	/**
	 * 删除标签
	 * @param tag 不给定则删除所有
	 * @return
	 */
	public Tags rm(String... tag){
		this.rms.addAll(Arrays.asList(tag));
		return this;
	}
	
	/**
	 * 保留给定标签,其他删除
	 * @param tag
	 * @return
	 */
	public Tags kp(String tag){
		this.kps.add(tag);
		return this;
	}
	
	/**
	 * 保留给定标签,其他删除
	 * @param tag
	 * @return
	 */
	public Tags kp(String... tag){
		this.kps.addAll(Arrays.asList(tag));
		return this;
	}
	
	/**
	 * 执行标签的清除
	 * @return
	 */
	public Tags exe(){
		if (!this.rms.isEmpty()){
			xml = cleanXmlTags(xml, this.empty, rms.toArray(new String[]{}));
			this.rms.clear();
			this.empty = false;
		} if (!this.kps.isEmpty()){
			xml = cleanOtherXmlTags(xml, this.empty, kps.toArray(new String[]{}));
			this.kps.clear();
		}
		
		return this;
	}
	
	/**
	 * 返回处理后的字符串
	 * @return
	 */
	public String ok(){
		exe();
		return xml;
	}
	
	/**
	 * 删除标签
	 * @param xml
	 * @isRMCnt 是否删除标签内的所有内容
	 * @param keepTags 保留的标签，如果不给定则删除所有标签
	 * @return
	 */
	public static String cleanOtherXmlTags(String xml, boolean isRMCnt, String... keepTags) {
		if (xml == null || xml.trim().length() == 0) return "";
		if (isRMCnt){
			for (String keepTag : keepTags){
				String x = inverseXmlTagsRegex(keepTag);
				List<String> tag = findByRegex(xml, x);
				if (tag == null || tag.isEmpty() || tag.size() % 2 != 0)
					continue;
				int size = tag.size() / 2;
				List<List<String>> tags = new ArrayList<List<String>>(size);
				
				List<String> _pair = new ArrayList<String>(2);
				for (int i = 1; i <= tag.size(); i++){
					_pair.add(tag.get(i-1));
					if (i % 2 == 0){
						tags.add(new ArrayList<String>(_pair));
						_pair.clear();
					}
				}
				
				for (List<String> _tag : tags) {
					String regex = resolveRegex(_tag.get(0)) + ".*" + resolveRegex(_tag.get(1));
					xml = xml.replaceAll(regex, "");
				}
			}
			return xml;
		}
		return xml.replaceAll(inverseXmlTagsRegex(keepTags), "");
	}
	
	/**
	 * 删除标签
	 * @param xml
	 * @param isRMCnt 是否删除标签内的所有内容 <p>This is p.<a href="#">This is a.</a></p>如果干掉a标签，就变成=><p>This is p.</p>
	 * @param delTags 需要删除的Tag，如果不给定则删除所有标签
	 * @return
	 */
	public static String cleanXmlTags(String xml, boolean isRMCnt, String... delTags) {
		if (xml == null || xml.trim().length() == 0) return "";
		if (isRMCnt){
			for (String delTag : delTags){
				List<String> tag = findByRegex(xml, xmlTagsRegex(delTag));
				if (tag == null || tag.isEmpty() || tag.size() != 2)
					continue;
				String regex = resolveRegex(tag.get(0)) + ".*" + resolveRegex(tag.get(1));
				xml = xml.replaceAll(regex, "");
			}
			return xml;
		}
		
		return xml.replaceAll(xmlTagsRegex(delTags), "");
	}
	
	public static String resolveRegex(String regex){
		List<String> cc = Arrays.asList("\\", "^", "$", "*", "+", "?", "{", "}", "(", ")", ".", "[", "]", "|");
		for (String c : cc) {
			regex = regex.replace(c, "\\"+c);
		}
		return regex;
	}
	
	/**
	 * 匹配除了给定标签意外其他标签的正则表达式
	 * @param keepTags 如果不给定则匹配所有标签
	 * @return
	 */
	public static String inverseXmlTagsRegex(String... keepTags) {
		if (keepTags == null || keepTags.length == 0)
			return "<[!/]?\\b\\w+\\b\\s*[^>]*>";
		String fmt = "\\b%s\\b";
		StringBuilder sb = new StringBuilder();
		for (String kt : keepTags){
			if (kt == null || kt.trim().length() == 0)
				continue;
			
			if (sb.length() > 0)
				sb.append("|");
			sb.append(String.format(fmt, kt));
		}
		if (sb.length() == 0)
			return "<[!/]?\\b\\w+\\b\\s*[^>]*>";
		
		String pattern = "<[!/]?\\b(?!("+sb.toString()+"))+\\b\\s*[^>]*>";
		
		return pattern;
	}
	
	/**
	 * 匹配给定标签的正则表达式
	 * @param tags 如果不给定则匹配所有标签
	 * @return
	 */
	public static String xmlTagsRegex(String... tags) {
		if (tags == null || tags.length == 0)
			return "<[!/]?\\b\\w+\\b\\s*[^>]*>";
		String fmt = "\\b%s\\b";
		StringBuilder sb = new StringBuilder();
		for (String kt : tags){
			if (kt == null || kt.trim().length() == 0)
				continue;
			
			if (sb.length() > 0)
				sb.append("|");
			sb.append(String.format(fmt, kt));
		}
		if (sb.length() == 0)
			return "<[!/]?\\b\\w+\\b\\s*[^>]*>";
		
		String pattern = "<[!/]?("+sb.toString()+")\\s*[^>]*>";
		
		return pattern;
	}
	
	public static List<String> findByRegex(String input, String regex){
		if (input == null || input.trim().length() == 0) return null;
		List<String> result = new ArrayList<String>();
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);
		while(m.find()){
			result.add(m.group());
		}
		
		if (result.isEmpty()) return null;
		
		return result;
	}
}
