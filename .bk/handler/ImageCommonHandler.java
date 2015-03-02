package com.eyuanku.web.framework.svg.handler;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.byteSources.ByteSource;
import org.apache.sanselan.common.byteSources.ByteSourceArray;
import org.apache.sanselan.formats.jpeg.JpegImageParser;
import org.apache.sanselan.formats.jpeg.segments.UnknownSegment;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.eyuanku.web.framework.dt.WidthAndHeight;
import com.eyuanku.web.framework.exce.GlobalException;
import com.eyuanku.web.framework.storage.file.IImgStorage;
import com.eyuanku.web.framework.storage.file.StorageBtype;
import com.eyuanku.web.framework.storage.file.oss.OSSKey;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

public class ImageCommonHandler {
	public static WidthAndHeight getImageWidthHeight(MultipartFile file) {
		try {
			byte[] bytes = file.getBytes();
			InputStream iis = new ByteArrayInputStream(bytes);
			InputStream iis4Check = new ByteArrayInputStream(bytes);
			return getImageWidthHeight(iis,iis4Check);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static WidthAndHeight getImageWidthHeight(
			InputStream iis, InputStream iis4Check) {
		BufferedImage srcBufImg = readImage(iis,iis4Check);

		String width = String.valueOf(srcBufImg.getWidth());
		String height = String.valueOf(srcBufImg.getHeight());
		WidthAndHeight wh = new WidthAndHeight(width, height);
		
		return wh;
	}
	
	public static WidthAndHeight getSVGWidthHeight(MultipartFile file) {
		try {
			return getSVGWidthAndHeight(file.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			throw new GlobalException(e);
		}
	}

	public static WidthAndHeight getSVGWidthAndHeight(InputStream svgInputStream) {
		String svgCode = InputStream2String(svgInputStream);
		Document svgDom;
		try {
			svgDom = DocumentHelper.parseText(svgCode);
			// 获取viewBox属性
			Element root = svgDom.getRootElement();
			String viewBoxAttr = root.attributeValue("viewBox");
			String width = "";
			String height = "";
			if (viewBoxAttr != null) {
				String[] index = viewBoxAttr.split(" ");
				double xMin = Double.parseDouble(index[0]);
				double yMin = Double.parseDouble(index[1]);
				double xMax = Double.parseDouble(index[2]);
				double yMax = Double.parseDouble(index[3]);

				width = String.valueOf(xMax - xMin);
				height = String.valueOf(yMax - yMin);
			} else {
				width = root.attributeValue("width");
				height = root.attributeValue("height");
			}

			return new WidthAndHeight(width, height);
		} catch (DocumentException e) {
			e.printStackTrace();
			throw new RuntimeException("svg解析错误", e);
		}
	}

	public static InputStream resizeInputStream(InputStream is,
			WidthAndHeight wah) {
		return resizeInputStream(is, Integer.valueOf(wah.getWidth()),
				Integer.valueOf(wah.getHeight()));
	}

	public static InputStream resizeInputStream(InputStream is, Integer width,
			Integer height, String contentType) {
		try {
			BufferedImage srcBufImg = ImageIO.read(is);
			Image img = srcBufImg.getScaledInstance(width, height,
					Image.SCALE_SMOOTH);
			BufferedImage disBufImg = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			disBufImg.getGraphics().drawImage(img, 0, 0, null);

			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			ImageIO.write(disBufImg, contentType,
					ImageIO.createImageOutputStream(bs));
			is = new ByteArrayInputStream(bs.toByteArray());
			return is;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public static InputStream resizeInputStream(InputStream is, Integer width,
			Integer height) {
		return resizeInputStream(is, width, height, "png");
	}

	public static InputStream resizeThumb(InputStream is, int newWidth){
		ByteArrayOutputStream out = null;
		try {
//			float quality = 1;

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] data = new byte[1024];
			int count = -1;

			while ((count = is.read(data)) != -1) {
				bos.write(data, 0, count);
			}

			data = bos.toByteArray();
			ImageIcon ii = new ImageIcon(data);
			Image i = ii.getImage();
			Image resizedImage = null;

			int iWidth = i.getWidth(null);
			int iHeight = i.getHeight(null);

			if (iWidth > iHeight) {
				resizedImage = i.getScaledInstance(newWidth,
						(newWidth * iHeight) / iWidth, Image.SCALE_SMOOTH);
			} else {
				resizedImage = i.getScaledInstance((newWidth * iWidth)
						/ iHeight, newWidth, Image.SCALE_SMOOTH);
			}
			Image temp = new ImageIcon(resizedImage).getImage();

			BufferedImage bufferedImage = new BufferedImage(
					temp.getWidth(null), temp.getHeight(null),
					BufferedImage.TYPE_INT_RGB);

			Graphics g = bufferedImage.createGraphics();

			g.setColor(Color.white);
			g.fillRect(0, 0, temp.getWidth(null), temp.getHeight(null));
			g.drawImage(temp, 0, 0, null);
			g.dispose();

			float softenFactor = 0.05f;
			float[] softenArray = { 0, softenFactor, 0, softenFactor,
					1 - (softenFactor * 4), softenFactor, 0, softenFactor, 0 };
			Kernel kernel = new Kernel(3, 3, softenArray);
			ConvolveOp cOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
			bufferedImage = cOp.filter(bufferedImage, null);

			out = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "png", out);

			ByteArrayInputStream swapStream = new ByteArrayInputStream(
					out.toByteArray());

			return swapStream;

		} catch (IOException e) {
			e.printStackTrace();
			throw new GlobalException("图片缩略出错", e);
		} finally {

			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new GlobalException("流关闭失败", e);
			}
		}

	}
	
	public static InputStream resize(InputStream is, int newWidth) {
		ByteArrayOutputStream out = null;
		try {
//			float quality = 1;

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] data = new byte[1024];
			int count = -1;

			while ((count = is.read(data)) != -1) {
				bos.write(data, 0, count);
			}

			data = bos.toByteArray();
			ImageIcon ii = new ImageIcon(data);
			Image i = ii.getImage();
			Image resizedImage = null;

			int iWidth = i.getWidth(null);
			int iHeight = i.getHeight(null);

			if (iWidth > iHeight) {
				resizedImage = i.getScaledInstance(newWidth,
						(newWidth * iHeight) / iWidth, Image.SCALE_SMOOTH);
			} else {
				resizedImage = i.getScaledInstance((newWidth * iWidth)
						/ iHeight, newWidth, Image.SCALE_SMOOTH);
			}
			Image temp = new ImageIcon(resizedImage).getImage();

			BufferedImage bufferedImage = new BufferedImage(
					temp.getWidth(null), temp.getHeight(null),
					BufferedImage.TYPE_INT_RGB);

			Graphics g = bufferedImage.createGraphics();

			g.setColor(Color.white);
			g.fillRect(0, 0, temp.getWidth(null), temp.getHeight(null));
			g.drawImage(temp, 0, 0, null);
			g.dispose();

			float softenFactor = 0.05f;
			float[] softenArray = { 0, softenFactor, 0, softenFactor,
					1 - (softenFactor * 4), softenFactor, 0, softenFactor, 0 };
			Kernel kernel = new Kernel(3, 3, softenArray);
			ConvolveOp cOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
			bufferedImage = cOp.filter(bufferedImage, null);

			out = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "jpg", out);
//			JPEGImageEncoder encoder = null;
//			JPEGEncodeParam param = null;
//			try {
//				encoder = JPEGCodec.createJPEGEncoder(out);
//				param = encoder
//						.getDefaultJPEGEncodeParam(bufferedImage);
//
//				param.setQuality(quality, true);
//
//				encoder.setJPEGEncodeParam(param);
//				encoder.encode(bufferedImage);
//			} finally {
//				encoder = null;
//				param = null;
//			}

			ByteArrayInputStream swapStream = new ByteArrayInputStream(
					out.toByteArray());

			return swapStream;

		} catch (IOException e) {
			e.printStackTrace();
			throw new GlobalException("图片缩略出错", e);
		} finally {

			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new GlobalException("流关闭失败", e);
			}
		}

	}

	// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
