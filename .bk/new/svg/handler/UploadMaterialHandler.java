package com.eyuanku.web.framework.svg.handler;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.web.multipart.MultipartFile;

import com.eyuanku.web.framework.dt.WidthAndHeight;
import com.eyuanku.web.framework.exce.GlobalException;
import com.eyuanku.web.framework.util.UploadUtil;

public class UploadMaterialHandler {
	private static Map<String, String> contentTypeMap = new HashMap<String, String>();

	static {
		contentTypeMap.put("jpg", "image/jpg");
		contentTypeMap.put("png", "image/png");
		contentTypeMap.put("jpeg", "image/jpeg");
		contentTypeMap.put("webp", "image/webp");
		contentTypeMap.put("bmp", "image/bmp");
		contentTypeMap.put("svg", "image/svg+xml");
	}
	
	private BufferedImage img;
	private InputStream inputStream;

	private WidthAndHeight w_h;

	private String fileName;
	private String ext;
	private String contentType;

	private InputStream fileInputStream;

	public UploadMaterialHandler(String filePath) {
		File file = new File(fileName);
		
		fileName = file.getName();
		ext = fileName.substring(fileName.lastIndexOf(".") + 1);
		contentType = contentTypeMap.get(ext.toLowerCase());
		try {
			fileInputStream = new FileInputStream(file);
		} catch (IOException e) {
			throw new GlobalException("读取图片失败", e);
		}
		handleMaterial(fileInputStream);
	}
	
	public UploadMaterialHandler(File file) {
		fileName = file.getName();
		ext = fileName.substring(fileName.lastIndexOf(".") + 1);
		contentType = contentTypeMap.get(ext.toLowerCase());
		try {
			fileInputStream = new FileInputStream(file);
		} catch (IOException e) {
			throw new GlobalException("读取图片失败", e);
		}
		handleMaterial(fileInputStream);
	}
	
	public UploadMaterialHandler(MultipartFile file) {
		fileName = file.getOriginalFilename();
		ext = fileName.substring(fileName.lastIndexOf(".") + 1);
		contentType = UploadUtil.getMultipartContentType(file);
		try {
			fileInputStream = file.getInputStream();
		} catch (IOException e) {
			throw new GlobalException("读取图片失败", e);
		}
		
		handleMaterial(fileInputStream);
	}

	// 不包括svg文件
	public void handleMaterial(InputStream fileInputStream) {
		try {
			img = CMYKReader.read(fileInputStream);
			w_h = initWidthAndHeight(img);

			img.flush();
			inputStream = readImg2InputStream(img);
		} catch (IOException e) {
			throw new GlobalException("读取图片失败", e);
		}
	}

	private InputStream readImg2InputStream(BufferedImage img) {
		try {
			ByteArrayOutputStream bs = new ByteArrayOutputStream();

			ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
			ImageIO.write(img, ext, imOut);
			return new ByteArrayInputStream(bs.toByteArray());
		} catch (IOException e) {
			throw new GlobalException("读取图片失败", e);
		}
	}

	private WidthAndHeight initWidthAndHeight(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		return new WidthAndHeight(width, height);
	}

	public BufferedImage getImg() {
		return img;
	}

	public void setImg(BufferedImage img) {
		this.img = img;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public WidthAndHeight getW_h() {
		return w_h;
	}

	public void setW_h(WidthAndHeight w_h) {
		this.w_h = w_h;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
