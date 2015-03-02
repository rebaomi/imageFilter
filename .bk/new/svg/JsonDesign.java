package com.eyuanku.web.framework.svg;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.w3c.dom.svg.SVGDocument;

import com.eyuanku.web.framework.exce.GlobalException;
import com.eyuanku.web.framework.svg.dt.DefaultFont;
import com.eyuanku.web.framework.svg.dt.Pages;

public class JsonDesign {

//	private String title = "";
	private Pages pages = null;
	private Map<String, DefaultFont> defaultFonts = new LinkedHashMap<String, DefaultFont>();
	private List<String> backgroundColor = new ArrayList<String>();
	private List<String> canvaColor = new ArrayList<String>();
	
	public JsonDesign(JSONObject json) {
		JSONObject pagesJson = json.getJSONObject("pages");
		pages = new Pages(pagesJson);
//		this.title = json.getString("title");
		
		if(json.containsKey("defaultFonts")) {
			JSONObject df = json.getJSONObject("defaultFonts");
			for(int i = 0,len = df.size();i<len;++i) {
				String iStr = String.valueOf(i);
				this.defaultFonts.put(iStr, new DefaultFont(df.getJSONObject(iStr)));
			}
		}
		if(json.containsKey("backgroundColor")) {
			JSONArray bc = json.getJSONArray("backgroundColor");
			for(int i = 0,len = bc.size();i<len;++i) {
				this.backgroundColor.add(bc.getString(i));
			}
		}
		if(json.containsKey("canvaColor")) {
			JSONArray cc = json.getJSONArray("canvaColor");
			for(int i = 0,len = cc.size();i<len;++i) {
				this.canvaColor.add(cc.getString(i));
			}
		}
	}

	public Map<Integer, SVGDocument> toSvg(SvgSeed seed) {
		return pages.toSvg(seed);
	}

	public static Map<Integer, String> svg2String(Map<Integer, SVGDocument> pagesSvg) {
		Map<Integer, String> pagesSvgStr = new LinkedHashMap<Integer, String>();
		for(Map.Entry<Integer, SVGDocument> m : pagesSvg.entrySet()) {
			pagesSvgStr.put(Integer.valueOf(m.getKey()), Utils.dom2Str(m.getValue()));
		}
		return pagesSvgStr;
	}

	public static Map<Integer, InputStream> string2Stream(Map<Integer, SVGDocument> pagesSvg)  {
		Map<Integer, InputStream> pagesSvgStream = new LinkedHashMap<Integer, InputStream>();
		for(Map.Entry<Integer, SVGDocument> m : pagesSvg.entrySet()) {
			try {
				pagesSvgStream.put(Integer.valueOf(m.getKey()), new ByteArrayInputStream(Utils.dom2Str(m.getValue()).getBytes("UTF-8")));
			} catch (UnsupportedEncodingException e) {
				throw new GlobalException("不支持的编码错误：", e);
			}
		}
		return pagesSvgStream;
	}

}
