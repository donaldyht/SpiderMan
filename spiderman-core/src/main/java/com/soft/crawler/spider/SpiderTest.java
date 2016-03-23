package com.soft.crawler.spider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


import com.soft.crawler.plugin.util.xml.BeanXMLUtil;
import com.soft.crawler.plugin.util.xml.XMLReader;
import com.soft.crawler.task.Task;
import com.soft.crawler.task.TaskQueue;
import com.soft.crawler.xml.Site;



public class SpiderTest {

	public Map run(Site site) throws Throwable {
		Map map=new HashMap<String,String>();
		Task task=new Task(site.getUrl(), site.getHttpMethod(), null, site, 10);
		Spider spider = new Spider();
		spider.init(task);
		spider.run();
		System.out.println(TaskQueue.getSize());
		while(TaskQueue.getSize()>0){
			task=TaskQueue.pollTask();
			spider.init(task);
			spider.run();
		}
			
		return map;
			
	}
	
	public static String txt2String(File file){
        String result = "";
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = null;
            while((s = br.readLine())!=null){
                result = result + "\n" +s;
            }
            br.close();    
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

	public static void main(String args[]) throws Throwable{
		File file=new File("D:/workspace/xml/hbzljd4.xml");
		String content=txt2String(file);
		/**
		 * XMLReader已知实现类BeanXMLReader,它是解析xml的人
		 * private String rootElementName;
		 * private String beanName;
	     * private String content;
	     * private Hashtable<String, Class<?>> classes = new Hashtable<String, Class<?>>();
	     * 
		 */
		
		XMLReader reader = BeanXMLUtil.getBeanXMLReader(content);
		reader.setBeanName("site");
		reader.setClass("site", Site.class);
		Site site = reader.readOne();
		SpiderTest spider=new SpiderTest();
		spider.run(site);
	}
	 
	
} 