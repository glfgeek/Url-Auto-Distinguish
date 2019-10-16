package com.glf.news.extractor.NewsExtractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 常用一般文件操作
 *
 * @author xiaoming Mar 26, 2015
 */
public class FileIOUtil {

	/**
	 * 追加文件：使用FileOutputStream，在构造FileOutputStream时，把第二个参数设为true
	 *
	 * @param fileName
	 *            文件名
	 * @param content
	 *            文件内容
	 */
	public static void writeFileAdd(String fileName, String content) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true), "UTF-8"));
			out.write(content + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 写一个新文件
	 *
	 * @param fileName
	 *            文件名
	 * @param content
	 *            文件内容
	 */
	public static void writeFileNew(String fileName, String content) {
		BufferedWriter out = null;
		// fileName = "html/" + fileName.replace(":", "").replace("?",
		// "").replace("/", "_") + ".html";
		try {
			String dirName = "./";
			if (fileName.contains("/")) {
				dirName = fileName.substring(0, fileName.lastIndexOf("/"));
			} else if (fileName.contains("\\")) {
				dirName = fileName.substring(0, fileName.lastIndexOf("\\"));
			}
			File dir = new File(dirName);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, false), "UTF-8"));
			out.write(content);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 按行读取文件
	 *
	 * @param fileName
	 *            文件名
	 * @return List<String> 行列表
	 * @throws IOException
	 */
	public static List<String> readFileByLine(String fileName) throws IOException {
		List<String> lineList = new ArrayList<String>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null) {
				lineList.add(line.trim());
			}
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return lineList;
	}

	/**
	 * 读取文件，作为一个字符串返回
	 *
	 * @param fileName
	 *            文件名
	 * @return String 文件内容
	 * @throws IOException
	 */

	public static String readFile(String fileName) {
		StringBuilder builder = new StringBuilder();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line.trim() + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return builder.toString().trim();
	}

	/**
	 * 将内容cnt写入文件
	 * 
	 * @param cnt
	 * @param filePath
	 * @return void
	 * @author wj.gao
	 * @date 2015年7月20日
	 */
	public void writeFile(String cnt, String filePath) {
		try {
			File f = new File(filePath);
			FileWriter fw = new FileWriter(f);
			fw.write(cnt);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
