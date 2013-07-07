/**
 * 
 */
package org.docx4j.convert.out.html;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.ConversionSectionWrapper;
import org.docx4j.convert.out.ConversionSectionWrappers;
import org.docx4j.convert.out.Preprocess;
import org.docx4j.convert.out.html.AbstractHtmlExporter.HtmlSettings;
import org.docx4j.model.BookmarkStartModel;
import org.docx4j.model.fields.FldSimpleModel;
import org.docx4j.model.fields.HyperlinkModel;
import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.model.images.WordXmlPictureE10;
import org.docx4j.model.images.WordXmlPictureE20;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.model.styles.Tree;
import org.docx4j.model.table.TableModel;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * Running Xalan extension functions on Android is problematic:
 * 
 *   http://stackoverflow.com/questions/10579339/is-it-possible-to-call-a-java-extension-function-from-xalan-on-android
 *   
 * so this uses TraversalUtils to generate HTML output
 * without any need for Xalan or XSLT.
 * 
 * We could use a simple JAXB model of HTML, but instead
 * this class uses org.w3c.dom to construct the HTML document.
 * 
 * This class might be neater if it used CompoundTraversalUtilVisitorCallback,
 * but it would be less obvious what is going on.  
 * 
 * @author jharrop
 *
 */
public class HtmlExporterNonXSLT {

	private static Logger log = LoggerFactory.getLogger(HtmlExporterNonXSLT.class);

	protected static String inputfilepath;	
	protected static String outputfilepath;
	
	protected HtmlSettings htmlSettings = null;
	
	org.w3c.dom.Document htmlDoc;
	Element headEl;
	Element bodyEl;
	
	public HtmlExporterNonXSLT(WordprocessingMLPackage wordMLPackage, 
			ConversionImageHandler conversionImageHandler) {
		
		htmlSettings = new HtmlSettings();
		htmlSettings.setWmlPackage(wordMLPackage);
		htmlSettings.setImageHandler(conversionImageHandler);
		//The HtmlExporterNonXSLT seems to ignore header and footers,
		//therefore the ConversionSectionWrapper can be null
	}
	
	/**
	 * Generate HTML for the entire MainDocumentPart.
	 * @return
	 */
	public org.w3c.dom.Document export() {
	HTMLConversionContextNonXSLT conversionContext = null;
	WordprocessingMLPackage originalWmlPackage = (WordprocessingMLPackage)htmlSettings.getWmlPackage();
	WordprocessingMLPackage localWmlPackage = null;
	ConversionSectionWrappers conversionSectionWrappers = null;
		try {
			//Do the default preparations
			localWmlPackage = Preprocess.process(originalWmlPackage, htmlSettings.getFeatures());
			//if the default HTML-features are applied, it will create only one "dummy" ConversionSectionWrapper
			conversionSectionWrappers = Preprocess.createWrappers(localWmlPackage, htmlSettings.getFeatures());
			htmlSettings.setWmlPackage(localWmlPackage);
			//Setup the context
			conversionContext = new HTMLConversionContextNonXSLT(htmlSettings, conversionSectionWrappers);
			
	    	htmlDoc = XmlUtils.neww3cDomDocument();
	    	
	    	Element htmlEl = htmlDoc.createElement("html");
	    	htmlDoc.appendChild(htmlEl);
	    	
	    	// head
	    	headEl = htmlDoc.createElement("head");
	    	htmlEl.appendChild(headEl);
	    	    	    	
	    	// body
	    	bodyEl = htmlDoc.createElement("body");
	    	htmlEl.appendChild(bodyEl);

	    	
			// css
	    	Element styleEl = htmlDoc.createElement("style");
	    	headEl.appendChild(styleEl);
	    	styleEl.appendChild(htmlDoc.createComment(getCss()));		
			
	    	List<ConversionSectionWrapper> sectionWrappers = conversionContext.getSections().getList();
	    	for (int secindex=0; secindex < sectionWrappers.size(); secindex++) {
	    		ConversionSectionWrapper sectionWrapper = sectionWrappers.get(secindex);
	    		conversionContext.getSections().next();
				List blockLevelContent = conversionContext.getSections().getCurrentSection().getContent();
		    	
				HTMLGenerator htmlGenerator = new HTMLGenerator(conversionContext, bodyEl);
				new TraversalUtil(blockLevelContent, htmlGenerator);
	    	}
		}
		catch (Docx4JException de) {
			log.error("Exception generating html", de);
		}
		finally {
			htmlSettings.setWmlPackage(originalWmlPackage);
		}

		return htmlDoc;
	}
	
