/*
 *  Copyright 2007-2008, Plutext Pty Ltd.
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

    This file is a translation into Java of a portion of 
    Program.cs from Microsoft's OpenXmlViewer, which was made available to
    the project under the following license:
    
		(c) Microsoft Corporation
		
		Microsoft Public License (Ms-PL)
		
		This license governs use of the accompanying software. If you use the
		software, you accept this license. If you do not accept the license, do
		not use the software.
		
		1. Definitions
		
		The terms "reproduce," "reproduction," "derivative works," and "distribution"
		have the same meaning here as under U.S. copyright law.
		
		A "contribution" is the original software, or any additions or changes to the software.
		
		A "contributor" is any person that distributes its contribution under this license.
		
		"Licensed patents" are a contributor's patent claims that read directly on its contribution.
		
		2. Grant of Rights
		
		(A) Copyright Grant- Subject to the terms of this license, including the
		license conditions and limitations in section 3, each contributor
		grants you a non-exclusive, worldwide, royalty-free copyright license
		to reproduce its contribution, prepare derivative works of its
		contribution, and distribute its contribution or any derivative works
		that you create.
		
		(B) Patent Grant- Subject to the terms of this
		license, including the license conditions and limitations in section 3,
		each contributor grants you a non-exclusive, worldwide, royalty-free
		license under its licensed patents to make, have made, use, sell, offer
		for sale, import, and/or otherwise dispose of its contribution in the
		software or derivative works of the contribution in the software.
		
		3. Conditions and Limitations
		
		(A) No Trademark License- This license does not grant you rights to use any contributors' name, logo, or trademarks.
		
		(B) If you bring a patent claim against any contributor over patents that
		you claim are infringed by the software, your patent license from such
		contributor to the software ends automatically.
		
		(C) If you distribute any portion of the software, you must retain all copyright,
		patent, trademark, and attribution notices that are present in the
		software.
		
		(D) If you distribute any portion of the software in
		source code form, you may do so only under this license by including a
		complete copy of this license with your distribution. If you distribute
		any portion of the software in compiled or object code form, you may
		only do so under a license that complies with this license.
		
		(E) The software is licensed "as-is." You bear the risk of using it. The
		contributors give no express warranties, guarantees or conditions. You
		may have additional consumer rights under your local laws which this
		license cannot change. To the extent permitted under your local laws,
		the contributors exclude the implied warranties of merchantability,
		fitness for a particular purpose and non-infringement.

 */
package org.docx4j.model.listnumbering;

import java.math.BigInteger;

