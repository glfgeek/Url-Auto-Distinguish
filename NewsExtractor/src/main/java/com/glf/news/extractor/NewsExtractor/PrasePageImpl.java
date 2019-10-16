/**  
* @author guolongfei  
* @date 2018年4月9日  
* @version 1.0
*/
package com.glf.news.extractor.NewsExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 * 
 * Title: PrasePageImpl Description:
 * 
 * @author guolongfei
 * @date 2018年4月9日
 */
public class PrasePageImpl implements ParsePage {

	// urlATagNodeListMap集合中key是url，value是a标签节点集合
	private HashMap<String, ArrayList<Node>> urlATagNodeListMap = new HashMap<String, ArrayList<Node>>();
	// 标签路径的tprList
	private ArrayList<Double> tagXpathTprList = new ArrayList<Double>();
	// URL路径的tprList
	private ArrayList<Double> URLXpathTprList = new ArrayList<Double>();

	/**
	 * Title: isEmptyNode Description: 判断文本节点是否为空
	 * 
	 * @param node
	 * @return
	 */
	public static boolean isEmptyNode(TextNode node) {
		int count = node.text().trim().length();
		return count == 0;
	}

	/**
	 * Title: getNodeName Description:
	 * 得到节点的类型，如果是文本节点，名字为“text”，如果是标签，名字为“小写标签名”
	 * 
	 * @param node
	 * @return
	 */
	private static String getNodeName(Node node) {
		if (node instanceof TextNode) {
			return "text";
		} else {
			Element element = (Element) node;
			return element.tagName().toLowerCase();
		}
	}

