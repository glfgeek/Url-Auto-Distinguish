/**
* @author guolongfei
* @date 2018年3月30日
* @version 1.0
*/
package com.glf.news.extractor.NewsExtractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * <p>
 * Title: NewsDetailTest
 * </p>
 * <p>
 * Description:
 * </p>
 *
 * @author guolongfei
 * @date 2018年3月30日
 */
public class NewsDetailTest {
	@Test
	public void testaa() {
		String url = "http://ent.ifeng.com";
		String html = null;
		try {
			html = HtmlExtractor.getHtml(url);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrasePageImpl prasePageImpl = new PrasePageImpl();
		DetailResult detailResult = prasePageImpl.analysisLinks(url, html);
		List<Detail> channels = detailResult.getChannels();
		List<Detail> newsList = detailResult.getNewsList();
		for (Detail detail : channels) {
			System.out.println("列表页URL为：" + detail.getUrl());
			System.out.println("列表页标题为：" + detail.getTitle());
			System.out.println("========================");
		}
		System.out.println("====================================");
		for (Detail detail : newsList) {
			System.out.println("详细页URL为：" + detail.getUrl());
			System.out.println("详细页标题为：" + detail.getTitle());
			System.out.println("========================");
		}
	}

	@Test
	public void testbb() {
		 String seedUrl = "http://news.sohu.com/";
//		String seedUrl = "http://www.henandaily.cn/content/szheng/2018/0412/96060.html";
		// String seedUrl = "http://news.qq.com/";
		// String seedUrl = "http://news.sina.com.cn/";
		// String seedUrl = "http://news.163.com/";
		String html = null;
		try {
			html = HtmlExtractor.getHtml(seedUrl);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrasePageImpl prasePageImpl = new PrasePageImpl();
		DetailResult detailResult = prasePageImpl.analysisLinks(seedUrl, html);
		List<Detail> newsList = detailResult.getNewsList();
		for (Detail detail : newsList) {
			String url = detail.getUrl();
			String title = detail.getTitle();
			String htmlaa = null;
			try {
				htmlaa = HtmlExtractor.getHtml(url);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String titleTime = prasePageImpl.analysisNewsTime(url, htmlaa, title);
			System.out.println("标题为：" + title);
			System.out.println("时间为：" + titleTime);
		}

	}

	@Test
	public void testcc() {
		// String seedUrl = "http://news.sohu.com/";
		String seedUrl = "http://news.ifeng.com/";
		// String seedUrl = "http://news.qq.com/";
		// String seedUrl = "http://news.sina.com.cn/";
		// String seedUrl = "http://news.163.com/";
		String html = null;
		try {
			html = HtmlExtractor.getHtml(seedUrl);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		PrasePageImpl prasePageImpl = new PrasePageImpl();
		DetailResult detailResult = prasePageImpl.analysisLinks(seedUrl, html);
		List<Detail> newsList = detailResult.getNewsList();
		for (Detail detail : newsList) {
			String url = detail.getUrl();
			String htmlaa = null;
			try {
				htmlaa = HtmlExtractor.getHtml(url);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ContentResult content = prasePageImpl.analysisNewsContent(url, htmlaa);
			System.out.println("正文内容为：" + content.getContent());
			System.out.println("==============================================");
		}

	}

	@Test
	public void test() throws IOException, SAXException {
		String surl = "http://news.qq.com/";
		String html = HtmlExtractor.getHtml(surl);
		Document doc = Jsoup.parse(html, surl);
		long startTime = System.currentTimeMillis(); // 获取开始时间
		HashMap<String, TagNodeInfo> tagMax = new NewsDetailPageExtrtactor(doc, surl).traverNodeTree();
		long endTime = System.currentTimeMillis(); // 获取结束时间
		System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
		for (Map.Entry<String, TagNodeInfo> entry : tagMax.entrySet()) {
			Node aNode = entry.getValue().getaNode();
			String url = entry.getKey();
			System.out.println("详细页title：" + NewsDetailPageExtrtactor.aNodeStr(aNode));
			System.out.println("详细页url：" + url);
			System.out.println("==========================================");
		}
		System.out.println("详细页数量为：" + tagMax.size());
	}

	public void test(String url, String fileName) throws IOException, SAXException, InterruptedException {
		String surl = url;
		String html = HtmlExtractor.getHtml(surl);
		Document doc = Jsoup.parse(html, surl);
		List<String> history = new ArrayList<String>();
		int count = 0;
		Elements es = doc.select("a");
		for (Iterator it = es.iterator(); it.hasNext();) {
			Element e = (Element) it.next();
			String aUrl = e.absUrl("href");
			if (!history.contains(aUrl)) {
				history.add(aUrl);
			}
		}
		System.out.println(fileName + "\t" + "第一次历史条数" + history.size());
		HashMap<String, TagNodeInfo> currentRecord1 = new NewsDetailPageExtrtactor(doc, surl).traverNodeTree();
		JSONObject json1 = JSONObject.fromObject(currentRecord1);
		FileIOUtil.writeFileNew(fileName + "/历史记录.txt", json1.toString());
		while (true) {
			count++;
			Thread.sleep(1000 * 600);
			html = HtmlExtractor.getHtml(surl);
			doc = Jsoup.parse(html, surl);

			List<String> newUrl = new ArrayList<String>();
			es = doc.select("a");
			for (Iterator it = es.iterator(); it.hasNext();) {
				Element e = (Element) it.next();
				String aUrl = e.absUrl("href");
				if (!history.contains(aUrl) && !newUrl.contains(aUrl)) {
					newUrl.add(aUrl);
				}
			}

			HashMap<String, TagNodeInfo> currentRecord2 = new NewsDetailPageExtrtactor(doc, surl).traverNodeTree();
			JSONObject json2 = JSONObject.fromObject(currentRecord2);
			FileIOUtil.writeFileNew(fileName + "/当前" + count + ".txt", json2.toString());

			HashMap<String, TagNodeInfo> newList = new HashMap<String, TagNodeInfo>();
			for (Map.Entry<String, TagNodeInfo> entry2 : currentRecord2.entrySet()) {
				if (newUrl.contains(entry2.getKey())) {
					newList.put(entry2.getKey(), entry2.getValue());
				}
			}

			history.addAll(newUrl);
			if (newUrl.size() > 0) {
				JSONArray json3 = JSONArray.fromObject(newUrl);
				FileIOUtil.writeFileNew(fileName + "/新增" + count + ".txt", json3.toString());
				System.out.println(fileName + "\t" + count + "/新增" + newList.size());
			}
			if (newList.size() > 0) {
				JSONObject json4 = JSONObject.fromObject(newList);
				FileIOUtil.writeFileNew(fileName + "/融合" + count + ".txt", json4.toString());
				System.out.println(fileName + "\t" + count + "/融合" + newList.size());
			}

			if (history.size() > 0) {
				JSONArray json = JSONArray.fromObject(history);
				FileIOUtil.writeFileNew(fileName + "/历史" + count + ".txt", json.toString());
				System.out.println(fileName + "\t" + count + "/当前历史条数" + history.size());
			}

		}

	}

	@Test
	public void ttttt() throws IOException, SAXException, InterruptedException {
		String baseFile = "E:/";
		test("http://news.qq.com/", baseFile + "qq");
		// test("http://news.sohu.com/", baseFile + "sohu");
		// test("http://news.ifeng.com/", baseFile + "ifeng");
		// test("http://news.163.com/", baseFile + "wangyi");
		// test("http://news.sina.com.cn/", baseFile + "sina");
	}

}
