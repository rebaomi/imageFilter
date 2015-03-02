package com.eyuanku.web.framework.svg.dt.elem;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.json.JSONObject;

import org.apache.batik.bridge.BridgeException;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.eyuanku.web.framework.exce.GlobalException;
import com.eyuanku.web.framework.svg.handler.ImageHttpReqHandler;
import com.eyuanku.web.framework.svg.SvgSeed;
import com.eyuanku.web.framework.svg.dt.AbsElem;

public class SvgElem extends AbsElem {

	private static Logger logger = LoggerFactory.getLogger(SvgElem.class);
	public static final String[] SHAPES = new String[] { "g", "path", "rect",
		"circle", "text", "line", "ellipse", "polyline", "polygon",
		"image", "clipPath" };
	
	private static String parser = XMLResourceDescriptor
			.getXMLParserClassName();
	private static SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(
			parser);
//	private static UserAgentAdapter userAgentAdapter = new UserAgentAdapter();
//	private static BridgeContext bridgeContext = new BridgeContext(
//			userAgentAdapter);
//	private static GVTBuilder builder = new GVTBuilder();
	
	private Map<String, String> fillColors;
	private Map<String, String> fontFamily;
	private Map<String, Double> fontSize;

	private String href;
	private Map<String, String> svgHref;

	public SvgElem(JSONObject elemJson) {
		super(elemJson);	
		this.href = elemJson.containsKey("href") ? elemJson.getString("href")
				: "";
		this.svgHref = (Map<String, String>) JSONObject.toBean(
				elemJson.getJSONObject("svgHref"), LinkedHashMap.class);
	}

	@Override
	public Node toSvgElement(SVGDocument doc, SvgSeed svgSeed) {
		String imgKey = this.getImgKey();
		if (imgKey == null) {
			return NullNodeElem.create("imgKey为空").toSvgElement(doc, svgSeed);
		}
		String imgUrl = svgSeed.getSvgUrl(imgKey);

		Element subRoot = null;
		try {
			if(svgHref == null || "{}".equals(svgHref)){
				subRoot = getRootElement(imgUrl);
			} else {
				subRoot = getLayoutRootElement(imgUrl, svgSeed);
			}
		} catch (BridgeException e) {
			subRoot = getLayoutRootElement(imgUrl, svgSeed);
		}

		Element newSub = handleSVG(subRoot, svgSeed);
		Node insertNode = doc.importNode(newSub, true);

		return insertNode;	
	}

	public Element handleSVG(Element subRoot, SvgSeed svgSeed) {
		NodeList gList = subRoot.getElementsByTagName("g");
		
		setTransformAttr((Element) gList.item(0));
		
		String[] shapes = SHAPES;

		for (String s : shapes) {
			NodeList nodeList = subRoot.getElementsByTagName(s);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element cur = (Element) nodeList.item(i);
				if (cur.hasAttribute("class")) {
					String cVal = cur.getAttribute("class");					
					if (fillColors != null && fillColors.containsKey(cVal)) {
						cur.setAttribute("fill", fillColors.get(cVal));
					}
					if (fontFamily != null && fontFamily.containsKey(cVal)) {
						cur.setAttribute("font-family", "'"+fontFamily.get(cVal)+"'");
					}
					if (fontSize != null && fontSize.containsKey(cVal)) {
						cur.setAttribute("font-size", String.valueOf(fontSize.get(cVal)));
					}
					if ("image".equals(s) && svgHref != null
							&& svgHref.containsKey(cVal)) {						
						JSONObject attrSVG = JSONObject.fromObject(svgHref
								.get(cVal));
						if (attrSVG.containsKey("img_key")) {
							// 消除svg素材本身transform对吸附图片的显示影响
							cur.removeAttribute("transform");							
							cur.setAttribute("xlink:href", svgSeed.getImgUrl(attrSVG.getString("img_key")));
							cur.setAttribute("width", attrSVG.containsKey("width") ? attrSVG.getString("width") : "");
							cur.setAttribute("height", attrSVG.containsKey("height") ? attrSVG.getString("height") : "");
							cur.setAttribute("x", attrSVG.containsKey("x") ? attrSVG.getString("x") : "");
							cur.setAttribute("y", attrSVG.containsKey("y") ? attrSVG.getString("y") : "");
						}
					}
				}
			}
			nodeList = null;
		}
		gList = null;

		setCommonAttr(subRoot);
		subRoot.setAttribute("overflow", "visible");

		return subRoot;
	}

	@Override
	protected String[] getValidationAttr() {
		return null;
	}

	public static Element getRootElement(String svgUrl) {
		SVGDocument svgDoc;
		try {
			svgDoc = (SVGDocument) factory.createSVGDocument(svgUrl);
		} catch (IOException e) {
			logger.error("解析svg失败", e);
			throw new GlobalException("解析svg失败", e);
		}
		return svgDoc.getRootElement();
	}
	
	public static Element getLayoutRootElement(String svgUrl, SvgSeed svgSeed) {
		Document doc = checkImageNode(svgUrl, svgSeed);
		return doc.getDocumentElement();
	}

//	private static GraphicsNode getRootNode(SVGDocument document) {
//		return builder.build(bridgeContext, document);
//	}

	private static Document checkImageNode(String svgUrl, SvgSeed svgSeed) {
		logger.debug(svgUrl);
		Document doc = str2Dom(ImageHttpReqHandler.httpGetRequest(svgUrl));
		NodeList nodeList = doc.getElementsByTagName("image");
		Element e = null;

		for (int i = 0; i < nodeList.getLength(); i++) {
			e = (Element) nodeList.item(i);
// 			String href = e.getAttribute("xlink:href");
 			e.setAttribute("xlink:href", svgSeed.getBlankImage());
//			if (href == null || "".equals(href)) {
//				e.setAttribute("xlink:href", getBlankImage());
//			}
		}
		nodeList = null;
		e = null;
		return doc;
	}

	public static Document str2Dom(String xmlStr) {
		StringReader sr = new StringReader(xmlStr);
		InputSource is = new InputSource(sr);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(is);
			return doc;
		} catch (ParserConfigurationException e) {
			throw new GlobalException(e);
		} catch (SAXException | IOException e) {
			throw new GlobalException(e);
		}
	}

}
