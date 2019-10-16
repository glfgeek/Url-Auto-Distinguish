package com.glf.news.extractor.NewsExtractor;

public interface ParsePage {

	/**
	 * 解析频道url列表和新闻url
	 * 
	 * @param baseUrl
	 *            请求url
	 * @param html
	 *            下载的html
	 * @author xiaoming mingyan.xia@gmail.com
	 * @date 2018年4月8日 下午7:19:05
	 * @version 1.0
	 * @return List<Detail>
	 */
	DetailResult analysisLinks(String baseUrl, String html);

	/**
	 * 解析新闻发布时间
	 * 
	 * @param baseUrl
	 *            请求url
	 * @param html
	 *            下载的html
	 * @param title
	 *            新闻标题
	 * @author xiaoming mingyan.xia@gmail.com
	 * @date 2018年4月8日 下午7:19:05
	 * @version 1.0
	 * @return List<Detail>
	 */
	String analysisNewsTime(String baseUrl, String html, String title);

	/**
	 * 解析新闻发布内容
	 * 
	 * @param baseUrl
	 *            请求url
	 * @param html
	 *            下载的html
	 * @author xiaoming mingyan.xia@gmail.com
	 * @date 2018年4月8日 下午7:19:05
	 * @version 1.0
	 * @return List<Detail>
	 */
	ContentResult analysisNewsContent(String baseUrl, String html);

}
