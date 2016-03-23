package com.soft.crawler.plugin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

public class URLUtil {
	
	public static String read(String _url) {
		return read(_url, null);
	}
	
	public static String read(String _url, String charset) {
		
		BufferedReader reader = null;
		try {
			URL url = new URL(_url);
			if (charset == null)
				reader = new BufferedReader(new InputStreamReader(url.openStream()));
			else
				reader = new BufferedReader(new InputStreamReader(url.openStream(), charset));
			
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null){
				sb.append(line);
			}
			
			return sb.toString();
		} catch (Throwable e){
			throw new RuntimeException(e);
		} finally {
			if (reader != null){
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static void main(String[] args){
		String json = URLUtil.read("http://localhost:8080/duan/api/create/url?data=http://www.baidu.com");
		System.out.println(json);
		Map<String, Object> jsonMap = CommonUtil.parse(json, Map.class);
		String shortLink = "http://localhost:8080/duan/" + jsonMap.get("code");
		System.out.println(shortLink);
	}
}
