package org.docx4j.convert.out.html;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.AbstractConversionSettings;
import org.docx4j.convert.out.Output;
import org.docx4j.fonts.Mapper;
import org.docx4j.jaxb.Context;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.listnumbering.Emulator.ResultTriple;
import org.docx4j.model.properties.AdHocProperty;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.PBorderBottom;
import org.docx4j.model.properties.paragraph.PBorderTop;
import org.docx4j.model.properties.paragraph.PShading;
import org.docx4j.model.properties.run.Font;
import org.docx4j.model.properties.table.BorderBottom;
import org.docx4j.model.properties.table.BorderLeft;
import org.docx4j.model.properties.table.BorderRight;
import org.docx4j.model.properties.table.BorderTop;
import org.docx4j.model.styles.StyleTree;
import org.docx4j.model.styles.StyleTree.AugmentedStyle;
import org.docx4j.model.styles.Tree;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTTblPrBase;
import org.docx4j.wml.CTTblStylePr;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TrPr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;

public abstract class AbstractHtmlExporter implements Output {
	
	
	protected static Logger log = Logger.getLogger(AbstractHtmlExporter.class);
	
	
	// Implement the interface.  
	public abstract void output(javax.xml.transform.Result result) throws Docx4JException;
	// End interface
	
	WordprocessingMLPackage wmlPackage;
	public void setWmlPackage(WordprocessingMLPackage wmlPackage) {
		this.wmlPackage = wmlPackage;
	}

	HtmlSettings htmlSettings;
	public void setHtmlSettings(HtmlSettings htmlSettings) {
		this.htmlSettings = htmlSettings;
	}
	
	
	
	/** Create an html version of the document, using CSS font family
	 *  stacks.  
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
	@Deprecated	
    public abstract void html(WordprocessingMLPackage wmlPackage, 
    		javax.xml.transform.Result result,
    		String imageDirPath) throws Exception;
    
	@Deprecated	
    public abstract void html(WordprocessingMLPackage wmlPackage, 
    		javax.xml.transform.Result result, 
    		boolean fontFamilyStack,
    		String imageDirPath) throws Exception; 
    
	/** Create an html version of the document. 
	 * 
	 * @param result
	 *            The javax.xml.transform.Result object to transform into 
	 * 
	 * */ 
    public abstract void html(WordprocessingMLPackage wmlPackage, 
    		javax.xml.transform.Result result, 
    		HtmlSettings htmlSettings) throws Exception;     

    /* 
    
		<w:hyperlink r:id="rId4" w:history="true">
			<w:r>
				<w:rPr>
				    <w:rStyle w:val="Hyperlink"/>
				</w:rPr>
				<w:t>hyperlink</w:t>
			</w:r>
		</w:hyperlink>
	
	  Micrososoft C# code replaces w:hyperlink with 
	  a new node 
	  
	      <w:hlink w:dest=".." [other attributes cloned] />
	      
	  before the XSLT is called.
	
	  But we use an extension function instead.
                    
                    */    
    public static String resolveHref( WordprocessingMLPackage wmlPackage, String id  )  {
    	
    	org.docx4j.relationships.Relationship rel = wmlPackage.getMainDocumentPart().getRelationshipsPart().getRelationshipByID(id);
    	
    	if ( rel != null) {
    		
        	// TODO resolve ServerRelativePath, if its not a full URL 

    		return rel.getTarget();
    		
    	} else {
    		
    		log.error("Couldn't resolve hyperlink for rel " + id);    		
    		return "";    		
    	}
    }
    
    

    
    /**
	 * The method used by the XSLT extension function during HTML export.
	 * 
	 * If there is no number, it returns an empty span element.
	 * 
	 * @param em
	 * @param levelId
	 * @param numId
	 * @return
	 */
    public static String getNumberXmlNode(WordprocessingMLPackage wmlPackage,
    		NodeIterator pPrNodeIt,
    		String pStyleVal, String numId, String levelId) {
    	
    	
    	// Note that this is invoked for every paragraph with a pPr node.
    	
    	log.debug("numbering, using style '" + pStyleVal + "'; numId=" + numId + "; ilvl " + levelId);    	
    	
        try {
        	        	
        	ResultTriple triple = org.docx4j.model.listnumbering.Emulator.getNumber(
        			wmlPackage, pStyleVal, numId, levelId);   
        	

			if (triple==null) {
        		log.debug("computed number ResultTriple was null");
        		return null;
        	}
			
			String styleVal = "";
			
    		if (triple.getBullet()!=null ) {
    			//return (triple.getBullet() + " " );  
    			// The old code did that:-
    			// https://github.com/plutext/docx4j/commit/7627863e47c5dc7b3c91290b8d993ae5a7cd9fab#docx4j/src/main/java/org/docx4j/convert/out/html/AbstractHtmlExporter.java
    			//What is wrong with that approach?
    			// 
    			return "\u2022  "; 
				// see notes in docx2xhtmlNG2.xslt as to why we don't use &bull;
    			
    			
    		} else if (triple.getNumString()==null) {
	    		log.error("computed NumString was null!");
	    		return "?  ";
	    		
	    	} else {
				return triple.getNumString() + " " ;
	    	}
			
		} catch (Exception e) {
			e.printStackTrace();
			// System.out.println(e.toString() );
			log.error(e);
		} 
    	
		return "?  ";
    	
    }
    
    
    
    
//    // Parses a string containing XML and returns a DocumentFragment
//    // containing the nodes of the parsed XML.
//    public static DocumentFragment parseXml(String fragment) {
//        // Wrap the fragment in an arbitrary element
//        fragment = "<p>"+fragment+"</p>";
//        try {
//            // Create a DOM builder and parse the fragment
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            
//            //Document d = factory.newDocumentBuilder().newDocument();
//            
//            Document d = factory.newDocumentBuilder().parse(
//                new InputSource(new StringReader(fragment)));
//        
//            // Create the document fragment node to hold the new nodes
//            DocumentFragment docfrag = d.createDocumentFragment();
//            
//            docfrag.appendChild(d.getDocumentElement());        
//            return docfrag;
//        } catch (Exception e) {
//        	log.error(e);
//            return null;
//        }
//    }    
    
