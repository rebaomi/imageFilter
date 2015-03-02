//
//
//import net.sf.json.JSONObject;
//
//import org.w3c.dom.Element;
//import org.w3c.dom.svg.SVGDocument;
//
//import com.eyuanku.web.framework.svg.SvgImage;
//import com.eyuanku.web.framework.svg.config.SVGConfig;
//
//public class Page {
//
//	private String width;
//	
//	private String height;
//	
//	private String bkColor;
//	
//	private Elements elements;
//	
//	private String x = SvgImage.x;
//	
//	private String y = SvgImage.y;
//	
//	private static final String[] TAG = new String[]{"width","height","backgroundColor"};
//	
//	public Page(JSONObject pJson) {
//		init(pJson);
//	}
//	
//	public void init(JSONObject pJson) {
//		SVGConfig.validateJson(TAG, pJson);
//		this.width = pJson.getString("width");
//		this.height = pJson.getString("height");
//		this.elements = new Elements(pJson.getJSONObject("elements"));
//		this.bkColor = pJson.containsKey("backgroundColor") ? pJson.getString("backgroundColor") : SvgImage.DEFAULT_BACKGROUND;
//		pJson = null;
//	}
//	
//	public SVGDocument toSVG() {
//		SVGDocument doc = SvgImage.createRootDocument();
//		Element svgRoot = doc.getDocumentElement();
//		svgRoot.setAttributeNS(null, "width", width);
//		svgRoot.setAttributeNS(null, "height", height);
//		svgRoot.setAttributeNS(null, "viewBox", x+SvgImage.sep+y+SvgImage.sep+width+SvgImage.sep+height);
//		svgRoot.setAttributeNS(null, "shape-rendering", "geometricPrecision");
//
//		Element rectBk = doc.createElement("rect");
//		rectBk.setAttributeNS(null, "width", width);
//		rectBk.setAttributeNS(null, "height", height);
//		if(bkColor!=null) {
//			rectBk.setAttributeNS(null, "style", "fill:"+bkColor);
//		}
////		rectBk.setAttributeNS(null, "stroke", "#FFFFFF");
////		rectBk.setAttributeNS(null, "stroke-miterlimit", "1");
////		rectBk.setAttributeNS(null, "fill", "none");
//		
//		svgRoot.appendChild(rectBk);
//		rectBk = null;
//		elements.toSVG(doc);
//		return doc;
//	}
//	
//	public String getWidth() {
//		return width;
//	}
//
//	public void setWidth(String width) {
//		this.width = width;
//	}
//
//	public String getHeight() {
//		return height;
//	}
//
//	public void setHeight(String height) {
//		this.height = height;
//	}
//
//	public Elements getElements() {
//		return elements;
//	}
//
//	public void setElements(Elements elements) {
//		this.elements = elements;
//	}
//
//	public String getX() {
//		return x;
//	}
//
//	public void setX(String x) {
//		this.x = x;
//	}
//
//	public String getY() {
//		return y;
//	}
//
//	public void setY(String y) {
//		this.y = y;
//	}
//
//	public String toString() {
//		return SvgImage.dom2Str(toSVG());
//	}
//	
//	public int size() {
//		return this.elements.size();
//	}
//}