	/**
	 * Title: getXpath Description: 得到xpath路径
	 * 
	 * @param node
	 * @return
	 */
	private static String getXpath(Node node) {
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

	/**
	 * Title: text Description: 递归遍历得到某个节点下所有文本节点的文本
	 * 
	 * @param node
	 * @return
	 */
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

	/**
	 * Title: aNodeStr Description: 如果有title输出title文本，没有title，就找此a标签的文本节点
	 * 
	 * @param aNode
	 * @return a节点文本字符串
	 */
	private static String aNodeStr(Node aNode) {
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
	 * Description: 去除有相同URL的a标签节点，得到一个URL下最长的文本节点的a节点,将元素添加到集合中返回集合
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
	private double computeTpr(ArrayList<Node> aNodeList) {
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
	 * 
	 * Title: ATagNodeInfo Description: 封装带有a标签节点，xpath，url和tpr的内部类
	 * 
	 * @author guolongfei
	 * @date 2018年4月9日
	 */
	class ATagNodeInfo {
		private Node aNode;
		private String xpath;
		private String url;
		private double tpr;

		public ATagNodeInfo(Node aNode, String xpath, String url, double tpr) {
			this.aNode = aNode;
			this.xpath = xpath;
			this.url = url;
			this.tpr = tpr;
		}

		public Node getaNode() {
			return aNode;
		}

		public String getXpath() {
			return xpath;
		}

		public String getUrl() {
			return url;
		}

		public double getTpr() {
			return tpr;
		}
	}

	/**
	 * Title: classification
	 *
	 * Description: 分类过滤详细页，列表页，噪音，得到详细页集合，列表页集合，噪音集合
	 * 
	 * @param TagtNodeInfoList
	 * @param UrltNodeInfoList
	 */
	private DetailResult classifiNews(HashMap<String, ATagNodeInfo> TagtNodeInfoList,
			HashMap<String, ATagNodeInfo> UrltNodeInfoList) {
		HashMap<String, ATagNodeInfo> TagMax = new HashMap<String, ATagNodeInfo>();
		HashMap<String, ATagNodeInfo> TagMin = new HashMap<String, ATagNodeInfo>();
		HashMap<String, ATagNodeInfo> TagNoise = new HashMap<String, ATagNodeInfo>();
		HashMap<String, ATagNodeInfo> URLMax = new HashMap<String, ATagNodeInfo>();
		HashMap<String, ATagNodeInfo> URLMin = new HashMap<String, ATagNodeInfo>();
		HashMap<String, ATagNodeInfo> URLNoise = new HashMap<String, ATagNodeInfo>();
		double TagNoiseMax = computeNoiseRange(tagXpathTprList).getNoiseMax();
		double TagNoiseMin = computeNoiseRange(tagXpathTprList).getNoiseMin();
		double UrlNoiseMax = computeNoiseRange(URLXpathTprList).getNoiseMax();
		double UrlNoiseMin = computeNoiseRange(URLXpathTprList).getNoiseMin();
		List<Detail> channels = new ArrayList<Detail>();
		List<Detail> newsList = new ArrayList<Detail>();
		// 构建标签路径下的详细页、列表页，噪声页
		for (Map.Entry<String, ATagNodeInfo> entry : TagtNodeInfoList.entrySet()) {
			if (entry.getValue().getTpr() >= TagNoiseMax) {
				TagMax.put(entry.getKey(), entry.getValue());
			} else if (entry.getValue().getTpr() <= TagNoiseMin) {
				TagMin.put(entry.getKey(), entry.getValue());
			} else {
				TagNoise.put(entry.getKey(), entry.getValue());
			}
		}
		// 构建url路径下的详细页、列表页，噪声页
		for (Map.Entry<String, ATagNodeInfo> entry : UrltNodeInfoList.entrySet()) {
			if (entry.getValue().getTpr() >= UrlNoiseMax) {
				URLMax.put(entry.getKey(), entry.getValue());

			} else if (entry.getValue().getTpr() <= UrlNoiseMin) {
				URLMin.put(entry.getKey(), entry.getValue());
			} else {
				URLNoise.put(entry.getKey(), entry.getValue());
			}
		}
		// 合并标签路径和url路径的详细页，合并后的集合是TagMax
		for (Map.Entry<String, ATagNodeInfo> entry : URLMax.entrySet()) {
			TagMax.put(entry.getKey(), entry.getValue());
		}

		// 合并标签路径和url路径的列表页，合并后的集合是TagMin
		for (Map.Entry<String, ATagNodeInfo> entry : URLMin.entrySet()) {
			TagMin.put(entry.getKey(), entry.getValue());
		}
		// 遍历合并后的详细页集合TagMax,取出title和url封装到Detail中
		for (Map.Entry<String, ATagNodeInfo> entry : TagMax.entrySet()) {
			String title = aNodeStr(entry.getValue().getaNode());
			if (title != null && title.length() >= 5) {
				String url = entry.getKey();
				Detail detail = new Detail();
				detail.setTitle(title);
				detail.setUrl(url);
				newsList.add(detail);
			}
		}
		// 遍历合并后的列表页集合TagMin,取出title和url封装到Detail中
		for (Map.Entry<String, ATagNodeInfo> entry : TagMin.entrySet()) {
			String title = aNodeStr(entry.getValue().getaNode());
			if (title != null) {
				String url = entry.getKey();
				Detail detail = new Detail();
				detail.setTitle(title);
				detail.setUrl(url);
				channels.add(detail);
			}
		}
		DetailResult detailResult = new DetailResult();
		detailResult.setChannels(channels);
		detailResult.setNewsList(newsList);
		return detailResult;
	}

	/*
	 * (non-Javadoc)获取新闻详细页和列表页集合
	 * 
	 * @see
	 * casia.isiteam.crawler.datapool.news.service.parse.ParsePage#analysisLinks
	 * (java.lang.String, java.lang.String)
	 */
	public DetailResult analysisLinks(String baseUrl, String html) {
		// TODO Auto-generated method stub
		Document doc = Jsoup.parse(html, baseUrl);
		final String sedurlDomain = DomainUtil.getDomain(baseUrl);
		doc.select("script").remove();
		doc.select("style").remove();
		doc.select("iframe").remove();
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
		HashMap<String, ATagNodeInfo> TagtNodeInfoList = new HashMap<String, ATagNodeInfo>();
		HashMap<String, ATagNodeInfo> UrltNodeInfoList = new HashMap<String, ATagNodeInfo>();
		for (Map.Entry<String, ArrayList<Node>> entry : xpathTagTNodeMap.entrySet()) {
			double tpr = computeTpr(entry.getValue());
			tagXpathTprList.add(tpr);
			String xpath = entry.getKey();
			ArrayList<Node> aNodeList = entry.getValue();
			for (Node aNode : aNodeList) {
				String url = aNode.absUrl("href");
				TagtNodeInfoList.put(url, new ATagNodeInfo(aNode, xpath, url, tpr));
			}
		}

		for (Map.Entry<String, ArrayList<Node>> entry : xpathURLTNodeMap.entrySet()) {
			double tpr = computeTpr(entry.getValue());
			URLXpathTprList.add(tpr);
			String xpath = entry.getKey();
			ArrayList<Node> aNodeList = entry.getValue();
			for (Node aNode : aNodeList) {
				String url = aNode.absUrl("href");
				UrltNodeInfoList.put(url, new ATagNodeInfo(aNode, xpath, url, tpr));
			}
		}
		DetailResult detailResult = classifiNews(TagtNodeInfoList, UrltNodeInfoList);
		return detailResult;
	}

	// 获取详情页的发布时间的文本节点集合
	private ArrayList<TextNode> timeTNodeList = new ArrayList<TextNode>();

	/**
	 * Title: childTextNode Description: 得到某个节点下的所有文本节点
	 * 
	 * @param node
	 * @return
	 */
	private ArrayList<TextNode> childTextNode(Node node) {
		final ArrayList<TextNode> cTNodeList = new ArrayList<TextNode>();
		new NodeTraversor(new NodeVisitor() {
			public void head(Node node, int depth) {
				if (node instanceof TextNode) {
					TextNode textNode = (TextNode) node;
					cTNodeList.add(textNode);
				}
			}

			public void tail(Node node, int depth) {
			}
		}).traverse(node);
		return cTNodeList;
	}

	/**
	 * Title: obtainTimeNode Description: 获取标题文本节点附近的发布时间
	 * 
	 * @param textNode
	 * @return
	 * @throws Exception
	 */
	private String obtainTimeNode(TextNode textNode) {
		// 标题节点的父节点
		Node pNode = textNode.parentNode();
		// 标题节点的父节点的兄弟节点
		List<Node> listNode = pNode.siblingNodes();
		for (Node bNode : listNode) {
			ArrayList<TextNode> cTNodeList = childTextNode(bNode);
			for (TextNode tNode : cTNodeList) {
				String str = tNode.text().trim();
				String publishInTime = null;
				try {
					publishInTime = DateUtil.getDateTime(str);
				} catch (Exception e) {
					System.out.println("文本节点不是时间的String，无法解析");
				}
				if (publishInTime != null) {
					return publishInTime;
				}
			}
		}
		// 标题节点的父节点的父节点
		Node parNode = pNode.parentNode();
		// 标题节点的父节点的父节点的兄弟节点
		List<Node> pbNode = parNode.siblingNodes();
		for (Node bbNode : pbNode) {
			ArrayList<TextNode> ccTNodeList = childTextNode(bbNode);
			for (TextNode ttNode : ccTNodeList) {
				String ttime = ttNode.text().trim();
				String publishOutTime = null;
				try {
					publishOutTime = DateUtil.getDateTime(ttime);
				} catch (Exception e) {
					System.out.println("文本节点不是时间的String，无法解析");
				}
				if (publishOutTime != null) {
					return publishOutTime;
				}
			}
		}
		return null;
	}

	/**
	 * Title: traverTextNodeList
	 * 
	 * Description: 遍历文本节点集合，相似度值较大的文本节点为标题文本节点
	 * 
	 * @param title
	 * @return
	 */
	private TextNode traverTextNodeList(String title) {
		HashMap<Double, TextNode> hm = new HashMap<Double, TextNode>();
		for (TextNode tNode : timeTNodeList) {
			if (!isEmptyNode(tNode)) {
				String ss = tNode.text().trim();
				double scTitle = SimilarSentenceChecker.sim(title, ss);
				hm.put(scTitle, tNode);
			}
		}
		if (hm.size() > 0) {
			double max = 0;
			for (Map.Entry<Double, TextNode> entry : hm.entrySet()) {
				if (entry.getKey() > max) {
					max = entry.getKey();
				}
			}
			for (Map.Entry<Double, TextNode> entry : hm.entrySet()) {
				if (entry.getKey() == max) {
					return entry.getValue();
				}
			}
		}
		return null;

	}

	/*
	 * (non-Javadoc)获取新闻详细页发布时间
	 * 
	 * @see casia.isiteam.crawler.datapool.news.service.parse.ParsePage#
	 * analysisNewsTime(java.lang.String, java.lang.String, java.lang.String)
	 */
	public String analysisNewsTime(String baseUrl, String html, String title) {
		// TODO Auto-generated method stub
		Document doc = Jsoup.parse(html, baseUrl);
		doc.traverse(new NodeVisitor() {
			public void head(Node node, int i) {
				if (node instanceof TextNode) {
					TextNode tNode = (TextNode) node;
					if (!isEmptyNode(tNode))
						timeTNodeList.add(tNode);
				}
			}

			public void tail(Node node, int i) {
			}

		});

		TextNode textNode = traverTextNodeList(title);
		if (!isEmptyNode(textNode)) {
			String time = obtainTimeNode(textNode);
			return time;
		}

		return null;
	}

	/**
	 * ClassName: CountInfo
	 *
	 * Description:正文提取中封装的文本节点的文本节点，文本长度，标点个数的类
	 * 
	 * @author guolongfei
	 * @date 2018年4月10日
	 */
	class CountInfo {
		TextNode tNode;
		public int textCount;
		public int puncCount;

		public CountInfo(TextNode tNode) {
			this.tNode = tNode;
			String text = tNode.text();
			this.textCount = countText(text);
			this.puncCount = countPunc(text);
		}
	}

	/**
	 * 
	 * ClassName: ComputeInfo
	 *
	 * Description:
	 * 
	 * tpr：相同标签路径的文本节点的总长度与相同标签路径的个数比
	 * 
	 * ppr：相同标签路径的文本节点的标点符号总个数与相同标签路径的个数比
	 * 
	 * cs：相同标签路径的文本节点的文本长度的标准差
	 * 
	 * ps：相同标签路径的文本节点的标点符号总数的标准差
	 * 
	 * etpr：扩展后的相同标签路径的文本节点的总长度与相同标签路径的个数比
	 * 
	 * @author guolongfei
	 * @date 2018年4月10日
	 */
	class ComputeInfo {

		double tpr;
		double ppr;
		double cs;
		double ps;
		double etpr;

		public ComputeInfo(double tpr, double ppr, double cs, double ps) {
			this.tpr = tpr;
			this.ppr = ppr;
			this.cs = cs;
			this.ps = ps;
			this.etpr = tpr * ppr * cs * ps;
		}
	}

	/** textNodeList：获取正文内容的文本节点集合 */
	private ArrayList<TextNode> textNodeList = new ArrayList<TextNode>();

	/** xpathMap：有相同标签路径的文本节点集合 */
	private HashMap<TextNode, String> xpathMap = new HashMap<TextNode, String>();

	/** countMap：有相同标签路径的文本节点的计算信息的类的集合 */
	private HashMap<String, ArrayList<CountInfo>> countMap = new HashMap<String, ArrayList<CountInfo>>();

	/**
	 * methodName: countText
	 *
	 * Description:计算文本的长度
	 * 
	 * @param text
	 * @return
	 */
	public static int countText(String text) {
		return text.trim().length();
	}

	/** puncs：标点符号数组 */
	public static char[] puncs = new char[] { ',', '.', ';', '\'', '\"', ',', '。', ';', '‘', '’', '“' };

	/**
	 * methodName: countPunc
	 *
	 * Description:计算文本的标点符号的个数
	 * 
	 * @param text
	 * @return
	 */
	public static int countPunc(String text) {
		text = text.trim();
		int sum = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			for (char punc : puncs) {
				if (punc == c) {
					sum++;
					break;
				}
			}
		}
		return sum;
	}

	/**
	 * methodName: getGaussWindow
	 *
	 * Description:高斯窗口函数
	 * 
	 * @param r
	 * @return
	 */
	public static double[] getGaussWindow(int r) {
		int size = 1 + r * 2;
		double[] window = new double[size];
		for (int i = -r; i <= r; i++) {
			window[i + r] = 1.0 / Math.pow(Math.E, (i * i + 0.0) / (2 * r * r));

		}
		return window;
	}

	/**
	 * methodName: gaussSmooth
	 *
	 * Description:高斯平滑
	 * 
	 * @param list
	 * @param r
	 * @return
	 */
	public static ArrayList<Double> gaussSmooth(ArrayList<Double> list, int r) {
		double[] window = getGaussWindow(r);
		double wSum = 0;
		for (double d : window) {
			wSum += d;
		}
		ArrayList<Double> result = new ArrayList<Double>();
		for (int i = 0; i < list.size(); i++) {
			if (i < r || i > list.size() - 1 - r) {
				result.add(list.get(i));
				continue;
			}
			double sum = 0;
			for (int j = -r; j <= r; j++) {
				int index = i + j;
				sum += window[j + r] * list.get(index);
			}
			double value = sum * window[r] / wSum;
			result.add(value);

		}
		return result;

	}

	/**
	 * methodName: computeDeviation
	 *
	 * Description:求标准差
	 * 
	 * @param list
	 * @return
	 */
	private double computeDeviation(ArrayList<Double> list) {
		if (list.size() == 0) {
			return 0;
		}
		double ave = 0;
		for (Double d : list) {
			ave += d;
		}
		ave = ave / list.size();
		double sum = 0;
		for (Double d : list) {
			sum += (d - ave) * (d - ave);
		}
		sum = sum / list.size();
		return Math.sqrt(sum);
	}

	/**
	 * methodName: getComputeInfo
	 *
	 * Description:计算tpr、ppr、cs、ps
	 * 
	 * @param countInfoList
	 * @return
	 */
	private ComputeInfo getComputeInfo(ArrayList<CountInfo> countInfoList) {
		double textSum = 0;
		double puncSum = 0;
		ArrayList<Double> textCountList = new ArrayList<Double>();
		ArrayList<Double> puncCountList = new ArrayList<Double>();
		for (CountInfo countInfo : countInfoList) {

			textSum += countInfo.textCount;
			puncSum += countInfo.puncCount;
			textCountList.add(countInfo.textCount + 0.0);
			puncCountList.add(countInfo.puncCount + 0.0);
		}
		double tpr = textSum / countInfoList.size();
		double ppr = puncSum / countInfoList.size();
		double cs = computeDeviation(textCountList);
		double ps = computeDeviation(puncCountList);
		return new ComputeInfo(tpr, ppr, cs, ps);
	}

	/**
	 * methodName: addTextNode
	 *
	 * Description:文本节点集合中添加文本节点，构建同一标签路径的文本节点集合
	 * 
	 * @param tNode
	 */
	private void addTextNode(TextNode tNode) {
		String text = tNode.text().trim();
		if (text.isEmpty()) {
			return;
		}
		String xpath = getXpath(tNode);
		textNodeList.add(tNode);
		xpathMap.put(tNode, xpath);
		CountInfo countInfo = new CountInfo(tNode);
		ArrayList<CountInfo> countInfoList = countMap.get(xpath);
		if (countInfoList == null) {
			countInfoList = new ArrayList<CountInfo>();
			countMap.put(xpath, countInfoList);
		}
		countInfoList.add(countInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see casia.isiteam.crawler.datapool.news.service.parse.ParsePage#
	 * analysisNewsContent(java.lang.String, java.lang.String)
	 */
	public ContentResult analysisNewsContent(String baseUrl, String html) {
		// TODO Auto-generated method stub
		Document doc = Jsoup.parse(html, baseUrl);
		doc.select("script").remove();
		doc.select("style").remove();
		doc.select("iframe").remove();
		doc.traverse(new NodeVisitor() {
			public void head(Node node, int i) {
				if (node instanceof TextNode) {
					TextNode tNode = (TextNode) node;
					addTextNode(tNode);
				}
			}

			public void tail(Node node, int i) {

			}
		});
		HashMap<String, ComputeInfo> computeMap = new HashMap<String, ComputeInfo>();
		for (Map.Entry<String, ArrayList<CountInfo>> entry : countMap.entrySet()) {
			ComputeInfo computeInfo = getComputeInfo(entry.getValue());
			computeMap.put(entry.getKey(), computeInfo);
		}
		ArrayList<Double> etprList = new ArrayList<Double>();
		for (TextNode tNode : textNodeList) {
			String xpath = xpathMap.get(tNode);
			double etpr = computeMap.get(xpath).etpr;
			etprList.add(etpr);
		}
		ArrayList<Double> gaussEtprList = gaussSmooth(etprList, 1);
		double threshold = computeDeviation(gaussEtprList) * 0.8;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < textNodeList.size(); i++) {
			TextNode tNode = textNodeList.get(i);
			double gEtpr = gaussEtprList.get(i);
			if (gEtpr > threshold) {
				sb.append(tNode.text().trim() + "\n");
			}
		}
		ContentResult contentResult = new ContentResult();
		contentResult.setContent(sb.toString());
		return contentResult;
	}

}
