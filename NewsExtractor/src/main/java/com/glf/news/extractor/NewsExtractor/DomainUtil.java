package com.glf.news.extractor.NewsExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * 对URL的一些操作集（提取主机、域名、相对URL转绝对URL）
 *
 * @author jf.rong Modify 2015-08-22
 * @author LiWei
 * @author PengX
 *
 */
public class DomainUtil {
	// private static Logger log = LoggerFactory.getLogger(DomainUtil.class);
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
			BufferedReader br = new BufferedReader(new FileReader(new File("/Users/jingshuo/idea_local/config/domain_suffix")));
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
			// log.error(ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		} catch (IOException e) {
			// log.error(ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
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
		if (url == null || StringUtils.isBlank(url)) {
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
		String url = "http://news.sohu.com/";
		System.out.println("域名是：" + DomainUtil.getDomain(url));
	}
}
