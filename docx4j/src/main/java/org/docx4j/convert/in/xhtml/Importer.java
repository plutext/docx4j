package org.docx4j.convert.in.xhtml;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.AbstractParagraphProperty;
import org.docx4j.model.properties.run.AbstractRunProperty;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.org.xhtmlrenderer.css.constants.CSSName;
import org.docx4j.org.xhtmlrenderer.css.constants.IdentValue;
import org.docx4j.org.xhtmlrenderer.css.style.CalculatedStyle;
import org.docx4j.org.xhtmlrenderer.css.style.DerivedValue;
import org.docx4j.org.xhtmlrenderer.css.style.FSDerivedValue;
import org.docx4j.org.xhtmlrenderer.docx.Docx4JFSImage;
import org.docx4j.org.xhtmlrenderer.docx.Docx4jUserAgent;
import org.docx4j.org.xhtmlrenderer.docx.DocxRenderer;
import org.docx4j.org.xhtmlrenderer.layout.Styleable;
import org.docx4j.org.xhtmlrenderer.newtable.TableCellBox;
import org.docx4j.org.xhtmlrenderer.newtable.TableSectionBox;
import org.docx4j.org.xhtmlrenderer.render.AnonymousBlockBox;
import org.docx4j.org.xhtmlrenderer.render.BlockBox;
import org.docx4j.org.xhtmlrenderer.render.Box;
import org.docx4j.org.xhtmlrenderer.render.InlineBox;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.P;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.PPrBase.NumPr.NumId;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner.GridSpan;
import org.docx4j.wml.TcPrInner.VMerge;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSValue;

/**
 * Convert XHTML + CSS to WordML content.
 * 
 * People complain flying-saucer is slow
 * (due to DTD related network lookups).
 * See http://stackoverflow.com/questions/5431646/is-there-any-way-improve-the-performance-of-flyingsaucer
 * 
 * Looking at FSEntityResolver, the problem is that there
 * is no resources/schema on dir anymore which can be put on
 * the classpath.  Once this problem is fixed, things work better.
 * 
 * TODO:
 * - a DOCTYPE declaration currently causes an NPE at org.docx4j.org.xhtmlrenderer.resource.FSEntityResolver.resolveEntity(FSEntityResolver.java:106)  
 * - fonts
 * - underline, insert, delete
 * - space-before, space-after unrecognized CSS property
 * - proper logging in XHTML renderer project
 * 
 * @author jharrop
 *
 */
public class Importer {
	
	protected static Logger log = Logger.getLogger(Importer.class);		
	    
	/**
	 * Configure, how the Importer styles hyperlinks
	 * 
	 * If hyperlinkStyleId is set to <code>null</code>, hyperlinks are
	 * styled using just the CSS. This is the default behavior.
	 * 
	 * If hyperlinkStyleId is set to <code>"someWordHyperlinkStyleName"</code>, 
	 * strings containing 'http://' or 'https://' or or 'mailto:' are converted to w:hyperlink.  
	 * The default Word hyperlink style name is "Hyperlink".
	 * 
	 * Due to the architecture of this class, this is a static flag changing the
	 * behavior of all following calls.
	 * 
	 * @param hyperlinkStyleID
	 *            The style to use for hyperlinks (eg Hyperlink)
	 */
	public static void setHyperlinkStyle (
			String hyperlinkStyleID) {
		hyperlinkStyleId = hyperlinkStyleID;
	}
	private static String hyperlinkStyleId = null;	
    private List<Object> imports = new ArrayList<Object>(); 
//    public List<Object>  getImportedContent() {
//    	return imports;
//    }
    
    private P currentP;
    
    private WordprocessingMLPackage wordMLPackage;
    private RelationshipsPart rp;
    private NumberingDefinitionsPart ndp;
    
    private ListHelper listHelper;
    
    private DocxRenderer renderer;
    
    private Importer(WordprocessingMLPackage wordMLPackage) {
    	this.wordMLPackage= wordMLPackage;
    	rp = wordMLPackage.getMainDocumentPart().getRelationshipsPart();
    	ndp = wordMLPackage.getMainDocumentPart().getNumberingDefinitionsPart();
    	
    	listHelper = new ListHelper();
    	
		if (hyperlinkStyleId !=null && wordMLPackage instanceof WordprocessingMLPackage) {
			((WordprocessingMLPackage)wordMLPackage).getMainDocumentPart().getPropertyResolver().activateStyle(hyperlinkStyleId);
		}
    }

