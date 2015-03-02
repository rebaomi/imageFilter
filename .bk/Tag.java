//
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//import net.sf.json.JSONObject;
//
//import org.apache.batik.bridge.BridgeException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.w3c.dom.svg.SVGDocument;
//
//import com.eyuanku.web.framework.storage.file.StorageBtype;
//import com.eyuanku.web.framework.svg.SvgImage;
//import com.eyuanku.web.framework.svg.config.SVGConfig;
//
//public class Tag {
//	
//	private static Logger logger = LoggerFactory.getLogger(Tag.class);
//	
//	private Integer elementsIndex;
//	private String imgKey;
//	private String height;
//	private String width;
//	private String left;
//	private String top;
//	private String scale;
//	private String rotateX;
//	private String rotateY;
//	private String type;
//	private String italic;
//	private String bold;
//	private String underline;
//	private Map<String, String> fillColors;
//
//	private Map<String, String> fontFamily;
//
//	private Map<String, Double> fontSize;
//	private Map<String, String> content;
//	private Map<String, String> fontPosition;
//	private Map<String, String> fontLetter;
//	private String href;
//	private Map<String, String> svgHref;
//
//	private String opacity;
//
//	private JSONObject tagJson;
//	
//	private static final String[] TAG = new String[]{"height","width","left","top","scale","type","opacity"};
//
//	@SuppressWarnings("unchecked")
//	public Tag(JSONObject tagJson) {
//		SVGConfig.validateJson(TAG, tagJson);
//		this.elementsIndex = tagJson.getInt("elementsIndex");
//		
//		this.imgKey = tagJson.containsKey("img_key") ? tagJson
//				.getString("img_key") : null;
//				
//		this.height = tagJson.containsKey("height") ? tagJson
//				.getString("height") : "0";
//				
//		this.width = tagJson.containsKey("width") ? tagJson.getString("width")
//				: "0";
//		
//		this.left = tagJson.containsKey("left") ? tagJson.getString("left")
//				: "0";
//		
//		this.top = tagJson.containsKey("top") ? tagJson.getString("top") : "0";
//		
//		this.scale = tagJson.containsKey("scale") ? tagJson.getString("scale")
//				: "0";
//		
//		this.italic = tagJson.containsKey("italic") ? tagJson.getString("italic")
//				: "0";
//		
//		this.bold = tagJson.containsKey("bold") ? tagJson.getString("bold")
//				: "0";
//		
//		this.underline = tagJson.containsKey("underline") ? tagJson.getString("underline")
//				: "0";
//		
//		this.rotateX = tagJson.containsKey("rotateX") ? tagJson
//				.getString("rotateX") : "0";
//		
//		this.rotateY = tagJson.containsKey("rotateY") ? tagJson
//				.getString("rotateY") : "0";
//		
//		this.type = tagJson.getString("type");
//		
//		this.opacity = tagJson.containsKey("opacity") ? tagJson
//				.getString("opacity") : "1";
//				
//		this.fillColors = (Map<String, String>) JSONObject.toBean(
//				tagJson.getJSONObject("fillColors"), LinkedHashMap.class);
//
//		this.fontFamily = (Map<String, String>) JSONObject.toBean(
//				tagJson.getJSONObject("fontFamily"), LinkedHashMap.class);
//		
//		this.fontSize = (Map<String, Double>) JSONObject.toBean(
//				tagJson.getJSONObject("fontSize"), LinkedHashMap.class);
//
//		this.content = (Map<String, String>) JSONObject.toBean(
//				tagJson.getJSONObject("content"), LinkedHashMap.class);
//
//		this.fontPosition = (Map<String, String>) JSONObject.toBean(
//				tagJson.getJSONObject("fontPosition"), LinkedHashMap.class);
//		
//		this.fontLetter = (Map<String, String>) JSONObject.toBean(
//				tagJson.getJSONObject("fontLetter"), LinkedHashMap.class);
//
//		this.href = tagJson.containsKey("href") ? tagJson.getString("href")
//				: "";
//		this.svgHref = (Map<String, String>) JSONObject.toBean(
//				tagJson.getJSONObject("svgHref"), LinkedHashMap.class);
//		
//		this.tagJson = tagJson;
//	}
//
//	public SVGDocument toSVG(SVGDocument doc) {
//		Element root = doc.getDocumentElement();
//		
//		Node insertNode = insertNode(doc);
//		
//		root.appendChild(insertNode);
//		
//		insertNode = null;		
//		return doc;
//	}
//
//	public Node insertNode(SVGDocument doc) {
//		Node node = null;
//		
//		switch (type) {
//		case "text":
//			node = insertTextNode(doc);
//			break;
//		case "img":
//			node = insertImgNode(doc);
//			// insertImgAntialiasing(doc);
//			break;
//		case "svg":
//			node = insertSVGNode(doc);
//			break;
//		default:
//			break;
//		}
//		return node;
//	}
//
//	public Node insertNullNode(SVGDocument doc) {
//		Element nullNode = doc.createElement("text");
//		nullNode.setAttributeNS(null, "width", "100");
//		nullNode.setAttributeNS(null, "height", "100");
//		nullNode.setAttributeNS(null, "fill", "black");
//		nullNode.setAttributeNS(null, "font-family", "微软雅黑");
//		nullNode.setAttributeNS(null, "font-size", "20");
//
//		Node text = doc.createTextNode("无法识别该素材类型");
//		nullNode.appendChild(text);
//
//		return nullNode;
//	}
//
//	public Node insertTextNode(SVGDocument doc) {
//		Element textNode = doc.createElement("text");
//		textNode.setAttribute("id", imgKey);
//		textNode.setAttribute("xml:space","preserve");
//		setCommonAttr(textNode);
//		
//		setTransformAttr(textNode);
//		textNode.setAttribute("style", "fill:"
//				+ (fillColors.containsKey("0") ? fillColors.get("0") : ""));
//		textNode.setAttribute("font-family",
//				fontFamily.containsKey("0") ? "'"+fontFamily.get("0")+"'" : "");
//		
//		if(fontSize.containsKey("0") && ! "null".equals(String.valueOf(fontSize.get("0"))) ){
//			textNode.setAttribute("font-size", String.valueOf(fontSize.get("0")));
//		}
//		for (Map.Entry<String, String> e : content.entrySet()) {
//			Element tspan = doc.createElement("tspan");
//			tspan.setAttributeNS(null, "x", left);
////			tspan.setAttributeNS(null, "dx", "0");
//			
////			String content2 = e.getValue();
////			
////			byte bytes[] = {(byte) 0xC2,(byte) 0xA0};
////            String UTFSpace;
////            String newCont;
////			try {
////				UTFSpace = new String(bytes,"UTF-8");
////				newCont = content2.replaceAll(UTFSpace, " ");
////				System.out.println(newCont + "--------" + content2+"-----");
////			} catch (UnsupportedEncodingException e1) {
////				throw new GlobalException(e1);
////			}
//			
////			List<String> dx = new LinkedList<String>();
////			int i = -1;
////			int counter = 0;
////			double  spaceWidth = 0.5;
////			while(++i < content2.length()) {
////			    if (content2.charAt(i) == ' ') {
////			      // 空格判断
////			      counter++;
////			    } else {
////			      dx.add(counter * 0.5 + "em");
////			      counter = 0;
////			    }
////			  }
////			StringBuilder dxsb = new StringBuilder();
////			for(int j = 0;j<dx.size();j++){
////				
////				dxsb.append(j==0 ? dx.get(j) : "," + dx.get(j));
////			}
////			tspan.setAttributeNS(null, "dx", dxsb.toString());
//			
//			
//			boolean isFirstLine = "0".equals(e.getKey());
//			int s = content.size();
//			if (s == 0) s = 1;
//			logger.info(height);
//			Double dy = null;
//			if (isFirstLine) {
//				Object obj = fontSize.get("0");
//				if (obj != null && obj instanceof Double) {
//					dy = (Double)obj;
//				} else if (obj != null && obj instanceof Integer) {
//					dy = ((Integer)obj).doubleValue();
//				} else {
//					dy = new Double(12);
//				}
//			} else {
//				dy = new Double(Double.parseDouble(height) / s);
//			}
//			tspan.setAttributeNS(null, "dy", dy.toString());
//			tspan.setAttributeNS(null, "text-anchor",fontPosition.get("0"));
//			tspan.setAttributeNS(null, "style", "inline-height:15px;letter-spacing:" + fontLetter.get("0") + ";" + ("1".equals(italic) ? "font-style:italic;" : "") + ("1".equals(bold) ? "font-weight:bold;" : "") + ("1".equals(underline) ? "text-decoration:underline;" : ""));
//			Node text = doc.createTextNode(null);
//
//			text.setNodeValue(e.getValue());
//			tspan.appendChild(text);
//			textNode.appendChild(tspan);
//		}
//		return textNode;
//	}
//
//	public Node insertImgNode(SVGDocument doc) {
//		if (imgKey == null) {
//			return insertNullNode(doc);
//		}
//		Element image = doc.createElement("image");
//		image.setAttribute("id", imgKey);
//
//		image.setAttribute("xlink:href",
//				SVGConfig.generateSign(StorageBtype.MATERIALS, imgKey));
////		String filterUrl = SVGConfig.generateFilterSign(StorageBtype.MATERIALS, imgKey, type);// "sketch"
////		System.out.println(filterUrl);
////		image.setAttribute("xlink:href", filterUrl);
//		setCommonAttr(image);
//		
//		setTransformAttr(image);
//		return image;
//	}
//
//	public Node insertSVGNode(SVGDocument doc) {
//		if (imgKey == null) {
//			return insertNullNode(doc);
//		}
//		SvgImage svgImage = null;
//		String imgUrl = SVGConfig.getSVGUrl(imgKey, StorageBtype.MATERIALS);
//
//		Element subRoot = null;
//		try {
//			if(svgHref == null || "{}".equals(svgHref)){
//				svgImage = new SvgImage(imgUrl, false);
//				SVGDocument insertSVGDoc = svgImage.getSvgDocument();
//				subRoot = insertSVGDoc.getRootElement();
//				insertSVGDoc = null;
//			} else {
//				svgImage = new SvgImage(imgUrl, true);
//				Document insertDoc = svgImage.getDocument();
//				subRoot = insertDoc.getDocumentElement();
//				insertDoc = null;
//			}
//		} catch (BridgeException e) {
//			svgImage = new SvgImage(imgUrl, true);
//			Document insertDoc = svgImage.getDocument();
//			subRoot = insertDoc.getDocumentElement();
//			insertDoc = null;
//		}
//
//		svgImage = null;
//		Element newSub = handleSVG(subRoot);
//		Node insertNode = doc.importNode(newSub, true);
//
//		newSub = null;
//
//		return insertNode;
//
//	}
//
//	public Element handleSVG(Element subRoot) {
//		NodeList gList = subRoot.getElementsByTagName("g");
//		
//		setTransformAttr((Element) gList.item(0));
//		
//		String[] shapes = SVGConfig.SHAPES;
//
//		for (String s : shapes) {
//			NodeList nodeList = subRoot.getElementsByTagName(s);
//			for (int i = 0; i < nodeList.getLength(); i++) {
//				Element cur = (Element) nodeList.item(i);
//				if (cur.hasAttribute("class")) {
//					String cVal = cur.getAttribute("class");
//					
//					if (fillColors != null && fillColors.containsKey(cVal)) {
//						cur.setAttribute("fill", fillColors.get(cVal));
//					}
//					if (fontFamily != null && fontFamily.containsKey(cVal)) {
//						cur.setAttribute("font-family", "'"+fontFamily.get(cVal)+"'");
//					}
//					if (fontSize != null && fontSize.containsKey(cVal)) {
//						cur.setAttribute("font-size",
//								String.valueOf(fontSize.get(cVal)));
//					}
//					if ("image".equals(s) && svgHref != null
//							&& svgHref.containsKey(cVal)) {
//						
//						JSONObject attrSVG = JSONObject.fromObject(svgHref
//								.get(cVal));
//						if (attrSVG.containsKey("img_key")) {
//							// 消除svg素材本身transform对吸附图片的显示影响
//							cur.removeAttribute("transform");
//							
//							cur.setAttribute("xlink:href", SVGConfig.generateSign(StorageBtype.MATERIALS, attrSVG.getString("img_key")));
//							cur.setAttribute("width",attrSVG.containsKey("width") ? attrSVG.getString("width") : "");
//							cur.setAttribute("height",attrSVG.containsKey("height") ? attrSVG.getString("height") : "");
//							cur.setAttribute("x", attrSVG.containsKey("x") ? attrSVG.getString("x") : "");
//							cur.setAttribute("y", attrSVG.containsKey("y") ? attrSVG.getString("y") : "");
//						}
//					}
//				}
//			}
//			nodeList = null;
//		}
//		gList = null;
//
//		setCommonAttr(subRoot);
//		subRoot.setAttribute("overflow", "visible");
//
//		return subRoot;
//	}
//
//	@Deprecated
//	public Node insertSVGLayoutNode(SVGDocument doc) {
//		return insertSVGNode(doc);
//	}
//
//	@Deprecated
//	public Node insertSVGTextNode(SVGDocument doc) {
//		return insertSVGNode(doc);
//	}
//
//	public void setTransformAttr(Element cur) {
//		if (scale != null ) {
//			if(!"null".equals(rotateX)
//					&& !"null".equals(rotateY)){
//				cur.setAttribute("transform", "rotate(" + scale
//						+ "," + rotateX + "," + rotateY + ")");
//			} else {
//				cur.setAttribute("transform", "rotate(" + scale + ")");
//			}
//		}
//	}
//	
//	public void setCommonAttr(Element cur) {
//		cur.setAttribute("width", width);
//		cur.setAttribute("height", height);
//		cur.setAttribute("x", left);
//		cur.setAttribute("y", top);
//		cur.setAttribute("opacity", opacity);
//	}
//	
//	@Deprecated
//	public void insertImgAntialiasing(SVGDocument doc) {
//		Element rect = doc.createElement("rect");
//		rect.setAttribute("fill", "none");
//		rect.setAttribute("stroke", "none");
//		rect.setAttribute("x", left);
//		rect.setAttribute("y", top);
//		rect.setAttribute("stroke-miterlimit", "1");
//		rect.setAttribute("height", height + "0");
//		rect.setAttribute("width", width + "0");
//		if (scale != null && !"null".equals(rotateX)
//				&& !"null".equals(rotateY)) {
//			rect.setAttribute("transform", "rotate(" + scale + "," + rotateX
//					+ "," + rotateY + ")");
//		}
//		doc.getDocumentElement().appendChild(rect);
//	}
//	
//	public Integer getElementsIndex() {
//		return elementsIndex;
//	}
//
//	public void setElementsIndex(Integer elementsIndex) {
//		this.elementsIndex = elementsIndex;
//	}
//
//	public String getImgKey() {
//		return imgKey;
//	}
//
//	public void setImgKey(String imgKey) {
//		this.imgKey = imgKey;
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
//	public String getWidth() {
//		return width;
//	}
//
//	public void setWidth(String width) {
//		this.width = width;
//	}
//
//	public String getLeft() {
//		return left;
//	}
//
//	public void setLeft(String left) {
//		this.left = left;
//	}
//
//	public String getTop() {
//		return top;
//	}
//
//	public void setTop(String top) {
//		this.top = top;
//	}
//
//	public String getScale() {
//		return scale;
//	}
//
//	public void setScale(String scale) {
//		this.scale = scale;
//	}
//
//	public String getRotateX() {
//		return rotateX;
//	}
//
//	public void setRotateX(String rotateX) {
//		this.rotateX = rotateX;
//	}
//
//	public String getRotateY() {
//		return rotateY;
//	}
//
//	public void setRotateY(String rotateY) {
//		this.rotateY = rotateY;
//	}
//
//	public String getType() {
//		return type;
//	}
//
//	public void setType(String type) {
//		this.type = type;
//	}
//
//	public Map<String, String> getFillColors() {
//		return fillColors;
//	}
//
//	public void setFillColors(Map<String, String> fillColors) {
//		this.fillColors = fillColors;
//	}
//
//	public Map<String, String> getFontFamily() {
//		return fontFamily;
//	}
//
//	public void setFontFamily(Map<String, String> fontFamily) {
//		this.fontFamily = fontFamily;
//	}
//
//	public Map<String, Double> getFontSize() {
//		return fontSize;
//	}
//
//	public void setFontSize(Map<String, Double> fontSize) {
//		this.fontSize = fontSize;
//	}
//
//	public Map<String, String> getContent() {
//		return content;
//	}
//
//	public void setContent(Map<String, String> content) {
//		this.content = content;
//	}
//
//	public Map<String, String> getFontPosition() {
//		return fontPosition;
//	}
//
//	public void setFontPosition(Map<String, String> fontPosition) {
//		this.fontPosition = fontPosition;
//	}
//
//	public String getHref() {
//		return href;
//	}
//
//	public void setHref(String href) {
//		this.href = href;
//	}
//
//	public Map<String, String> getSvgHref() {
//		return svgHref;
//	}
//
//	public void setSvgHref(Map<String, String> svgHref) {
//		this.svgHref = svgHref;
//	}
//
//	public String getOpacity() {
//		return opacity;
//	}
//
//	public void setOpacity(String opacity) {
//		this.opacity = opacity;
//	}
//
//	public String getItalic() {
//		return italic;
//	}
//
//	public void setItalic(String italic) {
//		this.italic = italic;
//	}
//
//	public String getBold() {
//		return bold;
//	}
//
//	public void setBold(String bold) {
//		this.bold = bold;
//	}
//
//	public String getUnderline() {
//		return underline;
//	}
//
//	public void setUnderline(String underline) {
//		this.underline = underline;
//	}
//
//	public Map<String, String> getFontLetter() {
//		return fontLetter;
//	}
//
//	public void setFontLetter(Map<String, String> fontLetter) {
//		this.fontLetter = fontLetter;
//	}
//
//	public JSONObject getTagJson() {
//		return tagJson;
//	}
//
//	public void setTagJson(JSONObject tagJson) {
//		this.tagJson = tagJson;
//	}
//
//}
