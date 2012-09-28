/**
 * 
 */
package org.docx4j.convert.out.html;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.model.images.ConversionImageHandler;
import org.docx4j.model.images.WordXmlPictureE10;
import org.docx4j.model.images.WordXmlPictureE20;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.model.styles.Tree;
import org.docx4j.model.table.TableModel;
import org.docx4j.model.table.TableModel.TableModelTransformState;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Tbl;
import org.w3c.dom.Attr;
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

	private static Logger log = Logger.getLogger(HtmlExporterNonXSLT.class);
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc;

	protected static String inputfilepath;	
	protected static String outputfilepath;
	
	org.w3c.dom.Document htmlDoc;
	Element headEl;
	Element bodyEl;
	
	WordprocessingMLPackage wordMLPackage;
	StyleTree styleTree;
	
	ConversionImageHandler conversionImageHandler;
	
	public HtmlExporterNonXSLT(WordprocessingMLPackage wordMLPackage, ConversionImageHandler conversionImageHandler) {
		
		this.wordMLPackage = wordMLPackage;
		this.conversionImageHandler = conversionImageHandler;

    	styleTree = null;
		try {
			styleTree = wordMLPackage.getMainDocumentPart().getStyleTree();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	
	/**
	 * Generate HTML for the entire MainDocumentPart.
	 * @return
	 */
	public org.w3c.dom.Document export() {
				
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
    	styleEl.appendChild(
    			htmlDoc.createComment(AbstractHtmlExporter.getCssForStyles(wordMLPackage)));		
		
    	
		List blockLevelContent = wordMLPackage.getMainDocumentPart().getContent();
    	
		HTMLGenerator htmlGenerator = new HTMLGenerator();
		new TraversalUtil(blockLevelContent, htmlGenerator);

		return htmlDoc;
	}
	
	public String getCss() {
		
		return AbstractHtmlExporter.getCssForStyles(wordMLPackage);
	}
	
	/**
	 * Generate HTML for the specified content.
	 * 
	 * @param blockLevelContent
	 * @return
	 */
	public org.w3c.dom.Document export(Object blockLevelContent, String cssClass, String cssId) {
		
    	htmlDoc = XmlUtils.neww3cDomDocument();	
    	
    	bodyEl = htmlDoc.createElement("div");
    	if (cssClass!=null) {
    		bodyEl.setAttribute("class", cssClass);
    	}
    	if (cssId!=null) {
    		bodyEl.setAttribute("id", cssId);
    	}
    	htmlDoc.appendChild(bodyEl);    	
		
		HTMLGenerator htmlGenerator = new HTMLGenerator();
		new TraversalUtil(blockLevelContent, htmlGenerator);

		return htmlDoc;
	}	
	
	void handlePPr(PPr pPr, Element currentEl) {

		if ( pPr!=null ) {
			
			String pStyleVal=null;
			
			// Set @class
			if (pPr.getPStyle()!=null
					&& pPr.getPStyle().getVal()!=null) {

				pStyleVal = pPr.getPStyle().getVal();						
				Tree<AugmentedStyle> pTree = styleTree.getParagraphStylesTree();		
				org.docx4j.model.styles.Node<AugmentedStyle> asn = pTree.get(pStyleVal);
				currentEl.setAttribute("class", 
						StyleTree.getHtmlClassAttributeValue(pTree, asn)			
				);
			}
						
			// Does our pPr contain anything else?
			boolean ignoreBorders = true;
			StringBuffer inlineStyle =  new StringBuffer();
			AbstractHtmlExporter.createCss(wordMLPackage, pPr, inlineStyle, ignoreBorders);				
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
        			wordMLPackage, pStyleVal, numId, levelId);   
        	

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

	void handleRPr(RPr rPr, Element spanEl) {

		// Set @class	
		if ( rPr.getRStyle()!=null) {
			String rStyleVal = rPr.getRStyle().getVal();
			Tree<AugmentedStyle> cTree = styleTree.getCharacterStylesTree();		
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
		AbstractHtmlExporter.createCss(wordMLPackage, rPr, inlineStyle);				
		if (!inlineStyle.toString().equals("") ) {
			spanEl.setAttribute("style", inlineStyle.toString() );
		}
			
	}
	
	TableModelTransformState tableModelTransformState = new TableModelTransformState();
	
    class HTMLGenerator extends CallbackImpl {
    	
    	Element currentP; 
    	Element currentSpan;
    	
    	// E20 image
    	Object anchorOrInline;
    	
    	@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof P) {
				
				currentP = htmlDoc.createElement("p");
				currentSpan = null;
				bodyEl.appendChild( currentP  );
				
				PPr pPr = ((P)o).getPPr();
				handlePPr(pPr, currentP);
				
			} else if (o instanceof org.docx4j.wml.R) {
				
				RPr rPr = ((R)o).getRPr();

				if ( rPr!=null ) {
					// Convert run to span
					Element spanEl = htmlDoc.createElement("span");
					currentP.appendChild( spanEl  );
					currentSpan = spanEl;
					
					handleRPr(rPr, currentSpan);
				}
								
			} else if (o instanceof org.docx4j.wml.Text) {
				
				if (currentSpan!=null) {
					currentSpan.appendChild(htmlDoc.createTextNode(
							((org.docx4j.wml.Text)o).getValue()));
				} else {
					currentP.appendChild(htmlDoc.createTextNode(
							((org.docx4j.wml.Text)o).getValue()));					
				}

			} else if (o instanceof org.docx4j.wml.Tbl) {

				Tbl tbl = (org.docx4j.wml.Tbl)o;
				// To use our existing model, first we need
				// childResults
				TableRowTraversor tableRowTraversor = new TableRowTraversor();
				new TraversalUtil(
						tbl.getContent(), tableRowTraversor);
				
//				NodeList childResults = tableRowTraversor.tableFragment.getChildNodes();
				
				try {
					TableModel tm = new TableModel();
					tm.setWordMLPackage(wordMLPackage);
					tm.build(tbl, tableRowTraversor.tableFragment);
					
					TableWriter tableWriter = new TableWriter();
					tableWriter.setWordMLPackage(wordMLPackage);
					Node htmlTable = tableWriter.toNode(tm, tableModelTransformState, htmlDoc);
					
					currentP.appendChild(htmlTable);
					
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
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
				
				DocumentFragment foreignFragment = WordXmlPictureE20.createHtmlImgE20(wordMLPackage, 
						conversionImageHandler, anchorOrInline);
				anchorOrInline = null;
				
				currentP.appendChild( htmlDoc.importNode(foreignFragment, true) );
				
			} else if (o instanceof org.docx4j.wml.Pict) {
		      /*<w:pict>
		          <v:shape id="_x0000_i1025" type="#_x0000_t75" style="width:428.25pt;height:321pt">
		            <v:imagedata r:id="rId4" o:title=""/>
		          </v:shape> */

				DocumentFragment foreignFragment = WordXmlPictureE10.createHtmlImgE10(wordMLPackage, 
						conversionImageHandler, o);
				
				currentP.appendChild( htmlDoc.importNode(foreignFragment, true) );
				
			} else {
				log.info("Encountered " + o.getClass().getName() );				
			}
			
			return null;
		}
    	
    	@Override
		public boolean shouldTraverse(Object o) {
    		if (o instanceof org.docx4j.wml.Tbl) {
    			return false;
    		} else {
    			return true;
    		}
		}
    	
    	
	}

    class TableRowTraversor extends CallbackImpl {

    	Element currentP; 
    	Element currentSpan; 

    	DocumentFragment tableFragment = htmlDoc.createDocumentFragment();
		Element tr;		
		Element tc;
		
    	@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof P) {
				
				currentP = htmlDoc.createElement("p");
				currentSpan = null;
				tc.appendChild( currentP  );
				
				PPr pPr = ((P)o).getPPr();
				handlePPr(pPr, currentP);
				
			} else if (o instanceof org.docx4j.wml.R) {
				
				RPr rPr = ((R)o).getRPr();

				if ( rPr!=null ) {
					// Convert run to span
					Element spanEl = htmlDoc.createElement("span");
					currentP.appendChild( spanEl  );
					currentSpan = spanEl;
					
					handleRPr(rPr, currentSpan);
				}
								
			} else if (o instanceof org.docx4j.wml.Text) {
				
				if (currentSpan!=null) {
					currentSpan.appendChild(htmlDoc.createTextNode(
							((org.docx4j.wml.Text)o).getValue()));
				} else {
					currentP.appendChild(htmlDoc.createTextNode(
							((org.docx4j.wml.Text)o).getValue()));					
				}


			} else if (o instanceof org.docx4j.wml.Tbl) {

				// TODO: haven't considered nested tables
				
			} else if (o instanceof org.docx4j.wml.Tr) {
				
				tr = htmlDoc.createElementNS(Namespaces.NS_WORD12, "tr");
				tableFragment.appendChild(tr);
				
			} else if (o instanceof org.docx4j.wml.Tc) {
				
				tc = htmlDoc.createElementNS(Namespaces.NS_WORD12, "tc");
				tr.appendChild(tc);
				// now the html p content will go temporarily go in w:tc,
				// which is what we need for our existing table model.
				
			} else {
				log.info("Encountered " + o.getClass().getName() );				
			}
			
			return null;
		}
    	
    }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {


		inputfilepath = System.getProperty("user.dir")
				+ "/hr.docx";
//		+ "/sample-docs/word/sample-docx.xml";
//		+ "/sample-docs/word/2003/word2003-vml.docx";

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
		
		HtmlExporterNonXSLT withoutXSLT = new HtmlExporterNonXSLT(wordMLPackage, new HTMLConversionImageHandler("c:\\temp", "/bar", true) );
		
		log.info(XmlUtils.w3CDomNodeToString(
				withoutXSLT.export()));

    	// Wondering where <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    	// comes from? See http://stackoverflow.com/questions/1409091/how-do-i-prevent-the-java-xml-transformer-using-html-method-from-adding-meta
	}

}
