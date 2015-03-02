package com.eyuanku.web.framework.svg.dt.elem;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

import com.eyuanku.web.framework.svg.SvgSeed;
import com.eyuanku.web.framework.svg.dt.AbsElem;

public class TextElem extends AbsElem {

	private static Logger logger = LoggerFactory.getLogger(TextElem.class);
	
	private String italic;
	private String bold;
	private String underline;

	private Map<String, String> fillColors;
	private Map<String, String> fontFamily;
	private Map<String, Double> fontSize;
	private Map<String, String> content;
	private Map<String, String> fontPosition;
	private Map<String, String> fontLetter;

	public TextElem(JSONObject elemJson) {
		super(elemJson);
		this.italic = elemJson.containsKey("italic") ? elemJson.getString("italic")
				: "0";
		this.bold = elemJson.containsKey("bold") ? elemJson.getString("bold")
				: "0";
		this.underline = elemJson.containsKey("underline") ? elemJson.getString("underline")
				: "0";
		this.fillColors = (Map<String, String>) JSONObject.toBean(
				elemJson.getJSONObject("fillColors"), LinkedHashMap.class);
		this.fontFamily = (Map<String, String>) JSONObject.toBean(
				elemJson.getJSONObject("fontFamily"), LinkedHashMap.class);		
		this.fontSize = (Map<String, Double>) JSONObject.toBean(
				elemJson.getJSONObject("fontSize"), LinkedHashMap.class);
		this.content = (Map<String, String>) JSONObject.toBean(
				elemJson.getJSONObject("content"), LinkedHashMap.class);
		this.fontPosition = (Map<String, String>) JSONObject.toBean(
				elemJson.getJSONObject("fontPosition"), LinkedHashMap.class);		
		this.fontLetter = (Map<String, String>) JSONObject.toBean(
				elemJson.getJSONObject("fontLetter"), LinkedHashMap.class);
	}

	@Override
	public Node toSvgElement(SVGDocument doc, SvgSeed svgSeed) {
		String imgKey = this.getImgKey();
		Element textNode = doc.createElement("text");
		textNode.setAttribute("id", imgKey);
		textNode.setAttribute("xml:space","preserve");
		setCommonAttr(textNode);
				
		// 斜体 加粗 下划线
		// textNode.setAttribute("style", "font-style:italic");
		// textNode.setAttribute("style", "text-decoration:underline");
		// textNode.setAttribute("style", "font-weight:bold");

		setTransformAttr(textNode);
		textNode.setAttribute("style", "fill:"
				+ (fillColors.containsKey("0") ? fillColors.get("0") : ""));
		textNode.setAttribute("font-family",
				fontFamily.containsKey("0") ? "'"+fontFamily.get("0")+"'" : "");
		
		if(fontSize.containsKey("0") && ! "null".equals(String.valueOf(fontSize.get("0"))) ){
			textNode.setAttribute("font-size", String.valueOf(fontSize.get("0")));
		}
		for (Map.Entry<String, String> e : content.entrySet()) {
			Element tspan = doc.createElement("tspan");
			tspan.setAttributeNS(null, "x", getLeft());
			
			boolean isFirstLine = "0".equals(e.getKey());
			int s = content.size();
			if (s == 0) s = 1;
			logger.info(getHeight());
			Double dy = null;
			if (isFirstLine) {
				Object obj = fontSize.get("0");
				if (obj != null && obj instanceof Double) {
					dy = (Double)obj;
				} else if (obj != null && obj instanceof Integer) {
					dy = ((Integer)obj).doubleValue();
				} else {
					dy = new Double(12);
				}
			} else {
				dy = new Double(Float.parseFloat(getHeight()) / s);
			}
			
			tspan.setAttributeNS(null, "dy", dy.toString());
			tspan.setAttributeNS(null,"text-anchor",fontPosition.get("0"));
//			tspan.setAttributeNS(null, "style", "letter-spacing:" + fontLetter.get("0") + ";" + ("1".equals(italic) ? "font-style:italic;" : "") + ("1".equals(bold) ? "font-weight:bold;" : "") + ("1".equals(underline) ? "text-decoration:underline;" : "") );
			tspan.setAttributeNS(null, "style", "inline-height:15px;letter-spacing:" + fontLetter.get("0") + ";" + ("1".equals(italic) ? "font-style:italic;" : "") + ("1".equals(bold) ? "font-weight:bold;" : "") + ("1".equals(underline) ? "text-decoration:underline;" : ""));
			Node text = doc.createTextNode(null);

			text.setNodeValue(e.getValue());
			tspan.appendChild(text);
			textNode.appendChild(tspan);
		}
		return textNode;
	}

	@Override
	protected String[] getValidationAttr() {
		return null;
	}

}
