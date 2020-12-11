package org.docx4j.convert.out.XSLFO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBElement;

import org.apache.commons.io.FileUtils;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.FOSettings;
import org.docx4j.convert.out.common.Exporter;
import org.docx4j.convert.out.fo.FOExporterVisitor;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.vml.CTPath;
import org.docx4j.vml.CTShape;
import org.docx4j.vml.CTShapetype;
import org.docx4j.vml.CTStroke;
import org.docx4j.vml.CTTextbox;
import org.docx4j.vml.wordprocessingDrawing.CTWrap;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.P;
import org.docx4j.wml.Pict;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.junit.Ignore;

/**
 * 
 * This class documents the various cases used in the development of the text box to FO code
 * found in PictWriter
 * 
 * The various cases can be renamed to main, then executed.
 * 
 * These aren't unit tests, yet.
 * 
 * For the notWrapped* case, front versus behind is not explicitly tested.  Front should
 * use a solid background. TODO investigate how Word does that. 
 * 
 * @author jharrop
 *
 */
@Ignore
public class TextBoxTest {
	
	
	static boolean save = true; 
	static boolean fo = false;
	
	static org.docx4j.vml.wordprocessingDrawing.ObjectFactory w10ObjectFactory = new org.docx4j.vml.wordprocessingDrawing.ObjectFactory(); 
	
	
	public static void main(String[] args) throws Exception {
		

		// paragraph
	    
		// NB, to implement this case properly, Word needs <w10:wrap type="square"/>
	    WordprocessingMLPackage wordMLPackage = createTextBoxDocx(true, "position:absolute;margin-left:0;margin-top:0;width:186pt;height:110.25pt;z-index:-251657216;visibility:visible;mso-wrap-style:square;mso-width-percent:400;mso-height-percent:200;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal:center;mso-position-horizontal-relative:text;mso-position-vertical:absolute;mso-position-vertical-relative:text;mso-width-percent:400;mso-height-percent:200;mso-width-relative:margin;mso-height-relative:margin;v-text-anchor:top" );
	    execute( wordMLPackage);
	}

	public static void notWrappedParagraph(String[] args) throws Exception {
		
		// NB, to implement this case properly, Word needs <w10:wrap type="square"/>
	    WordprocessingMLPackage wordMLPackage = createTextBoxDocx(true, "position:absolute;margin-left:0;margin-top:0;width:186pt;height:110.25pt;z-index:-251657216;visibility:visible;mso-wrap-style:square;mso-width-percent:400;mso-height-percent:200;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal:center;mso-position-horizontal-relative:text;mso-position-vertical:absolute;mso-position-vertical-relative:text;mso-width-percent:400;mso-height-percent:200;mso-width-relative:margin;mso-height-relative:margin;v-text-anchor:top" );
	    execute( wordMLPackage);
	}
	
	public static void notWrappedPageTop(String[] args) throws Exception {
	    
		// NB, to implement this case properly, Word needs <w10:wrap anchory="page"/>
	    WordprocessingMLPackage wordMLPackage = createTextBoxDocx(true, "position:absolute;margin-left:0;margin-top:0;width:186pt;height:110.25pt;z-index:-251657216;visibility:visible;mso-wrap-style:square;mso-width-percent:400;mso-height-percent:200;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal:center;mso-position-horizontal-relative:text;mso-position-vertical:top;mso-position-vertical-relative:page;mso-width-percent:400;mso-height-percent:200;mso-width-relative:margin;mso-height-relative:margin;v-text-anchor:top" );
	    execute( wordMLPackage);
	}
	
	public static void notWrappedPageBottom(String[] args) throws Exception {
		
	    WordprocessingMLPackage wordMLPackage = createTextBoxDocx(true, "position:absolute;margin-left:0;margin-top:0;width:186pt;height:110.25pt;z-index:-251657216;visibility:visible;mso-wrap-style:square;mso-width-percent:400;mso-height-percent:200;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal:center;mso-position-horizontal-relative:text;mso-position-vertical:bottom;mso-position-vertical-relative:page;mso-width-percent:400;mso-height-percent:200;mso-width-relative:margin;mso-height-relative:margin;v-text-anchor:top"  );
	    execute( wordMLPackage);
	}

