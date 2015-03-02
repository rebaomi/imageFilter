

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.eyuanku.web.framework.exce.GlobalException;

public class EykSVG {
	private Map<Integer, String> svgPages = new LinkedHashMap<Integer, String>();
	
	public EykSVG(Map<Integer, String> svgPages){
		this.svgPages = svgPages;
	}

	public Map<Integer, InputStream> toStream() {
		Map<Integer, InputStream> svgStreamMap = new LinkedHashMap<Integer, InputStream>();
		
		for(Map.Entry<Integer, String> e : svgPages.entrySet()) {
			try {
				svgStreamMap.put(e.getKey(), new ByteArrayInputStream(e.getValue().getBytes("UTF-8")));
			} catch (UnsupportedEncodingException e1) {
				throw new GlobalException("不支持该编码格式",e1);
			}
		}
		
		svgPages.clear();
		return svgStreamMap;
	}
	
	public Map<Integer, String> getSvgPages() {
		return svgPages;
	}

	public void setSvgPages(Map<Integer, String> svgPages) {
		this.svgPages = svgPages;
	}
	
}
