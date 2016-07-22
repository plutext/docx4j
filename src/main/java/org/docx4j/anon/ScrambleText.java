package org.docx4j.anon;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.JAXBElement;

import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.fonts.GlyphCheck;
import org.docx4j.fonts.RunFontSelector;
import org.docx4j.fonts.RunFontSelector.RunFontActionType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.vml.CTTextPath;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTFFData;
import org.docx4j.wml.CTFFName;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.DelText;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.SdtBlock;
import org.docx4j.wml.SdtElement;
import org.docx4j.wml.SdtPr;
import org.docx4j.wml.Text;
import org.jvnet.jaxb2_commons.ppp.Child;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

/**
 * This will replace Latin (eg English) text with lorem ipsum stuff;
 * non Latin text will be randomised. 
 * 
 * @author jharrop
 *
 */
public class ScrambleText extends CallbackImpl {
	
	private static Logger log = LoggerFactory.getLogger(ScrambleText.class);
	
	private ScrambleText() {}
	
	public ScrambleText(WordprocessingMLPackage pkg) {
		this.pkg = pkg;
		vis = new RunFontCharVisitorMinimal();
		rfs = new RunFontSelector(pkg, new /* dummy */ RunFontCharVisitorMinimal(), RunFontActionType.DISCOVERY);
//		langStats = new HashMap<String, Integer>();
	}
	
	private static Lorem lorem = LoremIpsum.getInstance();
	
	private WordprocessingMLPackage pkg;
	
	private RunFontSelector rfs = null;  
	private RunFontCharVisitorMinimal vis;
	
	String latinText = null;
	int beginIndex;
	
	Random random = new Random();
	
	PPr ppr = null;
	RPr rpr = null;
	
	boolean hasGreek = false;
	boolean hasCyrillic = false;
	boolean hasHebrew = false;
	boolean hasArabic = false;
	boolean hasHiragana = false;
	boolean hasKatakana = false;
	boolean hasCJK = false;
	
	int field_begin_counter = 0;
	int bookmark_start_counter = 0;

	@Override
	public void walkJAXBElements(Object parent) {
		
		List children = getChildren(parent);
		if (children != null) {

			for (Object o2 : children) {
				
				Object o = XmlUtils.unwrap(o2);
				
				// Need this, for proper SDT processing
				if (o instanceof Child) {
					if (parent instanceof SdtBlock) {
						((Child)o).setParent( ((SdtBlock)parent).getSdtContent() );
					} else if (parent instanceof List){
						// Do nothing
						if (log.isDebugEnabled()) {
							log.debug("Unknown parent for " + o.getClass().getName());
						}
					} else {
						((Child)o).setParent(parent);
					}
				}
				
				// Process the wrapped object								
				this.apply(o2);

				if (this.shouldTraverse(o)) {
					walkJAXBElements(o);
				}

			}
		}
	}	
	
	@Override
	public List<Object> apply(Object o) {
		
		if (o instanceof JAXBElement
				&& ((JAXBElement)o).getName().getLocalPart().equals("instrText")) {
				
			Text t = (Text)XmlUtils.unwrap(o);	
			String instr = t.getValue(); 
			log.debug(instr);
			
			if ( instr.contains("MERGEFIELD") ) {

				// eg <w:instrText xml:space="preserve"> MERGEFIELD  Kundenstrasse \* MERGEFORMAT </w:instrText>
				// or <w:instrText xml:space="preserve"> MERGEFIELD  Kundenstrasse</w:instrText>

				// we'll preseve the MERGEFIELD tag, but change the rest
				
				
				int start = instr.indexOf("MERGEFIELD") + 10;
				beginIndex += start;					
				
				String toProcess = instr.substring(start);
				
				System.out.println(toProcess);
				int tLen = toProcess.length();
							
				t.setValue( instr.substring(0, start) +
						unicodeRangeToFont(
								toProcess, 
								latinText.substring(beginIndex, beginIndex+tLen)));
				
				beginIndex += tLen;					
				
				return null;
				
			} else if (instr.contains("FORMCHECKBOX")
					|| instr.contains("FORMTEXT")
					|| instr.contains("PAGE")) {
				
				// leave as is
				return null;			
				
			} else {
				System.out.println("TO don't scramble: " + instr);
			}
			// TODO others eg REF (bookmark .. maintain integrity?)
			
		}


		o = XmlUtils.unwrap(o);	
		
		if (o instanceof org.docx4j.wml.FldChar) {
			FldChar fldChar = (FldChar)o;
			if (fldChar.getFldCharType().equals(STFldCharType.BEGIN) ) {
				field_begin_counter++;
				if (fldChar.getFfData()!=null) {
					CTFFData ffData = fldChar.getFfData();
					for (JAXBElement<?> el :  ffData.getNameOrEnabledOrCalcOnExit() ) {
						Object jObj = el.getValue();
						if (jObj instanceof CTFFName) {
							
							String name = ((CTFFName)jObj).getVal();
							//System.out.println("BEGIN " + name);

//							int tLen = name.length();							
//							// We didn't count these characters initially, so make more text
//							latinText += generateReplacement(tLen);
//							
//							((CTFFName)jObj).setVal(unicodeRangeToFont(
//									name, 
//									latinText.substring(tLen)));
//							
//							beginIndex += tLen;
//							// Hmmm, do we need to worry about duplicate random field names?
//							// Word 2010 is happy with spaces in the name
							
							((CTFFName)jObj).setVal("fieldname" + field_begin_counter);
							
						}
					}
				}
			}
			return null;
		}
		
		if (o instanceof CTBookmark) {
			CTBookmark bookmarkStart = (CTBookmark)o;
			bookmark_start_counter++;
			if (bookmarkStart.getName()!=null) {
				bookmarkStart.setName("bm" + bookmark_start_counter);
				
//				String name = bookmarkStart.getName().trim();
//				int tLen = name.length();							
//				
//				if (tLen>0) {
//					
//					// We didn't count these characters initially, so make more text
//					latinText += generateReplacement(tLen);
//					
//					bookmarkStart.setName(unicodeRangeToFont(
//							name, 
//							latinText.substring(tLen)));
//					
//					beginIndex += tLen;
//					//Word will convert spaces to underscore, and remove duplicates
//				}
			}
			return null;
		}
		
		
//		System.out.println(o.getClass().getName());
		
		if (o instanceof SdtElement) {
			// Remove databinding, tag, if any
			SdtPr sdtPr = ((SdtElement)o).getSdtPr();
			sdtPr.setDataBinding(null);
			sdtPr.setTag(null);
			return null;			
		}
		
		if (o instanceof P) {
			
			P p = (P)o;
			ppr = p.getPPr();
			
			StringWriter out = new StringWriter();
			try {
				TextUtils.extractText(p, out);
			} catch (Exception e) {
				e.printStackTrace();
			}

			latinText = generateReplacement(out.toString().length());
			beginIndex = 0;

			log.debug("latinText:" + latinText);
			
			return null;
		}
		
		if (o instanceof Text) {
						
			Text t = (Text)o;
			log.debug(t.getValue());
			int tLen = t.getValue().length();
			
			
			if (true) {
				t.setValue(
						unicodeRangeToFont(
								t.getValue(), 
								latinSubstring(tLen)));
						
			} 
//			else /* debug */ {
//				
//				String result = unicodeRangeToFont(
//						t.getValue(), 
//						latinText.substring(beginIndex, beginIndex+tLen));
//
//				System.out.println(t.getValue() + " --> " + result); 
//						
//				t.setValue(result);
//			}
			
			beginIndex += tLen;
			
		} else if ( o instanceof org.docx4j.wml.DelText) {

			DelText t = (DelText)o;
			int tLen = t.getValue().length();
			t.setValue(
					unicodeRangeToFont(t.getValue(), 
							latinSubstring(tLen)));
			beginIndex += tLen;
			
		} // org.docx4j.wml.RunIns is handled OK
		
		else if (o instanceof org.docx4j.dml.CTRegularTextRun
				&& ((CTRegularTextRun)o).getT()!=null) {
			
			CTRegularTextRun t = (CTRegularTextRun)o;
			int tLen = t.getT().length();
			t.setT(
					unicodeRangeToFont(t.getT(), 
							latinSubstring(tLen)));
			beginIndex += tLen;			

		} else if ( o instanceof org.docx4j.vml.CTTextPath) {
			
			CTTextPath t = (CTTextPath)o;
			
			if (t.getString()!=null) {
				
				int tLen = t.getString().length();
				String tmpLatin = generateReplacement(tLen);
				t.setString(
						unicodeRangeToFont(t.getString(), tmpLatin));				
			}
			
			
		} else {
//			System.out.println(o.getClass().getName());
		}
		
		return null;
	}
	