	public static class HtmlSettings extends AbstractConversionSettings {
		
		public static final String IMAGE_TARGET_URI = "imageTargetUri";
		public static final String CONDITIONAL_COMMENTS = "conditionalComments";
		public static final String FONT_FAMILY_STACK = "fontFamilyStack";
		public static final String USER_CSS = "userCSS";
		public static final String USER_SCRIPT = "userScript";
		public static final String USER_BODY_TOP = "userBodyTop";
		public static final String USER_BODY_TAIL = "userBodyTail";
		
		public HtmlSettings() {
			settings.put(CONDITIONAL_COMMENTS, Boolean.FALSE);
			settings.put(FONT_FAMILY_STACK,     Boolean.FALSE);
			
			settings.put(USER_CSS, "");
			settings.put(USER_SCRIPT, "");
			settings.put(USER_BODY_TOP, "<!-- userBodyTop goes here -->");
			settings.put(USER_BODY_TAIL, "<!-- userBodyTail goes here -->");
			
		}
		
		public void setConditionalComments(Boolean conditionalComments) {
			settings.put(CONDITIONAL_COMMENTS, conditionalComments);
		}
		
		public void setFontFamilyStack(boolean val) {
			settings.put(FONT_FAMILY_STACK, new Boolean(val));
		}

		public void setFontMapper(Mapper fontMapper) {
			settings.put("fontMapper", fontMapper);
		}
		public Mapper getFontMapper() {
			return (Mapper)settings.get("fontMapper");
		}
				
		public void setUserCSS(String val) {
			settings.put(USER_CSS, val);
		}

		public void setUserScript(String val) {
			settings.put(USER_SCRIPT, val);
		}

		public void setUserBodyTop(String val) {
			settings.put(USER_BODY_TOP, val);
		}

		public void setUserBodyTail(String val) {
			settings.put(USER_BODY_TAIL, val);
		}		
		
		public void setImageTargetUri(String imageTargetUri) {
			settings.put(IMAGE_TARGET_URI, imageTargetUri);
		}
		
		public String getImageTargetUri() {
			return (String)settings.get(IMAGE_TARGET_URI);
		}
	}
	
