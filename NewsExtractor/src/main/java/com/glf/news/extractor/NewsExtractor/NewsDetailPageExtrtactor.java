/**  

* @author guolongfei 

* @date 2018年3月28日  

* @version 1.0  

*/
package com.glf.news.extractor.NewsExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 * 
 * Title: NewsPageExtrtactor
 * 
 * Description:获取新闻网页列表页和详细页
 * 
 * @author guolongfei
 * @date 2018年3月28日
 */
public class NewsDetailPageExtrtactor {

	private Document doc;
	/** 源URL的域名 */
	private String sedurlDomain;
	// urlATagNodeListMap集合中key是url，value是a标签节点集合
	private HashMap<String, ArrayList<Node>> urlATagNodeListMap = new HashMap<String, ArrayList<Node>>();
	// 标签路径tprList
	private ArrayList<Double> tagXpathTprList = new ArrayList<Double>();
	// URL路径tprList
	private ArrayList<Double> URLXpathTprList = new ArrayList<Double>();

	public NewsDetailPageExtrtactor(Document document, String url) {
		this.doc = document;
		clean();
		sedurlDomain = DomainUtil.getDomain(url);
	}

	/**
	 * 
	 * Title: TextNodeInfo
	 *
	 * Description: 最后输出的节点对象
	 * 
	 * @author guolongfei
	 * @date 2018年3月29日
	 */

	/**
	 * Title: clean
	 * 
	 * Description:清除掉样式，js脚本等
	 * 
	 */
	private void clean() {
		doc.select("script").remove();
		doc.select("style").remove();
		doc.select("iframe").remove();
	}

	// 得到节点的类型，如果是文本节点，名字为“text”，如果是标签，名字为“小写标签名”
	public static String getNodeName(Node node) {
		if (node instanceof TextNode) {
			return "text";
		} else {
			Element element = (Element) node;
			return element.tagName().toLowerCase();
		}
	}

	// 得到xpath路径
	public static String getXpath(Node node) {
		String result = "";
		Node temp = node;
		while (temp != null) {
			String name = getNodeName(temp);
			result = "," + name + result;
			temp = temp.parent();
		}
		return result;
	}

	/**
	 * Title: addATagNode
	 *
	 * Description:往有相同url的a标签节点集合中添加元素
	 * 
	 * @param aNode
	 */
	private void addATagNode(Node aNode) {
		String url = aNode.absUrl("href");
		ArrayList<Node> urlANodeListmap = urlATagNodeListMap.get(url);
		if (urlANodeListmap == null) {
			urlANodeListmap = new ArrayList<Node>();
			urlATagNodeListMap.put(url, urlANodeListmap);
		}
		urlANodeListmap.add(aNode);
	}

	public static String text(Node node) {
		final StringBuilder accum = new StringBuilder();
		new NodeTraversor(new NodeVisitor() {
			public void head(Node node, int depth) {
				if (node instanceof TextNode) {
					TextNode textNode = (TextNode) node;
					accum.append(textNode.toString());
				}
			}

			public void tail(Node node, int depth) {
			}
		}).traverse(node);
		return accum.toString().trim();
	}

	// 传入a节点，得到a节点文本字符串，如果有title输出title文本，没有title，就找此a标签的文本节点
	public static String aNodeStr(Node aNode) {
		// TODO title
		if (aNode.hasAttr("title")) {
			String titleStr = aNode.attr("title");
			if (StringUtils.isNotBlank(titleStr)) {
				return titleStr;
			}
		} else {
			String cNodeStr = text(aNode);
			return cNodeStr;
		}
		return null;
	}

