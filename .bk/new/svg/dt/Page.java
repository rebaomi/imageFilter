package com.eyuanku.web.framework.svg.dt;

import net.sf.json.JSONObject;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

import com.eyuanku.web.framework.svg.SvgSeed;
import com.eyuanku.web.framework.svg.Utils;

public class Page {

	public static final String X = "0";
	public static final String Y = "0";
	public static final String SEP = " ";
//	public static final String DEFAULT_BACKGROUND = "rgb(255, 255, 255)";
	
	private static final String[] TAG = new String[]{"width","height","backgroundColor"};
	
	private String width;
	
	private String height;
	
	private String backgroundColor;
	
	private Elems elems = null;
	
	private String x = X;
	
	private String y = Y;
	
	
	public Page(JSONObject pageJson) {
		Utils.validateJson(TAG, pageJson);
		this.width = pageJson.getString("width");
		this.height = pageJson.getString("height");
		this.backgroundColor = pageJson.getString("backgroundColor");
		
		JSONObject elemsJson = pageJson.getJSONObject("elements");
		elems = new Elems(elemsJson);
	}
	
	public SVGDocument toSVG(SvgSeed seed) {
		SVGDocument doc = createRootDocument();
		Element svgRoot = doc.getDocumentElement();
		svgRoot.setAttributeNS(null, "width", width);
		svgRoot.setAttributeNS(null, "height", height);
		svgRoot.setAttributeNS(null, "viewBox", x+SEP+y+SEP+width+SEP+height);
		svgRoot.setAttributeNS(null, "shape-rendering", "geometricPrecision");

		Element rectBk = doc.createElement("rect");
		rectBk.setAttributeNS(null, "width", width);
		rectBk.setAttributeNS(null, "height", height);
		if(backgroundColor!=null) {
			rectBk.setAttributeNS(null, "style", "fill:"+backgroundColor);
		}
		rectBk.setAttributeNS(null, "stroke", "#FFFFFF");
		rectBk.setAttributeNS(null, "stroke-miterlimit", "1");
		rectBk.setAttributeNS(null, "fill", "none");
		
		svgRoot.appendChild(rectBk);
		rectBk = null;
		elems.toSVG(doc, seed);
		return doc;
	}

	private static SVGDocument createRootDocument() {
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		Document doc = impl.createDocument(svgNS, "svg", null);
		SVGDocument svgDoc = (SVGDocument) doc;
		return svgDoc;
	}

}
