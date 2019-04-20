package org.docx4j.convert.out.pdf.viaIText;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.Body;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

public class Conversion extends org.docx4j.convert.out.pdf.PdfConversion {
	
	protected static Logger log = LoggerFactory.getLogger(Conversion.class);	
	
	public Conversion(WordprocessingMLPackage wordMLPackage) {
		super(wordMLPackage);
		
		headerFooterPolicy = wordMLPackage.getHeaderFooterPolicy();
	}
		
    // iText style modifiers
    // see core.lowagie.text.pdf.BaseFont
    public final static String BOLD   = ",Bold";
    public final static String ITALIC = ",Italic";
    public final static String BOLD_ITALIC = ",BoldItalic";

    public final static int DEFAULT_FONT_SIZE=11;

	Map<String, BaseFont> baseFonts = new HashMap<String, BaseFont>();
	
	HeaderFooterPolicy headerFooterPolicy;
	
	Document pdfDoc;
    
	/** Create a pdf version of the document, using XSL FO. 
	 * 
	 * @param os
	 *            The OutputStream to write the pdf to 
	 * 
	 * */    
	@Override	
    public void output(OutputStream os, PdfSettings settings) throws Docx4JException {
    	
//    	Document pdfDoc = new Document();
    	pdfDoc = new Document(PageSize.A4, 50, 50, 70, 70);
    		// TODO - use document size settings in sectPr
    	
    	try {
    		
    		PdfWriter writer = PdfWriter.getInstance(pdfDoc, os);
            writer.setPageEvent(new EndPage());
    		pdfDoc.open();
    		
    		// Set up fonts
    		Map fontsInUse = wordMLPackage.getMainDocumentPart().fontsInUse();
    		Iterator fontMappingsIterator = fontsInUse.entrySet().iterator();
    		while (fontMappingsIterator.hasNext()) {
    		    Map.Entry pairs = (Map.Entry)fontMappingsIterator.next();
    		    if(pairs.getKey()==null) {
    		    	log.info("Skipped null key");
    		    	pairs = (Map.Entry)fontMappingsIterator.next();
    		    }
    		    
    		    String fontName = (String)pairs.getKey();		    
    		    
    		    PhysicalFont pf = wordMLPackage.getFontMapper().getFontMappings().get(fontName);
    		    
    		    if (pf==null) {
    		    	log.error("Document font " + fontName + " is not mapped to a physical font!");
    		    	continue;
    		    }

    		    BaseFont bf = BaseFont.createFont(pf.getEmbeddedFile(),
    		    		BaseFont.IDENTITY_H, 
						BaseFont.NOT_EMBEDDED);
    		    baseFonts.put(fontName, bf);
    		    
    		    // bold, italic etc
    		    PhysicalFont pfVariation = PhysicalFonts.getBoldForm(pf);
    		    if (pfVariation!=null) {
        		    bf = BaseFont.createFont(pfVariation.getEmbeddedFile(),
        		    		BaseFont.IDENTITY_H, 
    						BaseFont.NOT_EMBEDDED);
        		    baseFonts.put(fontName+BOLD, bf);
    		    }
    		    pfVariation = PhysicalFonts.getBoldItalicForm(pf);
    		    if (pfVariation!=null) {
        		    bf = BaseFont.createFont(pfVariation.getEmbeddedFile(),
        		    		BaseFont.IDENTITY_H, 
    						BaseFont.NOT_EMBEDDED);
        		    baseFonts.put(fontName+BOLD_ITALIC, bf);
    		    }
    		    pfVariation = PhysicalFonts.getItalicForm(pf);
    		    if (pfVariation!=null) {
        		    bf = BaseFont.createFont(pfVariation.getEmbeddedFile(),
        		    		BaseFont.IDENTITY_H, 
    						BaseFont.NOT_EMBEDDED);
        		    baseFonts.put(fontName+ITALIC, bf);
    		    }    			    
    		}
    		
    		
    		org.docx4j.wml.Document wmlDocumentEl 
    			= wordMLPackage.getMainDocumentPart().getJaxbElement();
    		Body body =  wmlDocumentEl.getBody();
    		List <Object> bodyChildren = body.getEGBlockLevelElts();
    		
    		traverseBlockLevelContent( bodyChildren, pdfDoc );    		
        	  
    	} catch (Exception e) {
    		throw new Docx4JException("iTextissues", e);
    	} finally {
			// Clean-up
			try {
				pdfDoc.close();
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
		
	}
    
	void traverseBlockLevelContent(List <Object> children, Object parent) throws Exception {

		
		for (Object o : children ) {
						
			log.debug("object: " + o.getClass().getName() );
			
			// TODO - handle tables (tbl)
			
			if (o instanceof org.docx4j.wml.P) {
				
				org.docx4j.wml.P p = (org.docx4j.wml.P) o;
		
//				if (p.getPPr() != null && p.getPPr().getPStyle() != null) {
//				}
		
//				if (p.getPPr() != null && p.getPPr().getRPr() != null) {
//				}
		
				Paragraph pdfParagraph = new Paragraph();
				getRunContent( p.getParagraphContent(), parent, pdfParagraph);
				
				if (parent instanceof Document) {				
					((Document)parent).add(pdfParagraph);
				} else if (parent instanceof PdfPTable) {
					
					((PdfPTable)parent).addCell(
							new PdfPCell(pdfParagraph)
					);
				} else {
					log.error("Trying to add paragraph to " + parent.getClass().getName() );
				}
		
			} else if (o instanceof org.docx4j.wml.SdtBlock) {

				org.docx4j.wml.SdtBlock sdt = (org.docx4j.wml.SdtBlock) o;				
				// Don't bother looking in SdtPr				
				traverseBlockLevelContent(sdt.getSdtContent().getContent(),
						parent);
				
//			} else if (o instanceof org.docx4j.wml.SdtContentBlock) {
//
//				org.docx4j.wml.SdtBlock sdt = (org.docx4j.wml.SdtBlock) o;
//				
//				// Don't bother looking in SdtPr
//				
//				traverseMainDocumentRecursive(sdt.getSdtContent().getEGContentBlockContent(),
//						fontsDiscovered, stylesInUse);
				
			} else if (o instanceof org.w3c.dom.Node) {
				
				// If Xerces is on the path, this will be a org.apache.xerces.dom.NodeImpl;
				// otherwise, it will be com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
				
				// Ignore these, eg w:bookmarkStart
				
				log.debug("not traversing into unhandled Node: " + ((org.w3c.dom.Node)o).getNodeName() );
				
			} else if ( o instanceof javax.xml.bind.JAXBElement) {

				log.debug( "Encountered " + ((JAXBElement) o).getDeclaredType().getName() );
					
//				if (((JAXBElement) o).getDeclaredType().getName().equals(
//						"org.docx4j.wml.P")) {
//					org.docx4j.wml.P p = (org.docx4j.wml.P) ((JAXBElement) o)
//							.getValue();
				
			} else {
				log.error( "UNEXPECTED: " + o.getClass().getName() );
			} 
		}
	}

	void getRunContent(List<Object> children, Object paraParent,
			Paragraph pdfParagraph) throws Exception {

		for (Object o : children) {
			log.debug("object: " + o.getClass().getName());

			if (o instanceof org.docx4j.wml.R) {

				Font font = null;					
				org.docx4j.wml.R run = (org.docx4j.wml.R) o;
				if (run.getRPr() != null) {

					// TODO - follow inheritance
					
					Font iTextFont = null;
					
					RPr rPr = run.getRPr();
					RFonts rFonts = rPr.getRFonts();
					String documentFont = Mapper.FONT_FALLBACK; 
					if (rFonts !=null ) {
						
						documentFont = rFonts.getAscii();
						
						if (documentFont==null) {
							// TODO - actually what Word does in this case
							// is inherit the default document font eg Calibri
							// (which is what it shows in its user interface)
							documentFont = rFonts.getCs();
						}
						
						if (documentFont==null) {
                            if(log.isErrorEnabled() {
                                log.error("Font was null in: " + XmlUtils.marshaltoString(rPr, true, true));
                            }
							documentFont=Mapper.FONT_FALLBACK;
						}
						
						log.info("Font: " + documentFont);
						
					}
					
					int fontSize = DEFAULT_FONT_SIZE;
					if (run.getRPr().getSz() != null) {
						org.docx4j.wml.HpsMeasure hps = run.getRPr().getSz();
						fontSize = hps.getVal().intValue() / 2;
					} 

					if (run.getRPr().getB() != null
							&& run.getRPr().getB().isVal()
							&& run.getRPr().getI() != null
							&& run.getRPr().getI().isVal()) {
						
						if ( baseFonts.get(documentFont + BOLD_ITALIC)!=null) {						
							log.debug(documentFont + BOLD_ITALIC + " not found; falling back to " + documentFont);
							font = new Font( baseFonts.get(documentFont + BOLD_ITALIC), fontSize  );
						} else {
							font = new Font( baseFonts.get(documentFont), fontSize  );							
						}
						
					} else if (run.getRPr().getI() != null
							&& run.getRPr().getI().isVal()) {
						
						if ( baseFonts.get(documentFont + ITALIC)!=null) {						
							log.debug(documentFont + ITALIC + " not found; falling back to " + documentFont);
							font = new Font( baseFonts.get(documentFont + ITALIC), fontSize  );
						} else {
							font = new Font( baseFonts.get(documentFont), fontSize  );							
						}
					} else if (run.getRPr().getB() != null
							&& run.getRPr().getB().isVal()) {
						
						if ( baseFonts.get(documentFont + BOLD)!=null) {						
							log.debug(documentFont + BOLD + " not found; falling back to " + documentFont);
							font = new Font( baseFonts.get(documentFont + BOLD), fontSize  );
						} else {
							font = new Font( baseFonts.get(documentFont), fontSize  );							
						}
					} else {
						font = new Font( baseFonts.get(documentFont), fontSize  );						
					}
					
				}

				List<Object> runContent = run.getRunContent();

				for (Object rc : runContent) {

					if (rc instanceof javax.xml.bind.JAXBElement) {

						log.debug("Encountered "
								+ ((JAXBElement) rc).getDeclaredType()
										.getName());

						if (((JAXBElement) rc).getDeclaredType().getName()
								.equals("org.docx4j.wml.Text")) {

							org.docx4j.wml.Text t = (org.docx4j.wml.Text) ((JAXBElement) rc)
									.getValue();

							if (font == null) {
								pdfParagraph.add(new Chunk(t.getValue()));
							} else {
								pdfParagraph.add(new Chunk(t.getValue(), font));
							}
							log.debug("Added content " + t.getValue());
						} else if (((JAXBElement) rc).getDeclaredType()
								.getName().equals("org.docx4j.wml.Drawing")) {

							org.docx4j.wml.Drawing drawing = (org.docx4j.wml.Drawing) ((JAXBElement) rc)
									.getValue();
							addDrawing(drawing, pdfDoc, pdfParagraph);

						} else {
							log.debug("What? Encountered "
									+ ((JAXBElement) rc).getDeclaredType()
											.getName());
							// eg org.docx4j.wml.R$LastRenderedPageBreak
						}

					} else if (rc instanceof org.docx4j.wml.Br) {

						if (paraParent instanceof Document) {				
							((Document)paraParent).newPage();
						} else {
							log.error("Trying to add new page to " + paraParent.getClass().getName() );
						}
						

					} else {

						log.debug("found in R: " + rc.getClass().getName());
					}
				}

			// } else if (o instanceof org.docx4j.wml.Drawing) {
			//				
			// addDrawing((Drawing)o, pdfDoc, pdfParagraph);

			} else if (o instanceof org.w3c.dom.Node) {

				log.debug("not traversing into unhandled Node: "
						+ ((org.w3c.dom.Node) o).getNodeName());

			} else if (o instanceof javax.xml.bind.JAXBElement) {

				log.debug("Encountered "
						+ ((JAXBElement) o).getDeclaredType().getName());

			} else {
				log.error("UNEXPECTED: " + o.getClass().getName());
			}
		}

	}

	void addDrawing(org.docx4j.wml.Drawing o,
			Object paraParent, Paragraph pdfParagraph) throws Exception {
	
		org.docx4j.wml.Drawing drawing = (org.docx4j.wml.Drawing) o;
		List<Object> list = drawing.getAnchorOrInline();
		if (list.size() != 1
			|| !(list.get(0) instanceof Inline)) {
			//There should not be an Anchor in 'list'
			//because it is not being supported and 
			//RunML.initChildren() prevents it from
			//being assigned to this InlineDrawingML object.
			//See: RunML.initChildren().
			throw new IllegalArgumentException("Unsupported Docx Object = " + o);			
		}
		
		Inline inline = (Inline) list.get(0);
//		if (inline.getExtent() != null) {
//			int cx = Long.valueOf(inline.getExtent().getCx()).intValue();
//			cx = StyleSheet.emuToPixels(cx);
//			int cy = Long.valueOf(inline.getExtent().getCy()).intValue();
//			cy = StyleSheet.emuToPixels(cy);
//			//this.extentInPixels = new Dimension(cx, cy);
//		}
		
		if (inline.getEffectExtent() != null) {
			//this.effectExtent = new CTEffectExtent(inline.getEffectExtent());
		}
		
		if (inline.getGraphic() != null) {
			
			byte[] imagedata = BinaryPartAbstractImage.getImage(wordMLPackage,
					inline.getGraphic() );
			Image img = Image.getInstance( imagedata );
			if (paraParent instanceof Document) {				
				((Document)paraParent).add(img);
			} else if (paraParent instanceof PdfPTable) {				
				((PdfPTable)paraParent).addCell(img);
			} else {
				log.error("Trying to add image to " + paraParent.getClass().getName() );
			}
			
		}
	}

	
	class EndPage extends PdfPageEventHelper {
	    
	    
	    /**
	     * @see com.lowagie.text.pdf.PdfPageEventHelper#onEndPage(com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document)
	     */
	    public void onEndPage(PdfWriter writer, Document document) {
	        try {
	            Rectangle page = document.getPageSize();

	            if (headerFooterPolicy.getHeader(writer.getPageNumber())!=null) {
		            Hdr hdr = headerFooterPolicy.getHeader(writer.getPageNumber()).getJaxbElement();
		            PdfPTable head = new PdfPTable(1); // num cols	            
		            // TODO - no cell borders
		            traverseBlockLevelContent( hdr.getEGBlockLevelElts(), head);
		            head.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
		            head.writeSelectedRows(0, -1, document.leftMargin(), page.getHeight() - document.topMargin() + head.getTotalHeight(),
		                writer.getDirectContent());
	            }
	            
	            if (headerFooterPolicy.getFooter(writer.getPageNumber())!=null) {
		            Ftr ftr = headerFooterPolicy.getFooter(writer.getPageNumber()).getJaxbElement();	            
		            PdfPTable foot = new PdfPTable(1);
		            traverseBlockLevelContent( ftr.getEGBlockLevelElts(), foot);
		            foot.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
		            foot.writeSelectedRows(0, -1, document.leftMargin(), document.bottomMargin(),
		                writer.getDirectContent());
	            }
	        }
	        catch (Exception e) {
	            throw new ExceptionConverter(e);
	        }
	    }

	}
	
	
	

	    }
    
