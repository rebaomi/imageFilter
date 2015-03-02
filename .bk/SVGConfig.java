

import java.net.URL;
import java.util.Date;

import net.sf.json.JSONObject;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.eyuanku.web.framework.exce.GlobalException;
import com.eyuanku.web.framework.storage.file.StorageBtype;
import com.eyuanku.web.framework.storage.file.oss.OSSKey;
import com.eyuanku.web.framework.storage.file.oss.OssImgStorage;
import com.eyuanku.web.framework.util.FilterConfig;
import com.eyuanku.web.framework.util.GlobalConfig;

/**
 * svg配置
 * 
 * @author lvxd
 * 
 */

public class SVGConfig {
//	public static final String[] SHAPES = new String[] { "g", "path", "rect",
//			"circle", "text", "line", "ellipse", "polyline", "polygon",
//			"image", "clipPath", "use", "defs", "linearGradient", "stop",
//			"perspective", "filter" };
	public static final String[] SHAPES = new String[] { "g", "path", "rect",
		"circle", "text", "line", "ellipse", "polyline", "polygon",
		"image", "clipPath" };

	public static final String URL_PROTOCOL = "http://";
	public static final String URL_SEP = "/";

//	private static String endPoint = "http://oss-cn-beijing.aliyuncs.com";// 外网
	private static String endPoint = "http://oss-cn-beijing-internal.aliyuncs.com";// 内网
	private static String accessID = "5jnc5LMLExLYmSdF";				 
	private static String accessKey = "vdZULUmBwbTUjrbDi3ZPMzQ0Ul2ywC";
	/**
	 * 获取oss上的svg地址
	 * 
	 * @param imgKey
	 * @param businessType
	 * @return
	 */
	public static String getSVGUrl(String imgKey, StorageBtype businessType) {
		WebApplicationContext context = ContextLoader
				.getCurrentWebApplicationContext();
		GlobalConfig config = context == null ? null : context.getBean(GlobalConfig.class);
		return new StringBuffer().append(URL_PROTOCOL)
				.append(config == null ? "eyuankupub.oss-cn-beijing-internal.aliyuncs.com/" : config.getOssInterHost()).append(URL_SEP)
				.append(businessType.getName()).append(URL_SEP).append(imgKey)
				.toString();
	}

	/**
	 * 生成oss文件访问签名
	 * 
	 * @param key
	 * @return
	 */
	public static String generateSign(String key) {
		OSSClient client = getOssKey().generateClient();
		Date expires = new Date(new Date().getTime() + 1000 * 60 * 60 * 2); // 有效期2个小时

		WebApplicationContext context = ContextLoader
				.getCurrentWebApplicationContext();
		OssImgStorage privateIImgStorage = null;
		if(context == null) {
			// 测试用
			privateIImgStorage = new OssImgStorage();
			privateIImgStorage.setBucketName("eyuankuimage");
			OSSKey ossKey = new OSSKey();
			ossKey.setEndpoint(endPoint);
			ossKey.setAccessId(accessID);
			ossKey.setAccessKey(accessKey);
			privateIImgStorage.setOssKey(ossKey);
		} else {
			privateIImgStorage = context.getBean("privateImageStorage",
					OssImgStorage.class);
		}
		
		GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
				privateIImgStorage.getBucketName(), key);

		generatePresignedUrlRequest.setExpiration(expires);
		generatePresignedUrlRequest.setMethod(HttpMethod.GET);
		URL url = client.generatePresignedUrl(generatePresignedUrlRequest);
		return url.toString();
	}
	
	public static String generateFilterSign(StorageBtype businessType, String fileKey, String effect) {
		WebApplicationContext context = ContextLoader
				.getCurrentWebApplicationContext();
		FilterConfig config = context == null ? null : context.getBean(FilterConfig.class);
		
		String ossServer = config == null ? "testeyk" : config.getOssEnv();
		String filterServer = config == null ? "http://123.57.36.160:8000/" : config.getFilterServer();
		String signUrl = generateSign(keyString(StorageBtype.MATERIALS, fileKey));
		String filterUrl = filterServer + fileKey + "?" 
						 + signUrl.split("[?]")[1]
						 + "&effect=" + effect
						 + "&ossServer=" + ossServer;
		return filterUrl;
	}

	public static String generateSign(StorageBtype businessType, String fileKey) {
		return generateSign(keyString(businessType, fileKey));
	}

	public static OSSKey getOssKey() {
		WebApplicationContext context = ContextLoader
				.getCurrentWebApplicationContext();
		GlobalConfig config = context == null ? null : context.getBean(GlobalConfig.class);
		
		OSSKey ossKey = new OSSKey();
		ossKey.setAccessId("5jnc5LMLExLYmSdF");
		ossKey.setAccessKey("vdZULUmBwbTUjrbDi3ZPMzQ0Ul2ywC");
		ossKey.setEndpoint("http://oss-cn-beijing.aliyuncs.com");// 外网
		if(config != null){
//			ossKey.setEndpoint("http://oss-cn-beijing-internal.aliyuncs.com");// 内网
			ossKey.setEndpoint(config.getEndPoint());
		}
		return ossKey;
	}

	protected static String keyString(StorageBtype businessType, String key) {
		return keyString(businessType.getName(), key);
	}

	protected static String keyString(String topDir, String key) {
		return topDir + "/" + key;
	}
	
	public static void printMem() {
		Runtime run = Runtime.getRuntime(); 
		long max = run.maxMemory(); 

		long total = run.totalMemory(); 

		long free = run.freeMemory(); 

		long usable = max - total + free; 
		
		String info = "最大内存 = " + max/ 1024 / 1024 + "M\n"
					+ "已分配内存 = " + total/ 1024 / 1024 + "M\n"
					+ "已分配内存中的剩余空间 = " + free/ 1024 / 1024 + "M\n"
					+ "最大可用内存 = " + usable/ 1024 / 1024 + "M\n"
					+ "==================================";
		
		System.out.println(info);
	}
	
	public static void validateJson(String[] tags, JSONObject json){
		for(String tag : tags){
			if(!json.containsKey(tag)){
				throw new GlobalException("属性"+tag+"为空");
			}
		}
	}
}
