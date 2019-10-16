package com.glf.news.extractor.NewsExtractor;

import java.io.Serializable;

public class Detail implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String url;
	private String title;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
