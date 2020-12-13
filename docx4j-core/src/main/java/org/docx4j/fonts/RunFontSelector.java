package org.docx4j.fonts;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.properties.Property;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.PPr;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STHint;
import org.docx4j.wml.Style;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

//import com.vdurmont.emoji.EmojiManager;

import java.awt.font.NumericShaper;
import java.util.concurrent.ExecutionException;

/**
 * Apply the appropriate font to the characters in the run,
 * following the rules specified in
 * http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/rFonts.html
 * and http://msdn.microsoft.com/en-us/library/ff533743.aspx
 * ([MS-OI29500] 2.1.87)
 * 
 * See also http://blogs.msdn.com/b/officeinteroperability/archive/2013/04/22/office-open-xml-themes-schemes-and-fonts.aspx
 * 
 * The ASCII font formats all characters in the ASCII range (character values 0–127). 
 * This font is specified using the ascii attribute on the rFonts element.
 * 
 * The East Asian font formats all characters that belong to Unicode sub ranges for East Asian languages. 
 * This font is specified using the eastAsia attribute on the rFonts element.
 * 
 * The complex script font formats all characters that belong to Unicode sub ranges for complex script languages. 
 * This font is specified using the cs attribute on the rFonts element.
 * 
 * The high ANSI font formats all characters that belong to Unicode sub ranges other than those explicitly included 
 * by one of the groups above. This font is specified using the hAnsi attribute on the rFonts element.	
 * 
 * Per Tristan Davis
 * http://openxmldeveloper.org/discussions/formats/f/13/t/150.aspx
 * 
 * First, the characters are classified into the high ansi / east asian / complex script buckets [per above]
 * 
 * Next, we grab *one* theme font from the theme for each bucket - in the settings part, there's an element called themeFontLang
 * The three attributes on that specify the language to use for the characters in each bucket
 * 
 * Then you take the language specified for each attribute and look out for the right language in the theme - and you use that font
 * 
 * See also http://blogs.msdn.com/b/officeinteroperability/archive/2013/04/22/office-open-xml-themes-schemes-and-fonts.aspx
 * regarding what to do if the font is not available on the computer.
 * 
 * @author jharrop
 *
 */
public class RunFontSelector {
	
	protected static Logger log = LoggerFactory.getLogger(RunFontSelector.class);	

	private WordprocessingMLPackage wordMLPackage;
	private RunFontCharacterVisitor vis;
		
	private RunFontActionType outputType;
	public enum RunFontActionType {
		XSL_FO,
		XHTML,
		DISCOVERY
	}
	