	public static void notWrappedBottomMarginArea(String[] args) throws Exception {
		
	    WordprocessingMLPackage wordMLPackage = createTextBoxDocx(true, "position:absolute;margin-left:0;margin-top:0;width:186pt;height:110.25pt;z-index:-251657216;visibility:visible;mso-wrap-style:square;mso-width-percent:400;mso-height-percent:200;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal:center;mso-position-horizontal-relative:text;mso-position-vertical:bottom;mso-position-vertical-relative:bottom-margin-area;mso-width-percent:400;mso-height-percent:200;mso-width-relative:margin;mso-height-relative:margin;v-text-anchor:top"  );
	    execute( wordMLPackage);
	}
	
	public static void notWrappedTopMarginArea(String[] args) throws Exception {
			    
	    WordprocessingMLPackage wordMLPackage = createTextBoxDocx(true, "position:absolute;margin-left:0;margin-top:0;width:186pt;height:110.25pt;z-index:-251657216;visibility:visible;mso-wrap-style:square;mso-width-percent:400;mso-height-percent:200;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal:center;mso-position-horizontal-relative:text;mso-position-vertical:top;mso-position-vertical-relative:top-margin-area;mso-width-percent:400;mso-height-percent:200;mso-width-relative:margin;mso-height-relative:margin;v-text-anchor:top"  );
	    execute( wordMLPackage);
	}
	
	public static void notWrappedRelPara(String[] args) throws Exception {
	    
	    WordprocessingMLPackage wordMLPackage = createTextBoxDocx(true, "position:absolute;margin-left:0;margin-top:0;width:186.95pt;height:110.55pt;z-index:251658240;visibility:visible;mso-wrap-style:square;mso-width-percent:0;mso-height-percent:0;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal:absolute;mso-position-horizontal-relative:text;mso-position-vertical:absolute;mso-position-vertical-relative:text;mso-width-percent:0;mso-height-percent:0;mso-width-relative:page;mso-height-relative:page;v-text-anchor:top" );
	    execute( wordMLPackage);
	}
	
	public static void wrappedLeft(String[] args) throws Exception {

		// margin top > 0 ; margin left = 0
	    
	    WordprocessingMLPackage wordMLPackage = createTextBoxDocx(false, "position:absolute;margin-left:0;margin-top:72.05pt;width:186.95pt;height:110.55pt;z-index:-251658240;visibility:visible;mso-wrap-style:square;mso-width-percent:0;mso-height-percent:0;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal:absolute;mso-position-horizontal-relative:text;mso-position-vertical:absolute;" +
	    		"mso-position-vertical-relative:text;mso-width-percent:0;mso-height-percent:0;mso-width-relative:page;mso-height-relative:page;v-text-anchor:top" );
	    execute( wordMLPackage);
	}
	

	public static void wrappedAboveAndAbsoluteToRightOfColumn(String[] args) throws Exception {
		
		// Absolute position to the right of column produces:

		// margin-left:108pt
		// mso-position-horizontal:absolute <------------
		// mso-position-horizontal-relative:text
    	// mso-wrap-style:square
		WordprocessingMLPackage wordMLPackage = createTextBoxDocx(false, "position:absolute;margin-left:50pt;margin-top:0;width:186.95pt;height:110.55pt;z-index:251659264;visibility:visible;mso-wrap-style:square;mso-width-percent:400;mso-height-percent:200;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;" +
						"mso-position-horizontal:absolute;mso-position-horizontal-relative:text;mso-position-vertical:absolute;mso-position-vertical-relative:text;mso-width-percent:400;mso-height-percent:200;mso-width-relative:margin;mso-height-relative:margin;v-text-anchor:top");
		execute( wordMLPackage);
	}
	
	
	public static void wrappedAboveAndLeft(String[] args) throws Exception {
		
	    		
		// Alignment LEFT
		
		WordprocessingMLPackage wordMLPackage = createTextBoxDocx(false, "position:absolute;margin-left:0;margin-top:0;width:186.95pt;height:110.55pt;z-index:251659264;visibility:visible;mso-wrap-style:square;mso-width-percent:400;mso-height-percent:200;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;" +
				"mso-position-horizontal:left;mso-position-horizontal-relative:text;mso-position-vertical:absolute;mso-position-vertical-relative:text;mso-width-percent:400;mso-height-percent:200;mso-width-relative:margin;mso-height-relative:margin;v-text-anchor:top");
	    
	    execute( wordMLPackage);
	}
	
