//
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//import net.sf.json.JSONObject;
//
//import org.w3c.dom.svg.SVGDocument;
//
//public class Elements {
//	private Map<String, Tag> elements = new LinkedHashMap<String, Tag>();
//
//	public Elements(JSONObject elsJson) {
//		init(elsJson);
//	}
//
//	public void init(JSONObject elsJson) {
//		String iStr = "";
//		for (int i = 0, len = elsJson.size(); i < len; ++i) {
//			iStr = String.valueOf(i);
//			if (elsJson.getJSONObject(iStr).containsKey("type") 
//					&& !"background".equals(elsJson.getJSONObject(iStr).getString("type")) ) {
//				this.elements.put(iStr, new Tag(elsJson.getJSONObject(iStr)));
//				
//			}
//		}
//		iStr = null;
//		elsJson = null;
//	}
//
//	public SVGDocument toSVG(SVGDocument doc) {
//		for (Map.Entry<String, Tag> m : elements.entrySet()) {
//			m.getValue().toSVG(doc);
//		}
//	
//		return doc;
//	}
//
//	public Map<String, Tag> getElements() {
//		return elements;
//	}
//
//	public void setElements(Map<String, Tag> elements) {
//		this.elements = elements;
//	}
//	
//	public int size() {
//		return this.elements.size();
//	}
//}
