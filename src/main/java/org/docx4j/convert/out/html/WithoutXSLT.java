/**
 * 
 */
package org.docx4j.convert.out.html;

import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.docx4j.TraversalUtil;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.model.Model;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.TransformState;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.Tree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.model.table.Cell;
import org.docx4j.model.table.TableModel;
import org.docx4j.model.table.TableModel.TableModelTransformState;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGridCol;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ae.javax.xml.bind.JAXBContext;

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
public class WithoutXSLT {

	private static Logger log = Logger.getLogger(WithoutXSLT.class);
	
	public static JAXBContext context = org.docx4j.jaxb.Context.jc;

	protected static String inputfilepath;	
	protected static String outputfilepath;
	
	org.w3c.dom.Document htmlDoc;
	Element headEl;
	Element bodyEl;
	
	WordprocessingMLPackage wordMLPackage;
	StyleTree styleTree;
	
	public WithoutXSLT(WordprocessingMLPackage wordMLPackage) {
		
		this.wordMLPackage = wordMLPackage;
		
    	htmlDoc = XmlUtils.neww3cDomDocument();
    	    	
    	Element htmlEl = htmlDoc.createElement("html");
    	htmlDoc.appendChild(htmlEl);
    	
    	// head
    	headEl = htmlDoc.createElement("head");
    	htmlEl.appendChild(headEl);
    	
    	// Wondering where <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    	// comes from? See http://stackoverflow.com/questions/1409091/how-do-i-prevent-the-java-xml-transformer-using-html-method-from-adding-meta
    	    	
    	// body
    	bodyEl = htmlDoc.createElement("body");
    	htmlEl.appendChild(bodyEl);
	}
	
	public org.w3c.dom.Document export() {
		
    	styleTree = null;
		try {
			styleTree = wordMLPackage.getMainDocumentPart().getStyleTree();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
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
					numberText = (triple.getBullet() + " ");
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

	void handleRPr(RPr rPr, Element currentEl) {

		if ( rPr!=null ) {
			
			// Convert run to span
			
			Element spanEl = htmlDoc.createElement("span");
			currentEl.appendChild( spanEl  );
			currentEl = spanEl;
		
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
	}
	
	TableModelTransformState tableModelTransformState = new TableModelTransformState();
	
    class HTMLGenerator extends CallbackImpl {
    	
    	Element currentEl; 
    	
    	@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof P) {
				
				currentEl = htmlDoc.createElement("p");
				bodyEl.appendChild( currentEl  );
				
				PPr pPr = ((P)o).getPPr();
				handlePPr(pPr, currentEl);
				
			} else if (o instanceof org.docx4j.wml.R) {
				
				RPr rPr = ((R)o).getRPr();
				handleRPr(rPr, currentEl);
				
			} else if (o instanceof org.docx4j.wml.Text) {
				
				currentEl.appendChild(htmlDoc.createTextNode(
						((org.docx4j.wml.Text)o).getValue()));

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
					
					currentEl.appendChild(htmlTable);
					
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
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

    	Element currentEl; 
		DocumentFragment tableFragment = htmlDoc.createDocumentFragment();
		Element tr;
		
    	@Override
		public List<Object> apply(Object o) {
			
			if (o instanceof P) {
				
				Element p = htmlDoc.createElement("p");
				currentEl.appendChild( p  );
				currentEl = p;
				
				PPr pPr = ((P)o).getPPr();
				handlePPr(pPr, currentEl);
				
			} else if (o instanceof org.docx4j.wml.R) {
				
				RPr rPr = ((R)o).getRPr();
				handleRPr(rPr, currentEl);
				
			} else if (o instanceof org.docx4j.wml.Text) {
				
				currentEl.appendChild(htmlDoc.createTextNode(
						((org.docx4j.wml.Text)o).getValue()));

			} else if (o instanceof org.docx4j.wml.Tbl) {

				// TODO: haven't considered nested tables
				
			} else if (o instanceof org.docx4j.wml.Tr) {
				
				tr = htmlDoc.createElementNS(Namespaces.NS_WORD12, "tr");
				tableFragment.appendChild(tr);
				
			} else if (o instanceof org.docx4j.wml.Tc) {
				
				Element tc = htmlDoc.createElementNS(Namespaces.NS_WORD12, "tc");
				tr.appendChild(tc);
				currentEl = tc;
				// now the html p content will go temporarily go in w:tc!
				
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
				+ "/sample-docs/word/sample-docx.xml";

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage
				.load(new java.io.File(inputfilepath));
		
		WithoutXSLT withoutXSLT = new WithoutXSLT(wordMLPackage);
				
		log.info(XmlUtils.w3CDomNodeToString(
				withoutXSLT.export()));

	}

}
