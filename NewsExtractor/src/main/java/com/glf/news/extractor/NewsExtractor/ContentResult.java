package com.glf.news.extractor.NewsExtractor;

import java.io.Serializable;
import java.util.List;

public class ContentResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String content;
	private String contentHtml;
	private List<Picture> pics;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentHtml() {
		return contentHtml;
	}

	public void setContentHtml(String contentHtml) {
		this.contentHtml = contentHtml;
	}

	public List<Picture> getPics() {
		return pics;
	}

	public void setPics(List<Picture> pics) {
		this.pics = pics;
	}

}
