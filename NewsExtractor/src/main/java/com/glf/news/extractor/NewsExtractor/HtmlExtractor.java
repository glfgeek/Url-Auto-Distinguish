/**  
* @author guolongfei  
* @date 2018年3月30日  
* @version 1.0
*/
package com.glf.news.extractor.NewsExtractor;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * 
 * <p>
 * Title: HtmlExtractor
 * </p>
 * <p>
 * Description:
 * </p>
 * 
 * @author guolongfei
 * @date 2018年3月30日
 */
public class HtmlExtractor {

	public static String getHtml(String url) throws ParseException, IOException {
		RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD)
				.setConnectionRequestTimeout(6000).setConnectTimeout(6000).build();
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).build();
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response = httpClient.execute(httpGet);
		try {
			HttpEntity entity = response.getEntity();
			String html = EntityUtils.toString(entity, "UTF-8");
			return html;
		} finally {
			if (response != null) {
				response.close();
			}
			httpClient.close();
		}

	}
}
