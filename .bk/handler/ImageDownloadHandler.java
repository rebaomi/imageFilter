package com.eyuanku.web.framework.svg.handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.eyuanku.web.framework.exce.GlobalException;

public class ImageDownloadHandler {
	/**
	 * 将图片放到http的响应中
	 * Content-Type:application/octet-stream
	 */
	public static HttpServletResponse injectImg2Response(String fileName, String contentType, InputStream is, boolean isSingle
			, HttpServletResponse response) {
		
		response.setContentType(isSingle ? contentType : "application/zip");
		String conType4Disposition = isSingle ? contentType : "application/zip";
		String filenamedisplay = fileName;
		String enc = "UTF-8";
		try {
			filenamedisplay = URLEncoder.encode(filenamedisplay, enc);
		} catch (UnsupportedEncodingException e) {
			throw new GlobalException("不支持该编码格式：" + enc);
		}
		switch (conType4Disposition) {
		case "application/pdf":
		case "application/pdf;charset=UTF-8":
		    filenamedisplay += ".pdf";
			break;
		case "image/png":
		case "application/png":
		case "application/png;charset-UTF-8":
			filenamedisplay += ".png";
			break;
		case "application/zip":
		case "application/zip;charset=UTF-8":
			filenamedisplay += ".zip";
			break;
		default:
			break;
		}
		response.addHeader("Content-Disposition", "attachment;filename="+filenamedisplay);
		try {
			response.addHeader("Content-Length", String.valueOf(is.available()));
		} catch (IOException e) {
			throw new GlobalException(e);
		}
		OutputStream output = null;

		try {
			output = response.getOutputStream();
			byte[] b = new byte[1024];
			int i = 0;
			while ((i = is.read(b)) > 0) {
				output.write(b, 0, i);
			}
			output.flush();
		} catch (IOException e) {
			throw new GlobalException(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					throw new GlobalException(e);
				}
				is = null;
			}
			if (output != null) {
				try {
					output.flush();
					output.close();
				} catch (IOException e) {
					throw new GlobalException(e);
				}
				output = null;
			}
		}
		return response;
	}

	public static InputStream mergeImgStream(boolean isSingle, Map<String, InputStream> map) {
		InputStream is = null;
		if(isSingle && map.size() != 0) {
			for(Map.Entry<String, InputStream> m : map.entrySet()) {
				is = m.getValue();
			}
		} else {
			is = ImageCommonHandler.multiCompress(map);
		}
		for(Map.Entry<String, InputStream> m : map.entrySet()) {
			try {
				m.getValue().close();
			} catch (IOException e) {
				throw new GlobalException("输入流关闭失败");
			}
		}
		map.clear();
		return is;
	}
	
	public static InputStream mergePdfStream(boolean isSingle, Map<String, InputStream> map) {
		InputStream is = null;
		if(isSingle && map.size() != 0) {
			for(Map.Entry<String, InputStream> m : map.entrySet()) {
				is = m.getValue();
			}
		} else {
			is = ImageCommonHandler.mergePdfInputStream(new ArrayList<InputStream>(map.values()));
		}
		for(Map.Entry<String, InputStream> m : map.entrySet()) {
			try {
				m.getValue().close();
			} catch (IOException e) {
				throw new GlobalException("输入流关闭失败");
			}
		}
		map.clear();
		return is;
	}
	
	public static void generateHighPdf(Map<String, InputStream> pdfOSSMap, HttpServletResponse response, String title) {
		boolean isSingle = pdfOSSMap.size() <= 1 ? true : false;
		
		try(InputStream  pdfIs = mergePdfStream(isSingle, pdfOSSMap);) {
			injectImg2Response(title, "application/pdf", pdfIs, true, response);
		} catch (IOException e) {
			throw new GlobalException(e);
		} 
	}
	
	public static void generateCommonPdf(List<OutputStream> pdfList, HttpServletResponse response, String title) {
		
		OutputStream os = new ByteArrayOutputStream();
		OutputStream reOs = null;
		InputStream pdfIs = null;
		try {
			reOs = ImageCommonHandler.makePdf(pdfList, os);
			pdfIs = new ByteArrayInputStream(((ByteArrayOutputStream)reOs).toByteArray());
			injectImg2Response(title, "application/pdf", pdfIs, true, response);
			
		} finally {
			try {
				os.flush();
				os.close();
			} catch (IOException e) {
				throw new GlobalException("输出流关闭失败");
			}
			try {
				reOs.flush();
				reOs.close();
			} catch (IOException e1) {
				throw new GlobalException("输出流关闭失败");
			}
			try {
				pdfIs.close();
			} catch (IOException e) {
				throw new GlobalException("输入流关闭失败");
			}
			pdfList.clear();
		}
		
	}
	
	public static String getCurTime() {
		return new SimpleDateFormat("yyyy/MM/dd hh:MM:ss").format(new Date(System.currentTimeMillis()));
	}
	
	public static void printCurTime(String pos) {
		System.out.println("[ "+getCurTime()+" ] : "+pos);
	}
}
