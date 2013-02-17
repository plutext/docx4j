/*
 *  Copyright 2011-2012, Plutext Pty Ltd.
 *   
 *  This file is part of docx4j.

    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 

    You may obtain a copy of the License at 

        http://www.apache.org/licenses/LICENSE-2.0 

    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.

 */
package org.docx4j.convert.in.xhtml;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.properties.Property;
import org.docx4j.model.properties.PropertyFactory;
import org.docx4j.model.properties.paragraph.AbstractParagraphProperty;
import org.docx4j.model.properties.run.AbstractRunProperty;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
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
import org.docx4j.org.xhtmlrenderer.newtable.TableBox;
import org.docx4j.org.xhtmlrenderer.newtable.TableCellBox;
import org.docx4j.org.xhtmlrenderer.newtable.TableSectionBox;
import org.docx4j.org.xhtmlrenderer.render.AnonymousBlockBox;
import org.docx4j.org.xhtmlrenderer.render.BlockBox;
import org.docx4j.org.xhtmlrenderer.render.Box;
import org.docx4j.org.xhtmlrenderer.render.InlineBox;
import org.docx4j.org.xhtmlrenderer.resource.XMLResource;
import org.docx4j.wml.CTTblLayoutType;
import org.docx4j.wml.CTTblPrBase.TblStyle;
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
import org.docx4j.wml.STTblLayoutType;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner.GridSpan;
import org.docx4j.wml.TcPrInner.VMerge;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSValue;
import org.xml.sax.InputSource;

/**
 * Convert XHTML + CSS to WordML content.  Can convert an entire document, 
 * or a fragment consisting of one or more block level objects.
 * 
 * Your XHTML must be well formed XML!  
 * 
 * For usage examples, please see org.docx4j.samples/XHTMLImportFragment, 
 * and XHTMLImportDocument 
 * 
 * For best results, be sure to include src/main/resources on your classpath. 
 * 
 * Includes rudimentary support for:
 * - paragraph and run formatting
 * - tables
 * - images
 * - lists (ordered, unordered)
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
 * - fonts
 * - underline, insert, delete
 * - space-before, space-after unrecognized CSS property
 * - no attempt is made to convert CSS classes to Word styles
 * 
 * @author jharrop
 * @since 2.8
 *
 */
public class XHTMLImporter {
	
	public static Logger log = Logger.getLogger(XHTMLImporter.class);		
	    