    public static String getCssForStyles(WordprocessingMLPackage wmlPackage) {
    	
    	StringBuffer result = new StringBuffer();
    	
    	StyleTree styleTree = null;
		try {
			styleTree = wmlPackage.getMainDocumentPart().getStyleTree();
		} catch (Exception e) {
			log.error("Couldn't getStyleTree", e);
    		return result.toString();			
		}

		// First iteration - table styles
		result.append("\n /* TABLE STYLES */ \n");    	
		Tree<AugmentedStyle> tableTree = styleTree.getTableStylesTree();		
    	for (org.docx4j.model.styles.Node<AugmentedStyle> n : tableTree.toList() ) {
    		Style s = n.getData().getStyle();

    		result.append( "."+ s.getStyleId()  + " {display:table;" );
    		
    		// TblPr
    		if (s.getTblPr()==null) {
    		} else {
    			log.debug("Applying tblPr..");
            	createCss(s.getTblPr(), result);
            	
    		}
    		
    		// TblStylePr - STTblStyleOverrideType stuff
    		if (s.getTblStylePr()==null) {
    		} else {
    			log.debug("Applying tblStylePr.. TODO!");
    			// Its a list, created automatically
            	createCss(s.getTblStylePr(), result);
    		}
    		
    		
    		// TrPr - eg jc, trHeight, wAfter, tblCellSpacing
    		if (s.getTrPr()==null) {
    		} else {
    			log.debug("Applying trPr.. TODO!");
            	createCss( s.getTrPr(), result);
    		}
    		
    		// TcPr - includes includes TcPrInner.TcBorders, CTShd, TcMar, CTVerticalJc
    		if (s.getTcPr()==null) {
    		} else {
    			log.debug("Applying tcPr.. ");
            	createCss( s.getTcPr(), result);
    		}
    		    		
        	if (s.getPPr()==null) {
        		log.debug("null pPr for style " + s.getStyleId());
        	} else {
        		createCss( wmlPackage, s.getPPr(), result, false );
        	}
        	if (s.getRPr()==null) {
        		log.debug("null rPr for style " + s.getStyleId());
        	} else {
            	createCss(wmlPackage, s.getRPr(), result);
        	}
        	result.append( "}\n" );         	
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
        		createCss( wmlPackage, s.getPPr(), result, false );
        	}
        	if (s.getRPr()==null) {
        		log.debug("null rPr for style " + s.getStyleId());
        	} else {
            	createCss(wmlPackage, s.getRPr(), result);
        	}
        	result.append( "}\n" );        	
    	}
		    	
	    // Third iteration, character styles
		result.append("\n /* CHARACTER STYLES */ ");
		//result.append("\n /* These come last, so they have more weight than the paragraph _rPr component styles */ \n ");
		
