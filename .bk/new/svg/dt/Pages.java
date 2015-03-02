package com.eyuanku.web.framework.svg.dt;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.w3c.dom.svg.SVGDocument;

import com.eyuanku.web.framework.svg.SvgSeed;
import com.eyuanku.web.framework.svg.Utils;

public class Pages {

	private Map<String, Page> pages = new LinkedHashMap<String, Page>();
	
	public Pages(JSONObject pagesJson) {
		for(int i = 0, len = pagesJson.size(); i<len; i++){
			String iStr = String.valueOf(i);
			JSONObject pageJson = pagesJson.getJSONObject(iStr);
			Page page = new Page(pageJson);
			pages.put(iStr, page);
		}
	}

	public Map<Integer, SVGDocument> toSvg(SvgSeed seed){
		Map<Integer, SVGDocument> pagesSVG = new LinkedHashMap<Integer, SVGDocument>();
		for(Map.Entry<String, Page> m : pages.entrySet()) {
			pagesSVG.put(Integer.valueOf(m.getKey()), m.getValue().toSVG(seed));
		}
		return pagesSVG;
	}

	public Map<Integer, String> toSvgString(SvgSeed seed){
		Map<Integer, SVGDocument> pagesSvg = toSvg(seed);
		Map<Integer, String> pagesSvgStr = new LinkedHashMap<Integer, String>();
		for(Map.Entry<Integer, SVGDocument> m : pagesSvg.entrySet()) {
			pagesSvgStr.put(Integer.valueOf(m.getKey()), Utils.dom2Str(m.getValue()));
		}
		return pagesSvgStr;
	}

}
