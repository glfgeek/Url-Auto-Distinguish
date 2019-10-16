/**  
* @author guolongfei  
* @date 2018年4月17日  
* @version 1.0
*/
package com.glf.news.extractor.NewsExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * ClassName: DomainUtilTest
 *
 * Description:
 * 
 * @author guolongfei
 * @date 2018年4月17日
 */
public class DomainUtilTest {
	/**
	 * 国际域名后缀
	 */
	private static HashSet<String> internationalSuffix;

	/**
	 * 国家域名后缀
	 */
	private static HashSet<String> countrySuffix;

	private static HashSet<String> completeSuffic;

	private static Pattern p = Pattern.compile("([\\w.-]+)");

	static {
		internationalSuffix = new HashSet<String>();
		countrySuffix = new HashSet<String>();
		completeSuffic = new HashSet<String>();

		try {

			BufferedReader br = new BufferedReader(new FileReader(new File("config/domain_suffix")));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.equalsIgnoreCase("[International]")) {
					while ((line = br.readLine()) != null && !line.equalsIgnoreCase("[/International]")) {
						line = line.replaceAll("[\\s.]+", "");
						if (!line.equals("")) {
							internationalSuffix.add(line);
						}
					}
				}

				if (line.equalsIgnoreCase("[Country]")) {
					while ((line = br.readLine()) != null && !line.equalsIgnoreCase("[/Country]")) {
						line = line.replaceAll("[\\s.]+", "");
						if (!line.equals("")) {
							countrySuffix.add(line);
						}
					}
				}
				if (line.equalsIgnoreCase("[Complete]")) {
					while ((line = br.readLine()) != null && !line.equalsIgnoreCase("[/Complete]")) {
						line = line.replaceAll("[\\s]+", "");
						if (!line.equals("")) {
							completeSuffic.add(line);
						}
					}
				}
			}

