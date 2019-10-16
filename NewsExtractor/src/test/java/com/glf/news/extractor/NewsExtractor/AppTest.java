package com.glf.news.extractor.NewsExtractor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Unit test for simple App.
 */
public class AppTest {

	public static String relative2AbsolutePath(String content, String url, String tag, String property)
			throws URISyntaxException, MalformedURLException {
		String newContent = "";
		if (content != null && content.trim() != "") {
			URI base = new URI(url);// 基本网页搜索URI
			Document doc = Jsoup.parse(content);
			for (Element ele : doc.getElementsByTag(tag)) {
				String elePropValue = ele.attr(property);
				if (!elePropValue.matches("^(https?|ftp):(\\\\|//).*$")) {
					URI abs = base.resolve(elePropValue);// 解析相对URL，得到绝对URI
					ele.attr(property, abs.toURL().toString());
				}
			}
			newContent = doc.html();
		}
		return newContent;
	}

	@SuppressWarnings("finally")
	public static String getAbsoluteURL(String baseURI, String relativePath) {
		String abURL = null;
		try {
			URI base = new URI(baseURI);// 基本网页URI
			URI abs = base.resolve(relativePath);// 解析于上述网页的相对URL，得到绝对URI
			URL absURL = abs.toURL();// 转成URL
			System.out.println("输出绝对路径：" + absURL);
			abURL = absURL.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			return abURL;
		}
	}

	public static URL resolve(URL base, String relUrl) throws MalformedURLException {
		// workaround: java resolves '//path/file + ?foo' to '//path/?foo', not
		// '//path/file?foo' as desired
		if (relUrl.startsWith("?"))
			relUrl = base.getPath() + relUrl;
		// workaround: //example.com + ./foo = //example.com/./foo, not
		// //example.com/foo
		System.out.println("file路径：" + base.getFile());
		if (relUrl.indexOf('.') == 0 && base.getFile().indexOf('/') != 0) {
			base = new URL(base.getProtocol(), base.getHost(), base.getPort(), "/" + base.getFile());
		}
		return new URL(base, relUrl);
	}

	/**
	 * Create a new absolute URL, from a provided existing absolute URL and a
	 * relative URL component.
	 * 
	 * @param baseUrl
	 *            the existing absolute base URL
	 * @param relUrl
	 *            the relative URL to resolve. (If it's already absolute, it
	 *            will be returned)
	 * @return an absolute URL if one was able to be generated, or the empty
	 *         string if not
	 */
	public static String resolve(final String baseUrl, final String relUrl) {
		URL base;
		try {
			try {
				base = new URL(baseUrl);
			} catch (MalformedURLException e) {
				// the base is unsuitable, but the attribute/rel may be abs on
				// its own, so try that
				URL abs = new URL(relUrl);
				return abs.toExternalForm();
			}
			return resolve(base, relUrl).toExternalForm();
		} catch (MalformedURLException e) {
			return "";
		}

	}

	public static String getAbsoluteURL() throws IOException {
		URL url = new URL("http://www.bbrtv.com/Simplified/news/radio/");
		Document doc = Jsoup.parse(url, 3 * 1000);
		Elements link = doc.select("a");
		for (Element el : link) {
			System.out.println("相对路径为：" + el.attr("href"));
			String absHref = el.attr("abs:href");
			int aa = "abs:".length();
			System.out.println("abs的长度：" + aa);
			System.out.println("绝对路径是：" + absHref);
			System.out.println("============================================");
			String ad = "abs:href".substring("abs:".length());
			System.out.println("aaaaaa:" + ad);
			// String absHref = el.absUrl("abs:href");
			String result = "http://www.bbrtv.com/2018/0419/395004.html";
			if (absHref.equals(result)) {
				System.out.println("找到URL=======================：" + absHref);
			}
		}
		// String relHref = link.attr("href"); // == "/"
		return null;
	}

	public static void main(String[] args) {
		try {
			getAbsoluteURL();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// String url = resolve("http://www.bbrtv.com/Simplified/news/radio/",
		// "2018/0417/394712.html");
		// System.out.println("绝对路径url：" + url);
		// String baseURL = "http://www.bbrtv.com/Simplified/news/radio/";//
		// 列表页URL
		// String srcURL = "2018/0417/394712.html";// 相对路径或者绝对路径
		// String aa = getAbsoluteURL(baseURL, srcURL);
		// String result = "http://www.bbrtv.com/2018/0417/394712.html";
		// System.out.println("srcURL为：" + srcURL);
		// System.out.println("baseURL为：" + baseURL);
		// System.out.println("正确的结果是：" + result);
		// System.out.println("输出绝对路径：" + aa);
		// System.out.println("结果是：" + result.equals(aa));
	}

}