	public static void wrappedAboveAndCentered(String[] args) throws Exception {
		
		
		//	mso-wrap-style:square
		//	mso-position-vertical:absolute
		//	mso-position-vertical-relative:text
		//	v-text-anchor:top",
		WordprocessingMLPackage wordMLPackage = createTextBoxDocx(false, "position:absolute;margin-left:0;margin-top:0;width:186.95pt;height:110.55pt;z-index:251659264;visibility:visible;mso-wrap-style:square;mso-width-percent:400;mso-height-percent:200;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal:center;mso-position-horizontal-relative:text;mso-position-vertical:absolute;mso-position-vertical-relative:text;mso-width-percent:400;mso-height-percent:200;mso-width-relative:margin;mso-height-relative:margin;v-text-anchor:top");
	    
	    execute( wordMLPackage);
	}

	public static void wrappedAboveAndRight(String[] args) throws Exception {
		
		
		// Aligment RIGHT relative to column produces:
		
		// margin-left:135.75pt
		// mso-position-horizontal:right <---------
		// mso-position-horizontal-relative:text
		// mso-wrap-style:square

		WordprocessingMLPackage wordMLPackage = createTextBoxDocx(false, "position:absolute;margin-left:135.75pt;margin-top:0;width:186.95pt;height:110.55pt;z-index:251659264;visibility:visible;mso-wrap-style:square;mso-width-percent:400;mso-height-percent:200;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal:right;mso-position-horizontal-relative:text;mso-position-vertical:absolute;mso-position-vertical-relative:text;mso-width-percent:400;mso-height-percent:200;mso-width-relative:margin;mso-height-relative:margin;v-text-anchor:top");
	    execute( wordMLPackage);
	}
	
	private static P createContent(String textContent) {
		
		P p = Context.getWmlObjectFactory().createP(); 

	    R r = Context.getWmlObjectFactory().createR(); 
	    p.getContent().add( r); 
	        // Create object for t (wrapped in JAXBElement) 
	        Text text = Context.getWmlObjectFactory().createText(); 
	        JAXBElement<org.docx4j.wml.Text> textWrapped = Context.getWmlObjectFactory().createRT(text); 
	        r.getContent().add( textWrapped); 
	            text.setValue( textContent); 
	            
	            return p;
	}
	
	private static R addFiller() {
		

	    R r = Context.getWmlObjectFactory().createR(); 
	        // Create object for t (wrapped in JAXBElement) 
	        Text text = Context.getWmlObjectFactory().createText(); 
	        JAXBElement<org.docx4j.wml.Text> textWrapped = Context.getWmlObjectFactory().createRT(text); 
	        r.getContent().add( textWrapped); 
	            text.setValue( "The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. The cat sat on the mat. "); 
	            
	            return r;
	}

