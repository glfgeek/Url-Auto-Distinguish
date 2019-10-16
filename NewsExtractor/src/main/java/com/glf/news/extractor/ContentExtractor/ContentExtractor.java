package com.glf.news.extractor.ContentExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

import com.glf.news.extractor.NewsExtractor.HtmlExtractor;

public class ContentExtractor {

	public class CountInfo {
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

	public class ComputeInfo {

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

	private ArrayList<TextNode> tNodeList = new ArrayList<TextNode>();
	private HashMap<TextNode, String> xpathMap = new HashMap<TextNode, String>();
	private HashMap<String, ArrayList<CountInfo>> countMap = new HashMap<String, ArrayList<CountInfo>>();

	public static int countText(String text) {
		return text.trim().length();
	}

	public static boolean isEmptyNode(TextNode node) {
		int count = countText(node.text());
		return count == 0;
	}

	public static char[] puncs = new char[] { ',', '.', ';', '\'', '\"', ',', '。', ';', '‘', '’', '“' };

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

	private static String getNodeName(Node node) {
		if (node instanceof TextNode) {
			return "text";
		} else {
			Element element = (Element) node;
			return element.tagName().toLowerCase();
		}
	}

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

	public static double[] getGaussWindow(int r) {
		int size = 1 + r * 2;
		double[] window = new double[size];
		for (int i = -r; i <= r; i++) {
			window[i + r] = 1.0 / Math.pow(Math.E, (i * i + 0.0) / (2 * r * r));

		}
		return window;
	}

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

	private void addTextNode(TextNode tNode) {

		String text = tNode.text().trim();
		if (StringUtils.isBlank(text)) {
			return;
		}
		String xpath = getXpath(tNode);
		tNodeList.add(tNode);
		xpathMap.put(tNode, xpath);

		CountInfo countInfo = new CountInfo(tNode);
		ArrayList<CountInfo> countInfoList = countMap.get(xpath);
		if (countInfoList == null) {
			countInfoList = new ArrayList<CountInfo>();
			countMap.put(xpath, countInfoList);
		}
		countInfoList.add(countInfo);
	}

	private String buildHisto(String html, String url) {
		Document doc = Jsoup.parse(html, url);
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
		for (TextNode tNode : tNodeList) {
			String xpath = xpathMap.get(tNode);
			double etpr = computeMap.get(xpath).etpr;
			etprList.add(etpr);
		}
		ArrayList<Double> gaussEtprList = gaussSmooth(etprList, 1);
		double threshold = computeDeviation(gaussEtprList) * 0.8;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tNodeList.size(); i++) {
			TextNode tNode = tNodeList.get(i);
			double gEtpr = gaussEtprList.get(i);
			if (gEtpr > threshold) {
				sb.append(tNode.text().trim() + "\n");
			}
		}
		return sb.toString();

	}

	public static void main(String[] args) throws Exception {
		String url = "https://news.ifeng.com/c/7q2MdYHQG48";
		String html = HtmlExtractor.getHtml(url);
		String str = new ContentExtractor().buildHisto(html, url);
		System.out.println(str);
	}

}
