package com.eyuanku.web.framework.svg.handler;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.batik.bridge.BridgeException;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;

import com.eyuanku.web.framework.dt.WidthAndHeight;
import com.eyuanku.web.framework.exce.GlobalException;

/**
 * svg转换工具类 dpi设置：ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER new Float(25.4f /
 * 300)
 */

public class SvgTranscoder {

	private static Transcoder pngTranscoder = new PNGTranscoder();
	private static Transcoder thumbPngTranscoder = new PNGTranscoder();
	private static Transcoder pdfTranscoder = new PDFTranscoder();
	private static Transcoder jpegTranscoder = new JPEGTranscoder();

	static {
		pdfTranscoder.addTranscodingHint(PDFTranscoder.KEY_STROKE_TEXT,
				Boolean.FALSE);

		double millimetresPerPixel = (25.4f / 300);
		pngTranscoder.addTranscodingHint(
				ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float(
						millimetresPerPixel));

		jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,
				new Float(0.95));
	}

	// svg输入流转换成pdf输出流
	public static void convert2Pdf(InputStream in, OutputStream out) {
		convert2Img(in, out, pdfTranscoder);
	}

	// svg输入流转换成png输出流
	public static void convert2PNG(InputStream in, OutputStream out) {
		convert2Img(in, out, pngTranscoder);
	}

	
	// svg输入流转换成png缩略图输出流
	public static void convert2PNG(InputStream in, OutputStream out,
			WidthAndHeight wh, Integer newWidth) {
		Integer newHeight = (wh.getDoubleHeight().intValue() * newWidth) / wh.getDoubleWidth().intValue();
		thumbPngTranscoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, new Float(
				newWidth));
		thumbPngTranscoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, new Float(
				newHeight));
		
		convert2Img(in, out, thumbPngTranscoder);
	}

	// svg输入流转换成jpg输出流
	public static void convert2JPG(InputStream in, OutputStream out)
			throws IOException, TranscoderException {
		convert2Img(in, out, jpegTranscoder);
	}

	// svg文件转成 pdf
	public static void convertSvgFile2Pdf(File svg, File pdf)
			throws IOException, TranscoderException {
		InputStream in = new FileInputStream(svg);
		OutputStream out = new FileOutputStream(pdf);
		out = new BufferedOutputStream(out);
		convert2Pdf(in, out);

	}

	// svg转jpg
	public static void convertSvgFile2Jpg(File svg, File jpg)
			throws IOException, TranscoderException {
		InputStream in = new FileInputStream(svg);
		OutputStream out = new FileOutputStream(jpg);
		out = new BufferedOutputStream(out);
		convert2JPG(in, out);
	}

	// svg转为png
	public static void convertSvgFile2Png(File svg, File png)
			throws IOException, TranscoderException {
		InputStream in = new FileInputStream(svg);
		OutputStream out = new FileOutputStream(png);
		out = new BufferedOutputStream(out);
		convert2PNG(in, out);
	}

	// 字符串转成pdf
	public static void convertStr2Pdf(String svg, File pdf) throws IOException,
			TranscoderException {
		InputStream in = new ByteArrayInputStream(svg.getBytes());
		OutputStream out = new FileOutputStream(pdf);
		out = new BufferedOutputStream(out);
		convert2Pdf(in, out);
	}

	public static InputStream String2InputStream(String str) {
		ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes());
		return stream;
	}
	
	public static void convert2Img(InputStream in, OutputStream out,
			Transcoder transcoder) {
		try {
			TranscoderInput input = new TranscoderInput(in);
			TranscoderOutput output = new TranscoderOutput(out);
			try {
				transcoder.transcode(input, output);
			} catch (BridgeException e) {
				e.printStackTrace();
				throw new GlobalException("image标签错误，图片连接为空或者打不开:"
						+ e.getMessage(), e);
			} catch (TranscoderException e) {
				e.printStackTrace();
				throw new GlobalException("svg转换错误:" + e.getMessage(), e);
			} catch (Exception e) {
				e.printStackTrace();
				throw new GlobalException(e);
			} finally {
				try {
					out.flush();
					if (out != null) {
						out.close();
					}

				} catch (IOException e) {
					throw new GlobalException("svg输出流关闭错误:" + e.getMessage(),
							e);
				}
			}
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				throw new GlobalException("svg输入流关闭错误:" + e.getMessage(), e);
			}
		}
	}
	public static void main(String[] args) throws IOException, TranscoderException {
		File svg = new File("C:\\Users\\Administrator\\Desktop\\1.svg");
		File png = new File("C:\\Users\\Administrator\\Desktop\\1.png");
		convertSvgFile2Png(svg, png);
	}
	
	// TODO 将图片进行base64编码
	public static String image4Base64(InputStream image){
		return null;
	}
	
}