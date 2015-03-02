

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPath;
import org.apache.batik.util.XMLResourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.eyuanku.web.framework.exce.GlobalException;
import com.eyuanku.web.framework.svg.handler.ImageHttpReqHandler;
import com.eyuanku.web.framework.util.GlobalConfig;

public final class SvgImage {

	private static Logger logger = LoggerFactory.getLogger(SvgImage.class);
	
	public static final String x = "0";
	public static final String y = "0";
	public static final String sep = " ";

	public static final String DEFAULT_BACKGROUND = "rgb(255, 255, 255)";
	
	private final GraphicsNode rootSvgNode;

	private final SVGDocument svgDocument;

	private final Document document;

	private final Element rootNode;
	
	private static GlobalConfig globalConfig = null;

	private static String parser = XMLResourceDescriptor
			.getXMLParserClassName();
	private static SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(
			parser);

	private static UserAgentAdapter userAgentAdapter = new UserAgentAdapter();
	private static BridgeContext bridgeContext = new BridgeContext(
			userAgentAdapter);
	private static GVTBuilder builder = new GVTBuilder();

//	private static WebApplicationContext context = ContextLoader
//			.getCurrentWebApplicationContext();
//	private static GloableConfig config = context == null ? null : context
//			.getBean(GloableConfig.class);

	public SvgImage(URL url) {
		svgDocument = generateSVGDoc(url.toString());
		rootSvgNode = getRootNode(svgDocument);
		document = null;
		rootNode = null;
	}

	public SvgImage(File file) {
		svgDocument = generateSVGDoc(file.toURI().toString());
		rootSvgNode = getRootNode(svgDocument);
		document = null;
		rootNode = null;
	}

	public SvgImage(SVGDocument doc) {
		svgDocument = doc;
		rootSvgNode = getRootNode(svgDocument);
		document = null;
		rootNode = null;
	}

	public SvgImage(String svgUrl, boolean isLayout) {
		if (isLayout) {
			svgDocument = null;
			rootSvgNode = null;
			document = checkImageNode(svgUrl);
			rootNode = document.getDocumentElement();
		} else {
			svgDocument = generateSVGDoc(svgUrl);
			rootSvgNode = getRootNode(svgDocument);
			document = null;
			rootNode = null;
		}

	}

	public SvgImage(String svgUrl, Map<String,String> imgMap) {
		svgDocument = null;
		rootSvgNode = null;
		document = checkImageNode(svgUrl);
		rootNode = document.getDocumentElement();
	}
	
	private static SVGDocument generateSVGDoc(String url) {
		try {
			return (SVGDocument) factory.createSVGDocument(url.toString());
		} catch (IOException e) {
			throw new GlobalException("解析svg失败", e);
		}
	}

	private static GraphicsNode getRootNode(SVGDocument document) {
		return builder.build(bridgeContext, document);
	}

	private static Document checkImageNode(String svgUrl) {
		logger.debug(svgUrl);
		Document doc = str2Dom(ImageHttpReqHandler.httpGetRequest(svgUrl));
		NodeList nodeList = doc.getElementsByTagName("image");
		Element e = null;

		for (int i = 0; i < nodeList.getLength(); i++) {
			e = (Element) nodeList.item(i);
// 			String href = e.getAttribute("xlink:href");
 			e.setAttribute("xlink:href", getBlankImage());
//			if (href == null || "".equals(href)) {
//				e.setAttribute("xlink:href", getBlankImage());
//			}
		}
		nodeList = null;
		e = null;
		return doc;
	}

	private static String getBlankImage() {
		if (globalConfig == null) {
			globalConfig = GlobalConfig.getInstance();
		}
		return "http://" + globalConfig.getOssInterHost() + "/sys/pic.png";
	}

	public Image getImage(int width, int height) {
		BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		Element elt = svgDocument.getRootElement();
		AffineTransform usr2dev = ViewBox.getViewTransform(null, elt, width,
				height, null);
		g2d.transform(usr2dev);
		rootSvgNode.paint(g2d);

		g2d.dispose();

		return bufferedImage;
	}

	public static SVGDocument createRootDocument() {
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		Document doc = impl.createDocument(svgNS, "svg", null);
		SVGDocument svgDoc = (SVGDocument) doc;
		return svgDoc;
	}

	// test
	public static void createShape(SVGGeneratorContext ctx) {
		// ctx = SVGGeneratorContext.createDefault(doc);
		Shape a = new Rectangle(10, 20, 200, 200);
		SVGPath aa = new SVGPath(ctx);
		aa.toSVG(a);
		System.out.println(SVGPath.toSVGPathData(a, ctx));
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

	public static Document str2Dom(String xmlStr) {
		StringReader sr = new StringReader(xmlStr);
		InputSource is = new InputSource(sr);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		Document doc;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(is);
			return doc;
		} catch (ParserConfigurationException e) {
			throw new GlobalException(e);
		} catch (SAXException | IOException e) {
			throw new GlobalException(e);
		}
	}

	/**
	 * 将image转成byte[]
	 * 用不到暂时   data:image/jpg;base64," + Base64.encode(SvgImage.convertImage2ByteArr(imageUrl))
	 */
	public static byte[] convertImage2ByteArr(String imageUrl) {
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(new URL(imageUrl));
		} catch (IOException e) {
			throw new GlobalException("图片加载失败:"+e, e);
		}
		
		WritableRaster raster = bufferedImage.getRaster();
		DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
		
		return data.getData();
	}
	
	public static SVGDocument str2SVGDom(String xmlStr) {
		return (SVGDocument) str2SVGDom(xmlStr);
	}

	public GraphicsNode getRootSvgNode() {
		return rootSvgNode;
	}

	public SVGDocument getSvgDocument() {
		return svgDocument;
	}

	public Document getDocument() {
		return document;
	}

	public Element getRootNode() {
		return rootNode;
	}

}
