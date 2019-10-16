/**  
* @author guolongfei  
* @date 2018年4月11日  
* @version 1.0
*/
package com.glf.news.extractor.NewsExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * ClassName: IdeaTest
 *
 * Description:
 * 
 * @author guolongfei
 * @date 2018年4月11日
 */
public class IdeaTest {

	private Map<String, String> mapaa = new HashMap<String, String>();

	private void bbb(String url, String str) {
		String aa = mapaa.get(url);
		if (aa == null) {
			aa = new String();
		}
		aa = str;
		mapaa.put(url, aa);
	}

	@Test
	public void testaa() {
		String url = "aaa";
		String str1 = "aa11";
		// String str2 = "aa22";
		// String str3 = "aa22";
		bbb(url, str1);
		// bbb(url, str2);
		// bbb(url, str3);
		for (Map.Entry<String, String> entry : mapaa.entrySet()) {
			System.out.println("key:" + entry.getKey() + ",value:" + entry.getValue());
		}
	}

	private Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();

	private void aaa(String url, String str) {
		ArrayList<String> aa = map.get(url);
		if (aa == null) {
			aa = new ArrayList<String>();
			map.put(url, aa);
		}
		aa.add(str);
	}

	@Test
	public void test() {
		String url = "aaa";
		String str1 = "aa11";
		String str2 = "aa22";
		String str3 = "aa22";
		aaa(url, str1);
		aaa(url, str2);
		aaa(url, str3);
		for (Map.Entry<String, ArrayList<String>> entry : map.entrySet()) {
			for (String str : entry.getValue()) {
				System.out.println(str);
			}
		}
	}

}
