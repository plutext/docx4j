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
import org.docx4j.wml.Text;
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
 * 
 * @author jharrop
 *
 */
public class Importer {
	
	protected static Logger log = Logger.getLogger(Importer.class);		
	    
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
    }

    public static List<Object> convert(File file, WordprocessingMLPackage wordMLPackage) throws IOException {

        Importer importer = new Importer(wordMLPackage);

        importer.renderer = new DocxRenderer();
        importer.renderer.setDocument(file);
        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), "");
        
        return importer.imports;    	
    }
    
    public static List<Object> convert(String uri, WordprocessingMLPackage wordMLPackage) {

        Importer importer = new Importer(wordMLPackage);
    	
        importer.renderer = new DocxRenderer();
        importer.renderer.setDocument(uri);
        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), "");
        
        return importer.imports;    	
    }

    public static List<Object> convertFromString(String content, WordprocessingMLPackage wordMLPackage, String baseUrl) {
    	
        Importer importer = new Importer(wordMLPackage);

        importer.renderer = new DocxRenderer();
        importer.renderer.setDocumentFromString(content, baseUrl);
        importer.renderer.layout();
                    
        importer.traverse(importer.renderer.getRootBox(), "");
        
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
    
    private void traverse(Box box, String indents) {
        
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
                
                // TEMP - Ignore tables
                if (e.getNodeName().equals("table")) return;
                
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
    		            imports.add(currentP);
    		            paraStillEmpty = true;
                	}            		
	            } else {
	            	
	            	// Avoid creating paragraphs for html, body
	            	if (imports.size()>0 && paraStillEmpty) {
			            imports.remove( imports.size()-1);                                        		
	            	} 
	            	
		            currentP = Context.getWmlObjectFactory().createP();
		            imports.add(currentP);
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
            
            switch (blockBox.getChildrenContentType()) {
                case BlockBox.CONTENT_BLOCK:
                    for (Object o : ((BlockBox)box).getChildren() ) {
                        traverse((Box)o, indents + "    ");                    
                    }
                    break;
                case BlockBox.CONTENT_INLINE:
                    if ( ((BlockBox)box).getInlineContent()!=null) {
                    	
                        for (Object o : ((BlockBox)box).getInlineContent() ) {
//                            log.info("        " + o.getClass().getName() ); 
                            if (o instanceof InlineBox ) {
//                                    && ((InlineBox)o).getElement()!=null // skip these (pseudo-elements?)
//                                    && ((InlineBox)o).isStartsHere()) {
                                
                            	processInlineBox( (InlineBox)o, indents);
                            	
                            } else if (o instanceof BlockBox ) {
                                traverse((Box)o, indents + "    "); // commenting out gets rid of unwanted extra parent elements
                            } else {
                                log.info("What to do with " + box.getClass().getName() );                        
                            }
                        }
                    }
                    break;
            }            
            
        } else if (box instanceof AnonymousBlockBox) {
            //log.info("AnonymousBlockBox");            
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

    private void processInlineBox( InlineBox inlineBox, String indents) {

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
		            imports.remove( imports.size()-1);                                        		
            	} 
	            imports.add(currentP);
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
            			"Hyperlink", 
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
            			"Hyperlink", theText, rp);                                    	
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

	private Hyperlink createHyperlink(String url, String style, String linkText, RelationshipsPart rp) {
		
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
            "<w:rPr>" +
            "<w:rStyle w:val=\"" + style + "\" />" +  
            "</w:rPr>" +
            "<w:t>" + linkText + "</w:t>" +
            "</w:r>" +
            "</w:hyperlink>";

			return (Hyperlink)XmlUtils.unmarshalString(hpl);
			
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
        File f = new File(System.getProperty("user.dir") + "/src/test/resources/xhtml/img.xhtml");
            
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