	/**
	 * Title: deduplication
	 * 
	 * Description: 去除有相同URL的a标签节点，得到一个URL下最长的文本节点的a节点,将元素添加到集合中
	 * 
	 * 
	 * @param urlTNodeListMap
	 * @return urlTNodeMap
	 */
	private HashMap<String, Node> deduplication(HashMap<String, ArrayList<Node>> urlANodeListMap) {
		HashMap<String, Node> urlTNodeMap = new HashMap<String, Node>();
		for (Map.Entry<String, ArrayList<Node>> entry : urlANodeListMap.entrySet()) {
			String url = entry.getKey();
			ArrayList<Node> aNodeList = entry.getValue();
			if (aNodeList.size() > 1) {
				ArrayList<TextNode> aTNodeList = new ArrayList<TextNode>();
				for (Node aNode : aNodeList) {
					if (aNode.hasAttr("title")) {
						urlTNodeMap.put(url, aNode);
					}
					List<Node> childNodes = aNode.childNodes();
					for (Node cNode : childNodes) {
						if (cNode instanceof TextNode) {
							TextNode tNode = (TextNode) cNode;
							aTNodeList.add(tNode);
						}
					}
				}
				if (aTNodeList.size() > 0) {
					TextNode max = aTNodeList.get(0);
					for (int i = 0; i < aTNodeList.size(); i++) {
						if (aTNodeList.get(i).toString().length() > max.toString().length()) {
							max = aTNodeList.get(i);
						}
					}
					urlTNodeMap.put(url, max.parentNode());
				}
			} else {
				urlTNodeMap.put(url, aNodeList.get(0));
			}
		}
		return urlTNodeMap;
	}

	/**
	 * Title: xpathTagTNode
	 *
	 * Description: 构建同一a标签路径下的a节点集合
	 * 
	 * @param mapTextNode
	 */
	private HashMap<String, ArrayList<Node>> xpathATagANode(HashMap<String, Node> mapANode) {
		HashMap<String, ArrayList<Node>> xpathATagANodeMap = new HashMap<String, ArrayList<Node>>();
		for (Map.Entry<String, Node> entry : mapANode.entrySet()) {
			Node aNode = entry.getValue();
			String xpath = getXpath(aNode);
			ArrayList<Node> aNodeList = xpathATagANodeMap.get(xpath);
			if (aNodeList == null) {
				aNodeList = new ArrayList<Node>();
				xpathATagANodeMap.put(xpath, aNodeList);
			}
			aNodeList.add(aNode);
		}
		return xpathATagANodeMap;
	}

	/**
	 * Title: xpathURLTNode
	 * 
	 * Description: 构建同一URL路径下的文本节点集合
	 * 
	 * @param mapTextNode
	 */
	private HashMap<String, ArrayList<Node>> xpathURLTNode(HashMap<String, Node> mapANode) {
		HashMap<String, ArrayList<Node>> xpathURLANodeMap = new HashMap<String, ArrayList<Node>>();
		for (Map.Entry<String, Node> entry : mapANode.entrySet()) {
			String url = entry.getKey();
			Node aNode = entry.getValue();
			String xpath = DomainUtil.urlPath(url);
			ArrayList<Node> aNodeList = xpathURLANodeMap.get(xpath);
			if (aNodeList == null) {
				aNodeList = new ArrayList<Node>();
				xpathURLANodeMap.put(xpath, aNodeList);
			}
			aNodeList.add(aNode);
		}
		return xpathURLANodeMap;
	}

	/**
	 * Title: computeTpr
	 *
	 * Description: 传入文本节点集合计算tpr
	 * 
	 * @param tNodeList
	 * @return
	 */
	private static double computeTpr(ArrayList<Node> aNodeList) {
		double textSum = 0;
		double aTextLen = 0;
		for (Node aNode : aNodeList) {
			if (StringUtils.isNotEmpty(aNodeStr(aNode)))
				aTextLen = aNodeStr(aNode).length();
			textSum += aTextLen;
		}
		double tpr = textSum / aNodeList.size();
		return tpr;
	}

	/**
	 * Title: NoiseThreshold
	 *
	 * Description:封装了噪音最大值和噪音最小值的类
	 * 
	 * @author guolongfei
	 * @date 2018年3月28日
	 */
	class NoiseThreshold {
		double noiseMax;
		double noiseMin;

		NoiseThreshold(double noiseMax, double noiseMin) {
			this.noiseMax = noiseMax;
			this.noiseMin = noiseMin;
		}

		public double getNoiseMax() {
			return noiseMax;
		}

		public double getNoiseMin() {
			return noiseMin;
		}
	}

	/**
	 * Title: computeNoiseRange
	 *
	 * Description: 传入tpr集合，得到噪音范围
	 * 
	 * @param tprList
	 * @return
	 */
	public NoiseThreshold computeNoiseRange(ArrayList<Double> tprList) {
		double sum = 0;
		double maxtpr = tprList.get(0);
		double mintpr = tprList.get(0);
		for (Double tp : tprList) {
			sum += tp;
			if (tp >= maxtpr) {
				maxtpr = tp;
			}
			if (tp <= mintpr) {
				mintpr = tp;
			}
		}
		double noiseMax = (sum / tprList.size() + (maxtpr - mintpr) / 2 * 0.2);
		double noiseMin = (sum / tprList.size() - (maxtpr - mintpr) / 2 * 0.2);
		return new NoiseThreshold(noiseMax, noiseMin);
	}

