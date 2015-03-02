//
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//import net.sf.json.JSONObject;
//
//import org.w3c.dom.svg.SVGDocument;
//
//public class Pages {
//	private Map<String, Page> pages = new LinkedHashMap<String, Page>();
//	
//	public Pages(JSONObject psJson) {
//		init(psJson);
//	}
//	
//	public void init(JSONObject psJson) {
//		for(int i = 0,len = psJson.size();i<len;++i){
//			String iStr = String.valueOf(i);
//			JSONObject page = psJson.getJSONObject(iStr);
//			if(!page.getJSONObject("elements").isEmpty()) {
//				this.pages.put(iStr, new Page(page));
//			}
//		}
//	}
//	
//	public Map<Integer, SVGDocument> toSVG(){
//		Map<Integer, SVGDocument> pagesSVG = new LinkedHashMap<Integer, SVGDocument>();
//		for(Map.Entry<String, Page> m : pages.entrySet()) {
//			pagesSVG.put(Integer.valueOf(m.getKey()), m.getValue().toSVG());
//		}
//		return pagesSVG;
//	}
//	
//	public Map<String, Page> getPages() {
//		return pages;
//	}
//
//	public void setPages(Map<String, Page> pages) {
//		this.pages = pages;
//	}
//	
//	public int size() {
//		return this.pages.size();
//	}
//}