	public RunFontSelector(WordprocessingMLPackage wordMLPackage, RunFontCharacterVisitor visitor, 
			RunFontActionType outputType) {
		
		this.wordMLPackage = wordMLPackage;
		this.vis = visitor;
		this.outputType = outputType;
				
		vis.setRunFontSelector(this);
		
		fallbackFont = getPhysicalFont(getDefaultFont());
		if (fallbackFont==null) {
			fallbackFont = getDefaultFont();
			if (outputType!= RunFontActionType.DISCOVERY) {
				log.warn(getDefaultFont() + " is not mapped!");
			}
		} 
		
		vis.setFallbackFont(fallbackFont);
		
		if (wordMLPackage.getMainDocumentPart().getDocumentSettingsPart()!=null) {
			try {
				themeFontLang = wordMLPackage.getMainDocumentPart().getDocumentSettingsPart().getContents().getThemeFontLang();
			} catch (Docx4JException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(), e);
			}
		}
		
	}
	
	String fallbackFont = null;
	
	CTLanguage themeFontLang = null;
	
	public final static String CSS_NAME = "font-family"; 
	public final static String FO_NAME  = "font-family"; 

	public String getCssName() {
		return CSS_NAME;
	}
	
	
	private ThemePart getThemePart() {
		return wordMLPackage.getMainDocumentPart().getThemePart();
	}
	
	private Style defaultParagraphStyle;
	
    private Style getDefaultPStyle() {
    	
    	if (defaultParagraphStyle==null) {
			defaultParagraphStyle = 
					(wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart(false) != null ?
							wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart(false).getDefaultParagraphStyle() :
					null);
    	}
		return defaultParagraphStyle;
    }
    
    
    private String defaultFont = null;
	public String getDefaultFont() {
		
		if (defaultFont == null) {
			
	    	PropertyResolver propertyResolver = wordMLPackage.getMainDocumentPart().getPropertyResolver();
			
			org.docx4j.wml.RFonts rFonts = propertyResolver.getDocumentDefaultRPr().getRFonts();
		
			if (rFonts==null) {
				log.info("No styles/docDefaults/rPrDefault/rPr/rFonts - default to Times New Roman");
				// Yes, Times New Roman is still buried in Word 2007
				defaultFont = "Times New Roman"; 						
			} else {						
				// Usual case
				if (rFonts.getAsciiTheme()==null ) {
					
					if (rFonts.getAscii()==null ) {
						// TODO
						log.error("Neither ascii or asciTheme.  What to do? ");
						defaultFont = "Times New Roman"; 						
						
					} else {
						log.info("rPrDefault/rFonts referenced " + rFonts.getAscii());								
						defaultFont = rFonts.getAscii(); 							
					}	
					
				} else {
					if (getThemePart()==null) {
						// No theme part - default to Calibri
						log.info("No theme part - default to Calibri");
						defaultFont= "Calibri"; 
					} else {
						String font=null;
						try {
							font = getThemePart().getFont(rFonts.getAsciiTheme(), themeFontLang);
						} catch (Docx4JException e) {
							// TODO Auto-generated catch block
							log.error(e.getMessage(), e);
						}
						if (font!=null) {
							defaultFont= font; 
						} else {
								// No minorFont/latin in theme part - default to Calibri
								log.info("No minorFont/latin in theme part - default to Calibri");								
								defaultFont= "Calibri"; 
						}
					}
				}  				
			} 
		}
//		System.out.println("!" + defaultFont);
		return defaultFont;
	}
	
	
    private DocumentFragment nullRPr(Document document, String text) {
    	
		if (outputType== RunFontActionType.DISCOVERY) {
			vis.fontAction(getDefaultFont());
			return null;
		} 

		// TODO: At present, we set a font on each and every span; 
		// if we set a default on eg body, this wouldn't be necessary.
		// Similarly for the FO case.
		Element	span = createElement(document);
		if (span!=null) {
			document.appendChild(span);  
			this.setAttribute(span, getDefaultFont());
			span.setTextContent(text);  
		}
		
		return result(document);
    }
    
    private DocumentFragment result(Document document) {
    	
		if (outputType== RunFontActionType.DISCOVERY) {
			/* Avoid
			 * 
				Exception in thread "main" java.lang.NullPointerException
					at com.sun.org.apache.xerces.internal.dom.ParentNode.internalInsertBefore(Unknown Source)
					at com.sun.org.apache.xerces.internal.dom.ParentNode.insertBefore(Unknown Source)
					at com.sun.org.apache.xerces.internal.dom.NodeImpl.appendChild(Unknown Source)
					at org.docx4j.fonts.RunFontSelector.result(RunFontSelector.java:202)
					at org.docx4j.fonts.RunFontSelector.fontSelector(RunFontSelector.java:366)
			 */
			return null;
		}
		DocumentFragment docfrag = document.createDocumentFragment();
		docfrag.appendChild(document.getDocumentElement());
		return docfrag;
    }
    

    public Element createElement(Document document) {
    	Element el=null;
		if (outputType== RunFontActionType.DISCOVERY) {
			return null;
		} else if (outputType==RunFontActionType.XHTML) {
    		 el = document.createElement("span");
    	} else if (outputType==RunFontActionType.XSL_FO) {
    		el = document.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:inline");
    	} 
		/* Can't do document.appendChild(el) here, since its a problem if called multiple times!
		 * 
			org.w3c.dom.DOMException: HIERARCHY_REQUEST_ERR: An attempt was made to insert a node where it is not permitted. 
				at com.sun.org.apache.xerces.internal.dom.CoreDocumentImpl.insertBefore(Unknown Source)
				at com.sun.org.apache.xerces.internal.dom.NodeImpl.appendChild(Unknown Source)
				at org.docx4j.fonts.RunFontSelector.createElement(RunFontSelector.java:205)
				at org.docx4j.convert.out.fo.FOConversionContext$3.createNew(FOConversionContext.java:139)
				at org.docx4j.fonts.RunFontSelector.unicodeRangeToFont(RunFontSelector.java:462)
				at org.docx4j.fonts.RunFontSelector.fontSelector(RunFontSelector.java:428)
				at org.docx4j.convert.out.common.XsltCommonFunctions.fontSelector(XsltCommonFunctions.java:117)
			 */
    	return el;
    }
    
    public void setAttribute(Element el, String fontName) {
    	
    	// could a document fragment contain just a #text node?
    	
		if (outputType== RunFontActionType.DISCOVERY) {
			return;
		} else if (outputType==RunFontActionType.XHTML) {
    		if (spacePreserve) {
    	    	/*
    	    	 * 	Convert @xml:space='preserve' to style="white-space:pre-wrap;"
    				which is good for FF3, and WebKit; not honoured by IE7 though. 
    	    	 */
    			el.setAttribute("style", getCssProperty(fontName) + "white-space:pre-wrap;");
    			
    		} else {
    			el.setAttribute("style", getCssProperty(fontName));
    		}
    	} else if (outputType==RunFontActionType.XSL_FO) {
    		String val = getPhysicalFont(fontName);
    		if (val==null) {
    			// Avoid @font-family="", which FOP doesn't like
    			el.setAttribute("font-family", fallbackFont );
    		} else {	
    			el.setAttribute("font-family", val );
    		}
    		
			// NB, for PDF/FOP, white space handling on the parent fo:block, 
    		// see XsltFOFunctions (for XSLT), and AbstractVisitorExporterGenerator (non XSLT)
    		
//    		if (spacePreserve) {
//    			el.setAttribute("white-space-treatment","preserve");
//    		}
    		// NB, that on its own may stop FOP 1.x from line wrapping!
    		
    	} 
    }
    
    private boolean spacePreserve;
    

    /**
     * Apply font selection algorithm to this Text, based on supplied PPr, RPr
     * (and docDefaults, Theme part etc).
     * 
     * @param pPr
     * @param rPr
     * @param wmlText
     * @return
     */
    public Object fontSelector(PPr pPr, RPr rPr, Text wmlText) {

    	String text=null;
    	if (wmlText==null) {
    		log.debug("Null Text object");
    	} else {
    		text = wmlText.getValue();
        	spacePreserve = (wmlText.getSpace()!=null) && (wmlText.getSpace().equals("preserve"));
    	}
    	
    	return fontSelector( pPr,  rPr,  text);
    }
    
    /**
     * Apply font selection algorithm to this Text, based on supplied PPr, RPr
     * (and docDefaults, Theme part etc).
     * 
     * @param pPr
     * @param rPr
     * @param wmlText
     * @return
     */
    public Object fontSelector(PPr pPr, RPr rPr, String text) {
    	
    	if (text==null) {
    		log.debug("w:t with null value"); 
    		if (outputType!= RunFontActionType.DISCOVERY) {
    			return null;
    		} // otherwise a font might be used in a run with content other than w:t?
    	} else {
    		log.debug(text);
    	}
    	
    	PropertyResolver propertyResolver = wordMLPackage.getMainDocumentPart().getPropertyResolver();
    	
//    	Style pStyle = null;
    	String pStyleId = null;
    	RPr pRPr = null;
    	if (pPr==null || pPr.getPStyle()==null) {
//    		pStyle = getDefaultPStyle(); 
    		if (getDefaultPStyle() == null) {
    			log.warn("getDefaultPStyle() returned null");
    		} else {
//	        	log.debug("using default p style");
//	        	pRPr = pStyle.getRPr();  // TODO pStyle.getRPr() should inherit from basedOn
	        	pStyleId = getDefaultPStyle().getStyleId();
    		}
    	} else {
    		pStyleId = pPr.getPStyle().getVal();
    	}
    		
    	if (pStyleId!=null && wordMLPackage.getMainDocumentPart().getStyleDefinitionsPart(false) != null) {
    		// apply the rPr in the stack of styles, including documentDefaultRPr
//    		log.debug(pStyleId);
    		pRPr = propertyResolver.getEffectiveRPr(pStyleId);
//        	log.debug("before getEffectiveRPrUsingPStyleRPr\n" + XmlUtils.marshaltoString(pRPr));
    	}

    	// Do we need boolean major??
    	// Can work that out from pStyle

    	
    	// now apply the direct rPr
    	rPr = propertyResolver.getEffectiveRPrUsingPStyleRPr(rPr, pRPr); 
    	// TODO use effective rPr, but don't inherit theme val,
    	// TODO, add cache?

    	if(log.isDebugEnabled()) {
            log.debug("effective\n" + XmlUtils.marshaltoString(rPr));
        }
    	
    	/* eg
    	 * 
				<w:r>
				  <w:rPr>
				    <w:rFonts w:ascii="Courier New" w:cs="Times New Roman" />
				  </w:rPr>
				  <w:t>English العربية</w:t>
				</w:r>
				
    	 */

		Document document = XmlUtils.getNewDocumentBuilder().newDocument();
		
		// No rPr .. only happens if no documentDefaultRPr
		if (rPr==null) {
			
			log.warn("effective rPr is null");
			return nullRPr(document, text);
		}
		
//		System.out.println(XmlUtils.marshaltoString(rPr, true, true));
		
		
		RFonts rFonts = rPr.getRFonts();
		if (rFonts==null) // compare empty, which RunFontSelectorChinese2Test is sensitive to; with empty on a quick skim it looks like unicodeRangeToFont is used. 
		{
			return nullRPr(document, text);
		}		
    	
		if (pPr!=null && pPr.getBidi()!=null && pPr.getBidi().isVal() ) {
			text = this.arabicNumbering(text, rPr.getRtl(), rPr.getCs(), themeFontLang);
		}
		
    	/* If the run has the cs element ("[ISO/IEC-29500-1] §17.3.2.7; cs") 
    	 * or the rtl element ("[ISO/IEC-29500-1] §17.3.2.30; rtl"), 
    	 * then the cs (or cstheme if defined) font is used, 
    	 * regardless of the Unicode character values of the run's content.
    	 */
    	if (rPr.getCs()!=null || rPr.getRtl()!=null ) {
    		    		
    		// use the cs (or cstheme if defined) font is used
    		if (rFonts.getCstheme()!=null) {
    			
    			String fontName = null; 
    			if (getThemePart()!=null) {
    				
    				try {
						fontName = getThemePart().getFont(rFonts.getCstheme(), themeFontLang);
					} catch (Docx4JException e) {
						// TODO Auto-generated catch block
						log.error(e.getMessage(), e);
					}
    			}
    			if (fontName==null
//    					|| fontName.trim().length()==0
    					) {
    				fontName = rFonts.getCs();
    			} 
    			if (fontName==null
//    					|| fontName.trim().length()==0
    					) {
    				// then what?
                    if(log.isWarnEnabled()) {
                        log.warn("font name is null, for " + text);
                        log.warn(XmlUtils.marshaltoString(rPr, true, true));
                    }
    				(new Throwable()).printStackTrace();
    			}    		
    			
    			Element	span = createElement(document);
    			if (span!=null) {
    				// It will be null in MainDocumentPart$FontAndStyleFinder case
	    			document.appendChild(span); 
	    			this.setAttribute(span, fontName);
	    			span.setTextContent(text);  
    			}
    			if (outputType== RunFontActionType.DISCOVERY) {
    				vis.fontAction(fontName);
    			}
    			
    			return result(document);
    			
    		} else if (rFonts.getCs()!=null) {

    			String fontName =rFonts.getCs();
    			Element	span = createElement(document);
    			if (span!=null) {
    				// It will be null in MainDocumentPart$FontAndStyleFinder case
	    			document.appendChild(span);     			
	    			this.setAttribute(span, fontName);
	    			span.setTextContent(text);
    			}
    			
    			if (outputType== RunFontActionType.DISCOVERY) {
    				vis.fontAction(fontName);
    			}
    			
    			return result(document);
    			
    		} else {
    			// No CS value.
    			// What to do?
    		}
    	}

		String eastAsia = null;
		String ascii = null;
		String hAnsi = null;
		
		STHint hint = rFonts.getHint(); 
		
		if (rFonts.getEastAsiaTheme()!=null
				&& getThemePart()!=null) {
			try {
				eastAsia = getThemePart().getFont(rFonts.getEastAsiaTheme(), themeFontLang);
			} catch (Docx4JException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(), e);
			}
			
			// ??
			//if (getPhysicalFont(eastAsia)==null) {
			//	log.info("theme font for lang " + themeFontLang + " is " + eastAsia + ", but we don't have that");
	    	//	eastAsia = rFonts.getEastAsia();
			//}
			
			if (eastAsia==null) {
				log.info("theme font for lang " + themeFontLang + " is " + eastAsia + ", but we don't have that");
	    		eastAsia = rFonts.getEastAsia();
			}
			
		} else {
			// No theme, so 
    		eastAsia = rFonts.getEastAsia();
		}
		
		if (rFonts.getAsciiTheme()!=null
				&& getThemePart()!=null) {
			try {
				ascii = getThemePart().getFont(rFonts.getAsciiTheme(), themeFontLang);
			} catch (Docx4JException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(), e);
			}
		} else {
			// No theme, so 
			ascii = rFonts.getAscii();
		}
		
		if (rFonts.getHAnsiTheme()!=null
				&& getThemePart()!=null) {
			try {
				hAnsi = getThemePart().getFont(rFonts.getHAnsiTheme(), themeFontLang);
			} catch (Docx4JException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(), e);
			}
		} else {
			// No theme, so 
			hAnsi = rFonts.getHAnsi();
		}
		
    	/*
    	 * If the eastAsia (or eastAsiaTheme if defined) attribute’s value is “Times New Roman”
    	 * and the ascii (or asciiTheme if defined) and hAnsi (or hAnsiTheme if defined) attributes are equal, 
    	 * then the ascii (or asciiTheme if defined) font is used.
    	 */
		if (("Times New Roman").equals(eastAsia)) {		
		
    		if (ascii!=null
    				&& ascii.equals(hAnsi)) {
    			// use ascii
    			
    			Element	span = createElement(document);
    			if (span!=null) {
    				// It will be null in MainDocumentPart$FontAndStyleFinder case    			
    				document.appendChild(span); 
    			}
    			
    			if (outputType== RunFontActionType.DISCOVERY) {
    				vis.fontAction(ascii);
        			return null; 
    			}
    			this.setAttribute(span, ascii);
    			span.setTextContent(text);    	
    			
    			
    			return result(document);
    			
    		}
		}
		
		if (ascii==null) {
			log.debug("No value for ascii, using default font");
			ascii = this.getDefaultFont();
		}
    		    	
    	/* Otherwise, the following table is used. For all ranges not listed in the following table, 
    	 * the hAnsi (or hAnsiTheme if defined) font shall be used.
    	 */
		if (hAnsi==null) {
			log.debug("No value for hAnsi, using default font");
			hAnsi = this.getDefaultFont();				
		}
		
		String langEastAsia = null;
		if (rPr.getLang()!=null) {
			langEastAsia = rPr.getLang().getEastAsia();
		}
		
		vis.setDocument(document);
		return unicodeRangeToFont(text,  hint,  langEastAsia,
	    		 eastAsia,  ascii,  hAnsi );
    }
    
    private boolean contains(String langEastAsia, String lang) {
    	
    	// eg <w:lang w:eastAsia="zh-CN" .. />
    	if (langEastAsia==null) return false;
    	
    	return langEastAsia.contains(lang);
    }

	private static String EMOJI_FONT=null;
	private static String getEmojiFont() {
		
		if (EMOJI_FONT==null) {
			EMOJI_FONT = Docx4jProperties.getProperty("docx4j.fonts.RunFontSelector.EmojiFont");
		}
		return EMOJI_FONT;
	}
    
    private Object unicodeRangeToFont(String text, STHint hint, String langEastAsia,
    		String eastAsia, String ascii, String hAnsi) {
    	
//    	String hAnsi = hAnsiActual;
//		if (hAnsi==null) {
//			log.debug("No value for hAnsi, using default font");
//			hAnsi = this.getDefaultFont();				
//		}
    	
    	// See http://stackoverflow.com/questions/196830/what-is-the-easiest-best-most-correct-way-to-iterate-through-the-characters-of-a
    	// and http://stackoverflow.com/questions/8894258/fastest-way-to-iterate-over-all-the-chars-in-a-string
    	
    	// The ranges specified at http://msdn.microsoft.com/en-us/library/ff533743.aspx
    	// are from 0000-FFFF, 
    	// but we do also handle astral characters (those outside Unicode Basic Multilingual Plane)
    	
    	char currentRangeLower='\u0000';
    	char currentRangeUpper='\u0000';
    	    	
    	if (text==null) {
    		return null; 
    	}
    	for (int i = 0; i < text.length(); i=text.offsetByCodePoints(i, 1)){
    		
    	    char c = text.charAt(i);
//    		int cp = text.codePointAt(i);
    	    
    	    if (Character.isHighSurrogate(c)) {
    	    	
    	    	// Populate previous span
    	    	vis.finishPrevious();
    	    	
    	    	// Create new span
    		    vis.createNew();
    		    vis.setMustCreateNewFlag(false);
    		    
    		    // Set the font
    		    if (getEmojiFont()==null) {
    		    	// Default
    		    	vis.fontAction(hAnsi);
    		    } else {
    		    	
    		    	// we know what to do with an emoji
    		    	
    		    	// Is it an emoji?  
        	    	/* Use this in next major release, or
        	    	 * better, TODO, use via reflection if present
	       	    	 *
	       	    	 * 		<dependency>
								  <groupId>com.vdurmont</groupId>
								  <artifactId>emoji-java</artifactId>
								  <version>5.1.1</version>
								</dependency>
	
	       	    	 * 
	       	    	if (EmojiManager.isEmoji(
	       	    			new String(
	       	    					Character.toChars(
	       	    							text.codePointAt(i))))) {
	       	    		
	       	    	}
    		    	// For now, a quick n dirty check
	       	    	
	       	    	*/
        	    	if (c=='\uD83D' || c=='\uD83D' || c=='\uD83E') {
        	    		
        	    		log.debug("assuming emoji " + Integer.toHexString(c));
        		    	
        		    	try {
							if (GlyphCheck.hasChar(hAnsi, c)) {
								// TODO: doubt this works for high surrogate 
								log.debug("present in " + hAnsi);
								vis.fontAction(hAnsi);        		    		
							} else {
								vis.fontAction(getEmojiFont());        		    		        		    		
							}
						} catch (ExecutionException e) {
							log.error(e.getMessage(), e);
						}
        	    	} else {
        		    	// Default
        		    	vis.fontAction(hAnsi);        	    		
        	    	}
    		    }
				
    	    	//vis.addCharacterToCurrent(c);
				vis.addCodePointToCurrent(text.codePointAt(i));
				
				log.debug("added as code point");
    	    	
    	    	currentRangeLower='\u0000';
    	    	currentRangeUpper='\u0000';    		    
    	    }
    	    else 
    	    	
    	    	if (vis.isReusable() && 
    	    		(c==' ' ||
    	    		(c>=currentRangeLower && c<=currentRangeUpper))) {
    	    	// Add it to existing
    	    	vis.addCharacterToCurrent(c);
    	    } else {
    	    	
    	    	// Populate previous span
    	    	vis.finishPrevious();
    	    	
    	    	// Create new span
    		    vis.createNew();
    		    vis.setMustCreateNewFlag(false);
    		    
//    		    System.out.println(c);    		    
    		    
    		    /* .. Basic Latin
    		     * 
    		     * http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/rFonts.html says 
    		     * @ascii (or @asciiTheme) is used to format all characters in the ASCII range 
    		     * (0 - 127)
    		     */
        	    if (c>='\u0000' && c<='\u007F') 
        	    {
        	    	vis.fontAction(ascii); 
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\u0000';
        	    	currentRangeUpper = '\u007F';
        	    } else 
    		    // ..  Latin-1 Supplement
        	    if (c>='\u00A0' && c<='\u00FF') 
        	    {
        	    	/* hAnsi (or hAnsiTheme if defined), with the following exceptions:
    					If hint is eastAsia, the following characters use eastAsia (or eastAsiaTheme if defined): A1, A4, A7 – A8, AA, AD, AF, B0 – B4, B6 – BA, BC – BF, D7, F7
    					If hint is eastAsia and the language of the run is either Chinese Traditional or Chinese Simplified, the following characters use eastAsia (or eastAsiaTheme if defined): E0 – E1, E8 – EA, EC – ED, F2 – F3, F9 – FA, FC
    					*/

	    			if (hint == STHint.EAST_ASIA
	    					&& eastAsia !=null) {
        	    	
    	    			if ( c=='\u00A1' || c=='\u00A4' 
	    					|| (c>='\u00A7' && c<='\u00A8')         	    					
	    					|| c=='\u00AA' 
	    	    			|| c=='\u00AD' // Known issues with soft hyphen
	    					|| c=='\u00AF'          	    					
	    					|| (c>='\u00B0' && c<='\u00B4')         	    					
	    					|| (c>='\u00B6' && c<='\u00BA') 
	    					|| (c>='\u00BC' && c<='\u00BF') 
	    					|| c=='\u00D7' || c=='\u00F7' ) {

                	    		// Don't use east asia unless hint tells us to!
        	    				vis.fontAction(eastAsia);
        	    				
    	    			} else if (contains(langEastAsia, "zh") &&

        	    			// the following characters use eastAsia (or eastAsiaTheme if defined): E0 – E1, E8 – EA, EC – ED, F2 – F3, F9 – FA, FC
        	    			 ( (c>='\u00E0' && c<='\u00E1')         	    					
        	    					|| (c>='\u00E8' && c<='\u00EA')         	    					
        	    					|| (c>='\u00EC' && c<='\u00ED')         	    					
        	    					|| (c>='\u00F2' && c<='\u00F3')         	    					
        	    					|| (c>='\u00F9' && c<='\u00FA') 
        	    					|| c=='\u00FC'))  {
        	    				vis.fontAction(eastAsia);
     	    				
    	    			}  else if (hAnsi!=null) {
        	    			vis.fontAction(hAnsi);
        	    				
        	    		} else {
                			vis.fontAction(getDefaultFont());
    	    			}  
        	    		
        	    	} else if (hAnsi!=null) {        	    		
	    				vis.fontAction(hAnsi);

        	    	} else {

        	    		// .. Ignore ascii and east Asia 
        				vis.fontAction(getDefaultFont());
        	    		
        	    	}
        	    	
        	    	vis.addCharacterToCurrent(c);
        		    vis.setMustCreateNewFlag(false);
        	    	
        	    	currentRangeLower = '\u0000';
        	    	currentRangeUpper = '\u007F';
        	    } else 
    		    // ..  Latin Extended-A, Latin Extended-B, IPA Extensions
        	    if (c>='\u0100' && c<='\u02AF') 
        	    {
        	    	/* hAnsi (or hAnsiTheme if defined), with the following exception:
    					If hint is eastAsia, and the language of the run is either Chinese Traditional or Chinese Simplified, 
    					or the character set of the eastAsia (or eastAsiaTheme if defined) font is Chinese5 or GB2312 
    					then eastAsia (or eastAsiaTheme if defined) font is used.
    					*/
        	    	if (hint == STHint.EAST_ASIA) {
        	    		if (contains(langEastAsia, "zh") ) {
    	    				vis.fontAction(eastAsia);
    	    			    vis.setMustCreateNewFlag(true);
        	    			
        	    		// else TODO: "or the character set of the eastAsia (or eastAsiaTheme if defined) font is Chinese5 or GB2312" 
        	    		// fetch the character set!?
        	    			
        	    		} else {
    	    				vis.fontAction(hAnsi);
    	    			    vis.setMustCreateNewFlag(true);
    	    			} 
        	    	} else {
        	    		// Usual case
        				vis.fontAction(hAnsi);
        	    	}
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\u0100';
        	    	currentRangeUpper = '\u02AF';
        	    } else 
        	    if (c>='\u02B0' && c<='\u04FF') 
        	    {
        	    	if (hint == STHint.EAST_ASIA) {
        				vis.fontAction(eastAsia);
        	    	} else {
        	    		// Usual case
        	    		vis.fontAction(hAnsi); // checked with russian/cyrillic
        	    	}
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\u02B0';
        	    	currentRangeUpper = '\u04FF';
        	    }
        	    else if (c>='\u0590' && c<='\u07BF') 
        	    {
        	    	try {
        	    		
        	    		// This is complex script range,
        	    		// so should we be using it??  
        	    		// Word doesn't seem to be in these edge cases
        	    		// (note that most of the real cs cases should
        	    		//  be handled without this method being invoked)
        	    		
        	    		// Word doesn't use Arial Unicode MS (where specified),
        	    		// so I assume it wouldn't use most other fonts either
        	    		
        	    		// It often uses TNR, so the following is good enough...
						if (GlyphCheck.hasChar("Times New Roman", c)) {
							vis.fontAction("Times New Roman");        	    		
						}
						
					} catch (ExecutionException e) {
						log.error(e.getMessage(), e);
					}
        	    	
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\u0590';
        	    	currentRangeUpper = '\u07BF';
        	    }
        	    else if (c>='\u1100' && c<='\u11FF') 
        	    {
        	    	if (eastAsia==null) {
        	    		vis.fontAction("Gungsuh"); // TODO what if not present?
        	    			// Why is it not found?  Its in batang.ttc
        	    	} else {        	    	
        	    		vis.fontAction(eastAsia);
        	    	}
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\u1100';
        	    	currentRangeUpper = '\u11FF';
        	    } else if (c>='\u1E00' && c<='\u1EFF') 
        	    {
        	    	if (hint == STHint.EAST_ASIA) {
        	    		if (contains(langEastAsia, "zh") ) {
    	    				vis.fontAction(eastAsia);	
        	    		} else {
    	    				vis.fontAction(hAnsi);
    	    			} 
        	    	} else {
        	    		// Usual case
        				vis.fontAction(hAnsi);
        	    	}
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\u1E00';
        	    	currentRangeUpper = '\u1EFF';
        	    }
        	    else if (c>='\u2000' && c<='\u2EFF') 
        	    {
        	    	if (hint == STHint.EAST_ASIA) {
        				vis.fontAction(eastAsia); 
        	    	} else {
        	    		// eg <w:rFonts w:ascii="Arial Unicode MS" w:hAnsi="Arial Unicode MS" 
        	    		//              w:eastAsia="Arial Unicode MS" w:cs="Arial Unicode MS"/>
        	    		if (hAnsi==null) {
        	    			log.warn("TODO: how to handle char '" + c + "' lacking hAnsi?");
        	    		} else {
        	    			
        	    			try {
        						if (GlyphCheck.hasChar(hAnsi, c)) {
        							vis.fontAction(hAnsi);        	    		
        						} else {
        							
        							// Note: what follows is based on what Word 2016
        							// does for Calibri 0x2751 (checkbox) 
    								// but TODO explore what it does for the entire range c>='\u2000' && c<='\u2EFF'
        							
        							// Microsoft Word 2016 uses Segoe UI Symbol
        							// (earlier versions used MS Gothic?)
        							
        							final String FONT_WORD_2016_USES = "Segoe UI Symbol";
                	    			Mapper fontMapper = wordMLPackage.getFontMapper();
                	    			PhysicalFont gothicSubs = fontMapper.get(FONT_WORD_2016_USES);
                	    			// You'll need to map a suitable font.
                	    			// Glyph 10065 (0x2751) not available in font Noto Sans Regular
                	    			// What we want is a dingbat font
                	    			// It doesn't seem to be in Noto Sans Symbols,
                	    			// but it is in DejaVu Sans.
                	    			// Google eg: "Lower right shadowed white square" font
                	    			// It is in Segoe UI Symbol, Wing Dings
        							
        							if (gothicSubs!=null && GlyphCheck.hasChar(gothicSubs, c)) {
	        							vis.fontAction(FONT_WORD_2016_USES);        	    		
	        						} else {
	                	    			log.warn("TODO: how to handle char '" + c + "' in range c>='\\u2000' && c<='\\u2EFF'?");        							
	        						}
        						}
        						
        					} catch (ExecutionException e) {
        						log.error(e.getMessage(), e);
        					}        	    			
        	    		}         	    	}
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\u2000';
        	    	currentRangeUpper = '\u2EFF';
        	    }
        	    else if (c>='\u2F00' && c<='\uDFFF') 
        	    {
        	    	/*
        	    	 * NB, with contrived cases using
        	    	 * Arial Unicode MS, Word substitutes
        	    	 * fonts, including:
        	    	 * - Meiryo
        	    	 * - PMingLiU
        	    	 * - Batang
        	    	 * - MS Mincho
        	    	 * depending on the char
        	    	 */
        	    	
        	    	if (eastAsia==null) {
        	    		
	    				vis.fontAction(hAnsi); 
	    				debugCheckGlyph(hAnsi, c);

        	    	} else {
        	    		// Japanese
            	    	// 2014 02 18 - not necessarily!
            	    	// eg 五、劳动报酬 is Chinese
	    				vis.fontAction(eastAsia); 
	    				debugCheckGlyph(eastAsia, c);
        	    	}
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\u2F00';
        	    	currentRangeUpper = '\uDFFF';
        	    }
        	    else if (c>='\uE000' && c<='\uF8FF') 
        	    {
        	    	
        	    	/* NB, in contrived cases using
        	    	 * Arial Unicode MS, 
        	    	 * Word is generally unable to substitute 
        	    	 * a suitable font!
        	    	 */ 
        	    	
        	    	if (hint == STHint.EAST_ASIA) {
        				vis.fontAction(eastAsia); 
        	    	} else {
        	    		// Usual case
        	    		
        	    		// F000 to F0FF expect to use symbol fonts
        	    		if (hAnsi==null) {
							log.warn("TODO: how to handle char '" + c + "' (0x"
			                    + Integer.toHexString(c) 
			                    + ") lacking hAnsi?");	        	    			
        	    		} else {
    	    				vis.fontAction(hAnsi); 										
    	    				debugCheckGlyph(hAnsi, c);
						}
        	    	}
        	    		
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\uE000';
        	    	currentRangeUpper = '\uF8FF';
        	    }
        	    else if (c>='\uF900' && c<='\uFAFF') 
        	    {
    				vis.fontAction(eastAsia); 
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\uF900';
        	    	currentRangeUpper = '\uFAFF';
        	    } else 
    		    // ..  Alphabetic Presentation Forms
        	    if (c>='\uFB00' && c<='\uFB4F') 
        	    {
        	    	/* hAnsi (or hAnsiTheme if defined), with the following exceptions:
        	    	 * 
    							If the hint is eastAsia then eastAsia (or eastAsiaTheme if defined) is used for characters in the range FB00 – FB1C.
    							For the range FB1D – FB4F, ascii (or asciiTheme if defined) is used.
    					*/
        	    	if (hint == STHint.EAST_ASIA) {
    	    			if ( c>='\uFB00' && c<='\uFB1C') {
    	    				vis.fontAction(eastAsia);
    	    			    vis.setMustCreateNewFlag(true);
    	    			} else {
    	    				vis.fontAction(hAnsi);
    	    			}
        	    			
        	    	} else if ( c>='\uFB1D' && c<='\uFB4F') {
        	    				
        				vis.fontAction(ascii);
        			    vis.setMustCreateNewFlag(true);
        				
        	    	} else {
        				vis.fontAction(hAnsi);
        	    	}
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\uFB00';
        	    	currentRangeUpper = '\uFB4F';
        	    } else if (c>='\uFB50' && c<='\uFDFF') {
    				    vis.fontAction(ascii);
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\uFB50';
        	    	currentRangeUpper = '\uFDFF';	
        	    } else if (c>='\uFE30' && c<='\uFE6F') {
    				vis.fontAction(eastAsia); 
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\uFE30';
        	    	currentRangeUpper = '\uFE6F';	
        	    } else if (c>='\uFE70' && c<='\uFEFE') {
    				vis.fontAction(ascii); 
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\uFE70';
        	    	currentRangeUpper = '\uFEFE';	
        	    } else if (c>='\uFF00' && c<='\uFFEF') {
        	    	
        	    	if (eastAsia==null) {
        	    		// eg <w:rFonts w:ascii="SimSun" w:hAnsi="SimSun" w:cs="SimSun"/>
        	    		// for "；" (0xff1b, semicolonmonospace)  and "，" (0xff0c, commamonospace) 
	    				vis.fontAction(hAnsi); 
        	    	} else {
        	    		vis.fontAction(eastAsia);
        	    	}
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\uFF00';
        	    	currentRangeUpper = '\uFFEF';	
        	    	
//        	    } else if (c>=Character.toChar(0x1F600) && c<='\u1F64F') {
        	    	
        	    } else {
        	    	// Per http://msdn.microsoft.com/en-us/library/ff533743.aspx
        	    	// for all ranges not listed in the above, the hAnsi (or hAnsiTheme if defined) font shall be used.
        	    	String hex = String.format("%04x", (int) c);
        	    	log.debug("Defaulting to hAnsi for char " + hex);
    				vis.fontAction(hAnsi); 
    				debugCheckGlyph(hAnsi, c);
    				
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower='\u0000';
        	    	currentRangeUpper='\u0000';
        	    	
        	    	// TODO: enhance to allow current to be reused, if font is same
        	    	
        	    	
        	    }
    	    }
    	} 
    	
    	// Handle final span
    	vis.finishPrevious();
    	return vis.getResult();
    }
    
    private void debugCheckGlyph(String fontName, char c) {
    	
		if (log.isDebugEnabled()) {
	    	try {
				if (!GlyphCheck.hasChar(fontName, c)) {
//					Throwable t = new Throwable();
//					log.debug("FIXME", t);
					log.debug(fontName + " missing " + c);
				}
			} catch (ExecutionException e) {
				log.error(e.getMessage(), e);
			}
	    }    	
    }
    
	
	private String getCssProperty(String fontName) {
		
		if (log.isDebugEnabled() && 
				fontName==null) {
			Throwable t = new Throwable();
			t.printStackTrace();
		}
		
		String font = getPhysicalFont(fontName);
		
		if (font!=null) {					
			return Property.composeCss(CSS_NAME, "'" + font + "'");
		} else {
			// We don't have this font, so don't specify it in our CSS
			log.info("No physical font for " + fontName);
			return Property.CSS_NULL;
		}
		
	}

	
	private String getPhysicalFont(String fontName) {
		
		log.debug("looking for: " + fontName);
//		if (log.isDebugEnabled()) {
//			Throwable t = new Throwable();
//			log.debug("Call stack", t);
//		}		
		
		PhysicalFont pf = wordMLPackage.getFontMapper().get(fontName);
		if (pf!=null) {
			log.debug("Font '" + fontName + "' maps to " + pf.getName() );
			return pf.getName();
		} else {
			
			// This is ok if it happens 
			// at org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart.fontsInUse(MainDocumentPart.java:238)
			// at org.docx4j.openpackaging.packages.WordprocessingMLPackage.setFontMapper(WordprocessingMLPackage.java:311)
			// Can suppress warning with either:
//				StackTraceElement[] ste= (new Throwable()).getStackTrace();
//				if (ste[2].getMethodName().equals("fontsInUse")) {
			// or
//				if (wordMLPackage.getFontMapper().getFontMappings().size()==0) {
			
			// Special cases; there are more; see http://en.wikipedia.org/wiki/List_of_CJK_fonts
			String englishFromCJK = CJKToEnglish.toEnglish( fontName);
			if (englishFromCJK==null) {
				if (wordMLPackage.getFontMapper().size()>0) {
					log.warn("Font '" + fontName + "' is not mapped to a physical font. " );
				}
				return null;
			} else {
				pf = wordMLPackage.getFontMapper().get(englishFromCJK);
			}
			
			if (pf==null) {
				if (wordMLPackage.getFontMapper().size()>0) {
					log.warn("Font '" + englishFromCJK + "' is not mapped to a physical font. " );
				}
				return null;
			}
			
			return pf.getName();
		}		
	}	
	
	public interface RunFontCharacterVisitor {
		
		void setRunFontSelector(RunFontSelector runFontSelector);
		
		void setDocument(Document document);
		
		void addCharacterToCurrent(char c);

		void addCodePointToCurrent(int cp); //@since 3.3.0
		
		void finishPrevious();

		void createNew();
		
		void setMustCreateNewFlag(boolean val);
		
		boolean isReusable();
		
		void fontAction(String fontname);
		
		void setFallbackFont(String fontname);
		
		Object getResult();  // when used in output a DocumentFragment; when used to find fonts, a Set.

	}
	
	// Arabic numbering stuff
	
	enum MicrosoftWordNumeralOption {
		Hindi, Context, Arabic, System;
	}
	enum NativeDigitsSetting {
		National, Context;
	}
	
	private static NumericShaper numericShaperArabicIndic = null;
	private static NumericShaper getNumericShaperArabicIndic() {
		
		if (numericShaperArabicIndic == null) {
			numericShaperArabicIndic = NumericShaper.getShaper(NumericShaper.ARABIC);
				// NumericShaper.EASTERN_ARABIC actually corresponds to Unicode EXTENDED ARABIC-INDIC DIGIT
		}
		return numericShaperArabicIndic;
	}
	
	private static NativeDigitsSetting nativeDigitsSetting = null;
	private static NativeDigitsSetting getNativeDigitsSetting() {
		
		if (nativeDigitsSetting==null) {
			nativeDigitsSetting = NativeDigitsSetting.valueOf(
					Docx4jProperties.getProperty("docx4j.MicrosoftWindows.Region.Format.Numbers.NativeDigits", "National"));
		}
		return nativeDigitsSetting;
	}

	private static MicrosoftWordNumeralOption microsoftWordNumeralOption = null;
	private static MicrosoftWordNumeralOption getMicrosoftWordNumeralOption() {
		
		if (microsoftWordNumeralOption==null) {
			microsoftWordNumeralOption = MicrosoftWordNumeralOption.valueOf(
					Docx4jProperties.getProperty("docx4j.MicrosoftWord.Numeral", "Arabic"));
		}
		return microsoftWordNumeralOption;
	}
	
	private String shapeAsArabicIndic(String text) {
		
		// Use U+0660 .. U+0669 are ARABIC-INDIC DIGIT values 0 through 9
		// See http://stackoverflow.com/questions/1676460/in-unicode-why-are-there-two-representations-for-the-arabic-digits
		 char[] chars = text.toCharArray();
		 getNumericShaperArabicIndic().shape(chars, 0, chars.length);
		 return new String(chars);		
	}
	
    private String arabicNumbering(String text, BooleanDefaultTrue rtl, BooleanDefaultTrue cs, CTLanguage themeFontLang ) {
    	
    	/* Rules below were inferred based on testing which always included
    	 * 
    	 *     <w:pPr>
			      <w:bidi/>
			    </w:pPr>

    	 */
    	if (themeFontLang!=null
    			&& themeFontLang.getBidi()!=null
    			&& themeFontLang.getBidi().equals("ar-SA")) {
    		// Do stuff in this method
    	} else {
    		// Do nothing if those conditions don't apply
    		return text;
    	}

    	if ( (rtl!=null && rtl.isVal())
    			|| (cs!=null && cs.isVal()) ) {

			// If rtl or cs present, use eastern numbering, except where numer option = arabic.  
			// Return ie (If both rtl and the hint are present, do what rtl tells you
			
	    	if (getMicrosoftWordNumeralOption().equals(MicrosoftWordNumeralOption.Arabic)) {
	    		return text;
	    	} else {
	    		return shapeAsArabicIndic(text);
	    	}
    	}
    	
    	if (getNativeDigitsSetting().equals(NativeDigitsSetting.National)) {
    		return shapeAsArabicIndic(text);
    	} else {
    		// Context
    		if (getMicrosoftWordNumeralOption().equals(MicrosoftWordNumeralOption.Hindi)) {
        		return shapeAsArabicIndic(text);
    		} else {
	    		return text;
    		}
    	}
    	
    }	
	// end Arabic Numbering stuff 	
}