		Tree<AugmentedStyle> cTree = styleTree.getCharacterStylesTree();		
    	for (org.docx4j.model.styles.Node<AugmentedStyle> n : cTree.toList() ) {
    		Style s = n.getData().getStyle();

    		result.append( "."+ s.getStyleId()  + " {display:inline;" );
        	if (s.getRPr()==null) {
        		log.warn("! null rPr for character style " + s.getStyleId());
        	} else {
            	createCss(wmlPackage, s.getRPr(), result);
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
    
    public static String getCssForTableCells(WordprocessingMLPackage wmlPackage, 
    		NodeIterator tables) {
    	
    	// The only way we seem to be able to make rules which
    	// apply to all the cells in a particular table
    	
    	Tbl tbl;
    	StringBuffer result = new StringBuffer();		
    	
		//DTMNodeProxy n = (DTMNodeProxy)tables.nextNode();
    	Element n = (Element)tables.nextNode();
		if (n==null) {
			// No tables in this document
			return "";
		}    	
    	int idx = 0;
		do {
			if (n.getNodeName().equals("w:tbl" )) {
				// n.getLocalName() -> tbl
				// n.getNodeName() -> w:tbl

    			Object jaxb;
				try {
					Unmarshaller u = Context.jc.createUnmarshaller();			
					u.setEventHandler(new org.docx4j.jaxb.JaxbValidationEventHandler());
					jaxb = u.unmarshal(n);
    				tbl =  (Tbl)jaxb;
    				
    				result.append(getCssForTableCells(wmlPackage, tbl,  idx) );
    				
				} catch (JAXBException e1) {
    		    	log.error("JAXB error", e1);
    			} catch (ClassCastException e) {
    		    	log.error("Couldn't cast to Tbl!");
    			}        	        			
				
			} else {
				log.warn("Expected table but encountered: " + n.getNodeName() );
			}
			// next 
			idx++;
			n = (Element)tables.nextNode();
			
		} while ( n !=null ); 
    	
		return result.toString();
    }
    
    
    public static String getCssForTableCells(WordprocessingMLPackage wmlPackage, 
    		Tbl tbl, int idx) {
    	
    	StringBuffer result = new StringBuffer();		
		PropertyResolver pr;
		try {
			pr = new PropertyResolver(wmlPackage);
		} catch (Docx4JException e) {
	    	log.error("docx4j error", e);
	    	return e.getMessage();
		} 
		Style s = pr.getEffectiveTableStyle(tbl.getTblPr() );
		
		result.append("#" + TableWriter.getId(idx) + " td { ");
    	List<Property> properties =  new ArrayList<Property>();
    	if (s.getTblPr()!=null
    			&& s.getTblPr().getTblBorders()!=null ) {
    		TblBorders tblBorders = s.getTblPr().getTblBorders();
    		if (tblBorders.getInsideH()!=null) {
    			if (tblBorders.getInsideH().getVal()==STBorder.NONE
    					|| tblBorders.getInsideH().getVal()==STBorder.NIL
    					|| tblBorders.getInsideH().getSz()==BigInteger.ZERO ) {
    				properties.add( new AdHocProperty("border-top-style", "none", null, null));
    				properties.add( new AdHocProperty("border-top-width", "0mm", null, null));
    				properties.add( new AdHocProperty("border-bottom-style", "none", null, null));
    				properties.add( new AdHocProperty("border-bottom-width", "0mm", null, null));
    			} else {
    				properties.add( new BorderTop(tblBorders.getTop() ));
    				properties.add( new BorderBottom(tblBorders.getBottom() ));
    			}
    		}
    		if (tblBorders.getInsideV()!=null) { 
    			if (tblBorders.getInsideV().getVal()==STBorder.NONE
    					|| tblBorders.getInsideV().getVal()==STBorder.NIL
    					|| tblBorders.getInsideV().getSz()==BigInteger.ZERO ) {
    				properties.add( new AdHocProperty("border-left-style", "none", null, null));
    				properties.add( new AdHocProperty("border-left-width", "0mm", null, null));
    				properties.add( new AdHocProperty("border-right-style", "none", null, null));
    				properties.add( new AdHocProperty("border-right-width", "0mm", null, null));
    			} else {
    				properties.add( new BorderRight(tblBorders.getRight() ));
    				properties.add( new BorderLeft(tblBorders.getLeft() ));
    			}
    		}
    	}
    	if (s.getTcPr()!=null ) {
    		PropertyFactory.createProperties(properties, s.getTcPr() );
    	}
		// Ensure empty cells have a sensible height
    	// TODO - this is no good with IE8, which doesn't treat this 
    	// as a minimum; it won't resize if there is more :-(
    	properties.add(new AdHocProperty("height", "5mm", null, null));
    	
    	
		for( Property p :  properties ) {
			if (p!=null) {
				result.append(p.getCssProperty());
			}
		}
		result.append("}\n");
		return result.toString();
    	
    }
    
    
    protected static void createCss(CTTblPrBase  tblPr, StringBuffer result) {
    	
		if (tblPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(tblPr);    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}    
    }
    protected static void createCss(List<CTTblStylePr> tblStylePrList, StringBuffer result) {
    	// STTblStyleOverrideType
    	
		if (tblStylePrList==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(tblStylePrList);    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}    
    }
    protected static void createCss(TrPr trPr, StringBuffer result) {
    	// includes jc, trHeight, wAfter, tblCellSpacing
    	
		if (trPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(trPr);    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}    
    }
    protected static void createCss(TcPr tcPr, StringBuffer result) {
    	// includes TcPrInner.TcBorders, CTShd, TcMar, CTVerticalJc
    	
		if (tcPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(tcPr);    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}    
    }
    
    
    public static void createCss(OpcPackage wmlPackage, PPr pPr, StringBuffer result, boolean ignoreBorders) {
    	
		if (pPr==null) {
			return;
		}
    	
    	List<Property> properties = PropertyFactory.createProperties(wmlPackage, pPr);    	
    	for( Property p :  properties ) {
    		
			if (ignoreBorders &&
					((p instanceof PBorderTop)
							|| (p instanceof PBorderBottom))) {
				continue;
			}
			
			if (p instanceof PShading) {
    	    	// To close the gap between divs, we need to avoid
    	    	// CSS margin collapse.    	    	
    	    	// To do that, we add a border the same color as 
    	    	// the background color				
				String fill = ((CTShd)p.getObject()).getFill();				
				result.append("border-color: #" + fill + "; border-style:solid; border-width:1px;");
			}
    		
    		result.append(p.getCssProperty());
    	}    
    }
    
    
    public static void createCss(OpcPackage wmlPackage, RPr rPr, StringBuffer result) {

    	List<Property> properties = PropertyFactory.createProperties(wmlPackage, rPr);
    	
    	for( Property p :  properties ) {
    		result.append(p.getCssProperty());
    	}
    }
    
 

}