	/**
	 * Title: classification
	 *
	 * Description: 分类过滤详细页，列表页，噪音，得到详细页集合，列表页集合，噪音集合
	 * 
	 * @param TagtNodeInfoList
	 * @param UrltNodeInfoList
	 */
	private HashMap<String, TagNodeInfo> classifiNews(HashMap<String, TagNodeInfo> TagtNodeInfoList,
			HashMap<String, TagNodeInfo> UrltNodeInfoList) {
		HashMap<String, TagNodeInfo> TagMax = new HashMap<String, TagNodeInfo>();
		HashMap<String, TagNodeInfo> URLMax = new HashMap<String, TagNodeInfo>();
		double TagNoiseMax = computeNoiseRange(tagXpathTprList).getNoiseMax();
		double UrlNoiseMax = computeNoiseRange(URLXpathTprList).getNoiseMax();
		// 构建标签路径下的详细页
		for (Map.Entry<String, TagNodeInfo> entry : TagtNodeInfoList.entrySet()) {
			if (entry.getValue().getTpr() >= TagNoiseMax) {
				TagMax.put(entry.getKey(), entry.getValue());
			}
		}
		// 构建url路径下的详细页
		for (Map.Entry<String, TagNodeInfo> entry : UrltNodeInfoList.entrySet()) {
			if (entry.getValue().getTpr() >= UrlNoiseMax) {
				URLMax.put(entry.getKey(), entry.getValue());
			}
		}
		// 合并标签路径和url路径的详细页，合并后的集合是TagMax
		for (Map.Entry<String, TagNodeInfo> entry : URLMax.entrySet()) {
			TagMax.put(entry.getKey(), entry.getValue());
		}
		return TagMax;
	}

	/**
	 * Title: traverNodeTree
	 *
	 * Description: 遍历节点树获取节点,找到所有a标签下文本节点
	 */

	public HashMap<String, TagNodeInfo> traverNodeTree() {
		doc.traverse(new NodeVisitor() {
			public void head(Node node, int i) {
				if (node instanceof Node && node.nodeName().equals("a")
						&& StringUtils.isNotEmpty(node.absUrl("href"))) {
					String domainUrl = DomainUtil.getDomain(node.absUrl("href"));
					if (domainUrl != null && domainUrl.equals(sedurlDomain)) {
						addATagNode(node);
					}
				}
			}

			public void tail(Node node, int i) {
			}
		});
		HashMap<String, Node> urlTNodeMap = deduplication(urlATagNodeListMap);
		HashMap<String, ArrayList<Node>> xpathTagTNodeMap = xpathATagANode(urlTNodeMap);
		HashMap<String, ArrayList<Node>> xpathURLTNodeMap = xpathURLTNode(urlTNodeMap);
		HashMap<String, TagNodeInfo> TagtNodeInfoList = new HashMap<String, TagNodeInfo>();
		HashMap<String, TagNodeInfo> UrltNodeInfoList = new HashMap<String, TagNodeInfo>();
		for (Map.Entry<String, ArrayList<Node>> entry : xpathTagTNodeMap.entrySet()) {
			double tpr = computeTpr(entry.getValue());
			tagXpathTprList.add(tpr);
			ArrayList<Node> aNodeList = entry.getValue();
			for (Node aNode : aNodeList) {
				String url = aNode.absUrl("href");
				String aStr = aNodeStr(aNode);
				if (aStr.length() > 5)
					TagtNodeInfoList.put(url, new TagNodeInfo(aNode, tpr));
			}
		}

		for (Map.Entry<String, ArrayList<Node>> entry : xpathURLTNodeMap.entrySet()) {
			double tpr = computeTpr(entry.getValue());
			URLXpathTprList.add(tpr);
			ArrayList<Node> aNodeList = entry.getValue();
			for (Node aNode : aNodeList) {
				String url = aNode.absUrl("href");
				String aStr = aNodeStr(aNode);
				if (aStr.length() > 5)
					UrltNodeInfoList.put(url, new TagNodeInfo(aNode, tpr));
			}
		}
		HashMap<String, TagNodeInfo> TagMax = classifiNews(TagtNodeInfoList, UrltNodeInfoList);
		return TagMax;
	}
}