	private static JAXBElement createPict(boolean w10WrapEl, String style, P textboxContent) {

		org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();

		Pict pict = wmlObjectFactory.createPict(); 
		JAXBElement<org.docx4j.wml.Pict> pictWrapped = wmlObjectFactory.createRPict(pict); 
		org.docx4j.vml.ObjectFactory vmlObjectFactory = new org.docx4j.vml.ObjectFactory();
		
		    // Create object for shapetype (wrapped in JAXBElement) 
		    CTShapetype shapetype = vmlObjectFactory.createCTShapetype(); 
		    JAXBElement<org.docx4j.vml.CTShapetype> shapetypeWrapped = vmlObjectFactory.createShapetype(shapetype); 
		    pict.getAnyAndAny().add( shapetypeWrapped); 
		        shapetype.setInsetmode(org.docx4j.vml.officedrawing.STInsetMode.CUSTOM);
		        shapetype.setSpt( new Float(202.0) );
		        shapetype.setConnectortype(org.docx4j.vml.officedrawing.STConnectorType.STRAIGHT);
		        // Create object for stroke (wrapped in JAXBElement) 
		        CTStroke stroke = vmlObjectFactory.createCTStroke(); 
		        JAXBElement<org.docx4j.vml.CTStroke> strokeWrapped = vmlObjectFactory.createStroke(stroke); 
		        shapetype.getEGShapeElements().add( strokeWrapped); 
		            stroke.setJoinstyle(org.docx4j.vml.STStrokeJoinStyle.MITER);
		        // Create object for path (wrapped in JAXBElement) 
		        CTPath path = vmlObjectFactory.createCTPath(); 
		        JAXBElement<org.docx4j.vml.CTPath> pathWrapped = vmlObjectFactory.createPath(path); 
		        shapetype.getEGShapeElements().add( pathWrapped); 
		            path.setGradientshapeok(org.docx4j.vml.STTrueFalse.T);
		            path.setConnecttype(org.docx4j.vml.officedrawing.STConnectType.RECT);
		        shapetype.setCoordsize( "21600,21600"); 
		        shapetype.setVmlId( "_x0000_t202"); 
		        shapetype.setHralign(org.docx4j.vml.officedrawing.STHrAlign.LEFT);
		        shapetype.setPath( "m,l,21600r21600,l21600,xe"); 
		        
		    // Create object for shape (wrapped in JAXBElement) 
		    CTShape shape = vmlObjectFactory.createCTShape(); 
		    JAXBElement<org.docx4j.vml.CTShape> shapeWrapped = vmlObjectFactory.createShape(shape); 
		    
		    pict.getAnyAndAny().add( shapeWrapped);
		    
		        shape.setStyle( style); 
		        shape.setSpid( "_x0000_s1026"); 
		        shape.setInsetmode(org.docx4j.vml.officedrawing.STInsetMode.CUSTOM);
		        shape.setConnectortype(org.docx4j.vml.officedrawing.STConnectorType.STRAIGHT);
		        
		        // Create object for textbox (wrapped in JAXBElement) 
		        CTTextbox textbox = vmlObjectFactory.createCTTextbox(); 
		        JAXBElement<org.docx4j.vml.CTTextbox> textboxWrapped = vmlObjectFactory.createTextbox(textbox); 
		        shape.getPathOrFormulasOrHandles().add( textboxWrapped); 
		            textbox.setStyle( "mso-fit-shape-to-text:t"); 
		            textbox.setInsetmode(org.docx4j.vml.officedrawing.STInsetMode.CUSTOM);
		            // Create object for txbxContent
		            CTTxbxContent txbxcontent = wmlObjectFactory.createCTTxbxContent(); 
		            textbox.setTxbxContent(txbxcontent); 
		            
		                txbxcontent.getContent().add( textboxContent); 
		                
		        if (w10WrapEl) {
		        	// Add <w10:wrap type="square"/>

//				    org.docx4j.vml.wordprocessingDrawing.CTWrap w10CTWrap = w10ObjectFactory.createCTWrap();
//				    w10CTWrap.setType(STWrapType.SQUARE);
//					JAXBElement<org.docx4j.vml.wordprocessingDrawing.CTWrap> w10Wrap = w10ObjectFactory.createWrap(w10CTWrap); 
//					shape.getPathOrFormulasOrHandles().add( w10Wrap);
					
	                org.docx4j.vml.wordprocessingDrawing.ObjectFactory vmlwordprocessingDrawingObjectFactory = new org.docx4j.vml.wordprocessingDrawing.ObjectFactory();
			        // Create object for wrap (wrapped in JAXBElement) 
			        CTWrap wrap = vmlwordprocessingDrawingObjectFactory.createCTWrap(); 
			        JAXBElement<org.docx4j.vml.wordprocessingDrawing.CTWrap> wrapWrapped = vmlwordprocessingDrawingObjectFactory.createWrap(wrap); 
			        shape.getPathOrFormulasOrHandles().add( wrapWrapped); 
			        wrap.setType(org.docx4j.vml.wordprocessingDrawing.STWrapType.TOP_AND_BOTTOM);					
		        }


		        shape.setVmlId( "Text Box 2"); 
		        shape.setHralign(org.docx4j.vml.officedrawing.STHrAlign.LEFT);
		        shape.setType( "#_x0000_t202"); 
		        // <w10:wrap type="topAndBottom"/>
		return pictWrapped;
		}

