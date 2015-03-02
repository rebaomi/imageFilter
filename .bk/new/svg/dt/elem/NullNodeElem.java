package com.eyuanku.web.framework.svg.dt.elem;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

import com.eyuanku.web.framework.svg.SvgSeed;
import com.eyuanku.web.framework.svg.dt.AbsElem;

public class NullNodeElem extends AbsElem {

	private static Logger logger = LoggerFactory.getLogger(NullNodeElem.class);
	private String msg;
	public NullNodeElem(JSONObject elemJson) {
		this("无法识别的该素材类型："+elemJson.getString("type"));
	}

	public NullNodeElem(String msg) {
		this.msg = msg;
	}

	@Override
	public Node toSvgElement(SVGDocument doc, SvgSeed svgSeed) {
		Element nullNode = doc.createElement("text");
		nullNode.setAttributeNS(null, "width", "100");
		nullNode.setAttributeNS(null, "height", "100");
		nullNode.setAttributeNS(null, "fill", "black");
		nullNode.setAttributeNS(null, "font-family", "微软雅黑");
		nullNode.setAttributeNS(null, "font-size", "20");

		Node text = doc.createTextNode(this.msg);
		logger.error(this.msg);
		nullNode.appendChild(text);
		return nullNode;
	}

	@Override
	protected String[] getValidationAttr() {
		return null;
	}

	public static NullNodeElem create(JSONObject elemJson) {
		return new NullNodeElem(elemJson);
	}
	public static NullNodeElem create(String msg) {
		return new NullNodeElem(msg);
	}
}