	public String getCss() {
		StringBuffer buffer = new StringBuffer();
		HTMLConversionContextNonXSLT conversionContext = 
				new HTMLConversionContextNonXSLT(htmlSettings, null);
		HtmlCssHelper.createCssForStyles(conversionContext.getWmlPackage(), 
										 conversionContext.getStyleTree(), 
										 buffer);
		return buffer.toString();
	}
	
	/**
	 * Generate HTML for the specified content.
	 * 
	 * @param blockLevelContent
	 * @return
	 */
	public org.w3c.dom.Document export(Object blockLevelContent, String cssClass, String cssId) {
		HTMLConversionContextNonXSLT conversionContext = 
				new HTMLConversionContextNonXSLT(htmlSettings, null);
		
    	htmlDoc = XmlUtils.neww3cDomDocument();	
    	
    	bodyEl = htmlDoc.createElement("div");
    	if (cssClass!=null) {
    		bodyEl.setAttribute("class", cssClass);
    	}
    	if (cssId!=null) {
    		bodyEl.setAttribute("id", cssId);
    	}
    	htmlDoc.appendChild(bodyEl);    	

    	// TODO: preprocessing?
    	// The default preprocessing can't be done on a partial blockLevelContent
    	// some functionality, like links might not work as they require a current part
		HTMLGenerator htmlGenerator = new HTMLGenerator(conversionContext, bodyEl);
		new TraversalUtil(blockLevelContent, htmlGenerator);

		return htmlDoc;
	}	
	
    class HTMLGenerator extends CallbackImpl {
    	HTMLConversionContextNonXSLT conversionContext = null;
    	
    	Node parentNode;
    	
    	Element currentP; 
    	Element currentSpan;
    	
		Element tr;		
		Element tc;
    	
    	
    	// E20 image
    	Object anchorOrInline;
    	
    	
    	HTMLGenerator(HTMLConversionContextNonXSLT conversionContext, Node parentNode) {
			super();
			this.conversionContext = conversionContext;
			this.parentNode = parentNode;
		}

