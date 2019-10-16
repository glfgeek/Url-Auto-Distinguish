/**  
* @author guolongfei  
* @date 2018年4月2日  
* @version 1.0
*/
package com.glf.news.extractor.NewsExtractor;

import org.jsoup.nodes.Node;

/**
 * 
 * Title: TextNodeInfo Description:
 * 
 * @author guolongfei
 * @date 2018年4月2日
 */
public class TagNodeInfo {
	private Node aNode;
	private double tpr;

	public TagNodeInfo(Node aNode, double tpr) {
		this.aNode = aNode;
		this.tpr = tpr;
	}

	public Node getaNode() {
		return aNode;
	}

	public double getTpr() {
		return tpr;
	}
}
