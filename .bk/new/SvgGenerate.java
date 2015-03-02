package com.eyuanku.web.framework.M;

import java.net.URL;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eyuanku.web.framework.storage.file.IImgStorage;
import com.eyuanku.web.framework.storage.file.StorageBtype;
import com.eyuanku.web.framework.svg.Utils;
import com.eyuanku.web.framework.svg.SvgSeed;
import com.eyuanku.web.framework.util.FilterConfig;
import com.eyuanku.web.framework.util.GlobalConfig;

@Service("svgGenerate")
public class SvgGenerate implements SvgSeed {

	private static final String URL_PROTOCOL = "http://";
	private static final String URL_SEP = "/";

	@Autowired
	GlobalConfig globalConfig;
	StorageBtype businessType = StorageBtype.MATERIALS;
	@Autowired
	IImgStorage privateImageStorage;
	
	@Override
	public String getSvgUrl(String imgKey) {
		return new StringBuffer().append(URL_PROTOCOL)
				.append(globalConfig == null ?
						"eyuankupub.oss-cn-beijing-internal.aliyuncs.com/" :
							globalConfig.getOssInterHost())
				.append(URL_SEP)
				.append(businessType.getName())
				.append(URL_SEP)
				.append(imgKey)
				.toString();
	}

	@Override
	public String getImgUrl(String imgKey) {
		Date expire = new Date(new Date().getTime() + 1000 * 60 * 60 * 2); // 有效期2个小时
		URL url = privateImageStorage.getImgUrl(businessType, imgKey, expire);
		return url.toString();
	}

	@Autowired
	FilterConfig filterConfig;
	
	@Override
	public String getImgUrl(String imgKey, String effect) {
		String baseSignUrl = getImgUrl(imgKey);
		
		String ossSign = baseSignUrl.split("[?]")[1];
		String ossEnv = filterConfig.getOssEnv();
		
		String imgPath = imgKey + "?" + ossSign 
						+ "&effect=" + effect 
						+ "&ossEnv=" + ossEnv;
		
		String filterServer = filterConfig.getFilterServer();
		return Utils.buildUrl(filterServer, imgPath);
	}
	
	@Override
	public String getBlankImage() {
		return "http://" + globalConfig.getOssInterHost() + "/sys/pic.png";
	}

}
