package com.glf.news.extractor.NewsExtractor;

import java.io.Serializable;
import java.util.List;

public class DetailResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<Detail> channels;

	private List<Detail> newsList;

	public List<Detail> getChannels() {
		return channels;
	}

	public void setChannels(List<Detail> channels) {
		this.channels = channels;
	}

	public List<Detail> getNewsList() {
		return newsList;
	}

	public void setNewsList(List<Detail> newsList) {
		this.newsList = newsList;
	}

}