		@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof P) {
				currentP = htmlDoc.createElement("p");
				currentSpan = null;
				if (tc!=null) {
					tc.appendChild( currentP  );
				} else {
					parentNode.appendChild( currentP  );					
				}
				
				PPr pPr = ((P)o).getPPr();
				handlePPr(pPr, currentP);
				
			} else if (o instanceof org.docx4j.wml.R) {
				
				RPr rPr = ((R)o).getRPr();

				if ( rPr!=null ) {
					// Convert run to span
					Element spanEl = htmlDoc.createElement("span");
					if (currentP==null) {
						// Hyperlink special case
						parentNode.appendChild(spanEl);
					} else {
						currentP.appendChild( spanEl  );
					}
					currentSpan = spanEl;
					
					handleRPr(rPr, currentSpan);
				}
								
			} else if (o instanceof org.docx4j.wml.Text) {
				getCurrentParent().appendChild(htmlDoc.createTextNode(
						((org.docx4j.wml.Text)o).getValue()));
			} else if (o instanceof org.docx4j.wml.CTSimpleField) {

				convertToNode(conversionContext, 
							  o, FldSimpleModel.MODEL_ID,
							  htmlDoc, getCurrentParent());
				

			} else if (o instanceof org.docx4j.wml.P.Hyperlink) {

				convertToNode(conversionContext, 
							  o, HyperlinkModel.MODEL_ID,
							  htmlDoc, getCurrentParent());
				

			} else if (o instanceof org.docx4j.wml.CTBookmark) {

				convertToNode(conversionContext, 
							  o, BookmarkStartModel.MODEL_ID,
							  htmlDoc, getCurrentParent());

			} else if (o instanceof org.docx4j.wml.Tbl) {

				convertToNode(conversionContext, 
							  o, TableModel.MODEL_ID,
							  htmlDoc, (currentP != null ? currentP : parentNode));
				System.out.println("currentP cleared");
				currentP=null;
				currentSpan=null;
				
			} else if (o instanceof org.docx4j.wml.Tr) {
				
				tr = htmlDoc.createElementNS(Namespaces.NS_WORD12, "tr");
				parentNode.appendChild(tr);
				
			} else if (o instanceof org.docx4j.wml.Tc) {
				
				tc = htmlDoc.createElementNS(Namespaces.NS_WORD12, "tc");
				tr.appendChild(tc);
				// now the html p content will go temporarily go in w:tc,
				// which is what we need for our existing table model.
								
			} else if (o instanceof org.docx4j.dml.wordprocessingDrawing.Inline
					|| o instanceof org.docx4j.dml.wordprocessingDrawing.Anchor) {
				
				anchorOrInline = o;  // keep this until we handle CTBlip
				
			} else if (o instanceof org.docx4j.dml.CTBlip) {
	            /*<w:drawing>
	                <wp:inline distT="0" distB="0" distL="0" distR="0">
	                  <a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">
	                    <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
	                      <pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
	                        <pic:blipFill>
	                          <a:blip r:embed="rId10" cstate="print"/> */
				
				DocumentFragment foreignFragment = 
						WordXmlPictureE20.createHtmlImgE20(conversionContext, anchorOrInline);
				anchorOrInline = null;
				
				currentP.appendChild( htmlDoc.importNode(foreignFragment, true) );
				
			} else if (o instanceof org.docx4j.wml.Pict) {
		      /*<w:pict>
		          <v:shape id="_x0000_i1025" type="#_x0000_t75" style="width:428.25pt;height:321pt">
		            <v:imagedata r:id="rId4" o:title=""/>
		          </v:shape> */

				DocumentFragment foreignFragment = 
						WordXmlPictureE10.createHtmlImgE10(conversionContext, o);
				
				currentP.appendChild( htmlDoc.importNode(foreignFragment, true) );
				
			} else {
				log.info("Encountered " + o.getClass().getName() );				
			}
			