	private static WordprocessingMLPackage createTextBoxDocx(boolean w10WrapEl, String style) throws InvalidFormatException {
		
		// speedup
//		String regex = ".*(calibri|cour|arial|times|comic|georgia|impact|LSANS|pala|tahoma|trebuc|verdana|symbol|webdings|wingding).*";
		String regex = ".*(calibri|cour|arial).*";
		PhysicalFonts.setRegex(regex);		

		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
		MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();
		
	    P p = new P();
	    mdp.getContent().add(p);
	    
	    R r = Context.getWmlObjectFactory().createR(); 
	    
	    r.getContent().add(
	    		
	    		// Absolute position to the right of column produces:

				// margin-left:108pt
				// mso-position-horizontal:absolute <------------
				// mso-position-horizontal-relative:text
		    	// mso-wrap-style:square

		// style="position:absolute;margin-left:108pt;margin-top:0;width:186.95pt;height:110.55pt;z-index:251659264;visibility:visible;mso-wrap-style:square;mso-width-percent:400;mso-height-percent:200;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal:absolute;mso-position-horizontal-relative:text;mso-position-vertical:absolute;mso-position-vertical-relative:text;mso-width-percent:400;mso-height-percent:200;mso-width-relative:margin;mso-height-relative:margin;v-text-anchor:top" 
	    		createPict(w10WrapEl, style,
	    				createContent("text box content text box content text box content text box content text box content text box content text box content text box content text box content text box content text box content text box content text box content text box content text box content text box content ")));

	    
	    
	    		// Relative position is done in percentages..

				//  mso-left-percent:600
				//  mso-position-horizontal-relative:left-margin-area
				//  mso-width-relative:margin
	    
//	    style="position:absolute;margin-left:0;margin-top:0;width:186.95pt;height:110.55pt;z-index:251659264;visibility:visible;mso-wrap-style:square;mso-width-percent:400;mso-height-percent:200;mso-left-percent:600;mso-wrap-distance-left:9pt;mso-wrap-distance-top:0;mso-wrap-distance-right:9pt;mso-wrap-distance-bottom:0;mso-position-horizontal-relative:left-margin-area;mso-position-vertical:absolute;mso-position-vertical-relative:text;mso-width-percent:400;mso-height-percent:200;mso-left-percent:600;mso-width-relative:margin;mso-height-relative:margin;v-text-anchor:top" 
	    

	    
	    p.getContent().add( r); 
	    
	    p.getContent().add(addFiller());
	    
	    return wordMLPackage;
		
	}
	
	private static void execute(WordprocessingMLPackage wordMLPackage) throws Docx4JException, IOException {
		
	   	// Pretty print the main document part
		System.out.println(
				XmlUtils.marshaltoString(wordMLPackage.getMainDocumentPart().getJaxbElement(), true, true) );
		
		// Optionally save it
		if (save) {
			String filename = System.getProperty("user.dir") + "/OUT_CreateWordprocessingMLDocument.docx";
			wordMLPackage.save(new File(filename) );
			System.out.println("Saved " + filename);
		}
		
		// FO
		if (fo) {
			
			OutputStream baos = new ByteArrayOutputStream();
			Exporter<FOSettings> exporter = FOExporterVisitor.getInstance();
			FOSettings settings = new FOSettings();
			settings.setWmlPackage(wordMLPackage);
			settings.setApacheFopMime(FOSettings.INTERNAL_FO_MIME);
			exporter.export(settings, baos);
			
			System.out.println( ((ByteArrayOutputStream) baos).toString("UTF-8"));
			
		} else {
			
			Docx4J.toPDF(wordMLPackage, 
					FileUtils.openOutputStream(new File(System.getProperty("user.dir") + "/OUT_textbox.pdf")));
			
		}		
	}
	
}