//	public static String GetImageStr(String imgFile) {
//		InputStream in = null;
//		byte[] data = null;
//		// 读取图片字节数组
//		try {
//			in = new FileInputStream(imgFile);
//			data = new byte[in.available()];
//			in.read(data);
//			in.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new RuntimeException("base64编码失败", e);
//		}
//		// 对字节数组Base64编码
//		BASE64Encoder encoder = new BASE64Encoder();
//		return encoder.encode(data);// 返回Base64编码过的字节数组字符串
//	}

	public static String InputStream2String(InputStream is) {
		try {
			String UTF8_BOM = "\uFEFF";
			String content = IOUtils.toString(is, "UTF-8").trim();
			if(content.startsWith(UTF8_BOM)) {
				content = content.replace(UTF8_BOM,"");
			}
			// 过滤特殊字符 \ufeff
			return content.replaceAll("[\\ufeff]", "");
//			return IOUtils.toString(is, "GB2312");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("inputstream转字符串失败", e);
		}
	}

	/**
	 * 生成oss文件访问签名
	 * 
	 * @param key
	 * @return
	 */
	public static String genetateSign(String key) {
		OSSClient client = getOssKey().generateClient();
		Date expires = new Date(new Date().getTime() + 1000 * 60 * 60 * 2); // 有效期2个小时
		WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
		IImgStorage privateIImgStorage = context.getBean("privateImageStorage", IImgStorage.class);
		
		GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
				privateIImgStorage.getBucketName(), key);
		generatePresignedUrlRequest.setExpiration(expires);
		generatePresignedUrlRequest.setMethod(HttpMethod.GET);
		URL url = client.generatePresignedUrl(generatePresignedUrlRequest);
		return url.toString();
	}

	public static String generateSign(StorageBtype businessType, String fileKey) {
		return genetateSign(keyString(businessType, fileKey));
	}

	public static OSSKey getOssKey() {
		OSSKey ossKey = new OSSKey();
		ossKey.setAccessId("5jnc5LMLExLYmSdF");
		ossKey.setAccessKey("vdZULUmBwbTUjrbDi3ZPMzQ0Ul2ywC");
		ossKey.setEndpoint("http://oss-cn-beijing.aliyuncs.com");
		return ossKey;
	}

	protected static String keyString(StorageBtype businessType, String key) {
		return keyString(businessType.getName(), key);
	}

	protected static String keyString(String topDir, String key) {
		return topDir + "/" + key;
	}

	public static InputStream multiCompress(Map<String, InputStream> imgMap) {
		final int BUFFER = 2048;
		byte buffer[] = new byte[BUFFER];

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(out);
		try {
			for (Map.Entry<String, InputStream> m : imgMap.entrySet()) {
				zos.putNextEntry(new ZipEntry(m.getKey()));
				int length;
				while ((length = m.getValue().read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
			}
		} catch (IOException e) {
			throw new GlobalException("压缩流失败", e);
		} finally {
			imgMap.clear();
			imgMap = null;
			try {
				out.close();
			} catch (IOException e) {
				throw new GlobalException("关闭当前out失败", e);
			}
			try {
				zos.closeEntry();
			} catch (IOException e) {
				throw new GlobalException("关闭当前zip的entry失败", e);
			}
			try {
				zos.close();
			} catch (IOException e) {
				throw new GlobalException("关闭当前的zip的输出流失败", e);
			}
		}
		return new ByteArrayInputStream(out.toByteArray());
	}

	public static InputStream mergePdfInputStream(List<InputStream> isList) {
		PDFMergerUtility mergePdf = new PDFMergerUtility();

		mergePdf.addSources(isList);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ByteArrayInputStream reOs = null;
		try {
			mergePdf.setDestinationStream(os);
			mergePdf.mergeDocuments();
		} catch (COSVisitorException | IOException e) {
			throw new GlobalException("合并pdf失败", e);
		}
		try {
			reOs = new ByteArrayInputStream(os.toByteArray());
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				throw new GlobalException("os流关闭失败", e);
			}
			try {
				reOs.close();
			} catch (IOException e) {
				throw new GlobalException("os流关闭失败", e);
			}
		}
		isList.clear();
		
		return reOs;
	}
	
//	public static final Font STKAIti = FontFactory.getFont("stkaiti.ttf",  
//            BaseFont.IDENTITY_H, BaseFont.EMBEDDED);  
//  
//    public static final Font HIMALAYA = FontFactory.getFont("himalaya.ttf",  
//            BaseFont.IDENTITY_H, BaseFont.EMBEDDED);  
	
	public static OutputStream makePdf(List<OutputStream> imgList, OutputStream os) {
		
		ByteArrayOutputStream oss = null;
		try {
			Rectangle pageSize = new Rectangle(PageSize.B1);
            pageSize.setBackgroundColor(BaseColor.WHITE);  
			com.itextpdf.text.Document doc = new com.itextpdf.text.Document(pageSize);
			doc.setMargins(0, 0, 0, 0);
			PdfWriter.getInstance(doc, os);
			doc.open();
			for (int i = 0; i < imgList.size(); i++) {
				doc.newPage();
				oss = (ByteArrayOutputStream)imgList.get(i);
				com.itextpdf.text.Image png = com.itextpdf.text.Image.getInstance(oss.toByteArray());
				png.setAlignment(com.itextpdf.text.Image.MIDDLE);

//				float heigth = png.getHeight();
//                float width = png.getWidth();
				png.scalePercent(100);
				
                doc.add(png);
			}
			doc.close();
			return os;
		} catch (com.itextpdf.text.DocumentException e) {
			throw new GlobalException(e);
        } catch (IOException e) {
        	throw new GlobalException(e);
        } finally {
        	imgList.clear();
        	try {
        		if(os != null){
        			os.close();
        		}
			} catch (IOException e) {
				throw new GlobalException(e);
			}
        	try {
        		if(oss != null){
        			oss.close();
        		}
			} catch (IOException e) {
				throw new GlobalException(e);
			}
        }
	}
	/**
     * 第一种解决方案 在不改变图片形状的同时，判断，如果h>w，则按h压缩，否则在w>h或w=h的情况下，按宽度压缩
     *
     * @param h
     * @param w
     * @return
     */

    public static int getPercent(float h, float w) {
        int p = 0;
        float p2 = 0.0f;
        if (h > w) {
            p2 = 297 / h * 100;
        } else {
            p2 = 210 / w * 100;
        }
        p = Math.round(p2);
        return p;
    }

    /**
     * 第二种解决方案，统一按照宽度压缩 这样来的效果是，所有图片的宽度是相等的，自我认为给客户的效果是最好的
     *
     * @param args
     */
    public static int getPercent2(float h, float w) {
        int p = 0;
        float p2 = 0.0f;
        p2 = 530 / w * 100;
        p = Math.round(p2);
        return p;
    }
    
    private static boolean hasAdobeMarker = false;
//	private static Logger log = LoggerFactory.getLogger(ImageTool.class);

	/*
	 * 返回图片的真实格式，此方法如果读取的图片为svg格式会返回空值
	 */
	public static String getRealFormatName(Object object){
		ImageInputStream iis;
		try {
			iis = ImageIO.createImageInputStream(object);
			Iterator<ImageReader> iterator = ImageIO.getImageReaders(iis);
			while (iterator.hasNext()) {
				ImageReader reader = (ImageReader) iterator.next();
				return reader.getFormatName();
			}
		} catch (IOException e) {
			throw new GlobalException(e);
		}
		
		return null;
	}

	/*
	 * 传图片的路径，和要将图片转成的格式
	 */
	public static void convertTif(String fileName, String extension) {
		try {
			File file = new File(fileName);
			if (file.exists() == false) {
				throw new GlobalException("File "+fileName+" not exist!!!");
			}
			BufferedImage image = ImageIO.read(file);
			BufferedImage convertedImage = new BufferedImage(image.getWidth(),
					image.getHeight(), BufferedImage.TYPE_INT_RGB);
			convertedImage.createGraphics().drawRenderedImage(image, null);
			ImageIO.write(convertedImage, extension, new File(fileName));
		} catch (IOException ex) {
			throw new GlobalException(ex);
		}
	}

	/*
	 * 传进需要读取的图片，fileName是用来记录CMYK图片的名字
	 */
	public static BufferedImage readImage(InputStream iis,InputStream iis4Check) {
		hasAdobeMarker = false;
		ImageInputStream stream;
		try {
			stream = ImageIO.createImageInputStream(iis);
			Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
			
			while (iter.hasNext()) {
				ImageReader reader = iter.next();
				reader.setInput(stream);

				BufferedImage image;
				ICC_Profile profile = null;
				try {
					image = reader.read(0);
				} catch (IIOException e) {
					byte[] bytes = com.aliyun.oss.common.utils.IOUtils.readStreamAsBytesArray(iis4Check);
					checkAdobeMarker(bytes);
					try {
						profile = Sanselan.getICCProfile(bytes);
					} catch (ImageReadException e1) {
						throw new GlobalException("获取icc文件失败："+e1,e1);
					}
					WritableRaster raster = (WritableRaster) reader.readRaster(
							0, null);
					if (hasAdobeMarker) {
						convertInvertedColors(raster);
					}
					image = convertCmykToRgb(raster, profile);
				}
				return image;
			}
		} catch (IOException e1) {
			throw new GlobalException("读取图片流失败：",e1);
		}
		return null;
	}
	
	private static void checkAdobeMarker(byte[] bytes) {
		JpegImageParser parser = new JpegImageParser();
		ByteSource byteSource = new ByteSourceArray(bytes);
		@SuppressWarnings("rawtypes")
		ArrayList segments;
		try {
			segments = parser.readSegments(byteSource, new int[] { 0xffee },
					true);

			if (segments != null && segments.size() >= 1) {
				UnknownSegment app14Segment = (UnknownSegment) segments.get(0);
				byte[] data = app14Segment.bytes;
				if (data.length >= 12 && data[0] == 'A' && data[1] == 'd'
						&& data[2] == 'o' && data[3] == 'b' && data[4] == 'e') {
					hasAdobeMarker = true;
				}
			}
		} catch (ImageReadException e) {
			throw new GlobalException("图片读取失败："+e,e);
		} catch (IOException e) {
			throw new GlobalException("图片文件读取读取失败："+e,e);
		}

	}

	private static void convertInvertedColors(WritableRaster raster) {
		int height = raster.getHeight();
		int width = raster.getWidth();
		int stride = width * 4;
		int[] pixelRow = new int[stride];
		for (int h = 0; h < height; h++) {
			raster.getPixels(0, h, width, 1, pixelRow);
			for (int x = 0; x < stride; x++) {
				pixelRow[x] = 255 - pixelRow[x];
			}
			raster.setPixels(0, h, width, 1, pixelRow);
		}
	}

	private static BufferedImage convertCmykToRgb(Raster cmykRaster,
			ICC_Profile cmykProfile) {
		if (cmykProfile == null) // 读取CMYK的色彩配置文件
		{
			try {
				InputStream in = ImageCommonHandler.class
						.getResourceAsStream("/ISOcoated_v2_300_eci.icc");
				cmykProfile = ICC_Profile.getInstance(in);
			} catch (IOException e) {
				throw new GlobalException("读取icc文件失败",e);
			}
		}
		ICC_ColorSpace cmykCS = new ICC_ColorSpace(cmykProfile);
		BufferedImage rgbImage = new BufferedImage(cmykRaster.getWidth(),
				cmykRaster.getHeight(), BufferedImage.TYPE_INT_RGB);
		WritableRaster rgbRaster = rgbImage.getRaster();
		ColorSpace rgbCS = rgbImage.getColorModel().getColorSpace();
		ColorConvertOp cmykToRgb = new ColorConvertOp(cmykCS, rgbCS, null);
		cmykToRgb.filter(cmykRaster, rgbRaster);
		return rgbImage;
	}
}
