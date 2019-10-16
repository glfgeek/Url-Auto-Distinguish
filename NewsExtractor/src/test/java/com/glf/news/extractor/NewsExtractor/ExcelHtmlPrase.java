/**  
q* @author guolongfei  
* @date 2018年4月26日  
* @version 1.0
*/
package com.glf.news.extractor.NewsExtractor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

/**
 * ClassName: ExcelHtmlPrase
 *
 * Description:
 * 
 * @author guolongfei
 * @date 2018年4月26日
 */
public class ExcelHtmlPrase {

	private static class ExcelHtmlURL {
		String siteUrl;
		String siteDomain;
		String siteText;

		public String getSiteUrl() {
			return siteUrl;
		}

		public void setSiteUrl(String siteUrl) {
			this.siteUrl = siteUrl;
		}

		public String getSiteDomain() {
			return siteDomain;
		}

		public void setSiteDomain(String siteDomain) {
			this.siteDomain = siteDomain;
		}

		public String getSiteText() {
			return siteText;
		}

		public void setSiteText(String siteText) {
			this.siteText = siteText;
		}
	}

	private static HashMap<String, TextNode> st = new HashMap<String, TextNode>();

	private static void addTextNode(TextNode tNode, Node node) {
		String url = node.absUrl("href");
		st.put(url, tNode);
	}

	public static void main(String[] args) throws IOException {
		File in = new File("C:/Users/Dell/Desktop/bookmarks_18_4_23俄语.html");
		Document doc = Jsoup.parse(in, "UTF-8", "");
		doc.traverse(new NodeVisitor() {
			public void head(Node node, int i) {
				if (node instanceof Node && node.nodeName().equals("a")) {
					List<Node> at = node.childNodes();
					for (Node nd : at) {
						if (nd instanceof TextNode) {
							TextNode tNode = (TextNode) nd;
							if (StringUtils.isNotEmpty(tNode.toString())) {
								addTextNode(tNode, node);
							}
						}
					}
				}
			}

			public void tail(Node node, int i) {
			}
		});
		for (Map.Entry<String, TextNode> entry : st.entrySet()) {
			String url = entry.getKey();
			String domian = DomainUtil.getDomain(entry.getKey());
			String text = entry.getValue().toString();
			ExcelHtmlURL excelHtmlURL = new ExcelHtmlURL();
			excelHtmlURL.setSiteDomain(domian);
			excelHtmlURL.setSiteUrl(url);
			excelHtmlURL.setSiteText(text);

			System.out.println("域名 " + domian + "url " + url + "网站名称 " + text);

		}

	}

}