			return null;
		}

		private Node getCurrentParent() {
			//this might be executed within a new level of traversal util
			//currentSpan or currentP might be null
			return (currentSpan != null ? 
					   currentSpan :
					   (currentP != null ? currentP : parentNode)
				   );
		}
		
		private void convertToNode(HTMLConversionContextNonXSLT conversionContext, 
								   Object unmarshalledNode, String modelId, 
								   org.w3c.dom.Document document, Node parentNode) throws DOMException {

			// To use our existing model, first we need childResults.
			// We get these using a new Generator object.
			
			DocumentFragment childResults = null;
			if (unmarshalledNode instanceof ContentAccessor) {
				childResults = document.createDocumentFragment();
				HTMLGenerator generator = new HTMLGenerator(conversionContext, childResults);
				new TraversalUtil(((ContentAccessor)unmarshalledNode).getContent(), generator);
			}
			
			Node resultNode = 
				 conversionContext.getModelRegistry().toNode(
						 conversionContext, 
						 unmarshalledNode, 
						 modelId, 
						 childResults, 
						 document);
			
			if (resultNode != null) {
				parentNode.appendChild(resultNode);
			}
		}
    	
    	@Override
		public boolean shouldTraverse(Object o) {
    		if ((o instanceof org.docx4j.wml.Tbl) ||
    			(o instanceof org.docx4j.wml.P.Hyperlink) ||
    			(o instanceof org.docx4j.wml.CTSimpleField)) {
    			return false;
    		} else {
    			return true;
    		}
		}
    	
    	
    	private void handlePPr(PPr pPr, Element currentEl) {

    		if ( pPr!=null ) {
    			
    			String pStyleVal=null;
    			
    			// Set @class
    			if (pPr.getPStyle()!=null
    					&& pPr.getPStyle().getVal()!=null) {

    				pStyleVal = pPr.getPStyle().getVal();						
    				Tree<AugmentedStyle> pTree = conversionContext.getStyleTree().getParagraphStylesTree();		
    				org.docx4j.model.styles.Node<AugmentedStyle> asn = pTree.get(pStyleVal);
    				currentEl.setAttribute("class", 
    						StyleTree.getHtmlClassAttributeValue(pTree, asn)			
    				);
    			}
    						
    			// Does our pPr contain anything else?
    			boolean ignoreBorders = true;
    			StringBuffer inlineStyle =  new StringBuffer();
    			HtmlCssHelper.createCss(conversionContext.getWmlPackage(), pPr, inlineStyle, ignoreBorders);				
    			if (!inlineStyle.toString().equals("") ) {
    				currentEl.setAttribute("style", inlineStyle.toString() );
    			}
    			
    			// Numbering
    			String numberText=null;
    			String numId=null;
    			String levelId=null;
    			if (pPr.getNumPr()!=null) {
    				numId = pPr.getNumPr().getNumId()==null ? null : pPr.getNumPr().getNumId().getVal().toString(); 
    				levelId = pPr.getNumPr().getIlvl()==null ? null : pPr.getNumPr().getIlvl().getVal().toString(); 
    			}
    			
            	ResultTriple triple = org.docx4j.model.listnumbering.Emulator.getNumber(
            			conversionContext.getWmlPackage(), pStyleVal, numId, levelId);   
            	

    			if (triple==null) {
            		log.debug("computed number ResultTriple was null");
            	} else {
    				if (triple.getBullet() != null) {
    					//numberText = (triple.getBullet() + " ");
    					numberText = "\u2022  "; 
    				} else if (triple.getNumString() == null) {
    					log.error("computed NumString was null!");
    					numberText = ("?");
    				} else {
    					numberText = (triple.getNumString() + " ");
    				}
            	}
    			if (numberText!=null) {
    				currentEl.appendChild(htmlDoc.createTextNode(
    						numberText + " "));				
    			}
    		}		
    	}

    	private void handleRPr(RPr rPr, Element spanEl) {

    		// Set @class	
    		if ( rPr.getRStyle()!=null) {
    			String rStyleVal = rPr.getRStyle().getVal();
    			Tree<AugmentedStyle> cTree = conversionContext.getStyleTree().getCharacterStylesTree();		
    			org.docx4j.model.styles.Node<AugmentedStyle> asn = cTree.get(rStyleVal);
    			if (asn==null) {
    				log.warn("No style node for: " + rStyleVal);
    			} else {
    				spanEl.setAttribute("class", 
    						StyleTree.getHtmlClassAttributeValue(cTree, asn)			
    				);		
    			}
    		}
    		
    		// Does our rPr contain anything else?
    		StringBuffer inlineStyle =  new StringBuffer();
    		HtmlCssHelper.createCss(conversionContext.getWmlPackage(), rPr, inlineStyle);				
    		if (!inlineStyle.toString().equals("") ) {
    			spanEl.setAttribute("style", inlineStyle.toString() );
    		}
    			
    	}
    	
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {


		inputfilepath = System.getProperty("user.dir")
//				+ "/hr.docx";
//		+ "/sample-docs/word/sample-docx.docx";
//		+ "/sample-docs/word/2003/word2003-vml.docx";
//				+ "/table-nested.docx";
		+ "/hlink.docx";

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
		
		HtmlExporterNonXSLT withoutXSLT = new HtmlExporterNonXSLT(wordMLPackage, new HTMLConversionImageHandler("c:\\temp", "/bar", true) );
		
		log.info(XmlUtils.w3CDomNodeToString(
				withoutXSLT.export()));

    	// Wondering where <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    	// comes from? See http://stackoverflow.com/questions/1409091/how-do-i-prevent-the-java-xml-transformer-using-html-method-from-adding-meta
	}

}
