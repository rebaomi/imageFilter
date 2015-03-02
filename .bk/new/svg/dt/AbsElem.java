package com.eyuanku.web.framework.svg.dt;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

import com.eyuanku.web.framework.svg.SvgSeed;
import com.eyuanku.web.framework.svg.dt.elem.ImgElem;
import com.eyuanku.web.framework.svg.dt.elem.NullNodeElem;
import com.eyuanku.web.framework.svg.dt.elem.SvgElem;
import com.eyuanku.web.framework.svg.dt.elem.TextElem;

public abstract class AbsElem {
	
	private static Logger logger = LoggerFactory.getLogger(AbsElem.class);
	
	private String type;
	
	private Integer elementsIndex;
	private String imgKey;
	private String height;
	private String width;
	private String left;
	private String top;
	private String scale;
	
	private String rotateX;
	private String rotateY;

	
	private String opacity;
	
	private static final String[] TAG = new String[]{
		"elementsIndex", "height","width",
		"left","top","scale","opacity"
	};

	private static Map<String, ElemCreator> ELEM_CREATOR_MAP = null;
	static {
		if (ELEM_CREATOR_MAP == null) {
			ELEM_CREATOR_MAP = new HashMap<String, AbsElem.ElemCreator>();
			AbsElem.regCreator("img", new ElemCreator() {
				@Override
				public AbsElem createElem(JSONObject elemJson) {
					return new ImgElem(elemJson);
				}});
			AbsElem.regCreator("svg", new ElemCreator() {
				@Override
				public AbsElem createElem(JSONObject elemJson) {
					return new SvgElem(elemJson);
				}});
			AbsElem.regCreator("text", new ElemCreator() {
				@Override
				public AbsElem createElem(JSONObject elemJson) {
					return new TextElem(elemJson);
				}});
		}
	}
	
	public static void load(){};
	public static AbsElem createElement(JSONObject elemJson) {
		String elemType = elemJson.getString("type");
		ElemCreator creator = ELEM_CREATOR_MAP.get(elemType);
		if (creator != null) {
			return creator.createElem(elemJson);
		}
		return NullNodeElem.create(elemJson);
	}
	
	public static void regCreator(String key, ElemCreator creator){
		ELEM_CREATOR_MAP.put(key, creator);
	}

	protected AbsElem() {}
	protected AbsElem(JSONObject elemJson) {
//		Utils.validateJson(getValidationAttr(), elemJson);
		/** 必填属性设定 **/
		this.elementsIndex = elemJson.getInt("elementsIndex");	
		this.height = elemJson.getString("height");				
		this.width = elemJson.getString("width");		
		this.left = elemJson.getString("left");		
		this.top = elemJson.getString("top");
		this.scale = elemJson.getString("scale");		
		this.opacity = elemJson.getString("opacity");
				
		/** 非必填属性设定 **/
		this.imgKey = elemJson.containsKey("img_key") ? elemJson
				.getString("img_key") : null;
		this.rotateX = elemJson.containsKey("rotateX") ? elemJson
				.getString("rotateX") : "0";		
		this.rotateY = elemJson.containsKey("rotateY") ? elemJson
				.getString("rotateY") : "0";	
	}

	protected void setCommonAttr(Element cur) {
		cur.setAttribute("width", width);
		cur.setAttribute("height", height);
		cur.setAttribute("x", left);
		cur.setAttribute("y", top);
		cur.setAttribute("opacity", opacity);
	}

	protected void setTransformAttr(Element cur) {
		if (scale != null ) {
			if(!"null".equals(rotateX)
					&& !"null".equals(rotateY)){
				cur.setAttribute("transform", "rotate(" + scale
						+ "," + rotateX + "," + rotateY + ")");
			} else {
				cur.setAttribute("transform", "rotate(" + scale + ")");
			}
		}
	}
		
	public abstract Node toSvgElement(SVGDocument doc, SvgSeed svgSeed);
	protected abstract String[] getValidationAttr();
	
	public interface ElemCreator {
		public AbsElem createElem(JSONObject elemJson);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getImgKey() {
		return imgKey;
	}

	public void setImgKey(String imgKey) {
		this.imgKey = imgKey;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getLeft() {
		return left;
	}

	public void setLeft(String left) {
		this.left = left;
	}

}