    /**
     * Convert the well formed XHTML (without DTD) contained in file to a list of WML objects.
     * 
     * @param file
     * @param wordMLPackage
     * @return
     * @throws IOException
     */
    public static List<Object> convert(File file, WordprocessingMLPackage wordMLPackage) throws IOException {

        Importer importer = new Importer(wordMLPackage);

        importer.renderer = new DocxRenderer();
        importer.renderer.setDocument(file);
        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), "", importer.imports);
        
        return importer.imports;    	
    }
    
    /**
     * Convert the well formed XHTML (without DTD) found at the specified URI to a list of WML objects.
     * 
     * @param uri
     * @param wordMLPackage
     * @return
     */
    public static List<Object> convert(String uri, WordprocessingMLPackage wordMLPackage) {

        Importer importer = new Importer(wordMLPackage);
    	
        importer.renderer = new DocxRenderer();
        importer.renderer.setDocument(uri);
        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), "", importer.imports);
        
        return importer.imports;    	
    }

    /**
     * 
     * Convert the well formed XHTML (without DTD) contained in the string to a list of WML objects.
     * 
     * @param content
     * @param wordMLPackage
     * @param baseUrl
     * @return
     */
    public static List<Object> convertFromString(String content, WordprocessingMLPackage wordMLPackage, String baseUrl) {
    	
        Importer importer = new Importer(wordMLPackage);

        importer.renderer = new DocxRenderer();
        importer.renderer.setDocumentFromString(content, baseUrl);
        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), "", importer.imports);
        
        return importer.imports;    	
    }
    
    
    private Map<String, CSSValue> getCascadedProperties(CalculatedStyle cs) {
    	
    	Map<String, CSSValue> cssMap = new HashMap<String, CSSValue>();
    	
    	FSDerivedValue[] derivedValues = cs.getDerivedValues();
        for (int i = 0; i < derivedValues.length; i++) {
        	        	
            CSSName name = CSSName.getByID(i);
            
            if (name.toString().startsWith("-fs")) continue;
                        
            FSDerivedValue val = cs.valueByName(name); // walks parents as necessary to get the value
            
            if (val != null && val instanceof DerivedValue) {    
            	
            	cssMap.put(name.toString(), ((DerivedValue)val).getCSSPrimitiveValue() );
            	
            } else if (val != null && val instanceof IdentValue) {
            	
            	cssMap.put(name.toString(), ((IdentValue)val).getCSSPrimitiveValue() );

            } else  if (val!=null ) {
            	
            	log.info("Skipping " +  name.toString() + " .. " + val.getClass().getName() );
            } else {
            	log.info("Skipping " +  name.toString() + " .. (null value)" );            	
            }
        }
    	
        return cssMap;
    	
    }

    // A paragraph created for a div can be replaced by
    // one created for a p within it, if it is still empty
    boolean paraStillEmpty;
    
    private void traverse(Box box, String indents, List<Object> contentContext) {
        
        log.info(box.getClass().getName() );
        if (box instanceof BlockBox) {
            BlockBox blockBox = ((BlockBox)box);

            Element e = box.getElement(); 

            // Don't add a new paragraph if this BlockBox is display: inline
            if (e==null) {
            	// Shouldn't happen
                log.info("<NULL>");
            } else {            
                log.info("BB" + indents + "<" + e.getNodeName() + " " + box.getStyle().toStringMine() );
                
                
            	//Map cssMap = styleReference.getCascadedPropertiesMap(e);
                Map<String, CSSValue> cssMap = getCascadedProperties(box.getStyle());
            	
            	/* Sometimes, when it is display: inline, the following is not set:
	            	CSSValue cssValue = (CSSValue)cssMap.get("display");
	            	if (cssValue !=null) {
	            		log.info(cssValue.getCssText() );
	            	}
	            */
            	// So do it this way ...
            	if (box.getStyle().getDisplayMine().equals("inline") ) {
                	// Don't add a paragraph for this, unless ..
                	if (currentP==null) {
                		currentP = Context.getWmlObjectFactory().createP();
    		            contentContext.add(currentP);
    		            paraStillEmpty = true;
                	}            		
                	
            	} else if (e.getNodeName().equals("table")) {
            		
            		// org.docx4j.org.xhtmlrenderer.newtable.TableBox
            		
            		// eg <table color: #000000; background-color: transparent; background-image: none; background-repeat: repeat; background-attachment: scroll; background-position: [0%, 0%]; background-size: [auto, auto]; 
            		//           border-collapse: collapse; -fs-border-spacing-horizontal: 2px; -fs-border-spacing-vertical: 2px; -fs-font-metric-src: none; -fs-keep-with-inline: auto; -fs-page-width: auto; -fs-page-height: auto; -fs-page-sequence: auto; -fs-pdf-font-embed: auto; -fs-pdf-font-encoding: Cp1252; -fs-page-orientation: auto; -fs-table-paginate: auto; -fs-text-decoration-extent: line; bottom: auto; caption-side: top; clear: none; ; content: normal; counter-increment: none; counter-reset: none; cursor: auto; ; display: table; empty-cells: show; float: none; font-style: normal; font-variant: normal; font-weight: normal; font-size: medium; line-height: normal; font-family: serif; -fs-table-cell-colspan: 1; -fs-table-cell-rowspan: 1; height: auto; left: auto; letter-spacing: normal; list-style-type: disc; list-style-position: outside; list-style-image: none; max-height: none; max-width: none; min-height: 0; min-width: 0; orphans: 2; ; ; ; overflow: visible; page: auto; page-break-after: auto; page-break-before: auto; page-break-inside: auto; position: relative; ; right: auto; src: none; 
            		//           table-layout: fixed; text-align: left; text-decoration: none; text-indent: 0; text-transform: none; top: auto; ; vertical-align: baseline; visibility: visible; white-space: normal; word-wrap: normal; widows: 2; width: auto; word-spacing: normal; z-index: auto; border-top-color: #000000; border-right-color: #000000; border-bottom-color: #000000; border-left-color: #000000; border-top-style: solid; border-right-style: solid; border-bottom-style: solid; border-left-style: solid; border-top-width: 1px; border-right-width: 1px; border-bottom-width: 1px; border-left-width: 1px; margin-top: 0; margin-right: 0; margin-bottom: 0; margin-left: 0in; padding-top: 0; padding-right: 0; padding-bottom: 0; padding-left: 0;
            		
            		// TODO support: border-collapse: collapse; table-layout: fixed;
                
            		log.info(".. processing table");  // what happened to <colgroup><col style="width: 2.47in;" /><col style="width: 2.47in;" /> 
            		
            		Tbl tbl = Context.getWmlObjectFactory().createTbl();
            		contentContext.add(tbl);
		            paraStillEmpty = true;
		            contentContext = tbl.getContent();
            		
            	} else if (box instanceof org.docx4j.org.xhtmlrenderer.newtable.TableSectionBox) {
            		
            		// eg <tbody color: #000000; background-color: transparent; background-image: none; background-repeat: repeat; background-attachment: scroll; background-position: [0%, 0%]; background-size: [auto, auto]; border-collapse: collapse; -fs-border-spacing-horizontal: 0; -fs-border-spacing-vertical: 0; -fs-font-metric-src: none; -fs-keep-with-inline: auto; -fs-page-width: auto; -fs-page-height: auto; -fs-page-sequence: auto; -fs-pdf-font-embed: auto; -fs-pdf-font-encoding: Cp1252; -fs-page-orientation: auto; -fs-table-paginate: auto; -fs-text-decoration-extent: line; bottom: auto; caption-side: top; clear: none; ; content: normal; counter-increment: none; counter-reset: none; cursor: auto; ; display: table-row-group; empty-cells: show; float: none; font-style: normal; font-variant: normal; font-weight: normal; font-size: medium; line-height: normal; font-family: serif; -fs-table-cell-colspan: 1; -fs-table-cell-rowspan: 1; height: auto; left: auto; letter-spacing: normal; list-style-type: disc; list-style-position: outside; list-style-image: none; max-height: none; max-width: none; min-height: 0; min-width: 0; orphans: 2; ; ; ; overflow: visible; page: auto; page-break-after: auto; page-break-before: auto; page-break-inside: auto; position: static; ; right: auto; src: none; table-layout: auto; text-align: left; text-decoration: none; text-indent: 0; text-transform: none; top: auto; ; vertical-align: middle; visibility: visible; white-space: normal; word-wrap: normal; widows: 2; width: auto; word-spacing: normal; z-index: auto; border-top-color: #000000; border-right-color: #000000; border-bottom-color: #000000; border-left-color: #000000; border-top-style: none; border-right-style: none; border-bottom-style: none; border-left-style: none; border-top-width: 2px; border-right-width: 2px; border-bottom-width: 2px; border-left-width: 2px; margin-top: 0; margin-right: 0; margin-bottom: 0; margin-left: 0; padding-top: 0; padding-right: 0; padding-bottom: 0; padding-left: 0; 
            		log.info(".. processing <tbody");
            		
            		// Do nothing here for now .. the switch statement below traverses children
            		
            		// TODO: give effect to this CSS
            		
            	} else if (box instanceof org.docx4j.org.xhtmlrenderer.newtable.TableRowBox) {
            		
            		// eg <tr color: #000000; background-color: transparent; background-image: none; background-repeat: repeat; background-attachment: scroll; background-position: [0%, 0%]; background-size: [auto, auto]; border-collapse: collapse; -fs-border-spacing-horizontal: 0; -fs-border-spacing-vertical: 0; -fs-font-metric-src: none; -fs-keep-with-inline: auto; -fs-page-width: auto; -fs-page-height: auto; -fs-page-sequence: auto; -fs-pdf-font-embed: auto; -fs-pdf-font-encoding: Cp1252; -fs-page-orientation: auto; -fs-table-paginate: auto; -fs-text-decoration-extent: line; bottom: auto; caption-side: top; clear: none; ; content: normal; counter-increment: none; counter-reset: none; cursor: auto; ; display: table-row; empty-cells: show; float: none; font-style: normal; font-variant: normal; font-weight: normal; font-size: medium; line-height: normal; font-family: serif; -fs-table-cell-colspan: 1; -fs-table-cell-rowspan: 1; height: auto; left: auto; letter-spacing: normal; list-style-type: disc; list-style-position: outside; list-style-image: none; max-height: none; max-width: none; min-height: 0; min-width: 0; orphans: 2; ; ; ; overflow: visible; page: auto; page-break-after: auto; page-break-before: auto; page-break-inside: auto; position: static; ; right: auto; src: none; table-layout: auto; text-align: left; text-decoration: none; text-indent: 0; text-transform: none; top: auto; ; vertical-align: top; visibility: visible; white-space: normal; word-wrap: normal; widows: 2; width: auto; word-spacing: normal; z-index: auto; border-top-color: #000000; border-right-color: #000000; border-bottom-color: #000000; border-left-color: #000000; border-top-style: none; border-right-style: none; border-bottom-style: none; border-left-style: none; border-top-width: 2px; border-right-width: 2px; border-bottom-width: 2px; border-left-width: 2px; margin-top: 0; margin-right: 0; margin-bottom: 0; margin-left: 0; padding-top: 0; padding-right: 0; padding-bottom: 0; padding-left: 0;
            		
            		// TODO support vertical-align
            		
            		log.info(".. processing <tr");            		

            		Tr tr = Context.getWmlObjectFactory().createTr();
            		contentContext.add(tr);
		            paraStillEmpty = true;
		            contentContext = tr.getContent();
            		
            	} else if (box instanceof org.docx4j.org.xhtmlrenderer.newtable.TableCellBox) {
            		
            		log.info(".. processing <td");            		
            		// eg <td color: #000000; background-color: transparent; background-image: none; background-repeat: repeat; background-attachment: scroll; background-position: [0%, 0%]; background-size: [auto, auto]; border-collapse: collapse; -fs-border-spacing-horizontal: 0; -fs-border-spacing-vertical: 0; -fs-font-metric-src: none; -fs-keep-with-inline: auto; -fs-page-width: auto; -fs-page-height: auto; -fs-page-sequence: auto; -fs-pdf-font-embed: auto; -fs-pdf-font-encoding: Cp1252; -fs-page-orientation: auto; -fs-table-paginate: auto; -fs-text-decoration-extent: line; bottom: auto; caption-side: top; clear: none; ; content: normal; counter-increment: none; counter-reset: none; cursor: auto; ; display: table-row; empty-cells: show; float: none; font-style: normal; font-variant: normal; font-weight: normal; font-size: medium; line-height: normal; font-family: serif; -fs-table-cell-colspan: 1; -fs-table-cell-rowspan: 1; height: auto; left: auto; letter-spacing: normal; list-style-type: disc; list-style-position: outside; list-style-image: none; max-height: none; max-width: none; min-height: 0; min-width: 0; orphans: 2; ; ; ; overflow: visible; page: auto; page-break-after: auto; page-break-before: auto; page-break-inside: auto; position: static; ; right: auto; src: none; table-layout: auto; text-align: left; text-decoration: none; text-indent: 0; text-transform: none; top: auto; ; vertical-align: top; visibility: visible; white-space: normal; word-wrap: normal; widows: 2; width: auto; word-spacing: normal; z-index: auto; border-top-color: #000000; border-right-color: #000000; border-bottom-color: #000000; border-left-color: #000000; border-top-style: none; border-right-style: none; border-bottom-style: none; border-left-style: none; border-top-width: 2px; border-right-width: 2px; border-bottom-width: 2px; border-left-width: 2px; margin-top: 0; margin-right: 0; margin-bottom: 0; margin-left: 0; padding-top: 0; padding-right: 0; padding-bottom: 0; padding-left: 0;
            		
            		org.docx4j.org.xhtmlrenderer.newtable.TableCellBox tcb = (org.docx4j.org.xhtmlrenderer.newtable.TableCellBox)box;
            		// tcb.getVerticalAlign()
            		
            		// TODO support rowspan
            		// vertically merged cells are
            		// represented as a top cell containing the actual content and a series
            		// of dummy cells having a vMerge tag with "continue" attribute.            		
            		
            		// FIXME: following doesn't work, because rol & col are only set on row 0,
            		// and then return 0 for all rows & cols ?!
            		// At least for table/tbody/tr/td
            		// because recalcCells is only done on first row!
            		
//            		// if cell to the left in source is part of a rowspan, 
//            		// insert dummy cell first            			
//            		TableSectionBox section = tcb.getSection();
//                    //int effCol = tcb.getTable().colToEffCol(tcb.getCol());
//                    //if (effCol != 0) {	                    
//	                    TableCellBox prevCell = section.cellAt(tcb.getRow(), tcb.getCol() - 1);
//	                    log.info("Got prevCell for " + tcb.getRow() + ", " + tcb.getCol() );
//	                    if (prevCell == TableCellBox.SPANNING_CELL) {
//	                		Tc dummy = Context.getWmlObjectFactory().createTc();
//	                		contentContext.add(dummy);
//
//	                		TcPr tcPr = Context.getWmlObjectFactory().createTcPr();
//	            			dummy.setTcPr(tcPr);
//	            			
//	            			VMerge vm = Context.getWmlObjectFactory().createTcPrInnerVMerge();
//	            			vm.setVal("continue");
//	            			tcPr.setVMerge(vm);
//	                    }
//                    //}
            		
            		Tc tc = Context.getWmlObjectFactory().createTc();
            		contentContext.add(tc);
            		
            		// if this is the last real cell in the source,
            		// is there a rowspan above and to the right?
            		

            		// colspan support: horizontally merged cells are represented by one cell
            		// with a gridSpan attribute; 
            		int colspan = tcb.getStyle().getColSpan(); 
            		if (colspan>1) {
            			TcPr tcPr = Context.getWmlObjectFactory().createTcPr();
            			tc.setTcPr(tcPr);

            			GridSpan gs = Context.getWmlObjectFactory().createTcPrInnerGridSpan();
            			gs.setVal( BigInteger.valueOf(colspan));
            			tcPr.setGridSpan(gs);
            		}
		            paraStillEmpty = true;
		            contentContext = tc.getContent();
            		
	            } else {
	            	
	            	// Avoid creating paragraphs for html, body
	            	if (contentContext.size()>0 && paraStillEmpty) {
			            contentContext.remove( contentContext.size()-1);                                        		
	            	} 
	            	
		            currentP = Context.getWmlObjectFactory().createP();
		            contentContext.add(currentP);
		            paraStillEmpty = true;
		            
		            // Paragraph level styling
		            currentP.setPPr(
		            		addParagraphProperties( cssMap ));
		            
		            if (e.getNodeName().equals("li")) {
		            	addNumbering(e, cssMap);
		            } else if  (e.getNodeName().equals("img")) {
		        		// TODO, should we be using ReplacedElementFactory approach instead?		            	
		            	addImage(e);		            	
		            }
		            
	            }
        	}
            
            // the recursive bit:
            
		            switch (blockBox.getChildrenContentType()) {
		                case BlockBox.CONTENT_BLOCK:
		                    for (Object o : ((BlockBox)box).getChildren() ) {
		                        traverse((Box)o, indents + "    ", contentContext);                    
		                    }
		                    break;
		                case BlockBox.CONTENT_INLINE:
		                    if ( ((BlockBox)box).getInlineContent()!=null) {
		                    	
		                        for (Object o : ((BlockBox)box).getInlineContent() ) {
		//                            log.info("        " + o.getClass().getName() ); 
		                            if (o instanceof InlineBox ) {
		//                                    && ((InlineBox)o).getElement()!=null // skip these (pseudo-elements?)
		//                                    && ((InlineBox)o).isStartsHere()) {
		                                
		                            	processInlineBox( (InlineBox)o, indents, contentContext);
		                            	
		                            } else if (o instanceof BlockBox ) {
		                                traverse((Box)o, indents + "    ", contentContext); // commenting out gets rid of unwanted extra parent elements
		                            } else {
		                                log.info("What to do with " + box.getClass().getName() );                        
		                            }
		                        }
		                    }
		                    break;
		            } 
            
		    
            log.info("Done processing children of " + box.getClass().getName() );
            // contentContext gets its old value back each time recursion finishes,
            // ensuring elements are added at the appropriate level (eg inside tr) 
            
        } else if (box instanceof AnonymousBlockBox) {
            log.info("AnonymousBlockBox");            
        }
    
    }

	private void addImage(Element e) {
		System.out.println("Detected an image!!! " + e.getAttribute("src"));
		
		Docx4jUserAgent docx4jUserAgent = renderer.getDocx4jUserAgent();
		Docx4JFSImage docx4JFSImage = docx4jUserAgent.getDocx4JImageResource( e.getAttribute("src") );
				
		BinaryPartAbstractImage imagePart;
		Inline inline = null;
		try {
			
			imagePart = BinaryPartAbstractImage.createImagePart(
					wordMLPackage, 
					docx4JFSImage.getBytes());
		    inline = imagePart.createImageInline( null, null, 0, 1, false);
		    
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		        		
		
		// Now add the inline in w:p/w:r/w:drawing
		org.docx4j.wml.R  run = Context.getWmlObjectFactory().createR();		
		currentP.getContent().add(run);        
		org.docx4j.wml.Drawing drawing = Context.getWmlObjectFactory().createDrawing();		
		run.getContent().add(drawing);		
		drawing.getAnchorOrInline().add(inline);
		
	    paraStillEmpty = false;
		
	}

	private void addNumbering(Element e, Map<String, CSSValue> cssMap) {
		Numbering.Num num = null;
		try {
			if ( cssMap.get("list-style-type" ).getCssText().equals("decimal")) {
				num = listHelper.getOrderedList(ndp);
			}
			if (cssMap.get("list-style-type" ).getCssText().equals("disc")) {
				num = listHelper.getUnorderedList(ndp);
			}
			
			// TODO: support other list-style-type
			
			// TODO: generate list definitions based on CSS 
			// (and multiple list definitions)
			
		} catch (JAXBException je) {
			// Shouldn't happen
			je.printStackTrace();
			log.error(e);
		}

		if (num==null) {
			log.warn( "No support for list-style-type: " 
					+ cssMap.get("list-style-type" ).getCssText()   );  // eg decimal, disc
			
		} else {

		    paraStillEmpty = false;
		    
		    // Create and add <w:numPr>
		    NumPr numPr =  Context.getWmlObjectFactory().createPPrBaseNumPr();
		    currentP.getPPr().setNumPr(numPr);

		    // The <w:numId> element
		    NumId numIdElement = Context.getWmlObjectFactory().createPPrBaseNumPrNumId();
		    numPr.setNumId(numIdElement);
		    numIdElement.setVal( num.getNumId() ); // point to the correct list
		    
		    // The <w:ilvl> element
		    Ilvl ilvlElement = Context.getWmlObjectFactory().createPPrBaseNumPrIlvl();
		    numPr.setIlvl(ilvlElement);
		    ilvlElement.setVal(BigInteger.valueOf(0));
		    
		    // TMP: don't let this override our numbering
		    currentP.getPPr().setInd(null);
		}
	}

    private void processInlineBox( InlineBox inlineBox, String indents, List<Object> contentContext) {

        // Doesn't extend box
        Styleable s = ((InlineBox)inlineBox );
        if (s.getStyle()==null) { // Assume this won't happen
        	log.error("getStyle returned null!");
        }
        Map<String, CSSValue> cssMap = getCascadedProperties(s.getStyle());
//        Map cssMap = styleReference.getCascadedPropertiesMap(s.getElement());
                
        boolean isHyperlink = false;
        
        String debug = "<UNKNOWN Styleable";
        if (s.getElement()!=null) {
            debug = indents + "    " + "<" + s.getElement().getNodeName();
            
            if (s.getElement().getNodeName().equals("a")) {
            	log.info("Ha!  found a hyperlink. ");
            	isHyperlink = true;
            } else if (s.getElement().getNodeName().equals("p")) {
            	// This seems to be the usual case. Odd?
            	log.debug("p in inline");
        		currentP = Context.getWmlObjectFactory().createP();                                        	
            	if (paraStillEmpty) {
            		// Replace it
		            contentContext.remove( contentContext.size()-1);                                        		
            	} 
	            contentContext.add(currentP);
	            paraStillEmpty = true;
	            currentP.setPPr(
	            		addParagraphProperties( cssMap ));
            }	            
        }
        if (s.getStyle()!=null) {
            debug +=  " " + s.getStyle().toStringMine();
        }
        
        
        log.info(debug );
        //log.info("'" + ((InlineBox)o).getTextNode().getTextContent() );  // don't use .getText()
        
        
        if (inlineBox.getTextNode()==null) {
            if (isHyperlink) { // eg <a href="http://slashdot.org/" /> ie empty
            	
            	Hyperlink h = createHyperlink(
            			s.getElement().getAttribute("href"), 
            			addRunProperties( cssMap ),
            			s.getElement().getAttribute("href"), rp);                                    	
                currentP.getContent().add(h);
                
            } else if (s.getElement().getNodeName().equals("br") ) {
                
                R run = Context.getWmlObjectFactory().createR();
                currentP.getContent().add(run);                
           		run.getContent().add(Context.getWmlObjectFactory().createBr());
                
            } else {
            	log.info("InlineBox has no TextNode, so skipping" );
            }
        } else  {
            log.info( inlineBox.getTextNode().getTextContent() );  // don't use .getText()

            String theText = inlineBox.getTextNode().getTextContent(); 
            log.info("Processing " + theText);
            
            paraStillEmpty = false;                                    
            
            if (isHyperlink) {
            	
            	Hyperlink h = createHyperlink(
            			s.getElement().getAttribute("href"),
            			addRunProperties( cssMap ),
            			theText, rp);                                    	
                currentP.getContent().add(h);
            	
            } else { // usual case
            
                R run = Context.getWmlObjectFactory().createR();
                Text text = Context.getWmlObjectFactory().createText();
                text.setValue( theText );
                if (theText.startsWith(" ")
                		|| theText.endsWith(" ") ) {
                	text.setSpace("preserve");
                }
                run.getContent().add(text);
                
                currentP.getContent().add(run);
                
                // Run level styling
	            run.setRPr(
	            		addRunProperties( cssMap ));
    	            
//                                    else {
//                                    	// Get it from the parent element eg p
//                        	            //Map cssMap = styleReference.getCascadedPropertiesMap(e);
//                        	            run.setRPr(
//                        	            		addRunProperties( cssMap ));                                    	                                    	
//                                    }
            }
        }
    }
    
    private PPr addParagraphProperties(Map cssMap) {

        PPr pPr =  Context.getWmlObjectFactory().createPPr();
        
        for (Object o : cssMap.keySet()) {
        	
        	String cssName = (String)o;
        	CSSValue cssValue = (CSSValue)cssMap.get(cssName);
        	
        	Property p = PropertyFactory.createPropertyFromCssName(cssName, cssValue);
        	
        	if (p!=null) {
	        	if (p instanceof AbstractParagraphProperty) {        		
	        		((AbstractParagraphProperty)p).set(pPr);
	        	} else {
	            	//log.info(p.getClass().getName() );
	        	}
        	}
        	
        }
                
//        for (int i = 0; i < cStyle.getDerivedValues().length; i++) {
//            CSSName name = CSSName.getByID(i);
//            FSDerivedValue val = cStyle.getDerivedValues()[i];
//            Property p = PropertyFactory.createPropertyFromCssName(name, value)
//        }
        
    	
        return pPr;
    }

    RPr addRunProperties(Map cssMap) {

        RPr rPr =  Context.getWmlObjectFactory().createRPr();
        
        for (Object o : cssMap.keySet()) {
        	
        	String cssName = (String)o;
        	CSSValue cssValue = (CSSValue)cssMap.get(cssName);
        	
        	Property p = PropertyFactory.createPropertyFromCssName(cssName, cssValue);
        	
        	if (p!=null) {
	        	if (p instanceof AbstractRunProperty) {        		
	        		((AbstractRunProperty)p).set(rPr);
	        	} else {
	            	//log.info(p.getClass().getName() );
	        	}
        	}
        }
        return rPr;
    }

	private Hyperlink createHyperlink(String url, RPr rPr, String linkText, RelationshipsPart rp) {
		
		try {

			// We need to add a relationship to word/_rels/document.xml.rels
			// but since its external, we don't use the 
			// usual wordMLPackage.getMainDocumentPart().addTargetPart
			// mechanism
			org.docx4j.relationships.ObjectFactory factory =
				new org.docx4j.relationships.ObjectFactory();
			
			org.docx4j.relationships.Relationship rel = factory.createRelationship();
			rel.setType( Namespaces.HYPERLINK  );
			rel.setTarget(url);
			rel.setTargetMode("External");  
									
			rp.addRelationship(rel);
			
			// addRelationship sets the rel's @Id
			
			String hpl = "<w:hyperlink r:id=\"" + rel.getId() + "\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" " +
            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" >" +
            "<w:r>" +
            "<w:t>" + linkText + "</w:t>" +
            "</w:r>" +
            "</w:hyperlink>";

			Hyperlink hyperlink = (Hyperlink)XmlUtils.unmarshalString(hpl);
			R r = (R)hyperlink.getContent().get(0);
			r.setRPr(rPr);
			if (hyperlinkStyleId!=null) {
				RStyle rStyle = Context.getWmlObjectFactory().createRStyle();
				rStyle.setVal(hyperlinkStyleId);
				rPr.setRStyle(rStyle );
			}
			return hyperlink;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}
    
    
    public static void main(String[] args) throws Exception {
        
//      File f = new File(System.getProperty("user.dir") + "/demos/browser/xhtml/hamlet-shortest.xhtml");
//      File f = new File(System.getProperty("user.dir") + "/input.html");
//        File f = new File(System.getProperty("user.dir") + "/src/test/resources/xhtml/inheritance.html");
//        File f = new File(System.getProperty("user.dir") + "/src/test/resources/xhtml/img.xhtml");
        File f = new File(System.getProperty("user.dir") + "/sample-docs/word/table-simple.html");

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
		ndp.unmarshalDefaultNumbering();		
		
		wordMLPackage.getMainDocumentPart().getContent().addAll( 
				convert(f, wordMLPackage) );
		
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true));
		
		wordMLPackage.save(new java.io.File(System.getProperty("user.dir") + "/html_output.docx") );
      
  }

    
}