	private String latinSubstring(int tLen) {

		// Sanity check
		if ((beginIndex+tLen) > latinText.length() ) {

			System.out.println("Not enough characters!");
			//System.out.println(t.getValue());
			
			// Not much point trying to fix latinText itself?
			return generateReplacement(tLen);
		} 
		
		return latinText.substring(beginIndex, beginIndex+tLen);
	}
	
	
	private String generateReplacement(int slenRqd) {
		
		StringBuffer replacement = new StringBuffer();
		int len = 0;
		
		do
		{
			// A bit of effort to get enough text
			
			int wordsNeeded = Math.round((slenRqd-len)/8) + 1; // always at least one word!
			String latin = lorem.getWords(wordsNeeded,wordsNeeded);
			len += latin.length();
			replacement.append(latin);
			
//			System.out.println(len + ", " + slenRqd);
								
		} while (len < slenRqd);

		return replacement.toString();
	}
	
	
	private static final int MAX_GLYPH_RETRIES = 10;
	
	private char getRandom(char rangeLower, char rangeUpper)  {
		
		boolean glyphOK;
		int tries = 0;
		
		char result;
		
		do {
			glyphOK = false;
			
			result = (char)(rangeLower + random.nextInt((int)rangeUpper-(int)rangeLower));
			
			if (font!=null) {
    			try {
					glyphOK = GlyphCheck.hasChar(font, result);
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				
			}
			
			tries++;
		} while (font!=null && !glyphOK && tries<MAX_GLYPH_RETRIES);
		
//		if (!glyphOK) {
//			// This will usually be because there is no physical font present
//			throw new RuntimeException("Ran out of patience getting glyph in font " + font);
//		}
    	
		return result;
		
	}

	String font = null;  // TODO add fontCache
	
    private String unicodeRangeToFont(String text, String latinText)  {
    	
    	// Check for exceptional case
    	if (latinText.length()<text.length()) {
    		
        	/*
    			<w:txbxContent>
    				<w:p >
    					<w:r><w:t>blah blah </w:t>
    					</w:r>
    				</w:p>
    			</w:txbxContent>
    			
    			</v:textbox></v:roundrect></w:pict>
    			
    			</mc:Fallback></mc:AlternateContent>
    			
    			</w:r>


    			<w:r w:rsidR="00CC5C27">
    				<w:t>zzz</w:t>
    			</w:r>

        	 * 
        	 */    		
    		latinText = generateReplacement(text.length());
    	}
    	
    	font = null;
    	
	    vis.createNew();
    	        	    	
    	if (text==null) {
    		log.warn("text==null; returning...");
    		return null; 
    	}
    	for (int i = 0; i < text.length(); i=text.offsetByCodePoints(i, 1)){
    		
    	    char c = text.charAt(i);
    	    
    	    
    	    log.debug(Integer.toHexString(c));
    	    
    	    if (Character.isHighSurrogate(c)) {

    		    log.info("high");
				vis.addCodePointToCurrent(text.codePointAt(i));
				        	    	
    	    }
    	    else if (c==' ' ) {
    	    	
    	    	// Add it to existing
    	    	vis.addCharacterToCurrent(c);
    	    	
    	    } else {
    		    
//    		    System.out.println(c);    		    

            	// Need to be able to check glyph exists in the relevant font.
            	// (so that ff it doesn't, we choose another randomly)
    	    	// To do this, we need to know which font.  For that, we basically 
    	    	// just test 1 char.
//    	    	try {
	    	    	if (font==null) {
	        	    	rfs.fontSelector(ppr, rpr, String.valueOf(c));
	        	    	font = vis.getFontname();
	        	    	if (font==null) {
	        	    		log.debug("still no font!");            	    		
	        	    	} 
	        	    }
//    	    	} catch (Exception e) {
//    	    		e.printStackTrace();
//    	    	}

	    		log.debug("      " + c);    		    
	    	    	
    		    /* .. Basic Latin
    		     * 
    		     * http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/rFonts.html says 
    		     * @ascii (or @asciiTheme) is used to format all characters in the ASCII range 
    		     * (0 - 127)
    		     */
        	    if (c>='\u0041' && c<='\u005A') // A-Z 
        	    {
        	    	try {
        	    		vis.addCharacterToCurrent( latinText.substring(i, i+1).charAt(0));
        	    	} catch (java.lang.StringIndexOutOfBoundsException e) {
        	    		System.out.println(latinText +  "( len " + latinText.length() + ") is too short ");
        	    		throw e;
        	    	}
        	    	
        	    } else if (c>='\u0061' && c<='\u007A') // a-z 
            	{
        	    	try {
        	    		vis.addCharacterToCurrent( latinText.substring(i, i+1).charAt(0));        	    			
        	    	} catch (java.lang.StringIndexOutOfBoundsException e) {
        	    		System.out.println(latinText +  "( len " + latinText.length() + ") is too short ");
        	    		throw e;
        	    	}            	    	
        	    } else if (c>='\u0000' && c<='\u007F') 
            	    {
            	    	vis.addCharacterToCurrent( c );
            	    	
        	    } else 
        	    	
	           if (c >= '\u0080' && c <= '\u00FF') {
					// Latin-1 Supplement
					//c = getRandom('\u0080', '\u00FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0100' && c <= '\u017F') {
					// Latin Extended-A
					c = getRandom('\u0100', '\u017F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0180' && c <= '\u024F') {
					// Latin Extended-B
					c = getRandom('\u0180', '\u024F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0250' && c <= '\u02AF') {
					// IPA Extensions
					c = getRandom('\u0250', '\u02AF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u02B0' && c <= '\u02FF') {
					// Spacing Modifier Letters
					c = getRandom('\u02B0', '\u02FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0300' && c <= '\u036F') {
					// Combining Diacritical Marks
					c = getRandom('\u0300', '\u036F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0370' && c <= '\u03FF') {
					// Greek and Coptic
					c = getRandom('\u0370', '\u03FF');
					vis.addCharacterToCurrent(c);
					hasGreek = true;
				} else if (c >= '\u0400' && c <= '\u04FF') {
					// Cyrillic
					c = getRandom('\u0400', '\u04FF');
					vis.addCharacterToCurrent(c);
					hasCyrillic = true;
				} else if (c >= '\u0500' && c <= '\u052F') {
					// Cyrillic Supplement
					c = getRandom('\u0500', '\u052F');
					vis.addCharacterToCurrent(c);
					hasCyrillic = true;
				} else if (c >= '\u0530' && c <= '\u058F') {
					// Armenian
					c = getRandom('\u0530', '\u058F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0590' && c <= '\u05FF') {
					// Hebrew
					c = getRandom('\u0590', '\u05FF');
					vis.addCharacterToCurrent(c);
					hasHebrew = true;
				} else if (c >= '\u0600' && c <= '\u06FF') {
					// Arabic
					c = getRandom('\u0600', '\u06FF');
					vis.addCharacterToCurrent(c);
					hasArabic = true;
				} else if (c >= '\u0700' && c <= '\u074F') {
					// Syriac
					c = getRandom('\u0700', '\u074F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0750' && c <= '\u077F') {
					// Arabic Supplement
					c = getRandom('\u0750', '\u077F');
					vis.addCharacterToCurrent(c);
					hasArabic = true;
				} else if (c >= '\u0780' && c <= '\u07BF') {
					// Thaana
					c = getRandom('\u0780', '\u07BF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u07C0' && c <= '\u07FF') {
					// NKo
					c = getRandom('\u07C0', '\u07FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0800' && c <= '\u083F') {
					// Samaritan
					c = getRandom('\u0800', '\u083F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0840' && c <= '\u085F') {
					// Mandaic
					c = getRandom('\u0840', '\u085F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u08A0' && c <= '\u08FF') {
					// Arabic Extended-A
					c = getRandom('\u08A0', '\u08FF');
					vis.addCharacterToCurrent(c);
					hasArabic = true;					
				} else if (c >= '\u0900' && c <= '\u097F') {
					// Devanagari
					c = getRandom('\u0900', '\u097F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0980' && c <= '\u09FF') {
					// Bengali
					c = getRandom('\u0980', '\u09FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0A00' && c <= '\u0A7F') {
					// Gurmukhi
					c = getRandom('\u0A00', '\u0A7F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0A80' && c <= '\u0AFF') {
					// Gujarati
					c = getRandom('\u0A80', '\u0AFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0B00' && c <= '\u0B7F') {
					// Oriya
					c = getRandom('\u0B00', '\u0B7F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0B80' && c <= '\u0BFF') {
					// Tamil
					c = getRandom('\u0B80', '\u0BFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0C00' && c <= '\u0C7F') {
					// Telugu
					c = getRandom('\u0C00', '\u0C7F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0C80' && c <= '\u0CFF') {
					// Kannada
					c = getRandom('\u0C80', '\u0CFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0D00' && c <= '\u0D7F') {
					// Malayalam
					c = getRandom('\u0D00', '\u0D7F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0D80' && c <= '\u0DFF') {
					// Sinhala
					c = getRandom('\u0D80', '\u0DFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0E00' && c <= '\u0E7F') {
					// Thai
					c = getRandom('\u0E00', '\u0E7F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0E80' && c <= '\u0EFF') {
					// Lao
					c = getRandom('\u0E80', '\u0EFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u0F00' && c <= '\u0FFF') {
					// Tibetan
					c = getRandom('\u0F00', '\u0FFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1000' && c <= '\u109F') {
					// Myanmar
					c = getRandom('\u1000', '\u109F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u10A0' && c <= '\u10FF') {
					// Georgian
					c = getRandom('\u10A0', '\u10FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1100' && c <= '\u11FF') {
					// Hangul Jamo
					c = getRandom('\u1100', '\u11FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1200' && c <= '\u137F') {
					// Ethiopic
					c = getRandom('\u1200', '\u137F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1380' && c <= '\u139F') {
					// Ethiopic Supplement
					c = getRandom('\u1380', '\u139F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u13A0' && c <= '\u13FF') {
					// Cherokee
					c = getRandom('\u13A0', '\u13FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1400' && c <= '\u167F') {
					// Unified Canadian Aboriginal Syllabics
					c = getRandom('\u1400', '\u167F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1680' && c <= '\u169F') {
					// Ogham
					c = getRandom('\u1680', '\u169F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u16A0' && c <= '\u16FF') {
					// Runic
					c = getRandom('\u16A0', '\u16FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1700' && c <= '\u171F') {
					// Tagalog
					c = getRandom('\u1700', '\u171F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1720' && c <= '\u173F') {
					// Hanunoo
					c = getRandom('\u1720', '\u173F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1740' && c <= '\u175F') {
					// Buhid
					c = getRandom('\u1740', '\u175F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1760' && c <= '\u177F') {
					// Tagbanwa
					c = getRandom('\u1760', '\u177F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1780' && c <= '\u17FF') {
					// Khmer
					c = getRandom('\u1780', '\u17FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1800' && c <= '\u18AF') {
					// Mongolian
					c = getRandom('\u1800', '\u18AF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u18B0' && c <= '\u18FF') {
					// Unified Canadian Aboriginal Syllabics Extended
					c = getRandom('\u18B0', '\u18FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1900' && c <= '\u194F') {
					// Limbu
					c = getRandom('\u1900', '\u194F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1950' && c <= '\u197F') {
					// Tai Le
					c = getRandom('\u1950', '\u197F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1980' && c <= '\u19DF') {
					// New Tai Lue
					c = getRandom('\u1980', '\u19DF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u19E0' && c <= '\u19FF') {
					// Khmer Symbols
					c = getRandom('\u19E0', '\u19FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1A00' && c <= '\u1A1F') {
					// Buginese
					c = getRandom('\u1A00', '\u1A1F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1A20' && c <= '\u1AAF') {
					// Tai Tham
					c = getRandom('\u1A20', '\u1AAF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1AB0' && c <= '\u1AFF') {
					// Combining Diacritical Marks Extended
					c = getRandom('\u1AB0', '\u1AFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1B00' && c <= '\u1B7F') {
					// Balinese
					c = getRandom('\u1B00', '\u1B7F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1B80' && c <= '\u1BBF') {
					// Sundanese
					c = getRandom('\u1B80', '\u1BBF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1BC0' && c <= '\u1BFF') {
					// Batak
					c = getRandom('\u1BC0', '\u1BFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1C00' && c <= '\u1C4F') {
					// Lepcha
					c = getRandom('\u1C00', '\u1C4F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1C50' && c <= '\u1C7F') {
					// Ol Chiki
					c = getRandom('\u1C50', '\u1C7F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1CC0' && c <= '\u1CCF') {
					// Sundanese Supplement
					c = getRandom('\u1CC0', '\u1CCF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1CD0' && c <= '\u1CFF') {
					// Vedic Extensions
					c = getRandom('\u1CD0', '\u1CFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1D00' && c <= '\u1D7F') {
					// Phonetic Extensions
					c = getRandom('\u1D00', '\u1D7F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1D80' && c <= '\u1DBF') {
					// Phonetic Extensions Supplement
					c = getRandom('\u1D80', '\u1DBF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1DC0' && c <= '\u1DFF') {
					// Combining Diacritical Marks Supplement
					c = getRandom('\u1DC0', '\u1DFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1E00' && c <= '\u1EFF') {
					// Latin Extended Additional
					c = getRandom('\u1E00', '\u1EFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u1F00' && c <= '\u1FFF') {
					// Greek Extended
					c = getRandom('\u1F00', '\u1FFF');
					vis.addCharacterToCurrent(c);
					hasGreek = true;
				} else if (c >= '\u2000' && c <= '\u206F') {
					// General Punctuation
					c = getRandom('\u2000', '\u206F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2070' && c <= '\u209F') {
					// Superscripts and Subscripts
					c = getRandom('\u2070', '\u209F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u20A0' && c <= '\u20CF') {
					// Currency Symbols
					c = getRandom('\u20A0', '\u20CF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u20D0' && c <= '\u20FF') {
					// Combining Diacritical Marks for Symbols
					c = getRandom('\u20D0', '\u20FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2100' && c <= '\u214F') {
					// Letterlike Symbols
					c = getRandom('\u2100', '\u214F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2150' && c <= '\u218F') {
					// Number Forms
					c = getRandom('\u2150', '\u218F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2190' && c <= '\u21FF') {
					// Arrows
					c = getRandom('\u2190', '\u21FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2200' && c <= '\u22FF') {
					// Mathematical Operators
					c = getRandom('\u2200', '\u22FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2300' && c <= '\u23FF') {
					// Miscellaneous Technical
					c = getRandom('\u2300', '\u23FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2400' && c <= '\u243F') {
					// Control Pictures
					c = getRandom('\u2400', '\u243F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2440' && c <= '\u245F') {
					// Optical Character Recognition
					c = getRandom('\u2440', '\u245F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2460' && c <= '\u24FF') {
					// Enclosed Alphanumerics
					c = getRandom('\u2460', '\u24FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2500' && c <= '\u257F') {
					// Box Drawing
					c = getRandom('\u2500', '\u257F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2580' && c <= '\u259F') {
					// Block Elements
					c = getRandom('\u2580', '\u259F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u25A0' && c <= '\u25FF') {
					// Geometric Shapes
					c = getRandom('\u25A0', '\u25FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2600' && c <= '\u26FF') {
					// Miscellaneous Symbols
					c = getRandom('\u2600', '\u26FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2700' && c <= '\u27BF') {
					// Dingbats
					c = getRandom('\u2700', '\u27BF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u27C0' && c <= '\u27EF') {
					// Miscellaneous Mathematical Symbols-A
					c = getRandom('\u27C0', '\u27EF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u27F0' && c <= '\u27FF') {
					// Supplemental Arrows-A
					c = getRandom('\u27F0', '\u27FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2800' && c <= '\u28FF') {
					// Braille Patterns
					c = getRandom('\u2800', '\u28FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2900' && c <= '\u297F') {
					// Supplemental Arrows-B
					c = getRandom('\u2900', '\u297F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2980' && c <= '\u29FF') {
					// Miscellaneous Mathematical Symbols-B
					c = getRandom('\u2980', '\u29FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2A00' && c <= '\u2AFF') {
					// Supplemental Mathematical Operators
					c = getRandom('\u2A00', '\u2AFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2B00' && c <= '\u2BFF') {
					// Miscellaneous Symbols and Arrows
					c = getRandom('\u2B00', '\u2BFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2C00' && c <= '\u2C5F') {
					// Glagolitic
					c = getRandom('\u2C00', '\u2C5F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2C60' && c <= '\u2C7F') {
					// Latin Extended-C
					c = getRandom('\u2C60', '\u2C7F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2C80' && c <= '\u2CFF') {
					// Coptic
					c = getRandom('\u2C80', '\u2CFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2D00' && c <= '\u2D2F') {
					// Georgian Supplement
					c = getRandom('\u2D00', '\u2D2F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2D30' && c <= '\u2D7F') {
					// Tifinagh
					c = getRandom('\u2D30', '\u2D7F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2D80' && c <= '\u2DDF') {
					// Ethiopic Extended
					c = getRandom('\u2D80', '\u2DDF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2DE0' && c <= '\u2DFF') {
					// Cyrillic Extended-A
					c = getRandom('\u2DE0', '\u2DFF');
					vis.addCharacterToCurrent(c);
					hasCyrillic = true;
				} else if (c >= '\u2E00' && c <= '\u2E7F') {
					// Supplemental Punctuation
					c = getRandom('\u2E00', '\u2E7F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2E80' && c <= '\u2EFF') {
					// CJK Radicals Supplement
					c = getRandom('\u2E80', '\u2EFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2F00' && c <= '\u2FDF') {
					// Kangxi Radicals
					c = getRandom('\u2F00', '\u2FDF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u2FF0' && c <= '\u2FFF') {
					// Ideographic Description Characters
					c = getRandom('\u2FF0', '\u2FFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u3000' && c <= '\u303F') {
					// CJK Symbols and Punctuation
					c = getRandom('\u3000', '\u303F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u3040' && c <= '\u309F') {
					// Hiragana
					c = getRandom('\u3040', '\u309F');
					vis.addCharacterToCurrent(c);
					hasHiragana = true;
				} else if (c >= '\u30A0' && c <= '\u30FF') {
					// Katakana
					c = getRandom('\u30A0', '\u30FF');
					vis.addCharacterToCurrent(c);
					hasKatakana = true;
				} else if (c >= '\u3100' && c <= '\u312F') {
					// Bopomofo
					c = getRandom('\u3100', '\u312F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u3130' && c <= '\u318F') {
					// Hangul Compatibility Jamo
					c = getRandom('\u3130', '\u318F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u3190' && c <= '\u319F') {
					// Kanbun
					c = getRandom('\u3190', '\u319F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u31A0' && c <= '\u31BF') {
					// Bopomofo Extended
					c = getRandom('\u31A0', '\u31BF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u31C0' && c <= '\u31EF') {
					// CJK Strokes
					c = getRandom('\u31C0', '\u31EF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u31F0' && c <= '\u31FF') {
					// Katakana Phonetic Extensions
					c = getRandom('\u31F0', '\u31FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u3200' && c <= '\u32FF') {
					// Enclosed CJK Letters and Months
					c = getRandom('\u3200', '\u32FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u3300' && c <= '\u33FF') {
					// CJK Compatibility
					c = getRandom('\u3300', '\u33FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u3400' && c <= '\u4DBF') {
					// CJK Unified Ideographs Extension A
					c = getRandom('\u3400', '\u4DBF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u4DC0' && c <= '\u4DFF') {
					// Yijing Hexagram Symbols
					c = getRandom('\u4DC0', '\u4DFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\u4E00' && c <= '\u9FFF') {
					// CJK Unified Ideographs
					c = getRandom('\u4E00', '\u9FFF');
					vis.addCharacterToCurrent(c);
					hasCJK = true;
				} else if (c >= '\uA000' && c <= '\uA48F') {
					// Yi Syllables
					c = getRandom('\uA000', '\uA48F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA490' && c <= '\uA4CF') {
					// Yi Radicals
					c = getRandom('\uA490', '\uA4CF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA4D0' && c <= '\uA4FF') {
					// Lisu
					c = getRandom('\uA4D0', '\uA4FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA500' && c <= '\uA63F') {
					// Vai
					c = getRandom('\uA500', '\uA63F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA640' && c <= '\uA69F') {
					// Cyrillic Extended-B
					c = getRandom('\uA640', '\uA69F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA6A0' && c <= '\uA6FF') {
					// Bamum
					c = getRandom('\uA6A0', '\uA6FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA700' && c <= '\uA71F') {
					// Modifier Tone Letters
					c = getRandom('\uA700', '\uA71F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA720' && c <= '\uA7FF') {
					// Latin Extended-D
					c = getRandom('\uA720', '\uA7FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA800' && c <= '\uA82F') {
					// Syloti Nagri
					c = getRandom('\uA800', '\uA82F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA830' && c <= '\uA83F') {
					// Common Indic Number Forms
					c = getRandom('\uA830', '\uA83F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA840' && c <= '\uA87F') {
					// Phags-pa
					c = getRandom('\uA840', '\uA87F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA880' && c <= '\uA8DF') {
					// Saurashtra
					c = getRandom('\uA880', '\uA8DF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA8E0' && c <= '\uA8FF') {
					// Devanagari Extended
					c = getRandom('\uA8E0', '\uA8FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA900' && c <= '\uA92F') {
					// Kayah Li
					c = getRandom('\uA900', '\uA92F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA930' && c <= '\uA95F') {
					// Rejang
					c = getRandom('\uA930', '\uA95F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA960' && c <= '\uA97F') {
					// Hangul Jamo Extended-A
					c = getRandom('\uA960', '\uA97F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA980' && c <= '\uA9DF') {
					// Javanese
					c = getRandom('\uA980', '\uA9DF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uA9E0' && c <= '\uA9FF') {
					// Myanmar Extended-B
					c = getRandom('\uA9E0', '\uA9FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uAA00' && c <= '\uAA5F') {
					// Cham
					c = getRandom('\uAA00', '\uAA5F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uAA60' && c <= '\uAA7F') {
					// Myanmar Extended-A
					c = getRandom('\uAA60', '\uAA7F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uAA80' && c <= '\uAADF') {
					// Tai Viet
					c = getRandom('\uAA80', '\uAADF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uAAE0' && c <= '\uAAFF') {
					// Meetei Mayek Extensions
					c = getRandom('\uAAE0', '\uAAFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uAB00' && c <= '\uAB2F') {
					// Ethiopic Extended-A
					c = getRandom('\uAB00', '\uAB2F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uAB30' && c <= '\uAB6F') {
					// Latin Extended-E
					c = getRandom('\uAB30', '\uAB6F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uAB70' && c <= '\uABBF') {
					// Cherokee Supplement
					c = getRandom('\uAB70', '\uABBF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uABC0' && c <= '\uABFF') {
					// Meetei Mayek
					c = getRandom('\uABC0', '\uABFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uAC00' && c <= '\uD7AF') {
					// Hangul Syllables
					c = getRandom('\uAC00', '\uD7AF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uD7B0' && c <= '\uD7FF') {
					// Hangul Jamo Extended-B
					c = getRandom('\uD7B0', '\uD7FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uD800' && c <= '\uDB7F') {
					// High Surrogates
					c = getRandom('\uD800', '\uDB7F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uDB80' && c <= '\uDBFF') {
					// High Private Use Surrogates
					c = getRandom('\uDB80', '\uDBFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uDC00' && c <= '\uDFFF') {
					// Low Surrogates
					c = getRandom('\uDC00', '\uDFFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uE000' && c <= '\uF8FF') {
					// Private Use Area
					c = getRandom('\uE000', '\uF8FF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uF900' && c <= '\uFAFF') {
					// CJK Compatibility Ideographs
					c = getRandom('\uF900', '\uFAFF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uFB00' && c <= '\uFB4F') {
					// Alphabetic Presentation Forms
					c = getRandom('\uFB00', '\uFB4F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uFB50' && c <= '\uFDFF') {
					// Arabic Presentation Forms-A
					c = getRandom('\uFB50', '\uFDFF');
					vis.addCharacterToCurrent(c);
					hasArabic = true;
					
				} else if (c >= '\uFE00' && c <= '\uFE0F') {
					// Variation Selectors
					c = getRandom('\uFE00', '\uFE0F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uFE10' && c <= '\uFE1F') {
					// Vertical Forms
					c = getRandom('\uFE10', '\uFE1F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uFE20' && c <= '\uFE2F') {
					// Combining Half Marks
					c = getRandom('\uFE20', '\uFE2F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uFE30' && c <= '\uFE4F') {
					// CJK Compatibility Forms
					c = getRandom('\uFE30', '\uFE4F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uFE50' && c <= '\uFE6F') {
					// Small Form Variants
					c = getRandom('\uFE50', '\uFE6F');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uFE70' && c <= '\uFEFF') {
					// Arabic Presentation Forms-B
					c = getRandom('\uFE70', '\uFEFF');
					vis.addCharacterToCurrent(c);
					hasArabic = true;
					
				} else if (c >= '\uFF00' && c <= '\uFFEF') {
					// Halfwidth and Fullwidth Forms
					c = getRandom('\uFF00', '\uFFEF');
					vis.addCharacterToCurrent(c);
				} else if (c >= '\uFFF0' && c <= '\uFFFF') {
					// Specials
					c = getRandom('\uFFF0', '\uFFFF');
					vis.addCharacterToCurrent(c);
	            	    	
//	            	    		            	    } else if (c>='\u10000' && c<='\u1007F') { //Linear B Syllabary
//	            	    		c = getRandom('\u10000' , '\u1007F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10080' && c<='\u100FF') { //Linear B Ideograms
//	            	    		c = getRandom('\u10080' , '\u100FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10100' && c<='\u1013F') { //Aegean Numbers
//	            	    		c = getRandom('\u10100' , '\u1013F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10140' && c<='\u1018F') { //Ancient Greek Numbers
//	            	    		c = getRandom('\u10140' , '\u1018F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10190' && c<='\u101CF') { //Ancient Symbols
//	            	    		c = getRandom('\u10190' , '\u101CF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u101D0' && c<='\u101FF') { //Phaistos Disc
//	            	    		c = getRandom('\u101D0' , '\u101FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10280' && c<='\u1029F') { //Lycian
//	            	    		c = getRandom('\u10280' , '\u1029F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u102A0' && c<='\u102DF') { //Carian
//	            	    		c = getRandom('\u102A0' , '\u102DF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u102E0' && c<='\u102FF') { //Coptic Epact Numbers
//	            	    		c = getRandom('\u102E0' , '\u102FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10300' && c<='\u1032F') { //Old Italic
//	            	    		c = getRandom('\u10300' , '\u1032F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10330' && c<='\u1034F') { //Gothic
//	            	    		c = getRandom('\u10330' , '\u1034F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10350' && c<='\u1037F') { //Old Permic
//	            	    		c = getRandom('\u10350' , '\u1037F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10380' && c<='\u1039F') { //Ugaritic
//	            	    		c = getRandom('\u10380' , '\u1039F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u103A0' && c<='\u103DF') { //Old Persian
//	            	    		c = getRandom('\u103A0' , '\u103DF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10400' && c<='\u1044F') { //Deseret
//	            	    		c = getRandom('\u10400' , '\u1044F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10450' && c<='\u1047F') { //Shavian
//	            	    		c = getRandom('\u10450' , '\u1047F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10480' && c<='\u104AF') { //Osmanya
//	            	    		c = getRandom('\u10480' , '\u104AF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10500' && c<='\u1052F') { //Elbasan
//	            	    		c = getRandom('\u10500' , '\u1052F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10530' && c<='\u1056F') { //Caucasian Albanian
//	            	    		c = getRandom('\u10530' , '\u1056F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10600' && c<='\u1077F') { //Linear A
//	            	    		c = getRandom('\u10600' , '\u1077F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10800' && c<='\u1083F') { //Cypriot Syllabary
//	            	    		c = getRandom('\u10800' , '\u1083F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10840' && c<='\u1085F') { //Imperial Aramaic
//	            	    		c = getRandom('\u10840' , '\u1085F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10860' && c<='\u1087F') { //Palmyrene
//	            	    		c = getRandom('\u10860' , '\u1087F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10880' && c<='\u108AF') { //Nabataean
//	            	    		c = getRandom('\u10880' , '\u108AF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u108E0' && c<='\u108FF') { //Hatran
//	            	    		c = getRandom('\u108E0' , '\u108FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10900' && c<='\u1091F') { //Phoenician
//	            	    		c = getRandom('\u10900' , '\u1091F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10920' && c<='\u1093F') { //Lydian
//	            	    		c = getRandom('\u10920' , '\u1093F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10980' && c<='\u1099F') { //Meroitic Hieroglyphs
//	            	    		c = getRandom('\u10980' , '\u1099F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u109A0' && c<='\u109FF') { //Meroitic Cursive
//	            	    		c = getRandom('\u109A0' , '\u109FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10A00' && c<='\u10A5F') { //Kharoshthi
//	            	    		c = getRandom('\u10A00' , '\u10A5F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10A60' && c<='\u10A7F') { //Old South Arabian
//	            	    		c = getRandom('\u10A60' , '\u10A7F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10A80' && c<='\u10A9F') { //Old North Arabian
//	            	    		c = getRandom('\u10A80' , '\u10A9F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10AC0' && c<='\u10AFF') { //Manichaean
//	            	    		c = getRandom('\u10AC0' , '\u10AFF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10B00' && c<='\u10B3F') { //Avestan
//	            	    		c = getRandom('\u10B00' , '\u10B3F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10B40' && c<='\u10B5F') { //Inscriptional Parthian
//	            	    		c = getRandom('\u10B40' , '\u10B5F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10B60' && c<='\u10B7F') { //Inscriptional Pahlavi
//	            	    		c = getRandom('\u10B60' , '\u10B7F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10B80' && c<='\u10BAF') { //Psalter Pahlavi
//	            	    		c = getRandom('\u10B80' , '\u10BAF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10C00' && c<='\u10C4F') { //Old Turkic
//	            	    		c = getRandom('\u10C00' , '\u10C4F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10C80' && c<='\u10CFF') { //Old Hungarian
//	            	    		c = getRandom('\u10C80' , '\u10CFF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u10E60' && c<='\u10E7F') { //Rumi Numeral Symbols
//	            	    		c = getRandom('\u10E60' , '\u10E7F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11000' && c<='\u1107F') { //Brahmi
//	            	    		c = getRandom('\u11000' , '\u1107F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11080' && c<='\u110CF') { //Kaithi
//	            	    		c = getRandom('\u11080' , '\u110CF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u110D0' && c<='\u110FF') { //Sora Sompeng
//	            	    		c = getRandom('\u110D0' , '\u110FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11100' && c<='\u1114F') { //Chakma
//	            	    		c = getRandom('\u11100' , '\u1114F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11150' && c<='\u1117F') { //Mahajani
//	            	    		c = getRandom('\u11150' , '\u1117F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11180' && c<='\u111DF') { //Sharada
//	            	    		c = getRandom('\u11180' , '\u111DF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u111E0' && c<='\u111FF') { //Sinhala Archaic Numbers
//	            	    		c = getRandom('\u111E0' , '\u111FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11200' && c<='\u1124F') { //Khojki
//	            	    		c = getRandom('\u11200' , '\u1124F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11280' && c<='\u112AF') { //Multani
//	            	    		c = getRandom('\u11280' , '\u112AF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u112B0' && c<='\u112FF') { //Khudawadi
//	            	    		c = getRandom('\u112B0' , '\u112FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11300' && c<='\u1137F') { //Grantha
//	            	    		c = getRandom('\u11300' , '\u1137F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11480' && c<='\u114DF') { //Tirhuta
//	            	    		c = getRandom('\u11480' , '\u114DF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11580' && c<='\u115FF') { //Siddham
//	            	    		c = getRandom('\u11580' , '\u115FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11600' && c<='\u1165F') { //Modi
//	            	    		c = getRandom('\u11600' , '\u1165F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11680' && c<='\u116CF') { //Takri
//	            	    		c = getRandom('\u11680' , '\u116CF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11700' && c<='\u1173F') { //Ahom
//	            	    		c = getRandom('\u11700' , '\u1173F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u118A0' && c<='\u118FF') { //Warang Citi
//	            	    		c = getRandom('\u118A0' , '\u118FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u11AC0' && c<='\u11AFF') { //Pau Cin Hau
//	            	    		c = getRandom('\u11AC0' , '\u11AFF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u12000' && c<='\u123FF') { //Cuneiform
//	            	    		c = getRandom('\u12000' , '\u123FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u12400' && c<='\u1247F') { //Cuneiform Numbers and Punctuation
//	            	    		c = getRandom('\u12400' , '\u1247F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u12480' && c<='\u1254F') { //Early Dynastic Cuneiform
//	            	    		c = getRandom('\u12480' , '\u1254F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u13000' && c<='\u1342F') { //Egyptian Hieroglyphs
//	            	    		c = getRandom('\u13000' , '\u1342F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u14400' && c<='\u1467F') { //Anatolian Hieroglyphs
//	            	    		c = getRandom('\u14400' , '\u1467F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u16800' && c<='\u16A3F') { //Bamum Supplement
//	            	    		c = getRandom('\u16800' , '\u16A3F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u16A40' && c<='\u16A6F') { //Mro
//	            	    		c = getRandom('\u16A40' , '\u16A6F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u16AD0' && c<='\u16AFF') { //Bassa Vah
//	            	    		c = getRandom('\u16AD0' , '\u16AFF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u16B00' && c<='\u16B8F') { //Pahawh Hmong
//	            	    		c = getRandom('\u16B00' , '\u16B8F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u16F00' && c<='\u16F9F') { //Miao
//	            	    		c = getRandom('\u16F00' , '\u16F9F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1B000' && c<='\u1B0FF') { //Kana Supplement
//	            	    		c = getRandom('\u1B000' , '\u1B0FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1BC00' && c<='\u1BC9F') { //Duployan
//	            	    		c = getRandom('\u1BC00' , '\u1BC9F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1BCA0' && c<='\u1BCAF') { //Shorthand Format Controls
//	            	    		c = getRandom('\u1BCA0' , '\u1BCAF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1D000' && c<='\u1D0FF') { //Byzantine Musical Symbols
//	            	    		c = getRandom('\u1D000' , '\u1D0FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1D100' && c<='\u1D1FF') { //Musical Symbols
//	            	    		c = getRandom('\u1D100' , '\u1D1FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1D200' && c<='\u1D24F') { //Ancient Greek Musical Notation
//	            	    		c = getRandom('\u1D200' , '\u1D24F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1D300' && c<='\u1D35F') { //Tai Xuan Jing Symbols
//	            	    		c = getRandom('\u1D300' , '\u1D35F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1D360' && c<='\u1D37F') { //Counting Rod Numerals
//	            	    		c = getRandom('\u1D360' , '\u1D37F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1D400' && c<='\u1D7FF') { //Mathematical Alphanumeric Symbols
//	            	    		c = getRandom('\u1D400' , '\u1D7FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1D800' && c<='\u1DAAF') { //Sutton SignWriting
//	            	    		c = getRandom('\u1D800' , '\u1DAAF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1E800' && c<='\u1E8DF') { //Mende Kikakui
//	            	    		c = getRandom('\u1E800' , '\u1E8DF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1EE00' && c<='\u1EEFF') { //Arabic Mathematical Alphabetic Symbols
//	            	    		c = getRandom('\u1EE00' , '\u1EEFF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1F000' && c<='\u1F02F') { //Mahjong Tiles
//	            	    		c = getRandom('\u1F000' , '\u1F02F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1F030' && c<='\u1F09F') { //Domino Tiles
//	            	    		c = getRandom('\u1F030' , '\u1F09F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1F0A0' && c<='\u1F0FF') { //Playing Cards
//	            	    		c = getRandom('\u1F0A0' , '\u1F0FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1F100' && c<='\u1F1FF') { //Enclosed Alphanumeric Supplement
//	            	    		c = getRandom('\u1F100' , '\u1F1FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1F200' && c<='\u1F2FF') { //Enclosed Ideographic Supplement
//	            	    		c = getRandom('\u1F200' , '\u1F2FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1F300' && c<='\u1F5FF') { //Miscellaneous Symbols and Pictographs
//	            	    		c = getRandom('\u1F300' , '\u1F5FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1F600' && c<='\u1F64F') { //Emoticons
//	            	    		c = getRandom('\u1F600' , '\u1F64F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1F650' && c<='\u1F67F') { //Ornamental Dingbats
//	            	    		c = getRandom('\u1F650' , '\u1F67F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1F680' && c<='\u1F6FF') { //Transport and Map Symbols
//	            	    		c = getRandom('\u1F680' , '\u1F6FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1F700' && c<='\u1F77F') { //Alchemical Symbols
//	            	    		c = getRandom('\u1F700' , '\u1F77F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1F780' && c<='\u1F7FF') { //Geometric Shapes Extended
//	            	    		c = getRandom('\u1F780' , '\u1F7FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1F800' && c<='\u1F8FF') { //Supplemental Arrows-C
//	            	    		c = getRandom('\u1F800' , '\u1F8FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u1F900' && c<='\u1F9FF') { //Supplemental Symbols and Pictographs
//	            	    		c = getRandom('\u1F900' , '\u1F9FF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u20000' && c<='\u2A6DF') { //CJK Unified Ideographs Extension B
//	            	    		c = getRandom('\u20000' , '\u2A6DF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u2A700' && c<='\u2B73F') { //CJK Unified Ideographs Extension C
//	            	    		c = getRandom('\u2A700' , '\u2B73F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u2B740' && c<='\u2B81F') { //CJK Unified Ideographs Extension D
//	            	    		c = getRandom('\u2B740' , '\u2B81F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u2B820' && c<='\u2CEAF') { //CJK Unified Ideographs Extension E
//	            	    		c = getRandom('\u2B820' , '\u2CEAF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u2F800' && c<='\u2FA1F') { //CJK Compatibility Ideographs Supplement
//	            	    		c = getRandom('\u2F800' , '\u2FA1F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\uE0000' && c<='\uE007F') { //Tags
//	            	    		c = getRandom('\uE0000' , '\uE007F');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\uE0100' && c<='\uE01EF') { //Variation Selectors Supplement
//	            	    		c = getRandom('\uE0100' , '\uE01EF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\uF0000' && c<='\uFFFFF') { //Supplementary Private Use Area-A
//	            	    		c = getRandom('\uF0000' , '\uFFFFF');
//	            	    		vis.addCharacterToCurrent(c);
//	            	    		            	    } else if (c>='\u100000' && c<='\u10FFFF') { //Supplementary Private Use Area-B
//	            	    		c = getRandom('\u100000' , '\u10FFFF');
//	            	    		vis.addCharacterToCurrent(c);
	            	    	
	            	    } else {
	        				
	                	    log.warn("TODO: handle " + Integer.toHexString(c));
	            	    	vis.addCharacterToCurrent(c);
	            	    	
	            	    	
        	    }
        	    
        	    
    	    }
    	} 
    	
    	return (String)vis.getResult();
    }

	
    
}