import org.docx4j.model.PropertyResolver;
import org.docx4j.openpackaging.exceptions.CyclicStylesException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.RPr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Emulator {
	
	/* There should be only one Emulator object per 
	 * WordprocessingML package.  It is set on the 
	 * numbering part.
	 * 
	 * TODO 2011 02 23: in addition to having numbering in the 
	 * Main document part, you can have numbering in other 
	 * stories:
	 *   - headers/footers
	 *   - comments
	 *   - footnotes/endnotes
	 * This means that ListLevel should have independent counters
	 * for each story, or the there should be a ListLevel defined
	 * for each story! 
	 * 
	 */
	
	protected static Logger log = LoggerFactory.getLogger(Emulator.class);
			
    public Emulator()
    {
    }
    

    /**
     * @param wmlPackage
     * @param pPr
     * @return
     * @since 3.0.1
     */
    public static ResultTriple getNumber(WordprocessingMLPackage wmlPackage, PPr pPr) {
    	return getNumber(wmlPackage, pPr, null);
    }

    /**
     * The next number for a paragraph, counted in the given state: a traversal's
     * own, one per story (see {@link NumberingStates}), so that two exports at
     * once do not interleave and a footer's list does not continue the body's.
     *
     * @param state the counters to increment; null for the numbering part's
     *        default state, which is what the no-state overloads use
     * @since 17.1.1 (CR-014 phase 4)
     */
    public static ResultTriple getNumber(WordprocessingMLPackage wmlPackage, PPr pPr, NumberingState state) {
    	
		if (pPr==null) return null;
		// a paragraph naming no style is resolved against the default paragraph
		// style, which may be numbered (CR-014 P6, measured; before 17.1.1 it was
		// assumed not to be)

		String pStyleVal = null;
		if (pPr.getPStyle()!=null) {
			pStyleVal = pPr.getPStyle().getVal();
		}
		String numIdStr = null;
		String levelIdStr = null;
		
		if (pPr.getNumPr()!=null) {
			if (pPr.getNumPr().getNumId()!=null) {
				BigInteger numId = pPr.getNumPr().getNumId().getVal();
				if (numId!=null) numIdStr = numId.toString();
			}
			if (pPr.getNumPr().getIlvl()!=null) {
				BigInteger levelId = pPr.getNumPr().getIlvl().getVal();
				if (levelId!=null) levelIdStr = levelId.toString();
			}
		}
			
		return getNumber( wmlPackage,  pStyleVal, numIdStr,  levelIdStr,
				numIdStr != null && !numIdStr.equals(""), state);

    }

    /**
     * What {@link #getNumber(WordprocessingMLPackage, PPr, NumberingState)} would
     * return, without taking the number: the given state is left as it was.
     *
     * @param state the traversal's state; null for the numbering part's default
     * @since 17.1.1
     */
    public static ResultTriple peek(WordprocessingMLPackage wmlPackage, PPr pPr, NumberingState state) {
    	if (state == null) {
    		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart ndp =
    				wmlPackage.getMainDocumentPart().getNumberingDefinitionsPart();
    		if (ndp == null) return null;
    		state = ndp.getNumberingState();
    	}
    	return getNumber(wmlPackage, pPr, state.copy());
    }
    
    /* Get the computed list number for the given list at this point in the
     * document.
     */
    public static ResultTriple getNumber(WordprocessingMLPackage wmlPackage, String pStyleVal,
    		String numId, String levelId) {
    	// a numId given here is the paragraph's own unless the caller says otherwise
    	return getNumber(wmlPackage, pStyleVal, numId, levelId, numId != null && !numId.equals(""));
    }

    /**
     * @param directNumPr whether the {@code w:numId} is the paragraph's own direct
     *        formatting, rather than one its paragraph style contributed.  A level
     *        which names a paragraph style of its own numbers only that style
     *        (&#xa7;2.8): see {@link #styleLinkedElsewhere}.
     * @since 17.1.0
     */
    public static ResultTriple getNumber(WordprocessingMLPackage wmlPackage, String pStyleVal,
    		String numId, String levelId, boolean directNumPr) {
    	return getNumber(wmlPackage, pStyleVal, numId, levelId, directNumPr, null);
    }

    /**
     * @param state the counters to increment (a traversal's own; see
     *        {@link NumberingStates}); null for the numbering part's default state
     * @since 17.1.1 (CR-014 phase 4)
     */
    public static ResultTriple getNumber(WordprocessingMLPackage wmlPackage, String pStyleVal,
    		String numId, String levelId, boolean directNumPr, NumberingState state) {


    	org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart numberingPart =
    		wmlPackage.getMainDocumentPart().getNumberingDefinitionsPart();
    	
    	if (numberingPart==null) {
    		return null;
    	}

//    	org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart stylesPart =
//        		wmlPackage.getMainDocumentPart().getStyleDefinitionsPart();
//    	numberingPart.setStyleDefinitionsPart(stylesPart);
    	
    	Emulator em = numberingPart.getEmulator();
    	if (state == null) state = numberingPart.getNumberingState();
    	
    	// Object to hold results
    	ResultTriple triple = new ResultTriple();    	
    	    	
    	PropertyResolver propertyResolver;
		try {
			propertyResolver = wmlPackage.getMainDocumentPart().getPropertyResolver();
		} catch (Docx4JException e) {
			log.error(e.getMessage(),e);
			return null;
		}
    	    	
    	NumRef ref = resolve(numberingPart, propertyResolver, pStyleVal, numId, levelId, directNumPr);
    	if (ref.notNumbered) {
    		log.debug(ref.reason);
    		return null;
    	}
    	numId = ref.numId;
    	levelId = ref.ilvl;

		if (numberingPart.getInstanceListDefinitions().containsKey(numId)
				&& numberingPart.getInstanceListDefinitions().get(numId).levelExists(
						levelId)) {

			numberingPart.getInstanceListDefinitions().get(numId).incrementCounter(
					levelId, state);
			triple.numString = numberingPart.getInstanceListDefinitions().get(numId)
					.getCurrentNumberString(levelId, state);
			
			if (log.isDebugEnabled()) log.debug("Got number: " + triple.numString);

			String font = numberingPart.getInstanceListDefinitions().get(numId)
					.getFont(levelId);

			if (font != null && !font.equals("")) {
				triple.numFont = font;
			}

			if (numberingPart.getInstanceListDefinitions().get(numId).isBullet(levelId)) {
				//triple.isBullet = true;
				triple.bullet = numberingPart.getInstanceListDefinitions().get(numId).getLevel(levelId).getLevelText();
			}

			/* The indent the instance's own level definition gives
			 * (w:num/w:lvlOverride/w:lvl, ECMA-376 17.9.8), which until 17.1.0 was lost:
			 * only the abstract level was read here, so the list-block had neither
			 * indent nor hanging indent to work from.  Measured on a document whose
			 * w:num 32 overrides ilvl 0 with <w:ind w:left="397" w:hanging="113"/>:
			 * Word puts the bullet at 14.2pt and the item's text at 19.85pt, where
			 * docx4j fell back to the paragraph's first tab stop and gave the label a
			 * width of 453.6pt, running the text off the page and turning Word's two
			 * pages into four.  Where the override states no indent the abstract
			 * level's stands, as NumberingDefinitionsPart.getInd resolves it.
			 *
			 * The override's *formatting* (its w:rPr) is deliberately not taken: the
			 * label and the item's text share one rPr here, so a level w:rPr carrying
			 * w:b sets the whole paragraph bold, where Word draws the number bold and
			 * the text after it in the regular face (measured).  Splitting the two is a
			 * change for every numbered paragraph, not only for the 20 documents of the
			 * three corpora which carry a w:lvlOverride/w:lvl, so it is left for a
			 * batch of its own.  @since 17.1.0 */
			ListLevel listLevel = numberingPart.getInstanceListDefinitions().get(numId).getLevel(levelId);
			triple.lvl = listLevel.getJaxbAbstractLvl();

			triple.ind = indOf(listLevel.getJaxbOverrideLvl());
			if (triple.ind==null) triple.ind = indOf(triple.getLvl());

			triple.rPr = (triple.getLvl()==null) ? null : triple.getLvl().getRPr();

			/* The label's own formatting: the override level's rPr where the w:num
			 * overrides this level with one, else the abstract level's.  A replacement,
			 * not a merge - measured on CR-014 probe P3 (2026-09-12): abstract w:i under
			 * an override w:b + w:sz 36 gives Word a bold 18pt label with no italic
			 * anywhere.  getRPr() above stays the abstract level's, as it always was.
			 * @since 17.1.1 */
			Lvl overrideLvl = listLevel.getJaxbOverrideLvl();
			triple.labelRPr = (overrideLvl != null && overrideLvl.getRPr() != null)
					? overrideLvl.getRPr() : triple.rPr;
			
		} else if (!numberingPart.getInstanceListDefinitions().containsKey(numId)){
			
			if (numId.equals("0")) {
				// By convention, in Word this generally means turn off numbering
				log.debug("Couldn't find list " + numId);
			} else {
				log.warn("Couldn't find list " + numId);
//				Throwable t = new Throwable();
//				t.printStackTrace();
			}
			
		} else if (!numberingPart.getInstanceListDefinitions().get(numId).levelExists(
				levelId)){
			
			log.error("Couldn't find level " + levelId + " in list " + numId);					
		}
		return triple;
    }

    
    /**
     * Whether this level belongs to a <em>different</em> paragraph style than the one
     * which brought the numbering to this paragraph, in which case Word paints no label
     * and does not count the paragraph.
     *
     * <p>ECMA-376 17.9.24's {@code w:pStyle} inside a {@code w:lvl} names the paragraph
     * style the level is linked to.  Where a paragraph reaches that level through a
     * {@code w:numPr} its <em>style</em> declares, and that style is not the one the
     * level names (nor a style it is based on), the level does not apply.  Measured on
     * {@code numbering-label-ilvl0}, whose level 0 of numId 20 is linked to the style
     * "NumLinked": a paragraph using a second style which carries the same
     * {@code w:numPr} gets no number at all from Word, and the next paragraph of the
     * list is numbered 6 where docx4j had counted it and reached 7.  Paragraphs using
     * the linked style itself are numbered, and so is a paragraph whose <em>own</em>
     * {@code w:numPr} names a numbering whose level is linked to a style it does not
     * use - direct formatting always applies.</p>
     *
     * @since 17.1.0
     */
    private static boolean styleLinkedElsewhere(
    		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart numberingPart,
    		PropertyResolver propertyResolver, String numId, String levelId, String pStyleVal) {

    	String linked = numberingPart.getLinkedStyleId(numId, levelId);
    	if (linked == null || linked.equals("")) return false;
    	if (pStyleVal == null) return true;
    	// the style itself, or any style it is based on, is the level's own
    	java.util.Set<String> seen = new java.util.HashSet<String>();
    	String id = pStyleVal;
    	while (id != null && seen.add(id)) {
    		if (linked.equals(id)) return false;
    		org.docx4j.wml.Style s = propertyResolver == null ? null : propertyResolver.getStyle(id);
    		id = (s == null || s.getBasedOn() == null) ? null : s.getBasedOn().getVal();
    	}
    	return true;
    }

    /**
     * The indent a paragraph's numbering level contributes, resolved the way
     * {@link #getNumber} resolves the level itself (same numId and ilvl, from the
     * paragraph's own {@code w:numPr} or its style chain) and read as
     * {@link org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart#getInd(String, String)}
     * reads it: the level's own {@code w:ind} first, then the linked style's, following
     * {@code w:basedOn}.  Null where the paragraph is not numbered or the level states
     * none.  Used in HTML output (XsltHTMLFunctions).
     *
     * <p>Before 17.1.1 this had its own copy of the resolution, which read the raw
     * style's {@code w:pPr} and followed {@code w:basedOn} only when the style carried
     * a {@code w:numPr} without a {@code w:numId}, so a style numbered purely through
     * its base got a number and no indent; and it read only the level's own
     * {@code w:ind}, never the linked style's.</p>
     *
     * @since 3.0.0
     */
    public static Ind getInd(WordprocessingMLPackage wmlPackage, String pStyleVal,
    		String numId, String levelId) {

    	org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart numberingPart =
    		wmlPackage.getMainDocumentPart().getNumberingDefinitionsPart();
    	if (numberingPart==null) {
    		return null;
    	}
    	PropertyResolver propertyResolver;
    	try {
    		propertyResolver = wmlPackage.getMainDocumentPart().getPropertyResolver();
		} catch (Docx4JException e) {
			log.error(e.getMessage(),e);
			return null;
		}
    	NumRef ref = resolve(numberingPart, propertyResolver, pStyleVal, numId, levelId,
    			numId != null && !numId.equals(""));
    	if (ref.notNumbered) {
    		log.debug(ref.reason);
    		return null;
    	}
    	return numberingPart.getInd(ref.numId, ref.ilvl);
    }

    /**
     * Where a paragraph's numbering comes from once its own {@code w:numPr} and its
     * style chain have been read: the numId and ilvl to count with, whether that numId
     * is the paragraph's own direct formatting, and whether Word numbers the paragraph
     * at all.  {@link #getNumber} and {@link #getInd} share one resolution,
     * {@link Emulator#resolve}.
     *
     * @since 17.1.1 (CR-014 phase 3)
     */
    public static final class NumRef {
    	/** The list, or null when not numbered. */
    	public final String numId;
    	/** The level (never null when numbered; "0" where nothing states one). */
    	public final String ilvl;
    	/** Whether the numId is the paragraph's own {@code w:numPr}, as opposed to one
    	 *  its paragraph style contributed. */
    	public final boolean direct;
    	/** Word paints no label and does not count the paragraph: nothing names a list
    	 *  (no {@code w:numPr}, and no paragraph style - not even the default - carrying
    	 *  one), or the level is linked to a different paragraph style than the one that
    	 *  brought the numbering (&#xa7;2.8, {@link Emulator#styleLinkedElsewhere}). */
    	public final boolean notNumbered;
    	/** Why not numbered; null otherwise. */
    	public final String reason;

    	NumRef(String numId, String ilvl, boolean direct) {
    		this.numId = numId; this.ilvl = ilvl; this.direct = direct;
    		this.notNumbered = false; this.reason = null;
    	}
    	NumRef(String reason) {
    		this.numId = null; this.ilvl = null; this.direct = false;
    		this.notNumbered = true; this.reason = reason;
    	}
    	@Override
    	public String toString() {
    		return notNumbered ? "not numbered: " + reason
    				: "numId " + numId + " ilvl " + ilvl + (direct ? " (direct)" : " (from style)");
    	}
    }

    /**
     * The one resolution of numId and ilvl for a paragraph, from its own
     * {@code w:numPr} where it has one and from the effective properties of its
     * paragraph style otherwise - the default paragraph style when it names none,
     * since that style may be numbered (measured, CR-014 P6) - then the &#xa7;2.8 rule
     * for a style-contributed level linked to another style.
     *
     * @param numId the paragraph's numId, or null/empty to read the style
     * @param levelId the paragraph's ilvl, or null/empty for the style's, else "0"
     * @param directNumPr whether a numId given here is the paragraph's own
     * @since 17.1.1
     */
    static NumRef resolve(
    		org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart numberingPart,
    		PropertyResolver propertyResolver, String pStyleVal, String numId, String levelId,
    		boolean directNumPr) {

    	String styleId = (pStyleVal == null || pStyleVal.equals("")) ? null : pStyleVal;

    	if (numId == null || numId.equals("")) {
    		// no explicit numId: is it provided by the style (ie does this style, or
    		// the default paragraph style, have a list associated with it)?
    		directNumPr = false;
    		if (styleId == null) {
    			styleId = propertyResolver == null ? null : propertyResolver.getDefaultParagraphStyleId();
    			if (styleId == null) {
    				return new NumRef("no numId, no paragraph style and no default paragraph style");
    			}
    		}
    		PPr ppr;
    		try {
    			ppr = propertyResolver.getEffectivePPr(styleId);
    		} catch (CyclicStylesException e) {
    			log.error(e.getMessage(), e);
    			return new NumRef("cyclic styles at " + styleId);
    		}
    		if (ppr == null) {
    			return new NumRef("style '" + styleId + "' has no pPr");
    		}
    		NumPr numPr = ppr.getNumPr();
    		if (numPr == null) {
    			// no numbering set on the style either; that's ok
    			return new NumRef("no numId, and style '" + styleId + "' is not numbered");
    		}
    		if (numPr.getNumId() == null || numPr.getNumId().getVal() == null) {
    			log.error("style '" + styleId + "' has a w:numPr without a w:numId val");
    			return new NumRef("style '" + styleId + "' has a w:numPr without a w:numId val");
    		}
    		numId = numPr.getNumId().getVal().toString();
    		if (levelId == null || levelId.equals("")) {
    			// w:ilvl (and its w:val) is optional; its absence means level 0
    			if (numPr.getIlvl() != null && numPr.getIlvl().getVal() != null) {
    				levelId = numPr.getIlvl().getVal().toString();
    				log.debug("levelId=" + levelId + " (from style)");
    			} else {
    				levelId = "0";
    			}
    		}
    	}
    	if (log.isDebugEnabled()) log.debug("Using numId: " + numId);

    	if (levelId == null || levelId.equals("")) {
    		log.warn("No level id?! Default to 0.");
    		levelId = "0";
    	}

    	if (!directNumPr && styleLinkedElsewhere(numberingPart, propertyResolver, numId, levelId, styleId)) {
    		return new NumRef("level " + levelId + " of numId " + numId + " is linked to a paragraph style other than '"
    				+ styleId + "'");
    	}
    	return new NumRef(numId, levelId, directNumPr);
    }

    /** A level definition's w:ind, or null (the level, its w:pPr or its w:ind absent).
     *  @since 17.1.0 */
    private static Ind indOf(Lvl lvl) {
    	if (lvl==null) return null;
    	PPr ppr = lvl.getPPr();
    	return (ppr==null) ? null : ppr.getInd();
    }
    
//    public ListLevel getListNumberingDefinition(NumberingDefinitionsPart numberingPart, NumPr numPr) {
//    	
//		if (numPr.getNumId()==null) {
//			return null; 	    			
//		}
//		
//		String numId = null;
//		if (numPr.getNumId()==null) {
//			log.error("numId was null or empty!");
//			return null;
//		} else {
//			numId = numPr.getNumId().getVal().toString();
//		}
//		
//		String levelId = "0";
//		if (numPr.getIlvl() != null ) {
//			levelId = numPr.getIlvl().getVal().toString();
//		}
//	
//		// Get the list
//		ListNumberingDefinition listNumberingDefinition
//			= numberingPart.getInstanceListDefinitions().get(numId);
//    	
//		if (listNumberingDefinition==null) {
//			return null;
//		} else {
//			return listNumberingDefinition.getLevel(levelId);
//		}
//    }
    
    
    /**
     * What a paragraph is numbered with: the label text (or the bullet), the level
     * definition it came from, the indent and the rPr that level contributes.  The
     * {@code getNumber} overloads return its deprecated subclass {@link ResultTriple}
     * until 17.2, so assign to either.
     *
     * @since 17.1.1 (CR-014 phase 5; the class was {@code ResultTriple}, a name that had
     *        become a misnomer)
     */
    public static class NumberingResult {
    	
    	String numString;
		public String getNumString() {
			return numString;
		}
    	
    	String numFont;
    	@Deprecated
		public String getNumFont() {
			return numFont;
		}
    	
//    	boolean isBullet = false;
//		public boolean isBullet() {
//			return isBullet;
//		}
		
		String bullet = null;
		public String getBullet() {
			return bullet;
		}
		
		Ind ind = null;
    	/**
    	 * Use getLvl().getPPr().getInd() instead
    	 * @return
    	 */
    	@Deprecated // consider whether to add getPpr and access via that.  
		public Ind getIndent() {
    		// set from getLvl().getPPr().getInd() 
			return ind;
		}
		
	    
	    RPr rPr;
	    /**
	     * The abstract level's rPr (never the override's: see {@link #getLabelRPr()}).
	     * 
	     * @return
	     */
	    public RPr getRPr() {
			return rPr;
		}

	    RPr labelRPr;
	    /**
	     * The rPr the label is drawn with: the {@code w:lvlOverride/w:lvl}'s where the
	     * instance overrides this level with one carrying an rPr, else the abstract
	     * level's - one or the other, as Word applies them (CR-014 probe P3).  It
	     * formats the number alone, never the paragraph's text (ECMA-376 17.9.24).
	     *
	     * @since 17.1.1
	     */
	    public RPr getLabelRPr() {
			return labelRPr;
		}
	    
	    Lvl lvl;
		/**
		 * @return
		 * @since 3.2.0
		 */
		public Lvl getLvl() {
			return lvl;
		}
    }

    /**
     * The old name of {@link NumberingResult}, kept as an empty subclass so that
     * {@code Emulator.ResultTriple} still compiles.  Static since 17.1.1 (it was an
     * inner class); the {@code getNumber} overloads keep returning it for one release.
     *
     * @deprecated since 17.1.1, use {@link NumberingResult}.  Removal no earlier than 17.2.
     */
    @Deprecated
    public static class ResultTriple extends NumberingResult {
    }

}