	/**
	 * Configure, how the Importer styles hyperlinks
	 * 
	 * If hyperlinkStyleId is set to <code>null</code>, hyperlinks are
	 * styled using just the CSS. This is the default behavior.
	 * 
	 * If hyperlinkStyleId is set to <code>"someWordHyperlinkStyleName"</code>, 
	 * that style is used. The default Word hyperlink style name is "Hyperlink".
	 * It is currently your responsibility to define that style in your
	 * styles definition part.
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
    
    private XHTMLImporter(WordprocessingMLPackage wordMLPackage) {
    	this.wordMLPackage= wordMLPackage;
    	rp = wordMLPackage.getMainDocumentPart().getRelationshipsPart();
    	ndp = wordMLPackage.getMainDocumentPart().getNumberingDefinitionsPart();
    	
    	listHelper = new ListHelper();
    	
		if (hyperlinkStyleId !=null && wordMLPackage instanceof WordprocessingMLPackage) {
			((WordprocessingMLPackage)wordMLPackage).getMainDocumentPart().getPropertyResolver().activateStyle(hyperlinkStyleId);
		}
    }

    /**
     * Convert the well formed XHTML contained in file to a list of WML objects.
     * 
     * @param file
     * @param baseUrl
     * @param wordMLPackage
     * @return
     * @throws IOException
     */
    public static List<Object> convert(File file, String baseUrl, WordprocessingMLPackage wordMLPackage) throws Docx4JException {

        XHTMLImporter importer = new XHTMLImporter(wordMLPackage);

        importer.renderer = new DocxRenderer();
        
        File parent = file.getAbsoluteFile().getParentFile();
        
        try {
			importer.renderer.setDocument(
					importer.renderer.loadDocument(file.toURI().toURL().toExternalForm()),
			        (parent == null ? "" : parent.toURI().toURL().toExternalForm())
			);
		} catch (MalformedURLException e) {
			throw new Docx4JException("Malformed URL", e);
		}

        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), importer.imports, null);
        
        return importer.imports;    	
    }

    /**
     * Convert the well formed XHTML from the specified SAX InputSource
     * 
     * @param is
     * @param baseUrl
     * @param wordMLPackage
     * @return
     * @throws IOException
     */
    public static List<Object> convert(InputSource is,  String baseUrl, WordprocessingMLPackage wordMLPackage) throws Docx4JException {

        XHTMLImporter importer = new XHTMLImporter(wordMLPackage);

        importer.renderer = new DocxRenderer();
        
        Document dom = XMLResource.load(is).getDocument();        
        importer.renderer.setDocument(dom, baseUrl);
        
        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), importer.imports, null);
        
        return importer.imports;    	
    }

    /**
     * @param is
     * @param baseUrl
     * @param wordMLPackage
     * @return
     * @throws IOException
     */
    public static List<Object> convert(InputStream is, String baseUrl, WordprocessingMLPackage wordMLPackage) throws Docx4JException {
        XHTMLImporter importer = new XHTMLImporter(wordMLPackage);
    	
        importer.renderer = new DocxRenderer();
        
        Document dom = XMLResource.load(is).getDocument();        
        importer.renderer.setDocument(dom, baseUrl);

        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), importer.imports, null);
        
        return importer.imports;    	    	
    }
    
    /**
     * @param node
     * @param baseUrl
     * @param wordMLPackage
     * @return
     * @throws IOException
     */
    public static List<Object> convert(Node node,  String baseUrl, WordprocessingMLPackage wordMLPackage) throws Docx4JException {
        XHTMLImporter importer = new XHTMLImporter(wordMLPackage);
    	
        importer.renderer = new DocxRenderer();
        if (node instanceof Document) {
        	importer.renderer.setDocument( (Document)node, baseUrl );
        } else {
        	Document doc = XmlUtils.neww3cDomDocument();
        	doc.importNode(node, true);
        	importer.renderer.setDocument( doc, baseUrl );
        }
        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), importer.imports, null);
        
        return importer.imports;    	    	
    }
    
    /**
     * @param reader
     * @param baseUrl
     * @param wordMLPackage
     * @return
     * @throws IOException
     */
    public static List<Object> convert(Reader reader,  String baseUrl, WordprocessingMLPackage wordMLPackage) throws Docx4JException {
        XHTMLImporter importer = new XHTMLImporter(wordMLPackage);
    	
        importer.renderer = new DocxRenderer();
        
        Document dom = XMLResource.load(reader).getDocument();        
        importer.renderer.setDocument(dom, baseUrl);
        
        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), importer.imports, null);
        
        return importer.imports;    	    	
    }
    
    /**
     * @param source
     * @param baseUrl
     * @param wordMLPackage
     * @return
     * @throws IOException
     */
    public static List<Object> convert(Source source,  String baseUrl, WordprocessingMLPackage wordMLPackage) throws Docx4JException {
    	
        XHTMLImporter importer = new XHTMLImporter(wordMLPackage);
    	
        importer.renderer = new DocxRenderer();
        
        Document dom = XMLResource.load(source).getDocument();        
        importer.renderer.setDocument(dom, baseUrl);

        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), importer.imports, null);
        
        return importer.imports;    	    	
    }
    
    //public static List<Object> convert(XMLEventReader reader, WordprocessingMLPackage wordMLPackage) throws IOException {
    //public static List<Object> convert(XMLStreamReader reader, WordprocessingMLPackage wordMLPackage) throws IOException {
    
    /**
     * Convert the well formed XHTML found at the specified URI to a list of WML objects.
     * 
     * @param uri
     * @param wordMLPackage
     * @return
     */
    public static List<Object> convert(URL url, WordprocessingMLPackage wordMLPackage) throws Docx4JException {

        XHTMLImporter importer = new XHTMLImporter(wordMLPackage);
    	
        importer.renderer = new DocxRenderer();
        
        String urlString = url.toString();
        Document dom =importer.renderer.loadDocument(urlString);
        importer.renderer.setDocument(dom, urlString);
        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), importer.imports, null);
        
        return importer.imports;    	
    }

    /**
     * 
     * Convert the well formed XHTML contained in the string to a list of WML objects.
     * 
     * @param content
     * @param baseUrl
     * @param wordMLPackage
     * @return
     */
    public static List<Object> convert(String content,  String baseUrl, WordprocessingMLPackage wordMLPackage) throws Docx4JException {
    	
        XHTMLImporter importer = new XHTMLImporter(wordMLPackage);

        importer.renderer = new DocxRenderer();
        
        InputSource is = new InputSource(new BufferedReader(new StringReader(content)));
        
        Document dom;
        try {
        	dom = XMLResource.load(is).getDocument();
        } catch  ( org.docx4j.org.xhtmlrenderer.util.XRRuntimeException xre) {
        	// javax.xml.transform.TransformerException te
        	Throwable t = xre.getCause();
        	if (t instanceof javax.xml.transform.TransformerException) {
	        	// eg content of elements must consist of well-formed character data or markup.
        		
        		
	        	Throwable t2 = ((javax.xml.transform.TransformerException)t).getCause();
	        	if (t2 instanceof org.xml.sax.SAXParseException) {
	        		throw new Docx4JException(
		        			"issues at Line " + ((org.xml.sax.SAXParseException)t2).getLineNumber() 
		        			+ ", Col " + ((org.xml.sax.SAXParseException)t2).getColumnNumber(), t);
	        		
	        	}

        		throw new Docx4JException(
	        			((javax.xml.transform.TransformerException)t).getLocationAsString(), t);
	        	
        	} else {
        		throw xre;
        	}
        }
        
        
        importer.renderer.setDocument(dom, baseUrl);
        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), importer.imports, null);
        
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
            	
            	log.debug("Skipping " +  name.toString() + " .. " + val.getClass().getName() );
            } else {
            	log.debug("Skipping " +  name.toString() + " .. (null value)" );            	
            }
        }
    	
        return cssMap;
    	
    }

    // A paragraph created for a div can be replaced by
    // one created for a p within it, if it is still empty
    boolean paraStillEmpty;

    private void traverse(Box box, List<Object> contentContext, TableProperties tableProperties) throws Docx4JException {
    	traverse( box, contentContext, null,  tableProperties);
    	}    
    
    private void traverse(Box box, List<Object> contentContext, Box parent, TableProperties tableProperties) throws Docx4JException {
        
        log.debug(box.getClass().getName() );
        if (box instanceof BlockBox) {
            BlockBox blockBox = ((BlockBox)box);

            Element e = box.getElement(); 

            // Don't add a new paragraph if this BlockBox is display: inline
            if (e==null) {
            	// Shouldn't happen
                log.debug("<NULL>");
            } else {            
                log.debug("BB"  + "<" + e.getNodeName() + " " + box.getStyle().toStringMine() );
                
                
            	//Map cssMap = styleReference.getCascadedPropertiesMap(e);
                Map<String, CSSValue> cssMap = getCascadedProperties(box.getStyle());
            	
            	/* Sometimes, when it is display: inline, the following is not set:
	            	CSSValue cssValue = (CSSValue)cssMap.get("display");
	            	if (cssValue !=null) {
	            		log.debug(cssValue.getCssText() );
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
                	
            	} else if (box instanceof org.docx4j.org.xhtmlrenderer.newtable.TableSectionBox) {
                	// nb, both TableBox and TableSectionBox 
                	// have node name 'table' (or can have),
            		// so this else clause is before the TableBox one,
            		// to avoid a class cast exception
            		
            		// eg <tbody color: #000000; background-color: transparent; background-image: none; background-repeat: repeat; background-attachment: scroll; background-position: [0%, 0%]; background-size: [auto, auto]; border-collapse: collapse; -fs-border-spacing-horizontal: 0; -fs-border-spacing-vertical: 0; -fs-font-metric-src: none; -fs-keep-with-inline: auto; -fs-page-width: auto; -fs-page-height: auto; -fs-page-sequence: auto; -fs-pdf-font-embed: auto; -fs-pdf-font-encoding: Cp1252; -fs-page-orientation: auto; -fs-table-paginate: auto; -fs-text-decoration-extent: line; bottom: auto; caption-side: top; clear: none; ; content: normal; counter-increment: none; counter-reset: none; cursor: auto; ; display: table-row-group; empty-cells: show; float: none; font-style: normal; font-variant: normal; font-weight: normal; font-size: medium; line-height: normal; font-family: serif; -fs-table-cell-colspan: 1; -fs-table-cell-rowspan: 1; height: auto; left: auto; letter-spacing: normal; list-style-type: disc; list-style-position: outside; list-style-image: none; max-height: none; max-width: none; min-height: 0; min-width: 0; orphans: 2; ; ; ; overflow: visible; page: auto; page-break-after: auto; page-break-before: auto; page-break-inside: auto; position: static; ; right: auto; src: none; table-layout: auto; text-align: left; text-decoration: none; text-indent: 0; text-transform: none; top: auto; ; vertical-align: middle; visibility: visible; white-space: normal; word-wrap: normal; widows: 2; width: auto; word-spacing: normal; z-index: auto; border-top-color: #000000; border-right-color: #000000; border-bottom-color: #000000; border-left-color: #000000; border-top-style: none; border-right-style: none; border-bottom-style: none; border-left-style: none; border-top-width: 2px; border-right-width: 2px; border-bottom-width: 2px; border-left-width: 2px; margin-top: 0; margin-right: 0; margin-bottom: 0; margin-left: 0; padding-top: 0; padding-right: 0; padding-bottom: 0; padding-left: 0; 
            		log.debug(".. processing <tbody");
            		
            		// Do nothing here for now .. the switch statement below traverses children
            		
            		// TODO: give effect to this CSS

            	} else if (box instanceof org.docx4j.org.xhtmlrenderer.newtable.TableBox)  {
                	
            		log.debug(".. processing table");  // what happened to <colgroup><col style="width: 2.47in;" /><col style="width: 2.47in;" /> 
            		
            		/*
            		 * BEWARE: xhtmlrenderer seems to parse tables differently,
            		 * depending on whether:
            		 * 
            		 * (i) the table is contained within a <div>
            		 * 
            		 * (ii) the table contains <caption>
            		 * 
            		 * See https://github.com/plutext/flyingsaucer/issues/1
            		 * 
            		 * Bare table with caption: BlockBox cannot be cast to TableSectionBox in xhtmlrenderer
            		 * 
            		 * div/table[count(caption)=1] ... table becomes TableBox, children are CONTENT_BLOCK
            		 * 
            		 * div/table[count(caption)=0] ... table becomes TableBox, children are CONTENT_BLOCK
            		 * 
            		 * 

            		 * 
            		 */

            		org.docx4j.org.xhtmlrenderer.newtable.TableBox cssTable = (org.docx4j.org.xhtmlrenderer.newtable.TableBox)box;
            		
            		tableProperties = new TableProperties();
            		tableProperties.setTableBox(cssTable);

            		// eg <table color: #000000; background-color: transparent; background-image: none; background-repeat: repeat; background-attachment: scroll; background-position: [0%, 0%]; background-size: [auto, auto]; 
            		//           border-collapse: collapse; -fs-border-spacing-horizontal: 2px; -fs-border-spacing-vertical: 2px; -fs-font-metric-src: none; -fs-keep-with-inline: auto; -fs-page-width: auto; -fs-page-height: auto; -fs-page-sequence: auto; -fs-pdf-font-embed: auto; -fs-pdf-font-encoding: Cp1252; -fs-page-orientation: auto; -fs-table-paginate: auto; -fs-text-decoration-extent: line; bottom: auto; caption-side: top; clear: none; ; content: normal; counter-increment: none; counter-reset: none; cursor: auto; ; display: table; empty-cells: show; float: none; font-style: normal; font-variant: normal; font-weight: normal; font-size: medium; line-height: normal; font-family: serif; -fs-table-cell-colspan: 1; -fs-table-cell-rowspan: 1; height: auto; left: auto; letter-spacing: normal; list-style-type: disc; list-style-position: outside; list-style-image: none; max-height: none; max-width: none; min-height: 0; min-width: 0; orphans: 2; ; ; ; overflow: visible; page: auto; page-break-after: auto; page-break-before: auto; page-break-inside: auto; position: relative; ; right: auto; src: none; 
            		//           table-layout: fixed; text-align: left; text-decoration: none; text-indent: 0; text-transform: none; top: auto; ; vertical-align: baseline; visibility: visible; white-space: normal; word-wrap: normal; widows: 2; width: auto; word-spacing: normal; z-index: auto; border-top-color: #000000; border-right-color: #000000; border-bottom-color: #000000; border-left-color: #000000; border-top-style: solid; border-right-style: solid; border-bottom-style: solid; border-left-style: solid; border-top-width: 1px; border-right-width: 1px; border-bottom-width: 1px; border-left-width: 1px; margin-top: 0; margin-right: 0; margin-bottom: 0; margin-left: 0in; padding-top: 0; padding-right: 0; padding-bottom: 0; padding-left: 0;
            		

            		contentContext = nestedTableHierarchyFix(contentContext,
							parent);
            		
            		Tbl tbl = Context.getWmlObjectFactory().createTbl();
            		contentContext.add(tbl);
		            paraStillEmpty = true;
		            contentContext = tbl.getContent();

            		TblPr tblPr = Context.getWmlObjectFactory().createTblPr();
            		tbl.setTblPr(tblPr);    

            		TblStyle tblStyle = Context.getWmlObjectFactory().createCTTblPrBaseTblStyle();
            		tblStyle.setVal("TableGrid");
            		tblPr.setTblStyle(tblStyle);  
            		
            		// borders.  rudimentary support
            		// for now, look just at border-top-style.
            		// If it is not 'none', for example 'solid', display a border.
            		// cssTable.getBorder requires a CssContext; so just
            		FSDerivedValue borderTopStyle = box.getStyle().valueByName(CSSName.BORDER_TOP_STYLE);
            		if (borderTopStyle!=null  // what to default to if its null?
            				&& borderTopStyle.asString().toLowerCase().contains("none")) {
            			log.debug("setting borders to none");
            			try {
							TblBorders borders = (TblBorders)XmlUtils.unmarshalString(WORDML_TABLE_BORDERS, Context.jc, TblBorders.class);
							tblPr.setTblBorders(borders);
						} catch (JAXBException e1) {}            			
            		} 
            		
            		// Table indent.  
            		// cssTable.getLeftMBP() which is setLeftMBP((int) margin.left() + (int) border.left() + (int) padding.left());
            		// cssTable.getTx(); which is (int) margin.left() + (int) border.left() + (int) padding.left();
            		// But want just margin.left
            		if (cssTable.getMargin() !=null
            				&& cssTable.getMargin().left()>0) {
            			log.debug("Calculating TblInd from margin.left: " + cssTable.getMargin().left() );
                		TblWidth tblIW = Context.getWmlObjectFactory().createTblWidth();
                		tblIW.setW( BigInteger.valueOf( Math.round(
                				cssTable.getMargin().left()
                				)));
                		tblIW.setType(TblWidth.TYPE_DXA);
            			tblPr.setTblInd(tblIW);
            		}
            			
            		// <w:tblW w:w="0" w:type="auto"/>
            		// for both fixed width and auto fit tables.
            		// You'd only set it to something else
            		// eg <w:tblW w:w="5670" w:type="dxa"/>
            		// for what in Word corresponds to 
            		// "Preferred width".  TODO: decide what CSS
            		// requires that.
            		TblWidth tblW = Context.getWmlObjectFactory().createTblWidth();
            		tblW.setW(BigInteger.ZERO);
            		tblW.setType(TblWidth.TYPE_AUTO);
            		tblPr.setTblW(tblW);
            		
	            	if (cssTable.getStyle().isIdent(CSSName.TABLE_LAYOUT, IdentValue.AUTO) 
	            			|| cssTable.getStyle().isAutoWidth()) {
	            		// Conditions under which FS creates AutoTableLayout
	            		
	            		tableProperties.setFixedWidth(false);
	            		
	            		// This is the default, so no need to set 
	            		// STTblLayoutType.AUTOFIT
	            		
	                } else {
	            		// FS creates FixedTableLayout
	            		tableProperties.setFixedWidth(true);
	            		
	            		// <w:tblLayout w:type="fixed"/>
	            		CTTblLayoutType tblLayout = Context.getWmlObjectFactory().createCTTblLayoutType();
	            		tblLayout.setType(STTblLayoutType.FIXED);
	            		tblPr.setTblLayout(tblLayout);
	                }		            	
		            
	            	// Word can generally open a table without tblGrid:
	                // <w:tblGrid>
	                //  <w:gridCol w:w="4621"/>
	                //  <w:gridCol w:w="4621"/>
	                // </w:tblGrid>
	            	// but for an AutoFit table (most common), it 
	            	// is the w:gridCol val which prob specifies the actual width
	            	TblGrid tblGrid = Context.getWmlObjectFactory().createTblGrid();
	            	tbl.setTblGrid(tblGrid);
	            	
	            	int[] colPos = tableProperties.getColumnPos();
	            	
	            	for (int i=1; i<=cssTable.numEffCols(); i++) {
	            		
	            		TblGridCol tblGridCol = Context.getWmlObjectFactory().createTblGridCol();
	            		tblGrid.getGridCol().add(tblGridCol);
	            		
	            		log.debug("colpos=" + colPos[i]);
	            		tblGridCol.setW( BigInteger.valueOf(colPos[i]-colPos[i-1]) );
	            		
	            	}
	            	
            	} else if (e.getNodeName().equals("table") ) {
            		// but not instanceof org.docx4j.org.xhtmlrenderer.newtable.TableBox
            		// .. this does happen.  See test/resources/block-level-lots.xhtml
            		
            		// TODO: look at whether we can style the table in this case

            		log.warn("Encountered non-TableBox table: " + box.getClass().getName() );
            		
            		contentContext = nestedTableHierarchyFix(contentContext,
							parent);
            		
            		Tbl tbl = Context.getWmlObjectFactory().createTbl();
            		contentContext.add(tbl);
		            paraStillEmpty = true;
		            contentContext = tbl.getContent();
            		
            	} else if (box instanceof org.docx4j.org.xhtmlrenderer.newtable.TableRowBox) {
            		
            		// eg <tr color: #000000; background-color: transparent; background-image: none; background-repeat: repeat; background-attachment: scroll; background-position: [0%, 0%]; background-size: [auto, auto]; border-collapse: collapse; -fs-border-spacing-horizontal: 0; -fs-border-spacing-vertical: 0; -fs-font-metric-src: none; -fs-keep-with-inline: auto; -fs-page-width: auto; -fs-page-height: auto; -fs-page-sequence: auto; -fs-pdf-font-embed: auto; -fs-pdf-font-encoding: Cp1252; -fs-page-orientation: auto; -fs-table-paginate: auto; -fs-text-decoration-extent: line; bottom: auto; caption-side: top; clear: none; ; content: normal; counter-increment: none; counter-reset: none; cursor: auto; ; display: table-row; empty-cells: show; float: none; font-style: normal; font-variant: normal; font-weight: normal; font-size: medium; line-height: normal; font-family: serif; -fs-table-cell-colspan: 1; -fs-table-cell-rowspan: 1; height: auto; left: auto; letter-spacing: normal; list-style-type: disc; list-style-position: outside; list-style-image: none; max-height: none; max-width: none; min-height: 0; min-width: 0; orphans: 2; ; ; ; overflow: visible; page: auto; page-break-after: auto; page-break-before: auto; page-break-inside: auto; position: static; ; right: auto; src: none; table-layout: auto; text-align: left; text-decoration: none; text-indent: 0; text-transform: none; top: auto; ; vertical-align: top; visibility: visible; white-space: normal; word-wrap: normal; widows: 2; width: auto; word-spacing: normal; z-index: auto; border-top-color: #000000; border-right-color: #000000; border-bottom-color: #000000; border-left-color: #000000; border-top-style: none; border-right-style: none; border-bottom-style: none; border-left-style: none; border-top-width: 2px; border-right-width: 2px; border-bottom-width: 2px; border-left-width: 2px; margin-top: 0; margin-right: 0; margin-bottom: 0; margin-left: 0; padding-top: 0; padding-right: 0; padding-bottom: 0; padding-left: 0;
            		
            		// TODO support vertical-align
            		
            		log.debug(".. processing <tr");            		

            		Tr tr = Context.getWmlObjectFactory().createTr();
            		contentContext.add(tr);
		            paraStillEmpty = true;
		            contentContext = tr.getContent();
            		
            	} else if (box instanceof org.docx4j.org.xhtmlrenderer.newtable.TableCellBox) {
            		            		
            		log.debug(".. processing <td");            		
            		// eg <td color: #000000; background-color: transparent; background-image: none; background-repeat: repeat; background-attachment: scroll; background-position: [0%, 0%]; background-size: [auto, auto]; border-collapse: collapse; -fs-border-spacing-horizontal: 0; -fs-border-spacing-vertical: 0; -fs-font-metric-src: none; -fs-keep-with-inline: auto; -fs-page-width: auto; -fs-page-height: auto; -fs-page-sequence: auto; -fs-pdf-font-embed: auto; -fs-pdf-font-encoding: Cp1252; -fs-page-orientation: auto; -fs-table-paginate: auto; -fs-text-decoration-extent: line; bottom: auto; caption-side: top; clear: none; ; content: normal; counter-increment: none; counter-reset: none; cursor: auto; ; display: table-row; empty-cells: show; float: none; font-style: normal; font-variant: normal; font-weight: normal; font-size: medium; line-height: normal; font-family: serif; -fs-table-cell-colspan: 1; -fs-table-cell-rowspan: 1; height: auto; left: auto; letter-spacing: normal; list-style-type: disc; list-style-position: outside; list-style-image: none; max-height: none; max-width: none; min-height: 0; min-width: 0; orphans: 2; ; ; ; overflow: visible; page: auto; page-break-after: auto; page-break-before: auto; page-break-inside: auto; position: static; ; right: auto; src: none; table-layout: auto; text-align: left; text-decoration: none; text-indent: 0; text-transform: none; top: auto; ; vertical-align: top; visibility: visible; white-space: normal; word-wrap: normal; widows: 2; width: auto; word-spacing: normal; z-index: auto; border-top-color: #000000; border-right-color: #000000; border-bottom-color: #000000; border-left-color: #000000; border-top-style: none; border-right-style: none; border-bottom-style: none; border-left-style: none; border-top-width: 2px; border-right-width: 2px; border-bottom-width: 2px; border-left-width: 2px; margin-top: 0; margin-right: 0; margin-bottom: 0; margin-left: 0; padding-top: 0; padding-right: 0; padding-bottom: 0; padding-left: 0;

            		List<Object> trContext = contentContext;
            		org.docx4j.org.xhtmlrenderer.newtable.TableCellBox tcb = (org.docx4j.org.xhtmlrenderer.newtable.TableCellBox)box;
            		// tcb.getVerticalAlign()
            		
            		// rowspan support: vertically merged cells are
            		// represented as a top cell containing the actual content with a vMerge tag with "restart" attribute 
            		// and a series of dummy cells having a vMerge tag with no (or "continue") attribute.            		
            		            		
					// if current cell is the first real cell in the row, but is not in the leftmost position, then
					// search for vertically spanned cells to the left and insert dummy cells before current
					if (tcb.getParent().getChild(0) == tcb && tcb.getCol() > 0) {
						insertDummyVMergedCells(trContext, tcb, true);
					}

					int effCol = tcb.getTable().colToEffCol(tcb.getCol());
            		
                    // The cell proper
            		Tc tc = Context.getWmlObjectFactory().createTc();
            		contentContext.add(tc);
		            contentContext = tc.getContent();

            		// if the td contains bare text (eg <td>apple</td>)
            		// we need a p for it
            		currentP = Context.getWmlObjectFactory().createP();                                        	
    	            contentContext.add(currentP);            		
		            paraStillEmpty = true;
            		
            		// Do we need a vMerge tag with "restart" attribute?
            		// get cell below (only 1 section supported at present)
            		TcPr tcPr = Context.getWmlObjectFactory().createTcPr();
        			tc.setTcPr(tcPr);
                    if (tcb.getStyle().getRowSpan()> 1) {
            			
            			VMerge vm = Context.getWmlObjectFactory().createTcPrInnerVMerge();
            			vm.setVal("restart");
            			tcPr.setVMerge(vm);            
                    }
                    // eg <w:tcW w:w="2268" w:type="dxa"/>
                    try {
	            		TblWidth tblW = Context.getWmlObjectFactory().createTblWidth();
	            		tblW.setW(BigInteger.valueOf(tableProperties.getColumnWidth(effCol+1) ));
	            		tblW.setType(TblWidth.TYPE_DXA);
	            		tcPr.setTcW(tblW);    	                    
                    } catch (java.lang.ArrayIndexOutOfBoundsException aioob) {
                    	// happens with http://en.wikipedia.org/wiki/Office_Open_XML
                    	log.error("Problem with getColumnWidth for col" + (effCol+1) );
                    }
/*                  The below works, but the above formulation is simpler
 * 
 * 					int r = tcb.getRow() + tcb.getStyle().getRowSpan() - 1;
                    if (r < tcb.getSection().numRows() - 1) {
                        // The cell is not in the last row, so use the next row in the
                        // section.
                        TableCellBox belowCell = section.cellAt( r + 1, effCol);
	                    log.debug("Got belowCell for " + tcb.getRow() + ", " + tcb.getCol() );
	                    log.debug("it is  " + belowCell.getRow() + ", " + belowCell.getCol() );
                        if (belowCell.getRow() > tcb.getRow() + 1 ) {
	                		TcPr tcPr = Context.getWmlObjectFactory().createTcPr();
	            			tc.setTcPr(tcPr);
	            			
	            			VMerge vm = Context.getWmlObjectFactory().createTcPrInnerVMerge();
	            			vm.setVal("restart");
	            			tcPr.setVMerge(vm);                        	
                        }
                    } 
 */            		
            		// colspan support: horizontally merged cells are represented by one cell
            		// with a gridSpan attribute; 
            		int colspan = tcb.getStyle().getColSpan(); 
            		if (colspan>1) {
            			
						TcPr tcPr2 = tc.getTcPr();
						if (tcPr2 == null) {
							tcPr2 = Context.getWmlObjectFactory().createTcPr();
							tc.setTcPr(tcPr2);
						}

            			GridSpan gs = Context.getWmlObjectFactory().createTcPrInnerGridSpan();
            			gs.setVal( BigInteger.valueOf(colspan));
            			tcPr2.setGridSpan(gs);
            			
            			this.setCellWidthAuto(tcPr2);            			
            		}
            		
					// search for vertically spanned cells to the right from current, and insert dummy cells after it
					insertDummyVMergedCells(trContext, tcb, false);

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
            
            Object lastChild = null;
            
            	log.debug("Processing children of " + box.getElement().getNodeName() );
	            switch (blockBox.getChildrenContentType()) {
	                case BlockBox.CONTENT_BLOCK:
	                	log.debug(".. which are BlockBox.CONTENT_BLOCK");	                	
	                    for (Object o : ((BlockBox)box).getChildren() ) {
	                    	
	                    	lastChild = o;
	                    	
	                        traverse((Box)o, contentContext,  box, tableProperties);                    
	                        log.debug(".. processed child " + o.getClass().getName() );
	                    }
	                    break;
	                case BlockBox.CONTENT_INLINE:
	                	
	                	log.debug(".. which are BlockBox.CONTENT_INLINE");	                	
	                	
	                    if ( ((BlockBox)box).getInlineContent()!=null) {

	                    	
	                        for (Object o : ((BlockBox)box).getInlineContent() ) {
	//                            log.debug("        " + o.getClass().getName() ); 
	                            if (o instanceof InlineBox ) {
	//                                    && ((InlineBox)o).getElement()!=null // skip these (pseudo-elements?)
	//                                    && ((InlineBox)o).isStartsHere()) {
	                                
	                            	processInlineBox( (InlineBox)o, contentContext);
	                            	
	                            } else if (o instanceof BlockBox ) {
	                                traverse((Box)o, contentContext, box, tableProperties); // commenting out gets rid of unwanted extra parent elements
	                            } else {
	                                log.debug("What to do with " + box.getClass().getName() );                        
	                            }
		                        log.debug(".. processed child " + o.getClass().getName() );
	                        }
	                    }
	                    break;
	            } 
            
		    
            log.debug("Done processing children of " + box.getClass().getName() );
            // contentContext gets its old value back each time recursion finishes,
            // ensuring elements are added at the appropriate level (eg inside tr) 
            
            // An empty tc shouldn't make the table disappear!
            // TODO - make more elegant
            if (e.getNodeName().equals("table")) {            	
            	paraStillEmpty = false;
            }

//        	if ( (lastChild instanceof Box)
//        			&& ((Box)lastChild).getElement().getNodeName().equals("table") ) {
//        		System.out.println("## " + e.getNodeName() );
//        	}       
        	

            // nested tables must end with a <p/> or Word 2010 can't open the docx!
            // ie:
            // <w:tc>
            //   <w:tbl>..</w:tbl>
            //   <w:p/>                <---------- 
            // </w:tc>
        	// This fixes the dodgy table/table case
    		if (box instanceof TableBox
    				|| box.getElement().getNodeName().equals("table") ) {
        	
            	if ( (lastChild instanceof Box)
            			&& ((Box)lastChild).getElement().getNodeName().equals("table") ) {
            		log.debug("Adding <w:p/> after nested table");
            		P extraP = Context.getWmlObjectFactory().createP();                                        	
    	            
            		Tr tr = (Tr)
            				contentContext.get(contentContext.size()-1);
            		((Tc)tr.getContent().get(tr.getContent().size()-1)).getContent().add(extraP);
            		//contentContext.add(extraP);            		
                	paraStillEmpty = false; // ??           		
            	}
            }
          
    		
          if (e.getNodeName().equals("td") ) {  // untested
        	  
          	if ( (lastChild instanceof Box)
        			&& ((Box)lastChild).getElement().getNodeName().equals("table") ) {
        		log.debug("Adding <w:p/> after nested table");
        		P extraP = Context.getWmlObjectFactory().createP();                                        	
	            
        		contentContext.add(extraP);            		
            	paraStillEmpty = false; // ??           		
        	}
        }
            
            
        } else if (box instanceof AnonymousBlockBox) {
            log.debug("AnonymousBlockBox");            
        }
    
    }

	/**
	 * Rowspan and colspan support.
	 * Search for lower parts of vertically merged cells, adjacent to current cell in given direction.
	 * Then insert the appropriate number of dummy cells, with the same horizontal merging as in their top parts into row context.
	 * @param trContext context of the row to insert dummies into
	 * @param tcb current cell
	 * @param backwards direction flag: if true, then scan to the left
	 */
	private void insertDummyVMergedCells(List<Object> trContext, TableCellBox tcb, boolean backwards) {

		log.debug("Scanning cells from " + tcb.getRow() + ", " + tcb.getCol() + " to the " + (backwards ? "left" : "right") );

		ArrayList<TableCellBox> adjCells = new ArrayList<TableCellBox>();
		int numEffCols = tcb.getTable().numEffCols();

		for ( int i = tcb.getCol(); i >= 0 && i < numEffCols; i += backwards ? -1 : 1 ) {

			TableCellBox adjCell = tcb.getSection().cellAt(tcb.getRow(), i);

			if ( adjCell == null ) {
				// Check your table is OK
				log.error("XHTML table import: Null adjCell for row " + tcb.getRow() + ", col " + tcb.getCol() + " at col " + i);
				break;
			}
			if ( adjCell == tcb || adjCell == TableCellBox.SPANNING_CELL ) {
				continue;
			}
			log.debug("Got adjCell, it is  " + adjCell.getRow() + ", " + adjCell.getCol());

			if ( adjCell.getRow() < tcb.getRow()
					&& adjCell.getStyle()!=null
					&& adjCell.getStyle().getRowSpan()>1 ) {
				// eg tcb is r2,c1 & adjCell is r1,c0
				adjCells.add(adjCell);
			} else {
				break;
			}
		}

		if ( backwards && !adjCells.isEmpty() ) {
			Collections.reverse(adjCells);
		}

		for (TableCellBox adjCell : adjCells) {
			Tc dummy = Context.getWmlObjectFactory().createTc();
			trContext.add(dummy);

			TcPr tcPr = Context.getWmlObjectFactory().createTcPr();
			dummy.setTcPr(tcPr);

			VMerge vm = Context.getWmlObjectFactory().createTcPrInnerVMerge();
			//vm.setVal("continue");
			tcPr.setVMerge(vm);

			int colspan = adjCell.getStyle().getColSpan();
			if (colspan > 1) {
				GridSpan gs = Context.getWmlObjectFactory().createTcPrInnerGridSpan();
				gs.setVal( BigInteger.valueOf(colspan));
				tcPr.setGridSpan(gs);
			}

			this.setCellWidthAuto(tcPr);

			// Must have an empty w:p
			dummy.getContent().add( new P() );
		}
	}


	/**
	 * nested tables XHTML renderer seems to construct a tree: table/table
	 * instead of table/tr/td/table?
	 * TODO fix this upstream.
	 * TestCase is http://en.wikipedia.org/wiki/Office_Open_XML
	 * 
	 * @param contentContext
	 * @param parent
	 * @return
	 */
	private List<Object> nestedTableHierarchyFix(List<Object> contentContext,
			Box parent) {
		if (parent instanceof TableBox
				|| parent.getElement().getNodeName().equals("table") ) {
			log.warn("table: Constructing missing w:tr/w:td..");

			Tr tr = Context.getWmlObjectFactory().createTr();
			contentContext.add(tr);
		    contentContext = tr.getContent();            			
			
			Tc tc = Context.getWmlObjectFactory().createTc();
			contentContext.add(tc);
		    contentContext = tc.getContent();            			
		}
		return contentContext;
	}
    
    private void setCellWidthAuto(TcPr tcPr) {
    	// <w:tcW w:w="0" w:type="auto"/>
		TblWidth tblW = Context.getWmlObjectFactory().createTblWidth();
		tblW.setW(BigInteger.ZERO);
		tblW.setType(TblWidth.TYPE_AUTO);
		tcPr.setTcW(tblW);    	
    }

		private void addImage(Element e) {
		boolean isError = false;
		try {
			byte[] imageBytes = null;

			if (e.getAttribute("src").startsWith("data:image")) {
				// Supports 
				//   data:[<MIME-type>][;charset=<encoding>][;base64],<data>
				// eg data:image/png;base64,iVBORw0KGgo...
				// http://www.greywyvern.com/code/php/binary2base64 is a convenient online encoder
				String base64String = e.getAttribute("src");
				int commaPos = base64String.indexOf(",");
				if (commaPos < 6) { // or so ...
					// .. its broken
					org.docx4j.wml.R run = Context.getWmlObjectFactory().createR();
					currentP.getContent().add(run);

					org.docx4j.wml.Text text = Context.getWmlObjectFactory().createText();
					text.setValue("[INVALID DATA URI: " + e.getAttribute("src"));

					run.getContent().add(text);

					paraStillEmpty = false;
					return;
				}
				base64String = base64String.substring(commaPos + 1);
				System.out.println(base64String);
				imageBytes = Base64.decodeBase64(base64String.getBytes("UTF8"));
			} else {
				Docx4jUserAgent docx4jUserAgent = renderer.getDocx4jUserAgent();
				Docx4JFSImage docx4JFSImage = docx4jUserAgent.getDocx4JImageResource(e.getAttribute("src"));
				if (docx4JFSImage != null) {// in case of wrong URL - docx4JFSImage will be null
					imageBytes = docx4JFSImage.getBytes();
				}
			}
			if (imageBytes == null) {
				isError = true;
			} else {
				BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, imageBytes);
				Inline inline = imagePart.createImageInline(null, null, 0, 1, false);

				// Now add the inline in w:p/w:r/w:drawing
				org.docx4j.wml.R run = Context.getWmlObjectFactory().createR();
				currentP.getContent().add(run);
				org.docx4j.wml.Drawing drawing = Context.getWmlObjectFactory().createDrawing();
				run.getContent().add(drawing);
				drawing.getAnchorOrInline().add(inline);
			}
		} catch (Exception e1) {
			log.error(MessageFormat.format("Error during image processing: ''{0}'', insert default text.", new Object[] {e.getAttribute("alt")}), e1);
			isError = true;
		}

		if (isError) {
			org.docx4j.wml.R run = Context.getWmlObjectFactory().createR();
			currentP.getContent().add(run);

			org.docx4j.wml.Text text = Context.getWmlObjectFactory().createText();
			text.setValue("[MISSING IMAGE: " + e.getAttribute("alt") + " ]");

			run.getContent().add(text);
		}

		paraStillEmpty = false;

	}

	private void addNumbering(Element e, Map<String, CSSValue> cssMap) {
		if (ndp==null) {
			log.debug("No NumberingDefinitions part - so adding");
			try {
				ndp = new NumberingDefinitionsPart();
				wordMLPackage.getMainDocumentPart().addTargetPart(ndp);
				ndp.setJaxbElement( Context.getWmlObjectFactory().createNumbering() );				
			} catch (InvalidFormatException e1) {
				// Won't happen
				e1.printStackTrace();
			}
		}
		Numbering.Num num = null;
		try {
			if ( cssMap.get("list-style-type" ).getCssText().equals("decimal")) {
				num = listHelper.getOrderedList(ndp);
			}
			if (cssMap.get("list-style-type" ).getCssText().equals("disc")
					|| cssMap.get("list-style-type" ).getCssText().equals("square")) {
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

	// For a hyperlink, we do all the processing when
	// we hit that element.  No need to add its children again
	private boolean inAlreadyProcessed = false; // TODO may need a stack of these.
	
    private void processInlineBox( InlineBox inlineBox, List<Object> contentContext) {
    	
    	log.debug(inlineBox.toString());

        // Doesn't extend box
        Styleable s = ((InlineBox)inlineBox );
        if (s.getStyle()==null) { // Assume this won't happen
        	log.error("getStyle returned null!");
        }
        
    	if (inAlreadyProcessed) {
    		log.debug(".. already done?!");
        	if (s.getElement() !=null
        			&& s.getElement().getNodeName().equals("a")
        			&& inlineBox.isEndsHere() ) {
        		// When we hit the end of the hyperlink
        		inAlreadyProcessed = false; // ready for next element
        	}                	
    		return; 
    	}
        
        Map<String, CSSValue> cssMap = getCascadedProperties(s.getStyle());
//        Map cssMap = styleReference.getCascadedPropertiesMap(s.getElement());
                        
        String debug = "<UNKNOWN Styleable";
        if (s.getElement()==null) {
        	// Do nothing
        } else {
            debug = "<" + s.getElement().getNodeName();
            
            if (s.getElement().getNodeName().equals("a")) {
            	log.debug("Ha!  found a hyperlink. ");
            	
            	/* For hyperlink anchors, there are three cases.
            	 * 
            	 * Case 1: hyperlink inline box contains another
            	 * inline box eg <a href=".."><span>my inline box</span></a>
            	 * 
            	 * Case 2: hyperlink inline box doesn't contain
            	 * another inline box eg <a href="..">no inline box</a>
            	 * 
            	 * Case 3: empty point tag eg 
            	 * <a href="http://slashdot.org/" /> ie empty - malformed
            	 * 
            	 * The code has been tested with the following examples:
            	 * 
			        String xhtml= "<p ><a href=\"http://davidpritchard.org/images/pacsoc-s1b.png\"><span>http://davidpritchard.org/images/pacsoc-s1b.png</span></a></p>";
			    	
			        String xhtml= "<p ><a href=\"http://davidpritchard.org/images/pacsoc-s1b.png\">http://davidpritchard.org/images/pacsoc-s1b.png</a></p>";        
			    	
			        String xhtml= "<p ><a href=\"slashdot.org\" /></p>";        
			        
			        String xhtml= "<p ><a href=\"slashdot.org\" >slash<b>dot</b>.<span>o<i>r</i>g</span> </a></p>";
			        
			          in the last case, the link formatting is dropped.
			          
			        TODO: there is still a weird case in http://en.wikipedia.org/wiki/Office_Open_XML
			        where the contents are repeated.  Seems very sensitive to the context?
			        
                    	 */
            	
            	if (inlineBox.isStartsHere()) {
            		
                	Hyperlink h = null;
                	String linkText = inlineBox.getElement().getTextContent();
                	log.debug(linkText);
                	if (linkText!=null
                			&& !linkText.trim().equals("")) {
                		// Cases 1 & 2
                    	h = createHyperlink(
                    			s.getElement().getAttribute("href"), 
                    			addRunProperties( cssMap ),
                    			linkText, rp);                                    	            		
                        currentP.getContent().add(h);
//                        if (inlineBox.getElement().getChildNodes().getLength()==1
//                        		&& inlineBox.getElement().getChildNodes().item(0).getNodeType()==Node.TEXT_NODE) {
//                        	// eg <a href="/wiki/Ecma_International" title="Ecma International">Ecma</a>
//                        	// endsHere incorrectly set to true in that case?
//                        	inAlreadyProcessed = true;                        	                        	
//                        } else 
                        	if (!inlineBox.isEndsHere() ) {
                        	inAlreadyProcessed = true;
                        }
                        return;
                	} else {
                    	// Case 3           	
                    	h = createHyperlink(
                    			s.getElement().getAttribute("href"), 
                    			addRunProperties( cssMap ),
                    			s.getElement().getAttribute("href"), rp);                                    	            		
                        currentP.getContent().add(h);
                        // No need to set inAlreadyProcessed = true;
                        // since no children to process
                        return;
                	}
            		
            	} 
            	
            	
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
        
//        // We've processed the hyperlink, so skip the inline boxes
//        // representing its children
//        if (awaitingEnd) return;
        
        log.debug(debug );
        //log.debug("'" + ((InlineBox)o).getTextNode().getTextContent() );  // don't use .getText()
        
        processInlineBoxContent(inlineBox, s, cssMap);
    }

	private void processInlineBoxContent(InlineBox inlineBox, Styleable s,
			Map<String, CSSValue> cssMap) {
		if (inlineBox.getTextNode()==null) {
                
            if (s.getElement().getNodeName().equals("br") ) {
                
                R run = Context.getWmlObjectFactory().createR();
                currentP.getContent().add(run);                
           		run.getContent().add(Context.getWmlObjectFactory().createBr());
                
            } else {
            	log.debug("InlineBox has no TextNode, so skipping" );
            	
            	// TODO .. a span in a span?
            	// need to traverse
            }
            
        } else  {
            log.debug( inlineBox.getTextNode().getTextContent() );  // don't use .getText()

            String theText = inlineBox.getTextNode().getTextContent(); 
            log.debug("Processing " + theText);
            
            paraStillEmpty = false;                                    
                        
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
	            	//log.debug(p.getClass().getName() );
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
	            	//log.debug(p.getClass().getName() );
	        	}
        	}
        }
        return rPr;
    }

	private Hyperlink createHyperlink(String url, RPr rPr, String linkText, RelationshipsPart rp) {
		
		if (linkText.contains("&")
				&& !linkText.contains("&amp;")) {
			// escape them so we can unmarshall
			linkText = linkText.replace("&", "&amp;");
		}
		
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
			// eg  org.xml.sax.SAXParseException: The reference to entity "ballot_id" must end with the ';' delimiter. 
			log.error("Dodgy link text: '" + linkText + "'", e);
			return null;
		}
		
		
	}
	
	static String  WORDML_TABLE_BORDERS = "<w:tblBorders xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" >"
			+"  <w:top w:val=\"none\" w:sz=\"0\" w:space=\"0\" w:color=\"auto\"/>"
			  +"  <w:left w:val=\"none\" w:sz=\"0\" w:space=\"0\" w:color=\"auto\"/>"
			  +"<w:bottom w:val=\"none\" w:sz=\"0\" w:space=\"0\" w:color=\"auto\"/>"
			  +"<w:right w:val=\"none\" w:sz=\"0\" w:space=\"0\" w:color=\"auto\"/>"
			  +"<w:insideH w:val=\"none\" w:sz=\"0\" w:space=\"0\" w:color=\"auto\"/>"
			  +"<w:insideV w:val=\"none\" w:sz=\"0\" w:space=\"0\" w:color=\"auto\"/>"
			  +"</w:tblBorders>";

	
	public final static class TableProperties {
		
		private TableBox tableBox;
		
		public TableBox getTableBox() {
			return tableBox;
		}

		public void setTableBox(TableBox tableBox) {
			this.tableBox = tableBox;
			colPos = tableBox.getColumnPos();
		}
		
    	private int[] colPos; 
    	public int[] getColumnPos() {
    		return colPos;
    	}
		
    	public int getColumnWidth(int col) {
    		return colPos[col] - colPos[col-1];
    	}

		boolean isFixedWidth;

		public boolean isFixedWidth() {
			return isFixedWidth;
		}

		public void setFixedWidth(boolean isFixedWidth) {
			this.isFixedWidth = isFixedWidth;
		}
	}
    
    
    
}
