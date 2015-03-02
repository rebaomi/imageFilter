package com.eyuanku.web.framework.svg.dt.elem;

import net.sf.json.JSONObject;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGDocument;

import com.eyuanku.web.framework.svg.SvgSeed;
import com.eyuanku.web.framework.svg.dt.AbsElem;

public class ImgElem extends AbsElem {

	private String effect;
	
	public ImgElem(JSONObject elemJson) {
		super(elemJson);
		this.effect = elemJson.containsKey("effect") ? elemJson.getString("effect")
				: "";
	}

	@Override
	public Node toSvgElement(SVGDocument doc, SvgSeed svgSeed) {
		String imgKey = this.getImgKey();
		if (imgKey == null) {
			return NullNodeElem.create("imgKey为空").toSvgElement(doc, svgSeed);
		}
		Element image = doc.createElement("image");
		image.setAttribute("id", imgKey);

		image.setAttribute("xlink:href", svgSeed.getImgUrl(imgKey));
//		image.setAttribute("xlink:href", svgSeed.getImgUrl(imgKey, effect));//"softenFace"
		setCommonAttr(image);
		
		setTransformAttr(image);
		return image;
	}

	@Override
	protected String[] getValidationAttr() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEffect() {
		return effect;
	}

	public void setEffect(String effect) {
		this.effect = effect;
	}

}
