package org.pptx4j.convert.out.svginhtml;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.docx4j.XmlUtils;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.html.HtmlCssHelper;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.CTTextParagraphProperties;
import org.docx4j.dml.CTTransform2D;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.model.styles.Tree;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.plutext.jaxb.svg11.Line;
import org.plutext.jaxb.svg11.ObjectFactory;
import org.plutext.jaxb.svg11.Svg;
import org.pptx4j.Box;
import org.pptx4j.Point;
import org.pptx4j.jaxb.Context;
import org.pptx4j.model.ResolvedLayout;
import org.pptx4j.model.TextStyles;
import org.pptx4j.pml.CxnSp;
import org.pptx4j.pml.GroupShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;

public class SvgExporter {
	
	public static class SvgSettings extends AbstractConversionSettings {
		public static final String IMAGE_TARGET_URI = "imageTargetUri";
		
		public void setImageTargetUri(String imageTargetUri) {
			settings.put(IMAGE_TARGET_URI, imageTargetUri);
		}
		
		public String getImageTargetUri() {
			return (String)settings.get(IMAGE_TARGET_URI);
		}
	}
	
	// NB: file suffix must end with .xhtml in order to see the SVG in a browser
	
	protected static Logger log = LoggerFactory.getLogger(SvgExporter.class);	

