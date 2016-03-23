package com.soft.crawler.plugin.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MYSQLUtil {
	public static String backup(String dbName, String user, String password,
			String toFilePath) {
		String result = null;
		// 获取操作系统名字
		String osName = System.getProperty("os.name");
		// 判断是否是windows操作系统
		boolean isWindows = osName == null ? false : osName
				.startsWith("Windows");
		// 判断是否是Linux操作系统
		boolean isLinux = osName == null ? false : osName.startsWith("Linux");

		String command = null;
		if (isWindows) {
			command = String.format("cmd /c mysqldump -u%s -p%s %s", user,
					password, dbName);
		} else if (isLinux) {
			command = String.format("sh /c mysqldump -u%s -p%s %s", user,
					password, dbName);
		} else {
			result = "不支持的操作系统!";
			return result;
		}
		if (command != null) {
			Runtime rt = Runtime.getRuntime();
			InputStream in = null;
			FileOutputStream fout = null;
			InputStreamReader reader = null;
			BufferedReader br = null;
			OutputStreamWriter writer = null;
			try {
				Process child = rt.exec(command);
				// 把进程中执行的控制台输出信息写入.sql文件，即生成了备份文件。注：如果不对控制台信息进行读出，则会导致进程堵塞无法运行
				in = child.getInputStream();
				reader = new InputStreamReader(in, "utf-8");// 这是输出流编码为utf-8，否则从流中读入的是乱码
				String inStr = null;
				String outStr = null;
				StringBuilder sb = new StringBuilder();
				br = new BufferedReader(reader);
				while ((inStr = br.readLine()) != null) {
					sb.append(inStr).append("\r\n");
				}
				outStr = sb.toString();

				// 写到文件中去
				fout = new FileOutputStream(toFilePath);
				writer = new OutputStreamWriter(fout, "utf-8");
				writer.write(outStr);
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
				result = "发生异常：" + e.getClass().getName();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
						result = "发生异常：" + e.getClass().getName();
					}
				}
				if (fout != null) {
					try {
						fout.close();
					} catch (IOException e) {
						e.printStackTrace();
						result = "发生异常：" + e.getClass().getName();
					}
				}
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
						result = "发生异常：" + e.getClass().getName();
					}
				}
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
						result = "发生异常：" + e.getClass().getName();
					}
				}
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
						result = "发生异常：" + e.getClass().getName();
					}
				}
			}
		}

		return result;
	}

	public static String load(String dbName, String user, String password,
			String source) {
		String result = null;
		// 获取操作系统名字
		String osName = System.getProperty("os.name");
		// 判断是否是windows操作系统
		boolean isWindows = osName == null ? false : osName
				.startsWith("Windows");
		// 判断是否是Linux操作系统
		boolean isLinux = osName == null ? false : osName.startsWith("Linux");

		String command = "";
		String prix = "";
		if (isWindows) {
			prix = "cmd /c ";
			command = String.format("cmd /c mysql.exe -u%s -p%s %s", user,
					password, dbName);
		} else if (isLinux) {
			prix = "sh /c ";
			command = String.format("sh /c mysql.exe -u%s -p%s %s", user,
					password, dbName);
		} else {
			result = "不支持的操作系统!";
			return result;
		}
		if (command != null) {
			OutputStream out = null;
			BufferedReader br = null;
			OutputStreamWriter writer = null;
			try {
				Runtime rt = Runtime.getRuntime();
				rt.exec(String.format(prix+"mysqladmin -u%s -p%s drop %s if exits",user,password,dbName));
				rt.exec(String.format(prix+"mysqladmin -u%s -p%s create %s",user,password,dbName));
				// 调用 mysql 的 cmd
				Process child = rt.exec(command);
				out = child.getOutputStream();// 控制台的输入信息作为输出流
				String inStr;
				StringBuffer sb = new StringBuffer("");
				String outStr;
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(source), "utf8"));
				while ((inStr = br.readLine()) != null) {
					sb.append(inStr + "\r\n");
				}
				outStr = sb.toString();
				writer = new OutputStreamWriter(out, "utf8");
				writer.write(outStr);
				// 注：这里如果用缓冲方式写入文件的话，会导致中文乱码，用flush()方法则可以避免
				writer.flush();
			} catch (Exception e) {
				e.printStackTrace();
				result = "发生异常：" + e.getClass().getName();
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
						result = "发生异常：" + e.getClass().getName();
					}
				}
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
						result = "发生异常：" + e.getClass().getName();
					}
				}
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
						result = "发生异常：" + e.getClass().getName();
					}
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		String dbName = "gdoucwc";
		String user = "root";
		String password = "root";
		String toFilePath = "c:/backup.sql";
		String error = MYSQLUtil.backup(dbName, user, password, toFilePath);
		System.out.println(error);
	}
}
