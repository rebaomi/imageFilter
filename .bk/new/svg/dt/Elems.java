package com.eyuanku.web.framework.svg.dt;

import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

import com.eyuanku.web.framework.svg.SvgSeed;

public class Elems {

	private Map<String, AbsElem> elements = new LinkedHashMap<String, AbsElem>();

	public Elems(JSONObject elsJson) {
		String iStr = "";
		for (int i = 0, len = elsJson.size(); i < len; ++i) {
			iStr = String.valueOf(i);
			if (elsJson.getJSONObject(iStr).containsKey("type") 
					&& !"background".equals(elsJson.getJSONObject(iStr).getString("type")) ) {
				elements.put(iStr, AbsElem.createElement(elsJson.getJSONObject(iStr)));
			}
		}
		iStr = null;
		elsJson = null;
	}

	public SVGDocument toSVG(SVGDocument doc, SvgSeed svgSeed) {
		Element root = doc.getDocumentElement();
		for (Map.Entry<String, AbsElem> m : elements.entrySet()) {
			Node svgElem = m.getValue().toSvgElement(doc, svgSeed);
			root.appendChild(svgElem);
		}
		return doc;
	}

}