	public static JAXBContext jcSVG;	
    static ObjectFactory oFactory;
	static Templates xslt;			
	static {
		
		try {
			jcSVG = JAXBContext.newInstance("org.plutext.jaxb.svg11");
			oFactory = new ObjectFactory();

			Source xsltSource = new StreamSource(
						org.docx4j.utils.ResourceUtils.getResource(
								"org/pptx4j/convert/out/svginhtml/pptx2svginhtml.xslt"));
			xslt = XmlUtils.getTransformerTemplate(xsltSource);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	private static String imageDirPath;
	public static void setImageDirPath(String _imageDirPath) {
		imageDirPath = _imageDirPath;
	}
	
	
	/**
	 * Create an HTML (with SVG) page representing the slide.
	 * @param presentationMLPackage
	 * @param slide
	 * @return
	 * @throws Exception
	 */
	public static String svg(PresentationMLPackage presentationMLPackage,
			SlidePart slide) throws Exception {
		return svg(presentationMLPackage, slide, null);
	}
	
	/**
	 * Create an HTML (with SVG) page representing the slide.
	 * @param presentationMLPackage
	 * @param slide
	 * @param settings
	 * @return
	 * @throws Exception
	 */
	public static String svg(PresentationMLPackage presentationMLPackage,
			SlidePart slide, SvgSettings settings) throws Exception {
		
    	ResolvedLayout rl = ((SlidePart)slide).getResolvedLayout();	

//    	System.out.println( XmlUtils.marshaltoString(rl.getShapeTree(), false, true, Context.jcPML,
//		"http://schemas.openxmlformats.org/presentationml/2006/main", "spTree", GroupShape.class) );
    	
    	return SvgExporter.svg(presentationMLPackage, rl, settings);
	}
	
	/**
	 * @param presentationMLPackage
	 * @param layout
	 * @return
	 * @throws Exception
	 */
	private static String svg(PresentationMLPackage presentationMLPackage,
			ResolvedLayout layout, SvgSettings settings) throws Exception {
	
		ByteArrayOutputStream intermediate = new ByteArrayOutputStream();
		Result intermediateResult =  new StreamResult( intermediate );
			
		svg(presentationMLPackage, layout, intermediateResult, settings);
			
		return intermediate.toString("UTF-8");
		//log.info(svg);
	}
	
	
	private static void svg(PresentationMLPackage presentationMLPackage,
			ResolvedLayout layout, javax.xml.transform.Result result,
			SvgSettings settings) throws Exception {
    SvgConversionContext context = null;			
		org.w3c.dom.Document doc = XmlUtils.marshaltoW3CDomDocument(
				layout.getShapeTree(),
				Context.jcPML,
				"http://schemas.openxmlformats.org/presentationml/2006/main", "spTree", GroupShape.class); 	

		if (settings == null) {
			settings = new SvgSettings();
		}
		if ((settings.getImageDirPath() == null) && (imageDirPath != null)) {
			settings.setImageDirPath(imageDirPath);
		}
		
		context = new SvgConversionContext(settings, presentationMLPackage, layout);
		org.docx4j.XmlUtils.transform(doc, xslt, context.getXsltParameters(), result);
	}

	public static boolean isDebugEnabled() {
		
		return log.isDebugEnabled();
	}
	
    public static DocumentFragment createBlockForP( 
    		SvgConversionContext context,
    		String lvl,
    		String cNvPrName,
    		String phType, NodeIterator childResults, NodeIterator lvlNpPr ) {
    	    	
    	
		StyleTree styleTree = null;
		try {
			styleTree = context.getPmlPackage().getStyleTree();
		} catch (InvalidFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
		log.debug("lvl:" +lvl);
		int level;
		if (lvl.equals("NaN")) {
			level = 1;
		} else {
			level = Integer.parseInt(lvl);
		}
		String pStyleVal;
    	
		System.out.println("cNvPrName: " + cNvPrName + "; " + "phType: " + phType );
		
		if (cNvPrName.toLowerCase().indexOf("subtitle")>-1
				|| phType.toLowerCase().indexOf("subtitle")>-1) {
			// Subtitle on first page in default layout is styled as a Body.
				pStyleVal = "Lvl" + level + "Master" + context.getResolvedLayout().getMasterNumber() + "Body";
		} else if (cNvPrName.toLowerCase().indexOf("title")>-1
			|| phType.toLowerCase().indexOf("title")>-1) {
			pStyleVal = "Lvl" + level + "Master" + context.getResolvedLayout().getMasterNumber() + "Title";
		} else {
			// eg cNvPrName: TextBox 2; phType:
			pStyleVal = "Lvl" + level + "Master" + context.getResolvedLayout().getMasterNumber() + "Other";			
		}
		System.out.println("--> " + pStyleVal );
		
        try {
        	
        	
            // Create a DOM builder and parse the fragment			
			Document document = XmlUtils.getNewDocumentBuilder().newDocument();
			
			//log.info("Document: " + document.getClass().getName() );

			Node xhtmlP = document.createElement("p");			
			document.appendChild(xhtmlP);
									    
			// Set @class
			log.debug(pStyleVal);
			Tree<AugmentedStyle> pTree = styleTree.getParagraphStylesTree();		
			org.docx4j.model.styles.Node<AugmentedStyle> asn = pTree.get(pStyleVal);
			((Element)xhtmlP).setAttribute("class", 
					StyleTree.getHtmlClassAttributeValue(pTree, asn)			
			);
			

			StringBuilder inlineStyle =  new StringBuilder();
			// Do we have CTTextParagraphProperties
			// <a:lvl?pPr>
			// Convert it to a WordML pPr
			CTTextParagraphProperties lvlPPr = unmarshalFormatting(lvlNpPr);
			if (lvlPPr!=null) {
			
				log.debug("We have lvlPPr");
				log.debug(
						XmlUtils.marshaltoString(lvlPPr, true, true, 
								Context.jcPML, "FIXME", "lvl1pPr",
								CTTextParagraphProperties.class)
						);
				PPr pPr = TextStyles.getWmlPPr(lvlPPr);
				if (pPr!=null) {
					HtmlCssHelper.createCss(context.getPmlPackage(), pPr, inlineStyle, false, false);				
				}
				// TODO RPR
			}
			// Without this, top-margin is too large in Webkit (Midor).
			// Not tested elsewhere...
			inlineStyle.append("margin-left:3px; margin-top:3px;");
			
			if (!inlineStyle.toString().equals("") ) {
				((Element)xhtmlP).setAttribute("style", inlineStyle.toString() );
			}
			
			// Our fo:block wraps whatever result tree fragment
			// our style sheet produced when it applied-templates
			// to the child nodes
			// init
			Node n = childResults.nextNode();
			do {	
				
				// getNumberXmlNode creates a span node, which is empty
				// if there is no numbering.
				// Let's get rid of any such <span/>.
				
				// What we actually get is a document node
				if (n.getNodeType()==Node.DOCUMENT_NODE) {
					log.debug("handling DOCUMENT_NODE");
					// Do just enough of the handling here
	                NodeList nodes = n.getChildNodes();
	                if (nodes != null) {
	                    for (int i=0; i<nodes.getLength(); i++) {
	                    	
	        				if (((Node)nodes.item(i)).getLocalName().equals("span")
	        						&& ! ((Node)nodes.item(i)).hasChildNodes() ) {
	        					// ignore
	        					log.debug(".. ignoring <span/> ");
	        				} else {
	        					XmlUtils.treeCopy( (Node)nodes.item(i),  xhtmlP );	        					
	        				}
	                    }
	                }					
				} else {
					
	//					log.info("Node we are importing: " + n.getClass().getName() );
	//					foBlockElement.appendChild(
	//							document.importNode(n, true) );
					/*
					 * Node we'd like to import is of type org.apache.xml.dtm.ref.DTMNodeProxy
					 * which causes
					 * org.w3c.dom.DOMException: NOT_SUPPORTED_ERR: The implementation does not support the requested type of object or operation.
					 * 
					 * See http://osdir.com/ml/text.xml.xerces-j.devel/2004-04/msg00066.html
					 * 
					 * So instead of importNode, use 
					 */
					XmlUtils.treeCopy( n,  xhtmlP );
				}
				// next 
				n = childResults.nextNode();
				
			} while ( n !=null ); 
			
			DocumentFragment docfrag = document.createDocumentFragment();
			docfrag.appendChild(document.getDocumentElement());

			return docfrag;
						
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} 
    	
    	return null;
    	
    }


  private static CTTextParagraphProperties unmarshalFormatting(NodeIterator lvlNpPr ) {
	
	// Get the pPr node as a JAXB object,
	// so we can read it using our standard
	// methods.  Its a bit sad that we 
	// can't just adorn our DOM tree with the
	// original JAXB objects?
	try {
		//CTTextListStyle lstStyle = null;
		CTTextParagraphProperties pPr = null;
		
		if (lvlNpPr!=null) {
			Node n = lvlNpPr.nextNode();
			
			log.debug(n.getClass().getName());
			
			String str = XmlUtils.w3CDomNodeToString(n);
			//log.debug("'" + str + "'");
			
			// Convert to String first ... 
			// unmarshalling the node directly doesn't work as expected
			// (see comment in XmlUtils) 
			
//			if (n!=null) {
//				return  (CTTextParagraphProperties)XmlUtils.unmarshal(n, Context.jcPML, 
//						CTTextParagraphProperties.class);
//			}
			
			if (!str.equals("")) {
				return  (CTTextParagraphProperties)XmlUtils.unmarshalString(str, Context.jcPML, 
						CTTextParagraphProperties.class);
			}
		}
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} 
	return null;
	
}

	public static DocumentFragment createBlockForR(
			SvgConversionContext context, NodeIterator rPrNodeIt,
			NodeIterator childResults) {

		DocumentFragment docfrag = null;
		Document d = null;
		Element span = null;

		try {

			// Create a DOM builder and parse the fragment
			d = XmlUtils.getNewDocumentBuilder().newDocument();

			span = d.createElement("span");
			d.appendChild(span);

			CTTextCharacterProperties textCharProps = (CTTextCharacterProperties) nodeToObjectModel(
					rPrNodeIt.nextNode(), CTTextCharacterProperties.class);

			RPr rPr = TextStyles.getWmlRPr(textCharProps);

			// Does our rPr contain anything else?
			StringBuilder inlineStyle = new StringBuilder();
			HtmlCssHelper.createCss(context.getPmlPackage(), rPr, inlineStyle);
			if (!inlineStyle.toString().equals("")) {
				span.setAttribute("style", inlineStyle.toString());
			}

			Node n = childResults.nextNode();
			XmlUtils.treeCopy(n, span);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			// If something went wrong with the formatting,
			// still try to display the text!
			Node n = childResults.nextNode();
			XmlUtils.treeCopy(n, span);
		}

		// Machinery
		docfrag = d.createDocumentFragment();
		docfrag.appendChild(d.getDocumentElement());
		return docfrag;

	}

	//    public static CTTextListStyle unmarshalFormatting(NodeIterator lstStyleNodeIt ) {
//		
//    	// Get the pPr node as a JAXB object,
//    	// so we can read it using our standard
//    	// methods.  Its a bit sad that we 
//    	// can't just adorn our DOM tree with the
//    	// original JAXB objects?
//    	try {
//			CTTextListStyle lstStyle = null;
//			if (lstStyleNodeIt!=null) {
//				Node n = lstStyleNodeIt.nextNode();
//				if (n!=null) {
//					return  (CTTextListStyle)XmlUtils.unmarshal(n, Context.jcPML, CTTextListStyle.class);
//				}
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//		return null;
//    	
//    }
	
    public static String getCssForStyles(SvgConversionContext context) {
    	
    	StringBuilder result = new StringBuilder();
    	
		StyleTree styleTree=null;
		try {
			styleTree = context.getPmlPackage().getStyleTree();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Second iteration - paragraph level pPr *and rPr*
		result.append("\n /* PARAGRAPH STYLES */ \n");    	
		Tree<AugmentedStyle> pTree = styleTree.getParagraphStylesTree();		
    	for (org.docx4j.model.styles.Node<AugmentedStyle> n : pTree.toList() ) {
    		Style s = n.getData().getStyle();

    		result.append( "."+ s.getStyleId()  + " {display:block;" );
        	if (s.getPPr()==null) {
        		log.debug("null pPr for style " + s.getStyleId());
        	} else {
        		HtmlCssHelper.createCss(context.getPmlPackage(), s.getPPr(), result, false, false );
        	}
        	if (s.getRPr()==null) {
        		log.debug("null rPr for style " + s.getStyleId());
        	} else {
        		HtmlCssHelper.createCss(context.getPmlPackage(), s.getRPr(), result);
        	}
        	result.append( "}\n" );        	
    	}
		    	    	
    	if (log.isDebugEnabled()) {
    		return result.toString();
    	} else {
    		String debug = result.toString();
    		return debug;
    	}
    }
    
    public static DocumentFragment shapeToSVG( 
    		SvgConversionContext context,
    		NodeIterator shapeIt ) {
    	
    	DocumentFragment docfrag=null;
    	Document d=null;
    	
    	try {
    		Object shape = null;    		
    		if (shapeIt!=null) {
    			Node n = shapeIt.nextNode();
    			if (n==null) {
    				d=makeErr( "[null node?!]" );
    			} else {
	    			log.debug("Handling " + n.getNodeName());
	    			
	    			if (n.getNodeName().equals("p:cxnSp") ) {
	    				
	    				shape = nodeToObjectModel(n, CxnSp.class);    				
	    				d = CxnSpToSVG( (CxnSp)shape);
	    				
	    			} else {    			
		    			log.info("** TODO " + n.getNodeName() );
	    				d=makeErr( "[" + n.getNodeName() + "]" );
	    			}
    			}
    		}
    	} catch (Exception e) {
			log.error(e.getMessage(), e);
			d=makeErr(e.getMessage() );
		}
    	
		// Machinery
		docfrag = d.createDocumentFragment();
		docfrag.appendChild(d.getDocumentElement());
		return docfrag;    	
    }
    
    
    /**
     * Connection (line)
     */
    public static Document CxnSpToSVG(CxnSp cxnSp) {
    	
    	// Geometrical transforms
    	CTTransform2D xfrm = cxnSp.getSpPr().getXfrm();
    	Box b = new Box(xfrm.getOff().getX(), xfrm.getOff().getY(),
    			xfrm.getExt().getCx(), xfrm.getExt().getCy() );
    	
    	if (xfrm.getRot()!=0) {
    		b.rotate(xfrm.getRot());
    	}
    	if (xfrm.isFlipH() ) {
    		b.flipH();
    	}
    	if (xfrm.isFlipV() ) {
    		b.flipV();
    	}
    	
    	// Convert from EMU to pixels
    	b.toPixels();

    	// Wrap in a div positioning it on the page
    	Document document = XmlUtils.getNewDocumentBuilder().newDocument();
		Element xhtmlDiv = document.createElement("div");
		// Firefox needs the following; Chrome doesn't
		xhtmlDiv.setAttribute("style", 
				"position: absolute; width:100%; height:100%; left:0px; top:0px;");		
		Node n = document.appendChild(xhtmlDiv);
    	
    	// Convert the object itself to SVG
		Svg svg = oFactory.createSvg();
    	Line line = oFactory.createLine();
    	svg.getSVGDescriptionClassOrSVGAnimationClassOrSVGStructureClass().add(line);
    	
    	line.setX1(b.getOffset().getXAsString() );
    	line.setY1(b.getOffset().getYAsString() );
    	
    	Point otherEnd = b.getOtherCorner();
    	
    	line.setX2( otherEnd.getXAsString() );
    	line.setY2( otherEnd.getYAsString() );

    	line.setStyle("stroke:rgb(99,99,99)");
    	// You can't see the line in Midori, unless you specify the color.
    	// width eg stroke-width:2 is optional
    	
    	Document d2 = XmlUtils.marshaltoW3CDomDocument(svg, jcSVG);   
    	XmlUtils.treeCopy(d2, n);
    	return document;
    	
    }

    private static Document makeErr(String msg) {
    	Document d=XmlUtils.getNewDocumentBuilder().newDocument();
		Element span = d.createElement("span");
		span.setAttribute("style", "color:red;");
		d.appendChild(span);
		
		Text err = d.createTextNode( msg );
		span.appendChild(err);
		return d;
    }

    public static Object nodeToObjectModel(Node n, Class declaredType) throws Docx4JException {
    	
    	if (n==null) {
    		throw new Docx4JException("null input");
    	}
    	
//    	if (log.isDebugEnabled() ) {
//    		System.out.println("IN: " + XmlUtils.w3CDomNodeToString(n));
//    	}
    	
		Object jaxb=null;
		try {
			jaxb = XmlUtils.unmarshal(n, Context.jcPML, declaredType); 
		} catch (JAXBException e1) {
			throw new Docx4JException("Couldn't unmarshall " + XmlUtils.w3CDomNodeToString(n), e1);
		}
		try {
			if (jaxb instanceof JAXBElement ) {
				
				JAXBElement jb = (JAXBElement)jaxb;
				if (jb.getDeclaredType().getName().equals(declaredType.getName() )) {
					return jb.getValue();
				} else {
    				log.error("UNEXPECTED " +
    						XmlUtils.JAXBElementDebug(jb)
    						);
    				throw new Docx4JException("Expected " + declaredType.getName() + " but got " +
    						XmlUtils.JAXBElementDebug(jb) );
				}
			} else if (jaxb.getClass().getName().equals(declaredType.getName() )) {    				
				return jaxb;
			} else {
				log.error( jaxb.getClass().getName() ); 
				throw new Docx4JException("Expected " + declaredType.getName() + " but got " +
						jaxb.getClass().getName() );
			}
		} catch (ClassCastException e) {
			throw new Docx4JException("Expected " + declaredType.getName() + " but got " +
					jaxb.getClass().getName(), e );
		}        	        			    	
    }
    
}