			br.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}

	}

	/**
	 * 从URL中提取域名，如http://news.163.com/xxx/xxx.html --> 163.com
	 * 
	 * @param url
	 *            输入的URL
	 * @return String
	 */
	public static String getDomain(String url) {
		String host = getHost(url);

		if (host == null) {
			return host;
		}
		if (host.matches("\\d+(\\.\\d+){3,}")) {
			return host;
		}

		String[] segments = host.split("\\.");

		if (segments.length < 3) {
			return host;
		}

		String lastSegment = segments[segments.length - 1];
		String secondLastSegment = segments[segments.length - 2];

		String twoSegment = secondLastSegment + "." + lastSegment;

		if (!completeSuffic.contains(twoSegment)) {
			if ((internationalSuffix.contains(lastSegment) || countrySuffix.contains(lastSegment))
					&& !internationalSuffix.contains(secondLastSegment)) {
				return secondLastSegment + "." + lastSegment;
			}
		}

		return segments[segments.length - 3] + "." + secondLastSegment + "." + lastSegment;
	}

	/**
	 * 从URL中提取主机，如http://news.163.com/xxx/xxx.html --> news.163.com
	 * 
	 * @param url
	 *            输入的URL
	 * @return String 主机地址
	 */
	public static String getHost(String url) {
		if (url == null || url.trim().isEmpty()) {
			return null;
		}
		try {
			url = URLDecoder.decode(url, "UTF-8");
			url = url.toLowerCase();
			url = url.replaceAll("#", "");
			url = url.replaceAll("!", "");
			if (url.contains("?")) {
				url = url.substring(0, url.indexOf("?"));
			}
		} catch (Exception e) {
			//
		}
		url = url.replaceAll("^https?://", "");
		Matcher matcher = p.matcher(url);
		matcher.find();
		return matcher.group(1);
	}

	/**
	 * 
	 * @param srcURL
	 *            srcURL是xpath提取出来的详细页的相对或者绝对路径
	 * @param baseURL
	 *            baseURL是这个列表页的url
	 * @return String URL绝对地址
	 */
	public static String getAbsoluteURL(String srcURL, String baseURL) {
		if (baseURL == null || srcURL == null) {
			return srcURL;
		}
		if (srcURL.matches("(?i)mailto:.*"))
			return null;
		if (srcURL.startsWith("//")) {
			if (baseURL.startsWith("https://")) {
				return "https:" + srcURL;
			} else {
				return "http:" + srcURL;
			}
		}
		srcURL = srcURL.replaceAll("^(?:\"|\\./|\"\\./)|\"$", "");
		if (srcURL.toLowerCase().startsWith("http://") || srcURL.toLowerCase().startsWith("https://"))
			return trimURL(srcURL, "extra");
		else if (srcURL.matches("(?i)javascript:.*"))
			return null;

		if (baseURL.endsWith("/") && !srcURL.startsWith("/")) {
			String absURL = baseURL + srcURL;
			return absURL;
		}
		String absoluteURL = null;
		String base_host = baseURL.replaceAll("(https?://[^/]+).*", "$1");
		String base_app = baseURL.replaceAll("https?://[^/]+(/(?:[^/\\?]+/)*)?.*", "$1");
		if (!base_app.startsWith("/")) {
			base_app = "/" + base_app;
		}

		if (srcURL.startsWith("/")) {
			absoluteURL = base_host + srcURL;
		} else if (srcURL.startsWith("../")) {
			while (srcURL.startsWith("../")) {
				srcURL = srcURL.substring(3);
				base_app = base_app.replaceAll("[^/]+/$", "");
			}

			absoluteURL = base_host + base_app + srcURL;
		} else if (srcURL.startsWith("?")) {
			if (baseURL.indexOf("?") > 0) {
				absoluteURL = baseURL.substring(0, baseURL.indexOf("?")) + srcURL;
			} else {
				absoluteURL = baseURL + srcURL;
			}
		} else {
			absoluteURL = base_host + base_app + srcURL;
		}

		return trimURL(absoluteURL, "extra;sid;fpage");
	}

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

	public static URL resolve(URL base, String relUrl) throws MalformedURLException {
		// workaround: java resolves '//path/file + ?foo' to '//path/?foo', not
		// '//path/file?foo' as desired
		if (relUrl.startsWith("/"))
			relUrl = base.getPath() + relUrl;
		// workaround: //example.com + ./foo = //example.com/./foo, not
		// //example.com/foo
		if (relUrl.indexOf('.') == 0 && base.getFile().indexOf('/') != 0) {
			base = new URL(base.getProtocol(), base.getHost(), base.getPort(), "/" + base.getFile());
		}
		return new URL(base, relUrl);
	}

	/**
	 * 去除URL中不需要的参数信息
	 * 
	 * @param url
	 * @param params
	 * @return String
	 */
	public static String trimURL(String url, String params) {
		if (url == null || params == null) {
			return url;
		}
		StringBuilder urlBuilder = new StringBuilder();
		String[] urls = url.split("[&?]");
		List<String> trimList = new ArrayList<String>();
		for (String trim : params.split(";")) {
			trimList.add(trim);
		}

		for (int i = 0; i < urls.length; i++) {
			if (!trimList.contains(urls[i].split("=")[0])) {
				if (i == 0) {
					urlBuilder.append(urls[i] + "?");
				} else {
					urlBuilder.append(urls[i] + "&");
				}
			}
		}
		if (urlBuilder.toString().endsWith("&") || urlBuilder.toString().endsWith("?")) {
			urlBuilder.delete(urlBuilder.length() - 1, urlBuilder.length());
		}

		return urlBuilder.toString();
	}

	public static String encodeIllegalCharacterInUrl(String url) {
		// TODO more character support
		// relativePath = relativePath.replaceAll("&", "%26");
		// relativePath = relativePath.replaceAll(" ", "%20");
		// relativePath = relativePath.replaceAll("#", "%23");
		// relativePath = relativePath.replaceAll("%", "%25");
		// relativePath = relativePath.replaceAll("\\{", "%7B");
		// relativePath = relativePath.replaceAll("}", "%7D");

		return url.replace(" ", "%20");
	}

	/**
	 * url 路径计算
	 * 
	 * @param url
	 * @author xiaoming mingyan.xia@gmail.com
	 * @date 2018年3月22日 上午10:26:32
	 * @version 1.0
	 * @return String
	 */
	public static String urlPath(String url) {
		if (StringUtils.isNotBlank(url)
				&& (url.startsWith("//") || url.startsWith("http://") || url.startsWith("https://"))) {
			url = url.substring(url.indexOf("//") + 2, url.length());
			List<String> result = new ArrayList<String>();
			String parm = null;
			String main = null;
			if (url.contains("?")) {
				String urlTemp[] = url.split("\\?");
				if (urlTemp.length >= 2) {
					main = urlTemp[0];
					parm = urlTemp[1];
				} else {
					main = urlTemp[0];
				}
			} else {
				main = url;
			}
			if (main != null && main.contains("/")) {
				String mainTemp[] = main.split("/");
				for (int i = 0; i < mainTemp.length; i++) {
					String mainStr = null;
					if (i == 0 && mainTemp[i].equals(getHost(url))) {
						result.add("siteDomain");
						continue;
					} else if (i == mainTemp.length - 1 && mainTemp[i].contains(".")) {
						mainStr = mainTemp[i].split("\\.")[1];
					} else {
						mainStr = mainTemp[i];
					}
					String numStr = mainStr;
					if (numStr.contains("_")) {
						numStr = numStr.replaceAll("_", "");
					}
					if (numStr.contains("-")) {
						numStr = numStr.replaceAll("-", "");
					}
					if (StringUtils.isNumeric(numStr)) {
						result.add("number");
					} else {
						result.add(mainStr);
					}

				}
			} else {
				if (main.equals(getHost(url))) {
					result.add("siteDomain");
				} else {
					result.add(main);
				}
			}
			if (parm != null) {
				if (parm.contains("&")) {
					String parmTemp[] = parm.split("&");
					for (int i = 0; i < parmTemp.length; i++) {
						String p = parmTemp[i];
						if (p.contains("=")) {
							result.add(p.split("=")[0]);
						}
					}
				} else if (parm.contains("=")) {
					result.add(p.split("=")[0]);
				}
			}
			if (result.size() == 0) {
				return null;
			}
			return StringUtils.join(result, "/");
		}
		return null;
	}

	public static void main(String[] args) {
		// String baseURL = "http://www.tywbw.com/jdgz/";// 列表页URL
		// String srcURL = "c/2018-04/17/content_67704.htm";// 相对路径或者绝对路径
		// String result =
		// "http://www.tywbw.com/jdgz/c/2018-04/17/content_67704.htm";
		String baseURL = "http://www.cqcb.com/highlights/";// 列表页URL
		String srcURL = "/hot/2018-04-16/782747_pc.html";// 相对路径或者绝对路径
		String result = "http://www.cqcb.com/hot/2018-04-16/782747_pc.html";
		String outResult = DomainUtilTest.getAbsoluteURL(srcURL, baseURL);
		System.out.println("srcURL为：" + srcURL);
		System.out.println("baseURL为：" + baseURL);
		System.out.println("正确的结果是：" + result);
		System.out.println("输出绝对路径：" + outResult);
		System.out.println("结果是：" + result.equals(outResult));
	}

}
