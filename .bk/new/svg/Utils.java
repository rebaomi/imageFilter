package com.eyuanku.web.framework.svg;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.json.JSONObject;

import org.w3c.dom.Document;

import com.eyuanku.web.framework.exce.GlobalException;

public class Utils {

	public static void validateJson(String[] tags, JSONObject json) {
		if (tags != null) {
			for (String tag : tags) {
				if (!json.containsKey(tag)) {
					throw new GlobalException("属性" + tag + "为空");
				}
			}
		}
	}

	public static String dom2Str(Document doc) {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t;
		try {
			t = tf.newTransformer();
			t.setOutputProperty("encoding", "UTF8");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			t.transform(new DOMSource(doc), new StreamResult(bos));
			return bos.toString("UTF8");
		} catch (TransformerConfigurationException e) {
			throw new GlobalException("dom转换str配置失败：",e);
		} catch (TransformerException e) {
			throw new GlobalException("dom转换str失败：",e);
		} catch (UnsupportedEncodingException e) {
			throw new GlobalException("");
		}
	}

	public static String buildUrl(String host, String path){
		String buildUrl = "http://" + host + "/" + path;
		return buildUrl;
	}
}
