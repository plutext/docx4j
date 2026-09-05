package org.docx4j.fonts;

import org.docx4j.Docx4jProperties;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.common.writer.SymbolMapper;
import org.docx4j.convert.out.common.writer.SymbolUtils;
import org.docx4j.model.PropertyResolver;
import org.docx4j.model.properties.Property;
import org.docx4j.openpackaging.exceptions.CyclicStylesException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STHint;
import org.docx4j.wml.Style;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docx4j.fonts.fop.fonts.CustomFont;
import org.docx4j.fonts.fop.fonts.Typeface;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

//import com.vdurmont.emoji.EmojiManager;

import java.awt.font.NumericShaper;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

/**
 * Apply the appropriate font to the characters in the run, following the rules in the
 * Microsoft implementer notes on rFonts: [MS-OI29500] Part 1 section 17.3.2.26 (ISO 29500),
 * https://learn.microsoft.com/en-us/openspecs/office_standards/ms-oi29500/aef3c9a6-5d6c-434b-90b7-85e761fd8e62
 * The table in [MS-OE376] Part 4 section 2.3.2.24 (the ECMA-376 notes, documenting Word 2007)
 * is identical, so the documented behaviour is stable across Word generations.
 *
 * ECMA-376 itself (mirrored at http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/rFonts.html)
 * just says font use "shall be determined by the Unicode character values of the run content",
 * without saying how; the old MSDN article ff533743 (link now dead) derived from the same
 * material as the implementer notes.
 *
 * The implementation was validated against the [MS-OI29500] table on 2026-08-19; for the
 * known deliberate divergences, see the comment at the top of unicodeRangeToFont.
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
		
		fallbackPhysicalFont = physicalFontFor(getDefaultFont());
		fallbackFont = getPhysicalFont(getDefaultFont());
		if (fallbackFont==null) {
			fallbackFont = getDefaultFont();
			if (outputType!= RunFontActionType.DISCOVERY) {
				log.warn("Default font " + getDefaultFont() + " is not mapped; Fallback set to default.");
			}
		} else {
			log.debug("Fallback font set to " + fallbackFont);			
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

	/** The physical font {@link #fallbackFont} names, registered with the Mapper the
	 *  first time a run actually falls back to it so that FopConfigUtil declares it:
	 *  it is the default font's physical font, which need not be among the fonts the
	 *  document names, in which case FOP reported "Font ... not found. Substituting
	 *  with any" and used a default font.  @since 17.0.5 */
	private PhysicalFont fallbackPhysicalFont = null;

	private final java.util.Set<String> registeredFonts = new java.util.HashSet<String>();

	/**
	 * Tell the Mapper (and so FopConfigUtil, which asks it after the FO has been
	 * generated) about a physical font this conversion actually put in @font-family.
	 * The FOP configuration is built from the fonts the document's runs name, so a
	 * font reached only through a paragraph mark, an empty paragraph, a style or the
	 * fallback was not declared and FOP reported "Font ... not found. Substituting
	 * with any", quietly rendering that text in a default font.
	 *
	 * @since 17.0.5
	 */
	private void registerUsedFont(String family, PhysicalFont pf) {
		if (pf==null || family==null || outputType!=RunFontActionType.XSL_FO) return;
		if (!registeredFonts.add(family)) return;
		if (wordMLPackage!=null && wordMLPackage.getFontMapper()!=null) {
			wordMLPackage.getFontMapper().registerLastResortFallback(pf);
		}
	}
	
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
			
	    	PropertyResolver propertyResolver=null;
			try {
				propertyResolver = wordMLPackage.getMainDocumentPart().getPropertyResolver();
			} catch (Docx4JException e) {
				log.error(e.getMessage(), e);
			}
			
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
		return (DocumentFragment)finish(docfrag);
    }

    /** The final touches on a converted run's fragment: a font which has the glyphs,
     *  then kerned spaces, then small caps. */
    private Object finish(Object fragment) {
    	return smallCaps(kernSpaces(glyphFallback(fragment)));
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
    		PhysicalFont resolved = physicalFontResolved(fontName);
    		String val = resolved==null ? null : resolved.getName();
    		if (val==null) {
    			if (log.isDebugEnabled() ) {
    				log.debug(fontName + " not mapped; using fallback " + fallbackFont);
    			}
    			// Avoid @font-family="", which FOP doesn't like
    			el.setAttribute("font-family", fallbackFont );
    			applyLineHeight(el, fontName, fallbackFont);
    			registerUsedFont(fallbackFont, fallbackPhysicalFont);
    		} else {
    			el.setAttribute("font-family", foFontFamily(val) );
    			applyLineHeight(el, fontName, val);
    			registerUsedFont(val, resolved);
    		}
    		if (fontName!=null) {
    			// glyphFallback needs the font the document asked for; it removes this
    			el.setAttribute(MARK_DOCUMENT_FONT, fontName);
    		}

			// NB, for PDF/FOP, white space handling on the parent fo:block, 
    		// see XsltFOFunctions (for XSLT), and AbstractVisitorExporterGenerator (non XSLT)
    		
//    		if (spacePreserve) {
//    			el.setAttribute("white-space-treatment","preserve");
//    		}
    		// NB, that on its own may stop FOP 1.x from line wrapping!
    		
    	} 
    }

    public void symbolSetAttribute(Element el, String fontName, String textValue) {
    	// We only pass the actual text here, so that we can do GlyphCheck
    	// to ensure the correct font in the PDF case.
    	// TODO: this assumes that each char in textValue uses the same font,
    	// which may not be true.  Unlikely edge case though...
    	// To fix this, we'd have to create a new span each time the font changed. 
    	
    	// could a document fragment contain just a #text node?
    	
		if (outputType== RunFontActionType.DISCOVERY) {
			return;
		} else if (outputType==RunFontActionType.XHTML) {
			// In XHTML, we leave it up to the browser to choose the specific font
    		if (spacePreserve) {
    	    	/*
    	    	 * 	Convert @xml:space='preserve' to style="white-space:pre-wrap;"
    				which is good for FF3, and WebKit; not honoured by IE7 though. 
    	    	 */
    			el.setAttribute("style", Property.composeCss(CSS_NAME, SymbolUtils.HTML_FONT_FAMILY) + "white-space:pre-wrap;");
    			
    		} else {
    			el.setAttribute("style", Property.composeCss(CSS_NAME, SymbolUtils.HTML_FONT_FAMILY) );
    		}
    	} else if (outputType==RunFontActionType.XSL_FO) {
    		
    		if (textValue.equals(SymbolUtils.MISSING_SYMBOL)) {
    			return;
    		}
    		
			PhysicalFont pf = null;
			PhysicalFont pf2 = null;
			if (fontName.equals("Webdings")
					|| fontName.equals("Wingdings")
					|| fontName.equals("Wingdings 2")
					|| fontName.equals("Wingdings 3")
					) {
				pf = PhysicalFonts.getWDingsFont();
				pf2 = PhysicalFonts.getWDingsFont2();
			} else if (fontName.equals("Symbol")) {
				pf = PhysicalFonts.getSymbolFont();
			} 
			
			try {
				if (pf !=null && GlyphCheck.hasCodepoint(pf, textValue.codePointAt(0))) {
					// good, it is there
				} else if (pf2!=null && GlyphCheck.hasCodepoint(pf2, textValue.codePointAt(0))) {
					pf =pf2; // use pf2
					log.debug("For " + fontName + " mapped to " + textValue.codePointAt(0) + ", using 2nd substitute font " + pf2.getName());
				} else {
					log.warn("Missing symbol " + fontName + " " + textValue);
				}
			} catch (ExecutionException e) {}
			
			String val = null;
			if (pf == null) {
				val = getPhysicalFont(fontName);
			} else {
				val = pf.getName();
			}
    		
    		if (val==null) {
    			// Avoid @font-family="", which FOP doesn't like
    			el.setAttribute("font-family", fallbackFont );
    			applyLineHeight(el, fontName, fallbackFont);
    			registerUsedFont(fallbackFont, fallbackPhysicalFont);
    		} else {	
    			el.setAttribute("font-family", foFontFamily(val) );
    			applyLineHeight(el, fontName, val);
    		}
    	} 
    }
    
    
    // ---- Word line height (XSL FO only); see WordLineMetrics. @since 17.0.5

    /** The paragraph spacing and run size of the run currently being processed, captured in
     *  fontSelector so that the spans it creates can carry the line-height Word would give them. */
    private PPrBase.Spacing currentSpacing;
    private double currentSizePt = -1;

    /**
     * Suffix of the font-family name under which FopConfigUtil registers a
     * font's kerned twin (same file, FOP kerning on).  Word kerns a run only
     * when its w:kern threshold is at or below its size (ECMA-376 17.3.2.19),
     * and FOP kerns per font, so kerned runs use the twin.
     *
     * @since 17.0.5
     */
    public static final String KERNED_SUFFIX = "+kern";

    /** whether the current run is kerned in Word: w:kern present, non-zero, and no
     *  larger than the run's w:sz (both half-points) */
    private boolean currentKerned;

    /** Word's rule for a run's kerning (the rPr must be the effective one). */
    public static boolean isKerned(RPr rPr) {
    	if (rPr==null || rPr.getKern()==null || rPr.getKern().getVal()==null) return false;
    	java.math.BigInteger kern = rPr.getKern().getVal();
    	if (kern.signum()<=0) return false;
    	java.math.BigInteger sz = (rPr.getSz()==null || rPr.getSz().getVal()==null)
    			? java.math.BigInteger.valueOf(20) : rPr.getSz().getVal(); // 10pt if nothing says otherwise
    	return sz.compareTo(kern) >= 0;
    }

    /** per-run kerning is only needed while FOP's fonts are unkerned (the default) */
    private static boolean perRunKerning() {
    	return !Docx4jProperties.getProperty("docx4j.convert.out.fo.kerning", false);
    }

    /** the font-family for the FO span: the kerned twin when the run kerns */
    private String foFontFamily(String physicalFontName) {
    	return (currentKerned && perRunKerning()) ? physicalFontName + KERNED_SUFFIX : physicalFontName;
    }

    /**
     * Word kerns pairs involving the space glyph as well (measured: "A␠" -50/1000
     * em, "␠A" -60, "T␠", "Y␠", "V␠", "W␠" in Liberation Serif), whereas FOP
     * kerns only within words, its spaces being glue.  For a kerned run each
     * space whose pairs kern is wrapped in an inline whose word-spacing carries
     * the pair values (FOP: a space's width is the glyph plus word-spacing), so
     * it stays a break opportunity.  Pairs across run boundaries are not seen.
     *
     * @since 17.0.5
     */
    private Object kernSpaces(Object fragment) {
    	if (!(fragment instanceof DocumentFragment) || outputType!=RunFontActionType.XSL_FO
    			|| !currentKerned || !perRunKerning() || currentSizePt<=0) return fragment;
    	DocumentFragment df = (DocumentFragment)fragment;
    	for (Node n = df.getFirstChild(); n!=null; n = n.getNextSibling()) {
    		if (!(n instanceof Element)) continue;
    		Element span = (Element)n;
    		String family = span.getAttribute("font-family");
    		if (family.length()==0 || span.getChildNodes().getLength()!=1 || !(span.getFirstChild() instanceof org.w3c.dom.Text)) continue;
    		String text = span.getTextContent();
    		if (text.indexOf(' ')<0) continue;
    		java.util.Map<Integer, java.util.Map<Integer, Integer>> kern = kerningPairs(PhysicalFonts.get(family));
    		if (kern==null) continue;
    		int[] cps = text.codePoints().toArray();
    		Document doc = span.getOwnerDocument();
    		StringBuilder seg = new StringBuilder();
    		boolean any = false;
    		java.util.List<Node> children = new java.util.ArrayList<>();
    		for (int i=0; i<cps.length; i++) {
    			int cp = cps[i];
    			if (cp==' ') {
    				int k = (i>0 ? kernValue(kern, cps[i-1], ' ') : 0) + (i+1<cps.length ? kernValue(kern, ' ', cps[i+1]) : 0);
    				if (k!=0) {
    					if (seg.length()>0) { children.add(doc.createTextNode(seg.toString())); seg.setLength(0); }
    					Element sp = doc.createElementNS(span.getNamespaceURI(),
    							(span.getPrefix()==null ? "" : span.getPrefix() + ":") + "inline");
    					sp.setAttribute("word-spacing", WordLineMetrics.format(k * currentSizePt / 1000.0));
    					sp.appendChild(doc.createTextNode(" "));
    					children.add(sp);
    					any = true;
    					continue;
    				}
    			}
    			seg.appendCodePoint(cp);
    		}
    		if (!any) continue;
    		if (seg.length()>0) children.add(doc.createTextNode(seg.toString()));
    		span.removeChild(span.getFirstChild());
    		for (Node c : children) span.appendChild(c);
    	}
    	return fragment;
    }

    private static int kernValue(java.util.Map<Integer, java.util.Map<Integer, Integer>> kern, int a, int b) {
    	java.util.Map<Integer, Integer> m = kern.get(a);
    	if (m==null) return 0;
    	Integer v = m.get(b);
    	return v==null ? 0 : v;
    }

    private static final java.util.Map<PhysicalFont, java.util.Map<Integer, java.util.Map<Integer, Integer>>> KERNING
    		= java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    private static final java.util.Map<Integer, java.util.Map<Integer, Integer>> NO_KERNING = java.util.Collections.emptyMap();

    /** the font's kern-table pairs (unicode -> unicode -> 1/1000 em), or null */
    private static java.util.Map<Integer, java.util.Map<Integer, Integer>> kerningPairs(PhysicalFont pf) {
    	if (pf==null) return null;
    	java.util.Map<Integer, java.util.Map<Integer, Integer>> m = KERNING.get(pf);
    	if (m==null) {
    		m = NO_KERNING;
    		try {
    			Typeface tf = GlyphCheck.getTypeface(pf);
    			if (tf instanceof CustomFont && ((CustomFont)tf).hasKerningInfo()) {
    				m = ((CustomFont)tf).getKerningInfo();
    			}
    		} catch (Exception e) {
    			log.warn("No kerning for " + pf.getName() + ": " + e.getMessage());
    		}
    		KERNING.put(pf, m);
    	}
    	return m==NO_KERNING ? null : m;
    }
    private PPr lastPPr;
    private PPrBase.Spacing lastPPrSpacing;

    /**
     * The document font an rPr asks for its ASCII text (w:ascii, or the theme font
     * w:asciiTheme names; failing those, w:hAnsi or the document default), for
     * line metrics of generated text and paragraph marks.
     *
     * @since 17.0.5
     */
    public String asciiFontName(RPr rPr) {
    	RFonts rFonts = rPr==null ? null : rPr.getRFonts();
    	if (rFonts!=null) {
    		if (rFonts.getAsciiTheme()!=null && getThemePart()!=null) {
    			try {
    				String f = getThemePart().getFont(rFonts.getAsciiTheme(), themeFontLang);
    				if (f!=null && f.length()>0) return f;
    			} catch (Exception e) {
    				log.debug(e.getMessage());
    			}
    		}
    		if (rFonts.getAscii()!=null) return rFonts.getAscii();
    		if (rFonts.getHAnsi()!=null) return rFonts.getHAnsi();
    	}
    	return getDefaultFont();
    }

    private void captureLineSpec(PropertyResolver propertyResolver, PPr pPr, RPr rPr) {
    	if (outputType!=RunFontActionType.XSL_FO) return;
    	currentKerned = isKerned(rPr);
    	currentSizePt = (rPr!=null && rPr.getSz()!=null && rPr.getSz().getVal()!=null)
    			? rPr.getSz().getVal().doubleValue()/2 : -1;
    	// effective pPr is needed for the style-inherited w:spacing; cache per pPr object
    	// (the visitor pathway passes the same object for every run of a paragraph)
    	if (pPr!=null && pPr==lastPPr) {
    		currentSpacing = lastPPrSpacing;
    		return;
    	}
    	PPrBase.Spacing spacing = null;
    	try {
    		PPr effective = (propertyResolver==null) ? pPr : propertyResolver.getEffectivePPr(pPr);
    		if (effective!=null) spacing = effective.getSpacing();
    	} catch (Exception e) {
    		log.warn("Couldn't resolve effective pPr for line height: " + e.getMessage());
    		if (pPr!=null) spacing = pPr.getSpacing();
    	}
    	lastPPr = pPr;
    	lastPPrSpacing = spacing;
    	currentSpacing = spacing;
    }

    /** Set line-height on this FO span from the physical font's Word metrics, the run's
     *  size and the paragraph's w:spacing.  Word sizes a line by the tallest run on it;
     *  with these on every fo:inline, FOP's max-height line stacking does the same. */
    /** Hint on the FO span naming the document font (the one the docx asks for), so the
     *  block's line box and the line manager can size the line from its metrics when a
     *  substitute renders it; removed by WordLayoutFixups.  @since 17.0.5 */
    public static final String HINT_FONT = "docx4j-font";

    private void applyLineHeight(Element el, String documentFontName, String physicalFontName) {
    	if (outputType!=RunFontActionType.XSL_FO || currentSizePt<=0 || el==null) return;
    	    	PhysicalFont pf = physicalFontName==null ? null : PhysicalFonts.get(physicalFontName);
    	    	el.setAttribute("line-height", WordLineMetrics.lineHeightPtString(documentFontName, pf, currentSizePt, currentSpacing));
    	    	if (documentFontName!=null && WordLineMetrics.hasTableEntry(documentFontName)
    	    			&& Docx4jProperties.getProperty("docx4j.convert.out.fo.wordLayoutFixups", true)) {
    	    		el.setAttribute(HINT_FONT, documentFontName);
    	    	}
    	// Not done: moving the text to where Word puts it within the line (FOP centres
    	// the leading, Word does not; see WordLineMetrics.baselineShiftPt).  Tried as
    	// baseline-shift on the span: FOP enlarges the line box by the shift instead of
    	// moving the glyphs inside it, so the pitch drifted.  Needs a layout-manager
    	// level change (CR-001 option B); the residual is a constant per paragraph.
    }

    // ---- glyph-aware last-resort fallback (XSL FO only). @since 17.0.5

    /**
     * Attribute in which setAttribute leaves the font the *document* asked for, so that
     * glyphFallback can consult its class and its line metrics; always removed there.
     */
    private static final String MARK_DOCUMENT_FONT = "docx4j-document-font";

    /** The choice made for a (document font, script) pair, for this conversion.  A null
     *  value means nothing installed covers it (already warned about). */
    private final java.util.Map<String, PhysicalFont> fallbackByScript
    		= new java.util.HashMap<String, PhysicalFont>();

    /**
     * Where the font a span ended up with has no glyphs for the span's characters, put
     * the characters in one which has.
     *
     * <p>An unmapped font falls back to the document default's physical font, which is
     * chosen without reference to what the run actually contains: a document setting
     * Georgian in a font the box lacks rendered as a row of notdef boxes even where the
     * box had Noto Sans Georgian installed (CR-001 cause C3).  The choice is made per
     * script, preferring a font of the document font's own class, and cached for the
     * conversion.  Where nothing installed covers the script, the span is left alone and
     * {@link FontFallback#warnNoCoverage} says so once, rather than FOP saying so once
     * per glyph.</p>
     *
     * <p>Only spans containing something outside Latin/Greek/Cyrillic are examined, so
     * ordinary documents pay one code point scan per span.</p>
     */
    private Object glyphFallback(Object fragment) {

    	if (outputType!=RunFontActionType.XSL_FO || !(fragment instanceof DocumentFragment)) {
    		return fragment;
    	}
    	for (Node n = ((DocumentFragment)fragment).getFirstChild(); n!=null; n = n.getNextSibling()) {
    		if (n instanceof Element) {
    			try {
    				glyphFallback((Element)n);
    			} catch (Exception e) {
    				log.warn("Couldn't check glyph coverage: " + e.getMessage());
    				((Element)n).removeAttribute(MARK_DOCUMENT_FONT);
    			}
    		}
    	}
    	return fragment;
    }

    private void glyphFallback(Element span) throws ExecutionException {

    	String documentFont = span.getAttribute(MARK_DOCUMENT_FONT);
    	span.removeAttribute(MARK_DOCUMENT_FONT);

    	String text = span.getTextContent();
    	if (text==null || text.length()==0 || span.getChildNodes().getLength()>1) return;

    	int[] cps = text.codePoints().toArray();
    	if (!FontFallback.needsCoverage(cps)) return;

    	String family = span.getAttribute("font-family");
    	if (family.length()==0) return;
    	boolean kerned = family.endsWith(KERNED_SUFFIX);
    	PhysicalFont current = PhysicalFonts.get(family); // strips the kerned suffix itself
    	if (current==null && documentFont.length()>0) current = physicalFontFor(documentFont);

    	// what the current font can't render, by script
    	java.util.Map<Character.UnicodeScript, java.util.List<Integer>> missing
    			= new java.util.LinkedHashMap<Character.UnicodeScript, java.util.List<Integer>>();
    	boolean[] covered = new boolean[cps.length];
    	for (int i=0; i<cps.length; i++) {
    		covered[i] = current!=null && GlyphCheck.hasCodepoint(current, cps[i]);
    		if (covered[i]) continue;
    		Character.UnicodeScript script = FontFallback.scriptOf(cps[i]);
    		java.util.List<Integer> list = missing.get(script);
    		if (list==null) {
    			list = new java.util.ArrayList<Integer>();
    			missing.put(script, list);
    		}
    		if (!list.contains(cps[i]) && list.size()<32) list.add(cps[i]);
    	}
    	if (missing.isEmpty()) return;

    	java.util.Map<Character.UnicodeScript, PhysicalFont> chosen
    			= new java.util.HashMap<Character.UnicodeScript, PhysicalFont>();
    	for (java.util.Map.Entry<Character.UnicodeScript, java.util.List<Integer>> e : missing.entrySet()) {
    		chosen.put(e.getKey(), fallbackFor(documentFont, e.getKey(), e.getValue()));
    	}

    	// assign a font per code point; a shared character (space, digit, punctuation) goes
    	// with what precedes it, so a Georgian phrase doesn't come apart at its spaces
    	PhysicalFont[] assigned = new PhysicalFont[cps.length];
    	PhysicalFont previous = null;
    	boolean any = false;
    	for (int i=0; i<cps.length; i++) {
    		Character.UnicodeScript script = FontFallback.scriptOf(cps[i]);
    		PhysicalFont pf;
    		if (covered[i]) {
    			pf = isShared(script) ? previous : null;
    			if (pf!=null && !GlyphCheck.hasCodepoint(pf, cps[i])) pf = null;
    		} else {
    			pf = chosen.get(script);
    		}
    		assigned[i] = pf;
    		if (pf!=null) any = true;
    		previous = pf;
    	}
    	if (!any) return;

    	// the common case: one substitute, and it can render the whole span
    	PhysicalFont single = null;
    	boolean one = true;
    	for (PhysicalFont pf : assigned) {
    		if (pf==null) continue;
    		if (single==null) single = pf;
    		else if (single!=pf) { one = false; break; }
    	}
    	if (one && single!=null && FontFallback.covers(single, cps)) {
    		setFallbackFamily(span, documentFont, single, kerned);
    		return;
    	}

    	// mixed: wrap each stretch which needs a substitute in an inline of its own
    	Document doc = span.getOwnerDocument();
    	java.util.List<Node> children = new java.util.ArrayList<Node>();
    	StringBuilder seg = new StringBuilder();
    	PhysicalFont segFont = assigned[0];
    	for (int i=0; i<cps.length; i++) {
    		if (assigned[i]!=segFont) {
    			children.add(segmentNode(doc, span, seg.toString(), segFont, documentFont, kerned));
    			seg.setLength(0);
    			segFont = assigned[i];
    		}
    		seg.appendCodePoint(cps[i]);
    	}
    	if (seg.length()>0) {
    		children.add(segmentNode(doc, span, seg.toString(), segFont, documentFont, kerned));
    	}
    	while (span.getFirstChild()!=null) span.removeChild(span.getFirstChild());
    	for (Node c : children) span.appendChild(c);
    }

    /** Characters a script shares with its neighbours (spaces, digits, punctuation). */
    private static boolean isShared(Character.UnicodeScript script) {
    	return script==Character.UnicodeScript.COMMON || script==Character.UnicodeScript.INHERITED;
    }

    private Node segmentNode(Document doc, Element span, String text, PhysicalFont pf,
    		String documentFont, boolean kerned) {

    	if (pf==null) return doc.createTextNode(text);
    	Element inline = doc.createElementNS(span.getNamespaceURI(),
    			(span.getPrefix()==null ? "" : span.getPrefix() + ":") + "inline");
    	setFallbackFamily(inline, documentFont, pf, kerned);
    	inline.appendChild(doc.createTextNode(text));
    	return inline;
    }

    private void setFallbackFamily(Element el, String documentFont, PhysicalFont pf, boolean kerned) {

    	el.setAttribute("font-family", kerned && perRunKerning() ? pf.getName() + KERNED_SUFFIX : pf.getName());
    	applyLineHeight(el, documentFont.length()==0 ? null : documentFont, pf.getName());
    	if (wordMLPackage!=null && wordMLPackage.getFontMapper()!=null) {
    		// so that the FOP configuration declares it; see FopConfigUtil
    		wordMLPackage.getFontMapper().registerLastResortFallback(pf);
    	}
    }

    /** The font to render this script in, for text the document sets in this font; null
     *  where nothing installed can. */
    private PhysicalFont fallbackFor(String documentFont, Character.UnicodeScript script,
    		java.util.List<Integer> codePoints) {

    	String key = documentFont + " " + script;
    	if (fallbackByScript.containsKey(key)) return fallbackByScript.get(key);

    	int[] cps = new int[codePoints.size()];
    	for (int i=0; i<cps.length; i++) cps[i] = codePoints.get(i);

    	PhysicalFont pf = FontFallback.selectCovering(documentFont, cps);
    	if (pf==null) {
    		FontFallback.warnNoCoverage(documentFont, cps);
    	} else if (log.isDebugEnabled()) {
    		log.debug(script + " set in " + documentFont + " rendered in " + pf.getName());
    	}
    	fallbackByScript.put(key, pf);
    	return pf;
    }

    // ---- w:caps / w:smallCaps, and the literal soft hyphen (XSL FO only). @since 17.0.5

    /**
     * The size of a small capital, as a proportion of the run's font size.  Word's
     * small caps are about 80% of the size (it uses the font's own small-cap design
     * where there is one, and scales otherwise); FO has no font-variant, and FOP
     * would ignore it, so docx4j uppercases the text itself and puts the
     * originally-lower-case stretches in an inline at this size.
     */
    private static final String SMALL_CAPS_SIZE = "80%";

    /** for the text last transformed: whether each code point was originally lower case
     *  (so is to be set as a small capital), or null when the run is not small caps */
    private boolean[] smallCapsMask;

    /**
     * The text as it is to be rendered: upper-cased where the effective run properties
     * say w:caps or w:smallCaps, and with literal soft hyphens (U+00AD) dropped.
     *
     * <p>Word applies w:caps and w:smallCaps when it renders, leaving the stored text
     * alone; FOP has no text-transform or font-variant, so the transformation has to
     * happen here.  It is upper-cased in the run's own language (w:lang, else the
     * document's, both of which the effective rPr carries), since Turkish and
     * Lithuanian case differently.</p>
     *
     * <p>FOP does not break at U+00AD, and most fonts have no glyph for it, so a
     * literal soft hyphen came out as a notdef box in the middle of a word; Word only
     * ever shows one where it breaks the line.</p>
     *
     * <p>HTML gets text-transform/font-variant instead (see the Caps and SmallCaps
     * properties), so the text itself is left alone there.</p>
     */
    private String capsAndSoftHyphens(RPr rPr, String text) {

    	smallCapsMask = null;
    	if (text==null || text.length()==0 || outputType!=RunFontActionType.XSL_FO) return text;

    	boolean caps = isOn(rPr==null ? null : rPr.getCaps());
    	boolean smallCaps = !caps && isOn(rPr==null ? null : rPr.getSmallCaps());
    	boolean softHyphen = text.indexOf('\u00AD')>=0;
    	if (!caps && !smallCaps && !softHyphen) return text;

    	java.util.Locale locale = runLocale(rPr);
    	StringBuilder sb = new StringBuilder(text.length());
    	java.util.List<Boolean> lower = smallCaps ? new java.util.ArrayList<Boolean>(text.length()) : null;
    	for (int i=0; i<text.length(); ) {
    		int cp = text.codePointAt(i);
    		i += Character.charCount(cp);
    		if (cp=='\u00AD') continue; // a discretionary hyphen: nothing is painted unless it breaks
    		int out = cp;
    		if (caps || smallCaps) {
    			String one = new String(Character.toChars(cp));
    			String up = one.toUpperCase(locale);
    			// keep the mapping one code point to one, so that the small caps mask lines
    			// up with the text (eg German ß upper-cases to SS, which Word leaves alone)
    			if (up.codePointCount(0, up.length())==1) out = up.codePointAt(0);
    		}
    		if (lower!=null) lower.add(Character.isLowerCase(cp));
    		sb.appendCodePoint(out);
    	}
    	if (lower!=null) {
    		boolean any = false;
    		boolean[] mask = new boolean[lower.size()];
    		for (int i=0; i<mask.length; i++) {
    			mask[i] = lower.get(i);
    			any |= mask[i];
    		}
    		if (any) smallCapsMask = mask;
    	}
    	return sb.toString();
    }

    private static boolean isOn(BooleanDefaultTrue val) {
    	return val!=null && val.isVal();
    }

    /** The run's language, for case conversion: w:lang (the effective rPr carries the
     *  document default), else the root locale. */
    private static java.util.Locale runLocale(RPr rPr) {
    	if (rPr!=null && rPr.getLang()!=null) {
    		String tag = rPr.getLang().getVal();
    		if (tag!=null && tag.length()>0) {
    			try {
    				java.util.Locale l = java.util.Locale.forLanguageTag(tag);
    				if (l!=null && l.getLanguage().length()>0) return l;
    			} catch (Exception e) {
    				log.debug("Not a language tag: " + tag);
    			}
    		}
    	}
    	return java.util.Locale.ROOT;
    }

    /**
     * Put the originally-lower-case stretches of a small caps run into an inline at
     * {@value #SMALL_CAPS_SIZE} of the run's size.  The mask was built from the text
     * this fragment was made from, so the two are walked in step; if they have come
     * apart (a run whose text was rewritten on the way, as Arabic-Indic shaping does),
     * the run is simply left in full capitals.
     */
    private Object smallCaps(Object fragment) {

    	boolean[] mask = smallCapsMask;
    	smallCapsMask = null;
    	if (mask==null || !(fragment instanceof DocumentFragment) || outputType!=RunFontActionType.XSL_FO) {
    		return fragment;
    	}
    	DocumentFragment df = (DocumentFragment)fragment;
    	java.util.List<org.w3c.dom.Text> texts = new java.util.ArrayList<>();
    	collectTextNodes(df, texts);
    	int total = 0;
    	for (org.w3c.dom.Text t : texts) {
    		String v = t.getNodeValue();
    		if (v!=null) total += v.codePointCount(0, v.length());
    	}
    	if (total!=mask.length) {
    		log.debug("Small caps skipped: " + total + " characters rendered, " + mask.length + " expected");
    		return fragment;
    	}
    	Document doc = df.getOwnerDocument();
    	int pos = 0;
    	for (org.w3c.dom.Text t : texts) {
    		String v = t.getNodeValue();
    		if (v==null || v.length()==0) continue;
    		int[] cps = v.codePoints().toArray();
    		java.util.List<Node> replacement = new java.util.ArrayList<>();
    		StringBuilder seg = new StringBuilder();
    		boolean segSmall = mask[pos];
    		for (int cp : cps) {
    			boolean small = mask[pos++];
    			if (small!=segSmall) {
    				flushSmallCapsSegment(doc, replacement, seg, segSmall);
    				segSmall = small;
    			}
    			seg.appendCodePoint(cp);
    		}
    		flushSmallCapsSegment(doc, replacement, seg, segSmall);
    		if (replacement.size()==1 && replacement.get(0) instanceof org.w3c.dom.Text) continue; // nothing to wrap
    		Node parent = t.getParentNode();
    		if (parent==null) continue;
    		for (Node n : replacement) parent.insertBefore(n, t);
    		parent.removeChild(t);
    	}
    	return fragment;
    }

    private void flushSmallCapsSegment(Document doc, java.util.List<Node> out, StringBuilder seg, boolean small) {
    	if (seg.length()==0) return;
    	if (small) {
    		Element inline = doc.createElementNS("http://www.w3.org/1999/XSL/Format", "fo:inline");
    		inline.setAttribute("font-size", SMALL_CAPS_SIZE);
    		inline.appendChild(doc.createTextNode(seg.toString()));
    		out.add(inline);
    	} else {
    		out.add(doc.createTextNode(seg.toString()));
    	}
    	seg.setLength(0);
    }

    private static void collectTextNodes(Node n, java.util.List<org.w3c.dom.Text> out) {
    	for (Node c = n.getFirstChild(); c!=null; c = c.getNextSibling()) {
    		if (c.getNodeType()==Node.TEXT_NODE) {
    			out.add((org.w3c.dom.Text)c);
    		} else {
    			collectTextNodes(c, out);
    		}
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
    	
    	PropertyResolver propertyResolver=null;
		try {
			propertyResolver = wordMLPackage.getMainDocumentPart().getPropertyResolver();
		} catch (Docx4JException e) {
			log.error(e.getMessage(), e);
		}
    	
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
    		try {
				pRPr = propertyResolver.getEffectiveRPr(pStyleId);
			} catch (CyclicStylesException e) {
				log.error(e.getMessage(), e);
			}
//        	log.debug("before getEffectiveRPrUsingPStyleRPr\n" + XmlUtils.marshaltoString(pRPr));
    	}

    	// Do we need boolean major??
    	// Can work that out from pStyle

    	
    	// now apply the direct rPr
    	try {
			rPr = propertyResolver.getEffectiveRPrUsingPStyleRPr(rPr, pRPr);
		} catch (CyclicStylesException e) {
			log.error(e.getMessage(), e);
		}
    	captureLineSpec(propertyResolver, pPr, rPr);
    	// TODO use effective rPr, but don't inherit theme val,
    	// TODO, add cache?

    	text = capsAndSoftHyphens(rPr, text);

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
		
		// Symbol handling
		// @since 11.5.5
		if (rFonts.getHAnsi()!=null) {  
			String actualFontName = rFonts.getHAnsi();
			if (actualFontName.equals("Symbol") || actualFontName.equals("Webdings") || actualFontName.equals("Wingdings") || actualFontName.equals("Wingdings 2") || actualFontName.equals("Wingdings 3") ) {
				// For these fonts, we depart from the general approach outline in the class comment above,
				// and map the char to a known Unicode replacement.
    			Element	span = createElement(document);
    			if (span!=null) {
    				// It will be null in MainDocumentPart$FontAndStyleFinder case
	    			document.appendChild(span); 
	    			
	    			StringBuffer sb = new StringBuffer();
	    			
	    			text.codePoints().forEach(cp -> 
		    			{
		    				String valStr = null;
		    				
		    				// VBA like rng.InsertAfter Chr(i); rng.Font.Name = "Wingdings"
		    				// for code points 128-159 (0x80-0x9F) results in Unicode you might not expect (rather than the code point asked for).
		    				// This is because these are used in the Windows-1252 codepage but are reserved in Unicode for 
		    				// control characters: https://en.wikipedia.org/wiki/Windows-1252
		    				// For example, codepoint 137 (0x89) gets translated to U+2030.	
		    				// See further https://github.com/plutext/docx4j/issues/632
		    				if (cp>255) {
		    					cp = translateUnicode2SingleByte(cp);
		    				}
		    				
		    				if (cp>255 /* couldn't translate using explicit mapping */ ) {
		    					// Word will also do the following...
		    					int mappedIndex = cp - 0xF000;
		    					valStr = SymbolMapper.getUnicodeReplacementChar(actualFontName, (short)mappedIndex);
		    					if (log.isDebugEnabled()) {
		    						log.debug("Mapped char: " + actualFontName + " " + (short)cp + " Hex " + Integer.toHexString(cp) + " to " + (short)mappedIndex + " Hex " + Integer.toHexString(mappedIndex));
		    					}
		    				} /* usual case */ else {
		    					valStr = SymbolMapper.getUnicodeReplacementChar(actualFontName, (short)cp);
		    				}
							if (valStr==null) {
								sb.append(SymbolUtils.MISSING_SYMBOL); 
								log.warn(actualFontName + " " + (short)cp + " Hex " + Integer.toHexString(cp) + " has no replacement.");
								
							} else {
								sb.append(valStr);  						
							}
		    			}
	    			);
	    			
	    			span.setTextContent(sb.toString());  
	    			this.symbolSetAttribute(span, actualFontName, span.getTextContent() ); 
    			}
    			if (outputType== RunFontActionType.DISCOVERY) {
    				vis.fontAction(actualFontName);
    			}
    			
    			return result(document);
				
			}
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
		String cs = null;

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

		if (rFonts.getCstheme()!=null
				&& getThemePart()!=null) {
			try {
				cs = getThemePart().getFont(rFonts.getCstheme(), themeFontLang);
			} catch (Docx4JException e) {
				log.error(e.getMessage(), e);
			}
			if (cs==null) {
				cs = rFonts.getCs();
			}
		} else {
			// No theme, so
			cs = rFonts.getCs();
		}
		if (cs!=null && cs.trim().length()==0) {
			// eg LibreOffice writes w:cs="" in docDefaults
			cs = null;
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
	    		 eastAsia,  ascii,  hAnsi,  cs );
    }
    
    private int translateUnicode2SingleByte(int cp) {

		switch (cp) {
		case 0x20AC: return 0x80;
		case 0x201A: return 0x82;
		case 0x0192: return 0x83;
		case 0x201E: return 0x84;
		case 0x2026: return 0x85;
		case 0x2020: return 0x86;
		case 0x2021: return 0x87;
		case 0x02C6: return 0x88;
		case 0x2030: return 0x89;
		case 0x0160: return 0x8A;
		case 0x2039: return 0x8B;
		case 0x0152: return 0x8C;
		case 0x017D: return 0x8E;
		case 0x2018: return 0x91;
		case 0x2019: return 0x92;
		case 0x201C: return 0x93;
		case 0x201D: return 0x94;
		case 0x2022: return 0x95;
		case 0x2013: return 0x96;
		case 0x2014: return 0x97;
		case 0x02DC: return 0x98;
		case 0x2122: return 0x99;
		case 0x0161: return 0x9A;
		case 0x203A: return 0x9B;
		case 0x0153: return 0x9C;
		case 0x017E: return 0x9E;
		case 0x0178: return 0x9F;		
		default: return cp;
		}
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
    		String eastAsia, String ascii, String hAnsi, String cs) {
    	
//    	String hAnsi = hAnsiActual;
//		if (hAnsi==null) {
//			log.debug("No value for hAnsi, using default font");
//			hAnsi = this.getDefaultFont();				
//		}
    	
    	// See http://stackoverflow.com/questions/196830/what-is-the-easiest-best-most-correct-way-to-iterate-through-the-characters-of-a
    	// and http://stackoverflow.com/questions/8894258/fastest-way-to-iterate-over-all-the-chars-in-a-string
    	
    	/* The range dispatch below follows the table in [MS-OI29500] section 17.3.2.26 (see
    	 * the class javadoc for the reference), validated against it on 2026-08-19.
    	 * Known deliberate divergences from that table:
    	 * - the Indic, Thai, Lao, Myanmar and Khmer ranges use cs, not the table's hAnsi,
    	 *   because that is what Word actually does (issues 666 and 622);
    	 * - U+2190-U+2BFF glyph-checks the font and substitutes a symbol font where the
    	 *   glyph is missing, which is beyond the table (observed Word 2016 behaviour);
    	 * - Hebrew/Arabic U+0590-U+07BF use a Times New Roman heuristic where the table
    	 *   says ascii (real bidi runs take the cs path before reaching this method);
    	 * - hint=eastAsia sends U+03D0-U+03FF and U+27C0-U+2E7F to eastAsia, where the
    	 *   table (by omission) says hAnsi - contrived cases, kept for continuity;
    	 * - the table's preamble rule (where eastAsia is "Times New Roman" and ascii equals
    	 *   hAnsi, use ascii) is not implemented.
    	 * The table covers 0000-FFFF; we also handle astral characters (those outside the
    	 * Unicode Basic Multilingual Plane). */
    	
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
							if (hasGlyph(hAnsi, c)) {
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
        	    		// (NB the [MS-OI29500] table says ascii for 0590-07BF)
						if (hasGlyph("Times New Roman", c)) {
							vis.fontAction("Times New Roman");        	    		
						}
						
					} catch (ExecutionException e) {
						log.error(e.getMessage(), e);
					}
        	    	
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	currentRangeLower = '\u0590';
        	    	currentRangeUpper = '\u07BF';
        	    }
        	    /* The Indic, Thai, Lao, Myanmar and Khmer ranges below are complex
        	     * script ranges not listed in the [MS-OI29500] table
        	     * (which says hAnsi for unlisted ranges), but Word formats them
        	     * with the cs (or cstheme if defined) font.  See issues 666 and 622.
        	     * Setting currentRange also keeps consecutive characters in a
        	     * single span, which FOP needs in order to shape them correctly. */
        	    else if (c>='\u0900' && c<='\u0DFF')
        	    {
        	    	// The Indic scripts: Devanagari, Bengali, Gurmukhi, Gujarati,
        	    	// Oriya, Tamil, Telugu, Kannada, Malayalam, Sinhala
        	    	// (contiguous blocks, and the font action is the same,
        	    	// so one range suffices)
    				vis.fontAction(cs==null ? hAnsi : cs);
        	    	vis.addCharacterToCurrent(c);

        	    	currentRangeLower = '\u0900';
        	    	currentRangeUpper = '\u0DFF';
        	    }
        	    else if (c>='\u0E00' && c<='\u0E7F')
        	    {
        	    	// Thai
    				vis.fontAction(cs==null ? hAnsi : cs);
        	    	vis.addCharacterToCurrent(c);

        	    	currentRangeLower = '\u0E00';
        	    	currentRangeUpper = '\u0E7F';
        	    }
        	    else if (c>='\u0E80' && c<='\u0EFF')
        	    {
        	    	// Lao
    				vis.fontAction(cs==null ? hAnsi : cs);
        	    	vis.addCharacterToCurrent(c);

        	    	currentRangeLower = '\u0E80';
        	    	currentRangeUpper = '\u0EFF';
        	    }
        	    else if (c>='\u1000' && c<='\u109F')
        	    {
        	    	// Myanmar
    				vis.fontAction(cs==null ? hAnsi : cs);
        	    	vis.addCharacterToCurrent(c);

        	    	currentRangeLower = '\u1000';
        	    	currentRangeUpper = '\u109F';
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
        	    }
        	    else if (c>='\u1780' && c<='\u17FF')
        	    {
        	    	// Khmer; see comment above (issue 666)
    				vis.fontAction(cs==null ? hAnsi : cs);
        	    	vis.addCharacterToCurrent(c);

        	    	currentRangeLower = '\u1780';
        	    	currentRangeUpper = '\u17FF';
        	    }
        	    else if (c>='\u19E0' && c<='\u19FF')
        	    {
        	    	// Khmer Symbols
    				vis.fontAction(cs==null ? hAnsi : cs);
        	    	vis.addCharacterToCurrent(c);

        	    	currentRangeLower = '\u19E0';
        	    	currentRangeUpper = '\u19FF';
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
        	    /* U+2000-U+218F: General Punctuation (the curly quotes, the en and em dashes,
        	     * the ellipsis, the bullet), Superscripts and Subscripts, Currency Symbols,
        	     * Combining Diacritical Marks for Symbols, Letterlike Symbols, Number Forms.
        	     * Ordinary text, which Word renders in the run's own font, so no glyph check
        	     * here (until 17.0.4 this range was handled with the symbol blocks below, so a
        	     * quotation mark the font lacked was set in a symbol font).  The whole-block
        	     * hint=eastAsia handling matches the [MS-OI29500] table: it has no
        	     * per-character exception lists for these blocks. */
        	    else if (c>='\u2000' && c<='\u218F')
        	    {
        	    	if (hint == STHint.EAST_ASIA) {
        				vis.fontAction(eastAsia);
        	    	} else {
        	    		// Usual case
        	    		vis.fontAction(hAnsi);
        	    	}
        	    	vis.addCharacterToCurrent(c);

        	    	currentRangeLower = '\u2000';
        	    	currentRangeUpper = '\u218F';
        	    }
        	    /* U+2190-U+2BFF: the symbol blocks - Arrows, Mathematical Operators,
        	     * Miscellaneous Technical, Control Pictures, OCR, Enclosed Alphanumerics,
        	     * Box Drawing, Block Elements, Geometric Shapes, Miscellaneous Symbols,
        	     * Dingbats, the supplemental arrow/math blocks, Braille.  Here a text font
        	     * often lacks the glyph, so ask, and look for a substitute where it hasn't.
        	     * (The substitution is beyond the [MS-OI29500] table, which just says hAnsi,
        	     * or eastAsia on hint; it reflects observed Word 2016 behaviour.) */
        	    else if (c>='\u2190' && c<='\u2BFF')
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
        						if (hasGlyph(hAnsi, c)) {
        							vis.fontAction(hAnsi);
        						} else {

        							// Note: what follows is based on what Word 2016
        							// does for Calibri 0x2751 (checkbox)
    								// but TODO explore what it does for the other symbol blocks

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
	                	    			/* In the discovery pass we are only collecting font names, and
	                	    			 * nothing can be resolved yet anyway: fontsInUse() runs before
	                	    			 * processEmbeddings and populateFontMappings (see
	                	    			 * WordprocessingMLPackage.setFontMapper), so every font looks
	                	    			 * missing.  The conversion pass makes the real decision.
	                	    			 * See the TODO in WordprocessingMLPackage.setFontMapper.
	                	    			 * @since 17.0.3 */
	                	    			String msg = "TODO: how to handle char '" + c + "' (0x" + Integer.toHexString(c)
	                	    					+ ") in range c>='\\u2190' && c<='\\u2BFF'? hAnsi=" + hAnsi
	                	    					+ ", which maps to " + physicalFontFor(hAnsi);
	                	    			if (outputType==RunFontActionType.DISCOVERY) {
	                	    				log.debug(msg + " (discovery pass; ignore)");
	                	    			} else {
	                	    				log.warn(msg);
	                	    			}
	        						}
        						}

        					} catch (ExecutionException e) {
        						log.error(e.getMessage(), e);
        					}
        	    		}         	    	}
        	    	vis.addCharacterToCurrent(c);

        	    	currentRangeLower = '\u2190';
        	    	currentRangeUpper = '\u2BFF';
        	    }
        	    /* U+2C00-U+2EFF: scripts and punctuation again - Glagolitic, Latin
        	     * Extended-C, Coptic, Georgian Supplement, Tifinagh, Ethiopic Extended,
        	     * Cyrillic Extended-A, Supplemental Punctuation, CJK Radicals Supplement -
        	     * so ordinary text handling, as for U+2000-U+218F above. */
        	    else if (c>='\u2C00' && c<='\u2EFF')
        	    {
        	    	if (hint == STHint.EAST_ASIA) {
        				vis.fontAction(eastAsia);
        	    	} else {
        	    		// Usual case
        	    		vis.fontAction(hAnsi);
        	    	}
        	    	vis.addCharacterToCurrent(c);

        	    	currentRangeLower = '\u2C00';
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
        	    	// Per [MS-OI29500] section 17.3.2.26,
        	    	// for all ranges not listed in the above, the hAnsi (or hAnsiTheme if defined) font shall be used.
        	    	String hex = String.format("%04x", (int) c);
        	    	log.debug("Defaulting to hAnsi for char " + hex);
    				vis.fontAction(hAnsi); 
    				debugCheckGlyph(hAnsi, c);
    				
        	    	vis.addCharacterToCurrent(c);
        	    	
        	    	/* Every character of this gap takes this branch and so gets hAnsi:
        	    	 * remember the whole gap, so that the next character of the same
        	    	 * script joins this span.  (Until 17.0.5 the range was reset to
        	    	 * 0000-0000, which no character matches, so a Georgian or Ethiopic
        	    	 * word became one fo:inline per character - and FOP kerns and
        	    	 * letter-spaces within an inline, not across two.) */
        	    	char[] gap = defaultRange(c);
        	    	currentRangeLower = gap[0];
        	    	currentRangeUpper = gap[1];
        	    	
        	    }
    	    }
    	} 
    	
    	// Handle final span
    	vis.finishPrevious();
    	return finish(vis.getResult());
    }

    /* The ranges the dispatch above tests, in order.  Anything outside them falls
     * through to its final else, which always uses hAnsi. */
    private static final char[][] LISTED_RANGES = {
    	{'\u0000','\u007F'}, {'\u00A0','\u00FF'}, {'\u0100','\u02AF'}, {'\u02B0','\u04FF'},
    	{'\u0590','\u07BF'}, {'\u0900','\u0DFF'}, {'\u0E00','\u0E7F'}, {'\u0E80','\u0EFF'},
    	{'\u1000','\u109F'}, {'\u1100','\u11FF'}, {'\u1780','\u17FF'}, {'\u19E0','\u19FF'},
    	{'\u1E00','\u1EFF'}, {'\u2000','\u218F'}, {'\u2190','\u2BFF'}, {'\u2C00','\u2EFF'},
    	{'\u2F00','\uDFFF'}, {'\uE000','\uF8FF'}, {'\uF900','\uFAFF'}, {'\uFB00','\uFB4F'},
    	{'\uFB50','\uFDFF'}, {'\uFE30','\uFE6F'}, {'\uFE70','\uFEFE'}, {'\uFF00','\uFFEF'}
    };

    /**
     * The gap between the ranges [MS-OI29500] 17.3.2.26 lists (see
     * {@link #LISTED_RANGES}) that this character falls in: the run of characters
     * which all take the dispatch's final else and so all get hAnsi, and which can
     * therefore share one span.  Georgian (U+10A0-U+10FF), Armenian (U+0530-U+058F),
     * Tibetan, Ethiopic, Mongolian and Greek Extended are the common ones.
     *
     * @since 17.0.5
     */
    static char[] defaultRange(char c) {
    	char lower = '\u0000';
    	char upper = '\uFFFF';
    	for (char[] r : LISTED_RANGES) {
    		if (c >= r[0] && c <= r[1]) {
    			// listed after all: only this character (the caller's branch is not used)
    			return new char[] { c, c };
    		}
    		if (r[1] < c && r[1] >= lower) lower = (char)(r[1] + 1);
    		if (r[0] > c && r[0] <= upper) upper = (char)(r[0] - 1);
    	}
    	return new char[] { lower, upper };
    }
    
    /** The PhysicalFont this *document* font name maps to.
     *
     *  This must go via the Mapper, not PhysicalFonts: a font embedded in the document
     *  is deliberately not added to PhysicalFonts (those are shared by all documents;
     *  see ObfuscatedFontPart.extract), and a document font is commonly mapped to a
     *  substitute with a different name (eg Arial to Arimo Regular), which
     *  PhysicalFonts.get(documentFontName) wouldn't find either.
     *
     * @since 17.0.3
     */
    private PhysicalFont physicalFontFor(String documentFontName) {

    	if (documentFontName==null) return null;
    	Mapper fontMapper = wordMLPackage.getFontMapper();
    	PhysicalFont pf = (fontMapper==null ? null : fontMapper.get(documentFontName));
    	return (pf!=null ? pf : PhysicalFonts.get(documentFontName));
    }

    /** Whether the font this document font name maps to has a glyph for c; false if
     *  there is no such font, so that the caller falls back as it would have done.
     *
     * @since 17.0.3
     */
    private boolean hasGlyph(String documentFontName, char c) throws ExecutionException {

    	PhysicalFont pf = physicalFontFor(documentFontName);
    	if (pf==null) {
    		log.debug("No physical font for " + documentFontName);
    		return false;
    	}
    	return GlyphCheck.hasChar(pf, c);
    }

    private void debugCheckGlyph(String fontName, char c) {
    	
		if (log.isDebugEnabled()) {
	    	try {
				if (!hasGlyph(fontName, c)) {
//					Throwable t = new Throwable();
//					log.debug("FIXME", t);
					log.debug(fontName + "'s PhysicalFont is missing char " + c);
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
		PhysicalFont pf = physicalFontResolved(fontName);
		return pf==null ? null : pf.getName();
	}

	/** The physical font this document font name resolves to (via the CJK alias where
	 *  there is one), or null. */
	private PhysicalFont physicalFontResolved(String fontName) {
		
		log.debug("looking for: " + fontName);
//		if (log.isDebugEnabled()) {
//			Throwable t = new Throwable();
//			log.debug("Call stack", t);
//		}		
		
		PhysicalFont pf = wordMLPackage.getFontMapper().get(fontName);
		if (pf!=null) {
			log.debug("Font '" + fontName + "' maps to " + pf.getName() );
			return pf;
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
				log.debug("englishFromCJK==null");
				if (wordMLPackage.getFontMapper().size()>0) {
					log.warn("Font '" + fontName + "' is not mapped to a physical font. " );
				} else {
					log.info("No font mappings present");
				}
				return null;
			} else {
				pf = wordMLPackage.getFontMapper().get(englishFromCJK);
			}
			
			if (pf==null) {
				if (wordMLPackage.getFontMapper().size()>0) {
					log.warn("Font '" + englishFromCJK + "'  (from CJK)  is not mapped to a physical font. " );
				} else {
					log.info("No font mappings present");
				}
				return null;
			} else {
				log.debug(englishFromCJK + " (from CJK) maps to " + pf.getName() );				
			}
			
			return pf;
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
