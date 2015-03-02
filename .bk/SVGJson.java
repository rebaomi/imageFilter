//package com.eyuanku.web.framework.svg.parse;
//
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//import net.sf.json.JSONArray;
//import net.sf.json.JSONObject;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.w3c.dom.svg.SVGDocument;
//
//import com.eyuanku.web.framework.svg.SvgImage;
//import com.eyuanku.web.framework.svg.config.SVGConfig;
//
//public class SVGJson {
//
//	private Pages pages;
//	private Map<String, DefaultFont> defaultFonts = new LinkedHashMap<String, DefaultFont>();
//	private List<String> backgroundColor = new ArrayList<String>();
//	private List<String> canvaColor = new ArrayList<String>();;
//	private String title;
//	private static final String[] TAG = new String[]{"title"};
//	
//	private static Logger log = LoggerFactory.getLogger(SVGJson.class);
//	
//	public SVGJson(JSONObject svgJson) {
//		init(svgJson);
//	}
//	
//	public void init(JSONObject svgJson) {
//		SVGConfig.validateJson(TAG, svgJson);
//		this.pages = new Pages(svgJson.getJSONObject("pages"));
//		this.title = svgJson.getString("title");
//		if(svgJson.containsKey("defaultFonts")) {
//			JSONObject df = svgJson.getJSONObject("defaultFonts");
//			for(int i = 0,len = df.size();i<len;++i) {
//				String iStr = String.valueOf(i);
//				this.defaultFonts.put(iStr, new DefaultFont(df.getJSONObject(iStr)));
//			}
//		}
//		if(svgJson.containsKey("backgroundColor")) {
//			JSONArray bc = svgJson.getJSONArray("backgroundColor");
//			for(int i = 0,len = bc.size();i<len;++i) {
//				this.backgroundColor.add(bc.getString(i));
//			}
//		}
//		if(svgJson.containsKey("canvaColor")) {
//			JSONArray cc = svgJson.getJSONArray("canvaColor");
//			for(int i = 0,len = cc.size();i<len;++i) {
//				this.canvaColor.add(cc.getString(i));
//			}
//		}
//		svgJson = null;
//	}
//
//	public EykSVG toSVG() {
//		Pages pages = this.pages;
//		
//		LinkedHashMap<Integer, String> svgMap =  new LinkedHashMap<Integer, String>();
//		
//		LinkedHashMap<Integer, SVGDocument> svgDocMap =  (LinkedHashMap<Integer, SVGDocument>)pages.toSVG();
//		
//		for(Map.Entry<Integer, SVGDocument> e : svgDocMap.entrySet()) {
//			String svgStr = SvgImage.dom2Str(e.getValue());
//			log.info("svg:\n{}",svgStr);
//			System.out.println(svgStr);
//			svgMap.put(e.getKey(), svgStr);
//		}
//		
//		svgDocMap.clear();
//		return new EykSVG(svgMap);
//	}
//	
//	public Map<Integer, InputStream> toStream() {
//		return toSVG().toStream();
//	}
//	
//	public Pages getPages() {
//		return pages;
//	}
//
//	public void setPages(Pages pages) {
//		this.pages = pages;
//	}
//
//	public Map<String, DefaultFont> getDefaultFonts() {
//		return defaultFonts;
//	}
//
//	public void setDefaultFonts(Map<String, DefaultFont> defaultFonts) {
//		this.defaultFonts = defaultFonts;
//	}
//
//	public List<String> getBackgroundColor() {
//		return backgroundColor;
//	}
//
//	public void setBackgroundColor(List<String> backgroundColor) {
//		this.backgroundColor = backgroundColor;
//	}
//
//	public List<String> getCanvaColor() {
//		return canvaColor;
//	}
//
//	public void setCanvaColor(List<String> canvaColor) {
//		this.canvaColor = canvaColor;
//	}
//
//	public String getTitle() {
//		return title;
//	}
//
//	public void setTitle(String title) {
//		this.title = title;
//	}
//
//	public int size() {
//		return this.pages.size();
//	}
//}
